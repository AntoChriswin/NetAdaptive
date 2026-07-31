package com.simats.netadaptive.ui.apps

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.simats.netadaptive.data.model.AppUsageData
import com.simats.netadaptive.data.repository.AppUsageRepository
import com.simats.netadaptive.ui.onboarding.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllAppsScreen(
    onHomeClick: () -> Unit = {},
    onNetworkClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onPriorityRankingClick: () -> Unit = {},
    onAppClick: (AppUsageData) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val apps by AppUsageRepository.appsUsage.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    
    val hasPermission = remember { mutableStateOf(AppUsageRepository.hasUsageStatsPermission(context)) }

    val filteredApps = apps.filter { app ->
        val matchesSearch = app.name.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "All" -> true
            "Foreground" -> app.status == "Foreground"
            "Background" -> app.status == "Background"
            "High usage" -> app.usageBytes >= 500 * 1024 * 1024
            else -> true
        }
        matchesSearch && matchesFilter
    }
    
    LaunchedEffect(Unit) {
        while(true) {
            hasPermission.value = AppUsageRepository.hasUsageStatsPermission(context)
            kotlinx.coroutines.delay(2000)
        }
    }
    
    val highUsageApps = filteredApps.filter { it.usageBytes >= 500 * 1024 * 1024 }
    val mediumUsageApps = filteredApps.filter { it.usageBytes in (50 * 1024 * 1024)..<(500 * 1024 * 1024) }
    val lowUsageApps = filteredApps.filter { it.usageBytes < 50 * 1024 * 1024 }
    
    val featuredApp = apps.maxByOrNull { it.currentSpeedBytes } ?: apps.firstOrNull()
    
    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "App Monitoring",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Text(
                            text = "Live session usage",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background.copy(alpha = 0.8f)
                )
            )
        },
        bottomBar = {
            AppsBottomNav(
                onHomeClick = onHomeClick,
                onNetworkClick = onNetworkClick,
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SearchAndFilters(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                selectedFilter = selectedFilter,
                onFilterChange = { selectedFilter = it }
            )
            
            ActiveAppFeaturedCard(featuredApp, onAppClick)
            
            if (!hasPermission.value) {
                PermissionWarningCard(context)
            }
            
            if (apps.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Monitoring app traffic...", color = OnSurfaceVariant)
                    }
                }
            } else if (filteredApps.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No apps match your search", color = OnSurfaceVariant)
                }
            }

            if (highUsageApps.isNotEmpty()) {
                UsageSection(
                    title = "HIGH USAGE",
                    subtitle = "500MB+",
                    apps = highUsageApps,
                    onAppClick = onAppClick
                )
            }

            if (mediumUsageApps.isNotEmpty()) {
                UsageSection(
                    title = "MEDIUM USAGE",
                    subtitle = "50-500MB",
                    apps = mediumUsageApps,
                    onAppClick = onAppClick
                )
            }

            if (lowUsageApps.isNotEmpty()) {
                UsageSection(
                    title = "LOW USAGE",
                    subtitle = "Below 50MB",
                    apps = lowUsageApps,
                    onAppClick = onAppClick
                )
            }

            TeaserStrips(onPriorityRankingClick = onPriorityRankingClick)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SearchAndFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterChange: (String) -> Unit
) {
    val filters = listOf("All", "Foreground", "Background", "High usage")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(12.dp)),
            placeholder = { Text("Search apps...", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = OnSurfaceVariant) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceContainerLow,
                unfocusedContainerColor = SurfaceContainerLow,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filters) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceContainerLow,
                        labelColor = OnSurfaceVariant
                    ),
                    border = null,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
private fun PermissionWarningCard(context: android.content.Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
        border = BorderStroke(1.dp, Color(0xFFFED7AA)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEA580C))
                Text(text = "Usage Access Required", fontWeight = FontWeight.Bold, color = Color(0xFF9A3412))
            }
            Text(
                text = "To see data usage for other apps, please enable 'Usage Access' in system settings.",
                fontSize = 12.sp,
                color = Color(0xFF9A3412)
            )
            Button(
                onClick = {
                    val intent = android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Enable in Settings", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ActiveAppFeaturedCard(app: AppUsageData?, onAppClick: (AppUsageData) -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth().clickable { app?.let { onAppClick(it) } },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box {
                        if (app != null) {
                            val icon = remember(app.packageName) {
                                try { context.packageManager.getApplicationIcon(app.packageName) } catch (e: Exception) { null }
                            }
                            AsyncImage(
                                model = icon,
                                contentDescription = app.name,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(modifier = Modifier.size(56.dp).background(SurfaceContainerLow, RoundedCornerShape(12.dp)))
                        }
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 8.dp, y = (-8).dp),
                            color = Primary,
                            shape = CircleShape,
                            border = BorderStroke(2.dp, Color.White)
                        ) {
                            Text(
                                text = "P${app?.priority ?: 1}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(text = app?.name ?: "No Active App", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val isActive = (app?.currentSpeedBytes ?: 0L) > 0
                            Box(modifier = Modifier.size(8.dp).background(if (isActive) Color(0xFF4ADE80) else Color.Gray, CircleShape))
                            Text(
                                text = if (isActive) "ACTIVE NOW" else "IDLE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) Color(0xFF16A34A) else Color.Gray,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = app?.currentSpeed ?: "0 B/s", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Primary)
                    Text(text = "Current speed", fontSize = 12.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(text = "Bandwidth Breakdown", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceVariant)
                    Text(text = "85% Priority", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerLow)
                ) {
                    Box(modifier = Modifier.weight(0.85f).fillMaxHeight().background(Primary))
                    Box(modifier = Modifier.weight(0.15f).fillMaxHeight().background(SecondaryContainer.copy(alpha = 0.5f)))
                }
            }
        }
    }
}

@Composable
private fun UsageSection(title: String, subtitle: String, apps: List<AppUsageData>, onAppClick: (AppUsageData) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    val displayedApps = if (isExpanded) apps else apps.take(5)
    val remainingCount = apps.size - 5

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant, letterSpacing = 1.5.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineVariant.copy(alpha = 0.3f))
            Text(text = subtitle, fontSize = 11.sp, color = OnSurfaceVariant)
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            displayedApps.forEach { app ->
                AppUsageRow(app, onAppClick)
            }
        }

        if (remainingCount > 0) {
            Button(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary.copy(alpha = 0.05f),
                    contentColor = Primary
                ),
                elevation = null
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Show less" else "Show $remainingCount more apps",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppUsageRow(app: AppUsageData, onAppClick: (AppUsageData) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAppClick(app) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val icon = remember(app.packageName) {
                try { context.packageManager.getApplicationIcon(app.packageName) } catch (e: Exception) { null }
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = app.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                    Text(text = "-", fontSize = 14.sp, color = OnSurfaceVariant)
                    Text(text = app.usageDisplay, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Primary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val status = app.status
                    status?.let {
                        Surface(
                            color = when(it) {
                                "Foreground" -> PrimaryContainer.copy(alpha = 0.2f)
                                "Background" -> Color(0xFFF3F4F6)
                                else -> if (app.isError) Color(0xFFFFEBEE) else PrimaryContainer.copy(alpha = 0.2f)
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = it.uppercase(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = when(it) {
                                    "Foreground" -> Primary
                                    "Background" -> OnSurfaceVariant
                                    else -> if (app.isError) ErrorRed else Primary
                                }
                            )
                        }
                    }
                    Text(
                        text = "• ${app.category}",
                        fontSize = 10.sp,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
        if (app.currentSpeedBytes > 0) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = app.currentSpeed,
                    fontSize = 12.sp,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "LIVE", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Primary.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun TeaserStrips(onPriorityRankingClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TeaserStrip("Priority ranking active", Icons.Default.Leaderboard, TertiaryContainer, OnSurface, onClick = onPriorityRankingClick)
    }
}

@Composable
private fun TeaserStrip(label: String, icon: ImageVector, bgColor: Color, tint: Color, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = bgColor.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, bgColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    color = bgColor,
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                    }
                }
                Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Primary)
        }
    }
}

@Composable
private fun AppsBottomNav(
    onHomeClick: () -> Unit,
    onNetworkClick: () -> Unit,
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
            NavItem(Icons.Default.Widgets, "Apps", true, onClick = {})
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
fun AllAppsPreview() {
    AllAppsScreen()
}
