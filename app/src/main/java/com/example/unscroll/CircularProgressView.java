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
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * Ultra-Clean Minimalist Sunrise Progress Ring.
 * Simple, calm, and legible:
 * - Warm sand background ring (#EDE8E1)
 * - Smooth Sage-to-Teal progress arc (#7FB69A -> #3E8E7E)
 * - Clean Fraunces serif time in the center + simple Heat-to-Cool status label
 */
public class CircularProgressView extends View {

    private Paint trackPaint;
    private Paint progressPaint;
    private Paint valuePaint;
    private Paint subTextPaint;
    private Paint statusPaint;

    private final RectF arcRect = new RectF();

    private int progress = 38; // screen time minutes
    private int max = 120; // daily goal minutes
    private ThemeManager.HeatStatus currentHeatStatus;

    public CircularProgressView(Context context) {
        super(context);
        init(context);
    }

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CircularProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(dpToPx(12));
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setColor(ContextCompat.getColor(context, R.color.dawn_sand));

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(dpToPx(12));
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setTextSize(dpToPx(36));
        valuePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        valuePaint.setColor(ContextCompat.getColor(context, R.color.text_primary));

        subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subTextPaint.setTextAlign(Paint.Align.CENTER);
        subTextPaint.setTextSize(dpToPx(12));
        subTextPaint.setColor(ContextCompat.getColor(context, R.color.text_secondary));

        statusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        statusPaint.setTextAlign(Paint.Align.CENTER);
        statusPaint.setTextSize(dpToPx(11.5f));
        statusPaint.setFakeBoldText(true);

        currentHeatStatus = ThemeManager.getHeatStatus(progress, max);
    }

    public void setProgress(int currentScreenMinutes, int maxGoalMinutes) {
        this.progress = Math.max(0, currentScreenMinutes);
        this.max = Math.max(1, maxGoalMinutes);
        this.currentHeatStatus = ThemeManager.getHeatStatus(progress, max);

        updateColors(getWidth(), getHeight());
        setContentDescription("Screen time: " + progress + "m of " + max + "m. " + currentHeatStatus.accessibilityDesc);
        invalidate();
    }

    private void updateColors(int w, int h) {
        if (w <= 0 || h <= 0) return;
        int heatColor = ContextCompat.getColor(getContext(), currentHeatStatus.colorRes);
        int tealColor = ContextCompat.getColor(getContext(), R.color.teal_accent);
        statusPaint.setColor(heatColor);

        LinearGradient grad = new LinearGradient(0, 0, w, h,
                new int[]{heatColor, tealColor}, null, Shader.TileMode.CLAMP);
        progressPaint.setShader(grad);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float pad = dpToPx(18);
        arcRect.set(pad, pad, w - pad, h - pad);
        updateColors(w, h);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        // 1. Background Ring
        canvas.drawArc(arcRect, -90f, 360f, false, trackPaint);

        // 2. Smooth Sunrise Progress Arc
        float ratio = Math.max(0.08f, Math.min(1f, (float) (max - Math.min(progress, max)) / max));
        canvas.drawArc(arcRect, -90f, 360f * ratio, false, progressPaint);

        // 3. Center Time Saved / Remaining
        int remMins = Math.max(0, max - progress);
        int h = remMins / 60;
        int m = remMins % 60;
        String timeStr = h > 0 ? (h + "h " + m + "m") : (m + "m");
        canvas.drawText(timeStr, cx, cy - dpToPx(4), valuePaint);

        canvas.drawText("focus time left", cx, cy + dpToPx(18), subTextPaint);

        // 4. Clean Heat-to-Cool Status Label
        if (currentHeatStatus != null) {
            String cleanLabel = currentHeatStatus.label
                    .replaceAll("[^a-zA-Z0-9()\\s-]", "")
                    .trim();
            canvas.drawText(cleanLabel, cx, cy + dpToPx(38), statusPaint);
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
