package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ContentType
import com.example.data.model.MatchItem
import com.example.data.model.getActiveServers
import com.example.ui.components.DownloadDialog
import com.example.ui.components.MatchDetailsDialog
import com.example.ui.components.SaribBottomNav
import com.example.ui.components.SaribDrawerContent
import com.example.ui.components.SaribTopHeader
import com.example.ui.components.SettingsDialog
import com.example.ui.components.VpnBlockedDialog
import com.example.ui.screens.CategoryDetailScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.EntertainmentScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MatchesScreen
import com.example.ui.screens.MatchDetailScreen
import com.example.ui.screens.NewsScreen
import com.example.ui.screens.ChannelsScreen
import com.example.ui.screens.MediaCategoryDetailScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SeriesDetailScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

data class PendingDownloadItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val posterUrl: String = "",
    val streamUrl: String,
    val servers: List<Pair<String, String>> = emptyList(),
    val contentType: String = "MOVIE"
)

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
    val newsArticles by viewModel.newsList.collectAsState()
    val featuredMovies by viewModel.featuredMovies.collectAsState()
    val featuredSeries by viewModel.featuredSeries.collectAsState()
    val animePicks by viewModel.animePicks.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()

    // Persistent scroll states across navigation and category exits
    val homeListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val channelsListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val matchesListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val newsListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
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
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isImportingM3u by viewModel.isImportingM3u.collectAsState()
    val importStatusMessage by viewModel.importStatusMessage.collectAsState()

    // Download Manager states
    val activeDownloads by viewModel.activeDownloads.collectAsState()
    val completedDownloads by viewModel.completedDownloads.collectAsState()
    val realtimeDownloadProgress by viewModel.realtimeDownloadProgress.collectAsState()
    var pendingDownload by remember { mutableStateOf<PendingDownloadItem?>(null) }

    val visitedTabs = remember { mutableStateListOf("home") }
    LaunchedEffect(currentTab) {
        if (!visitedTabs.contains(currentTab)) {
            visitedTabs.add(currentTab)
        }
    }

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
                servers = media.getActiveServers(),
                posterUrl = media.posterUrl,
                contentType = if (media.type == ContentType.SERIES) "SERIES" else "MOVIE"
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
            },
            onFetchLineups = { id -> viewModel.getMatchLineups(id) },
            onFetchEvents = { id -> viewModel.getMatchEvents(id) }
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

    // DOWNLOAD SELECTION DIALOG
    pendingDownload?.let { item ->
        DownloadDialog(
            title = item.title,
            subtitle = item.subtitle,
            posterUrl = item.posterUrl,
            streamUrl = item.streamUrl,
            servers = item.servers,
            contentType = item.contentType,
            onDismiss = { pendingDownload = null },
            onStartInternalDownload = { quality, subUrl, subName, resolvedUrl ->
                pendingDownload = null
                viewModel.startDownload(
                    id = item.id,
                    title = item.title,
                    subtitle = item.subtitle,
                    posterUrl = item.posterUrl,
                    streamUrl = resolvedUrl,
                    selectedQuality = quality,
                    subtitleUrl = subUrl,
                    subtitleName = subName,
                    contentType = item.contentType
                )
                Toast.makeText(context, "تم بدء تحميل: ${item.title}", Toast.LENGTH_LONG).show()
            },
            onNavigateToDownloads = {
                pendingDownload = null
                viewModel.navigateToDownloads()
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
                    onNavigateToMatches = {
                        scope.launch { drawerState.close() }
                        viewModel.selectTab("matches")
                    },
                    onNavigateToNews = {
                        scope.launch { drawerState.close() }
                        viewModel.selectTab("news")
                    },
                    onNavigateToEntertainment = {
                        scope.launch { drawerState.close() }
                        viewModel.selectTab("entertainment")
                    },
                    onNavigateToFavorites = {
                        scope.launch { drawerState.close() }
                        viewModel.selectTab("favorites")
                    },
                    onNavigateToDownloads = {
                        scope.launch { drawerState.close() }
                        viewModel.navigateToDownloads()
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
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background,
                        topBar = {
                            SaribTopHeader(
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onTelegramClick = openTelegram,
                                onFavoritesClick = { viewModel.selectTab("favorites") },
                                onDownloadsClick = { viewModel.navigateToDownloads() },
                                onSearchClick = { viewModel.navigateTo(AppScreen.Search) },
                                onRefreshClick = { viewModel.refreshAllData() },
                                isRefreshing = isRefreshing
                            )
                        },
                        bottomBar = {
                            SaribBottomNav(
                                currentTab = currentTab,
                                onTabSelected = { viewModel.selectTab(it) }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            // Home Screen Tab
                            if (visitedTabs.contains("home") || currentTab == "home") {
                                val isVisible = currentTab == "home"
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .then(
                                            if (isVisible) Modifier.zIndex(1f)
                                            else Modifier
                                                .size(0.dp)
                                                .graphicsLayer { alpha = 0f }
                                        )
                                ) {
                                    HomeScreen(
                                        heroSliders = heroSliders,
                                        popularChannels = mostWatchedChannels,
                                        movies = featuredMovies.take(5),
                                        series = featuredSeries.take(5),
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
                                                "news" -> viewModel.selectTab("news")
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
                                                servers = channel.getActiveServers(),
                                                posterUrl = channel.logoUrl,
                                                contentType = "CHANNEL"
                                            )
                                        },
                                        onMediaClick = handleMediaClick,
                                        onHeroWatchClick = { banner ->
                                            viewModel.playMedia(
                                                title = banner.title,
                                                subtitle = banner.subtitle,
                                                streamUrl = banner.streamUrl,
                                                isLive = banner.isLive,
                                                servers = banner.getActiveServers(),
                                                posterUrl = banner.backdropUrl,
                                                contentType = if (banner.isLive) "CHANNEL" else "MOVIE"
                                            )
                                        },
                                        onViewAllChannelsClick = { viewModel.selectTab("channels") },
                                        onViewAllEntertainmentClick = { viewModel.selectTab("entertainment") },
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
                                        onMediaFavoriteToggle = handleMediaFavoriteToggle,
                                        listState = homeListState,
                                        showBars = false
                                    )
                                }
                            }

                            // Channels Screen Tab
                            if (visitedTabs.contains("channels") || currentTab == "channels") {
                                val isVisible = currentTab == "channels"
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .then(
                                            if (isVisible) Modifier.zIndex(1f)
                                            else Modifier
                                                .size(0.dp)
                                                .graphicsLayer { alpha = 0f }
                                        )
                                ) {
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
                                        listState = channelsListState,
                                        showBars = false,
                                        defaultM3uUrl = viewModel.getDefaultM3uUrl(),
                                        isImportingM3u = isImportingM3u,
                                        importStatusMessage = importStatusMessage,
                                        onImportM3u = { url, name ->
                                            viewModel.importM3uPlaylist(url, name)
                                        },
                                        onClearImportStatus = {
                                            viewModel.clearImportStatusMessage()
                                        }
                                    )
                                }
                            }

                            // Matches Screen Tab
                            if (visitedTabs.contains("matches") || currentTab == "matches") {
                                val isVisible = currentTab == "matches"
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .then(
                                            if (isVisible) Modifier.zIndex(1f)
                                            else Modifier
                                                .size(0.dp)
                                                .graphicsLayer { alpha = 0f }
                                        )
                                ) {
                                    MatchesScreen(
                                        matches = allMatches,
                                        selectedDate = selectedMatchDate,
                                        onDateSelected = { date, offset -> viewModel.selectMatchDate(date, offset) },
                                        onMatchClick = { match ->
                                            viewModel.openMatchDetail(match)
                                        },
                                        onMenuClick = { scope.launch { drawerState.open() } },
                                        onTelegramClick = openTelegram,
                                        onFavoritesClick = { viewModel.selectTab("favorites") },
                                        onSearchClick = { viewModel.navigateTo(AppScreen.Search) },
                                        currentTab = currentTab,
                                        onTabSelected = { viewModel.selectTab(it) },
                                        listState = matchesListState,
                                        showBars = false
                                    )
                                }
                            }

                            // Entertainment Screen Tab
                            if (visitedTabs.contains("entertainment") || currentTab == "entertainment") {
                                val isVisible = currentTab == "entertainment"
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .then(
                                            if (isVisible) Modifier.zIndex(1f)
                                            else Modifier
                                                .size(0.dp)
                                                .graphicsLayer { alpha = 0f }
                                        )
                                ) {
                                    EntertainmentScreen(
                                        movies = featuredMovies,
                                        series = featuredSeries,
                                        anime = animePicks,
                                        entertainmentCategories = entertainmentCategories,
                                        vodCategories = vodCategories,
                                        seriesCategories = seriesCategories,
                                        watchHistory = watchHistory,
                                        onCategoryClick = { cat -> viewModel.openMediaCategory(cat) },
                                        onMediaClick = handleMediaClick,
                                        onWatchHistoryClick = { historyItem ->
                                            viewModel.playMedia(
                                                title = historyItem.title,
                                                subtitle = historyItem.subtitle,
                                                streamUrl = historyItem.streamUrl,
                                                isLive = historyItem.contentType == "CHANNEL",
                                                servers = emptyList(),
                                                posterUrl = historyItem.posterUrl,
                                                contentType = historyItem.contentType,
                                                initialProgressMs = historyItem.progressMs
                                            )
                                        },
                                        onDeleteWatchHistoryItem = { id -> viewModel.deleteWatchHistoryItem(id) },
                                        onClearWatchHistory = { viewModel.clearWatchHistory() },
                                        onFavoriteToggle = handleMediaFavoriteToggle,
                                        onMenuClick = { scope.launch { drawerState.open() } },
                                        onTelegramClick = openTelegram,
                                        onFavoritesClick = { viewModel.selectTab("favorites") },
                                        onSearchClick = { viewModel.navigateTo(AppScreen.Search) },
                                        currentTab = currentTab,
                                        onTabSelected = { viewModel.selectTab(it) },
                                        listState = entertainmentListState,
                                        showBars = false
                                    )
                                }
                            }

                            // Favorites Screen Tab
                            if (visitedTabs.contains("favorites") || currentTab == "favorites") {
                                val isVisible = currentTab == "favorites"
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .then(
                                            if (isVisible) Modifier.zIndex(1f)
                                            else Modifier
                                                .size(0.dp)
                                                .graphicsLayer { alpha = 0f }
                                        )
                                ) {
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
                                        listState = favoritesListState,
                                        showBars = false
                                    )
                                }
                            }

                            // News Screen Tab
                            if (visitedTabs.contains("news") || currentTab == "news") {
                                val isVisible = currentTab == "news"
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .then(
                                            if (isVisible) Modifier.zIndex(1f)
                                            else Modifier
                                                .size(0.dp)
                                                .graphicsLayer { alpha = 0f }
                                        )
                                ) {
                                    NewsScreen(
                                        newsArticles = newsArticles,
                                        onRefresh = { viewModel.refreshNews() },
                                        onMenuClick = { scope.launch { drawerState.open() } },
                                        onTelegramClick = openTelegram,
                                        onFavoritesClick = { viewModel.selectTab("favorites") },
                                        onSearchClick = { viewModel.navigateTo(AppScreen.Search) },
                                        listState = newsListState
                                    )
                                }
                            }
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
                        onFavoriteToggle = handleMediaFavoriteToggle,
                        onDownloadClick = { media ->
                            pendingDownload = PendingDownloadItem(
                                id = media.id,
                                title = media.title,
                                subtitle = "${media.year} • ${media.genre}",
                                posterUrl = media.posterUrl,
                                streamUrl = media.streamUrl,
                                servers = media.getActiveServers(),
                                contentType = if (media.type == ContentType.SERIES) "SERIES" else "MOVIE"
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
                            val epServers = mutableListOf<Pair<String, String>>()
                            if (ep.streamUrl.isNotBlank()) {
                                epServers.add("السيرفر الأساسي (الحلقة ${ep.episodeNum})" to ep.streamUrl)
                                val baseWithoutExt = ep.streamUrl.substringBeforeLast('.')
                                if (ep.streamUrl.endsWith(".mp4", ignoreCase = true)) {
                                    epServers.add("سيرفر بديل (TS)" to "$baseWithoutExt.ts")
                                    epServers.add("سيرفر بديل (M3U8)" to "$baseWithoutExt.m3u8")
                                    epServers.add("سيرفر بديل (MKV)" to "$baseWithoutExt.mkv")
                                } else if (ep.streamUrl.endsWith(".ts", ignoreCase = true)) {
                                    epServers.add("سيرفر بديل (MP4)" to "$baseWithoutExt.mp4")
                                    epServers.add("سيرفر بديل (M3U8)" to "$baseWithoutExt.m3u8")
                                    epServers.add("سيرفر بديل (MKV)" to "$baseWithoutExt.mkv")
                                } else if (ep.streamUrl.endsWith(".mkv", ignoreCase = true)) {
                                    epServers.add("سيرفر بديل (MP4)" to "$baseWithoutExt.mp4")
                                    epServers.add("سيرفر بديل (TS)" to "$baseWithoutExt.ts")
                                }
                            }
                            viewModel.playMedia(
                                title = epTitle,
                                subtitle = "${screen.mediaItem.title} • الحلقة ${ep.episodeNum}",
                                streamUrl = ep.streamUrl,
                                isLive = false,
                                servers = epServers
                            )
                        },
                        onPlayDirect = {
                            val firstEp = currentSeriesDetail?.seasons?.firstOrNull()?.episodes?.firstOrNull()
                            val streamToPlay = firstEp?.streamUrl ?: screen.mediaItem.streamUrl
                            val directServers = mutableListOf<Pair<String, String>>()
                            if (streamToPlay.isNotBlank()) {
                                directServers.add("السيرفر الأساسي" to streamToPlay)
                            }
                            directServers.addAll(screen.mediaItem.getActiveServers().filter { it.second != streamToPlay })
                            viewModel.playMedia(
                                title = screen.mediaItem.title,
                                subtitle = "${screen.mediaItem.year} • ${screen.mediaItem.genre}",
                                streamUrl = streamToPlay,
                                isLive = false,
                                servers = directServers
                            )
                        },
                        onDownloadEpisode = { ep, epTitle ->
                            val epServers = mutableListOf<Pair<String, String>>()
                            if (ep.streamUrl.isNotBlank()) {
                                epServers.add("السيرفر الأساسي (الحلقة ${ep.episodeNum})" to ep.streamUrl)
                                val baseWithoutExt = ep.streamUrl.substringBeforeLast('.')
                                if (ep.streamUrl.endsWith(".mp4", ignoreCase = true)) {
                                    epServers.add("سيرفر بديل (TS)" to "$baseWithoutExt.ts")
                                    epServers.add("سيرفر بديل (M3U8)" to "$baseWithoutExt.m3u8")
                                    epServers.add("سيرفر بديل (MKV)" to "$baseWithoutExt.mkv")
                                } else if (ep.streamUrl.endsWith(".ts", ignoreCase = true)) {
                                    epServers.add("سيرفر بديل (MP4)" to "$baseWithoutExt.mp4")
                                    epServers.add("سيرفر بديل (M3U8)" to "$baseWithoutExt.m3u8")
                                }
                            }
                            pendingDownload = PendingDownloadItem(
                                id = "ep_${ep.id}",
                                title = epTitle,
                                subtitle = "${screen.mediaItem.title} • الحلقة ${ep.episodeNum}",
                                posterUrl = ep.coverUrl.ifBlank { screen.mediaItem.posterUrl },
                                streamUrl = ep.streamUrl,
                                servers = epServers,
                                contentType = "SERIES"
                            )
                        },
                        onDownloadDirect = {
                            val firstEp = currentSeriesDetail?.seasons?.firstOrNull()?.episodes?.firstOrNull()
                            val streamToDownload = firstEp?.streamUrl ?: screen.mediaItem.streamUrl
                            val directServers = mutableListOf<Pair<String, String>>()
                            if (streamToDownload.isNotBlank()) {
                                directServers.add("السيرفر الأساسي" to streamToDownload)
                            }
                            directServers.addAll(screen.mediaItem.getActiveServers().filter { it.second != streamToDownload })
                            pendingDownload = PendingDownloadItem(
                                id = screen.mediaItem.id,
                                title = screen.mediaItem.title,
                                subtitle = "${screen.mediaItem.year} • ${screen.mediaItem.genre}",
                                posterUrl = screen.mediaItem.posterUrl,
                                streamUrl = streamToDownload,
                                servers = directServers,
                                contentType = "SERIES"
                            )
                        }
                    )
                }

                is AppScreen.Downloads -> {
                    DownloadsScreen(
                        activeDownloads = activeDownloads,
                        completedDownloads = completedDownloads,
                        realtimeProgress = realtimeDownloadProgress,
                        storageInfo = viewModel.getStorageInfo(),
                        onPauseDownload = { id -> viewModel.pauseDownload(id) },
                        onResumeDownload = { item -> viewModel.resumeDownload(item) },
                        onCancelDownload = { id -> viewModel.cancelDownload(id) },
                        onDeleteCompletedDownload = { id -> viewModel.deleteCompletedDownload(id) },
                        onPlayOffline = { item ->
                            viewModel.playMedia(
                                title = item.title,
                                subtitle = item.subtitle.ifEmpty { "تشغيل بدون إنترنت (${item.selectedQuality})" },
                                streamUrl = item.localFilePath,
                                isLive = false,
                                posterUrl = item.posterUrl,
                                contentType = item.contentType
                            )
                        },
                        onNavigateBack = { viewModel.popBack() },
                        onBrowseEntertainment = {
                            viewModel.selectTab("entertainment")
                        }
                    )
                }

                is AppScreen.MatchDetail -> {
                    MatchDetailScreen(
                        match = screen.match,
                        onBackClick = { viewModel.popBack() },
                        onWatchMatch = { match, streamUrl ->
                            viewModel.playMedia(
                                title = "${match.homeTeam} vs ${match.awayTeam}",
                                subtitle = "${match.leagueName} • ${match.commentator.ifEmpty { "بث مباشر" }}",
                                streamUrl = streamUrl,
                                isLive = match.isLive,
                                servers = match.getActiveServers()
                            )
                        },
                        onFetchLineups = { id -> viewModel.getMatchLineups(id) },
                        onFetchEvents = { id -> viewModel.getMatchEvents(id) },
                        onFetchStatistics = { id -> viewModel.getMatchStatistics(id) }
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
                        availableChannels = allChannels,
                        initialProgressMs = screen.initialProgressMs,
                        onProgressUpdate = { progress, duration ->
                            viewModel.updatePlaybackProgress(
                                streamUrl = screen.streamUrl,
                                title = screen.title,
                                progressMs = progress,
                                durationMs = duration
                            )
                        }
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
