package com.simats.netadaptive.ui.apps

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.simats.netadaptive.ui.onboarding.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriorityRankingScreen(
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onNetworkClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: PriorityRankingViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val tieredApps by viewModel.tieredApps.collectAsState()
    val isAutoMode by viewModel.isAutoMode.collectAsState()
    val availableApps by viewModel.availableApps.collectAsState()
    
    var showAppPickerForTier by remember { mutableStateOf<String?>(null) }

    if (showAppPickerForTier != null) {
        AppPickerDialog(
            apps = availableApps,
            onDismiss = { showAppPickerForTier = null },
            onAppSelected = { app ->
                viewModel.addAppToTier(app, showAppPickerForTier!!)
                showAppPickerForTier = null
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF4F5F7),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Priority Ranking",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Ocean
                        )
                        Text(
                            text = if (isAutoMode) "AI dynamically optimizing" else "Manual override active",
                            fontSize = 11.sp,
                            color = if (isAutoMode) Color(0xFFB0B8C8) else Primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, SurfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Ocean)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.setAutoMode(!isAutoMode) },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(
                                if (isAutoMode) Color(0xFFF0F9FF) else Color(0xFFF8FAFC), 
                                RoundedCornerShape(20.dp)
                            )
                            .border(
                                1.dp, 
                                if (isAutoMode) Color(0xFF0EA5E9) else Color(0xFFE2E8F0), 
                                RoundedCornerShape(20.dp)
                            ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (isAutoMode) Color(0xFF0EA5E9) else Color(0xFF64748B), 
                                        CircleShape
                                    )
                            )
                            Text(
                                text = if (isAutoMode) "Auto" else "Manual",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAutoMode) Color(0xFF0369A1) else Color(0xFF475569)
                            )
                        }
                    }

                    Button(
                        onClick = { 
                            Log.e("PriorityRanking", "Save button clicked")
                            viewModel.saveChanges() 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text("Save", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ExplainerBanner()

            TierSection(
                tierNumber = 1,
                title = "Critical",
                tierKey = "TIER1",
                statusLabel = "Always protected",
                description = "Full bandwidth guaranteed · Never throttled or blocked",
                borderColor = PrimaryContainer,
                apps = tieredApps["TIER1"] ?: emptyList(),
                onAddApp = { showAppPickerForTier = "TIER1" }
            )

            TierSection(
                tierNumber = 2,
                title = "High",
                tierKey = "TIER2",
                statusLabel = "Prioritized",
                description = "Gets bandwidth after Critical · Throttled only in severe degradation",
                borderColor = SecondaryContainer,
                apps = tieredApps["TIER2"] ?: emptyList(),
                onAddApp = { showAppPickerForTier = "TIER2" }
            )

            TierSection(
                tierNumber = 3,
                title = "Normal",
                tierKey = "TIER3",
                statusLabel = "Standard",
                description = "Default bandwidth allocation · Throttled during moderate degradation",
                borderColor = Color(0xFF60A5FA),
                apps = tieredApps["TIER3"] ?: emptyList(),
                onAddApp = { showAppPickerForTier = "TIER3" }
            )

            TierSection(
                tierNumber = 4,
                title = "Low",
                tierKey = "TIER4",
                statusLabel = "Background only",
                description = "Blocked first during any degradation · Background sync only",
                borderColor = OnSurfaceVariant.copy(alpha = 0.4f),
                apps = tieredApps["TIER4"] ?: emptyList(),
                onAddApp = { showAppPickerForTier = "TIER4" }
            )

            ResetStrip()
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AppPickerDialog(
    apps: List<String>,
    onDismiss: () -> Unit,
    onAppSelected: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select App",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyColumn {
                    items(apps) { app ->
                        TextButton(
                            onClick = { onAppSelected(app) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = app, color = Ocean)
                        }
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                    }
                }
            }
        }
    }
}

@Composable
private fun ExplainerBanner() {
    Surface(
        color = Color(0xFFFFF3E8),
        border = BorderStroke(1.dp, Primary.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Primary)
            Column {
                Text(
                    text = "How priority works",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnPrimaryContainer
                )
                Text(
                    text = "Our AI engine allocates bandwidth dynamically based on your tier ranking. High priority apps get dedicated lanes during congestion.",
                    fontSize = 11.sp,
                    color = OnSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun TierSection(
    tierNumber: Int,
    title: String,
    tierKey: String,
    statusLabel: String,
    description: String,
    borderColor: Color,
    apps: List<PriorityApp>,
    onAddApp: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFF97316), PrimaryContainer)
                                )
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "TIER $tierNumber",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ocean)
            }
            Surface(
                color = when(tierNumber) {
                    1 -> Color(0xFFF0FDF4)
                    2 -> Color(0xFFFFF7ED)
                    else -> Color(0xFFEFF6FF)
                },
                shape = CircleShape,
                border = BorderStroke(1.dp, when(tierNumber) {
                    1 -> Color(0xFFBBF7D0)
                    2 -> SecondaryContainer.copy(alpha = 0.3f)
                    else -> Color(0xFFDBEAFE)
                })
            ) {
                Text(
                    text = statusLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when(tierNumber) {
                        1 -> Color(0xFF15803D)
                        2 -> OnSecondaryContainer
                        else -> Color(0xFF2563EB)
                    }
                )
            }
        }
        Text(text = description, fontSize = 11.sp, color = OnSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = borderColor,
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(0f, size.height),
                            strokeWidth = 4.dp.toPx()
                        )
                    }
            ) {
                apps.forEach { app ->
                    AppPriorityItem(app)
                    HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(start = 4.dp))
                }
                TextButton(
                    onClick = onAddApp,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(text = "Add app to $title", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppPriorityItem(app: PriorityApp) {
    val context = LocalContext.current
    val icon = remember(app.packageName) {
        if (app.packageName.isEmpty()) return@remember null
        try {
            context.packageManager.getApplicationIcon(app.packageName)
        } catch (e: Exception) {
            null
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Outlined.DragIndicator,
            contentDescription = "Drag",
            tint = OnSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
        AsyncImage(
            model = icon,
            contentDescription = app.name,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF3F4F6)),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = app.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Text(text = app.usage, fontSize = 11.sp, color = OnSurfaceVariant)
        }
        Surface(
            color = PrimaryContainer,
            shape = CircleShape,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "${app.rank}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ResetStrip() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    color = ErrorRed.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, tint = ErrorRed)
                    }
                }
                Column {
                    Text(text = "Reset to defaults", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
                    Text(text = "Restore standard priority Tiers", fontSize = 11.sp, color = OnSurfaceVariant)
                }
            }
            OutlinedButton(
                onClick = { },
                shape = CircleShape,
                border = BorderStroke(1.dp, ErrorRed),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text("Reset", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
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
            NavItem(Icons.Default.Dashboard, "Home", false, onClick = onHomeClick)
            NavItem(Icons.Default.SettingsEthernet, "Network", false, onClick = onNetworkClick)
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
            color = SecondaryContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.clickable { onClick() }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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
