package com.simats.netadaptive.ui.settings

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.simats.netadaptive.ui.onboarding.*
import com.simats.netadaptive.viewmodel.settings.ProfileUiState
import com.simats.netadaptive.viewmodel.settings.ProfileViewModel
import com.simats.netadaptive.viewmodel.vpn.VpnViewModel
import com.simats.netadaptive.vpn.VpnState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onHomeClick: () -> Unit = {},
    onNetworkClick: () -> Unit = {},
    onAppsClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {},
    onStartVpnClick: () -> Unit = {},
    onStopVpnClick: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel(),
    vpnViewModel: VpnViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val vpnStatus by vpnViewModel.vpnState.collectAsState()
    val scrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Redirect to login if logged out
    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            onLogoutClick()
        }
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Account & Profile",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(40.dp)
                                .background(Color.White, CircleShape)
                                .shadow(elevation = 2.dp, shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = OnSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
                )
            },
            bottomBar = {
                ProfileBottomNav(
                    onHomeClick = onHomeClick,
                    onNetworkClick = onNetworkClick,
                    onAppsClick = onAppsClick,
                    onAnalyticsClick = onAnalyticsClick
                )
            },
            containerColor = Background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ProfileHeroCard(uiState)

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // VPN Control Section
                    VpnControlSection(
                        status = vpnStatus,
                        onStartClick = onStartVpnClick,
                        onStopClick = onStopVpnClick
                    )

                    InfoSection(
                        title = "Personal Info",
                        items = listOf(
                            InfoItem("Display Name", uiState.name),
                            InfoItem("Email", uiState.email, isVerified = true)
                        )
                    )

                    InfoSection(
                        title = "Device Info",
                        items = listOf(
                            InfoItem("Device Name", uiState.deviceName, icon = Icons.Default.Smartphone),
                            InfoItem("Manufacturer", uiState.manufacturer),
                            InfoItem("Model", uiState.model),
                            InfoItem("Android Version", "Android ${uiState.androidVersion}"),
                            InfoItem("API Level", uiState.apiLevel),
                            InfoItem("Device ID", uiState.deviceId, isMono = true),
                            InfoItem("App Version", "v${uiState.appVersion}")
                        )
                    )

                    ActionSection(
                        onLogoutClick = {
                            viewModel.logout()
                            onLogoutClick()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showDeleteDialog) {
        DeleteAccountSheet(
            deviceName = uiState.deviceName,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDeleteAccountClick()
            }
        )
    }
}

@Composable
private fun VpnControlSection(
    status: VpnState,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VpnLock,
                    contentDescription = null,
                    tint = if (status is VpnState.Running) SuccessGreen else OnSurfaceVariant
                )
                Text(
                    text = "VPN Configuration",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            }
            
            val statusText = when(status) {
                is VpnState.Running -> "Connected"
                is VpnState.Connecting -> "Connecting..."
                is VpnState.Stopping -> "Stopping..."
                is VpnState.Error -> "Error: ${status.message}"
                else -> "Disconnected"
            }

            Text(
                text = "Status: $statusText",
                fontSize = 14.sp,
                color = if (status is VpnState.Running) SuccessGreen else ErrorRed,
                fontWeight = FontWeight.SemiBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onStartClick,
                    modifier = Modifier.weight(1f),
                    enabled = status is VpnState.Idle || status is VpnState.Error,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Start VPN")
                }
                Button(
                    onClick = onStopClick,
                    modifier = Modifier.weight(1f),
                    enabled = status is VpnState.Running,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Stop VPN")
                }
            }
        }
    }
}

@Composable
private fun ProfileHeroCard(uiState: ProfileUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .padding(4.dp)
                    .drawBehind {
                        drawCircle(
                            color = PrimaryContainer,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (uiState.photoUrl != null) {
                    AsyncImage(
                        model = uiState.photoUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(PrimaryContainer, SecondaryContainer)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.name.take(2).uppercase(),
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = uiState.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
            Text(
                text = uiState.email,
                fontSize = 16.sp,
                color = OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .drawBehind {
                        drawLine(
                            color = SurfaceVariant,
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatColumn("Active", "Online", Modifier.weight(1f))
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(SurfaceVariant))
                StatColumn("Type", "Dynamic", Modifier.weight(1f))
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(SurfaceVariant))
                StatColumn("Status", "Secure", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceVariant,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
    }
}

@Composable
private fun InfoSection(title: String, items: List<InfoItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f))
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.label,
                                fontSize = 12.sp,
                                color = OnSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = item.value,
                                    fontSize = 16.sp,
                                    color = OnSurface,
                                    fontFamily = if (item.isMono) androidx.compose.ui.text.font.FontFamily.Monospace else androidx.compose.ui.text.font.FontFamily.Default
                                )
                                if (item.isVerified) {
                                    Surface(
                                        color = SuccessGreenContainer.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = null,
                                                tint = SuccessGreen,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "VERIFIED",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SuccessGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (item.icon != null) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = OnSurfaceVariant
                            )
                        }
                    }
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = SurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionSection(onLogoutClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
    ) {
        Column {
            ActionRow(
                icon = Icons.Default.Logout,
                label = "Log Out",
                tint = PrimaryContainer,
                onClick = onLogoutClick
            )
        }
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    subtitle: String? = null,
    tint: Color = OnSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (tint == OnSurface) OnSurfaceVariant else tint
            )
            Column {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = if (tint != OnSurface) FontWeight.SemiBold else FontWeight.Normal,
                    color = tint
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = tint.copy(alpha = 0.6f),
                        lineHeight = 14.sp
                    )
                }
            }
        }
        if (subtitle == null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = OnSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteAccountSheet(deviceName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    color = ErrorRed.copy(alpha = 0.1f),
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ErrorRed
                        )
                    }
                }
                Text(
                    text = "Delete your account?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            }

            Text(
                text = "This action is irreversible. All of your network optimizations, security logs, and hardware configurations for $deviceName will be permanently deleted from our servers.",
                fontSize = 16.sp,
                color = OnSurfaceVariant,
                lineHeight = 22.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Yes, Delete Account", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHigh),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ProfileBottomNav(
    onHomeClick: () -> Unit,
    onNetworkClick: () -> Unit,
    onAppsClick: () -> Unit,
    onAnalyticsClick: () -> Unit
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
            ProfileNavItem(Icons.Default.Home, "Home", false, onClick = onHomeClick)
            ProfileNavItem(Icons.Default.Lan, "Network", false, onClick = onNetworkClick)
            ProfileNavItem(Icons.Default.Widgets, "Apps", false, onClick = onAppsClick)
            ProfileNavItem(Icons.Default.Insights, "Analytics", false, onClick = onAnalyticsClick)
            ProfileNavItem(Icons.Default.Settings, "Settings", true, onClick = {})
        }
    }
}

@Composable
private fun ProfileNavItem(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
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

private data class InfoItem(
    val label: String,
    val value: String,
    val isVerified: Boolean = false,
    val isMono: Boolean = false,
    val icon: ImageVector? = null
)
