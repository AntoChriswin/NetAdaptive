package com.simats.netadaptive.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.netadaptive.data.PredictionRepository
import com.simats.netadaptive.data.repository.AppUsageRepository
import com.simats.netadaptive.data.model.AppUsageData
import com.simats.netadaptive.data.model.NetworkMetrics
import com.simats.netadaptive.data.model.PredictionResult
import com.simats.netadaptive.data.model.PriorityLevel
import com.simats.netadaptive.data.model.User
import com.simats.netadaptive.ml.QualityScoreCalculator
import com.simats.netadaptive.ui.onboarding.*
import java.util.Calendar
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun DashboardScreen(
    user: User? = null,
    metrics: NetworkMetrics? = null,
    prediction: PredictionResult? = null,
    appsUsage: List<AppUsageData> = emptyList(),
    onProfileClick: () -> Unit = {},
    onNetworkClick: () -> Unit = {},
    onAppsClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    // Fallback collection if parameters are null or empty (handles cases where the host activity doesn't pass data)
    val collectedMetrics by PredictionRepository.latestMetrics.collectAsState()
    val collectedPrediction by PredictionRepository.latestPrediction.collectAsState()
    val collectedAppsUsage by AppUsageRepository.appsUsage.collectAsState()

    val displayMetrics = metrics ?: collectedMetrics
    val displayPrediction = prediction ?: collectedPrediction
    val displayAppsUsage = if (appsUsage.isNotEmpty()) appsUsage else collectedAppsUsage

    val scrollState = rememberScrollState()

    // Find the actual active app (highest speed or foreground)
    val activeApp = displayAppsUsage.firstOrNull { it.status == "Foreground" } ?: displayAppsUsage.firstOrNull { it.currentSpeedBytes > 0 } ?: displayAppsUsage.firstOrNull()
    val backgroundApps = displayAppsUsage.filter { it != activeApp }.take(8)

    Scaffold(
        containerColor = Background,
        bottomBar = {
            DashboardBottomNav(
                onHomeClick = {},
                onNetworkClick = onNetworkClick,
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
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DashboardHeader(user = user, onProfileClick = onProfileClick)
            NetworkQualityCard(displayMetrics, displayPrediction)
            PredictionBanner(displayPrediction)
            ActiveAppCard(activeApp)
            BackgroundAppsSection(backgroundApps)
            DataUsageSummaryCard(displayAppsUsage)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DashboardHeader(
    user: User? = null,
    onProfileClick: () -> Unit = {}
) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
    
    val displayName = user?.name ?: "Network Admin"
    val initials = if (displayName.contains(" ")) {
        val parts = displayName.split(" ")
        (parts.getOrNull(0)?.firstOrNull()?.toString() ?: "") + (parts.getOrNull(1)?.firstOrNull()?.toString() ?: "")
    } else {
        displayName.take(2).uppercase()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$greeting, ${user?.name?.split(" ")?.firstOrNull() ?: "there"} 👋",
                fontSize = 13.sp,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Hello, $displayName",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clickable { onProfileClick() },
                shape = CircleShape,
                color = PrimaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initials,
                        color = OnPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkQualityCard(metrics: NetworkMetrics?, prediction: PredictionResult?) {
    // If we have actual metrics, use them to show a score immediately
    val currentQuality = metrics?.let { QualityScoreCalculator.calculate(it.rssi, it.latency, it.packetLoss) } ?: 0
    val qualityScore = prediction?.predictedQualityScore ?: currentQuality
    
    val qualityText = when {
        qualityScore >= 90 -> "EXCELLENT"
        qualityScore >= 75 -> "GOOD"
        qualityScore >= 50 -> "FAIR"
        qualityScore > 0 -> "POOR"
        else -> "SCANNING"
    }
    
    val qualityColor = when {
        qualityScore >= 75 -> Emerald
        qualityScore >= 50 -> Color(0xFFF59E0B)
        qualityScore > 0 -> Color(0xFFEF4444)
        else -> OnSurfaceVariant.copy(alpha = 0.5f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariant.copy(alpha = 0.6f),
                    letterSpacing = 1.2.sp
                )
                
                // Status Badge
                Surface(
                    color = if (metrics != null && (metrics.latency > 0 || metrics.rssi != -100)) Color(0xFFF0FDF4) else Color(0xFFF9FAFB),
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (metrics != null && (metrics.latency > 0 || metrics.rssi != -100)) Emerald else Color.Gray,
                                    CircleShape
                                )
                        )
                        Text(
                            text = if (metrics != null && (metrics.latency > 0 || metrics.rssi != -100)) "Live" else "Scanning...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (metrics != null && (metrics.latency > 0 || metrics.rssi != -100)) Emerald else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Gauge Implementation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    // Background Arc
                    drawArc(
                        color = Color(0xFFF1F3F4),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Foreground Arc
                    if (qualityScore > 0) {
                        drawArc(
                            color = qualityColor,
                            startAngle = 180f,
                            sweepAngle = 180f * (qualityScore / 100f),
                            useCenter = false,
                            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset(y = 15.dp)
                ) {
                    Text(
                        text = if (qualityScore > 0) qualityScore.toString() else "--",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = if (qualityScore > 0) OnSurface else OnSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Text(
                        text = qualityText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = qualityColor,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QualityStatItem(
                    modifier = Modifier.weight(1f), 
                    label = "Latency", 
                    value = if (metrics != null) "${metrics.latency.toInt()}ms" else "--", 
                    dotColor = if (metrics != null && metrics.latency < 50) Emerald else Color(0xFFF59E0B)
                )
                QualityStatItem(
                    modifier = Modifier.weight(1f), 
                    label = "Loss", 
                    value = if (metrics != null) "${"%.1f".format(metrics.packetLoss)}%" else "--", 
                    dotColor = if (metrics != null && metrics.packetLoss < 1) Emerald else Color(0xFFEF4444)
                )
                QualityStatItem(
                    modifier = Modifier.weight(1f), 
                    label = "Signal", 
                    value = if (metrics != null) "${metrics.rssi}dBm" else "--", 
                    dotColor = if (metrics != null && metrics.rssi > -70) Emerald else Color(0xFFF59E0B)
                )
            }
        }
    }
}

@Composable
private fun QualityStatItem(modifier: Modifier, label: String, value: String, dotColor: Color) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF9FAFB),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F3F4))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.size(7.dp).background(dotColor, CircleShape))
                Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            }
        }
    }
}

@Composable
private fun PredictionBanner(prediction: PredictionResult?) {
    val isStable = (prediction?.predictedLatency ?: 0f) > 0f && (prediction?.predictedLatency ?: 24f) < 100f
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isStable) Color(0xFFF0FDF4) else if ((prediction?.predictedLatency ?: 0f) > 0f) Color(0xFFFEF2F2) else Color(0xFFF9FAFB),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isStable) Color(0xFFDCFCE7) else if ((prediction?.predictedLatency ?: 0f) > 0f) Color(0xFFFEE2E2) else Color(0xFFF1F3F4))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isStable) Icons.Default.Radar else if ((prediction?.predictedLatency ?: 0f) > 0f) Icons.Default.Warning else Icons.Default.HourglassEmpty,
                contentDescription = null,
                tint = if (isStable) Emerald else if ((prediction?.predictedLatency ?: 0f) > 0f) Color(0xFFEF4444) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = if (isStable) "Network stable for next 15s" else if ((prediction?.predictedLatency ?: 0f) > 0f) "Potential instability ahead" else "Analyzing network patterns...",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isStable) Color(0xFF166534) else if ((prediction?.predictedLatency ?: 0f) > 0f) Color(0xFF991B1B) else OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActiveAppCard(app: AppUsageData?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F3F4))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE APP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariant.copy(alpha = 0.6f),
                    letterSpacing = 1.2.sp
                )
                Surface(
                    color = if (app?.priority == PriorityLevel.HIGH) PrimaryFixed else SecondaryContainer.copy(alpha = 0.15f),
                    shape = CircleShape
                ) {
                    Text(
                        text = if (app?.priority == PriorityLevel.HIGH) "Priority: High" else if (app != null) "Optimized" else "Scanning",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (app?.priority == PriorityLevel.HIGH) Color(0xFF703700) else OnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = PrimaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (app?.category) {
                                "Gaming" -> Icons.Default.SportsEsports
                                "Streaming" -> Icons.Default.PlayCircle
                                "Social" -> Icons.AutoMirrored.Filled.Chat
                                "Navigation" -> Icons.Default.Map
                                else -> Icons.Default.Apps
                            },
                            contentDescription = null,
                            tint = OnPrimaryContainer,
                            modifier = Modifier.size(34.dp)
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
                        Text(
                            text = app?.name ?: "Detecting app...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = app?.currentSpeed ?: "0 B/s",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            textAlign = TextAlign.End
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F3F4))
                    ) {
                        val progress = if (app != null && app.currentSpeedBytes > 0) 0.65f else 0.05f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(Primary, CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AppSpeedItem(Icons.Default.ArrowDownward, app?.currentSpeed ?: "0 B/s")
                        AppSpeedItem(Icons.Default.History, app?.usageDisplay ?: "0 B")
                    }
                }
            }
        }
    }
}

@Composable
private fun AppSpeedItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = OnSurfaceVariant)
        Text(text = text, fontSize = 12.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BackgroundAppsSection(apps: List<AppUsageData>) {
    Column {
        Text(
            text = "BACKGROUND APPS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceVariant.copy(alpha = 0.6f),
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 4.dp)
        ) {
            if (apps.isEmpty()) {
                item { 
                    Surface(
                        modifier = Modifier.width(160.dp).height(64.dp),
                        color = Color.White,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color(0xFFF1F3F4))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Monitoring...", fontSize = 13.sp, color = OnSurfaceVariant)
                        }
                    }
                }
            } else {
                items(apps) { app ->
                    BackgroundAppCard(
                        name = app.name,
                        icon = when (app.category) {
                            "Gaming" -> Icons.Default.SportsEsports
                            "Streaming" -> Icons.Default.PlayCircle
                            "Social" -> Icons.AutoMirrored.Filled.Chat
                            "Communication" -> Icons.AutoMirrored.Filled.Message
                            "Browser" -> Icons.Default.Language
                            else -> Icons.Default.Apps
                        },
                        iconBg = when (app.priority) {
                            PriorityLevel.HIGH -> PrimaryContainer
                            PriorityLevel.MEDIUM -> SecondaryContainer
                            else -> Color(0xFFE2E8F0)
                        },
                        priority = app.priority
                    )
                }
            }
        }
    }
}

@Composable
private fun BackgroundAppCard(name: String, icon: ImageVector, iconBg: Color, priority: PriorityLevel) {
    Surface(
        modifier = Modifier.width(160.dp),
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F3F4)),
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                color = iconBg,
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = if (iconBg == Color(0xFFE2E8F0)) OnSurfaceVariant else OnPrimaryContainer, modifier = Modifier.size(22.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(if (priority != PriorityLevel.LOW) Emerald else Color(0xFFF59E0B), CircleShape))
                    Text(
                        text = if (priority != PriorityLevel.LOW) "Active" else "Optimized",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (priority != PriorityLevel.LOW) Emerald else Color(0xFFD97706)
                    )
                }
            }
        }
    }
}

@Composable
private fun DataUsageSummaryCard(appsUsage: List<AppUsageData>) {
    val totalBytes = appsUsage.sumOf { it.usageBytes }
    val totalGB = totalBytes / (1024.0 * 1024.0 * 1024.0)
    val foregroundBytes = appsUsage.filter { it.status == "Foreground" }.sumOf { it.usageBytes }
    val backgroundBytes = totalBytes - foregroundBytes

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F3F4))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SESSION USAGE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariant.copy(alpha = 0.6f),
                    letterSpacing = 1.2.sp
                )
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "%.2f".format(totalGB), fontSize = 26.sp, fontWeight = FontWeight.Black)
                    Text(text = "GB", fontSize = 13.sp, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F3F4))
            ) {
                if (totalBytes > 0) {
                    val fgWeight = foregroundBytes.toFloat() / totalBytes
                    val bgWeight = backgroundBytes.toFloat() / totalBytes
                    
                    if (fgWeight > 0) Box(modifier = Modifier.fillMaxHeight().weight(maxOf(0.01f, fgWeight)).background(Primary))
                    if (bgWeight > 0) Box(modifier = Modifier.fillMaxHeight().weight(maxOf(0.01f, bgWeight)).background(SecondaryContainer))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                UsageItem(Modifier.weight(1f), "Foreground", formatBytes(foregroundBytes), Primary)
                UsageItem(Modifier.weight(1f), "Background", formatBytes(backgroundBytes), SecondaryContainer)
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color(0xFFF1F3F4))
            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth().clickable { },
                color = Color(0xFFF0FDF4),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            color = Color.White.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.Savings, contentDescription = null, tint = Emerald, modifier = Modifier.size(22.dp))
                            }
                        }
                        Column {
                            Text(text = "Priority Savings", fontSize = 12.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                            Text(text = formatBytes((totalBytes * 0.12).toLong()), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF166534), modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
    return String.format(Locale.US, "%.1f %s", bytes / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
}

@Composable
private fun UsageItem(modifier: Modifier = Modifier, label: String, value: String, color: Color) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(9.dp).background(color, CircleShape))
            Text(text = label, fontSize = 12.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
        }
        Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DashboardBottomNav(
    onHomeClick: () -> Unit,
    onNetworkClick: () -> Unit,
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
                .padding(vertical = 8.dp, horizontal = 12.dp)
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(Icons.Default.Home, "Home", true, onClick = onHomeClick)
            NavItem(Icons.Default.Lan, "Network", false, onClick = onNetworkClick)
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
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.clickable { onClick() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = OnPrimaryContainer, modifier = Modifier.size(24.dp))
                Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnPrimaryContainer)
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
            Text(text = label, fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    DashboardScreen(
        user = User("123", "Network Admin", "admin@simats.com", null),
        metrics = NetworkMetrics(-65, 24f, 0.5f, 1.2f, 12.5f, 4.2f, "WiFi", "5GHz"),
        prediction = PredictionResult(24f, 32f, 0.5f, 0.8f, 88),
        appsUsage = listOf(
            AppUsageData("com.google.android.youtube", "YouTube", 1024L * 1024 * 450, "450 MB", "2.5 MB/s", 1024L * 1024 * 2, "Foreground", "Streaming", PriorityLevel.HIGH),
            AppUsageData("com.instagram.android", "Instagram", 1024L * 1024 * 120, "120 MB", "150 KB/s", 1024L * 150, "Background", "Social", PriorityLevel.MEDIUM)
        )
    )
}
