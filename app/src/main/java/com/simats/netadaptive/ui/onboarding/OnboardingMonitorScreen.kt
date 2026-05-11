package com.simats.netadaptive.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color constants moved to OnboardingColors.kt
private val Aqua = Color(0xFFA8DBDE)
private val Persimmon = Color(0xFFFE9237)
private val Gold = Color(0xFFFEB913)
private val MonitorBackground = Color(0xFFF4F5F7)
private val SurfaceLowest = Color(0xFFFFFFFF)
private val OnSurfaceVariantMonitor = Color(0xFF554337)

@Composable
fun OnboardingMonitorScreen(
    onGetStartedClick: () -> Unit = {},
    onSkipClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MonitorBackground)
    ) {
        // Subtle Background Decorative Elements
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = 100.dp)
                .blur(80.dp)
                .background(Azure.copy(alpha = 0.05f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp, y = (-200).dp)
                .blur(60.dp)
                .background(Persimmon.copy(alpha = 0.05f), CircleShape)
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 24.dp, end = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NetAdaptive",
                    color = Ocean,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                TextButton(onClick = onSkipClick) {
                    Text(
                        text = "Skip",
                        color = OnSurfaceVariantMonitor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Illustration Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Background Circle
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(elevation = 2.dp, shape = CircleShape)
                            .background(Color(0xFFEAF6F6), CircleShape)
                    )

                    // Floating Stats Grid
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .offset(x = 8.dp, y = (-8).dp),
                                icon = Icons.Default.Speed,
                                iconColor = Persimmon,
                                value = "24ms",
                                label = "Latency"
                            )
                            StatCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .offset(x = (-8).dp, y = 16.dp),
                                icon = Icons.Default.CheckCircle,
                                iconColor = Emerald,
                                value = "0.2%",
                                label = "Loss"
                            )
                        }
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .offset(x = 16.dp, y = (-16).dp),
                                icon = Icons.Default.NetworkCheck,
                                iconColor = Azure,
                                value = "94",
                                label = "Quality"
                            )
                            StatCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .offset(x = (-16).dp, y = 8.dp),
                                icon = Icons.Default.DataUsage,
                                iconColor = Gold,
                                value = "1.4GB",
                                label = "Saved"
                            )
                        }
                    }

                    // Status Pill
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 16.dp),
                        shape = CircleShape,
                        color = SurfaceLowest,
                        tonalElevation = 4.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Aqua.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Emerald, CircleShape)
                                    .shadow(elevation = 8.dp, shape = CircleShape, ambientColor = Emerald, spotColor = Emerald)
                            )
                            Text(
                                text = "Network stable",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Ocean
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Text Content
                Text(
                    text = "MONITOR",
                    color = Azure,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Monitor everything from one dashboard",
                    color = Ocean,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Get a comprehensive view of your network's health and performance across all your devices.",
                    color = OnSurfaceVariantMonitor,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Bottom Actions
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceLowest.copy(alpha = 0.5f),
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Aqua.copy(alpha = 0.4f)))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Aqua.copy(alpha = 0.4f)))
                        Box(
                            modifier = Modifier
                                .size(width = 24.dp, height = 8.dp)
                                .clip(CircleShape)
                                .background(Persimmon)
                                .shadow(elevation = 4.dp, shape = CircleShape, spotColor = Persimmon)
                        )
                    }

                    // Primary Action
                    Button(
                        onClick = onGetStartedClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp), spotColor = Ocean.copy(alpha = 0.25f)),
                        colors = ButtonDefaults.buttonColors(containerColor = Ocean),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Get started",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Persimmon,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    value: String,
    label: String
) {
    Surface(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceLowest,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Aqua.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = iconColor
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = OnSurfaceVariantMonitor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingMonitorPreview() {
    OnboardingMonitorScreen()
}
