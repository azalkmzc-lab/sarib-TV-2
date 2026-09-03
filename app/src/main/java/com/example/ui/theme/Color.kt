package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// SARIB TV Premium Dark Theme Palette
val SaribDarkBackground = Color(0xFF070C14)
val SaribDarkSurface = Color(0xFF0E1624)
val SaribDarkSurfaceVariant = Color(0xFF142033)
val SaribDarkCard = Color(0xFF111C2D)
val SaribCardBg = Color(0xFF111C2D)
val SaribCardBgSecondary = Color(0xFF16253C)
val SaribCardBorder = Color(0xFF223652)
val SaribCardBorderSubtle = Color(0xFF1A2B42)

// AMOLED Theme Palette
val SaribAmoledBackground = Color(0xFF000000)
val SaribAmoledSurface = Color(0xFF080808)
val SaribAmoledCard = Color(0xFF101010)
val SaribAmoledBorder = Color(0xFF262626)

// Light Theme Palette
val SaribLightBackground = Color(0xFFF3F4F6)
val SaribLightSurface = Color(0xFFFFFFFF)
val SaribLightSurfaceVariant = Color(0xFFE5E7EB)
val SaribLightCard = Color(0xFFFFFFFF)
val SaribLightCardBorder = Color(0xFFD1D5DB)
val SaribLightCardBorderSubtle = Color(0xFFE5E7EB)
val SaribLightTextPrimary = Color(0xFF111827)
val SaribLightTextSecondary = Color(0xFF4B5563)
val SaribLightTextMuted = Color(0xFF6B7280)

// Neon & Electric Accents
val SaribElectricBlue = Color(0xFF0088FF)
val SaribCyanAccent = Color(0xFF00C8FF)
val SaribBlueGlow = Color(0x660088FF)
val SaribBlueGlowSubtle = Color(0x330088FF)
val SaribGoldRating = Color(0xFFF59E0B)

// Functional Colors
val SaribTextPrimary = Color(0xFFFFFFFF)
val SaribTextSecondary = Color(0xFF94A3B8)
val SaribTextMuted = Color(0xFF64748B)

// Badges
val SaribTop01 = Color(0xFFF43F5E) // Red/Rose
val SaribTop02 = Color(0xFFF59E0B) // Amber
val SaribTop03 = Color(0xFFEAB308) // Yellow
val SaribLiveRed = Color(0xFFEF4444)
val SaribSuccessGreen = Color(0xFF10B981)

// Gradients
val SaribHeaderGradientStart = Color(0xFF101C2F)
val SaribHeaderGradientEnd = Color(0xFF0B1422)
val SaribPillGradientStart = Color(0xFF0066CC)
val SaribPillGradientEnd = Color(0xFF0099FF)

// Reactive Theme Color Accessors for Instant Dark / Light / AMOLED Switching
object AppThemeColors {
    val background: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.background

    val surface: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surface

    val cardBg: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surfaceVariant

    val cardBorder: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.outline

    val textPrimary: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onBackground

    val textSecondary: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurfaceVariant
}
