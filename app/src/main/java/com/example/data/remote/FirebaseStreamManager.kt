package com.example.data.remote

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class RemoteStreamConfig(
    val serverHost: String = "http://cliccck52258.club:2082",
    val username: String = "khaledsliman",
    val password: String = "755246419856",
    val matchesApiUrl: String = "https://bab-elmoshahd.online/api/index.php?path=matches&day=",
    val announcement: String = "",
    val telegramLink: String = "https://t.me/sarib_tv",
    val heroTitle: String = "بلدة الضياع S1-S4",
    val heroSubtitle: String = "مسلسل • دراما • رعب • أحجية",
    val heroStreamUrl: String = "http://cliccck52258.club:2082/series/khaledsliman/755246419856/1.mp4"
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
}
