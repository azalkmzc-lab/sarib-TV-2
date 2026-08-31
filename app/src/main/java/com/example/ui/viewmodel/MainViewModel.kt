package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FavoriteEntity
import com.example.data.model.AdminLog
import com.example.data.model.ApiSourceConfig
import com.example.data.model.ChannelCategory
import com.example.data.model.ChannelItem
import com.example.data.model.ContentType
import com.example.data.model.HeroBannerItem
import com.example.data.model.MatchItem
import com.example.data.model.MediaItem
import com.example.data.model.ServerStats
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
    data class Player(val title: String, val subtitle: String, val streamUrl: String, val isLive: Boolean = false) : AppScreen
    object Search : AppScreen
    object AdminAuth : AppScreen
    object AdminDashboard : AppScreen
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SaribRepository(application)

    // Navigation State
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
    val heroBanner: HeroBannerItem = repository.getHeroBanner()

    val categories: StateFlow<List<ChannelCategory>> = repository.getAllCategories()
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

    // View Mode (Grid / List) for channels
    private val _viewMode = MutableStateFlow(ViewMode.GRID)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    // Matches Date Selector
    private val _selectedMatchDate = MutableStateFlow("31 أغسطس")
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

    // Admin State
    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    private val _adminStats = MutableStateFlow(ServerStats())
    val adminStats: StateFlow<ServerStats> = _adminStats.asStateFlow()

    val apiSources: StateFlow<List<ApiSourceConfig>> = repository.getApiSources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _adminLogs = MutableStateFlow<List<AdminLog>>(emptyList())
    val adminLogs: StateFlow<List<AdminLog>> = _adminLogs.asStateFlow()

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
                _connectionError.value = "تعذر الاتصال بخادم البث. يرجى التحقق من اتصال الإنترنت."
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectTab(tab: String) {
        _currentTab.value = tab
        _currentScreen.value = AppScreen.Main
    }

    fun openCategory(category: ChannelCategory) {
        _selectedCategory.value = category
        viewModelScope.launch {
            repository.getChannelsByCategory(category.id).collect { list ->
                _categoryChannels.value = list
            }
        }
        _currentScreen.value = AppScreen.CategoryDetail(category)
    }

    fun toggleViewMode() {
        _viewMode.value = if (_viewMode.value == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
    }

    fun selectMatchDate(date: String) {
        _selectedMatchDate.value = date
    }

    fun selectHomeChip(chip: String) {
        _selectedHomeChip.value = chip
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        // Check for hidden admin code trigger
        if (query.trim() == "k4569870" || query.trim() == "admin77") {
            _currentScreen.value = AppScreen.AdminAuth
        }
    }

    fun playMedia(title: String, subtitle: String, streamUrl: String, isLive: Boolean = false) {
        _currentScreen.value = AppScreen.Player(
            title = title,
            subtitle = subtitle,
            streamUrl = streamUrl.ifEmpty { SaribRepository.SAMPLE_STREAM_HLS_1 },
            isLive = isLive
        )
    }

    fun toggleFavorite(itemId: String, title: String, subtitle: String, type: String, streamUrl: String, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(itemId, title, subtitle, type, streamUrl, isFav)
        }
    }

    // Admin Functions
    fun authenticateAdmin(pin: String): Boolean {
        if (pin == "456987" || pin == "admin" || pin == "1234") {
            _isAdminAuthenticated.value = true
            _currentScreen.value = AppScreen.AdminDashboard
            loadAdminStats()
            return true
        }
        return false
    }

    fun logoutAdmin() {
        _isAdminAuthenticated.value = false
        _currentScreen.value = AppScreen.Main
    }

    fun loadAdminStats() {
        viewModelScope.launch {
            _adminStats.value = repository.getAdminServerStats()
            _adminLogs.value = repository.getAdminLogs()
        }
    }

    fun addChannel(channel: ChannelItem) {
        viewModelScope.launch {
            repository.addChannel(channel)
            loadAdminStats()
        }
    }

    fun updateChannel(channel: ChannelItem) {
        viewModelScope.launch {
            repository.updateChannel(channel)
            loadAdminStats()
        }
    }

    fun deleteChannel(channelId: String) {
        viewModelScope.launch {
            repository.deleteChannel(channelId)
            loadAdminStats()
        }
    }

    fun addApiSource(api: ApiSourceConfig) {
        viewModelScope.launch {
            repository.addApiSource(api)
            loadAdminStats()
        }
    }

    fun testApiConnection(baseUrl: String, endpoint: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.testApiConnection(baseUrl, endpoint)
            onResult(success)
        }
    }
}
