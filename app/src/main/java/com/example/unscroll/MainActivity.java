package com.example.unscroll;

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
 * MainActivity hosting the Jetpack Compose "CoolingPlanet" UI:
 * - Home Screen (Pulsing & glowing Cooling Planet, screen time, streak, Start focus button, heat level tester)
 * - Focus Session Screen (Countdown inside the cooling planet, End session button, gentle completion animation)
 * - 4-Week Heat Map Screen (28-day rounded grid with tap-to-inspect day total card)
 * - App Blocking Screen (Toggleable app list + calm Blocked screen with cooling planet & Back to focus button)
 * - Smooth 3-tab Bottom Navigation Bar (Home, Heat Map, Blocking)
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

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
