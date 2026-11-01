package com.example.unscroll.session;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.unscroll.receiver.LockdownDeviceAdminReceiver;
import com.example.unscroll.service.AppBlockerService;

/**
 * Helper that enforces physical screen locking (via Accessibility GLOBAL_ACTION_LOCK_SCREEN
 * or DevicePolicyManager.lockNow()) and immersive system-bar locking during Phone Lockdown Mode,
 * while preserving Incoming Calls and Emergency SOS access.
 */
public class ScreenLockHelper {

    private static final String TAG = "ScreenLockHelper";

    /**
     * Immediately locks the device hardware screen if AccessibilityService or DeviceAdmin is active.
     * Does not lock if an incoming phone call is ringing or an active phone call is in progress.
     *
     * @return true if hardware screen lock was triggered, false otherwise.
     */
    public static boolean lockDeviceScreenNow(Context context) {
        if (context == null) {
            return false;
        }

        if (isPhoneCallActiveOrRinging(context)) {
            Log.d(TAG, "Phone call active or ringing — skipping screen lock so user can talk/answer.");
            return false;
        }

        // 1. Try AccessibilityService.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN) (Android 9+)
        if (AppBlockerService.lockScreenViaAccessibility()) {
            Log.d(TAG, "Device screen locked via AccessibilityService GLOBAL_ACTION_LOCK_SCREEN.");
            return true;
        }

        // 2. Try DevicePolicyManager.lockNow() if Device Admin is enabled
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminComponent = new ComponentName(context, LockdownDeviceAdminReceiver.class);
            if (dpm != null && dpm.isAdminActive(adminComponent)) {
                dpm.lockNow();
                Log.d(TAG, "Device screen locked via DevicePolicyManager.lockNow().");
                return true;
            }
        } catch (Exception e) {
            Log.w(TAG, "Unable to lock via DevicePolicyManager", e);
        }

        return false;
    }

    /**
     * Checks if either Accessibility Service or Device Admin is active to perform hardware screen lock.
     */
    public static boolean canLockHardwareScreen(Context context) {
        if (AppBlockerService.isServiceRunning()) {
            return true;
        }
        return isDeviceAdminActive(context);
    }

    public static boolean isDeviceAdminActive(Context context) {
        if (context == null) {
            return false;
        }
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminComponent = new ComponentName(context, LockdownDeviceAdminReceiver.class);
            return dpm != null && dpm.isAdminActive(adminComponent);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Launches the system prompt to grant Device Admin permission for hardware screen locking.
     */
    public static void requestDeviceAdminPermission(Context context) {
        try {
            ComponentName adminComponent = new ComponentName(context, LockdownDeviceAdminReceiver.class);
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            intent.putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "UnScroll uses Device Admin to immediately lock your screen during Phone Lockdown Mode (15m – 5h) while still allowing Incoming Calls and Emergency SOS."
            );
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        } catch (Exception e) {
            Log.w(TAG, "Failed to launch Device Admin request", e);
        }
    }

    /**
     * Returns true when the phone is ringing (incoming call) or in an active voice/VoIP call.
     */
    public static boolean isPhoneCallActiveOrRinging(Context context) {
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                int mode = audioManager.getMode();
                return mode == AudioManager.MODE_RINGTONE
                        || mode == AudioManager.MODE_IN_CALL
                        || mode == AudioManager.MODE_IN_COMMUNICATION;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Enforces full-screen immersive lockdown mode on the given Activity window:
     * hides status & navigation bars so the user cannot swipe away or pull down notifications.
     */
    public static void setImmersiveLockdownMode(Activity activity, boolean enabled) {
        if (activity == null) {
            return;
        }
        Window window = activity.getWindow();
        if (window == null) {
            return;
        }

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());

        if (enabled) {
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
            controller.hide(WindowInsetsCompat.Type.systemBars());
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars());
        }
    }
}
