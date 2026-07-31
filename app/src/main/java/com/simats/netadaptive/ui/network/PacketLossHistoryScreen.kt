package com.simats.netadaptive.ui.network

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.netadaptive.data.PredictionRepository
import com.simats.netadaptive.data.model.NetworkMetrics
import com.simats.netadaptive.ui.onboarding.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacketLossHistoryScreen(
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onAppsClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val metrics by PredictionRepository.latestMetrics.collectAsState()
    val history by PredictionRepository.metricsHistory.collectAsState()

    // Calculations based on history
    val losses = history.map { it.packetLoss }
    val minLoss = if (losses.isNotEmpty()) losses.minOrNull() ?: 0f else 0f
    val maxLoss = if (losses.isNotEmpty()) losses.maxOrNull() ?: 0f else 0f
    val avgLoss = if (losses.isNotEmpty()) losses.average().toFloat() else 0f
    val p95Loss = if (losses.isNotEmpty()) {
        val sorted = losses.sorted()
        val index = (0.95 * (sorted.size - 1)).roundToInt()
        sorted[index]
    } else 0f

    Scaffold(
        containerColor = Color(0xFFF4F5F7),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Packet Loss History",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = Ocean
                        )
                        Text(
                            text = "Data loss rate over time",
                            fontSize = 11.sp,
                            color = OnSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, SurfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Ocean)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            PacketLossBottomNav(
                onHomeClick = onHomeClick,
                onAppsClick = onAppsClick,
                onAnalyticsClick = onAnalyticsClick,
                onSettingsClick = onSettingsClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PacketLossHeroCard(metrics, minLoss, maxLoss, avgLoss, p95Loss)
            PacketLossChartCard(history, avgLoss)
            SpikeEventsList(history)
            LossDistributionCard(history)
            ImpactOnAppsCard(history)
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun PacketLossHeroCard(
    metrics: NetworkMetrics?,
    min: Float,
    max: Float,
    avg: Float,
    p95: Float
) {
    val loss = metrics?.packetLoss ?: 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
                    text = "CURRENT PACKET LOSS",
                    fontSize = 11.sp,
                    color = OnSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Surface(
                    color = SuccessGreen.copy(alpha = 0.1f),
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(SuccessGreen.copy(alpha = alpha), CircleShape)
                        )
                        Text(
                            text = "Live",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${"%.1f".format(loss)}%",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (loss < 2.0) SuccessGreen else if (loss < 10.0) Color(0xFFFCB810) else ErrorRed
                )
                Spacer(modifier = Modifier.width(16.dp))
                Surface(
                    color = SuccessGreen.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "Real-time updates",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }

            Text(
                text = when {
                    loss < 2.0 -> "Negligible loss"
                    loss < 10.0 -> "Moderate loss"
                    else -> "High loss detected"
                },
                fontSize = 14.sp,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = Color(0xFFF3F4F6),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatBox("Min", "${"%.1f".format(min)}%", Ocean, modifier = Modifier.weight(1f))
                    StatBox("Max", "${"%.1f".format(max)}%", ErrorRed, showStartBorder = true, modifier = Modifier.weight(1f))
                    StatBox("Avg", "${"%.1f".format(avg)}%", Azure, showStartBorder = true, modifier = Modifier.weight(1f))
                    StatBox("P95", "${"%.1f".format(p95)}%", Color(0xFFFCB810), showStartBorder = true, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, valueColor: Color, showStartBorder: Boolean = false, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (showStartBorder) {
            Box(modifier = Modifier.width(1.dp).height(32.dp).background(OutlineVariant.copy(alpha = 0.3f)))
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column {
            Text(text = label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
private fun PacketLossChartCard(history: List<NetworkMetrics>, avgLoss: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
                    text = "PACKET LOSS OVER SESSION",
                    fontSize = 11.sp,
                    color = OnSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                val spikeCount = history.count { it.packetLoss > 5.0 }
                if (spikeCount > 0) {
                    Surface(
                        color = ErrorRed.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                            Text(text = "$spikeCount spikes detected", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                // Background Zones (Weights match linear scale: 0-2% = 0.1, 2-10% = 0.4, 10-20% = 0.5)
                Column(modifier = Modifier.fillMaxSize()) {
                    ChartZone(Color(0xFFD85A30), "POOR (10%+)", 0.5f)
                    ChartZone(Color(0xFFFCB810), "FAIR (2-10%)", 0.4f)
                    ChartZone(Color(0xFF1D9E75), "GOOD (0-2%)", 0.1f)
                }

                // Average Line
                if (history.isNotEmpty()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxScale = 20f // %
                        val y = size.height - (avgLoss / maxScale * size.height).coerceAtMost(size.height)
                        drawLine(
                            color = Azure.copy(alpha = 0.4f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }
                    val avgYOffset = (240 - (avgLoss / 20f * 240).coerceAtMost(240f)).dp
                    Text(
                        text = "AVG ${"%.1f".format(avgLoss)}%",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 8.dp)
                            .offset(y = avgYOffset - 15.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Azure
                    )
                }

                // Chart Path
                if (history.size > 1) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxScale = 20f // %
                        val path = Path()
                        val stepX = size.width / (history.size - 1)

                        history.forEachIndexed { index, metric ->
                            val x = index * stepX
                            val y = size.height - (metric.packetLoss / maxScale * size.height).coerceAtMost(size.height)
                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        drawPath(
                            path = path,
                            color = Azure,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Azure.copy(alpha = 0.1f), Color.Transparent)
                            )
                        )
                    }
                } else if (history.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Waiting for data...", color = OnSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val times = listOf("Start", "", "", "", "NOW")
                times.forEach { time ->
                    Text(
                        text = time,
                        fontSize = 10.sp,
                        color = OnSurfaceVariant,
                        fontWeight = if (time == "NOW") FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.ChartZone(color: Color, label: String, weight: Float) {
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxWidth()
            .background(color.copy(alpha = 0.05f))
            .border(width = 0.5.dp, color = color.copy(alpha = 0.2f), shape = RectangleShape)
    ) {
        Text(
            text = label,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun SpikeEventsList(history: List<NetworkMetrics>) {
    val spikes = history.filter { it.packetLoss > 5.0 }.takeLast(2).reversed()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "SESSION SPIKE EVENTS",
                fontSize = 11.sp,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            if (spikes.isNotEmpty()) {
                spikes.forEach { metric ->
                    SpikeEventItem(
                        title = "Packet loss spike",
                        time = "Just now",
                        peak = "${"%.1f".format(metric.packetLoss)}%",
                        duration = "22s",
                        cause = "Weak signal strength",
                        icon = Icons.Default.NetworkCheck,
                        iconColor = Color(0xFFD85A30)
                    )
                }
            } else {
                Text(
                    text = "No packet loss spikes in current session",
                    fontSize = 13.sp,
                    color = SuccessGreen,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SpikeEventItem(
    title: String,
    time: String,
    peak: String,
    duration: String,
    cause: String,
    icon: ImageVector,
    iconColor: Color,
    isSubtle: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE1E2E4).copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .then(if (isSubtle) Modifier.alpha(0.8f) else Modifier)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ocean)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "$time • ", fontSize = 12.sp, color = OnSurfaceVariant)
                        Text(text = "Peak: $peak", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = iconColor)
                        Text(text = " • Duration: $duration", fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                }
            }
            if (!isSubtle) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFE1E2E4).copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    Text(
                        text = "Cause: ",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(start = 64.dp)
                    )
                    Text(
                        text = cause,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Ocean
                    )
                }
            }
        }
    }
}

@Composable
private fun LossDistributionCard(history: List<NetworkMetrics>) {
    val total = history.size.coerceAtLeast(1)
    val negligible = history.count { it.packetLoss < 2.0 }.toFloat() / total
    val moderate = history.count { it.packetLoss in 2.0..10.0 }.toFloat() / total
    val high = history.count { it.packetLoss in 10.0..15.0 }.toFloat() / total
    val severe = history.count { it.packetLoss > 15.0 }.toFloat() / total

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(
                text = "SESSION LOSS DISTRIBUTION",
                fontSize = 11.sp,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            DistributionItem("Negligible (0-2%)", negligible, SuccessGreen)
            DistributionItem("Moderate (2-10%)", moderate, Color(0xFFFCB810))
            DistributionItem("High (10-15%)", high, Color(0xFFFE9237))
            DistributionItem("Severe (15%+)", severe, Color(0xFFD85A30))
        }
    }
}

@Composable
private fun DistributionItem(label: String, progress: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = "${(progress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurface)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = color,
            trackColor = Color(0xFFF3F4F6),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun ImpactOnAppsCard(history: List<NetworkMetrics>) {
    val lastLoss = history.lastOrNull()?.packetLoss ?: 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(
                text = "IMPACT ON APPS (LIVE)",
                fontSize = 11.sp,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                AppImpactItem(Modifier.weight(1f), "YouTube", (lastLoss * 1.5f).coerceIn(0f, 20f) / 20f, "${"%.1f".format(lastLoss * 1.5f)}% loss", Color(0xFFD85A30))
                AppImpactItem(Modifier.weight(1f), "Spotify", (lastLoss * 0.8f).coerceIn(0f, 20f) / 20f, "${"%.1f".format(lastLoss * 0.8f)}% loss", Color(0xFFFCB810))
                AppImpactItem(Modifier.weight(1f), "WhatsApp", (lastLoss * 0.4f).coerceIn(0f, 20f) / 20f, "${"%.1f".format(lastLoss * 0.4f)}% loss", Color(0xFFFE9237))
            }
        }
    }
}

@Composable
private fun AppImpactItem(modifier: Modifier, name: String, progress: Float, peakText: String, color: Color) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = OnSurface)
        Text(text = peakText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .width(60.dp)
                .height(8.dp)
                .clip(CircleShape),
            color = color,
            trackColor = Color(0xFFF3F4F6),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun PacketLossBottomNav(
    onHomeClick: () -> Unit,
    onAppsClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(Icons.Default.Home, "Home", false, onClick = onHomeClick)
            NavItem(Icons.Default.Lan, "Network", true, onClick = {})
            NavItem(Icons.Default.Apps, "Apps", false, onClick = onAppsClick)
            NavItem(Icons.Default.Analytics, "Analytics", false, onClick = onAnalyticsClick)
            NavItem(Icons.Default.Settings, "Settings", false, onClick = onSettingsClick)
        }
    }
}

@Composable
private fun NavItem(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .then(if (active) Modifier.background(SecondaryContainer) else Modifier)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) OnSecondaryContainer else OnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                color = if (active) OnSecondaryContainer else OnSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PacketLossHistoryPreview() {
    PacketLossHistoryScreen()
}
