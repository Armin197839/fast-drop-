package com.example.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.FastDropBlue
import com.example.ui.theme.FastDropBlueLight
import com.example.ui.theme.FastDropBlueSoft
import com.example.ui.theme.FastDropBlueSubtle
import com.example.ui.theme.FastDropCyan
import com.example.viewmodel.AppScreen

import androidx.compose.material.icons.filled.Palette
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.model.BackgroundTheme
import com.example.ui.components.BackgroundThemePickerSheet
import com.example.ui.components.SpeedBenchmarkDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    wifiIp: String?,
    isWifiConnected: Boolean,
    currentTheme: BackgroundTheme,
    onSelectTheme: (BackgroundTheme) -> Unit,
    onRefreshNetwork: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    var showThemePicker by remember { mutableStateOf(false) }
    var showSpeedTest by remember { mutableStateOf(false) }

    if (showThemePicker) {
        BackgroundThemePickerSheet(
            currentTheme = currentTheme,
            onSelectTheme = onSelectTheme,
            onDismiss = { showThemePicker = false }
        )
    }

    if (showSpeedTest) {
        SpeedBenchmarkDialog(
            wifiIp = wifiIp ?: "127.0.0.1",
            isWifiConnected = isWifiConnected,
            onDismiss = { showSpeedTest = false }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FastDropBlue,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "FastDrop Ultra",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = FastDropBlue
                                )
                            )
                            Text(
                                text = "انتقال فوق سریع فایل بدون اینترنت",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF64748B)
                                )
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showThemePicker = true },
                        modifier = Modifier.testTag("theme_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "انتخاب پس‌زمینه",
                            tint = FastDropBlue
                        )
                    }

                    IconButton(
                        onClick = { onNavigate(AppScreen.SETTINGS) },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "تنظیمات",
                            tint = FastDropBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.92f)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Futuristic Quick Bar (Theme Switcher + LAN Speedometer)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showThemePicker = true }
                        .testTag("quick_theme_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0E3FF))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FastDropBlueSoft,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = FastDropBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "تنوع پس‌زمینه",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            )
                            Text(
                                text = currentTheme.title.take(15) + "...",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = FastDropBlue,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showSpeedTest = true }
                        .testTag("quick_speed_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0E3FF))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE6F9F0),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "تست سرعت شبکه",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            )
                            Text(
                                text = "سنجش زنده MB/s ⚡",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF10B981),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            // Hero Visual Banner (White/Blue Card)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_banner_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFE8F1FF), Color(0xFFD0E3FF))
                                )
                            )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.fastdrop_banner),
                            contentDescription = "FastDrop Hero",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isWifiConnected) Color(0xFFE6F9F0) else Color(0xFFFFECEB),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isWifiConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                                        contentDescription = "وضعیت شبکه",
                                        tint = if (isWifiConnected) Color(0xFF10B981) else Color(0xFFEF4444),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = if (isWifiConnected) "شبکه فعال: متصل به وای‌فای / هات‌اسپات" else "آفلاین یا در انتظار اتصال",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                )
                                Text(
                                    text = if (wifiIp != null) "آدرس IP دستگاه: $wifiIp" else "برای انتقال هات‌اسپات یا وای‌فای را روشن کنید",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF64748B)
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = onRefreshNetwork,
                            modifier = Modifier
                                .testTag("refresh_network_btn")
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "بروزرسانی شبکه",
                                tint = FastDropBlue
                            )
                        }
                    }
                }
            }

            // Two Big Primary Action Buttons (Send & Receive)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Big Send Button (Pure Electric Blue)
                PrimaryActionCard(
                    title = "ارسال فایل",
                    subtitle = "انتخاب و ساخت بارکد",
                    icon = Icons.Default.CallMade,
                    accentColor = FastDropBlue,
                    backgroundColor = FastDropBlue,
                    textColor = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_send_button"),
                    onClick = { onNavigate(AppScreen.SEND) }
                )

                // Big Receive Button (Clean Crisp White with Blue Accent)
                PrimaryActionCard(
                    title = "دریافت فایل",
                    subtitle = "اسکن QR یا دانلود مستقیم",
                    icon = Icons.Default.CallReceived,
                    accentColor = FastDropBlue,
                    backgroundColor = Color.White,
                    textColor = Color(0xFF0F172A),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_receive_button"),
                    onClick = { onNavigate(AppScreen.RECEIVE) }
                )
            }

            // Feature Section Header
            Text(
                text = "امکانات پیشرفته (بیشتر از شریت)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                ),
                modifier = Modifier.padding(top = 6.dp)
            )

            // Grid of 4 Extra Core Features
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureSmallCard(
                    title = "اشتراک برنامه‌ها (APK)",
                    caption = "استخراج برنامه‌های نصب شده",
                    icon = Icons.Default.Apps,
                    iconBg = Color(0xFFE8F1FF),
                    iconColor = FastDropBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppScreen.FILE_MANAGER) }
                )

                FeatureSmallCard(
                    title = "اشتراک با مرورگر (PC)",
                    caption = "بدون نیاز به نصب اپلیکیشن",
                    icon = Icons.Default.Language,
                    iconBg = Color(0xFFE0F7FA),
                    iconColor = FastDropCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppScreen.WEB_SHARE) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureSmallCard(
                    title = "رادار شناسایی خودکار",
                    caption = "پیدا کردن دستگاه‌های اطراف",
                    icon = Icons.Default.Radar,
                    iconBg = Color(0xFFEDE7F6),
                    iconColor = Color(0xFF673AB7),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppScreen.RECEIVE) }
                )

                FeatureSmallCard(
                    title = "تاریخچه انتقال‌ها",
                    caption = "لیست تمام فایل‌های مبادله شده",
                    icon = Icons.Default.History,
                    iconBg = Color(0xFFFFF3E0),
                    iconColor = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppScreen.HISTORY) }
                )
            }

            // Advantage comparison banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FastDropBlueSubtle),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0E3FF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = FastDropBlue,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "سرعت تا ۴۰ مگابایت بر ثانیه (بدون تبلیغات)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = FastDropBlue
                            )
                        )
                        Text(
                            text = "انتقال نامحدود، بدون افت کیفیت و کاملاً رایگان و امن",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF475569)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun PrimaryActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(135.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = if (backgroundColor == Color.White) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2E8F0)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (backgroundColor == Color.White) FastDropBlueSoft else Color.White.copy(alpha = 0.25f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (backgroundColor == Color.White) accentColor else Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (textColor == Color.White) Color.White.copy(alpha = 0.85f) else Color(0xFF64748B)
                    )
                )
            }
        }
    }
}

@Composable
fun FeatureSmallCard(
    title: String,
    caption: String,
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(115.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconBg,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    ),
                    maxLines = 1
                )
                Text(
                    text = caption,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF64748B)
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
