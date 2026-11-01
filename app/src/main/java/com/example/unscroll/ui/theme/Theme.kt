package com.example.unscroll.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Thermal Aurora is dark-only with deep cosmic background #0B0E1D (never pure black),
 * primary green #3DDC97 with #052A1A text, white primary text, and #D5D9EE secondary text.
 */
private val ThermalAuroraColorScheme = darkColorScheme(
    primary = AuroraGreenButton,
    onPrimary = OnAuroraGreenButton,
    primaryContainer = GlassSurfaceFillActive,
    onPrimaryContainer = AuroraTextPrimary,
    secondary = ThermalCoolTeal,
    onSecondary = OnAuroraGreenButton,
    tertiary = ThermalSoftViolet,
    onTertiary = AuroraTextPrimary,
    background = AuroraBackgroundDark,
    onBackground = AuroraTextPrimary,
    surface = AuroraSurfaceDark,
    onSurface = AuroraTextPrimary,
    surfaceVariant = AuroraSurfaceElevated,
    onSurfaceVariant = AuroraTextSecondary,
    outline = GlassBorderStroke
)

val ThermalAuroraShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

object CoolingPlanetSpacing {
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 40.dp
}

@Composable
fun ThermalAuroraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ThermalAuroraColorScheme,
        typography = ThermalAuroraTypography,
        shapes = ThermalAuroraShapes,
        content = content
    )
}

@Composable
fun CoolingPlanetTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    ThermalAuroraTheme(content = content)
}

@Composable
fun UnScrollTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ThermalAuroraTheme(content = content)
}
