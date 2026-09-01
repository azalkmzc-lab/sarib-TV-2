package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.EpisodeItem
import com.example.data.model.MediaItem
import com.example.data.model.SeriesDetail
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribDarkBackground
import com.example.ui.theme.SaribDarkCard
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribGoldRating
import com.example.ui.theme.SaribTextMuted
import com.example.ui.theme.SaribTextPrimary
import com.example.ui.theme.SaribTextSecondary

@Composable
fun SeriesDetailScreen(
    mediaItem: MediaItem,
    seriesDetail: SeriesDetail?,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onPlayEpisode: (EpisodeItem, String) -> Unit,
    onPlayDirect: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSeasonIndex by remember { mutableIntStateOf(0) }

    val activeSeasons = seriesDetail?.seasons ?: emptyList()
    val currentSeason = activeSeasons.getOrNull(selectedSeasonIndex)
    val currentEpisodes = currentSeason?.episodes ?: emptyList()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SaribDarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SaribDarkBackground),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Hero Header with Backdrop
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        val backdropUrl = seriesDetail?.backdropUrl?.ifEmpty { seriesDetail.coverUrl }
                            ?: mediaItem.backdropUrl.ifEmpty { mediaItem.posterUrl }

                        AsyncImage(
                            model = backdropUrl,
                            contentDescription = mediaItem.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Gradient protection
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0x80000000),
                                            Color.Transparent,
                                            SaribDarkBackground
                                        )
                                    )
                                )
                        )

                        // Back Button
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.TopStart)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x80000000))
                                .testTag("series_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Info Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = seriesDetail?.title ?: mediaItem.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = SaribTextPrimary,
                                fontWeight = FontWeight.Black
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Rating Badge
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x33FFD700))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = SaribGoldRating,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = seriesDetail?.rating ?: mediaItem.rating,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SaribGoldRating,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            // Year
                            Text(
                                text = seriesDetail?.releaseDate?.take(4) ?: mediaItem.year,
                                style = MaterialTheme.typography.labelSmall.copy(color = SaribTextSecondary)
                            )

                            // Genre
                            Text(
                                text = "•  ${seriesDetail?.genre ?: mediaItem.genre}",
                                style = MaterialTheme.typography.labelSmall.copy(color = SaribCyanAccent)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Plot Story
                        val plotText = seriesDetail?.plot?.ifEmpty { mediaItem.description }
                            ?: mediaItem.description.ifEmpty { "شاهد جميع حلقات ومواسم هذا العمل بجودة فائقة Full HD مع خوادم بث سريعة بدون تقطيع." }
                        Text(
                            text = plotText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = SaribTextMuted,
                                lineHeight = 20.sp
                            ),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Direct Play Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(SaribElectricBlue, SaribCyanAccent)
                                    )
                                )
                                .clickable {
                                    val firstEp = currentEpisodes.firstOrNull()
                                    if (firstEp != null) {
                                        onPlayEpisode(firstEp, "${mediaItem.title} - ${firstEp.title}")
                                    } else {
                                        onPlayDirect()
                                    }
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = if (currentEpisodes.isNotEmpty()) "بدء تشغيل الحلقة 1" else "تشغيل المسلسل",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // Loading State for Episodes
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = SaribCyanAccent,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "جاري جلب قائمة الحلقات من السيرفر...",
                                    style = MaterialTheme.typography.bodySmall.copy(color = SaribTextMuted)
                                )
                            }
                        }
                    }
                } else if (activeSeasons.isNotEmpty()) {
                    // Seasons Tabs Row
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "المواسم والحلقات (${activeSeasons.size} مواسم)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )

                            ScrollableTabRow(
                                selectedTabIndex = selectedSeasonIndex.coerceIn(0, activeSeasons.lastIndex),
                                containerColor = SaribDarkBackground,
                                contentColor = SaribCyanAccent,
                                edgePadding = 16.dp,
                                indicator = { tabPositions ->
                                    if (selectedSeasonIndex < tabPositions.size) {
                                        TabRowDefaults.SecondaryIndicator(
                                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSeasonIndex]),
                                            color = SaribCyanAccent
                                        )
                                    }
                                },
                                divider = {}
                            ) {
                                activeSeasons.forEachIndexed { index, season ->
                                    Tab(
                                        selected = selectedSeasonIndex == index,
                                        onClick = { selectedSeasonIndex = index },
                                        text = {
                                            Text(
                                                text = season.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (selectedSeasonIndex == index) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (selectedSeasonIndex == index) SaribCyanAccent else SaribTextMuted
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    // Episodes List
                    if (currentEpisodes.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "لا توجد حلقات متاحة في هذا الموسم حالياً",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = SaribTextMuted)
                                )
                            }
                        }
                    } else {
                        items(currentEpisodes) { ep ->
                            EpisodeCardItem(
                                episode = ep,
                                seriesTitle = mediaItem.title,
                                onClick = {
                                    onPlayEpisode(ep, "${mediaItem.title} - ${ep.title}")
                                }
                            )
                        }
                    }
                } else {
                    // Fallback when no seasons are returned
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "تم تجهيز مشغل البث المباشر لهذا العمل",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = SaribTextMuted)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EpisodeCardItem(
    episode: EpisodeItem,
    seriesTitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SaribDarkCard)
            .border(1.dp, Color(0x2200D2FF), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail / Icon
            Box(
                modifier = Modifier
                    .size(width = 90.dp, height = 55.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (episode.coverUrl.isNotBlank()) {
                    AsyncImage(
                        model = episode.coverUrl,
                        contentDescription = episode.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0x99000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "تشغيل",
                        tint = SaribCyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Episode Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = episode.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الحلقة ${episode.episodeNum}",
                        style = MaterialTheme.typography.labelSmall.copy(color = SaribCyanAccent)
                    )
                    if (episode.duration.isNotBlank()) {
                        Text(
                            text = "• ${episode.duration}",
                            style = MaterialTheme.typography.labelSmall.copy(color = SaribTextMuted)
                        )
                    }
                }
            }
        }
    }
}
