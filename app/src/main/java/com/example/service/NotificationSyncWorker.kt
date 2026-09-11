package com.example.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.util.NotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class NotificationSyncWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val prefs: SharedPreferences = appContext.getSharedPreferences("sarib_notifications_prefs", Context.MODE_PRIVATE)
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    override suspend fun doWork(): Result {
        return try {
            NotificationHelper.initNotificationChannels(appContext)

            // 1. Check RTDB Endpoints
            checkHttpEndpoints()

            // 2. Check Firestore
            checkFirestore()

            Result.success()
        } catch (e: Exception) {
            Log.w("NotificationWorker", "Sync worker error: ${e.message}")
            Result.retry()
        }
    }

    private fun checkHttpEndpoints() {
        val urls = listOf(
            "https://iptvpro-f5172-default-rtdb.firebaseio.com/notifications.json",
            "https://iptvpro-f5172-default-rtdb.firebaseio.com/broadcast_notifications.json",
            "https://iptvpro-f5172-default-rtdb.firebaseio.com/announcements.json"
        )

        for (url in urls) {
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
                        if (hasProcessedNotification(key)) continue

                        val obj = jsonObj.optJSONObject(key)
                        if (obj != null) {
                            val title = obj.optString("title", obj.optString("name", "إشعار SARIB TV"))
                            val message = obj.optString("message", obj.optString("body", obj.optString("text", "")))

                            if (title.isNotBlank() || message.isNotBlank()) {
                                markNotificationProcessed(key)
                                NotificationHelper.showBroadcastNotification(
                                    context = appContext,
                                    notifId = key,
                                    title = title,
                                    message = message.ifBlank { title }
                                )
                            }
                        }
                    }
                } else if (body.startsWith("[")) {
                    val jsonArray = JSONArray(body)
                    for (i in 0 until jsonArray.length()) {
                        val key = "notif_arr_$i"
                        if (hasProcessedNotification(key)) continue

                        val obj = jsonArray.optJSONObject(i) ?: continue
                        val title = obj.optString("title", "إشعار SARIB TV")
                        val message = obj.optString("message", obj.optString("body", ""))

                        if (title.isNotBlank() || message.isNotBlank()) {
                            markNotificationProcessed(key)
                            NotificationHelper.showBroadcastNotification(
                                context = appContext,
                                notifId = key,
                                title = title,
                                message = message.ifBlank { title }
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("NotificationWorker", "Error fetching from $url: ${e.message}")
            }
        }
    }

    private suspend fun checkFirestore() {
        try {
            if (FirebaseApp.getApps(appContext).isNotEmpty()) {
                val firestore = FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("notifications").get().await()
                for (doc in snapshot.documents) {
                    val notifId = doc.id
                    if (hasProcessedNotification(notifId)) continue

                    val title = doc.getString("title") ?: "إشعار جديد من SARIB TV"
                    val message = doc.getString("message") ?: doc.getString("body") ?: ""

                    if (title.isNotBlank() || message.isNotBlank()) {
                        markNotificationProcessed(notifId)
                        NotificationHelper.showBroadcastNotification(
                            context = appContext,
                            notifId = notifId,
                            title = title,
                            message = message.ifBlank { title }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("NotificationWorker", "Firestore worker check failed: ${e.message}")
        }
    }

    private fun hasProcessedNotification(id: String): Boolean {
        return prefs.getBoolean("processed_$id", false)
    }

    private fun markNotificationProcessed(id: String) {
        prefs.edit().putBoolean("processed_$id", true).apply()
    }
}
