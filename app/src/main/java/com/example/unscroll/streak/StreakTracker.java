package com.example.unscroll.streak;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Manages daily streak counting using SharedPreferences.
 * Tracks consecutive days where total social media usage stays under the daily limit (e.g. 60 mins).
 */
public class StreakTracker {

    private static final String TAG = "StreakTracker";
    private static final String PREF_NAME = "unscroll_streak_prefs";
    private static final String KEY_LAST_DATE = "key_last_date";
    private static final String KEY_BASE_STREAK = "key_base_streak";
    private static final String KEY_PREV_DAY_SUCCESS = "key_prev_day_success";
    private static final String KEY_DAILY_LIMIT_MINUTES = "key_daily_limit_minutes";

    public static final int DEFAULT_DAILY_LIMIT_MINUTES = 60;

    private final SharedPreferences prefs;

    public StreakTracker(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Value holder class containing current streak information and daily status.
     */
    public static class StreakInfo {
        private final int streakCount;
        private final boolean isUnderLimit;
        private final int dailyLimitMinutes;
        private final long currentUsageMinutes;
        private final String statusMessage;

        public StreakInfo(int streakCount, boolean isUnderLimit, int dailyLimitMinutes, long currentUsageMinutes, String statusMessage) {
            this.streakCount = streakCount;
            this.isUnderLimit = isUnderLimit;
            this.dailyLimitMinutes = dailyLimitMinutes;
            this.currentUsageMinutes = currentUsageMinutes;
            this.statusMessage = statusMessage;
        }

        public int getStreakCount() {
            return streakCount;
        }

        public boolean isUnderLimit() {
            return isUnderLimit;
        }

        public int getDailyLimitMinutes() {
            return dailyLimitMinutes;
        }

        public long getCurrentUsageMinutes() {
            return currentUsageMinutes;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public String getBadgeText() {
            return "🔥 " + streakCount + " Day Streak" + (streakCount == 1 ? "" : "s") + "!";
        }
    }

    public int getDailyLimitMinutes() {
        return prefs.getInt(KEY_DAILY_LIMIT_MINUTES, DEFAULT_DAILY_LIMIT_MINUTES);
    }

    public void setDailyLimitMinutes(int minutes) {
        prefs.edit().putInt(KEY_DAILY_LIMIT_MINUTES, minutes).apply();
    }

    /**
     * Calculates streak count based on today's social media screen-time and SharedPreferences history.
     *
     * @param totalScreenTimeMillis Today's total screen time in milliseconds.
     * @return StreakInfo object with updated streak calculations and display properties.
     */
    public StreakInfo updateAndGetStreak(long totalScreenTimeMillis) {
        int limitMinutes = getDailyLimitMinutes();
        long usageMinutes = TimeUnit.MILLISECONDS.toMinutes(totalScreenTimeMillis);
        boolean isUnderLimit = usageMinutes <= limitMinutes;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String todayStr = sdf.format(new Date());

        String lastDateStr = prefs.getString(KEY_LAST_DATE, "");
        int baseStreak = prefs.getInt(KEY_BASE_STREAK, 0);
        boolean prevDaySuccess = prefs.getBoolean(KEY_PREV_DAY_SUCCESS, true);

        if (lastDateStr.isEmpty()) {
            // Initial setup for the app
            baseStreak = 0;
            prefs.edit()
                    .putString(KEY_LAST_DATE, todayStr)
                    .putInt(KEY_BASE_STREAK, 0)
                    .putBoolean(KEY_PREV_DAY_SUCCESS, isUnderLimit)
                    .apply();
        } else if (!lastDateStr.equals(todayStr)) {
            // Day rolled over
            long daysDiff = getDaysDifference(lastDateStr, todayStr);
            if (daysDiff == 1 && prevDaySuccess) {
                // Previous day ended successfully -> increment accumulated base streak
                baseStreak += 1;
            } else {
                // Missed one or more days, or previous day failed -> reset base streak
                baseStreak = 0;
            }
            prefs.edit()
                    .putString(KEY_LAST_DATE, todayStr)
                    .putInt(KEY_BASE_STREAK, baseStreak)
                    .putBoolean(KEY_PREV_DAY_SUCCESS, isUnderLimit)
                    .apply();
        } else {
            // Same day -> record today's current status
            prefs.edit().putBoolean(KEY_PREV_DAY_SUCCESS, isUnderLimit).apply();
        }

        int currentStreak = isUnderLimit ? (baseStreak + 1) : 0;

        String statusMessage;
        if (isUnderLimit) {
            long remainingMins = limitMinutes - usageMinutes;
            statusMessage = "On Track! " + remainingMins + " mins remaining of your " + limitMinutes + "m limit.";
        } else {
            long overMins = usageMinutes - limitMinutes;
            statusMessage = "Limit Exceeded by " + overMins + " mins. Streak reset for today.";
        }

        return new StreakInfo(currentStreak, isUnderLimit, limitMinutes, usageMinutes, statusMessage);
    }

    private long getDaysDifference(String dateStr1, String dateStr2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date1 = sdf.parse(dateStr1);
            Date date2 = sdf.parse(dateStr2);
            if (date1 != null && date2 != null) {
                long diffMillis = date2.getTime() - date1.getTime();
                return TimeUnit.MILLISECONDS.toDays(diffMillis);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating date difference", e);
        }
        return 0;
    }
}
