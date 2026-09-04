package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ContentType
import com.example.data.model.MatchItem
import com.example.data.model.getActiveServers
import com.example.ui.components.MatchDetailsDialog
import com.example.ui.components.SaribDrawerContent
import com.example.ui.components.SettingsDialog
import com.example.ui.components.VpnBlockedDialog
import com.example.ui.screens.CategoryDetailScreen
import com.example.ui.screens.EntertainmentScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MatchesScreen
import com.example.ui.screens.ChannelsScreen
import com.example.ui.screens.MediaCategoryDetailScreen
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
    val entertainmentCategories by viewModel.entertainmentCategories.collectAsState()
    val vodCategories by viewModel.vodCategories.collectAsState()
    val seriesCategories by viewModel.seriesCategories.collectAsState()
    val mostWatchedChannels by viewModel.mostWatchedChannels.collectAsState()
    val allChannels by viewModel.allChannels.collectAsState()
    val allMatches by viewModel.allMatches.collectAsState()
    val featuredMovies by viewModel.featuredMovies.collectAsState()
    val featuredSeries by viewModel.featuredSeries.collectAsState()
    val animePicks by viewModel.animePicks.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    // Persistent scroll states across navigation and category exits
    val homeListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val channelsListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val matchesListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val entertainmentListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val favoritesListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    val categoryChannels by viewModel.categoryChannels.collectAsState()
    val categoryMediaList by viewModel.categoryMediaList.collectAsState()
    val isCategoryLoading by viewModel.isCategoryLoading.collectAsState()
    val currentSeriesDetail by viewModel.currentSeriesDetail.collectAsState()
    val isSeriesLoading by viewModel.isSeriesLoading.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val selectedMatchDate by viewModel.selectedMatchDate.collectAsState()
    val selectedHomeChip by viewModel.selectedHomeChip.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    val isVpnDetected by viewModel.isVpnDetected.collectAsState()

    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    // Dialog state for Match Details and Settings
    var selectedMatchForDetails by remember { mutableStateOf<MatchItem?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }

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
                isLive = false,
                servers = media.getActiveServers()
            )
        }
    }

    val handleMediaFavoriteToggle: (com.example.data.model.MediaItem) -> Unit = { media ->
        viewModel.toggleFavorite(
            itemId = media.id,
            title = media.title,
            subtitle = "${media.year} • ${media.genre}",
            type = if (media.type == ContentType.SERIES) "SERIES" else "MOVIE",
            streamUrl = media.streamUrl,
            isFav = media.isFavorite
        )
    }

    // Context-aware Back Button handling
    BackHandler {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (selectedMatchForDetails != null) {
            selectedMatchForDetails = null
        } else if (showSettingsDialog) {
            showSettingsDialog = false
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

    // MATCH DETAILS & MULTI-SERVER DIALOG
    selectedMatchForDetails?.let { match ->
        MatchDetailsDialog(
            match = match,
            onDismissRequest = { selectedMatchForDetails = null },
            onWatchMatch = { selectedMatch, streamUrl ->
                selectedMatchForDetails = null
                viewModel.playMedia(
                    title = "${selectedMatch.homeTeam} vs ${selectedMatch.awayTeam}",
                    subtitle = "${selectedMatch.leagueName} • ${selectedMatch.commentator.ifEmpty { "بث مباشر" }}",
                    streamUrl = streamUrl,
                    isLive = selectedMatch.isLive,
                    servers = selectedMatch.getActiveServers()
                )
            }
        )
    }

    // SETTINGS DIALOG (Theme, Language, Cache)
    if (showSettingsDialog) {
        SettingsDialog(
            onDismissRequest = { showSettingsDialog = false },
            onClearCache = {
                viewModel.clearDatabaseCache()
            }
        )
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
                    onNavigateToEntertainment = {
                        scope.launch { drawerState.close() }
                        viewModel.selectTab("entertainment")
                    },
                    onNavigateToFavorites = {
                        scope.launch { drawerState.close() }
                        viewModel.selectTab("favorites")
                    },
                    onSettingsClick = {
                        scope.launch { drawerState.close() }
                        showSettingsDialog = true
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
                                selectedChip = selectedHomeChip,
                                onChipSelected = { viewModel.selectHomeChip(it) },
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onTelegramClick = openTelegram,
                                onFavoritesClick = { viewModel.selectTab("favorites") },
                                onSearchClick = { viewModel.navigateTo(AppScreen.Search) },
                                onCategoryClick = { route ->
                                    when (route) {
                                        "channels" -> viewModel.selectTab("channels")
                                        "movies", "series", "anime" -> viewModel.selectTab("entertainment")
                                        else -> viewModel.selectTab("channels")
                                    }
                                },
                                onChannelClick = { channel ->
                                    viewModel.playMedia(
                                        title = channel.name,
                                        subtitle = "${channel.categoryName} • ${channel.country}",
                                        streamUrl = channel.streamUrl,
                                        isLive = true,
                                        servers = channel.getActiveServers()
                                    )
                                },
                                onHeroWatchClick = { banner ->
                                    viewModel.playMedia(
                                        title = banner.title,
                                        subtitle = banner.subtitle,
                                        streamUrl = banner.streamUrl,
                                        isLive = banner.isLive,
                                        servers = banner.getActiveServers()
                                    )
                                },
                                onViewAllChannelsClick = { viewModel.selectTab("channels") },
                                currentTab = currentTab,
                                onTabSelected = { viewModel.selectTab(it) },
                                onFavoriteToggle = { channel ->
                                    viewModel.toggleFavorite(
                                        itemId = channel.id,
                                        title = channel.name,
                                        subtitle = channel.categoryName,
                                        type = "CHANNEL",
                                        streamUrl = channel.streamUrl,
                                        isFav = channel.isFavorite
                                    )
                                },
                                listState = homeListState
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
                                onTabSelected = { viewModel.selectTab(it) },
                                listState = channelsListState
                            )
                        }

                        "matches" -> {
                            MatchesScreen(
                                matches = allMatches,
                                selectedDate = selectedMatchDate,
                                onDateSelected = { date, offset -> viewModel.selectMatchDate(date, offset) },
                                onMatchClick = { match ->
                                    selectedMatchForDetails = match
                                },
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onTelegramClick = openTelegram,
                                onFavoritesClick = { viewModel.selectTab("favorites") },
                                onSearchClick = { viewModel.navigateTo(AppScreen.Search) },
                                currentTab = currentTab,
                                onTabSelected = { viewModel.selectTab(it) },
                                listState = matchesListState
                            )
                        }

                        "entertainment" -> {
                            EntertainmentScreen(
                                movies = featuredMovies,
                                series = featuredSeries,
                                anime = animePicks,
                                entertainmentCategories = entertainmentCategories,
                                vodCategories = vodCategories,
                                seriesCategories = seriesCategories,
                                onCategoryClick = { cat -> viewModel.openMediaCategory(cat) },
                                onMediaClick = handleMediaClick,
                                onFavoriteToggle = handleMediaFavoriteToggle,
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onTelegramClick = openTelegram,
                                onFavoritesClick = { viewModel.selectTab("favorites") },
                                onSearchClick = { viewModel.navigateTo(AppScreen.Search) },
                                currentTab = currentTab,
                                onTabSelected = { viewModel.selectTab(it) },
                                listState = entertainmentListState
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
                                onTabSelected = { viewModel.selectTab(it) },
                                listState = favoritesListState
                            )
                        }
                    }
                }

                is AppScreen.CategoryDetail -> {
                    CategoryDetailScreen(
                        category = screen.category,
                        channels = categoryChannels,
                        isLoading = isCategoryLoading,
                        viewMode = viewMode,
                        onToggleViewMode = { viewModel.toggleViewMode() },
                        onBackClick = { viewModel.popBack() },
                        onRefresh = { viewModel.refreshCategory(screen.category) },
                        onChannelClick = { channel ->
                            viewModel.playMedia(
                                title = channel.name,
                                subtitle = "${channel.categoryName} • ${channel.country}",
                                streamUrl = channel.streamUrl,
                                isLive = true,
                                servers = channel.getActiveServers()
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

                is AppScreen.MediaCategoryDetail -> {
                    MediaCategoryDetailScreen(
                        category = screen.category,
                        mediaList = categoryMediaList,
                        isLoading = isCategoryLoading,
                        onBackClick = { viewModel.popBack() },
                        onRefresh = { viewModel.refreshMediaCategory(screen.category) },
                        onMediaClick = handleMediaClick,
                        onFavoriteToggle = handleMediaFavoriteToggle
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
                                isLive = false,
                                servers = screen.mediaItem.getActiveServers()
                            )
                        },
                        onPlayDirect = {
                            viewModel.playMedia(
                                title = screen.mediaItem.title,
                                subtitle = "${screen.mediaItem.year} • ${screen.mediaItem.genre}",
                                streamUrl = screen.mediaItem.streamUrl,
                                isLive = false,
                                servers = screen.mediaItem.getActiveServers()
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
                        onBackClick = { viewModel.popBack() },
                        servers = screen.servers,
                        availableChannels = allChannels
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
                                isLive = true,
                                servers = channel.getActiveServers()
                            )
                        },
                        onMediaClick = handleMediaClick,
                        onBackClick = { viewModel.popBack() }
                    )
                }
            }
        }
    }

    // Global Anti-VPN Security Overlay (Periodic check every 3 seconds across entire app)
    if (isVpnDetected && currentScreen !is AppScreen.Player) {
        VpnBlockedDialog(
            onRecheckClick = {
                viewModel.recheckVpnNow()
            },
            onExitApp = {
                (context as? android.app.Activity)?.finish()
            }
        )
    }
}
