package com.example.unscroll.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object for Daily Mindful Reflection Journal.
 */
@Dao
public interface ReflectionDao {

    @Insert
    void insertReflection(ReflectionEntity reflection);

    @Query("SELECT * FROM reflections ORDER BY timestamp DESC")
    List<ReflectionEntity> getAllReflections();

    @Query("SELECT * FROM reflections WHERE timestamp >= :startOfDay ORDER BY timestamp DESC LIMIT 1")
    ReflectionEntity getTodayReflection(long startOfDay);
}
