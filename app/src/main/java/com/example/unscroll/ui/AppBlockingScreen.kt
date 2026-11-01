package com.example.unscroll.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unscroll.ui.theme.AuroraGreenButton
import com.example.unscroll.ui.theme.AuroraTextPrimary
import com.example.unscroll.ui.theme.AuroraTextSecondary
import com.example.unscroll.ui.theme.CoolingPlanetSpacing
import com.example.unscroll.ui.theme.ManropeFontFamily
import com.example.unscroll.ui.theme.OnAuroraGreenButton
import com.example.unscroll.ui.theme.ThermalCalmMint
import com.example.unscroll.ui.theme.ThermalUppercaseLabelStyle

/**
 * Thermal Aurora App Blocking Screen:
 * - Glass rows with app name, subtitle, and a toggle using #3DDC97 green when on
 * - Tap any row or "Preview Blocked Screen" button to view the calm Blocked screen
 */
@Composable
fun ThermalAppBlockingScreen(
    apps: List<BlockableAppItem>,
    onToggleApp: (Int, Boolean) -> Unit,
    onPreviewBlockedScreen: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = CoolingPlanetSpacing.lg, vertical = CoolingPlanetSpacing.xl)
    ) {
        Text(
            text = "QUIET MODE · APPS",
            style = ThermalUppercaseLabelStyle,
            color = AuroraTextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Blocking",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Light,
                fontSize = 36.sp
            ),
            color = AuroraTextPrimary
        )

        Spacer(modifier = Modifier.height(CoolingPlanetSpacing.md))

        GlassOutlineButton(
            text = "Preview Blocked Screen",
            onClick = {
                val firstActive = apps.firstOrNull { it.isBlocked }?.appName ?: "Instagram"
                onPreviewBlockedScreen(firstActive)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(CoolingPlanetSpacing.md))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            items(apps.size) { index ->
                val app = apps[index]
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    onClick = { onPreviewBlockedScreen(app.appName) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.appName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AuroraTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (app.isBlocked) "Quiet mode active · Tap to preview" else app.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AuroraTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Switch(
                            checked = app.isBlocked,
                            onCheckedChange = { checked -> onToggleApp(index, checked) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OnAuroraGreenButton,
                                checkedTrackColor = AuroraGreenButton,
                                uncheckedThumbColor = AuroraTextSecondary,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.14f),
                                uncheckedBorderColor = Color.White.copy(alpha = 0.24f)
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Thermal Aurora Calm Blocked Screen:
 * - Cool teal-green aurora (heatLevel = 0.05f)
 * - "Quiet mode" glass chip with glowing mint dot
 * - Big light-weight headline like "Instagram is resting right now"
 * - Short calm sentence
 * - Green "Back to focus" button (#3DDC97 background, #052A1A text, green glow)
 */
@Composable
fun ThermalBlockedScreen(
    appName: String,
    onBackToFocus: () -> Unit
) {
    AuroraBackground(heatLevel = 0.05f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = CoolingPlanetSpacing.xl, vertical = CoolingPlanetSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "UNSCROLL · SHIELD",
                style = ThermalUppercaseLabelStyle,
                color = AuroraTextSecondary
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                GlassChip(
                    label = "Quiet mode",
                    dotColor = ThermalCalmMint
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "$appName is resting right now",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 42.sp,
                        lineHeight = 50.sp
                    ),
                    color = AuroraTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Take a slow breath. The aurora is cool and calm while distractions stay quiet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AuroraTextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            AuroraPrimaryButton(
                text = "Back to focus",
                onClick = onBackToFocus
            )
        }
    }
}
