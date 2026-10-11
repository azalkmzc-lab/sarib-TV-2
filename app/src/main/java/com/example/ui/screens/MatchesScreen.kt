package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.tr
import com.example.data.model.MatchItem
import com.example.ui.components.MatchCardItem
import com.example.ui.components.SaribBottomNav
import com.example.ui.components.SaribTopHeader
import com.example.ui.components.SectionHeader
import com.example.ui.theme.SaribCardBg
import com.example.ui.theme.SaribCardBorder
import com.example.ui.theme.SaribCardBorderSubtle
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribLiveRed
import com.example.ui.theme.SaribTextMuted
import com.example.ui.theme.SaribTextPrimary
import com.example.ui.theme.SaribTextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DateTabItem(
    val title: String,
    val dateDisplay: String,
    val isoDate: String,
    val dayOffset: Int,
    val icon: ImageVector
)

@Composable
fun MatchesScreen(
    matches: List<MatchItem>,
    onMatchClick: (MatchItem) -> Unit,
    onMenuClick: () -> Unit,
    onTelegramClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onSearchClick: () -> Unit,
    currentTab: String,
    onTabSelected: (String) -> Unit,
    selectedDate: String = "اليوم",
    onDateSelected: (String, Int) -> Unit = { _, _ -> },
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    showBars: Boolean = true,
    modifier: Modifier = Modifier
) {
    var activeDateTab by remember(selectedDate) { mutableStateOf(selectedDate) }
    var selectedLeagueFilter by remember { mutableStateOf("الكل") }
    var matchSearchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }

    // Date calculations for Yesterday, Today, Tomorrow
    val sdfIso = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val sdfDisplay = remember { SimpleDateFormat("d MMMM", Locale("ar")) }

    val todayCalendar = remember { Calendar.getInstance() }
    val todayIso = remember { sdfIso.format(todayCalendar.time) }
    val todayDisplay = remember { sdfDisplay.format(todayCalendar.time) }

    val yesterdayIso = remember {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, -1)
        sdfIso.format(c.time)
    }
    val yesterdayDisplay = remember {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, -1)
        sdfDisplay.format(c.time)
    }

    val tomorrowIso = remember {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, 1)
        sdfIso.format(c.time)
    }
    val tomorrowDisplay = remember {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, 1)
        sdfDisplay.format(c.time)
    }

    val dateTabs = remember(todayDisplay, yesterdayDisplay, tomorrowDisplay) {
        listOf(
            DateTabItem("الأمس", yesterdayDisplay, yesterdayIso, -1, Icons.Default.History),
            DateTabItem("اليوم", todayDisplay, todayIso, 0, Icons.Default.Today),
            DateTabItem("الغد", tomorrowDisplay, tomorrowIso, 1, Icons.Default.Event)
        )
    }

    // Date matching helper
    fun matchesTargetDate(match: MatchItem, targetTab: String): Boolean {
        val d = match.matchDate.trim()
        return when (targetTab) {
            "الأمس" -> d == yesterdayIso || d.contains("أمس") || d.contains("yesterday", ignoreCase = true)
            "الغد" -> d == tomorrowIso || d.contains("غد") || d.contains("tomorrow", ignoreCase = true)
            else -> { // "اليوم"
                d.isBlank() || d == todayIso || d.contains("اليوم") || d.contains("today", ignoreCase = true) ||
                (d != yesterdayIso && d != tomorrowIso && !d.contains("أمس") && !d.contains("غد"))
            }
        }
    }

    // Matches for active date
    val activeDateMatches = remember(matches, activeDateTab) {
        matches.filter { matchesTargetDate(it, activeDateTab) }
    }

    // Dynamic League list extracted from matches of the active date
    val availableLeagues = remember(activeDateMatches) {
        val list = mutableListOf("الكل")
        val leagues = activeDateMatches.map { it.leagueName.trim() }.filter { it.isNotBlank() }.distinct()
        list.addAll(leagues)
        list
    }

    // Filter matches based on selected league & search query
    val filteredMatches = remember(activeDateMatches, selectedLeagueFilter, matchSearchQuery) {
        activeDateMatches.filter { match ->
            val matchLeague = if (selectedLeagueFilter == "الكل") true else match.leagueName.contains(selectedLeagueFilter, ignoreCase = true)
            val matchQuery = if (matchSearchQuery.isBlank()) true else {
                match.homeTeam.contains(matchSearchQuery, ignoreCase = true) ||
                match.awayTeam.contains(matchSearchQuery, ignoreCase = true) ||
                match.leagueName.contains(matchSearchQuery, ignoreCase = true) ||
                match.commentator.contains(matchSearchQuery, ignoreCase = true) ||
                match.channelName.contains(matchSearchQuery, ignoreCase = true)
            }
            matchLeague && matchQuery
        }
    }

    val liveMatchesCount = remember(activeDateMatches) { activeDateMatches.count { it.isLive } }

    val content: @Composable (Modifier) -> Unit = { paddingModifier ->
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .then(paddingModifier)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = if (showBars) 24.dp else 12.dp)
        ) {
            // Hero Sports Live Banner / Live Matches summary
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    color = SaribCardBg,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SaribCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (liveMatchesCount > 0) SaribLiveRed.copy(alpha = 0.2f) else SaribElectricBlue.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SportsSoccer,
                                    contentDescription = null,
                                    tint = if (liveMatchesCount > 0) SaribLiveRed else SaribCyanAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "جدول المباريات والبطولات",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                )
                                Text(
                                    text = if (liveMatchesCount > 0) "🔴 $liveMatchesCount مباريات جارية الآن مباشرة" else "${activeDateMatches.size} مباراة متوفرة ليوم $activeDateTab",
                                    style = MaterialTheme.typography.labelSmall.copy(color = if (liveMatchesCount > 0) SaribLiveRed else SaribTextMuted)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val offset = when (activeDateTab) {
                                        "الأمس" -> -1
                                        "الغد" -> 1
                                        else -> 0
                                    }
                                    onDateSelected(activeDateTab, offset)
                                },
                                modifier = Modifier
                                    .testTag("match_refresh_button")
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x22FFFFFF))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "تحديث",
                                    tint = SaribCyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { showSearchBar = !showSearchBar },
                                modifier = Modifier
                                    .testTag("match_search_toggle")
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33FFFFFF))
                            ) {
                                Icon(
                                    imageVector = if (showSearchBar) Icons.Default.Clear else Icons.Default.Search,
                                    contentDescription = "بحث في المباريات",
                                    tint = SaribCyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Expandable Search Bar
            if (showSearchBar) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.padding(horizontal = 14.dp)) {
                        OutlinedTextField(
                            value = matchSearchQuery,
                            onValueChange = { matchSearchQuery = it },
                            placeholder = { Text("ابحث عن فريق، دوري، معلق، أو قناة...", color = SaribTextMuted, fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("match_search_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SaribCyanAccent,
                                unfocusedBorderColor = SaribCardBorderSubtle,
                                focusedContainerColor = SaribCardBg,
                                unfocusedContainerColor = SaribCardBg,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (matchSearchQuery.isNotBlank()) {
                                    IconButton(onClick = { matchSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "مسح", tint = SaribTextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // ⚽ DATE SELECTOR TABS: مباريات الأمس • مباريات اليوم • مباريات الغد
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dateTabs.forEach { tab ->
                        val isSelected = activeDateTab == tab.title
                        var isFocused by remember { mutableStateOf(false) }
                        val tabCount = matches.count { matchesTargetDate(it, tab.title) }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("match_date_tab_${tab.title}")
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    width = if (isSelected || isFocused) 1.5.dp else 1.dp,
                                    color = if (isFocused) SaribCyanAccent else if (isSelected) SaribCyanAccent else SaribCardBorder,
                                    shape = RoundedCornerShape(16.dp)
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
                                        activeDateTab = tab.title
                                        onDateSelected(tab.title, tab.dayOffset)
                                        true
                                    } else false
                                }
                                .clickable {
                                    activeDateTab = tab.title
                                    onDateSelected(tab.title, tab.dayOffset)
                                },
                            color = if (isSelected) SaribCyanAccent.copy(alpha = 0.16f) else SaribCardBg,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) SaribCyanAccent else SaribTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = tab.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = if (isSelected) SaribCyanAccent else SaribTextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = tab.dateDisplay,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) SaribCyanAccent.copy(alpha = 0.85f) else SaribTextMuted,
                                            fontSize = 11.sp
                                        )
                                    )
                                    if (tabCount > 0) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) SaribCyanAccent.copy(alpha = 0.3f) else Color(0x22FFFFFF))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "$tabCount",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (isSelected) SaribCyanAccent else SaribTextMuted,
                                                    fontSize = 10.sp,
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

            // League Filter Tabs Row
            if (availableLeagues.size > 1) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableLeagues, key = { "league_$it" }) { league ->
                            val isSelected = selectedLeagueFilter == league
                            var isLeagueFocused by remember { mutableStateOf(false) }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) SaribCyanAccent.copy(alpha = 0.2f) else SaribCardBg)
                                    .border(
                                        if (isLeagueFocused) 2.dp else 1.dp,
                                        if (isLeagueFocused) SaribCyanAccent else if (isSelected) SaribCyanAccent else Color.Transparent,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .onFocusChanged { isLeagueFocused = it.isFocused }
                                    .focusable()
                                    .onKeyEvent { keyEvent ->
                                        if (keyEvent.type == KeyEventType.KeyUp && (
                                            keyEvent.key == Key.DirectionCenter ||
                                            keyEvent.key == Key.Enter ||
                                            keyEvent.key == Key.NumPadEnter ||
                                            keyEvent.key == Key.ButtonA
                                        )) {
                                            selectedLeagueFilter = league
                                            true
                                        } else false
                                    }
                                    .clickable { selectedLeagueFilter = league }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = league,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isSelected || isLeagueFocused) SaribCyanAccent else SaribTextMuted,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Matches List Header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                val dateTitle = when (activeDateTab) {
                    "الأمس" -> "مباريات الأمس • $yesterdayDisplay"
                    "الغد" -> "مباريات الغد • $tomorrowDisplay"
                    else -> "مباريات اليوم • $todayDisplay"
                }
                val headerTitle = if (selectedLeagueFilter == "الكل") dateTitle else "$dateTitle ($selectedLeagueFilter)"
                SectionHeader(
                    title = headerTitle,
                    onViewAllClick = { }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (filteredMatches.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = null,
                                tint = SaribTextMuted,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            val emptyMsg = when {
                                matchSearchQuery.isNotBlank() -> "لم يتم العثور على مباريات مطابقة للبحث"
                                activeDateTab == "الأمس" -> "لا توجد مباريات مسجلة ليوم الأمس ($yesterdayDisplay)"
                                activeDateTab == "الغد" -> "لا توجد مباريات مسجلة ليوم الغد ($tomorrowDisplay)"
                                else -> "لا توجد مباريات متوفرة حالياً في جدول اليوم ($todayDisplay)"
                            }
                            Text(
                                text = emptyMsg,
                                style = MaterialTheme.typography.bodyMedium.copy(color = SaribTextMuted)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            if (activeDateTab != "اليوم") {
                                Button(
                                    onClick = {
                                        activeDateTab = "اليوم"
                                        onDateSelected("اليوم", 0)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SaribCyanAccent.copy(alpha = 0.25f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Today, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("العودة إلى مباريات اليوم", color = SaribCyanAccent, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredMatches, key = { it.id }, contentType = { "match_card" }) { match ->
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                        MatchCardItem(
                            match = match,
                            onClick = onMatchClick
                        )
                    }
                }
            }
        }
    }

    if (showBars) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                SaribTopHeader(
                    onMenuClick = onMenuClick,
                    onTelegramClick = onTelegramClick,
                    onFavoritesClick = onFavoritesClick,
                    onSearchClick = onSearchClick
                )
            },
            bottomBar = {
                SaribBottomNav(
                    currentTab = currentTab,
                    onTabSelected = onTabSelected
                )
            }
        ) { innerPadding ->
            content(Modifier.padding(innerPadding))
        }
    } else {
        content(Modifier)
    }
}
