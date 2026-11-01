package com.example.unscroll.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing a focus session and its parameters/status.
 */
@Entity(tableName = "focus_sessions")
public class FocusSessionEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private int durationMinutes;
    private boolean strictModeEnabled;
    private String pinCode;
    private boolean isCompleted;
    private long startTimeMillis;
    private long endTimeMillis;

    public FocusSessionEntity(int durationMinutes, boolean strictModeEnabled, String pinCode, boolean isCompleted, long startTimeMillis, long endTimeMillis) {
        this.durationMinutes = durationMinutes;
        this.strictModeEnabled = strictModeEnabled;
        this.pinCode = pinCode;
        this.isCompleted = isCompleted;
        this.startTimeMillis = startTimeMillis;
        this.endTimeMillis = endTimeMillis;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isStrictModeEnabled() {
        return strictModeEnabled;
    }

    public void setStrictModeEnabled(boolean strictModeEnabled) {
        this.strictModeEnabled = strictModeEnabled;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }

    public void setEndTimeMillis(long endTimeMillis) {
        this.endTimeMillis = endTimeMillis;
    }
}
