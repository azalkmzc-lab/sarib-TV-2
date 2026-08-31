package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.MatchItem
import com.example.ui.components.MatchCardItem
import com.example.ui.components.SaribBottomNav
import com.example.ui.components.SaribTopHeader
import com.example.ui.components.SectionHeader
import com.example.ui.theme.SaribCardBg
import com.example.ui.theme.SaribCardBorder
import com.example.ui.theme.SaribCardBorderSubtle
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribDarkBackground
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribTextMuted
import com.example.ui.theme.SaribTextPrimary
import com.example.ui.theme.SaribTextSecondary

@Composable
fun MatchesScreen(
    matches: List<MatchItem>,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onMatchClick: (MatchItem) -> Unit,
    onMenuClick: () -> Unit,
    onTelegramClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onSearchClick: () -> Unit,
    currentTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dates = listOf(
        Pair("الأحد", "30 أغسطس"),
        Pair("الإثنين", "31 أغسطس"),
        Pair("الثلاثاء", "01 سبتمبر"),
        Pair("الأربعاء", "02 سبتمبر"),
        Pair("الخميس", "03 سبتمبر")
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SaribDarkBackground,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SaribDarkBackground),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Date Selector Header Row
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = SaribCyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "جدول المباريات",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaribTextPrimary
                            )
                        )
                    }

                    Text(
                        text = "اختر التاريخ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SaribTextMuted
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Date Pills
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dates) { (day, dateStr) ->
                        val isSelected = dateStr == selectedDate
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) SaribElectricBlue else SaribCardBg)
                                .border(
                                    1.dp,
                                    if (isSelected) SaribCyanAccent else SaribCardBorderSubtle,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { onDateSelected(dateStr) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else SaribTextMuted
                                    )
                                )
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isSelected) Color.White else SaribTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Matches List
            item {
                SectionHeader(
                    title = "أهم مباريات اليوم",
                    onViewAllClick = { }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(matches) { match ->
                Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)) {
                    MatchCardItem(
                        match = match,
                        onClick = onMatchClick
                    )
                }
            }
        }
    }
}
