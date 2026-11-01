package com.example.unscroll;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * Studio-Grade Botanical Bonsai & Sanctuary Graphic for UnScroll.
 * Pure architectural vector illustration—zero emojis:
 * - Soft morning sun halo behind the canopy
 * - Sculpted terracotta vessel with shadow lip & dark organic soil bed
 * - Curved cubic-bezier botanical trunk and primary/secondary branches
 * - Layered sage & mint foliage clusters with delicate lavender buds
 */
public class GardenGrowingTreeView extends View {

    private Paint sunHaloPaint;
    private Paint potBodyPaint;
    private Paint potRimPaint;
    private Paint soilPaint;
    private Paint trunkPaint;
    private Paint canopyBackPaint;
    private Paint canopyMidPaint;
    private Paint canopyFrontPaint;
    private Paint blossomPaint;
    private Paint captionPaint;

    private final RectF rimRect = new RectF();
    private final RectF soilRect = new RectF();
    private float growthProgress = 0.85f;
    private ValueAnimator animator;

    public GardenGrowingTreeView(Context context) {
        super(context);
        init(context);
    }

    public GardenGrowingTreeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GardenGrowingTreeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        sunHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sunHaloPaint.setStyle(Paint.Style.FILL);

        potBodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        potBodyPaint.setStyle(Paint.Style.FILL);
        potBodyPaint.setColor(Color.parseColor("#C87D65"));

        potRimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        potRimPaint.setStyle(Paint.Style.FILL);
        potRimPaint.setColor(Color.parseColor("#D9886E"));

        soilPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        soilPaint.setStyle(Paint.Style.FILL);
        soilPaint.setColor(Color.parseColor("#3E342D"));

        trunkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trunkPaint.setStyle(Paint.Style.STROKE);
        trunkPaint.setStrokeCap(Paint.Cap.ROUND);
        trunkPaint.setStrokeJoin(Paint.Join.ROUND);
        trunkPaint.setColor(Color.parseColor("#4E4037"));

        canopyBackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        canopyBackPaint.setStyle(Paint.Style.FILL);
        canopyBackPaint.setColor(Color.parseColor("#588B76"));

        canopyMidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        canopyMidPaint.setStyle(Paint.Style.FILL);
        canopyMidPaint.setColor(ContextCompat.getColor(context, R.color.sage_green));

        canopyFrontPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        canopyFrontPaint.setStyle(Paint.Style.FILL);
        canopyFrontPaint.setColor(Color.parseColor("#A4CBB7"));

        blossomPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blossomPaint.setStyle(Paint.Style.FILL);
        blossomPaint.setColor(Color.parseColor("#F5EFE6"));

        captionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        captionPaint.setTextAlign(Paint.Align.CENTER);
        captionPaint.setTextSize(dpToPx(10.5f));
        captionPaint.setLetterSpacing(0.06f);
        captionPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        captionPaint.setColor(ContextCompat.getColor(context, R.color.text_secondary));
    }

    public void setGrowthProgress(float progress, boolean animate) {
        float target = Math.max(0.10f, Math.min(1.0f, progress));
        if (!animate) {
            growthProgress = target;
            invalidate();
            return;
        }

        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }

        animator = ValueAnimator.ofFloat(growthProgress, target);
        animator.setDuration(1000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            growthProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            RadialGradient halo = new RadialGradient(
                    w / 2f, h * 0.42f, Math.min(w, h) * 0.45f,
                    new int[]{Color.parseColor("#387FB69A"), Color.parseColor("#007FB69A")},
                    null, Shader.TileMode.CLAMP
            );
            sunHaloPaint.setShader(halo);

            LinearGradient potGrad = new LinearGradient(
                    w * 0.3f, 0, w * 0.7f, 0,
                    Color.parseColor("#D9886E"), Color.parseColor("#B56A52"),
                    Shader.TileMode.CLAMP
            );
            potBodyPaint.setShader(potGrad);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float cx = w / 2f;
        float basePotY = h - dpToPx(24);

        // 1. Morning Sun Halo behind tree
        canvas.drawCircle(cx, h * 0.42f, Math.min(w, h) * 0.42f, sunHaloPaint);

        // 2. Sculpted Terracotta Planter Vessel
        float potTopW = dpToPx(46);
        float potBotW = dpToPx(32);
        float potH = dpToPx(26);
        float potTopY = basePotY - potH;

        Path potPath = new Path();
        potPath.moveTo(cx - potTopW / 2f, potTopY);
        potPath.lineTo(cx + potTopW / 2f, potTopY);
        potPath.lineTo(cx + potBotW / 2f, basePotY);
        potPath.lineTo(cx - potBotW / 2f, basePotY);
        potPath.close();
        canvas.drawPath(potPath, potBodyPaint);

        // Planter Rim
        rimRect.set(cx - potTopW / 2f - dpToPx(3.5f), potTopY - dpToPx(5), cx + potTopW / 2f + dpToPx(3.5f), potTopY + dpToPx(1));
        canvas.drawRoundRect(rimRect, dpToPx(3), dpToPx(3), potRimPaint);

        // Organic Soil Mound
        soilRect.set(cx - potTopW / 2f + dpToPx(2), potTopY - dpToPx(7), cx + potTopW / 2f - dpToPx(2), potTopY - dpToPx(2));
        canvas.drawOval(soilRect, soilPaint);

        // 3. Curved Botanical Trunk & Branches
        float maxTreeH = (potTopY - dpToPx(28)) * 0.68f;
        float curTreeH = maxTreeH * growthProgress;
        float crownY = potTopY - dpToPx(5) - curTreeH;

        trunkPaint.setStrokeWidth(dpToPx(5.5f) * Math.max(0.55f, growthProgress));
        Path trunkPath = new Path();
        trunkPath.moveTo(cx, potTopY - dpToPx(4));
        trunkPath.cubicTo(
                cx - dpToPx(8) * growthProgress, potTopY - curTreeH * 0.35f,
                cx + dpToPx(6) * growthProgress, potTopY - curTreeH * 0.70f,
                cx, crownY
        );
        canvas.drawPath(trunkPath, trunkPaint);

        // Secondary Side Branches
        if (growthProgress > 0.35f) {
            trunkPaint.setStrokeWidth(dpToPx(3.2f) * growthProgress);
            float branchLeftY = potTopY - curTreeH * 0.55f;
            canvas.drawLine(cx - dpToPx(2), branchLeftY, cx - dpToPx(18) * growthProgress, branchLeftY - dpToPx(12) * growthProgress, trunkPaint);

            float branchRightY = potTopY - curTreeH * 0.68f;
            canvas.drawLine(cx + dpToPx(2), branchRightY, cx + dpToPx(17) * growthProgress, branchRightY - dpToPx(11) * growthProgress, trunkPaint);
        }

        // 4. Layered Botanical Canopy Clouds (Depth & Highlighting)
        float rScale = Math.max(0.35f, growthProgress);
        if (growthProgress > 0.20f) {
            // Deep shadow foliage layer
            canvas.drawCircle(cx - dpToPx(15) * rScale, crownY + dpToPx(6), dpToPx(16) * rScale, canopyBackPaint);
            canvas.drawCircle(cx + dpToPx(16) * rScale, crownY + dpToPx(5), dpToPx(15) * rScale, canopyBackPaint);

            // Mid-tone primary sage foliage
            canvas.drawCircle(cx, crownY - dpToPx(4), dpToPx(20) * rScale, canopyMidPaint);
            canvas.drawCircle(cx - dpToPx(12) * rScale, crownY + dpToPx(2), dpToPx(14) * rScale, canopyMidPaint);
            canvas.drawCircle(cx + dpToPx(12) * rScale, crownY + dpToPx(2), dpToPx(14) * rScale, canopyMidPaint);

            // Sunlit top mint highlights
            if (growthProgress > 0.55f) {
                canvas.drawCircle(cx - dpToPx(5) * rScale, crownY - dpToPx(8) * rScale, dpToPx(11) * rScale, canopyFrontPaint);
                canvas.drawCircle(cx + dpToPx(8) * rScale, crownY - dpToPx(3) * rScale, dpToPx(9) * rScale, canopyFrontPaint);
            }

            // Delicate ivory-lavender botanical buds at peak focus
            if (growthProgress > 0.75f) {
                canvas.drawCircle(cx - dpToPx(11), crownY - dpToPx(6), dpToPx(2.5f), blossomPaint);
                canvas.drawCircle(cx + dpToPx(9), crownY - dpToPx(9), dpToPx(2.8f), blossomPaint);
                canvas.drawCircle(cx + dpToPx(14), crownY + dpToPx(3), dpToPx(2.2f), blossomPaint);
            }
        }

        // 5. Clean Architectural Stage Label (No Emojis)
        String stage;
        if (growthProgress < 0.35f) {
            stage = "STAGE I • SPROUT";
        } else if (growthProgress < 0.75f) {
            stage = "STAGE II • SAPLING";
        } else {
            stage = "SANCTUARY • IN BLOOM";
        }
        canvas.drawText(stage, cx, h - dpToPx(6), captionPaint);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
