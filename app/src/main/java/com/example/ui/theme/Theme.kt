package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import com.example.data.local.AppPreferences
import com.example.data.local.AppThemeMode
import com.example.data.local.LocalAppPreferences

private val SaribDarkColorScheme = darkColorScheme(
    primary = SaribElectricBlue,
    onPrimary = Color.White,
    primaryContainer = SaribDarkSurfaceVariant,
    onPrimaryContainer = SaribCyanAccent,
    secondary = SaribCyanAccent,
    onSecondary = Color.Black,
    secondaryContainer = SaribCardBgSecondary,
    onSecondaryContainer = SaribTextPrimary,
    background = SaribDarkBackground,
    onBackground = SaribTextPrimary,
    surface = SaribDarkSurface,
    onSurface = SaribTextPrimary,
    surfaceVariant = SaribCardBg,
    onSurfaceVariant = SaribTextSecondary,
    outline = SaribCardBorder,
    outlineVariant = SaribCardBorderSubtle,
    error = SaribLiveRed,
    onError = Color.White
)

private val SaribAmoledColorScheme = darkColorScheme(
    primary = SaribElectricBlue,
    onPrimary = Color.White,
    primaryContainer = SaribAmoledSurface,
    onPrimaryContainer = SaribCyanAccent,
    secondary = SaribCyanAccent,
    onSecondary = Color.Black,
    secondaryContainer = SaribAmoledCard,
    onSecondaryContainer = SaribTextPrimary,
    background = SaribAmoledBackground,
    onBackground = SaribTextPrimary,
    surface = SaribAmoledSurface,
    onSurface = SaribTextPrimary,
    surfaceVariant = SaribAmoledCard,
    onSurfaceVariant = SaribTextSecondary,
    outline = SaribAmoledBorder,
    outlineVariant = SaribAmoledBorder,
    error = SaribLiveRed,
    onError = Color.White
)

private val SaribLightColorScheme = lightColorScheme(
    primary = SaribElectricBlue,
    onPrimary = Color.White,
    primaryContainer = SaribLightSurfaceVariant,
    onPrimaryContainer = SaribElectricBlue,
    secondary = SaribCyanAccent,
    onSecondary = Color.White,
    secondaryContainer = SaribLightSurfaceVariant,
    onSecondaryContainer = SaribLightTextPrimary,
    background = SaribLightBackground,
    onBackground = SaribLightTextPrimary,
    surface = SaribLightSurface,
    onSurface = SaribLightTextPrimary,
    surfaceVariant = SaribLightCard,
    onSurfaceVariant = SaribLightTextSecondary,
    outline = SaribLightCardBorder,
    outlineVariant = SaribLightCardBorderSubtle,
    error = SaribLiveRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    appPreferences: AppPreferences,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appPreferences.currentTheme) {
        AppThemeMode.LIGHT -> SaribLightColorScheme
        AppThemeMode.AMOLED -> SaribAmoledColorScheme
        AppThemeMode.DARK -> SaribDarkColorScheme
    }

    CompositionLocalProvider(
        LocalAppPreferences provides appPreferences,
        LocalLayoutDirection provides appPreferences.currentLanguage.layoutDirection
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
