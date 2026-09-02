package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Background notification listener that ensures push notifications sent from
 * the dashboard/Firebase (Realtime Database & Firestore) pop up in the system
 * notification tray even when the app is running in the background.
 */
class FirebaseNotificationListener(private val context: Context) {

    private val TAG = "SaribNotifListener"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs: SharedPreferences = context.getSharedPreferences("sarib_notifications_prefs", Context.MODE_PRIVATE)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private var isListening = false

    fun startListening() {
        if (isListening) return
        isListening = true

        NotificationHelper.initNotificationChannels(context)

        // 1. Attach Firestore real-time snapshot listener
        attachFirestoreListener()

        // 2. Periodic background check for RTDB & Firestore notifications
        scope.launch {
            while (true) {
                try {
                    pollHttpNotifications()
                } catch (e: Exception) {
                    Log.w(TAG, "Notification poll error: ${e.message}")
                }
                delay(15_000) // Poll every 15 seconds
            }
        }
    }

    private fun attachFirestoreListener() {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("notifications").addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Firestore notification listen failed: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        for (docChange in snapshot.documentChanges) {
                            if (docChange.type == DocumentChange.Type.ADDED || docChange.type == DocumentChange.Type.MODIFIED) {
                                val doc = docChange.document
                                val notifId = doc.id
                                if (hasProcessedNotification(notifId)) continue

                                val title = doc.getString("title") ?: "إشعار جديد من SARIB TV"
                                val message = doc.getString("message") ?: doc.getString("body") ?: ""

                                if (title.isNotBlank() || message.isNotBlank()) {
                                    markNotificationProcessed(notifId)
                                    NotificationHelper.showBroadcastNotification(
                                        context = context,
                                        notifId = notifId,
                                        title = title,
                                        message = message.ifBlank { title }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore listener setup failed: ${e.message}")
        }
    }

    private fun pollHttpNotifications() {
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
                                    context = context,
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
                                context = context,
                                notifId = key,
                                title = title,
                                message = message.ifBlank { title }
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore network errors in periodic background check
            }
        }
    }

    private fun hasProcessedNotification(id: String): Boolean {
        return prefs.getBoolean("processed_$id", false)
    }

    private fun markNotificationProcessed(id: String) {
        prefs.edit().putBoolean("processed_$id", true).apply()
    }
}
