package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object NotificationHelper {

    const val MATCHES_CHANNEL_ID = "sarib_matches_channel"
    const val NEWS_CHANNEL_ID = "sarib_news_channel"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val matchesChannel = NotificationChannel(
                MATCHES_CHANNEL_ID,
                "إشعارات المباريات والأندية",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات انطلاق المباريات المباشرة وأهداف الفرق المفضلة"
                enableVibration(true)
            }

            val newsChannel = NotificationChannel(
                NEWS_CHANNEL_ID,
                "تنبيهات وأخبار SARIB TV",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "إشعارات القنوات والتحديثات الجديدة"
            }

            notificationManager.createNotificationChannel(matchesChannel)
            notificationManager.createNotificationChannel(newsChannel)
        }
    }

    fun showMatchAlertNotification(
        context: Context,
        matchId: String,
        title: String,
        message: String
    ) {
        try {
            initNotificationChannels(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                matchId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, MATCHES_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val manager = NotificationManagerCompat.from(context)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                manager.notify(matchId.hashCode(), notification)
            }
        } catch (e: Exception) {
            // Ignored if permissions not granted
        }
    }

    fun showBroadcastNotification(
        context: Context,
        notifId: String,
        title: String,
        message: String
    ) {
        try {
            initNotificationChannels(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notifId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, NEWS_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val manager = NotificationManagerCompat.from(context)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                manager.notify(notifId.hashCode(), notification)
            }
        } catch (e: Exception) {
            // Ignored
        }
    }
}
