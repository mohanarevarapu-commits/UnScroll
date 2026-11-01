package com.example.unscroll.ui

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.unscroll.ui.theme.CalmGreenPrimary
import com.example.unscroll.ui.theme.CoolingPlanetSpacing
import com.example.unscroll.ui.theme.CoolingPlanetTheme
import com.example.unscroll.ui.theme.HeatCalmBlueGreen
import com.example.unscroll.ui.theme.HeatCoolSage
import com.example.unscroll.ui.theme.HeatPaleSand
import com.example.unscroll.ui.theme.HeatPeach
import com.example.unscroll.ui.theme.HeatRedOrange
import com.example.unscroll.ui.theme.planetColorForHeat
import com.example.unscroll.ui.theme.planetLabelForHeat
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Top-level destinations for the 3-tab bottom navigation bar.
 */
enum class CoolingPlanetTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Outlined.Home),
    HEAT_MAP("Heat Map", Icons.Outlined.DateRange),
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
 * Data model for an app item on the App Blocking screen.
 */
data class BlockableAppItem(
    val packageName: String,
    val appName: String,
    val subtitle: String,
    val isBlocked: Boolean
)

/**
 * Helper to mount the CoolingPlanet Compose app inside an Activity.
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
                CoolingPlanetTheme {
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
                CoolingPlanetTheme {
                    CalmBlockedScreen(
                        appName = appName,
                        heatLevel = 0.22f,
                        onBackToFocus = onBackToFocus
                    )
                }
            }
        }
    }
}

/**
 * Root Composable hosting the Bottom Navigation Bar (Home, Heat Map, Blocking),
 * full-screen Focus Session screen, and Calm Blocked screen with smooth transitions.
 */
@Composable
fun CoolingPlanetApp(
    initialStreakDays: Int = 5,
    onToggleAppBlocked: (String, String, Boolean) -> Unit = { _, _, _ -> }
) {
    var selectedTab by rememberSaveable { mutableStateOf(CoolingPlanetTab.HOME) }

    // State variable on Home Screen to test different heat levels (0.0f = Cool Blue-Green, 1.0f = Lava Red-Orange)
    var testHeatLevel by rememberSaveable { mutableFloatStateOf(0.68f) }

    // Active overlay screens (Focus Session or Calm Blocked Screen)
    var isFocusSessionActive by rememberSaveable { mutableStateOf(false) }
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

    // Overlay routing with smooth fade/scale transition
    AnimatedContent(
        targetState = Triple(isFocusSessionActive, blockedPreviewAppName, selectedTab),
        transitionSpec = {
            (fadeIn(animationSpec = tween(380, easing = FastOutSlowInEasing)) +
                scaleIn(initialScale = 0.98f, animationSpec = tween(380, easing = FastOutSlowInEasing)))
                .togetherWith(fadeOut(animationSpec = tween(260)))
        },
        label = "RootScreenTransition"
    ) { (focusActive, blockedApp, currentTab) ->
        when {
            blockedApp != null -> {
                CalmBlockedScreen(
                    appName = blockedApp,
                    heatLevel = (testHeatLevel * 0.45f).coerceIn(0.15f, 0.45f),
                    onBackToFocus = { blockedPreviewAppName = null }
                )
            }
            focusActive -> {
                FocusSessionScreen(
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
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        CoolingPlanetBottomBar(
                            selectedTab = currentTab,
                            onSelectTab = { selectedTab = it }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            CoolingPlanetTab.HOME -> {
                                HomeScreen(
                                    heatLevel = testHeatLevel,
                                    onHeatLevelChange = { testHeatLevel = it },
                                    streakDays = initialStreakDays.coerceAtLeast(1),
                                    onStartFocus = { isFocusSessionActive = true }
                                )
                            }
                            CoolingPlanetTab.HEAT_MAP -> {
                                HeatMapScreen(todayHeatLevel = testHeatLevel)
                            }
                            CoolingPlanetTab.BLOCKING -> {
                                AppBlockingScreen(
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
                }
            }
        }
    }
}

/**
 * Simple, calm 3-tab bottom navigation bar linking Home, Heat Map, and Blocking.
 */
@Composable
private fun CoolingPlanetBottomBar(
    selectedTab: CoolingPlanetTab,
    onSelectTab: (CoolingPlanetTab) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            CoolingPlanetTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                NavigationBarItem(
                    selected = selected,
                    onClick = { onSelectTab(tab) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label
                        )
                    },
                    label = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

// ============================================================================
// SHARED COMPONENT: THE COOLING PLANET ORB (SLOW PULSE + SOFT GLOW)
// ============================================================================

@Composable
fun CoolingPlanetOrb(
    heatLevel: Float,
    modifier: Modifier = Modifier,
    planetSize: Dp = 240.dp,
    contentDescriptionText: String = planetLabelForHeat(heatLevel),
    centerContent: @Composable () -> Unit = {}
) {
    val targetPlanetColor by animateColorAsState(
        targetValue = planetColorForHeat(heatLevel),
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "PlanetColorAnimation"
    )
    val rimColor by animateColorAsState(
        targetValue = planetColorForHeat((heatLevel - 0.18f).coerceAtLeast(0f)),
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "PlanetRimColorAnimation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "PlanetPulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PlanetPulseScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.44f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PlanetGlowAlpha"
    )

    Box(
        modifier = modifier
            .size(planetSize * 1.32f)
            .semantics { contentDescription = contentDescriptionText },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f
            val coreRadius = maxRadius * 0.72f

            // 1. Soft atmospheric outer radial glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        targetPlanetColor.copy(alpha = glowAlpha),
                        targetPlanetColor.copy(alpha = glowAlpha * 0.35f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius
                ),
                radius = maxRadius,
                center = center
            )

            // 2. Main spherical planet body with soft 3D lighting gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.32f),
                        targetPlanetColor,
                        rimColor
                    ),
                    center = Offset(
                        x = center.x - coreRadius * 0.28f,
                        y = center.y - coreRadius * 0.28f
                    ),
                    radius = coreRadius * 1.35f
                ),
                radius = coreRadius,
                center = center
            )

            // 3. Subtle planet core color fill to keep rich saturation
            drawCircle(
                color = targetPlanetColor.copy(alpha = 0.72f),
                radius = coreRadius,
                center = center
            )

            // 4. Soft crescent highlight on top-left
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.28f),
                        Color.Transparent
                    ),
                    center = Offset(
                        x = center.x - coreRadius * 0.32f,
                        y = center.y - coreRadius * 0.32f
                    ),
                    radius = coreRadius * 0.65f
                ),
                radius = coreRadius,
                center = center
            )

            // 5. Delicate atmospheric ring border
            drawCircle(
                color = Color.White.copy(alpha = 0.28f),
                radius = coreRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Optional content inside the planet (e.g., countdown timer)
        centerContent()
    }
}

// ============================================================================
// 2. HOME SCREEN
// ============================================================================

@Composable
fun HomeScreen(
    heatLevel: Float,
    onHeatLevelChange: (Float) -> Unit,
    streakDays: Int,
    onStartFocus: () -> Unit
) {
    // Derive screen time from heatLevel so testing the slider updates screen time realistically
    val totalMinutesToday = (15 + (heatLevel * 225).roundToInt())
    val hours = totalMinutesToday / 60
    val mins = totalMinutesToday % 60
    val formattedScreenTime = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    val heatStatusLabel = planetLabelForHeat(heatLevel)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = CoolingPlanetSpacing.lg, vertical = CoolingPlanetSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Minimal Header
        Text(
            text = "UnScroll",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Scroll less. Cool your planet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(CoolingPlanetSpacing.md))

        // Large Circular "Cooling Planet" in the center
        CoolingPlanetOrb(
            heatLevel = heatLevel,
            planetSize = 220.dp,
            contentDescriptionText = "Cooling planet. $heatStatusLabel. Screen time today $formattedScreenTime"
        )

        // Accessible thermal state pill
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(planetColorForHeat(heatLevel))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = heatStatusLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(CoolingPlanetSpacing.lg))

        // Today's Total Screen Time & Streak Counter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CoolingPlanetSpacing.md)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formattedScreenTime,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Today's screen time",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$streakDays Days",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mindful streak",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(CoolingPlanetSpacing.lg))

        // Calm Green "Start focus" Button (24dp rounded corners, white text)
        Button(
            onClick = onStartFocus,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CalmGreenPrimary,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Start focus",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(CoolingPlanetSpacing.lg))

        // Interactive State Tester Card so the user can test different heat levels
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Test Heat Level",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${(heatLevel * 100).roundToInt()}% heat",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Slider(
                    value = heatLevel,
                    onValueChange = onHeatLevelChange,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = planetColorForHeat(heatLevel),
                        activeTrackColor = planetColorForHeat(heatLevel)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Cool Blue-Green",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Lava Red-Orange",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(CoolingPlanetSpacing.md))
    }
}

// ============================================================================
// 3. FOCUS SESSION SCREEN
// ============================================================================

@Composable
fun FocusSessionScreen(
    startingHeatLevel: Float,
    onSessionFinished: (Float) -> Unit,
    onEndSession: () -> Unit
) {
    // Allow 15m, 25m, 45m, plus a quick 10-second demo option to preview the cooling & completion animation
    val durationOptionsSeconds = listOf(10, 15 * 60, 25 * 60, 45 * 60)
    val durationLabels = listOf("10s Demo", "15 min", "25 min", "45 min")

    var selectedDurationIndex by rememberSaveable { mutableIntStateOf(2) } // Default 25 min
    val totalSeconds = durationOptionsSeconds[selectedDurationIndex]
    var remainingSeconds by rememberSaveable(selectedDurationIndex) { mutableIntStateOf(totalSeconds) }
    var isCompleted by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(selectedDurationIndex, isCompleted) {
        if (!isCompleted) {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds -= 1
            }
            isCompleted = true
        }
    }

    // As the countdown progresses, the planet slowly cools from startingHeatLevel down to 0f (Calm Blue-Green)
    val progress = if (totalSeconds > 0) {
        ((totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 1f
    val currentSessionHeat = if (isCompleted) {
        0f
    } else {
        (startingHeatLevel * (1f - progress)).coerceIn(0f, 1f)
    }

    // Gentle completion scale & ring animation when session finishes
    val completionRippleScale by animateFloatAsState(
        targetValue = if (isCompleted) 1.18f else 0.85f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "CompletionRippleScale"
    )
    val completionRippleAlpha by animateFloatAsState(
        targetValue = if (isCompleted) 0.45f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = LinearEasing),
        label = "CompletionRippleAlpha"
    )

    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val formattedCountdown = String.format(Locale.US, "%02d:%02d", mins, secs)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = CoolingPlanetSpacing.lg, vertical = CoolingPlanetSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status & Duration Selector Chips
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isCompleted) "Planet Cooled" else "Focus Session",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isCompleted) {
                        "Wonderful focus. Your planet is calm and restored."
                    } else {
                        "Your planet slowly cools with every quiet minute."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(CoolingPlanetSpacing.md))

                if (!isCompleted) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        durationLabels.forEachIndexed { index, label ->
                            FilterChip(
                                selected = selectedDurationIndex == index,
                                onClick = {
                                    selectedDurationIndex = index
                                    remainingSeconds = durationOptionsSeconds[index]
                                    isCompleted = false
                                },
                                label = { Text(label) },
                                shape = RoundedCornerShape(24.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // Center Planet with Countdown Timer inside
            Box(contentAlignment = Alignment.Center) {
                if (isCompleted) {
                    // Gentle completion halo ring
                    Box(
                        modifier = Modifier
                            .size(290.dp)
                            .graphicsLayer {
                                scaleX = completionRippleScale
                                scaleY = completionRippleScale
                                alpha = completionRippleAlpha
                            }
                            .border(
                                width = 3.dp,
                                color = HeatCalmBlueGreen,
                                shape = CircleShape
                            )
                    )
                }

                CoolingPlanetOrb(
                    heatLevel = currentSessionHeat,
                    planetSize = 250.dp,
                    contentDescriptionText = if (isCompleted) {
                        "Focus session completed. Planet cooled to calm blue-green."
                    } else {
                        "Focus timer $formattedCountdown remaining. ${planetLabelForHeat(currentSessionHeat)}"
                    }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Complete",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = formattedCountdown,
                                style = MaterialTheme.typography.displayLarge,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "cooling...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.88f)
                            )
                        }
                    }
                }
            }

            // Bottom Minimal "End session" or "Done" Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isCompleted) {
                    Button(
                        onClick = { onSessionFinished(0.15f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CalmGreenPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Back to Home",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onEndSession,
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(52.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = "End session",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 4. HEAT MAP SCREEN (LAST 4 WEEKS = 28 DAYS GRID + TAP FOR DAY TOTAL CARD)
// ============================================================================

@Composable
fun HeatMapScreen(todayHeatLevel: Float) {
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    // Generate deterministic 28-day history for the last 4 weeks ending today
    val records = remember(todayHeatLevel) {
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.US)
        val dayNumFormat = SimpleDateFormat("d", Locale.US)
        val sampleHeats = floatArrayOf(
            0.88f, 0.76f, 0.65f, 0.72f, 0.82f, 0.92f, 0.60f, // Week 1 (warmer)
            0.68f, 0.54f, 0.48f, 0.62f, 0.58f, 0.74f, 0.45f, // Week 2 (moderating)
            0.42f, 0.35f, 0.28f, 0.50f, 0.38f, 0.44f, 0.22f, // Week 3 (cooling)
            0.30f, 0.24f, 0.18f, 0.32f, 0.20f, 0.14f, todayHeatLevel // Week 4 (cool + today)
        )

        cal.add(Calendar.DAY_OF_YEAR, -27)
        List(28) { idx ->
            val heat = sampleHeats[idx].coerceIn(0f, 1f)
            val mins = (20 + (heat * 210).roundToInt())
            val record = DayHeatRecord(
                dayIndex = idx,
                dateLabel = if (idx == 27) "Today • ${dateFormat.format(cal.time)}" else dateFormat.format(cal.time),
                dayOfMonth = dayNumFormat.format(cal.time),
                minutesUsed = mins,
                heatLevel = heat
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
            record
        }
    }

    var selectedDayIndex by rememberSaveable { mutableIntStateOf(27) }
    val selectedDay = records[selectedDayIndex.coerceIn(0, records.lastIndex)]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = CoolingPlanetSpacing.lg, vertical = CoolingPlanetSpacing.md)
    ) {
        Text(
            text = "4-Week Heat Map",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tap any day to inspect that day's screen time total.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(CoolingPlanetSpacing.lg))

        // 4-Week Grid Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Day-of-week column headers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    daysOfWeek.forEach { dayName ->
                        Text(
                            text = dayName,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4 rows x 7 columns of soft rounded squares
                for (week in 0 until 4) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (dayCol in 0 until 7) {
                            val index = week * 7 + dayCol
                            val dayRecord = records[index]
                            val isSelected = index == selectedDayIndex
                            val cellColor = planetColorForHeat(dayRecord.heatLevel)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(cellColor)
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(
                                                width = 2.5.dp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .clickable { selectedDayIndex = index },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayRecord.dayOfMonth,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Heat-to-Cool Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cool (Low)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            HeatCalmBlueGreen,
                            HeatCoolSage,
                            HeatPaleSand,
                            HeatPeach,
                            HeatRedOrange
                        ).forEach { swatchColor ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(swatchColor)
                            )
                        }
                    }
                    Text(
                        text = "Hot (High)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(CoolingPlanetSpacing.lg))

        // Selected Day Detail Card
        AnimatedVisibility(visible = true) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(planetColorForHeat(selectedDay.heatLevel))
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = selectedDay.dateLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = planetLabelForHeat(selectedDay.heatLevel),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = selectedDay.formattedTotal,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// ============================================================================
// 5. APP BLOCKING SCREEN + CALM "BLOCKED" SCREEN
// ============================================================================

@Composable
fun AppBlockingScreen(
    apps: List<BlockableAppItem>,
    onToggleApp: (Int, Boolean) -> Unit,
    onPreviewBlockedScreen: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = CoolingPlanetSpacing.lg, vertical = CoolingPlanetSpacing.md)
    ) {
        Text(
            text = "App Blocking",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Choose distracting apps to quiet during focus sessions.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(CoolingPlanetSpacing.md))

        // Button to preview the Calm Blocked Screen directly
        OutlinedButton(
            onClick = {
                val firstBlocked = apps.firstOrNull { it.isBlocked }?.appName ?: "Instagram"
                onPreviewBlockedScreen(firstBlocked)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = "Preview Calm \"Blocked\" Screen",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(CoolingPlanetSpacing.md))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(apps.size) { index ->
                val app = apps[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPreviewBlockedScreen(app.appName) }
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.appName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (app.isBlocked) "Quiet mode enabled • Tap to preview" else app.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Switch(
                            checked = app.isBlocked,
                            onCheckedChange = { checked -> onToggleApp(index, checked) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CalmGreenPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Calm "Blocked" Screen showing the cooling planet, a short gentle message,
 * and a green "Back to focus" button.
 */
@Composable
fun CalmBlockedScreen(
    appName: String,
    heatLevel: Float = 0.20f,
    onBackToFocus: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = CoolingPlanetSpacing.xl, vertical = CoolingPlanetSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CoolingPlanetOrb(
                    heatLevel = heatLevel,
                    planetSize = 220.dp,
                    contentDescriptionText = "Cooling planet resting. $appName is quiet right now."
                )

                Spacer(modifier = Modifier.height(CoolingPlanetSpacing.lg))

                Text(
                    text = "$appName is resting right now",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Take a slow breath. Every moment away from the feed helps your planet cool.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = onBackToFocus,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CalmGreenPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Back to focus",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }
    }
}
