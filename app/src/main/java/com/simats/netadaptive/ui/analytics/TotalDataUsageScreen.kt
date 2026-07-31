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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.simats.netadaptive.data.repository.DataUsageRepository
import com.simats.netadaptive.ui.onboarding.*
import com.simats.netadaptive.viewmodel.analytics.AnalyticsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TotalDataUsageScreen(
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onNetworkClick: () -> Unit = {},
    onAppsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val monthlyUsage by viewModel.monthlyUsage.collectAsState()
    val dailyAnalytics by viewModel.weeklyAnalytics.collectAsState()

    val currentMonthTotal = (monthlyUsage?.totalWiFiBytes ?: 0L) + (monthlyUsage?.totalMobileBytes ?: 0L)
    val totalSaved = dailyAnalytics.sumOf { it.totalDataSavedBytes }
    
    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()).uppercase()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total Data Usage",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ocean
                        )
                        Text(
                            text = "All apps combined",
                            fontSize = 11.sp,
                            color = OnSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color(0xFFE8EAED), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Ocean
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        bottomBar = {
            TotalDataUsageBottomNav(
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
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            HeroUsageCard(
                monthLabel = monthName,
                totalBytes = currentMonthTotal,
                wifiBytes = monthlyUsage?.totalWiFiBytes ?: 0L,
                mobileBytes = monthlyUsage?.totalMobileBytes ?: 0L,
                savedBytes = totalSaved,
                lastMonthBytes = monthlyUsage?.lastMonthTotalBytes ?: 0L
            )

            monthlyUsage?.let { usage ->
                DailyUsageChart(usage.dailyUsage)
                MonthlyTrendCard(usage)
                WeekByWeekBreakdown(usage.dailyUsage)
                PeakUsageDays(usage.dailyUsage)
            }

            ComparisonStrip(
                currentTotal = currentTotalDisplay(currentMonthTotal),
                lastTotal = currentTotalDisplay(monthlyUsage?.lastMonthTotalBytes ?: 0L)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun currentTotalDisplay(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return "%.1f GB".format(gb)
}

@Composable
private fun HeroUsageCard(
    monthLabel: String,
    totalBytes: Long,
    wifiBytes: Long,
    mobileBytes: Long,
    savedBytes: Long,
    lastMonthBytes: Long
) {
    val totalGb = totalBytes / (1024.0 * 1024.0 * 1024.0)
    val wifiGb = wifiBytes / (1024.0 * 1024.0 * 1024.0)
    val mobileGb = mobileBytes / (1024.0 * 1024.0 * 1024.0)
    val savedGb = savedBytes / (1024.0 * 1024.0 * 1024.0)

    val diffPercent = if (lastMonthBytes > 0) {
        ((totalBytes - lastMonthBytes).toFloat() / lastMonthBytes * 100).toInt()
    } else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "$monthLabel · TOTAL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "%.1f".format(totalGb),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ocean
                        )
                        Text(
                            text = "GB",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ocean,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
                if (diffPercent != 0) {
                    Surface(
                        color = if (diffPercent > 0) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
                        shape = CircleShape
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (diffPercent > 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (diffPercent > 0) ErrorRed else SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${Math.abs(diffPercent)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (diffPercent > 0) ErrorRed else SuccessGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val wifiProgress = if (totalBytes > 0) wifiBytes.toFloat() / totalBytes else 0f
                val mobileProgress = if (totalBytes > 0) mobileBytes.toFloat() / totalBytes else 0f

                UsageProgressItem(label = "WiFi", value = "%.1f GB".format(wifiGb), progress = wifiProgress, color = Azure)
                UsageProgressItem(label = "Mobile", value = "%.1f GB".format(mobileGb), progress = mobileProgress, color = PrimaryContainer)

                Surface(
                    color = Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Data Saved",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SuccessGreen
                            )
                        }
                        Text(
                            text = "%.1f GB".format(savedGb),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageProgressItem(label: String, value: String, progress: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                Text(text = label, fontSize = 12.sp, color = OnSurfaceVariant)
            }
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = OnSurface)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(SurfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(color, CircleShape)
            )
        }
    }
}

@Composable
private fun DailyUsageChart(dailyUsage: List<DataUsageRepository.DailyUsagePoint>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DAILY USAGE — ${SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date()).uppercase()}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariant,
                    letterSpacing = 1.sp
                )

                val avgUsage = if (dailyUsage.isNotEmpty()) dailyUsage.sumOf { it.wifiBytes + it.mobileBytes } / dailyUsage.size else 0L
                Text(
                    text = "avg %.1f GB/day".format(avgUsage / (1024.0 * 1024.0 * 1024.0)),
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .drawBehind {
                        val gridLineCount = 4
                        val step = size.height / gridLineCount
                        for (i in 0..gridLineCount) {
                            drawLine(
                                color = Color(0xFFE2E8F0),
                                start = Offset(0f, i * step),
                                end = Offset(size.width, i * step),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val maxBytes = dailyUsage.maxOfOrNull { it.wifiBytes + it.mobileBytes }?.coerceAtLeast(1L) ?: 1L
                    val recentDays = dailyUsage.takeLast(7)

                    recentDays.forEach { point ->
                        val wifiHeight = point.wifiBytes.toFloat() / maxBytes
                        val mobileHeight = point.mobileBytes.toFloat() / maxBytes

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(modifier = Modifier.width(12.dp).fillMaxHeight(wifiHeight).background(Azure, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)))
                            Box(modifier = Modifier.width(12.dp).fillMaxHeight(mobileHeight).background(PrimaryContainer, RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp)))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = point.dayOfMonth.toString(),
                                fontSize = 10.sp,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyTrendCard(usage: DataUsageRepository.MonthlyUsage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "MONTHLY TREND",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariant,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                TrendColumn("Last", usage.lastMonthTotalBytes)
                TrendColumn("Curr", usage.totalWiFiBytes + usage.totalMobileBytes, isCurrent = true)
            }
        }
    }
}

@Composable
private fun TrendColumn(label: String, bytes: Long, isCurrent: Boolean = false) {
    val height = (bytes / (1024.0 * 1024.0 * 1024.0) * 10).coerceAtMost(120.0).dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(if (isCurrent) Ocean else Azure.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 11.sp, color = if (isCurrent) Ocean else TextMuted)
    }
}

@Composable
private fun WeekByWeekBreakdown(dailyUsage: List<DataUsageRepository.DailyUsagePoint>) {
    val weeks = dailyUsage.chunked(7)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Week-by-week Breakdown",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                weeks.takeLast(3).forEachIndexed { index, points ->
                    val wifi = points.sumOf { it.wifiBytes }
                    val mobile = points.sumOf { it.mobileBytes }
                    val total = wifi + mobile

                    WeekItem(
                        name = "Week ${weeks.size - index}",
                        value = "%.1f GB".format(total / (1024.0 * 1024.0 * 1024.0)),
                        change = if (index > 0) "Dynamic" else "Current",
                        changeColor = Ocean,
                        wifiWeight = if (total > 0) wifi.toFloat() / total else 0.5f,
                        mobileWeight = if (total > 0) mobile.toFloat() / total else 0.5f
                    )
                    if (index < 2 && index < weeks.size - 1) {
                        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekItem(name: String, value: String, change: String, changeColor: Color, wifiWeight: Float, mobileWeight: Float) {
    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = name, fontSize = 14.sp, color = OnSurface, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ocean)
                Text(text = change, fontSize = 11.sp, color = changeColor, fontWeight = if (change == "Partial") FontWeight.Normal else FontWeight.Bold)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(SurfaceContainerLow)
        ) {
            Box(modifier = Modifier.fillMaxHeight().weight(wifiWeight).background(Azure))
            Box(modifier = Modifier.fillMaxHeight().weight(mobileWeight).background(PrimaryContainer))
        }
    }
}

@Composable
private fun PeakUsageDays(dailyUsage: List<DataUsageRepository.DailyUsagePoint>) {
    val sortedDays = dailyUsage.sortedByDescending { it.wifiBytes + it.mobileBytes }.take(3)
    val colors = listOf(PrimaryContainer, Color(0xFFFEB913), Color(0xFFA8DBDE))

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Peak Usage Days",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceVariant
        )
        
        sortedDays.forEachIndexed { index, point ->
            val total = point.wifiBytes + point.mobileBytes
            PeakDayItem(
                index = "0${index + 1}",
                date = SimpleDateFormat("MMM dd", Locale.getDefault()).format(point.date),
                value = "%.1f GB".format(total / (1024.0 * 1024.0 * 1024.0)),
                color = colors.getOrElse(index) { Azure },
                showLeftBorder = index > 0
            )
        }
    }
}

@Composable
private fun PeakDayItem(index: String, date: String, value: String, color: Color, showLeftBorder: Boolean = false) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (showLeftBorder) Modifier.border(BorderStroke(4.dp, color), RoundedCornerShape(12.dp)) else Modifier),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    color = color.copy(alpha = 0.1f),
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = index, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (color == Color(0xFFA8DBDE)) Ocean else color)
                    }
                }
                Text(text = date, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = OnSurface)
            }
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (color == Color(0xFFA8DBDE)) Ocean else color)
        }
    }
}

@Composable
private fun ComparisonStrip(currentTotal: String, lastTotal: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ComparisonCard(Modifier.weight(1f), "This Month", currentTotal)
            ComparisonCard(Modifier.weight(1f), "Last Month", lastTotal)
        }
    }
}

@Composable
private fun ComparisonCard(modifier: Modifier, label: String, value: String, valueColor: Color = Ocean) {
    Surface(
        modifier = modifier,
        color = SurfaceContainerLow,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = valueColor, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun TotalDataUsageBottomNav(
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
            TotalNavItem(Icons.Default.Home, "Home", false, onClick = onHomeClick)
            TotalNavItem(Icons.Default.Lan, "Network", false, onClick = onNetworkClick)
            TotalNavItem(Icons.Default.Widgets, "Apps", false, onClick = onAppsClick)
            TotalNavItem(Icons.Default.Analytics, "Analytics", true, onClick = onAnalyticsClick)
            TotalNavItem(Icons.Default.Settings, "Settings", false, onClick = onSettingsClick)
        }
    }
}

@Composable
private fun TotalNavItem(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
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
fun TotalDataUsagePreview() {
    TotalDataUsageScreen()
}
