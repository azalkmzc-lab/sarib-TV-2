package com.example.data.download

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.util.Log
import android.widget.Toast
import com.example.data.local.DownloadEntity
import com.example.data.local.SaribDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class SaribDownloadManager private constructor(private val context: Context) {

    private val db = SaribDatabase.getDatabase(context)
    private val dao = db.saribDao()
    private val downloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val activeJobs = ConcurrentHashMap<String, Job>()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    val allDownloads: Flow<List<DownloadEntity>> = dao.getAllDownloads()
    val activeDownloads: Flow<List<DownloadEntity>> = dao.getActiveDownloads()
    val completedDownloads: Flow<List<DownloadEntity>> = dao.getCompletedDownloads()

    // Real-time memory state for fast UI updates during active downloads
    private val _realtimeProgress = MutableStateFlow<Map<String, DownloadEntity>>(emptyMap())
    val realtimeProgress: StateFlow<Map<String, DownloadEntity>> = _realtimeProgress.asStateFlow()

    data class StorageInfo(
        val appUsedBytes: Long,
        val totalStorageBytes: Long,
        val freeStorageBytes: Long
    )

    fun startDownload(
        id: String,
        title: String,
        subtitle: String = "",
        posterUrl: String = "",
        streamUrl: String,
        selectedQuality: String = "1080p FHD",
        subtitleUrl: String = "",
        subtitleName: String = "",
        contentType: String = "MOVIE"
    ) {
        if (streamUrl.isBlank()) {
            Toast.makeText(context, "رابط الفيديو غير صالح للتحميل", Toast.LENGTH_SHORT).show()
            return
        }

        // Cancel existing job if running
        activeJobs[id]?.cancel()

        val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9\\u0600-\\u06FF_-]"), "_").take(40)
        val ext = when {
            streamUrl.contains(".mp4", ignoreCase = true) -> "mp4"
            streamUrl.contains(".mkv", ignoreCase = true) -> "mkv"
            streamUrl.contains(".ts", ignoreCase = true) -> "ts"
            streamUrl.contains(".m3u8", ignoreCase = true) -> "mp4"
            else -> "mp4"
        }
        val fileName = "${sanitizedTitle}_${System.currentTimeMillis()}.$ext"

        val downloadDir = getDownloadDirectory()
        val targetFile = File(downloadDir, fileName)

        val entity = DownloadEntity(
            id = id,
            title = title,
            subtitle = subtitle,
            posterUrl = posterUrl,
            streamUrl = streamUrl,
            selectedQuality = selectedQuality,
            subtitleUrl = subtitleUrl,
            subtitleName = subtitleName,
            localFilePath = targetFile.absolutePath,
            localFileName = fileName,
            contentType = contentType,
            status = "DOWNLOADING",
            progress = 0,
            bytesDownloaded = 0L,
            totalBytes = 0L,
            speedBps = 0L,
            etaSeconds = 0L,
            createdAt = System.currentTimeMillis()
        )

        val job = downloadScope.launch {
            try {
                dao.insertOrUpdateDownload(entity)
                updateMemoryState(entity)

                // Optional: Download subtitle file if selected
                if (subtitleUrl.isNotBlank()) {
                    downloadSubtitleFile(subtitleUrl, sanitizedTitle, downloadDir)
                }

                executeDownloadLoop(entity, targetFile)
            } catch (e: Exception) {
                Log.e("SaribDownloadManager", "Download error: ${e.message}", e)
                if (isActive) {
                    dao.markDownloadFailed(id, e.localizedMessage ?: "فشل التنزيل")
                    removeFromMemoryState(id)
                }
            } finally {
                activeJobs.remove(id)
            }
        }

        activeJobs[id] = job
        Toast.makeText(context, "بدأ تنزيل: $title", Toast.LENGTH_SHORT).show()
    }

    private suspend fun executeDownloadLoop(initialEntity: DownloadEntity, targetFile: File) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(initialEntity.streamUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
            .header("Accept", "*/*")
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val err = "خطأ في الخادم (رمز ${response.code})"
            dao.markDownloadFailed(initialEntity.id, err)
            removeFromMemoryState(initialEntity.id)
            response.close()
            return@withContext
        }

        val body = response.body ?: run {
            dao.markDownloadFailed(initialEntity.id, "محتوى الفيديو غير متاح")
            removeFromMemoryState(initialEntity.id)
            response.close()
            return@withContext
        }

        val totalLength = body.contentLength().coerceAtLeast(0L)
        var bytesDownloaded = 0L
        val inputStream: InputStream = body.byteStream()
        val outputStream = FileOutputStream(targetFile)

        val buffer = ByteArray(64 * 1024)
        var bytesRead: Int

        var lastDbUpdateTime = System.currentTimeMillis()
        var lastBytesInInterval = 0L
        var speedBps = 0L
        var etaSeconds = 0L

        try {
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                if (!isActive) {
                    // Paused or cancelled
                    break
                }
                outputStream.write(buffer, 0, bytesRead)
                bytesDownloaded += bytesRead
                lastBytesInInterval += bytesRead

                val now = System.currentTimeMillis()
                val elapsedSinceUpdate = now - lastDbUpdateTime

                if (elapsedSinceUpdate >= 800L) {
                    speedBps = if (elapsedSinceUpdate > 0) (lastBytesInInterval * 1000L) / elapsedSinceUpdate else 0L
                    lastBytesInInterval = 0L
                    lastDbUpdateTime = now

                    val progress = if (totalLength > 0) {
                        ((bytesDownloaded * 100L) / totalLength).toInt().coerceIn(0, 99)
                    } else {
                        50 // indeterminate
                    }

                    etaSeconds = if (speedBps > 0 && totalLength > bytesDownloaded) {
                        (totalLength - bytesDownloaded) / speedBps
                    } else {
                        0L
                    }

                    val updatedEntity = initialEntity.copy(
                        progress = progress,
                        bytesDownloaded = bytesDownloaded,
                        totalBytes = if (totalLength > 0) totalLength else bytesDownloaded,
                        speedBps = speedBps,
                        etaSeconds = etaSeconds,
                        status = "DOWNLOADING"
                    )

                    updateMemoryState(updatedEntity)
                    dao.updateDownloadProgress(
                        id = initialEntity.id,
                        progress = progress,
                        bytesDownloaded = bytesDownloaded,
                        totalBytes = if (totalLength > 0) totalLength else bytesDownloaded,
                        speedBps = speedBps,
                        etaSeconds = etaSeconds,
                        status = "DOWNLOADING"
                    )
                }
            }

            outputStream.flush()

            if (isActive) {
                // Download Finished Successfully
                val finalTotal = if (totalLength > 0) totalLength else bytesDownloaded
                dao.updateDownloadProgress(
                    id = initialEntity.id,
                    progress = 100,
                    bytesDownloaded = finalTotal,
                    totalBytes = finalTotal,
                    speedBps = 0L,
                    etaSeconds = 0L,
                    status = "COMPLETED"
                )
                dao.markDownloadCompleted(
                    id = initialEntity.id,
                    filePath = targetFile.absolutePath,
                    status = "COMPLETED",
                    completedAt = System.currentTimeMillis()
                )
                removeFromMemoryState(initialEntity.id)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "اكتمل تنزيل: ${initialEntity.title}", Toast.LENGTH_LONG).show()
                }
            }
        } finally {
            try { outputStream.close() } catch (_: Exception) {}
            try { inputStream.close() } catch (_: Exception) {}
            try { response.close() } catch (_: Exception) {}
        }
    }

    private suspend fun downloadSubtitleFile(subUrl: String, title: String, downloadDir: File) = withContext(Dispatchers.IO) {
        try {
            val subFile = File(downloadDir, "${title}_subtitle.srt")
            val request = Request.Builder().url(subUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful && response.body != null) {
                val bytes = response.body!!.bytes()
                subFile.writeBytes(bytes)
                Log.d("SaribDownloadManager", "Downloaded subtitle to: ${subFile.absolutePath}")
            }
            response.close()
        } catch (e: Exception) {
            Log.w("SaribDownloadManager", "Subtitle download ignored: ${e.message}")
        }
    }

    fun pauseDownload(id: String) {
        activeJobs[id]?.cancel()
        activeJobs.remove(id)
        downloadScope.launch {
            dao.updateDownloadStatus(id, "PAUSED")
            removeFromMemoryState(id)
        }
        Toast.makeText(context, "تم إيقاف التنزيل مؤقتاً", Toast.LENGTH_SHORT).show()
    }

    fun resumeDownload(entity: DownloadEntity) {
        startDownload(
            id = entity.id,
            title = entity.title,
            subtitle = entity.subtitle,
            posterUrl = entity.posterUrl,
            streamUrl = entity.streamUrl,
            selectedQuality = entity.selectedQuality,
            subtitleUrl = entity.subtitleUrl,
            subtitleName = entity.subtitleName,
            contentType = entity.contentType
        )
    }

    fun cancelDownload(id: String) {
        activeJobs[id]?.cancel()
        activeJobs.remove(id)
        downloadScope.launch {
            val item = dao.getDownloadById(id)
            if (item != null && item.localFilePath.isNotBlank()) {
                try {
                    val f = File(item.localFilePath)
                    if (f.exists()) f.delete()
                } catch (_: Exception) {}
            }
            dao.deleteDownloadById(id)
            removeFromMemoryState(id)
        }
        Toast.makeText(context, "تم إلغاء التنزيل", Toast.LENGTH_SHORT).show()
    }

    fun deleteCompletedDownload(id: String) {
        downloadScope.launch {
            val item = dao.getDownloadById(id)
            if (item != null && item.localFilePath.isNotBlank()) {
                try {
                    val f = File(item.localFilePath)
                    if (f.exists()) f.delete()
                } catch (_: Exception) {}
            }
            dao.deleteDownloadById(id)
        }
        Toast.makeText(context, "تم حذف الملف من الجهاز", Toast.LENGTH_SHORT).show()
    }

    private fun updateMemoryState(entity: DownloadEntity) {
        val current = _realtimeProgress.value.toMutableMap()
        current[entity.id] = entity
        _realtimeProgress.value = current
    }

    private fun removeFromMemoryState(id: String) {
        val current = _realtimeProgress.value.toMutableMap()
        current.remove(id)
        _realtimeProgress.value = current
    }

    private fun getDownloadDirectory(): File {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "SaribTV")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getStorageInfo(): StorageInfo {
        var usedBytes = 0L
        try {
            val dir = getDownloadDirectory()
            dir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    usedBytes += file.length()
                }
            }
        } catch (_: Exception) {}

        var freeBytes = 0L
        var totalBytes = 0L
        try {
            val stat = StatFs(context.filesDir.absolutePath)
            freeBytes = stat.availableBlocksLong * stat.blockSizeLong
            totalBytes = stat.blockCountLong * stat.blockSizeLong
        } catch (_: Exception) {}

        return StorageInfo(
            appUsedBytes = usedBytes,
            totalStorageBytes = totalBytes,
            freeStorageBytes = freeBytes
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: SaribDownloadManager? = null

        fun getInstance(context: Context): SaribDownloadManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SaribDownloadManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }

        /**
         * Opens stream URL in external download manager (1DM, ADM, IDM, Browser, etc.)
         */
        fun openInExternalDownloader(context: Context, streamUrl: String, title: String) {
            if (streamUrl.isBlank()) {
                Toast.makeText(context, "رابط الفيديو غير متوفر", Toast.LENGTH_SHORT).show()
                return
            }

            try {
                val uri = Uri.parse(streamUrl)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "video/*")
                    putExtra(Intent.EXTRA_TITLE, title)
                    putExtra("title", title)
                    putExtra("android.intent.extra.TITLE", title)
                    putExtra("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                val chooser = Intent.createChooser(intent, "اختر تطبيق التحميل (1DM / ADM / المتصفح)")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                // Fallback to browser or copy
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(streamUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                } catch (e2: Exception) {
                    copyToClipboard(context, streamUrl, title)
                }
            }
        }

        fun copyToClipboard(context: Context, text: String, title: String = "") {
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Download Link", text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "تم نسخ رابط التحميل المباشر بنجاح 📋", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "فشل نسخ الرابط", Toast.LENGTH_SHORT).show()
            }
        }

        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 MB"
            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.2f GB", gb)
                mb >= 1.0 -> String.format("%.1f MB", mb)
                else -> String.format("%.0f KB", kb)
            }
        }

        fun formatSpeed(speedBps: Long): String {
            if (speedBps <= 0) return "0 KB/s"
            val kbps = speedBps / 1024.0
            val mbps = kbps / 1024.0
            return when {
                mbps >= 1.0 -> String.format("%.1f MB/s", mbps)
                else -> String.format("%.0f KB/s", kbps)
            }
        }

        fun formatEta(etaSeconds: Long): String {
            if (etaSeconds <= 0) return "لحظات متبقية..."
            val minutes = etaSeconds / 60
            val seconds = etaSeconds % 60
            val hours = minutes / 60
            return when {
                hours > 0 -> "متبقي $hours ساعة و ${minutes % 60} دقيقة"
                minutes > 0 -> "متبقي $minutes دقيقة و $seconds ثانية"
                else -> "متبقي $seconds ثانية"
            }
        }
    }
}
