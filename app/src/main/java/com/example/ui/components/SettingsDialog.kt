package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.local.AppLanguage
import com.example.data.local.AppThemeMode
import com.example.data.local.LocalAppPreferences
import com.example.data.local.tr
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribTextMuted

@Composable
fun SettingsDialog(
    onDismissRequest: () -> Unit,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs = LocalAppPreferences.current
    val context = LocalContext.current

    val surfaceBg = MaterialTheme.colorScheme.surface
    val cardBg = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, SaribCyanAccent.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            color = surfaceBg,
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SaribElectricBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Brightness4,
                                contentDescription = null,
                                tint = SaribCyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = tr("settings"),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = textPrimary
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = tr("close"),
                            tint = SaribTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 1: Theme Mode
                Text(
                    text = tr("theme"),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SaribCyanAccent
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Dark mode option
                ThemeOptionItem(
                    title = tr("theme_dark"),
                    isSelected = prefs.currentTheme == AppThemeMode.DARK,
                    cardBg = cardBg,
                    borderColor = borderColor,
                    textPrimary = textPrimary,
                    onClick = { prefs.setTheme(AppThemeMode.DARK) }
                )

                // Light mode option
                ThemeOptionItem(
                    title = tr("theme_light"),
                    isSelected = prefs.currentTheme == AppThemeMode.LIGHT,
                    cardBg = cardBg,
                    borderColor = borderColor,
                    textPrimary = textPrimary,
                    onClick = { prefs.setTheme(AppThemeMode.LIGHT) }
                )

                // AMOLED mode option
                ThemeOptionItem(
                    title = tr("theme_amoled"),
                    isSelected = prefs.currentTheme == AppThemeMode.AMOLED,
                    cardBg = cardBg,
                    borderColor = borderColor,
                    textPrimary = textPrimary,
                    onClick = { prefs.setTheme(AppThemeMode.AMOLED) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Section 2: App Language
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = SaribCyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr("language"),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SaribCyanAccent
                        )
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Arabic Button
                    LanguageOptionCard(
                        title = "العربية",
                        subtitle = "Arabic (RTL)",
                        isSelected = prefs.currentLanguage == AppLanguage.ARABIC,
                        cardBg = cardBg,
                        borderColor = borderColor,
                        textPrimary = textPrimary,
                        onClick = { prefs.setLanguage(AppLanguage.ARABIC) },
                        modifier = Modifier.weight(1f)
                    )

                    // English Button
                    LanguageOptionCard(
                        title = "English",
                        subtitle = "الإنجليزية (LTR)",
                        isSelected = prefs.currentLanguage == AppLanguage.ENGLISH,
                        cardBg = cardBg,
                        borderColor = borderColor,
                        textPrimary = textPrimary,
                        onClick = { prefs.setLanguage(AppLanguage.ENGLISH) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 3: Performance & Cache
                Text(
                    text = tr("clear_cache"),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SaribCyanAccent
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                        .clickable {
                            onClearCache()
                            Toast.makeText(context, prefs.getString("cache_cleared"), Toast.LENGTH_SHORT).show()
                        },
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = SaribCyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = tr("clear_cache"),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary
                                )
                            )
                        }

                        Text(
                            text = if (prefs.currentLanguage == AppLanguage.ARABIC) "مسح فوري" else "Instant Clean",
                            style = MaterialTheme.typography.labelSmall.copy(color = textSecondary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 4: Security Shield Info
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = cardBg.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SaribCyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = tr("security_protected"),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                            )
                            Text(
                                text = if (prefs.currentLanguage == AppLanguage.ARABIC) "حماية متقدمة ضد VPN والقرصنة" else "Anti-VPN & Sniffer Protection Active",
                                style = MaterialTheme.typography.labelSmall.copy(color = textSecondary)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Close Button
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaribElectricBlue)
                ) {
                    Text(
                        text = tr("close"),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionItem(
    title: String,
    isSelected: Boolean,
    cardBg: Color,
    borderColor: Color,
    textPrimary: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) SaribElectricBlue.copy(alpha = 0.2f) else cardBg)
            .border(
                1.dp,
                if (isSelected) SaribCyanAccent else borderColor,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) SaribCyanAccent else textPrimary
                )
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = SaribCyanAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun LanguageOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    cardBg: Color,
    borderColor: Color,
    textPrimary: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) SaribElectricBlue.copy(alpha = 0.25f) else cardBg)
            .border(
                1.5.dp,
                if (isSelected) SaribCyanAccent else borderColor,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) SaribCyanAccent else textPrimary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSelected) SaribElectricBlue else textPrimary.copy(alpha = 0.6f)
                )
            )
        }
    }
}
