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
    val seriesCategoriesAccounts: Map<String, XtreamAccount> = emptyMap(),
    val vodCategoriesAccounts: Map<String, XtreamAccount> = emptyMap(),
    val matchesApiUrl: String = "https://bab-elmoshahd.online/api/index.php?path=matches&day=",
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

                    baseConfig = RemoteStreamConfig(
                        serverHost = serverHost,
                        username = username,
                        password = password,
                        seriesAccount = XtreamAccount(serverHost = seriesHost, username = seriesUser, password = seriesPass, name = "سيرفر المسلسلات"),
                        vodAccount = XtreamAccount(serverHost = vodHost, username = vodUser, password = vodPass, name = "سيرفر الأفلام"),
                        matchesApiUrl = docSnapshot.getString("matches_api_url") ?: "https://bab-elmoshahd.online/api/index.php?path=matches&day=",
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

                    if (targetObj.has("server_host") || targetObj.has("username")) {
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

                        baseConfig = RemoteStreamConfig(
                            serverHost = serverHost,
                            username = username,
                            password = password,
                            seriesAccount = XtreamAccount(serverHost = seriesHost, username = seriesUser, password = seriesPass, name = "سيرفر المسلسلات"),
                            vodAccount = XtreamAccount(serverHost = vodHost, username = vodUser, password = vodPass, name = "سيرفر الأفلام"),
                            matchesApiUrl = targetObj.optString("matches_api_url", baseConfig.matchesApiUrl),
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
}
