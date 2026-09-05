package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Movie
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ChannelCategory
import com.example.data.model.ContentType
import com.example.data.model.MediaItem
import com.example.ui.components.MediaCardItem
import com.example.ui.theme.SaribCardBg
import com.example.ui.theme.SaribCardBorder
import com.example.ui.theme.SaribCardBorderSubtle
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribDarkBackground
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribGoldRating
import com.example.ui.theme.SaribTextMuted
import com.example.ui.theme.SaribTextPrimary
import com.example.ui.theme.SaribTextSecondary

@Composable
fun MediaCategoryDetailScreen(
    category: ChannelCategory,
    mediaList: List<MediaItem>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    onFavoriteToggle: ((MediaItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isGridView by rememberSaveable { mutableStateOf(true) }

    val filteredMedia = remember(mediaList, searchQuery) {
        if (searchQuery.isBlank()) {
            mediaList
        } else {
            mediaList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.genre.contains(searchQuery, ignoreCase = true) ||
                        it.year.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SaribDarkBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SaribCardBg)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .testTag("media_category_back_button")
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
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isLoading) "جاري سحب المحتوى..." else "${filteredMedia.size} متوفر",
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
                    IconButton(
                        onClick = { isGridView = !isGridView },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SaribDarkBackground)
                            .border(1.dp, SaribCardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
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
            // Search field
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
                // Stylish Loading Indicator
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
                                text = "جاري تحميل محتوى القسم...",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SaribTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "يتم سحب الأفلام والمسلسلات بدقة عالية من السيرفر",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SaribTextSecondary
                                )
                            )
                        }
                    }
                }
            } else if (filteredMedia.isEmpty()) {
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
                            text = "لا توجد عناصر متوفرة في هذا القسم حالياً",
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
            } else if (isGridView) {
                // Grid of posters
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 110.dp),
                    contentPadding = PaddingValues(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredMedia, key = { it.id }, contentType = { "media_grid" }) { item ->
                        MediaCardItem(
                            item = item,
                            onClick = onMediaClick,
                            onFavoriteToggle = onFavoriteToggle,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // List of items
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredMedia, key = { it.id }, contentType = { "media_list" }) { item ->
                        val displayImageUrl = item.posterUrl.ifBlank { item.backdropUrl }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, SaribCardBorder, RoundedCornerShape(16.dp))
                                .clickable { onMediaClick(item) },
                            colors = CardDefaults.cardColors(containerColor = SaribCardBg)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 54.dp, height = 72.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF09111E)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (displayImageUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = displayImageUrl,
                                                contentDescription = item.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(
                                                imageVector = if (item.type == ContentType.SERIES) Icons.Default.Tv else Icons.Default.Movie,
                                                contentDescription = null,
                                                tint = SaribCyanAccent,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                color = SaribTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "${item.year.ifBlank { "2024" }} • ${item.genre.ifBlank { "HD" }}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = SaribTextSecondary
                                            )
                                        )
                                        if (item.rating.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "⭐ ${item.rating}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = SaribGoldRating,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (onFavoriteToggle != null) {
                                        IconButton(
                                            onClick = { onFavoriteToggle(item) },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(if (item.isFavorite) Color(0x33FF2A4B) else Color(0xFF0C1929))
                                                .border(
                                                    1.dp,
                                                    if (item.isFavorite) Color(0xFFFF2A4B) else SaribCardBorderSubtle,
                                                    CircleShape
                                                )
                                        ) {
                                            Icon(
                                                imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "المفضلة",
                                                tint = if (item.isFavorite) Color(0xFFFF2A4B) else Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(SaribElectricBlue.copy(alpha = 0.2f))
                                            .clickable { onMediaClick(item) },
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
}
