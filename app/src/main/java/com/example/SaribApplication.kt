package com.example

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.service.NotificationSyncWorker
import com.example.util.FirebaseNotificationListener
import com.example.util.NotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class SaribApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        
        // 1. Initialize Notification Channels
        try {
            NotificationHelper.initNotificationChannels(this)
        } catch (_: Exception) {}

        // 2. Start persistent in-app listener
        try {
            FirebaseNotificationListener(this).startListening()
        } catch (_: Exception) {}

        // 3. Schedule Background WorkManager for notifications when app is closed
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWorkRequest = PeriodicWorkRequestBuilder<NotificationSyncWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "SaribNotificationSyncWork",
                ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest
            )
        } catch (e: Exception) {
            android.util.Log.w("SaribApp", "WorkManager schedule error: ${e.message}")
        }

        // 4. Subscribe to FCM topics for push notifications even when closed
        try {
            if (FirebaseApp.getApps(this).isNotEmpty()) {
                val messaging = FirebaseMessaging.getInstance()
                messaging.subscribeToTopic("all")
                messaging.subscribeToTopic("notifications")
                messaging.subscribeToTopic("broadcast")
                messaging.subscribeToTopic("general")
                messaging.subscribeToTopic("matches")
                messaging.subscribeToTopic("news")
            }
        } catch (e: Exception) {
            android.util.Log.w("SaribApp", "FCM topic subscription error: ${e.message}")
        }
    }

    override fun newImageLoader(): ImageLoader {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .allowRgb565(true)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("sarib_image_cache"))
                    .maxSizeBytes(80L * 1024 * 1024) // 80 MB disk cache
                    .build()
            }
            .crossfade(150)
            .respectCacheHeaders(false)
            .build()
    }
}
