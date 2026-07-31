package com.simats.netadaptive.ui.analytics

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.simats.netadaptive.data.model.AppUsageData
import com.simats.netadaptive.data.model.PriorityLevel
import com.simats.netadaptive.ui.onboarding.*
import com.simats.netadaptive.viewmodel.analytics.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerAppDataReportScreen(
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onNetworkClick: () -> Unit = {},
    onAppsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val todaysUsage by viewModel.todaysUsage.collectAsState()
    val appsUsage by viewModel.appsUsage.collectAsState()

    var selectedFilter by remember { mutableStateOf("All Apps") }
    var showAllApps by remember { mutableStateOf(false) }

    val filteredApps = remember(appsUsage, selectedFilter) {
        when (selectedFilter) {
            "Restricted" -> appsUsage.filter { it.isThrottled || it.isDelayed }
            "Whitelisted" -> appsUsage.filter { it.priority == PriorityLevel.HIGH }
            else -> appsUsage
        }
    }

    Scaffold(
        containerColor = Color(0xFFF4F5F7),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Per-App Data Report",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ocean
                        )
                        Text(
                            text = "Top data consumers",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
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
                actions = {
                    IconButton(onClick = { /* Export Logic */ }) {
                        Icon(imageVector = Icons.Default.FileDownload, tint = Ocean, contentDescription = "Export")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            PerAppBottomNav(
                onHomeClick = onHomeClick,
                onNetworkClick = onNetworkClick,
                onAppsClick = onAppsClick,
                onAnalyticsClick = {},
                onSettingsClick = onSettingsClick
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            item { HeroUsageSection(todaysUsage, appsUsage) }
            
            item {
                FilterBar(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )
            }
            
            item {
                TopAppsListSection(
                    apps = filteredApps,
                    totalUsage = todaysUsage,
                    showAll = showAllApps,
                    onToggleShowAll = { showAllApps = !showAllApps }
                )
            }
            
            item { ComparisonSection(appsUsage) }
        }
    }
}

@Composable
private fun HeroUsageSection(todaysTotal: Long, appsUsage: List<AppUsageData>) {
    val totalGb = todaysTotal / (1024.0 * 1024.0 * 1024.0)
    val top5 = appsUsage.take(5)
    val proportions = if (todaysTotal > 0) {
        top5.map { it.usageBytes.toFloat() / todaysTotal }
    } else emptyList()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TODAY'S USAGE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariant,
                letterSpacing = 1.5.sp
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "%.1f".format(totalGb), fontSize = 42.sp, fontWeight = FontWeight.Black, color = Primary)
                Text(text = "GB", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Primary, modifier = Modifier.padding(bottom = 8.dp))
            }
            Text(text = "Total used by all apps", fontSize = 12.sp, color = TextMuted)

            Spacer(modifier = Modifier.height(32.dp))

            val chartColors = listOf(Primary, Azure, SuccessGreen, NotificationGold, ErrorRed, Color(0xFFEDEEF0))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                DonutChart(
                    modifier = Modifier.fillMaxSize(),
                    proportions = proportions + listOf(maxOf(0f, 1f - proportions.sum())),
                    colors = chartColors
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "TOP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = "${top5.size}", fontSize = 28.sp, fontWeight = FontWeight.Black, color = OnSurface)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                top5.chunked(2).forEach { pair ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        pair.forEach { app ->
                            val percent = if (todaysTotal > 0) (app.usageBytes.toFloat() / todaysTotal * 100).toInt() else 0
                            val colorIdx = appsUsage.indexOf(app)
                            LegendItem(
                                modifier = Modifier.weight(1f),
                                label = app.name,
                                percent = "$percent%",
                                color = chartColors.getOrElse(colorIdx) { Color.Gray }
                            )
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(modifier: Modifier, proportions: List<Float>, colors: List<Color>) {
    Canvas(modifier = modifier) {
        var startAngle = -90f
        proportions.forEachIndexed { index, proportion ->
            val sweepAngle = proportion * 360f
            if (sweepAngle > 0.5f) {
                drawArc(
                    color = colors.getOrElse(index) { Color.Gray },
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            startAngle += sweepAngle
        }
    }
}

@Composable
private fun LegendItem(modifier: Modifier = Modifier, label: String, percent: String, color: Color) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Text(
            text = label,
            fontSize = 12.sp,
            color = OnSurfaceVariant,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(text = percent, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurface)
    }
}

@Composable
private fun FilterBar(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val filters = listOf("All Apps", "Restricted", "Whitelisted")

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(filters) { filter ->
            FilterChip(filter, filter == selectedFilter) { onFilterSelected(filter) }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) Primary.copy(alpha = 0.1f) else Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (selected) Primary else OutlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Primary else OnSurfaceVariant
        )
    }
}

@Composable
private fun TopAppsListSection(
    apps: List<AppUsageData>,
    totalUsage: Long,
    showAll: Boolean,
    onToggleShowAll: () -> Unit
) {
    val displayApps = if (showAll) apps else apps.take(5)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.1f))
    ) {
        Column {
            if (displayApps.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No apps found", color = TextMuted)
                }
            } else {
                displayApps.forEachIndexed { index, app ->
                    val progress = if (totalUsage > 0) app.usageBytes.toFloat() / totalUsage else 0f
                    AppUsageRow(
                        rank = "%02d".format(index + 1),
                        name = app.name,
                        category = app.category,
                        amount = app.usageDisplay,
                        progress = progress,
                        percentOfTotal = "${(progress * 100).toInt()}% of total"
                    )
                    if (index < displayApps.size - 1) {
                        HorizontalDivider(color = Color(0xFFF1F3F4), modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
                
                if (apps.size > 5) {
                    TextButton(
                        onClick = onToggleShowAll,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = Primary)
                    ) {
                        Text(
                            text = if (showAll) "Show Less" else "Show all ${apps.size} apps",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppUsageRow(
    rank: String,
    name: String,
    category: String,
    amount: String,
    progress: Float,
    percentOfTotal: String
) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = rank,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Center
        )

        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Azure.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Text(name.take(1), fontWeight = FontWeight.Bold, color = Azure, fontSize = 18.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = category, fontSize = 11.sp, color = TextMuted)
        }

        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = amount, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Box(modifier = Modifier.width(70.dp).height(4.dp).clip(CircleShape).background(Color(0xFFF1F3F4))) {
                Box(modifier = Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().background(Primary, CircleShape))
            }
            Text(text = percentOfTotal, fontSize = 10.sp, color = TextMuted)
        }
    }
}

@Composable
private fun ComparisonSection(appsUsage: List<AppUsageData>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ForegroundBackgroundCard(appsUsage)
        WifiMobileComparisonCard(appsUsage)
    }
}

@Composable
private fun ForegroundBackgroundCard(appsUsage: List<AppUsageData>) {
    val totalFg = appsUsage.sumOf { it.fgUsageBytes }
    val totalBg = appsUsage.sumOf { it.bgUsageBytes }
    val total = totalFg + totalBg
    val fgProgress = if (total > 0) totalFg.toFloat() / total else 0.5f
    val bgProgress = 1f - fgProgress

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = "Active vs Passive", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Text(text = "Foreground vs Background usage", fontSize = 12.sp, color = TextMuted)
            
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                UsageProgressColumn(modifier = Modifier.weight(1f), label = "FOREGROUND", value = formatBytes(totalFg), progress = fgProgress, color = Primary)
                UsageProgressColumn(modifier = Modifier.weight(1f), label = "BACKGROUND", value = formatBytes(totalBg), progress = bgProgress, color = Azure)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "TOP BACKGROUND CONSUMERS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(12.dp))

            appsUsage.filter { it.bgUsageBytes > 0 }.sortedByDescending { it.bgUsageBytes }.take(3).forEach { app ->
                BackgroundConsumerRow(app.name, formatBytes(app.bgUsageBytes))
            }
        }
    }
}

@Composable
private fun WifiMobileComparisonCard(appsUsage: List<AppUsageData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Network Split", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                    Text(text = "WiFi vs Mobile Distribution", fontSize = 12.sp, color = TextMuted)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegendIndicator(Azure, "WiFi")
                    LegendIndicator(PrimaryContainer, "Mobile")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            appsUsage.filter { it.usageBytes > 0 }.take(5).forEach { app ->
                val total = app.wifiUsageBytes + app.mobileUsageBytes
                val wifiP = if (total > 0) app.wifiUsageBytes.toFloat() / total else 0.5f
                val wifiPercent = (wifiP * 100).toInt()
                WifiMobileAppRow(app.name, "$wifiPercent% WiFi", wifiP)
            }
        }
    }
}

@Composable
private fun LegendIndicator(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Text(text = label, fontSize = 11.sp, color = OnSurfaceVariant)
    }
}

@Composable
private fun WifiMobileAppRow(name: String, label: String, wifiProgress: Float) {
    Column(modifier = Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = OnSurface)
            Text(text = label, fontSize = 11.sp, color = TextMuted)
        }
        Row(modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(PrimaryContainer.copy(alpha = 0.2f))) {
            Box(modifier = Modifier.fillMaxWidth(wifiProgress).fillMaxHeight().background(Azure))
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return "%.1f %s".format(bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

@Composable
private fun UsageProgressColumn(modifier: Modifier, label: String, value: String, progress: Float, color: Color) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
        Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Color(0xFFF1F3F4))) {
            Box(modifier = Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().background(color, CircleShape))
        }
    }
}

@Composable
private fun BackgroundConsumerRow(name: String, amount: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = name, fontSize = 13.sp, color = OnSurfaceVariant)
        Text(text = amount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OnSurface)
    }
}

@Composable
private fun PerAppBottomNav(
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .then(if (active) Modifier.offset(y = (-4).dp) else Modifier)
    ) {
        if (active) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(PrimaryContainer)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) PrimaryContainer else OnSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = if (active) PrimaryContainer else OnSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PerAppDataReportPreview() {
    PerAppDataReportScreen()
}
