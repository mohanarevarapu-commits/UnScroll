package com.example.unscroll.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.unscroll.R

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val manropeGoogleFont = GoogleFont("Manrope")

val ManropeFontFamily = FontFamily(
    Font(googleFont = manropeGoogleFont, fontProvider = fontProvider, weight = FontWeight.Light),
    Font(googleFont = manropeGoogleFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = manropeGoogleFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = manropeGoogleFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = manropeGoogleFont, fontProvider = fontProvider, weight = FontWeight.Bold)
)

/**
 * Reusable style for small uppercase section labels with wide letter spacing (~2.5sp).
 */
val ThermalUppercaseLabelStyle = TextStyle(
    fontFamily = ManropeFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 2.5.sp,
    color = AuroraTextSecondary
)

val ThermalAuroraTypography = Typography(
    // 100sp Light weight (300) for Focus Session countdown timer
    displayLarge = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 100.sp,
        lineHeight = 108.sp,
        letterSpacing = (-2.0).sp
    ),
    // 86sp Light weight (300) for Home screen hero time ("2h 48m")
    displayMedium = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 86.sp,
        lineHeight = 94.sp,
        letterSpacing = (-1.5).sp
    ),
    // Big light headline for Blocked screen ("Instagram is resting right now")
    displaySmall = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 38.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp
    ),
    labelLarge = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp
    ),
    // Small uppercase labels with wide letter spacing (~2.5sp)
    labelMedium = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 2.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 2.2.sp
    )
)

val CoolingPlanetTypography = ThermalAuroraTypography
val Typography = ThermalAuroraTypography
