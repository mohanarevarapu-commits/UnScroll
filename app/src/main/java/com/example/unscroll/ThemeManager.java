package com.example.unscroll;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.content.ContextCompat;

/**
 * Manager to persist and apply user-selected color themes and manage the "Dawn" design system.
 * Implements Step 3 Theme & Design specifications:
 * - "Dawn" concept: warm ivory background (#F6F3EE), crisp white cards, sage green primary (#7FB69A)
 * - Subtle accents: soft lavender (#EAE4F2), sand (#EDE8E1), and clay (#D9886E)
 * - Heat-to-Cool progression: red-orange (hot) -> amber -> pale yellow -> mint -> cool blue (restful)
 * - Accessibility: high-contrast colors, 48dp minimum targets, and descriptive text labels for every color state.
 */
public class ThemeManager {
    private static final String PREF_NAME = "unscroll_prefs";
    private static final String KEY_SELECTED_THEME = "selected_theme";

    public static final int THEME_DAWN = 0; // Dawn (Calm Neutral: Sage Green & Warm Ivory)
    public static final int THEME_CALM_NEUTRAL = 0;
    public static final int THEME_EMERALD_FOREST = 1;
    public static final int THEME_OCEAN_DEEP = 2;
    public static final int THEME_SUNSET_WARM = 3;

    public static int getSelectedTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_SELECTED_THEME, THEME_DAWN);
    }

    public static void setSelectedTheme(Context context, int themeId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_SELECTED_THEME, themeId).apply();
    }

    public static int getThemeStyleRes(int themeId) {
        switch (themeId) {
            case THEME_EMERALD_FOREST:
                return R.style.Theme_UnScroll_EmeraldForest;
            case THEME_OCEAN_DEEP:
                return R.style.Theme_UnScroll_OceanDeep;
            case THEME_SUNSET_WARM:
                return R.style.Theme_UnScroll_SunsetWarm;
            case THEME_DAWN:
            default:
                return R.style.Theme_UnScroll_Dawn;
        }
    }

    public static void applyTheme(Activity activity) {
        int themeId = getSelectedTheme(activity);
        activity.setTheme(getThemeStyleRes(themeId));
    }

    public static int getPrimaryColor(Context context) {
        int themeId = getSelectedTheme(context);
        switch (themeId) {
            case THEME_EMERALD_FOREST:
                return ContextCompat.getColor(context, R.color.emerald_primary);
            case THEME_OCEAN_DEEP:
                return ContextCompat.getColor(context, R.color.ocean_primary);
            case THEME_SUNSET_WARM:
                return ContextCompat.getColor(context, R.color.sunset_primary);
            case THEME_DAWN:
            default:
                return ContextCompat.getColor(context, R.color.sage_green);
        }
    }

    /**
     * Heat-to-Cool progression state for screen time.
     * Ensures color is never the only clue by always providing a label and accessibility text.
     */
    public static class HeatStatus {
        public final int colorRes;
        public final int bgRes;
        public final String label;
        public final String accessibilityDesc;

        public HeatStatus(int colorRes, int bgRes, String label, String accessibilityDesc) {
            this.colorRes = colorRes;
            this.bgRes = bgRes;
            this.label = label;
            this.accessibilityDesc = accessibilityDesc;
        }

        public int getColor(Context context) {
            return ContextCompat.getColor(context, colorRes);
        }

        public int getBgColor(Context context) {
            return ContextCompat.getColor(context, bgRes);
        }
    }

    /**
     * Calculates the Heat-to-Cool temperature based on current usage versus daily goal.
     * Scale:
     * - > 120% goal: Hot (Red-Orange, #E76F51) - Heavy screen time
     * - 90% - 120% goal: Warm (Amber, #F4A261) - Elevated screen time
     * - 60% - 90% goal: Moderate (Pale Yellow, #E9C46A) - Moderate screen time
     * - 30% - 60% goal: Cool (Mint, #8AB89E) - Mindful & balanced
     * - <= 30% goal: Restful (Cool Blue, #5E81AC) - Zen / deep peace
     */
    public static HeatStatus getHeatStatus(long minutesSpent, long goalMinutes) {
        if (goalMinutes <= 0) goalMinutes = 120; // default 2 hours baseline
        float ratio = (float) minutesSpent / (float) goalMinutes;

        if (ratio > 1.20f) {
            return new HeatStatus(
                    R.color.heat_hot,
                    R.color.heat_hot_bg,
                    "🔥 Hot (Heavy)",
                    "Screen time status: Hot. Heavy screen time exceeding daily goal."
            );
        } else if (ratio > 0.90f) {
            return new HeatStatus(
                    R.color.heat_warm,
                    R.color.heat_warm_bg,
                    "⚡ Warm (Elevated)",
                    "Screen time status: Warm. Elevated screen time approaching daily goal."
            );
        } else if (ratio > 0.60f) {
            return new HeatStatus(
                    R.color.heat_moderate,
                    R.color.heat_moderate_bg,
                    "🌤️ Moderate (Steady)",
                    "Screen time status: Moderate. Steady screen time within normal bounds."
            );
        } else if (ratio > 0.30f) {
            return new HeatStatus(
                    R.color.heat_cool,
                    R.color.heat_cool_bg,
                    "🌿 Cool (Mindful)",
                    "Screen time status: Cool. Mindful screen time well below limit."
            );
        } else {
            return new HeatStatus(
                    R.color.heat_restful,
                    R.color.heat_restful_bg,
                    "🌊 Restful (Zen)",
                    "Screen time status: Restful. Zen digital balance with minimal screen time."
            );
        }
    }
}
