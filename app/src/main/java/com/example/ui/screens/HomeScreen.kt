package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.local.tr
import com.example.data.model.ChannelItem
import com.example.data.model.HeroBannerItem
import com.example.ui.components.CategoryChipsRow
import com.example.ui.components.ChannelCardItem
import com.example.ui.components.HeroSlider
import com.example.ui.components.MainCategoriesRoundGrid
import com.example.ui.components.SaribBottomNav
import com.example.ui.components.SaribTopHeader
import com.example.ui.components.SectionHeader

@Composable
fun HomeScreen(
    heroSliders: List<HeroBannerItem>,
    popularChannels: List<ChannelItem>,
    selectedChip: String,
    onChipSelected: (String) -> Unit,
    onMenuClick: () -> Unit,
    onTelegramClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onChannelClick: (ChannelItem) -> Unit,
    onHeroWatchClick: (HeroBannerItem) -> Unit,
    onViewAllChannelsClick: () -> Unit,
    currentTab: String,
    onTabSelected: (String) -> Unit,
    onFavoriteToggle: (ChannelItem) -> Unit = {},
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    showBars: Boolean = true,
    modifier: Modifier = Modifier
) {
    val homeCategoryChips = androidx.compose.runtime.remember {
        listOf("الكل", "مسلسل", "دراما", "رعب", "أكشن", "أجنبية", "أحجية", "وثائقي", "كوميدي")
    }

    val content: @Composable (Modifier) -> Unit = { paddingModifier ->
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .then(paddingModifier)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = if (showBars) 24.dp else 12.dp)
        ) {
            // Category Chips Row
            item {
                Spacer(modifier = Modifier.height(10.dp))
                CategoryChipsRow(
                    categories = homeCategoryChips,
                    selectedCategory = selectedChip,
                    onCategorySelected = onChipSelected
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Hero Banner Slider
            item {
                HeroSlider(
                    sliders = heroSliders,
                    onWatchClick = onHeroWatchClick
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 4 Main Categories Round Cards (القنوات, الأفلام, المسلسلات, الأنمي)
            item {
                MainCategoriesRoundGrid(
                    onCategoryClick = onCategoryClick
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Most Watched Channels (القنوات الأكثر مشاهدة)
            item {
                SectionHeader(
                    title = tr("most_watched"),
                    onViewAllClick = onViewAllChannelsClick
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(popularChannels, key = { it.id }, contentType = { "popular_channel" }) { channel ->
                        ChannelCardItem(
                            channel = channel,
                            onClick = onChannelClick,
                            onFavoriteToggle = onFavoriteToggle
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
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
