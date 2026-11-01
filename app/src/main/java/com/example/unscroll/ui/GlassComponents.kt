package com.example.unscroll.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.unscroll.ui.theme.AuroraGreenButton
import com.example.unscroll.ui.theme.AuroraTextPrimary
import com.example.unscroll.ui.theme.AuroraTextSecondary
import com.example.unscroll.ui.theme.GlassBorderStroke
import com.example.unscroll.ui.theme.GlassSurfaceFill
import com.example.unscroll.ui.theme.GlassSurfaceFillActive
import com.example.unscroll.ui.theme.OnAuroraGreenButton

/**
 * Reusable GlassCard:
 * - Translucent white fill (~9% opacity)
 * - 1dp border (~18% white opacity)
 * - 24dp rounded corners
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    fillColor: Color = GlassSurfaceFill,
    borderColor: Color = GlassBorderStroke,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .clip(shape)
            .background(fillColor, shape)
            .border(BorderStroke(1.dp, borderColor), shape)
            .then(clickableModifier)
            .padding(contentPadding),
        content = content
    )
}

/**
 * Translucent glass chip with a glowing colored dot and label (e.g. "High heat", "Cooling · 38% heat").
 */
@Composable
fun GlassChip(
    label: String,
    modifier: Modifier = Modifier,
    dotColor: Color? = null
) {
    val chipShape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .clip(chipShape)
            .background(GlassSurfaceFill, chipShape)
            .border(BorderStroke(1.dp, GlassBorderStroke), chipShape)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (dotColor != null) {
            Canvas(modifier = Modifier.size(18.dp)) {
                val r = size.minDimension / 2f
                val c = Offset(size.width / 2f, size.height / 2f)
                // Smooth radial glow reaching 0 alpha at radius r (no boxy edge)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            dotColor.copy(alpha = 0.70f),
                            dotColor.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = c,
                        radius = r
                    ),
                    radius = r,
                    center = c
                )
                // Crisp solid core dot
                drawCircle(
                    color = dotColor,
                    radius = 4.dp.toPx(),
                    center = c
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = AuroraTextPrimary
        )
    }
}

/**
 * Primary green button:
 * - Background #3DDC97
 * - Text #052A1A (Bold)
 * - Soft green glow shadow
 */
@Composable
fun AuroraPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 58.dp
) {
    val buttonShape = RoundedCornerShape(28.dp)
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shadow(
                elevation = 20.dp,
                shape = buttonShape,
                ambientColor = AuroraGreenButton,
                spotColor = AuroraGreenButton
            ),
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = AuroraGreenButton,
            contentColor = OnAuroraGreenButton
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OnAuroraGreenButton
        )
    }
}

/**
 * Translucent glass outline button for secondary actions like "End session".
 */
@Composable
fun GlassOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonShape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .heightIn(min = 54.dp)
            .clip(buttonShape)
            .background(GlassSurfaceFill, buttonShape)
            .border(BorderStroke(1.dp, GlassBorderStroke), buttonShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 32.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = AuroraTextPrimary
        )
    }
}

/**
 * Floating bottom navigation bar:
 * - 68dp tall, 34dp rounded corners, glass style
 * - Three labelled buttons (Home, Heat map, Blocking)
 * - Active tab has a lighter translucent pill behind it
 * - All tap targets at least 48dp
 */
@Composable
fun GlassFloatingBottomBar(
    selectedTab: CoolingPlanetTab,
    onSelectTab: (CoolingPlanetTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val barShape = RoundedCornerShape(34.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .height(68.dp)
            .clip(barShape)
            .background(Color(0x1FFFFFFF), barShape)
            .border(BorderStroke(1.dp, GlassBorderStroke), barShape)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoolingPlanetTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            val pillColor by animateColorAsState(
                targetValue = if (isSelected) GlassSurfaceFillActive else Color.Transparent,
                animationSpec = tween(durationMillis = 250),
                label = "BottomNavPillColor"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) AuroraTextPrimary else AuroraTextSecondary,
                animationSpec = tween(durationMillis = 250),
                label = "BottomNavContentColor"
            )
            val pillShape = RoundedCornerShape(28.dp)

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .heightIn(min = 48.dp)
                    .clip(pillShape)
                    .background(pillColor, pillShape)
                    .then(
                        if (isSelected) {
                            Modifier.border(BorderStroke(1.dp, GlassBorderStroke), pillShape)
                        } else {
                            Modifier
                        }
                    )
                    .clickable { onSelectTab(tab) }
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.label,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor
                )
            }
        }
    }
}
