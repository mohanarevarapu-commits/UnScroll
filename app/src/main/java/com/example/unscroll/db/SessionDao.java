package com.example.unscroll.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * DAO interface for managing focus sessions in Room DB.
 */
@Dao
public interface SessionDao {

    @Insert
    long insert(FocusSessionEntity session);

    @Update
    void update(FocusSessionEntity session);

    @Query("SELECT * FROM focus_sessions ORDER BY startTimeMillis DESC")
    List<FocusSessionEntity> getAllSessions();

    @Query("SELECT * FROM focus_sessions WHERE isCompleted = 0 ORDER BY startTimeMillis DESC LIMIT 1")
    FocusSessionEntity getActiveSession();

    @Query("SELECT * FROM focus_sessions WHERE id = :id")
    FocusSessionEntity getSessionById(long id);

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE isCompleted = 1")
    int getCompletedSessionsCount();

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE isCompleted = 1")
    Long getTotalFocusTimeMinutes();
}
