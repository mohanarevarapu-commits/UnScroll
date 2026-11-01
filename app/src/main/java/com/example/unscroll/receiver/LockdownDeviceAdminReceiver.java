package com.example.unscroll.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * DeviceAdminReceiver enabling hardware screen locking (DevicePolicyManager.lockNow())
 * during UnScroll Phone Lockdown Mode (15 mins to 5 hours).
 */
public class LockdownDeviceAdminReceiver extends DeviceAdminReceiver {

    private static final String TAG = "LockdownDeviceAdmin";

    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        super.onEnabled(context, intent);
        Log.d(TAG, "UnScroll Lockdown Device Admin enabled.");
    }

    @Override
    public void onDisabled(@NonNull Context context, @NonNull Intent intent) {
        super.onDisabled(context, intent);
        Log.d(TAG, "UnScroll Lockdown Device Admin disabled.");
    }
}
