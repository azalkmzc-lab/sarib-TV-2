package com.example.data.remote

import android.util.Log
import com.example.data.model.ChannelCategory
import com.example.data.model.ChannelItem
import com.example.data.model.ContentType
import com.example.data.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class XtreamApiClient(
    var serverHost: String = "http://cliccck52258.club:2082",
    var username: String = "khaledsliman",
    var password: String = "755246419856"
) {
    companion object {
        private val sharedClient = OkHttpClient.Builder()
            .connectionPool(ConnectionPool(32, 5, TimeUnit.MINUTES))
            .dispatcher(Dispatcher().apply {
                maxRequests = 64
                maxRequestsPerHost = 16
            })
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        private val memoryCache = ConcurrentHashMap<String, Pair<Long, Any>>()
        private const val CACHE_TTL_MS = 5 * 60 * 1000L // 5 minutes
    }

    private val client: OkHttpClient get() = sharedClient

    private fun getBaseUrl() = "${serverHost.trimEnd('/')}/player_api.php?username=$username&password=$password"

    fun updateCredentials(host: String, user: String, pass: String) {
        if (this.serverHost != host || this.username != user || this.password != pass) {
            this.serverHost = host
            this.username = user
            this.password = pass
            memoryCache.clear()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getFromCache(key: String): T? {
        val entry = memoryCache[key] ?: return null
        if (System.currentTimeMillis() - entry.first < CACHE_TTL_MS) {
            return entry.second as? T
        }
        memoryCache.remove(key)
        return null
    }

    private fun putInCache(key: String, value: Any) {
        memoryCache[key] = Pair(System.currentTimeMillis(), value)
    }

    suspend fun pingServer(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "${getBaseUrl()}&action=get_live_categories"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            response.isSuccessful && !response.body?.string().isNullOrBlank()
        } catch (e: Exception) {
            Log.w("XtreamApiClient", "Server ping failed: ${e.message}")
            false
        }
    }

    suspend fun fetchLiveCategories(
        batchSize: Int = 20,
        onBatch: suspend (List<ChannelCategory>) -> Unit = {}
    ): List<ChannelCategory> = withContext(Dispatchers.IO) {
        val cacheKey = "live_cats_${serverHost}_$username"
        getFromCache<List<ChannelCategory>>(cacheKey)?.let {
            if (it.isNotEmpty()) onBatch(it)
            return@withContext it
        }

        try {
            val url = "${getBaseUrl()}&action=get_live_categories"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val jsonStr = response.body?.string().orEmpty()
            if (jsonStr.isBlank()) return@withContext emptyList()

            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<ChannelCategory>()
            val currentBatch = mutableListOf<ChannelCategory>()
            val colors = listOf("#9333EA", "#2563EB", "#7C3AED", "#059669", "#DC2626", "#65A30D", "#0D9488", "#D97706", "#EC4899", "#F59E0B")

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val catId = obj.optString("category_id", "")
                val catName = obj.optString("category_name", "باقة قنوات")
                if (catId.isNotEmpty()) {
                    val category = ChannelCategory(
                        id = catId,
                        name = catName,
                        subtitle = "باقة بث مباشر",
                        channelCount = 0,
                        categoryType = if (catName.contains("sport", ignoreCase = true) || catName.contains("رياض", ignoreCase = true)) "sports" else "entertainment",
                        gradientColorHex = colors[i % colors.size]
                    )
                    list.add(category)
                    currentBatch.add(category)
                    if (currentBatch.size >= batchSize) {
                        onBatch(currentBatch.toList())
                        currentBatch.clear()
                    }
                }
            }
            if (currentBatch.isNotEmpty()) {
                onBatch(currentBatch.toList())
                currentBatch.clear()
            }
            if (list.isNotEmpty()) putInCache(cacheKey, list)
            list
        } catch (e: Exception) {
            Log.e("XtreamApiClient", "Error fetching live categories: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchVodCategories(
        batchSize: Int = 20,
        onBatch: suspend (List<ChannelCategory>) -> Unit = {}
    ): List<ChannelCategory> = withContext(Dispatchers.IO) {
        val cacheKey = "vod_cats_${serverHost}_$username"
        getFromCache<List<ChannelCategory>>(cacheKey)?.let {
            if (it.isNotEmpty()) onBatch(it)
            return@withContext it
        }

        try {
            val url = "${getBaseUrl()}&action=get_vod_categories"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val jsonStr = response.body?.string().orEmpty()
            if (jsonStr.isBlank()) return@withContext emptyList()

            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<ChannelCategory>()
            val currentBatch = mutableListOf<ChannelCategory>()
            val colors = listOf("#E11D48", "#2563EB", "#7C3AED", "#D97706", "#059669", "#9333EA")

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val catId = obj.optString("category_id", "")
                val catName = obj.optString("category_name", "قسم أفلام")
                if (catId.isNotEmpty()) {
                    val category = ChannelCategory(
                        id = "vod_$catId",
                        name = catName,
                        subtitle = "أفلام سينمائية",
                        channelCount = 0,
                        categoryType = "vod",
                        gradientColorHex = colors[i % colors.size]
                    )
                    list.add(category)
                    currentBatch.add(category)
                    if (currentBatch.size >= batchSize) {
                        onBatch(currentBatch.toList())
                        currentBatch.clear()
                    }
                }
            }
            if (currentBatch.isNotEmpty()) {
                onBatch(currentBatch.toList())
                currentBatch.clear()
            }
            if (list.isNotEmpty()) putInCache(cacheKey, list)
            list
        } catch (e: Exception) {
            Log.e("XtreamApiClient", "Error fetching VOD categories: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchSeriesCategories(
        batchSize: Int = 20,
        onBatch: suspend (List<ChannelCategory>) -> Unit = {}
    ): List<ChannelCategory> = withContext(Dispatchers.IO) {
        val cacheKey = "series_cats_${serverHost}_$username"
        getFromCache<List<ChannelCategory>>(cacheKey)?.let {
            if (it.isNotEmpty()) onBatch(it)
            return@withContext it
        }

        try {
            val url = "${getBaseUrl()}&action=get_series_categories"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val jsonStr = response.body?.string().orEmpty()
            if (jsonStr.isBlank()) return@withContext emptyList()

            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<ChannelCategory>()
            val currentBatch = mutableListOf<ChannelCategory>()
            val colors = listOf("#7C3AED", "#059669", "#2563EB", "#E11D48", "#D97706")

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val catId = obj.optString("category_id", "")
                val catName = obj.optString("category_name", "قسم مسلسلات")
                if (catId.isNotEmpty()) {
                    val category = ChannelCategory(
                        id = "series_$catId",
                        name = catName,
                        subtitle = "مسلسلات حصرية",
                        channelCount = 0,
                        categoryType = "series",
                        gradientColorHex = colors[i % colors.size]
                    )
                    list.add(category)
                    currentBatch.add(category)
                    if (currentBatch.size >= batchSize) {
                        onBatch(currentBatch.toList())
                        currentBatch.clear()
                    }
                }
            }
            if (currentBatch.isNotEmpty()) {
                onBatch(currentBatch.toList())
                currentBatch.clear()
            }
            if (list.isNotEmpty()) putInCache(cacheKey, list)
            list
        } catch (e: Exception) {
            Log.e("XtreamApiClient", "Error fetching Series categories: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchLiveStreams(
        categoryId: String? = null,
        limit: Int = -1,
        batchSize: Int = 25,
        onBatch: suspend (List<ChannelItem>) -> Unit = {}
    ): List<ChannelItem> = withContext(Dispatchers.IO) {
        val cacheKey = "live_streams_${serverHost}_${categoryId}_$limit"
        getFromCache<List<ChannelItem>>(cacheKey)?.let {
            if (it.isNotEmpty()) onBatch(it)
            return@withContext it
        }

        try {
            val url = if (categoryId.isNullOrBlank()) {
                "${getBaseUrl()}&action=get_live_streams"
            } else {
                "${getBaseUrl()}&action=get_live_streams&category_id=$categoryId"
            }
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val jsonStr = response.body?.string().orEmpty()
            if (jsonStr.isBlank()) return@withContext emptyList()

            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<ChannelItem>()
            val currentBatch = mutableListOf<ChannelItem>()
            val maxItems = if (limit > 0) minOf(limit, jsonArray.length()) else jsonArray.length()

            for (i in 0 until maxItems) {
                val obj = jsonArray.getJSONObject(i)
                val streamId = obj.optString("stream_id", "")
                val name = obj.optString("name", "قناة")
                val icon = obj.optString("stream_icon", "")
                val catId = obj.optString("category_id", categoryId ?: "")
                val num = obj.optInt("num", i + 1)

                if (streamId.isNotEmpty()) {
                    val streamUrl = "${serverHost.trimEnd('/')}/live/$username/$password/$streamId.m3u8"
                    val backupUrl = "${serverHost.trimEnd('/')}/live/$username/$password/$streamId.ts"
                    val channel = ChannelItem(
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
                    list.add(channel)
                    currentBatch.add(channel)
                    if (currentBatch.size >= batchSize) {
                        onBatch(currentBatch.toList())
                        currentBatch.clear()
                    }
                }
            }
            if (currentBatch.isNotEmpty()) {
                onBatch(currentBatch.toList())
                currentBatch.clear()
            }
            if (list.isNotEmpty()) putInCache(cacheKey, list)
            list
        } catch (e: Exception) {
            Log.e("XtreamApiClient", "Error fetching live streams: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchVodStreams(
        categoryId: String? = null,
        limit: Int = -1,
        batchSize: Int = 25,
        onBatch: suspend (List<MediaItem>) -> Unit = {}
    ): List<MediaItem> = withContext(Dispatchers.IO) {
        val cacheKey = "vod_streams_${serverHost}_${categoryId}_$limit"
        getFromCache<List<MediaItem>>(cacheKey)?.let {
            if (it.isNotEmpty()) onBatch(it)
            return@withContext it
        }
        try {
            val cleanCatId = categoryId?.removePrefix("vod_")
            val url = if (cleanCatId.isNullOrBlank()) {
                "${getBaseUrl()}&action=get_vod_streams"
            } else {
                "${getBaseUrl()}&action=get_vod_streams&category_id=$cleanCatId"
            }
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val jsonStr = response.body?.string().orEmpty()
            if (jsonStr.isBlank()) return@withContext emptyList()

            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<MediaItem>()
            val currentBatch = mutableListOf<MediaItem>()
            val maxItems = if (limit > 0) minOf(limit, jsonArray.length()) else jsonArray.length()

            for (i in 0 until maxItems) {
                val obj = jsonArray.getJSONObject(i)
                val streamId = obj.optString("stream_id", "")
                val name = obj.optString("name", "فيلم")
                val icon = obj.optString("stream_icon", "")
                val rating = obj.optString("rating", "8.5")
                val container = obj.optString("container_extension", "mp4").ifEmpty { "mp4" }

                if (streamId.isNotEmpty()) {
                    val streamUrl = "${serverHost.trimEnd('/')}/movie/$username/$password/$streamId.$container"
                    val movie = MediaItem(
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
                    list.add(movie)
                    currentBatch.add(movie)
                    if (currentBatch.size >= batchSize) {
                        onBatch(currentBatch.toList())
                        currentBatch.clear()
                    }
                }
            }
            if (currentBatch.isNotEmpty()) {
                onBatch(currentBatch.toList())
                currentBatch.clear()
            }
            if (list.isNotEmpty()) putInCache(cacheKey, list)
            list
        } catch (e: Exception) {
            Log.e("XtreamApiClient", "Error fetching VOD streams: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchSeries(
        categoryId: String? = null,
        limit: Int = -1,
        batchSize: Int = 25,
        onBatch: suspend (List<MediaItem>) -> Unit = {}
    ): List<MediaItem> = withContext(Dispatchers.IO) {
        val cacheKey = "series_streams_${serverHost}_${categoryId}_$limit"
        getFromCache<List<MediaItem>>(cacheKey)?.let {
            if (it.isNotEmpty()) onBatch(it)
            return@withContext it
        }

        try {
            val cleanCatId = categoryId?.removePrefix("series_")
            val url = if (cleanCatId.isNullOrBlank()) {
                "${getBaseUrl()}&action=get_series"
            } else {
                "${getBaseUrl()}&action=get_series&category_id=$cleanCatId"
            }
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val jsonStr = response.body?.string().orEmpty()
            if (jsonStr.isBlank()) return@withContext emptyList()

            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<MediaItem>()
            val currentBatch = mutableListOf<MediaItem>()
            val maxItems = if (limit > 0) minOf(limit, jsonArray.length()) else jsonArray.length()

            for (i in 0 until maxItems) {
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
                    val seriesItem = MediaItem(
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
                        isFavorite = false,
                        rawSeriesId = seriesId
                    )
                    list.add(seriesItem)
                    currentBatch.add(seriesItem)
                    if (currentBatch.size >= batchSize) {
                        onBatch(currentBatch.toList())
                        currentBatch.clear()
                    }
                }
            }
            if (currentBatch.isNotEmpty()) {
                onBatch(currentBatch.toList())
                currentBatch.clear()
            }
            if (list.isNotEmpty()) putInCache(cacheKey, list)
            list
        } catch (e: Exception) {
            Log.e("XtreamApiClient", "Error fetching series: ${e.message}", e)
            emptyList()
        }
    }

    fun copyClient(host: String = serverHost, user: String = username, pass: String = password): XtreamApiClient {
        return XtreamApiClient(host, user, pass)
    }

    suspend fun fetchSeriesDetails(seriesId: String): com.example.data.model.SeriesDetail? = withContext(Dispatchers.IO) {
        val cacheKey = "series_detail_${serverHost}_$seriesId"
        getFromCache<com.example.data.model.SeriesDetail>(cacheKey)?.let { return@withContext it }

        try {
            val cleanId = seriesId.removePrefix("xt_ser_")
            val url = "${getBaseUrl()}&action=get_series_info&series_id=$cleanId"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val jsonStr = response.body?.string().orEmpty()
            if (jsonStr.isBlank() || !jsonStr.startsWith("{")) return@withContext null

            val rootObj = org.json.JSONObject(jsonStr)
            val infoObj = rootObj.optJSONObject("info")
            val title = infoObj?.optString("name", "تفاصيل العمل") ?: "مسلسل"
            val cover = infoObj?.optString("cover", "") ?: ""
            val backdrop = if (infoObj?.has("backdrop_path") == true) {
                val bdArr = infoObj.optJSONArray("backdrop_path")
                if (bdArr != null && bdArr.length() > 0) bdArr.optString(0, "") else infoObj.optString("backdrop_path", cover)
            } else cover
            val plot = infoObj?.optString("plot", "") ?: ""
            val genre = infoObj?.optString("genre", "دراما") ?: ""
            val releaseDate = infoObj?.optString("releaseDate", "2024") ?: "2024"
            val rating = infoObj?.optString("rating", "8.9") ?: "8.9"

            val seasonsMap = mutableMapOf<Int, MutableList<com.example.data.model.EpisodeItem>>()
            val episodesObj = rootObj.optJSONObject("episodes")

            if (episodesObj != null) {
                val keys = episodesObj.keys()
                while (keys.hasNext()) {
                    val seasonKey = keys.next()
                    val seasonNum = seasonKey.toIntOrNull() ?: 1
                    val epArray = episodesObj.optJSONArray(seasonKey)

                    if (epArray != null) {
                        for (j in 0 until epArray.length()) {
                            val epObj = epArray.getJSONObject(j)
                            val epId = epObj.optString("id", "")
                            val epNum = epObj.optInt("episode_num", j + 1)
                            val epTitle = epObj.optString("title", "الحلقة $epNum")
                            val ext = epObj.optString("container_extension", "mp4").ifEmpty { "mp4" }
                            val epInfo = epObj.optJSONObject("info")
                            val epDuration = epInfo?.optString("duration", "45:00") ?: "45 دقيقة"
                            val epPlot = epInfo?.optString("plot", "") ?: ""
                            val epCover = epInfo?.optString("movie_image", cover) ?: cover

                            val streamUrl = "${serverHost.trimEnd('/')}/series/$username/$password/$epId.$ext"
                            val epItem = com.example.data.model.EpisodeItem(
                                id = epId,
                                episodeNum = epNum,
                                title = if (epTitle.isNotBlank() && epTitle != "null") epTitle else "الحلقة $epNum",
                                seasonNum = seasonNum,
                                containerExtension = ext,
                                duration = epDuration,
                                overview = epPlot,
                                coverUrl = epCover,
                                streamUrl = streamUrl
                            )
                            seasonsMap.getOrPut(seasonNum) { mutableListOf() }.add(epItem)
                        }
                    }
                }
            } else if (rootObj.optJSONArray("episodes") != null) {
                val epArray = rootObj.getJSONArray("episodes")
                for (j in 0 until epArray.length()) {
                    val epObj = epArray.getJSONObject(j)
                    val epId = epObj.optString("id", "")
                    val seasonNum = epObj.optInt("season", epObj.optInt("season_num", 1))
                    val epNum = epObj.optInt("episode_num", j + 1)
                    val epTitle = epObj.optString("title", "الحلقة $epNum")
                    val ext = epObj.optString("container_extension", "mp4").ifEmpty { "mp4" }
                    val epInfo = epObj.optJSONObject("info")
                    val epDuration = epInfo?.optString("duration", "45:00") ?: "45 دقيقة"
                    val epPlot = epInfo?.optString("plot", "") ?: ""
                    val epCover = epInfo?.optString("movie_image", cover) ?: cover

                    val streamUrl = "${serverHost.trimEnd('/')}/series/$username/$password/$epId.$ext"
                    val epItem = com.example.data.model.EpisodeItem(
                        id = epId,
                        episodeNum = epNum,
                        title = if (epTitle.isNotBlank() && epTitle != "null") epTitle else "الحلقة $epNum",
                        seasonNum = seasonNum,
                        containerExtension = ext,
                        duration = epDuration,
                        overview = epPlot,
                        coverUrl = epCover,
                        streamUrl = streamUrl
                    )
                    seasonsMap.getOrPut(seasonNum) { mutableListOf() }.add(epItem)
                }
            }

            val seasonsList = seasonsMap.map { (seasonNum, episodes) ->
                com.example.data.model.SeasonItem(
                    seasonNumber = seasonNum,
                    name = "الموسم $seasonNum",
                    episodeCount = episodes.size,
                    episodes = episodes.sortedBy { it.episodeNum },
                    airDate = releaseDate,
                    coverUrl = cover
                )
            }.sortedBy { it.seasonNumber }

            val detail = com.example.data.model.SeriesDetail(
                id = seriesId,
                title = title,
                coverUrl = cover,
                backdropUrl = backdrop,
                plot = plot,
                genre = genre,
                releaseDate = releaseDate,
                rating = rating,
                seasons = seasonsList
            )
            putInCache(cacheKey, detail)
            detail
        } catch (e: Exception) {
            Log.e("XtreamApiClient", "Error parsing series details: ${e.message}", e)
            null
        }
    }
}
