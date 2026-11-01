package com.example.unscroll.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.example.unscroll.ui.theme.AuroraBackgroundDark
import com.example.unscroll.ui.theme.ThermalCalmMint
import com.example.unscroll.ui.theme.ThermalCoolTeal
import com.example.unscroll.ui.theme.ThermalHotCrimson
import com.example.unscroll.ui.theme.ThermalSoftViolet
import com.example.unscroll.ui.theme.ThermalWarmOrange

/**
 * Reusable full-screen Thermal Aurora background with 3 large soft blurred circles
 * (300dp to 340dp radius, blur 70dp to 80dp with Unbounded edge treatment so no square clip boxes appear)
 * on #0B0E1D.
 *
 * - At heatLevel = 1f (Hot): blobs are magenta-orange (#FF4D6D, #FF8A3D) with violet (#A98BFF) at the bottom.
 * - At heatLevel = 0f (Cool): blobs are teal-green (#2FC4B2, #4FE3A0) with violet (#A98BFF) at the bottom.
 * - Animates color changes smoothly with [animateColorAsState] and drifts very slowly.
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

    // Slow continuous drift for the 3 aurora circles
    val infiniteTransition = rememberInfiniteTransition(label = "AuroraDriftTransition")

    val driftX1 by infiniteTransition.animateFloat(
        initialValue = -32f,
        targetValue = 36f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Blob1DriftX"
    )
    val driftY1 by infiniteTransition.animateFloat(
        initialValue = -24f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 17000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Blob1DriftY"
    )

    val driftX2 by infiniteTransition.animateFloat(
        initialValue = 28f,
        targetValue = -34f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Blob2DriftX"
    )
    val driftY2 by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = -26f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 19000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Blob2DriftY"
    )

    val driftX3 by infiniteTransition.animateFloat(
        initialValue = -24f,
        targetValue = 26f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Blob3DriftX"
    )
    val driftY3 by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = -22f,
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
        // Full-screen Canvas with Unbounded 75dp blur so the 3 circles never clip into square boxes
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 75.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        ) {
            val w = size.width
            val h = size.height

            // 1. Top-Left Aurora Circle (~340dp diameter, opacity ~0.72)
            val center1 = Offset(
                x = w * 0.22f + driftX1.dp.toPx(),
                y = h * 0.18f + driftY1.dp.toPx()
            )
            val radius1 = 230.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        topBlobColor.copy(alpha = (0.72f + extraAlpha).coerceAtMost(0.88f)),
                        topBlobColor.copy(alpha = 0.34f),
                        Color.Transparent
                    ),
                    center = center1,
                    radius = radius1
                ),
                radius = radius1,
                center = center1
            )

            // 2. Center-Right Aurora Circle (~320dp diameter, opacity ~0.64)
            val center2 = Offset(
                x = w * 0.80f + driftX2.dp.toPx(),
                y = h * 0.44f + driftY2.dp.toPx()
            )
            val radius2 = 220.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        midBlobColor.copy(alpha = (0.64f + extraAlpha).coerceAtMost(0.85f)),
                        midBlobColor.copy(alpha = 0.28f),
                        Color.Transparent
                    ),
                    center = center2,
                    radius = radius2
                ),
                radius = radius2,
                center = center2
            )

            // 3. Bottom-Center Violet Aurora Circle (~310dp diameter, opacity ~0.52)
            val center3 = Offset(
                x = w * 0.45f + driftX3.dp.toPx(),
                y = h * 0.82f + driftY3.dp.toPx()
            )
            val radius3 = 225.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        bottomBlobColor.copy(alpha = (0.54f + extraAlpha).coerceAtMost(0.80f)),
                        bottomBlobColor.copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    center = center3,
                    radius = radius3
                ),
                radius = radius3,
                center = center3
            )
        }

        // Foreground screen content
        content()
    }
}
