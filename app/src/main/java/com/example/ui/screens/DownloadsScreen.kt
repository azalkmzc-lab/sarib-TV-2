package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.download.SaribDownloadManager
import com.example.data.local.DownloadEntity
import com.example.ui.theme.SaribCardBg
import com.example.ui.theme.SaribCardBgSecondary
import com.example.ui.theme.SaribCardBorder
import com.example.ui.theme.SaribCardBorderSubtle
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribDarkBackground
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribGoldRating
import com.example.ui.theme.SaribLiveRed
import com.example.ui.theme.SaribTextMuted
import com.example.ui.theme.SaribTextPrimary
import com.example.ui.theme.SaribTextSecondary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DownloadsScreen(
    activeDownloads: List<DownloadEntity>,
    completedDownloads: List<DownloadEntity>,
    realtimeProgress: Map<String, DownloadEntity> = emptyMap(),
    storageInfo: SaribDownloadManager.StorageInfo = SaribDownloadManager.StorageInfo(0, 0, 0),
    onPauseDownload: (String) -> Unit,
    onResumeDownload: (DownloadEntity) -> Unit,
    onCancelDownload: (String) -> Unit,
    onDeleteCompletedDownload: (String) -> Unit,
    onPlayOffline: (DownloadEntity) -> Unit,
    onNavigateBack: () -> Unit,
    onBrowseEntertainment: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(if (activeDownloads.isNotEmpty()) 0 else 1) }

    // Merge in-memory realtime progress into active downloads
    val mergedActiveDownloads = remember(activeDownloads, realtimeProgress) {
        if (realtimeProgress.isEmpty()) activeDownloads
        else {
            activeDownloads.map { item ->
                realtimeProgress[item.id] ?: item
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x22FFFFFF))
                                    .testTag("downloads_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "رجوع",
                                    tint = SaribTextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "مركز التنزيلات",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            color = SaribTextPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "أوفلاين",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            color = SaribCyanAccent
                                        )
                                    )
                                }
                                Text(
                                    text = "مشاهدة وتحميل الأفلام والمسلسلات بدون إنترنت",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SaribTextMuted)
                                )
                            }
                        }

                        // Storage Usage Pill
                        Surface(
                            color = SaribCardBgSecondary,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SaribCardBorderSubtle),
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "المستخدم: ${SaribDownloadManager.formatBytes(storageInfo.appUsedBytes)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SaribTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                    if (storageInfo.freeStorageBytes > 0) {
                                        Text(
                                            text = "متاح: ${SaribDownloadManager.formatBytes(storageInfo.freeStorageBytes)}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = SaribTextMuted,
                                                fontSize = 9.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tab Selector: (جاري التنزيل / المكتملة)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SaribCardBgSecondary)
                            .border(1.dp, SaribCardBorderSubtle, RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Tab 0: Active Downloads
                        val isTab0 = selectedTab == 0
                        Surface(
                            color = if (isTab0) SaribElectricBlue else Color.Transparent,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedTab = 0 }
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = if (isTab0) Color.White else SaribTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "جاري التنزيل (${mergedActiveDownloads.size})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isTab0) Color.White else SaribTextSecondary,
                                        fontWeight = if (isTab0) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            }
                        }

                        // Tab 1: Completed Media
                        val isTab1 = selectedTab == 1
                        Surface(
                            color = if (isTab1) SaribElectricBlue else Color.Transparent,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedTab = 1 }
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isTab1) SaribCyanAccent else SaribTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "المكتملة أوفلاين (${completedDownloads.size})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isTab1) Color.White else SaribTextSecondary,
                                        fontWeight = if (isTab1) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedTab == 0) {
                // ================= TAB 0: ACTIVE DOWNLOADS =================
                if (mergedActiveDownloads.isEmpty()) {
                    EmptyDownloadsState(
                        title = "لا توجد تنزيلات جارية حالياً",
                        description = "يمكنك تنزيل أي فيلم أو مسلسل بجودات مختلفة لمشاهدته لاحقاً بدون إنترنت!",
                        buttonText = "تصفح الأفلام والمسلسلات للتنزيل",
                        onButtonClick = onBrowseEntertainment
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(mergedActiveDownloads, key = { it.id }) { item ->
                            ActiveDownloadCard(
                                item = item,
                                onPause = { onPauseDownload(item.id) },
                                onResume = { onResumeDownload(item) },
                                onCancel = { onCancelDownload(item.id) },
                                onOpenExternal = {
                                    SaribDownloadManager.openInExternalDownloader(
                                        context = context,
                                        streamUrl = item.streamUrl,
                                        title = "${item.title} (${item.selectedQuality})"
                                    )
                                }
                            )
                        }
                    }
                }
            } else {
                // ================= TAB 1: COMPLETED DOWNLOADS =================
                if (completedDownloads.isEmpty()) {
                    EmptyDownloadsState(
                        title = "لم تقم بتنزيل أي محتوى بعد",
                        description = "الأفلام والمسلسلات التي تقوم بتنزيلها ستظهر هنا لتتمكن من تشغيلها في أي وقت بدون استهلاك للإنترنت.",
                        buttonText = "استكشاف الأفلام والمسلسلات",
                        onButtonClick = onBrowseEntertainment
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(completedDownloads, key = { it.id }) { item ->
                            CompletedDownloadCard(
                                item = item,
                                onPlay = { onPlayOffline(item) },
                                onDelete = { onDeleteCompletedDownload(item.id) },
                                onShareExternal = {
                                    try {
                                        val file = File(item.localFilePath)
                                        val uri = Uri.fromFile(file)
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "video/*")
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        SaribDownloadManager.openInExternalDownloader(context, item.streamUrl, item.title)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveDownloadCard(
    item: DownloadEntity,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onOpenExternal: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1f,
        animationSpec = tween(120),
        label = "active_card_scale"
    )

    val isPaused = item.status == "PAUSED"
    val isFailed = item.status == "FAILED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.5.dp,
                if (isFocused) SaribCyanAccent else if (isFailed) SaribLiveRed.copy(alpha = 0.6f) else SaribCardBorder,
                RoundedCornerShape(20.dp)
            )
            .testTag("active_download_item_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = SaribCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Row 1: Poster + Titles + Quality Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.posterUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.posterUrl,
                        contentDescription = item.title,
                        modifier = Modifier
                            .size(width = 56.dp, height = 78.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, SaribCardBorder, RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = SaribTextPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (item.subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.labelSmall.copy(color = SaribCyanAccent),
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Quality Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SaribElectricBlue.copy(alpha = 0.3f))
                                .border(0.5.dp, SaribCyanAccent, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.selectedQuality,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SaribCyanAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                        }

                        // Subtitle Badge if present
                        if (item.subtitleName.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x33FFFFFF))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.subtitleName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SaribTextSecondary,
                                        fontSize = 9.sp
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Progress Bar + Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when {
                        isFailed -> "فشل التنزيل: ${item.errorMessage}"
                        isPaused -> "متوقف مؤقتاً"
                        else -> "جاري التنزيل..."
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isFailed) SaribLiveRed else if (isPaused) SaribGoldRating else SaribTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "${item.progress}%",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = SaribCyanAccent,
                        fontWeight = FontWeight.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Animated Progress Bar
            LinearProgressIndicator(
                progress = { (item.progress / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isPaused) SaribGoldRating else if (isFailed) SaribLiveRed else SaribCyanAccent,
                trackColor = Color(0xFF1E2838)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Live Stats (Size downloaded, Speed, ETA)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Size
                Text(
                    text = "${SaribDownloadManager.formatBytes(item.bytesDownloaded)} / ${SaribDownloadManager.formatBytes(item.totalBytes)}",
                    style = MaterialTheme.typography.labelSmall.copy(color = SaribTextMuted, fontSize = 10.sp)
                )

                // Speed
                if (!isPaused && !isFailed && item.speedBps > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = SaribDownloadManager.formatSpeed(item.speedBps),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SaribCyanAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // ETA remaining time
                if (!isPaused && !isFailed && item.etaSeconds > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = SaribGoldRating, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = SaribDownloadManager.formatEta(item.etaSeconds),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SaribGoldRating,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SaribCardBorderSubtle, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Row 4: Controls (Pause/Resume, Cancel, External)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pause / Resume Button
                Surface(
                    color = if (isPaused || isFailed) SaribCyanAccent else Color(0x22FFFFFF),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isPaused) SaribCyanAccent else Color(0x33FFFFFF)),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            if (isPaused || isFailed) onResume() else onPause()
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPaused || isFailed) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            tint = if (isPaused || isFailed) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPaused || isFailed) "استئناف" else "إيقاف مؤقت",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isPaused || isFailed) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // External Downloader Launcher
                Surface(
                    color = Color(0x220088FF),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SaribCyanAccent.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onOpenExternal() }
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تحميل خارجي",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Cancel Button
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SaribLiveRed.copy(alpha = 0.2f))
                        .border(1.dp, SaribLiveRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إلغاء التنزيل",
                        tint = SaribLiveRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CompletedDownloadCard(
    item: DownloadEntity,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
    onShareExternal: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1f,
        animationSpec = tween(120),
        label = "completed_card_scale"
    )

    val formattedDate = remember(item.completedAt) {
        val d = if (item.completedAt > 0) Date(item.completedAt) else Date(item.createdAt)
        SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(d)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.5.dp,
                if (isFocused) SaribCyanAccent else SaribCardBorder,
                RoundedCornerShape(20.dp)
            )
            .clickable { onPlay() }
            .testTag("completed_download_item_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = SaribCardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Poster thumbnail
            Box(
                modifier = Modifier
                    .size(width = 68.dp, height = 96.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, SaribCardBorder, RoundedCornerShape(14.dp))
            ) {
                if (item.posterUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.posterUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SaribCardBgSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(32.dp))
                    }
                }

                // Offline ready badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SaribCyanAccent)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "أوفلاين",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 8.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = SaribTextPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(color = SaribCyanAccent),
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Quality
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SaribElectricBlue.copy(alpha = 0.3f))
                            .border(0.5.dp, SaribCyanAccent, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.selectedQuality,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SaribCyanAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }

                    // Size
                    Text(
                        text = SaribDownloadManager.formatBytes(item.totalBytes),
                        style = MaterialTheme.typography.labelSmall.copy(color = SaribTextMuted, fontSize = 10.sp)
                    )

                    // Date
                    Text(
                        text = "• $formattedDate",
                        style = MaterialTheme.typography.labelSmall.copy(color = SaribTextMuted, fontSize = 10.sp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Play Button (Primary)
                    Button(
                        onClick = onPlay,
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = SaribCyanAccent),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "مشاهدة الآن",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Share / Open External
                    IconButton(
                        onClick = onShareExternal,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x22FFFFFF))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = "مشغل خارجي", tint = SaribTextSecondary, modifier = Modifier.size(16.dp))
                    }

                    // Delete Button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SaribLiveRed.copy(alpha = 0.15f))
                            .border(1.dp, SaribLiveRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف من الجهاز", tint = SaribLiveRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyDownloadsState(
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(SaribElectricBlue.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
                .border(1.5.dp, SaribCyanAccent.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                tint = SaribCyanAccent,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                color = SaribTextPrimary,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall.copy(color = SaribTextMuted),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onButtonClick,
            modifier = Modifier
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = SaribElectricBlue)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Movie, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = buttonText, style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
            }
        }
    }
}
