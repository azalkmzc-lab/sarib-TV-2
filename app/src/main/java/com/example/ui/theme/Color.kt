package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// SARIB TV Soft, Calm, Modern Minimalist Palette
val SaribDarkBackground = Color(0xFF0F141C)       // Soft slate night background
val SaribDarkSurface = Color(0xFF171E29)          // Smooth subtle surface
val SaribDarkSurfaceVariant = Color(0xFF1E2736)   // Gentle elevated surface
val SaribDarkCard = Color(0xFF18202C)             // Clean soft card background
val SaribCardBg = Color(0xFF18202C)
val SaribCardBgSecondary = Color(0xFF202A3A)
val SaribCardBorder = Color(0xFF2A364B)           // Soft understated border
val SaribCardBorderSubtle = Color(0xFF222D3F)     // Whisper-quiet divider/border

// AMOLED Theme Palette
val SaribAmoledBackground = Color(0xFF05070A)
val SaribAmoledSurface = Color(0xFF0D1117)
val SaribAmoledCard = Color(0xFF141A23)
val SaribAmoledBorder = Color(0xFF232C3B)

// Light Theme Palette
val SaribLightBackground = Color(0xFFF8FAFC)
val SaribLightSurface = Color(0xFFFFFFFF)
val SaribLightSurfaceVariant = Color(0xFFF1F5F9)
val SaribLightCard = Color(0xFFFFFFFF)
val SaribLightCardBorder = Color(0xFFE2E8F0)
val SaribLightCardBorderSubtle = Color(0xFFF1F5F9)
val SaribLightTextPrimary = Color(0xFF0F172A)
val SaribLightTextSecondary = Color(0xFF475569)
val SaribLightTextMuted = Color(0xFF64748B)

// Soft & Pleasant Accents (No harsh neon)
val SaribElectricBlue = Color(0xFF3B82F6)         // Soft pleasant sky-indigo blue
val SaribCyanAccent = Color(0xFF38BDF8)           // Gentle sky blue accent
val SaribBlueGlow = Color(0x2638BDF8)             // Very soft subtle tint
val SaribBlueGlowSubtle = Color(0x1438BDF8)
val SaribGoldRating = Color(0xFFFBBF24)
val SaribAmberGold = Color(0xFFFBBF24)

// Functional Typography Colors
val SaribTextPrimary = Color(0xFFF8FAFC)          // Crisp comfortable off-white
val SaribTextSecondary = Color(0xFF94A3B8)        // Calm slate gray
val SaribTextMuted = Color(0xFF64748B)            // Soft muted label gray

// Badges
val SaribTop01 = Color(0xFFF43F5E)
val SaribTop02 = Color(0xFFF59E0B)
val SaribTop03 = Color(0xFFEAB308)
val SaribLiveRed = Color(0xFFEF4444)
val SaribSuccessGreen = Color(0xFF10B981)

// Soft Header & Pill Gradients
val SaribHeaderGradientStart = Color(0xFF151C26)
val SaribHeaderGradientEnd = Color(0xFF0F141C)
val SaribPillGradientStart = Color(0xFF2563EB)
val SaribPillGradientEnd = Color(0xFF38BDF8)

// Reactive Theme Color Accessors
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
