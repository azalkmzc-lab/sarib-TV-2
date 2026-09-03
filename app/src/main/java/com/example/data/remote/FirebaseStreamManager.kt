package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.model.ContentType
import com.example.data.model.HeroBannerItem
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import com.example.util.M3uPlaylistParser
import com.example.util.ParsedM3uResult

data class RemoteStreamConfig(
    val serverHost: String = "http://cliccck52258.club:2082",
    val username: String = "khaledsliman",
    val password: String = "755246419856",
    val matchesApiUrl: String = "https://bab-elmoshahd.online/api/index.php?path=matches&day=",
    val m3uPlaylistUrl: String = "https://github.com/zezo81795-cell/IO/raw/refs/heads/main/BEINSPORTS.M3U",
    val announcement: String = "",
    val telegramLink: String = "https://t.me/sarib_tv",
    val heroTitle: String = "بلدة الضياع S1-S4",
    val heroSubtitle: String = "مسلسل • دراما • رعب • أحجية",
    val heroStreamUrl: String = "http://cliccck52258.club:2082/series/khaledsliman/755246419856/1.mp4"
)

data class RemoteM3uSource(
    val id: String,
    val name: String,
    val url: String,
    val isEnabled: Boolean = true
)

class FirebaseStreamManager(private val context: Context) {

    private val TAG = "FirebaseStreamManager"
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private fun isFirebaseAvailable(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchRemoteConfig(): RemoteStreamConfig = withContext(Dispatchers.IO) {
        // Strategy 1: Try Firebase Firestore (stream_config/main_config)
        if (isFirebaseAvailable()) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val docSnapshot = firestore.collection("stream_config")
                    .document("main_config")
                    .get()
                    .await()

                if (docSnapshot != null && docSnapshot.exists()) {
                    Log.i(TAG, "Loaded stream configuration from Firebase Firestore.")
                    return@withContext RemoteStreamConfig(
                        serverHost = docSnapshot.getString("server_host") ?: "http://cliccck52258.club:2082",
                        username = docSnapshot.getString("username") ?: "khaledsliman",
                        password = docSnapshot.getString("password") ?: "755246419856",
                        matchesApiUrl = docSnapshot.getString("matches_api_url") ?: "https://bab-elmoshahd.online/api/index.php?path=matches&day=",
                        m3uPlaylistUrl = docSnapshot.getString("m3u_playlist_url") 
                            ?: docSnapshot.getString("m3u_url") 
                            ?: "https://github.com/zezo81795-cell/IO/raw/refs/heads/main/BEINSPORTS.M3U",
                        announcement = docSnapshot.getString("announcement") ?: "",
                        telegramLink = docSnapshot.getString("telegram_link") ?: "https://t.me/sarib_tv",
                        heroTitle = docSnapshot.getString("hero_title") ?: "بلدة الضياع S1-S4",
                        heroSubtitle = docSnapshot.getString("hero_subtitle") ?: "مسلسل • دراما • رعب • أحجية",
                        heroStreamUrl = docSnapshot.getString("hero_stream_url") ?: "http://cliccck52258.club:2082/series/khaledsliman/755246419856/1.mp4"
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firestore fetch failed, checking Realtime Database: ${e.message}")
            }
        }

        // Strategy 2: Try Firebase Realtime Database (iptvpro-f5172-default-rtdb)
        try {
            val rtdbUrls = listOf(
                "https://iptvpro-f5172-default-rtdb.firebaseio.com/stream_config.json",
                "https://iptvpro-f5172-default-rtdb.firebaseio.com/.json"
            )
            for (url in rtdbUrls) {
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                val body = response.body?.string().orEmpty().trim()
                if (body.isNotEmpty() && body != "null" && body.startsWith("{")) {
                    val json = JSONObject(body)
                    val targetObj = if (json.has("stream_config") && json.optJSONObject("stream_config") != null) {
                        json.getJSONObject("stream_config")
                    } else {
                        json
                    }

                    if (targetObj.has("server_host") || targetObj.has("username")) {
                        Log.i(TAG, "Loaded stream configuration from Firebase Realtime Database.")
                        return@withContext RemoteStreamConfig(
                            serverHost = targetObj.optString("server_host", "http://cliccck52258.club:2082"),
                            username = targetObj.optString("username", "khaledsliman"),
                            password = targetObj.optString("password", "755246419856"),
                            matchesApiUrl = targetObj.optString("matches_api_url", "https://bab-elmoshahd.online/api/index.php?path=matches&day="),
                            m3uPlaylistUrl = targetObj.optString("m3u_playlist_url", targetObj.optString("m3u_url", "https://github.com/zezo81795-cell/IO/raw/refs/heads/main/BEINSPORTS.M3U")),
                            announcement = targetObj.optString("announcement", ""),
                            telegramLink = targetObj.optString("telegram_link", "https://t.me/sarib_tv"),
                            heroTitle = targetObj.optString("hero_title", "بلدة الضياع S1-S4"),
                            heroSubtitle = targetObj.optString("hero_subtitle", "مسلسل • دراما • رعب • أحجية"),
                            heroStreamUrl = targetObj.optString("hero_stream_url", "http://cliccck52258.club:2082/series/khaledsliman/755246419856/1.mp4")
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "RTDB fetch fallback error: ${e.message}")
        }

        // Strategy 3: Default fallback
        RemoteStreamConfig()
    }

    /**
     * Fetches dynamic sliders from Firebase Firestore collection 'sliders' or Realtime Database path '/sliders.json'.
     * This path is independent from the Xtream account and allows managing multiple custom sliders from the HTML dashboard.
     */
    suspend fun fetchSliders(): List<HeroBannerItem> = withContext(Dispatchers.IO) {
        val resultList = mutableListOf<HeroBannerItem>()

        // 1. Try Firebase Firestore ('sliders' collection)
        if (isFirebaseAvailable()) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("sliders")
                    .get()
                    .await()

                if (snapshot != null && !snapshot.isEmpty) {
                    for (doc in snapshot.documents) {
                        val isActive = doc.getBoolean("isActive") ?: doc.getBoolean("is_active") ?: true
                        if (!isActive) continue

                        val id = doc.id
                        val title = doc.getString("title").orEmpty()
                        val subtitle = doc.getString("subtitle").orEmpty()
                        val backdropUrl = doc.getString("backdropUrl") 
                            ?: doc.getString("backdrop_url") 
                            ?: doc.getString("imageUrl") 
                            ?: doc.getString("image_url") 
                            ?: ""
                        val badge = doc.getString("badge") ?: "حصري"
                        val streamUrl = doc.getString("streamUrl") ?: doc.getString("stream_url") ?: ""
                        val isLive = doc.getBoolean("isLive") ?: doc.getBoolean("is_live") ?: false
                        val sortOrder = doc.getLong("sortOrder")?.toInt() 
                            ?: doc.getLong("sort_order")?.toInt() 
                            ?: 0
                        val typeStr = doc.getString("contentType") ?: doc.getString("type") ?: "SERIES"
                        val contentType = when (typeStr.uppercase()) {
                            "MOVIE" -> ContentType.MOVIE
                            "CHANNEL", "LIVE" -> ContentType.CHANNEL
                            "MATCH" -> ContentType.MATCH
                            else -> ContentType.SERIES
                        }
                        
                        val tagsList = mutableListOf<String>()
                        val tagsObj = doc.get("genreTags") ?: doc.get("tags")
                        if (tagsObj is List<*>) {
                            tagsList.addAll(tagsObj.mapNotNull { it?.toString() })
                        } else if (tagsObj is String && tagsObj.isNotEmpty()) {
                            tagsList.addAll(tagsObj.split(",", "•", "-").map { it.trim() })
                        }
                        if (tagsList.isEmpty()) {
                            tagsList.addAll(listOf("مميز", "عالي الدقة"))
                        }

                        if (title.isNotEmpty()) {
                            resultList.add(
                                HeroBannerItem(
                                    id = id,
                                    title = title,
                                    subtitle = subtitle,
                                    backdropUrl = backdropUrl,
                                    badge = badge,
                                    genreTags = tagsList,
                                    streamUrl = streamUrl,
                                    contentType = contentType,
                                    isLive = isLive,
                                    sortOrder = sortOrder,
                                    isActive = true
                                )
                            )
                        }
                    }

                    if (resultList.isNotEmpty()) {
                        Log.i(TAG, "Loaded ${resultList.size} sliders from Firestore.")
                        return@withContext resultList.sortedBy { it.sortOrder }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load sliders from Firestore: ${e.message}")
            }
        }

        // 2. Try Firebase Realtime Database path '/sliders.json'
        try {
            val url = "https://iptvpro-f5172-default-rtdb.firebaseio.com/sliders.json"
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string().orEmpty().trim()
            
            if (body.isNotEmpty() && body != "null") {
                if (body.startsWith("[")) {
                    val jsonArray = JSONArray(body)
                    for (i in 0 until jsonArray.length()) {
                        val itemObj = jsonArray.optJSONObject(i) ?: continue
                        parseSliderJson(itemObj, "slider_$i")?.let { resultList.add(it) }
                    }
                } else if (body.startsWith("{")) {
                    val jsonObj = JSONObject(body)
                    val keys = jsonObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val itemObj = jsonObj.optJSONObject(key) ?: continue
                        parseSliderJson(itemObj, key)?.let { resultList.add(it) }
                    }
                }

                if (resultList.isNotEmpty()) {
                    Log.i(TAG, "Loaded ${resultList.size} sliders from RTDB.")
                    return@withContext resultList.sortedBy { it.sortOrder }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "RTDB sliders fetch error: ${e.message}")
        }

        resultList
    }

    private fun parseSliderJson(obj: JSONObject, defaultId: String): HeroBannerItem? {
        val isActive = obj.optBoolean("isActive", obj.optBoolean("is_active", true))
        if (!isActive) return null

        val id = obj.optString("id", defaultId)
        val title = obj.optString("title", "")
        if (title.isEmpty()) return null

        // Check if slider has expired based on duration and createdAt
        val duration = obj.optDouble("duration", 0.0)
        val createdAt = obj.optLong("createdAt", 0L)
        if (duration > 0 && createdAt > 0) {
            val expiryTime = createdAt + (duration * 60 * 60 * 1000).toLong()
            if (System.currentTimeMillis() > expiryTime) {
                return null
            }
        }

        val typeRaw = obj.optString("type", obj.optString("contentType", "normal")).lowercase()
        val isLive = typeRaw == "match" || obj.optBoolean("isLive", obj.optBoolean("is_live", false))

        val contentType = when {
            typeRaw == "movie" -> ContentType.MOVIE
            typeRaw == "match" -> ContentType.MATCH
            typeRaw == "channel" || typeRaw == "live" -> ContentType.CHANNEL
            else -> ContentType.SERIES
        }

        // Subtitle logic based on type and fields
        val subtitle = when {
            obj.has("subtitle") && obj.optString("subtitle").isNotEmpty() -> obj.optString("subtitle")
            typeRaw == "match" -> {
                val matchTime = obj.optLong("matchTime", 0L)
                if (matchTime > 0) "بث مباشر • انطلاق المباراة" else "بث مباشر للمباراة"
            }
            typeRaw == "movie" -> {
                val rating = obj.optString("movieRating", "")
                if (rating.isNotEmpty()) "فيلم • تقييم ⭐ $rating/10" else "فيلم سينمائي"
            }
            else -> "عرض مميز بدقة عالية"
        }

        // Image / Backdrop URL
        val backdropUrl = obj.optString(
            "image",
            obj.optString(
                "backdropUrl",
                obj.optString("backdrop_url", obj.optString("imageUrl", obj.optString("image_url", "")))
            )
        )

        // Badge
        val badge = when {
            obj.has("badge") && obj.optString("badge").isNotEmpty() -> obj.optString("badge")
            typeRaw == "match" -> "مباشر LIVE"
            typeRaw == "movie" -> "فيلم"
            else -> "مميز"
        }

        // Stream URL resolution: server1..server5, url, movieUrl, streamUrl
        val streamUrl = listOf(
            obj.optString("server1", ""),
            obj.optString("server2", ""),
            obj.optString("server3", ""),
            obj.optString("server4", ""),
            obj.optString("server5", ""),
            obj.optString("movieUrl", ""),
            obj.optString("streamUrl", ""),
            obj.optString("stream_url", ""),
            obj.optString("url", "")
        ).firstOrNull { it.isNotBlank() } ?: ""

        val sortOrder = obj.optInt("sortOrder", obj.optInt("sort_order", obj.optInt("order", 0)))

        val tagsList = mutableListOf<String>()
        val tagsStr = obj.optString("genreTags", obj.optString("tags", ""))
        if (tagsStr.isNotEmpty()) {
            tagsList.addAll(tagsStr.split(",", "•", "-").map { it.trim() }.filter { it.isNotEmpty() })
        }
        if (tagsList.isEmpty()) {
            when (typeRaw) {
                "match" -> tagsList.addAll(listOf("مباراة", "بث مباشر", "HD"))
                "movie" -> tagsList.addAll(listOf("فيلم", "سينما", "Full HD"))
                else -> tagsList.addAll(listOf("مميز", "HD"))
            }
        }

        return HeroBannerItem(
            id = id,
            title = title,
            subtitle = subtitle,
            backdropUrl = backdropUrl,
            badge = badge,
            genreTags = tagsList,
            streamUrl = streamUrl,
            contentType = contentType,
            isLive = isLive,
            sortOrder = sortOrder,
            isActive = true
        )
    }

    suspend fun fetchCustomCategories(): List<com.example.data.model.ChannelCategory> = withContext(Dispatchers.IO) {
        val list = mutableListOf<com.example.data.model.ChannelCategory>()
        try {
            val url = "https://iptvpro-f5172-default-rtdb.firebaseio.com/categories.json"
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string().orEmpty().trim()
            if (body.isNotEmpty() && body != "null" && body.startsWith("{")) {
                val jsonObj = JSONObject(body)
                val keys = jsonObj.keys()
                var index = 0
                val colors = listOf("#0088FF", "#9333EA", "#2563EB", "#059669", "#DC2626", "#D97706")
                while (keys.hasNext()) {
                    val key = keys.next()
                    val obj = jsonObj.optJSONObject(key) ?: continue
                    val name = obj.optString("name", "قسم خاص")
                    val poster = obj.optString("poster", obj.optString("iconUrl", ""))
                    list.add(
                        com.example.data.model.ChannelCategory(
                            id = key,
                            name = name,
                            subtitle = "قسم سحابي مخصص",
                            channelCount = 0,
                            iconUrl = poster,
                            categoryType = "custom",
                            gradientColorHex = colors[index % colors.size]
                        )
                    )
                    index++
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Custom categories fetch error: ${e.message}")
        }
        list
    }

    suspend fun fetchCustomChannels(): List<com.example.data.model.ChannelItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<com.example.data.model.ChannelItem>()
        try {
            val url = "https://iptvpro-f5172-default-rtdb.firebaseio.com/channels.json"
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string().orEmpty().trim()
            if (body.isNotEmpty() && body != "null" && body.startsWith("{")) {
                val jsonObj = JSONObject(body)
                val keys = jsonObj.keys()
                var sort = 1
                while (keys.hasNext()) {
                    val key = keys.next()
                    val obj = jsonObj.optJSONObject(key) ?: continue
                    val name = obj.optString("name", "قناة خاصة")
                    val catId = obj.optString("categoryId", obj.optString("category_id", "custom"))
                    val logo = obj.optString("logo", obj.optString("logoUrl", ""))
                    val s1 = obj.optString("server1", "")
                    val s2 = obj.optString("server2", "")
                    val urlDirect = obj.optString("url", "")
                    val mpd = obj.optString("mpd", "")
                    val streamUrl = listOf(s1, s2, mpd, urlDirect).firstOrNull { it.isNotBlank() } ?: ""
                    val backupUrl = if (s2.isNotBlank() && s2 != streamUrl) s2 else urlDirect

                    if (streamUrl.isNotBlank()) {
                        list.add(
                            com.example.data.model.ChannelItem(
                                id = "fb_ch_$key",
                                name = name,
                                categoryId = catId,
                                categoryName = "باقة البث المباشر السحابي",
                                logoUrl = logo,
                                streamUrl = streamUrl,
                                backupUrl = backupUrl,
                                country = "سحابي Cloud",
                                language = "العربية",
                                isFavorite = false,
                                isEnabled = true,
                                sortOrder = sort++,
                                viewsCount = (500..3000).random()
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Custom channels fetch error: ${e.message}")
        }
        list
    }

    suspend fun fetchM3uSources(): List<RemoteM3uSource> = withContext(Dispatchers.IO) {
        val list = mutableListOf<RemoteM3uSource>()
        try {
            val url = "https://iptvpro-f5172-default-rtdb.firebaseio.com/m3u_playlists.json"
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string().orEmpty().trim()
            if (body.isNotEmpty() && body != "null" && body.startsWith("{")) {
                val jsonObj = JSONObject(body)
                val keys = jsonObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val obj = jsonObj.optJSONObject(key) ?: continue
                    val name = obj.optString("name", obj.optString("title", "باقة قنوات M3U"))
                    val playlistUrl = obj.optString("url", obj.optString("playlist_url", obj.optString("streamUrl", "")))
                    val isEnabled = obj.optBoolean("enabled", obj.optBoolean("isEnabled", true))
                    if (playlistUrl.isNotBlank() && isEnabled) {
                        list.add(RemoteM3uSource(id = key, name = name, url = playlistUrl, isEnabled = isEnabled))
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "M3U sources fetch error: ${e.message}")
        }
        list
    }

    suspend fun fetchM3uPlaylist(url: String, defaultName: String = "باقة القنوات المباشرة"): ParsedM3uResult {
        return M3uPlaylistParser.parseFromUrl(url, defaultName)
    }

    suspend fun fetchCustomMovies(): List<com.example.data.model.MediaItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<com.example.data.model.MediaItem>()
        try {
            val urls = listOf(
                "https://iptvpro-f5172-default-rtdb.firebaseio.com/movies.json",
                "https://iptvpro-f5172-default-rtdb.firebaseio.com/custom_movies.json"
            )
            for (url in urls) {
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                val body = response.body?.string().orEmpty().trim()
                if (body.isNotEmpty() && body != "null" && body.startsWith("{")) {
                    val jsonObj = JSONObject(body)
                    val keys = jsonObj.keys()
                    var i = 0
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val obj = jsonObj.optJSONObject(key) ?: continue
                        val title = obj.optString("title", obj.optString("name", "فيلم"))
                        val poster = obj.optString("poster", obj.optString("posterUrl", obj.optString("image", "")))
                        val rating = obj.optString("rating", "8.9")
                        val year = obj.optString("year", "2024")
                        val story = obj.optString("story", obj.optString("description", "فيلم مخصص عبر لوحة التحكم"))
                        val streamUrl = listOf(
                            obj.optString("server1", ""),
                            obj.optString("server2", ""),
                            obj.optString("streamUrl", ""),
                            obj.optString("url", "")
                        ).firstOrNull { it.isNotBlank() } ?: ""

                        if (title.isNotBlank() && streamUrl.isNotBlank()) {
                            list.add(
                                com.example.data.model.MediaItem(
                                    id = "fb_mov_$key",
                                    title = title,
                                    posterUrl = poster,
                                    backdropUrl = poster,
                                    type = ContentType.MOVIE,
                                    year = year,
                                    rating = rating,
                                    genre = "أفلام سينما",
                                    description = story,
                                    duration = "120 دقيقة",
                                    streamUrl = streamUrl,
                                    isTop = i < 5,
                                    topRank = String.format("%02d", i + 1),
                                    isFavorite = false
                                )
                            )
                            i++
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Custom movies fetch error: ${e.message}")
        }
        list
    }
}
