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
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class ParsedM3uResult(
    val categories: List<ChannelCategory>,
    val channels: List<ChannelItem>,
    val movieCategories: List<ChannelCategory> = emptyList(),
    val movies: List<MediaItem> = emptyList()
)

object M3uPlaylistParser {

    private const val TAG = "M3uPlaylistParser"
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val GROUP_TITLE_PATTERN = Pattern.compile("group-title=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE)
    private val TVG_LOGO_PATTERN = Pattern.compile("tvg-logo=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE)
    private val TVG_NAME_PATTERN = Pattern.compile("tvg-name=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE)
    private val TVG_ID_PATTERN = Pattern.compile("tvg-id=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE)

    /**
     * Downloads and parses an M3U / M3U8 playlist from a remote URL.
     */
    suspend fun parseFromUrl(
        playlistUrl: String,
        defaultGroupName: String = "باقة القنوات المباشرة"
    ): ParsedM3uResult = withContext(Dispatchers.IO) {
        val categoriesMap = mutableMapOf<String, ChannelCategory>()
        val channels = mutableListOf<ChannelItem>()
        val movieCategoriesMap = mutableMapOf<String, ChannelCategory>()
        val moviesList = mutableListOf<MediaItem>()

        if (playlistUrl.isBlank()) {
            return@withContext ParsedM3uResult(emptyList(), emptyList(), emptyList(), emptyList())
        }

        try {
            val request = Request.Builder()
                .url(playlistUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body ?: return@withContext ParsedM3uResult(emptyList(), emptyList(), emptyList(), emptyList())

            val reader = BufferedReader(InputStreamReader(body.byteStream(), Charsets.UTF_8))
            var currentName = ""
            var currentGroup = defaultGroupName
            var currentLogo = ""
            var currentTvgId = ""
            var channelIndex = 0
            var movieIndex = 0

            val colors = listOf("#0088FF", "#00C8FF", "#2563EB", "#7C3AED", "#DC2626", "#059669", "#D97706", "#EC4899")

            reader.useLines { lines ->
                for (rawLine in lines) {
                    val line = rawLine.trim()
                    if (line.isEmpty()) continue

                    if (line.startsWith("#EXTINF:", ignoreCase = true)) {
                        // Extract group-title
                        val groupMatcher = GROUP_TITLE_PATTERN.matcher(line)
                        currentGroup = if (groupMatcher.find()) {
                            groupMatcher.group(1)?.trim() ?: defaultGroupName
                        } else {
                            defaultGroupName
                        }

                        // Extract tvg-logo
                        val logoMatcher = TVG_LOGO_PATTERN.matcher(line)
                        currentLogo = if (logoMatcher.find()) {
                            logoMatcher.group(1)?.trim().orEmpty()
                        } else {
                            ""
                        }

                        // Extract tvg-id
                        val idMatcher = TVG_ID_PATTERN.matcher(line)
                        currentTvgId = if (idMatcher.find()) {
                            idMatcher.group(1)?.trim().orEmpty()
                        } else {
                            ""
                        }

                        // Extract Channel / Movie Name from after the last comma
                        val commaIndex = line.lastIndexOf(',')
                        currentName = if (commaIndex != -1 && commaIndex < line.length - 1) {
                            line.substring(commaIndex + 1).trim()
                        } else {
                            val nameMatcher = TVG_NAME_PATTERN.matcher(line)
                            if (nameMatcher.find()) nameMatcher.group(1)?.trim().orEmpty() else "محتوى مباشر"
                        }
                    } else if (!line.startsWith("#")) {
                        val streamUrl = line

                        if (streamUrl.startsWith("http://", ignoreCase = true) ||
                            streamUrl.startsWith("https://", ignoreCase = true)
                        ) {
                            val lowerGroup = currentGroup.lowercase()
                            val isMovie = lowerGroup.contains("فيلم") ||
                                    lowerGroup.contains("أفلام") ||
                                    lowerGroup.contains("افلام") ||
                                    lowerGroup.contains("movie") ||
                                    lowerGroup.contains("movies") ||
                                    lowerGroup.contains("vod") ||
                                    lowerGroup.contains("cinema") ||
                                    lowerGroup.contains("سينما") ||
                                    lowerGroup.contains("مسرحيات") ||
                                    streamUrl.endsWith(".mp4", ignoreCase = true) ||
                                    streamUrl.endsWith(".mkv", ignoreCase = true) ||
                                    streamUrl.endsWith(".avi", ignoreCase = true) ||
                                    streamUrl.endsWith(".mov", ignoreCase = true) ||
                                    streamUrl.contains("/movie/", ignoreCase = true) ||
                                    streamUrl.contains("/movies/", ignoreCase = true)

                            if (isMovie) {
                                val categoryId = "m3u_mov_cat_" + currentGroup.replace("[^a-zA-Z0-9_]".toRegex(), "_")
                                if (!movieCategoriesMap.containsKey(categoryId)) {
                                    val color = colors[movieCategoriesMap.size % colors.size]
                                    movieCategoriesMap[categoryId] = ChannelCategory(
                                        id = categoryId,
                                        name = currentGroup,
                                        subtitle = "أفلام وسينما M3U8 سحابية",
                                        channelCount = 0,
                                        iconUrl = currentLogo,
                                        categoryType = "movies",
                                        gradientColorHex = color
                                    )
                                }

                                val yearRegex = "\\b(19\\d{2}|20\\d{2})\\b".toRegex()
                                val year = yearRegex.find(currentName)?.value ?: "2024"
                                val cleanTitle = currentName.replace(yearRegex, "").replace("[()]|\\[\\]".toRegex(), "").trim()

                                val movieId = if (currentTvgId.isNotBlank()) {
                                    "m3u_mov_${currentTvgId}_$movieIndex"
                                } else {
                                    "m3u_mov_${movieIndex}_${streamUrl.hashCode()}"
                                }

                                moviesList.add(
                                    MediaItem(
                                        id = movieId,
                                        title = if (cleanTitle.isNotBlank()) cleanTitle else currentName.ifBlank { "فيلم ${movieIndex + 1}" },
                                        posterUrl = currentLogo,
                                        backdropUrl = currentLogo,
                                        type = ContentType.MOVIE,
                                        year = year,
                                        rating = "8.8",
                                        genre = currentGroup,
                                        description = "فيلم سينمائي عالي الجودة متوفر عبر البث المباشر السحابي M3U8.",
                                        duration = "120 دقيقة",
                                        streamUrl = streamUrl,
                                        isTop = movieIndex < 6,
                                        topRank = String.format("%02d", movieIndex + 1),
                                        isFavorite = false
                                    )
                                )
                                movieIndex++
                            } else {
                                val categoryId = "m3u_cat_" + currentGroup.replace("[^a-zA-Z0-9_]".toRegex(), "_")
                                if (!categoriesMap.containsKey(categoryId)) {
                                    val color = colors[categoriesMap.size % colors.size]
                                    categoriesMap[categoryId] = ChannelCategory(
                                        id = categoryId,
                                        name = currentGroup,
                                        subtitle = "بث مباشر سريع CDN",
                                        channelCount = 0,
                                        iconUrl = currentLogo,
                                        categoryType = "live",
                                        gradientColorHex = color
                                    )
                                }

                                val channelId = if (currentTvgId.isNotBlank()) {
                                    "m3u_${currentTvgId}_$channelIndex"
                                } else {
                                    "m3u_ch_${channelIndex}_${streamUrl.hashCode()}"
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
                    }
                }
            }

            // Fallback: If no #EXTINF was found but this is a direct M3U8/MPD stream link
            if (channels.isEmpty() && moviesList.isEmpty() && (playlistUrl.contains(".m3u8", ignoreCase = true) || playlistUrl.contains(".mpd", ignoreCase = true))) {
                val catId = "m3u_cat_" + defaultGroupName.replace("[^a-zA-Z0-9_]".toRegex(), "_")
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
                        id = "m3u_direct_${playlistUrl.hashCode()}",
                        name = defaultGroupName,
                        categoryId = catId,
                        categoryName = defaultGroupName,
                        logoUrl = "",
                        streamUrl = playlistUrl,
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

            Log.i(TAG, "Parsed ${channels.size} channels, ${moviesList.size} movies across ${categoriesMap.size + movieCategoriesMap.size} categories.")
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching/parsing M3U playlist: ${e.message}")
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

        // If items were parsed as channels instead of movies, convert them into movies
        val convertedMovies = result.channels.mapIndexed { index, ch ->
            val yearRegex = "\\b(19\\d{2}|20\\d{2})\\b".toRegex()
            val year = yearRegex.find(ch.name)?.value ?: "2024"
            MediaItem(
                id = "m3u_mov_conv_${index}_${ch.streamUrl.hashCode()}",
                title = ch.name,
                posterUrl = ch.logoUrl,
                backdropUrl = ch.logoUrl,
                type = ContentType.MOVIE,
                year = year,
                rating = "8.9",
                genre = ch.categoryName.ifBlank { defaultGroupName },
                description = "فيلم سينمائي بجودة عالية متوفر عبر رابط M3U8 مباشر.",
                duration = "ساعتان",
                streamUrl = ch.streamUrl,
                isTop = index < 5,
                topRank = String.format("%02d", index + 1),
                isFavorite = false
            )
        }

        val convertedCategories = result.categories.map { cat ->
            cat.copy(categoryType = "movies", subtitle = "أفلام M3U8 سحابية")
        }

        ParsedM3uResult(
            categories = emptyList(),
            channels = emptyList(),
            movieCategories = if (convertedCategories.isNotEmpty()) convertedCategories else listOf(
                ChannelCategory(
                    id = "m3u_mov_cat_default",
                    name = defaultGroupName,
                    subtitle = "أفلام M3U8 سحابية",
                    categoryType = "movies"
                )
            ),
            movies = convertedMovies
        )
    }
}
