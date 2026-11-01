package com.example.unscroll.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Main Room Database singleton for UnScroll application.
 */
@Database(
        entities = {
                AppEntity.class,
                BlockedNotificationEntity.class,
                FocusSessionEntity.class,
                DailyUsageEntity.class,
                ReflectionEntity.class
        },
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "unscroll_db";
    private static volatile AppDatabase INSTANCE;

    public abstract AppDao appDao();
    public abstract NotificationDao notificationDao();
    public abstract SessionDao sessionDao();
    public abstract DailyUsageDao dailyUsageDao();
    public abstract ReflectionDao reflectionDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DB_NAME
                            )
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
