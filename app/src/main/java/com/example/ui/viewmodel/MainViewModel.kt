package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FavoriteEntity
import com.example.data.model.ChannelCategory
import com.example.data.model.ChannelItem
import com.example.data.model.ContentType
import com.example.data.model.HeroBannerItem
import com.example.data.model.MatchItem
import com.example.data.model.MediaItem
import com.example.data.model.ViewMode
import com.example.data.repository.SaribRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AppScreen {
    object Splash : AppScreen
    object Main : AppScreen
    data class CategoryDetail(val category: ChannelCategory) : AppScreen
    data class MediaCategoryDetail(val category: ChannelCategory) : AppScreen
    data class SeriesDetail(val mediaItem: MediaItem) : AppScreen
    data class Player(
        val title: String,
        val subtitle: String,
        val streamUrl: String,
        val isLive: Boolean = false,
        val servers: List<Pair<String, String>> = emptyList()
    ) : AppScreen
    object Search : AppScreen
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SaribRepository(application)

    // Navigation State & Backstack
    private val backStack = mutableListOf<AppScreen>()
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Splash)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentTab = MutableStateFlow("home")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Splash State
    private val _isConnecting = MutableStateFlow(true)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError.asStateFlow()

    // Home & Content States
    val heroBanner: HeroBannerItem
        get() = repository.getHeroBanner()

    val heroSliders: StateFlow<List<HeroBannerItem>> = repository.heroSliders

    val categories: StateFlow<List<ChannelCategory>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val entertainmentCategories: StateFlow<List<ChannelCategory>> = repository.getEntertainmentCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vodCategories: StateFlow<List<ChannelCategory>> = repository.getVodCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val seriesCategories: StateFlow<List<ChannelCategory>> = repository.getSeriesCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostWatchedChannels: StateFlow<List<ChannelItem>> = repository.getMostWatchedChannels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChannels: StateFlow<List<ChannelItem>> = repository.getAllChannels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMatches: StateFlow<List<MatchItem>> = repository.getAllMatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredMovies: StateFlow<List<MediaItem>> = repository.getMediaByType(ContentType.MOVIE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredSeries: StateFlow<List<MediaItem>> = repository.getMediaByType(ContentType.SERIES)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val animePicks: StateFlow<List<MediaItem>> = repository.getMediaByType(ContentType.ANIME)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<FavoriteEntity>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Category Detail Screen State
    private val _selectedCategory = MutableStateFlow<ChannelCategory?>(null)
    val selectedCategory: StateFlow<ChannelCategory?> = _selectedCategory.asStateFlow()

    private val _categoryChannels = MutableStateFlow<List<ChannelItem>>(emptyList())
    val categoryChannels: StateFlow<List<ChannelItem>> = _categoryChannels.asStateFlow()

    private val _categoryMediaList = MutableStateFlow<List<MediaItem>>(emptyList())
    val categoryMediaList: StateFlow<List<MediaItem>> = _categoryMediaList.asStateFlow()

    private val _isCategoryLoading = MutableStateFlow(false)
    val isCategoryLoading: StateFlow<Boolean> = _isCategoryLoading.asStateFlow()

    // Series Detail Screen State (Episodes & Seasons)
    private val _currentSeriesDetail = MutableStateFlow<com.example.data.model.SeriesDetail?>(null)
    val currentSeriesDetail: StateFlow<com.example.data.model.SeriesDetail?> = _currentSeriesDetail.asStateFlow()

    private val _isSeriesLoading = MutableStateFlow(false)
    val isSeriesLoading: StateFlow<Boolean> = _isSeriesLoading.asStateFlow()

    // View Mode (Grid / List) for channels
    private val _viewMode = MutableStateFlow(ViewMode.GRID)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    // Matches Date Selector
    private val _selectedMatchDate = MutableStateFlow("اليوم")
    val selectedMatchDate: StateFlow<String> = _selectedMatchDate.asStateFlow()

    // Category chips in Home
    private val _selectedHomeChip = MutableStateFlow("الكل")
    val selectedHomeChip: StateFlow<String> = _selectedHomeChip.asStateFlow()

    // Search Query & Results
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<Pair<List<ChannelItem>, List<MediaItem>>> = combine(
        allChannels,
        featuredMovies,
        featuredSeries,
        animePicks,
        _searchQuery
    ) { chs, movs, sers, anis, query ->
        if (query.isBlank()) {
            Pair(emptyList<ChannelItem>(), emptyList<MediaItem>())
        } else {
            val q = query.trim().lowercase()
            val matchedChannels = chs.filter { it.name.lowercase().contains(q) }
            val matchedMedia = (movs + sers + anis).filter { it.title.lowercase().contains(q) }
            Pair(matchedChannels, matchedMedia)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(emptyList(), emptyList()))

    init {
        startConnectionFlow()
    }

    fun startConnectionFlow() {
        viewModelScope.launch {
            _isConnecting.value = true
            _connectionError.value = null
            val result = repository.initializeBackendConnection()
            if (result.isSuccess) {
                _isConnecting.value = false
                _currentScreen.value = AppScreen.Main
            } else {
                _isConnecting.value = false
                val errorMsg = result.exceptionOrNull()?.message?.takeIf { it.isNotBlank() }
                    ?: "لم يتصل بالسيرفر. يرجى التحقق من اتصالك بالإنترنت أو حالة السيرفر."
                _connectionError.value = errorMsg
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        val curr = _currentScreen.value
        if (curr != screen && curr != AppScreen.Splash) {
            backStack.add(curr)
        }
        _currentScreen.value = screen
    }

    fun openSeriesDetails(media: MediaItem) {
        val curr = _currentScreen.value
        if (curr != AppScreen.Splash && curr !is AppScreen.SeriesDetail) {
            backStack.add(curr)
        }
        _currentScreen.value = AppScreen.SeriesDetail(media)
        _currentSeriesDetail.value = null
        _isSeriesLoading.value = true

        viewModelScope.launch {
            try {
                val details = repository.getSeriesDetails(media.id)
                _currentSeriesDetail.value = details
            } catch (e: Exception) {
                _currentSeriesDetail.value = null
            } finally {
                _isSeriesLoading.value = false
            }
        }
    }

    fun popBack(): Boolean {
        val curr = _currentScreen.value
        if (curr is AppScreen.Player) {
            if (backStack.isNotEmpty()) {
                val previous = backStack.removeAt(backStack.lastIndex)
                _currentScreen.value = previous
                return true
            } else {
                _currentScreen.value = AppScreen.Main
                return true
            }
        } else if (curr is AppScreen.SeriesDetail) {
            if (backStack.isNotEmpty()) {
                val previous = backStack.removeAt(backStack.lastIndex)
                _currentScreen.value = previous
                return true
            } else {
                _currentScreen.value = AppScreen.Main
                return true
            }
        } else if (curr is AppScreen.CategoryDetail || curr is AppScreen.MediaCategoryDetail) {
            if (backStack.isNotEmpty()) {
                val previous = backStack.removeAt(backStack.lastIndex)
                _currentScreen.value = previous
                return true
            } else {
                _currentScreen.value = AppScreen.Main
                return true
            }
        } else if (curr is AppScreen.Search) {
            _currentScreen.value = AppScreen.Main
            backStack.clear()
            return true
        } else if (backStack.isNotEmpty()) {
            val previous = backStack.removeAt(backStack.lastIndex)
            _currentScreen.value = previous
            return true
        } else if (curr != AppScreen.Main) {
            _currentScreen.value = AppScreen.Main
            return true
        }
        return false
    }

    fun selectTab(tab: String) {
        _currentTab.value = tab
        backStack.clear()
        _currentScreen.value = AppScreen.Main
    }

    fun openCategory(category: ChannelCategory, forceRefresh: Boolean = false) {
        _selectedCategory.value = category
        val curr = _currentScreen.value
        if (curr != AppScreen.Splash && curr !is AppScreen.CategoryDetail) {
            backStack.add(curr)
        }
        _currentScreen.value = AppScreen.CategoryDetail(category)
        
        viewModelScope.launch {
            _isCategoryLoading.value = true
            try {
                val channels = repository.getChannelsForCategoryOnDemand(category.id, forceRefresh = forceRefresh)
                _categoryChannels.value = channels
            } catch (e: Exception) {
                // Keep current or empty
            } finally {
                _isCategoryLoading.value = false
            }
        }
    }

    fun refreshCategory(category: ChannelCategory) {
        openCategory(category, forceRefresh = true)
    }

    fun openMediaCategory(category: ChannelCategory, forceRefresh: Boolean = false) {
        _selectedCategory.value = category
        val curr = _currentScreen.value
        if (curr != AppScreen.Splash && curr !is AppScreen.MediaCategoryDetail) {
            backStack.add(curr)
        }
        _currentScreen.value = AppScreen.MediaCategoryDetail(category)

        viewModelScope.launch {
            _isCategoryLoading.value = true
            try {
                val media = if (category.categoryType == "series") {
                    repository.getSeriesForCategoryOnDemand(category.id)
                } else {
                    repository.getMoviesForCategoryOnDemand(category.id)
                }
                _categoryMediaList.value = media
            } catch (e: Exception) {
                // Keep current or empty
            } finally {
                _isCategoryLoading.value = false
            }
        }
    }

    fun refreshMediaCategory(category: ChannelCategory) {
        openMediaCategory(category, forceRefresh = true)
    }

    fun toggleViewMode() {
        _viewMode.value = if (_viewMode.value == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
    }

    fun selectMatchDate(date: String, dayOffset: Int = 0) {
        _selectedMatchDate.value = date
        viewModelScope.launch {
            repository.fetchMatchesForDay(dayOffset)
        }
    }

    fun selectHomeChip(chip: String) {
        _selectedHomeChip.value = chip
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playMedia(
        title: String,
        subtitle: String,
        streamUrl: String,
        isLive: Boolean = false,
        servers: List<Pair<String, String>> = emptyList()
    ) {
        val curr = _currentScreen.value
        if (curr != AppScreen.Splash && curr !is AppScreen.Player) {
            backStack.add(curr)
        }
        _currentScreen.value = AppScreen.Player(
            title = title,
            subtitle = subtitle,
            streamUrl = streamUrl,
            isLive = isLive,
            servers = servers
        )
    }

    fun toggleFavorite(itemId: String, title: String, subtitle: String, type: String, streamUrl: String, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(itemId, title, subtitle, type, streamUrl, isFav)
        }
    }

    fun clearDatabaseCache() {
        viewModelScope.launch {
            repository.clearAllCache()
            startConnectionFlow()
        }
    }
}
