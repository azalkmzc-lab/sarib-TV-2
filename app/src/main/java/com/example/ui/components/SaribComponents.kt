package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.util.StreamUrlParser
import kotlinx.coroutines.delay
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.focusable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import com.example.R
import com.example.data.model.ChannelCategory
import com.example.data.model.ChannelItem
import com.example.data.model.HeroBannerItem
import com.example.data.model.MatchItem
import com.example.data.model.MediaItem
import com.example.ui.theme.SaribBlueGlow
import com.example.ui.theme.SaribCardBg
import com.example.ui.theme.SaribCardBgSecondary
import com.example.ui.theme.SaribCardBorder
import com.example.ui.theme.SaribCardBorderSubtle
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribDarkBackground
import com.example.ui.theme.SaribDarkSurface
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribHeaderGradientEnd
import com.example.ui.theme.SaribHeaderGradientStart
import com.example.ui.theme.SaribLiveRed
import com.example.ui.theme.SaribSuccessGreen
import com.example.ui.theme.SaribTextMuted
import com.example.ui.theme.SaribTextPrimary
import com.example.ui.theme.SaribTextSecondary
import com.example.ui.theme.SaribTop01
import com.example.ui.theme.SaribTop02
import com.example.ui.theme.SaribTop03
import kotlinx.coroutines.delay

@Composable
fun SaribTopHeader(
    onMenuClick: () -> Unit,
    onTelegramClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onSearchClick: (() -> Unit)? = null,
    onRefreshClick: (() -> Unit)? = null,
    isRefreshing: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .border(
                1.dp,
                Brush.verticalGradient(listOf(SaribCardBorder, SaribBlueGlow)),
                RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            ),
        color = SaribHeaderGradientStart
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(SaribHeaderGradientStart, SaribHeaderGradientEnd)
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Menu Hamburger Icon
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .testTag("menu_button")
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SaribCardBg)
                        .border(1.dp, SaribCardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "القائمة",
                        tint = SaribCyanAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Center SARIB TV Branding
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.dp, SaribElectricBlue, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_sarib_logo),
                            contentDescription = "شعار SARIB TV",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SARIB",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = SaribTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "TV",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = SaribCyanAccent
                        )
                    )
                }

                // Right Action Icons (Search, Refresh, Telegram, Favorites)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onSearchClick != null) {
                        IconButton(
                            onClick = onSearchClick,
                            modifier = Modifier
                                .testTag("header_search_button")
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SaribCardBg)
                                .border(1.dp, SaribCardBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "البحث",
                                tint = SaribTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (onRefreshClick != null) {
                        val spinRotation by rememberInfiniteTransition(label = "refresh_spin").animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = androidx.compose.animation.core.LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "spin_anim"
                        )
                        IconButton(
                            onClick = onRefreshClick,
                            modifier = Modifier
                                .testTag("header_refresh_button")
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isRefreshing) SaribElectricBlue.copy(alpha = 0.35f) else SaribCardBg)
                                .border(1.dp, if (isRefreshing) SaribCyanAccent else SaribCardBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "تحديث محتوى التطبيق",
                                tint = if (isRefreshing) SaribCyanAccent else SaribTextPrimary,
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SaribCardBg)
                            .border(1.dp, SaribCardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "تيليجرام",
                            tint = SaribCyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onFavoritesClick,
                        modifier = Modifier
                            .testTag("header_favorites_button")
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SaribCardBg)
                            .border(1.dp, SaribCardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = SaribTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
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
        items(categories) { category ->
            val isSelected = category == selectedCategory
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) SaribElectricBlue else SaribCardBg,
                label = "chipBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else SaribTextSecondary,
                label = "chipText"
            )
            val borderColor = if (isSelected) SaribCyanAccent else SaribCardBorderSubtle

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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

    // Auto-advance slider smoothly every 10 seconds
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
                .height(240.dp)
                .shadow(16.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, SaribCardBorder, RoundedCornerShape(22.dp)),
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
            Spacer(modifier = Modifier.height(10.dp))

            // Pagination Indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pageCount) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width = if (isSelected) 24.dp else 8.dp
                    val color = if (isSelected) SaribCyanAccent else SaribTextMuted
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(width)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}

/**
 * Standard Hero Slider Content (Movies, Series, Live Channels) with enhanced poster fit.
 */
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
        // 1. Dual-Layer Poster: Ambient background crop + crisp complete poster
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

        // 2. Centered fit layer if distinct vertical poster exists
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

        // 3. Auto-Playing Video in Slider (Appears smoothly after 3s when ready)
        if (videoUrl.isNotBlank() && isActive) {
            HeroSliderVideoBackground(
                streamUrl = videoUrl,
                isActive = isActive,
                isMuted = isMuted,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 4. Dark Gradient Vignette for pristine contrast & readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x66070C14),
                            Color(0x33070C14),
                            Color(0xF8070C14)
                        )
                    )
                )
        )

        // 5. Top Header Badges & Mute/Unmute Control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 10.dp, start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Top info badge (LIVE / Category)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xDD0A0F1A))
                    .border(0.5.dp, SaribCyanAccent.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.isLive) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SaribLiveRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = SaribCyanAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = if (item.badge.isNotBlank()) "${item.badge} • ${item.subtitle}" else item.subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Mute / Unmute Button for Video Preview
            if (videoUrl.isNotBlank()) {
                IconButton(
                    onClick = onMuteToggle,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC000000))
                        .border(1.dp, Color(0x44FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = if (isMuted) "تشغيل الصوت" else "كتم الصوت",
                        tint = if (isMuted) Color.White else SaribCyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 6. Bottom Title & Action Button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Watch Button (Direct navigation to full player)
            Box(
                modifier = Modifier
                    .testTag("hero_watch_button")
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(SaribElectricBlue, SaribCyanAccent)
                        )
                    )
                    .clickable { onWatchClick() }
                    .padding(horizontal = 22.dp, vertical = 7.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (item.isLive) "مشاهدة البث المباشر" else stringResource(id = R.string.watch_now),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

/**
 * Specialized Match Hero Slider (سلايدر المباراة):
 * Displays Host Team (Logo/Name), Guest Team (Logo/Name), League Badge & Name,
 * Kickoff Time, Live Score or Countdown Timer, and Match details.
 */
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
        // 1. Full Stadium / Match Backdrop Poster
        if (backdropImage.isNotBlank()) {
            AsyncImage(
                model = backdropImage,
                contentDescription = "${item.homeTeam} vs ${item.awayTeam}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Default stadium gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0F172A), Color(0xFF064E3B), Color(0xFF022C22))
                        )
                    )
            )
        }

        // 2. Video Stream Background if live
        if (videoUrl.isNotBlank() && isActive) {
            HeroSliderVideoBackground(
                streamUrl = videoUrl,
                isActive = isActive,
                isMuted = isMuted,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 3. Dark Gradient Overlay for Match clarity
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x88050912),
                            Color(0x66050912),
                            Color(0xFA050912)
                        )
                    )
                )
        )

        // 4. Content Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Header: League Pill & Match Status / Countdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // League Pill (Logo + Name)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xDD0A0F1A))
                        .border(0.5.dp, SaribCyanAccent.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.leagueLogoUrl.isNotBlank()) {
                        AsyncImage(
                            model = item.leagueLogoUrl,
                            contentDescription = item.leagueName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(18.dp)
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
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Match Status & Countdown Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (item.isLive) Color(0xDDDC2626) else Color(0xDD0B132B))
                        .border(0.5.dp, if (item.isLive) SaribLiveRed else SaribCyanAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.isLive) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "مباشر LIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = SaribCyanAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val timeDisplay = when {
                            item.matchTime.isNotBlank() -> "تبدأ ${item.matchTime}"
                            item.matchDate.isNotBlank() -> item.matchDate
                            else -> "قريباً"
                        }
                        Text(
                            text = timeDisplay,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Center Match Face-Off: Host Team vs Away Team
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Host Team (الفريق المضيف)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xCC0D1B2A))
                            .border(1.5.dp, SaribCyanAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.homeLogoUrl.isNotBlank()) {
                            AsyncImage(
                                model = item.homeLogoUrl,
                                contentDescription = item.homeTeam,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = null,
                                tint = SaribCyanAccent,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.homeTeam.ifBlank { "الفريق المضيف" },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Center Score or VS
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    if (item.isLive) {
                        Text(
                            text = "${item.homeScore} - ${item.awayScore}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = SaribCyanAccent
                            )
                        )
                        Text(
                            text = item.matchStatus.ifBlank { "مباشر الآن" },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SaribLiveRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xAA0A1128))
                                .border(1.dp, SaribElectricBlue.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "VS",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = SaribCyanAccent
                                )
                            )
                        }
                        if (item.matchTime.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.matchTime,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                // Away Team (الفريق الضيف)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xCC0D1B2A))
                            .border(1.5.dp, SaribElectricBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.awayLogoUrl.isNotBlank()) {
                            AsyncImage(
                                model = item.awayLogoUrl,
                                contentDescription = item.awayTeam,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = null,
                                tint = SaribElectricBlue,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.awayTeam.ifBlank { "الفريق الضيف" },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Bottom Bar: Commentator / Channel & Watch Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Channel & Commentator info
                val detailsText = listOfNotNull(
                    item.channelName.takeIf { it.isNotBlank() }?.let { "📺 $it" },
                    item.commentator.takeIf { it.isNotBlank() }?.let { "🎤 $it" }
                ).joinToString(" • ")

                if (detailsText.isNotBlank()) {
                    Text(
                        text = detailsText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFCBD5E1),
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Watch Button
                Box(
                    modifier = Modifier
                        .testTag("match_watch_button")
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(SaribElectricBlue, SaribCyanAccent)
                            )
                        )
                        .clickable { onWatchClick() }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (item.isLive) "مشاهدة البث" else "تفاصيل المباراة",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * High-performance, lifecycle-safe Video Auto-Play background composable for HeroSlider.
 */
@Composable
fun HeroSliderVideoBackground(
    streamUrl: String,
    isActive: Boolean,
    isMuted: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlayerReady by remember { mutableStateOf(false) }

    val exoPlayer = remember(streamUrl) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = true
            volume = if (isMuted) 0f else 1f
            videoScalingMode = androidx.media3.common.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        }
    }

    // Update volume dynamically when isMuted changes
    LaunchedEffect(isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    // Prepare and play stream with StreamUrlParser (supports HLS, Dash, TS, Proxy worker streams)
    LaunchedEffect(streamUrl, isActive) {
        if (isActive && streamUrl.isNotBlank()) {
            delay(3000) // Stay on poster for 3 seconds before auto-playing
            try {
                val parsed = StreamUrlParser.parse(streamUrl)
                val httpFactory = DefaultHttpDataSource.Factory()
                    .setUserAgent(parsed.userAgent ?: StreamUrlParser.DEFAULT_USER_AGENT)
                    .setAllowCrossProtocolRedirects(true)
                    .setConnectTimeoutMs(8000)
                    .setReadTimeoutMs(8000)
                StreamUrlParser.configureHttpDataSource(httpFactory, parsed)

                val mediaSourceFactory = DefaultMediaSourceFactory(httpFactory, StreamUrlParser.createExtractorsFactory())
                val drmManager = StreamUrlParser.createDrmSessionManager(parsed)
                if (drmManager != null) {
                    mediaSourceFactory.setDrmSessionManagerProvider { drmManager }
                }

                val mediaItemBuilder = androidx.media3.common.MediaItem.Builder().setUri(parsed.cleanUrl)
                parsed.mimeType?.let { mediaItemBuilder.setMimeType(it) }

                val mediaSource = mediaSourceFactory.createMediaSource(mediaItemBuilder.build())
                exoPlayer.setMediaSource(mediaSource)
                exoPlayer.prepare()
                exoPlayer.play()
            } catch (e: Exception) {
                // Fallback gracefully without breaking UI
            }
        } else {
            exoPlayer.pause()
        }
    }

    // Lifecycle cleanup
    DisposableEffect(streamUrl) {
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
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

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

@Composable
fun MainCategoriesRoundGrid(
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple("القنوات", Icons.Default.Tv, "channels"),
        Triple("المباريات", Icons.Default.SportsSoccer, "matches"),
        Triple("الأخبار", Icons.AutoMirrored.Filled.Article, "news"),
        Triple("الأفلام", Icons.Default.Movie, "movies"),
        Triple("المسلسلات", Icons.Default.VideoLibrary, "series")
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (title, icon, route) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onCategoryClick(route) }
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(SaribCardBgSecondary, SaribCardBg)
                            )
                        )
                        .border(1.5.dp, SaribElectricBlue.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SaribElectricBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = SaribCyanAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SaribTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
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
        // Right side (RTL Start): Blue vertical line & Section Title
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SaribCyanAccent)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SaribTextPrimary
                )
            )
        }

        // Left side: View All Pill Button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(SaribCardBg)
                .border(1.dp, SaribCardBorder, RoundedCornerShape(14.dp))
                .clickable { onViewAllClick() }
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = SaribCyanAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(id = R.string.view_all),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SaribCyanAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
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
            .width(115.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, SaribCardBorder, RoundedCornerShape(18.dp))
            .clickable { onClick(channel) },
        colors = CardDefaults.cardColors(containerColor = SaribCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Channel Logo Box
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF09111E))
                    .border(1.dp, SaribCardBorderSubtle, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (channel.logoUrl.isNotBlank()) {
                    AsyncImage(
                        model = channel.logoUrl,
                        contentDescription = channel.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                    )
                } else {
                    // Stylized Fallback Channel Logo
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = SaribCyanAccent,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = if (channel.name.contains("beIN", ignoreCase = true)) "beIN"
                            else if (channel.name.contains("MBC", ignoreCase = true)) "MBC"
                            else if (channel.name.contains("SSC", ignoreCase = true)) "SSC"
                            else "TV",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                }

                // Favorite Toggle Button (Top-Right)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xAA000000))
                        .clickable { onFavoriteToggle(channel) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (channel.isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "المفضلة",
                        tint = if (channel.isFavorite) Color(0xFFFF2A4B) else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = channel.name,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, SaribCardBorder, RoundedCornerShape(20.dp))
            .clickable { onClick(match) },
        colors = CardDefaults.cardColors(containerColor = SaribCardBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(SaribCardBgSecondary, SaribCardBg)
                    )
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header: League Badge & Live Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // League Name & Icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (match.leagueIconUrl.isNotBlank()) {
                            AsyncImage(
                                model = match.leagueIconUrl,
                                contentDescription = match.leagueName,
                                modifier = Modifier.size(16.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SaribCyanAccent)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = match.leagueName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SaribCyanAccent,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Status Pill
                    if (match.isLive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SaribLiveRed.copy(alpha = 0.2f))
                                .border(1.dp, SaribLiveRed, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "مباشر",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SaribLiveRed,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    } else {
                        Text(
                            text = match.matchDate,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SaribTextMuted
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Teams & Score
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
                        if (match.homeLogoUrl.isNotBlank()) {
                            AsyncImage(
                                model = match.homeLogoUrl,
                                contentDescription = match.homeTeam,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1B263B)),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1B263B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = match.homeTeam.take(1),
                                    style = MaterialTheme.typography.titleMedium.copy(
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
                                fontWeight = FontWeight.Bold,
                                color = SaribTextPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Middle Score or Time Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SaribDarkBackground)
                            .border(1.dp, SaribCardBorderSubtle, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (match.isLive || match.status.contains("-")) match.status else match.matchTime,
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = if (match.isLive) SaribCyanAccent else SaribTextSecondary,
                                fontWeight = FontWeight.Black
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
                                fontWeight = FontWeight.Bold,
                                color = SaribTextPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (match.awayLogoUrl.isNotBlank()) {
                            AsyncImage(
                                model = match.awayLogoUrl,
                                contentDescription = match.awayTeam,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1B263B)),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1B263B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = match.awayTeam.take(1),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = SaribElectricBlue,
                                        fontWeight = FontWeight.Bold
                                    )
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
fun MediaCardItem(
    item: MediaItem,
    onClick: (MediaItem) -> Unit,
    onFavoriteToggle: ((MediaItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val displayImageUrl = item.posterUrl.ifBlank { item.backdropUrl }
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "mediaCardScale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) SaribCyanAccent else SaribCardBorder,
        animationSpec = tween(durationMillis = 150),
        label = "mediaCardBorder"
    )
    val borderWidth = if (isFocused) 2.5.dp else 1.dp

    Card(
        modifier = modifier
            .width(135.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(18.dp))
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
            .clickable { onClick(item) },
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) SaribCardBgSecondary else SaribCardBg
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFF111E30))
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

                // Top Badge if Top ranked
                if (item.isTop) {
                    val badgeColor = when (item.topRank) {
                        "01" -> SaribTop01
                        "02" -> SaribTop02
                        else -> SaribTop03
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeColor)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "TOP ${item.topRank}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                }

                // Favorite Toggle Button (Top-Start)
                if (onFavoriteToggle != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xAA000000))
                            .clickable { onFavoriteToggle(item) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = if (item.isFavorite) Color(0xFFFF2A4B) else Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                // Overlay gradient at bottom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xCC070C14))
                            )
                        )
                )

                // Bottom title inside poster
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isFocused) SaribCyanAccent else Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp),
                    textAlign = TextAlign.Center
                )
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
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.04f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "catCardScale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) SaribCyanAccent else SaribCardBorder,
        animationSpec = tween(durationMillis = 150),
        label = "catCardBorder"
    )
    val borderWidth = if (isFocused) 2.dp else 1.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
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
            .clickable { onClick(category) },
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) SaribCardBgSecondary else SaribCardBg
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            SaribCardBgSecondary,
                            SaribCardBg
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon & Title & Subtitle
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        SaribElectricBlue.copy(alpha = 0.3f),
                                        Color(0xFF0F2440)
                                    )
                                )
                            )
                            .border(1.dp, SaribCyanAccent.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
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
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaribTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = category.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SaribTextSecondary
                            )
                        )
                    }
                }

                // Arrow
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = SaribTextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
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
    val navSurfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(borderColor, SaribBlueGlow, borderColor)),
                RoundedCornerShape(24.dp)
            ),
        color = navBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(navBg, navSurfaceVariant)
                    )
                )
                .padding(horizontal = 6.dp, vertical = 4.dp),
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
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            when {
                                isSelected -> SaribElectricBlue.copy(alpha = 0.35f)
                                isItemFocused -> SaribElectricBlue.copy(alpha = 0.2f)
                                else -> Color.Transparent
                            }
                        )
                        .border(
                            width = if (isSelected || isItemFocused) 1.5.dp else 0.dp,
                            color = if (isItemFocused) SaribCyanAccent else if (isSelected) SaribCyanAccent else Color.Transparent,
                            shape = RoundedCornerShape(18.dp)
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
                    if (isSelected) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = SaribCyanAccent,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                maxLines = 1
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isItemFocused) SaribCyanAccent else SaribTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isItemFocused) SaribCyanAccent else SaribTextMuted,
                                    fontSize = 10.sp
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "actionBtnScale"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> SaribCyanAccent
            isActive -> SaribCyanAccent
            else -> SaribCardBorder
        },
        animationSpec = tween(durationMillis = 150),
        label = "actionBtnBorder"
    )

    androidx.compose.material3.Card(
        modifier = modifier
            .height(58.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .border(
                if (isFocused) 2.dp else 1.dp,
                borderColor,
                RoundedCornerShape(16.dp)
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
            .clickable { onClick() },
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (isFocused) SaribCardBgSecondary else if (isActive) SaribElectricBlue.copy(alpha = 0.15f) else SaribCardBg
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isFocused || isActive) SaribCyanAccent else SaribTextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isFocused || isActive) SaribCyanAccent else SaribTextPrimary
                )
            )
        }
    }
}

