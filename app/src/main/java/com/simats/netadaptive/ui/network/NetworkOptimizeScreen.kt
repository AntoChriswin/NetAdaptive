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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkOptimizeScreen(
    onHomeClick: () -> Unit = {},
    onAppsClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onPredictionClick: () -> Unit = {},
    onLatencyClick: () -> Unit = {},
    onPacketLossClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val metrics by PredictionRepository.latestMetrics.collectAsState()
    val prediction by PredictionRepository.latestPrediction.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF4F5F7),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Network",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Text(
                            text = if (metrics != null) "Last updated · just now" else "Waiting for data...",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NetworkBottomNav(
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
            NetworkIdentityCard(metrics)
            QualityScoreCard(metrics, prediction)
            SignalStrengthCard(metrics)
            LatencyCard(metrics, onClick = onLatencyClick)
            PacketLossCard(metrics, onClick = onPacketLossClick)
            PredictionPreviewStrip(prediction, onClick = onPredictionClick)
            Spacer(modifier = Modifier.height(80.dp)) // Extra space for bottom nav
        }
    }
}

@Composable
private fun NetworkIdentityCard(metrics: NetworkMetrics?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        color = PrimaryContainer.copy(alpha = 0.1f),
                        shape = CircleShape
                    ) {
                        val networkLabel = when (metrics?.networkType) {
                            "WiFi" -> "WiFi · ${metrics.frequencyBand}"
                            "Cellular" -> "Mobile · 4G/5G"
                            else -> metrics?.networkType ?: "Checking..."
                        }
                        Text(
                            text = networkLabel,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        val isConnected = metrics != null && metrics.networkType != "None"
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (isConnected) Color(0xFF22C55E).copy(alpha = alpha) else Color.Gray,
                                    CircleShape
                                )
                        )
                        Text(
                            text = if (isConnected) "Connected" else "Searching...",
                            fontSize = 12.sp,
                            color = if (isConnected) Color(0xFF16A34A) else Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    val displayName = when {
                        metrics?.ssid != null && metrics.ssid != "<unknown ssid>" -> metrics.ssid
                        metrics?.networkType == "WiFi" -> "Connected WiFi"
                        metrics?.networkType == "Cellular" -> "Mobile Network"
                        else -> "Scanning..."
                    }
                    Text(
                        text = displayName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = if (metrics?.networkType == "WiFi") "Wireless Access Point" else "Provider Network",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    NetworkDetailChip("IP Address", metrics?.ipAddress ?: "--")
                }
                Box(modifier = Modifier.weight(1f)) {
                    NetworkDetailChip("Network Band", metrics?.frequencyBand ?: "--")
                }
            }
        }
    }
}

@Composable
private fun NetworkDetailChip(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF3F4F6),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label.replace(":", ""),
                fontSize = 10.sp,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                color = OnSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QualityScoreCard(metrics: NetworkMetrics?, prediction: com.simats.netadaptive.data.model.PredictionResult?) {
    val score = prediction?.predictedQualityScore ?: 85 
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = "$score", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        Text(text = "/100", fontSize = 24.sp, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val status = when {
                            score >= 80 -> "Excellent"
                            score >= 60 -> "Good"
                            else -> "Fair"
                        }
                        val statusColor = if (score >= 60) Color(0xFF16A34A) else Color(0xFFBA1A1A)
                        Text(text = status, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
                    }
                }

                // Arc Gauge
                Box(
                    modifier = Modifier
                        .size(width = 96.dp, height = 48.dp)
                        .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(modifier = Modifier.size(96.dp)) {
                        drawArc(
                            color = Color(0xFFF3F4F6),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color(0xFF22C55E),
                            startAngle = 180f,
                            sweepAngle = 180f * (score / 100f),
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Signal: -100 (0%) to -40 (100%)
                val signalProgress = ((metrics?.rssi?.plus(100)?.toFloat() ?: 0f) / 60f).coerceIn(0f, 1f)
                // Latency: 0ms (100%) to 200ms (0%)
                val latencyProgress = (1f - ((metrics?.latency ?: 0f) / 200f)).coerceIn(0f, 1f)
                // Packet Loss: 0% (100%) to 5% (0%)
                val lossProgress = (1f - ((metrics?.packetLoss ?: 0f) / 5f)).coerceIn(0f, 1f)

                QualityProgressItem("Signal Strength", signalProgress, PrimaryContainer)
                QualityProgressItem("Latency Stability", latencyProgress, SecondaryContainer)
                QualityProgressItem("Packet Integrity", lossProgress, TertiaryContainer)
            }
        }
    }
}

@Composable
private fun QualityProgressItem(label: String, progress: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, fontSize = 12.sp, color = OnSurfaceVariant)
            Text(text = "${(progress * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurface)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = Color(0xFFF3F4F6),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun SignalStrengthCard(metrics: NetworkMetrics?) {
    val rssi = metrics?.rssi ?: -100
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        Icons.Default.WifiTethering,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(text = "$rssi dBm", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        Text(
                            text = when {
                                rssi > -60 -> "Excellent signal"
                                rssi > -75 -> "Good signal"
                                else -> "Weak signal"
                            }, 
                            fontSize = 12.sp, 
                            color = OnSurfaceVariant
                        )
                    }
                }
                
                // Signal Bars
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    val bars = when {
                        rssi > -60 -> 4
                        rssi > -70 -> 3
                        rssi > -80 -> 2
                        rssi > -90 -> 1
                        else -> 0
                    }
                    SignalBar(12.dp, if (bars >= 1) Primary else Color(0xFFF3F4F6))
                    SignalBar(16.dp, if (bars >= 2) Primary else Color(0xFFF3F4F6))
                    SignalBar(24.dp, if (bars >= 3) Primary else Color(0xFFF3F4F6))
                    SignalBar(32.dp, if (bars >= 4) Primary else Color(0xFFF3F4F6))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Scale Strip
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp) // Room for the pointer icon
                ) {
                    val barWidth = maxWidth
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFBA1A1A), Color(0xFFFCB810), Color(0xFF22C55E))
                                )
                            )
                    )
                    
                    // RSSI Progress: -100 to -40
                    val progress = ((rssi + 100).toFloat() / 60f).coerceIn(0f, 1f)
                    val pointerOffset = barWidth * progress
                    
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier
                            .size(24.dp)
                            .offset(x = pointerOffset - 12.dp, y = (-16).dp)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Weak", fontSize = 11.sp, color = OnSurfaceVariant)
                    Text(text = "Stable", fontSize = 11.sp, color = OnSurfaceVariant)
                    Text(text = "Strong", fontSize = 11.sp, color = OnSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SignalBar(height: androidx.compose.ui.unit.Dp, color: Color) {
    Box(
        modifier = Modifier
            .width(6.dp)
            .height(height)
            .background(color, RoundedCornerShape(topStart = 1.dp, topEnd = 1.dp))
    )
}

@Composable
private fun LatencyCard(metrics: NetworkMetrics?, onClick: () -> Unit = {}) {
    val latency = metrics?.latency ?: 0f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "LATENCY",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = "${latency.toInt()}", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        Text(text = "ms", fontSize = 14.sp, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (latency < 50) "Low latency - Great for gaming" else "Moderate latency",
                fontSize = 12.sp,
                color = if (latency < 50) Color(0xFF16A34A) else OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun LatencyStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = OnSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurface)
    }
}

@Composable
private fun PacketLossCard(metrics: NetworkMetrics?, onClick: () -> Unit = {}) {
    val loss = metrics?.packetLoss ?: 0f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PACKET LOSS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = "$loss", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        Text(text = "%", fontSize = 14.sp, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
                    }
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                    CircularProgressIndicator(
                        progress = { (loss / 10f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxSize(),
                        color = if (loss < 1.0) Color(0xFF16A34A) else Primary,
                        trackColor = Color(0xFFF3F4F6),
                        strokeWidth = 6.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Icon(
                        Icons.Default.Inventory,
                        contentDescription = null,
                        tint = if (loss < 1.0) Color(0xFF16A34A) else Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = if (loss < 1.0) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (loss < 1.0) Color(0xFFBBF7D0) else Color(0xFFFECACA))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (loss < 1.0) Icons.Default.CheckCircle else Icons.Default.Warning, 
                        contentDescription = null, 
                        tint = if (loss < 1.0) Color(0xFF16A34A) else Color(0xFFBA1A1A), 
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (loss < 1.0) "No significant packet loss detected" else "High packet loss detected", 
                        fontSize = 12.sp, 
                        color = if (loss < 1.0) Color(0xFF15803D) else Color(0xFF991B1B)
                    )
                }
            }
        }
    }
}

@Composable
private fun PredictionPreviewStrip(prediction: com.simats.netadaptive.data.model.PredictionResult?, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = SecondaryContainer,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFFEBB1B).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition()
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.9f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF5E4200), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Radar,
                    contentDescription = null,
                    tint = SecondaryContainer,
                    modifier = Modifier.size(20.dp).rotate(scale * 10f)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val status = if (prediction != null) "Next 5m looks stable" else "Analyzing network..."
                Text(text = status, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5E4200))
                Text(
                    text = if (prediction != null) 
                        "Predicted latency: ${prediction.predictedLatency.toInt()} ms | Loss: ${prediction.predictedPacketLoss}%"
                        else "Collecting data for initial forecast",
                    fontSize = 11.sp,
                    color = Color(0xFF5E4200).copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun NetworkBottomNav(
    onHomeClick: () -> Unit,
    onAppsClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Background,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
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
            NavItem(Icons.Default.Widgets, "Apps", false, onClick = onAppsClick)
            NavItem(Icons.Default.Insights, "Analytics", false, onClick = onAnalyticsClick)
            NavItem(Icons.Default.Settings, "Settings", false, onClick = onSettingsClick)
        }
    }
}

@Composable
private fun NavItem(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    if (active) {
        Surface(
            color = PrimaryContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.clickable { onClick() }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
                Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
fun NetworkOptimizePreview() {
    NetworkOptimizeScreen()
}
