package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.CategoryEntity
import com.example.data.local.ChannelEntity
import com.example.data.local.FavoriteEntity
import com.example.data.local.MatchEntity
import com.example.data.local.MediaEntity
import com.example.data.local.SaribDatabase
import com.example.data.model.ChannelCategory
import com.example.data.model.ChannelItem
import com.example.data.model.ContentType
import com.example.data.model.HeroBannerItem
import com.example.data.model.MatchItem
import com.example.data.model.MediaItem
import com.example.data.remote.FirebaseStreamManager
import com.example.data.remote.MatchesApiClient
import com.example.data.remote.RemoteStreamConfig
import com.example.data.remote.XtreamApiClient
import com.example.util.SecurityChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SaribRepository(private val context: Context) {

    private val db = SaribDatabase.getDatabase(context)
    private val dao = db.saribDao()

    private val firebaseStreamManager = FirebaseStreamManager(context)
    private var currentRemoteConfig = RemoteStreamConfig()

    private val xtreamClient = XtreamApiClient(
        serverHost = currentRemoteConfig.serverHost,
        username = currentRemoteConfig.username,
        password = currentRemoteConfig.password
    )
    private val matchesClient = MatchesApiClient(
        apiUrlBase = currentRemoteConfig.matchesApiUrl
    )

    private val _heroSliders = MutableStateFlow<List<HeroBannerItem>>(emptyList())
    val heroSliders: StateFlow<List<HeroBannerItem>> = _heroSliders.asStateFlow()

    suspend fun initializeBackendConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // 1. Security Check
            val securityStatus = SecurityChecker.performSecurityAudit(context)
            if (!securityStatus.isSecure) {
                Log.w("SaribRepository", "Security issue detected: ${securityStatus.message}")
                return@withContext Result.failure(Exception(securityStatus.message))
            }

            // 2. Fetch remote config from Firebase
            try {
                val firebaseConfig = firebaseStreamManager.fetchRemoteConfig()
                currentRemoteConfig = firebaseConfig
                xtreamClient.updateCredentials(
                    host = firebaseConfig.serverHost,
                    user = firebaseConfig.username,
                    pass = firebaseConfig.password
                )
                matchesClient.apiUrlBase = firebaseConfig.matchesApiUrl
                Log.d("SaribRepository", "Applied remote config from Firebase: ${firebaseConfig.serverHost}")
            } catch (e: Exception) {
                Log.w("SaribRepository", "Could not load Firebase config: ${e.message}")
            }

            // 3. Fetch Firebase Sliders from independent path (/sliders)
            try {
                val remoteSliders = firebaseStreamManager.fetchSliders()
                if (remoteSliders.isNotEmpty()) {
                    _heroSliders.value = remoteSliders
                    Log.d("SaribRepository", "Loaded ${remoteSliders.size} custom sliders from Firebase.")
                }
            } catch (e: Exception) {
                Log.w("SaribRepository", "Could not load Firebase sliders: ${e.message}")
            }

            // 4. Ultra-Fast Startup: Channels exclusively from Firebase, Xtream exclusively for VOD Movies & Series
            val syncResult = coroutineScope {
                // Firebase: Channels, Categories, Custom Movies, Matches
                val customCatsDeferred = async { firebaseStreamManager.fetchCustomCategories() }
                val customChannelsDeferred = async { firebaseStreamManager.fetchCustomChannels() }
                val customMoviesDeferred = async { firebaseStreamManager.fetchCustomMovies() }
                val matchesDeferred = async { matchesClient.fetchMatches(0) }

                // Xtream: VOD Movies & Series categories and previews only
                val vodCategoriesDeferred = async { xtreamClient.fetchVodCategories() }
                val seriesCategoriesDeferred = async { xtreamClient.fetchSeriesCategories() }
                val topMoviesDeferred = async { xtreamClient.fetchVodStreams(limit = 10) }
                val topSeriesDeferred = async { xtreamClient.fetchSeries(limit = 10) }

                val customCats = customCatsDeferred.await()
                val customChannels = customChannelsDeferred.await()
                val customMovies = customMoviesDeferred.await()
                val remoteMatches = matchesDeferred.await()
                val vodCategories = vodCategoriesDeferred.await()
                val seriesCategories = seriesCategoriesDeferred.await()
                val topMovies = topMoviesDeferred.await()
                val topSeries = topSeriesDeferred.await()

                val allCats = (customCats + vodCategories + seriesCategories).distinctBy { it.id }
                val allMovs = (customMovies + topMovies).distinctBy { it.id }

                // Update local Room database with fresh items
                if (allCats.isNotEmpty()) {
                    dao.clearAllCategories()
                    dao.insertCategories(allCats.map { it.toEntity() })
                }
                if (customChannels.isNotEmpty()) {
                    dao.clearAllChannels()
                    dao.insertChannels(customChannels.map { it.toEntity() })
                }
                if (allMovs.isNotEmpty() || topSeries.isNotEmpty()) {
                    dao.clearAllMedia()
                    if (allMovs.isNotEmpty()) {
                        dao.insertMediaItems(allMovs.map { it.toEntity() })
                    }
                    if (topSeries.isNotEmpty()) {
                        dao.insertMediaItems(topSeries.map { it.toEntity() })
                    }
                }
                if (remoteMatches.isNotEmpty()) {
                    dao.clearAllMatches()
                    dao.insertMatches(remoteMatches.map { it.toEntity() })
                }

                // If no custom sliders were defined in Firebase, construct dynamic sliders from top real media/streams
                if (_heroSliders.value.isEmpty()) {
                    val fallbackSliders = mutableListOf<HeroBannerItem>()
                    if (currentRemoteConfig.heroTitle.isNotBlank()) {
                        fallbackSliders.add(
                            HeroBannerItem(
                                id = "hero_main",
                                title = currentRemoteConfig.heroTitle,
                                subtitle = currentRemoteConfig.heroSubtitle,
                                backdropUrl = "",
                                badge = "مميز",
                                genreTags = listOf("مسلسل", "دراما", "أكشن"),
                                streamUrl = currentRemoteConfig.heroStreamUrl,
                                contentType = ContentType.SERIES,
                                isLive = false,
                                sortOrder = 0,
                                isActive = true
                            )
                        )
                    }

                    allMovs.firstOrNull()?.let { movie ->
                        fallbackSliders.add(
                            HeroBannerItem(
                                id = movie.id,
                                title = movie.title,
                                subtitle = "${movie.year} • ${movie.genre}",
                                backdropUrl = movie.backdropUrl.ifEmpty { movie.posterUrl },
                                badge = "سينما VIP",
                                genreTags = listOf("فيلم", "HD"),
                                streamUrl = movie.streamUrl,
                                contentType = ContentType.MOVIE,
                                isLive = false,
                                sortOrder = 1,
                                isActive = true
                            )
                        )
                    }

                    customChannels.firstOrNull()?.let { ch ->
                        fallbackSliders.add(
                            HeroBannerItem(
                                id = ch.id,
                                title = ch.name,
                                subtitle = "${ch.categoryName} • بث مباشر عالي الدقة",
                                backdropUrl = ch.logoUrl,
                                badge = "مباشر LIVE",
                                genreTags = listOf("قناة", "مباشر"),
                                streamUrl = ch.streamUrl,
                                contentType = ContentType.CHANNEL,
                                isLive = true,
                                sortOrder = 2,
                                isActive = true
                            )
                        )
                    }

                    if (fallbackSliders.isNotEmpty()) {
                        _heroSliders.value = fallbackSliders
                    }
                }

                val hasRemoteData = customCats.isNotEmpty() || customChannels.isNotEmpty() || allMovs.isNotEmpty() || topSeries.isNotEmpty()
                hasRemoteData
            }

            if (syncResult) {
                Result.success(true)
            } else {
                val localCats = dao.getChannelsCount()
                if (localCats > 0) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("لم يتصل بالسيرفر. يرجى التحقق من اتصالك بالإنترنت أو حالة السيرفر."))
                }
            }
        } catch (e: Exception) {
            Log.e("SaribRepository", "Init backend sync error: ${e.message}", e)
            val msg = e.message ?: "لم يتصل بالسيرفر. يرجى التحقق من اتصال الإنترنت."
            Result.failure(Exception(msg))
        }
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        dao.clearAllChannels()
        dao.clearAllCategories()
        dao.clearAllMedia()
        dao.clearAllMatches()
    }

    suspend fun getChannelsForCategoryOnDemand(categoryId: String, forceRefresh: Boolean = false): List<ChannelItem> = withContext(Dispatchers.IO) {
        try {
            if (forceRefresh) {
                val freshFirebaseChannels = firebaseStreamManager.fetchCustomChannels()
                if (freshFirebaseChannels.isNotEmpty()) {
                    dao.clearAllChannels()
                    dao.insertChannels(freshFirebaseChannels.map { it.toEntity() })
                }
            }

            if (categoryId.isBlank() || categoryId == "all" || categoryId == "custom") {
                return@withContext dao.getAllChannelsList().map { it.toModel() }
            }

            val categoryChannels = dao.getChannelsListByCategory(categoryId)
            if (categoryChannels.isNotEmpty()) {
                categoryChannels.map { it.toModel() }
            } else {
                dao.getAllChannelsList().map { it.toModel() }
            }
        } catch (e: Exception) {
            Log.e("SaribRepository", "Error fetching channels for category $categoryId: ${e.message}", e)
            dao.getAllChannelsList().map { it.toModel() }
        }
    }

    suspend fun getMoviesForCategoryOnDemand(categoryId: String?): List<MediaItem> = withContext(Dispatchers.IO) {
        try {
            val remoteMovies = xtreamClient.fetchVodStreams(categoryId = categoryId)
            if (remoteMovies.isNotEmpty()) {
                dao.insertMediaItems(remoteMovies.map { it.toEntity() })
            }
            remoteMovies
        } catch (e: Exception) {
            Log.e("SaribRepository", "Error fetching movies for category $categoryId: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getSeriesForCategoryOnDemand(categoryId: String?): List<MediaItem> = withContext(Dispatchers.IO) {
        try {
            val remoteSeries = xtreamClient.fetchSeries(categoryId = categoryId)
            if (remoteSeries.isNotEmpty()) {
                dao.insertMediaItems(remoteSeries.map { it.toEntity() })
            }
            remoteSeries
        } catch (e: Exception) {
            Log.e("SaribRepository", "Error fetching series for category $categoryId: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun refreshSliders() = withContext(Dispatchers.IO) {
        try {
            val remoteSliders = firebaseStreamManager.fetchSliders()
            if (remoteSliders.isNotEmpty()) {
                _heroSliders.value = remoteSliders
            }
        } catch (e: Exception) {
            Log.e("SaribRepository", "Error refreshing sliders: ${e.message}", e)
        }
    }

    suspend fun fetchMatchesForDay(dayOffset: Int) = withContext(Dispatchers.IO) {
        try {
            val matches = matchesClient.fetchMatches(dayOffset)
            if (matches.isNotEmpty()) {
                dao.insertMatches(matches.map { it.toEntity() })
            }
        } catch (e: Exception) {
            Log.e("SaribRepository", "Error fetching matches for day $dayOffset: ${e.message}", e)
        }
    }

    fun getHeroBanner(): HeroBannerItem {
        return _heroSliders.value.firstOrNull() ?: HeroBannerItem(
            id = "hero_default",
            title = currentRemoteConfig.heroTitle,
            subtitle = currentRemoteConfig.heroSubtitle,
            backdropUrl = "",
            badge = "حصري",
            genreTags = listOf("مسلسل", "دراما", "رعب"),
            streamUrl = currentRemoteConfig.heroStreamUrl,
            contentType = ContentType.SERIES,
            isLive = false,
            sortOrder = 0,
            isActive = true
        )
    }

    // Filter out categories with empty titles / empty indicators
    fun getAllCategories(): Flow<List<ChannelCategory>> {
        return dao.getAllCategories().map { list ->
            list.filter { it.name.isNotBlank() && it.categoryType !in listOf("movies", "vod", "series") }
                .map { it.toModel() }
        }.flowOn(Dispatchers.IO)
    }

    fun getEntertainmentCategories(): Flow<List<ChannelCategory>> {
        return dao.getCategoriesByTypes(listOf("movies", "vod", "series", "entertainment", "anime")).map { list ->
            list.filter { it.name.isNotBlank() }.map { it.toModel() }
        }.flowOn(Dispatchers.IO)
    }

    fun getVodCategories(): Flow<List<ChannelCategory>> {
        return dao.getCategoriesByTypes(listOf("movies", "vod")).map { list ->
            list.filter { it.name.isNotBlank() }.map { it.toModel() }
        }.flowOn(Dispatchers.IO)
    }

    fun getSeriesCategories(): Flow<List<ChannelCategory>> {
        return dao.getCategoriesByType("series").map { list ->
            list.filter { it.name.isNotBlank() }.map { it.toModel() }
        }.flowOn(Dispatchers.IO)
    }

    fun getChannelsByCategory(categoryId: String): Flow<List<ChannelItem>> {
        return dao.getChannelsByCategory(categoryId).map { list ->
            list.map { it.toModel() }
        }.flowOn(Dispatchers.IO)
    }

    fun getMostWatchedChannels(): Flow<List<ChannelItem>> {
        return dao.getMostWatchedChannels().map { list ->
            list.map { it.toModel() }
        }.flowOn(Dispatchers.IO)
    }

    fun getAllChannels(): Flow<List<ChannelItem>> {
        return dao.getAllChannels().map { list ->
            list.map { it.toModel() }
        }.flowOn(Dispatchers.IO)
    }

    fun searchChannels(query: String): Flow<List<ChannelItem>> {
        return dao.searchChannels(query).map { list ->
            list.map { it.toModel() }
        }.flowOn(Dispatchers.IO)
    }

    fun getAllMatches(): Flow<List<MatchItem>> {
        return dao.getAllMatches().map { list ->
            list.map { it.toModel() }
        }.flowOn(Dispatchers.IO)
    }

    fun getMatchesByDate(date: String): Flow<List<MatchItem>> {
        return dao.getMatchesByDate(date).map { list ->
            list.map { it.toModel() }
        }.flowOn(Dispatchers.IO)
    }

    fun getMediaByType(type: ContentType): Flow<List<MediaItem>> {
        return dao.getMediaByType(type.name).map { list ->
            list.map { it.toModel() }
        }.flowOn(Dispatchers.IO)
    }

    fun getTopMedia(): Flow<List<MediaItem>> {
        return dao.getTopMedia().map { list ->
            list.map { it.toModel() }
        }.flowOn(Dispatchers.IO)
    }

    fun searchMedia(query: String): Flow<List<MediaItem>> {
        return dao.searchMedia(query).map { list ->
            list.map { it.toModel() }
        }.flowOn(Dispatchers.IO)
    }

    fun getFavorites(): Flow<List<FavoriteEntity>> {
        return dao.getAllFavorites().flowOn(Dispatchers.IO)
    }

    suspend fun toggleFavorite(itemId: String, title: String, subtitle: String, type: String, streamUrl: String, isFav: Boolean) {
        withContext(Dispatchers.IO) {
            if (isFav) {
                dao.removeFavorite(itemId)
            } else {
                dao.addFavorite(
                    FavoriteEntity(
                        itemId = itemId,
                        itemType = type,
                        title = title,
                        subtitle = subtitle,
                        imageUrl = "",
                        streamUrl = streamUrl
                    )
                )
            }
        }
    }

    fun isFavorite(id: String): Flow<Boolean> {
        return dao.isFavorite(id).flowOn(Dispatchers.IO)
    }

    suspend fun getSeriesDetails(seriesId: String): com.example.data.model.SeriesDetail? = withContext(Dispatchers.IO) {
        xtreamClient.fetchSeriesDetails(seriesId)
    }

    suspend fun clearAllCache() = withContext(Dispatchers.IO) {
        dao.clearAllChannels()
        dao.clearAllCategories()
        dao.clearAllMatches()
        dao.clearAllMedia()
    }
}

// Extension converters
fun CategoryEntity.toModel() = ChannelCategory(id, name, subtitle, channelCount, iconUrl, categoryType, gradientColorHex)
fun ChannelCategory.toEntity() = CategoryEntity(id, name, subtitle, channelCount, iconUrl, categoryType, gradientColorHex)
fun ChannelEntity.toModel() = ChannelItem(id, name, categoryId, categoryName, logoUrl, streamUrl, backupUrl, country, language, isFavorite, isEnabled, sortOrder, viewsCount)
fun ChannelItem.toEntity() = ChannelEntity(id, name, categoryId, categoryName, logoUrl, streamUrl, backupUrl, country, language, isFavorite, isEnabled, sortOrder, viewsCount)
fun MatchEntity.toModel() = MatchItem(id, leagueName, leagueIconUrl, homeTeam, homeLogoUrl, awayTeam, awayLogoUrl, matchTime, matchDate, status, homeScore, awayScore, streamUrl, isLive, isFavorite)
fun MatchItem.toEntity() = MatchEntity(id, leagueName, leagueIconUrl, homeTeam, homeLogoUrl, awayTeam, awayLogoUrl, matchTime, matchDate, status, homeScore, awayScore, streamUrl, isLive, isFavorite)
fun MediaEntity.toModel() = MediaItem(id, title, posterUrl, backdropUrl, ContentType.valueOf(type), year, rating, genre, description, duration, seasonsCount, episodesCount, streamUrl, isTop, topRank, isFavorite)
fun MediaItem.toEntity() = MediaEntity(id, title, posterUrl, backdropUrl, type.name, year, rating, genre, description, duration, seasonsCount, episodesCount, streamUrl, isTop, topRank, isFavorite)
