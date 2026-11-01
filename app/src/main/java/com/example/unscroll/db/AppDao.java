package com.example.unscroll.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * DAO interface for managing blocked application records in Room DB.
 */
@Dao
public interface AppDao {

    @Query("SELECT * FROM apps")
    List<AppEntity> getAllApps();

    @Query("SELECT * FROM apps WHERE isBlocked = 1")
    List<AppEntity> getBlockedApps();

    @Query("SELECT * FROM apps WHERE packageName = :packageName LIMIT 1")
    AppEntity getAppByPackage(String packageName);

    @Query("SELECT isBlocked FROM apps WHERE packageName = :packageName LIMIT 1")
    Boolean isAppBlocked(String packageName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(AppEntity app);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AppEntity> apps);

    @Query("DELETE FROM apps WHERE packageName = :packageName")
    void deleteByPackage(String packageName);

    @Query("SELECT COUNT(*) FROM apps WHERE isBlocked = 1")
    int getBlockedAppsCount();
}
