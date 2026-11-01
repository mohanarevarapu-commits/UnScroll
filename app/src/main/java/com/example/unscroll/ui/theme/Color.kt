package com.example.unscroll.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// ============================================================================
// Thermal Aurora Core Dark-Only Palette
// ============================================================================
val AuroraBackgroundDark = Color(0xFF0B0E1D) // Deep cosmic navy (never pure black)
val AuroraSurfaceDark = Color(0xFF13182E)
val AuroraSurfaceElevated = Color(0xFF1B213D)

// Primary Green Action Button (#3DDC97 background, #052A1A text)
val AuroraGreenButton = Color(0xFF3DDC97)
val OnAuroraGreenButton = Color(0xFF052A1A)
val AuroraGreenGlow = Color(0x663DDC97)

// Text Colors
val AuroraTextPrimary = Color(0xFFFFFFFF)
val AuroraTextSecondary = Color(0xFFD5D9EE)
val AuroraTextMuted = Color(0x99D5D9EE)

// Glassmorphism Tokens (8-10% white fill, 16-20% white border)
val GlassSurfaceFill = Color(0x17FFFFFF)        // ~9% white
val GlassSurfaceFillActive = Color(0x29FFFFFF)  // ~16% white for active pills
val GlassBorderStroke = Color(0x2EFFFFFF)       // ~18% white
val GlassBorderHighlight = Color(0x47FFFFFF)    // ~28% white

// ============================================================================
// Thermal Aurora 6-Stop Heat Palette (Hot to Cool)
// #FF4D6D -> #FF8A3D -> #FFB454 -> #A98BFF -> #2FC4B2 -> #4FE3A0
// ============================================================================
val ThermalHotCrimson = Color(0xFFFF4D6D)    // Hot 1.0
val ThermalWarmOrange = Color(0xFFFF8A3D)    // Warm-Hot 0.8
val ThermalAmberGold = Color(0xFFFFB454)     // Moderate-Warm 0.6
val ThermalSoftViolet = Color(0xFFA98BFF)    // Mid Transition 0.4
val ThermalCoolTeal = Color(0xFF2FC4B2)      // Cool 0.2
val ThermalCalmMint = Color(0xFF4FE3A0)      // Coolest 0.0

// Ordered from Cool (0f) to Hot (1f) for interpolation
val ThermalPaletteCoolToHot = listOf(
    ThermalCalmMint,     // #4FE3A0 (0.0)
    ThermalCoolTeal,     // #2FC4B2 (0.2)
    ThermalSoftViolet,   // #A98BFF (0.4)
    ThermalAmberGold,    // #FFB454 (0.6)
    ThermalWarmOrange,   // #FF8A3D (0.8)
    ThermalHotCrimson    // #FF4D6D (1.0)
)

// Backwards-compatible aliases
val CalmGreenPrimary = AuroraGreenButton
val HeatCalmBlueGreen = ThermalCoolTeal
val HeatCoolSage = ThermalCalmMint
val HeatPaleSand = ThermalAmberGold
val HeatPeach = ThermalWarmOrange
val HeatRedOrange = ThermalHotCrimson

/**
 * Smoothly interpolates across the 6-stop Thermal Aurora palette
 * from Cool Mint/Teal (0f) to Hot Crimson/Orange (1f).
 */
fun thermalColorForHeat(heatLevel: Float): Color {
    val clamped = heatLevel.coerceIn(0f, 1f)
    val scaled = clamped * (ThermalPaletteCoolToHot.size - 1)
    val lowerIndex = scaled.toInt().coerceAtMost(ThermalPaletteCoolToHot.size - 2)
    val fraction = scaled - lowerIndex
    return lerp(ThermalPaletteCoolToHot[lowerIndex], ThermalPaletteCoolToHot[lowerIndex + 1], fraction)
}

fun planetColorForHeat(heatLevel: Float): Color = thermalColorForHeat(heatLevel)

/**
 * Short chip label for the Home screen & Heat Map (e.g., "High heat", "Moderate heat", "Cool calm").
 */
fun thermalShortLabelForHeat(heatLevel: Float): String {
    val clamped = heatLevel.coerceIn(0f, 1f)
    return when {
        clamped < 0.22f -> "Cool calm"
        clamped < 0.42f -> "Cooling"
        clamped < 0.62f -> "Moderate heat"
        clamped < 0.80f -> "Warm heat"
        else -> "High heat"
    }
}

fun planetLabelForHeat(heatLevel: Float): String = thermalShortLabelForHeat(heatLevel)
