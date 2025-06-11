package com.example.simpletodolist;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText taskNameInput;
    private ImageButton datePickerBtn;
    private Button addTaskBtn;
    private LinearLayout todayTasksContainer;
    private LinearLayout futureTasksContainer;
    private TextView selectedDateText;

    private List<Task> tasks;
    private LocalDate selectedDate;
    private DateTimeFormatter dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeData();
        setupEventListeners();
    }

    private void initializeViews() {
        taskNameInput = findViewById(R.id.taskNameInput);
        datePickerBtn = findViewById(R.id.datePickerButton);
        addTaskBtn = findViewById(R.id.addTaskButton);
        todayTasksContainer = findViewById(R.id.todayTasksContainer);
        futureTasksContainer = findViewById(R.id.futureTasksContainer);
        selectedDateText = findViewById(R.id.selectedDateText);
    }

    private void initializeData() {
        tasks = new ArrayList<>();
        selectedDate = null; // Initialize selectedDate as null
        dateFormat = DateTimeFormatter.ofPattern("dd MMM, yyyy");
    }

    private void setupEventListeners() {
        datePickerBtn.setOnClickListener(v -> showDatePicker());
        addTaskBtn.setOnClickListener(v -> addTask());
    }

    private void showDatePicker() {
        LocalDate initialDate = selectedDate != null ? selectedDate : LocalDate.now();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
          this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
              selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
              updateSelectedDateText();
                },
                initialDate.getYear(),
                initialDate.getMonthValue() - 1,
                initialDate.getDayOfMonth()
        );

        // Set minimum date to today (prevent selecting past dates)
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.show();
    }
    private void updateSelectedDateText() {
        if (selectedDate != null) {
            selectedDateText.setText(selectedDate.format(dateFormat));
        } else {
            selectedDateText.setText(""); 
        }
    }

    private void addTask() {
        String taskName = taskNameInput.getText().toString().trim();
        if (!taskName.isEmpty()) {
            LocalDate taskDate;
            if (selectedDate != null) {
                taskDate = selectedDate;
            } else {
                taskDate = LocalDate.now(); // Default to today if no date is selected
            }
            Task newTask = new Task(taskName, taskDate);
            tasks.add(newTask);
            taskNameInput.setText("");
            selectedDate = null; // Reset selectedDate after adding a task
            updateSelectedDateText(); // Update the text to be empty

            refreshTaskLists();
        }
    }

    private void refreshTaskLists() {
        todayTasksContainer.removeAllViews();
        futureTasksContainer.removeAllViews();

        LocalDate today = LocalDate.now();

        // Sort tasks by date (ascending) and then by completion status (incomplete first)
        tasks.sort(Comparator.comparing(Task::getDate)
                .thenComparing(Task::isCompleted));

        for (Task task : tasks) {
            View taskView = createTaskView(task);
            if (task.getDate().equals(today)) {
                todayTasksContainer.addView(taskView); // Keep adding to container directly for now
            } else if (task.getDate().isAfter(today)) {
                futureTasksContainer.addView(taskView); // Keep adding to container directly for now
            }
        }
    }

    @NonNull
    private View createTaskView(@NonNull Task task) {
        LinearLayout taskLayout = new LinearLayout(this);
        taskLayout.setOrientation(LinearLayout.HORIZONTAL);
        taskLayout.setPadding(16, 8, 16, 8);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 4, 0, 4);
        taskLayout.setLayoutParams(layoutParams);

        // Checkbox
        CheckBox checkBox = new CheckBox(this);
        checkBox.setChecked(task.isCompleted());
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            updateTaskAppearance(taskLayout, task);
        });

        // Task name and date
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView taskNameText = new TextView(this);
        taskNameText.setText(task.getName());
        taskNameText.setTextSize(16);
        taskNameText.setTextColor(ContextCompat.getColor(this, R.color.text_primary));

        TextView taskDateText = new TextView(this);
        taskDateText.setText(task.getDate().format(dateFormat));
        taskDateText.setTextSize(12);
        taskDateText.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

        textLayout.addView(taskNameText);
        textLayout.addView(taskDateText);

        // Edit button
        Button editButton = new Button(this);
        editButton.setText(R.string.edit_text);
        editButton.setBackgroundResource(R.drawable.button_edit);
        editButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        editButton.setOnClickListener(v -> showEditDialog(task));

        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        editParams.setMargins(8, 0, 8, 0);
        editButton.setLayoutParams(editParams);

        // Delete button
        Button deleteButton = new Button(this);
        deleteButton.setText(R.string.delete_text);
        deleteButton.setBackgroundResource(R.drawable.button_delete);
        deleteButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        deleteButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(R.string.delete_task_title)
                .setMessage(getString(R.string.delete_task_confirmation) + " \"" + task.getName() + "\"?")
                .setPositiveButton(R.string.delete_text, (dialog, which) -> {
                    tasks.remove(task);
                    refreshTaskLists();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show());

        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        deleteButton.setLayoutParams(deleteParams);

        taskLayout.addView(checkBox);
        taskLayout.addView(textLayout);
        taskLayout.addView(editButton);
        taskLayout.addView(deleteButton);

        updateTaskAppearance(taskLayout, task);

        return taskLayout;
    }

    private void updateTaskAppearance(@NonNull LinearLayout taskLayout, @NonNull Task task) {
        LinearLayout textLayout = (LinearLayout) taskLayout.getChildAt(1);
        TextView taskNameText = (TextView) textLayout.getChildAt(0);
        TextView taskDateText = (TextView) textLayout.getChildAt(1);

        if (task.isCompleted()) {
            taskNameText.setPaintFlags(taskNameText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            taskDateText.setPaintFlags(taskDateText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            taskNameText.setAlpha(0.6f);
            taskDateText.setAlpha(0.6f);
        } else {
            taskNameText.setPaintFlags(taskNameText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            taskDateText.setPaintFlags(taskDateText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            taskNameText.setAlpha(1.0f);
            taskDateText.setAlpha(1.0f);
        }
    }

    private void showEditDialog(@NonNull Task task) {
        Dialog editDialog = new Dialog(this);
        editDialog.setContentView(R.layout.dialog_edit_task);
        editDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        EditText editTaskName = editDialog.findViewById(R.id.editTaskName);
        DatePicker editDatePicker = editDialog.findViewById(R.id.editDatePicker);
        Button saveButton = editDialog.findViewById(R.id.saveButton);
        Button cancelButton = editDialog.findViewById(R.id.cancelButton);

        // Set current values
        editTaskName.setText(task.getName());
        editDatePicker.init(
                task.getDate().getYear(),
                task.getDate().getMonthValue() - 1,
                task.getDate().getDayOfMonth(),
                null
        );

        // Set minimum date to today (prevent selecting past dates)
        editDatePicker.setMinDate(System.currentTimeMillis());

        cancelButton.setOnClickListener(v -> editDialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String newTaskName = editTaskName.getText().toString().trim();
            if (!newTaskName.isEmpty()) {
                task.setName(newTaskName);

                LocalDate newDate = LocalDate.of(
                        editDatePicker.getYear(),
                        editDatePicker.getMonth() + 1,
                        editDatePicker.getDayOfMonth()
                );
                task.setDate(newDate);

                refreshTaskLists();
                editDialog.dismiss();
            }
        });
        editDialog.show();
    }
}