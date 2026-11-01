package com.example.unscroll;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.platform.ComposeView;

import com.example.unscroll.session.SessionManager;
import com.example.unscroll.ui.CoolingPlanetHost;
import com.example.unscroll.usage.UsageStatsHelper;

import kotlin.Unit;

/**
 * Calm "blocked" or strict "Phone Lockdown" overlay activity.
 */
public class BlockOverlayActivity extends AppCompatActivity {

    public static final String EXTRA_TARGET_PACKAGE = "extra_target_package";
    public static final String EXTRA_LOCKDOWN_MODE = "extra_lockdown_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }

        SessionManager sessionManager = SessionManager.getInstance(this);
        boolean isLockdown = getIntent().getBooleanExtra(EXTRA_LOCKDOWN_MODE, false)
                || sessionManager.isLockdownActive();

        if (isLockdown) {
            ComposeView lockdownComposeView = CoolingPlanetHost.createLockdownOverlayComposeView(
                    this,
                    () -> {
                        sessionManager.clearExpiredLockdown();
                        finish();
                        return Unit.INSTANCE;
                    }
            );
            setContentView(lockdownComposeView);

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if (!sessionManager.isLockdownActive()) {
                        finish();
                    }
                    // Strictly ignore back press while Phone Lockdown is active
                }
            });
            return;
        }

        String targetPackage = getIntent().getStringExtra(EXTRA_TARGET_PACKAGE);
        String appName = getFriendlyAppName(targetPackage);

        ComposeView composeView = CoolingPlanetHost.createBlockedOverlayComposeView(
                this,
                appName,
                () -> {
                    navigateToHomeScreen();
                    return Unit.INSTANCE;
                }
        );
        setContentView(composeView);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToHomeScreen();
            }
        });
    }

    private String getFriendlyAppName(String packageName) {
        if (packageName == null) {
            return "App";
        }

        switch (packageName.toLowerCase()) {
            case UsageStatsHelper.PKG_INSTAGRAM:
                return "Instagram";
            case UsageStatsHelper.PKG_SNAPCHAT:
                return "Snapchat";
            case UsageStatsHelper.PKG_WHATSAPP:
                return "WhatsApp";
            case UsageStatsHelper.PKG_FACEBOOK:
                return "Facebook";
            case UsageStatsHelper.PKG_YOUTUBE:
                return "YouTube";
            default:
                try {
                    PackageManager pm = getPackageManager();
                    ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
                    CharSequence label = pm.getApplicationLabel(ai);
                    return label.toString();
                } catch (PackageManager.NameNotFoundException e) {
                    return packageName;
                }
        }
    }

    private void navigateToHomeScreen() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        finish();
    }
}
