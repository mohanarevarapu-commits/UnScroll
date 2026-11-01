package com.example.unscroll.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unscroll.ui.theme.AuroraTextPrimary
import com.example.unscroll.ui.theme.AuroraTextSecondary
import com.example.unscroll.ui.theme.CoolingPlanetSpacing
import com.example.unscroll.ui.theme.GlassBorderStroke
import com.example.unscroll.ui.theme.GlassSurfaceFill
import com.example.unscroll.ui.theme.GlassSurfaceFillActive
import com.example.unscroll.ui.theme.ManropeFontFamily
import com.example.unscroll.ui.theme.ThermalUppercaseLabelStyle
import com.example.unscroll.ui.theme.thermalColorForHeat
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Thermal Aurora Focus Session Screen:
 * - Large countdown timer (~100sp, Manrope Light 300) in the center
 * - Glass chip like "Cooling · 38% heat" with glowing indicator dot
 * - GlassCard with a short encouraging line
 * - Glass outline "End session" button
 * - Aurora cools slowly as time passes, with a gentle shimmer when finished
 */
@Composable
fun ThermalFocusSessionScreen(
    startingHeatLevel: Float,
    onSessionFinished: (Float) -> Unit,
    onEndSession: () -> Unit
) {
    val durationOptionsSeconds = listOf(10, 15 * 60, 25 * 60, 45 * 60)
    val durationLabels = listOf("10s Demo", "15m", "25m", "45m")

    var selectedDurationIndex by rememberSaveable { mutableIntStateOf(2) } // Default 25m
    val totalSeconds = durationOptionsSeconds[selectedDurationIndex]
    var remainingSeconds by rememberSaveable(selectedDurationIndex) { mutableIntStateOf(totalSeconds) }
    var isCompleted by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(selectedDurationIndex, isCompleted) {
        if (!isCompleted) {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds -= 1
            }
            isCompleted = true
        }
    }

    val progress = if (totalSeconds > 0) {
        ((totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 1f

    val currentSessionHeat = if (isCompleted) {
        0.05f
    } else {
        (startingHeatLevel * (1f - progress * 0.88f)).coerceIn(0.08f, 1f)
    }

    // Gentle completion shimmer pulse when finished
    val infiniteTransition = rememberInfiniteTransition(label = "CompletionShimmerTransition")
    val shimmerPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CompletionShimmerPulse"
    )

    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val formattedCountdown = String.format(Locale.US, "%02d:%02d", mins, secs)
    val heatPercent = (currentSessionHeat * 100).roundToInt()

    AuroraBackground(
        heatLevel = currentSessionHeat,
        shimmerBoost = if (isCompleted) shimmerPulse else 0f
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = CoolingPlanetSpacing.lg, vertical = CoolingPlanetSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Uppercase Header + Duration Selector Pills
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isCompleted) "FOCUS SESSION · COMPLETE" else "FOCUS SESSION",
                    style = ThermalUppercaseLabelStyle,
                    color = AuroraTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    durationLabels.forEachIndexed { index, label ->
                        val selected = selectedDurationIndex == index
                        val pillShape = RoundedCornerShape(50)
                        Box(
                            modifier = Modifier
                                .clip(pillShape)
                                .background(
                                    if (selected) GlassSurfaceFillActive else GlassSurfaceFill,
                                    pillShape
                                )
                                .border(
                                    BorderStroke(1.dp, if (selected) Color.White.copy(alpha = 0.35f) else GlassBorderStroke),
                                    pillShape
                                )
                                .clickable {
                                    selectedDurationIndex = index
                                    remainingSeconds = durationOptionsSeconds[index]
                                    isCompleted = false
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) AuroraTextPrimary else AuroraTextSecondary
                            )
                        }
                    }
                }
            }

            // Center 100sp Light Countdown Timer + Glass Chip
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isCompleted) "00:00" else formattedCountdown,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 100.sp,
                        lineHeight = 106.sp
                    ),
                    color = AuroraTextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                GlassChip(
                    label = if (isCompleted) "Cooled · $heatPercent% heat" else "Cooling · $heatPercent% heat",
                    dotColor = thermalColorForHeat(currentSessionHeat)
                )
            }

            // Bottom Encouraging GlassCard + Glass Outline "End session" Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = if (isCompleted) {
                            "Session complete. The aurora around you is cool, clear, and calm."
                        } else {
                            "Every quiet minute cools the aurora around you. Stay present."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = AuroraTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(CoolingPlanetSpacing.lg))

                if (isCompleted) {
                    AuroraPrimaryButton(
                        text = "Back to Home",
                        onClick = { onSessionFinished(0.16f) }
                    )
                } else {
                    GlassOutlineButton(
                        text = "End session",
                        onClick = onEndSession,
                        modifier = Modifier.fillMaxWidth(0.65f)
                    )
                }
            }
        }
    }
}
