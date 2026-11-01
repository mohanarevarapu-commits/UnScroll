package com.example.unscroll;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.unscroll.usage.UsageStatsHelper;
import com.google.android.material.button.MaterialButton;

/**
 * Gentle Permission Setup Activity listing required permissions with plain-language reasons
 * and reactive green tick (✓) status checks.
 */
public class PermissionsSetupActivity extends AppCompatActivity {

    private TextView tvUsageStatus, tvOverlayStatus, tvAccessibilityStatus, tvNotificationStatus;
    private Button btnGrantUsage, btnGrantOverlay, btnGrantAccessibility, btnGrantNotification;
    private MaterialButton btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_setup);

        tvUsageStatus = findViewById(R.id.tvUsageStatus);
        btnGrantUsage = findViewById(R.id.btnGrantUsage);

        tvOverlayStatus = findViewById(R.id.tvOverlayStatus);
        btnGrantOverlay = findViewById(R.id.btnGrantOverlay);

        tvAccessibilityStatus = findViewById(R.id.tvAccessibilityStatus);
        btnGrantAccessibility = findViewById(R.id.btnGrantAccessibility);

        tvNotificationStatus = findViewById(R.id.tvNotificationStatus);
        btnGrantNotification = findViewById(R.id.btnGrantNotification);

        btnContinue = findViewById(R.id.btnContinue);

        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    private void setupClickListeners() {
        btnGrantUsage.setOnClickListener(v -> UsageStatsHelper.openUsageAccessSettings(this));

        btnGrantOverlay.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        });

        btnGrantAccessibility.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        btnGrantNotification.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        });

        btnContinue.setOnClickListener(v -> {
            OnboardingActivity.markComplete(this);
            // Reuse the Home screen if it's already open (opened from Home or Settings), otherwise start it
            Intent intent = new Intent(PermissionsSetupActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void checkPermissions() {
        // 1. Usage Access
        boolean usageGranted = UsageStatsHelper.hasUsageStatsPermission(this);
        updateStatusUI(tvUsageStatus, btnGrantUsage, usageGranted);

        // 2. Display Over Other Apps
        boolean overlayGranted = Settings.canDrawOverlays(this);
        updateStatusUI(tvOverlayStatus, btnGrantOverlay, overlayGranted);

        // 3. Accessibility Service
        boolean accessibilityGranted = PermissionUtils.isAccessibilityServiceEnabled(this);
        updateStatusUI(tvAccessibilityStatus, btnGrantAccessibility, accessibilityGranted);

        // 4. Notification Listener Access
        boolean notificationGranted = PermissionUtils.isNotificationListenerEnabled(this);
        updateStatusUI(tvNotificationStatus, btnGrantNotification, notificationGranted);
    }

    private void updateStatusUI(TextView statusTv, Button grantBtn, boolean granted) {
        if (granted) {
            statusTv.setText("✓ Granted");
            statusTv.setTextColor(getResources().getColor(R.color.active_green, getTheme()));
            grantBtn.setVisibility(View.GONE);
        } else {
            statusTv.setText("Not Granted");
            statusTv.setTextColor(getResources().getColor(R.color.warning_red, getTheme()));
            grantBtn.setVisibility(View.VISIBLE);
        }
    }
}
