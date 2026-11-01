package com.example.unscroll.service;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.example.unscroll.AppIntelligence;
import com.example.unscroll.db.AppDatabase;
import com.example.unscroll.db.BlockedNotificationEntity;
import com.example.unscroll.session.SessionManager;
import com.example.unscroll.usage.UsageStatsHelper;

import java.util.concurrent.Executors;

/**
 * NotificationListenerService that cancels incoming notifications from blocked/social/marketing apps during focus sessions or snooze mode
 * and logs them into Room Database with intelligence category tags for the dashboard analytics.
 */
public class NotificationSnoozerService extends NotificationListenerService {

    private static final String TAG = "NotificationSnoozer";
    public static final String PREFS_NAME = "unscroll_prefs";
    public static final String KEY_SNOOZE_MODE_ENABLED = "snooze_mode_enabled";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null) {
            return;
        }

        String packageName = sbn.getPackageName();
        if (packageName == null || packageName.equals(getPackageName())) {
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isSnoozeEnabled = prefs.getBoolean(KEY_SNOOZE_MODE_ENABLED, false);
        boolean isSessionActive = SessionManager.getInstance(this).isSessionActive();

        if (!isSnoozeEnabled && !isSessionActive) {
            return;
        }

        Boolean dbBlocked = AppDatabase.getInstance(this).appDao().isAppBlocked(packageName);
        boolean isBlocked = Boolean.TRUE.equals(dbBlocked) || isTargetDistractingOrMarketingApp(packageName);

        if (isBlocked) {
            Log.d(TAG, "Cancelling & logging notification from: " + packageName);

            Notification notification = sbn.getNotification();
            String title = "";
            String text = "";

            if (notification != null && notification.extras != null) {
                Bundle extras = notification.extras;
                CharSequence titleSeq = extras.getCharSequence(Notification.EXTRA_TITLE);
                CharSequence textSeq = extras.getCharSequence(Notification.EXTRA_TEXT);

                if (titleSeq != null) title = titleSeq.toString();
                if (textSeq != null) text = textSeq.toString();
            }

            String appName = resolveAppName(packageName);
            String category = AppIntelligence.classifyPackage(packageName);
            long timestamp = sbn.getPostTime();
            if (timestamp <= 0) {
                timestamp = System.currentTimeMillis();
            }

            final String finalTitle = title;
            final String finalText = text;
            final String finalCategory = category;
            final long finalTimestamp = timestamp;

            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                db.notificationDao().insert(new BlockedNotificationEntity(
                        packageName, appName, finalTitle, finalText, finalTimestamp, finalCategory
                ));
            });

            try {
                cancelNotification(sbn.getKey());
            } catch (Exception e) {
                Log.e(TAG, "Failed to cancel notification for key: " + sbn.getKey(), e);
            }
        }
    }

    private boolean isTargetDistractingOrMarketingApp(String packageName) {
        for (String targetPkg : UsageStatsHelper.TARGET_PACKAGES) {
            if (targetPkg.equalsIgnoreCase(packageName)) {
                return true;
            }
        }
        for (String ecommercePkg : AppIntelligence.ECOMMERCE_PACKAGES) {
            if (ecommercePkg.equalsIgnoreCase(packageName)) {
                return true;
            }
        }
        return false;
    }

    private String resolveAppName(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationLabel(ai).toString();
        } catch (Exception e) {
            return packageName;
        }
    }
}
