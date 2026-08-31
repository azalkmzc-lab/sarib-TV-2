package com.example.data.remote

import android.util.Log
import com.example.data.model.ChannelCategory
import com.example.data.model.ChannelItem
import com.example.data.model.ContentType
import com.example.data.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class XtreamApiClient(
    val serverHost: String = "http://cliccck52258.club:2082",
    val username: String = "khaledsliman",
    val password: String = "755246419856"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "${serverHost.trimEnd('/')}/player_api.php?username=$username&password=$password"

    suspend fun fetchLiveCategories(): List<ChannelCategory> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl&action=get_live_categories"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val jsonStr = response.body?.string().orEmpty()
            if (jsonStr.isBlank()) return@withContext emptyList()

            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<ChannelCategory>()
            val colors = listOf("#9333EA", "#2563EB", "#7C3AED", "#059669", "#DC2626", "#65A30D", "#0D9488", "#D97706", "#EC4899", "#F59E0B")

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val catId = obj.optString("category_id", "")
                val catName = obj.optString("category_name", "باقة قنوات")
                if (catId.isNotEmpty()) {
                    list.add(
                        ChannelCategory(
                            id = catId,
                            name = catName,
                            subtitle = "باقة بث مباشر",
                            channelCount = 0,
                            categoryType = if (catName.contains("sport", ignoreCase = true) || catName.contains("رياض", ignoreCase = true)) "sports" else "entertainment",
                            gradientColorHex = colors[i % colors.size]
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            Log.e("XtreamApiClient", "Error fetching live categories: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchLiveStreams(categoryId: String? = null): List<ChannelItem> = withContext(Dispatchers.IO) {
        try {
            val url = if (categoryId.isNullOrBlank()) {
                "$baseUrl&action=get_live_streams"
            } else {
                "$baseUrl&action=get_live_streams&category_id=$categoryId"
            }
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val jsonStr = response.body?.string().orEmpty()
            if (jsonStr.isBlank()) return@withContext emptyList()

            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<ChannelItem>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val streamId = obj.optString("stream_id", "")
                val name = obj.optString("name", "قناة")
                val icon = obj.optString("stream_icon", "")
                val catId = obj.optString("category_id", categoryId ?: "")
                val num = obj.optInt("num", i + 1)

                if (streamId.isNotEmpty()) {
                    val streamUrl = "${serverHost.trimEnd('/')}/live/$username/$password/$streamId.m3u8"
                    val backupUrl = "${serverHost.trimEnd('/')}/live/$username/$password/$streamId.ts"
                    list.add(
                        ChannelItem(
                            id = "xt_ch_$streamId",
                            name = name,
                            categoryId = catId,
                            categoryName = "باقة $catId",
                            logoUrl = icon,
                            streamUrl = streamUrl,
                            backupUrl = backupUrl,
                            country = "العالم العربي",
                            language = "العربية",
                            isFavorite = false,
                            isEnabled = true,
                            sortOrder = num,
                            viewsCount = (1000..9900).random()
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            Log.e("XtreamApiClient", "Error fetching live streams: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchVodStreams(): List<MediaItem> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl&action=get_vod_streams"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val jsonStr = response.body?.string().orEmpty()
            if (jsonStr.isBlank()) return@withContext emptyList()

            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<MediaItem>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val streamId = obj.optString("stream_id", "")
                val name = obj.optString("name", "فيلم")
                val icon = obj.optString("stream_icon", "")
                val rating = obj.optString("rating", "8.5")
                val container = obj.optString("container_extension", "mp4").ifEmpty { "mp4" }

                if (streamId.isNotEmpty()) {
                    val streamUrl = "${serverHost.trimEnd('/')}/movie/$username/$password/$streamId.$container"
                    list.add(
                        MediaItem(
                            id = "xt_mov_$streamId",
                            title = name,
                            posterUrl = icon,
                            backdropUrl = icon,
                            type = ContentType.MOVIE,
                            year = "2024",
                            rating = if (rating.isNotBlank() && rating != "0") rating.take(3) else "8.7",
                            genre = "أفلام سينما",
                            description = "مشاهدة مباشرة بدقة عالية عبر SARIB TV",
                            duration = "120 دقيقة",
                            streamUrl = streamUrl,
                            isTop = i < 10,
                            topRank = String.format("%02d", i + 1),
                            isFavorite = false
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            Log.e("XtreamApiClient", "Error fetching VOD streams: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchSeries(): List<MediaItem> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl&action=get_series"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val jsonStr = response.body?.string().orEmpty()
            if (jsonStr.isBlank()) return@withContext emptyList()

            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<MediaItem>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val seriesId = obj.optString("series_id", "")
                val name = obj.optString("name", "مسلسل")
                val cover = obj.optString("cover", "")
                val plot = obj.optString("plot", "مسلسل درامي حصري على SARIB TV")
                val rating = obj.optString("rating", "8.9")
                val releaseDate = obj.optString("releaseDate", "2024")
                val genre = obj.optString("genre", "دراما / تشويق")

                if (seriesId.isNotEmpty()) {
                    val streamUrl = "${serverHost.trimEnd('/')}/series/$username/$password/$seriesId.mp4"
                    list.add(
                        MediaItem(
                            id = "xt_ser_$seriesId",
                            title = name,
                            posterUrl = cover,
                            backdropUrl = cover,
                            type = if (genre.contains("أنمي", ignoreCase = true) || name.contains("anime", ignoreCase = true)) ContentType.ANIME else ContentType.SERIES,
                            year = releaseDate.take(4).ifEmpty { "2024" },
                            rating = if (rating.isNotBlank() && rating != "0") rating.take(3) else "8.9",
                            genre = genre.ifEmpty { "مسلسل حصري" },
                            description = plot,
                            duration = "45 دقيقة",
                            seasonsCount = 1,
                            episodesCount = 10,
                            streamUrl = streamUrl,
                            isTop = i < 10,
                            topRank = String.format("%02d", i + 1),
                            isFavorite = false
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            Log.e("XtreamApiClient", "Error fetching series: ${e.message}", e)
            emptyList()
        }
    }
}
