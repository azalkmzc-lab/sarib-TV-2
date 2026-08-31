package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.ChannelItem
import com.example.data.model.HeroBannerItem
import com.example.data.model.MatchItem
import com.example.data.model.MediaItem
import com.example.ui.components.CategoryChipsRow
import com.example.ui.components.ChannelCardItem
import com.example.ui.components.HeroSlider
import com.example.ui.components.MainCategoriesRoundGrid
import com.example.ui.components.MatchCardItem
import com.example.ui.components.MediaCardItem
import com.example.ui.components.SaribBottomNav
import com.example.ui.components.SaribTopHeader
import com.example.ui.components.SectionHeader
import com.example.ui.theme.SaribDarkBackground

@Composable
fun HomeScreen(
    heroBanner: HeroBannerItem,
    popularChannels: List<ChannelItem>,
    todaysMatches: List<MatchItem>,
    featuredMovies: List<MediaItem>,
    featuredSeries: List<MediaItem>,
    animePicks: List<MediaItem>,
    selectedChip: String,
    onChipSelected: (String) -> Unit,
    onMenuClick: () -> Unit,
    onTelegramClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onChannelClick: (ChannelItem) -> Unit,
    onMatchClick: (MatchItem) -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    onHeroWatchClick: (HeroBannerItem) -> Unit,
    onViewAllChannelsClick: () -> Unit,
    onViewAllMatchesClick: () -> Unit,
    onViewAllMoviesClick: () -> Unit,
    currentTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val homeCategoryChips = listOf("الكل", "مسلسل", "دراما", "رعب", "أكشن", "أجنبية", "أحجية", "وثائقي", "كوميدي")

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
                    banner = heroBanner,
                    onWatchClick = onHeroWatchClick
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 5 Main Categories Round Cards (القنوات, المباريات, الأفلام, المسلسلات, الأنمي)
            item {
                MainCategoriesRoundGrid(
                    onCategoryClick = onCategoryClick
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Section 1: Most Watched Channels (القنوات الأكثر مشاهدة)
            item {
                SectionHeader(
                    title = "القنوات الأكثر مشاهدة",
                    onViewAllClick = onViewAllChannelsClick
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(popularChannels) { channel ->
                        ChannelCardItem(
                            channel = channel,
                            onClick = onChannelClick,
                            onFavoriteToggle = { /* handled in detail or long press */ }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Section 2: Today's Matches (أهم مباريات اليوم)
            item {
                SectionHeader(
                    title = "أهم مباريات اليوم",
                    onViewAllClick = onViewAllMatchesClick
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    todaysMatches.take(3).forEach { match ->
                        MatchCardItem(
                            match = match,
                            onClick = onMatchClick
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Section 3: Featured Movies (أفلام مختارة) with TOP badges
            item {
                SectionHeader(
                    title = "أفلام مختارة",
                    onViewAllClick = onViewAllMoviesClick
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredMovies) { movie ->
                        MediaCardItem(
                            item = movie,
                            onClick = onMediaClick
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Section 4: Recommended Series (مسلسلات تستحق المشاهدة)
            item {
                SectionHeader(
                    title = "مسلسلات تستحق المشاهدة",
                    onViewAllClick = onViewAllMoviesClick
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredSeries) { series ->
                        MediaCardItem(
                            item = series,
                            onClick = onMediaClick
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Section 5: Anime Picks (اختيارات الأنمي)
            item {
                SectionHeader(
                    title = "اختيارات الأنمي",
                    onViewAllClick = onViewAllMoviesClick
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(animePicks) { anime ->
                        MediaCardItem(
                            item = anime,
                            onClick = onMediaClick
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
