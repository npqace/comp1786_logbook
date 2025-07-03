package com.example.todolistsqlite.database;

import androidx.room.TypeConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Converters {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMM, yyyy");

    @TypeConverter
    public static String fromLocalDate(LocalDate date) {
        return date != null ? date.format(FORMATTER) : null;
    }

    @TypeConverter
    public static LocalDate toLocalDate(String value) {
        if (value == null) return null;
        try {
            return LocalDate.parse(value, FORMATTER);
        } catch (DateTimeParseException e) {
            // Fallback to ISO
            return LocalDate.parse(value);
        }
    }
} 