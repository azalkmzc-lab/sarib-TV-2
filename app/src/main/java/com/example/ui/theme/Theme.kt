package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

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

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // Application is in Arabic RTL layout by default
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = SaribDarkColorScheme,
            typography = Typography,
            content = content
        )
    }
}
