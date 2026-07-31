package com.simats.netadaptive.ui.analytics

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.simats.netadaptive.data.repository.DataUsageRepository
import com.simats.netadaptive.ui.onboarding.*
import com.simats.netadaptive.viewmodel.analytics.AnalyticsViewModel

// Local color if not in common
val AquaTint = Color(0xFFA8DBDE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForegroundBackgroundScreen(
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onNetworkClick: () -> Unit = {},
    onAppsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val fgBytes by viewModel.totalForegroundBytes.collectAsState()
    val bgBytes by viewModel.totalBackgroundBytes.collectAsState()
    val appsUsage by viewModel.appsUsage.collectAsState()
    val vpnMetrics by viewModel.vpnMetrics.collectAsState()
    val hourlyUsage by viewModel.hourlyUsage.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF4F5F7),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Foreground vs Background",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            FGBottomNav(
                onHomeClick = onHomeClick,
                onNetworkClick = onNetworkClick,
                onAppsClick = onAppsClick,
                onAnalyticsClick = {},
                onSettingsClick = onSettingsClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            HeroSplitCard(fgBytes, bgBytes, vpnMetrics.blockedPackets * 1024L)
            UsageByHourCard(hourlyUsage)
            PerAppDeepDiveSection(appsUsage)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeroSplitCard(fgBytes: Long, bgBytes: Long, blockedBytes: Long) {
    val total = fgBytes + bgBytes
    val fgPercent = if (total > 0) fgBytes.toFloat() / total else 0.6f
    val bgPercent = 1f - fgPercent

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Foreground Usage", fontSize = 14.sp, color = OnSurfaceVariant)
                Text(text = "Background Usage", fontSize = 14.sp, color = OnSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(maxOf(0.01f, fgPercent))
                        .background(PrimaryContainer),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (fgPercent > 0.1f) {
                        Text(
                            text = "${(fgPercent * 100).toInt()}%",
                            color = OnPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(maxOf(0.01f, bgPercent))
                        .background(AquaTint),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (bgPercent > 0.1f) {
                        Text(
                            text = "${(bgPercent * 100).toInt()}%",
                            color = OnSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                UsageInfoBox(
                    modifier = Modifier.weight(1f),
                    label = "Foreground",
                    value = formatBytes(fgBytes),
                    badge = "Active Use",
                    badgeColor = PrimaryContainer,
                    borderColor = PrimaryContainer
                )
                UsageInfoBox(
                    modifier = Modifier.weight(1f),
                    label = "Background",
                    value = formatBytes(bgBytes),
                    badge = "Passive Use",
                    badgeColor = AquaTint,
                    borderColor = AquaTint,
                    blockedValue = "${formatBytes(blockedBytes)} Blocked"
                )
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.toDouble())).toInt()
    return "%.1f %s".format(bytes / Math.pow(1024.toDouble(), digitGroups.toDouble()), units[digitGroups])
}

@Composable
private fun UsageInfoBox(
    modifier: Modifier,
    label: String,
    value: String,
    badge: String,
    badgeColor: Color,
    borderColor: Color,
    blockedValue: String? = null
) {
    Surface(
        modifier = modifier,
        color = SurfaceContainerLow,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.1f))
    ) {
        Box(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left border indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(borderColor)
                    .align(Alignment.CenterStart)
            )

            Column(modifier = Modifier.padding(12.dp).padding(start = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = label.uppercase(), fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                    }
                    Surface(
                        color = badgeColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = badge,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (badge == "Active Use") Primary else OnSurfaceVariant
                        )
                    }
                }
                if (blockedValue != null) {
                    Text(
                        text = blockedValue,
                        fontSize = 11.sp,
                        color = ErrorRed,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UsageByHourCard(hourlyData: List<DataUsageRepository.HourlyUsagePoint>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "USAGE BY HOUR", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendItem("Fg", PrimaryContainer)
                    LegendItem("Bg", AquaTint)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                if (hourlyData.isEmpty()) {
                    Text("Insufficient data for today", modifier = Modifier.align(Alignment.Center), color = TextMuted)
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val maxBytes = hourlyData.maxOf { maxOf(it.fgBytes, it.bgBytes, 1024L) }.toFloat()

                        // Draw grid
                        val gridStep = h / 4
                        for (i in 0..4) {
                            drawLine(Color.LightGray.copy(alpha = 0.3f), Offset(0f, i * gridStep), Offset(w, i * gridStep))
                        }

                        // Drawing logic for two areas
                        val fgPath = Path()
                        val bgPath = Path()

                        hourlyData.forEachIndexed { index, point ->
                            val x = (index.toFloat() / 23f) * w
                            val fgY = h - (point.fgBytes / maxBytes) * h
                            val bgY = h - (point.bgBytes / maxBytes) * h

                            if (index == 0) {
                                fgPath.moveTo(x, fgY)
                                bgPath.moveTo(x, bgY)
                            } else {
                                fgPath.lineTo(x, fgY)
                                bgPath.lineTo(x, bgY)
                            }
                        }

                        // Close FG path
                        val fgFill = Path().apply {
                            addPath(fgPath)
                            lineTo((hourlyData.size - 1).toFloat() / 23f * w, h)
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(fgFill, color = PrimaryContainer.copy(alpha = 0.5f))
                        drawPath(fgPath, color = PrimaryContainer, style = Stroke(width = 2.dp.toPx()))

                        // Close BG path
                        val bgFill = Path().apply {
                            addPath(bgPath)
                            lineTo((hourlyData.size - 1).toFloat() / 23f * w, h)
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(bgFill, color = AquaTint.copy(alpha = 0.4f))
                        drawPath(bgPath, color = AquaTint, style = Stroke(width = 2.dp.toPx()))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val labels = listOf("12 AM", "6 AM", "12 PM", "6 PM", "11 PM")
                labels.forEach { label ->
                    Text(text = label, fontSize = 11.sp, color = OnSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Text(text = label, fontSize = 12.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PerAppDeepDiveSection(appsUsage: List<com.simats.netadaptive.data.model.AppUsageData>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "Per-App Deep Dive", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurface)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                appsUsage.take(5).forEach { app ->
                    val total = app.fgUsageBytes + app.bgUsageBytes
                    val fgP = if (total > 0) app.fgUsageBytes.toFloat() / total else 0f
                    val bgP = 1f - fgP

                    AppDiveItem(
                        name = app.name,
                        iconUrl = "", // Placeholder
                        totalData = app.usageDisplay,
                        fgPercent = fgP,
                        bgPercent = bgP,
                        showShield = app.priority == com.simats.netadaptive.data.model.PriorityLevel.HIGH,
                        isWarned = app.isThrottled,
                        isBlocked = app.isDelayed
                    )
                }

                TextButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Expand", color = Primary, fontWeight = FontWeight.Bold)
                        Icon(imageVector = Icons.Default.ExpandMore, contentDescription = null, tint = Primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppDiveItem(
    name: String,
    iconUrl: String,
    totalData: String,
    fgPercent: Float,
    bgPercent: Float,
    showShield: Boolean = false,
    isWarned: Boolean = false,
    isBlocked: Boolean = false
) {
    val containerModifier = if (isWarned) {
        Modifier
            .fillMaxWidth()
            .background(SecondaryContainer.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .border(1.dp, SecondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    } else if (isBlocked) {
        Modifier
            .fillMaxWidth()
            .background(ErrorRed.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .border(1.dp, ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    } else {
        Modifier.fillMaxWidth()
    }

    Column(modifier = containerModifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(32.dp).background(Color.LightGray, CircleShape), contentAlignment = Alignment.Center) {
                    Text(name.take(1), fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column {
                    Text(text = name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                    if (showShield) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = Secondary, modifier = Modifier.size(12.dp))
                            Text("Prioritized", fontSize = 10.sp, color = Secondary)
                        }
                    }
                }
            }
            Text(text = totalData, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceVariant)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(SurfaceContainer)
        ) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fgPercent).background(PrimaryContainer))
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .background(if (isBlocked) AquaTint.copy(alpha = 0.4f) else AquaTint)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "${(fgPercent * 100).toInt()}% Foreground", fontSize = 11.sp, color = OnSurfaceVariant)
            Text(
                text = if (isBlocked) "${(bgPercent * 100).toInt()}% Blocked" else "${(bgPercent * 100).toInt()}% Background",
                fontSize = 11.sp,
                color = OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun FGBottomNav(
    onHomeClick: () -> Unit,
    onNetworkClick: () -> Unit,
    onAppsClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp)
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FGNavItem(Icons.Default.Home, "Home", false, onClick = onHomeClick)
            FGNavItem(Icons.Default.Lan, "Network", false, onClick = onNetworkClick)
            FGNavItem(Icons.Default.Widgets, "Apps", false, onClick = onAppsClick)
            FGNavItem(Icons.Default.Insights, "Analytics", true, onClick = onAnalyticsClick)
            FGNavItem(Icons.Default.Settings, "Settings", false, onClick = onSettingsClick)
        }
    }
}

@Composable
private fun FGNavItem(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    if (active) {
        Surface(
            color = PrimaryContainer.copy(alpha = 0.1f),
            shape = CircleShape,
            modifier = Modifier.clickable { onClick() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = Primary, modifier = Modifier.size(22.dp))
                Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Primary)
            }
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable { onClick() }
                .padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Text(text = label, fontSize = 10.sp, color = OnSurfaceVariant)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForegroundBackgroundPreview() {
    ForegroundBackgroundScreen()
}
