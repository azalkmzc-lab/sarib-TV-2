package com.example.util

import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.DrmSessionManager
import androidx.media3.exoplayer.drm.FrameworkMediaDrm
import androidx.media3.exoplayer.drm.LocalMediaDrmCallback
import org.json.JSONArray
import org.json.JSONObject

data class ParsedStreamConfig(
    val cleanUrl: String,
    val mimeType: String?,
    val headers: Map<String, String>,
    val userAgent: String?,
    val drmScheme: String?,
    val clearKeyJson: String?,
    val widevineLicenseUrl: String?
)

object StreamUrlParser {

    private const val TAG = "StreamUrlParser"

    /**
     * Parses IPTV stream URLs that may contain DRM license parameters or HTTP headers.
     * Example input:
     * https://shahid-sports-1-enc.edgenextcdn.net/out/v1/0169cfa282614ebe97ef0201da87bb04/index.mpd?|drmScheme=clearkey&drmLicense=ab081a704aad41829b1123b09b6ecafd:dd51609092fff15e68b91debf5d591f9
     */
    fun parse(rawUrl: String): ParsedStreamConfig {
        var cleanUrl = rawUrl.trim()
        val headers = mutableMapOf<String, String>()
        var userAgent: String? = null
        var drmScheme: String? = null
        var clearKeyJson: String? = null
        var widevineLicenseUrl: String? = null

        try {
            // Check for delimiter '|' (pipe delimiter commonly used in IPTV / m3u / Xtream)
            var queryPart = ""
            if (cleanUrl.contains("|")) {
                val parts = cleanUrl.split("|", limit = 2)
                cleanUrl = parts[0].trim().trimEnd('?').trim()
                queryPart = parts.getOrNull(1)?.trim().orEmpty()
            } else if (cleanUrl.contains("drmScheme=") || cleanUrl.contains("drmLicense=")) {
                val idx = cleanUrl.indexOf("drmScheme=")
                if (idx > 0) {
                    queryPart = cleanUrl.substring(idx)
                    cleanUrl = cleanUrl.substring(0, idx).trimEnd('?', '&').trim()
                }
            }

            if (queryPart.isNotEmpty()) {
                val params = queryPart.split("&")
                var keyIdHex: String? = null
                var keyHex: String? = null

                for (param in params) {
                    val kv = param.split("=", limit = 2)
                    if (kv.size == 2) {
                        val key = kv[0].trim()
                        val value = kv[1].trim()

                        when {
                            key.equals("drmScheme", ignoreCase = true) -> {
                                drmScheme = value.lowercase()
                            }
                            key.equals("drmLicense", ignoreCase = true) || key.equals("license_key", ignoreCase = true) -> {
                                if (value.contains(":")) {
                                    val keyParts = value.split(":", limit = 2)
                                    keyIdHex = keyParts[0].trim()
                                    keyHex = keyParts[1].trim()
                                } else if (value.startsWith("http://") || value.startsWith("https://")) {
                                    widevineLicenseUrl = value
                                }
                            }
                            key.equals("keyId", ignoreCase = true) -> {
                                keyIdHex = value
                            }
                            key.equals("key", ignoreCase = true) -> {
                                keyHex = value
                            }
                            key.equals("User-Agent", ignoreCase = true) || key.equals("user_agent", ignoreCase = true) -> {
                                userAgent = value
                                headers["User-Agent"] = value
                            }
                            key.equals("Referer", ignoreCase = true) || key.equals("referrer", ignoreCase = true) -> {
                                headers["Referer"] = value
                            }
                            key.equals("Origin", ignoreCase = true) -> {
                                headers["Origin"] = value
                            }
                            key.equals("Cookie", ignoreCase = true) -> {
                                headers["Cookie"] = value
                            }
                            key.equals("Authorization", ignoreCase = true) -> {
                                headers["Authorization"] = value
                            }
                            else -> {
                                headers[key] = value
                            }
                        }
                    }
                }

                // Construct ClearKey JSON if keyId and key are present
                if (!keyIdHex.isNullOrBlank() && !keyHex.isNullOrBlank()) {
                    clearKeyJson = buildClearKeyJson(keyIdHex, keyHex)
                    drmScheme = "clearkey"
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing stream URL: ${e.message}")
        }

        // Determine MimeType
        val mimeType = when {
            cleanUrl.contains(".mpd", ignoreCase = true) || cleanUrl.contains("/dash/", ignoreCase = true) -> {
                MimeTypes.APPLICATION_MPD
            }
            cleanUrl.contains(".m3u8", ignoreCase = true) || cleanUrl.contains("/hls/", ignoreCase = true) -> {
                MimeTypes.APPLICATION_M3U8
            }
            cleanUrl.contains(".mp4", ignoreCase = true) -> {
                MimeTypes.APPLICATION_MP4
            }
            else -> null
        }

        return ParsedStreamConfig(
            cleanUrl = cleanUrl,
            mimeType = mimeType,
            headers = headers,
            userAgent = userAgent,
            drmScheme = drmScheme,
            clearKeyJson = clearKeyJson,
            widevineLicenseUrl = widevineLicenseUrl
        )
    }

    /**
     * Converts Hex keyId and Hex key to W3C ClearKey JSON
     */
    private fun buildClearKeyJson(keyIdHex: String, keyHex: String): String {
        val kidBase64 = hexToBase64Url(keyIdHex)
        val keyBase64 = hexToBase64Url(keyHex)

        val keyObj = JSONObject().apply {
            put("kty", "oct")
            put("k", keyBase64)
            put("kid", kidBase64)
        }

        val keysArray = JSONArray().apply {
            put(keyObj)
        }

        return JSONObject().apply {
            put("keys", keysArray)
            put("type", "temporary")
        }.toString()
    }

    private fun hexToBase64Url(hexString: String): String {
        val clean = hexString.trim().replace("-", "").replace(" ", "")
        val bytes = ByteArray(clean.length / 2)
        var i = 0
        while (i < clean.length) {
            val high = Character.digit(clean[i], 16)
            val low = Character.digit(clean[i + 1], 16)
            bytes[i / 2] = ((high shl 4) + low).toByte()
            i += 2
        }
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    /**
     * Creates an ExoPlayer DrmSessionManager if ClearKey or Widevine DRM is present.
     */
    fun createDrmSessionManager(config: ParsedStreamConfig): DrmSessionManager? {
        if (!config.clearKeyJson.isNullOrBlank()) {
            try {
                val callback = LocalMediaDrmCallback(config.clearKeyJson.toByteArray(Charsets.UTF_8))
                return DefaultDrmSessionManager.Builder()
                    .setUuidAndExoMediaDrmProvider(C.CLEARKEY_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
                    .setMultiSession(false)
                    .build(callback)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to build ClearKey DrmSessionManager: ${e.message}")
            }
        }
        return null
    }

    /**
     * Applies parsed headers to HttpDataSource.Factory
     */
    fun configureHttpDataSource(
        factory: DefaultHttpDataSource.Factory,
        config: ParsedStreamConfig
    ) {
        val customUa = config.userAgent ?: "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 SARIB-TV-Player/1.0"
        factory.setUserAgent(customUa)
        if (config.headers.isNotEmpty()) {
            factory.setDefaultRequestProperties(config.headers)
        }
    }
}
