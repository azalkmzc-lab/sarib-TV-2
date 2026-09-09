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
import com.example.data.model.MatchStreamOverride
import com.example.data.model.MediaItem
import com.example.data.model.NewsArticle
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
    private val seriesXtreamClient = XtreamApiClient(
        serverHost = currentRemoteConfig.seriesAccount.serverHost,
        username = currentRemoteConfig.seriesAccount.username,
        password = currentRemoteConfig.seriesAccount.password
    )
    private val vodXtreamClient = XtreamApiClient(
        serverHost = currentRemoteConfig.vodAccount.serverHost,
        username = currentRemoteConfig.vodAccount.username,
        password = currentRemoteConfig.vodAccount.password
    )
    private val liveXtreamClient = XtreamApiClient(
        serverHost = currentRemoteConfig.liveXtreamAccount.serverHost,
        username = currentRemoteConfig.liveXtreamAccount.username,
        password = currentRemoteConfig.liveXtreamAccount.password
    )
    private val categorySeriesClients = mutableMapOf<String, XtreamApiClient>()
    private val categoryVodClients = mutableMapOf<String, XtreamApiClient>()

    private val matchesClient = MatchesApiClient(
        apiUrlBase = currentRemoteConfig.matchesApiUrl
    )

    private val _heroSliders = MutableStateFlow<List<HeroBannerItem>>(emptyList())
    val heroSliders: StateFlow<List<HeroBannerItem>> = _heroSliders.asStateFlow()

    private val _newsList = MutableStateFlow<List<NewsArticle>>(emptyList())
    val newsList: StateFlow<List<NewsArticle>> = _newsList.asStateFlow()

    fun getSeriesClientForCategory(categoryId: String?): XtreamApiClient {
        if (!categoryId.isNullOrBlank()) {
            val cleanCatId = categoryId.removePrefix("xt_ser_cat_").removePrefix("series_")
            categorySeriesClients[cleanCatId]?.let { return it }
            categorySeriesClients[categoryId]?.let { return it }
            val account = currentRemoteConfig.seriesCategoriesAccounts[cleanCatId] ?: currentRemoteConfig.seriesCategoriesAccounts[categoryId]
            if (account != null) {
                val client = XtreamApiClient(account.serverHost, account.username, account.password)
                categorySeriesClients[cleanCatId] = client
                return client
            }
        }
        return seriesXtreamClient
    }

    fun getVodClientForCategory(categoryId: String?): XtreamApiClient {
        if (!categoryId.isNullOrBlank()) {
            val cleanCatId = categoryId.removePrefix("xt_vod_cat_").removePrefix("vod_").removePrefix("movies_")
            categoryVodClients[cleanCatId]?.let { return it }
            categoryVodClients[categoryId]?.let { return it }
            val account = currentRemoteConfig.vodCategoriesAccounts[cleanCatId] ?: currentRemoteConfig.vodCategoriesAccounts[categoryId]
            if (account != null) {
                val client = XtreamApiClient(account.serverHost, account.username, account.password)
                categoryVodClients[cleanCatId] = client
                return client
            }
        }
        return vodXtreamClient
    }

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
                seriesXtreamClient.updateCredentials(
                    host = firebaseConfig.seriesAccount.serverHost,
                    user = firebaseConfig.seriesAccount.username,
                    pass = firebaseConfig.seriesAccount.password
                )
                vodXtreamClient.updateCredentials(
                    host = firebaseConfig.vodAccount.serverHost,
                    user = firebaseConfig.vodAccount.username,
                    pass = firebaseConfig.vodAccount.password
                )
                liveXtreamClient.updateCredentials(
                    host = firebaseConfig.liveXtreamAccount.serverHost,
                    user = firebaseConfig.liveXtreamAccount.username,
                    pass = firebaseConfig.liveXtreamAccount.password
                )
                categorySeriesClients.clear()
                firebaseConfig.seriesCategoriesAccounts.forEach { (catId, acc) ->
                    categorySeriesClients[catId] = XtreamApiClient(acc.serverHost, acc.username, acc.password)
                }
                categoryVodClients.clear()
                firebaseConfig.vodCategoriesAccounts.forEach { (catId, acc) ->
                    categoryVodClients[catId] = XtreamApiClient(acc.serverHost, acc.username, acc.password)
                }

                matchesClient.apiUrlBase = firebaseConfig.matchesApiUrl
                Log.d("SaribRepository", "Applied remote config from Firebase: host=${firebaseConfig.serverHost}, seriesHost=${firebaseConfig.seriesAccount.serverHost}, liveXtream=${firebaseConfig.isLiveXtreamEnabled}, channelsApi=${firebaseConfig.isChannelsApiEnabled}")
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

            // 4. Ultra-Fast Startup & Multi-Source Sync
            val syncResult = coroutineScope {
                // Firebase & M3U & API: Channels, Categories, Custom Movies, Matches
                val customCatsDeferred = async { firebaseStreamManager.fetchCustomCategories() }
                val customChannelsDeferred = async { firebaseStreamManager.fetchCustomChannels() }
                val channelsApiDeferred = async {
                    if (currentRemoteConfig.isChannelsApiEnabled && currentRemoteConfig.channelsApiUrl.isNotBlank()) {
                        firebaseStreamManager.fetchChannelsFromApi(currentRemoteConfig.channelsApiUrl)
                    } else emptyList()
                }
                val liveXtreamCatsDeferred = async {
                    if (currentRemoteConfig.isLiveXtreamEnabled && currentRemoteConfig.liveXtreamAccount.serverHost.isNotBlank()) {
                        liveXtreamClient.fetchLiveCategories()
                    } else emptyList()
                }
                val liveXtreamChannelsDeferred = async {
                    if (currentRemoteConfig.isLiveXtreamEnabled && currentRemoteConfig.liveXtreamAccount.serverHost.isNotBlank()) {
                        liveXtreamClient.fetchLiveStreams(limit = 100)
                    } else emptyList()
                }
                val m3uResultDeferred = async {
                    val m3uSources = firebaseStreamManager.fetchM3uSources()
                    val sourcesToFetch = mutableListOf<Pair<String, String>>()
                    if (currentRemoteConfig.m3uPlaylistUrl.isNotBlank()) {
                        sourcesToFetch.add(Pair(currentRemoteConfig.m3uPlaylistUrl, "باقة القنوات المباشرة"))
                    }
                    for (src in m3uSources) {
                        if (src.url.isNotBlank() && src.isEnabled) {
                            sourcesToFetch.add(Pair(src.url, src.name.ifBlank { "باقة M3U سحابية" }))
                        }
                    }

                    // Add user imported playlists from local preferences
                    try {
                        val userM3uList = com.example.data.local.AppPreferences(context).getCustomM3uList()
                        for (userSrc in userM3uList) {
                            if (userSrc.first.isNotBlank()) {
                                sourcesToFetch.add(Pair(userSrc.first, userSrc.second))
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("SaribRepository", "Error reading custom M3U preferences: ${e.message}")
                    }

                    val aggregatedCategories = mutableListOf<com.example.data.model.ChannelCategory>()
                    val aggregatedChannels = mutableListOf<com.example.data.model.ChannelItem>()

                    for ((url, name) in sourcesToFetch.distinctBy { it.first }) {
                        try {
                            val parsed = firebaseStreamManager.fetchM3uPlaylist(url, name)
                            aggregatedCategories.addAll(parsed.categories)
                            aggregatedChannels.addAll(parsed.channels)
                        } catch (e: Exception) {
                            Log.w("SaribRepository", "Error parsing M3U source $url: ${e.message}")
                        }
                    }

                    com.example.util.ParsedM3uResult(
                        categories = aggregatedCategories.distinctBy { it.id },
                        channels = aggregatedChannels.distinctBy { it.id }
                    )
                }
                val customMoviesDeferred = async { firebaseStreamManager.fetchCustomMovies(currentRemoteConfig.moviesApiUrl) }
                val customMovieCategoriesDeferred = async { firebaseStreamManager.fetchCustomMovieCategories() }
                val m3uMoviesDeferred = async {
                    firebaseStreamManager.fetchMoviesFromM3uSources(
                        if (currentRemoteConfig.m3uMoviesUrl.isNotBlank()) listOf(currentRemoteConfig.m3uMoviesUrl) else emptyList()
                    )
                }
                val matchesDeferred = async { fetchMatchesForDay(0) }
                val newsDeferred = async { fetchNews() }

                // Xtream: VOD Movies & Series categories and previews using dedicated accounts
                val vodCategoriesDeferred = async { vodXtreamClient.fetchVodCategories() }
                val seriesCategoriesDeferred = async { seriesXtreamClient.fetchSeriesCategories() }
                val topMoviesDeferred = async { vodXtreamClient.fetchVodStreams(limit = 10) }
                val topSeriesDeferred = async { seriesXtreamClient.fetchSeries(limit = 10) }

                val customCats = customCatsDeferred.await()
                val customChannels = customChannelsDeferred.await()
                val channelsApiChannels = channelsApiDeferred.await()
                val liveXtreamCats = liveXtreamCatsDeferred.await()
                val liveXtreamChannels = liveXtreamChannelsDeferred.await()
                val m3uResult = m3uResultDeferred.await()
                val customMovies = customMoviesDeferred.await()
                val customMovieCategories = customMovieCategoriesDeferred.await()
                val m3uMoviesResult = m3uMoviesDeferred.await()
                val remoteMatches = matchesDeferred.await()
                newsDeferred.await()
                val vodCategories = vodCategoriesDeferred.await()
                val seriesCategories = seriesCategoriesDeferred.await()
                val topMovies = topMoviesDeferred.await()
                val topSeries = topSeriesDeferred.await()

                val combinedChannels = (customChannels + channelsApiChannels + liveXtreamChannels + m3uResult.channels).distinctBy { it.id }
                val m3uParsedMovies = (m3uResult.movies + m3uMoviesResult.movies).distinctBy { it.id }
                val m3uParsedMovieCategories = (m3uResult.movieCategories + m3uMoviesResult.movieCategories).distinctBy { it.id }
                val allCats = (customCats + liveXtreamCats + m3uResult.categories + vodCategories + seriesCategories + customMovieCategories + m3uParsedMovieCategories).distinctBy { it.id }
                val allMovs = (customMovies + m3uParsedMovies + topMovies).distinctBy { it.id }

                // Update local Room database with fresh items
                if (allCats.isNotEmpty()) {
                    dao.clearAllCategories()
                    dao.insertCategories(allCats.map { it.toEntity() })
                }
                if (combinedChannels.isNotEmpty()) {
                    dao.clearAllChannels()
                    combinedChannels.chunked(250).forEach { chunk ->
                        dao.insertChannels(chunk.map { it.toEntity() })
                    }
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
                    // If matches exist, add top live or upcoming match as a Match Slider
                    val topMatch = remoteMatches.firstOrNull { it.isLive } ?: remoteMatches.firstOrNull()
                    if (topMatch != null) {
                        fallbackSliders.add(
                            HeroBannerItem(
                                id = "match_${topMatch.id}",
                                title = "${topMatch.homeTeam} VS ${topMatch.awayTeam}",
                                subtitle = "${topMatch.leagueName} • ${topMatch.matchTime}",
                                backdropUrl = "",
                                badge = if (topMatch.isLive) "مباشر LIVE" else "مباراة القمة",
                                genreTags = listOf("مباراة", topMatch.leagueName.ifBlank { "بث مباشر" }, "FHD"),
                                streamUrl = topMatch.streamUrl,
                                contentType = ContentType.MATCH,
                                isLive = topMatch.isLive,
                                sortOrder = 0,
                                isActive = true,
                                server1 = topMatch.server1,
                                server2 = topMatch.server2,
                                server3 = topMatch.server3,
                                server4 = topMatch.server4,
                                server5 = topMatch.server5,
                                isMatchSlider = true,
                                homeTeam = topMatch.homeTeam,
                                homeLogoUrl = topMatch.homeLogoUrl,
                                awayTeam = topMatch.awayTeam,
                                awayLogoUrl = topMatch.awayLogoUrl,
                                leagueName = topMatch.leagueName,
                                leagueLogoUrl = topMatch.leagueIconUrl,
                                matchTime = topMatch.matchTime,
                                matchDate = topMatch.matchDate,
                                homeScore = topMatch.homeScore,
                                awayScore = topMatch.awayScore,
                                matchStatus = topMatch.status,
                                commentator = topMatch.commentator,
                                channelName = topMatch.channelName
                            )
                        )
                    }

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
                                sortOrder = 1,
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

                val hasRemoteData = customCats.isNotEmpty() || customChannels.isNotEmpty() || channelsApiChannels.isNotEmpty() || liveXtreamChannels.isNotEmpty() || allMovs.isNotEmpty() || topSeries.isNotEmpty()
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
            val cachedCount = try { dao.getChannelsCount() } catch (ex: Exception) { 0 }
            if (cachedCount > 0) {
                Result.success(true)
            } else {
                val msg = e.message ?: "لم يتصل بالسيرفر. يرجى التحقق من اتصال الإنترنت."
                Result.failure(Exception(msg))
            }
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
                return@withContext categoryChannels.map { it.toModel() }
            }

            // On-Demand fetch from Xtream Live if category is from Live Xtream
            val isXtreamCategory = !categoryId.startsWith("m3u_") && !categoryId.startsWith("fb_") && !categoryId.startsWith("api_")
            if (isXtreamCategory && currentRemoteConfig.isLiveXtreamEnabled && currentRemoteConfig.liveXtreamAccount.serverHost.isNotBlank()) {
                try {
                    val remoteChannels = liveXtreamClient.fetchLiveStreams(categoryId = categoryId)
                    if (remoteChannels.isNotEmpty()) {
                        dao.insertChannels(remoteChannels.map { it.toEntity() })
                        return@withContext remoteChannels
                    }
                } catch (e: Exception) {
                    Log.w("SaribRepository", "Xtream Live channels fetch fallback: ${e.message}")
                }
            }

            dao.getAllChannelsList().map { it.toModel() }
        } catch (e: Exception) {
            Log.e("SaribRepository", "Error fetching channels for category $categoryId: ${e.message}", e)
            dao.getAllChannelsList().map { it.toModel() }
        }
    }

    suspend fun getMoviesForCategoryOnDemand(categoryId: String?): List<MediaItem> = withContext(Dispatchers.IO) {
        try {
            // Check local Room cache first (handles M3U, API, and previously loaded movies)
            val localMovies = if (categoryId.isNullOrBlank() || categoryId == "all" || categoryId == "all_movies") {
                dao.getMediaListByType("MOVIE")
            } else {
                val allLocal = dao.getMediaListByType("MOVIE")
                val matched = allLocal.filter {
                    it.genre.contains(categoryId, ignoreCase = true) ||
                    it.id.startsWith(categoryId) ||
                    it.id.contains(categoryId)
                }
                if (matched.isNotEmpty()) matched else emptyList()
            }

            // If category is an Xtream category (numeric or standard Xtream format), query dedicated/per-category Xtream account
            val isXtreamCategory = categoryId != null && !categoryId.startsWith("m3u_") && !categoryId.startsWith("fb_")
            if (isXtreamCategory) {
                try {
                    val client = getVodClientForCategory(categoryId)
                    val remoteMovies = client.fetchVodStreams(categoryId = categoryId)
                    if (remoteMovies.isNotEmpty()) {
                        dao.insertMediaItems(remoteMovies.map { it.toEntity() })
                        return@withContext remoteMovies
                    }
                } catch (e: Exception) {
                    Log.w("SaribRepository", "Xtream VOD fetch fallback: ${e.message}")
                }
            }

            if (localMovies.isNotEmpty()) {
                return@withContext localMovies.map { it.toModel() }
            }

            // Fallback: try fetching custom movies from Firebase/API
            val customMovies = firebaseStreamManager.fetchCustomMovies(currentRemoteConfig.moviesApiUrl)
            if (customMovies.isNotEmpty()) {
                dao.insertMediaItems(customMovies.map { it.toEntity() })
                return@withContext customMovies
            }

            // General fallback to all cached movies
            dao.getMediaListByType("MOVIE").map { it.toModel() }
        } catch (e: Exception) {
            Log.e("SaribRepository", "Error fetching movies for category $categoryId: ${e.message}", e)
            dao.getMediaListByType("MOVIE").map { it.toModel() }
        }
    }

    suspend fun getSeriesForCategoryOnDemand(categoryId: String?): List<MediaItem> = withContext(Dispatchers.IO) {
        try {
            val client = getSeriesClientForCategory(categoryId)
            var remoteSeries = client.fetchSeries(categoryId = categoryId)
            if (remoteSeries.isEmpty() && client != seriesXtreamClient) {
                remoteSeries = seriesXtreamClient.fetchSeries(categoryId = categoryId)
            }
            if (remoteSeries.isEmpty() && client != xtreamClient) {
                remoteSeries = xtreamClient.fetchSeries(categoryId = categoryId)
            }
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

    suspend fun fetchMatchesForDay(dayOffset: Int): List<MatchItem> = withContext(Dispatchers.IO) {
        try {
            val manualMatchesDeferred = async { firebaseStreamManager.fetchManualMatches() }
            val overridesDeferred = async { firebaseStreamManager.fetchMatchStreamOverrides() }
            val apiMatchesDeferred = async { matchesClient.fetchMatches(dayOffset) }

            val manualMatches = manualMatchesDeferred.await()
            val streamOverrides = overridesDeferred.await()
            val apiMatches = apiMatchesDeferred.await()

            val enrichedApiMatches = apiMatches.mapIndexed { index, match ->
                applyMatchOverride(match, index + 1, streamOverrides)
            }

            val combined = (manualMatches + enrichedApiMatches).distinctBy { it.id }
            if (combined.isNotEmpty()) {
                if (dayOffset == 0) {
                    dao.clearAllMatches()
                }
                dao.insertMatches(combined.map { it.toEntity() })
            }
            combined
        } catch (e: Exception) {
            Log.e("SaribRepository", "Error fetching matches for day $dayOffset: ${e.message}", e)
            dao.getAllMatchesList().map { it.toModel() }
        }
    }

    private fun applyMatchOverride(match: MatchItem, matchNumber: Int, overrides: List<MatchStreamOverride>): MatchItem {
        val override = overrides.firstOrNull { ov ->
            ov.isEnabled && (
                ov.matchKey.equals("match_$matchNumber", ignoreCase = true) ||
                ov.matchKey.equals("match$matchNumber", ignoreCase = true) ||
                ov.matchKey.equals("$matchNumber", ignoreCase = true) ||
                ov.matchKey.equals(match.id, ignoreCase = true) ||
                (ov.matchKey.isNotBlank() && (
                    match.homeTeam.contains(ov.matchKey, ignoreCase = true) ||
                    match.awayTeam.contains(ov.matchKey, ignoreCase = true)
                ))
            )
        } ?: return match

        val newS1 = if (override.server1.isNotBlank()) override.server1 else match.server1
        val newS2 = if (override.server2.isNotBlank()) override.server2 else match.server2
        val newS3 = if (override.server3.isNotBlank()) override.server3 else match.server3
        val newS4 = if (override.server4.isNotBlank()) override.server4 else match.server4
        val newS5 = if (override.server5.isNotBlank()) override.server5 else match.server5
        val newStreamUrl = if (override.streamUrl.isNotBlank()) override.streamUrl else listOf(newS1, newS2, newS3, newS4, newS5, match.streamUrl).firstOrNull { it.isNotBlank() } ?: match.streamUrl

        return match.copy(
            streamUrl = newStreamUrl,
            server1 = newS1,
            server2 = newS2,
            server3 = newS3,
            server4 = newS4,
            server5 = newS5,
            commentator = if (override.commentator.isNotBlank()) override.commentator else match.commentator,
            channelName = if (override.channelName.isNotBlank()) override.channelName else match.channelName,
            status = if (override.status.isNotBlank()) override.status else match.status,
            isLive = match.isLive || newStreamUrl.isNotBlank()
        )
    }

    suspend fun fetchNews(): List<NewsArticle> = withContext(Dispatchers.IO) {
        try {
            val apiUrl = if (currentRemoteConfig.isNewsApiEnabled) currentRemoteConfig.newsApiUrl else ""
            val articles = firebaseStreamManager.fetchNews(apiUrl)
            if (articles.isNotEmpty()) {
                _newsList.value = articles
            }
            articles
        } catch (e: Exception) {
            Log.e("SaribRepository", "Error fetching news: ${e.message}", e)
            _newsList.value
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

    // Filter out categories with empty titles / empty indicators, and prepend All Channels if available
    fun getAllCategories(): Flow<List<ChannelCategory>> {
        return dao.getAllCategories().map { list ->
            val filtered = list.filter { it.name.isNotBlank() && it.categoryType !in listOf("movies", "vod", "series") }
                .map { it.toModel() }
            val totalCount = dao.getChannelsCount()
            if (totalCount > 0) {
                val allChannelsCategory = ChannelCategory(
                    id = "all",
                    name = "جميع القنوات المتاحة",
                    subtitle = "$totalCount قناة متوفرة",
                    channelCount = totalCount,
                    iconUrl = "",
                    categoryType = "live",
                    gradientColorHex = "#0088FF"
                )
                listOf(allChannelsCategory) + filtered
            } else {
                filtered
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getDefaultM3uUrl(): String {
        return currentRemoteConfig.m3uPlaylistUrl
    }

    suspend fun importM3uPlaylist(url: String, name: String = ""): Result<Int> = withContext(Dispatchers.IO) {
        val cleanUrl = url.trim()
        if (cleanUrl.isBlank()) {
            return@withContext Result.failure(Exception("يرجى إدخال رابط باقة M3U / M3U8 صالح"))
        }

        try {
            val playlistName = name.trim().ifBlank { "باقة قنوات M3U8" }
            val parsed = com.example.util.M3uPlaylistParser.parseFromUrl(cleanUrl, playlistName)
            val channels = parsed.channels
            val categories = parsed.categories

            if (channels.isEmpty() && parsed.movies.isEmpty()) {
                return@withContext Result.failure(Exception("لم يتم العثور على أي قنوات صالحة في هذا الرابط. تأكد من صحة الرابط وعمله."))
            }

            // Save to persistent user preferences
            val prefs = com.example.data.local.AppPreferences(context)
            prefs.addCustomM3u(cleanUrl, playlistName)

            // Insert into Room database in safe chunks
            if (categories.isNotEmpty()) {
                dao.insertCategories(categories.map { it.toEntity() })
            }
            if (channels.isNotEmpty()) {
                channels.chunked(250).forEach { chunk ->
                    dao.insertChannels(chunk.map { it.toEntity() })
                }
            }
            if (parsed.movies.isNotEmpty()) {
                parsed.movies.chunked(250).forEach { chunk ->
                    dao.insertMediaItems(chunk.map { it.toEntity() })
                }
            }

            val totalImported = channels.size + parsed.movies.size
            Log.i("SaribRepository", "Successfully imported $totalImported items from $cleanUrl")
            Result.success(totalImported)
        } catch (e: Exception) {
            Log.e("SaribRepository", "Error importing M3U playlist: ${e.message}", e)
            Result.failure(Exception(e.message ?: "فشل في سحب القنوات من الرابط"))
        }
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

    fun getWatchHistory(): Flow<List<com.example.data.local.WatchHistoryEntity>> {
        return dao.getWatchHistory().flowOn(Dispatchers.IO)
    }

    suspend fun addToWatchHistory(
        id: String,
        title: String,
        subtitle: String = "",
        posterUrl: String = "",
        streamUrl: String = "",
        contentType: String = "MOVIE",
        progressMs: Long = 0L,
        durationMs: Long = 0L
    ) = withContext(Dispatchers.IO) {
        if (title.isBlank()) return@withContext
        val itemId = id.ifBlank { title }
        val existing = dao.getWatchHistoryById(itemId)
        val finalProgress = if (progressMs > 0L) progressMs else (existing?.progressMs ?: 0L)
        val finalDuration = if (durationMs > 0L) durationMs else (existing?.durationMs ?: 0L)
        dao.insertWatchHistory(
            com.example.data.local.WatchHistoryEntity(
                id = itemId,
                title = title,
                subtitle = subtitle,
                posterUrl = posterUrl.ifBlank { existing?.posterUrl ?: "" },
                streamUrl = streamUrl.ifBlank { existing?.streamUrl ?: "" },
                contentType = contentType,
                watchedAt = System.currentTimeMillis(),
                progressMs = finalProgress,
                durationMs = finalDuration
            )
        )
    }

    suspend fun getWatchHistoryItem(id: String): com.example.data.local.WatchHistoryEntity? = withContext(Dispatchers.IO) {
        dao.getWatchHistoryById(id)
    }

    suspend fun updateWatchProgress(id: String, progressMs: Long, durationMs: Long) = withContext(Dispatchers.IO) {
        if (id.isNotBlank()) {
            dao.updateWatchProgress(id, progressMs, durationMs)
        }
    }

    suspend fun deleteWatchHistoryById(id: String) = withContext(Dispatchers.IO) {
        dao.deleteWatchHistoryById(id)
    }

    suspend fun clearWatchHistory() = withContext(Dispatchers.IO) {
        dao.clearWatchHistory()
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
        // 1. Try with dedicated seriesXtreamClient
        val detail = seriesXtreamClient.fetchSeriesDetails(seriesId)
        if (detail != null && detail.seasons.isNotEmpty()) return@withContext detail

        // 2. Try with main xtreamClient
        val mainDetail = xtreamClient.fetchSeriesDetails(seriesId)
        if (mainDetail != null && mainDetail.seasons.isNotEmpty()) return@withContext mainDetail

        // 3. Try with category-specific clients if available
        for ((_, catClient) in categorySeriesClients) {
            val catDetail = catClient.fetchSeriesDetails(seriesId)
            if (catDetail != null && catDetail.seasons.isNotEmpty()) return@withContext catDetail
        }

        detail ?: mainDetail
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
