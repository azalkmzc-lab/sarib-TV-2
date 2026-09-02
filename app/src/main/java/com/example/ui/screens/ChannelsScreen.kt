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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChannelCategory
import com.example.data.model.ChannelItem
import com.example.data.model.ViewMode
import com.example.ui.components.ActionButtonCard
import com.example.ui.components.ChannelCardItem
import com.example.ui.components.LargeChannelCategoryCard
import com.example.ui.components.SaribBottomNav
import com.example.ui.components.SaribTopHeader
import com.example.ui.theme.SaribCardBg
import com.example.ui.theme.SaribCardBgSecondary
import com.example.ui.theme.SaribCardBorder
import com.example.ui.theme.SaribCardBorderSubtle
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribDarkBackground
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribTextMuted
import com.example.ui.theme.SaribTextPrimary
import com.example.ui.theme.SaribTextSecondary

@Composable
fun ChannelsScreen(
    categories: List<ChannelCategory>,
    onCategoryClick: (ChannelCategory) -> Unit,
    onMenuClick: () -> Unit,
    onTelegramClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onSearchClick: () -> Unit,
    viewMode: ViewMode,
    onToggleViewMode: () -> Unit,
    currentTab: String,
    onTabSelected: (String) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    modifier: Modifier = Modifier
) {
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
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SaribDarkBackground),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 3 Large Action Buttons Row matching Screenshot 3
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Search Button
                    ActionButtonCard(
                        title = "البحث",
                        icon = Icons.Default.Search,
                        onClick = onSearchClick,
                        modifier = Modifier.weight(1f)
                    )

                    // View Mode Button (Grid / List toggle)
                    ActionButtonCard(
                        title = "الشكل",
                        icon = if (viewMode == ViewMode.GRID) Icons.Default.GridView else Icons.Default.ViewList,
                        onClick = onToggleViewMode,
                        modifier = Modifier.weight(1f)
                    )

                    // Categories Button
                    ActionButtonCard(
                        title = "الأصناف",
                        icon = Icons.Default.Category,
                        onClick = { /* Stay on categories */ },
                        isActive = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Categories List
            items(categories, key = { it.id }) { category ->
                Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)) {
                    LargeChannelCategoryCard(
                        category = category,
                        onClick = onCategoryClick
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryDetailScreen(
    category: ChannelCategory,
    channels: List<ChannelItem>,
    isLoading: Boolean = false,
    viewMode: ViewMode,
    onToggleViewMode: () -> Unit,
    onBackClick: () -> Unit,
    onChannelClick: (ChannelItem) -> Unit,
    onFavoriteToggle: (ChannelItem) -> Unit,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filteredChannels = remember(channels, searchQuery) {
        if (searchQuery.isBlank()) channels
        else channels.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SaribDarkBackground,
        topBar = {
            // Header for Category Detail
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SaribCardBg)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .testTag("back_button")
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SaribDarkBackground)
                            .border(1.dp, SaribCardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = SaribCyanAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaribTextPrimary
                            )
                        )
                        Text(
                            text = if (isLoading) "جاري سحب القنوات..." else "${filteredChannels.size} قناة متاحة",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isLoading) SaribCyanAccent else SaribTextSecondary
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SaribDarkBackground)
                            .border(1.dp, SaribCardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث",
                            tint = SaribCyanAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Toggle View Mode
                    IconButton(
                        onClick = onToggleViewMode,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SaribDarkBackground)
                            .border(1.dp, SaribCardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (viewMode == ViewMode.GRID) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "تبديل العرض",
                            tint = SaribCyanAccent
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SaribDarkBackground)
        ) {
            // Search field within category
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "البحث في ${category.name}...",
                        color = SaribTextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = SaribCyanAccent
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "مسح",
                                tint = SaribTextSecondary
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaribCyanAccent,
                    unfocusedBorderColor = SaribCardBorder,
                    focusedContainerColor = SaribCardBg,
                    unfocusedContainerColor = SaribCardBg,
                    focusedTextColor = SaribTextPrimary,
                    unfocusedTextColor = SaribTextPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )

            if (isLoading) {
                // Stylish On-Demand Loading indicator matching user request
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(RoundedCornerShape(22.dp))
                            .border(1.dp, SaribCyanAccent.copy(alpha = 0.5f), RoundedCornerShape(22.dp)),
                        colors = CardDefaults.cardColors(containerColor = SaribCardBg)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = SaribCyanAccent,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "جاري تحميل قنوات القسم...",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SaribTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "يتم الآن سحب القنوات بدقة عالية من السيرفر",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SaribTextSecondary
                                )
                            )
                        }
                    }
                }
            } else if (filteredChannels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.no_content_available),
                            style = MaterialTheme.typography.bodyLarge.copy(color = SaribTextSecondary)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onRefresh,
                            colors = ButtonDefaults.buttonColors(containerColor = SaribElectricBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("إعادة المحاولة", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (viewMode == ViewMode.GRID) {
                // Grid View
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    contentPadding = PaddingValues(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredChannels, key = { it.id }) { channel ->
                        ChannelCardItem(
                            channel = channel,
                            onClick = onChannelClick,
                            onFavoriteToggle = onFavoriteToggle
                        )
                    }
                }
            } else {
                // List View
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredChannels, key = { it.id }) { channel ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, SaribCardBorder, RoundedCornerShape(16.dp))
                                .clickable { onChannelClick(channel) },
                            colors = CardDefaults.cardColors(containerColor = SaribCardBg)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF09111E)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tv,
                                            contentDescription = null,
                                            tint = SaribCyanAccent,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = channel.name,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                color = SaribTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = "${channel.country} • ${channel.language}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = SaribTextSecondary
                                            )
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(SaribElectricBlue.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "تشغيل",
                                        tint = SaribCyanAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
