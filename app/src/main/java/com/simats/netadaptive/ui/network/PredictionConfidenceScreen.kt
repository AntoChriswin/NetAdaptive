package com.simats.netadaptive.ui.network

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionConfidenceScreen(
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onNetworkClick: () -> Unit = {},
    onSecurityClick: () -> Unit = {},
    onNodesClick: () -> Unit = {},
    onLogsClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

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
                            text = "Prediction Confidence",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Model accuracy breakdown",
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
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(38.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF063A5D))
            )
        },
        bottomBar = {
            ConfidenceBottomNav(
                onNetworkClick = onNetworkClick,
                onSecurityClick = onSecurityClick,
                onNodesClick = onNodesClick,
                onLogsClick = onLogsClick
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
            OverallConfidenceHeroCard()
            InputFeatureWeightsCard()
            AccuracyTrackRecordCard()
            PredictionErrorDistributionCard()
            ModelMetadataCard()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OverallConfidenceHeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
                Text(
                    text = "OVERALL CONFIDENCE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF554337),
                    letterSpacing = 1.5.sp
                )
                Surface(
                    color = Color(0xFFEDEEF0),
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, Color(0xFFE1E2E4).copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Model v1.0 · TFLite",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191C1E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(148.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 12.dp.toPx()
                        drawArc(
                            color = Color(0xFFEDEEF0),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth)
                        )
                        drawArc(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFFE9237), Color(0xFF1D9E75))
                            ),
                            startAngle = -90f,
                            sweepAngle = 360f * 0.89f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "89",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF063A5D)
                            )
                            Text(
                                text = "%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF063A5D),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF1D9E75),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "High Confidence",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1D9E75)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = Color(0xFFEDEEF0))
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ConfidenceSubScore("91%", "Latency", Color(0xFFFE9237))
                ConfidenceSubScore("88%", "Packet Loss", Color(0xFF2364A0))
                ConfidenceSubScore("87%", "Quality Score", Color(0xFFFEB913))
            }
        }
    }
}

@Composable
private fun ConfidenceSubScore(percentage: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 3.dp.toPx()
                drawArc(
                    color = Color(0xFFEDEEF0),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
                val sweep = (percentage.replace("%", "").toFloatOrNull() ?: 0f) / 100f * 360f
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
            }
            Text(text = percentage, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, color = Color(0xFF554337))
    }
}

@Composable
private fun InputFeatureWeightsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE1E2E4).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.BarChart, contentDescription = null, tint = Color(0xFF063A5D), modifier = Modifier.size(18.dp))
                Text(
                    text = "Input Feature Weights",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF063A5D)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                WeightBar("Historical RSSI", 0.85f, "85%")
                WeightBar("Current RSSI", 0.72f, "72%")
                WeightBar("Network Type", 0.60f, "60%")
                WeightBar("App Traffic", 0.55f, "55%")
                WeightBar("Time Pattern", 0.48f, "48%")
            }
        }
    }
}

@Composable
private fun WeightBar(label: String, progress: Float, valueText: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF191C1E))
            Text(text = valueText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFE9237))
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = Color(0xFFFE9237),
            trackColor = Color(0xFFEDEEF0),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun AccuracyTrackRecordCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE1E2E4).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Accuracy Track Record",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF063A5D)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TrackStat("Last 24h", "94%")
                TrackStat("Last 7d", "91%")
                TrackStat("All Time", "89%")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sparkline bar chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val heights = listOf(0.60f, 0.75f, 0.65f, 0.85f, 0.70f, 0.90f, 0.94f)
                heights.forEach { h ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(h)
                            .background(
                                color = Color(0xFFFE9237).copy(alpha = if (h == 0.94f) 1f else 0.2f + h * 0.6f),
                                shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.TrackStat(label: String, value: String) {
    Column(
        modifier = Modifier
            .weight(1f)
            .background(Color(0xFFEDEEF0).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF554337))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF944B00))
    }
}

@Composable
private fun PredictionErrorDistributionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE1E2E4).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Prediction Error Distribution",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF063A5D)
            )

            Spacer(modifier = Modifier.height(24.dp))

            ErrorSection("Latency", listOf("±0-10ms", "±10-30ms", "±30-50ms", ">50ms"), listOf(0.70f, 0.15f, 0.10f, 0.05f))
            Spacer(modifier = Modifier.height(24.dp))
            ErrorSection("Packet Loss", listOf("±0-1%", "±1-5%", "±5-10%", ">10%"), listOf(0.65f, 0.20f, 0.10f, 0.05f))
        }
    }
}

@Composable
private fun ErrorSection(title: String, ranges: List<String>, distributions: List<Float>) {
    val colors = listOf(Color(0xFF1D9E75), Color(0xFFFEB913), Color(0xFFFE9237), Color(0xFFBA1A1A))
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF554337))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ranges.forEachIndexed { index, range ->
                Surface(
                    modifier = Modifier.weight(1f),
                    color = colors[index].copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, colors[index].copy(alpha = 0.2f))
                ) {
                    Text(
                        text = range,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors[index],
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(CircleShape)
        ) {
            distributions.forEachIndexed { index, dist ->
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(dist)
                        .background(colors[index])
                )
            }
        }
    }
}

@Composable
private fun ModelMetadataCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE1E2E4).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Model Metadata",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF063A5D)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                MetadataRow("Model Type", "TensorFlow Lite (Edge)")
                HorizontalDivider(color = Color(0xFFEDEEF0))
                MetadataRow("Training Data", "1,204 readings")
                HorizontalDivider(color = Color(0xFFEDEEF0))
                MetadataRow("Last Retrained", "Oct 24, 2023 · 04:12")
                HorizontalDivider(color = Color(0xFFEDEEF0))
                MetadataRow("Inference Time", "<100ms", Color(0xFF1D9E75))
            }
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String, valueColor: Color = Color(0xFF063A5D)) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 16.sp, color = Color(0xFF554337))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun ConfidenceBottomNav(
    onNetworkClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onNodesClick: () -> Unit,
    onLogsClick: () -> Unit
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
            ConfidenceNavItem(Icons.Default.Lan, "Network", true, onClick = onNetworkClick)
            ConfidenceNavItem(Icons.Default.Security, "Security", false, onClick = onSecurityClick)
            ConfidenceNavItem(Icons.Default.AccountTree, "Nodes", false, onClick = onNodesClick)
            ConfidenceNavItem(Icons.AutoMirrored.Filled.ListAlt, "Logs", false, onClick = onLogsClick)
        }
    }
}

@Composable
private fun ConfidenceNavItem(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    if (active) {
        Surface(
            color = Color(0xFF2364A0).copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.clickable { onClick() }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = Color(0xFFFE9237), modifier = Modifier.size(24.dp))
                Text(text = label.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFFFE9237))
            }
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .clickable { onClick() }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFA8DBDE).copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
            Text(text = label.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFFA8DBDE).copy(alpha = 0.5f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PredictionConfidencePreview() {
    PredictionConfidenceScreen()
}
