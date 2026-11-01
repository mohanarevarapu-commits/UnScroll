package com.example.unscroll.db;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.unscroll.AppIntelligence;

/**
 * Entity representing a notification captured and snoozed during a focus session or snooze mode,
 * including its classification category (MARKETING, SOCIAL, IMPORTANT).
 */
@Entity(tableName = "blocked_notifications")
public class BlockedNotificationEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String packageName;
    private String appName;
    private String title;
    private String text;
    private long timestamp;
    private String category; // MARKETING, SOCIAL, IMPORTANT

    public BlockedNotificationEntity(String packageName, String appName, String title, String text, long timestamp, String category) {
        this.packageName = packageName;
        this.appName = appName;
        this.title = title;
        this.text = text;
        this.timestamp = timestamp;
        this.category = category != null ? category : AppIntelligence.classifyPackage(packageName);
    }

    @Ignore
    public BlockedNotificationEntity(String packageName, String appName, String title, String text, long timestamp) {
        this.packageName = packageName;
        this.appName = appName;
        this.title = title;
        this.text = text;
        this.timestamp = timestamp;
        this.category = AppIntelligence.classifyPackage(packageName);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
