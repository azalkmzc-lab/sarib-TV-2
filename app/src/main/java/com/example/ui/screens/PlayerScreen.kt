package com.example.ui.screens

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Rational
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.data.model.ChannelItem
import com.example.security.AppSecurityGuard
import com.example.ui.components.SaribLoadingIndicator
import com.example.ui.components.VpnBlockedDialog
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribDarkBackground
import com.example.ui.theme.SaribDarkCard
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribLiveRed
import com.example.ui.theme.SaribTextMuted
import com.example.ui.theme.SaribTextPrimary
import com.example.ui.theme.SaribTextSecondary
import com.example.util.StreamUrlParser
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale
import java.util.concurrent.TimeUnit

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

data class QualityOption(
    val label: String,
    val resolutionLabel: String,
    val maxHeight: Int,
    val maxBitrate: Int
)

data class AudioTrackOption(
    val id: String,
    val label: String,
    val language: String,
    val trackGroupIndex: Int,
    val trackIndex: Int
)

data class SubtitleTrackOption(
    val id: String,
    val label: String,
    val language: String,
    val trackGroupIndex: Int = -1,
    val trackIndex: Int = -1
)

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    title: String,
    subtitle: String,
    streamUrl: String,
    isLive: Boolean,
    onBackClick: () -> Unit,
    servers: List<Pair<String, String>> = emptyList(),
    availableChannels: List<ChannelItem> = emptyList(),
    initialProgressMs: Long = 0L,
    onProgressUpdate: (progressMs: Long, durationMs: Long) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    val serverOptions = remember(streamUrl, servers) {
        val valid = servers.filter { it.second.isNotBlank() }
        if (valid.isNotEmpty()) {
            valid
        } else {
            listOf("سيرفر البث المباشر (الرئيسي)" to streamUrl)
        }
    }

    var selectedServerIndex by remember { mutableIntStateOf(0) }
    var currentActiveUrl by remember(selectedServerIndex, serverOptions) {
        mutableStateOf(serverOptions.getOrNull(selectedServerIndex)?.second ?: streamUrl)
    }

    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(initialProgressMs.coerceAtLeast(0L)) }
    var duration by remember { mutableLongStateOf(0L) }
    var hasResumedInitialProgress by remember { mutableStateOf(false) }
    var areControlsVisible by remember { mutableStateOf(true) }
    var isControlsLocked by remember { mutableStateOf(false) }
    var isLandscape by remember { mutableStateOf(true) }
    var resizeMode by remember { mutableStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }

    // Dialog sheets
    var showQualityDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }
    var showCastDialog by remember { mutableStateOf(false) }
    var showChannelPickerSheet by remember { mutableStateOf(false) }
    var activePickingSlot by remember { mutableIntStateOf(1) } // 0 = main, 1 = slot2, 2 = slot3
    var autoRetryCount by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    var availableQualityOptions by remember {
        mutableStateOf(
            listOf(
                QualityOption("تلقائي (الأفضل متكيف)", "Auto Adaptive", Int.MAX_VALUE, Int.MAX_VALUE),
                QualityOption("1080p FHD", "Full HD 60fps", 1080, 8_000_000),
                QualityOption("720p HD", "High Definition", 720, 4_000_000),
                QualityOption("480p SD", "Standard Def", 480, 1_500_000),
                QualityOption("360p توفير البيانات", "Data Saver", 360, 800_000)
            )
        )
    }
    var selectedQualityIndex by remember { mutableIntStateOf(0) }

    var availableAudioTracks by remember { mutableStateOf<List<AudioTrackOption>>(emptyList()) }
    var selectedAudioTrackIndex by remember { mutableIntStateOf(0) }

    var availableSubtitleTracks by remember { mutableStateOf<List<SubtitleTrackOption>>(emptyList()) }
    var selectedSubtitleIndex by remember { mutableIntStateOf(0) }

    // Multi-View state (3 simultaneous independent channels)
    var isMultiViewMode by remember { mutableStateOf(false) }
    var activeAudioSlot by remember { mutableIntStateOf(0) } // 0 = main, 1 = slot2, 2 = slot3
    var slot0Title by remember(title) { mutableStateOf(title) }
    var slot1Channel by remember { mutableStateOf<ChannelItem?>(null) }
    var slot2Channel by remember { mutableStateOf<ChannelItem?>(null) }

    // Anti-VPN 3-Second Security Scanner state
    var isVpnDetectedInPlayer by remember { mutableStateOf(false) }

    // High performance SINGLE ExoPlayer configuration with robust live buffering to prevent dropouts
    val exoPlayer = remember {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                12000,  // Min buffer 12s: prevents sudden playback interruption on weak/fluctuating networks
                45000,  // Max buffer 45s: solid buffer ceiling
                1500,   // Buffer for initial playback 1.5s
                2500    // Buffer for resume after rebuffer 2.5s
            )
            .setBackBuffer(10000, false)
            .setTargetBufferBytes(20 * 1024 * 1024) // 20 MB buffer ceiling prevents high-RAM footprint
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .build().apply {
                playWhenReady = true
            }
    }

    // Function to play or switch stream cleanly using StreamUrlParser (ClearKey DRM, MPD, HLS, headers, proxy workers)
    val playStream: (String) -> Unit = remember(exoPlayer) {
        { url ->
            if (url.isNotBlank()) {
                isBuffering = true
                hasError = false
                try {
                    exoPlayer.stop()
                    exoPlayer.clearMediaItems()

                    val parsed = StreamUrlParser.parse(url)
                    val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                        .setUserAgent(parsed.userAgent ?: StreamUrlParser.DEFAULT_USER_AGENT)
                        .setAllowCrossProtocolRedirects(true)
                        .setConnectTimeoutMs(25000)
                        .setReadTimeoutMs(25000)
                    StreamUrlParser.configureHttpDataSource(httpDataSourceFactory, parsed)

                    val mediaSourceFactory = DefaultMediaSourceFactory(httpDataSourceFactory, StreamUrlParser.createExtractorsFactory())
                    val drmManager = StreamUrlParser.createDrmSessionManager(parsed)
                    if (drmManager != null) {
                        mediaSourceFactory.setDrmSessionManagerProvider { drmManager }
                    }

                    val mediaItemBuilder = MediaItem.Builder().setUri(Uri.parse(parsed.cleanUrl))
                    if (parsed.mimeType != null) {
                        mediaItemBuilder.setMimeType(parsed.mimeType)
                    }
                    val mediaSource = mediaSourceFactory.createMediaSource(mediaItemBuilder.build())
                    exoPlayer.setMediaSource(mediaSource)
                    exoPlayer.prepare()
                    exoPlayer.play()
                } catch (e: Exception) {
                    android.util.Log.e("PlayerScreen", "Error playing stream: ${e.message}", e)
                    hasError = true
                    isBuffering = false
                }
            }
        }
    }

    // Secondary sub-players for Multi-View 3-channel mode with low-latency lightweight buffering
    val subLoadControl = remember {
        DefaultLoadControl.Builder()
            .setBufferDurationsMs(2500, 10000, 800, 1200)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
    }

    val playInSubPlayer: (ExoPlayer?, String) -> Unit = remember {
        { player, url ->
            if (player != null && url.isNotBlank()) {
                try {
                    player.stop()
                    player.clearMediaItems()
                    val parsed = StreamUrlParser.parse(url)
                    val httpFactory = DefaultHttpDataSource.Factory()
                        .setUserAgent(parsed.userAgent ?: StreamUrlParser.DEFAULT_USER_AGENT)
                        .setAllowCrossProtocolRedirects(true)
                        .setConnectTimeoutMs(15000)
                        .setReadTimeoutMs(15000)
                    StreamUrlParser.configureHttpDataSource(httpFactory, parsed)
                    val msFactory = DefaultMediaSourceFactory(httpFactory, StreamUrlParser.createExtractorsFactory())
                    val drm = StreamUrlParser.createDrmSessionManager(parsed)
                    if (drm != null) msFactory.setDrmSessionManagerProvider { drm }
                    val mb = MediaItem.Builder().setUri(Uri.parse(parsed.cleanUrl))
                    if (parsed.mimeType != null) mb.setMimeType(parsed.mimeType)
                    player.setMediaSource(msFactory.createMediaSource(mb.build()))
                    player.prepare()
                    player.play()
                } catch (e: Exception) {
                    android.util.Log.e("PlayerScreen", "Error playing subplayer stream: ${e.message}")
                }
            }
        }
    }

    val subPlayer1 = remember(isMultiViewMode) {
        if (isMultiViewMode) {
            ExoPlayer.Builder(context)
                .setLoadControl(subLoadControl)
                .build().apply {
                    playWhenReady = true
                    volume = if (activeAudioSlot == 1) 1f else 0f
                }
        } else null
    }

    val subPlayer2 = remember(isMultiViewMode) {
        if (isMultiViewMode) {
            ExoPlayer.Builder(context)
                .setLoadControl(subLoadControl)
                .build().apply {
                    playWhenReady = true
                    volume = if (activeAudioSlot == 2) 1f else 0f
                }
        } else null
    }

    // Anti-VPN Periodic Security Scanner in Player (Runs safely on Dispatchers.IO)
    LaunchedEffect(exoPlayer, subPlayer1, subPlayer2) {
        withContext(Dispatchers.IO) {
            while (isActive) {
                val vpnOn = AppSecurityGuard.isVpnOrProxyActive(context)
                if (vpnOn != isVpnDetectedInPlayer) {
                    withContext(Dispatchers.Main) {
                        isVpnDetectedInPlayer = vpnOn
                        if (vpnOn) {
                            exoPlayer.pause()
                            subPlayer1?.pause()
                            subPlayer2?.pause()
                        } else {
                            exoPlayer.play()
                            if (isMultiViewMode) {
                                subPlayer1?.play()
                                subPlayer2?.play()
                            }
                        }
                    }
                }
                delay(5000L) // 5-second interval on IO thread prevents CPU load
            }
        }
    }

    // Default channels initialization when entering Multi-View mode
    LaunchedEffect(isMultiViewMode, availableChannels) {
        if (isMultiViewMode) {
            if (slot1Channel == null && availableChannels.isNotEmpty()) {
                val candidate1 = availableChannels.firstOrNull { it.name != slot0Title && it.streamUrl != currentActiveUrl }
                    ?: availableChannels.firstOrNull()
                slot1Channel = candidate1
            }
            if (slot2Channel == null && availableChannels.isNotEmpty()) {
                val candidate2 = availableChannels.firstOrNull { 
                    it.name != slot0Title && it.streamUrl != currentActiveUrl && it.id != slot1Channel?.id 
                } ?: availableChannels.getOrNull(1) ?: availableChannels.firstOrNull()
                slot2Channel = candidate2
            }
        }
    }

    // Reactive playback for slot 1 channel
    LaunchedEffect(subPlayer1, slot1Channel) {
        if (subPlayer1 != null) {
            val url = slot1Channel?.streamUrl ?: serverOptions.getOrNull(1)?.second ?: currentActiveUrl
            playInSubPlayer(subPlayer1, url)
        }
    }

    // Reactive playback for slot 2 channel
    LaunchedEffect(subPlayer2, slot2Channel) {
        if (subPlayer2 != null) {
            val url = slot2Channel?.streamUrl ?: serverOptions.getOrNull(2)?.second ?: serverOptions.getOrNull(0)?.second ?: currentActiveUrl
            playInSubPlayer(subPlayer2, url)
        }
    }

    // Cleanup subplayers when exiting multi-view
    DisposableEffect(isMultiViewMode) {
        onDispose {
            subPlayer1?.release()
            subPlayer2?.release()
        }
    }

    // Audio routing between the 3 channels
    LaunchedEffect(isMultiViewMode, activeAudioSlot) {
        if (isMultiViewMode) {
            exoPlayer.volume = if (activeAudioSlot == 0) 1f else 0f
            subPlayer1?.volume = if (activeAudioSlot == 1) 1f else 0f
            subPlayer2?.volume = if (activeAudioSlot == 2) 1f else 0f
        } else {
            exoPlayer.volume = 1f
        }
    }

    // Play active url whenever it changes (e.g., server switch)
    LaunchedEffect(currentActiveUrl) {
        playStream(currentActiveUrl)
    }

    // Keep Screen On & Orientation configuration
    DisposableEffect(activity) {
        val window = activity?.window
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        isLandscape = true

        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = state == Player.STATE_BUFFERING
                if (state == Player.STATE_READY) {
                    autoRetryCount = 0
                    duration = exoPlayer.duration.coerceAtLeast(0L)
                    hasError = false
                    if (!isLive && initialProgressMs > 3000L && !hasResumedInitialProgress) {
                        hasResumedInitialProgress = true
                        val validSeek = initialProgressMs.coerceAtMost((exoPlayer.duration - 2000L).coerceAtLeast(0L))
                        if (validSeek > 0L) {
                            exoPlayer.seekTo(validSeek)
                            Toast.makeText(context, "تم استئناف المشاهدة من حيث توقفت", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                android.util.Log.w("PlayerScreen", "Player encountered error (Code: ${error.errorCode}, Msg: ${error.message})")

                // 1. Recover Behind Live Window Exception automatically (common in HLS live streams)
                if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                    exoPlayer.seekToDefaultPosition()
                    exoPlayer.prepare()
                    exoPlayer.play()
                    return
                }

                // 2. Automatic Reconnection & Self-Healing before reporting error
                if (autoRetryCount < 3) {
                    autoRetryCount++
                    isBuffering = true
                    hasError = false
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(1200L)
                        try {
                            if (isLive) {
                                exoPlayer.seekToDefaultPosition()
                            }
                            exoPlayer.prepare()
                            exoPlayer.play()
                        } catch (e: Exception) {
                            android.util.Log.e("PlayerScreen", "Auto reconnect attempt error: ${e.message}")
                        }
                    }
                } else if (selectedServerIndex < serverOptions.size - 1) {
                    // 3. Automatic Failover to backup server if available
                    autoRetryCount = 0
                    val nextIdx = selectedServerIndex + 1
                    val nextServer = serverOptions[nextIdx]
                    selectedServerIndex = nextIdx
                    currentActiveUrl = nextServer.second
                    hasError = false
                    isBuffering = true
                    Toast.makeText(
                        context,
                        "جاري التبديل التلقائي إلى السيرفر البديل: ${nextServer.first}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // 4. Fatal error only after all retries & servers fail
                    hasError = true
                    isBuffering = false
                    Toast.makeText(
                        context,
                        "تعذر استعادة البث، اضغط على زر إعادة المحاولة أو اختر سيرفر آخر",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                // 1. Dynamic Video Quality Extraction
                val dynamicQualities = mutableListOf<QualityOption>()
                dynamicQualities.add(
                    QualityOption("تلقائي (الأفضل متكيف)", "Auto Adaptive (تلقائي حسب سرعة النت)", Int.MAX_VALUE, Int.MAX_VALUE)
                )

                for (groupIndex in 0 until tracks.groups.size) {
                    val group = tracks.groups[groupIndex]
                    if (group.type == C.TRACK_TYPE_VIDEO) {
                        for (trackIndex in 0 until group.length) {
                            val format = group.getTrackFormat(trackIndex)
                            val h = format.height
                            val w = format.width
                            val bitrate = format.bitrate
                            if (h > 0) {
                                val label = when {
                                    h >= 2160 -> "4K UHD ($w x $h)"
                                    h >= 1080 -> "1080p FHD ($w x $h)"
                                    h >= 720 -> "720p HD ($w x $h)"
                                    h >= 480 -> "480p SD ($w x $h)"
                                    else -> "${h}p ($w x $h)"
                                }
                                val brLabel = if (bitrate > 0) "${bitrate / 1000} kbps" else "معدل بت أصلي"
                                if (dynamicQualities.none { it.maxHeight == h }) {
                                    dynamicQualities.add(
                                        QualityOption(label, brLabel, h, if (bitrate > 0) bitrate else (h * 5000))
                                    )
                                }
                            }
                        }
                    }
                }
                if (dynamicQualities.size > 1) {
                    availableQualityOptions = dynamicQualities.sortedByDescending { it.maxHeight }
                }

                // 2. Audio & Subtitles Tracks
                val audioList = mutableListOf<AudioTrackOption>()
                audioList.add(AudioTrackOption("default", "الصوت الافتراضي (تلقائي)", "ar", -1, -1))
                
                val subList = mutableListOf<SubtitleTrackOption>()
                subList.add(SubtitleTrackOption("off", "إيقاف الترجمة (Off)", "none", -1, -1))
                subList.add(SubtitleTrackOption("ar", "العربية (Arabic)", "ar", -1, -1))
                subList.add(SubtitleTrackOption("en", "الإنجليزية (English)", "en", -1, -1))
                subList.add(SubtitleTrackOption("fr", "الفرنسية (Français)", "fr", -1, -1))
                subList.add(SubtitleTrackOption("es", "الإسبانية (Español)", "es", -1, -1))

                var trackCounter = 1
                for (groupIndex in 0 until tracks.groups.size) {
                    val group = tracks.groups[groupIndex]
                    if (group.type == C.TRACK_TYPE_AUDIO) {
                        for (trackIndex in 0 until group.length) {
                            val format = group.getTrackFormat(trackIndex)
                            val lang = format.language ?: "und"
                            val langName = when (lang.lowercase()) {
                                "ar", "ara", "arabic" -> "العربية"
                                "en", "eng", "english" -> "الإنجليزية (English)"
                                "fr", "fra", "french" -> "الفرنسية (Français)"
                                "es", "spa", "spanish" -> "الإسبانية (Español)"
                                else -> "المسار $trackCounter ($lang)"
                            }
                            audioList.add(
                                AudioTrackOption(
                                    id = "track_${groupIndex}_$trackIndex",
                                    label = langName,
                                    language = lang,
                                    trackGroupIndex = groupIndex,
                                    trackIndex = trackIndex
                                )
                            )
                            trackCounter++
                        }
                    } else if (group.type == C.TRACK_TYPE_TEXT) {
                        for (trackIndex in 0 until group.length) {
                            val format = group.getTrackFormat(trackIndex)
                            val lang = format.language ?: "und"
                            val subLabel = format.label ?: when (lang.lowercase()) {
                                "ar", "ara" -> "ترجمة عربية مدمجة"
                                "en", "eng" -> "English Subtitles"
                                else -> "ترجمة $lang"
                            }
                            subList.add(
                                SubtitleTrackOption(
                                    id = "sub_${groupIndex}_$trackIndex",
                                    label = subLabel,
                                    language = lang,
                                    trackGroupIndex = groupIndex,
                                    trackIndex = trackIndex
                                )
                            )
                        }
                    }
                }
                availableAudioTracks = audioList
                availableSubtitleTracks = subList
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            if (!isLive && exoPlayer.currentPosition > 0L) {
                onProgressUpdate(exoPlayer.currentPosition, exoPlayer.duration.coerceAtLeast(0L))
            }
            exoPlayer.removeListener(listener)
            exoPlayer.release()
            activity?.requestedOrientation = originalOrientation
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.navigationBars())
            }
        }
    }

    // Position tracker loop - optimized: only run if duration > 0 and not live TV stream, preventing periodic UI frame drops
    LaunchedEffect(isPlaying, duration, isLive) {
        if (!isLive && duration > 0) {
            while (isPlaying) {
                currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
                if (currentPosition > 0L) {
                    onProgressUpdate(currentPosition, duration)
                }
                delay(1000)
            }
        }
    }

    // Auto-hide controls timer
    LaunchedEffect(areControlsVisible, isPlaying, isControlsLocked, showChannelPickerSheet) {
        if (areControlsVisible && isPlaying && !isControlsLocked && !showQualityDialog && !showAudioDialog && !showSubtitleDialog && !showServerDialog && !showChannelPickerSheet) {
            delay(4500)
            areControlsVisible = false
        }
    }

    val toggleScreenOrientation = {
        if (isLandscape) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            isLandscape = false
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            isLandscape = true
        }
    }

    val enterPiPMode: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                activity?.enterPictureInPictureMode(params)
            } catch (e: Exception) {
                Toast.makeText(context, "تعذر تفعيل ميزة صورة في صورة على هذا الجهاز", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "خاصية صورة في صورة غير مدعومة على إصدار أندرويد هذا", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (showQualityDialog || showAudioDialog || showSubtitleDialog || showServerDialog) {
                    showQualityDialog = false
                    showAudioDialog = false
                    showSubtitleDialog = false
                    showServerDialog = false
                } else if (!isControlsLocked) {
                    areControlsVisible = !areControlsVisible
                }
            }
    ) {
        // Player Surface View with Multi-View 3-channel support
        if (isMultiViewMode) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Primary Channel (Slot 0)
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxSize()
                        .border(
                            width = if (activeAudioSlot == 0) 2.5.dp else 1.dp,
                            color = if (activeAudioSlot == 0) SaribCyanAccent else Color(0x55FFFFFF)
                        )
                        .clickable { activeAudioSlot = 0 }
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = false
                                this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                keepScreenOn = true
                            }
                        },
                        update = { pv ->
                            if (pv.player != exoPlayer) pv.player = exoPlayer
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        color = Color(0xDD000000),
                        shape = RoundedCornerShape(bottomEnd = 10.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (activeAudioSlot == 0) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = null,
                                tint = if (activeAudioSlot == 0) SaribCyanAccent else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "1: $slot0Title",
                                color = if (activeAudioSlot == 0) SaribCyanAccent else Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                modifier = Modifier.widthIn(max = 140.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Change channel button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SaribElectricBlue.copy(alpha = 0.6f))
                                    .clickable {
                                        activePickingSlot = 0
                                        showChannelPickerSheet = true
                                    }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Tv, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("تغيير القناة", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold))
                                }
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    isMultiViewMode = false
                                    activeAudioSlot = 0
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Fullscreen, contentDescription = "ملء الشاشة", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Sub-Channels Column (Slot 1 & Slot 2)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    // Channel 2 (Slot 1)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .border(
                                width = if (activeAudioSlot == 1) 2.5.dp else 1.dp,
                                color = if (activeAudioSlot == 1) SaribCyanAccent else Color(0x55FFFFFF)
                            )
                            .clickable { activeAudioSlot = 1 }
                    ) {
                        if (subPlayer1 != null) {
                            AndroidView(
                                factory = { ctx ->
                                    PlayerView(ctx).apply {
                                        player = subPlayer1
                                        useController = false
                                        this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                        keepScreenOn = true
                                    }
                                },
                                update = { pv ->
                                    if (pv.player != subPlayer1) pv.player = subPlayer1
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Surface(
                            color = Color(0xDD000000),
                            shape = RoundedCornerShape(bottomEnd = 10.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (activeAudioSlot == 1) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    contentDescription = null,
                                    tint = if (activeAudioSlot == 1) SaribCyanAccent else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "2: ${slot1Channel?.name ?: "قناة 2"}",
                                    color = if (activeAudioSlot == 1) SaribCyanAccent else Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    modifier = Modifier.widthIn(max = 120.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SaribElectricBlue.copy(alpha = 0.6f))
                                        .clickable {
                                            activePickingSlot = 1
                                            showChannelPickerSheet = true
                                        }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Tv, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("تغيير القناة", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold))
                                    }
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        val currentSlot0Url = currentActiveUrl
                                        val currentSlot0Name = slot0Title
                                        val newMain = slot1Channel
                                        if (newMain != null) {
                                            slot0Title = newMain.name
                                            currentActiveUrl = newMain.streamUrl
                                            val oldMainChannel = availableChannels.find { it.streamUrl == currentSlot0Url }
                                                ?: ChannelItem(id = "slot0_prev", name = currentSlot0Name, categoryId = "", categoryName = "", streamUrl = currentSlot0Url)
                                            slot1Channel = oldMainChannel
                                            activeAudioSlot = 0
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = "تبديل للرئيسية", tint = SaribCyanAccent, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // Channel 3 (Slot 2)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .border(
                                width = if (activeAudioSlot == 2) 2.5.dp else 1.dp,
                                color = if (activeAudioSlot == 2) SaribCyanAccent else Color(0x55FFFFFF)
                            )
                            .clickable { activeAudioSlot = 2 }
                    ) {
                        if (subPlayer2 != null) {
                            AndroidView(
                                factory = { ctx ->
                                    PlayerView(ctx).apply {
                                        player = subPlayer2
                                        useController = false
                                        this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                        keepScreenOn = true
                                    }
                                },
                                update = { pv ->
                                    if (pv.player != subPlayer2) pv.player = subPlayer2
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Surface(
                            color = Color(0xDD000000),
                            shape = RoundedCornerShape(bottomEnd = 10.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (activeAudioSlot == 2) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    contentDescription = null,
                                    tint = if (activeAudioSlot == 2) SaribCyanAccent else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "3: ${slot2Channel?.name ?: "قناة 3"}",
                                    color = if (activeAudioSlot == 2) SaribCyanAccent else Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    modifier = Modifier.widthIn(max = 120.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SaribElectricBlue.copy(alpha = 0.6f))
                                        .clickable {
                                            activePickingSlot = 2
                                            showChannelPickerSheet = true
                                        }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Tv, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("تغيير القناة", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold))
                                    }
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        val currentSlot0Url = currentActiveUrl
                                        val currentSlot0Name = slot0Title
                                        val newMain = slot2Channel
                                        if (newMain != null) {
                                            slot0Title = newMain.name
                                            currentActiveUrl = newMain.streamUrl
                                            val oldMainChannel = availableChannels.find { it.streamUrl == currentSlot0Url }
                                                ?: ChannelItem(id = "slot0_prev", name = currentSlot0Name, categoryId = "", categoryName = "", streamUrl = currentSlot0Url)
                                            slot2Channel = oldMainChannel
                                            activeAudioSlot = 0
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = "تبديل للرئيسية", tint = SaribCyanAccent, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        this.resizeMode = resizeMode
                        keepScreenOn = true
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { playerView ->
                    playerView.resizeMode = resizeMode
                    playerView.keepScreenOn = true
                    if (playerView.player != exoPlayer) {
                        playerView.player = exoPlayer
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Modern Buffering Indicator
        if (isBuffering && !hasError) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                SaribLoadingIndicator(
                    size = 56.dp,
                    label = "جاري التحميل الفائق..."
                )
            }
        }

        // Error message overlay
        if (hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "تعذر تشغيل هذا البث، جاري إعادة المحاولة بالسيرفر البديل...",
                        style = MaterialTheme.typography.titleMedium.copy(color = SaribLiveRed)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    IconButton(
                        onClick = {
                            hasError = false
                            exoPlayer.prepare()
                            exoPlayer.play()
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SaribElectricBlue)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "إعادة المحاولة",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // FLOATING UNLOCK BUTTON
        if (isControlsLocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = {
                        isControlsLocked = false
                        areControlsVisible = true
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(SaribCyanAccent)
                        .testTag("unlock_screen_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "إلغاء قفل الشاشة",
                        tint = Color.Black,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // Controls Overlay
        AnimatedVisibility(
            visible = areControlsVisible && !isControlsLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xEE040810),
                                Color(0x20000000),
                                Color(0xF5040810)
                            )
                        )
                    )
                    .padding(14.dp)
            ) {
                // ================= TOP BAR (matching image & TV Cast Action) =================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(top = 10.dp, start = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left side: Back button + Title + Live badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .testTag("player_back_button")
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Red LIVE pill badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF5A1414))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFF3B30))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isLive) "LIVE" else "VOD",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFFF5252),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Right side: TV Cast Button, Multi-View, PiP, Lock
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Cast To TV Button (زر بث ومشاركة الشاشة على التلفاز)
                        Surface(
                            color = SaribElectricBlue.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SaribCyanAccent.copy(alpha = 0.8f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    showCastDialog = true
                                    showQualityDialog = false
                                    showAudioDialog = false
                                    showSubtitleDialog = false
                                    showServerDialog = false
                                }
                                .testTag("tv_cast_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tv,
                                    contentDescription = "بث على التلفاز",
                                    tint = SaribCyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "بث للشاشة",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        // 2. Multi-View Button (if channels exist)
                        if (availableChannels.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    isMultiViewMode = !isMultiViewMode
                                    if (isMultiViewMode) activeAudioSlot = 0
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isMultiViewMode) SaribCyanAccent.copy(alpha = 0.35f) else Color(0x55000000))
                                    .testTag("multi_view_toggle_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dashboard,
                                    contentDescription = "تعدد القنوات",
                                    tint = if (isMultiViewMode) SaribCyanAccent else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // 3. PiP Button
                        IconButton(
                            onClick = enterPiPMode,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x55000000))
                                .testTag("pip_mode_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureInPictureAlt,
                                contentDescription = "صورة في صورة",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 4. Lock Screen Button
                        IconButton(
                            onClick = {
                                isControlsLocked = true
                                areControlsVisible = false
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x55000000))
                                .testTag("lock_screen_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "قفل الشاشة",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // ================= CENTER CONTROLS (Rewind, Play/Pause, Forward) =================
                Box(
                    modifier = Modifier.align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasError) {
                        Surface(
                            color = Color(0xDD120406),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SaribLiveRed.copy(alpha = 0.6f)),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = SaribLiveRed,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "تعذر تشغيل البث من هذا المصدر",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "يرجى المحاولة مجدداً أو تجربة سيرفر آخر",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SaribTextSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            hasError = false
                                            playStream(currentActiveUrl)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SaribCyanAccent),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("إعادة المحاولة", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                    if (serverOptions.size > 1) {
                                        OutlinedButton(
                                            onClick = {
                                                val nextIdx = (selectedServerIndex + 1) % serverOptions.size
                                                selectedServerIndex = nextIdx
                                                currentActiveUrl = serverOptions[nextIdx].second
                                                hasError = false
                                                playStream(currentActiveUrl)
                                                Toast.makeText(context, "تم التحويل إلى: ${serverOptions[nextIdx].first}", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, SaribCyanAccent)
                                        ) {
                                            Text("السيرفر التالي", color = SaribCyanAccent, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    } else if (isBuffering) {
                        Surface(
                            color = Color(0xB3000000),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SaribLoadingIndicator(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "جاري تحضير البث المباشر...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    } else {
                        // Rewind << | Main White Circle Play/Pause | Forward >>
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(48.dp)
                        ) {
                            // Fast Rewind <<
                            IconButton(
                                onClick = {
                                    val target = (exoPlayer.currentPosition - 10000).coerceAtLeast(0L)
                                    exoPlayer.seekTo(target)
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FastRewind,
                                    contentDescription = "تقديم 10 ثوان",
                                    tint = Color(0x88FFFFFF),
                                    modifier = Modifier.size(38.dp)
                                )
                            }

                            // Large Pure White Circular Play/Pause Button
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable {
                                        if (isPlaying) {
                                            exoPlayer.pause()
                                        } else {
                                            exoPlayer.play()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                                    tint = Color.Black,
                                    modifier = Modifier.size(38.dp)
                                )
                            }

                            // Fast Forward >>
                            IconButton(
                                onClick = {
                                    val target = (exoPlayer.currentPosition + 10000).coerceAtMost(if (duration > 0) duration else Long.MAX_VALUE)
                                    exoPlayer.seekTo(target)
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FastForward,
                                    contentDescription = "تأخير 10 ثوان",
                                    tint = Color(0x88FFFFFF),
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }
                    }
                }

                // ================= BOTTOM AREA: TIME SLIDER & 6 ACTION ICONS =================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                ) {
                    // 1. Time Slider Bar (00:00 ------- 00:00)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDuration(currentPosition),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )

                        Slider(
                            value = if (duration > 0) (currentPosition.toFloat() / duration).coerceIn(0f, 1f) else 0f,
                            onValueChange = { frac ->
                                if (duration > 0) {
                                    val newPos = (frac * duration).toLong()
                                    exoPlayer.seekTo(newPos)
                                    currentPosition = newPos
                                }
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color.Transparent,
                                activeTrackColor = Color(0xFF8E8E93),
                                inactiveTrackColor = Color(0xFF38383A)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                        )

                        Text(
                            text = if (duration > 0) formatDuration(duration) else "00:00",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Centered 6 Action Icons (matching design screenshot)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(36.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Subtitles / Language (文A)
                        IconButton(
                            onClick = {
                                showSubtitleDialog = true
                                showServerDialog = false
                                showQualityDialog = false
                                showAudioDialog = false
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text(
                                text = "文A",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 2. Microphone / Audio Track (🎤)
                        IconButton(
                            onClick = {
                                showAudioDialog = true
                                showSubtitleDialog = false
                                showServerDialog = false
                                showQualityDialog = false
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "الصوت والمعلق",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // 3. Alerts & Server Switcher (🔔)
                        IconButton(
                            onClick = {
                                showServerDialog = true
                                showSubtitleDialog = false
                                showQualityDialog = false
                                showAudioDialog = false
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "السيرفرات والتنبيهات",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // 4. Aspect Ratio / Expand (⤢)
                        IconButton(
                            onClick = {
                                resizeMode = when (resizeMode) {
                                    AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                    else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                                val modeLabel = when (resizeMode) {
                                    AspectRatioFrameLayout.RESIZE_MODE_FIT -> "تناسب (Fit)"
                                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> "تكبير (Zoom)"
                                    else -> "ملء الشاشة (Fill)"
                                }
                                Toast.makeText(context, modeLabel, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "أبعاد الشاشة",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // 5. Settings / Quality (⚙)
                        IconButton(
                            onClick = {
                                showQualityDialog = true
                                showSubtitleDialog = false
                                showServerDialog = false
                                showAudioDialog = false
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "الجودة والإعدادات",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // 6. Screen Rotate (📱 / 🔄)
                        IconButton(
                            onClick = toggleScreenOrientation,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenRotation,
                                contentDescription = "تدوير الشاشة",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // SUBTITLES / TRANSLATION MODAL
        AnimatedVisibility(
            visible = showSubtitleDialog,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .width(340.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SaribDarkCard)
                    .border(1.5.dp, SaribCyanAccent.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ClosedCaption, contentDescription = null, tint = SaribCyanAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "الترجمة والنصوص (Subtitles)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        IconButton(
                            onClick = { showSubtitleDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = SaribTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    availableSubtitleTracks.forEachIndexed { index, subOption ->
                        val isSelected = selectedSubtitleIndex == index
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SaribElectricBlue.copy(alpha = 0.35f) else Color(0x33000000))
                                .border(1.dp, if (isSelected) SaribCyanAccent else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedSubtitleIndex = index
                                    if (subOption.id == "off") {
                                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                            .buildUpon()
                                            .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                                            .build()
                                        Toast.makeText(context, "تم إيقاف الترجمة", Toast.LENGTH_SHORT).show()
                                    } else {
                                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                            .buildUpon()
                                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                            .setPreferredTextLanguage(subOption.language)
                                            .build()
                                        Toast.makeText(context, "تم اختيار الترجمة: ${subOption.label}", Toast.LENGTH_SHORT).show()
                                    }
                                    showSubtitleDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = subOption.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isSelected) SaribCyanAccent else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // SERVER SWITCHER MODAL (5 SERVERS)
        AnimatedVisibility(
            visible = showServerDialog,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .width(360.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SaribDarkCard)
                    .border(1.5.dp, SaribCyanAccent.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Dns, contentDescription = null, tint = SaribCyanAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مبدل السيرفرات السحابية",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        IconButton(
                            onClick = { showServerDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = SaribTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    serverOptions.forEachIndexed { index, (srvName, _) ->
                        val isSelected = selectedServerIndex == index
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SaribElectricBlue.copy(alpha = 0.35f) else Color(0x33000000))
                                .border(1.dp, if (isSelected) SaribCyanAccent else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedServerIndex = index
                                    val newUrl = serverOptions.getOrNull(index)?.second ?: streamUrl
                                    currentActiveUrl = newUrl
                                    playStream(newUrl)
                                    Toast.makeText(context, "تم التبديل إلى $srvName", Toast.LENGTH_SHORT).show()
                                    showServerDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = srvName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isSelected) SaribCyanAccent else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // QUALITY SELECTION MODAL
        AnimatedVisibility(
            visible = showQualityDialog,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .width(340.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SaribDarkCard)
                    .border(1.dp, SaribCyanAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "اختيار جودة البث",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        IconButton(
                            onClick = { showQualityDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = SaribTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    availableQualityOptions.forEachIndexed { index, option ->
                        val isSelected = selectedQualityIndex == index
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SaribElectricBlue.copy(alpha = 0.3f) else Color(0x33000000))
                                .border(
                                    1.dp,
                                    if (isSelected) SaribCyanAccent else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedQualityIndex = index
                                    if (option.maxHeight == Int.MAX_VALUE) {
                                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                            .buildUpon()
                                            .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                                            .setMaxVideoSize(Int.MAX_VALUE, Int.MAX_VALUE)
                                            .setMaxVideoBitrate(Int.MAX_VALUE)
                                            .build()
                                    } else {
                                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                            .buildUpon()
                                            .setMaxVideoSize(option.maxHeight * 2, option.maxHeight)
                                            .setMaxVideoBitrate(option.maxBitrate)
                                            .build()
                                    }
                                    Toast.makeText(context, "تم ضبط الجودة: ${option.label}", Toast.LENGTH_SHORT).show()
                                    showQualityDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (isSelected) SaribCyanAccent else Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                    Text(
                                        text = option.resolutionLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(color = SaribTextMuted)
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "محدد",
                                        tint = SaribCyanAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // AUDIO TRACK SELECTION MODAL
        AnimatedVisibility(
            visible = showAudioDialog,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .width(340.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SaribDarkCard)
                    .border(1.dp, SaribCyanAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مسار الصوت واللغة",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        IconButton(
                            onClick = { showAudioDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = SaribTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (availableAudioTracks.isEmpty()) {
                        Text(
                            text = "المسار الافتراضي هو المتاح للبث المباشر الحالي",
                            style = MaterialTheme.typography.bodySmall.copy(color = SaribTextMuted),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        availableAudioTracks.forEachIndexed { index, track ->
                            val isSelected = selectedAudioTrackIndex == index
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SaribElectricBlue.copy(alpha = 0.3f) else Color(0x33000000))
                                    .border(
                                        1.dp,
                                        if (isSelected) SaribCyanAccent else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedAudioTrackIndex = index
                                        if (track.trackGroupIndex >= 0) {
                                            val tracks = exoPlayer.currentTracks
                                            if (track.trackGroupIndex < tracks.groups.size) {
                                                val group = tracks.groups[track.trackGroupIndex]
                                                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                                    .buildUpon()
                                                    .setOverrideForType(
                                                        TrackSelectionOverride(group.mediaTrackGroup, track.trackIndex)
                                                    )
                                                    .build()
                                            }
                                        } else {
                                            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                                .buildUpon()
                                                .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
                                                .build()
                                        }
                                        Toast.makeText(context, "تم تحديد الصوت: ${track.label}", Toast.LENGTH_SHORT).show()
                                        showAudioDialog = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = track.label,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (isSelected) SaribCyanAccent else Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "محدد",
                                            tint = SaribCyanAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // MULTI-VIEW CHANNEL SELECTOR MODAL (اختيار القنوات للعرض المتعدد)
        AnimatedVisibility(
            visible = showChannelPickerSheet,
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit = slideOutVertically { it / 2 } + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            var channelSearchQuery by remember { mutableStateOf("") }
            var selectedCategoryFilter by remember { mutableStateOf("الكل") }

            val categoriesList = remember(availableChannels) {
                listOf("الكل") + availableChannels.map { it.categoryName }.filter { it.isNotBlank() }.distinct()
            }

            val filteredChannels = remember(channelSearchQuery, selectedCategoryFilter, availableChannels) {
                availableChannels.filter { ch ->
                    val matchesQuery = channelSearchQuery.isBlank() ||
                        ch.name.contains(channelSearchQuery, ignoreCase = true) ||
                        ch.categoryName.contains(channelSearchQuery, ignoreCase = true)
                    val matchesCategory = selectedCategoryFilter == "الكل" || ch.categoryName == selectedCategoryFilter
                    matchesQuery && matchesCategory
                }
            }

            val slotName = when (activePickingSlot) {
                0 -> "الشاشة 1 (الرئيسية)"
                1 -> "الشاشة 2"
                else -> "الشاشة 3"
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 440.dp)
                    .fillMaxWidth(0.88f)
                    .heightIn(max = 460.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SaribDarkCard)
                    .border(1.5.dp, SaribCyanAccent.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = SaribCyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "اختيار قناة لـ $slotName",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "اختر أي قناة لتشغيلها فوراً في هذه الشاشة",
                                    style = MaterialTheme.typography.bodySmall.copy(color = SaribTextSecondary)
                                )
                            }
                        }
                        IconButton(
                            onClick = { showChannelPickerSheet = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SaribTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Field
                    OutlinedTextField(
                        value = channelSearchQuery,
                        onValueChange = { channelSearchQuery = it },
                        placeholder = {
                            Text("بحث عن قناة بالاسم...", color = SaribTextMuted, style = MaterialTheme.typography.bodySmall)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (channelSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { channelSearchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = SaribTextMuted)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaribCyanAccent,
                            unfocusedBorderColor = Color(0x44FFFFFF),
                            focusedContainerColor = Color(0x33000000),
                            unfocusedContainerColor = Color(0x33000000),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category Chips Filter
                    if (categoriesList.size > 2) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            items(categoriesList) { cat ->
                                val isSelected = selectedCategoryFilter == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) SaribCyanAccent else Color(0x22FFFFFF))
                                        .clickable { selectedCategoryFilter = cat }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) Color.Black else Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Channels List
                    if (filteredChannels.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (availableChannels.isEmpty()) "لا توجد قنوات مسجلة حالياً" else "لا توجد قنوات تطابق البحث",
                                color = SaribTextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            items(filteredChannels, key = { it.id }) { channel ->
                                val isCurrentInThisSlot = when (activePickingSlot) {
                                    0 -> slot0Title == channel.name
                                    1 -> slot1Channel?.id == channel.id
                                    else -> slot2Channel?.id == channel.id
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isCurrentInThisSlot) SaribElectricBlue.copy(alpha = 0.35f) else Color(0x22000000))
                                        .border(
                                            width = 1.dp,
                                            color = if (isCurrentInThisSlot) SaribCyanAccent else Color(0x22FFFFFF),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            when (activePickingSlot) {
                                                0 -> {
                                                    slot0Title = channel.name
                                                    currentActiveUrl = channel.streamUrl
                                                    playStream(channel.streamUrl)
                                                }
                                                1 -> {
                                                    slot1Channel = channel
                                                    playInSubPlayer(subPlayer1, channel.streamUrl)
                                                }
                                                2 -> {
                                                    slot2Channel = channel
                                                    playInSubPlayer(subPlayer2, channel.streamUrl)
                                                }
                                            }
                                            showChannelPickerSheet = false
                                            Toast.makeText(context, "تم تشغيل ${channel.name} في $slotName", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF09111E)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (channel.logoUrl.isNotBlank()) {
                                                AsyncImage(
                                                    model = channel.logoUrl,
                                                    contentDescription = channel.name,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(3.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Tv,
                                                    contentDescription = null,
                                                    tint = SaribCyanAccent,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = channel.name,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${channel.categoryName.ifBlank { "عام" }} • ${channel.country}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = SaribTextSecondary),
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isCurrentInThisSlot) SaribCyanAccent else SaribElectricBlue.copy(alpha = 0.5f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (isCurrentInThisSlot) "تعمل الآن" else "تشغيل",
                                            color = if (isCurrentInThisSlot) Color.Black else Color.White,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // TV SCREEN CAST / WIRELESS DISPLAY MODAL (STRICTLY WIRELESS DISPLAY - NO LINK EXPOSURE)
        AnimatedVisibility(
            visible = showCastDialog,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth(0.90f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(SaribDarkCard)
                    .border(1.5.dp, SaribCyanAccent.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SaribCyanAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tv,
                                    contentDescription = null,
                                    tint = SaribCyanAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "بث الشاشة اللاسلكي للتلفاز",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = "Wireless Display & Smart View",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SaribCyanAccent,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                        IconButton(
                            onClick = { showCastDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = SaribTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Wi-Fi Connection Tip Banner
                    Surface(
                        color = Color(0x2200D4FF),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SaribCyanAccent.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = SaribCyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "تأكد من اتصال الهاتف وشاشة التلفاز بنفس شبكة الـ Wi-Fi المنزلية",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 17.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Connect Button
                    Button(
                        onClick = {
                            showCastDialog = false
                            val castIntents = listOf(
                                Intent("android.settings.CAST_SETTINGS"),
                                Intent("android.settings.WIFI_DISPLAY_SETTINGS"),
                                Intent(Settings.ACTION_CAST_SETTINGS),
                                Intent(Settings.ACTION_WIRELESS_SETTINGS)
                            )
                            var launched = false
                            for (intent in castIntents) {
                                try {
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                    launched = true
                                    break
                                } catch (_: Exception) {}
                            }
                            if (!launched) {
                                try {
                                    val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(fallback)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "تعذر فتح إعدادات البث اللاسلكي في جهازك", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SaribCyanAccent,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("launch_smart_view_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "اتصال وبث للشاشة الآن (Smart View)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "يدعم شاشات Samsung Smart View, LG, Android TV, Roku وغيرها لاسلكياً وبأعلى حماية للمحتوى",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SaribTextMuted,
                            fontSize = 10.5.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }

        // Anti-VPN Fullscreen Blocker inside Player (Checked every 3 seconds)
        if (isVpnDetectedInPlayer) {
            VpnBlockedDialog(
                onRecheckClick = {
                    val stillActive = AppSecurityGuard.isVpnOrProxyActive(context)
                    isVpnDetectedInPlayer = stillActive
                    if (!stillActive) {
                        exoPlayer.play()
                        if (isMultiViewMode) {
                            subPlayer1?.play()
                            subPlayer2?.play()
                        }
                    }
                },
                onExitApp = {
                    onBackClick()
                }
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hours)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes)
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

@Composable
private fun PlayerBottomActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subLabel: String = "",
    isActive: Boolean = false,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) SaribElectricBlue.copy(alpha = 0.35f) else Color(0x40000000),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isActive) SaribCyanAccent else Color(0x22FFFFFF)
        ),
        modifier = modifier
            .testTag(testTag)
            .height(52.dp)
            .widthIn(min = 68.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) SaribCyanAccent else Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (subLabel.isNotBlank()) subLabel else label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isActive) SaribCyanAccent else Color(0xCCFFFFFF)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

