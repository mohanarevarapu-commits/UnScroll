package com.example.unscroll.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.unscroll.ui.theme.AuroraBackgroundDark
import com.example.unscroll.ui.theme.ThermalCalmMint
import com.example.unscroll.ui.theme.ThermalCoolTeal
import com.example.unscroll.ui.theme.ThermalHotCrimson
import com.example.unscroll.ui.theme.ThermalSoftViolet
import com.example.unscroll.ui.theme.ThermalWarmOrange
import kotlin.math.roundToInt

/**
 * Reusable full-screen Thermal Aurora background with 3 large soft blurred circles
 * (300dp to 340dp, blur 70dp to 80dp, opacity 0.4 to 0.8) on #0B0E1D.
 *
 * - At heatLevel = 1f (Hot): top-left & center-right blobs are magenta-orange (#FF4D6D, #FF8A3D)
 *   with soft violet (#A98BFF) at the bottom.
 * - At heatLevel = 0f (Cool): top-left & center-right blobs are teal-green (#2FC4B2, #4FE3A0)
 *   with soft violet (#A98BFF) at the bottom.
 * - Animates color transitions smoothly with [animateColorAsState] and drifts very slowly.
 */
@Composable
fun AuroraBackground(
    heatLevel: Float,
    modifier: Modifier = Modifier,
    shimmerBoost: Float = 0f,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val clampedHeat = heatLevel.coerceIn(0f, 1f)

    // Target colors interpolated between Cool (0f) and Hot (1f)
    val targetTopBlobColor = lerp(ThermalCoolTeal, ThermalHotCrimson, clampedHeat)
    val targetMidBlobColor = lerp(ThermalCalmMint, ThermalWarmOrange, clampedHeat)
    val targetBottomBlobColor = lerp(
        ThermalSoftViolet.copy(alpha = 0.85f),
        ThermalSoftViolet,
        clampedHeat
    )

    val topBlobColor by animateColorAsState(
        targetValue = targetTopBlobColor,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "TopAuroraBlobColor"
    )
    val midBlobColor by animateColorAsState(
        targetValue = targetMidBlobColor,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "MidAuroraBlobColor"
    )
    val bottomBlobColor by animateColorAsState(
        targetValue = targetBottomBlobColor,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "BottomAuroraBlobColor"
    )

    // Slow continuous drift for the 3 aurora blobs
    val infiniteTransition = rememberInfiniteTransition(label = "AuroraDriftTransition")

    val driftX1 by infiniteTransition.animateFloat(
        initialValue = -28f,
        targetValue = 32f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Blob1DriftX"
    )
    val driftY1 by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 26f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 17000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Blob1DriftY"
    )

    val driftX2 by infiniteTransition.animateFloat(
        initialValue = 26f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Blob2DriftX"
    )
    val driftY2 by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = -24f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 19000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Blob2DriftY"
    )

    val driftX3 by infiniteTransition.animateFloat(
        initialValue = -22f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Blob3DriftX"
    )
    val driftY3 by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Blob3DriftY"
    )

    val extraAlpha = (shimmerBoost * 0.18f).coerceIn(0f, 0.2f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuroraBackgroundDark)
    ) {
        // Blob 1: Top-left (340dp, blur 80dp, opacity ~0.72)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset {
                    IntOffset(
                        x = ((-48 + driftX1) * density).roundToInt(),
                        y = ((-36 + driftY1) * density).roundToInt()
                    )
                }
                .size(340.dp)
                .blur(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            topBlobColor.copy(alpha = (0.74f + extraAlpha).coerceAtMost(0.9f)),
                            topBlobColor.copy(alpha = 0.28f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Blob 2: Center-right (320dp, blur 75dp, opacity ~0.64)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset {
                    IntOffset(
                        x = ((54 + driftX2) * density).roundToInt(),
                        y = ((-30 + driftY2) * density).roundToInt()
                    )
                }
                .size(320.dp)
                .blur(75.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            midBlobColor.copy(alpha = (0.66f + extraAlpha).coerceAtMost(0.88f)),
                            midBlobColor.copy(alpha = 0.24f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Blob 3: Bottom-center Violet (#A98BFF, 310dp, blur 75dp, opacity ~0.48)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset {
                    IntOffset(
                        x = ((driftX3) * density).roundToInt(),
                        y = ((40 + driftY3) * density).roundToInt()
                    )
                }
                .size(310.dp)
                .blur(75.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            bottomBlobColor.copy(alpha = (0.50f + extraAlpha).coerceAtMost(0.8f)),
                            bottomBlobColor.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Foreground screen content
        content()
    }
}
