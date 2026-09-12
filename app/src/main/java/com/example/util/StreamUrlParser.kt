package com.example.util

import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory
import androidx.media3.extractor.ts.TsExtractor
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.DrmSessionManager
import androidx.media3.exoplayer.drm.FrameworkMediaDrm
import androidx.media3.exoplayer.drm.LocalMediaDrmCallback
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder

data class ParsedStreamConfig(
    val cleanUrl: String,
    val mimeType: String?,
    val headers: Map<String, String>,
    val userAgent: String?,
    val drmScheme: String?,
    val clearKeyJson: String?,
    val widevineLicenseUrl: String?,
    val targetHost: String? = null
)

object StreamUrlParser {

    private const val TAG = "StreamUrlParser"
    const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

    /**
     * Parses IPTV, Cloudflare Worker proxies, and DRM stream URLs.
     * Supports:
     * 1. Proxy worker streams like:
     *    https://ostora.ravynerinnn.workers.dev/proxy?url=https%3A%2F%2Fwww.maziikaaaaaa.shop%2Fx1%2F225587619216738.php&ua=Mozilla...&iv=...
     * 2. Pipe delimiter streams like:
     *    https://server.com/live.m3u8|User-Agent=Mozilla&Referer=https://site.com
     * 3. ClearKey & Widevine DRM parameter strings.
     */
    fun parse(rawUrl: String): ParsedStreamConfig {
        var cleanUrl = rawUrl.trim()
        val headers = mutableMapOf<String, String>()
        var userAgent: String? = null
        var drmScheme: String? = null
        var clearKeyJson: String? = null
        var widevineLicenseUrl: String? = null
        var targetHost: String? = null
        var embeddedTargetUrl: String? = null

        try {
            // 1. Handle Pipe Delimiter (|) common in IPTV/M3U formats
            var pipeQueryPart = ""
            if (cleanUrl.contains("|")) {
                val parts = cleanUrl.split("|", limit = 2)
                cleanUrl = parts[0].trim().trimEnd('?').trim()
                pipeQueryPart = parts.getOrNull(1)?.trim().orEmpty()
            } else if (cleanUrl.contains("drmScheme=") || cleanUrl.contains("drmLicense=")) {
                val idx = cleanUrl.indexOf("drmScheme=")
                if (idx > 0) {
                    pipeQueryPart = cleanUrl.substring(idx)
                    cleanUrl = cleanUrl.substring(0, idx).trimEnd('?', '&').trim()
                }
            }

            // 2. Parse direct URI query parameters (e.g. for worker proxies with ?url=...&ua=...&iv=...)
            try {
                val uri = Uri.parse(cleanUrl)
                if (uri.isHierarchical) {
                    // Extract User-Agent from query parameters if present
                    val uaParam = uri.getQueryParameter("ua")
                        ?: uri.getQueryParameter("user_agent")
                        ?: uri.getQueryParameter("user-agent")
                        ?: uri.getQueryParameter("User-Agent")
                        ?: uri.getQueryParameter("u-a")
                    if (!uaParam.isNullOrBlank()) {
                        val decodedUa = try {
                            URLDecoder.decode(uaParam, "UTF-8")
                        } catch (e: Exception) {
                            uaParam
                        }
                        userAgent = decodedUa
                        headers["User-Agent"] = decodedUa
                    }

                    // Extract embedded target URL (e.g., ?url=https%3A%2F%2Fwww.maziikaaaaaa.shop...)
                    val innerUrlParam = uri.getQueryParameter("url")
                    if (!innerUrlParam.isNullOrBlank()) {
                        val decodedInnerUrl = try {
                            URLDecoder.decode(innerUrlParam, "UTF-8")
                        } catch (e: Exception) {
                            innerUrlParam
                        }
                        embeddedTargetUrl = decodedInnerUrl
                        val innerUri = Uri.parse(decodedInnerUrl)
                        val host = innerUri.host
                        if (!host.isNullOrBlank()) {
                            targetHost = host
                            val scheme = innerUri.scheme ?: "https"
                            val originUrl = "$scheme://$host"
                            headers.putIfAbsent("Origin", originUrl)
                            headers.putIfAbsent("Referer", "$originUrl/")
                        }
                    }

                    // Extract Referer / Referrer from query
                    val refParam = uri.getQueryParameter("referer") ?: uri.getQueryParameter("referrer")
                    if (!refParam.isNullOrBlank()) {
                        val decodedRef = try { URLDecoder.decode(refParam, "UTF-8") } catch (e: Exception) { refParam }
                        headers["Referer"] = decodedRef
                    }

                    // Extract Origin from query
                    val originParam = uri.getQueryParameter("origin")
                    if (!originParam.isNullOrBlank()) {
                        val decodedOrigin = try { URLDecoder.decode(originParam, "UTF-8") } catch (e: Exception) { originParam }
                        headers["Origin"] = decodedOrigin
                    }

                    // Extract Cookie from query
                    val cookieParam = uri.getQueryParameter("cookie")
                    if (!cookieParam.isNullOrBlank()) {
                        headers["Cookie"] = cookieParam
                    }

                    // Extract all other query parameters (e.g., iv, token, auth, key) into headers
                    try {
                        for (paramName in uri.queryParameterNames) {
                            if (!paramName.equals("url", ignoreCase = true) &&
                                !paramName.equals("ua", ignoreCase = true) &&
                                !paramName.equals("user-agent", ignoreCase = true) &&
                                !paramName.equals("u-a", ignoreCase = true)
                            ) {
                                val paramVal = uri.getQueryParameter(paramName)
                                if (!paramVal.isNullOrBlank()) {
                                    headers[paramName] = paramVal
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not enumerate query parameters: ${e.message}")
                    }

                    // Extract DRM scheme/key from query
                    val qDrmScheme = uri.getQueryParameter("drmScheme")
                    if (!qDrmScheme.isNullOrBlank()) drmScheme = qDrmScheme.lowercase()
                    val qDrmLicense = uri.getQueryParameter("drmLicense") ?: uri.getQueryParameter("license_key")
                    if (!qDrmLicense.isNullOrBlank()) {
                        if (qDrmLicense.contains(":")) {
                            val parts = qDrmLicense.split(":", limit = 2)
                            clearKeyJson = buildClearKeyJson(parts[0].trim(), parts[1].trim())
                            drmScheme = "clearkey"
                        } else if (qDrmLicense.startsWith("http://") || qDrmLicense.startsWith("https://")) {
                            widevineLicenseUrl = qDrmLicense
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Non-critical error parsing URI query parameters: ${e.message}")
            }

            // 3. Parse pipe query part if available
            if (pipeQueryPart.isNotEmpty()) {
                val params = pipeQueryPart.split("&")
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
                            key.equals("User-Agent", ignoreCase = true) || key.equals("user_agent", ignoreCase = true) || key.equals("ua", ignoreCase = true) -> {
                                val decoded = try { URLDecoder.decode(value, "UTF-8") } catch (e: Exception) { value }
                                userAgent = decoded
                                headers["User-Agent"] = decoded
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

        // Set high-compatibility headers if not already set
        val finalUserAgent = userAgent ?: DEFAULT_USER_AGENT
        headers.putIfAbsent("User-Agent", finalUserAgent)
        headers.putIfAbsent("Accept", "*/*")
        headers.putIfAbsent("Accept-Language", "ar,en-US;q=0.9,en;q=0.8")
        headers.putIfAbsent("Connection", "keep-alive")

        // 4. Intelligent MIME Type resolution
        val lower = cleanUrl.lowercase()
        val targetLower = embeddedTargetUrl?.lowercase().orEmpty()

        // Extract path portion without query string to accurately determine file extensions
        val pathWithoutQuery = cleanUrl.substringBefore('?').substringBefore('#').lowercase()
        val targetPathWithoutQuery = (embeddedTargetUrl ?: "").substringBefore('?').substringBefore('#').lowercase()

        val isDASH = pathWithoutQuery.endsWith(".mpd") || lower.contains(".mpd") || targetPathWithoutQuery.endsWith(".mpd") || targetLower.contains(".mpd")
        val isM3u8 = pathWithoutQuery.endsWith(".m3u8") || lower.contains(".m3u8") || targetPathWithoutQuery.endsWith(".m3u8") || targetLower.contains(".m3u8") || lower.contains(".m3u") || targetLower.contains(".m3u")
        val isTs = pathWithoutQuery.endsWith(".ts") || targetPathWithoutQuery.endsWith(".ts") ||
                lower.contains(".ts?") || targetLower.contains(".ts?") ||
                pathWithoutQuery.contains(".ts") || targetPathWithoutQuery.contains(".ts")
        val isMp4 = pathWithoutQuery.endsWith(".mp4") || targetPathWithoutQuery.endsWith(".mp4") ||
                lower.contains(".mp4?") || targetLower.contains(".mp4?")
        val isMkv = pathWithoutQuery.endsWith(".mkv") || targetPathWithoutQuery.endsWith(".mkv")
        val isWebm = pathWithoutQuery.endsWith(".webm") || targetPathWithoutQuery.endsWith(".webm")

        val mimeType = when {
            isDASH -> MimeTypes.APPLICATION_MPD
            isM3u8 -> MimeTypes.APPLICATION_M3U8
            isTs -> MimeTypes.VIDEO_MP2T
            isMp4 -> MimeTypes.APPLICATION_MP4
            isMkv -> MimeTypes.APPLICATION_MATROSKA
            isWebm -> MimeTypes.APPLICATION_WEBM
            // Cloudflare Worker Proxies, PHP stream scripts (when not an explicit video file)
            lower.contains("workers.dev") || lower.contains("/proxy") || lower.contains("proxy?url=") ||
            lower.contains(".php") || targetLower.contains(".php") ||
            lower.contains("playlist") || lower.contains("manifest") -> {
                MimeTypes.APPLICATION_M3U8
            }
            else -> null
        }

        return ParsedStreamConfig(
            cleanUrl = cleanUrl,
            mimeType = mimeType,
            headers = headers,
            userAgent = finalUserAgent,
            drmScheme = drmScheme,
            clearKeyJson = clearKeyJson,
            widevineLicenseUrl = widevineLicenseUrl,
            targetHost = targetHost
        )
    }

    /**
     * Builds a DefaultExtractorsFactory optimized for MPEG-TS, HLS, MP4 and live streams with fast packet seeking.
     */
    fun createExtractorsFactory(): DefaultExtractorsFactory {
        return DefaultExtractorsFactory()
            .setConstantBitrateSeekingEnabled(true)
            .setTsExtractorTimestampSearchBytes(1500 * TsExtractor.TS_PACKET_SIZE)
            .setTsExtractorFlags(
                DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES or
                DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS
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
     * Applies parsed headers to HttpDataSource.Factory with high performance network properties
     */
    fun configureHttpDataSource(
        factory: DefaultHttpDataSource.Factory,
        config: ParsedStreamConfig
    ) {
        val customUa = config.userAgent ?: DEFAULT_USER_AGENT
        factory.setUserAgent(customUa)
        factory.setAllowCrossProtocolRedirects(true)
        factory.setConnectTimeoutMs(4000)
        factory.setReadTimeoutMs(8000)
        val combinedHeaders = mutableMapOf<String, String>(
            "Connection" to "keep-alive",
            "Accept-Encoding" to "identity"
        )
        combinedHeaders.putAll(config.headers)
        factory.setDefaultRequestProperties(combinedHeaders)
    }
}

