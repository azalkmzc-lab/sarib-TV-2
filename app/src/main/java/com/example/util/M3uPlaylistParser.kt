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

    /**
     * Builds an OkHttpClient with permissive SSL/TLS compatibility for older Android releases
     * (Android 5.0 - 7.1.1) and Android TV boxes that lack modern Let's Encrypt or Cloudflare root CAs.
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
                .connectTimeout(25, TimeUnit.SECONDS)
                .readTimeout(35, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .build()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to create permissive SSL socket factory: ${e.message}")
            OkHttpClient.Builder()
                .connectTimeout(25, TimeUnit.SECONDS)
                .readTimeout(35, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .build()
        }
    }

    /**
     * Extracts an attribute value from an M3U/M3U8 line supporting:
     * attr="value", attr='value', and attr=value
     */
    private fun extractAttribute(line: String, attrName: String): String {
        val regex = Pattern.compile("(?i)\\b$attrName\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^,\\s>]+))")
        val matcher = regex.matcher(line)
        if (matcher.find()) {
            return (matcher.group(1) ?: matcher.group(2) ?: matcher.group(3) ?: "").trim()
        }
        return ""
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
     * Downloads and parses an M3U / M3U8 playlist from a remote URL.
     * Fully compatible with:
     * 1. Multi-channel standard IPTV playlists (#EXTINF).
     * 2. Multi-variant / multi-rendition HLS master playlists (#EXT-X-STREAM-INF / #EXT-X-MEDIA).
     * 3. Playlists with BOM, Unicode / Arabic category names, and relative chunklist paths.
     * 4. Older Android versions (API 21+) and Android TV devices.
     */
    suspend fun parseFromUrl(
        playlistUrl: String,
        defaultGroupName: String = "باقة القنوات المباشرة"
    ): ParsedM3uResult = withContext(Dispatchers.IO) {
        val categoriesMap = mutableMapOf<String, ChannelCategory>()
        val channels = mutableListOf<ChannelItem>()
        val movieCategoriesMap = mutableMapOf<String, ChannelCategory>()
        val moviesList = mutableListOf<MediaItem>()

        val cleanUrl = playlistUrl.trim()
        if (cleanUrl.isBlank()) {
            return@withContext ParsedM3uResult(emptyList(), emptyList(), emptyList(), emptyList())
        }

        try {
            val request = Request.Builder()
                .url(cleanUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                .header("Accept", "*/*")
                .header("Accept-Language", "ar,en-US;q=0.9,en;q=0.8")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "HTTP error ${response.code} fetching playlist from: $cleanUrl")
                return@withContext ParsedM3uResult(emptyList(), emptyList(), emptyList(), emptyList())
            }

            val body = response.body ?: return@withContext ParsedM3uResult(emptyList(), emptyList(), emptyList(), emptyList())

            val reader = BufferedReader(InputStreamReader(body.byteStream(), Charsets.UTF_8))
            var currentName = ""
            var currentGroup = defaultGroupName
            var currentLogo = ""
            var currentTvgId = ""
            var isHlsVariant = false
            var channelIndex = 0
            var movieIndex = 0

            val colors = listOf("#0088FF", "#00C8FF", "#2563EB", "#7C3AED", "#DC2626", "#059669", "#D97706", "#EC4899")

            reader.useLines { lines ->
                for (rawLine in lines) {
                    // Strip BOM and whitespace
                    val line = rawLine.replace("\uFEFF", "").trim()
                    if (line.isEmpty()) continue

                    // 1. Check for standard #EXTINF
                    if (line.startsWith("#EXTINF", ignoreCase = true)) {
                        isHlsVariant = false

                        // Extract group-title
                        val group = extractAttribute(line, "group-title").ifBlank {
                            extractAttribute(line, "group")
                        }
                        currentGroup = if (group.isNotBlank()) group else defaultGroupName

                        // Extract tvg-logo
                        currentLogo = extractAttribute(line, "tvg-logo").ifBlank {
                            extractAttribute(line, "logo")
                        }

                        // Extract tvg-id
                        currentTvgId = extractAttribute(line, "tvg-id")

                        // Extract channel name: standard M3U places title after the FIRST comma on the #EXTINF line
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
                    // 4. Check for HLS multi-media program: #EXT-X-MEDIA
                    else if (line.startsWith("#EXT-X-MEDIA", ignoreCase = true)) {
                        val mediaType = extractAttribute(line, "TYPE")
                        val mediaUri = extractAttribute(line, "URI")
                        val mediaName = extractAttribute(line, "NAME")
                        val mediaGroup = extractAttribute(line, "GROUP-ID").ifBlank { defaultGroupName }

                        if (mediaUri.isNotBlank() && (mediaType.equals("VIDEO", ignoreCase = true) || mediaType.isBlank())) {
                            val fullStreamUrl = resolveUrl(cleanUrl, mediaUri)
                            val catId = "m3u_cat_" + Math.abs(mediaGroup.trim().hashCode())

                            val existingCat = categoriesMap[catId]
                            val newCount = (existingCat?.channelCount ?: 0) + 1
                            categoriesMap[catId] = ChannelCategory(
                                id = catId,
                                name = mediaGroup,
                                subtitle = "بث مباشر سريع CDN",
                                channelCount = newCount,
                                iconUrl = "",
                                categoryType = "live",
                                gradientColorHex = colors[categoriesMap.size % colors.size]
                            )

                            channels.add(
                                ChannelItem(
                                    id = "m3u_media_${channelIndex}_${Math.abs(fullStreamUrl.hashCode())}",
                                    name = mediaName.ifBlank { "قناة ${channelIndex + 1}" },
                                    categoryId = catId,
                                    categoryName = mediaGroup,
                                    logoUrl = "",
                                    streamUrl = fullStreamUrl,
                                    backupUrl = "",
                                    country = "سحابي Cloud",
                                    language = "العربية",
                                    isFavorite = false,
                                    isEnabled = true,
                                    sortOrder = channelIndex,
                                    viewsCount = (300..2500).random()
                                )
                            )
                            channelIndex++
                        }
                    }
                    // 5. Stream URL line (non-comment line)
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
                                movieCategoriesMap[categoryId] = ChannelCategory(
                                    id = categoryId,
                                    name = currentGroup,
                                    subtitle = "أفلام وسينما سحابية",
                                    channelCount = newCount,
                                    iconUrl = currentLogo,
                                    categoryType = "movies",
                                    gradientColorHex = colors[movieCategoriesMap.size % colors.size]
                                )

                                val yearRegex = "\\b(19\\d{2}|20\\d{2})\\b".toRegex()
                                val year = yearRegex.find(currentName)?.value ?: "2024"
                                val cleanTitle = currentName.replace(yearRegex, "").replace("[()]|\\[\\]".toRegex(), "").trim()

                                val movieId = if (currentTvgId.isNotBlank()) {
                                    "m3u_mov_${currentTvgId}_$movieIndex"
                                } else {
                                    "m3u_mov_${movieIndex}_${Math.abs(streamUrl.hashCode())}"
                                }

                                moviesList.add(
                                    MediaItem(
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
                                )
                                movieIndex++
                            } else {
                                val categoryId = "m3u_cat_" + Math.abs(currentGroup.trim().hashCode())
                                val existingCat = categoriesMap[categoryId]
                                val newCount = (existingCat?.channelCount ?: 0) + 1
                                categoriesMap[categoryId] = ChannelCategory(
                                    id = categoryId,
                                    name = currentGroup,
                                    subtitle = "بث مباشر سريع CDN",
                                    channelCount = newCount,
                                    iconUrl = currentLogo,
                                    categoryType = "live",
                                    gradientColorHex = colors[categoriesMap.size % colors.size]
                                )

                                val channelId = if (currentTvgId.isNotBlank()) {
                                    "m3u_${currentTvgId}_$channelIndex"
                                } else {
                                    "m3u_ch_${channelIndex}_${Math.abs(streamUrl.hashCode())}"
                                }

                                channels.add(
                                    ChannelItem(
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
                                        viewsCount = (300..2500).random()
                                    )
                                )
                                channelIndex++
                            }
                        }

                        // Reset temporary fields
                        currentName = ""
                        currentGroup = defaultGroupName
                        currentLogo = ""
                        currentTvgId = ""
                        isHlsVariant = false
                    }
                }
            }

            // Fallback: If no #EXTINF was found but this is a direct M3U8/MPD/TS stream link
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
                channels.add(
                    ChannelItem(
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
                )
            }

            Log.i(TAG, "Parsed ${channels.size} channels, ${moviesList.size} movies across ${categoriesMap.size} categories from $cleanUrl")
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

    /**
     * Parses an M3U playlist specifically dedicated to movies and VOD streams.
     */
    suspend fun parseMoviesFromUrl(
        playlistUrl: String,
        defaultGroupName: String = "أفلام سينما سحابية"
    ): ParsedM3uResult = withContext(Dispatchers.IO) {
        val result = parseFromUrl(playlistUrl, defaultGroupName)
        if (result.movies.isNotEmpty()) {
            return@withContext result
        }

        val convertedMovies = result.channels.mapIndexed { index, ch ->
            val yearRegex = "\\b(19\\d{2}|20\\d{2})\\b".toRegex()
            val year = yearRegex.find(ch.name)?.value ?: "2024"
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
}
