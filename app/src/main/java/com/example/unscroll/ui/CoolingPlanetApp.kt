package com.example.unscroll.ui

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import com.example.unscroll.session.SessionManager
import com.example.unscroll.ui.theme.ThermalAuroraTheme

/**
 * Top-level destinations for the 3-tab floating glass bottom navigation bar.
 */
enum class CoolingPlanetTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Outlined.Home),
    HEAT_MAP("Heat map", Icons.Outlined.DateRange),
    BLOCKING("Blocking", Icons.Outlined.Lock)
}

/**
 * Data model for each day in the 4-week (28-day) Heat Map grid.
 */
data class DayHeatRecord(
    val dayIndex: Int,
    val dateLabel: String,
    val dayOfMonth: String,
    val minutesUsed: Int,
    val heatLevel: Float
) {
    val formattedTotal: String
        get() {
            val hours = minutesUsed / 60
            val mins = minutesUsed % 60
            return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
        }
}

/**
 * Data model for an app row on the App Blocking screen.
 */
data class BlockableAppItem(
    val packageName: String,
    val appName: String,
    val subtitle: String,
    val isBlocked: Boolean
)

/**
 * Helper to mount the Thermal Aurora Compose app inside an Activity.
 */
object CoolingPlanetHost {
    @JvmStatic
    fun createMainComposeView(
        activity: Activity,
        initialStreakDays: Int,
        onToggleAppBlocked: (String, String, Boolean) -> Unit
    ): ComposeView {
        return ComposeView(activity).apply {
            setContent {
                ThermalAuroraTheme {
                    CoolingPlanetApp(
                        initialStreakDays = initialStreakDays,
                        onToggleAppBlocked = onToggleAppBlocked
                    )
                }
            }
        }
    }

    @JvmStatic
    fun createBlockedOverlayComposeView(
        activity: Activity,
        appName: String,
        onBackToFocus: () -> Unit
    ): ComposeView {
        return ComposeView(activity).apply {
            setContent {
                ThermalAuroraTheme {
                    ThermalBlockedScreen(
                        appName = appName,
                        onBackToFocus = onBackToFocus
                    )
                }
            }
        }
    }

    @JvmStatic
    fun createLockdownOverlayComposeView(
        activity: Activity,
        onLockdownCompleted: () -> Unit
    ): ComposeView {
        val sessionManager = SessionManager.getInstance(activity)
        val remainingSeconds = ((sessionManager.lockdownRemainingMillis / 1000L).toInt())
            .coerceAtLeast(15)
        return ComposeView(activity).apply {
            setContent {
                ThermalAuroraTheme {
                    ThermalActiveLockdownScreen(
                        totalDurationSeconds = remainingSeconds,
                        onLockdownCompleted = onLockdownCompleted
                    )
                }
            }
        }
    }
}

/**
 * Root Composable for the "Thermal Aurora" UnScroll experience:
 * - Full-screen AuroraBackground with 3 drifting blurred blobs reacting to `testHeatLevel`
 * - Floating 68dp glass bottom navigation bar (Home, Heat map, Blocking)
 * - Smooth transitions between tabs, Focus Session screen, Phone Lockdown (15m-5h), and Blocked screen
 */
@Composable
fun CoolingPlanetApp(
    initialStreakDays: Int = 5,
    onToggleAppBlocked: (String, String, Boolean) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context) }

    var selectedTab by rememberSaveable { mutableStateOf(CoolingPlanetTab.HOME) }

    // State variable to test different heat levels (0.28f = Cool blue light & shade by default)
    var testHeatLevel by rememberSaveable { mutableFloatStateOf(0.28f) }

    // Active full-screen overlays
    var isFocusSessionActive by rememberSaveable { mutableStateOf(false) }
    var isLockdownSetupOpen by rememberSaveable { mutableStateOf(false) }
    var isLockdownActive by rememberSaveable { mutableStateOf(sessionManager.isLockdownActive) }
    var lockdownDurationSeconds by rememberSaveable {
        mutableIntStateOf(sessionManager.lockdownDurationSeconds)
    }
    var blockedPreviewAppName by rememberSaveable { mutableStateOf<String?>(null) }

    // App blocking list state
    val blockedApps = remember {
        mutableStateListOf(
            BlockableAppItem("com.instagram.android", "Instagram", "Reels & feed quieted during focus", true),
            BlockableAppItem("com.google.android.youtube", "YouTube & Shorts", "Short-form video feed paused", true),
            BlockableAppItem("com.snapchat.android", "Snapchat", "Stories & Spotlight quieted", true),
            BlockableAppItem("com.whatsapp", "WhatsApp", "Mute non-urgent group chatter", false),
            BlockableAppItem("com.facebook.katana", "Facebook", "Infinite scroll feed paused", true),
            BlockableAppItem("com.flipkart.android", "Flipkart", "Promotional deal alerts snoozed", true),
            BlockableAppItem("in.swiggy.android", "Swiggy", "Food promo notifications quieted", true),
            BlockableAppItem("com.application.zomato", "Zomato", "Promo notifications snoozed", false)
        )
    }

    val overlayKey = when {
        isLockdownActive -> "LOCKDOWN_ACTIVE"
        isLockdownSetupOpen -> "LOCKDOWN_SETUP"
        blockedPreviewAppName != null -> "BLOCKED_$blockedPreviewAppName"
        isFocusSessionActive -> "FOCUS_ACTIVE"
        else -> "TAB_${selectedTab.name}"
    }

    AnimatedContent(
        targetState = overlayKey,
        transitionSpec = {
            (fadeIn(animationSpec = tween(380, easing = FastOutSlowInEasing)) +
                scaleIn(initialScale = 0.98f, animationSpec = tween(380, easing = FastOutSlowInEasing)))
                .togetherWith(fadeOut(animationSpec = tween(260)))
        },
        label = "ThermalAuroraScreenTransition"
    ) { stateKey ->
        when {
            stateKey == "LOCKDOWN_ACTIVE" -> {
                ThermalActiveLockdownScreen(
                    totalDurationSeconds = lockdownDurationSeconds,
                    onLockdownCompleted = {
                        sessionManager.clearExpiredLockdown()
                        isLockdownActive = false
                        testHeatLevel = 0.12f
                    }
                )
            }
            stateKey == "LOCKDOWN_SETUP" -> {
                ThermalLockdownSetupScreen(
                    onStartLockdownSeconds = { durationSecs ->
                        sessionManager.startLockdownSeconds(durationSecs)
                        lockdownDurationSeconds = durationSecs
                        isLockdownSetupOpen = false
                        isLockdownActive = true
                    },
                    onCancel = {
                        isLockdownSetupOpen = false
                    }
                )
            }
            stateKey.startsWith("BLOCKED_") -> {
                ThermalBlockedScreen(
                    appName = blockedPreviewAppName ?: "Instagram",
                    onBackToFocus = { blockedPreviewAppName = null }
                )
            }
            stateKey == "FOCUS_ACTIVE" -> {
                ThermalFocusSessionScreen(
                    startingHeatLevel = testHeatLevel,
                    onSessionFinished = { cooledHeat ->
                        testHeatLevel = cooledHeat
                        isFocusSessionActive = false
                    },
                    onEndSession = {
                        isFocusSessionActive = false
                    }
                )
            }
            else -> {
                val backgroundHeat = when (selectedTab) {
                    CoolingPlanetTab.HOME -> testHeatLevel
                    CoolingPlanetTab.HEAT_MAP -> (testHeatLevel * 0.65f).coerceIn(0.18f, 0.75f)
                    CoolingPlanetTab.BLOCKING -> 0.16f
                }

                AuroraBackground(heatLevel = backgroundHeat) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            when (selectedTab) {
                                CoolingPlanetTab.HOME -> {
                                    ThermalHomeScreen(
                                        heatLevel = testHeatLevel,
                                        onHeatLevelChange = { testHeatLevel = it },
                                        streakDays = initialStreakDays.coerceAtLeast(1),
                                        onStartFocus = { isFocusSessionActive = true },
                                        onOpenLockdown = { isLockdownSetupOpen = true }
                                    )
                                }
                                CoolingPlanetTab.HEAT_MAP -> {
                                    ThermalHeatMapScreen(todayHeatLevel = testHeatLevel)
                                }
                                CoolingPlanetTab.BLOCKING -> {
                                    ThermalAppBlockingScreen(
                                        apps = blockedApps,
                                        onToggleApp = { index, isChecked ->
                                            val item = blockedApps[index]
                                            blockedApps[index] = item.copy(isBlocked = isChecked)
                                            onToggleAppBlocked(item.packageName, item.appName, isChecked)
                                        },
                                        onPreviewBlockedScreen = { appName ->
                                            blockedPreviewAppName = appName
                                        }
                                    )
                                }
                            }
                        }

                        GlassFloatingBottomBar(
                            selectedTab = selectedTab,
                            onSelectTab = { selectedTab = it }
                        )
                    }
                }
            }
        }
    }
}
