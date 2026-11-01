package com.example.unscroll.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing an application and its blocked status.
 */
@Entity(tableName = "apps")
public class AppEntity {

    @PrimaryKey
    @NonNull
    private String packageName;
    private String appName;
    private boolean isBlocked;

    public AppEntity(@NonNull String packageName, String appName, boolean isBlocked) {
        this.packageName = packageName;
        this.appName = appName;
        this.isBlocked = isBlocked;
    }

    @NonNull
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(@NonNull String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}
