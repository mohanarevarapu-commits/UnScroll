package com.example.unscroll.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.unscroll.db.AppDatabase;
import com.example.unscroll.db.DailyUsageEntity;
import com.example.unscroll.usage.UsageStatsHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * WorkManager Worker that queries UsageStatsManager and persists daily social vs other screen-time logs into Room DB.
 */
public class UsageSyncWorker extends Worker {

    public UsageSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        // Query today's social media usage
        UsageStatsHelper.SocialAppUsageData usageData = UsageStatsHelper.queryTodaySocialUsage(context);
        long socialMillis = usageData.getTotalSocialTimeMillis();

        // Estimate non-social / other screen time (e.g. 1.5x social or baseline)
        long otherMillis = Math.max(30 * 60 * 1000L, socialMillis / 2);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String todayStr = sdf.format(new Date());
        long now = System.currentTimeMillis();

        DailyUsageEntity entity = new DailyUsageEntity(todayStr, socialMillis, otherMillis, now);

        AppDatabase db = AppDatabase.getInstance(context);
        db.dailyUsageDao().insertOrUpdate(entity);

        return Result.success();
    }
}
