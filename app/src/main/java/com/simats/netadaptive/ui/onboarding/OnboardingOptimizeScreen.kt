package com.simats.netadaptive.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.South
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color constants from the provided design
private val OptimizePrimary = Color(0xFFFEB913)
private val OptimizeSecondary = Color(0xFFA8DBDE)
private val DarkBlue = Color(0xFF063A5D)
private val OnSurfaceOptimize = Color(0xFF191C1E)
private val OnSurfaceVariantOptimize = Color(0xFF554337)
private val SurfaceContainerLowOptimize = Color(0xFFF3F4F6)
private val SurfaceContainerLowestOptimize = Color(0xFFFFFFFF)
private val SurfaceVariantOptimize = Color(0xFFE1E2E4)
private val OutlineOptimize = Color(0xFF897365)

@Composable
fun OnboardingOptimizeScreen(
    onNextClick: () -> Unit = {},
    onSkipClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceContainerLowOptimize)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Illustration Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Circle
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(OptimizePrimary.copy(alpha = 0.3f))
                )

                // Floating Data Card
                Surface(
                    modifier = Modifier
                        .width(240.dp)
                        .offset(y = (-8).dp),
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceContainerLowestOptimize,
                    tonalElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariantOptimize)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DataRow(
                            icon = Icons.Default.PlayCircle,
                            color = OptimizePrimary,
                            percentage = 0.82f,
                            label = "82%"
                        )
                        DataRow(
                            icon = Icons.Default.GraphicEq,
                            color = OptimizeSecondary,
                            percentage = 0.28f,
                            label = "28%"
                        )
                        DataRow(
                            icon = Icons.Default.SystemUpdate,
                            color = OutlineOptimize,
                            percentage = 0.08f,
                            label = "8%"
                        )
                    }
                }

                // Floating Stat Chips
                StatChip(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-20).dp, y = 40.dp),
                    icon = Icons.Default.Speed,
                    iconColor = OptimizePrimary,
                    text = "3x faster"
                )

                StatChip(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 20.dp, y = (-40).dp),
                    icon = Icons.Default.South,
                    iconColor = OptimizeSecondary,
                    text = "60% bg data"
                )
            }

            // Content Area
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "OPTIMIZE",
                    color = OptimizePrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Optimize bandwidth per app, in real time",
                    color = OnSurfaceOptimize,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 38.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = "Allocate resources where they matter most. Prioritize streaming, work, or gaming with a single tap.",
                    color = OnSurfaceVariantOptimize,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // Indicators
            Row(
                modifier = Modifier
                    .padding(top = 40.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SurfaceVariantOptimize))
                Box(modifier = Modifier.size(width = 32.dp, height = 8.dp).clip(CircleShape).background(PrimaryContainer))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SurfaceVariantOptimize))
            }

            // Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onNextClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OptimizePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Next",
                        color = DarkBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(onClick = onSkipClick) {
                    Text(
                        text = "Skip",
                        color = OnSurfaceVariantOptimize,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun DataRow(
    icon: ImageVector,
    color: Color,
    percentage: Float,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(CircleShape)
                .background(SurfaceContainerLowOptimize)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Text(
            text = label,
            color = OnSurfaceVariantOptimize,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    text: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = SurfaceContainerLowestOptimize,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariantOptimize)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                color = OnSurfaceOptimize,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingOptimizePreview() {
    OnboardingOptimizeScreen()
}
