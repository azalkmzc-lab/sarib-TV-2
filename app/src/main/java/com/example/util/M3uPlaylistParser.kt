package com.example.util

import android.util.Log
import com.example.data.model.ChannelCategory
import com.example.data.model.ChannelItem
import com.example.data.model.ContentType
import com.example.data.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class ParsedM3uResult(
    val categories: List<ChannelCategory>,
    val channels: List<ChannelItem>,
    val movieCategories: List<ChannelCategory> = emptyList(),
    val movies: List<MediaItem> = emptyList()
)

object M3uPlaylistParser {

    private const val TAG = "M3uPlaylistParser"
    private val YEAR_PATTERN: Pattern = Pattern.compile("\\b(19\\d{2}|20\\d{2})\\b")
    private val BRACKETS_PATTERN: Pattern = Pattern.compile("[()]|\\[\\]")
    private val PRESET_COLORS = listOf("#0088FF", "#00C8FF", "#2563EB", "#7C3AED", "#DC2626", "#059669", "#D97706", "#EC4899")

    /**
     * Builds an OkHttpClient with permissive SSL/TLS compatibility for older Android releases
     * and Android TV boxes.
     */
    private val httpClient: OkHttpClient by lazy {
        createCompatibleHttpClient()
    }

    private fun createCompatibleHttpClient(): OkHttpClient {
        return try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())

            OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .connectTimeout(6, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .build()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to create permissive SSL socket factory: ${e.message}")
            OkHttpClient.Builder()
                .connectTimeout(6, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .build()
        }
    }

    /**
     * Fast zero-allocation attribute extractor from M3U / M3U8 tags.
     * Supports attr="value", attr='value', and attr=value without compiling regex per line.
     */
    private fun extractAttribute(line: String, attrName: String): String {
        val key = "$attrName="
        val idx = line.indexOf(key, ignoreCase = true)
        if (idx == -1) return ""
        var start = idx + key.length
        if (start >= line.length) return ""
        val quote = line[start]
        return if (quote == '"' || quote == '\'') {
            start++
            val end = line.indexOf(quote, start)
            if (end != -1) line.substring(start, end).trim() else line.substring(start).trim()
        } else {
            var end = start
            while (end < line.length && line[end] != ' ' && line[end] != ',' && line[end] != '>') {
                end++
            }
            line.substring(start, end).trim()
        }
    }

    /**
     * Resolves relative stream URLs to full URLs using the base playlist URL.
     */
    fun resolveUrl(baseUrl: String, rawUrl: String): String {
        val trimmed = rawUrl.trim()
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("rtmp://", ignoreCase = true) ||
            trimmed.startsWith("rtsp://", ignoreCase = true)
        ) {
            return trimmed
        }
        if (trimmed.startsWith("//")) {
            val scheme = if (baseUrl.startsWith("https:", ignoreCase = true)) "https:" else "http:"
            return scheme + trimmed
        }
        return try {
            val baseUri = URI(baseUrl.trim())
            baseUri.resolve(trimmed).toString()
        } catch (e: Exception) {
            if (baseUrl.contains("/")) {
                baseUrl.substringBeforeLast("/") + "/" + trimmed.removePrefix("/")
            } else {
                trimmed
            }
        }
    }

    /**
     * Downloads and parses an M3U / M3U8 playlist from a remote URL with PROGRESSIVE STREAMING.
     * Yields batches of parsed categories, channels, and movies immediately as lines are read.
     */
    suspend fun parseFromUrlStreaming(
        playlistUrl: String,
        defaultGroupName: String = "باقة القنوات المباشرة",
        batchSize: Int = 150,
        onBatchParsed: suspend (categories: List<ChannelCategory>, channels: List<ChannelItem>, movieCategories: List<ChannelCategory>, movies: List<MediaItem>) -> Unit = { _, _, _, _ -> }
    ): ParsedM3uResult = withContext(Dispatchers.IO) {
        val categoriesMap = mutableMapOf<String, ChannelCategory>()
        val channels = mutableListOf<ChannelItem>()
        val movieCategoriesMap = mutableMapOf<String, ChannelCategory>()
        val moviesList = mutableListOf<MediaItem>()

        val pendingChannels = mutableListOf<ChannelItem>()
        val pendingCategories = mutableListOf<ChannelCategory>()
        val pendingMovies = mutableListOf<MediaItem>()
        val pendingMovieCategories = mutableListOf<ChannelCategory>()

        val cleanUrl = playlistUrl.trim()
        if (cleanUrl.isBlank()) {
            return@withContext ParsedM3uResult(emptyList(), emptyList(), emptyList(), emptyList())
        }

        try {
            val request = Request.Builder()
                .url(cleanUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "*/*")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "HTTP error ${response.code} fetching playlist from: $cleanUrl")
                return@withContext ParsedM3uResult(emptyList(), emptyList(), emptyList(), emptyList())
            }

            val body = response.body ?: return@withContext ParsedM3uResult(emptyList(), emptyList(), emptyList(), emptyList())
            val reader = BufferedReader(InputStreamReader(body.byteStream(), Charsets.UTF_8), 32768)

            var currentName = ""
            var currentGroup = defaultGroupName
            var currentLogo = ""
            var currentTvgId = ""
            var isHlsVariant = false
            var channelIndex = 0
            var movieIndex = 0

            suspend fun flushBatch() {
                if (pendingChannels.isNotEmpty() || pendingCategories.isNotEmpty() || pendingMovies.isNotEmpty() || pendingMovieCategories.isNotEmpty()) {
                    val cats = pendingCategories.toList()
                    val chs = pendingChannels.toList()
                    val mCats = pendingMovieCategories.toList()
                    val movs = pendingMovies.toList()
                    pendingCategories.clear()
                    pendingChannels.clear()
                    pendingMovieCategories.clear()
                    pendingMovies.clear()
                    onBatchParsed(cats, chs, mCats, movs)
                }
            }

            var rawLine = reader.readLine()
            while (rawLine != null) {
                val line = rawLine.replace("[\uFEFF\u200B\u200C\u200D\u00A0]".toRegex(), "").trim()
                if (line.isNotEmpty()) {
                    // 1. Check for standard #EXTINF
                    if (line.startsWith("#EXTINF", ignoreCase = true)) {
                        isHlsVariant = false

                        val group = extractAttribute(line, "group-title").ifBlank {
                            extractAttribute(line, "group")
                        }
                        currentGroup = if (group.isNotBlank()) group else defaultGroupName

                        currentLogo = extractAttribute(line, "tvg-logo").ifBlank {
                            extractAttribute(line, "logo")
                        }

                        currentTvgId = extractAttribute(line, "tvg-id")

                        val commaIdx = line.indexOf(',')
                        currentName = if (commaIdx != -1 && commaIdx < line.length - 1) {
                            line.substring(commaIdx + 1).trim()
                        } else {
                            val tvgName = extractAttribute(line, "tvg-name")
                            if (tvgName.isNotBlank()) tvgName else if (currentTvgId.isNotBlank()) currentTvgId else "قناة ${channelIndex + 1}"
                        }
                    }
                    // 2. Check for separate group line: #EXTGRP: or #EXT-X-GROUP:
                    else if (line.startsWith("#EXTGRP:", ignoreCase = true) || line.startsWith("#EXT-X-GROUP:", ignoreCase = true)) {
                        val grp = line.substringAfter(':').trim()
                        if (grp.isNotBlank()) {
                            currentGroup = grp
                        }
                    }
                    // 3. Check for HLS multi-stream variant: #EXT-X-STREAM-INF
                    else if (line.startsWith("#EXT-X-STREAM-INF", ignoreCase = true)) {
                        isHlsVariant = true
                        val streamName = extractAttribute(line, "NAME")
                        val resolution = extractAttribute(line, "RESOLUTION")
                        val bandwidth = extractAttribute(line, "BANDWIDTH")

                        currentName = when {
                            streamName.isNotBlank() -> streamName
                            resolution.isNotBlank() -> "بث بدقة $resolution"
                            bandwidth.isNotBlank() -> {
                                val bps = bandwidth.toDoubleOrNull() ?: 0.0
                                val mbps = bps / 1_000_000.0
                                if (mbps > 0) "بث بدقة ${String.format("%.1f", mbps)} Mbps" else "بث بديل"
                            }
                            else -> "قناة ${channelIndex + 1}"
                        }
                        currentGroup = defaultGroupName
                    }
                    // 4. Stream URL line (non-comment line)
                    else if (!line.startsWith("#")) {
                        val streamUrl = resolveUrl(cleanUrl, line)

                        if (streamUrl.startsWith("http://", ignoreCase = true) ||
                            streamUrl.startsWith("https://", ignoreCase = true) ||
                            streamUrl.startsWith("rtmp://", ignoreCase = true) ||
                            streamUrl.startsWith("rtsp://", ignoreCase = true)
                        ) {
                            val lowerGroup = currentGroup.lowercase()
                            val lowerUrl = streamUrl.lowercase()
                            val isMovie = !isHlsVariant && (
                                lowerGroup.contains("فيلم") ||
                                lowerGroup.contains("أفلام") ||
                                lowerGroup.contains("افلام") ||
                                lowerGroup.contains("movie") ||
                                lowerGroup.contains("movies") ||
                                lowerGroup.contains("vod") ||
                                lowerGroup.contains("cinema") ||
                                lowerGroup.contains("سينما") ||
                                lowerGroup.contains("مسرحيات") ||
                                lowerUrl.endsWith(".mp4") ||
                                lowerUrl.endsWith(".mkv") ||
                                lowerUrl.endsWith(".avi") ||
                                lowerUrl.contains("/movie/") ||
                                lowerUrl.contains("/movies/")
                            )

                            if (isMovie) {
                                val categoryId = "m3u_mov_cat_" + Math.abs(currentGroup.trim().hashCode())
                                val existingCat = movieCategoriesMap[categoryId]
                                val newCount = (existingCat?.channelCount ?: 0) + 1
                                val mCat = ChannelCategory(
                                    id = categoryId,
                                    name = currentGroup,
                                    subtitle = "أفلام وسينما سحابية",
                                    channelCount = newCount,
                                    iconUrl = currentLogo,
                                    categoryType = "movies",
                                    gradientColorHex = PRESET_COLORS[movieCategoriesMap.size % PRESET_COLORS.size]
                                )
                                movieCategoriesMap[categoryId] = mCat
                                if (existingCat == null) {
                                    pendingMovieCategories.add(mCat)
                                }

                                val matcher = YEAR_PATTERN.matcher(currentName)
                                val year = if (matcher.find()) matcher.group() else "2024"
                                val cleanTitle = BRACKETS_PATTERN.matcher(currentName.replace(YEAR_PATTERN.pattern().toRegex(), "")).replaceAll("").trim()

                                val movieId = if (currentTvgId.isNotBlank()) {
                                    "m3u_mov_${currentTvgId}_$movieIndex"
                                } else {
                                    "m3u_mov_${movieIndex}_${Math.abs(streamUrl.hashCode())}"
                                }

                                val mov = MediaItem(
                                    id = movieId,
                                    title = cleanTitle.ifBlank { currentName.ifBlank { "فيلم ${movieIndex + 1}" } },
                                    posterUrl = currentLogo,
                                    backdropUrl = currentLogo,
                                    type = ContentType.MOVIE,
                                    year = year,
                                    rating = "8.8",
                                    genre = currentGroup,
                                    description = "فيلم سينمائي عالي الجودة متوفر عبر البث السحابي.",
                                    duration = "120 دقيقة",
                                    streamUrl = streamUrl,
                                    isTop = movieIndex < 6,
                                    topRank = String.format("%02d", movieIndex + 1),
                                    isFavorite = false
                                )
                                moviesList.add(mov)
                                pendingMovies.add(mov)
                                movieIndex++

                                if (pendingMovies.size >= batchSize || pendingMovieCategories.size >= 25) {
                                    flushBatch()
                                }
                            } else {
                                val categoryId = "m3u_cat_" + Math.abs(currentGroup.trim().hashCode())
                                val existingCat = categoriesMap[categoryId]
                                val newCount = (existingCat?.channelCount ?: 0) + 1
                                val category = ChannelCategory(
                                    id = categoryId,
                                    name = currentGroup,
                                    subtitle = "بث مباشر سريع CDN",
                                    channelCount = newCount,
                                    iconUrl = currentLogo,
                                    categoryType = "live",
                                    gradientColorHex = PRESET_COLORS[categoriesMap.size % PRESET_COLORS.size]
                                )
                                categoriesMap[categoryId] = category
                                if (existingCat == null) {
                                    pendingCategories.add(category)
                                }

                                val channelId = if (currentTvgId.isNotBlank()) {
                                    "m3u_${currentTvgId}_$channelIndex"
                                } else {
                                    "m3u_ch_${channelIndex}_${Math.abs(streamUrl.hashCode())}"
                                }

                                val ch = ChannelItem(
                                    id = channelId,
                                    name = currentName.ifBlank { "قناة ${channelIndex + 1}" },
                                    categoryId = categoryId,
                                    categoryName = currentGroup,
                                    logoUrl = currentLogo,
                                    streamUrl = streamUrl,
                                    backupUrl = "",
                                    country = "سحابي Cloud",
                                    language = "العربية",
                                    isFavorite = false,
                                    isEnabled = true,
                                    sortOrder = channelIndex,
                                    viewsCount = 1200
                                )
                                channels.add(ch)
                                pendingChannels.add(ch)
                                channelIndex++

                                if (pendingChannels.size >= batchSize || pendingCategories.size >= 25) {
                                    flushBatch()
                                }
                            }
                        }

                        currentName = ""
                        currentGroup = defaultGroupName
                        currentLogo = ""
                        currentTvgId = ""
                        isHlsVariant = false
                    }
                }
                rawLine = reader.readLine()
            }

            flushBatch()

            if (channels.isEmpty() && moviesList.isEmpty() && (cleanUrl.contains(".m3u8", ignoreCase = true) || cleanUrl.contains(".mpd", ignoreCase = true) || cleanUrl.contains(".ts", ignoreCase = true))) {
                val catId = "m3u_cat_" + Math.abs(defaultGroupName.trim().hashCode())
                val category = ChannelCategory(
                    id = catId,
                    name = defaultGroupName,
                    subtitle = "بث مباشر سريع CDN",
                    channelCount = 1,
                    iconUrl = "",
                    categoryType = "live",
                    gradientColorHex = "#0088FF"
                )
                categoriesMap[catId] = category
                val ch = ChannelItem(
                    id = "m3u_direct_${Math.abs(cleanUrl.hashCode())}",
                    name = defaultGroupName,
                    categoryId = catId,
                    categoryName = defaultGroupName,
                    logoUrl = "",
                    streamUrl = cleanUrl,
                    backupUrl = "",
                    country = "سحابي Cloud",
                    language = "العربية",
                    isFavorite = false,
                    isEnabled = true,
                    sortOrder = 0,
                    viewsCount = 1500
                )
                channels.add(ch)
                onBatchParsed(listOf(category), listOf(ch), emptyList(), emptyList())
            }

            Log.i(TAG, "Parsed ${channels.size} channels, ${moviesList.size} movies from $cleanUrl")
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching/parsing M3U playlist from $cleanUrl: ${e.message}", e)
        }

        ParsedM3uResult(
            categories = categoriesMap.values.toList(),
            channels = channels,
            movieCategories = movieCategoriesMap.values.toList(),
            movies = moviesList
        )
    }

    suspend fun parseFromUrl(
        playlistUrl: String,
        defaultGroupName: String = "باقة القنوات المباشرة"
    ): ParsedM3uResult = parseFromUrlStreaming(playlistUrl, defaultGroupName, batchSize = 500) { _, _, _, _ -> }

    suspend fun parseMoviesFromUrl(
        playlistUrl: String,
        defaultGroupName: String = "أفلام سينما سحابية"
    ): ParsedM3uResult = withContext(Dispatchers.IO) {
        val result = parseFromUrl(playlistUrl, defaultGroupName)
        if (result.movies.isNotEmpty()) {
            return@withContext result
        }

        val convertedMovies = result.channels.mapIndexed { index, ch ->
            val matcher = YEAR_PATTERN.matcher(ch.name)
            val year = if (matcher.find()) matcher.group() else "2024"
            MediaItem(
                id = "m3u_mov_conv_${index}_${Math.abs(ch.streamUrl.hashCode())}",
                title = ch.name,
                posterUrl = ch.logoUrl,
                backdropUrl = ch.logoUrl,
                type = ContentType.MOVIE,
                year = year,
                rating = "8.9",
                genre = ch.categoryName.ifBlank { defaultGroupName },
                description = "فيلم سينمائي بجودة عالية متوفر عبر البث المباشر.",
                duration = "ساعتان",
                streamUrl = ch.streamUrl,
                isTop = index < 5,
                topRank = String.format("%02d", index + 1),
                isFavorite = false
            )
        }

        val convertedCategories = result.categories.map { cat ->
            cat.copy(categoryType = "movies", subtitle = "أفلام سحابية")
        }

        ParsedM3uResult(
            categories = emptyList(),
            channels = emptyList(),
            movieCategories = if (convertedCategories.isNotEmpty()) convertedCategories else listOf(
                ChannelCategory(
                    id = "m3u_mov_cat_default",
                    name = defaultGroupName,
                    subtitle = "أفلام سحابية",
                    categoryType = "movies"
                )
            ),
            movies = convertedMovies
        )
    }

    suspend fun parseCategoriesOnlyFromUrlStreaming(
        playlistUrl: String,
        defaultGroupName: String = "باقة القنوات المباشرة",
        onCategoryBatch: suspend (List<ChannelCategory>) -> Unit
    ) = withContext(Dispatchers.IO) {
        val cleanUrl = playlistUrl.trim()
        if (cleanUrl.isBlank()) return@withContext

        try {
            val request = Request.Builder()
                .url(cleanUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "*/*")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext
            val body = response.body ?: return@withContext

            val reader = BufferedReader(InputStreamReader(body.byteStream(), Charsets.UTF_8), 32768)
            val seenCategories = mutableSetOf<String>()
            val pendingCategories = mutableListOf<ChannelCategory>()

            var rawLine = reader.readLine()
            while (rawLine != null) {
                val line = rawLine.replace("\uFEFF", "").trim()
                if (line.startsWith("#EXTINF", ignoreCase = true)) {
                    val group = extractAttribute(line, "group-title").ifBlank {
                        extractAttribute(line, "group")
                    }.ifBlank { defaultGroupName }

                    val catId = "m3u_cat_" + Math.abs(group.trim().hashCode())
                    if (seenCategories.add(catId)) {
                        val category = ChannelCategory(
                            id = catId,
                            name = group,
                            subtitle = "بث مباشر سريع CDN",
                            channelCount = 0,
                            iconUrl = "",
                            categoryType = "live",
                            gradientColorHex = PRESET_COLORS[seenCategories.size % PRESET_COLORS.size]
                        )
                        pendingCategories.add(category)
                        if (pendingCategories.size >= 40) {
                            val chunk = pendingCategories.toList()
                            pendingCategories.clear()
                            onCategoryBatch(chunk)
                        }
                    }
                } else if (line.startsWith("#EXTGRP:", ignoreCase = true) || line.startsWith("#EXT-X-GROUP:", ignoreCase = true)) {
                    val grp = line.substringAfter(':').trim()
                    if (grp.isNotBlank()) {
                        val catId = "m3u_cat_" + Math.abs(grp.hashCode())
                        if (seenCategories.add(catId)) {
                            val category = ChannelCategory(
                                id = catId,
                                name = grp,
                                subtitle = "بث مباشر سريع CDN",
                                channelCount = 0,
                                iconUrl = "",
                                categoryType = "live",
                                gradientColorHex = PRESET_COLORS[seenCategories.size % PRESET_COLORS.size]
                            )
                            pendingCategories.add(category)
                            if (pendingCategories.size >= 40) {
                                val chunk = pendingCategories.toList()
                                pendingCategories.clear()
                                onCategoryBatch(chunk)
                            }
                        }
                    }
                }
                rawLine = reader.readLine()
            }

            if (pendingCategories.isNotEmpty()) {
                val chunk = pendingCategories.toList()
                pendingCategories.clear()
                onCategoryBatch(chunk)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error in parseCategoriesOnlyFromUrlStreaming: ${e.message}")
        }
    }

    /**
     * Fast On-Demand streamer that queries remote M3U/M3U8 playlists for a SPECIFIC category.
     * Streams matching channels line-by-line directly to the UI and caches them progressively.
     */
    suspend fun fetchChannelsForCategoryStreaming(
        sources: List<Pair<String, String>>,
        targetCategoryId: String,
        targetCategoryName: String,
        batchSize: Int = 15,
        onBatchLoaded: suspend (List<ChannelItem>) -> Unit = {}
    ): List<ChannelItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ChannelItem>()
        val cleanTargetName = targetCategoryName.trim()
        val cleanTargetId = targetCategoryId.trim()

        for ((playlistUrl, defaultGroupName) in sources) {
            val cleanUrl = playlistUrl.trim()
            if (cleanUrl.isBlank()) continue

            try {
                val request = Request.Builder()
                    .url(cleanUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Accept", "*/*")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) continue
                val body = response.body ?: continue

                val reader = BufferedReader(InputStreamReader(body.byteStream(), Charsets.UTF_8), 32768)

                var currentName = ""
                var currentGroup = defaultGroupName
                var currentLogo = ""
                var currentTvgId = ""
                var channelIndex = 0
                val pendingBatch = mutableListOf<ChannelItem>()

                var rawLine = reader.readLine()
                while (rawLine != null) {
                    val line = rawLine.replace("[\uFEFF\u200B\u200C\u200D\u00A0]".toRegex(), "").trim()
                    if (line.isNotEmpty()) {
                        if (line.startsWith("#EXTINF", ignoreCase = true)) {
                            val group = extractAttribute(line, "group-title").ifBlank {
                                extractAttribute(line, "group")
                            }
                            currentGroup = if (group.isNotBlank()) group else defaultGroupName
                            currentLogo = extractAttribute(line, "tvg-logo").ifBlank {
                                extractAttribute(line, "logo")
                            }
                            currentTvgId = extractAttribute(line, "tvg-id")
                            val commaIdx = line.indexOf(',')
                            currentName = if (commaIdx != -1 && commaIdx < line.length - 1) {
                                line.substring(commaIdx + 1).trim()
                            } else {
                                val tvgName = extractAttribute(line, "tvg-name")
                                if (tvgName.isNotBlank()) tvgName else "قناة ${channelIndex + 1}"
                            }
                        } else if (line.startsWith("#EXTGRP:", ignoreCase = true) || line.startsWith("#EXT-X-GROUP:", ignoreCase = true)) {
                            val grp = line.substringAfter(':').trim()
                            if (grp.isNotBlank()) currentGroup = grp
                        } else if (line.startsWith("#EXT-X-STREAM-INF", ignoreCase = true)) {
                            val streamName = extractAttribute(line, "NAME")
                            val resolution = extractAttribute(line, "RESOLUTION")
                            val bandwidth = extractAttribute(line, "BANDWIDTH")
                            currentName = when {
                                streamName.isNotBlank() -> streamName
                                resolution.isNotBlank() -> "بث بدقة $resolution"
                                bandwidth.isNotBlank() -> {
                                    val bps = bandwidth.toDoubleOrNull() ?: 0.0
                                    val mbps = bps / 1_000_000.0
                                    if (mbps > 0) "بث بدقة ${String.format("%.1f", mbps)} Mbps" else "بث بديل"
                                }
                                else -> "قناة ${channelIndex + 1}"
                            }
                            currentGroup = defaultGroupName
                        } else if (!line.startsWith("#")) {
                            val streamUrl = resolveUrl(cleanUrl, line)
                            if (streamUrl.startsWith("http://", ignoreCase = true) || streamUrl.startsWith("https://", ignoreCase = true) ||
                                streamUrl.startsWith("rtmp://", ignoreCase = true) || streamUrl.startsWith("rtsp://", ignoreCase = true)) {

                                val cleanGrp = currentGroup.trim()
                                val cleanTgt = cleanTargetName.trim()
                                val generatedCatId = "m3u_cat_" + Math.abs(cleanGrp.hashCode())
                                val matchesCategory = (cleanTgt.isNotBlank() && cleanGrp.equals(cleanTgt, ignoreCase = true)) ||
                                        (cleanTgt.isNotBlank() && (cleanGrp.contains(cleanTgt, ignoreCase = true) || cleanTgt.contains(cleanGrp, ignoreCase = true))) ||
                                        (cleanTargetId.isNotBlank() && (cleanTargetId == generatedCatId || cleanTargetId.equals(cleanGrp, ignoreCase = true) || cleanTargetId.removePrefix("m3u_cat_") == generatedCatId.removePrefix("m3u_cat_"))) ||
                                        (cleanTargetId == "all" || cleanTargetId.isBlank() || cleanTargetId == "custom")

                                if (matchesCategory) {
                                    val chId = if (currentTvgId.isNotBlank()) {
                                        "m3u_${currentTvgId}_$channelIndex"
                                    } else {
                                        "m3u_ch_${channelIndex}_${Math.abs(streamUrl.hashCode())}"
                                    }
                                    val ch = ChannelItem(
                                        id = chId,
                                        name = currentName.ifBlank { "قناة ${channelIndex + 1}" },
                                        categoryId = if (cleanTargetId.isNotBlank() && cleanTargetId != "all") cleanTargetId else generatedCatId,
                                        categoryName = currentGroup,
                                        logoUrl = currentLogo,
                                        streamUrl = streamUrl,
                                        backupUrl = "",
                                        country = "سحابي Cloud",
                                        language = "العربية",
                                        isFavorite = false,
                                        isEnabled = true,
                                        sortOrder = channelIndex,
                                        viewsCount = 1200
                                    )
                                    results.add(ch)
                                    pendingBatch.add(ch)
                                    channelIndex++

                                    if (pendingBatch.size >= batchSize) {
                                        val chunk = pendingBatch.toList()
                                        pendingBatch.clear()
                                        onBatchLoaded(chunk)
                                    }
                                }
                            }
                            currentName = ""
                            currentGroup = defaultGroupName
                            currentLogo = ""
                            currentTvgId = ""
                        }
                    }
                    rawLine = reader.readLine()
                }

                if (pendingBatch.isNotEmpty()) {
                    val chunk = pendingBatch.toList()
                    pendingBatch.clear()
                    onBatchLoaded(chunk)
                }

                if (results.isEmpty() && (cleanUrl.contains(".m3u8", ignoreCase = true) || cleanUrl.contains(".ts", ignoreCase = true) || cleanUrl.contains(".mpd", ignoreCase = true))) {
                    val ch = ChannelItem(
                        id = "m3u_direct_${Math.abs(cleanUrl.hashCode())}",
                        name = defaultGroupName.ifBlank { cleanTargetName.ifBlank { "بث مباشر" } },
                        categoryId = cleanTargetId.ifBlank { "m3u_cat_" + Math.abs(defaultGroupName.hashCode()) },
                        categoryName = defaultGroupName.ifBlank { cleanTargetName },
                        logoUrl = "",
                        streamUrl = cleanUrl,
                        backupUrl = "",
                        country = "سحابي Cloud",
                        language = "العربية",
                        isFavorite = false,
                        isEnabled = true,
                        sortOrder = 0,
                        viewsCount = 1500
                    )
                    results.add(ch)
                    onBatchLoaded(listOf(ch))
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error fetching channels for category $cleanTargetName from $cleanUrl: ${e.message}")
            }
        }
        results
    }
}
