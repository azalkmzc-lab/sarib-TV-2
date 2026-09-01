package com.example.data.model

enum class ContentType {
    CHANNEL,
    MATCH,
    MOVIE,
    SERIES,
    ANIME
}

enum class ViewMode {
    GRID,
    LIST
}

data class ChannelItem(
    val id: String,
    val name: String,
    val categoryId: String,
    val categoryName: String,
    val logoUrl: String = "",
    val streamUrl: String = "",
    val backupUrl: String = "",
    val country: String = "العالم العربي",
    val language: String = "العربية",
    val isFavorite: Boolean = false,
    val isEnabled: Boolean = true,
    val sortOrder: Int = 0,
    val viewsCount: Int = 0
)

data class ChannelCategory(
    val id: String,
    val name: String,
    val subtitle: String = "",
    val channelCount: Int = 0,
    val iconUrl: String = "",
    val categoryType: String = "sports", // sports, entertainment, news, movies
    val gradientColorHex: String = "#0088FF"
)

data class HeroBannerItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val backdropUrl: String = "",
    val badge: String = "حصري",
    val genreTags: List<String> = listOf("مسلسل", "دراما"),
    val streamUrl: String = "",
    val contentType: ContentType = ContentType.SERIES,
    val isLive: Boolean = false,
    val sortOrder: Int = 0,
    val isActive: Boolean = true
)

data class MatchItem(
    val id: String,
    val leagueName: String,
    val leagueIconUrl: String = "",
    val homeTeam: String,
    val homeLogoUrl: String = "",
    val awayTeam: String,
    val awayLogoUrl: String = "",
    val matchTime: String,
    val matchDate: String,
    val status: String, // "لم تبدأ", "مباشر", "انتهت"
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val streamUrl: String = "",
    val isLive: Boolean = false,
    val isFavorite: Boolean = false
)

data class MediaItem(
    val id: String,
    val title: String,
    val posterUrl: String,
    val backdropUrl: String = "",
    val type: ContentType = ContentType.MOVIE,
    val year: String = "2024",
    val rating: String = "8.8",
    val genre: String = "أكشن / دراما",
    val description: String = "",
    val duration: String = "120 دقيقة",
    val seasonsCount: Int = 1,
    val episodesCount: Int = 10,
    val streamUrl: String = "",
    val isTop: Boolean = false,
    val topRank: String = "01",
    val isFavorite: Boolean = false
)

data class ApiSourceConfig(
    val id: String,
    val name: String,
    val baseUrl: String,
    val endpoint: String,
    val apiKey: String,
    val headers: String = "{}",
    val isEnabled: Boolean = true,
    val status: String = "جاهز للاتصال",
    val lastChecked: String = ""
)

data class AdminLog(
    val id: Long = 0,
    val timestamp: String,
    val action: String,
    val details: String
)

data class ServerStats(
    val totalChannels: Int = 0,
    val totalMovies: Int = 0,
    val totalSeries: Int = 0,
    val totalMatches: Int = 0,
    val activeUsers: Int = 1420,
    val serverStatus: String = "متصل وسريع",
    val lastSyncTime: String = "الآن"
)
