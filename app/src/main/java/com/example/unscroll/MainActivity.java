package com.example.unscroll;

import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.platform.ComposeView;

import com.example.unscroll.db.AppDatabase;
import com.example.unscroll.db.AppEntity;
import com.example.unscroll.streak.StreakTracker;
import com.example.unscroll.ui.CoolingPlanetHost;
import com.example.unscroll.usage.UsageStatsHelper;

import java.util.concurrent.Executors;

import kotlin.Unit;

/**
 * MainActivity hosting the Jetpack Compose "Thermal Aurora / Blue Light & Shade" UI:
 * - Home Screen (Screen time, streak, heat level, Start focus button, Phone Lockdown 15m-5h button)
 * - Focus Session Screen (Countdown, End session button, gentle completion animation)
 * - Phone Lockdown Screen (Strict 15 mins to 5 hours phone lockdown with Incoming Calls & Emergency SOS access)
 * - 4-Week Heat Map Screen (28-day rounded grid with tap-to-inspect day total card)
 * - App Blocking Screen (Toggleable app list + calm Blocked screen)
 * - Smooth 3-tab Bottom Navigation Bar (Home, Heat Map, Blocking)
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }

        StreakTracker streakTracker = new StreakTracker(this);
        UsageStatsHelper.SocialAppUsageData usageData = UsageStatsHelper.queryTodaySocialUsage(this);
        StreakTracker.StreakInfo streakInfo = streakTracker.updateAndGetStreak(usageData.getTotalSocialTimeMillis());
        int streakDays = Math.max(5, streakInfo.getStreakCount());

        ComposeView composeView = CoolingPlanetHost.createMainComposeView(
                this,
                streakDays,
                (packageName, appName, isBlocked) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                        db.appDao().insertOrUpdate(new AppEntity(packageName, appName, isBlocked));
                    });
                    return Unit.INSTANCE;
                }
        );

        setContentView(composeView);
    }
}
