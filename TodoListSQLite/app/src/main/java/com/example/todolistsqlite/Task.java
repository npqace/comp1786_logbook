package com.example.todolistsqlite;

import java.time.LocalDate;

public class Task {
    private long id;
    private String name;
    private LocalDate date;
    private boolean completed;

    public Task(String name, LocalDate date) {
        this.name = name;
        this.date = date;
        this.completed = false;
    }

    public Task(long id, String name, LocalDate date, boolean completed) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.completed = completed;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

