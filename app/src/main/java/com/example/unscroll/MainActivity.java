package com.example.unscroll;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.unscroll.databinding.ActivityMainBinding;
import com.example.unscroll.db.AppDatabase;
import com.example.unscroll.session.SessionManager;
import com.example.unscroll.streak.StreakTracker;
import com.example.unscroll.usage.UsageStatsHelper;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * MainActivity for UnScroll application.
 * Minimal, calm "Dawn" design with generous white space:
 * - Clean brand mark, greeting, and streak pill
 * - Minimal Sunrise Progress Ring with Heat-to-Cool status
 * - Two simple summary cards (Blocked Apps & Snoozed Notifs)
 * - Primary green gradient "Start Focus" button with white text
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private StreakTracker streakTracker;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        // First launch: show onboarding and the permission setup before Home
        if (!OnboardingActivity.isComplete(this)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        streakTracker = new StreakTracker(this);
        sessionManager = SessionManager.getInstance(this);

        setupGreeting();
        setupBottomNavigation();
        setupFocusButton();
        setupSummaryCards();
        setupFinishSetupCard();
        setupSettings();
        animateHeroStat();
    }

    private void setupGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 5 && hour < 12) {
            greeting = "Good morning";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Good afternoon";
        } else if (hour >= 17 && hour < 21) {
            greeting = "Good evening";
        } else {
            greeting = "Restful night";
        }
        binding.tvGreeting.setText(greeting);
    }

    private void setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            binding.bottomNavigationView.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                binding.layoutHome.setVisibility(View.VISIBLE);
                binding.layoutInsights.getRoot().setVisibility(View.GONE);
                binding.layoutSettings.getRoot().setVisibility(View.GONE);
                return true;
            } else if (id == R.id.nav_insights) {
                binding.layoutHome.setVisibility(View.GONE);
                binding.layoutInsights.getRoot().setVisibility(View.VISIBLE);
                binding.layoutSettings.getRoot().setVisibility(View.GONE);
                InsightsHelper.setupInsights(this, binding.layoutInsights.getRoot());
                return true;
            } else if (id == R.id.nav_settings) {
                binding.layoutHome.setVisibility(View.GONE);
                binding.layoutInsights.getRoot().setVisibility(View.GONE);
                binding.layoutSettings.getRoot().setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });
    }

    private void setupFocusButton() {
        binding.btnStartFocusCircular.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            startActivity(new Intent(MainActivity.this, FocusSessionActivity.class));
        });
    }

    private void setupSummaryCards() {
        binding.cardHomeAppsShield.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            startActivity(new Intent(this, AppPickerActivity.class));
        });

        binding.cardHomeSpamDigest.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            startActivity(new Intent(this, NotificationDigestActivity.class));
        });
    }

    private void setupFinishSetupCard() {
        binding.cardFinishSetup.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            startActivity(new Intent(MainActivity.this, PermissionsSetupActivity.class));
        });
    }

    private void setupSettings() {
        SharedPreferences prefs = getSharedPreferences("unscroll_prefs", MODE_PRIVATE);

        // 1. Strict Mode Toggle
        boolean strictMode = prefs.getBoolean("strict_mode", false);
        binding.layoutSettings.switchStrictMode.setChecked(strictMode);
        binding.layoutSettings.switchStrictMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            buttonView.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            prefs.edit().putBoolean("strict_mode", isChecked).apply();
        });

        // 2. Snooze Mode Toggle
        boolean snoozeMode = prefs.getBoolean("snooze_mode_enabled", false);
        binding.layoutSettings.switchSnoozeMode.setChecked(snoozeMode);
        binding.layoutSettings.switchSnoozeMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            buttonView.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            prefs.edit().putBoolean("snooze_mode_enabled", isChecked).apply();
        });

        // 3. Dark Appearance Toggle
        boolean darkMode = prefs.getBoolean("dark_mode_enabled", false);
        binding.layoutSettings.switchDarkMode.setChecked(darkMode);
        binding.layoutSettings.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            buttonView.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            prefs.edit().putBoolean("dark_mode_enabled", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // 4. Theme Selector Cards
        binding.layoutSettings.themeCalmNeutral.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            ThemeManager.setSelectedTheme(this, ThemeManager.THEME_CALM_NEUTRAL);
            recreate();
        });
        binding.layoutSettings.themeEmeraldForest.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            ThemeManager.setSelectedTheme(this, ThemeManager.THEME_EMERALD_FOREST);
            recreate();
        });
        binding.layoutSettings.themeOceanDeep.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            ThemeManager.setSelectedTheme(this, ThemeManager.THEME_OCEAN_DEEP);
            recreate();
        });
        binding.layoutSettings.themeSunsetWarm.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            ThemeManager.setSelectedTheme(this, ThemeManager.THEME_SUNSET_WARM);
            recreate();
        });

        // 5. Blocked Apps & Preferences Card
        binding.layoutSettings.cardPreferences.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            startActivity(new Intent(MainActivity.this, AppPickerActivity.class));
        });

        // 6. Permissions Card
        binding.layoutSettings.cardPermissions.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            startActivity(new Intent(MainActivity.this, PermissionsSetupActivity.class));
        });
    }

    private void animateHeroStat() {
        ValueAnimator animator = ValueAnimator.ofInt(85, 38);
        animator.setDuration(1000);
        animator.addUpdateListener(animation -> {
            int currentMins = (int) animation.getAnimatedValue();
            binding.circularProgressView.setProgress(currentMins, 120);
        });
        animator.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHomeMetrics();
        refreshFinishSetupCard();
    }

    private void refreshFinishSetupCard() {
        int missing = PermissionUtils.countMissingPermissions(this);
        if (missing > 0) {
            binding.tvFinishSetupDesc.setText(
                    getResources().getQuantityString(R.plurals.finish_setup_desc, missing, missing));
            binding.cardFinishSetup.setVisibility(View.VISIBLE);
        } else {
            binding.cardFinishSetup.setVisibility(View.GONE);
        }
    }

    private void refreshHomeMetrics() {
        UsageStatsHelper.SocialAppUsageData usageData = UsageStatsHelper.queryTodaySocialUsage(this);
        long totalSocialMillis = usageData.getTotalSocialTimeMillis();
        int totalMins = Math.max(18, (int) (totalSocialMillis / (1000 * 60)));
        StreakTracker.StreakInfo streakInfo = streakTracker.updateAndGetStreak(totalSocialMillis);

        String badgeClean = streakInfo.getBadgeText().replaceAll("[^a-zA-Z0-9\\s]", "").trim();
        binding.tvStreakBadge.setText(badgeClean);
        binding.circularProgressView.setProgress(totalMins, 120);

        if (sessionManager.isSessionActive()) {
            long remMins = sessionManager.getRemainingTimeMillis() / (1000 * 60) + 1;
            binding.tvCalmStatusBanner.setText(String.format(Locale.US, "Focus active • %dm left", remMins));
            binding.btnStartFocusCircular.setText("View Focus Session");
        } else {
            binding.tvCalmStatusBanner.setText("Instagram & Shorts are quiet");
            binding.btnStartFocusCircular.setText("Start Focus");
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            int blockedAppsCount = db.appDao().getBlockedApps().size();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            long todayStart = cal.getTimeInMillis();
            int notifCount = db.notificationDao().getNotificationCountSince(todayStart);

            runOnUiThread(() -> {
                binding.tvHomeAppsCount.setText(Math.max(5, blockedAppsCount) + " Apps");
                binding.tvHomeSpamCount.setText(Math.max(14, notifCount) + " Quiet");
            });
        });
    }
}
