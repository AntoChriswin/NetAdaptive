package com.simats.netadaptive.ui.analytics

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.simats.netadaptive.ui.onboarding.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.simats.netadaptive.viewmodel.analytics.AnalyticsViewModel
import com.simats.netadaptive.data.model.AppUsageData
import com.simats.netadaptive.data.local.entities.AnalyticsDailyEntity

@Composable
fun AnalyticsScreen(
    onHomeClick: () -> Unit = {},
    onNetworkClick: () -> Unit = {},
    onAppsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onTotalDataUsageClick: () -> Unit = {},
    onPerAppDataClick: () -> Unit = {},
    onForegroundBackgroundClick: () -> Unit = {},
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    
    val latestMetrics by viewModel.latestMetrics.collectAsState(null)
    val vpnMetrics by viewModel.vpnMetrics.collectAsState()
    val dataSaved by viewModel.dataSaved.collectAsState()
    val spikesAvoided by viewModel.spikesAvoided.collectAsState()
    val qualityScore by viewModel.qualityScore.collectAsState()
    val uptimePercent by viewModel.uptimePercent.collectAsState()
    val totalOptimizations by viewModel.totalOptimizations.collectAsState()
    val appsUsage by viewModel.appsUsage.collectAsState()
    val weeklyAnalytics by viewModel.weeklyAnalytics.collectAsState()
    val mostOptimizedApp by viewModel.mostOptimizedApp.collectAsState()

    Scaffold(
        containerColor = Background,
        bottomBar = {
            AnalyticsBottomNav(
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
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AnalyticsHeader()
            HeroSummaryGrid(dataSaved, spikesAvoided, qualityScore, uptimePercent)
            KeyMetricsRow(
                latency = latestMetrics?.latency ?: 0f,
                packetLoss = latestMetrics?.packetLoss ?: 0f,
                optimizations = totalOptimizations
            )
            WeeklyTrendCard(weeklyAnalytics)
            
            TopConsumerSection(appsUsage)
            
            mostOptimizedApp?.let { app: AppUsageData ->
                MostOptimizedAppCard(app)
            }
            
            VpnStatisticsCard(vpnMetrics)
            
            DetailedReportsList(
                onTotalDataUsageClick = onTotalDataUsageClick,
                onPerAppDataClick = onPerAppDataClick,
                onForegroundBackgroundClick = onForegroundBackgroundClick
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AnalyticsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Analytics",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Ocean
            )
            Text(
                text = "May 2026 · all devices",
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun HeroSummaryGrid(
    dataSaved: String,
    spikesAvoided: Int,
    qualityScore: Int,
    uptimePercent: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "THIS WEEK AT A GLANCE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HeroMetricCard(
                        modifier = Modifier.weight(1f),
                        label = "Data saved",
                        value = dataSaved,
                        badgeText = "Saved",
                        badgeIcon = Icons.Default.TrendingUp,
                        gradient = Brush.linearGradient(listOf(PrimaryContainer, Primary)),
                        icon = Icons.Default.CloudDownload
                    )
                    HeroMetricCard(
                        modifier = Modifier.weight(1f),
                        label = "Spikes avoided",
                        value = spikesAvoided.toString(),
                        badgeText = "Safe",
                        badgeIcon = Icons.Default.Shield,
                        gradient = Brush.linearGradient(listOf(Azure, Ocean)),
                        icon = Icons.Default.Bolt
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HeroMetricCard(
                        modifier = Modifier.weight(1f),
                        label = "Quality score",
                        value = qualityScore.toString(),
                        isProgress = true,
                        progress = qualityScore / 100f,
                        gradient = Brush.linearGradient(listOf(SuccessGreen, Color(0xFF0D4D3A))),
                        icon = Icons.Default.Speed
                    )
                    HeroMetricCard(
                        modifier = Modifier.weight(1f),
                        label = "Uptime",
                        value = uptimePercent,
                        badgeText = "VPN",
                        badgeIcon = Icons.Default.CheckCircle,
                        gradient = Brush.linearGradient(listOf(NotificationGold, Color(0xFF7C5800))),
                        icon = Icons.Default.Sensors
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    badgeText: String? = null,
    badgeIcon: ImageVector? = null,
    isProgress: Boolean = false,
    progress: Float = 0f,
    gradient: Brush,
    icon: ImageVector
) {
    Box(
        modifier = modifier
            .height(112.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(gradient)
            .padding(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.1f),
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 16.dp, y = 16.dp)
        )
        
        Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(text = label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                Text(text = value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            if (isProgress) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(Color.White, CircleShape)
                    )
                }
            } else if (badgeText != null) {
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (badgeIcon != null) {
                        Icon(imageVector = badgeIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                    Text(text = badgeText, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun KeyMetricsRow(latency: Float, packetLoss: Float, optimizations: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SmallMetricCard(Modifier.weight(1f), Icons.Default.Schedule, "${latency.toInt()}ms", "Latency")
        SmallMetricCard(Modifier.weight(1f), Icons.Default.WifiOff, "${"%.1f".format(packetLoss)}%", "Loss")
        SmallMetricCard(Modifier.weight(1f), Icons.Default.Bolt, optimizations.toString(), "Optims")
    }
}

@Composable
private fun SmallMetricCard(modifier: Modifier, icon: ImageVector, value: String, label: String) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Text(text = label, fontSize = 11.sp, color = OnSurfaceVariant)
        }
    }
}

@Composable
private fun WeeklyTrendCard(weeklyData: List<AnalyticsDailyEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "QUALITY SCORE · 7 DAYS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                if (weeklyData.isEmpty()) {
                    Text("No data available", modifier = Modifier.align(Alignment.Center), color = TextMuted)
                } else {
                    QualityLineChart(
                        modifier = Modifier.fillMaxSize(),
                        data = weeklyData.reversed()
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(ErrorRed, "Spike")
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem(SecondaryContainer, "Peak")
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem(SuccessGreen, "Avg")
            }
        }
    }
}

@Composable
private fun QualityLineChart(modifier: Modifier, data: List<AnalyticsDailyEntity>) {
    val primaryColor = Primary.toArgb()
    
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(false)
                setDrawGridBackground(false)
                
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    textColor = android.graphics.Color.GRAY
                    textSize = 8f
                    granularity = 1f
                }
                
                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = android.graphics.Color.LTGRAY
                    setDrawAxisLine(false)
                    textColor = android.graphics.Color.GRAY
                    textSize = 8f
                    axisMinimum = 0f
                    axisMaximum = 100f
                }
                
                axisRight.isEnabled = false
            }
        },
        modifier = modifier,
        update = { chart ->
            val entries = data.mapIndexed { index, entity ->
                Entry(index.toFloat(), entity.avgQualityScore)
            }
            
            val dataSet = LineDataSet(entries, "Quality Score").apply {
                color = primaryColor
                setDrawCircles(true)
                setCircleColor(primaryColor)
                circleRadius = 3f
                setDrawValues(false)
                lineWidth = 2f
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawFilled(true)
                fillColor = primaryColor
                fillAlpha = 40
            }
            
            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}

@Composable
private fun TopConsumerSection(appsUsage: List<AppUsageData>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Top Bandwidth Consumers",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )
        
        appsUsage.take(3).forEach { app ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(32.dp).background(PrimaryContainer.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                            Text(app.name.take(1), fontWeight = FontWeight.Bold, color = Primary)
                        }
                        Column {
                            Text(text = app.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = app.packageName, fontSize = 10.sp, color = TextMuted)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = app.usageDisplay, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ocean)
                        Text(text = app.currentSpeed, fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun MostOptimizedAppCard(app: AppUsageData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, tint = SuccessGreen)
            Column {
                Text(text = "MOST OPTIMIZED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                Text(text = app.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                Text(text = "Efficiency improved by adaptive bandwidth", fontSize = 12.sp, color = TextMuted)
            }
        }
    }
}

@Composable
private fun VpnStatisticsCard(metrics: com.simats.netadaptive.vpn.models.VpnMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "VPN STATISTICS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                VpnStatItem("Processed", metrics.forwardedPackets.toString(), Icons.Default.Inventory)
                VpnStatItem("Optimized", metrics.delayedPackets.toString(), Icons.Default.Speed)
                VpnStatItem("Dropped", metrics.blockedPackets.toString(), Icons.Default.Block)
            }
        }
    }
}

@Composable
private fun VpnStatItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurface)
        Text(text = label, fontSize = 10.sp, color = TextMuted)
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Text(text = label, fontSize = 11.sp, color = OnSurfaceVariant)
    }
}

@Composable
private fun DetailedReportsList(
    onTotalDataUsageClick: () -> Unit,
    onPerAppDataClick: () -> Unit,
    onForegroundBackgroundClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Detailed Reports",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        ReportItem(Icons.Default.DataUsage, "Total Data Usage", onClick = onTotalDataUsageClick)
        ReportItem(Icons.Default.Layers, "Foreground vs Background", onClick = onForegroundBackgroundClick)
        ReportItem(Icons.Default.Apps, "Per-App Data Report", onClick = onPerAppDataClick)
    }
}

@Composable
private fun ReportItem(icon: ImageVector, title: String, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(imageVector = icon, contentDescription = null, tint = Primary)
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = OnSurface)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = SurfaceVariant)
        }
    }
}

@Composable
private fun AnalyticsBottomNav(
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
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(Icons.Default.Home, "Home", false, onClick = onHomeClick)
            NavItem(Icons.Default.Lan, "Network", false, onClick = onNetworkClick)
            NavItem(Icons.Default.Widgets, "Apps", false, onClick = onAppsClick)
            NavItem(Icons.Default.Insights, "Analytics", true, onClick = onAnalyticsClick)
            NavItem(Icons.Default.Settings, "Settings", false, onClick = onSettingsClick)
        }
    }
}

@Composable
private fun NavItem(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
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
                Icon(imageVector = icon, contentDescription = label, tint = PrimaryContainer, modifier = Modifier.size(22.dp))
                Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryContainer)
            }
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clickable { onClick() }
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = TextMuted, modifier = Modifier.size(22.dp))
            Text(text = label, fontSize = 10.sp, color = TextMuted)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsPreview() {
    AnalyticsScreen()
}
