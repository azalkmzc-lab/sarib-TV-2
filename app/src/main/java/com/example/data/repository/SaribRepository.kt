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

class SaribRepository(context: Context) {

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
            // 1. Fetch remote config & multi-sliders from Firebase
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

            // 2. Fetch Firebase Sliders from independent path (/sliders)
            try {
                val remoteSliders = firebaseStreamManager.fetchSliders()
                if (remoteSliders.isNotEmpty()) {
                    _heroSliders.value = remoteSliders
                    Log.d("SaribRepository", "Loaded ${remoteSliders.size} custom sliders from Firebase.")
                }
            } catch (e: Exception) {
                Log.w("SaribRepository", "Could not load Firebase sliders: ${e.message}")
            }

            // 3. Fetch real live streams, VOD, series and matches from Xtream server and Matches API & Firebase
            coroutineScope {
                val liveCategoriesDeferred = async { xtreamClient.fetchLiveCategories() }
                val liveStreamsDeferred = async { xtreamClient.fetchLiveStreams() }
                val vodStreamsDeferred = async { xtreamClient.fetchVodStreams() }
                val seriesStreamsDeferred = async { xtreamClient.fetchSeries() }
                val matchesDeferred = async { matchesClient.fetchMatches(0) }
                val customCatsDeferred = async { firebaseStreamManager.fetchCustomCategories() }
                val customChannelsDeferred = async { firebaseStreamManager.fetchCustomChannels() }
                val customMoviesDeferred = async { firebaseStreamManager.fetchCustomMovies() }

                val remoteCategories = liveCategoriesDeferred.await()
                val remoteStreams = liveStreamsDeferred.await()
                val remoteMovies = vodStreamsDeferred.await()
                val remoteSeries = seriesStreamsDeferred.await()
                val remoteMatches = matchesDeferred.await()
                val customCats = customCatsDeferred.await()
                val customChannels = customChannelsDeferred.await()
                val customMovies = customMoviesDeferred.await()

                val allCats = (customCats + remoteCategories).distinctBy { it.id }
                val allChans = (customChannels + remoteStreams).distinctBy { it.id }
                val allMovs = (customMovies + remoteMovies).distinctBy { it.id }

                if (allCats.isNotEmpty()) {
                    dao.insertCategories(allCats.map { it.toEntity() })
                }
                if (allChans.isNotEmpty()) {
                    dao.insertChannels(allChans.map { it.toEntity() })
                }
                if (allMovs.isNotEmpty()) {
                    dao.insertMediaItems(allMovs.map { it.toEntity() })
                }
                if (remoteSeries.isNotEmpty()) {
                    dao.insertMediaItems(remoteSeries.map { it.toEntity() })
                }
                if (remoteMatches.isNotEmpty()) {
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

                    // Add top movie from real server data if available
                    remoteMovies.firstOrNull()?.let { movie ->
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

                    // Add top live channel from real server data if available
                    remoteStreams.firstOrNull()?.let { ch ->
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
            }

            Result.success(true)
        } catch (e: Exception) {
            Log.e("SaribRepository", "Init backend sync error: ${e.message}", e)
            Result.success(true)
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

    fun getAllCategories(): Flow<List<ChannelCategory>> {
        return dao.getAllCategories().map { list ->
            list.map { it.toModel() }
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
