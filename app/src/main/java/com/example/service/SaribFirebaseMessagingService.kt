package com.example.service

import android.util.Log
import com.example.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SaribFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        try {
            val title = remoteMessage.notification?.title
                ?: remoteMessage.data["title"]
                ?: remoteMessage.data["name"]
                ?: "إشعار SARIB TV"

            val message = remoteMessage.notification?.body
                ?: remoteMessage.data["message"]
                ?: remoteMessage.data["body"]
                ?: remoteMessage.data["text"]
                ?: ""

            val notifId = remoteMessage.messageId ?: "fcm_${System.currentTimeMillis()}"

            if (title.isNotBlank() || message.isNotBlank()) {
                NotificationHelper.showBroadcastNotification(
                    context = applicationContext,
                    notifId = notifId,
                    title = title,
                    message = message.ifBlank { title }
                )
            }
        } catch (e: Exception) {
            Log.e("SaribFCM", "Failed to handle incoming FCM message: ${e.message}", e)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("SaribFCM", "New FCM registration token received: $token")
    }
}
