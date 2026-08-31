package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.MediaItem
import com.example.ui.components.CategoryChipsRow
import com.example.ui.components.MediaCardItem
import com.example.ui.components.SaribBottomNav
import com.example.ui.components.SaribTopHeader
import com.example.ui.components.SectionHeader
import com.example.ui.theme.SaribDarkBackground

@Composable
fun EntertainmentScreen(
    movies: List<MediaItem>,
    series: List<MediaItem>,
    anime: List<MediaItem>,
    onMediaClick: (MediaItem) -> Unit,
    onMenuClick: () -> Unit,
    onTelegramClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onSearchClick: () -> Unit,
    currentTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("الكل") }
    val entertainmentCategories = listOf("الكل", "موفي كافيه", "كوكب الترفيه", "عالم السينما", "مسلسلات حصرية", "أنمي الأسبوع")

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
            // Category Chips
            item {
                Spacer(modifier = Modifier.height(10.dp))
                CategoryChipsRow(
                    categories = entertainmentCategories,
                    selectedCategory = selectedFilter,
                    onCategorySelected = { selectedFilter = it }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Section 1: Featured Movies
            item {
                SectionHeader(
                    title = "موفي كافيه - أفلام مختارة",
                    onViewAllClick = { }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(movies) { item ->
                        MediaCardItem(
                            item = item,
                            onClick = onMediaClick
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Section 2: Top TV Series
            item {
                SectionHeader(
                    title = "عالم السينما والمسلسلات",
                    onViewAllClick = { }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(series) { item ->
                        MediaCardItem(
                            item = item,
                            onClick = onMediaClick
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Section 3: Anime Collection
            item {
                SectionHeader(
                    title = "كوكب الترفيه - الأنمي الياباني",
                    onViewAllClick = { }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(anime) { item ->
                        MediaCardItem(
                            item = item,
                            onClick = onMediaClick
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Section 4: Kids & Family
            item {
                SectionHeader(
                    title = "للأطفال والعائلة",
                    onViewAllClick = { }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(movies.take(2) + anime.take(2)) { item ->
                        MediaCardItem(
                            item = item,
                            onClick = onMediaClick
                        )
                    }
                }
            }
        }
    }
}
