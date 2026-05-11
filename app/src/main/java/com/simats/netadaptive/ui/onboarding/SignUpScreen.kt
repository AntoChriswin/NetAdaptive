package com.simats.netadaptive.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color constants matching the design (reusing from OnboardingPredictScreen)
private val Outline = Color(0xFF897365)
private val localSecondary = Color(0xFF7C5800)
private val localTertiaryFixedDim = Color(0xFFEEC141)
private val localSurfaceContainer = Color(0xFFEDEEF0)
private val localSurfaceContainerHigh = Color(0xFFE7E8EA)
private val localOutlineVariant = Color(0xFFDCC1B1)

@Composable
fun SignUpScreen(
    onSignUpClick: (String, String) -> Unit = { _, _ -> },
    onLoginClick: () -> Unit = {},
    onGoogleSignUpClick: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceContainerLowest, RoundedCornerShape(12.dp))
                .border(1.dp, localSurfaceContainer, RoundedCornerShape(12.dp))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ... (rest of the header)

            // Header
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(PrimaryContainer, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = OnPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "NetAdaptive",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface
                    )
                }
                
                Text(
                    text = "Create your account",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    lineHeight = 38.sp
                )
                
                Text(
                    text = "Start predicting and optimizing your network today.",
                    fontSize = 16.sp,
                    color = OnSurfaceVariant
                )
            }

            // Form
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Full Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Outline) },
                    trailingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = localSecondary) },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Outline,
                        focusedBorderColor = PrimaryContainer
                    ),
                    singleLine = true
                )

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Email address") },
                    leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, tint = PrimaryContainer) },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = PrimaryContainer,
                        focusedBorderColor = PrimaryContainer
                    ),
                    singleLine = true
                )

                // Password
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Outline) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Outline
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = localOutlineVariant,
                            focusedBorderColor = PrimaryContainer
                        ),
                        singleLine = true
                    )
                    
                    // Strength Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(PrimaryContainer, CircleShape))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(localTertiaryFixedDim, CircleShape))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(localSecondary, CircleShape))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(localSurfaceContainerHigh, CircleShape))
                    }
                }

                // Terms
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .offset(y = 2.dp)
                            .background(PrimaryContainer, RoundedCornerShape(4.dp))
                            .clickable { termsAccepted = !termsAccepted },
                        contentAlignment = Alignment.Center
                    ) {
                        if (termsAccepted) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                    Text(
                        text = "I agree to the Terms of Service and Privacy Policy",
                        fontSize = 11.sp,
                        color = OnSurfaceVariant,
                        lineHeight = 14.sp
                    )
                }

                // CTA
                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            onSignUpClick(email, password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    enabled = termsAccepted
                ) {
                    Text("Create account", color = OnPrimaryContainer, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                // Divider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = localSurfaceContainer)
                    Text("or continue with", fontSize = 11.sp, color = OnSurfaceVariant)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = localSurfaceContainer)
                }

                // Social Buttons
                OutlinedButton(
                    onClick = onGoogleSignUpClick,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, localOutlineVariant)
                ) {
                    Text("Google", color = OnSurface, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Footer
            Text(
                text = "Already have an account? Log in",
                fontSize = 14.sp,
                color = OnSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLoginClick() },
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpPreview() {
    SignUpScreen()
}
