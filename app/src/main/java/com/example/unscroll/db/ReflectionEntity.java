package com.example.unscroll.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing a daily mindful reflection check-in entry in Room Database.
 * Stores timestamp, feeling (Mindful, Restless, Focused, Calm), and optional note.
 */
@Entity(tableName = "reflections")
public class ReflectionEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long timestamp;
    private String feeling; // e.g. "Mindful", "Restless", "Focused", "Calm"
    private String note;

    public ReflectionEntity(long timestamp, String feeling, String note) {
        this.timestamp = timestamp;
        this.feeling = feeling;
        this.note = note;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFeeling() {
        return feeling;
    }

    public void setFeeling(String feeling) {
        this.feeling = feeling;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
