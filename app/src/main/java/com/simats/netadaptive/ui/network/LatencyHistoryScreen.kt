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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
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
fun LatencyHistoryScreen(
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
    val latencies = history.map { it.latency }
    val minLatency = if (latencies.isNotEmpty()) latencies.minOrNull()?.roundToInt() ?: 0 else 0
    val maxLatency = if (latencies.isNotEmpty()) latencies.maxOrNull()?.roundToInt() ?: 0 else 0
    val avgLatency = if (latencies.isNotEmpty()) latencies.average().roundToInt() else 0
    val p95Latency = if (latencies.isNotEmpty()) {
        val sorted = latencies.sorted()
        val index = (0.95 * (sorted.size - 1)).roundToInt()
        sorted[index].roundToInt()
    } else 0

    Scaffold(
        containerColor = Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Latency History",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = "RESPONSE TIME OVER TIME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(38.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, OutlineVariant.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Primary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(38.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, OutlineVariant.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Primary, modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            LatencyBottomNav(
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LatencyHeroCard(metrics, minLatency, maxLatency, avgLatency, p95Latency)
            LatencyChartCard(history, avgLatency)
            SpikeEventsCard(history)
            LatencyDistributionCard(history)
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun LatencyHeroCard(
    metrics: NetworkMetrics?,
    min: Int,
    max: Int,
    avg: Int,
    p95: Int
) {
    val currentLatency = metrics?.latency?.toInt() ?: 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CURRENT LATENCY",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Surface(
                    color = SuccessGreen.copy(alpha = 0.1f),
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                .size(6.dp)
                                .background(SuccessGreen.copy(alpha = alpha), CircleShape)
                        )
                        Text(
                            text = "Live",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SuccessGreen
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "$currentLatency",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
                Text(
                    text = "ms",
                    fontSize = 24.sp,
                    color = SuccessGreen.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statusText = when {
                    currentLatency < 30 -> "Excellent response time"
                    currentLatency < 100 -> "Good response time"
                    else -> "Poor response time"
                }
                val statusColor = when {
                    currentLatency < 30 -> SuccessGreen
                    currentLatency < 100 -> Color(0xFFFCB810)
                    else -> ErrorRed
                }
                Text(
                    text = statusText,
                    fontSize = 13.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Real-time updates",
                    fontSize = 11.sp,
                    color = OnSurfaceVariant
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = OutlineVariant.copy(alpha = 0.3f)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                LatencyStatItem(Modifier.weight(1f), "Min", "${min}ms")
                LatencyStatItem(Modifier.weight(1f), "Max", "${max}ms")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                LatencyStatItem(Modifier.weight(1f), "Avg", "${avg}ms")
                LatencyStatItem(Modifier.weight(1f), "P95", "${p95}ms")
            }
        }
    }
}

@Composable
private fun LatencyStatItem(modifier: Modifier, label: String, value: String) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label.uppercase(), fontSize = 11.sp, color = OnSurfaceVariant)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
    }
}

@Composable
private fun LatencyChartCard(history: List<NetworkMetrics>, avgLatency: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIVE LATENCY TRACKER",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                val spikeCount = history.count { it.latency > 150 }
                if (spikeCount > 0) {
                    Surface(
                        color = ErrorRed.copy(alpha = 0.1f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "$spikeCount SPIKES DETECTED",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, OutlineVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
            ) {
                // Background Zones
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(ErrorRed.copy(alpha = 0.05f))) {
                        Text(
                            text = "POOR (150ms+)",
                            modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed.copy(alpha = 0.5f)
                        )
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(SecondaryContainer.copy(alpha = 0.05f))) {
                        Text(
                            text = "FAIR (50-150ms)",
                            modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSecondaryContainer.copy(alpha = 0.5f)
                        )
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(SuccessGreen.copy(alpha = 0.05f))) {
                        Text(
                            text = "GOOD (0-50ms)",
                            modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen.copy(alpha = 0.5f)
                        )
                    }
                }

                // Average Line
                if (history.isNotEmpty()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxScale = 250f // ms
                        val y = size.height - (avgLatency.toFloat() / maxScale * size.height).coerceAtMost(size.height)
                        drawLine(
                            color = OnSurfaceVariant.copy(alpha = 0.3f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    val avgYOffset = (200 - (avgLatency.toFloat() / 250f * 200).coerceAtMost(200f)).dp
                    Text(
                        text = "avg ${avgLatency}ms",
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 8.dp)
                            .offset(y = avgYOffset - 12.dp)
                            .background(Color.White.copy(alpha = 0.8f))
                            .padding(horizontal = 4.dp),
                        fontSize = 9.sp,
                        color = OnSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Chart Path
                if (history.size > 1) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxScale = 250f // ms
                        val path = Path()
                        val stepX = size.width / (history.size - 1)

                        history.forEachIndexed { index, metric ->
                            val x = index * stepX
                            val y = size.height - (metric.latency / maxScale * size.height).coerceAtMost(size.height)
                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        drawPath(
                            path = path,
                            color = PrimaryContainer,
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
                                colors = listOf(PrimaryContainer.copy(alpha = 0.2f), Color.Transparent)
                            )
                        )
                    }
                } else if (history.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Waiting for data...", color = OnSurfaceVariant)
                    }
                }

                // Current Value Marker
                if (history.isNotEmpty()) {
                    val last = history.last()
                    val maxScale = 250f
                    val yPos = (last.latency / maxScale * 200).coerceAtMost(200f).dp

                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 8.dp)
                            .offset(y = (200.dp - yPos) - 45.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = OnSurface,
                            shape = RoundedCornerShape(4.dp),
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                text = "${last.latency.roundToInt()}ms",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Box(modifier = Modifier.width(2.dp).height(12.dp).background(PrimaryContainer))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(PrimaryContainer, CircleShape)
                                .border(1.dp, Color.White, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpikeEventsCard(history: List<NetworkMetrics>) {
    val spikes = history.filter { it.latency > 150 }.takeLast(3).reversed()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "RECENT SPIKE EVENTS",
                fontSize = 12.sp,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            if (spikes.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    spikes.forEachIndexed { index, metric ->
                        SpikeEventRow("Latency spike", "${metric.latency.roundToInt()}ms", "Just now · Device: WiFi")
                        if (index < spikes.size - 1) {
                            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.1f))
                        }
                    }
                }
            } else {
                Text(
                    text = "No spikes detected in current session",
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
private fun SpikeEventRow(title: String, value: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(ErrorRed.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
                Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
            }
            Text(text = subtitle, fontSize = 11.sp, color = OnSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                Text(text = "OPTIMIZED BY NETADAPTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Primary)
            }
        }
    }
}

@Composable
private fun LatencyDistributionCard(history: List<NetworkMetrics>) {
    val total = history.size.coerceAtLeast(1)
    val excellent = history.count { it.latency < 30 }.toFloat() / total
    val good = history.count { it.latency in 30.0..50.0 }.toFloat() / total
    val fair = history.count { it.latency in 50.0..150.0 }.toFloat() / total
    val poor = history.count { it.latency > 150.0 }.toFloat() / total

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SESSION LATENCY DISTRIBUTION",
                fontSize = 12.sp,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .border(1.dp, OutlineVariant.copy(alpha = 0.1f), CircleShape)
            ) {
                if (excellent > 0) Box(modifier = Modifier.weight(excellent).fillMaxHeight().background(SuccessGreen))
                if (good > 0) Box(modifier = Modifier.weight(good).fillMaxHeight().background(PrimaryContainer))
                if (fair > 0) Box(modifier = Modifier.weight(fair).fillMaxHeight().background(SecondaryContainer))
                if (poor > 0) Box(modifier = Modifier.weight(poor).fillMaxHeight().background(ErrorRed))
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    DistributionLegendItem(Modifier.weight(1f), SuccessGreen, "Excellent (${(excellent * 100).roundToInt()}%)")
                    DistributionLegendItem(Modifier.weight(1f), PrimaryContainer, "Good (${(good * 100).roundToInt()}%)")
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    DistributionLegendItem(Modifier.weight(1f), SecondaryContainer, "Fair (${(fair * 100).roundToInt()}%)")
                    DistributionLegendItem(Modifier.weight(1f), ErrorRed, "Poor (${(poor * 100).roundToInt()}%)")
                }
            }
        }
    }
}

@Composable
private fun DistributionLegendItem(modifier: Modifier, color: Color, label: String) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = OnSurfaceVariant)
    }
}

@Composable
private fun LatencyBottomNav(
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
    if (active) {
        Surface(
            color = SecondaryContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.clickable { onClick() }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = OnSecondaryContainer, modifier = Modifier.size(24.dp))
                Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnSecondaryContainer)
            }
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clickable { onClick() }
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
            Text(text = label, fontSize = 10.sp, color = OnSurfaceVariant)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LatencyHistoryPreview() {
    LatencyHistoryScreen()
}
