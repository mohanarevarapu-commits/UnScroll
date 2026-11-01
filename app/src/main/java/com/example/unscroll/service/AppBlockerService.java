package com.example.unscroll.service;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.unscroll.BlockOverlayActivity;
import com.example.unscroll.db.AppDatabase;
import com.example.unscroll.session.ScreenLockHelper;
import com.example.unscroll.session.SessionManager;
import com.example.unscroll.usage.UsageStatsHelper;

import java.util.Objects;

/**
 * AccessibilityService that monitors app usage, focus sessions, Room DB blocked apps,
 * detects YouTube Shorts feeds, and enforces strict hardware Screen Lock during Phone Lockdown Mode.
 */
public class AppBlockerService extends AccessibilityService {

    private static final String TAG = "AppBlockerService";
    public static final String PREFS_NAME = "unscroll_prefs";
    public static final String KEY_APP_BLOCKER_ENABLED = "app_blocker_enabled";
    public static final String KEY_SNOOZE_MODE_ENABLED = "snooze_mode_enabled";

    private static volatile AppBlockerService instance;

    private final BroadcastReceiver userPresentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            SessionManager sessionManager = SessionManager.getInstance(context);
            if (sessionManager.isLockdownActive() && !ScreenLockHelper.isPhoneCallActiveOrRinging(context)) {
                Log.d(TAG, "Device unlocked during active Phone Lockdown — re-engaging Lockdown screen & locking screen.");
                launchLockdownOverlay();
                lockScreenViaAccessibility();
            }
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_USER_PRESENT);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            registerReceiver(userPresentReceiver, filter);
        } catch (Exception e) {
            Log.w(TAG, "Failed to register userPresentReceiver", e);
        }
        Log.d(TAG, "AppBlockerService connected.");
    }

    @Override
    public void onDestroy() {
        if (instance == this) {
            instance = null;
        }
        try {
            unregisterReceiver(userPresentReceiver);
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }

    /**
     * Immediately locks the physical device screen via AccessibilityService (API 28+).
     */
    public static boolean lockScreenViaAccessibility() {
        AppBlockerService service = instance;
        if (service != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN);
        }
        return false;
    }

    public static boolean isServiceRunning() {
        return instance != null;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) {
            return;
        }

        CharSequence packageNameSeq = event.getPackageName();
        if (packageNameSeq == null) {
            return;
        }

        String packageName = packageNameSeq.toString();

        // Ignore events from our own application
        if (Objects.equals(getPackageName(), packageName)) {
            return;
        }

        SessionManager sessionManager = SessionManager.getInstance(this);

        // 1. Strict Phone Lockdown Mode (15 mins to 5 hours):
        // Blocks every foreground app & launcher except Phone Calls, Incoming Call UI, and Emergency SOS
        if (sessionManager.isLockdownActive()) {
            if (isAllowedDuringLockdown(packageName)) {
                return;
            }
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                Log.d(TAG, "Phone Lockdown active! Intercepting package & locking screen: " + packageName);
                launchLockdownOverlay();
                ScreenLockHelper.lockDeviceScreenNow(this);
            }
            return;
        }

        boolean sessionActive = sessionManager.isSessionActive();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isBlockerEnabled = prefs.getBoolean(KEY_APP_BLOCKER_ENABLED, true);
        boolean isSnoozeEnabled = prefs.getBoolean(KEY_SNOOZE_MODE_ENABLED, false);

        // Active Focus Session OR App Blocker Enabled (without snooze)
        if (sessionActive || (isBlockerEnabled && !isSnoozeEnabled)) {
            if (sessionManager.isEmergencyBypassActive(packageName)) {
                return;
            }

            // Check Room DB blocked list or default target social apps
            Boolean dbBlocked = AppDatabase.getInstance(this).appDao().isAppBlocked(packageName);
            boolean isBlockedInDb = Boolean.TRUE.equals(dbBlocked);
            boolean isTargetSocial = isTargetSocialApp(packageName);

            if (isBlockedInDb || isTargetSocial) {
                if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    Log.d(TAG, "Blocked app detected: " + packageName + ". Launching overlay.");
                    launchBlockOverlay(packageName);
                    return;
                }
            }

            // YouTube Shorts Detection
            if (UsageStatsHelper.PKG_YOUTUBE.equalsIgnoreCase(packageName)) {
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                if (rootNode != null) {
                    if (detectYouTubeShorts(rootNode)) {
                        Log.d(TAG, "YouTube Shorts detected! Launching overlay.");
                        launchBlockOverlay(packageName);
                    }
                    rootNode.recycle();
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "AppBlockerService interrupted.");
    }

    /**
     * During Phone Lockdown Mode, only incoming/outgoing Phone Calls, Telecom/In-Call UI,
     * and Emergency SOS dialers are permitted.
     */
    public static boolean isPackageAllowedDuringLockdown(String packageName) {
        if (packageName == null) {
            return true;
        }
        String lower = packageName.toLowerCase();
        return lower.contains("incallui")
                || lower.contains("dialer")
                || lower.contains("telecom")
                || lower.contains("emergency")
                || lower.contains("safetyhub")
                || lower.equals("com.android.phone")
                || lower.equals("com.android.systemui");
    }

    private boolean isAllowedDuringLockdown(String packageName) {
        return isPackageAllowedDuringLockdown(packageName);
    }

    private boolean isTargetSocialApp(String packageName) {
        for (String targetPkg : UsageStatsHelper.TARGET_PACKAGES) {
            if (targetPkg.equalsIgnoreCase(packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean detectYouTubeShorts(AccessibilityNodeInfo node) {
        if (node == null) {
            return false;
        }

        CharSequence viewIdRes = node.getViewIdResourceName();
        if (viewIdRes != null) {
            String idStr = viewIdRes.toString().toLowerCase();
            if (idStr.contains("shorts") || idStr.contains("shorts_player") || idStr.contains("reel_player") || idStr.contains("shorts_container")) {
                return true;
            }
        }

        CharSequence textSeq = node.getText();
        if (textSeq != null) {
            String text = textSeq.toString().toLowerCase();
            if (text.equals("shorts") || text.contains("shorts_player")) {
                return true;
            }
        }

        CharSequence contentDescSeq = node.getContentDescription();
        if (contentDescSeq != null) {
            String desc = contentDescSeq.toString().toLowerCase();
            if (desc.equals("shorts") || desc.contains("shorts_player")) {
                return true;
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                boolean found = detectYouTubeShorts(child);
                child.recycle();
                if (found) {
                    return true;
                }
            }
        }

        return false;
    }

    private void launchBlockOverlay(String targetPackage) {
        Intent intent = new Intent(this, BlockOverlayActivity.class);
        intent.putExtra(BlockOverlayActivity.EXTRA_TARGET_PACKAGE, targetPackage);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void launchLockdownOverlay() {
        Intent intent = new Intent(this, BlockOverlayActivity.class);
        intent.putExtra(BlockOverlayActivity.EXTRA_LOCKDOWN_MODE, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
