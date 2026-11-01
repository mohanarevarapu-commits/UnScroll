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
import com.example.unscroll.ui.theme.AuroraCyanMistLight
import com.example.unscroll.ui.theme.AuroraDeepAbyssShade
import com.example.unscroll.ui.theme.AuroraIndigoShade
import com.example.unscroll.ui.theme.AuroraMidBlueShade
import com.example.unscroll.ui.theme.AuroraSapphireGlow
import com.example.unscroll.ui.theme.AuroraSkyBlueGlow
import com.example.unscroll.ui.theme.AuroraTopBlueLight

/**
 * Full-screen "Blue Light & Shade" Background:
 * - Multi-stop vertical blue shade base (#102A52 upper blue light -> #091933 mid sapphire shade -> #040B18 deep midnight shadow)
 * - Soft, unbounded glowing Blue Light beams (Sky Blue #4CC9F0, Ice Cyan #64DFDF, Royal Sapphire #3A86FF, and Deep Indigo #1E3A8A)
 *   creating a calm, atmospheric interplay of blue light and shadow across the screen.
 */
@Composable
fun AuroraBackground(
    heatLevel: Float,
    modifier: Modifier = Modifier,
    shimmerBoost: Float = 0f,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val clampedHeat = heatLevel.coerceIn(0f, 1f)

    // Pure Blue Light & Shade progression (bright ice-sky blue light <-> deep royal sapphire & indigo shade)
    val targetTopLightColor = lerp(AuroraCyanMistLight, AuroraSkyBlueGlow, clampedHeat)
    val targetMidLightColor = lerp(AuroraSkyBlueGlow, AuroraSapphireGlow, clampedHeat)
    val targetBottomShadeColor = lerp(AuroraSapphireGlow, AuroraIndigoShade, clampedHeat)

    val topLightColor by animateColorAsState(
        targetValue = targetTopLightColor,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "TopBlueLightColor"
    )
    val midLightColor by animateColorAsState(
        targetValue = targetMidLightColor,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "MidBlueLightColor"
    )
    val bottomShadeColor by animateColorAsState(
        targetValue = targetBottomShadeColor,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "BottomBlueShadeColor"
    )

    // Slow continuous drift for the blue light & shade layers
    val infiniteTransition = rememberInfiniteTransition(label = "BlueLightDriftTransition")

    val driftX1 by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 34f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BlueBeam1DriftX"
    )
    val driftY1 by infiniteTransition.animateFloat(
        initialValue = -22f,
        targetValue = 26f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 17000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BlueBeam1DriftY"
    )

    val driftX2 by infiniteTransition.animateFloat(
        initialValue = 28f,
        targetValue = -32f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BlueBeam2DriftX"
    )
    val driftY2 by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = -24f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 19000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BlueBeam2DriftY"
    )

    val driftX3 by infiniteTransition.animateFloat(
        initialValue = -24f,
        targetValue = 26f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BlueBeam3DriftX"
    )
    val driftY3 by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BlueBeam3DriftY"
    )

    val extraAlpha = (shimmerBoost * 0.18f).coerceIn(0f, 0.2f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AuroraTopBlueLight,
                        AuroraMidBlueShade,
                        AuroraDeepAbyssShade
                    )
                )
            )
    ) {
        // Unbounded blurred Canvas rendering soft Blue Light & Deep Blue Shade layers
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 80.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        ) {
            val w = size.width
            val h = size.height

            // 1. Overhead Sky-Blue Light Beam (bright soft blue light from the top)
            val overheadCenter = Offset(w * 0.50f, h * 0.06f)
            val overheadRadius = w * 0.90f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AuroraSkyBlueGlow.copy(alpha = 0.48f + extraAlpha),
                        AuroraSapphireGlow.copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    center = overheadCenter,
                    radius = overheadRadius
                ),
                radius = overheadRadius,
                center = overheadCenter
            )

            // 2. Top-Left Luminous Ice/Sky Blue Light Sphere
            val center1 = Offset(
                x = w * 0.20f + driftX1.dp.toPx(),
                y = h * 0.22f + driftY1.dp.toPx()
            )
            val radius1 = 240.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        topLightColor.copy(alpha = (0.60f + extraAlpha).coerceAtMost(0.85f)),
                        AuroraSapphireGlow.copy(alpha = 0.26f),
                        Color.Transparent
                    ),
                    center = center1,
                    radius = radius1
                ),
                radius = radius1,
                center = center1
            )

            // 3. Center-Right Royal Sapphire Blue Light & Shade Sphere
            val center2 = Offset(
                x = w * 0.82f + driftX2.dp.toPx(),
                y = h * 0.48f + driftY2.dp.toPx()
            )
            val radius2 = 240.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        midLightColor.copy(alpha = (0.55f + extraAlpha).coerceAtMost(0.82f)),
                        AuroraIndigoShade.copy(alpha = 0.34f),
                        Color.Transparent
                    ),
                    center = center2,
                    radius = radius2
                ),
                radius = radius2,
                center = center2
            )

            // 4. Bottom Deep Indigo-Sapphire Shade & Glow
            val center3 = Offset(
                x = w * 0.42f + driftX3.dp.toPx(),
                y = h * 0.82f + driftY3.dp.toPx()
            )
            val radius3 = 235.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        bottomShadeColor.copy(alpha = (0.52f + extraAlpha).coerceAtMost(0.78f)),
                        AuroraDeepAbyssShade.copy(alpha = 0.40f),
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
