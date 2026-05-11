package com.simats.netadaptive.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun ForgetPassSuccessScreen(
    email: String = "admin@network-core.a",
    onOpenEmailApp: () -> Unit = {},
    onBackToLogin: () -> Unit = {},
    onResendClick: () -> Unit = {}
) {
    var timeLeft by remember { mutableIntStateOf(45) }
    val isResendEnabled by remember { derivedStateOf { timeLeft == 0 } }

    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000L)
            timeLeft -= 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.95f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Success Illustration
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .background(SuccessGreen.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(SuccessGreenContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Copy
            Text(
                text = "Check your inbox!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "We've sent a password reset link to your email address.",
                fontSize = 16.sp,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Confirmation Pill
            Surface(
                color = SuccessPillBackground,
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mail,
                        contentDescription = null,
                        tint = PrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = email,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Email Preview Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceContainerLowest,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariant),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NetAdaptive",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface
                        )
                        Surface(
                            color = PrimaryContainer.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "30 min",
                                fontSize = 11.sp,
                                color = PrimaryContainer,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Reset your password",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "We received a request to reset your password for your NetAdaptive admin account. Click the link below to set a new password.",
                        fontSize = 13.sp,
                        color = OnSurfaceVariant,
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // CTAs
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onOpenEmailApp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OnSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Open email app",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onBackToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Back to login",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Timer / Resend
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Didn't receive it?",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant
                )
                TextButton(
                    onClick = {
                        timeLeft = 45
                        onResendClick()
                    },
                    enabled = isResendEnabled
                ) {
                    Text(
                        text = if (isResendEnabled) "Resend" else "Resend in 0:${timeLeft.toString().padStart(2, '0')}",
                        fontSize = 12.sp,
                        color = if (isResendEnabled) PrimaryContainer else OnSurfaceVariant.copy(alpha = 0.5f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgetPassSuccessPreview() {
    ForgetPassSuccessScreen()
}
