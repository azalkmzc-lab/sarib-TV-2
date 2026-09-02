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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.LocalAppPreferences
import com.example.data.local.tr
import com.example.data.model.MatchItem
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
    onWatchMatch: (MatchItem, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs = LocalAppPreferences.current
    val context = LocalContext.current
    var hasAlert by remember { mutableStateOf(prefs.hasMatchNotification(match.id)) }

    val serverList = remember(match) {
        val list = mutableListOf<Pair<String, String>>()
        if (match.streamUrl.isNotBlank()) list.add("سيرفر 1 (الرئيسي FHD)" to match.streamUrl)
        if (match.server1.isNotBlank() && match.server1 != match.streamUrl) list.add("سيرفر 1 (FHD)" to match.server1)
        if (match.server2.isNotBlank()) list.add("سيرفر 2 (HD)" to match.server2)
        if (match.server3.isNotBlank()) list.add("سيرفر 3 (سريع CDN)" to match.server3)
        if (match.server4.isNotBlank()) list.add("سيرفر 4 (جودة متوسطة)" to match.server4)
        if (match.server5.isNotBlank()) list.add("سيرفر 5 (بث مباشر M3U8)" to match.server5)

        if (list.isEmpty()) {
            list.add("سيرفر 1 (البث السحابي)" to match.streamUrl)
            list.add("سيرفر 2 (احتياطي)" to match.streamUrl)
        }
        list
    }

    var selectedServerIndex by remember { mutableIntStateOf(0) }

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
                    .padding(20.dp)
            ) {
                // Top Action Bar (League Title + Notification Toggle + Close)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (match.leagueIconUrl.isNotBlank()) {
                            AsyncImage(
                                model = match.leagueIconUrl,
                                contentDescription = match.leagueName,
                                modifier = Modifier.size(22.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = match.leagueName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaribCyanAccent
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Team Notification Alert Bell Button
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

                Spacer(modifier = Modifier.height(16.dp))

                // Teams Score / Versus Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(SaribCardBg, Color(0xFF09101C))
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
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1B263B)),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
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
                                )
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
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = tr("live"),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Black
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            Text(
                                text = if (match.isLive || match.status.contains("-")) match.status else match.matchTime,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = if (match.isLive) SaribCyanAccent else SaribTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = match.matchDate,
                                style = MaterialTheme.typography.labelSmall.copy(color = SaribTextMuted)
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
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1B263B)),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
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
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info Rows: Stadium, Commentator, Channel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SaribCardBg)
                        .border(1.dp, SaribCardBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section: Streaming Servers Selection (5 Servers Slider / Chooser)
                Text(
                    text = tr("match_servers"),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SaribCyanAccent
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                serverList.forEachIndexed { index, (name, _) ->
                    val isSelected = selectedServerIndex == index
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) SaribElectricBlue.copy(alpha = 0.25f) else SaribCardBg)
                            .border(
                                1.dp,
                                if (isSelected) SaribCyanAccent else SaribCardBorderSubtle,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedServerIndex = index }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = null,
                                    tint = if (isSelected) SaribCyanAccent else SaribTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isSelected) SaribCyanAccent else SaribTextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }

                            if (isSelected) {
                                Text(
                                    text = "جاهز للبث",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SaribCyanAccent)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Watch Button
                Button(
                    onClick = {
                        val chosenUrl = serverList.getOrNull(selectedServerIndex)?.second ?: match.streamUrl
                        onWatchMatch(match, chosenUrl)
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaribElectricBlue)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr("watch_now"),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}
