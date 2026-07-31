package com.simats.netadaptive.ui.network

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
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
import com.simats.netadaptive.data.model.PredictionResult
import com.simats.netadaptive.ui.onboarding.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivePredictionScreen(
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onAppsClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onConfidenceClick: () -> Unit = {},
    onPredictionLogClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val metrics by PredictionRepository.latestMetrics.collectAsState()
    val prediction by PredictionRepository.latestPrediction.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF4F5F7),
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Live Prediction",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFE9237)
                        )
                        Text(
                            text = "Next 5-minute forecast",
                            fontSize = 11.sp,
                            color = Color(0xFFA8DBDE).copy(alpha = 0.6f)
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
                            .border(1.dp, Color(0xFFE8EAED), CircleShape)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF063A5D))
                    }
                },
                actions = {
                    // Info icon removed
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF063A5D))
            )
        },
        bottomBar = {
            LivePredictionBottomNav(
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LiveStatusPill()
            LatencyForecastCard(metrics, prediction)
            PacketLossForecastCard(metrics, prediction)
            QualityScoreForecastRow(metrics, prediction)
            PredictionLogButton(onClick = onPredictionLogClick)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LiveStatusPill() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            color = Color(0xFFEDFAF5),
            shape = CircleShape,
            border = BorderStroke(1.dp, Color(0xFF1D9E75).copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition()
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(750, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF1D9E75).copy(alpha = alpha), CircleShape)
                )
                Text(
                    text = "Model running · updating every 1s",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D9E75)
                )
                Text(
                    text = "v1.0",
                    fontSize = 12.sp,
                    color = Color(0xFFB0B8C8)
                )
            }
        }
    }
}

@Composable
private fun ConfidenceCheckButton(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE1E2E4).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF1D9E75))
                Column {
                    Text(text = "Check Confidence", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
                    Text(text = "Detailed reliability scores...", fontSize = 11.sp, color = Color(0xFFB0B8C8))
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF897365))
        }
    }
}

@Composable
private fun LatencyForecastCard(metrics: NetworkMetrics?, prediction: PredictionResult?) {
    val currentLatency = metrics?.latency ?: 0f
    val predictedLatency = prediction?.predictedLatency ?: 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFE1E2E4).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFFFE9237),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(text = "Latency Forecast", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
                }
                Surface(
                    color = Color(0xFFEDEEF0),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "ML PREDICTION",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF063A5D)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column {
                    Text(text = "NOW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB0B8C8))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = "${currentLatency.toInt()}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D9E75))
                        Text(text = "ms", fontSize = 12.sp, color = Color(0xFF1D9E75), modifier = Modifier.padding(bottom = 4.dp, start = 2.dp))
                    }
                }
                Icon(
                    Icons.AutoMirrored.Filled.TrendingFlat,
                    contentDescription = null,
                    tint = Color(0xFFDCC1B1),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Column {
                    Text(text = "PREDICTED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB0B8C8))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = "${predictedLatency.toInt()}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (predictedLatency > currentLatency) Color(0xFFFEB913) else Color(0xFF1D9E75))
                        Text(text = "ms", fontSize = 12.sp, color = if (predictedLatency > currentLatency) Color(0xFFFEB913) else Color(0xFF1D9E75), modifier = Modifier.padding(bottom = 4.dp, start = 2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Mappings for visualization
                    fun normalizeH(value: Float) = (1f - (value / 200f)).coerceIn(0f, 1f) * height

                    val nowH = normalizeH(currentLatency)
                    val predH = normalizeH(predictedLatency)

                    val actualPath = Path().apply {
                        moveTo(0f, height * 0.8f)
                        lineTo(width * 0.25f, height * 0.75f)
                        lineTo(width * 0.5f, nowH)
                    }

                    val predictedPath = Path().apply {
                        moveTo(width * 0.5f, nowH)
                        lineTo(width * 0.75f, (nowH + predH) / 2)
                        lineTo(width, predH)
                    }

                    drawPath(
                        path = actualPath,
                        color = Color(0xFFFE9237),
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    drawPath(
                        path = predictedPath,
                        color = Color(0xFFFE9237),
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                        )
                    )

                    drawCircle(
                        color = Color(0xFFFE9237),
                        radius = 4.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(width * 0.5f, nowH)
                    )
                }
                Text(
                    text = "now",
                    modifier = Modifier.align(Alignment.Center).offset(y = (-30).dp),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF063A5D)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = if (predictedLatency <= currentLatency + 10) Color(0xFFEDFAF5) else Color(0xFFFFF8E6),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statusIcon = if (predictedLatency <= currentLatency + 10) Icons.Default.CheckCircle else Icons.Default.Warning
                    val statusTint = if (predictedLatency <= currentLatency + 10) Color(0xFF1D9E75) else Color(0xFFFEB913)
                    val statusText = if (predictedLatency <= currentLatency + 10) "Latency stable — no action needed" else "Slight increase predicted — within acceptable range"
                    
                    Icon(statusIcon, contentDescription = null, tint = statusTint, modifier = Modifier.size(18.dp))
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusTint
                    )
                }
            }
        }
    }
}

@Composable
private fun PacketLossForecastCard(metrics: NetworkMetrics?, prediction: PredictionResult?) {
    val currentLoss = metrics?.packetLoss ?: 0f
    val predictedLoss = prediction?.predictedPacketLoss ?: 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFE1E2E4).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = Color(0xFF2364A0),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(text = "Packet Loss Forecast", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
                }
                Surface(
                    color = Color(0xFFEDEEF0),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "ML PREDICTION",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF063A5D)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column {
                    Text(text = "NOW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB0B8C8))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = "$currentLoss", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (currentLoss < 1.0) Color(0xFF1D9E75) else Color(0xFFBA1A1A))
                        Text(text = "%", fontSize = 12.sp, color = if (currentLoss < 1.0) Color(0xFF1D9E75) else Color(0xFFBA1A1A), modifier = Modifier.padding(bottom = 4.dp, start = 2.dp))
                    }
                }
                Icon(
                    Icons.AutoMirrored.Filled.TrendingFlat,
                    contentDescription = null,
                    tint = Color(0xFFDCC1B1),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Column {
                    Text(text = "PREDICTED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB0B8C8))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = "$predictedLoss", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (predictedLoss < 1.0) Color(0xFF1D9E75) else Color(0xFFFEB913))
                        Text(text = "%", fontSize = 12.sp, color = if (predictedLoss < 1.0) Color(0xFF1D9E75) else Color(0xFFFEB913), modifier = Modifier.padding(bottom = 4.dp, start = 2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    fun normalizeH(value: Float) = (1f - (value / 5f)).coerceIn(0f, 1f) * height
                    
                    val nowH = normalizeH(currentLoss)
                    val predH = normalizeH(predictedLoss)

                    val actualPath = Path().apply {
                        moveTo(0f, height * 0.75f)
                        lineTo(width * 0.25f, height * 0.8f)
                        lineTo(width * 0.5f, nowH)
                    }

                    val predictedPath = Path().apply {
                        moveTo(width * 0.5f, nowH)
                        lineTo(width * 0.75f, (nowH + predH) / 2)
                        lineTo(width, predH)
                    }

                    drawPath(
                        path = actualPath,
                        color = Color(0xFF2364A0),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    drawPath(
                        path = predictedPath,
                        color = Color(0xFF2364A0),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    )

                    drawCircle(
                        color = Color(0xFF2364A0),
                        radius = 3.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(width * 0.5f, nowH)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = if (predictedLoss < 1.0) Color(0xFFEDFAF5) else Color(0xFFFFF8E6),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statusIcon = if (predictedLoss < 1.0) Icons.Default.CheckCircle else Icons.Default.Warning
                    val statusTint = if (predictedLoss < 1.0) Color(0xFF1D9E75) else Color(0xFFFEB913)
                    val statusText = if (predictedLoss < 1.0) "Packet loss stable — no action needed" else "High packet loss predicted soon"

                    Icon(statusIcon, contentDescription = null, tint = statusTint, modifier = Modifier.size(18.dp))
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusTint
                    )
                }
            }
        }
    }
}

@Composable
private fun QualityScoreForecastRow(metrics: NetworkMetrics?, prediction: PredictionResult?) {
    val currentScore = if (metrics != null) {
        (100 - (metrics.latency / 5) - (metrics.packetLoss * 5)).toInt().coerceIn(0, 100)
    } else 0
    val predictedScore = prediction?.predictedQualityScore ?: currentScore
    val scoreDiff = predictedScore - currentScore

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE1E2E4).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFFE9237).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFFFE9237))
                }
                Column {
                    Text(text = "Quality Score", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "$currentScore", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (currentScore >= 60) Color(0xFF1D9E75) else Color(0xFFBA1A1A))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF897365), modifier = Modifier.size(14.dp))
                        Text(text = "$predictedScore", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (predictedScore >= currentScore) Color(0xFF1D9E75) else Color(0xFFFEB913))
                    }
                }
            }
            Surface(
                color = if (scoreDiff >= 0) Color(0xFFEDFAF5) else Color(0xFFFFDAD6).copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (scoreDiff >= 0) "+$scoreDiff" else "$scoreDiff",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = if (scoreDiff >= 0) Color(0xFF1D9E75) else Color(0xFFBA1A1A)
                )
            }
        }
    }
}

// ModelInputsSummaryCard and InputRow removed as requested

@Composable
private fun PredictionLogButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE1E2E4).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.ListAlt, contentDescription = null, tint = Color(0xFF063A5D))
                Column {
                    Text(text = "Prediction Log", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
                    Text(text = "View all past predictions...", fontSize = 11.sp, color = Color(0xFFB0B8C8))
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF897365))
        }
    }
}

@Composable
private fun LivePredictionBottomNav(
    onHomeClick: () -> Unit,
    onAppsClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF063A5D),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiveNavItem(Icons.Default.Home, "Home", false, onClick = onHomeClick)
            LiveNavItem(Icons.Default.Lan, "Network", true, onClick = {})
            LiveNavItem(Icons.Default.Apps, "Apps", false, onClick = onAppsClick)
            LiveNavItem(Icons.Default.Analytics, "Analysis", false, onClick = onAnalyticsClick)
            LiveNavItem(Icons.Default.Settings, "Settings", false, onClick = onSettingsClick)
        }
    }
}

@Composable
private fun LiveNavItem(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    if (active) {
        Surface(
            color = Color(0xFF2364A0).copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.clickable { onClick() }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = Color(0xFFFE9237), modifier = Modifier.size(24.dp))
                Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFFFE9237))
            }
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clickable { onClick() }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFA8DBDE).copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFFA8DBDE).copy(alpha = 0.5f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LivePredictionPreview() {
    LivePredictionScreen()
}
