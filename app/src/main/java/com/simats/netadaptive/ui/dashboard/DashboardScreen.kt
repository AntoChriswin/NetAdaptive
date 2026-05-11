package com.simats.netadaptive.ui.dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.netadaptive.ui.onboarding.*

@Composable
fun DashboardScreen(
    onProfileClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    
    Scaffold(
        containerColor = Color(0xFFF4F5F7),
        bottomBar = { DashboardBottomNav() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DashboardHeader(onProfileClick = onProfileClick)
            NetworkQualityCard()
            PredictionBanner()
            ActiveAppCard()
            BackgroundAppsSection()
            DataUsageSummaryCard()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DashboardHeader(
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Good morning, Arjun 👋",
                fontSize = 13.sp,
                color = OnSurfaceVariant
            )
            Text(
                text = "Hello, Network Admin",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFEDEEF0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = OnSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(SecondaryContainer, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp)
                )
            }
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onProfileClick() },
                shape = CircleShape,
                color = PrimaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "AM",
                        color = OnPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkQualityCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NETWORK QUALITY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
                Surface(
                    color = Color(0xFFEDFAF5),
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(Emerald, CircleShape))
                        Text(
                            text = "VPN Active",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(140.dp)) {
                    drawArc(
                        color = Color(0xFFEDEEF0),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = PrimaryContainer,
                        startAngle = 180f,
                        sweepAngle = 180f * 0.88f,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset(y = 20.dp)
                ) {
                    Text(
                        text = "88",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Text(
                        text = "EXCELLENT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QualityStatItem(Modifier.weight(1f), "Latency", "24ms", Emerald)
                QualityStatItem(Modifier.weight(1f), "Packet loss", "0.3%", Emerald)
                QualityStatItem(Modifier.weight(1f), "Signal", "-68dBm", Color(0xFFF59E0B))
            }
        }
    }
}

@Composable
private fun QualityStatItem(modifier: Modifier, label: String, value: String, dotColor: Color) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF3F4F6),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, color = OnSurfaceVariant)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.size(6.dp).background(dotColor, CircleShape))
                Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            }
        }
    }
}

@Composable
private fun PredictionBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFEDFAF5),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFD1FAE5))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Radar,
                contentDescription = null,
                tint = Emerald,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Network stable for next 15s",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF065F46)
            )
        }
    }
}

@Composable
private fun ActiveAppCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE APP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
                Surface(
                    color = PrimaryFixed,
                    shape = CircleShape
                ) {
                    Text(
                        text = "Full bandwidth",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF703700),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFF0000)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "YouTube", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "2.4 MB/s", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEDEEF0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .fillMaxHeight()
                                .background(Primary, CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AppSpeedItem(Icons.Default.ArrowDownward, "2.1 MB/s")
                        AppSpeedItem(Icons.Default.ArrowUpward, "0.3 MB/s")
                    }
                }
            }
        }
    }
}

@Composable
private fun AppSpeedItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = OnSurfaceVariant)
        Text(text = text, fontSize = 11.sp, color = OnSurfaceVariant)
    }
}

@Composable
private fun BackgroundAppsSection() {
    Column {
        Text(
            text = "BACKGROUND APPS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = OnSurfaceVariant.copy(alpha = 0.7f),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            item { BackgroundAppCard("Spotify", Icons.Default.MusicNote, Color(0xFF1DB954)) }
            item { BackgroundAppCard("WhatsApp", Icons.Default.Chat, Color(0xFF25D366)) }
            item { BackgroundAppCard("Chrome", Icons.Default.Language, Color(0xFF4285F4)) }
        }
    }
}

@Composable
private fun BackgroundAppCard(name: String, icon: ImageVector, iconBg: Color) {
    Surface(
        modifier = Modifier.width(140.dp),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                color = iconBg,
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            Column {
                Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(Emerald, CircleShape))
                    Text(text = "Allowed", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Emerald)
                }
            }
        }
    }
}

@Composable
private fun DataUsageSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S USAGE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "1.8", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(text = "GB", fontSize = 14.sp, color = OnSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(CircleShape)
            ) {
                Box(modifier = Modifier.fillMaxHeight().weight(0.6f).background(Primary))
                Box(modifier = Modifier.fillMaxHeight().weight(0.25f).background(SecondaryContainer))
                Box(modifier = Modifier.fillMaxHeight().weight(0.15f).background(Emerald))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                UsageItem("Foreground", "1.2 GB", Primary)
                UsageItem("Background", "0.6 GB", SecondaryContainer)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF0F1F3))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        color = Color(0xFFEDFAF5),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Savings, contentDescription = null, tint = Emerald)
                        }
                    }
                    Column {
                        Text(text = "Data Saved", fontSize = 11.sp, color = OnSurfaceVariant)
                        Text(text = "340 MB", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Emerald)
                    }
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = OnSurfaceVariant)
            }
        }
    }
}

@Composable
private fun UsageItem(label: String, value: String, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
            Text(text = label, fontSize = 11.sp, color = OnSurfaceVariant)
        }
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DashboardBottomNav() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(Icons.Default.Home, "Home", true)
            BottomNavItem(Icons.Default.Lan, "Network", false)
            BottomNavItem(Icons.Default.Apps, "Apps", false)
            BottomNavItem(Icons.Default.Analytics, "Analytics", false)
            BottomNavItem(Icons.Default.Settings, "Settings", false)
        }
    }
}

@Composable
private fun BottomNavItem(icon: ImageVector, label: String, active: Boolean) {
    if (active) {
        Surface(
            color = SecondaryContainer,
            shape = CircleShape
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = OnSecondaryContainer, modifier = Modifier.size(20.dp))
                Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSecondaryContainer)
            }
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
            Text(text = label, fontSize = 12.sp, color = OnSurfaceVariant)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    DashboardScreen()
}
