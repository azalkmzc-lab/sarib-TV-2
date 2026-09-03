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
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.tr
import com.example.data.model.ChannelCategory
import com.example.data.model.MediaItem
import com.example.ui.components.ActionButtonCard
import com.example.ui.components.CategoryChipsRow
import com.example.ui.components.LargeChannelCategoryCard
import com.example.ui.components.MediaCardItem
import com.example.ui.components.SaribBottomNav
import com.example.ui.components.SaribTopHeader
import com.example.ui.components.SectionHeader
import com.example.ui.theme.SaribCardBg
import com.example.ui.theme.SaribCardBorder
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribDarkBackground
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribTextMuted
import com.example.ui.theme.SaribTextPrimary
import com.example.ui.theme.SaribTextSecondary

@Composable
fun EntertainmentScreen(
    movies: List<MediaItem>,
    series: List<MediaItem>,
    anime: List<MediaItem>,
    entertainmentCategories: List<ChannelCategory>,
    vodCategories: List<ChannelCategory>,
    seriesCategories: List<ChannelCategory>,
    onCategoryClick: (ChannelCategory) -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    onMenuClick: () -> Unit,
    onTelegramClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onSearchClick: () -> Unit,
    currentTab: String,
    onTabSelected: (String) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    var selectedFilterIndex by rememberSaveable { mutableStateOf(0) }
    val filterTabs = listOf("الكل", "الأفلام", "المسلسلات", "تصنيفات الترفيه")

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
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Action Navigation Shortcut Cards
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionButtonCard(
                        title = tr("search"),
                        icon = Icons.Default.Search,
                        onClick = onSearchClick,
                        modifier = Modifier.weight(1f)
                    )
                    ActionButtonCard(
                        title = tr("movies"),
                        icon = Icons.Default.Movie,
                        onClick = { selectedFilterIndex = 1 },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButtonCard(
                        title = tr("series"),
                        icon = Icons.Default.Tv,
                        onClick = { selectedFilterIndex = 2 },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Dynamic filter tabs
            item {
                CategoryChipsRow(
                    categories = filterTabs,
                    selectedCategory = filterTabs.getOrElse(selectedFilterIndex) { "الكل" },
                    onCategorySelected = { title ->
                        selectedFilterIndex = filterTabs.indexOf(title).coerceAtLeast(0)
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // FILTER: ALL (الكل)
            if (selectedFilterIndex == 0) {
                // Movie Section with View All
                item {
                    val firstVodCat = vodCategories.firstOrNull() ?: ChannelCategory(
                        id = "all_movies",
                        name = "أحدث الأفلام",
                        subtitle = "مكتبة سينمائية VIP",
                        categoryType = "movies"
                    )
                    SectionHeader(
                        title = "مكتبة الأفلام الحديثة",
                        onViewAllClick = { onCategoryClick(firstVodCat) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (movies.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(movies, key = { it.id }) { item ->
                                MediaCardItem(
                                    item = item,
                                    onClick = onMediaClick
                                )
                            }
                        }
                    } else {
                        EmptyPreviewPlaceholder(
                            title = "جاري تحميل أحدث الأفلام...",
                            onClick = { onCategoryClick(firstVodCat) }
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Series Section with View All
                item {
                    val firstSeriesCat = seriesCategories.firstOrNull() ?: ChannelCategory(
                        id = "all_series",
                        name = "المسلسلات الحصرية",
                        subtitle = "عالم الدراما والتشويق",
                        categoryType = "series"
                    )
                    SectionHeader(
                        title = "عالم المسلسلات والدراما",
                        onViewAllClick = { onCategoryClick(firstSeriesCat) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (series.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(series, key = { it.id }) { item ->
                                MediaCardItem(
                                    item = item,
                                    onClick = onMediaClick
                                )
                            }
                        }
                    } else {
                        EmptyPreviewPlaceholder(
                            title = "جاري تحميل أحدث المسلسلات...",
                            onClick = { onCategoryClick(firstSeriesCat) }
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Xtream VOD Categories Section
                if (vodCategories.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "أقسام وتصنيفات الأفلام (اكستريم)",
                            onViewAllClick = { selectedFilterIndex = 1 }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    items(vodCategories.take(5), key = { it.id }) { cat ->
                        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                            LargeChannelCategoryCard(
                                category = cat,
                                onClick = { onCategoryClick(cat) }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                // Xtream Series Categories Section
                if (seriesCategories.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "أقسام وتصنيفات المسلسلات (اكستريم)",
                            onViewAllClick = { selectedFilterIndex = 2 }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    items(seriesCategories.take(5), key = { it.id }) { cat ->
                        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                            LargeChannelCategoryCard(
                                category = cat,
                                onClick = { onCategoryClick(cat) }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                // Anime / Additional picks if present
                if (anime.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "عالم الأنمي والكرتون",
                            onViewAllClick = {
                                val animeCat = ChannelCategory(
                                    id = "anime",
                                    name = "عالم الأنمي",
                                    subtitle = "مكتبة مسلسلات وأفلام الأنمي",
                                    categoryType = "series"
                                )
                                onCategoryClick(animeCat)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(anime, key = { it.id }) { item ->
                                MediaCardItem(
                                    item = item,
                                    onClick = onMediaClick
                                )
                            }
                        }
                    }
                }
            }

            // FILTER: MOVIES (الأفلام)
            if (selectedFilterIndex == 1) {
                item {
                    SectionHeader(
                        title = "أفلام مختارة",
                        onViewAllClick = {
                            val cat = vodCategories.firstOrNull() ?: ChannelCategory(
                                id = "all_movies",
                                name = "جميع الأفلام",
                                subtitle = "مكتبة سينمائية كاملة",
                                categoryType = "movies"
                            )
                            onCategoryClick(cat)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(movies, key = { it.id }) { item ->
                            MediaCardItem(
                                item = item,
                                onClick = onMediaClick
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "تصنيفات الأفلام المتاحة من حساب اكستريم (${vodCategories.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SaribTextPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                items(vodCategories, key = { it.id }) { cat ->
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                        LargeChannelCategoryCard(
                            category = cat,
                            onClick = { onCategoryClick(cat) }
                        )
                    }
                }
            }

            // FILTER: SERIES (المسلسلات)
            if (selectedFilterIndex == 2) {
                item {
                    SectionHeader(
                        title = "مسلسلات مختارة",
                        onViewAllClick = {
                            val cat = seriesCategories.firstOrNull() ?: ChannelCategory(
                                id = "all_series",
                                name = "جميع المسلسلات",
                                subtitle = "مكتبة دراما كاملة",
                                categoryType = "series"
                            )
                            onCategoryClick(cat)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(series, key = { it.id }) { item ->
                            MediaCardItem(
                                item = item,
                                onClick = onMediaClick
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "تصنيفات المسلسلات المتاحة من حساب اكستريم (${seriesCategories.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SaribTextPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                items(seriesCategories, key = { it.id }) { cat ->
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                        LargeChannelCategoryCard(
                            category = cat,
                            onClick = { onCategoryClick(cat) }
                        )
                    }
                }
            }

            // FILTER: ALL CATEGORIES (تصنيفات الترفيه)
            if (selectedFilterIndex == 3) {
                item {
                    Text(
                        text = "جميع تصنيفات الأفلام والمسلسلات في حساب اكستريم (${entertainmentCategories.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SaribTextPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                items(entertainmentCategories, key = { it.id }) { cat ->
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                        LargeChannelCategoryCard(
                            category = cat,
                            onClick = { onCategoryClick(cat) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyPreviewPlaceholder(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, SaribCardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SaribCardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SaribTextSecondary
                )
            )
            Text(
                text = "عرض الكل",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = SaribCyanAccent,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
