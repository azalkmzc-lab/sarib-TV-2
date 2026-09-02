package com.example.ui.screens

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
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
import com.example.data.local.tr
import com.example.ui.components.SaribLoadingIndicator
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribDarkBackground
import com.example.ui.theme.SaribDarkCard
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribLiveRed
import com.example.ui.theme.SaribTextMuted
import com.example.ui.theme.SaribTextPrimary
import com.example.ui.theme.SaribTextSecondary
import kotlinx.coroutines.delay
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
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var areControlsVisible by remember { mutableStateOf(true) }
    var isControlsLocked by remember { mutableStateOf(false) }
    var isLandscape by remember { mutableStateOf(true) }
    var resizeMode by remember { mutableStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }

    // Dialog sheets
    var showQualityDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }

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

    // High performance ExoPlayer configuration
    val exoPlayer = remember(currentActiveUrl) {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                2000,
                15000,
                1000,
                1500
            )
            .build()

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 SARIB-TV-Player/1.0")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)

        val mediaItemBuilder = MediaItem.Builder().setUri(Uri.parse(currentActiveUrl))
        if (currentActiveUrl.contains(".mpd", ignoreCase = true) || currentActiveUrl.contains("dash", ignoreCase = true)) {
            mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_MPD)
        } else if (currentActiveUrl.contains(".m3u8", ignoreCase = true) || currentActiveUrl.contains("hls", ignoreCase = true)) {
            mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
        }

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .build().apply {
                playWhenReady = true
                setMediaItem(mediaItemBuilder.build())
                prepare()
            }
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
                    duration = exoPlayer.duration.coerceAtLeast(0L)
                    hasError = false
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                // Automatic failover to next available server if available
                if (selectedServerIndex < serverOptions.size - 1) {
                    val nextIdx = selectedServerIndex + 1
                    val nextServer = serverOptions[nextIdx]
                    selectedServerIndex = nextIdx
                    currentActiveUrl = nextServer.second
                    hasError = false
                    isBuffering = true
                    Toast.makeText(
                        context,
                        "تعذر تشغيل السيرفر الحالي. جاري الانتقال التلقائي إلى: ${nextServer.first}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    hasError = true
                    isBuffering = false
                    Toast.makeText(
                        context,
                        "تعذر تشغيل البث من كافة السيرفرات المتاحة",
                        Toast.LENGTH_LONG
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
            exoPlayer.removeListener(listener)
            exoPlayer.release()
            activity?.requestedOrientation = originalOrientation
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Position tracker loop
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
            delay(1000)
        }
    }

    // Auto-hide controls timer
    LaunchedEffect(areControlsVisible, isPlaying, isControlsLocked) {
        if (areControlsVisible && isPlaying && !isControlsLocked && !showQualityDialog && !showAudioDialog && !showSubtitleDialog && !showServerDialog) {
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
        // Player Surface View with keepScreenOn enabled
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
            },
            modifier = Modifier.fillMaxSize()
        )

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
                                Color(0xDD000000),
                                Color(0x30000000),
                                Color(0xEE000000)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left group: Back & Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .testTag("player_back_button")
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0x66000000))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )
                            if (subtitle.isNotEmpty()) {
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SaribTextSecondary
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right group: LIVE tag, Server Switcher, Subtitles CC, Quality, Audio, PiP, Screen Rotate, Lock
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isLive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SaribLiveRed)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "مباشر LIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Black
                                    )
                                )
                            }
                        }

                        // Server Switcher Button (زر مبدل السيرفرات 5 سيرفرات)
                        IconButton(
                            onClick = {
                                showServerDialog = true
                                showSubtitleDialog = false
                                showQualityDialog = false
                                showAudioDialog = false
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SaribElectricBlue.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = "مبدل السيرفرات",
                                tint = SaribCyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Subtitle / Translation Button (زر الترجمة الحقيقية)
                        IconButton(
                            onClick = {
                                showSubtitleDialog = true
                                showServerDialog = false
                                showQualityDialog = false
                                showAudioDialog = false
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (selectedSubtitleIndex != 0) SaribCyanAccent.copy(alpha = 0.3f) else Color(0x66000000))
                                .testTag("subtitles_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ClosedCaption,
                                contentDescription = "الترجمة",
                                tint = if (selectedSubtitleIndex != 0) SaribCyanAccent else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Quality Selector Button
                        IconButton(
                            onClick = {
                                showQualityDialog = true
                                showSubtitleDialog = false
                                showServerDialog = false
                                showAudioDialog = false
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (selectedQualityIndex != 0) SaribElectricBlue else Color(0x66000000))
                                .testTag("quality_selector_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.HighQuality,
                                contentDescription = "تغيير الجودة",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Audio Track Selector Button
                        IconButton(
                            onClick = {
                                showAudioDialog = true
                                showSubtitleDialog = false
                                showServerDialog = false
                                showQualityDialog = false
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (selectedAudioTrackIndex != 0) SaribElectricBlue else Color(0x66000000))
                                .testTag("audio_track_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = "مسارات الصوت",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Picture-in-Picture Button
                        IconButton(
                            onClick = enterPiPMode,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x66000000))
                                .testTag("pip_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureInPictureAlt,
                                contentDescription = "صورة في صورة PiP",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Screen Orientation Switcher
                        IconButton(
                            onClick = toggleScreenOrientation,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x66000000))
                                .testTag("rotate_screen_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenRotation,
                                contentDescription = if (isLandscape) "الوضع العمودي" else "الوضع الأفقي",
                                tint = SaribCyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Screen Lock Button
                        IconButton(
                            onClick = {
                                isControlsLocked = true
                                areControlsVisible = false
                                Toast.makeText(context, "تم قفل الشاشة لمنع اللمس غير المقصود", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x66000000))
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

                // Center Controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(36.dp)
                ) {
                    IconButton(
                        onClick = {
                            val target = (exoPlayer.currentPosition - 10000).coerceAtLeast(0L)
                            exoPlayer.seekTo(target)
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "تأخير 10 ثوان",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(SaribElectricBlue, Color(0xFF0055D4))
                                )
                            )
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
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val target = (exoPlayer.currentPosition + 10000).coerceAtMost(duration)
                            exoPlayer.seekTo(target)
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "تقديم 10 ثوان",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Bottom Controls Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    if (duration > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatDuration(currentPosition),
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Slider(
                                value = if (duration > 0) (currentPosition.toFloat() / duration).coerceIn(0f, 1f) else 0f,
                                onValueChange = { frac ->
                                    val newPos = (frac * duration).toLong()
                                    exoPlayer.seekTo(newPos)
                                    currentPosition = newPos
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = SaribCyanAccent,
                                    activeTrackColor = SaribCyanAccent,
                                    inactiveTrackColor = Color(0x66FFFFFF)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formatDuration(duration),
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isLive) "سيرفر البث: ${serverOptions.getOrNull(selectedServerIndex)?.first ?: ""}" else availableQualityOptions.getOrNull(selectedQualityIndex)?.label ?: "تلقائي",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = SaribCyanAccent,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        IconButton(
                            onClick = {
                                resizeMode = when (resizeMode) {
                                    AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                    else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                                val modeName = when (resizeMode) {
                                    AspectRatioFrameLayout.RESIZE_MODE_FIT -> "تناسب أصلي (Fit)"
                                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> "تكبير سينمائي (Zoom)"
                                    else -> "ملء الشاشة بالكامل (Fill)"
                                }
                                Toast.makeText(context, modeName, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x66000000))
                        ) {
                            Icon(
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = "نسبة العرض",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
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
