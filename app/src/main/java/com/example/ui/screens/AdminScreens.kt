package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminLog
import com.example.data.model.ApiSourceConfig
import com.example.data.model.ChannelItem
import com.example.data.model.ServerStats
import com.example.ui.theme.SaribCardBg
import com.example.ui.theme.SaribCardBgSecondary
import com.example.ui.theme.SaribCardBorder
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribDarkBackground
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribLiveRed
import com.example.ui.theme.SaribSuccessGreen
import com.example.ui.theme.SaribTextMuted
import com.example.ui.theme.SaribTextPrimary
import com.example.ui.theme.SaribTextSecondary

@Composable
fun AdminAuthScreen(
    onAuthenticate: (String) -> Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SaribDarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.5.dp, SaribCyanAccent.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = SaribCardBg)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(SaribElectricBlue.copy(alpha = 0.2f))
                            .border(1.dp, SaribCyanAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = SaribCyanAccent,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "بوابة إدارة SARIB TV",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SaribTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "أدخل رمز الدخول السري للإدارة",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SaribTextSecondary)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            pin = it
                            errorMessage = null
                        },
                        placeholder = { Text("رمز المرور (مثال: 456987)", color = SaribTextMuted) },
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = SaribCyanAccent)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaribCyanAccent,
                            unfocusedBorderColor = SaribCardBorder,
                            focusedContainerColor = SaribDarkBackground,
                            unfocusedContainerColor = SaribDarkBackground,
                            focusedTextColor = SaribTextPrimary,
                            unfocusedTextColor = SaribTextPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_pin_field")
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall.copy(color = SaribLiveRed)
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = SaribTextSecondary)
                        }

                        Button(
                            onClick = {
                                val success = onAuthenticate(pin)
                                if (!success) {
                                    errorMessage = "رمز الدخول غير صحيح"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaribElectricBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_submit_button")
                        ) {
                            Text("دخول", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDashboardScreen(
    stats: ServerStats,
    channels: List<ChannelItem>,
    apiSources: List<ApiSourceConfig>,
    logs: List<AdminLog>,
    onAddChannel: (ChannelItem) -> Unit,
    onDeleteChannel: (String) -> Unit,
    onAddApiSource: (ApiSourceConfig) -> Unit,
    onTestApiConnection: (String, String, (Boolean) -> Unit) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("نظرة عامة", "القنوات", "الـ APIs", "السجلات")

    var showAddChannelDialog by remember { mutableStateOf(false) }
    var showAddApiDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SaribDarkBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SaribCardBg)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
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
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = SaribCyanAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "لوحة تحكم SARIB TV",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaribTextPrimary
                            )
                        )
                        Text(
                            text = "الخادم: ${stats.serverStatus}",
                            style = MaterialTheme.typography.bodySmall.copy(color = SaribSuccessGreen)
                        )
                    }
                }

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .testTag("admin_logout_button")
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SaribDarkBackground)
                        .border(1.dp, SaribCardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "تسجيل خروج",
                        tint = SaribLiveRed
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SaribDarkBackground)
        ) {
            // Tabs Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = SaribCardBg,
                contentColor = SaribCyanAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = SaribCyanAccent
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) SaribCyanAccent else SaribTextSecondary
                                )
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> AdminOverviewTab(stats = stats)
                1 -> AdminChannelsTab(
                    channels = channels,
                    onAddClick = { showAddChannelDialog = true },
                    onDeleteClick = onDeleteChannel
                )
                2 -> AdminApisTab(
                    apiSources = apiSources,
                    onAddClick = { showAddApiDialog = true },
                    onTestConnection = onTestApiConnection
                )
                3 -> AdminLogsTab(logs = logs)
            }
        }

        // Add Channel Dialog
        if (showAddChannelDialog) {
            AddChannelDialog(
                onDismiss = { showAddChannelDialog = false },
                onConfirm = { newChannel ->
                    onAddChannel(newChannel)
                    showAddChannelDialog = false
                }
            )
        }

        // Add API Dialog
        if (showAddApiDialog) {
            AddApiDialog(
                onDismiss = { showAddApiDialog = false },
                onConfirm = { newApi ->
                    onAddApiSource(newApi)
                    showAddApiDialog = false
                }
            )
        }
    }
}

@Composable
fun AdminOverviewTab(stats: ServerStats) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "الإحصائيات المباشرة للنظام",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SaribTextPrimary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "إجمالي القنوات",
                    value = "${stats.totalChannels}",
                    icon = Icons.Default.Tv,
                    color = SaribCyanAccent,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "إجمالي الأفلام",
                    value = "${stats.totalMovies}",
                    icon = Icons.Default.Movie,
                    color = SaribElectricBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "المسلسلات",
                    value = "${stats.totalSeries}",
                    icon = Icons.Default.Tv,
                    color = Color(0xFFA855F7),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "المباريات",
                    value = "${stats.totalMatches}",
                    icon = Icons.Default.SportsSoccer,
                    color = SaribSuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, SaribCardBorder, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = SaribCardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "حالة خادم البث SARIB CDN",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SaribTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("الحالة:", color = SaribTextSecondary)
                        Text(stats.serverStatus, color = SaribSuccessGreen, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("المستخدمون النشطون:", color = SaribTextSecondary)
                        Text("${stats.activeUsers} متصل الآن", color = SaribCyanAccent, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("آخر مزامنة:", color = SaribTextSecondary)
                        Text(stats.lastSyncTime, color = SaribTextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, SaribCardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SaribCardBg)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = SaribTextPrimary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(color = SaribTextSecondary)
            )
        }
    }
}

@Composable
fun AdminChannelsTab(
    channels: List<ChannelItem>,
    onAddClick: () -> Unit,
    onDeleteClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "قائمة القنوات (${channels.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SaribTextPrimary
                    )
                )

                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SaribElectricBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("admin_add_channel_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إضافة قناة", color = Color.White)
                }
            }
        }

        items(channels) { channel ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, SaribCardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SaribCardBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = channel.name,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaribTextPrimary
                            )
                        )
                        Text(
                            text = "التصنيف: ${channel.categoryName} • ${channel.country}",
                            style = MaterialTheme.typography.bodySmall.copy(color = SaribTextSecondary)
                        )
                        Text(
                            text = "الرابط: ${channel.streamUrl.take(30)}...",
                            style = MaterialTheme.typography.bodySmall.copy(color = SaribTextMuted)
                        )
                    }

                    IconButton(onClick = { onDeleteClick(channel.id) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = SaribLiveRed.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminApisTab(
    apiSources: List<ApiSourceConfig>,
    onAddClick: () -> Unit,
    onTestConnection: (String, String, (Boolean) -> Unit) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مصادر واجهات البرمجة (APIs)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SaribTextPrimary
                    )
                )

                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SaribElectricBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إضافة API", color = Color.White)
                }
            }
        }

        items(apiSources) { api ->
            var testStatus by remember { mutableStateOf(api.status) }
            var isTesting by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, SaribCardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SaribCardBg)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = api.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaribTextPrimary
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SaribSuccessGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = testStatus,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SaribSuccessGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Base URL: ${api.baseUrl}",
                        style = MaterialTheme.typography.bodySmall.copy(color = SaribTextSecondary)
                    )
                    Text(
                        text = "Endpoint: ${api.endpoint}",
                        style = MaterialTheme.typography.bodySmall.copy(color = SaribTextSecondary)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            isTesting = true
                            onTestConnection(api.baseUrl, api.endpoint) { ok ->
                                isTesting = false
                                testStatus = if (ok) "متصل وسريع" else "فشل الاتصال"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaribElectricBlue.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(color = SaribCyanAccent, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text("اختبار الاتصال بالـ API", color = SaribCyanAccent, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminLogsTab(logs: List<AdminLog>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "سجل أنشطة النظام",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SaribTextPrimary
                )
            )
        }

        if (logs.isEmpty()) {
            item {
                Text(
                    text = "لا توجد سجلات حالياً.",
                    color = SaribTextSecondary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }

        items(logs) { log ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, SaribCardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SaribCardBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = log.action,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaribCyanAccent
                            )
                        )
                        Text(
                            text = log.details,
                            style = MaterialTheme.typography.bodySmall.copy(color = SaribTextSecondary)
                        )
                    }
                    Text(
                        text = log.timestamp,
                        style = MaterialTheme.typography.labelSmall.copy(color = SaribTextMuted)
                    )
                }
            }
        }
    }
}

@Composable
fun AddChannelDialog(
    onDismiss: () -> Unit,
    onConfirm: (ChannelItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf("bein_all") }
    var categoryName by remember { mutableStateOf("beIN Sports") }
    var streamUrl by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("العالم العربي") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SaribCardBg,
        title = { Text("إضافة قناة جديدة", color = SaribTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم القناة", color = SaribTextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SaribTextPrimary,
                        unfocusedTextColor = SaribTextPrimary,
                        focusedBorderColor = SaribCyanAccent,
                        unfocusedBorderColor = SaribCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = streamUrl,
                    onValueChange = { streamUrl = it },
                    label = { Text("رابط البث (HLS/M3U8/MP4)", color = SaribTextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SaribTextPrimary,
                        unfocusedTextColor = SaribTextPrimary,
                        focusedBorderColor = SaribCyanAccent,
                        unfocusedBorderColor = SaribCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("الدولة / المنطقة", color = SaribTextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SaribTextPrimary,
                        unfocusedTextColor = SaribTextPrimary,
                        focusedBorderColor = SaribCyanAccent,
                        unfocusedBorderColor = SaribCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val id = "ch_${System.currentTimeMillis()}"
                        onConfirm(
                            ChannelItem(
                                id = id,
                                name = name,
                                categoryId = categoryId,
                                categoryName = categoryName,
                                streamUrl = streamUrl,
                                country = country
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaribElectricBlue)
            ) {
                Text("إضافة", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = SaribTextSecondary)
            }
        }
    )
}

@Composable
fun AddApiDialog(
    onDismiss: () -> Unit,
    onConfirm: (ApiSourceConfig) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("") }
    var endpoint by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SaribCardBg,
        title = { Text("إضافة مصدر API", color = SaribTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المصدر", color = SaribTextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SaribTextPrimary,
                        unfocusedTextColor = SaribTextPrimary,
                        focusedBorderColor = SaribCyanAccent,
                        unfocusedBorderColor = SaribCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL", color = SaribTextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SaribTextPrimary,
                        unfocusedTextColor = SaribTextPrimary,
                        focusedBorderColor = SaribCyanAccent,
                        unfocusedBorderColor = SaribCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    label = { Text("Endpoint", color = SaribTextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SaribTextPrimary,
                        unfocusedTextColor = SaribTextPrimary,
                        focusedBorderColor = SaribCyanAccent,
                        unfocusedBorderColor = SaribCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key (اختياري)", color = SaribTextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SaribTextPrimary,
                        unfocusedTextColor = SaribTextPrimary,
                        focusedBorderColor = SaribCyanAccent,
                        unfocusedBorderColor = SaribCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && baseUrl.isNotBlank()) {
                        val id = "api_${System.currentTimeMillis()}"
                        onConfirm(
                            ApiSourceConfig(
                                id = id,
                                name = name,
                                baseUrl = baseUrl,
                                endpoint = endpoint,
                                apiKey = apiKey
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaribElectricBlue)
            ) {
                Text("حفظ", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = SaribTextSecondary)
            }
        }
    )
}
