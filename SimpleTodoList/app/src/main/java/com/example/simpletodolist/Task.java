package com.example.simpletodolist;

import java.time.LocalDate;

public class Task {
    private String name;
    private LocalDate date;
    private boolean isCompleted;

    public Task(String name, LocalDate date) {
        this.name = name;
        this.date = date;
        this.isCompleted = false;
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
}
