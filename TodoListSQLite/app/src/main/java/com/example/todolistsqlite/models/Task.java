package com.example.todolistsqlite.models;

import java.time.LocalDate;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    private long id; // Database primary key
    private String name;
    private LocalDate date;
    @ColumnInfo(name = "completed")
    private boolean isCompleted;

    // Constructor for new tasks that are not yet stored in DB
    @Ignore
    public Task(String name, LocalDate date) {
        this(0, name, date, false);
    }

    // Constructor used when reading existing tasks from DB
    public Task(long id, String name, LocalDate date, boolean isCompleted) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.isCompleted = isCompleted;
    }

    // Getters
    public String getName() {
        return name;
    }
    public LocalDate getDate() {
        return date;
    }
    public boolean isCompleted() {
        return isCompleted;
    }
    public long getId() {
        return id;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public void setCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
    public void setId(long id) {
        this.id = id;
    }
}
