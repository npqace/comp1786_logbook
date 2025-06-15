package com.example.todolistsqlite;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "todolist_db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "tasks";
    public static final String ID_COLUMN = "id";
    public static final String NAME_COLUMN = "name";
    public static final String DATE_COLUMN = "date";
    public static final String IS_COMPLETED_COLUMN = "completed";

    private SQLiteDatabase database;

    private static final String TABLE_CREATE = String.format(
            "CREATE TABLE %s (" +
                    "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "%s TEXT NOT NULL, " +
                    "%s TEXT NOT NULL, " +
                    "%s INTEGER DEFAULT 0)",
            TABLE_NAME, ID_COLUMN, NAME_COLUMN, DATE_COLUMN, IS_COMPLETED_COLUMN
    );

    // The constructor makes a call to the method in the super class, passing the database name
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        database = getWritableDatabase();
    }

    // Overriding the onCreate() method which generates the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    // This method upgrades the database if the version number changes
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        Log.v(this.getClass().getName(), TABLE_NAME +
                "database upgrade to version" + newVersion + " - old data lost"
        );
        onCreate(db);
    }

    // Insert a new task
    public long insertTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        // ContentValues represents a single table row as a key/value map
        ContentValues rowValues = new ContentValues();

        rowValues.put(NAME_COLUMN, task.getName());
        rowValues.put(DATE_COLUMN, task.getDate().toString());
        rowValues.put(IS_COMPLETED_COLUMN, task.isCompleted() ? 1 : 0);

        long id = db.insert(TABLE_NAME, null, rowValues);
        db.close();
        return id;
    }

    // Get all tasks
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + DATE_COLUMN + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(ID_COLUMN));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(NAME_COLUMN));
                String dateString = cursor.getString(cursor.getColumnIndexOrThrow(DATE_COLUMN));
                boolean completed = cursor.getInt(cursor.getColumnIndexOrThrow(IS_COMPLETED_COLUMN)) == 1;

                LocalDate date = LocalDate.parse(dateString);
                Task task = new Task(id, name, date, completed);
                tasks.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return tasks;
    }

    // Update a task
    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(NAME_COLUMN, task.getName());
        values.put(DATE_COLUMN, task.getDate().toString());
        values.put(IS_COMPLETED_COLUMN, task.isCompleted() ? 1 : 0);

        int rowsAffected = db.update(TABLE_NAME, values,
                ID_COLUMN + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
        return rowsAffected;
    }

    // Delete a task
    public void deleteTask(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, ID_COLUMN + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Get task count
    public int getTaskCount() {
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    // Delete all completed tasks
    public void deleteCompletedTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, IS_COMPLETED_COLUMN + " = ?", new String[]{"1"});
        db.close();
    }

//    public String getDetails() {
//        Cursor results = database.query(TABLE_NAME,
//                new String[]{ID_COLUMN, NAME_COLUMN, DATE_COLUMN, IS_COMPLETED_COLUMN},
//                null, null, null, null, NAME_COLUMN
//        );
//        String resultText = "";
//
//        // Moves to the first position of the result set
//        results.moveToFirst();
//
//        // Checks whether there are more rows in the result set
//        while (!results.isAfterLast()) {
//
//            // Extracts the values from the row
//            int id = results.getInt(0);
//            String name = results.getString(1);
//            String date = results.getString(2);
//            String isCompleted = results.getString(3);
//
//            // Concatenates the text values
//            resultText += id + " " + name + " " + date + " " + isCompleted + "\n";
//
//            // Moves to the next row in the result set
//            results.moveToNext();
//        }
//
//        // Returns a long string of all results
//        return resultText;
//    }
}
