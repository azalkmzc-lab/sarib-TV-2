package com.example.util

import android.util.Log
import com.example.data.model.ChannelCategory
import com.example.data.model.ChannelItem
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
    val channels: List<ChannelItem>
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

        if (playlistUrl.isBlank()) {
            return@withContext ParsedM3uResult(emptyList(), emptyList())
        }

        try {
            val request = Request.Builder()
                .url(playlistUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body ?: return@withContext ParsedM3uResult(emptyList(), emptyList())

            val reader = BufferedReader(InputStreamReader(body.byteStream(), Charsets.UTF_8))
            var currentName = ""
            var currentGroup = defaultGroupName
            var currentLogo = ""
            var currentTvgId = ""
            var channelIndex = 0

            val colors = listOf("#0088FF", "#00C8FF", "#2563EB", "#7C3AED", "#DC2626", "#059669", "#D97706")

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

                        // Extract Channel Name from after the last comma
                        val commaIndex = line.lastIndexOf(',')
                        currentName = if (commaIndex != -1 && commaIndex < line.length - 1) {
                            line.substring(commaIndex + 1).trim()
                        } else {
                            val nameMatcher = TVG_NAME_PATTERN.matcher(line)
                            if (nameMatcher.find()) nameMatcher.group(1)?.trim().orEmpty() else "قناة مباشرة"
                        }
                    } else if (!line.startsWith("#")) {
                        // This line is the Stream URL (can be http, https, mpd with pipe DRM, etc.)
                        val streamUrl = line

                        if (streamUrl.startsWith("http://", ignoreCase = true) ||
                            streamUrl.startsWith("https://", ignoreCase = true)
                        ) {
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

                        // Reset temporary fields
                        currentName = ""
                        currentGroup = defaultGroupName
                        currentLogo = ""
                        currentTvgId = ""
                    }
                }
            }

            // Fallback: If no #EXTINF was found but this is a direct M3U8/MPD stream link
            if (channels.isEmpty() && (playlistUrl.contains(".m3u8", ignoreCase = true) || playlistUrl.contains(".mpd", ignoreCase = true))) {
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

            Log.i(TAG, "Parsed ${channels.size} channels across ${categoriesMap.size} categories from M3U playlist.")
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching/parsing M3U playlist: ${e.message}")
        }

        ParsedM3uResult(
            categories = categoriesMap.values.toList(),
            channels = channels
        )
    }
}
