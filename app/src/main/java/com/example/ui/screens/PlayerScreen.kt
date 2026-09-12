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
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.GridView
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
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.model.ChannelItem
import com.example.data.model.getActiveServers
import com.example.ui.components.SaribLoadingIndicator
import com.example.ui.theme.SaribCardBg
import com.example.ui.theme.SaribCardBorder
import com.example.ui.theme.SaribCardBorderSubtle
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribLiveRed
import com.example.ui.theme.SaribTextMuted
import com.example.ui.theme.SaribTextPrimary
import com.example.ui.theme.SaribTextSecondary
import com.example.util.StreamUrlParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class QualityOption(
    val name: String,
    val resolutionLabel: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val bitrate: Int = 0,
    val isAuto: Boolean = false,
    val trackGroup: Tracks.Group? = null,
    val trackIndex: Int = -1
)

data class AudioTrackOption(
    val label: String,
    val language: String,
    val trackGroupIndex: Int = -1,
    val trackIndex: Int = -1
)

data class SubtitleTrackOption(
    val label: String,
    val language: String,
    val trackGroupIndex: Int = -1,
    val trackIndex: Int = -1
)

data class MultiStreamSlot(
    val title: String,
    val streamUrl: String,
    val serverName: String
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

    var currentTitle by remember(title) { mutableStateOf(title) }
    var currentSubtitle by remember(subtitle) { mutableStateOf(subtitle) }
    var currentServersList by remember(servers, streamUrl) {
        val valid = servers.filter { it.second.isNotBlank() }
        mutableStateOf(
            if (valid.isNotEmpty()) valid
            else listOf("سيرفر البث المباشر (الرئيسي FHD)" to streamUrl)
        )
    }

    var selectedServerIndex by remember { mutableIntStateOf(0) }
    var currentActiveUrl by remember(selectedServerIndex, currentServersList) {
        mutableStateOf(currentServersList.getOrNull(selectedServerIndex)?.second ?: streamUrl)
    }

    // Playback state
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
    var showInPlayerChannelDrawer by remember { mutableStateOf(false) }
    var autoRetryCount by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    // Multi-Server & Multi-Stream State (Up to 5 simultaneous servers/channels)
    var isMultiStreamMode by remember { mutableStateOf(false) }
    var activeAudioSlot by remember { mutableIntStateOf(0) } // 0..4
    var activePickingSlot by remember { mutableIntStateOf(0) }

    // Multi-stream slots data
    var slotStreams by remember(currentServersList, availableChannels) {
        val list = mutableListOf<MultiStreamSlot>()
        // Fill up to 5 slots from available servers or fallback channels
        for (i in 0 until 5) {
            if (i < currentServersList.size) {
                list.add(MultiStreamSlot(title = "$currentTitle (${currentServersList[i].first})", streamUrl = currentServersList[i].second, serverName = currentServersList[i].first))
            } else {
                val channel = availableChannels.getOrNull(i - currentServersList.size)
                if (channel != null) {
                    list.add(MultiStreamSlot(title = channel.name, streamUrl = channel.streamUrl, serverName = "قناة ${i + 1}"))
                }
            }
        }
        mutableStateOf(list)
    }

    // Main Single / Primary ExoPlayer (Slot 0) - Ultra High Performance & Instant Startup
    val exoPlayer = remember {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ if (isLive) 800 else 1500,
                /* maxBufferMs = */ if (isLive) 30000 else 90000,
                /* bufferForPlaybackMs = */ 100, // Starts immediately (100ms)
                /* bufferForPlaybackAfterRebufferMs = */ 250
            )
            .setBackBuffer(if (isLive) 3000 else 15000, true)
            .setTargetBufferBytes(C.LENGTH_UNSET) // Unlimited buffer size to unleash full internet speed without throttling
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            .setEnableDecoderFallback(true)

        ExoPlayer.Builder(context, renderersFactory)
            .setLoadControl(loadControl)
            .build().apply {
                playWhenReady = true
            }
    }

    // Quality, Audio & Subtitles
    var availableQualityOptions by remember {
        mutableStateOf(
            listOf(
                QualityOption("تلقائي (متكيف حسب سرعة النت)", "Auto Adaptive", isAuto = true)
            )
        )
    }
    var selectedQualityIndex by remember { mutableIntStateOf(0) }
    var availableAudioTracks by remember { mutableStateOf<List<AudioTrackOption>>(emptyList()) }
    var selectedAudioTrackIndex by remember { mutableIntStateOf(0) }
    var availableSubtitleTracks by remember { mutableStateOf<List<SubtitleTrackOption>>(emptyList()) }
    var selectedSubtitleIndex by remember { mutableIntStateOf(0) }

    val applyQualitySelection: (QualityOption, Int) -> Unit = remember(exoPlayer) {
        { opt, idx ->
            selectedQualityIndex = idx
            try {
                if (opt.isAuto) {
                    exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                        .buildUpon()
                        .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                        .clearVideoSizeConstraints()
                        .setMaxVideoBitrate(Int.MAX_VALUE)
                        .build()
                } else if (opt.trackGroup != null && opt.trackIndex >= 0) {
                    exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                        .buildUpon()
                        .setOverrideForType(
                            TrackSelectionOverride(
                                opt.trackGroup.mediaTrackGroup,
                                listOf(opt.trackIndex)
                            )
                        )
                        .build()
                } else if (opt.height > 0) {
                    exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                        .buildUpon()
                        .setMaxVideoSize(opt.width.coerceAtLeast(1920), opt.height)
                        .setMaxVideoBitrate(if (opt.bitrate > 0) opt.bitrate + 500_000 else Int.MAX_VALUE)
                        .build()
                }
            } catch (e: Exception) {
                android.util.Log.e("PlayerScreen", "Error applying track override: ${e.message}")
            }
        }
    }

    // Anti-VPN 3-Second Security Scanner state
    var isVpnDetectedInPlayer by remember { mutableStateOf(false) }

    // Function to play main stream cleanly using StreamUrlParser
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
                        .setConnectTimeoutMs(8000)
                        .setReadTimeoutMs(15000)
                    StreamUrlParser.configureHttpDataSource(httpDataSourceFactory, parsed)

                    val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
                    val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory, StreamUrlParser.createExtractorsFactory())
                    val drmManager = StreamUrlParser.createDrmSessionManager(parsed)
                    if (drmManager != null) {
                        mediaSourceFactory.setDrmSessionManagerProvider { drmManager }
                    }

                    val mediaItemBuilder = MediaItem.Builder().setUri(Uri.parse(parsed.cleanUrl))
                    if (parsed.mimeType != null) {
                        mediaItemBuilder.setMimeType(parsed.mimeType)
                    }
                    if (isLive) {
                        mediaItemBuilder.setLiveConfiguration(
                            MediaItem.LiveConfiguration.Builder()
                                .setTargetOffsetMs(1000)
                                .setMinOffsetMs(500)
                                .setMaxOffsetMs(4000)
                                .setMinPlaybackSpeed(0.97f)
                                .setMaxPlaybackSpeed(1.03f)
                                .build()
                        )
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

    // Sub-Players for Multi-Server & Multi-View (Slots 1, 2, 3, 4)
    val subLoadControl = remember {
        DefaultLoadControl.Builder()
            .setBufferDurationsMs(2000, 8000, 800, 1200)
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

    val subPlayer1 = remember(isMultiStreamMode) {
        if (isMultiStreamMode) {
            ExoPlayer.Builder(context).setLoadControl(subLoadControl).build().apply {
                playWhenReady = true
                volume = if (activeAudioSlot == 1) 1f else 0f
            }
        } else null
    }

    val subPlayer2 = remember(isMultiStreamMode) {
        if (isMultiStreamMode) {
            ExoPlayer.Builder(context).setLoadControl(subLoadControl).build().apply {
                playWhenReady = true
                volume = if (activeAudioSlot == 2) 1f else 0f
            }
        } else null
    }

    val subPlayer3 = remember(isMultiStreamMode) {
        if (isMultiStreamMode) {
            ExoPlayer.Builder(context).setLoadControl(subLoadControl).build().apply {
                playWhenReady = true
                volume = if (activeAudioSlot == 3) 1f else 0f
            }
        } else null
    }

    val subPlayer4 = remember(isMultiStreamMode) {
        if (isMultiStreamMode) {
            ExoPlayer.Builder(context).setLoadControl(subLoadControl).build().apply {
                playWhenReady = true
                volume = if (activeAudioSlot == 4) 1f else 0f
            }
        } else null
    }

    // Audio routing between active slots
    LaunchedEffect(isMultiStreamMode, activeAudioSlot) {
        if (isMultiStreamMode) {
            exoPlayer.volume = if (activeAudioSlot == 0) 1f else 0f
            subPlayer1?.volume = if (activeAudioSlot == 1) 1f else 0f
            subPlayer2?.volume = if (activeAudioSlot == 2) 1f else 0f
            subPlayer3?.volume = if (activeAudioSlot == 3) 1f else 0f
            subPlayer4?.volume = if (activeAudioSlot == 4) 1f else 0f
        } else {
            exoPlayer.volume = 1f
        }
    }

    // Launch subplayers when multi-stream is active
    LaunchedEffect(isMultiStreamMode, slotStreams) {
        if (isMultiStreamMode) {
            slotStreams.getOrNull(1)?.let { playInSubPlayer(subPlayer1, it.streamUrl) }
            slotStreams.getOrNull(2)?.let { playInSubPlayer(subPlayer2, it.streamUrl) }
            slotStreams.getOrNull(3)?.let { playInSubPlayer(subPlayer3, it.streamUrl) }
            slotStreams.getOrNull(4)?.let { playInSubPlayer(subPlayer4, it.streamUrl) }
        }
    }

    // Cleanup subplayers when exiting multi-stream
    DisposableEffect(isMultiStreamMode) {
        onDispose {
            subPlayer1?.release()
            subPlayer2?.release()
            subPlayer3?.release()
            subPlayer4?.release()
        }
    }

    // Play active url on main player
    LaunchedEffect(currentActiveUrl) {
        playStream(currentActiveUrl)
    }

    // Channel Switching Logic without exiting player
    val currentChannelIndex = remember(currentActiveUrl, availableChannels) {
        availableChannels.indexOfFirst { it.streamUrl == currentActiveUrl || it.name == currentTitle }
    }

    val switchChannel: (ChannelItem) -> Unit = remember {
        { channel ->
            currentTitle = channel.name
            currentSubtitle = channel.categoryName
            val activeServers = channel.getActiveServers()
            currentServersList = if (activeServers.isNotEmpty()) activeServers else listOf("سيرفر البث الرئيسي" to channel.streamUrl)
            selectedServerIndex = 0
            currentActiveUrl = channel.streamUrl
            playStream(channel.streamUrl)
            showInPlayerChannelDrawer = false
            Toast.makeText(context, "تم التحويل إلى: ${channel.name}", Toast.LENGTH_SHORT).show()
        }
    }

    val nextChannel: () -> Unit = {
        if (availableChannels.isNotEmpty()) {
            val nextIdx = if (currentChannelIndex >= 0) (currentChannelIndex + 1) % availableChannels.size else 0
            switchChannel(availableChannels[nextIdx])
        }
    }

    val previousChannel: () -> Unit = {
        if (availableChannels.isNotEmpty()) {
            val prevIdx = if (currentChannelIndex > 0) currentChannelIndex - 1 else availableChannels.size - 1
            switchChannel(availableChannels[prevIdx])
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
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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
                        }
                    }
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                    exoPlayer.seekToDefaultPosition()
                    exoPlayer.prepare()
                    exoPlayer.play()
                    return
                }

                if (autoRetryCount < 3) {
                    autoRetryCount++
                    coroutineScope.launch {
                        delay(1200L)
                        playStream(currentActiveUrl)
                    }
                } else if (currentServersList.size > 1 && selectedServerIndex < currentServersList.size - 1) {
                    autoRetryCount = 0
                    selectedServerIndex = (selectedServerIndex + 1) % currentServersList.size
                    currentActiveUrl = currentServersList[selectedServerIndex].second
                    Toast.makeText(context, "تم التحويل التلقائي للسيرفر الاحتياطي: ${currentServersList[selectedServerIndex].first}", Toast.LENGTH_SHORT).show()
                } else {
                    hasError = true
                    isBuffering = false
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                val videoQualityList = mutableListOf<QualityOption>()
                val audioList = mutableListOf<AudioTrackOption>()
                val subList = mutableListOf<SubtitleTrackOption>()
                var maxW = 0
                var maxH = 0
                var maxBr = 0

                tracks.groups.forEachIndexed { groupIdx, group ->
                    val type = group.type
                    for (trackIdx in 0 until group.length) {
                        val format = group.getTrackFormat(trackIdx)
                        val lang = format.language ?: "und"
                        val label = format.label ?: if (lang != "und") lang else "مسار ${trackIdx + 1}"

                        if (type == C.TRACK_TYPE_VIDEO) {
                            val w = format.width
                            val h = format.height
                            val br = format.bitrate
                            val fps = if (format.frameRate > 0f) " ${format.frameRate.toInt()}fps" else ""
                            if (h > 0 && w > 0) {
                                if (h > maxH) maxH = h
                                if (w > maxW) maxW = w
                                if (br > maxBr) maxBr = br

                                val resLabel = when {
                                    h >= 2160 -> "4K Ultra HD ($w×$h$fps)"
                                    h >= 1440 -> "2K QHD ($w×$h$fps)"
                                    h >= 1080 -> "1080p FHD ($w×$h$fps)"
                                    h >= 720 -> "720p HD ($w×$h$fps)"
                                    h >= 576 -> "576p SD ($w×$h$fps)"
                                    h >= 480 -> "480p SD ($w×$h$fps)"
                                    h >= 360 -> "360p منخفض ($w×$h$fps)"
                                    else -> "${h}p ($w×$h$fps)"
                                }
                                val brLabel = if (br > 0) " (${br / 1000} Kbps)" else ""
                                val displayTitle = format.label ?: "$resLabel$brLabel"

                                videoQualityList.add(
                                    QualityOption(
                                        name = displayTitle,
                                        resolutionLabel = "$w×$h$fps",
                                        width = w,
                                        height = h,
                                        bitrate = br,
                                        isAuto = false,
                                        trackGroup = group,
                                        trackIndex = trackIdx
                                    )
                                )
                            }
                        } else if (type == C.TRACK_TYPE_AUDIO) {
                            audioList.add(AudioTrackOption(label = label, language = lang, trackGroupIndex = groupIdx, trackIndex = trackIdx))
                        } else if (type == C.TRACK_TYPE_TEXT) {
                            subList.add(SubtitleTrackOption(label = label, language = lang, trackGroupIndex = groupIdx, trackIndex = trackIdx))
                        }
                    }
                }

                videoQualityList.sortWith(compareByDescending<QualityOption> { it.height }.thenByDescending { it.bitrate })

                val finalQualities = mutableListOf<QualityOption>()
                val autoTitle = if (maxH > 0) {
                    "تلقائي متكيف (أقصى دقة للقناة: ${maxH}p)"
                } else {
                    "تلقائي متكيف (أفضل دقة حسب سرعة النت)"
                }
                finalQualities.add(
                    QualityOption(
                        name = autoTitle,
                        resolutionLabel = if (maxH > 0) "$maxW×$maxH" else "Auto Adaptive",
                        width = maxW,
                        height = maxH,
                        bitrate = maxBr,
                        isAuto = true
                    )
                )
                finalQualities.addAll(videoQualityList)

                availableQualityOptions = finalQualities
                availableAudioTracks = audioList
                availableSubtitleTracks = subList
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
            activity?.requestedOrientation = originalOrientation
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Periodic time & progress update
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration.coerceAtLeast(0L)
            if (!isLive && currentPosition > 0L) {
                onProgressUpdate(currentPosition, duration)
            }
            delay(1000L)
        }
    }

    // Auto-hide controls after 5 seconds
    LaunchedEffect(areControlsVisible, isControlsLocked) {
        if (areControlsVisible && !isControlsLocked) {
            delay(5000L)
            areControlsVisible = false
        }
    }

    val enterPiPMode: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity != null) {
            val aspectRatio = Rational(16, 9)
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .build()
            activity.enterPictureInPictureMode(params)
        }
    }

    val playerFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        try {
            playerFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    // MAIN CONTAINER WITH TV REMOTE CONTROL SUPPORT
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(playerFocusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp) {
                    when (keyEvent.key) {
                        Key.DirectionCenter, Key.Enter, Key.NumPadEnter, Key.ButtonA -> {
                            if (!isControlsLocked) {
                                areControlsVisible = !areControlsVisible
                            }
                            true
                        }
                        Key.MediaPlayPause -> {
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            true
                        }
                        Key.MediaPlay -> {
                            exoPlayer.play()
                            true
                        }
                        Key.MediaPause -> {
                            exoPlayer.pause()
                            true
                        }
                        Key.DirectionLeft, Key.MediaFastForward -> {
                            if (!isLive) {
                                val newPos = (exoPlayer.currentPosition + 10000L).coerceAtMost(exoPlayer.duration)
                                exoPlayer.seekTo(newPos)
                                areControlsVisible = true
                            }
                            true
                        }
                        Key.DirectionRight, Key.MediaRewind -> {
                            if (!isLive) {
                                val newPos = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                                exoPlayer.seekTo(newPos)
                                areControlsVisible = true
                            }
                            true
                        }
                        Key.DirectionUp, Key.DirectionDown -> {
                            areControlsVisible = true
                            true
                        }
                        Key.Back, Key.Escape -> {
                            if (areControlsVisible) {
                                areControlsVisible = false
                                true
                            } else {
                                onBackClick()
                                true
                            }
                        }
                        else -> false
                    }
                } else false
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!isControlsLocked) {
                    areControlsVisible = !areControlsVisible
                }
            }
    ) {
        // ================= VIDEO RENDERING LAYER =================
        if (isMultiStreamMode) {
            // MULTI-STREAM GRID (5 Simultaneous Servers / Channels)
            val streamCount = slotStreams.size.coerceIn(2, 5)
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Row of Multi-Stream
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // Slot 0 (Main Player)
                    MultiStreamTile(
                        player = exoPlayer,
                        title = "1: ${slotStreams.getOrNull(0)?.serverName ?: "سيرفر 1"}",
                        isActive = activeAudioSlot == 0,
                        onSelect = { activeAudioSlot = 0 },
                        onExpand = {
                            isMultiStreamMode = false
                            selectedServerIndex = 0
                            currentActiveUrl = slotStreams[0].streamUrl
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )

                    // Slot 1 (Sub Player 1)
                    if (subPlayer1 != null && streamCount >= 2) {
                        MultiStreamTile(
                            player = subPlayer1,
                            title = "2: ${slotStreams.getOrNull(1)?.serverName ?: "سيرفر 2"}",
                            isActive = activeAudioSlot == 1,
                            onSelect = { activeAudioSlot = 1 },
                            onExpand = {
                                isMultiStreamMode = false
                                currentActiveUrl = slotStreams.getOrNull(1)?.streamUrl ?: currentActiveUrl
                            },
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }

                    // Slot 2 (Sub Player 2 for 3 or 5 streams)
                    if (subPlayer2 != null && (streamCount == 3 || streamCount >= 5)) {
                        MultiStreamTile(
                            player = subPlayer2,
                            title = "3: ${slotStreams.getOrNull(2)?.serverName ?: "سيرفر 3"}",
                            isActive = activeAudioSlot == 2,
                            onSelect = { activeAudioSlot = 2 },
                            onExpand = {
                                isMultiStreamMode = false
                                currentActiveUrl = slotStreams.getOrNull(2)?.streamUrl ?: currentActiveUrl
                            },
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                }

                // Bottom Row of Multi-Stream (for 4 or 5 streams)
                if (streamCount >= 4) {
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        if (subPlayer3 != null) {
                            MultiStreamTile(
                                player = subPlayer3,
                                title = "4: ${slotStreams.getOrNull(3)?.serverName ?: "سيرفر 4"}",
                                isActive = activeAudioSlot == 3,
                                onSelect = { activeAudioSlot = 3 },
                                onExpand = {
                                    isMultiStreamMode = false
                                    currentActiveUrl = slotStreams.getOrNull(3)?.streamUrl ?: currentActiveUrl
                                },
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }

                        if (subPlayer4 != null && streamCount >= 5) {
                            MultiStreamTile(
                                player = subPlayer4,
                                title = "5: ${slotStreams.getOrNull(4)?.serverName ?: "سيرفر 5"}",
                                isActive = activeAudioSlot == 4,
                                onSelect = { activeAudioSlot = 4 },
                                onExpand = {
                                    isMultiStreamMode = false
                                    currentActiveUrl = slotStreams.getOrNull(4)?.streamUrl ?: currentActiveUrl
                                },
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                    }
                }
            }
        } else {
            // SINGLE FULLSCREEN PLAYER
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

        // Loading Indicator
        if (isBuffering && !hasError) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                SaribLoadingIndicator(size = 56.dp, label = "جاري البث بجودة فائقة...")
            }
        }

        // Floating Unlock Button when Screen is Locked
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
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SaribCyanAccent)
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "إلغاء قفل الشاشة",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // ================= CONTROLS OVERLAY =================
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
                                Color(0xDD040810),
                                Color(0x10000000),
                                Color(0xF2040810)
                            )
                        )
                    )
                    .padding(12.dp)
            ) {
                // TOP BAR: Back Button + Title + Top Action Tools
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(top = 4.dp, start = 6.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Text(
                                text = currentTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (currentSubtitle.isNotBlank()) {
                                Text(
                                    text = currentSubtitle,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SaribTextMuted,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // LIVE Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isLive) SaribLiveRed else SaribElectricBlue)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isLive) "LIVE" else "VOD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }

                        // Aspect Ratio / Resize Mode
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
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x55000000))
                        ) {
                            Icon(Icons.Default.AspectRatio, contentDescription = "تنسيق الأبعاد", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        // Cast to TV Button
                        IconButton(
                            onClick = {
                                showCastDialog = true
                                showServerDialog = false
                                showQualityDialog = false
                                showInPlayerChannelDrawer = false
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x55000000))
                        ) {
                            Icon(Icons.Default.Cast, contentDescription = "بث للشاشة", tint = SaribCyanAccent, modifier = Modifier.size(18.dp))
                        }

                        // PiP Button
                        IconButton(
                            onClick = enterPiPMode,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x55000000))
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureInPictureAlt,
                                contentDescription = "صورة في صورة",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Screen Lock Button
                        IconButton(
                            onClick = {
                                isControlsLocked = true
                                areControlsVisible = false
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x55000000))
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "قفل الأزرار", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // ================= CENTER PLAYBACK CONTROLS =================
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
                ) {
                    // Previous Channel
                    IconButton(
                        onClick = previousChannel,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                            .border(1.dp, SaribCyanAccent.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "القناة السابقة",
                            tint = SaribCyanAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Fast Rewind 10s
                    IconButton(
                        onClick = {
                            val target = (exoPlayer.currentPosition - 10000).coerceAtLeast(0L)
                            exoPlayer.seekTo(target)
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "تأخير 10 ثوان",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Main Glowing Play / Pause Button
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(SaribCyanAccent)
                            .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                            .clickable {
                                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "إيقاف" else "تشغيل",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Fast Forward 10s
                    IconButton(
                        onClick = {
                            val target = (exoPlayer.currentPosition + 10000).coerceAtMost(if (duration > 0) duration else Long.MAX_VALUE)
                            exoPlayer.seekTo(target)
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "تقديم 10 ثوان",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Next Channel
                    IconButton(
                        onClick = nextChannel,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                            .border(1.dp, SaribCyanAccent.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "القناة التالية",
                            tint = SaribCyanAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // ================= UNIFIED ALL-IN-ONE BOTTOM CONTROLS DOCK =================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xE608101E))
                        .border(1.dp, SaribCardBorderSubtle, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    // Row 1: Time Slider (Seek Bar)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDuration(currentPosition),
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 11.sp)
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
                                thumbColor = SaribCyanAccent,
                                activeTrackColor = SaribCyanAccent,
                                inactiveTrackColor = Color(0xFF2A374A)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                                .padding(horizontal = 8.dp)
                        )

                        Text(
                            text = if (duration > 0) formatDuration(duration) else if (isLive) "مباشر" else "00:00",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isLive) SaribCyanAccent else Color.White,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Row 2: Prominent Action Buttons Bar (إظهار كافة الوظائف بوضوح ودون اختفاء)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 1. In-Player Channel Switcher Drawer Button (قائمة القنوات)
                        Surface(
                            color = SaribElectricBlue.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SaribCyanAccent.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    showInPlayerChannelDrawer = true
                                    showServerDialog = false
                                    showQualityDialog = false
                                    showCastDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Tv, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("القنوات", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                            }
                        }

                        // 2. Individual Server Picker (السيرفرات)
                        Surface(
                            color = Color(0x22FFFFFF),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    showServerDialog = true
                                    showInPlayerChannelDrawer = false
                                    showQualityDialog = false
                                    showCastDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Dns, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "السيرفرات (${currentServersList.size})",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                )
                            }
                        }

                        // 3. Real Quality & Audio Settings (الجودة والصوت)
                        val currentQualityLabel = availableQualityOptions.getOrNull(selectedQualityIndex)?.let {
                            if (it.isAuto) "تلقائي" else it.resolutionLabel.ifBlank { "مخصص" }
                        } ?: "الجودة"

                        Surface(
                            color = Color(0x22FFFFFF),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    showQualityDialog = true
                                    showServerDialog = false
                                    showInPlayerChannelDrawer = false
                                    showCastDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "الجودة ($currentQualityLabel)",
                                    color = SaribCyanAccent,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                )
                            }
                        }

                        // 4. Multi-Server / Multi-Stream Toggle Button (تشغيل كل السيرفرات معاً)
                        Surface(
                            color = if (isMultiStreamMode) SaribCyanAccent.copy(alpha = 0.35f) else Color(0x22FFFFFF),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isMultiStreamMode) SaribCyanAccent else Color(0x33FFFFFF)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    isMultiStreamMode = !isMultiStreamMode
                                    if (isMultiStreamMode) {
                                        activeAudioSlot = 0
                                        Toast.makeText(context, "تم تفعيل عرض كل السيرفرات معاً", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.GridView, contentDescription = null, tint = if (isMultiStreamMode) SaribCyanAccent else Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isMultiStreamMode) "فردي" else "كل السيرفرات",
                                    color = if (isMultiStreamMode) SaribCyanAccent else Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                )
                            }
                        }

                        // 5. Instant Reload / Refresh Stream Button (تحديث البث)
                        Surface(
                            color = Color(0x22FFFFFF),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    playStream(currentActiveUrl)
                                    Toast.makeText(context, "جاري تحديث وإعادة تشغيل البث...", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تحديث", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                            }
                        }
                    }
                }
            }
        }

        // ================= IN-PLAYER CHANNEL DRAWER (تبديل القناة دون خروج من المشغل) =================
        AnimatedVisibility(
            visible = showInPlayerChannelDrawer,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            var channelFilterQuery by remember { mutableStateOf("") }
            var selectedCategoryFilter by remember { mutableStateOf("الكل") }

            val categoriesList = remember(availableChannels) {
                val list = mutableListOf("الكل")
                list.addAll(availableChannels.map { it.categoryName.trim() }.filter { it.isNotBlank() }.distinct())
                list
            }

            val filteredChannelsList = remember(availableChannels, channelFilterQuery, selectedCategoryFilter) {
                availableChannels.filter { ch ->
                    val catMatch = if (selectedCategoryFilter == "الكل") true else ch.categoryName.contains(selectedCategoryFilter, ignoreCase = true)
                    val queryMatch = if (channelFilterQuery.isBlank()) true else ch.name.contains(channelFilterQuery, ignoreCase = true)
                    catMatch && queryMatch
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(320.dp),
                color = Color(0xF2070D18),
                border = androidx.compose.foundation.BorderStroke(1.dp, SaribCardBorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📺 قائمة القنوات السريعة",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SaribCyanAccent)
                        )
                        IconButton(onClick = { showInPlayerChannelDrawer = false }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SaribTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search input
                    OutlinedTextField(
                        value = channelFilterQuery,
                        onValueChange = { channelFilterQuery = it },
                        placeholder = { Text("بحث عن قناة...", color = SaribTextMuted, fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaribCyanAccent,
                            unfocusedBorderColor = SaribCardBorderSubtle,
                            focusedContainerColor = Color(0x33000000),
                            unfocusedContainerColor = Color(0x33000000),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(16.dp)) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(categoriesList) { cat ->
                            val isSel = selectedCategoryFilter == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) SaribCyanAccent.copy(alpha = 0.2f) else Color(0x22FFFFFF))
                                    .border(0.5.dp, if (isSel) SaribCyanAccent else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selectedCategoryFilter = cat }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSel) SaribCyanAccent else SaribTextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Channels List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredChannelsList, key = { it.id }) { channel ->
                            val isCurrent = channel.name == currentTitle || channel.streamUrl == currentActiveUrl
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { switchChannel(channel) },
                                color = if (isCurrent) SaribElectricBlue.copy(alpha = 0.3f) else Color(0x22FFFFFF),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, if (isCurrent) SaribCyanAccent else Color(0x22FFFFFF))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (channel.logoUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = channel.logoUrl,
                                            contentDescription = channel.name,
                                            modifier = Modifier.size(30.dp).clip(CircleShape),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Icon(Icons.Default.Tv, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = channel.name,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = if (isCurrent) SaribCyanAccent else Color.White,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = channel.categoryName,
                                            style = MaterialTheme.typography.labelSmall.copy(color = SaribTextMuted, fontSize = 10.sp),
                                            maxLines = 1
                                        )
                                    }
                                    if (isCurrent) {
                                        Icon(Icons.Default.Check, contentDescription = "مشغل حالياً", tint = SaribCyanAccent, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= SERVER SELECTION DIALOG =================
        if (showServerDialog) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(max = 380.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SaribCyanAccent, RoundedCornerShape(20.dp)),
                color = Color(0xF2070D18)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📡 سيرفرات البث المتاحة",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SaribCyanAccent)
                        )
                        IconButton(onClick = { showServerDialog = false }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SaribTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    currentServersList.forEachIndexed { idx, (srvName, srvUrl) ->
                        val isSel = selectedServerIndex == idx
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) SaribElectricBlue.copy(alpha = 0.3f) else Color(0x22FFFFFF))
                                .border(0.5.dp, if (isSel) SaribCyanAccent else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedServerIndex = idx
                                    currentActiveUrl = srvUrl
                                    showServerDialog = false
                                    playStream(srvUrl)
                                    Toast.makeText(context, "تم التحويل إلى: $srvName", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = srvName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isSel) SaribCyanAccent else Color.White,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                                if (isSel) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= QUALITY & AUDIO SETTINGS DIALOG =================
        if (showQualityDialog) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(max = 420.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SaribCyanAccent, RoundedCornerShape(20.dp)),
                color = Color(0xF2070D18)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "جودة البث والصوت الحقيقية",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                            )
                        }
                        IconButton(onClick = { showQualityDialog = false }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SaribTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "دقة الفيديو الفعلية للبث:",
                        style = MaterialTheme.typography.labelMedium.copy(color = SaribCyanAccent, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (availableQualityOptions.isEmpty()) {
                        Text(
                            text = "جاري الكشف عن الجودات المتاحة...",
                            style = MaterialTheme.typography.bodySmall.copy(color = SaribTextMuted)
                        )
                    } else {
                        availableQualityOptions.forEachIndexed { idx, opt ->
                            val isSel = selectedQualityIndex == idx
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) SaribElectricBlue.copy(alpha = 0.35f) else Color(0x22FFFFFF))
                                    .border(1.dp, if (isSel) SaribCyanAccent else Color(0x15FFFFFF), RoundedCornerShape(10.dp))
                                    .clickable {
                                        applyQualitySelection(opt, idx)
                                        showQualityDialog = false
                                        Toast.makeText(context, "تم تطبيق: ${opt.name}", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = opt.name,
                                            color = if (isSel) SaribCyanAccent else Color.White,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium)
                                        )
                                        if (opt.resolutionLabel.isNotBlank()) {
                                            Text(
                                                text = "الدقة: ${opt.resolutionLabel}",
                                                color = SaribTextMuted,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                            )
                                        }
                                    }
                                    if (isSel) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "محدد",
                                            tint = SaribCyanAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Multi Audio Tracks Section (إذا تواجدت عدة مسارات صوتية)
                    if (availableAudioTracks.size > 1) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "المسارات الصوتية / المعلق:",
                            style = MaterialTheme.typography.labelMedium.copy(color = SaribCyanAccent, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        availableAudioTracks.forEachIndexed { aIdx, aTrack ->
                            val isAudioSel = selectedAudioTrackIndex == aIdx
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isAudioSel) SaribElectricBlue.copy(alpha = 0.35f) else Color(0x18FFFFFF))
                                    .clickable {
                                        selectedAudioTrackIndex = aIdx
                                        try {
                                            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                                .buildUpon()
                                                .setPreferredAudioLanguage(aTrack.language)
                                                .build()
                                            Toast.makeText(context, "تم تغيير الصوت: ${aTrack.label}", Toast.LENGTH_SHORT).show()
                                        } catch (_: Exception) {}
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = aTrack.label,
                                        color = if (isAudioSel) SaribCyanAccent else Color.White,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    if (isAudioSel) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= CAST TO TV DIALOG =================
        if (showCastDialog) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(max = 380.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SaribCyanAccent, RoundedCornerShape(20.dp)),
                color = Color(0xF2070D18)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Cast, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("بث ومشاركة الشاشة على التلفاز", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "يمكنك فتح إعدادات البث اللاسلكي أو نسخ رابط البث المباشر لتشغيله على أي مشغل خارجي",
                        style = MaterialTheme.typography.bodySmall.copy(color = SaribTextMuted, textAlign = TextAlign.Center)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_CAST_SETTINGS))
                            } catch (e: Exception) {
                                Toast.makeText(context, "خاصية البث اللاسلكي غير مدعومة", Toast.LENGTH_SHORT).show()
                            }
                            showCastDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaribElectricBlue),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("فتح إعدادات البث على التلفاز (Cast)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * MultiStream Sub-player Tile for Multi-Server & Multi-View playback.
 */
@Composable
private fun MultiStreamTile(
    player: ExoPlayer,
    title: String,
    isActive: Boolean,
    onSelect: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                width = if (isActive) 2.5.dp else 0.8.dp,
                color = if (isActive) SaribCyanAccent else Color(0x44FFFFFF)
            )
            .clickable { onSelect() }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                    this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    keepScreenOn = true
                }
            },
            update = { pv ->
                if (pv.player != player) pv.player = player
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Badge & Controls
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
                    imageVector = if (isActive) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = null,
                    tint = if (isActive) SaribCyanAccent else Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    color = if (isActive) SaribCyanAccent else Color.White,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                    maxLines = 1,
                    modifier = Modifier.widthIn(max = 140.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onExpand, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Default.Fullscreen, contentDescription = "تكبير", tint = SaribCyanAccent, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    if (millis <= 0L) return "00:00"
    val totalSec = millis / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) {
        String.format("%02d:%02d:%02d", h, m, s)
    } else {
        String.format("%02d:%02d", m, s)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
