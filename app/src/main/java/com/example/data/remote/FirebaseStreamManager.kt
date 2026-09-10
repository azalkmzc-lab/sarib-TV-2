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

data class XtreamAccount(
    val serverHost: String = "http://cliccck52258.club:2082",
    val username: String = "khaledsliman",
    val password: String = "755246419856",
    val name: String = "الافتراضي"
)

data class RemoteStreamConfig(
    val serverHost: String = "http://cliccck52258.club:2082",
    val username: String = "khaledsliman",
    val password: String = "755246419856",
    val seriesAccount: XtreamAccount = XtreamAccount(
        serverHost = "http://cliccck52258.club:2082",
        username = "khaledsliman",
        password = "755246419856",
        name = "حساب المسلسلات"
    ),
    val vodAccount: XtreamAccount = XtreamAccount(
        serverHost = "http://cliccck52258.club:2082",
        username = "khaledsliman",
        password = "755246419856",
        name = "حساب الأفلام"
    ),
    val liveXtreamAccount: XtreamAccount = XtreamAccount(
        serverHost = "http://cliccck52258.club:2082",
        username = "khaledsliman",
        password = "755246419856",
        name = "حساب القنوات المباشرة"
    ),
    val isLiveXtreamEnabled: Boolean = false,
    val channelsApiUrl: String = "",
    val isChannelsApiEnabled: Boolean = false,
    val newsApiUrl: String = "",
    val isNewsApiEnabled: Boolean = false,
    val seriesCategoriesAccounts: Map<String, XtreamAccount> = emptyMap(),
    val vodCategoriesAccounts: Map<String, XtreamAccount> = emptyMap(),
    val matchesApiUrl: String = "https://bab-elmoshahd.online/api/index.php?path=matches&day=",
    val apiFootballKey: String = "0f0396f63d80f2bad18ec0e706985c88",
    val m3uPlaylistUrl: String = "https://github.com/zezo81795-cell/IO/raw/refs/heads/main/BEINSPORTS.M3U",
    val m3uMoviesUrl: String = "",
    val moviesApiUrl: String = "",
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
    private val httpClient: OkHttpClient by lazy {
        try {
            val trustAllCerts = arrayOf<javax.net.ssl.TrustManager>(object : javax.net.ssl.X509TrustManager {
                override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
            })
            val sslContext = javax.net.ssl.SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as javax.net.ssl.X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .build()
        } catch (e: Exception) {
            OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .build()
        }
    }

    private fun isFirebaseAvailable(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchRemoteConfig(): RemoteStreamConfig = withContext(Dispatchers.IO) {
        var baseConfig = RemoteStreamConfig()

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
                    val serverHost = docSnapshot.getString("server_host") ?: "http://cliccck52258.club:2082"
                    val username = docSnapshot.getString("username") ?: "khaledsliman"
                    val password = docSnapshot.getString("password") ?: "755246419856"

                    val seriesHost = docSnapshot.getString("series_server_host") ?: docSnapshot.getString("series_host") ?: serverHost
                    val seriesUser = docSnapshot.getString("series_username") ?: docSnapshot.getString("series_user") ?: username
                    val seriesPass = docSnapshot.getString("series_password") ?: docSnapshot.getString("series_pass") ?: password

                    val vodHost = docSnapshot.getString("vod_server_host") ?: docSnapshot.getString("movies_server_host") ?: serverHost
                    val vodUser = docSnapshot.getString("vod_username") ?: docSnapshot.getString("movies_username") ?: username
                    val vodPass = docSnapshot.getString("vod_password") ?: docSnapshot.getString("movies_password") ?: password

                    val liveHost = docSnapshot.getString("channels_server_host") ?: docSnapshot.getString("live_server_host") ?: docSnapshot.getString("live_host") ?: serverHost
                    val liveUser = docSnapshot.getString("channels_username") ?: docSnapshot.getString("live_username") ?: docSnapshot.getString("live_user") ?: username
                    val livePass = docSnapshot.getString("channels_password") ?: docSnapshot.getString("live_password") ?: docSnapshot.getString("live_pass") ?: password
                    val isLiveXtreamEnabled = docSnapshot.getBoolean("channels_xtream_enabled") 
                        ?: docSnapshot.getBoolean("live_xtream_enabled") 
                        ?: (docSnapshot.getString("channels_xtream_enabled") == "true")
                        ?: (docSnapshot.getString("live_xtream_enabled") == "true")
                        ?: false

                    val channelsApiUrl = docSnapshot.getString("channels_api_url") ?: docSnapshot.getString("channels_api") ?: ""
                    val isChannelsApiEnabled = docSnapshot.getBoolean("channels_api_enabled") 
                        ?: docSnapshot.getBoolean("is_channels_api_enabled") 
                        ?: (docSnapshot.getString("channels_api_enabled") == "true")
                        ?: channelsApiUrl.isNotBlank()

                    val newsApiUrl = docSnapshot.getString("news_api_url") ?: docSnapshot.getString("news_api") ?: ""
                    val isNewsApiEnabled = docSnapshot.getBoolean("news_api_enabled") 
                        ?: docSnapshot.getBoolean("is_news_api_enabled") 
                        ?: (docSnapshot.getString("news_api_enabled") == "true")
                        ?: newsApiUrl.isNotBlank()

                    baseConfig = RemoteStreamConfig(
                        serverHost = serverHost,
                        username = username,
                        password = password,
                        seriesAccount = XtreamAccount(serverHost = seriesHost, username = seriesUser, password = seriesPass, name = "سيرفر المسلسلات"),
                        vodAccount = XtreamAccount(serverHost = vodHost, username = vodUser, password = vodPass, name = "سيرفر الأفلام"),
                        liveXtreamAccount = XtreamAccount(serverHost = liveHost, username = liveUser, password = livePass, name = "سيرفر القنوات المباشرة"),
                        isLiveXtreamEnabled = isLiveXtreamEnabled,
                        channelsApiUrl = channelsApiUrl,
                        isChannelsApiEnabled = isChannelsApiEnabled,
                        newsApiUrl = newsApiUrl,
                        isNewsApiEnabled = isNewsApiEnabled,
                        matchesApiUrl = docSnapshot.getString("matches_api_url") ?: "https://bab-elmoshahd.online/api/index.php?path=matches&day=",
                        apiFootballKey = docSnapshot.getString("api_football_key") 
                            ?: docSnapshot.getString("football_api_key") 
                            ?: docSnapshot.getString("matches_api_key") 
                            ?: docSnapshot.getString("rapidapi_key") 
                            ?: "0f0396f63d80f2bad18ec0e706985c88",
                        m3uPlaylistUrl = docSnapshot.getString("m3u_playlist_url") 
                            ?: docSnapshot.getString("m3u_url") 
                            ?: "https://github.com/zezo81795-cell/IO/raw/refs/heads/main/BEINSPORTS.M3U",
                        m3uMoviesUrl = docSnapshot.getString("m3u_movies_url")
                            ?: docSnapshot.getString("movies_m3u_url") ?: "",
                        moviesApiUrl = docSnapshot.getString("movies_api_url")
                            ?: docSnapshot.getString("movies_api") ?: "",
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

                    if (targetObj.has("server_host") || targetObj.has("username") || targetObj.has("channels_api_url") || targetObj.has("live_server_host")) {
                        Log.i(TAG, "Loaded stream configuration from Firebase Realtime Database.")
                        val serverHost = targetObj.optString("server_host", baseConfig.serverHost)
                        val username = targetObj.optString("username", baseConfig.username)
                        val password = targetObj.optString("password", baseConfig.password)

                        val seriesHost = targetObj.optString("series_server_host", targetObj.optString("series_host", serverHost))
                        val seriesUser = targetObj.optString("series_username", targetObj.optString("series_user", username))
                        val seriesPass = targetObj.optString("series_password", targetObj.optString("series_pass", password))

                        val vodHost = targetObj.optString("vod_server_host", targetObj.optString("movies_server_host", serverHost))
                        val vodUser = targetObj.optString("vod_username", targetObj.optString("movies_username", username))
                        val vodPass = targetObj.optString("vod_password", targetObj.optString("movies_password", password))

                        val liveHost = targetObj.optString("channels_server_host", targetObj.optString("live_server_host", targetObj.optString("live_host", serverHost)))
                        val liveUser = targetObj.optString("channels_username", targetObj.optString("live_username", targetObj.optString("live_user", username)))
                        val livePass = targetObj.optString("channels_password", targetObj.optString("live_password", targetObj.optString("live_pass", password)))
                        val isLiveXtreamEnabled = targetObj.optBoolean("channels_xtream_enabled", targetObj.optBoolean("live_xtream_enabled", baseConfig.isLiveXtreamEnabled))

                        val channelsApiUrl = targetObj.optString("channels_api_url", targetObj.optString("channels_api", baseConfig.channelsApiUrl))
                        val isChannelsApiEnabled = targetObj.optBoolean("channels_api_enabled", targetObj.optBoolean("is_channels_api_enabled", channelsApiUrl.isNotBlank()))

                        val newsApiUrl = targetObj.optString("news_api_url", targetObj.optString("news_api", baseConfig.newsApiUrl))
                        val isNewsApiEnabled = targetObj.optBoolean("news_api_enabled", targetObj.optBoolean("is_news_api_enabled", newsApiUrl.isNotBlank()))

                        baseConfig = RemoteStreamConfig(
                            serverHost = serverHost,
                            username = username,
                            password = password,
                            seriesAccount = XtreamAccount(serverHost = seriesHost, username = seriesUser, password = seriesPass, name = "سيرفر المسلسلات"),
                            vodAccount = XtreamAccount(serverHost = vodHost, username = vodUser, password = vodPass, name = "سيرفر الأفلام"),
                            liveXtreamAccount = XtreamAccount(serverHost = liveHost, username = liveUser, password = livePass, name = "سيرفر القنوات المباشرة"),
                            isLiveXtreamEnabled = isLiveXtreamEnabled,
                            channelsApiUrl = channelsApiUrl,
                            isChannelsApiEnabled = isChannelsApiEnabled,
                            newsApiUrl = newsApiUrl,
                            isNewsApiEnabled = isNewsApiEnabled,
                            matchesApiUrl = targetObj.optString("matches_api_url", baseConfig.matchesApiUrl),
                            apiFootballKey = targetObj.optString("api_football_key", targetObj.optString("football_api_key", targetObj.optString("matches_api_key", targetObj.optString("rapidapi_key", baseConfig.apiFootballKey)))),
                            m3uPlaylistUrl = targetObj.optString("m3u_playlist_url", targetObj.optString("m3u_url", baseConfig.m3uPlaylistUrl)),
                            m3uMoviesUrl = targetObj.optString("m3u_movies_url", targetObj.optString("movies_m3u_url", baseConfig.m3uMoviesUrl)),
                            moviesApiUrl = targetObj.optString("movies_api_url", targetObj.optString("movies_api", baseConfig.moviesApiUrl)),
                            announcement = targetObj.optString("announcement", baseConfig.announcement),
                            telegramLink = targetObj.optString("telegram_link", baseConfig.telegramLink),
                            heroTitle = targetObj.optString("hero_title", baseConfig.heroTitle),
                            heroSubtitle = targetObj.optString("hero_subtitle", baseConfig.heroSubtitle),
                            heroStreamUrl = targetObj.optString("hero_stream_url", baseConfig.heroStreamUrl)
                        )
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "RTDB fetch fallback error: ${e.message}")
        }

        // Also fetch per-category accounts from RTDB & Firestore paths
        val seriesCatAccounts = fetchSeriesCategoriesAccounts()
        val vodCatAccounts = fetchVodCategoriesAccounts()

        baseConfig.copy(
            seriesCategoriesAccounts = seriesCatAccounts,
            vodCategoriesAccounts = vodCatAccounts
        )
    }

    /**
     * Fetches custom Xtream accounts assigned to specific series categories.
     * Path in Firebase: Firestore collection 'series_categories_accounts' or RTDB '/series_categories_accounts.json'.
     */
    suspend fun fetchSeriesCategoriesAccounts(): Map<String, XtreamAccount> = withContext(Dispatchers.IO) {
        val map = mutableMapOf<String, XtreamAccount>()

        // 1. Try Firestore
        if (isFirebaseAvailable()) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("series_categories_accounts").get().await()
                if (snapshot != null && !snapshot.isEmpty) {
                    for (doc in snapshot.documents) {
                        val catId = doc.id
                        val host = doc.getString("server_host") ?: doc.getString("host") ?: ""
                        val user = doc.getString("username") ?: doc.getString("user") ?: ""
                        val pass = doc.getString("password") ?: doc.getString("pass") ?: ""
                        val name = doc.getString("name") ?: "حساب $catId"
                        if (host.isNotBlank() && user.isNotBlank()) {
                            map[catId] = XtreamAccount(host, user, pass, name)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firestore series_categories_accounts error: ${e.message}")
            }
        }

        // 2. Try RTDB
        try {
            val url = "https://iptvpro-f5172-default-rtdb.firebaseio.com/series_categories_accounts.json"
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string().orEmpty().trim()
            if (body.isNotEmpty() && body != "null" && body.startsWith("{")) {
                val json = JSONObject(body)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val obj = json.optJSONObject(key)
                    if (obj != null) {
                        val host = obj.optString("server_host", obj.optString("host", ""))
                        val user = obj.optString("username", obj.optString("user", ""))
                        val pass = obj.optString("password", obj.optString("pass", ""))
                        val name = obj.optString("name", "حساب $key")
                        if (host.isNotBlank() && user.isNotBlank()) {
                            map[key] = XtreamAccount(host, user, pass, name)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "RTDB series_categories_accounts error: ${e.message}")
        }

        map
    }

    /**
     * Fetches custom Xtream accounts assigned to specific movie categories or dedicated movie server.
     * Path in Firebase: Firestore collection 'vod_categories_accounts' or RTDB '/vod_categories_accounts.json'.
     */
    suspend fun fetchVodCategoriesAccounts(): Map<String, XtreamAccount> = withContext(Dispatchers.IO) {
        val map = mutableMapOf<String, XtreamAccount>()

        // 1. Try Firestore
        if (isFirebaseAvailable()) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("vod_categories_accounts").get().await()
                if (snapshot != null && !snapshot.isEmpty) {
                    for (doc in snapshot.documents) {
                        val catId = doc.id
                        val host = doc.getString("server_host") ?: doc.getString("host") ?: ""
                        val user = doc.getString("username") ?: doc.getString("user") ?: ""
                        val pass = doc.getString("password") ?: doc.getString("pass") ?: ""
                        val name = doc.getString("name") ?: "حساب $catId"
                        if (host.isNotBlank() && user.isNotBlank()) {
                            map[catId] = XtreamAccount(host, user, pass, name)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firestore vod_categories_accounts error: ${e.message}")
            }
        }

        // 2. Try RTDB
        try {
            val url = "https://iptvpro-f5172-default-rtdb.firebaseio.com/vod_categories_accounts.json"
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string().orEmpty().trim()
            if (body.isNotEmpty() && body != "null" && body.startsWith("{")) {
                val json = JSONObject(body)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val obj = json.optJSONObject(key)
                    if (obj != null) {
                        val host = obj.optString("server_host", obj.optString("host", ""))
                        val user = obj.optString("username", obj.optString("user", ""))
                        val pass = obj.optString("password", obj.optString("pass", ""))
                        val name = obj.optString("name", "حساب $key")
                        if (host.isNotBlank() && user.isNotBlank()) {
                            map[key] = XtreamAccount(host, user, pass, name)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "RTDB vod_categories_accounts error: ${e.message}")
        }

        map
    }

    /**
     * Fetches dynamic sliders and match sliders from Firebase Firestore ('sliders', 'match_sliders')
     * and Realtime Database paths ('/sliders.json', '/match_sliders.json').
     */
    suspend fun fetchSliders(): List<HeroBannerItem> = withContext(Dispatchers.IO) {
        val resultList = mutableListOf<HeroBannerItem>()
        val seenIds = mutableSetOf<String>()

        // 1. Try Firebase Firestore ('sliders' & 'match_sliders' collections)
        if (isFirebaseAvailable()) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val collectionsToFetch = listOf("sliders", "match_sliders")
                
                for (colName in collectionsToFetch) {
                    try {
                        val snapshot = firestore.collection(colName).get().await()
                        if (snapshot != null && !snapshot.isEmpty) {
                            for (doc in snapshot.documents) {
                                val isActive = doc.getBoolean("isActive") ?: doc.getBoolean("is_active") ?: true
                                if (!isActive) continue

                                val id = doc.id
                                if (seenIds.contains(id)) continue
                                seenIds.add(id)

                                val homeTeam = doc.getString("home_team") ?: doc.getString("homeTeam") ?: doc.getString("team1").orEmpty()
                                val homeLogo = doc.getString("home_logo") ?: doc.getString("home_logo_url") ?: doc.getString("homeLogoUrl") ?: doc.getString("homePoster") ?: doc.getString("team1_logo").orEmpty()
                                val awayTeam = doc.getString("away_team") ?: doc.getString("awayTeam") ?: doc.getString("team2").orEmpty()
                                val awayLogo = doc.getString("away_logo") ?: doc.getString("away_logo_url") ?: doc.getString("awayLogoUrl") ?: doc.getString("awayPoster") ?: doc.getString("team2_logo").orEmpty()
                                val leagueName = doc.getString("league_name") ?: doc.getString("leagueName") ?: doc.getString("league") ?: doc.getString("tournament").orEmpty()
                                val leagueLogo = doc.getString("league_logo") ?: doc.getString("league_logo_url") ?: doc.getString("leagueLogoUrl") ?: doc.getString("leagueIconUrl").orEmpty()
                                val matchTime = doc.getString("match_time") ?: doc.getString("matchTime") ?: doc.getString("time") ?: doc.getString("kickoff").orEmpty()
                                val matchDate = doc.getString("match_date") ?: doc.getString("matchDate") ?: doc.getString("date").orEmpty()
                                val matchStatus = doc.getString("match_status") ?: doc.getString("status") ?: if (colName == "match_sliders") "لم تبدأ" else ""
                                val commentator = doc.getString("commentator") ?: doc.getString("commentary").orEmpty()
                                val channelName = doc.getString("channel_name") ?: doc.getString("channelName") ?: doc.getString("channel").orEmpty()
                                val homeScore = doc.getLong("home_score")?.toInt() ?: doc.getLong("homeScore")?.toInt() ?: 0
                                val awayScore = doc.getLong("away_score")?.toInt() ?: doc.getLong("awayScore")?.toInt() ?: 0

                                val typeStr = doc.getString("contentType") ?: doc.getString("type") ?: doc.getString("slider_type") ?: if (colName == "match_sliders" || homeTeam.isNotBlank()) "MATCH" else "SERIES"
                                val isMatch = colName == "match_sliders" || typeStr.uppercase() == "MATCH" || homeTeam.isNotBlank()

                                var title = doc.getString("title").orEmpty()
                                if (title.isEmpty() && isMatch && homeTeam.isNotBlank()) {
                                    title = "$homeTeam VS $awayTeam"
                                }
                                if (title.isEmpty()) continue

                                var subtitle = doc.getString("subtitle").orEmpty()
                                if (subtitle.isEmpty() && isMatch) {
                                    subtitle = if (leagueName.isNotBlank()) "$leagueName • $matchTime" else "مباراة قمة اليوم"
                                }

                                val posterUrl = doc.getString("poster_url") ?: doc.getString("posterUrl") ?: doc.getString("poster") ?: doc.getString("cover").orEmpty()
                                val backdropUrl = doc.getString("backdropUrl")
                                    ?: doc.getString("backdrop_url")
                                    ?: doc.getString("imageUrl")
                                    ?: doc.getString("image_url")
                                    ?: doc.getString("stadium_image")
                                    ?: doc.getString("image")
                                    ?: posterUrl

                                val badge = doc.getString("badge") ?: if (isMatch) "مباراة اليوم" else "حصري"
                                val streamUrl = doc.getString("streamUrl") ?: doc.getString("stream_url") ?: doc.getString("server1").orEmpty()
                                val isLive = doc.getBoolean("isLive") ?: doc.getBoolean("is_live") ?: (matchStatus == "مباشر" || matchStatus == "شوط أول" || matchStatus == "شوط ثاني")
                                val sortOrder = doc.getLong("sortOrder")?.toInt() ?: doc.getLong("sort_order")?.toInt() ?: 0

                                val server1 = doc.getString("server1").orEmpty()
                                val server2 = doc.getString("server2").orEmpty()
                                val server3 = doc.getString("server3").orEmpty()
                                val server4 = doc.getString("server4").orEmpty()
                                val server5 = doc.getString("server5").orEmpty()

                                val contentType = if (isMatch) ContentType.MATCH else when (typeStr.uppercase()) {
                                    "MOVIE" -> ContentType.MOVIE
                                    "CHANNEL", "LIVE" -> ContentType.CHANNEL
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
                                    if (isMatch) {
                                        tagsList.addAll(listOf("مباراة", leagueName.ifBlank { "بث مباشر" }, "FHD"))
                                    } else {
                                        tagsList.addAll(listOf("مميز", "عالي الدقة"))
                                    }
                                }

                                resultList.add(
                                    HeroBannerItem(
                                        id = id,
                                        title = title,
                                        subtitle = subtitle,
                                        backdropUrl = backdropUrl,
                                        posterUrl = posterUrl,
                                        badge = badge,
                                        genreTags = tagsList,
                                        streamUrl = streamUrl,
                                        contentType = contentType,
                                        isLive = isLive,
                                        sortOrder = sortOrder,
                                        isActive = true,
                                        server1 = server1,
                                        server2 = server2,
                                        server3 = server3,
                                        server4 = server4,
                                        server5 = server5,
                                        isMatchSlider = isMatch,
                                        homeTeam = homeTeam,
                                        homeLogoUrl = homeLogo,
                                        awayTeam = awayTeam,
                                        awayLogoUrl = awayLogo,
                                        leagueName = leagueName,
                                        leagueLogoUrl = leagueLogo,
                                        matchTime = matchTime,
                                        matchDate = matchDate,
                                        homeScore = homeScore,
                                        awayScore = awayScore,
                                        matchStatus = matchStatus,
                                        commentator = commentator,
                                        channelName = channelName
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed reading Firestore collection $colName: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load sliders from Firestore: ${e.message}")
            }
        }

        // 2. Try Firebase Realtime Database paths ('/sliders.json' & '/match_sliders.json')
        val rtdbPaths = listOf(
            "https://iptvpro-f5172-default-rtdb.firebaseio.com/sliders.json",
            "https://iptvpro-f5172-default-rtdb.firebaseio.com/match_sliders.json"
        )
        for (url in rtdbPaths) {
            try {
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                val body = response.body?.string().orEmpty().trim()

                if (body.isNotEmpty() && body != "null") {
                    val isMatchPath = url.contains("match_sliders")
                    if (body.startsWith("[")) {
                        val jsonArray = JSONArray(body)
                        for (i in 0 until jsonArray.length()) {
                            val itemObj = jsonArray.optJSONObject(i) ?: continue
                            val parsed = parseSliderJson(itemObj, "slider_${isMatchPath}_$i", isMatchPath)
                            if (parsed != null && !seenIds.contains(parsed.id)) {
                                seenIds.add(parsed.id)
                                resultList.add(parsed)
                            }
                        }
                    } else if (body.startsWith("{")) {
                        val jsonObj = JSONObject(body)
                        val keys = jsonObj.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val itemObj = jsonObj.optJSONObject(key) ?: continue
                            val parsed = parseSliderJson(itemObj, key, isMatchPath)
                            if (parsed != null && !seenIds.contains(parsed.id)) {
                                seenIds.add(parsed.id)
                                resultList.add(parsed)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "RTDB sliders fetch error for $url: ${e.message}")
            }
        }

        if (resultList.isNotEmpty()) {
            Log.i(TAG, "Total loaded sliders from Firebase: ${resultList.size}")
            return@withContext resultList.sortedBy { it.sortOrder }
        }

        resultList
    }

    private fun parseSliderJson(obj: JSONObject, defaultId: String, forceMatch: Boolean = false): HeroBannerItem? {
        val isActive = obj.optBoolean("isActive", obj.optBoolean("is_active", true))
        if (!isActive) return null

        val id = obj.optString("id", defaultId)

        val homeTeam = obj.optString("home_team", obj.optString("homeTeam", obj.optString("team1", "")))
        val homeLogo = obj.optString("home_logo", obj.optString("home_logo_url", obj.optString("homeLogoUrl", obj.optString("homePoster", obj.optString("team1_logo", "")))))
        val awayTeam = obj.optString("away_team", obj.optString("awayTeam", obj.optString("team2", "")))
        val awayLogo = obj.optString("away_logo", obj.optString("away_logo_url", obj.optString("awayLogoUrl", obj.optString("awayPoster", obj.optString("team2_logo", "")))))
        val leagueName = obj.optString("league_name", obj.optString("leagueName", obj.optString("league", obj.optString("tournament", ""))))
        val leagueLogo = obj.optString("league_logo", obj.optString("league_logo_url", obj.optString("leagueLogoUrl", obj.optString("leagueIconUrl", ""))))
        val matchTime = obj.optString("match_time", obj.optString("matchTime", obj.optString("time", obj.optString("kickoff", ""))))
        val matchDate = obj.optString("match_date", obj.optString("matchDate", obj.optString("date", "")))
        val matchStatus = obj.optString("match_status", obj.optString("status", if (forceMatch) "لم تبدأ" else ""))
        val commentator = obj.optString("commentator", obj.optString("commentary", ""))
        val channelName = obj.optString("channel_name", obj.optString("channelName", obj.optString("channel", "")))
        val homeScore = obj.optInt("home_score", obj.optInt("homeScore", 0))
        val awayScore = obj.optInt("away_score", obj.optInt("awayScore", 0))

        val typeRaw = obj.optString("type", obj.optString("contentType", obj.optString("slider_type", ""))).lowercase()
        val isMatch = forceMatch || typeRaw == "match" || homeTeam.isNotEmpty()

        var title = obj.optString("title", "")
        if (title.isEmpty() && isMatch && homeTeam.isNotEmpty()) {
            title = "$homeTeam VS $awayTeam"
        }
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

        val isLive = typeRaw == "live" || matchStatus == "مباشر" || matchStatus == "شوط أول" || matchStatus == "شوط ثاني" || obj.optBoolean("isLive", obj.optBoolean("is_live", false))

        val contentType = when {
            isMatch -> ContentType.MATCH
            typeRaw == "movie" -> ContentType.MOVIE
            typeRaw == "channel" || typeRaw == "live" -> ContentType.CHANNEL
            else -> ContentType.SERIES
        }

        // Subtitle logic based on type and fields
        val subtitle = when {
            obj.has("subtitle") && obj.optString("subtitle").isNotEmpty() -> obj.optString("subtitle")
            isMatch -> {
                if (leagueName.isNotEmpty() && matchTime.isNotEmpty()) "$leagueName • $matchTime"
                else if (leagueName.isNotEmpty()) leagueName
                else "مباراة اليوم • بث مباشر"
            }
            typeRaw == "movie" -> {
                val rating = obj.optString("movieRating", "")
                if (rating.isNotEmpty()) "فيلم • تقييم ⭐ $rating/10" else "فيلم سينمائي"
            }
            else -> "عرض مميز بدقة عالية"
        }

        // Poster / Backdrop URL
        val posterUrl = obj.optString("poster", obj.optString("posterUrl", obj.optString("poster_url", obj.optString("cover", ""))))
        val backdropUrl = obj.optString(
            "image",
            obj.optString(
                "backdropUrl",
                obj.optString(
                    "backdrop_url",
                    obj.optString(
                        "imageUrl",
                        obj.optString("image_url", obj.optString("stadium_image", posterUrl))
                    )
                )
            )
        )

        // Badge
        val badge = when {
            obj.has("badge") && obj.optString("badge").isNotEmpty() -> obj.optString("badge")
            isMatch -> if (isLive) "مباشر LIVE" else "مباراة قمة"
            typeRaw == "movie" -> "فيلم"
            else -> "حصري"
        }

        val server1 = obj.optString("server1", "")
        val server2 = obj.optString("server2", "")
        val server3 = obj.optString("server3", "")
        val server4 = obj.optString("server4", "")
        val server5 = obj.optString("server5", "")

        val streamUrl = listOf(
            server1,
            server2,
            server3,
            server4,
            server5,
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
            when {
                isMatch -> tagsList.addAll(listOf("مباراة", leagueName.ifBlank { "بث مباشر" }, "FHD"))
                typeRaw == "movie" -> tagsList.addAll(listOf("فيلم", "سينما", "Full HD"))
                else -> tagsList.addAll(listOf("مميز", "HD"))
            }
        }

        return HeroBannerItem(
            id = id,
            title = title,
            subtitle = subtitle,
            backdropUrl = backdropUrl,
            posterUrl = posterUrl,
            badge = badge,
            genreTags = tagsList,
            streamUrl = streamUrl,
            contentType = contentType,
            isLive = isLive,
            sortOrder = sortOrder,
            isActive = true,
            server1 = server1,
            server2 = server2,
            server3 = server3,
            server4 = server4,
            server5 = server5,
            isMatchSlider = isMatch,
            homeTeam = homeTeam,
            homeLogoUrl = homeLogo,
            awayTeam = awayTeam,
            awayLogoUrl = awayLogo,
            leagueName = leagueName,
            leagueLogoUrl = leagueLogo,
            matchTime = matchTime,
            matchDate = matchDate,
            homeScore = homeScore,
            awayScore = awayScore,
            matchStatus = matchStatus,
            commentator = commentator,
            channelName = channelName
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

        // 1. Try Firestore 'm3u_playlists' or 'playlists' collections
        if (isFirebaseAvailable()) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val collections = listOf("m3u_playlists", "playlists", "m3u_sources")
                for (colName in collections) {
                    val snapshot = firestore.collection(colName).get().await()
                    if (snapshot != null && !snapshot.isEmpty) {
                        for (doc in snapshot.documents) {
                            val name = doc.getString("name") ?: doc.getString("title") ?: "باقة قنوات M3U"
                            val playlistUrl = doc.getString("url")
                                ?: doc.getString("playlist_url")
                                ?: doc.getString("streamUrl")
                                ?: doc.getString("m3u_url")
                                ?: ""
                            val isEnabled = doc.getBoolean("enabled") ?: doc.getBoolean("isEnabled") ?: true
                            if (playlistUrl.isNotBlank() && isEnabled) {
                                list.add(RemoteM3uSource(id = "fs_${doc.id}", name = name, url = playlistUrl, isEnabled = isEnabled))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firestore M3U sources fetch: ${e.message}")
            }
        }

        // 2. Try Firebase Realtime Database
        try {
            val rtdbUrls = listOf(
                "https://iptvpro-f5172-default-rtdb.firebaseio.com/m3u_playlists.json",
                "https://iptvpro-f5172-default-rtdb.firebaseio.com/m3u_sources.json",
                "https://iptvpro-f5172-default-rtdb.firebaseio.com/playlists.json"
            )
            for (url in rtdbUrls) {
                try {
                    val request = Request.Builder().url(url).build()
                    val response = httpClient.newCall(request).execute()
                    val body = response.body?.string().orEmpty().trim()
                    if (body.isEmpty() || body == "null") continue

                    if (body.startsWith("{")) {
                        val jsonObj = JSONObject(body)
                        val keys = jsonObj.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val obj = jsonObj.optJSONObject(key) ?: continue
                            val name = obj.optString("name", obj.optString("title", "باقة قنوات M3U"))
                            val playlistUrl = obj.optString("url", obj.optString("playlist_url", obj.optString("streamUrl", obj.optString("m3u_url", ""))))
                            val isEnabled = obj.optBoolean("enabled", obj.optBoolean("isEnabled", true))
                            if (playlistUrl.isNotBlank() && isEnabled) {
                                list.add(RemoteM3uSource(id = "rtdb_$key", name = name, url = playlistUrl, isEnabled = isEnabled))
                            }
                        }
                    } else if (body.startsWith("[")) {
                        val jsonArray = JSONArray(body)
                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.optJSONObject(i) ?: continue
                            val name = obj.optString("name", obj.optString("title", "باقة قنوات M3U"))
                            val playlistUrl = obj.optString("url", obj.optString("playlist_url", obj.optString("streamUrl", obj.optString("m3u_url", ""))))
                            val isEnabled = obj.optBoolean("enabled", obj.optBoolean("isEnabled", true))
                            if (playlistUrl.isNotBlank() && isEnabled) {
                                list.add(RemoteM3uSource(id = "rtdb_arr_$i", name = name, url = playlistUrl, isEnabled = isEnabled))
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error querying $url: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "M3U sources fetch error: ${e.message}")
        }

        list.distinctBy { it.url }
    }

    suspend fun fetchM3uPlaylist(url: String, defaultName: String = "باقة القنوات المباشرة"): ParsedM3uResult {
        return M3uPlaylistParser.parseFromUrl(url, defaultName)
    }

    suspend fun fetchCustomMovies(apiUrl: String = ""): List<com.example.data.model.MediaItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<com.example.data.model.MediaItem>()
        try {
            val urls = mutableListOf(
                "https://iptvpro-f5172-default-rtdb.firebaseio.com/movies.json",
                "https://iptvpro-f5172-default-rtdb.firebaseio.com/custom_movies.json",
                "https://iptvpro-f5172-default-rtdb.firebaseio.com/vod.json"
            )
            if (apiUrl.isNotBlank()) {
                urls.add(0, apiUrl)
            }

            for (url in urls) {
                try {
                    val request = Request.Builder().url(url).build()
                    val response = httpClient.newCall(request).execute()
                    val body = response.body?.string().orEmpty().trim()
                    if (body.isEmpty() || body == "null") continue

                    if (body.startsWith("{")) {
                        val jsonObj = JSONObject(body)
                        val keys = jsonObj.keys()
                        var i = list.size
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val obj = jsonObj.optJSONObject(key) ?: continue
                            parseMovieItem(obj, key, i)?.let {
                                list.add(it)
                                i++
                            }
                        }
                    } else if (body.startsWith("[")) {
                        val jsonArray = JSONArray(body)
                        var i = list.size
                        for (idx in 0 until jsonArray.length()) {
                            val obj = jsonArray.optJSONObject(idx) ?: continue
                            parseMovieItem(obj, "api_mov_$idx", i)?.let {
                                list.add(it)
                                i++
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed reading movies from $url: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Custom movies fetch error: ${e.message}")
        }
        list.distinctBy { it.id }
    }

    private fun parseMovieItem(obj: JSONObject, key: String, index: Int): com.example.data.model.MediaItem? {
        val title = obj.optString("title", obj.optString("name", "")).trim()
        if (title.isEmpty()) return null

        val poster = obj.optString("poster", obj.optString("posterUrl", obj.optString("image", obj.optString("logo", ""))))
        val backdrop = obj.optString("backdrop", obj.optString("backdropUrl", poster))
        val rating = obj.optString("rating", obj.optString("movieRating", "8.9"))
        val year = obj.optString("year", "2024")
        val story = obj.optString("story", obj.optString("description", "فيلم متاح عبر البث السحابي عالي الدقة"))
        val genre = obj.optString("genre", obj.optString("category", obj.optString("categoryName", "أفلام سينما")))
        val duration = obj.optString("duration", "120 دقيقة")

        val s1 = obj.optString("server1", "")
        val s2 = obj.optString("server2", "")
        val s3 = obj.optString("server3", "")
        val s4 = obj.optString("server4", "")
        val s5 = obj.optString("server5", "")
        val m3u8Url = obj.optString("m3u8", obj.optString("m3u8Url", ""))
        val streamUrlCandidate = listOf(s1, s2, m3u8Url, obj.optString("streamUrl", ""), obj.optString("url", "")).firstOrNull { it.isNotBlank() } ?: ""

        if (streamUrlCandidate.isBlank()) return null

        return com.example.data.model.MediaItem(
            id = "fb_mov_$key",
            title = title,
            posterUrl = poster,
            backdropUrl = backdrop,
            type = ContentType.MOVIE,
            year = year,
            rating = rating,
            genre = genre,
            description = story,
            duration = duration,
            streamUrl = streamUrlCandidate,
            isTop = index < 6,
            topRank = String.format("%02d", index + 1),
            isFavorite = false,
            server1 = s1,
            server2 = s2,
            server3 = s3,
            server4 = s4,
            server5 = s5
        )
    }

    suspend fun fetchCustomMovieCategories(): List<com.example.data.model.ChannelCategory> = withContext(Dispatchers.IO) {
        val list = mutableListOf<com.example.data.model.ChannelCategory>()
        try {
            val urls = listOf(
                "https://iptvpro-f5172-default-rtdb.firebaseio.com/movies_categories.json",
                "https://iptvpro-f5172-default-rtdb.firebaseio.com/vod_categories.json"
            )
            val colors = listOf("#0088FF", "#00C8FF", "#2563EB", "#7C3AED", "#DC2626", "#059669", "#D97706", "#EC4899")

            for (url in urls) {
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                val body = response.body?.string().orEmpty().trim()
                if (body.isNotEmpty() && body != "null" && body.startsWith("{")) {
                    val jsonObj = JSONObject(body)
                    val keys = jsonObj.keys()
                    var index = list.size
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val obj = jsonObj.optJSONObject(key) ?: continue
                        val name = obj.optString("name", "أفلام سينما")
                        val poster = obj.optString("poster", obj.optString("iconUrl", ""))
                        list.add(
                            com.example.data.model.ChannelCategory(
                                id = "fb_mov_cat_$key",
                                name = name,
                                subtitle = "تصنيف أفلام سحابي API",
                                channelCount = 0,
                                iconUrl = poster,
                                categoryType = "movies",
                                gradientColorHex = colors[index % colors.size]
                            )
                        )
                        index++
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Custom movie categories fetch error: ${e.message}")
        }
        list
    }

    suspend fun fetchM3uMovieSources(): List<RemoteM3uSource> = withContext(Dispatchers.IO) {
        val list = mutableListOf<RemoteM3uSource>()
        try {
            val urls = listOf(
                "https://iptvpro-f5172-default-rtdb.firebaseio.com/m3u_movies_playlists.json",
                "https://iptvpro-f5172-default-rtdb.firebaseio.com/m3u_movies.json",
                "https://iptvpro-f5172-default-rtdb.firebaseio.com/vod_playlists.json"
            )
            for (url in urls) {
                try {
                    val request = Request.Builder().url(url).build()
                    val response = httpClient.newCall(request).execute()
                    val body = response.body?.string().orEmpty().trim()
                    if (body.isNotEmpty() && body != "null" && body.startsWith("{")) {
                        val jsonObj = JSONObject(body)
                        val keys = jsonObj.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val obj = jsonObj.optJSONObject(key) ?: continue
                            val name = obj.optString("name", obj.optString("title", "باقة أفلام M3U8"))
                            val playlistUrl = obj.optString("url", obj.optString("playlist_url", obj.optString("streamUrl", "")))
                            val isEnabled = obj.optBoolean("enabled", obj.optBoolean("isEnabled", true))
                            if (playlistUrl.isNotBlank() && isEnabled) {
                                list.add(RemoteM3uSource(id = "mov_$key", name = name, url = playlistUrl, isEnabled = isEnabled))
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Movie M3U source fetch error from $url: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "M3U movies sources error: ${e.message}")
        }
        list
    }

    suspend fun fetchMoviesFromM3uSources(dedicatedUrls: List<String> = emptyList()): ParsedM3uResult = withContext(Dispatchers.IO) {
        val allSources = fetchM3uMovieSources().toMutableList()
        for (url in dedicatedUrls) {
            if (url.isNotBlank()) {
                allSources.add(RemoteM3uSource(id = "cfg_mov_${url.hashCode()}", name = "أفلام سينما سحابية M3U8", url = url))
            }
        }

        val aggregatedCategories = mutableListOf<com.example.data.model.ChannelCategory>()
        val aggregatedMovies = mutableListOf<com.example.data.model.MediaItem>()

        for (source in allSources.distinctBy { it.url }) {
            try {
                val parsed = M3uPlaylistParser.parseMoviesFromUrl(source.url, source.name)
                aggregatedCategories.addAll(parsed.movieCategories)
                aggregatedMovies.addAll(parsed.movies)
            } catch (e: Exception) {
                Log.w(TAG, "Failed parsing movie M3U from ${source.url}: ${e.message}")
            }
        }

        ParsedM3uResult(
            categories = emptyList(),
            channels = emptyList(),
            movieCategories = aggregatedCategories.distinctBy { it.id },
            movies = aggregatedMovies.distinctBy { it.id }
        )
    }

    /**
     * Fetches custom channels dynamically from an external Channels API endpoint.
     * Path configured via 'channels_api_url' in Firebase with 'channels_api_enabled' toggle.
     */
    suspend fun fetchChannelsFromApi(apiUrl: String): List<com.example.data.model.ChannelItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<com.example.data.model.ChannelItem>()
        if (apiUrl.isBlank()) return@withContext list

        try {
            val request = Request.Builder().url(apiUrl).build()
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string().orEmpty().trim()
            if (body.isNotEmpty() && body != "null") {
                if (body.startsWith("[")) {
                    val arr = JSONArray(body)
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        parseChannelFromJson(obj, "api_ch_$i", i + 1)?.let { list.add(it) }
                    }
                } else if (body.startsWith("{")) {
                    val jsonObj = JSONObject(body)
                    val targetObj = if (jsonObj.has("channels") && jsonObj.optJSONObject("channels") != null) {
                        jsonObj.getJSONObject("channels")
                    } else if (jsonObj.has("data") && jsonObj.optJSONArray("data") != null) {
                        val arr = jsonObj.getJSONArray("data")
                        for (i in 0 until arr.length()) {
                            val obj = arr.optJSONObject(i) ?: continue
                            parseChannelFromJson(obj, "api_ch_$i", i + 1)?.let { list.add(it) }
                        }
                        return@withContext list
                    } else {
                        jsonObj
                    }

                    val keys = targetObj.keys()
                    var sort = 1
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val obj = targetObj.optJSONObject(key) ?: continue
                        parseChannelFromJson(obj, "api_ch_$key", sort++)?.let { list.add(it) }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Channels API fetch error from $apiUrl: ${e.message}")
        }
        list
    }

    private fun parseChannelFromJson(obj: JSONObject, defaultId: String, sortOrder: Int): com.example.data.model.ChannelItem? {
        val name = obj.optString("name", obj.optString("title", obj.optString("channel_name", "")))
        if (name.isBlank()) return null

        val id = obj.optString("id", obj.optString("stream_id", defaultId))
        val catId = obj.optString("categoryId", obj.optString("category_id", obj.optString("category", "api_channels")))
        val catName = obj.optString("categoryName", obj.optString("category_name", "باقة القنوات المباشرة API"))
        val logo = obj.optString("logo", obj.optString("logoUrl", obj.optString("logo_url", obj.optString("stream_icon", obj.optString("icon", "")))))
        val s1 = obj.optString("server1", obj.optString("server_1", ""))
        val s2 = obj.optString("server2", obj.optString("server_2", ""))
        val s3 = obj.optString("server3", obj.optString("server_3", ""))
        val s4 = obj.optString("server4", obj.optString("server_4", ""))
        val s5 = obj.optString("server5", obj.optString("server_5", ""))
        val directUrl = obj.optString("url", obj.optString("streamUrl", obj.optString("stream_url", obj.optString("m3u8", ""))))
        val mpd = obj.optString("mpd", "")

        val streamUrl = listOf(s1, directUrl, s2, s3, mpd, s4, s5).firstOrNull { it.isNotBlank() } ?: return null
        val backupUrl = listOf(s2, s3, directUrl).firstOrNull { it.isNotBlank() && it != streamUrl } ?: ""

        return com.example.data.model.ChannelItem(
            id = id,
            name = name,
            categoryId = catId,
            categoryName = catName,
            logoUrl = logo,
            streamUrl = streamUrl,
            backupUrl = backupUrl,
            country = obj.optString("country", "سحابي Cloud"),
            language = obj.optString("language", "العربية"),
            isFavorite = false,
            isEnabled = true,
            sortOrder = sortOrder,
            viewsCount = (500..5000).random()
        )
    }

    /**
     * Fetches News articles from Firebase (Firestore & RTDB) and external News API.
     */
    suspend fun fetchNews(apiUrl: String = ""): List<com.example.data.model.NewsArticle> = withContext(Dispatchers.IO) {
        val resultList = mutableListOf<com.example.data.model.NewsArticle>()
        val seenIds = mutableSetOf<String>()

        // 1. Try Firebase Firestore ('news' and 'custom_news' collections)
        if (isFirebaseAvailable()) {
            val firestore = FirebaseFirestore.getInstance()
            val colNames = listOf("news", "custom_news")
            for (col in colNames) {
                try {
                    val snapshot = firestore.collection(col).get().await()
                    for (doc in snapshot.documents) {
                        val id = doc.id
                        if (seenIds.contains(id)) continue
                        val title = doc.getString("title").orEmpty()
                        if (title.isBlank()) continue
                        seenIds.add(id)

                        val content = doc.getString("content") ?: doc.getString("description") ?: doc.getString("body") ?: doc.getString("details").orEmpty()
                        val image = doc.getString("imageUrl") ?: doc.getString("image_url") ?: doc.getString("image") ?: doc.getString("thumbnail") ?: doc.getString("poster").orEmpty()
                        val category = doc.getString("category") ?: doc.getString("tag") ?: "أخبار رياضية"
                        val date = doc.getString("date") ?: doc.getString("time") ?: doc.getString("publishedAt") ?: "اليوم"
                        val source = doc.getString("source") ?: doc.getString("sourceName") ?: doc.getString("author") ?: "SARIB NEWS"
                        val sourceUrl = doc.getString("sourceUrl") ?: doc.getString("source_url") ?: doc.getString("url") ?: doc.getString("link").orEmpty()
                        val isBreaking = doc.getBoolean("isBreaking") ?: doc.getBoolean("is_breaking") ?: false
                        val views = doc.getLong("views")?.toInt() ?: doc.getLong("viewsCount")?.toInt() ?: (100..2500).random()
                        val sortOrder = doc.getLong("sortOrder")?.toInt() ?: doc.getLong("order")?.toInt() ?: 0

                        resultList.add(
                            com.example.data.model.NewsArticle(
                                id = id,
                                title = title,
                                content = content,
                                imageUrl = image,
                                category = category,
                                date = date,
                                source = source,
                                sourceUrl = sourceUrl,
                                isBreaking = isBreaking,
                                isManual = true,
                                viewsCount = views,
                                sortOrder = sortOrder
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed reading Firestore collection $col: ${e.message}")
                }
            }
        }

        // 2. Try Firebase Realtime Database ('/news.json' and '/custom_news.json')
        val rtdbNewsPaths = listOf(
            "https://iptvpro-f5172-default-rtdb.firebaseio.com/news.json",
            "https://iptvpro-f5172-default-rtdb.firebaseio.com/custom_news.json"
        )
        for (url in rtdbNewsPaths) {
            try {
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                val body = response.body?.string().orEmpty().trim()
                if (body.isNotEmpty() && body != "null") {
                    if (body.startsWith("[")) {
                        val arr = JSONArray(body)
                        for (i in 0 until arr.length()) {
                            val obj = arr.optJSONObject(i) ?: continue
                            parseNewsArticleJson(obj, "news_$i", isManual = true)?.let {
                                if (!seenIds.contains(it.id)) {
                                    seenIds.add(it.id)
                                    resultList.add(it)
                                }
                            }
                        }
                    } else if (body.startsWith("{")) {
                        val jsonObj = JSONObject(body)
                        val keys = jsonObj.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val obj = jsonObj.optJSONObject(key) ?: continue
                            parseNewsArticleJson(obj, key, isManual = true)?.let {
                                if (!seenIds.contains(it.id)) {
                                    seenIds.add(it.id)
                                    resultList.add(it)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "RTDB news fetch error from $url: ${e.message}")
            }
        }

        // 3. Fetch from External News API if provided
        if (apiUrl.isNotBlank()) {
            try {
                val request = Request.Builder()
                    .url(apiUrl)
                    .addHeader("User-Agent", "Mozilla/5.0 (Android; SARIB TV App)")
                    .addHeader("Accept", "application/json")
                    .build()
                val response = httpClient.newCall(request).execute()
                val body = response.body?.string().orEmpty().trim()
                if (body.isNotEmpty() && body != "null") {
                    if (body.startsWith("[")) {
                        val arr = JSONArray(body)
                        for (i in 0 until arr.length()) {
                            val obj = arr.optJSONObject(i) ?: continue
                            parseNewsArticleJson(obj, "api_news_$i", isManual = false)?.let {
                                if (!seenIds.contains(it.id) && !seenIds.contains(it.title)) {
                                    seenIds.add(it.id)
                                    resultList.add(it)
                                }
                            }
                        }
                    } else if (body.startsWith("{")) {
                        val root = JSONObject(body)
                        val arr = root.optJSONArray("articles")
                            ?: root.optJSONArray("data")
                            ?: root.optJSONArray("news")
                            ?: root.optJSONArray("items")
                            ?: root.optJSONArray("results")

                        if (arr != null) {
                            for (i in 0 until arr.length()) {
                                val obj = arr.optJSONObject(i) ?: continue
                                parseNewsArticleJson(obj, "api_news_$i", isManual = false)?.let {
                                    if (!seenIds.contains(it.id) && !seenIds.contains(it.title)) {
                                        seenIds.add(it.id)
                                        resultList.add(it)
                                    }
                                }
                            }
                        } else {
                            val keys = root.keys()
                            var idx = 0
                            while (keys.hasNext()) {
                                val key = keys.next()
                                val obj = root.optJSONObject(key) ?: continue
                                parseNewsArticleJson(obj, "api_news_${idx++}", isManual = false)?.let {
                                    if (!seenIds.contains(it.id) && !seenIds.contains(it.title)) {
                                        seenIds.add(it.id)
                                        resultList.add(it)
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "External News API error for $apiUrl: ${e.message}")
            }
        }

        // If no news returned from remote/api, add informative default SARIB news
        if (resultList.isEmpty()) {
            resultList.addAll(getDefaultNewsArticles())
        }

        resultList.sortedWith(compareByDescending<com.example.data.model.NewsArticle> { it.isBreaking }
            .thenBy { it.sortOrder }
            .thenByDescending { it.isManual })
    }

    private fun parseNewsArticleJson(obj: JSONObject, defaultId: String, isManual: Boolean): com.example.data.model.NewsArticle? {
        val title = obj.optString("title", obj.optString("headline", obj.optString("name", "")))
        if (title.isBlank()) return null

        val id = obj.optString("id", defaultId)
        val content = obj.optString("content", obj.optString("description", obj.optString("body", obj.optString("details", obj.optString("summary", "")))))
        val image = obj.optString("imageUrl", obj.optString("image_url", obj.optString("image", obj.optString("urlToImage", obj.optString("thumbnail", obj.optString("poster", ""))))))
        val category = obj.optString("category", obj.optString("tag", obj.optString("section", "أخبار عامة")))
        val date = obj.optString("date", obj.optString("publishedAt", obj.optString("time", obj.optString("created_at", "اليوم"))))
        
        // Handle nested source object if present (common in news APIs)
        var source = "SARIB NEWS"
        val sourceObj = obj.optJSONObject("source")
        if (sourceObj != null) {
            source = sourceObj.optString("name", "SARIB NEWS")
        } else {
            source = obj.optString("source", obj.optString("sourceName", obj.optString("author", "SARIB NEWS")))
        }

        val sourceUrl = obj.optString("sourceUrl", obj.optString("source_url", obj.optString("url", obj.optString("link", ""))))
        val isBreaking = obj.optBoolean("isBreaking", obj.optBoolean("is_breaking", false))
        val views = obj.optInt("views", obj.optInt("viewsCount", (150..3500).random()))
        val sortOrder = obj.optInt("sortOrder", obj.optInt("order", 0))

        return com.example.data.model.NewsArticle(
            id = id,
            title = title,
            content = content,
            imageUrl = image,
            category = category,
            date = date,
            source = source,
            sourceUrl = sourceUrl,
            isBreaking = isBreaking,
            isManual = isManual,
            viewsCount = views,
            sortOrder = sortOrder
        )
    }

    private fun getDefaultNewsArticles(): List<com.example.data.model.NewsArticle> {
        return listOf(
            com.example.data.model.NewsArticle(
                id = "default_news_1",
                title = "تغطية شاملة ومباشرة لقمة دوري أبطال أوروبا على قنوات SARIB TV VIP",
                content = "استمتع بمشاهدة أحدث مباريات القمة العالمية بجودة عالية FHD وبدون تقطيع مع توفير 5 سيرفرات بث مباشر ومعلقين عرب متميزين.",
                imageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=800&q=80",
                category = "رياضة",
                date = "اليوم",
                source = "SARIB Sports",
                isBreaking = true,
                isManual = true,
                viewsCount = 3840
            ),
            com.example.data.model.NewsArticle(
                id = "default_news_2",
                title = "إطلاق باقة أفلام ومسلسلات 2025 الحصرية مع سيرفرات متعددة عالية السرعة",
                content = "تمت إضافة أحدث الأعمال السينمائية والمسلسلات العربية والأجنبية مع ترجمة احترافية ودعم جودات 4K و 1080p لتجربة مشاهدة سينمائية متكاملة.",
                imageUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&q=80",
                category = "سينما ومسلسلات",
                date = "منذ ساعات",
                source = "SARIB Cinema",
                isBreaking = false,
                isManual = true,
                viewsCount = 2190
            ),
            com.example.data.model.NewsArticle(
                id = "default_news_3",
                title = "تحديثات تقنية متقدمة للمشغل الداخلي لدعم البث المباشر وبث الشاشة اللاسلكي",
                content = "تم تعزيز المشغل بميزات جديدة تشمل استقرار الاتصال، وخيارات السيرفرات البديلة، وميزة بث الشاشة على أجهزة التلفاز الذكية Smart TV بدون تقطيع.",
                imageUrl = "https://images.unsplash.com/photo-1593784991095-a205069470b6?w=800&q=80",
                category = "تقنية",
                date = "اليوم",
                source = "فريق الدعم الفني",
                isBreaking = false,
                isManual = true,
                viewsCount = 1750
            )
        )
    }

    /**
     * Fetches manual matches configured directly from Firebase (RTDB & Firestore).
     */
    suspend fun fetchManualMatches(): List<com.example.data.model.MatchItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<com.example.data.model.MatchItem>()
        val seenIds = mutableSetOf<String>()

        // 1. Try Firestore 'matches' and 'custom_matches'
        if (isFirebaseAvailable()) {
            val firestore = FirebaseFirestore.getInstance()
            for (col in listOf("matches", "custom_matches")) {
                try {
                    val snapshot = firestore.collection(col).get().await()
                    for (doc in snapshot.documents) {
                        val id = doc.id
                        if (seenIds.contains(id)) continue
                        val homeTeam = doc.getString("home_team") ?: doc.getString("homeTeam") ?: doc.getString("team1").orEmpty()
                        val awayTeam = doc.getString("away_team") ?: doc.getString("awayTeam") ?: doc.getString("team2").orEmpty()
                        if (homeTeam.isBlank() && awayTeam.isBlank()) continue
                        seenIds.add(id)

                        val league = doc.getString("league_name") ?: doc.getString("leagueName") ?: doc.getString("league") ?: "مباريات اليوم"
                        val leagueIcon = doc.getString("league_logo") ?: doc.getString("leagueIconUrl") ?: ""
                        val homeLogo = doc.getString("home_logo") ?: doc.getString("homeLogoUrl") ?: ""
                        val awayLogo = doc.getString("away_logo") ?: doc.getString("awayLogoUrl") ?: ""
                        val time = doc.getString("match_time") ?: doc.getString("matchTime") ?: doc.getString("time") ?: "09:00 م"
                        val date = doc.getString("match_date") ?: doc.getString("matchDate") ?: doc.getString("date") ?: "اليوم"
                        val status = doc.getString("match_status") ?: doc.getString("status") ?: "لم تبدأ"
                        val homeScore = doc.getLong("home_score")?.toInt() ?: doc.getLong("homeScore")?.toInt() ?: 0
                        val awayScore = doc.getLong("away_score")?.toInt() ?: doc.getLong("awayScore")?.toInt() ?: 0
                        val stadium = doc.getString("stadium") ?: "الملعب الرئيسي"
                        val commentator = doc.getString("commentator") ?: "المعلق المعتمد"
                        val channel = doc.getString("channel_name") ?: doc.getString("channel") ?: "SARIB Sports HD"

                        val s1 = doc.getString("server1").orEmpty()
                        val s2 = doc.getString("server2").orEmpty()
                        val s3 = doc.getString("server3").orEmpty()
                        val s4 = doc.getString("server4").orEmpty()
                        val s5 = doc.getString("server5").orEmpty()
                        val directStream = doc.getString("streamUrl") ?: doc.getString("stream_url") ?: ""
                        val streamUrl = listOf(s1, directStream, s2, s3, s4, s5).firstOrNull { it.isNotBlank() } ?: ""

                        val isLive = doc.getBoolean("isLive") ?: doc.getBoolean("is_live") ?: (status == "مباشر" || status == "شوط أول" || status == "شوط ثاني")

                        list.add(
                            com.example.data.model.MatchItem(
                                id = "fb_match_$id",
                                leagueName = league,
                                leagueIconUrl = leagueIcon,
                                homeTeam = homeTeam,
                                homeLogoUrl = homeLogo,
                                awayTeam = awayTeam,
                                awayLogoUrl = awayLogo,
                                matchTime = time,
                                matchDate = date,
                                status = status,
                                homeScore = homeScore,
                                awayScore = awayScore,
                                streamUrl = streamUrl,
                                isLive = isLive,
                                isFavorite = false,
                                stadium = stadium,
                                commentator = commentator,
                                channelName = channel,
                                server1 = s1,
                                server2 = s2,
                                server3 = s3,
                                server4 = s4,
                                server5 = s5,
                                isManual = true
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed reading manual matches from Firestore $col: ${e.message}")
                }
            }
        }

        // 2. Try RTDB '/matches.json' & '/custom_matches.json'
        for (url in listOf("https://iptvpro-f5172-default-rtdb.firebaseio.com/matches.json", "https://iptvpro-f5172-default-rtdb.firebaseio.com/custom_matches.json")) {
            try {
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                val body = response.body?.string().orEmpty().trim()
                if (body.isNotEmpty() && body != "null") {
                    if (body.startsWith("[")) {
                        val arr = JSONArray(body)
                        for (i in 0 until arr.length()) {
                            val obj = arr.optJSONObject(i) ?: continue
                            parseManualMatchJson(obj, "fb_match_$i")?.let {
                                if (!seenIds.contains(it.id)) {
                                    seenIds.add(it.id)
                                    list.add(it)
                                }
                            }
                        }
                    } else if (body.startsWith("{")) {
                        val jsonObj = JSONObject(body)
                        val keys = jsonObj.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val obj = jsonObj.optJSONObject(key) ?: continue
                            parseManualMatchJson(obj, "fb_match_$key")?.let {
                                if (!seenIds.contains(it.id)) {
                                    seenIds.add(it.id)
                                    list.add(it)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "RTDB manual matches error from $url: ${e.message}")
            }
        }

        list
    }

    private fun parseManualMatchJson(obj: JSONObject, defaultId: String): com.example.data.model.MatchItem? {
        val homeTeam = obj.optString("home_team", obj.optString("homeTeam", obj.optString("team1", "")))
        val awayTeam = obj.optString("away_team", obj.optString("awayTeam", obj.optString("team2", "")))
        if (homeTeam.isBlank() && awayTeam.isBlank()) return null

        val id = obj.optString("id", defaultId)
        val league = obj.optString("league_name", obj.optString("leagueName", obj.optString("league", "مباريات اليوم")))
        val leagueIcon = obj.optString("league_logo", obj.optString("leagueLogoUrl", ""))
        val homeLogo = obj.optString("home_logo", obj.optString("homeLogoUrl", obj.optString("team1_logo", "")))
        val awayLogo = obj.optString("away_logo", obj.optString("awayLogoUrl", obj.optString("team2_logo", "")))
        val time = obj.optString("match_time", obj.optString("matchTime", obj.optString("time", "09:00 م")))
        val date = obj.optString("match_date", obj.optString("matchDate", obj.optString("date", "اليوم")))
        val status = obj.optString("match_status", obj.optString("status", "لم تبدأ"))
        val homeScore = obj.optInt("home_score", obj.optInt("homeScore", 0))
        val awayScore = obj.optInt("away_score", obj.optInt("awayScore", 0))
        val stadium = obj.optString("stadium", "الملعب الرئيسي")
        val commentator = obj.optString("commentator", "المعلق المعتمد")
        val channel = obj.optString("channel_name", obj.optString("channel", "SARIB Sports HD"))

        val s1 = obj.optString("server1", "")
        val s2 = obj.optString("server2", "")
        val s3 = obj.optString("server3", "")
        val s4 = obj.optString("server4", "")
        val s5 = obj.optString("server5", "")
        val directStream = obj.optString("streamUrl", obj.optString("stream_url", ""))
        val streamUrl = listOf(s1, directStream, s2, s3, s4, s5).firstOrNull { it.isNotBlank() } ?: ""

        val isLive = obj.optBoolean("isLive", obj.optBoolean("is_live", status == "مباشر" || status == "شوط أول" || status == "شوط ثاني"))

        return com.example.data.model.MatchItem(
            id = id,
            leagueName = league,
            leagueIconUrl = leagueIcon,
            homeTeam = homeTeam,
            homeLogoUrl = homeLogo,
            awayTeam = awayTeam,
            awayLogoUrl = awayLogo,
            matchTime = time,
            matchDate = date,
            status = status,
            homeScore = homeScore,
            awayScore = awayScore,
            streamUrl = streamUrl,
            isLive = isLive,
            isFavorite = false,
            stadium = stadium,
            commentator = commentator,
            channelName = channel,
            server1 = s1,
            server2 = s2,
            server3 = s3,
            server4 = s4,
            server5 = s5,
            isManual = true
        )
    }

    /**
     * Fetches match stream overrides from Firebase to link manual stream URLs
     * and servers (server1-5) to API matches by match index (1, 2, 3...), match ID, or team names.
     */
    suspend fun fetchMatchStreamOverrides(): List<com.example.data.model.MatchStreamOverride> = withContext(Dispatchers.IO) {
        val list = mutableListOf<com.example.data.model.MatchStreamOverride>()
        val seenKeys = mutableSetOf<String>()

        // 1. Try Firestore 'match_streams' and 'match_overrides'
        if (isFirebaseAvailable()) {
            val firestore = FirebaseFirestore.getInstance()
            for (col in listOf("match_streams", "match_overrides", "match_stream_overrides")) {
                try {
                    val snapshot = firestore.collection(col).get().await()
                    for (doc in snapshot.documents) {
                        val key = doc.id
                        if (seenKeys.contains(key)) continue
                        seenKeys.add(key)

                        val s1 = doc.getString("server1").orEmpty()
                        val s2 = doc.getString("server2").orEmpty()
                        val s3 = doc.getString("server3").orEmpty()
                        val s4 = doc.getString("server4").orEmpty()
                        val s5 = doc.getString("server5").orEmpty()
                        val stream = doc.getString("streamUrl") ?: doc.getString("stream_url") ?: s1

                        list.add(
                            com.example.data.model.MatchStreamOverride(
                                matchKey = key,
                                streamUrl = stream,
                                server1 = s1,
                                server2 = s2,
                                server3 = s3,
                                server4 = s4,
                                server5 = s5,
                                commentator = doc.getString("commentator").orEmpty(),
                                channelName = doc.getString("channel_name") ?: doc.getString("channel").orEmpty(),
                                status = doc.getString("status").orEmpty(),
                                isEnabled = doc.getBoolean("isEnabled") ?: doc.getBoolean("is_enabled") ?: true
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed reading stream overrides from Firestore $col: ${e.message}")
                }
            }
        }

        // 2. Try RTDB '/match_streams.json', '/match_stream_overrides.json', '/match_overrides.json'
        val rtdbUrls = listOf(
            "https://iptvpro-f5172-default-rtdb.firebaseio.com/match_streams.json",
            "https://iptvpro-f5172-default-rtdb.firebaseio.com/match_stream_overrides.json",
            "https://iptvpro-f5172-default-rtdb.firebaseio.com/match_overrides.json"
        )
        for (url in rtdbUrls) {
            try {
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                val body = response.body?.string().orEmpty().trim()
                if (body.isNotEmpty() && body != "null") {
                    if (body.startsWith("[")) {
                        val arr = JSONArray(body)
                        for (i in 0 until arr.length()) {
                            val obj = arr.optJSONObject(i) ?: continue
                            val matchKey = obj.optString("matchKey", obj.optString("match_key", obj.optString("match", "match_${i + 1}")))
                            if (!seenKeys.contains(matchKey)) {
                                seenKeys.add(matchKey)
                                parseMatchStreamOverrideJson(obj, matchKey)?.let { list.add(it) }
                            }
                        }
                    } else if (body.startsWith("{")) {
                        val jsonObj = JSONObject(body)
                        val keys = jsonObj.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val obj = jsonObj.optJSONObject(key) ?: continue
                            if (!seenKeys.contains(key)) {
                                seenKeys.add(key)
                                parseMatchStreamOverrideJson(obj, key)?.let { list.add(it) }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "RTDB stream overrides error from $url: ${e.message}")
            }
        }

        list
    }

    private fun parseMatchStreamOverrideJson(obj: JSONObject, key: String): com.example.data.model.MatchStreamOverride? {
        val s1 = obj.optString("server1", obj.optString("server_1", ""))
        val s2 = obj.optString("server2", obj.optString("server_2", ""))
        val s3 = obj.optString("server3", obj.optString("server_3", ""))
        val s4 = obj.optString("server4", obj.optString("server_4", ""))
        val s5 = obj.optString("server5", obj.optString("server_5", ""))
        val directStream = obj.optString("streamUrl", obj.optString("stream_url", obj.optString("url", "")))
        val streamUrl = listOf(s1, directStream, s2, s3, s4, s5).firstOrNull { it.isNotBlank() } ?: ""

        val isEnabled = obj.optBoolean("isEnabled", obj.optBoolean("is_enabled", obj.optBoolean("enabled", true)))

        return com.example.data.model.MatchStreamOverride(
            matchKey = key,
            streamUrl = streamUrl,
            server1 = s1,
            server2 = s2,
            server3 = s3,
            server4 = s4,
            server5 = s5,
            commentator = obj.optString("commentator", ""),
            channelName = obj.optString("channel_name", obj.optString("channel", "")),
            status = obj.optString("status", ""),
            isEnabled = isEnabled
        )
    }

    /**
     * Remote API key fetcher specifically for Football / Matches data.
     * Checks Firestore 'matches_config/main_config', 'stream_config/main_config',
     * RTDB '/matches_config.json', '/api_football_key.json', '/football_api_key.json', '/matches_api_key.json'.
     */
    suspend fun fetchMatchesApiKey(): String? = withContext(Dispatchers.IO) {
        // 1. Try Firestore
        if (isFirebaseAvailable()) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val doc = firestore.collection("matches_config").document("main_config").get().await()
                if (doc != null && doc.exists()) {
                    val key = doc.getString("api_football_key") ?: doc.getString("football_api_key") ?: doc.getString("matches_api_key") ?: doc.getString("api_key") ?: doc.getString("rapidapi_key")
                    if (!key.isNullOrBlank()) return@withContext key.trim()
                }
            } catch (e: Exception) {
                // Ignore and try RTDB
            }
        }

        // 2. Try RTDB direct paths
        val directUrls = listOf(
            "https://iptvpro-f5172-default-rtdb.firebaseio.com/matches_config.json",
            "https://iptvpro-f5172-default-rtdb.firebaseio.com/api_football_key.json",
            "https://iptvpro-f5172-default-rtdb.firebaseio.com/football_api_key.json",
            "https://iptvpro-f5172-default-rtdb.firebaseio.com/matches_api_key.json"
        )
        for (url in directUrls) {
            try {
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                val body = response.body?.string().orEmpty().trim()
                if (body.isNotEmpty() && body != "null") {
                    if (body.startsWith("{")) {
                        val obj = JSONObject(body)
                        val key = obj.optString("api_football_key", obj.optString("football_api_key", obj.optString("matches_api_key", obj.optString("api_key", ""))))
                        if (key.isNotBlank()) return@withContext key.trim()
                    } else if (body.startsWith("\"") && body.endsWith("\"")) {
                        val rawKey = body.removeSurrounding("\"").trim()
                        if (rawKey.isNotBlank()) return@withContext rawKey
                    } else if (body.length in 10..100) {
                        return@withContext body
                    }
                }
            } catch (e: Exception) {
                // Next
            }
        }
        null
    }
}
