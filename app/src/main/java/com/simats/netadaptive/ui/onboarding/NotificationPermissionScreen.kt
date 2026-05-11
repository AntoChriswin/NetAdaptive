package com.simats.netadaptive.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotificationPermissionScreen(
    onAllowClick: () -> Unit = {},
    onNotNowClick: () -> Unit = {},
    onQuietHoursClick: () -> Unit = {}
) {
    var degradationEnabled by remember { mutableStateOf(true) }
    var optimizationEnabled by remember { mutableStateOf(true) }
    var summariesEnabled by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 160.dp) // Space for fixed buttons
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Step 4 of 5",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Normal
                )
            }

            // Progress Indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(ProgressTrack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .fillMaxHeight()
                        .background(PrimaryContainer)
                )
            }

            // Illustration Zone
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer soft glow circle
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .background(NotificationGold.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner circle
                    Surface(
                        modifier = Modifier.size(108.dp),
                        shape = CircleShape,
                        color = NotificationGoldContainer,
                        shadowElevation = 1.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = NotificationGold,
                                modifier = Modifier.size(48.dp)
                            )
                            // Notification Dot
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-24).dp, y = 20.dp)
                                    .size(12.dp)
                                    .border(2.dp, Background, CircleShape)
                                    .background(PrimaryContainer, CircleShape)
                            )
                        }
                    }
                }

                // Floating Chips
                FloatingChip(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = 100.dp, y = 20.dp),
                    icon = Icons.Default.Error,
                    iconColor = PrimaryContainer,
                    text = "Latency spike detected"
                )

                FloatingChip(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = (-10).dp, y = (-20).dp)
                        .rotate(6f),
                    icon = Icons.Default.Build,
                    iconColor = Azure,
                    text = "Optimizing now..."
                )

                FloatingChip(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(x = 100.dp, y = (-40).dp)
                        .rotate(12f),
                    icon = Icons.Default.CheckCircle,
                    iconColor = SuccessGreen,
                    text = "Network stable",
                    alpha = 0.6f
                )
            }

            // Header Copy
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Stay ahead of drops",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ocean,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Get instant alerts when NetAdaptive detects degradation or takes action — so you're always in control.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            // Notification Type List
            Surface(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8EAED)),
                shadowElevation = 1.dp
            ) {
                Column {
                    NotificationRow(
                        icon = Icons.Default.NetworkWifi,
                        iconColor = PrimaryContainer,
                        bgColor = LightOrange,
                        title = "Degradation alerts",
                        subtitle = "Latency spikes, signal drops",
                        checked = degradationEnabled,
                        onCheckedChange = { degradationEnabled = it }
                    )
                    HorizontalDivider(color = Color(0xFFE8EAED))
                    NotificationRow(
                        icon = Icons.Default.Bolt,
                        iconColor = Azure,
                        bgColor = LightBlue,
                        title = "Optimization actions",
                        subtitle = "When apps are throttled or blocked",
                        checked = optimizationEnabled,
                        onCheckedChange = { optimizationEnabled = it }
                    )
                    HorizontalDivider(color = Color(0xFFE8EAED))
                    NotificationRow(
                        icon = Icons.Default.BarChart,
                        iconColor = SuccessGreen,
                        bgColor = LightGreen,
                        title = "Weekly summaries",
                        subtitle = "Data usage & savings report",
                        checked = summariesEnabled,
                        onCheckedChange = { summariesEnabled = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quiet Hours Teaser
            Surface(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .clickable { onQuietHoursClick() },
                shape = RoundedCornerShape(14.dp),
                color = QuietHoursBlue,
                border = androidx.compose.foundation.BorderStroke(1.dp, Azure.copy(alpha = 0.18f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = Azure,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                                append("Quiet hours")
                            }
                            append(" available in Settings — silence alerts overnight.")
                        },
                        fontSize = 13.sp,
                        color = Ocean,
                        modifier = Modifier.weight(1f),
                        lineHeight = 18.sp
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Azure.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // CTA Section (Fixed Bottom)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Background, Background),
                        startY = 0f,
                        endY = 100f
                    )
                )
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAllowClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NotificationGold),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Ocean,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Allow notifications",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Ocean
                        )
                    }
                }

                OutlinedButton(
                    onClick = onNotNowClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E5EA))
                ) {
                    Text(
                        text = "Not now",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }

                Text(
                    text = "You can change this anytime in Android Settings.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun NotificationRow(
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = bgColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ocean
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryContainer,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE2E5EA),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun FloatingChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    text: String,
    alpha: Float = 1f
) {
    Surface(
        modifier = modifier.shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = alpha)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ocean
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationPermissionPreview() {
    NotificationPermissionScreen()
}
