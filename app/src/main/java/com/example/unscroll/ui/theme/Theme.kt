package com.example.unscroll.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val CoolingPlanetLightColorScheme = lightColorScheme(
    primary = CalmGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = CalmGreenContainer,
    onPrimaryContainer = OnCalmGreenContainer,
    secondary = HeatCoolSage,
    onSecondary = Color.White,
    tertiary = HeatCalmBlueGreen,
    onTertiary = Color.White,
    background = WarmOffWhite,
    onBackground = InkPrimaryLight,
    surface = SoftWhiteCard,
    onSurface = InkPrimaryLight,
    surfaceVariant = SoftGreySurface,
    onSurfaceVariant = InkSecondaryLight,
    outline = OutlineSoftLight
)

private val CoolingPlanetDarkColorScheme = darkColorScheme(
    primary = CalmGreenPrimaryDark,
    onPrimary = Color(0xFF0E241A),
    primaryContainer = CalmGreenContainerDark,
    onPrimaryContainer = OnCalmGreenContainerDark,
    secondary = HeatCoolSage,
    onSecondary = Color(0xFF10281E),
    tertiary = HeatCalmBlueGreen,
    onTertiary = Color(0xFF0C2826),
    background = NightBackground,
    onBackground = InkPrimaryDark,
    surface = NightCardSurface,
    onSurface = InkPrimaryDark,
    surfaceVariant = NightGreySurface,
    onSurfaceVariant = InkSecondaryDark,
    outline = OutlineSoftDark
)

/**
 * 24dp rounded corners across cards, buttons, and containers.
 */
val CoolingPlanetShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

/**
 * Generous spacing tokens for the CoolingPlanet design system.
 */
object CoolingPlanetSpacing {
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 40.dp
}

@Composable
fun CoolingPlanetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        CoolingPlanetDarkColorScheme
    } else {
        CoolingPlanetLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CoolingPlanetTypography,
        shapes = CoolingPlanetShapes,
        content = content
    )
}

@Composable
fun UnScrollTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CoolingPlanetTheme(darkTheme = darkTheme, content = content)
}
