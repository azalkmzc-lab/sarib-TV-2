package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.ContentType
import com.example.data.model.MediaItem
import com.example.data.model.getActiveServers
import com.example.ui.theme.SaribCardBg
import com.example.ui.theme.SaribCardBgSecondary
import com.example.ui.theme.SaribCardBorder
import com.example.ui.theme.SaribCardBorderSubtle
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribDarkBackground
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribGoldRating
import com.example.ui.theme.SaribSuccessGreen
import com.example.ui.theme.SaribTextMuted
import com.example.ui.theme.SaribTextPrimary
import com.example.ui.theme.SaribTextSecondary

@Composable
fun MovieDetailsDialog(
    media: MediaItem,
    onDismissRequest: () -> Unit,
    onPlayMovie: (MediaItem) -> Unit,
    onDownloadMovie: (MediaItem) -> Unit,
    onToggleFavorite: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeServers = remember(media) { media.getActiveServers() }
    val displayPoster = media.backdropUrl.ifBlank { media.posterUrl }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(26.dp))
                .border(1.5.dp, SaribCyanAccent.copy(alpha = 0.6f), RoundedCornerShape(26.dp))
                .testTag("movie_details_dialog"),
            colors = CardDefaults.cardColors(containerColor = SaribDarkBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Hero Backdrop & Badges
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .background(Color(0xFF0C1929))
                ) {
                    if (displayPoster.isNotBlank()) {
                        AsyncImage(
                            model = displayPoster,
                            contentDescription = media.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Multi-layer Gradient Protection
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x99000000),
                                        Color.Transparent,
                                        SaribDarkBackground
                                    )
                                )
                            )
                    )

                    // Top Action Bar inside Poster (Close & Favorite)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xCC05101E))
                                .border(1.dp, SaribCardBorderSubtle, CircleShape)
                                .testTag("movie_dialog_close")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { onToggleFavorite(media) },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xCC05101E))
                                .border(1.dp, SaribCardBorderSubtle, CircleShape)
                                .testTag("movie_dialog_favorite")
                        ) {
                            Icon(
                                imageVector = if (media.isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "المفضلة",
                                tint = if (media.isFavorite) Color(0xFFFF2A4B) else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Play Overlay Icon in Center
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(SaribElectricBlue.copy(alpha = 0.85f))
                            .border(2.dp, SaribCyanAccent, CircleShape)
                            .clickable { onPlayMovie(media) }
                            .testTag("movie_dialog_poster_play"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "تشغيل الفيلم",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Quality Badge at Bottom
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SaribElectricBlue.copy(alpha = 0.9f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HighQuality,
                                contentDescription = null,
                                tint = SaribCyanAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "FHD 1080p سينمائي",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Content Details Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    // Movie Title
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = SaribTextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 21.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Metadata Row: Year • Genre • Rating
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (media.rating.isNotBlank() && media.rating != "0.0") {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x33FFB300),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SaribGoldRating.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = SaribGoldRating,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${media.rating} / 10",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SaribGoldRating,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        if (media.year.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SaribCardBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SaribCardBorderSubtle)
                            ) {
                                Text(
                                    text = media.year,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SaribCyanAccent,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        val typeLabel = if (media.type == ContentType.SERIES) "مسلسل" else "فيلم"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SaribCardBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SaribCardBorderSubtle)
                        ) {
                            Text(
                                text = typeLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SaribTextSecondary,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        if (media.genre.isNotBlank()) {
                            Text(
                                text = media.genre,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SaribTextMuted,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Description / Synopsis
                    val description = if (media.description.isNotBlank()) {
                        media.description
                    } else {
                        "استمتع بمشاهدة وتحميل ${media.title} بجودة فائقة الوضوح مع دعم المشاهدة المباشرة والتحميل فائق السرعة بدون إنترنت."
                    }
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = SaribTextSecondary,
                            lineHeight = 22.sp,
                            fontSize = 13.sp
                        )
                    )

                    // Real Server info
                    if (activeServers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SaribCardBg)
                                .border(1.dp, SaribCardBorderSubtle, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = SaribCyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "السيرفرات الحقيقية المتاحة: ${activeServers.size} سيرفر شغال",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SaribTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ==========================================
                    // TWO PRIMARY PROMINENT ACTION BUTTONS:
                    // 1. PLAY NOW (تشغيل الفيلم)
                    // 2. DOWNLOAD (تحميل الفيلم بدون نت)
                    // ==========================================

                    // 1. MAIN PLAY BUTTON
                    Button(
                        onClick = { onPlayMovie(media) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("movie_dialog_btn_play"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SaribElectricBlue
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تشغيل الفيلم الآن",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. MAIN DOWNLOAD BUTTON
                    Button(
                        onClick = { onDownloadMovie(media) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .border(1.dp, SaribCyanAccent, RoundedCornerShape(14.dp))
                            .testTag("movie_dialog_btn_download"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0C273B)
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = SaribCyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تحميل الفيلم للمشاهدة بدون نت",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = SaribCyanAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
