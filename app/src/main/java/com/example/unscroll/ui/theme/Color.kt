package com.example.unscroll.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// ============================================================================
// Blue Light & Shade Core Palette
// ============================================================================
val AuroraBackgroundDark = Color(0xFF061022)    // Deep midnight blue shade
val AuroraTopBlueLight = Color(0xFF102A52)      // Upper atmospheric blue light
val AuroraMidBlueShade = Color(0xFF091933)      // Mid-tone sapphire shade
val AuroraDeepAbyssShade = Color(0xFF040B18)    // Deep bottom shadow

val AuroraSkyBlueGlow = Color(0xFF4CC9F0)       // Luminous sky-blue light
val AuroraSapphireGlow = Color(0xFF3A86FF)      // Vibrant royal sapphire light
val AuroraCyanMistLight = Color(0xFF64DFDF)     // Cool cyan-blue mist
val AuroraIndigoShade = Color(0xFF1E3A8A)       // Deep indigo-blue shade

val AuroraSurfaceDark = Color(0xFF0D1E3A)
val AuroraSurfaceElevated = Color(0xFF152C52)

// Primary Action Button (#3DDC97 background, #052A1A text)
val AuroraGreenButton = Color(0xFF3DDC97)
val OnAuroraGreenButton = Color(0xFF052A1A)
val AuroraGreenGlow = Color(0x663DDC97)

// Text Colors
val AuroraTextPrimary = Color(0xFFFFFFFF)
val AuroraTextSecondary = Color(0xFFD5E2F6)
val AuroraTextMuted = Color(0x99D5E2F6)

// Glassmorphism Tokens (translucent blue-tinted white fill & border)
val GlassSurfaceFill = Color(0x1ACCE3FF)        // ~10% ice-blue white fill
val GlassSurfaceFillActive = Color(0x2ECCE3FF)  // ~18% ice-blue white for active pills
val GlassBorderStroke = Color(0x33D8EAFF)       // ~20% soft blue-white border
val GlassBorderHighlight = Color(0x52D8EAFF)

// ============================================================================
// Thermal Aurora 6-Stop Heat Palette (Hot to Cool Blue)
// ============================================================================
val ThermalHotCrimson = Color(0xFFFF4D6D)    // Hot 1.0
val ThermalWarmOrange = Color(0xFFFF8A3D)    // Warm-Hot 0.8
val ThermalAmberGold = Color(0xFFFFB454)     // Moderate-Warm 0.6
val ThermalSoftViolet = Color(0xFF5E8BFF)    // Cool Sapphire Blue 0.4
val ThermalCoolTeal = Color(0xFF4CC9F0)      // Luminous Sky Blue 0.2
val ThermalCalmMint = Color(0xFF64DFDF)      // Coolest Ice Cyan-Blue 0.0

val ThermalPaletteCoolToHot = listOf(
    ThermalCalmMint,     // #64DFDF (0.0)
    ThermalCoolTeal,     // #4CC9F0 (0.2)
    ThermalSoftViolet,   // #5E8BFF (0.4)
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
 * Smoothly interpolates across the 6-stop palette from Cool Blue (0f) to Hot (1f).
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
 * Short chip label for the Home screen & Heat Map.
 */
fun thermalShortLabelForHeat(heatLevel: Float): String {
    val clamped = heatLevel.coerceIn(0f, 1f)
    return when {
        clamped < 0.22f -> "Cool blue calm"
        clamped < 0.42f -> "Cooling blue"
        clamped < 0.62f -> "Moderate heat"
        clamped < 0.80f -> "Warm heat"
        else -> "High heat"
    }
}

fun planetLabelForHeat(heatLevel: Float): String = thermalShortLabelForHeat(heatLevel)
