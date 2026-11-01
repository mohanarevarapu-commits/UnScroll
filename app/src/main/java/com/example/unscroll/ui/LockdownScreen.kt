package com.example.unscroll.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unscroll.session.ScreenLockHelper
import com.example.unscroll.session.SessionManager
import com.example.unscroll.ui.theme.AuroraGreenButton
import com.example.unscroll.ui.theme.AuroraTextPrimary
import com.example.unscroll.ui.theme.AuroraTextSecondary
import com.example.unscroll.ui.theme.CoolingPlanetSpacing
import com.example.unscroll.ui.theme.GlassBorderStroke
import com.example.unscroll.ui.theme.GlassSurfaceFill
import com.example.unscroll.ui.theme.GlassSurfaceFillActive
import com.example.unscroll.ui.theme.ManropeFontFamily
import com.example.unscroll.ui.theme.ThermalCalmMint
import com.example.unscroll.ui.theme.ThermalCoolTeal
import com.example.unscroll.ui.theme.ThermalHotCrimson
import com.example.unscroll.ui.theme.ThermalUppercaseLabelStyle
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Setup screen for Phone Lockdown Mode (15 minutes to 5 hours):
 * - Presets: 15m, 30m, 1h, 2h, 3h, 5h (+ 15s Demo for instant testing)
 * - Continuous slider from 15 mins to 300 mins (5 hours)
 * - Immediately locks the device hardware screen when enabled while allowing Incoming Calls & Emergency SOS
 */
@Composable
fun ThermalLockdownSetupScreen(
    onStartLockdownSeconds: (Int) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var canLockHardware by rememberSaveable {
        mutableStateOf(ScreenLockHelper.canLockHardwareScreen(context))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canLockHardware = ScreenLockHelper.canLockHardwareScreen(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val presetLabels = listOf("15s Demo", "15m", "30m", "1h", "2h", "3h", "5h")
    val presetSeconds = listOf(
        15,
        15 * 60,
        30 * 60,
        60 * 60,
        120 * 60,
        180 * 60,
        300 * 60
    )

    var selectedDurationSeconds by rememberSaveable { mutableIntStateOf(60 * 60) } // Default 1 hour

    val isDemo = selectedDurationSeconds < 15 * 60
    val durationMinutes = (selectedDurationSeconds / 60).coerceIn(15, 300)
    val formattedSelectedDuration = if (isDemo) {
        "${selectedDurationSeconds}s Demo"
    } else {
        val h = durationMinutes / 60
        val m = durationMinutes % 60
        when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0 -> "${h}h 00m"
            else -> "${m} mins"
        }
    }

    AuroraBackground(heatLevel = 0.18f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = CoolingPlanetSpacing.lg, vertical = CoolingPlanetSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "UNSCROLL · STRICT LOCKDOWN",
                    style = ThermalUppercaseLabelStyle,
                    color = AuroraTextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Phone Lockdown",
                    style = MaterialTheme.typography.headlineLarge,
                    color = AuroraTextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Locks your phone screen from 15 mins to 5 hours",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuroraTextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Big Selected Duration Display + Slider + Preset Chips
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LOCKDOWN DURATION",
                        style = ThermalUppercaseLabelStyle,
                        color = AuroraTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = formattedSelectedDuration,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.Light,
                            fontSize = 64.sp,
                            lineHeight = 70.sp
                        ),
                        color = AuroraTextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Slider for 15 mins to 300 mins (5 hours)
                    Slider(
                        value = durationMinutes.toFloat(),
                        onValueChange = { newMins ->
                            // Snap to 5-minute increments
                            val snapped = ((newMins / 5f).roundToInt() * 5).coerceIn(15, 300)
                            selectedDurationSeconds = snapped * 60
                        },
                        valueRange = 15f..300f,
                        colors = SliderDefaults.colors(
                            thumbColor = ThermalCoolTeal,
                            activeTrackColor = ThermalCoolTeal,
                            inactiveTrackColor = AuroraTextSecondary.copy(alpha = 0.24f)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "15 mins",
                            style = MaterialTheme.typography.labelMedium,
                            color = AuroraTextSecondary
                        )
                        Text(
                            text = "5 hours",
                            style = MaterialTheme.typography.labelMedium,
                            color = AuroraTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preset Pills Row 1 & Row 2
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            presetLabels.take(4).forEachIndexed { index, label ->
                                val targetSecs = presetSeconds[index]
                                LockdownPresetPill(
                                    label = label,
                                    selected = selectedDurationSeconds == targetSecs,
                                    onClick = { selectedDurationSeconds = targetSecs }
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            presetLabels.drop(4).forEachIndexed { idx, label ->
                                val targetSecs = presetSeconds[idx + 4]
                                LockdownPresetPill(
                                    label = label,
                                    selected = selectedDurationSeconds == targetSecs,
                                    onClick = { selectedDurationSeconds = targetSecs }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Safety & Rules GlassCard
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Text(
                    text = "HOW LOCKDOWN WORKS",
                    style = ThermalUppercaseLabelStyle,
                    color = AuroraTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                LockdownFeatureRow(
                    dotColor = ThermalCoolTeal,
                    title = "Locks Screen Immediately",
                    subtitle = "Enabling Lockdown turns off & locks your screen until the countdown ends."
                )

                Spacer(modifier = Modifier.height(10.dp))

                LockdownFeatureRow(
                    dotColor = AuroraGreenButton,
                    title = "Incoming Calls Always Work",
                    subtitle = "If someone calls you, your phone rings normally so you can pick up."
                )

                Spacer(modifier = Modifier.height(10.dp))

                LockdownFeatureRow(
                    dotColor = ThermalHotCrimson,
                    title = "Emergency SOS & Dialer Ready",
                    subtitle = "Instant access to Emergency SOS (112 / 911) and Phone Dialer on screen."
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Hardware Screen Lock Permission Helper (if neither Accessibility nor Device Admin is enabled yet)
            if (!canLockHardware) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                    onClick = { ScreenLockHelper.requestDeviceAdminPermission(context) }
                ) {
                    Text(
                        text = "HARDWARE SCREEN LOCK PERMISSION",
                        style = ThermalUppercaseLabelStyle,
                        color = ThermalCoolTeal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap here to enable Device Admin so UnScroll can physically turn off & lock your screen.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuroraTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AuroraPrimaryButton(
                    text = "Lockdown Phone ($formattedSelectedDuration)",
                    onClick = {
                        onStartLockdownSeconds(selectedDurationSeconds)
                        // Immediately lock the hardware screen when Lockdown is enabled
                        ScreenLockHelper.lockDeviceScreenNow(context)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                GlassOutlineButton(
                    text = "Back to Home",
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(0.65f)
                )
            }
        }
    }
}

@Composable
private fun LockdownPresetPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val pillShape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .clip(pillShape)
            .background(
                if (selected) GlassSurfaceFillActive else GlassSurfaceFill,
                pillShape
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (selected) ThermalCoolTeal.copy(alpha = 0.75f) else GlassBorderStroke
                ),
                pillShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) AuroraTextPrimary else AuroraTextSecondary
        )
    }
}

@Composable
private fun LockdownFeatureRow(
    dotColor: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Canvas(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(12.dp)
        ) {
            drawCircle(
                color = dotColor,
                radius = size.minDimension / 2f
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = AuroraTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = AuroraTextSecondary
            )
        }
    }
}

/**
 * Active Full-Screen Phone Lockdown Screen:
 * - Immediately locks the physical device screen and enters full-screen immersive lockdown mode
 * - Intercepts Back button presses so user cannot exit until timer reaches 00:00
 * - Displays remaining time in HH:MM:SS or MM:SS (Manrope Light)
 * - Allows only Emergency SOS call (tel:112), Phone Dialer, and immediate Re-Lock Screen
 */
@Composable
fun ThermalActiveLockdownScreen(
    totalDurationSeconds: Int,
    onLockdownCompleted: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val sessionManager = SessionManager.getInstance(context)

    var remainingSeconds by rememberSaveable {
        val fromPrefs = (sessionManager.lockdownRemainingMillis / 1000L).toInt()
        mutableIntStateOf(if (fromPrefs > 0) fromPrefs else totalDurationSeconds)
    }
    var isCompleted by rememberSaveable { mutableStateOf(false) }

    // Block hardware/gesture back press while Lockdown is active
    BackHandler(enabled = !isCompleted) {
        // Re-lock screen if user presses back during active lockdown
        ScreenLockHelper.lockDeviceScreenNow(context)
    }

    // Enter full-screen immersive lockdown mode & lock hardware screen when started
    DisposableEffect(isCompleted) {
        if (!isCompleted) {
            ScreenLockHelper.setImmersiveLockdownMode(activity, true)
            ScreenLockHelper.lockDeviceScreenNow(context)
        } else {
            ScreenLockHelper.setImmersiveLockdownMode(activity, false)
        }
        onDispose {
            if (!sessionManager.isLockdownActive) {
                ScreenLockHelper.setImmersiveLockdownMode(activity, false)
            }
        }
    }

    LaunchedEffect(isCompleted) {
        if (!isCompleted) {
            while (remainingSeconds > 0) {
                delay(1000L)
                val syncedRemaining = (sessionManager.lockdownRemainingMillis / 1000L).toInt()
                remainingSeconds = if (syncedRemaining > 0) {
                    syncedRemaining
                } else {
                    (remainingSeconds - 1).coerceAtLeast(0)
                }
            }
            sessionManager.clearExpiredLockdown()
            isCompleted = true
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "LockdownPulseTransition")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LockdownPulseGlow"
    )

    val hours = remainingSeconds / 3600
    val mins = (remainingSeconds % 3600) / 60
    val secs = remainingSeconds % 60
    val formattedTime = if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs)
    } else {
        String.format(Locale.US, "%02d:%02d", mins, secs)
    }

    AuroraBackground(
        heatLevel = 0.08f,
        shimmerBoost = if (isCompleted) pulseGlow else 0.15f
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = CoolingPlanetSpacing.lg, vertical = CoolingPlanetSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Header + Chip
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isCompleted) "PHONE LOCKDOWN · COMPLETE" else "PHONE LOCKDOWN · SCREEN LOCKED",
                    style = ThermalUppercaseLabelStyle,
                    color = AuroraTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                GlassChip(
                    label = if (isCompleted) {
                        "Device unlocked · Well done"
                    } else {
                        "Screen Locked · Incoming calls & SOS only"
                    },
                    dotColor = if (isCompleted) ThermalCalmMint else ThermalCoolTeal
                )
            }

            // Center Glowing Lock Shield + Countdown Timer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(112.dp)
                        .clickable(enabled = !isCompleted) {
                            ScreenLockHelper.lockDeviceScreenNow(context)
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val r = size.minDimension / 2f
                        val c = Offset(size.width / 2f, size.height / 2f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    ThermalCoolTeal.copy(alpha = 0.38f * pulseGlow),
                                    ThermalCoolTeal.copy(alpha = 0.12f * pulseGlow),
                                    Color.Transparent
                                ),
                                center = c,
                                radius = r
                            ),
                            radius = r,
                            center = c
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(GlassSurfaceFill, CircleShape)
                            .border(BorderStroke(1.dp, GlassBorderStroke), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Phone Locked",
                            tint = if (isCompleted) ThermalCalmMint else ThermalCoolTeal,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isCompleted) "00:00" else formattedTime,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = if (hours > 0) 74.sp else 92.sp,
                        lineHeight = if (hours > 0) 80.sp else 98.sp
                    ),
                    color = AuroraTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isCompleted) {
                        "Your lockdown session has finished."
                    } else {
                        "Screen is locked · Tap lock icon to turn screen off immediately"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuroraTextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // Bottom Section: Emergency SOS + Phone Dialer (or Return Home when finished)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Text(
                        text = if (isCompleted) {
                            "You stayed disconnected and present. All apps and your home screen are now restored."
                        } else {
                            "Incoming phone calls will ring normally and can be answered. Use the buttons below for Emergency SOS or urgent phone calls."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuroraTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (isCompleted) {
                    AuroraPrimaryButton(
                        text = "Unlock & Return Home",
                        onClick = onLockdownCompleted
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Emergency SOS Button (tel:112)
                        EmergencyActionGlassButton(
                            modifier = Modifier.weight(1f),
                            label = "Emergency SOS",
                            sublabel = "Dial 112 / SOS",
                            accentColor = ThermalHotCrimson,
                            isEmergency = true,
                            onClick = { openEmergencyDialer(context) }
                        )

                        // Phone Dialer Button (Incoming/Outgoing Calls)
                        EmergencyActionGlassButton(
                            modifier = Modifier.weight(1f),
                            label = "Phone Calls",
                            sublabel = "Open Dialer",
                            accentColor = ThermalCoolTeal,
                            isEmergency = false,
                            onClick = { openPhoneDialer(context) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmergencyActionGlassButton(
    modifier: Modifier = Modifier,
    label: String,
    sublabel: String,
    accentColor: Color,
    isEmergency: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(GlassSurfaceFill, shape)
            .border(BorderStroke(1.dp, accentColor.copy(alpha = 0.55f)), shape)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isEmergency) Icons.Outlined.Warning else Icons.Outlined.Call,
            contentDescription = label,
            tint = accentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = AuroraTextPrimary
        )
        Text(
            text = sublabel,
            style = MaterialTheme.typography.labelMedium,
            color = AuroraTextSecondary
        )
    }
}

private fun openEmergencyDialer(context: Context) {
    try {
        val emergencyIntent = Intent("com.android.phone.EmergencyDialer.DIAL").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(emergencyIntent)
    } catch (_: Exception) {
        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(dialIntent)
    }
}

private fun openPhoneDialer(context: Context) {
    try {
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(dialIntent)
    } catch (_: Exception) {
        // No dialer available on device
    }
}
