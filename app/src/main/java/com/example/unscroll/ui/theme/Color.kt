package com.example.unscroll.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// ============================================================================
// CoolingPlanet Core Neutral & Calm Green Palette (Light Mode)
// ============================================================================
val WarmOffWhite = Color(0xFFF7F5F0)
val SoftGreySurface = Color(0xFFEEECE6)
val SoftWhiteCard = Color(0xFFFFFFFF)
val CalmGreenPrimary = Color(0xFF4F8A6E)
val CalmGreenDark = Color(0xFF3B6E56)
val CalmGreenContainer = Color(0xFFDCECE3)
val OnCalmGreenContainer = Color(0xFF163828)

val InkPrimaryLight = Color(0xFF1F2622)
val InkSecondaryLight = Color(0xFF5F6963)
val OutlineSoftLight = Color(0xFFDCD8CF)

// ============================================================================
// CoolingPlanet Core Neutral & Calm Green Palette (Dark Mode)
// ============================================================================
val NightBackground = Color(0xFF121614)
val NightGreySurface = Color(0xFF1C221F)
val NightCardSurface = Color(0xFF232A26)
val CalmGreenPrimaryDark = Color(0xFF72B093)
val CalmGreenContainerDark = Color(0xFF234234)
val OnCalmGreenContainerDark = Color(0xFFD6EFE2)

val InkPrimaryDark = Color(0xFFECEAE4)
val InkSecondaryDark = Color(0xFFA2ADA6)
val OutlineSoftDark = Color(0xFF323B36)

// ============================================================================
// CoolingPlanet 5-Stage "Heat-to-Cool" Palette
// Moves smoothly: Soft Red-Orange -> Peach -> Pale Sand -> Cool Sage -> Calm Blue-Green
// ============================================================================
val HeatRedOrange = Color(0xFFE56B55)      // 1.00 - Hot (Lava Red-Orange)
val HeatPeach = Color(0xFFF29E6D)          // 0.75 - Warm (Soft Peach)
val HeatPaleSand = Color(0xFFEAD29C)       // 0.50 - Moderate (Pale Sand)
val HeatCoolSage = Color(0xFF8AB89E)       // 0.25 - Cool (Cool Sage)
val HeatCalmBlueGreen = Color(0xFF4F9D96)  // 0.00 - Coolest (Calm Blue-Green)

val HeatPaletteStops = listOf(
    HeatCalmBlueGreen,
    HeatCoolSage,
    HeatPaleSand,
    HeatPeach,
    HeatRedOrange
)

/**
 * Smoothly interpolates the planet color from Calm Blue-Green (0f, low screen time)
 * to Lava Red-Orange (1f, high screen time) across the 5-stage heat palette.
 */
fun planetColorForHeat(heatLevel: Float): Color {
    val clamped = heatLevel.coerceIn(0f, 1f)
    val scaled = clamped * (HeatPaletteStops.size - 1)
    val lowerIndex = scaled.toInt().coerceAtMost(HeatPaletteStops.size - 2)
    val fraction = scaled - lowerIndex
    return lerp(HeatPaletteStops[lowerIndex], HeatPaletteStops[lowerIndex + 1], fraction)
}

/**
 * Accessible text label paired with every heat level so color is never the only clue.
 */
fun planetLabelForHeat(heatLevel: Float): String {
    val clamped = heatLevel.coerceIn(0f, 1f)
    return when {
        clamped < 0.20f -> "Calm Blue-Green • Low usage"
        clamped < 0.45f -> "Cool Sage • Mindful pace"
        clamped < 0.65f -> "Pale Sand • Moderate usage"
        clamped < 0.85f -> "Warm Peach • High usage"
        else -> "Lava Red-Orange • Overheated"
    }
}
