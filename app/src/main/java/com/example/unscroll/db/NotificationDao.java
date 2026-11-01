package com.example.unscroll.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * DAO interface for managing blocked notification records in Room DB with advanced analytics queries.
 */
@Dao
public interface NotificationDao {

    @Insert
    void insert(BlockedNotificationEntity notification);

    @Query("SELECT * FROM blocked_notifications ORDER BY timestamp DESC")
    List<BlockedNotificationEntity> getAllNotifications();

    @Query("SELECT * FROM blocked_notifications WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    List<BlockedNotificationEntity> getNotificationsSince(long startTime);

    @Query("SELECT COUNT(*) FROM blocked_notifications")
    int getNotificationCount();

    @Query("SELECT COUNT(*) FROM blocked_notifications WHERE timestamp >= :startTime")
    int getNotificationCountSince(long startTime);

    @Query("SELECT COUNT(*) FROM blocked_notifications WHERE category = :category AND timestamp >= :startTime")
    int getCountByCategorySince(String category, long startTime);

    @Query("SELECT COUNT(DISTINCT packageName) FROM blocked_notifications WHERE timestamp >= :startTime")
    int getDistinctDistractingAppsCountSince(long startTime);

    @Query("SELECT COUNT(DISTINCT packageName) FROM blocked_notifications WHERE category = :category AND timestamp >= :startTime")
    int getDistinctAppsByCategorySince(String category, long startTime);

    @Query("SELECT COUNT(*) FROM blocked_notifications WHERE packageName = :packageName")
    int getCountForPackage(String packageName);

    @Query("DELETE FROM blocked_notifications")
    void deleteAll();
}
