package com.simats.netadaptive.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log

// Color constants moved to OnboardingColors.kt

@Composable
fun OnboardingPredictScreen(
    onNextClick: () -> Unit = {},
    onSkipClick: () -> Unit = {}
) {
    Log.d("OnboardingPredictScreen", "Composing OnboardingPredictScreen")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceContainerLow)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Illustration Area
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .padding(bottom = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Circles
                Box(modifier = Modifier.size(320.dp).border(1.dp, PrimaryContainer.copy(alpha = 0.05f), CircleShape))
                Box(modifier = Modifier.size(280.dp).border(1.dp, PrimaryContainer.copy(alpha = 0.1f), CircleShape))
                Box(modifier = Modifier.size(240.dp).border(1.dp, PrimaryContainer.copy(alpha = 0.2f), CircleShape))
                Box(modifier = Modifier.size(200.dp).clip(CircleShape).background(PrimaryFixed.copy(alpha = 0.6f)))

                // Floating Card
                Surface(
                    modifier = Modifier
                        .width(140.dp)
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp)),
                    color = SurfaceContainerLowest,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.height(8.dp).fillMaxWidth(0.85f).clip(CircleShape).background(SecondaryContainer))
                        Box(modifier = Modifier.height(8.dp).fillMaxWidth().clip(CircleShape).background(PrimaryContainer))
                        Box(modifier = Modifier.height(8.dp).fillMaxWidth(0.6f).clip(CircleShape).background(TertiaryContainer))
                        
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PrimaryContainer,
                            modifier = Modifier.size(20.dp).align(Alignment.End)
                        )
                    }
                }
            }

            // Text Content
            Text(
                text = "PREDICT",
                color = PrimaryContainer,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "Predict network drops before they happen",
                color = OnSurface,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 38.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Stay ahead of connectivity issues with AI-driven forecasting that alerts you to potential downtime.",
                color = OnSurfaceVariant,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.dp).padding(bottom = 64.dp)
            )

            // Pagination Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                Box(modifier = Modifier.size(width = 24.dp, height = 8.dp).clip(CircleShape).background(PrimaryContainer))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SurfaceVariant))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SurfaceVariant))
            }

            // Buttons
            Button(
                onClick = onNextClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "Next",
                    color = OnPrimaryContainer,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onSkipClick) {
                Text(
                    text = "Skip",
                    color = OnSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingPredictPreview() {
    OnboardingPredictScreen()
}
