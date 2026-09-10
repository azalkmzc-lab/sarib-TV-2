package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.LocalAppPreferences
import com.example.data.local.tr
import com.example.data.model.MatchEventItem
import com.example.data.model.MatchItem
import com.example.data.model.MatchPlayer
import com.example.data.model.TeamLineup
import com.example.ui.theme.SaribCardBg
import com.example.ui.theme.SaribCardBorder
import com.example.ui.theme.SaribCardBorderSubtle
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribDarkBackground
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribLiveRed
import com.example.ui.theme.SaribTextMuted
import com.example.ui.theme.SaribTextPrimary
import com.example.ui.theme.SaribTextSecondary

@Composable
fun MatchDetailsDialog(
    match: MatchItem,
    onDismissRequest: () -> Unit,
    onWatchMatch: ((MatchItem, String) -> Unit)? = null,
    onFetchLineups: suspend (String) -> Pair<TeamLineup?, TeamLineup?> = { _ -> Pair(null, null) },
    onFetchEvents: suspend (String) -> List<MatchEventItem> = { _ -> emptyList() },
    modifier: Modifier = Modifier
) {
    val prefs = LocalAppPreferences.current
    val context = LocalContext.current
    var hasAlert by remember { mutableStateOf(prefs.hasMatchNotification(match.id)) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Fetch Lineups & Events from API-Football
    var homeLineup by remember { mutableStateOf<TeamLineup?>(null) }
    var awayLineup by remember { mutableStateOf<TeamLineup?>(null) }
    var matchEvents by remember { mutableStateOf<List<MatchEventItem>>(emptyList()) }
    var isLoadingDetails by remember { mutableStateOf(false) }

    LaunchedEffect(match.id) {
        isLoadingDetails = true
        try {
            val lineups = onFetchLineups(match.id)
            homeLineup = lineups.first
            awayLineup = lineups.second
            matchEvents = onFetchEvents(match.id)
        } catch (e: Exception) {
            // Graceful fallback
        } finally {
            isLoadingDetails = false
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .border(
                    1.5.dp,
                    Brush.verticalGradient(
                        listOf(SaribCyanAccent.copy(alpha = 0.6f), SaribElectricBlue.copy(alpha = 0.3f))
                    ),
                    RoundedCornerShape(26.dp)
                ),
            color = SaribDarkBackground,
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Top Action Bar (League Title + Notification Toggle + Close)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        if (match.leagueIconUrl.isNotBlank()) {
                            AsyncImage(
                                model = match.leagueIconUrl,
                                contentDescription = match.leagueName,
                                modifier = Modifier.size(24.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = match.leagueName.ifBlank { "تفاصيل المباراة" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaribCyanAccent
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val newState = prefs.toggleMatchNotification(match.id)
                                hasAlert = newState
                                val msg = if (newState) {
                                    "${prefs.getString("alert_set")} (${match.homeTeam} & ${match.awayTeam})"
                                } else {
                                    prefs.getString("alert_removed")
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (hasAlert) SaribCyanAccent.copy(alpha = 0.2f) else Color(0x33000000))
                        ) {
                            Icon(
                                imageVector = if (hasAlert) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "إشعار المباراة",
                                tint = if (hasAlert) SaribCyanAccent else SaribTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = tr("close"),
                                tint = SaribTextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Teams Score / Versus Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0F1A2E), Color(0xFF080D17))
                            )
                        )
                        .border(1.dp, SaribCardBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Home Team
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (match.homeLogoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = match.homeLogoUrl,
                                    contentDescription = match.homeTeam,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1B263B)),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1B263B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = match.homeTeam.take(1),
                                        style = MaterialTheme.typography.titleLarge.copy(color = SaribCyanAccent)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = match.homeTeam,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SaribTextPrimary,
                                    textAlign = TextAlign.Center
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Center Status / Score
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            if (match.isLive) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SaribLiveRed)
                                        .padding(horizontal = 10.dp, vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "مباشر LIVE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            val scoreDisplay = if (match.homeScore > 0 || match.awayScore > 0 || match.status.contains("-")) {
                                if (match.status.contains("-")) match.status else "${match.homeScore} - ${match.awayScore}"
                            } else {
                                "VS"
                            }

                            Text(
                                text = scoreDisplay,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = if (match.isLive) SaribCyanAccent else Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = match.matchTime.ifBlank { match.matchDate },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (match.isLive) SaribCyanAccent else SaribTextMuted,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        // Away Team
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (match.awayLogoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = match.awayLogoUrl,
                                    contentDescription = match.awayTeam,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1B263B)),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1B263B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = match.awayTeam.take(1),
                                        style = MaterialTheme.typography.titleLarge.copy(color = SaribCyanAccent)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = match.awayTeam,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SaribTextPrimary,
                                    textAlign = TextAlign.Center
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Modern 3-Tab Navigation: [التشكيلة | الأحداث | تفاصيل اللقاء]
                val tabs = listOf("التشكيلة", "الأحداث", "المعلومات")
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = SaribCardBg,
                    contentColor = SaribCyanAccent,
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = SaribCyanAccent,
                                height = 3.dp
                            )
                        }
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, SaribCardBorderSubtle, RoundedCornerShape(12.dp))
                ) {
                    tabs.forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    ),
                                    color = if (selectedTabIndex == index) SaribCyanAccent else SaribTextMuted
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (selectedTabIndex) {
                    // TAB 0: REAL LINEUPS (التشكيلة الحقيقية من API Football)
                    0 -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (homeLineup == null && awayLineup == null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.People, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (isLoadingDetails) "جاري سحب التشكيلة الرسمية من API-Football..." else "سيتم نشر التشكيلة الرسمية قبل انطلاق المباراة بساعة",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = SaribTextMuted, textAlign = TextAlign.Center)
                                        )
                                    }
                                }
                            } else {
                                // Home Team Lineup Card
                                homeLineup?.let { lineup ->
                                    TeamLineupSection(teamName = match.homeTeam, lineup = lineup, isHome = true)
                                    Spacer(modifier = Modifier.height(14.dp))
                                }

                                // Away Team Lineup Card
                                awayLineup?.let { lineup ->
                                    TeamLineupSection(teamName = match.awayTeam, lineup = lineup, isHome = false)
                                }
                            }
                        }
                    }

                    // TAB 1: REAL MATCH EVENTS TIMELINE (أحداث اللقاء)
                    1 -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (matchEvents.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.EventNote, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (isLoadingDetails) "جاري سحب أحداث اللقاء اللحظية..." else "لا توجد أحداث مسجلة حتى الآن (أهداف، كروت، تبديلات)",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = SaribTextMuted, textAlign = TextAlign.Center)
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    matchEvents.forEach { event ->
                                        MatchEventCard(event = event)
                                    }
                                }
                            }
                        }
                    }

                    // TAB 2: MATCH INFO
                    2 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SaribCardBg)
                                .border(1.dp, SaribCardBorderSubtle, RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (match.stadium.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Stadium, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "${tr("stadium")}: ", style = MaterialTheme.typography.labelMedium.copy(color = SaribTextSecondary))
                                    Text(text = match.stadium, style = MaterialTheme.typography.bodyMedium.copy(color = SaribTextPrimary, fontWeight = FontWeight.SemiBold))
                                }
                            }

                            if (match.commentator.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Mic, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "${tr("commentator")}: ", style = MaterialTheme.typography.labelMedium.copy(color = SaribTextSecondary))
                                    Text(text = match.commentator, style = MaterialTheme.typography.bodyMedium.copy(color = SaribTextPrimary, fontWeight = FontWeight.SemiBold))
                                }
                            }

                            if (match.channelName.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Tv, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "${tr("channel")}: ", style = MaterialTheme.typography.labelMedium.copy(color = SaribTextSecondary))
                                    Text(text = match.channelName, style = MaterialTheme.typography.bodyMedium.copy(color = SaribTextPrimary, fontWeight = FontWeight.SemiBold))
                                }
                            }

                            if (match.leagueName.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = SaribCyanAccent, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "البطولة: ", style = MaterialTheme.typography.labelMedium.copy(color = SaribTextSecondary))
                                    Text(text = match.leagueName, style = MaterialTheme.typography.bodyMedium.copy(color = SaribTextPrimary, fontWeight = FontWeight.SemiBold))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamLineupSection(teamName: String, lineup: TeamLineup, isHome: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SaribCardBg)
            .border(1.dp, if (isHome) SaribCyanAccent.copy(alpha = 0.5f) else SaribElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$teamName (${lineup.formation.ifBlank { "التشكيلة الأساسية" }})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = if (isHome) SaribCyanAccent else SaribElectricBlue)
            )
            if (lineup.coachName.isNotBlank()) {
                Text(
                    text = "المدرب: ${lineup.coachName}",
                    style = MaterialTheme.typography.labelSmall.copy(color = SaribTextMuted)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Starting XI
        Text(text = "اللاعبون الأساسيون:", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(6.dp))

        lineup.starters.forEach { player ->
            PlayerRowItem(player = player)
        }

        if (lineup.substitutes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "دكة البدلاء:", style = MaterialTheme.typography.labelSmall.copy(color = SaribTextMuted, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(4.dp))
            lineup.substitutes.take(7).forEach { sub ->
                PlayerRowItem(player = sub, isSubstitute = true)
            }
        }
    }
}

@Composable
private fun PlayerRowItem(player: MatchPlayer, isSubstitute: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isSubstitute) Color(0x33FFFFFF) else SaribCyanAccent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (player.number > 0) player.number.toString() else "-",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = player.name,
            style = MaterialTheme.typography.bodySmall.copy(color = if (isSubstitute) SaribTextSecondary else SaribTextPrimary, fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f)
        )
        if (player.position.isNotBlank()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x22FFFFFF))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = player.position,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = SaribCyanAccent)
                )
            }
        }
    }
}

@Composable
private fun MatchEventCard(event: MatchEventItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SaribCardBg)
            .border(0.5.dp, SaribCardBorderSubtle, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Minute Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(SaribElectricBlue)
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = "${event.minute}'",
                style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Event Type Icon
        val eventIcon = when (event.type.lowercase()) {
            "goal" -> "⚽"
            "card" -> if (event.detail.contains("Red", ignoreCase = true)) "🟥" else "🟨"
            "subst" -> "🔄"
            else -> "⚡"
        }

        Text(text = eventIcon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.playerName.ifBlank { event.type },
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
            if (event.assistPlayer.isNotBlank()) {
                Text(
                    text = "صناعة: ${event.assistPlayer}",
                    style = MaterialTheme.typography.labelSmall.copy(color = SaribTextMuted, fontSize = 10.sp)
                )
            }
        }

        if (event.teamName.isNotBlank()) {
            Text(
                text = event.teamName,
                style = MaterialTheme.typography.labelSmall.copy(color = SaribCyanAccent, fontSize = 10.sp)
            )
        }
    }
}
