package com.simats.netadaptive.ui.onboarding //[cite: 1]

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color constants moved to OnboardingColors.kt

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit = {},
    onResendClick: () -> Unit = {},
    onSendClick: (String) -> Unit = {}
) {
    var email by remember { mutableStateOf("admin@netadaptive.com") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Main Content Card
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                .background(SurfaceContainerLowest, RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = OutlineVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(32.dp)
        ) {

            // Back Navigation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.5.dp, SurfaceVariant, CircleShape)
                        .clip(CircleShape)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = OnSurface
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Back to login",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnSurfaceVariant,
                    modifier = Modifier.clickable { onBackClick() }
                )
            }

            // Illustration & Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Circular Graphic
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Dashed Ring
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                drawCircle(
                                    color = PrimaryContainer.copy(alpha = 0.3f),
                                    style = Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(
                                            floatArrayOf(15f, 15f), 0f
                                        )
                                    )
                                )
                            }
                    )

                    // Inner Circle
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(PrimaryFixed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = Primary,
                            modifier = Modifier.size(48.dp)
                        )

                        // Checkmark Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-4).dp, y = (-4).dp)
                                .size(24.dp)
                                .background(PrimaryContainer, CircleShape)
                                .border(2.dp, SurfaceContainerLowest, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Check",
                                tint = OnPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Forgot your password?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = OnSurface,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "No worries — enter your email and we'll send you a reset link right away.",
                    fontSize = 16.sp,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Form
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryContainer)
                    },
                    trailingIcon = {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SecondaryContainer)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryContainer,
                        unfocusedBorderColor = PrimaryContainer,
                        focusedContainerColor = SurfaceContainerLowest,
                        unfocusedContainerColor = SurfaceContainerLowest,
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(SecondaryContainer, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "We'll send a secure link to reset your password.",
                        fontSize = 11.sp,
                        color = OnSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onSendClick(email) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryContainer,
                        contentColor = OnPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Send reset link",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tip Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerLow, RoundedCornerShape(12.dp))
                    .border(
                        BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = PrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Reset link expires in 30 minutes",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = OnSurface,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "For security reasons, the link will become invalid after this time. You can always request a new one.",
                        fontSize = 11.sp,
                        color = OnSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Link
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Didn't receive it? Resend email",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryContainer,
                    modifier = Modifier.clickable { onResendClick() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordPreview() {
    MaterialTheme {
        ForgotPasswordScreen()
    }
}