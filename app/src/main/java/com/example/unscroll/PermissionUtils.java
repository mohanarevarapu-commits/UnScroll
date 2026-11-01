package com.example.unscroll;

import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import com.example.unscroll.service.AppBlockerService;
import com.example.unscroll.service.NotificationSnoozerService;
import com.example.unscroll.usage.UsageStatsHelper;

/**
 * Utility class providing robust permission and service enabling state detection methods.
 */
public class PermissionUtils {

    /**
     * Checks if AppBlockerService is enabled in Android Accessibility settings.
     * Checks Settings.Secure.ACCESSIBILITY_ENABLED flag and specifically verifies if
     * com.example.unscroll/com.example.unscroll.service.AppBlockerService is present in
     * Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES.
     *
     * @param context Application context
     * @return true if accessibility service is enabled for UnScroll's AppBlockerService, false otherwise.
     */
    public static boolean isAccessibilityServiceEnabled(Context context) {
        if (context == null) {
            return false;
        }

        // 1. Verify global ACCESSIBILITY_ENABLED flag is set to 1
        int accessibilityEnabled;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED
            );
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }

        if (accessibilityEnabled != 1) {
            return false;
        }

        // 2. Query enabled accessibility services string list
        String settingValue = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );

        if (settingValue == null || settingValue.isEmpty()) {
            return false;
        }

        ComponentName expectedComponent = new ComponentName(context, AppBlockerService.class);
        String expectedFull = expectedComponent.flattenToString();
        String expectedShort = expectedComponent.flattenToShortString();
        String targetPackage = context.getPackageName();
        String targetClassName = AppBlockerService.class.getName();

        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(settingValue);

        while (splitter.hasNext()) {
            String serviceString = splitter.next();
            if (serviceString.equalsIgnoreCase(expectedFull) || serviceString.equalsIgnoreCase(expectedShort)) {
                return true;
            }
            ComponentName cn = ComponentName.unflattenFromString(serviceString);
            if (cn != null && targetPackage.equalsIgnoreCase(cn.getPackageName())
                    && targetClassName.equalsIgnoreCase(cn.getClassName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if NotificationSnoozerService is enabled in Notification Listener settings.
     * Specifically verifies if com.example.unscroll/com.example.unscroll.service.NotificationSnoozerService is present in
     * Settings.Secure.ENABLED_NOTIFICATION_LISTENERS.
     *
     * @param context Application context
     * @return true if notification listener service is enabled for UnScroll's NotificationSnoozerService, false otherwise.
     */
    public static boolean isNotificationListenerEnabled(Context context) {
        if (context == null) {
            return false;
        }

        String settingValue = Settings.Secure.getString(
                context.getContentResolver(),
                "enabled_notification_listeners"
        );

        if (settingValue == null || settingValue.isEmpty()) {
            return false;
        }

        ComponentName expectedComponent = new ComponentName(context, NotificationSnoozerService.class);
        String expectedFull = expectedComponent.flattenToString();
        String expectedShort = expectedComponent.flattenToShortString();
        String targetPackage = context.getPackageName();
        String targetClassName = NotificationSnoozerService.class.getName();

        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(settingValue);

        while (splitter.hasNext()) {
            String serviceString = splitter.next();
            if (serviceString.equalsIgnoreCase(expectedFull) || serviceString.equalsIgnoreCase(expectedShort)) {
                return true;
            }
            ComponentName cn = ComponentName.unflattenFromString(serviceString);
            if (cn != null && targetPackage.equalsIgnoreCase(cn.getPackageName())
                    && targetClassName.equalsIgnoreCase(cn.getClassName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Counts how many of the permissions on the setup screen are still off:
     * Usage Access, Display Over Other Apps, Accessibility Service and Notification Access.
     */
    public static int countMissingPermissions(Context context) {
        int missing = 0;
        if (!UsageStatsHelper.hasUsageStatsPermission(context)) missing++;
        if (!Settings.canDrawOverlays(context)) missing++;
        if (!isAccessibilityServiceEnabled(context)) missing++;
        if (!isNotificationListenerEnabled(context)) missing++;
        return missing;
    }
}
