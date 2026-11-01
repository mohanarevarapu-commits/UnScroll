package com.example.unscroll.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing daily screen-time logs (social vs other app usage).
 */
@Entity(tableName = "daily_usage")
public class DailyUsageEntity {

    @PrimaryKey
    @NonNull
    private String dateString; // e.g., "2026-03-30"
    private long socialUsageMillis;
    private long otherUsageMillis;
    private long timestamp;

    public DailyUsageEntity(@NonNull String dateString, long socialUsageMillis, long otherUsageMillis, long timestamp) {
        this.dateString = dateString;
        this.socialUsageMillis = socialUsageMillis;
        this.otherUsageMillis = otherUsageMillis;
        this.timestamp = timestamp;
    }

    @NonNull
    public String getDateString() {
        return dateString;
    }

    public void setDateString(@NonNull String dateString) {
        this.dateString = dateString;
    }

    public long getSocialUsageMillis() {
        return socialUsageMillis;
    }

    public void setSocialUsageMillis(long socialUsageMillis) {
        this.socialUsageMillis = socialUsageMillis;
    }

    public long getOtherUsageMillis() {
        return otherUsageMillis;
    }

    public void setOtherUsageMillis(long otherUsageMillis) {
        this.otherUsageMillis = otherUsageMillis;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
