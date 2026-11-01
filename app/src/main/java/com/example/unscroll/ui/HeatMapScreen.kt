package com.example.unscroll.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unscroll.ui.theme.AuroraTextPrimary
import com.example.unscroll.ui.theme.AuroraTextSecondary
import com.example.unscroll.ui.theme.CoolingPlanetSpacing
import com.example.unscroll.ui.theme.ThermalCalmMint
import com.example.unscroll.ui.theme.ThermalCoolTeal
import com.example.unscroll.ui.theme.ThermalHotCrimson
import com.example.unscroll.ui.theme.ThermalSoftViolet
import com.example.unscroll.ui.theme.ThermalUppercaseLabelStyle
import com.example.unscroll.ui.theme.ThermalWarmOrange
import com.example.unscroll.ui.theme.thermalColorForHeat
import com.example.unscroll.ui.theme.thermalShortLabelForHeat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Thermal Aurora Heat Map Screen:
 * - Uppercase label "LAST 4 WEEKS" (2.5sp letter spacing) and title "Heat map"
 * - Inside a GlassCard: 4 weeks as a 7-column grid (M T W T F S S) of glowing circles (34dp)
 *   coloured with the heat palette by each day's usage, with a soft glow in the same colour.
 * - Today has a crisp white ring.
 * - Bottom GlassCard shows the selected day and its total screen time; tapping any day updates it.
 */
@Composable
fun ThermalHeatMapScreen(todayHeatLevel: Float) {
    val dayHeaders = listOf("M", "T", "W", "T", "F", "S", "S")

    val records = remember(todayHeatLevel) {
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.US)
        val dayNumFormat = SimpleDateFormat("d", Locale.US)
        val sampleHeats = floatArrayOf(
            0.92f, 0.78f, 0.64f, 0.72f, 0.85f, 0.95f, 0.58f,
            0.68f, 0.52f, 0.44f, 0.62f, 0.56f, 0.74f, 0.42f,
            0.38f, 0.30f, 0.24f, 0.48f, 0.34f, 0.40f, 0.18f,
            0.28f, 0.22f, 0.14f, 0.32f, 0.18f, 0.10f, todayHeatLevel
        )

        cal.add(Calendar.DAY_OF_YEAR, -27)
        List(28) { idx ->
            val heat = sampleHeats[idx].coerceIn(0f, 1f)
            val mins = (18 + (heat * 205).roundToInt())
            val record = DayHeatRecord(
                dayIndex = idx,
                dateLabel = if (idx == 27) "Today · ${dateFormat.format(cal.time)}" else dateFormat.format(cal.time),
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
    val selectedColor = thermalColorForHeat(selectedDay.heatLevel)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = CoolingPlanetSpacing.lg, vertical = CoolingPlanetSpacing.xl)
    ) {
        Text(
            text = "LAST 4 WEEKS",
            style = ThermalUppercaseLabelStyle,
            color = AuroraTextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Heat map",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Light,
                fontSize = 36.sp
            ),
            color = AuroraTextPrimary
        )

        Spacer(modifier = Modifier.height(CoolingPlanetSpacing.lg))

        // Main 4-Week Grid GlassCard
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 22.dp)
        ) {
            // 7-column header (M T W T F S S)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayHeaders.forEach { header ->
                    Box(
                        modifier = Modifier.size(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = header,
                            style = ThermalUppercaseLabelStyle,
                            textAlign = TextAlign.Center,
                            color = AuroraTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4 rows x 7 columns of glowing 34dp circles
            for (week in 0 until 4) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (col in 0 until 7) {
                        val index = week * 7 + col
                        val day = records[index]
                        val isToday = index == 27
                        val isSelected = index == selectedDayIndex
                        val circleColor = thermalColorForHeat(day.heatLevel)

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable { selectedDayIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            // True circular radial glow on Canvas (zero boxy edges)
                            Canvas(modifier = Modifier.size(48.dp)) {
                                val r = size.minDimension / 2f
                                val c = Offset(size.width / 2f, size.height / 2f)
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            circleColor.copy(alpha = if (isSelected) 0.72f else 0.48f),
                                            circleColor.copy(alpha = 0.18f),
                                            Color.Transparent
                                        ),
                                        center = c,
                                        radius = r
                                    ),
                                    radius = r,
                                    center = c
                                )
                            }

                            // 34dp Glowing Circle (Today has a crisp white ring)
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(circleColor.copy(alpha = 0.88f), CircleShape)
                                    .then(
                                        when {
                                            isToday -> Modifier.border(
                                                BorderStroke(2.dp, Color.White),
                                                CircleShape
                                            )
                                            isSelected -> Modifier.border(
                                                BorderStroke(1.5.dp, Color.White.copy(alpha = 0.75f)),
                                                CircleShape
                                            )
                                            else -> Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.dayOfMonth,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = Color(0xFF0B0E1D)
                                )
                            }
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
                    text = "COOL",
                    style = ThermalUppercaseLabelStyle.copy(fontSize = 10.sp),
                    color = AuroraTextSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        ThermalCalmMint,
                        ThermalCoolTeal,
                        ThermalSoftViolet,
                        ThermalWarmOrange,
                        ThermalHotCrimson
                    ).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(color, CircleShape)
                        )
                    }
                }
                Text(
                    text = "HOT",
                    style = ThermalUppercaseLabelStyle.copy(fontSize = 10.sp),
                    color = AuroraTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(CoolingPlanetSpacing.lg))

        // Bottom GlassCard showing the selected day and its total
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(modifier = Modifier.size(38.dp)) {
                        val r = size.minDimension / 2f
                        val c = Offset(size.width / 2f, size.height / 2f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    selectedColor.copy(alpha = 0.65f),
                                    selectedColor.copy(alpha = 0.20f),
                                    Color.Transparent
                                ),
                                center = c,
                                radius = r
                            ),
                            radius = r,
                            center = c
                        )
                        drawCircle(
                            color = selectedColor,
                            radius = 10.dp.toPx(),
                            center = c
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = selectedDay.dateLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = AuroraTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${thermalShortLabelForHeat(selectedDay.heatLevel)} · ${(selectedDay.heatLevel * 100).roundToInt()}% heat",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AuroraTextSecondary
                        )
                    }
                }

                Text(
                    text = selectedDay.formattedTotal,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Light,
                        fontSize = 30.sp
                    ),
                    color = AuroraTextPrimary
                )
            }
        }
    }
}
