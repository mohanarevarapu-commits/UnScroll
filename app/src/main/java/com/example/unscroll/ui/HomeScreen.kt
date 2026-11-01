package com.example.unscroll.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unscroll.ui.theme.AuroraTextPrimary
import com.example.unscroll.ui.theme.AuroraTextSecondary
import com.example.unscroll.ui.theme.CoolingPlanetSpacing
import com.example.unscroll.ui.theme.ManropeFontFamily
import com.example.unscroll.ui.theme.ThermalUppercaseLabelStyle
import com.example.unscroll.ui.theme.thermalColorForHeat
import com.example.unscroll.ui.theme.thermalShortLabelForHeat
import kotlin.math.roundToInt

/**
 * Thermal Aurora Home Screen:
 * - Interactive UnScroll brand logo at the top + 5-logo showcase picker card
 * - Small uppercase label "UNSCROLL · TODAY" (2.5sp letter spacing)
 * - Huge light-weight time (~86sp, Manrope Light 300), e.g., "2h 48m"
 * - Glass chip with glowing dot and heat label, e.g., "High heat"
 * - Two GlassCards side by side ("5 days / Mindful streak" and "82% / Heat level")
 * - Full-width green "Start focus" button (#3DDC97 background, #052A1A text, green glow)
 * - Interactive heat level slider to test different aurora states (0f to 1f)
 */
@Composable
fun ThermalHomeScreen(
    heatLevel: Float,
    onHeatLevelChange: (Float) -> Unit,
    streakDays: Int,
    onStartFocus: () -> Unit
) {
    var selectedLogo by rememberSaveable { mutableStateOf(UnScrollLogoStyle.NEON_US_RIBBON) }

    // At 0.82f heatLevel, screen time is 2h 48m (168 mins)
    val totalMinutesToday = (12 + (heatLevel * 190f).roundToInt())
    val hours = totalMinutesToday / 60
    val mins = totalMinutesToday % 60
    val formattedScreenTime = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

    val heatPercent = (heatLevel * 100).roundToInt()
    val heatLabel = thermalShortLabelForHeat(heatLevel)
    val dotColor = thermalColorForHeat(heatLevel)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = CoolingPlanetSpacing.lg, vertical = CoolingPlanetSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top-Center Hero Section: Active Brand Logo + Uppercase Label + 86sp Light Time + Glowing Glass Chip
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            UnScrollBrandLogo(
                style = selectedLogo,
                size = 64.dp,
                isSelected = true,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .clickable {
                        val nextIdx = (selectedLogo.ordinal + 1) % UnScrollLogoStyle.entries.size
                        selectedLogo = UnScrollLogoStyle.entries[nextIdx]
                    }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "UNSCROLL · TODAY",
                style = ThermalUppercaseLabelStyle,
                color = AuroraTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formattedScreenTime,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 86.sp,
                    lineHeight = 92.sp
                ),
                color = AuroraTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            GlassChip(
                label = heatLabel,
                dotColor = dotColor
            )
        }

        Spacer(modifier = Modifier.height(CoolingPlanetSpacing.lg))

        // 5-Logo Interactive Showcase Card
        UnScrollLogoShowcaseCard(
            selectedLogo = selectedLogo,
            onSelectLogo = { selectedLogo = it }
        )

        Spacer(modifier = Modifier.height(CoolingPlanetSpacing.md))

        // Bottom Section: Two Side-by-Side Glass Cards + Full-Width Green Button + Heat Tester
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CoolingPlanetSpacing.md)
            ) {
                GlassCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 18.dp, horizontal = 18.dp)
                ) {
                    Text(
                        text = "$streakDays days",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Light,
                            fontSize = 30.sp
                        ),
                        color = AuroraTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mindful streak",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuroraTextSecondary
                    )
                }

                GlassCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 18.dp, horizontal = 18.dp)
                ) {
                    Text(
                        text = "$heatPercent%",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Light,
                            fontSize = 30.sp
                        ),
                        color = AuroraTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Heat level",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuroraTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(CoolingPlanetSpacing.md))

            AuroraPrimaryButton(
                text = "Start focus",
                onClick = onStartFocus
            )

            Spacer(modifier = Modifier.height(CoolingPlanetSpacing.md))

            // Interactive Heat Level Tester Glass Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TEST AURORA HEAT",
                        style = ThermalUppercaseLabelStyle,
                        color = AuroraTextSecondary
                    )
                    Text(
                        text = "$heatPercent%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AuroraTextPrimary
                    )
                }

                Slider(
                    value = heatLevel,
                    onValueChange = onHeatLevelChange,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = dotColor,
                        activeTrackColor = dotColor,
                        inactiveTrackColor = AuroraTextSecondary.copy(alpha = 0.24f)
                    )
                )
            }
        }
    }
}
