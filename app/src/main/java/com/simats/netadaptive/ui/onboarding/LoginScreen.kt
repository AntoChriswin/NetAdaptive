package com.simats.netadaptive.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Reusing colors from design
private val SuccessColor = Color(0xFF1D9E75)
private val localOutline = Color(0xFF897365)

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onGoogleLoginClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceContainerLowest, RoundedCornerShape(12.dp))
                .border(1.dp, SurfaceVariant, RoundedCornerShape(12.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ... (Logo section)

            // Logo Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = RoundedCornerShape(36.dp),
                    color = PrimaryContainer.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = PrimaryContainer,
                            tonalElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
                
                Text(
                    text = "NetAdaptive",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    modifier = Modifier.padding(top = 12.dp)
                )
                
                Text(
                    text = "PREDICT · OPTIMIZE · ADAPT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = localOutline,
                    modifier = Modifier.padding(top = 4.dp),
                    letterSpacing = 1.sp
                )
            }

            // Header Copy
            Text(
                text = "Welcome back",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Log in to continue managing your network.",
                fontSize = 16.sp,
                color = localOutline,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Form Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Email address") },
                    leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, tint = localOutline) },
                    trailingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = SurfaceVariant,
                        focusedBorderColor = PrimaryContainer
                    ),
                    singleLine = true
                )

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryContainer) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = localOutline
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = PrimaryContainer,
                        focusedBorderColor = PrimaryContainer
                    ),
                    singleLine = true
                )
                
                // Forgot Password
                Text(
                    text = "Forgot password?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryContainer,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { onForgotPasswordClick() }
                )

                // Login CTA
                Button(
                    onClick = { onLoginClick(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OnSurface),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Log in", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                    }
                }

                // Divider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceVariant)
                    Text("or continue with", fontSize = 11.sp, color = localOutline)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceVariant)
                }

                // OAuth Button
                OutlinedButton(
                    onClick = onGoogleLoginClick,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, SurfaceVariant)
                ) {
                    Text("Google", color = OnSurface, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Footer
            Text(
                text = "Don't have an account? Sign up",
                fontSize = 12.sp,
                color = localOutline,
                modifier = Modifier
                    .padding(top = 32.dp, bottom = 8.dp)
                    .clickable { onSignUpClick() },
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    LoginScreen()
}
