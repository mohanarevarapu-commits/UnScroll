package com.example.unscroll

import com.example.unscroll.service.AppBlockerService
import com.example.unscroll.session.SessionManager
import com.example.unscroll.ui.BlockableAppItem
import com.example.unscroll.ui.DayHeatRecord
import com.example.unscroll.ui.theme.ThermalCalmMint
import com.example.unscroll.ui.theme.ThermalCoolTeal
import com.example.unscroll.ui.theme.ThermalHotCrimson
import com.example.unscroll.ui.theme.thermalColorForHeat
import com.example.unscroll.ui.theme.thermalShortLabelForHeat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Automated Unit Test Suite for UnScroll ("Thermal Aurora / Blue Light & Shade" + Phone Lockdown Mode).
 */
class UnScrollTestSuite {

    @Test
    fun lockdownDurationClamping_enforces15MinutesTo5HoursRange() {
        // Below minimum (< 15 mins) clamps to 15 mins
        assertEquals(15, SessionManager.clampLockdownMinutes(0))
        assertEquals(15, SessionManager.clampLockdownMinutes(5))
        assertEquals(15, SessionManager.clampLockdownMinutes(14))

        // Valid presets within [15m, 300m] (15 mins to 5 hours) remain unchanged
        assertEquals(15, SessionManager.clampLockdownMinutes(15))
        assertEquals(30, SessionManager.clampLockdownMinutes(30))
        assertEquals(60, SessionManager.clampLockdownMinutes(60))
        assertEquals(120, SessionManager.clampLockdownMinutes(120))
        assertEquals(180, SessionManager.clampLockdownMinutes(180))
        assertEquals(300, SessionManager.clampLockdownMinutes(300))

        // Above maximum (> 300 mins / 5 hours) clamps to 300 mins
        assertEquals(300, SessionManager.clampLockdownMinutes(360))
        assertEquals(300, SessionManager.clampLockdownMinutes(600))
    }

    @Test
    fun lockdownCallAndEmergencyAllowlist_allowsPhoneCallsAndEmergencyDialers() {
        // Incoming / outgoing call UI & dialers must be allowed during Phone Lockdown
        assertTrue(AppBlockerService.isPackageAllowedDuringLockdown("com.google.android.dialer"))
        assertTrue(AppBlockerService.isPackageAllowedDuringLockdown("com.android.incallui"))
        assertTrue(AppBlockerService.isPackageAllowedDuringLockdown("com.samsung.android.incallui"))
        assertTrue(AppBlockerService.isPackageAllowedDuringLockdown("com.samsung.android.dialer"))
        assertTrue(AppBlockerService.isPackageAllowedDuringLockdown("com.android.dialer"))
        assertTrue(AppBlockerService.isPackageAllowedDuringLockdown("com.android.server.telecom"))
        assertTrue(AppBlockerService.isPackageAllowedDuringLockdown("com.android.phone"))

        // Emergency SOS & Personal Safety packages must be allowed during Phone Lockdown
        assertTrue(AppBlockerService.isPackageAllowedDuringLockdown("com.android.emergency"))
        assertTrue(AppBlockerService.isPackageAllowedDuringLockdown("com.google.android.apps.safetyhub"))
    }

    @Test
    fun lockdownCallAndEmergencyAllowlist_blocksSocialAppsLaunchersAndBrowsers() {
        // Social, video, shopping, launcher, browser, and settings packages must be intercepted
        assertFalse(AppBlockerService.isPackageAllowedDuringLockdown("com.instagram.android"))
        assertFalse(AppBlockerService.isPackageAllowedDuringLockdown("com.google.android.youtube"))
        assertFalse(AppBlockerService.isPackageAllowedDuringLockdown("com.snapchat.android"))
        assertFalse(AppBlockerService.isPackageAllowedDuringLockdown("com.whatsapp"))
        assertFalse(AppBlockerService.isPackageAllowedDuringLockdown("com.facebook.katana"))
        assertFalse(AppBlockerService.isPackageAllowedDuringLockdown("com.flipkart.android"))
        assertFalse(AppBlockerService.isPackageAllowedDuringLockdown("in.swiggy.android"))
        assertFalse(AppBlockerService.isPackageAllowedDuringLockdown("com.google.android.apps.nexuslauncher"))
        assertFalse(AppBlockerService.isPackageAllowedDuringLockdown("com.android.chrome"))
        assertFalse(AppBlockerService.isPackageAllowedDuringLockdown("com.android.settings"))
    }

    @Test
    fun thermalPaletteInterpolation_mapsCoolBlueToHotCrimson() {
        // Exact stop endpoints
        assertEquals(ThermalCalmMint, thermalColorForHeat(0.0f))
        assertEquals(ThermalCoolTeal, thermalColorForHeat(0.2f))
        assertEquals(ThermalHotCrimson, thermalColorForHeat(1.0f))

        // Out-of-bounds clamping
        assertEquals(ThermalCalmMint, thermalColorForHeat(-0.5f))
        assertEquals(ThermalHotCrimson, thermalColorForHeat(1.5f))
    }

    @Test
    fun thermalShortLabels_returnAccessibleLabelsAcrossHeatSpectrum() {
        assertEquals("Cool blue calm", thermalShortLabelForHeat(0.10f))
        assertEquals("Cooling blue", thermalShortLabelForHeat(0.28f))
        assertEquals("Moderate heat", thermalShortLabelForHeat(0.50f))
        assertEquals("Warm heat", thermalShortLabelForHeat(0.72f))
        assertEquals("High heat", thermalShortLabelForHeat(0.90f))
    }

    @Test
    fun dayHeatRecord_formatsHoursAndMinutesCorrectly() {
        val underOneHour = DayHeatRecord(
            dayIndex = 0,
            dateLabel = "Mon, Oct 28",
            dayOfMonth = "28",
            minutesUsed = 45,
            heatLevel = 0.20f
        )
        assertEquals("45m", underOneHour.formattedTotal)

        val multiHour = DayHeatRecord(
            dayIndex = 27,
            dateLabel = "Today",
            dayOfMonth = "24",
            minutesUsed = 168,
            heatLevel = 0.82f
        )
        assertEquals("2h 48m", multiHour.formattedTotal)
    }

    @Test
    fun blockableAppItem_togglesBlockedStateImmutably() {
        val item = BlockableAppItem(
            packageName = "com.instagram.android",
            appName = "Instagram",
            subtitle = "Reels & feed quieted during focus",
            isBlocked = true
        )
        val unblocked = item.copy(isBlocked = false)
        assertTrue(item.isBlocked)
        assertFalse(unblocked.isBlocked)
        assertEquals("Instagram", unblocked.appName)
    }

    @Test
    fun emulatorScreenshots_arePresentAndValidPngFiles() {
        val projectRoot = java.io.File(System.getProperty("user.dir") ?: ".").let { dir ->
            if (dir.name == "app") dir.parentFile ?: dir else dir
        }
        val docsScreenshotsDir = java.io.File(projectRoot, "docs/screenshots")

        val expectedScreenshots = listOf(
            "01_home_screen.png",
            "02_focus_session.png",
            "03_phone_lockdown_setup.png",
            "04_phone_lockdown_active.png",
            "05_phone_lockdown_complete.png",
            "06_heat_map_screen.png",
            "07_app_blocking_screen.png",
            "08_blocked_overlay_screen.png"
        )

        expectedScreenshots.forEach { targetFileName ->
            val destFile = java.io.File(docsScreenshotsDir, targetFileName)
            assertTrue("Expected screenshot $targetFileName to exist in docs/screenshots", destFile.exists())
            assertTrue("Expected screenshot $targetFileName to be non-empty PNG", destFile.length() > 50_000L)
        }
    }
}
