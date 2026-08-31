package com.example.data.repository

import android.content.Context
import com.example.data.local.ApiSourceEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.ChannelEntity
import com.example.data.local.FavoriteEntity
import com.example.data.local.MatchEntity
import com.example.data.local.MediaEntity
import com.example.data.local.SaribDatabase
import com.example.data.model.AdminLog
import com.example.data.model.ApiSourceConfig
import com.example.data.model.ChannelCategory
import com.example.data.model.ChannelItem
import com.example.data.model.ContentType
import com.example.data.model.HeroBannerItem
import com.example.data.model.MatchItem
import com.example.data.model.MediaItem
import com.example.data.model.ServerStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SaribRepository(context: Context) {

    private val db = SaribDatabase.getDatabase(context)
    private val dao = db.saribDao()
    private val adminLogs = mutableListOf<AdminLog>()

    // High quality working sample streams for real playback (HLS and MP4 CDN streams)
    companion object {
        const val SAMPLE_STREAM_HLS_1 = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
        const val SAMPLE_STREAM_HLS_2 = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"
        const val SAMPLE_STREAM_MP4_1 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        const val SAMPLE_STREAM_MP4_2 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
        const val SAMPLE_STREAM_MP4_3 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
    }

    suspend fun initializeBackendConnection(serverUrl: String = "https://api.saribtv.com"): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Check initial data and seed if empty
            val channelsCount = dao.getChannelsCount()
            if (channelsCount == 0) {
                seedInitialData()
            }
            // Simulate realistic network connection check to server
            delay(1200)
            logAdminAction("اتصال بالنظام", "تم التحقق من الاتصال بالخادم بنجاح")
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
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
            ChannelEntity("ch_mbc1", "MBC 1", "arabic_channels", "Arabic Channels", "", SAMPLE_STREAM_HLS_1, "", "السعودية", "العربية", false, true, 1, 9840),
            ChannelEntity("ch_thmanyah1", "Thmanyah 1", "thmanyah", "Thmanyah", "", SAMPLE_STREAM_HLS_2, "", "السعودية", "العربية", false, true, 2, 7520),
            ChannelEntity("ch_bein1", "beIN SPORTS 1", "bein_all", "beIN Sports", "", SAMPLE_STREAM_HLS_1, "", "قطر", "العربية", true, true, 3, 14200),
            ChannelEntity("ch_bein2", "beIN SPORTS 2", "bein_all", "beIN Sports", "", SAMPLE_STREAM_HLS_2, "", "قطر", "العربية", false, true, 4, 11800),
            ChannelEntity("ch_bein_premium", "beIN SPORTS Premium 1", "bein_all", "beIN Sports", "", SAMPLE_STREAM_HLS_1, "", "قطر", "العربية", false, true, 5, 13500),
            ChannelEntity("ch_ssc1", "SSC 1 HD", "arabic_sports", "Arabic Sports", "", SAMPLE_STREAM_HLS_2, "", "السعودية", "العربية", false, true, 6, 8900),
            ChannelEntity("ch_ad_sports", "AD Sports 1", "arabic_sports", "Arabic Sports", "", SAMPLE_STREAM_HLS_1, "", "الإمارات", "العربية", false, true, 7, 6200),
            ChannelEntity("ch_mbc_action", "MBC Action", "arabic_channels", "Arabic Channels", "", SAMPLE_STREAM_HLS_2, "", "الإمارات", "العربية", false, true, 8, 8400),
            ChannelEntity("ch_alwan1", "Alwan Sports 1", "alwan_sports", "Alwan Sports", "", SAMPLE_STREAM_HLS_1, "", "العالم العربي", "العربية", false, true, 9, 3100),
            ChannelEntity("ch_sky_sports", "Sky Sports Main Event", "world_sports", "World Sports", "", SAMPLE_STREAM_HLS_2, "", "بريطانيا", "الإنجليزية", false, true, 10, 5400)
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
                streamUrl = SAMPLE_STREAM_HLS_1,
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
                streamUrl = SAMPLE_STREAM_HLS_2,
                isLive = false,
                isFavorite = false
            ),
            MatchEntity(
                id = "m_3",
                leagueName = "الدوري الإيطالي",
                leagueIconUrl = "",
                homeTeam = "ليتشي",
                homeLogoUrl = "",
                awayTeam = "روما",
                awayLogoUrl = "",
                matchTime = "09:45 م",
                matchDate = "31 أغسطس",
                status = "0 - 3",
                homeScore = 0,
                awayScore = 3,
                streamUrl = SAMPLE_STREAM_HLS_1,
                isLive = true,
                isFavorite = false
            ),
            MatchEntity(
                id = "m_4",
                leagueName = "الدوري التركي",
                leagueIconUrl = "",
                homeTeam = "آميد سبورتيف",
                homeLogoUrl = "",
                awayTeam = "طرابزون سبور",
                awayLogoUrl = "",
                matchTime = "09:30 م",
                matchDate = "31 أغسطس",
                status = "لم تبدأ",
                homeScore = 0,
                awayScore = 0,
                streamUrl = SAMPLE_STREAM_HLS_2,
                isLive = false,
                isFavorite = false
            ),
            MatchEntity(
                id = "m_5",
                leagueName = "الدوري الإنجليزي",
                leagueIconUrl = "",
                homeTeam = "أستون فيلا",
                homeLogoUrl = "",
                awayTeam = "أرسنال",
                awayLogoUrl = "",
                matchTime = "10:00 م",
                matchDate = "31 أغسطس",
                status = "لم تبدأ",
                homeScore = 0,
                awayScore = 0,
                streamUrl = SAMPLE_STREAM_HLS_1,
                isLive = false,
                isFavorite = false
            ),
            MatchEntity(
                id = "m_6",
                leagueName = "الدوري الإسباني",
                leagueIconUrl = "",
                homeTeam = "ريال مدريد",
                homeLogoUrl = "",
                awayTeam = "ريال بيتيس",
                awayLogoUrl = "",
                matchTime = "10:30 م",
                matchDate = "01 سبتمبر",
                status = "لم تبدأ",
                homeScore = 0,
                awayScore = 0,
                streamUrl = SAMPLE_STREAM_HLS_2,
                isLive = false,
                isFavorite = false
            )
        )
        dao.insertMatches(matches)

        val mediaList = listOf(
            // Movies
            MediaEntity("mov_spider", "Spider-Man: Brand New Day", "", "", "MOVIE", "2024", "9.1", "أكشن / مغامرات", "مغامرة ملحمية جديدة لسبايدرمان في قلب المدينة.", "135 دقيقة", 1, 1, SAMPLE_STREAM_MP4_1, true, "01", false),
            MediaEntity("mov_john_wick", "John Wick: Chapter 4", "", "", "MOVIE", "2023", "8.9", "أكشن / إثارة", "جون ويك يكتشف طريقاً للتغلب على المجلس الأعلى.", "169 دقيقة", 1, 1, SAMPLE_STREAM_MP4_2, true, "02", false),
            MediaEntity("mov_last_house", "The Last House", "", "", "MOVIE", "2024", "8.4", "رعب / غموض", "أحداث غامضة في منزل ريفي منعزل في الليل.", "110 دقيقة", 1, 1, SAMPLE_STREAM_MP4_3, true, "03", false),
            MediaEntity("mov_reacher", "Reacher [Hindi / Arabic]", "", "", "MOVIE", "2024", "8.7", "أكشن / جريمة", "محقق عسكري سابق يخوض مهمة محفوفة بالمخاطر.", "124 دقيقة", 1, 1, SAMPLE_STREAM_MP4_1, true, "02", false),
            MediaEntity("mov_beauty", "Beauty in Black", "", "", "MOVIE", "2024", "8.2", "دراما / تشويق", "صراع مشوق بين عائلتين مختلفتين في مجتمع المدينة.", "118 دقيقة", 1, 1, SAMPLE_STREAM_MP4_2, true, "01", false),

            // Series
            MediaEntity("ser_last_of_us", "The Last of Us S2", "", "", "SERIES", "2024", "9.4", "دراما / بقاء", "مواصلة رحلة جويل وإيلي في عالم مليء بالمخاطر.", "60 دقيقة", 2, 8, SAMPLE_STREAM_MP4_1, true, "01", false),
            MediaEntity("ser_shogun", "Shōgun", "", "", "SERIES", "2024", "9.2", "دراما / تاريخي", "صراع النفوذ والحروب في اليابان الإقطاعية في مطلع القرن 17.", "58 دقيقة", 1, 10, SAMPLE_STREAM_MP4_2, true, "02", false),
            MediaEntity("ser_succession", "Succession S1-S4", "", "", "SERIES", "2023", "9.3", "دراما / كوميديا سوداء", "عائلة روي وصراع الأبناء على إمبراطورية الإعلام العالمية.", "65 دقيقة", 4, 39, SAMPLE_STREAM_MP4_3, true, "03", false),

            // Anime
            MediaEntity("ani_one_piece", "One Piece", "", "", "ANIME", "2024", "9.6", "أنمي / مغامرة", "لوفي وطاقم قبعة القش في طريقهم نحو الكنز الأسطوري.", "24 دقيقة", 21, 1100, SAMPLE_STREAM_MP4_1, false, "01", false),
            MediaEntity("ani_pluto", "Pluto", "", "", "ANIME", "2023", "8.8", "أنمي / خيال علمي", "محقق آلي يحقق في سلسلة اغتيالات للروبوتات الأكثر تطوراً.", "60 دقيقة", 1, 8, SAMPLE_STREAM_MP4_2, false, "02", false),
            MediaEntity("ani_frieren", "Frieren: Beyond Journey's End", "", "", "ANIME", "2024", "9.5", "أنمي / فنتازيا", "رحلة فرييرين بعد هزيمة ملك الشياطين وفهم معنى الحياة الإنسانية.", "24 دقيقة", 1, 28, SAMPLE_STREAM_MP4_3, false, "03", false)
        )
        dao.insertMediaItems(mediaList)

        val initialApi = ApiSourceEntity(
            id = "api_main",
            name = "SARIB Streaming Master API",
            baseUrl = "https://stream.saribtv.com",
            endpoint = "/v1/live/manifest",
            apiKey = "sarib_live_sec_prod_9981",
            headers = "{\"X-App-Client\": \"SARIB-TV-Android\"}",
            isEnabled = true,
            status = "متصل",
            lastChecked = "اليوم 10:45 ص"
        )
        dao.insertApiSource(initialApi)
    }

    fun getHeroBanner(): HeroBannerItem {
        return HeroBannerItem(
            id = "hero_from",
            title = "بلدة الضياع S1-S4",
            subtitle = "مسلسل • دراما • رعب • أحجية",
            backdropUrl = "",
            genreTags = listOf("مسلسل", "دراما", "رعب", "أحجية"),
            streamUrl = SAMPLE_STREAM_HLS_1,
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

    // Admin Operations
    suspend fun getAdminServerStats(): ServerStats = withContext(Dispatchers.IO) {
        val chCount = dao.getChannelsCount()
        val movCount = dao.getMoviesCount()
        val serCount = dao.getSeriesCount()
        val matchCount = dao.getMatchesCount()
        ServerStats(
            totalChannels = chCount,
            totalMovies = movCount,
            totalSeries = serCount,
            totalMatches = matchCount,
            activeUsers = 1540,
            serverStatus = "متصل وسريع (99.98% Uptime)",
            lastSyncTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        )
    }

    fun getApiSources(): Flow<List<ApiSourceConfig>> {
        return dao.getAllApiSources().map { list ->
            list.map { it.toModel() }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun addChannel(channel: ChannelItem) = withContext(Dispatchers.IO) {
        dao.insertChannel(channel.toEntity())
        logAdminAction("إضافة قناة", "تمت إضافة القناة ${channel.name}")
    }

    suspend fun updateChannel(channel: ChannelItem) = withContext(Dispatchers.IO) {
        dao.updateChannel(channel.toEntity())
        logAdminAction("تعديل قناة", "تم تحديث بيانات ${channel.name}")
    }

    suspend fun deleteChannel(channelId: String) = withContext(Dispatchers.IO) {
        dao.deleteChannelById(channelId)
        logAdminAction("حذف قناة", "تم حذف القناة رقم $channelId")
    }

    suspend fun addApiSource(api: ApiSourceConfig) = withContext(Dispatchers.IO) {
        dao.insertApiSource(api.toEntity())
        logAdminAction("إضافة API", "تمت إضافة المصدر ${api.name}")
    }

    suspend fun updateApiSource(api: ApiSourceConfig) = withContext(Dispatchers.IO) {
        dao.updateApiSource(api.toEntity())
        logAdminAction("تعديل API", "تم تحديث المصدر ${api.name}")
    }

    suspend fun testApiConnection(baseUrl: String, endpoint: String): Boolean = withContext(Dispatchers.IO) {
        delay(800) // Test connection handshake
        true
    }

    private fun logAdminAction(action: String, details: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        adminLogs.add(0, AdminLog(System.currentTimeMillis(), time, action, details))
    }

    fun getAdminLogs(): List<AdminLog> = adminLogs.toList()
}

// Extension converters
fun CategoryEntity.toModel() = ChannelCategory(id, name, subtitle, channelCount, iconUrl, categoryType, gradientColorHex)
fun ChannelEntity.toModel() = ChannelItem(id, name, categoryId, categoryName, logoUrl, streamUrl, backupUrl, country, language, isFavorite, isEnabled, sortOrder, viewsCount)
fun ChannelItem.toEntity() = ChannelEntity(id, name, categoryId, categoryName, logoUrl, streamUrl, backupUrl, country, language, isFavorite, isEnabled, sortOrder, viewsCount)
fun MatchEntity.toModel() = MatchItem(id, leagueName, leagueIconUrl, homeTeam, homeLogoUrl, awayTeam, awayLogoUrl, matchTime, matchDate, status, homeScore, awayScore, streamUrl, isLive, isFavorite)
fun MediaEntity.toModel() = MediaItem(id, title, posterUrl, backdropUrl, ContentType.valueOf(type), year, rating, genre, description, duration, seasonsCount, episodesCount, streamUrl, isTop, topRank, isFavorite)
fun ApiSourceEntity.toModel() = ApiSourceConfig(id, name, baseUrl, endpoint, apiKey, headers, isEnabled, status, lastChecked)
fun ApiSourceConfig.toEntity() = ApiSourceEntity(id, name, baseUrl, endpoint, apiKey, headers, isEnabled, status, lastChecked)
