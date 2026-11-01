package com.example.unscroll.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.unscroll.BlockOverlayActivity;
import com.example.unscroll.session.SessionManager;
import com.example.unscroll.worker.UsageSyncWorker;

import java.util.concurrent.TimeUnit;

/**
 * BroadcastReceiver catching ACTION_BOOT_COMPLETED to resume active focus sessions,
 * active Phone Lockdown sessions, and schedule WorkManager jobs.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        Log.d(TAG, "UnScroll BootReceiver triggered after device reboot.");

        // 1. Resume active Phone Lockdown or Focus Session post-reboot
        SessionManager sessionManager = SessionManager.getInstance(context);
        if (sessionManager.isLockdownActive()) {
            Log.d(TAG, "Active Phone Lockdown restored post-reboot. Remaining time: " + sessionManager.getLockdownRemainingMillis() + "ms");
            Intent lockdownIntent = new Intent(context, BlockOverlayActivity.class);
            lockdownIntent.putExtra(BlockOverlayActivity.EXTRA_LOCKDOWN_MODE, true);
            lockdownIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(lockdownIntent);
        } else if (sessionManager.isSessionActive()) {
            Log.d(TAG, "Active focus session restored post-reboot. Remaining time: " + sessionManager.getRemainingTimeMillis() + "ms");
        }

        // 2. Schedule WorkManager periodic usage sync job
        PeriodicWorkRequest syncWorkRequest =
                new PeriodicWorkRequest.Builder(UsageSyncWorker.class, 15, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "UsageSyncWork",
                ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest
        );
    }
}
