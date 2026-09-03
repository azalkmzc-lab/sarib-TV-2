package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.LayoutDirection

enum class AppThemeMode {
    DARK,
    LIGHT,
    AMOLED
}

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String, val layoutDirection: LayoutDirection) {
    ARABIC("ar", "Arabic", "العربية", LayoutDirection.Rtl),
    ENGLISH("en", "English", "English", LayoutDirection.Ltr)
}

object LocalStrings {
    // English & Arabic dictionary for instant reactive localization across all screens
    private val arStrings = mapOf(
        "app_name" to "SARIB TV",
        "home" to "الرئيسية",
        "channels" to "القنوات",
        "matches" to "المباريات",
        "entertainment" to "الترفيه",
        "favorites" to "المفضلة",
        "settings" to "الإعدادات",
        "movies" to "الأفلام",
        "series" to "المسلسلات",
        "anime" to "الأنمي",
        "cinema_library" to "مكتبة السينما والأفلام",
        "series_library" to "مكتبة المسلسلات العالمية",
        "anime_library" to "مكتبة الأنمي المترجم والمدبلج",
        "all_channels_packages" to "باقات القنوات المشفرة والمفتوحة",
        "theme" to "المظهر والسمة",
        "theme_dark" to "الوضع الداكن (الافتراضي)",
        "theme_light" to "الوضع الفاتح (العادي)",
        "theme_amoled" to "أسود نقي (AMOLED)",
        "language" to "لغة التطبيق",
        "language_ar" to "العربية",
        "language_en" to "English",
        "search" to "البحث",
        "search_hint" to "ابحث عن قنوات، أفلام، مسلسلات...",
        "watch_now" to "مشاهدة الآن",
        "view_all" to "عرض الكل",
        "connecting" to "جارٍ تجهيز الاتصال بالسيرفر...",
        "live" to "مباشر LIVE",
        "retry" to "إعادة المحاولة",
        "most_watched" to "القنوات الأكثر مشاهدة",
        "todays_matches" to "أهم مباريات اليوم",
        "featured_movies" to "أفلام مختارة",
        "recommended_series" to "مسلسلات مميزة",
        "anime_picks" to "اختيارات الأنمي",
        "no_content" to "لا يوجد محتوى متوفر حالياً",
        "server" to "السيرفر",
        "subtitles" to "الترجمة (CC)",
        "subtitles_off" to "إيقاف الترجمة",
        "quality" to "الجودة",
        "audio_tracks" to "مسارات الصوت",
        "match_details" to "تفاصيل المباراة",
        "match_servers" to "سيرفرات البث المتاحة",
        "stadium" to "الملعب",
        "commentator" to "المعلق",
        "channel" to "القناة الناقلة",
        "tournament" to "البطولة",
        "alert_set" to "تم تفعيل إشعار المباراة لفريقك!",
        "alert_removed" to "تم إلغاء الإشعار للمباراة",
        "server_switcher" to "مبدل السيرفرات السحابية",
        "server_1" to "سيرفر 1 (الرئيسي FHD)",
        "server_2" to "سيرفر 2 (احتياطي HD)",
        "server_3" to "سيرفر 3 (سريع CDN)",
        "server_4" to "سيرفر 4 (منخفض البينج)",
        "server_5" to "سيرفر 5 (مباشر M3U8)",
        "fast_entrance" to "دخول فائق السرعة",
        "clear_cache" to "مسح الذاكرة المؤقتة",
        "cache_cleared" to "تم مسح الذاكرة المؤقتة بنجاح",
        "app_info" to "معلومات التطبيق",
        "version" to "الإصدار",
        "close" to "إغلاق",
        "apply" to "تطبيق",
        "filter_all" to "الكل",
        "no_favorites" to "لم تقم بإضافة أي عناصر للمفضلة بعد",
        "vpn_warning_title" to "تم رصد اتصال VPN أو بروكسي!",
        "vpn_warning_msg" to "لأسباب أمنية وحماية لحقوق البث، يمنع تشغيل البث أثناء تفعيل برامج الـ VPN. يرجى إيقاف الـ VPN ومتابعة المشاهدة.",
        "exit_player" to "إغلاق المشغل",
        "exit_app" to "إغلاق التطبيق نهائياً",
        "security_protected" to "محمي بنظام أمان فائق",
        "search_title" to "البحث الفوري والشامل",
        "search_results" to "نتائج البحث",
        "no_results" to "لم يتم العثور على أي نتائج مطابقة",
        "telegram_channel" to "قناة التيليجرام الرسمية",
        "grid_view" to "شبكة",
        "list_view" to "قائمة",
        "all" to "الكل"
    )

    private val enStrings = mapOf(
        "app_name" to "SARIB TV",
        "home" to "Home",
        "channels" to "Channels",
        "matches" to "Matches",
        "entertainment" to "Entertainment",
        "favorites" to "Favorites",
        "settings" to "Settings",
        "movies" to "Movies",
        "series" to "TV Series",
        "anime" to "Anime",
        "cinema_library" to "Cinema & Movies Library",
        "series_library" to "Global TV Series Library",
        "anime_library" to "Subbed & Dubbed Anime Library",
        "all_channels_packages" to "Encrypted & Free Channels Packages",
        "theme" to "Theme & Appearance",
        "theme_dark" to "Dark Mode (Default)",
        "theme_light" to "Light Mode (Standard)",
        "theme_amoled" to "Pure Black (AMOLED)",
        "language" to "App Language",
        "language_ar" to "العربية",
        "language_en" to "English",
        "search" to "Search",
        "search_hint" to "Search channels, movies, series...",
        "watch_now" to "Watch Now",
        "view_all" to "View All",
        "connecting" to "Connecting to ultra-fast servers...",
        "live" to "LIVE",
        "retry" to "Retry",
        "most_watched" to "Most Watched Channels",
        "todays_matches" to "Today's Top Matches",
        "featured_movies" to "Featured Movies",
        "recommended_series" to "Top TV Series",
        "anime_picks" to "Anime Picks",
        "no_content" to "No content available right now",
        "server" to "Server",
        "subtitles" to "Subtitles (CC)",
        "subtitles_off" to "Subtitles Off",
        "quality" to "Quality",
        "audio_tracks" to "Audio Tracks",
        "match_details" to "Match Details",
        "match_servers" to "Available Streaming Servers",
        "stadium" to "Stadium",
        "commentator" to "Commentator",
        "channel" to "Broadcast Channel",
        "tournament" to "Tournament",
        "alert_set" to "Match notification alert activated for both teams!",
        "alert_removed" to "Match alert removed",
        "server_switcher" to "Cloud Server Switcher",
        "server_1" to "Server 1 (Primary FHD)",
        "server_2" to "Server 2 (Backup HD)",
        "server_3" to "Server 3 (Fast CDN)",
        "server_4" to "Server 4 (Low Ping)",
        "server_5" to "Server 5 (Direct M3U8)",
        "fast_entrance" to "Ultra-Fast Entrance",
        "clear_cache" to "Clear Cache",
        "cache_cleared" to "Cache cleared successfully",
        "app_info" to "Application Info",
        "version" to "Version",
        "close" to "Close",
        "apply" to "Apply",
        "filter_all" to "All",
        "no_favorites" to "No favorites added yet",
        "vpn_warning_title" to "VPN or Proxy Connection Detected!",
        "vpn_warning_msg" to "For security and content protection reasons, streaming over VPN or Proxy is strictly prohibited. Please disconnect your VPN to resume playback.",
        "exit_player" to "Close Player",
        "exit_app" to "Exit Application",
        "security_protected" to "High-Grade Security Protected",
        "search_title" to "Instant Global Search",
        "search_results" to "Search Results",
        "no_results" to "No matching results found",
        "telegram_channel" to "Official Telegram Channel",
        "grid_view" to "Grid",
        "list_view" to "List",
        "all" to "All"
    )

    fun getString(key: String, language: AppLanguage): String {
        val dict = if (language == AppLanguage.ARABIC) arStrings else enStrings
        return dict[key] ?: arStrings[key] ?: key
    }
}

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sarib_settings", Context.MODE_PRIVATE)

    var currentTheme: AppThemeMode by mutableStateOf(
        when (prefs.getString("theme_mode", "DARK")) {
            "LIGHT" -> AppThemeMode.LIGHT
            "AMOLED" -> AppThemeMode.AMOLED
            else -> AppThemeMode.DARK
        }
    )
        private set

    var currentLanguage: AppLanguage by mutableStateOf(
        when (prefs.getString("language_code", "ar")) {
            "en" -> AppLanguage.ENGLISH
            else -> AppLanguage.ARABIC
        }
    )
        private set

    fun setTheme(theme: AppThemeMode) {
        currentTheme = theme
        prefs.edit().putString("theme_mode", theme.name).apply()
    }

    fun setLanguage(language: AppLanguage) {
        currentLanguage = language
        prefs.edit().putString("language_code", language.code).apply()
    }

    fun toggleMatchNotification(matchId: String): Boolean {
        val key = "alert_match_$matchId"
        val current = prefs.getBoolean(key, false)
        val newState = !current
        prefs.edit().putBoolean(key, newState).apply()
        return newState
    }

    fun hasMatchNotification(matchId: String): Boolean {
        return prefs.getBoolean("alert_match_$matchId", false)
    }

    fun getString(key: String): String {
        return LocalStrings.getString(key, currentLanguage)
    }
}

val LocalAppPreferences = compositionLocalOf<AppPreferences> {
    error("No AppPreferences provided")
}

@Composable
fun tr(key: String): String {
    return LocalAppPreferences.current.getString(key)
}
