package com.simats.netadaptive.ui.apps

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.simats.netadaptive.data.model.AppUsageData
import com.simats.netadaptive.ui.onboarding.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    app: AppUsageData,
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onNetworkClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color(0xFFF4F5F7),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "App Detail",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ocean
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(38.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color(0xFFE8EAED), CircleShape)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Ocean)
                    }
                },
                actions = {
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(38.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color(0xFFE8EAED), CircleShape)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Ocean)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add to whitelist", color = Ocean, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Move to group", color = Ocean, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Reset app data", color = Ocean, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Report issue", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                            onClick = { showMenu = false }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            AppsBottomNav(
                onHomeClick = onHomeClick,
                onNetworkClick = onNetworkClick,
                onAnalyticsClick = onAnalyticsClick,
                onSettingsClick = onSettingsClick,
                onAppsClick = onBackClick
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
            AppIdentityCard(app)
            LiveBandwidthCard(app)
            UsageStatsCard(app)
            TrafficRuleCard()
            ResetSettingsButton()
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AppIdentityCard(app: AppUsageData) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFDECEA)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = try { context.packageManager.getApplicationIcon(app.packageName) } catch(e: Exception) { null },
                            contentDescription = app.name,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp),
                        color = PrimaryContainer,
                        shape = CircleShape,
                        border = BorderStroke(2.dp, Color.White)
                    ) {
                        Text(
                            text = "${app.priorityScore}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column {
                    Text(text = app.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ocean)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color(0xFFE8EAED))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(label = "STATE", content = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PulseDot()
                        Text(text = app.status ?: "Foreground", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                })
                VerticalDivider(modifier = Modifier.height(32.dp), color = Color(0xFFE8EAED))
                InfoItem(label = "PRIORITY", content = {
                    Text(text = "Critical #${app.priorityScore}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
                })
                VerticalDivider(modifier = Modifier.height(32.dp), color = Color(0xFFE8EAED))
                InfoItem(label = "RULE", content = {
                    Text(text = "Allow", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                })
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun PulseDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .background(SuccessGreen.copy(alpha = alpha), CircleShape)
    )
}

@Composable
private fun LiveBandwidthCard(app: AppUsageData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                    Text(text = app.currentSpeed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Text(text = "Download speed", fontSize = 11.sp, color = OnSurfaceVariant)
            }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Upload, contentDescription = null, tint = TertiaryContainer, modifier = Modifier.size(18.dp))
                    Text(text = "0.3 MB/s", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Text(text = "Upload speed", fontSize = 11.sp, color = OnSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = app.usageDisplay, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ocean)
                Text(text = "Total today", fontSize = 11.sp, color = OnSurfaceVariant)
            }
        }
    }
}

@Composable
private fun UsageStatsCard(app: AppUsageData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Usage stats", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ocean)
                Surface(
                    color = SurfaceContainerLow,
                    shape = CircleShape
                ) {
                    Text(
                        text = "Today",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(text = buildAnnotatedString {
                    append(app.usageDisplay.substringBefore(" "))
                    pushStyle(androidx.compose.ui.text.SpanStyle(fontSize = 14.sp))
                    append(" " + app.usageDisplay.substringAfter(" "))
                    pop()
                }, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Ocean)
                Text(text = "Total data consumed", fontSize = 11.sp, color = OnSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerLow)
            ) {
                Box(modifier = Modifier.weight(0.72f).fillMaxHeight().background(PrimaryContainer))
                Box(modifier = Modifier.weight(0.28f).fillMaxHeight().background(Color(0xFFA8DBDE)))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LegendItem(color = PrimaryContainer, label = "Foreground (72%)")
                LegendItem(color = Color(0xFFA8DBDE), label = "Background (28%)")
            }

            Spacer(modifier = Modifier.height(32.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                NetworkInfoBox(icon = Icons.Default.Wifi, amount = "612 MB", label = "WiFi Network", modifier = Modifier.weight(1f))
                NetworkInfoBox(icon = Icons.Default.SignalCellularAlt, amount = "230 MB", label = "Mobile Data", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))
            WeeklyChart()
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Text(text = label, fontSize = 11.sp, color = OnSurfaceVariant)
    }
}

@Composable
private fun NetworkInfoBox(icon: ImageVector, amount: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = SurfaceContainerLow,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = Ocean, modifier = Modifier.size(16.dp))
                Text(text = amount, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ocean)
            }
            Text(text = label, fontSize = 10.sp, color = OnSurfaceVariant)
        }
    }
}

@Composable
private fun WeeklyChart() {
    val data = listOf(0.4f, 0.6f, 0.3f, 0.8f, 0.45f, 0.9f, 1.0f)
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEachIndexed { index, value ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .fillMaxHeight(value)
                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        .background(if (index == 6) PrimaryContainer else SurfaceContainerLow)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariant,
                    letterSpacing = (-0.5).sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TrafficRuleCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Traffic rule", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ocean)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                    Text(text = "Allow", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RuleButton(icon = Icons.Default.Check, label = "Allow", selected = true, modifier = Modifier.weight(1f))
                    RuleButton(icon = Icons.Default.Speed, label = "Throttle", selected = false, modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RuleButton(icon = Icons.Default.Block, label = "Block", selected = false, modifier = Modifier.weight(1f))
                    RuleButton(icon = Icons.Default.HourglassEmpty, label = "Delay", selected = false, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RuleButton(icon: ImageVector, label: String, selected: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = if (selected) PrimaryContainer.copy(alpha = 0.1f) else Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) PrimaryContainer else Color(0xFFE8EAED))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                tint = if (selected) PrimaryContainer else OnSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) PrimaryContainer else OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun ResetSettingsButton() {
    OutlinedButton(
        onClick = { },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.2f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(text = "Reset app settings", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AppsBottomNav(
    onHomeClick: () -> Unit,
    onNetworkClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAppsClick: () -> Unit
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
            NavItem(Icons.Default.Widgets, "Apps", true, onClick = onAppsClick)
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
fun AppDetailPreview() {
    AppDetailScreen(
        app = AppUsageData(
            packageName = "com.google.android.youtube",
            name = "YouTube",
            usageBytes = 842 * 1024 * 1024,
            usageDisplay = "842 MB",
            status = "Foreground",
            priorityScore = 1
        )
    )
}
