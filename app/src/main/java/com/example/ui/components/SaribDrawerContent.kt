package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.SaribCardBg
import com.example.ui.theme.SaribCardBorder
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribDarkBackground
import com.example.ui.theme.SaribDarkSurface
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribTextMuted
import com.example.ui.theme.SaribTextPrimary
import com.example.ui.theme.SaribTextSecondary

@Composable
fun SaribDrawerContent(
    onNavigateToHome: () -> Unit,
    onNavigateToChannels: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToEntertainment: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onTelegramClick: () -> Unit,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp),
        color = SaribDarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0F1826), SaribDarkBackground)
                    )
                )
                .padding(20.dp)
        ) {
            // Header: SARIB TV Branding
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, SaribCyanAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_sarib_logo),
                        contentDescription = "SARIB TV",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SARIB",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = SaribTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "TV",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = SaribCyanAccent
                            )
                        )
                    }
                    Text(
                        text = "الإصدار 1.0.0 • PRO VIP",
                        style = MaterialTheme.typography.labelSmall.copy(color = SaribTextMuted)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = SaribCardBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Navigation items list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    DrawerItem(
                        icon = Icons.Default.Home,
                        title = "الرئيسية",
                        onClick = onNavigateToHome
                    )
                }
                item {
                    DrawerItem(
                        icon = Icons.Default.Tv,
                        title = "باقات القنوات المشفرة والمفتوحة",
                        onClick = onNavigateToChannels
                    )
                }
                item {
                    DrawerItem(
                        icon = Icons.Default.SportsSoccer,
                        title = "جدول ونتائج المباريات",
                        onClick = onNavigateToMatches
                    )
                }
                item {
                    DrawerItem(
                        icon = Icons.Default.Movie,
                        title = "مكتبة السينما والمسلسلات والأنمي",
                        onClick = onNavigateToEntertainment
                    )
                }
                item {
                    DrawerItem(
                        icon = Icons.Default.Favorite,
                        title = "المفضلة",
                        onClick = onNavigateToFavorites
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = SaribCardBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))
                }

                item {
                    DrawerItem(
                        icon = Icons.AutoMirrored.Filled.Send,
                        title = "قناة التليجرام الرسمية",
                        tint = SaribCyanAccent,
                        onClick = onTelegramClick
                    )
                }

                item {
                    DrawerItem(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "الوصول الإداري السري",
                        tint = SaribTextMuted,
                        onClick = onAdminClick
                    )
                }
            }

            // Footer
            Text(
                text = "جميع الحقوق محفوظة لـ SARIB TV © 2024",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SaribTextMuted,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    title: String,
    tint: Color = SaribTextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (tint == SaribTextPrimary) SaribCyanAccent else tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = tint
            )
        )
    }
}
