package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ContentType
import com.example.ui.components.SaribDrawerContent
import com.example.ui.screens.CategoryDetailScreen
import com.example.ui.screens.EntertainmentScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MatchesScreen
import com.example.ui.screens.ChannelsScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SeriesDetailScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun SaribApp(
    viewModel: MainViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val isConnecting by viewModel.isConnecting.collectAsState()
    val connectionError by viewModel.connectionError.collectAsState()

    val heroSliders by viewModel.heroSliders.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val mostWatchedChannels by viewModel.mostWatchedChannels.collectAsState()
    val allMatches by viewModel.allMatches.collectAsState()
    val featuredMovies by viewModel.featuredMovies.collectAsState()
    val featuredSeries by viewModel.featuredSeries.collectAsState()
    val animePicks by viewModel.animePicks.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    val categoryChannels by viewModel.categoryChannels.collectAsState()
    val currentSeriesDetail by viewModel.currentSeriesDetail.collectAsState()
    val isSeriesLoading by viewModel.isSeriesLoading.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val selectedMatchDate by viewModel.selectedMatchDate.collectAsState()
    val selectedHomeChip by viewModel.selectedHomeChip.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    val openTelegram: () -> Unit = {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/sarib_tv"))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "SARIB TV Official Channel: @sarib_tv", Toast.LENGTH_SHORT).show()
        }
    }

    val handleMediaClick: (com.example.data.model.MediaItem) -> Unit = { media ->
        if (media.type == ContentType.SERIES || media.type == ContentType.ANIME) {
            viewModel.openSeriesDetails(media)
        } else {
            viewModel.playMedia(
                title = media.title,
                subtitle = "${media.year} • ${media.genre}",
                streamUrl = media.streamUrl,
                isLive = false
            )
        }
    }

    // Context-aware Back Button handling
    BackHandler {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            val handled = viewModel.popBack()
            if (!handled) {
                if (currentTab != "home") {
                    viewModel.selectTab("home")
                } else {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressTime < 2000) {
                        (context as? android.app.Activity)?.finish()
                    } else {
                        lastBackPressTime = currentTime
                        Toast.makeText(context, "اضغط مرة أخرى للخروج من SARIB TV", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentScreen is AppScreen.Main,
        drawerContent = {
            ModalDrawerSheet {
                SaribDrawerContent(
                    onNavigateToHome = {
                        scope.launch { drawerState.close() }
                        viewModel.selectTab("home")
                    },
                    onNavigateToChannels = {
                        scope.launch { drawerState.close() }
                        viewModel.selectTab("channels")
                    },
                    onNavigateToMatches = {
                        scope.launch { drawerState.close() }
                        viewModel.selectTab("matches")
                    },
                    onNavigateToEntertainment = {
                        scope.launch { drawerState.close() }
                        viewModel.selectTab("entertainment")
                    },
                    onNavigateToFavorites = {
                        scope.launch { drawerState.close() }
                        viewModel.selectTab("favorites")
                    },
                    onTelegramClick = {
                        scope.launch { drawerState.close() }
                        openTelegram()
                    }
                )
            }
        }
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                is AppScreen.Splash -> {
                    SplashScreen(
                        isConnecting = isConnecting,
                        errorMessage = connectionError,
                        onRetry = { viewModel.startConnectionFlow() }
                    )
                }

                is AppScreen.Main -> {
                    when (currentTab) {
                        "home" -> {
                            HomeScreen(
                                heroSliders = heroSliders,
                                popularChannels = mostWatchedChannels,
                                todaysMatches = allMatches,
                                featuredMovies = featuredMovies,
                                featuredSeries = featuredSeries,
                                animePicks = animePicks,
                                selectedChip = selectedHomeChip,
                                onChipSelected = { viewModel.selectHomeChip(it) },
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onTelegramClick = openTelegram,
                                onFavoritesClick = { viewModel.selectTab("favorites") },
                                onSearchClick = { viewModel.navigateTo(AppScreen.Search) },
                                onCategoryClick = { route ->
                                    when (route) {
                                        "channels" -> viewModel.selectTab("channels")
                                        "matches" -> viewModel.selectTab("matches")
                                        "movies", "series", "anime" -> viewModel.selectTab("entertainment")
                                    }
                                },
                                onChannelClick = { channel ->
                                    viewModel.playMedia(
                                        title = channel.name,
                                        subtitle = "${channel.categoryName} • ${channel.country}",
                                        streamUrl = channel.streamUrl,
                                        isLive = true
                                    )
                                },
                                onMatchClick = { match ->
                                    viewModel.playMedia(
                                        title = "${match.homeTeam} vs ${match.awayTeam}",
                                        subtitle = match.leagueName,
                                        streamUrl = match.streamUrl,
                                        isLive = match.isLive
                                    )
                                },
                                onMediaClick = handleMediaClick,
                                onHeroWatchClick = { banner ->
                                    viewModel.playMedia(
                                        title = banner.title,
                                        subtitle = banner.subtitle,
                                        streamUrl = banner.streamUrl,
                                        isLive = banner.isLive
                                    )
                                },
                                onViewAllChannelsClick = { viewModel.selectTab("channels") },
                                onViewAllMatchesClick = { viewModel.selectTab("matches") },
                                onViewAllMoviesClick = { viewModel.selectTab("entertainment") },
                                currentTab = currentTab,
                                onTabSelected = { viewModel.selectTab(it) }
                            )
                        }

                        "channels" -> {
                            ChannelsScreen(
                                categories = categories,
                                onCategoryClick = { viewModel.openCategory(it) },
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onTelegramClick = openTelegram,
                                onFavoritesClick = { viewModel.selectTab("favorites") },
                                onSearchClick = { viewModel.navigateTo(AppScreen.Search) },
                                viewMode = viewMode,
                                onToggleViewMode = { viewModel.toggleViewMode() },
                                currentTab = currentTab,
                                onTabSelected = { viewModel.selectTab(it) }
                            )
                        }

                        "matches" -> {
                            MatchesScreen(
                                matches = allMatches,
                                selectedDate = selectedMatchDate,
                                onDateSelected = { date, offset -> viewModel.selectMatchDate(date, offset) },
                                onMatchClick = { match ->
                                    viewModel.playMedia(
                                        title = "${match.homeTeam} vs ${match.awayTeam}",
                                        subtitle = match.leagueName,
                                        streamUrl = match.streamUrl,
                                        isLive = match.isLive
                                    )
                                },
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onTelegramClick = openTelegram,
                                onFavoritesClick = { viewModel.selectTab("favorites") },
                                onSearchClick = { viewModel.navigateTo(AppScreen.Search) },
                                currentTab = currentTab,
                                onTabSelected = { viewModel.selectTab(it) }
                            )
                        }

                        "entertainment" -> {
                            EntertainmentScreen(
                                movies = featuredMovies,
                                series = featuredSeries,
                                anime = animePicks,
                                onMediaClick = handleMediaClick,
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onTelegramClick = openTelegram,
                                onFavoritesClick = { viewModel.selectTab("favorites") },
                                onSearchClick = { viewModel.navigateTo(AppScreen.Search) },
                                currentTab = currentTab,
                                onTabSelected = { viewModel.selectTab(it) }
                            )
                        }

                        "favorites" -> {
                            FavoritesScreen(
                                favorites = favorites,
                                onItemClick = { fav ->
                                    viewModel.playMedia(
                                        title = fav.title,
                                        subtitle = fav.subtitle,
                                        streamUrl = fav.streamUrl,
                                        isLive = fav.itemType == "CHANNEL" || fav.itemType == "MATCH"
                                    )
                                },
                                onRemoveFavorite = { fav ->
                                    viewModel.toggleFavorite(
                                        itemId = fav.itemId,
                                        title = fav.title,
                                        subtitle = fav.subtitle,
                                        type = fav.itemType,
                                        streamUrl = fav.streamUrl,
                                        isFav = true
                                    )
                                },
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onTelegramClick = openTelegram,
                                onFavoritesClick = { viewModel.selectTab("favorites") },
                                onSearchClick = { viewModel.navigateTo(AppScreen.Search) },
                                currentTab = currentTab,
                                onTabSelected = { viewModel.selectTab(it) }
                            )
                        }
                    }
                }

                is AppScreen.CategoryDetail -> {
                    CategoryDetailScreen(
                        category = screen.category,
                        channels = categoryChannels,
                        viewMode = viewMode,
                        onToggleViewMode = { viewModel.toggleViewMode() },
                        onBackClick = { viewModel.popBack() },
                        onChannelClick = { channel ->
                            viewModel.playMedia(
                                title = channel.name,
                                subtitle = "${channel.categoryName} • ${channel.country}",
                                streamUrl = channel.streamUrl,
                                isLive = true
                            )
                        },
                        onFavoriteToggle = { channel ->
                            viewModel.toggleFavorite(
                                itemId = channel.id,
                                title = channel.name,
                                subtitle = channel.categoryName,
                                type = "CHANNEL",
                                streamUrl = channel.streamUrl,
                                isFav = channel.isFavorite
                            )
                        }
                    )
                }

                is AppScreen.SeriesDetail -> {
                    SeriesDetailScreen(
                        mediaItem = screen.mediaItem,
                        seriesDetail = currentSeriesDetail,
                        isLoading = isSeriesLoading,
                        onBackClick = { viewModel.popBack() },
                        onPlayEpisode = { ep, epTitle ->
                            viewModel.playMedia(
                                title = epTitle,
                                subtitle = "${screen.mediaItem.title} • الحلقة ${ep.episodeNum}",
                                streamUrl = ep.streamUrl,
                                isLive = false
                            )
                        },
                        onPlayDirect = {
                            viewModel.playMedia(
                                title = screen.mediaItem.title,
                                subtitle = "${screen.mediaItem.year} • ${screen.mediaItem.genre}",
                                streamUrl = screen.mediaItem.streamUrl,
                                isLive = false
                            )
                        }
                    )
                }

                is AppScreen.Player -> {
                    PlayerScreen(
                        title = screen.title,
                        subtitle = screen.subtitle,
                        streamUrl = screen.streamUrl,
                        isLive = screen.isLive,
                        onBackClick = { viewModel.popBack() }
                    )
                }

                is AppScreen.Search -> {
                    SearchScreen(
                        searchQuery = searchQuery,
                        onQueryChange = { viewModel.updateSearchQuery(it) },
                        searchResults = searchResults,
                        onChannelClick = { channel ->
                            viewModel.playMedia(
                                title = channel.name,
                                subtitle = "${channel.categoryName} • ${channel.country}",
                                streamUrl = channel.streamUrl,
                                isLive = true
                            )
                        },
                        onMediaClick = handleMediaClick,
                        onBackClick = { viewModel.popBack() }
                    )
                }
            }
        }
    }
}
