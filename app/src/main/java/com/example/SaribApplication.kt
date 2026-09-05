package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.util.FirebaseNotificationListener
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class SaribApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        // Start persistent background push notification listener
        try {
            FirebaseNotificationListener(this).startListening()
        } catch (e: Exception) {
            // Ignored
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
