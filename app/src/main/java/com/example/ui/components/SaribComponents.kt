package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ChannelCategory
import com.example.data.model.ChannelItem
import com.example.data.model.HeroBannerItem
import com.example.data.model.MatchItem
import com.example.data.model.MediaItem
import com.example.ui.theme.SaribCardBg
import com.example.ui.theme.SaribCardBgSecondary
import com.example.ui.theme.SaribCardBorder
import com.example.ui.theme.SaribCardBorderSubtle
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribDarkSurface
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribGoldRating
import com.example.ui.theme.SaribLiveRed
import com.example.ui.theme.SaribTextMuted
import com.example.ui.theme.SaribTextPrimary
import com.example.ui.theme.SaribTextSecondary
import com.example.ui.theme.SaribTop01
import com.example.ui.theme.SaribTop02
import com.example.ui.theme.SaribTop03
import com.example.util.StreamUrlParser
import kotlinx.coroutines.delay

@Composable
fun SaribTopHeader(
    onMenuClick: () -> Unit,
    onTelegramClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onDownloadsClick: (() -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null,
    onRefreshClick: (() -> Unit)? = null,
    isRefreshing: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left/Start: Menu + Brand Title
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier
                            .testTag("menu_button")
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "القائمة",
                            tint = SaribTextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Image(
                        painter = painterResource(id = R.drawable.ic_sarib_logo),
                        contentDescription = "شعار SARIB TV",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "SARIB",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SaribTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "TV",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SaribCyanAccent
                        )
                    )
                }

                // Right/End: Clean Icon Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (onSearchClick != null) {
                        IconButton(
                            onClick = onSearchClick,
                            modifier = Modifier
                                .testTag("header_search_button")
                                .size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "البحث",
                                tint = SaribTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (onRefreshClick != null) {
                        val spinRotation by rememberInfiniteTransition(label = "refresh_spin").animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "spin_anim"
                        )
                        IconButton(
                            onClick = onRefreshClick,
                            modifier = Modifier
                                .testTag("header_refresh_button")
                                .size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "تحديث محتوى التطبيق",
                                tint = if (isRefreshing) SaribCyanAccent else SaribTextSecondary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .graphicsLayer {
                                        if (isRefreshing) rotationZ = spinRotation
                                    }
                            )
                        }
                    }

                    IconButton(
                        onClick = onTelegramClick,
                        modifier = Modifier
                            .testTag("telegram_button")
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "تيليجرام",
                            tint = SaribCyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (onDownloadsClick != null) {
                        IconButton(
                            onClick = onDownloadsClick,
                            modifier = Modifier
                                .testTag("header_downloads_button")
                                .size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "التنزيلات",
                                tint = SaribTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onFavoritesClick,
                        modifier = Modifier
                            .testTag("header_favorites_button")
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = SaribTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            HorizontalDivider(color = SaribCardBorderSubtle, thickness = 1.dp)
        }
    }
}

@Composable
fun CategoryChipsRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories, key = { it }) { category ->
            val isSelected = category == selectedCategory
            val bgColor = if (isSelected) SaribElectricBlue else SaribCardBg
            val textColor = if (isSelected) Color.White else SaribTextSecondary
            val borderColor = if (isSelected) Color.Transparent else SaribCardBorderSubtle

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = androidx.compose.material3.ripple(bounded = true, color = SaribCyanAccent)
                    ) { onCategorySelected(category) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                )
            }
        }
    }
}

@Composable
fun HeroSlider(
    sliders: List<HeroBannerItem>,
    onWatchClick: (HeroBannerItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (sliders.isEmpty()) return

    val pageCount = sliders.size
    val pagerState = rememberPagerState(pageCount = { pageCount })
    var isMuted by remember { mutableStateOf(true) }

    LaunchedEffect(pagerState, pageCount) {
        if (pageCount > 1) {
            while (true) {
                delay(10000)
                val nextPage = (pagerState.currentPage + 1) % pageCount
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, SaribCardBorderSubtle, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SaribCardBg)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val currentItem = sliders.getOrNull(page) ?: sliders.first()
                val isCurrentActivePage = pagerState.currentPage == page

                if (currentItem.isMatchSlider || currentItem.contentType == com.example.data.model.ContentType.MATCH || currentItem.homeTeam.isNotBlank()) {
                    HeroMatchSliderContent(
                        item = currentItem,
                        isActive = isCurrentActivePage,
                        isMuted = isMuted,
                        onMuteToggle = { isMuted = !isMuted },
                        onWatchClick = { onWatchClick(currentItem) }
                    )
                } else {
                    HeroStandardSliderContent(
                        item = currentItem,
                        isActive = isCurrentActivePage,
                        isMuted = isMuted,
                        onMuteToggle = { isMuted = !isMuted },
                        onWatchClick = { onWatchClick(currentItem) }
                    )
                }
            }
        }

        if (pageCount > 1) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pageCount) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width = if (isSelected) 18.dp else 6.dp
                    val color = if (isSelected) SaribCyanAccent else SaribCardBorder
                    Box(
                        modifier = Modifier
                            .height(5.dp)
                            .width(width)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroStandardSliderContent(
    item: HeroBannerItem,
    isActive: Boolean,
    isMuted: Boolean,
    onMuteToggle: () -> Unit,
    onWatchClick: () -> Unit
) {
    val videoUrl = item.streamUrl.ifBlank { item.server1 }
    val posterImage = item.posterUrl.ifBlank { item.backdropUrl }
    val backdropImage = item.backdropUrl.ifBlank { item.posterUrl }

    Box(modifier = Modifier.fillMaxSize()) {
        if (backdropImage.isNotBlank()) {
            AsyncImage(
                model = backdropImage,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.hero_lost_town),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (posterImage.isNotBlank() && posterImage != backdropImage) {
            AsyncImage(
                model = posterImage,
                contentDescription = item.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }

        if (videoUrl.isNotBlank() && isActive) {
            HeroSliderVideoBackground(
                streamUrl = videoUrl,
                isActive = isActive,
                isMuted = isMuted,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x440F141C),
                            Color(0x660F141C),
                            Color(0xEE0F141C)
                        )
                    )
                )
        )

        // Top Header Badge & Mute Control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 10.dp, start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xCC171E29))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.isLive) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(SaribLiveRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = if (item.badge.isNotBlank()) "${item.badge} • ${item.subtitle}" else item.subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SaribTextPrimary,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (videoUrl.isNotBlank()) {
                IconButton(
                    onClick = onMuteToggle,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xAA171E29))
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = if (isMuted) "تشغيل الصوت" else "كتم الصوت",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Bottom Title & Clean Action Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .testTag("hero_watch_button")
                    .clip(RoundedCornerShape(10.dp))
                    .background(SaribElectricBlue)
                    .clickable { onWatchClick() }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (item.isLive) "مشاهدة" else stringResource(id = R.string.watch_now),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMatchSliderContent(
    item: HeroBannerItem,
    isActive: Boolean,
    isMuted: Boolean,
    onMuteToggle: () -> Unit,
    onWatchClick: () -> Unit
) {
    val videoUrl = item.streamUrl.ifBlank { item.server1 }
    val backdropImage = item.backdropUrl.ifBlank { item.posterUrl }

    Box(modifier = Modifier.fillMaxSize()) {
        if (backdropImage.isNotBlank()) {
            AsyncImage(
                model = backdropImage,
                contentDescription = "${item.homeTeam} vs ${item.awayTeam}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SaribCardBgSecondary)
            )
        }

        if (videoUrl.isNotBlank() && isActive) {
            HeroSliderVideoBackground(
                streamUrl = videoUrl,
                isActive = isActive,
                isMuted = isMuted,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x990F141C),
                            Color(0xBB0F141C),
                            Color(0xF50F141C)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top League & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xCC171E29))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.leagueLogoUrl.isNotBlank()) {
                        AsyncImage(
                            model = item.leagueLogoUrl,
                            contentDescription = item.leagueName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = SaribCyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = item.leagueName.ifBlank { "مباراة اليوم" },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (item.isLive) SaribLiveRed else Color(0xCC171E29))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.isLive) {
                        Text(
                            text = "مباشر",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = SaribTextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val timeDisplay = when {
                            item.matchTime.isNotBlank() -> item.matchTime
                            item.matchDate.isNotBlank() -> item.matchDate
                            else -> "قريباً"
                        }
                        Text(
                            text = timeDisplay,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // Center Match Teams
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SaribCardBg)
                            .border(1.dp, SaribCardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.homeLogoUrl.isNotBlank()) {
                            AsyncImage(
                                model = item.homeLogoUrl,
                                contentDescription = item.homeTeam,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = null,
                                tint = SaribTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.homeTeam.ifBlank { "الفريق المضيف" },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    if (item.isLive) {
                        Text(
                            text = "${item.homeScore} - ${item.awayScore}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaribCyanAccent
                            )
                        )
                    } else {
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaribTextSecondary
                            )
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SaribCardBg)
                            .border(1.dp, SaribCardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.awayLogoUrl.isNotBlank()) {
                            AsyncImage(
                                model = item.awayLogoUrl,
                                contentDescription = item.awayTeam,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = null,
                                tint = SaribTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.awayTeam.ifBlank { "الفريق الضيف" },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Bottom Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val detailsText = listOfNotNull(
                    item.channelName.takeIf { it.isNotBlank() },
                    item.commentator.takeIf { it.isNotBlank() }
                ).joinToString(" • ")

                Text(
                    text = detailsText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SaribTextSecondary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .testTag("match_watch_button")
                        .clip(RoundedCornerShape(10.dp))
                        .background(SaribElectricBlue)
                        .clickable { onWatchClick() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (item.isLive) "مشاهدة" else "التفاصيل",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeroSliderVideoBackground(
    streamUrl: String,
    isActive: Boolean,
    isMuted: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlayerReady by remember { mutableStateOf(false) }

    LaunchedEffect(isMuted, exoPlayer) {
        exoPlayer?.volume = if (isMuted) 0f else 1f
    }

    LaunchedEffect(streamUrl, isActive) {
        if (isActive && streamUrl.isNotBlank()) {
            delay(3500)
            if (!isActive) return@LaunchedEffect
            try {
                val player = ExoPlayer.Builder(context).build().apply {
                    repeatMode = Player.REPEAT_MODE_ONE
                    playWhenReady = true
                    volume = if (isMuted) 0f else 1f
                    videoScalingMode = androidx.media3.common.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                }
                val listener = object : Player.Listener {
                    override fun onRenderedFirstFrame() {
                        isPlayerReady = true
                    }
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            isPlayerReady = true
                        }
                    }
                }
                player.addListener(listener)

                val parsed = StreamUrlParser.parse(streamUrl)
                val httpFactory = DefaultHttpDataSource.Factory()
                    .setUserAgent(parsed.userAgent ?: StreamUrlParser.DEFAULT_USER_AGENT)
                    .setAllowCrossProtocolRedirects(true)
                    .setConnectTimeoutMs(6000)
                    .setReadTimeoutMs(6000)
                StreamUrlParser.configureHttpDataSource(httpFactory, parsed)

                val mediaSourceFactory = DefaultMediaSourceFactory(httpFactory, StreamUrlParser.createExtractorsFactory())
                val drmManager = StreamUrlParser.createDrmSessionManager(parsed)
                if (drmManager != null) {
                    mediaSourceFactory.setDrmSessionManagerProvider { drmManager }
                }

                val mediaItemBuilder = androidx.media3.common.MediaItem.Builder().setUri(parsed.cleanUrl)
                parsed.mimeType?.let { mediaItemBuilder.setMimeType(it) }

                val mediaSource = mediaSourceFactory.createMediaSource(mediaItemBuilder.build())
                player.setMediaSource(mediaSource)
                player.prepare()
                player.play()
                exoPlayer = player
            } catch (e: Exception) {
                isPlayerReady = false
            }
        } else {
            isPlayerReady = false
            exoPlayer?.stop()
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            isPlayerReady = false
            exoPlayer?.stop()
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    if (exoPlayer != null) {
        Box(modifier = modifier.alpha(if (isPlayerReady) 1f else 0f)) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    }
                },
                update = { playerView ->
                    if (playerView.player != exoPlayer) {
                        playerView.player = exoPlayer
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun MainCategoriesRoundGrid(
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember {
        listOf(
            Triple("القنوات", Icons.Default.Tv, "channels"),
            Triple("المباريات", Icons.Default.SportsSoccer, "matches"),
            Triple("الأخبار", Icons.AutoMirrored.Filled.Article, "news"),
            Triple("الأفلام", Icons.Default.Movie, "movies"),
            Triple("المسلسلات", Icons.Default.VideoLibrary, "series")
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (title, icon, route) ->
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = androidx.compose.material3.ripple(bounded = true, color = SaribCyanAccent)
                    ) { onCategoryClick(route) },
                color = SaribCardBg,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SaribCardBorderSubtle)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = SaribCyanAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SaribTextPrimary,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = SaribTextPrimary
            )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onViewAllClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.view_all),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = SaribCyanAccent,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = SaribCyanAccent,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun ChannelCardItem(
    channel: ChannelItem,
    onClick: (ChannelItem) -> Unit,
    onFavoriteToggle: (ChannelItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(110.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, SaribCardBorderSubtle, RoundedCornerShape(14.dp))
            .clickable { onClick(channel) },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SaribCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SaribDarkSurface),
                contentAlignment = Alignment.Center
            ) {
                if (channel.logoUrl.isNotBlank()) {
                    AsyncImage(
                        model = channel.logoUrl,
                        contentDescription = channel.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = SaribTextSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(3.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0x88000000))
                        .clickable { onFavoriteToggle(channel) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (channel.isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "المفضلة",
                        tint = if (channel.isFavorite) SaribLiveRed else Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = channel.name,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = SaribTextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MatchCardItem(
    match: MatchItem,
    onClick: (MatchItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = if (isFocused) SaribCyanAccent else if (match.isLive) SaribLiveRed.copy(alpha = 0.4f) else SaribCardBorderSubtle,
                shape = RoundedCornerShape(14.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp && (
                    keyEvent.key == Key.DirectionCenter ||
                    keyEvent.key == Key.Enter ||
                    keyEvent.key == Key.NumPadEnter ||
                    keyEvent.key == Key.ButtonA
                )) {
                    onClick(match)
                    true
                } else false
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(bounded = true, color = SaribCyanAccent)
            ) { onClick(match) },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) SaribCardBgSecondary else SaribCardBg
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: League & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (match.leagueIconUrl.isNotBlank()) {
                        AsyncImage(
                            model = match.leagueIconUrl,
                            contentDescription = match.leagueName,
                            modifier = Modifier.size(16.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = match.leagueName.ifBlank { "مباريات اليوم" },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SaribTextSecondary,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (match.isLive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SaribLiveRed.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "مباشر",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SaribLiveRed,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            )
                        )
                    }
                } else {
                    Text(
                        text = if (match.status.isNotBlank() && !match.status.contains("-")) match.status else match.matchDate.ifBlank { "لم تبدأ" },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SaribTextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Middle Teams & Score/Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SaribDarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        if (match.homeLogoUrl.isNotBlank()) {
                            AsyncImage(
                                model = match.homeLogoUrl,
                                contentDescription = match.homeTeam,
                                modifier = Modifier.size(26.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(
                                text = match.homeTeam.take(1),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = SaribCyanAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = match.homeTeam,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = SaribTextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Score / Time Pill
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SaribDarkSurface)
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val scoreOrTime = if (match.isLive || match.status.contains("-") || match.homeScore > 0 || match.awayScore > 0) {
                        if (match.status.contains("-")) match.status else "${match.homeScore} - ${match.awayScore}"
                    } else {
                        match.matchTime.ifBlank { "VS" }
                    }

                    Text(
                        text = scoreOrTime,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = if (match.isLive) SaribCyanAccent else SaribTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Away Team
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = match.awayTeam,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = SaribTextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SaribDarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        if (match.awayLogoUrl.isNotBlank()) {
                            AsyncImage(
                                model = match.awayLogoUrl,
                                contentDescription = match.awayTeam,
                                modifier = Modifier.size(26.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(
                                text = match.awayTeam.take(1),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = SaribElectricBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Bottom Metadata
            if (match.channelName.isNotBlank() || match.commentator.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = SaribCardBorderSubtle, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (match.channelName.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = SaribTextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = match.channelName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SaribTextSecondary,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }

                    if (match.commentator.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = SaribTextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = match.commentator,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SaribTextSecondary,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MediaCardItem(
    item: MediaItem,
    onClick: (MediaItem) -> Unit,
    onFavoriteToggle: ((MediaItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val displayImageUrl = item.posterUrl.ifBlank { item.backdropUrl }
    var isFocused by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .width(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = if (isFocused) SaribCyanAccent else SaribCardBorderSubtle,
                shape = RoundedCornerShape(12.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp && (
                    keyEvent.key == Key.DirectionCenter ||
                    keyEvent.key == Key.Enter ||
                    keyEvent.key == Key.NumPadEnter ||
                    keyEvent.key == Key.ButtonA
                )) {
                    onClick(item)
                    true
                } else false
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(bounded = true, color = SaribCyanAccent)
            ) { onClick(item) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) SaribCardBgSecondary else SaribCardBg
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .background(SaribDarkSurface)
            ) {
                if (displayImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = displayImageUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.hero_lost_town),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (item.isTop) {
                    val badgeColor = when (item.topRank) {
                        "01" -> SaribTop01
                        "02" -> SaribTop02
                        else -> SaribTop03
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "TOP ${item.topRank}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                } else if (item.rating.isNotBlank() && item.rating != "0.0") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xBB000000))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⭐", fontSize = 9.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = item.rating,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SaribGoldRating,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                if (onFavoriteToggle != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0x99000000))
                            .clickable { onFavoriteToggle(item) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = if (item.isFavorite) SaribLiveRed else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Clean Title Bar Below Poster
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (isFocused) SaribCyanAccent else SaribTextPrimary,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.year.isNotBlank()) {
                    Text(
                        text = item.year,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SaribTextMuted,
                            fontSize = 10.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun LargeChannelCategoryCard(
    category: ChannelCategory,
    onClick: (ChannelCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = if (isFocused) SaribCyanAccent else SaribCardBorderSubtle,
                shape = RoundedCornerShape(14.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp && (
                    keyEvent.key == Key.DirectionCenter ||
                    keyEvent.key == Key.Enter ||
                    keyEvent.key == Key.NumPadEnter ||
                    keyEvent.key == Key.ButtonA
                )) {
                    onClick(category)
                    true
                } else false
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(bounded = true, color = SaribCyanAccent)
            ) { onClick(category) },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) SaribCardBgSecondary else SaribCardBg
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SaribDarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (category.categoryType) {
                            "sports" -> Icons.Default.SportsSoccer
                            "movies" -> Icons.Default.Movie
                            else -> Icons.Default.Tv
                        },
                        contentDescription = null,
                        tint = SaribCyanAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = SaribTextPrimary
                        )
                    )
                    if (category.subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = category.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SaribTextSecondary
                            )
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = SaribTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SaribBottomNav(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember {
        listOf(
            Triple("home", Icons.Outlined.Home, "home"),
            Triple("channels", Icons.Outlined.LiveTv, "channels"),
            Triple("entertainment", Icons.Outlined.VideoLibrary, "entertainment"),
            Triple("favorites", Icons.Default.FavoriteBorder, "favorites")
        )
    }

    val navBg = MaterialTheme.colorScheme.surface

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = navBg
    ) {
        Column {
            HorizontalDivider(color = SaribCardBorderSubtle, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { (labelKey, icon, tabKey) ->
                    val label = com.example.data.local.tr(labelKey)
                    val isSelected = currentTab == tabKey
                    var isItemFocused by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isSelected -> SaribElectricBlue.copy(alpha = 0.15f)
                                    isItemFocused -> SaribElectricBlue.copy(alpha = 0.1f)
                                    else -> Color.Transparent
                                }
                            )
                            .onFocusChanged { isItemFocused = it.isFocused }
                            .focusable()
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyUp && (
                                    keyEvent.key == Key.DirectionCenter ||
                                    keyEvent.key == Key.Enter ||
                                    keyEvent.key == Key.NumPadEnter ||
                                    keyEvent.key == Key.ButtonA
                                )) {
                                    onTabSelected(tabKey)
                                    true
                                } else false
                            }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = androidx.compose.material3.ripple(bounded = true, color = SaribCyanAccent)
                            ) {
                                onTabSelected(tabKey)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected || isItemFocused) SaribCyanAccent else SaribTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected || isItemFocused) SaribCyanAccent else SaribTextMuted,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButtonCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = if (isFocused || isActive) SaribCyanAccent else SaribCardBorderSubtle,
                shape = RoundedCornerShape(12.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp && (
                    keyEvent.key == Key.DirectionCenter ||
                    keyEvent.key == Key.Enter ||
                    keyEvent.key == Key.NumPadEnter ||
                    keyEvent.key == Key.ButtonA
                )) {
                    onClick()
                    true
                } else false
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(bounded = true, color = SaribCyanAccent)
            ) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused || isActive) SaribElectricBlue.copy(alpha = 0.15f) else SaribCardBg
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isFocused || isActive) SaribCyanAccent else SaribTextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isFocused || isActive) SaribCyanAccent else SaribTextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
