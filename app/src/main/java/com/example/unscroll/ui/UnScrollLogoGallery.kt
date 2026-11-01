package com.example.unscroll.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unscroll.ui.theme.AuroraBackgroundDark
import com.example.unscroll.ui.theme.AuroraGreenButton
import com.example.unscroll.ui.theme.AuroraTextPrimary
import com.example.unscroll.ui.theme.AuroraTextSecondary
import com.example.unscroll.ui.theme.GlassBorderStroke
import com.example.unscroll.ui.theme.ThermalAmberGold
import com.example.unscroll.ui.theme.ThermalCalmMint
import com.example.unscroll.ui.theme.ThermalCoolTeal
import com.example.unscroll.ui.theme.ThermalHotCrimson
import com.example.unscroll.ui.theme.ThermalSoftViolet
import com.example.unscroll.ui.theme.ThermalUppercaseLabelStyle
import com.example.unscroll.ui.theme.ThermalWarmOrange

enum class UnScrollLogoStyle(val title: String, val subtitle: String) {
    NEON_US_RIBBON("1. Neon US", "Glowing ribbon monogram"),
    ECLIPSE_HORIZON("2. Eclipse", "Thermal horizon orb"),
    UNSCROLL_ARROW("3. Break Loop", "Escaping scroll loop"),
    PRISM_CREST("4. Prism US", "Futuristic geometric crest"),
    AURORA_HOURGLASS("5. Time Wave", "Aurora hourglass & S-wave")
}

/**
 * Renders any of the 5 signature UnScroll app logo concepts at the given [size].
 */
@Composable
fun UnScrollBrandLogo(
    style: UnScrollLogoStyle,
    modifier: Modifier = Modifier,
    size: Dp = 68.dp,
    isSelected: Boolean = false
) {
    val iconShape = RoundedCornerShape(size * 0.28f)
    Box(
        modifier = modifier
            .size(size)
            .clip(iconShape)
            .background(AuroraBackgroundDark, iconShape)
            .border(
                BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) AuroraGreenButton else Color.White.copy(alpha = 0.24f)
                ),
                iconShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val center = Offset(w / 2f, h / 2f)

            // Ambient background aurora inside the squircle icon
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ThermalCoolTeal.copy(alpha = 0.32f),
                        ThermalSoftViolet.copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.32f, h * 0.28f),
                    radius = w * 0.75f
                ),
                radius = w * 0.75f,
                center = center
            )

            when (style) {
                UnScrollLogoStyle.NEON_US_RIBBON -> {
                    val strokeW = w * 0.095f
                    // Soft outer glow
                    val uPath = Path().apply {
                        moveTo(w * 0.24f, h * 0.30f)
                        lineTo(w * 0.24f, h * 0.57f)
                        cubicTo(w * 0.24f, h * 0.70f, w * 0.32f, h * 0.74f, w * 0.40f, h * 0.74f)
                        cubicTo(w * 0.48f, h * 0.74f, w * 0.54f, h * 0.68f, w * 0.54f, h * 0.56f)
                        lineTo(w * 0.54f, h * 0.28f)
                    }
                    drawPath(
                        path = uPath,
                        brush = Brush.linearGradient(
                            colors = listOf(ThermalCalmMint, ThermalCoolTeal)
                        ),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    // Upward arrow tip on U
                    val arrowPath = Path().apply {
                        moveTo(w * 0.46f, h * 0.36f)
                        lineTo(w * 0.54f, h * 0.26f)
                        lineTo(w * 0.62f, h * 0.36f)
                    }
                    drawPath(
                        path = arrowPath,
                        color = ThermalCalmMint,
                        style = Stroke(width = strokeW * 0.78f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Interlocking S curve
                    val sPath = Path().apply {
                        moveTo(w * 0.78f, h * 0.35f)
                        cubicTo(w * 0.74f, h * 0.27f, w * 0.62f, h * 0.28f, w * 0.61f, h * 0.39f)
                        cubicTo(w * 0.60f, h * 0.49f, w * 0.79f, h * 0.51f, w * 0.78f, h * 0.63f)
                        cubicTo(w * 0.77f, h * 0.73f, w * 0.64f, h * 0.75f, w * 0.58f, h * 0.68f)
                    }
                    drawPath(
                        path = sPath,
                        brush = Brush.linearGradient(
                            colors = listOf(ThermalCoolTeal, ThermalSoftViolet, ThermalHotCrimson)
                        ),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                UnScrollLogoStyle.ECLIPSE_HORIZON -> {
                    // Glowing thermal aurora ring around a dark eclipse planet
                    val orbRadius = w * 0.31f
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                ThermalCalmMint,
                                ThermalCoolTeal,
                                ThermalSoftViolet,
                                ThermalHotCrimson,
                                ThermalWarmOrange,
                                ThermalCalmMint
                            ),
                            center = center
                        ),
                        radius = orbRadius * 1.12f,
                        center = center
                    )
                    // Inner dark planet body shifted slightly bottom-right for crescent rim effect
                    drawCircle(
                        color = AuroraBackgroundDark,
                        radius = orbRadius * 0.94f,
                        center = Offset(center.x + w * 0.03f, center.y + h * 0.03f)
                    )
                    // Minimal upward sunrise beam inside the eclipse
                    val beamPath = Path().apply {
                        moveTo(w * 0.36f, h * 0.56f)
                        lineTo(w * 0.50f, h * 0.40f)
                        lineTo(w * 0.64f, h * 0.56f)
                    }
                    drawPath(
                        path = beamPath,
                        brush = Brush.linearGradient(listOf(ThermalCalmMint, ThermalCoolTeal)),
                        style = Stroke(width = w * 0.075f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    drawCircle(
                        color = ThermalCalmMint,
                        radius = w * 0.045f,
                        center = Offset(w * 0.50f, h * 0.64f)
                    )
                }

                UnScrollLogoStyle.UNSCROLL_ARROW -> {
                    // Broken scroll loop soaring upward into a free arrow
                    val strokeW = w * 0.095f
                    // Bottom warm scroll loop
                    val loopPath = Path().apply {
                        moveTo(w * 0.68f, h * 0.52f)
                        cubicTo(w * 0.68f, h * 0.69f, w * 0.59f, h * 0.76f, w * 0.47f, h * 0.76f)
                        cubicTo(w * 0.34f, h * 0.76f, w * 0.28f, h * 0.67f, w * 0.28f, h * 0.54f)
                        cubicTo(w * 0.28f, h * 0.43f, w * 0.38f, h * 0.40f, w * 0.50f, h * 0.36f)
                        lineTo(w * 0.50f, h * 0.22f)
                    }
                    drawPath(
                        path = loopPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(ThermalCalmMint, ThermalCoolTeal, ThermalSoftViolet, ThermalHotCrimson)
                        ),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    // Soaring upward arrow head
                    val headPath = Path().apply {
                        moveTo(w * 0.36f, h * 0.34f)
                        lineTo(w * 0.50f, h * 0.20f)
                        lineTo(w * 0.64f, h * 0.34f)
                    }
                    drawPath(
                        path = headPath,
                        color = ThermalCalmMint,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                UnScrollLogoStyle.PRISM_CREST -> {
                    // Geometric faceted diamond/prism frame + sharp US monogram
                    val prismPath = Path().apply {
                        moveTo(w * 0.50f, h * 0.14f)
                        lineTo(w * 0.84f, h * 0.36f)
                        lineTo(w * 0.72f, h * 0.80f)
                        lineTo(w * 0.28f, h * 0.80f)
                        lineTo(w * 0.16f, h * 0.36f)
                        close()
                    }
                    drawPath(
                        path = prismPath,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                ThermalCoolTeal.copy(alpha = 0.28f),
                                ThermalSoftViolet.copy(alpha = 0.28f)
                            )
                        )
                    )
                    drawPath(
                        path = prismPath,
                        brush = Brush.linearGradient(
                            colors = listOf(ThermalCalmMint, ThermalCoolTeal, ThermalSoftViolet)
                        ),
                        style = Stroke(width = w * 0.045f, join = StrokeJoin.Round)
                    )
                    // Sharp geometric U + S bars inside the crest
                    val uGeo = Path().apply {
                        moveTo(w * 0.33f, h * 0.38f)
                        lineTo(w * 0.33f, h * 0.62f)
                        lineTo(w * 0.47f, h * 0.62f)
                        lineTo(w * 0.47f, h * 0.38f)
                    }
                    drawPath(
                        path = uGeo,
                        color = Color.White,
                        style = Stroke(width = w * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    val sGeo = Path().apply {
                        moveTo(w * 0.67f, h * 0.38f)
                        lineTo(w * 0.55f, h * 0.38f)
                        lineTo(w * 0.55f, h * 0.50f)
                        lineTo(w * 0.67f, h * 0.50f)
                        lineTo(w * 0.67f, h * 0.62f)
                        lineTo(w * 0.55f, h * 0.62f)
                    }
                    drawPath(
                        path = sGeo,
                        color = ThermalCalmMint,
                        style = Stroke(width = w * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                UnScrollLogoStyle.AURORA_HOURGLASS -> {
                    // Flowing Aurora Hourglass + S-wave inside a glowing circle
                    val strokeW = w * 0.08f
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(ThermalCalmMint, ThermalCoolTeal, ThermalSoftViolet, ThermalAmberGold)
                        ),
                        topLeft = Offset(w * 0.22f, h * 0.22f),
                        size = Size(w * 0.56f, h * 0.56f),
                        cornerRadius = CornerRadius(w * 0.28f, w * 0.28f),
                        style = Stroke(width = w * 0.04f)
                    )
                    val wave1 = Path().apply {
                        moveTo(w * 0.34f, h * 0.32f)
                        lineTo(w * 0.66f, h * 0.32f)
                        cubicTo(w * 0.66f, h * 0.48f, w * 0.34f, h * 0.52f, w * 0.34f, h * 0.68f)
                        lineTo(w * 0.66f, h * 0.68f)
                    }
                    drawPath(
                        path = wave1,
                        brush = Brush.verticalGradient(
                            colors = listOf(ThermalCalmMint, ThermalCoolTeal, ThermalSoftViolet)
                        ),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        }
    }
}

/**
 * Interactive glass showcase strip displaying all 5 UnScroll logo concepts side by side.
 * Tapping any logo immediately applies it to the Home screen header.
 */
@Composable
fun UnScrollLogoShowcaseCard(
    selectedLogo: UnScrollLogoStyle,
    onSelectLogo: (UnScrollLogoStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CHOOSE APP LOGO",
                style = ThermalUppercaseLabelStyle,
                color = AuroraTextSecondary
            )
            Text(
                text = selectedLogo.subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = AuroraGreenButton
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            UnScrollLogoStyle.entries.forEach { style ->
                val selected = style == selectedLogo
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectLogo(style) }
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                ) {
                    UnScrollBrandLogo(
                        style = style,
                        size = 54.dp,
                        isSelected = selected
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = style.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) AuroraTextPrimary else AuroraTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
