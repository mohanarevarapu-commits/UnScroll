package com.example.unscroll;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.unscroll.usage.UsageStatsHelper;

/**
 * Activity presenting a 5-item permissions checklist with live status indicators.
 */
public class PermissionsActivity extends AppCompatActivity {

    private TextView tvUsageStatus;
    private Button btnGrantUsage;

    private TextView tvOverlayStatus;
    private Button btnGrantOverlay;

    private TextView tvAccessibilityStatus;
    private Button btnGrantAccessibility;

    private TextView tvNotificationStatus;
    private Button btnGrantNotification;

    private TextView tvBatteryStatus;
    private Button btnGrantBattery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        tvUsageStatus = findViewById(R.id.tvUsageStatus);
        btnGrantUsage = findViewById(R.id.btnGrantUsage);

        tvOverlayStatus = findViewById(R.id.tvOverlayStatus);
        btnGrantOverlay = findViewById(R.id.btnGrantOverlay);

        tvAccessibilityStatus = findViewById(R.id.tvAccessibilityStatus);
        btnGrantAccessibility = findViewById(R.id.btnGrantAccessibility);

        tvNotificationStatus = findViewById(R.id.tvNotificationStatus);
        btnGrantNotification = findViewById(R.id.btnGrantNotification);

        tvBatteryStatus = findViewById(R.id.tvBatteryStatus);
        btnGrantBattery = findViewById(R.id.btnGrantBattery);

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

        btnGrantBattery.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName()));
            try {
                startActivity(intent);
            } catch (Exception e) {
                Intent fallback = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                startActivity(fallback);
            }
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

        // 5. Battery Optimization Exemption
        boolean batteryGranted = isBatteryOptimizationExempt(this);
        updateStatusUI(tvBatteryStatus, btnGrantBattery, batteryGranted);
    }

    private boolean isBatteryOptimizationExempt(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            return pm.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return false;
    }

    private void updateStatusUI(TextView statusTv, Button grantBtn, boolean granted) {
        if (granted) {
            statusTv.setText("✓ Granted");
            statusTv.setTextColor(getResources().getColor(R.color.active_green, getTheme()));
            grantBtn.setVisibility(View.GONE);
        } else {
            statusTv.setText("✗ Not Granted");
            statusTv.setTextColor(getResources().getColor(R.color.warning_red, getTheme()));
            grantBtn.setVisibility(View.VISIBLE);
        }
    }
}
