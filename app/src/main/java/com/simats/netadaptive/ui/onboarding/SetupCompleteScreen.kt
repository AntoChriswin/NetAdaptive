package com.simats.netadaptive.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SetupCompleteScreen(
    onStartOptimizingClick: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        delay(3000)
        onStartOptimizingClick()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp)
        ) {
            // 1. Progress Indicator (Top)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color(0xFFE2E5EA))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .background(SuccessGreen)
                    )
                }
                Text(
                    text = "All done!",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = SuccessGreen.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp, end = 24.dp)
                )
            }

            // 2. Celebration Illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Glow
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(PrimaryContainer.copy(alpha = 0.15f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )

                // Circles and Shield
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .border(2.dp, SuccessGreen.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Sparkles
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NotificationGold,
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = (-6).dp)
                    )
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NotificationGold,
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.CenterEnd)
                            .offset(x = 6.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NotificationGold,
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.BottomStart)
                            .offset(x = 16.dp, y = (-8).dp)
                    )

                    // Inner Circle
                    Surface(
                        modifier = Modifier.size(92.dp),
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFFFFF3E8), Color(0xFFFFFBF0))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(48.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
                
                // Static Confetti Simulation
                ConfettiPiece(Modifier.align(Alignment.TopStart).offset(x = 60.dp, y = 80.dp), Color(0xFFFFD150), 8.dp)
                ConfettiPiece(Modifier.align(Alignment.TopEnd).offset(x = (-60.dp), y = 60.dp), Color(0xFFFEB913), 12.dp, true)
                ConfettiPiece(Modifier.align(Alignment.CenterStart).offset(x = 40.dp, y = 20.dp), Color(0xFFA8DBDE), 8.dp)
                ConfettiPiece(Modifier.align(Alignment.BottomEnd).offset(x = (-60.dp), y = (-80.dp)), Color(0xFFFE9237), 8.dp)
                ConfettiPiece(Modifier.align(Alignment.BottomStart).offset(x = 80.dp, y = (-60.dp)), Color(0xFFFFD150), 12.dp, true)
            }

            // 3. Header Copy
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "YOU'RE ALL SET",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = SuccessGreen,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "NetAdaptive is ready",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ocean
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Your network is now being monitored. Predictions will begin within the first 60 seconds.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.widthIn(max = 280.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Setup Summary Card
            Surface(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8EAED))
            ) {
                Column {
                    SummaryRow("VPN protection", "Active", SuccessGreen)
                    HorizontalDivider(color = Color(0xFFF0F1F3))
                    SummaryRow("Notifications", "Enabled", SuccessGreen)
                    HorizontalDivider(color = Color(0xFFF0F1F3))
                    SummaryRow("Prediction engine", "Learning", NotificationGold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Quick Stat Preview Strip
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatItem(modifier = Modifier.weight(1f), value = "5–15s", label = "prediction window")
                StatItem(modifier = Modifier.weight(1f), value = "< 100ms", label = "response time")
                StatItem(modifier = Modifier.weight(1f), value = "0 → 100", label = "quality score")
            }

            Spacer(modifier = Modifier.weight(1f))

            // 6. CTA Section (Bottom)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Button(
                    onClick = onStartOptimizingClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(PrimaryContainer, NotificationGold)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start optimizing",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp).offset(y = 2.dp)
                    )
                    Text(
                        text = "Your data is processed entirely on-device. No personal content is ever shared.",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, status: String, statusColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = OnSurface)
        Surface(
            color = statusColor.copy(alpha = 0.1f),
            shape = CircleShape
        ) {
            Text(
                text = status,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun StatItem(modifier: Modifier = Modifier, value: String, label: String) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF3F4F6).copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Primary)
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 10.sp
            )
        }
    }
}

@Composable
private fun ConfettiPiece(modifier: Modifier, color: Color, size: androidx.compose.ui.unit.Dp, isRectangle: Boolean = false) {
    Box(
        modifier = modifier
            .size(if (isRectangle) size.times(1.5f) else size, if (isRectangle) size.times(0.5f) else size)
            .clip(if (isRectangle) RoundedCornerShape(1.dp) else CircleShape)
            .background(color.copy(alpha = 0.6f))
    )
}

@Preview(showBackground = true)
@Composable
fun SetupCompletePreview() {
    SetupCompleteScreen()
}
