package com.example.unscroll;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Studio-Grade 7-Day Bio-Thermal Spectrum Matrix (Oura / Apple Health style).
 * Zero emojis—pure geometric precision:
 * - Soft recessed vertical track capsule for each day
 * - Dynamic thermal fill bar proportional to screen load, colored along the Heat-to-Cool spectrum
 * - Clean day initials, formatted time label, and a continuous 5-stop Thermal Spectrum Legend Bar below
 */
public class HeatMapView extends View {

    public static class DayHeatEntry {
        public final String dayLabel;
        public final int minutesSpent;
        public final int goalMinutes;

        public DayHeatEntry(String dayLabel, int minutesSpent, int goalMinutes) {
            this.dayLabel = dayLabel;
            this.minutesSpent = minutesSpent;
            this.goalMinutes = goalMinutes;
        }
    }

    private final List<DayHeatEntry> entries = new ArrayList<>();
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dayTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint timeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint spectrumPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint legendLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF trackRect = new RectF();
    private final RectF barRect = new RectF();
    private final RectF spectrumRect = new RectF();

    private int selectedIndex = 6; // Default highlight Sunday (Restful)

    public HeatMapView(Context context) {
        super(context);
        init();
    }

    public HeatMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HeatMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        trackPaint.setStyle(Paint.Style.FILL);
        trackPaint.setColor(Color.parseColor("#F3F0EA"));

        barPaint.setStyle(Paint.Style.FILL);

        selectedStrokePaint.setStyle(Paint.Style.STROKE);
        selectedStrokePaint.setStrokeWidth(dpToPx(1.75f));
        selectedStrokePaint.setColor(ContextCompat.getColor(getContext(), R.color.text_primary));

        dayTextPaint.setTextAlign(Paint.Align.CENTER);
        dayTextPaint.setTextSize(dpToPx(11f));
        dayTextPaint.setFakeBoldText(true);
        dayTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_secondary));

        timeTextPaint.setTextAlign(Paint.Align.CENTER);
        timeTextPaint.setTextSize(dpToPx(10.5f));
        timeTextPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        timeTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_primary));

        legendLabelPaint.setTextSize(dpToPx(10f));
        legendLabelPaint.setLetterSpacing(0.05f);
        legendLabelPaint.setFakeBoldText(true);
        legendLabelPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_secondary));

        // 7-Day Cooling Progression (Mon Hot -> Sun Restful)
        entries.add(new DayHeatEntry("MON", 175, 120));
        entries.add(new DayHeatEntry("TUE", 138, 120));
        entries.add(new DayHeatEntry("WED", 108, 120));
        entries.add(new DayHeatEntry("THU", 92, 120));
        entries.add(new DayHeatEntry("FRI", 68, 120));
        entries.add(new DayHeatEntry("SAT", 52, 120));
        entries.add(new DayHeatEntry("SUN", 28, 120));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0) {
            int[] spectrumColors = new int[]{
                    ContextCompat.getColor(getContext(), R.color.heat_hot),
                    ContextCompat.getColor(getContext(), R.color.heat_warm),
                    ContextCompat.getColor(getContext(), R.color.heat_moderate),
                    ContextCompat.getColor(getContext(), R.color.heat_cool),
                    ContextCompat.getColor(getContext(), R.color.heat_restful)
            };
            LinearGradient grad = new LinearGradient(
                    dpToPx(48), 0, w - dpToPx(56), 0,
                    spectrumColors, null, Shader.TileMode.CLAMP
            );
            spectrumPaint.setShader(grad);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (entries.isEmpty()) return;

        float width = getWidth();
        int count = entries.size();
        float spacing = dpToPx(8);
        float colWidth = (width - spacing * (count - 1)) / count;
        float capsuleTop = dpToPx(18);
        float capsuleHeight = dpToPx(64);
        float capsuleBottom = capsuleTop + capsuleHeight;

        for (int i = 0; i < count; i++) {
            DayHeatEntry entry = entries.get(i);
            ThemeManager.HeatStatus status = ThemeManager.getHeatStatus(entry.minutesSpent, entry.goalMinutes);

            float left = i * (colWidth + spacing);
            float right = left + colWidth;
            float cx = (left + right) / 2f;

            // 1. Top minutes label
            int hrs = entry.minutesSpent / 60;
            int mins = entry.minutesSpent % 60;
            String tLabel = hrs > 0 ? (hrs + "h" + (mins > 0 ? mins : "")) : (mins + "m");
            canvas.drawText(tLabel, cx, dpToPx(12), timeTextPaint);

            // 2. Recessed Background Capsule
            trackRect.set(left, capsuleTop, right, capsuleBottom);
            canvas.drawRoundRect(trackRect, dpToPx(12), dpToPx(12), trackPaint);

            // 3. Proportional Bio-Thermal Fill Capsule anchored to bottom
            float fillRatio = Math.max(0.22f, Math.min(1f, entry.minutesSpent / 180f));
            float fillHeight = capsuleHeight * fillRatio;
            barRect.set(left, capsuleBottom - fillHeight, right, capsuleBottom);

            barPaint.setColor(status.getColor(getContext()));
            canvas.drawRoundRect(barRect, dpToPx(12), dpToPx(12), barPaint);

            // 4. Selection Ring
            if (i == selectedIndex) {
                canvas.drawRoundRect(trackRect, dpToPx(12), dpToPx(12), selectedStrokePaint);
            }

            // 5. Day Label below capsule
            dayTextPaint.setColor(i == selectedIndex
                    ? ContextCompat.getColor(getContext(), R.color.text_primary)
                    : ContextCompat.getColor(getContext(), R.color.text_secondary));
            canvas.drawText(entry.dayLabel, cx, capsuleBottom + dpToPx(15), dayTextPaint);
        }

        // 6. Continuous Bio-Thermal Spectrum Legend Bar at bottom
        float barY = capsuleBottom + dpToPx(28);
        legendLabelPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("HOT", 0, barY + dpToPx(6), legendLabelPaint);

        spectrumRect.set(dpToPx(34), barY, width - dpToPx(48), barY + dpToPx(6));
        canvas.drawRoundRect(spectrumRect, dpToPx(3), dpToPx(3), spectrumPaint);

        legendLabelPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("COOL", width, barY + dpToPx(6), legendLabelPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            int count = entries.size();
            float spacing = dpToPx(8);
            float colWidth = (getWidth() - spacing * (count - 1)) / count;
            for (int i = 0; i < count; i++) {
                float left = i * (colWidth + spacing);
                if (x >= left && x <= left + colWidth) {
                    selectedIndex = i;
                    performClick();
                    invalidate();
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
