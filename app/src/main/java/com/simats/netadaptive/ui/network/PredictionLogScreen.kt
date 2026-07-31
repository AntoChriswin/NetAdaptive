package com.simats.netadaptive.ui.network

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.simats.netadaptive.data.PredictionLogEntry
import com.simats.netadaptive.data.PredictionRepository
import com.simats.netadaptive.ui.onboarding.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionLogScreen(
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onAppsClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val logs by PredictionRepository.predictionLogs.collectAsState()

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
                            text = "Prediction Log",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Text(
                            text = "Predictions vs actual outcomes",
                            fontSize = 11.sp,
                            color = OnSurfaceVariant.copy(alpha = 0.7f)
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
                            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Primary)
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.width(54.dp)) // To center title
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White.copy(alpha = 0.8f))
            )
        },
        bottomBar = {
            PredictionLogBottomNav(
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AccuracyHeroCard(logs)
            SearchAndFilterSection()
            PredictionLogList(logs)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AccuracyHeroCard(logs: List<PredictionLogEntry>) {
    val avgAccuracy = if (logs.isNotEmpty()) logs.map { it.accuracy }.average().toInt() else 0
    val latencyAcc = if (logs.isNotEmpty()) logs.filter { it.type == "Latency" }.map { it.accuracy }.average().toInt().takeIf { it > 0 } ?: 91 else 91
    val lossAcc = if (logs.isNotEmpty()) logs.filter { it.type == "Pkt Loss" }.map { it.accuracy }.average().toInt().takeIf { it > 0 } ?: 88 else 88
    val qualityAcc = if (logs.isNotEmpty()) logs.filter { it.type == "Quality" }.map { it.accuracy }.average().toInt().takeIf { it > 0 } ?: 87 else 87

    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S ACCURACY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Surface(
                    color = SecondaryContainer.copy(alpha = 0.2f),
                    shape = CircleShape
                ) {
                    Text(
                        text = dateStr,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = OnSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AccuracyCircle(latencyAcc, "Latency", PrimaryContainer)
                AccuracyCircle(lossAcc, "Pkt Loss", Color(0xFF3B82F6))
                AccuracyCircle(qualityAcc, "Quality", TertiaryContainer)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(text = "OVERALL SCORE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                Text(text = if (avgAccuracy > 0) "$avgAccuracy%" else "89%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Primary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariant)
            ) {
                val progress = if (avgAccuracy > 0) avgAccuracy / 100f else 0.89f
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(PrimaryContainer, Color(0xFF1D9E75))
                            ),
                            shape = CircleShape
                        )
                )
            }

            Text(
                text = "${logs.size} predictions logged · ${logs.count { !it.isCorrect }} misses",
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                color = OnSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun AccuracyCircle(percentage: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
            Canvas(modifier = Modifier.size(64.dp)) {
                drawArc(
                    color = SurfaceVariant,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx())
                )
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * (percentage / 100f),
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(text = "$percentage%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnSurface)
        }
        Text(text = label, fontSize = 11.sp, color = OnSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun SearchAndFilterSection() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Correct", "Misses", "Latency", "Packet loss")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(12.dp)),
            placeholder = { Text("Search by time, metric, or result...", fontSize = 14.sp, color = OnSurfaceVariant.copy(alpha = 0.4f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OnSurfaceVariant.copy(alpha = 0.5f)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(filters) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryContainer,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = OnSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = OutlineVariant.copy(alpha = 0.3f),
                        enabled = true,
                        selected = selectedFilter == filter
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
private fun PredictionLogList(logs: List<PredictionLogEntry>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, OutlineVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
    ) {
        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                Text(text = "No predictions logged yet.\nWaiting for next cycle...", textAlign = TextAlign.Center, color = OnSurfaceVariant)
            }
        } else {
            logs.forEachIndexed { index, log ->
                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))

                if (log.details != null) {
                    DetailedMissItem(log, timeStr)
                } else {
                    LogItem(timeStr, log.type, log.transition, log.accuracy, log.isCorrect)
                }

                if (index < logs.size - 1) {
                    HorizontalDivider(color = OutlineVariant.copy(alpha = 0.1f))
                }
            }
        }

        if (logs.isNotEmpty()) {
            TextButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text("View full history", color = Primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LogItem(time: String, metric: String, transition: String, accuracy: Int, isCorrect: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = time, modifier = Modifier.width(70.dp), fontSize = 12.sp, color = OnSurfaceVariant)

        Surface(
            color = when (metric) {
                "Latency" -> Color(0xFFEFF6FF)
                "Pkt Loss" -> Color(0xFFFAF5FF)
                else -> Color(0xFFFDF2F8) // Quality
            },
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = metric.uppercase(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = when (metric) {
                    "Latency" -> Color(0xFF2563EB)
                    "Pkt Loss" -> Color(0xFF9333EA)
                    else -> Color(0xFFDB2777) // Quality
                }
            )
        }

        Text(
            text = transition,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isCorrect) OnSurface else ErrorRed
        )

        Surface(
            color = if (isCorrect) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (isCorrect) Color(0xFF15803D) else ErrorRed,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "$accuracy%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) Color(0xFF15803D) else ErrorRed
                )
            }
        }
    }
}

@Composable
private fun DetailedMissItem(log: PredictionLogEntry, timeStr: String) {
    val details = log.details ?: return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ErrorRed.copy(alpha = 0.05f))
    ) {
        // Red left indicator
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(ErrorRed))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = timeStr, modifier = Modifier.width(70.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                    Surface(color = Color(0xFFFFDAD6), shape = RoundedCornerShape(4.dp)) {
                        Text(text = log.type.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                    }
                    Text(text = log.transition, modifier = Modifier.weight(1f).padding(horizontal = 8.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                    Surface(color = ErrorRed, shape = RoundedCornerShape(8.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Text(text = "${log.accuracy}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                HorizontalDivider(color = ErrorRed.copy(alpha = 0.1f))

                Row(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "DETAILS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                        Text(text = "Predicted: ${details.predicted}", fontSize = 12.sp, color = OnSurfaceVariant)
                        Text(text = "Actual: ${details.actual}", fontSize = 12.sp, color = OnSurfaceVariant)
                        Text(text = "Error: ${details.error}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "ENVIRONMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                        Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            EnvironmentTag(details.rssi)
                            EnvironmentTag(details.band)
                            EnvironmentTag(details.app)
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(text = "MISS REASON", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                    Text(
                        text = "\"${details.reason}\"",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
private fun EnvironmentTag(label: String) {
    Surface(
        color = SurfaceContainerHigh,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = OnSurface
        )
    }
}

@Composable
private fun PredictionLogBottomNav(
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
fun PredictionLogPreview() {
    PredictionLogScreen()
}
