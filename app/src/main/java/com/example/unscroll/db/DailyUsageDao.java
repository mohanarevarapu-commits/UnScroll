package com.example.unscroll.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * DAO interface for daily usage logs.
 */
@Dao
public interface DailyUsageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(DailyUsageEntity dailyUsage);

    @Query("SELECT * FROM daily_usage ORDER BY timestamp DESC LIMIT 7")
    List<DailyUsageEntity> getLast7DaysUsage();

    @Query("SELECT * FROM daily_usage WHERE dateString = :dateString LIMIT 1")
    DailyUsageEntity getUsageByDate(String dateString);
}
