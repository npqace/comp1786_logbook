package com.example.todolistsqlite.activites;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
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

import com.example.todolistsqlite.database.AppDatabase;
import com.example.todolistsqlite.R;
import com.example.todolistsqlite.models.Task;
import com.example.todolistsqlite.dao.TaskDao;
import com.google.android.material.tabs.TabLayout;

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
    private TabLayout tabLayout;
    private LinearLayout logContainer;
    private LinearLayout addTaskSection;
    
    // Empty state views
    private View mainEmptyState;
    private View historyEmptyState;

    private List<Task> tasks;
    private LocalDate selectedDate;
    private DateTimeFormatter dateFormat;

    // Obtain Room database DAO
    private TaskDao taskDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain Room database DAO
        taskDao = AppDatabase.getInstance(this).taskDao();

        initializeViews();
        initializeData();
        setupEventListeners();

        // Refresh lists initially so Log tab has content
        refreshTaskLists();
    }

    private void initializeViews() {
        taskNameInput = findViewById(R.id.taskNameInput);
        datePickerBtn = findViewById(R.id.datePickerButton);
        addTaskBtn = findViewById(R.id.addTaskButton);
        todayTasksContainer = findViewById(R.id.todayTasksContainer);
        futureTasksContainer = findViewById(R.id.futureTasksContainer);
        selectedDateText = findViewById(R.id.selectedDateText);
        addTaskSection = findViewById(R.id.addTaskSection);
        logContainer = findViewById(R.id.logContainer);
        tabLayout = findViewById(R.id.tabLayout);
        
        // Initialize empty state views
        createEmptyStateViews();
    }

    private void createEmptyStateViews() {
        LayoutInflater inflater = LayoutInflater.from(this);
        
        // Create empty state for main page (when no tasks exist at all)
        mainEmptyState = inflater.inflate(R.layout.empty_state_view, null);
        TextView mainTitle = mainEmptyState.findViewById(R.id.emptyStateTitle);
        TextView mainMessage = mainEmptyState.findViewById(R.id.emptyStateMessage);
        mainTitle.setText(R.string.empty_main_title);
        mainMessage.setText(R.string.empty_main_message);
        
        // Create empty state for history
        historyEmptyState = inflater.inflate(R.layout.empty_state_view, null);
        TextView historyTitle = historyEmptyState.findViewById(R.id.emptyStateTitle);
        TextView historyMessage = historyEmptyState.findViewById(R.id.emptyStateMessage);
        historyTitle.setText(R.string.empty_history_title);
    }

    private void initializeData() {
        // Load tasks that were persisted previously
        tasks = new ArrayList<>(taskDao.getAll());
        selectedDate = null; // Initialize selectedDate as null
        dateFormat = DateTimeFormatter.ofPattern("dd MMM, yyyy");
    }

    private void setupEventListeners() {
        datePickerBtn.setOnClickListener(v -> showDatePicker());
        addTaskBtn.setOnClickListener(v -> addTask());
        setupTabLayout();
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
            long id = taskDao.insert(newTask);
            newTask.setId(id);
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

        TextView todayTasksHeader = findViewById(R.id.todayTasksHeader);
        TextView futureTasksHeader = findViewById(R.id.futureTasksHeader);

        LocalDate today = LocalDate.now();

        // Sort tasks by date (ascending) and then by completion status (incomplete first)
        tasks.sort(Comparator.comparing(Task::getDate)
                .thenComparing(Task::isCompleted));

        boolean hasTodayTasks = false;
        boolean hasFutureTasks = false;

        LocalDate lastFutureDate = null;

        for (Task task : tasks) {
            if (task.getDate().equals(today)) {
                View taskView = createTaskView(task);
                todayTasksContainer.addView(taskView);
                hasTodayTasks = true;
            } else if (task.getDate().isAfter(today)) {
                // Add date header if this is a new date group
                if (lastFutureDate == null || !lastFutureDate.equals(task.getDate())) {
                    TextView dateHeader = new TextView(this);
                    dateHeader.setText(task.getDate().format(dateFormat));
                    dateHeader.setTextSize(18);
                    dateHeader.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));
                    dateHeader.setPadding(0, 16, 0, 8);
                    futureTasksContainer.addView(dateHeader);
                    lastFutureDate = task.getDate();
                }
                View taskView = createTaskView(task);
                futureTasksContainer.addView(taskView);
                hasFutureTasks = true;
            }
        }
        if (todayTasksHeader != null) {
            todayTasksHeader.setVisibility(hasTodayTasks ? View.VISIBLE : View.GONE);
        }
        if (futureTasksHeader != null) {
            futureTasksHeader.setVisibility(hasFutureTasks ? View.VISIBLE : View.GONE);
        }
        
        // Show/hide empty states
        updateEmptyStates(hasTodayTasks || hasFutureTasks);

        if (tabLayout != null && tabLayout.getSelectedTabPosition() == 0) {
            refreshLogList();
        }
    }
    
    private void updateEmptyStates(boolean hasAnyTasks) {
        // Remove existing empty state from containers
        todayTasksContainer.removeView(mainEmptyState);
        futureTasksContainer.removeView(mainEmptyState);
        
        // Add empty state view if no tasks exist at all
        if (!hasAnyTasks) {
            todayTasksContainer.addView(mainEmptyState);
        }
    }

    @NonNull
    private View createTaskView(@NonNull Task task) {
        // Inflate the reusable task item layout instead of creating the view programmatically
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout taskLayout = (LinearLayout) inflater.inflate(R.layout.item_task, null);

        // Bind views
        CheckBox checkBox = taskLayout.findViewById(R.id.taskCheckBox);
        TextView taskNameText = taskLayout.findViewById(R.id.taskNameText);
        TextView taskDateText = taskLayout.findViewById(R.id.taskDateText);
        Button editButton = taskLayout.findViewById(R.id.editButton);
        Button deleteButton = taskLayout.findViewById(R.id.deleteButton);

        // Initialise values
        taskNameText.setText(task.getName());
        taskDateText.setText(task.getDate().format(dateFormat));

        // Checkbox behaviour
        checkBox.setChecked(task.isCompleted());
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            updateTaskAppearance(taskLayout, task);
            taskDao.update(task);
        });

        // Edit button behaviour
        editButton.setOnClickListener(v -> showEditDialog(task));

        // Delete button behaviour
        deleteButton.setOnClickListener(v -> {
            AlertDialog deleteDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_task_title)
                    .setMessage(getString(R.string.delete_task_confirmation) + " \"" + task.getName() + "\"?")
                    .setPositiveButton(R.string.delete_text, (dialog, which) -> {
                        tasks.remove(task);
                        taskDao.delete(task);
                        if (tabLayout != null && tabLayout.getSelectedTabPosition() == 0) {
                            refreshTaskLists();
                        } else {
                            refreshLogList();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
            
            deleteDialog.show();
            
            // Set delete button text color to red
            deleteDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.delete_red));
        });

        // Apply strikethrough / alpha styling depending on completion status
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

                taskDao.update(task);
                if (tabLayout != null && tabLayout.getSelectedTabPosition() == 0) {
                    refreshTaskLists();
                } else {
                    refreshLogList();
                }
                editDialog.dismiss();
            }
        });
        editDialog.show();
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                boolean isMain = (tab.getPosition() == 0);

                // Toggle visibility of sections
                addTaskSection.setVisibility(isMain ? View.VISIBLE : View.GONE);
                todayTasksContainer.setVisibility(isMain ? View.VISIBLE : View.GONE);
                futureTasksContainer.setVisibility(isMain ? View.VISIBLE : View.GONE);
                logContainer.setVisibility(isMain ? View.GONE : View.VISIBLE);
                
                // Remove empty states from containers when switching tabs to prevent duplication
                if (!isMain) {
                    todayTasksContainer.removeView(mainEmptyState);
                    futureTasksContainer.removeView(mainEmptyState);
                } else {
                    logContainer.removeView(historyEmptyState);
                }

                if (isMain) {
                    // Ensure headers show/hide correctly based on current tasks
                    refreshTaskLists();
                } else {
                    // Always hide headers in History tab
                    findViewById(R.id.todayTasksHeader).setVisibility(View.GONE);
                    findViewById(R.id.futureTasksHeader).setVisibility(View.GONE);
                    refreshLogList();
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void refreshLogList() {
        if (logContainer == null) return;
        logContainer.removeAllViews();

        LocalDate today = LocalDate.now();

        java.util.Map<java.time.LocalDate, java.util.List<Task>> map = new java.util.TreeMap<>((d1, d2) -> d2.compareTo(d1));
        for (Task task : tasks) {
            if (task.getDate().isBefore(today)) { // Only past tasks
                map.computeIfAbsent(task.getDate(), k -> new java.util.ArrayList<>()).add(task);
            }
        }

        if (map.isEmpty()) {
            // Show empty state for history
            logContainer.addView(historyEmptyState);
        } else {
            for (java.util.Map.Entry<java.time.LocalDate, java.util.List<Task>> entry : map.entrySet()) {
                TextView dateHeader = new TextView(this);
                dateHeader.setText(entry.getKey().format(dateFormat));
                dateHeader.setTextSize(18);
                dateHeader.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));
                dateHeader.setPadding(0, 16, 0, 8);
                logContainer.addView(dateHeader);

                for (Task task : entry.getValue()) {
                    View taskView = createTaskView(task);
                    logContainer.addView(taskView);
                }
            }
        }
    }

    // Room handles DB closing automatically; nothing to close
}