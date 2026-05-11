package com.simats.netadaptive.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun VPNPermissionScreen(
    onBackClick: () -> Unit = {},
    onEnableClick: () -> Unit = {},
    onWhyNeedClick: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp) // Space for fixed bottom CTA
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = OnSurface
                    )
                }
                Text(
                    text = "Step 3 of 5",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.width(48.dp)) // Equalizer
            }

            // Progress Bar
            LinearProgressIndicator(
                progress = { 0.6f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(4.dp)
                    .clip(CircleShape),
                color = PrimaryContainer,
                trackColor = Azure.copy(alpha = 0.2f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Illustration
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Dashed Ring
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                drawCircle(
                                    color = PrimaryContainer.copy(alpha = 0.25f),
                                    style = Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    )
                                )
                            }
                    )

                    // Center Shield
                    Surface(
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(elevation = 8.dp, shape = CircleShape),
                        color = Azure,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = PrimaryContainer.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Floating Lock Badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = (-4).dp)
                            .size(32.dp)
                            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), CircleShape),
                        color = SurfaceContainerLowest,
                        shape = CircleShape,
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = PrimaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Header
                Text(
                    text = "Enable VPN protection",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ocean,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "NetAdaptive uses a local VPN to monitor network traffic and block threats directly on your device.",
                    fontSize = 16.sp,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Explainer Cards
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExplainerCard(
                        icon = Icons.Default.Lan,
                        iconColor = Azure,
                        bgColor = Azure.copy(alpha = 0.1f),
                        title = "Intercepts app traffic locally",
                        description = "Traffic is routed through our local filter before reaching the internet."
                    )
                    ExplainerCard(
                        icon = Icons.Default.VisibilityOff,
                        iconColor = PrimaryContainer,
                        bgColor = PrimaryContainer.copy(alpha = 0.1f),
                        title = "Your data stays on your device",
                        description = "We do not send your browsing history or traffic to our servers."
                    )
                    ExplainerCard(
                        icon = Icons.Default.VerifiedUser,
                        iconColor = Emerald,
                        bgColor = Emerald.copy(alpha = 0.1f),
                        title = "Encrypted content is never inspected",
                        description = "We only analyze connection destinations, not the content of encrypted packets."
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Note Strip
                Surface(
                    color = TertiaryFixed.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TertiaryFixed)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = SecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "You will see a standard system prompt asking for permission to set up a VPN connection. Please tap 'OK' to continue.",
                            fontSize = 11.sp,
                            color = OnSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Bottom CTA Section
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = SurfaceContainerLowest.copy(alpha = 0.9f),
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Enable VPN & continue",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                TextButton(onClick = onWhyNeedClick) {
                    Text(
                        text = "Why do you need this?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Azure
                    )
                }

                Text(
                    text = "You can disable this later in settings.",
                    fontSize = 11.sp,
                    color = OnSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        // System Dialog Overlay Simulation
        if (showDialog) {
            Dialog(
                onDismissRequest = { showDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Ocean.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .width(320.dp)
                            .padding(24.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = SurfaceContainerLowest,
                        shadowElevation = 24.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(32.dp),
                                    color = Azure,
                                    shape = CircleShape
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.VpnKey,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Connection request",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = OnSurface
                                )
                            }

                            Text(
                                text = "NetAdaptive wants to set up a VPN connection that allows it to monitor network traffic. Only accept if you trust the source.",
                                fontSize = 16.sp,
                                color = OnSurfaceVariant,
                                lineHeight = 24.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = OnSurfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "A key icon appears at the top of your screen when VPN is active.",
                                    fontSize = 11.sp,
                                    color = OnSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { showDialog = false }) {
                                    Text("Cancel", color = Primary, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = { 
                                    showDialog = false
                                    onEnableClick()
                                }) {
                                    Text("OK", color = Primary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExplainerCard(
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    title: String,
    description: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceContainerLowest,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.2f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = bgColor,
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = OnSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VPNPermissionPreview() {
    VPNPermissionScreen()
}
