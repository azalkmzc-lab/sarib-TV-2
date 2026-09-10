package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.example.data.model.MatchEventItem
import com.example.data.model.MatchItem
import com.example.data.model.MatchPlayer
import com.example.data.model.MatchStatisticItem
import com.example.data.model.TeamLineup
import com.example.data.model.getActiveServers
import com.example.ui.theme.SaribAmberGold
import com.example.ui.theme.SaribCardBg
import com.example.ui.theme.SaribCardBgSecondary
import com.example.ui.theme.SaribCardBorder
import com.example.ui.theme.SaribCardBorderSubtle
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribDarkBackground
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribLiveRed
import com.example.ui.theme.SaribSuccessGreen
import com.example.ui.theme.SaribTextMuted
import com.example.ui.theme.SaribTextPrimary
import com.example.ui.theme.SaribTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    match: MatchItem,
    onBackClick: () -> Unit,
    onWatchMatch: (MatchItem, String) -> Unit,
    onFetchLineups: suspend (String) -> Pair<TeamLineup?, TeamLineup?>,
    onFetchEvents: suspend (String) -> List<MatchEventItem>,
    onFetchStatistics: suspend (String) -> List<MatchStatisticItem>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("نظرة عامة", "التشكيلة والخطة", "أحداث اللقاء", "الإحصائيات")

    // Async Data States
    var isLoadingLineups by remember { mutableStateOf(false) }
    var homeLineup by remember { mutableStateOf<TeamLineup?>(null) }
    var awayLineup by remember { mutableStateOf<TeamLineup?>(null) }
    var lineupsFetched by remember { mutableStateOf(false) }

    var isLoadingEvents by remember { mutableStateOf(false) }
    var matchEvents by remember { mutableStateOf<List<MatchEventItem>>(emptyList()) }
    var eventsFetched by remember { mutableStateOf(false) }

    var isLoadingStats by remember { mutableStateOf(false) }
    var matchStats by remember { mutableStateOf<List<MatchStatisticItem>>(emptyList()) }
    var statsFetched by remember { mutableStateOf(false) }

    val activeServers = remember(match) { match.getActiveServers() }

    // Load initial data for tabs
    LaunchedEffect(match.id, selectedTabIndex) {
        when (selectedTabIndex) {
            1 -> {
                if (!lineupsFetched) {
                    isLoadingLineups = true
                    try {
                        val (home, away) = onFetchLineups(match.id)
                        homeLineup = home
                        awayLineup = away
                        lineupsFetched = true
                    } catch (e: Exception) {
                        homeLineup = null
                        awayLineup = null
                    } finally {
                        isLoadingLineups = false
                    }
                }
            }
            2 -> {
                if (!eventsFetched) {
                    isLoadingEvents = true
                    try {
                        matchEvents = onFetchEvents(match.id)
                        eventsFetched = true
                    } catch (e: Exception) {
                        matchEvents = emptyList()
                    } finally {
                        isLoadingEvents = false
                    }
                }
            }
            3 -> {
                if (!statsFetched) {
                    isLoadingStats = true
                    try {
                        matchStats = onFetchStatistics(match.id)
                        statsFetched = true
                    } catch (e: Exception) {
                        matchStats = emptyList()
                    } finally {
                        isLoadingStats = false
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SaribDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = match.leagueName.ifBlank { "تفاصيل المباراة" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${match.homeTeam} vs ${match.awayTeam}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SaribCyanAccent
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("match_detail_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (match.isLive) {
                        Surface(
                            color = SaribLiveRed.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SaribLiveRed.copy(alpha = 0.5f)),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(SaribLiveRed)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "مباشر LIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SaribLiveRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SaribDarkBackground,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. HERO MATCH SCOREBOARD BANNER
            item {
                MatchHeroScoreboard(
                    match = match,
                    onWatchClick = {
                        val targetUrl = activeServers.firstOrNull()?.second ?: match.streamUrl
                        onWatchMatch(match, targetUrl)
                    },
                    hasStream = activeServers.isNotEmpty() || match.streamUrl.isNotBlank()
                )
            }

            // 2. LIVE BROADCAST SERVERS (If links exist)
            if (activeServers.isNotEmpty() || match.streamUrl.isNotBlank()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    MatchBroadcastServersCard(
                        match = match,
                        activeServers = activeServers,
                        onServerSelect = { serverUrl ->
                            onWatchMatch(match, serverUrl)
                        }
                    )
                }
            }

            // 3. NAVIGATION TABS ROW
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    color = SaribCardBg,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SaribCardBorderSubtle)
                ) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = SaribCyanAccent,
                        edgePadding = 8.dp,
                        indicator = { tabPositions ->
                            if (selectedTabIndex < tabPositions.size) {
                                TabRowDefaults.Indicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                    height = 3.dp,
                                    color = SaribCyanAccent
                                )
                            }
                        },
                        divider = {}
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTabIndex == index
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) SaribCyanAccent else SaribTextMuted,
                                            fontSize = 13.sp
                                        ),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                },
                                modifier = Modifier.testTag("match_tab_$index")
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 4. TAB CONTENTS
            when (selectedTabIndex) {
                0 -> {
                    // OVERVIEW TAB
                    item {
                        MatchOverviewContent(match = match)
                    }
                }
                1 -> {
                    // LINEUPS & TACTICS TAB
                    item {
                        MatchLineupsContent(
                            match = match,
                            isLoading = isLoadingLineups,
                            homeLineup = homeLineup,
                            awayLineup = awayLineup,
                            onRefresh = {
                                scope.launch {
                                    isLoadingLineups = true
                                    try {
                                        val (home, away) = onFetchLineups(match.id)
                                        homeLineup = home
                                        awayLineup = away
                                        lineupsFetched = true
                                    } finally {
                                        isLoadingLineups = false
                                    }
                                }
                            }
                        )
                    }
                }
                2 -> {
                    // EVENTS & TIMELINE TAB
                    item {
                        MatchEventsContent(
                            match = match,
                            isLoading = isLoadingEvents,
                            events = matchEvents,
                            onRefresh = {
                                scope.launch {
                                    isLoadingEvents = true
                                    try {
                                        matchEvents = onFetchEvents(match.id)
                                        eventsFetched = true
                                    } finally {
                                        isLoadingEvents = false
                                    }
                                }
                            }
                        )
                    }
                }
                3 -> {
                    // STATISTICS TAB
                    item {
                        MatchStatisticsContent(
                            match = match,
                            isLoading = isLoadingStats,
                            stats = matchStats,
                            onRefresh = {
                                scope.launch {
                                    isLoadingStats = true
                                    try {
                                        matchStats = onFetchStatistics(match.id)
                                        statsFetched = true
                                    } finally {
                                        isLoadingStats = false
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 1. HERO MATCH SCOREBOARD COMPONENT
 */
@Composable
private fun MatchHeroScoreboard(
    match: MatchItem,
    onWatchClick: () -> Unit,
    hasStream: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .shadow(16.dp, RoundedCornerShape(24.dp)),
        color = SaribCardBg,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SaribElectricBlue.copy(alpha = 0.35f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF131D31),
                            SaribCardBg,
                            Color(0xFF0C111C)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // League & Round Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x3300D4FF))
                        .border(1.dp, SaribCyanAccent.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    if (match.leagueIconUrl.isNotBlank()) {
                        AsyncImage(
                            model = match.leagueIconUrl,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = SaribAmberGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = match.leagueName.ifBlank { "بطولة رسمية" },
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = SaribCyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Teams vs Score Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home Team
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            color = Color(0x22FFFFFF),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, SaribCyanAccent.copy(alpha = 0.4f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (match.homeLogoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = match.homeLogoUrl,
                                        contentDescription = match.homeTeam,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .padding(4.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.SportsSoccer,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = match.homeTeam,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Score & Time Center Hub
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1.2f)
                            .padding(horizontal = 4.dp)
                    ) {
                        val isFinished = match.status.contains("انتهت", ignoreCase = true) || match.status.contains("FT", ignoreCase = true)
                        val isLive = match.isLive || match.status.contains("مباشر", ignoreCase = true) || match.status.contains("Live", ignoreCase = true)

                        if (isLive || isFinished) {
                            // Live or FT Scoreboard
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "${match.homeScore}",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = if (isLive) SaribCyanAccent else Color.White,
                                        fontSize = 32.sp
                                    )
                                )
                                Text(
                                    text = " - ",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SaribTextMuted,
                                        fontSize = 28.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                Text(
                                    text = "${match.awayScore}",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = if (isLive) SaribCyanAccent else Color.White,
                                        fontSize = 32.sp
                                    )
                                )
                            }
                        } else {
                            // Kickoff Time Display
                            Surface(
                                color = Color(0x33FFFFFF),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF))
                            ) {
                                Text(
                                    text = match.matchTime.ifBlank { "لم تبدأ" },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SaribCyanAccent,
                                        fontSize = 17.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Match Status Pill
                        Surface(
                            color = when {
                                isLive -> SaribLiveRed.copy(alpha = 0.25f)
                                isFinished -> Color(0x334CAF50)
                                else -> Color(0x22FFFFFF)
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when {
                                    isLive -> SaribLiveRed.copy(alpha = pulseAlpha)
                                    isFinished -> SaribSuccessGreen.copy(alpha = 0.4f)
                                    else -> Color(0x33FFFFFF)
                                }
                            )
                        ) {
                            Text(
                                text = match.status.ifBlank { if (isLive) "مباشر الآن" else "لم تبدأ" },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = when {
                                        isLive -> SaribLiveRed
                                        isFinished -> SaribSuccessGreen
                                        else -> SaribTextMuted
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }

                        if (match.matchDate.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = match.matchDate,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SaribTextMuted,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // Away Team
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            color = Color(0x22FFFFFF),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, SaribCyanAccent.copy(alpha = 0.4f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (match.awayLogoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = match.awayLogoUrl,
                                        contentDescription = match.awayTeam,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .padding(4.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.SportsSoccer,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = match.awayTeam,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Quick Action Watch Button (If stream exists)
                if (hasStream) {
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onWatchClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("match_detail_hero_watch"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (match.isLive) SaribLiveRed else SaribElectricBlue
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (match.isLive) Icons.Default.LiveTv else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (match.isLive) "مشاهدة البث المباشر الآن" else "بدء تشغيل سيرفر المباراة",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. LIVE BROADCAST SERVERS CAROUSEL
 */
@Composable
private fun MatchBroadcastServersCard(
    match: MatchItem,
    activeServers: List<Pair<String, String>>,
    onServerSelect: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        color = SaribCardBg,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SaribCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = null,
                        tint = SaribCyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "سيرفرات البث المباشر المتاحة",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Surface(
                    color = SaribCyanAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "${activeServers.size.coerceAtLeast(1)} سيرفرات",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SaribCyanAccent,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Server Chips List
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activeServers, key = { it.first + it.second }) { (label, url) ->
                    var isServerFocused by remember { mutableStateOf(false) }

                    Surface(
                        modifier = Modifier
                            .onFocusChanged { isServerFocused = it.isFocused }
                            .focusable()
                            .clickable { onServerSelect(url) },
                        color = if (isServerFocused) SaribElectricBlue else SaribCardBgSecondary,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isServerFocused) SaribCyanAccent else SaribCardBorderSubtle
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isServerFocused) Color.White else SaribCyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isServerFocused) Color.White else SaribTextPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. TAB 1: OVERVIEW CONTENT
 */
@Composable
private fun MatchOverviewContent(match: MatchItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
    ) {
        // Match Info Grid Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SaribCardBg,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SaribCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "معلومات وبيانات اللقاء",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                MatchInfoRow(
                    icon = Icons.Default.EmojiEvents,
                    label = "البطولة والمسابقة",
                    value = match.leagueName.ifBlank { "غير محدد" },
                    accentColor = SaribAmberGold
                )

                Divider(color = SaribCardBorderSubtle, modifier = Modifier.padding(vertical = 10.dp))

                MatchInfoRow(
                    icon = Icons.Default.Tv,
                    label = "القناة الناقلة",
                    value = match.channelName.ifBlank { "قناة البث المباشر" },
                    accentColor = SaribCyanAccent
                )

                Divider(color = SaribCardBorderSubtle, modifier = Modifier.padding(vertical = 10.dp))

                MatchInfoRow(
                    icon = Icons.Default.Mic,
                    label = "معلق المباراة",
                    value = match.commentator.ifBlank { "معلق رياضي معتمد" },
                    accentColor = Color(0xFFE040FB)
                )

                Divider(color = SaribCardBorderSubtle, modifier = Modifier.padding(vertical = 10.dp))

                MatchInfoRow(
                    icon = Icons.Default.Stadium,
                    label = "ملعب اللقاء",
                    value = match.stadium.ifBlank { "الملعب الرئيسي" },
                    accentColor = SaribSuccessGreen
                )

                Divider(color = SaribCardBorderSubtle, modifier = Modifier.padding(vertical = 10.dp))

                MatchInfoRow(
                    icon = Icons.Default.CalendarMonth,
                    label = "تاريخ وتوقيت المباراة",
                    value = "${match.matchDate.ifBlank { "اليوم" }} • ${match.matchTime.ifBlank { "توقيت مكة المكرمة" }}",
                    accentColor = SaribElectricBlue
                )
            }
        }
    }
}

@Composable
private fun MatchInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SaribTextMuted,
                    fontSize = 13.sp
                )
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 13.sp,
                textAlign = TextAlign.End
            ),
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

/**
 * 4. TAB 2: LINEUPS & FORMATIONS CONTENT
 */
@Composable
private fun MatchLineupsContent(
    match: MatchItem,
    isLoading: Boolean,
    homeLineup: TeamLineup?,
    awayLineup: TeamLineup?,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = SaribCyanAccent, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "جاري جلب التشكيلة الرسمية من المصدر...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SaribTextMuted)
                    )
                }
            }
        } else if (homeLineup == null && awayLineup == null) {
            // EMPTY STATE (STRICTLY NO DUMMY/FAKE DATA AS REQUESTED BY USER)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SaribCardBg,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SaribCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = null,
                        tint = SaribTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "لا توجد تشكيلة متوفرة حالياً لهذا اللقاء من المصدر الرسمي",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "تعلن التشكيلات الرسمية للفرق عادة قبل موعد انطلاق المباراة بحوالي 45 إلى 60 دقيقة من صافرة البداية.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SaribTextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SaribCardBgSecondary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث",
                            tint = SaribCyanAccent
                        )
                    }
                }
            }
        } else {
            // Formations Header Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SaribCardBg,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SaribCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = homeLineup?.teamName ?: match.homeTeam,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = SaribCyanAccent)
                        )
                        Text(
                            text = "خطة: ${homeLineup?.formation ?: "4-3-3"}",
                            style = MaterialTheme.typography.labelSmall.copy(color = SaribTextMuted)
                        )
                    }

                    Surface(
                        color = Color(0x3300D4FF),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "التشكيلة الرسمية",
                            style = MaterialTheme.typography.labelSmall.copy(color = SaribCyanAccent, fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = awayLineup?.teamName ?: match.awayTeam,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = SaribAmberGold)
                        )
                        Text(
                            text = "خطة: ${awayLineup?.formation ?: "4-3-3"}",
                            style = MaterialTheme.typography.labelSmall.copy(color = SaribTextMuted)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Starters XI
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Home Starters
                Column(modifier = Modifier.weight(1f)) {
                    LineupTeamCard(
                        teamName = homeLineup?.teamName ?: match.homeTeam,
                        coachName = homeLineup?.coachName.orEmpty(),
                        starters = homeLineup?.starters.orEmpty(),
                        substitutes = homeLineup?.substitutes.orEmpty(),
                        isHome = true
                    )
                }

                // Away Starters
                Column(modifier = Modifier.weight(1f)) {
                    LineupTeamCard(
                        teamName = awayLineup?.teamName ?: match.awayTeam,
                        coachName = awayLineup?.coachName.orEmpty(),
                        starters = awayLineup?.starters.orEmpty(),
                        substitutes = awayLineup?.substitutes.orEmpty(),
                        isHome = false
                    )
                }
            }
        }
    }
}

@Composable
private fun LineupTeamCard(
    teamName: String,
    coachName: String,
    starters: List<MatchPlayer>,
    substitutes: List<MatchPlayer>,
    isHome: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SaribCardBg,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SaribCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = teamName,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isHome) SaribCyanAccent else SaribAmberGold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (coachName.isNotBlank()) {
                Text(
                    text = "المدرب: $coachName",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SaribTextMuted,
                        fontSize = 10.sp
                    )
                )
            }

            Divider(color = SaribCardBorderSubtle, modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "التشكيل الأساسي (${starters.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            starters.forEach { player ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(20.dp),
                        shape = CircleShape,
                        color = if (isHome) SaribElectricBlue else Color(0x33FFA000)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${player.number}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (player.position.isNotBlank()) {
                        Text(
                            text = player.position,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SaribTextMuted,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }

            if (substitutes.isNotEmpty()) {
                Divider(color = SaribCardBorderSubtle, modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "دكة البدلاء (${substitutes.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SaribTextMuted
                    ),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                substitutes.take(8).forEach { sub ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${sub.number}",
                            style = MaterialTheme.typography.labelSmall.copy(color = SaribTextMuted, fontSize = 9.sp),
                            modifier = Modifier.width(18.dp)
                        )
                        Text(
                            text = sub.name,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SaribTextSecondary,
                                fontSize = 10.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * 5. TAB 3: EVENTS & TIMELINE CONTENT
 */
@Composable
private fun MatchEventsContent(
    match: MatchItem,
    isLoading: Boolean,
    events: List<MatchEventItem>,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = SaribCyanAccent, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "جاري جلب أحداث ومجريات اللقاء...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SaribTextMuted)
                    )
                }
            }
        } else if (events.isEmpty()) {
            // EMPTY STATE (STRICTLY NO DUMMY/FAKE DATA AS REQUESTED BY USER)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SaribCardBg,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SaribCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = SaribTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "لا توجد أحداث مسجلة بعد لهذا اللقاء",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "يتم تحديث مجريات وأهداف والبطاقات الصفراء والحمراء لحظة بلحظة أثناء سير المباراة من المصدر الرسمي.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SaribTextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SaribCardBgSecondary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث",
                            tint = SaribCyanAccent
                        )
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SaribCardBg,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SaribCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "جدول أحداث ومجريات اللقاء (${events.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    events.forEachIndexed { index, event ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Minute badge
                            Surface(
                                color = SaribElectricBlue.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SaribCyanAccent.copy(alpha = 0.4f)),
                                modifier = Modifier.width(42.dp)
                            ) {
                                Text(
                                    text = "${event.minute}'",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SaribCyanAccent,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Event Type Icon
                            val iconEmoji = when {
                                event.type.contains("Goal", ignoreCase = true) -> "⚽"
                                event.type.contains("Yellow", ignoreCase = true) || event.detail.contains("Yellow", ignoreCase = true) -> "🟨"
                                event.type.contains("Red", ignoreCase = true) || event.detail.contains("Red", ignoreCase = true) -> "🟥"
                                event.type.contains("subst", ignoreCase = true) -> "🔄"
                                else -> "⏱"
                            }

                            Text(
                                text = iconEmoji,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(end = 6.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = event.playerName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                val subText = if (event.assistPlayer.isNotBlank()) "صناعة: ${event.assistPlayer}" else if (event.detail.isNotBlank()) event.detail else event.type
                                Text(
                                    text = "$subText • ${event.teamName}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SaribTextMuted
                                    )
                                )
                            }
                        }

                        if (index < events.size - 1) {
                            Divider(color = SaribCardBorderSubtle, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 6. TAB 4: STATISTICS CONTENT
 */
@Composable
private fun MatchStatisticsContent(
    match: MatchItem,
    isLoading: Boolean,
    stats: List<MatchStatisticItem>,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = SaribCyanAccent, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "جاري جلب إحصائيات المباراة المقارنة...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SaribTextMuted)
                    )
                }
            }
        } else if (stats.isEmpty()) {
            // EMPTY STATE (STRICTLY NO DUMMY/FAKE DATA AS REQUESTED BY USER)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SaribCardBg,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SaribCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = SaribTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "لا توجد إحصائيات متوفرة لهذا اللقاء حالياً من المصدر",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "تتوفر الإحصائيات الدقيقة عادة خلال مجريات الشوط الأول وبعد نهاية اللقاء مباشرة من مزود البيانات الرياضية.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SaribTextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SaribCardBgSecondary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث",
                            tint = SaribCyanAccent
                        )
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SaribCardBg,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SaribCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header comparing both teams
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = match.homeTeam,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SaribCyanAccent),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "الإحصائيات المقارنة",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Text(
                            text = match.awayTeam,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SaribAmberGold, textAlign = TextAlign.End),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    stats.forEachIndexed { index, stat ->
                        StatComparisonRow(stat = stat)
                        if (index < stats.size - 1) {
                            Divider(color = SaribCardBorderSubtle, modifier = Modifier.padding(vertical = 10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatComparisonRow(stat: MatchStatisticItem) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stat.homeValue,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SaribCyanAccent
                )
            )

            Text(
                text = stat.typeArabic,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = SaribTextSecondary
                )
            )

            Text(
                text = stat.awayValue,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SaribAmberGold
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Dual comparison bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(SaribCardBgSecondary)
        ) {
            Box(
                modifier = Modifier
                    .weight(stat.homePercent.coerceAtLeast(0.05f))
                    .fillMaxSize()
                    .background(SaribCyanAccent)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Box(
                modifier = Modifier
                    .weight(stat.awayPercent.coerceAtLeast(0.05f))
                    .fillMaxSize()
                    .background(SaribAmberGold)
            )
        }
    }
}
