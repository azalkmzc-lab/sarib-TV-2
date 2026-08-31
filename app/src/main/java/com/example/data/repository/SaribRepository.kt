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
import com.example.data.remote.MatchesApiClient
import com.example.data.remote.XtreamApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SaribRepository(context: Context) {

    private val db = SaribDatabase.getDatabase(context)
    private val dao = db.saribDao()

    private val xtreamClient = XtreamApiClient(
        serverHost = "http://cliccck52258.club:2082",
        username = "khaledsliman",
        password = "755246419856"
    )
    private val matchesClient = MatchesApiClient()

    companion object {
        const val SAMPLE_STREAM_HLS_1 = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
        const val SAMPLE_STREAM_HLS_2 = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"
    }

    suspend fun initializeBackendConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // 1. Check if DB has existing data or seed initial fallback
            val channelsCount = dao.getChannelsCount()
            if (channelsCount == 0) {
                seedInitialData()
            }

            // 2. Fetch live data from Xtream Server and Matches API asynchronously
            coroutineScope {
                val liveCategoriesDeferred = async { xtreamClient.fetchLiveCategories() }
                val liveStreamsDeferred = async { xtreamClient.fetchLiveStreams() }
                val vodStreamsDeferred = async { xtreamClient.fetchVodStreams() }
                val seriesStreamsDeferred = async { xtreamClient.fetchSeries() }
                val matchesDeferred = async { matchesClient.fetchMatches(0) }

                val remoteCategories = liveCategoriesDeferred.await()
                val remoteStreams = liveStreamsDeferred.await()
                val remoteMovies = vodStreamsDeferred.await()
                val remoteSeries = seriesStreamsDeferred.await()
                val remoteMatches = matchesDeferred.await()

                if (remoteCategories.isNotEmpty()) {
                    dao.insertCategories(remoteCategories.map { it.toEntity() })
                }
                if (remoteStreams.isNotEmpty()) {
                    dao.insertChannels(remoteStreams.map { it.toEntity() })
                }
                if (remoteMovies.isNotEmpty()) {
                    dao.insertMediaItems(remoteMovies.map { it.toEntity() })
                }
                if (remoteSeries.isNotEmpty()) {
                    dao.insertMediaItems(remoteSeries.map { it.toEntity() })
                }
                if (remoteMatches.isNotEmpty()) {
                    dao.insertMatches(remoteMatches.map { it.toEntity() })
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            Log.e("SaribRepository", "Init backend sync error: ${e.message}", e)
            // Even if network has hiccups, app will work using stored cached data
            Result.success(true)
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

    private suspend fun seedInitialData() {
        val categories = listOf(
            CategoryEntity("bein_all", "beIN Sports (All)", "48 قناة رياضية", 48, "", "sports", "#9333EA"),
            CategoryEntity("world_sports", "World Sports", "897 قناة رياضية", 897, "", "sports", "#2563EB"),
            CategoryEntity("arabic_channels", "Arabic Channels", "748 قناة ترفيهية", 748, "", "entertainment", "#7C3AED"),
            CategoryEntity("world_countries", "World Countries", "4588 قناة ترفيهية", 4588, "", "entertainment", "#059669"),
            CategoryEntity("thmanyah", "Thmanyah", "6 قنوات رياضية", 6, "", "sports", "#DC2626"),
            CategoryEntity("alwan_sports", "Alwan Sports (AR)", "18 قناة رياضية", 18, "", "sports", "#65A30D"),
            CategoryEntity("arabic_sports", "Arabic Sports", "77 قناة رياضية", 77, "", "sports", "#0D9488"),
            CategoryEntity("movies_hub", "Cinema & Movies", "1250 فيلم ومسلسل", 1250, "", "movies", "#D97706")
        )
        dao.insertCategories(categories)

        val channels = listOf(
            ChannelEntity("ch_mbc1", "MBC 1", "arabic_channels", "Arabic Channels", "", "http://cliccck52258.club:2082/live/khaledsliman/755246419856/1.m3u8", "", "السعودية", "العربية", false, true, 1, 9840),
            ChannelEntity("ch_thmanyah1", "Thmanyah 1", "thmanyah", "Thmanyah", "", "http://cliccck52258.club:2082/live/khaledsliman/755246419856/2.m3u8", "", "السعودية", "العربية", false, true, 2, 7520),
            ChannelEntity("ch_bein1", "beIN SPORTS 1 HD", "bein_all", "beIN Sports", "", "http://cliccck52258.club:2082/live/khaledsliman/755246419856/3.m3u8", "", "قطر", "العربية", true, true, 3, 14200),
            ChannelEntity("ch_bein2", "beIN SPORTS 2 HD", "bein_all", "beIN Sports", "", "http://cliccck52258.club:2082/live/khaledsliman/755246419856/4.m3u8", "", "قطر", "العربية", false, true, 4, 11800),
            ChannelEntity("ch_bein_premium", "beIN SPORTS Premium 1", "bein_all", "beIN Sports", "", "http://cliccck52258.club:2082/live/khaledsliman/755246419856/5.m3u8", "", "قطر", "العربية", false, true, 5, 13500),
            ChannelEntity("ch_ssc1", "SSC 1 HD", "arabic_sports", "Arabic Sports", "", "http://cliccck52258.club:2082/live/khaledsliman/755246419856/6.m3u8", "", "السعودية", "العربية", false, true, 6, 8900),
            ChannelEntity("ch_ad_sports", "AD Sports 1", "arabic_sports", "Arabic Sports", "", "http://cliccck52258.club:2082/live/khaledsliman/755246419856/7.m3u8", "", "الإمارات", "العربية", false, true, 7, 6200),
            ChannelEntity("ch_mbc_action", "MBC Action", "arabic_channels", "Arabic Channels", "", "http://cliccck52258.club:2082/live/khaledsliman/755246419856/8.m3u8", "", "الإمارات", "العربية", false, true, 8, 8400)
        )
        dao.insertChannels(channels)

        val matches = listOf(
            MatchEntity(
                id = "m_1",
                leagueName = "الدوري الإيطالي",
                leagueIconUrl = "",
                homeTeam = "كالياري",
                homeLogoUrl = "",
                awayTeam = "إنتر ميلان",
                awayLogoUrl = "",
                matchTime = "09:45 م",
                matchDate = "31 أغسطس",
                status = "0 - 1",
                homeScore = 0,
                awayScore = 1,
                streamUrl = "http://cliccck52258.club:2082/live/khaledsliman/755246419856/3.m3u8",
                isLive = true,
                isFavorite = false
            ),
            MatchEntity(
                id = "m_2",
                leagueName = "الدوري الإيطالي",
                leagueIconUrl = "",
                homeTeam = "نابولي",
                homeLogoUrl = "",
                awayTeam = "بولونيا",
                awayLogoUrl = "",
                matchTime = "07:30 م",
                matchDate = "31 أغسطس",
                status = "2 - 1",
                homeScore = 2,
                awayScore = 1,
                streamUrl = "http://cliccck52258.club:2082/live/khaledsliman/755246419856/4.m3u8",
                isLive = false,
                isFavorite = false
            ),
            MatchEntity(
                id = "m_3",
                leagueName = "الدوري الإنجليزي",
                leagueIconUrl = "",
                homeTeam = "أرسنال",
                homeLogoUrl = "",
                awayTeam = "مانشستر سيتي",
                awayLogoUrl = "",
                matchTime = "10:00 م",
                matchDate = "31 أغسطس",
                status = "لم تبدأ",
                homeScore = 0,
                awayScore = 0,
                streamUrl = "http://cliccck52258.club:2082/live/khaledsliman/755246419856/3.m3u8",
                isLive = false,
                isFavorite = false
            )
        )
        dao.insertMatches(matches)

        val mediaList = listOf(
            MediaEntity("mov_spider", "Spider-Man: Brand New Day", "", "", "MOVIE", "2024", "9.1", "أكشن / مغامرات", "مغامرة ملحمية جديدة لسبايدرمان في قلب المدينة.", "135 دقيقة", 1, 1, "http://cliccck52258.club:2082/movie/khaledsliman/755246419856/1.mp4", true, "01", false),
            MediaEntity("mov_john_wick", "John Wick: Chapter 4", "", "", "MOVIE", "2023", "8.9", "أكشن / إثارة", "جون ويك يكتشف طريقاً للتغلب على المجلس الأعلى.", "169 دقيقة", 1, 1, "http://cliccck52258.club:2082/movie/khaledsliman/755246419856/2.mp4", true, "02", false),
            MediaEntity("mov_last_house", "The Last House", "", "", "MOVIE", "2024", "8.4", "رعب / غموض", "أحداث غامضة في منزل ريفي منعزل في الليل.", "110 دقيقة", 1, 1, "http://cliccck52258.club:2082/movie/khaledsliman/755246419856/3.mp4", true, "03", false),
            MediaEntity("ser_last_of_us", "The Last of Us S2", "", "", "SERIES", "2024", "9.4", "دراما / بقاء", "مواصلة رحلة جويل وإيلي في عالم مليء بالمخاطر.", "60 دقيقة", 2, 8, "http://cliccck52258.club:2082/series/khaledsliman/755246419856/1.mp4", true, "01", false),
            MediaEntity("ser_shogun", "Shōgun", "", "", "SERIES", "2024", "9.2", "دراما / تاريخي", "صراع النفوذ والحروب في اليابان الإقطاعية في مطلع القرن 17.", "58 دقيقة", 1, 10, "http://cliccck52258.club:2082/series/khaledsliman/755246419856/2.mp4", true, "02", false),
            MediaEntity("ani_one_piece", "One Piece", "", "", "ANIME", "2024", "9.6", "أنمي / مغامرة", "لوفي وطاقم قبعة القش في طريقهم نحو الكنز الأسطوري.", "24 دقيقة", 21, 1100, "http://cliccck52258.club:2082/series/khaledsliman/755246419856/3.mp4", false, "01", false)
        )
        dao.insertMediaItems(mediaList)
    }

    fun getHeroBanner(): HeroBannerItem {
        return HeroBannerItem(
            id = "hero_from",
            title = "بلدة الضياع S1-S4",
            subtitle = "مسلسل • دراما • رعب • أحجية",
            backdropUrl = "",
            genreTags = listOf("مسلسل", "دراما", "رعب", "أحجية"),
            streamUrl = "http://cliccck52258.club:2082/series/khaledsliman/755246419856/1.mp4",
            contentType = ContentType.SERIES
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
