package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    const val MATCHES_CHANNEL_ID = "sarib_matches_channel"
    const val NEWS_CHANNEL_ID = "sarib_news_channel"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val matchesChannel = NotificationChannel(
                MATCHES_CHANNEL_ID,
                "إشعارات وتنبيهات المباريات",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات انطلاق المباريات المباشرة وتحديثات البث"
                enableVibration(true)
                setShowBadge(true)
            }

            val newsChannel = NotificationChannel(
                NEWS_CHANNEL_ID,
                "إشعارات وإعلانات SARIB TV",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات القنوات والمحتوى الجديد ورسائل لوحة التحكم السحابية"
                enableVibration(true)
                setShowBadge(true)
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

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val notification = NotificationCompat.Builder(context, MATCHES_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_sarib_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(defaultSoundUri)
                .setVibrate(longArrayOf(0, 300, 200, 300))
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
            // Ignored
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

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val notification = NotificationCompat.Builder(context, NEWS_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_sarib_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(defaultSoundUri)
                .setVibrate(longArrayOf(0, 300, 200, 300))
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
