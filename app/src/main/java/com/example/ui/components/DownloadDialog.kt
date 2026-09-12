package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.download.SaribDownloadManager
import com.example.ui.theme.SaribCardBg
import com.example.ui.theme.SaribCardBgSecondary
import com.example.ui.theme.SaribCardBorder
import com.example.ui.theme.SaribCardBorderSubtle
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribGoldRating
import com.example.ui.theme.SaribTextMuted
import com.example.ui.theme.SaribTextPrimary
import com.example.ui.theme.SaribTextSecondary

data class DownloadQualityItem(
    val label: String,
    val description: String,
    val badge: String,
    val streamUrlOverride: String = ""
)

data class DownloadSubtitleItem(
    val label: String,
    val description: String,
    val subUrl: String = ""
)

@Composable
fun DownloadDialog(
    title: String,
    subtitle: String = "",
    posterUrl: String = "",
    streamUrl: String,
    servers: List<Pair<String, String>> = emptyList(),
    contentType: String = "MOVIE",
    onDismiss: () -> Unit,
    onStartInternalDownload: (quality: String, subUrl: String, subName: String, resolvedUrl: String) -> Unit,
    onOpenExternalDownloader: (streamUrl: String, title: String) -> Unit = { url, t -> },
    onNavigateToDownloads: (() -> Unit)? = null
) {
    val context = LocalContext.current

    val qualityOptions = remember(servers, streamUrl) {
        val list = mutableListOf<DownloadQualityItem>()
        if (servers.isNotEmpty()) {
            servers.forEachIndexed { index, pair ->
                val badgeName = if (index == 0) "الأساسي" else "سيرفر ${index + 1}"
                list.add(
                    DownloadQualityItem(
                        label = pair.first,
                        description = "سيرفر بث حقيقي مباشر",
                        badge = badgeName,
                        streamUrlOverride = pair.second
                    )
                )
            }
        } else {
            list.add(
                DownloadQualityItem(
                    label = "البث المباشر الحقيقي (Direct Stream)",
                    description = "تنزيل الملف بأعلى جودة متوفرة من السيرفر الأصلي",
                    badge = "الجودة المتاحة",
                    streamUrlOverride = streamUrl
                )
            )
        }
        list
    }

    val subtitleOptions = remember {
        listOf(
            DownloadSubtitleItem("الترجمة والصوت المدمج بالملف (Embedded Audio/Subs)", "تشغيل مسارات الصوت والترجمة المدمجة تلقائياً في مشغل التطبيق", ""),
            DownloadSubtitleItem("الصوت الأصلي المباشر فقط", "تحميل الفيديو بدون مسار ترجمة خارجي إضافي", "")
        )
    }

    var selectedQualityIndex by remember { mutableIntStateOf(0) }
    var selectedSubtitleIndex by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(26.dp))
                .border(1.5.dp, SaribCyanAccent.copy(alpha = 0.7f), RoundedCornerShape(26.dp))
                .testTag("download_dialog"),
            colors = CardDefaults.cardColors(containerColor = SaribCardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Header: Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SaribElectricBlue.copy(alpha = 0.3f))
                                .border(1.dp, SaribCyanAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = SaribCyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "خيارات تحميل الفيديو",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = SaribTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "اختر الجودة وطريقة التحميل المفضلة",
                                style = MaterialTheme.typography.labelSmall.copy(color = SaribTextMuted)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = SaribTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = SaribCardBorderSubtle, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Media Preview Snippet Card
                Surface(
                    color = SaribCardBgSecondary,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SaribCardBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (posterUrl.isNotBlank()) {
                            AsyncImage(
                                model = posterUrl,
                                contentDescription = title,
                                modifier = Modifier
                                    .size(width = 54.dp, height = 75.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, SaribCardBorder, RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = SaribTextPrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (subtitle.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.labelSmall.copy(color = SaribCyanAccent),
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SaribElectricBlue)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (contentType == "SERIES" || contentType == "EPISODE") "حلقة مسلسل" else "فيلم سينمائي",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "متاح للمشاهدة بدون إنترنت",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SaribTextMuted, fontSize = 10.sp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 1: Video Quality & Server Picker
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HighQuality, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "1. اختر جودة الفيديو والسيرفر:",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = SaribTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    qualityOptions.forEachIndexed { index, option ->
                        val isSelected = selectedQualityIndex == index
                        val borderC by animateColorAsState(
                            targetValue = if (isSelected) SaribCyanAccent else SaribCardBorder,
                            label = "qualityBorder"
                        )
                        val bgC by animateColorAsState(
                            targetValue = if (isSelected) SaribElectricBlue.copy(alpha = 0.25f) else Color(0x15FFFFFF),
                            label = "qualityBg"
                        )

                        Surface(
                            color = bgC,
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderC),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedQualityIndex = index }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) SaribCyanAccent else Color.Transparent)
                                            .border(1.5.dp, if (isSelected) SaribCyanAccent else SaribTextMuted, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = option.label,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = if (isSelected) Color.White else SaribTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = option.description,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = SaribTextMuted,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) SaribCyanAccent.copy(alpha = 0.2f) else Color(0x33000000))
                                        .border(0.5.dp, if (isSelected) SaribCyanAccent else SaribCardBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = option.badge,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) SaribCyanAccent else SaribTextSecondary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 2: Subtitle Options
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Subtitles, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "2. ترجمة الفيديو (الترجمة العربية):",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = SaribTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    subtitleOptions.forEachIndexed { index, option ->
                        val isSelected = selectedSubtitleIndex == index
                        val borderC by animateColorAsState(
                            targetValue = if (isSelected) SaribCyanAccent else SaribCardBorder,
                            label = "subBorder"
                        )
                        val bgC by animateColorAsState(
                            targetValue = if (isSelected) SaribElectricBlue.copy(alpha = 0.25f) else Color(0x15FFFFFF),
                            label = "subBg"
                        )

                        Surface(
                            color = bgC,
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderC),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedSubtitleIndex = index }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) SaribCyanAccent else Color.Transparent)
                                        .border(1.5.dp, if (isSelected) SaribCyanAccent else SaribTextMuted, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isSelected) Color.White else SaribTextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = option.description,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SaribTextMuted,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = SaribCardBorderSubtle, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 3: Action Buttons
                val resolvedStreamUrl = qualityOptions.getOrNull(selectedQualityIndex)?.streamUrlOverride?.ifBlank { streamUrl } ?: streamUrl
                val selectedQualityLabel = qualityOptions.getOrNull(selectedQualityIndex)?.badge ?: "1080p FHD"
                val selectedSub = subtitleOptions.getOrNull(selectedSubtitleIndex)

                // 1. In-App Download Button (Primary Gradient)
                Button(
                    onClick = {
                        onDismiss()
                        onStartInternalDownload(
                            selectedQualityLabel,
                            selectedSub?.subUrl ?: "",
                            selectedSub?.label ?: "ترجمة مدمجة",
                            resolvedStreamUrl
                        )
                        onNavigateToDownloads?.invoke()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .testTag("start_in_app_download_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SaribCyanAccent)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "بدء التنزيل داخل التطبيق (أوفلاين)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Black,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. External Downloader Button (1DM / ADM / Browser)
                Surface(
                    color = Color(0x330088FF),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SaribCyanAccent.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            onDismiss()
                            SaribDownloadManager.openInExternalDownloader(
                                context = context,
                                streamUrl = resolvedStreamUrl,
                                title = "$title ($selectedQualityLabel)"
                            )
                            onOpenExternalDownloader(resolvedStreamUrl, title)
                        }
                        .padding(vertical = 12.dp)
                        .testTag("open_external_downloader_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = SaribCyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تحميل عبر تطبيق خارجي (1DM / ADM / المتصفح)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Copy Direct Link Button
                OutlinedButton(
                    onClick = {
                        SaribDownloadManager.copyToClipboard(context, resolvedStreamUrl, title)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SaribTextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SaribCardBorder)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = SaribTextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("نسخ رابط التحميل المباشر", style = MaterialTheme.typography.labelMedium.copy(color = SaribTextSecondary))
                    }
                }
            }
        }
    }
}
