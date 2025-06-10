package com.example.lengthunitconverter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.lengthunitconverter.R;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private EditText editTextInput;
    private Spinner spinnerFromUnit, spinnerToUnit;
    private Button buttonClear;
    private TextView textViewResultValue;

    private String[] units = {"m", "mm", "mi", "ft"};
    private String[] unitNames = {"Metre", "Millimetre", "Mile", "Foot"};
    private String fromUnit = "m";
    private String toUnit = "mm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupSpinners();
        setupButtonListeners();
        setupTextWatcher();

        // Set initial selection
        spinnerFromUnit.setSelection(0); // m
        spinnerToUnit.setSelection(1);   // mm
    }

    private void initializeViews() {
        editTextInput = findViewById(R.id.editTextInput);
        spinnerFromUnit = findViewById(R.id.spinnerFromUnit);
        spinnerToUnit = findViewById(R.id.spinnerToUnit);
        buttonClear = findViewById(R.id.buttonClear);
        textViewResultValue = findViewById(R.id.textViewResultValue);
    }

    private void setupSpinners() {
        // Create custom adapter for unit abbreviations
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, units) {
            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setText(unitNames[position] + " (" + units[position] + ")");
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFromUnit.setAdapter(adapter);
        spinnerToUnit.setAdapter(adapter);

        spinnerFromUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromUnit = units[position];
                performConversion();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerToUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toUnit = units[position];
                performConversion();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupTextWatcher() {
        editTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                performConversion();
            }
        });
    }

    private void setupButtonListeners() {
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAll();
            }
        });
    }

    private void performConversion() {
        String inputText = editTextInput.getText().toString().trim();

        if (TextUtils.isEmpty(inputText)) {
            textViewResultValue.setText("0");
            return;
        }

        try {
            double inputValue = Double.parseDouble(inputText);
            if (inputValue < 0) {
                textViewResultValue.setText("0");
                return;
            }

            // Perform conversion
            double result = convertLength(inputValue, fromUnit, toUnit);

            // Display result with appropriate formatting
            DecimalFormat df = new DecimalFormat("#.######");
            String formattedInput = df.format(inputValue);
            String formattedResult = df.format(result);

            // Update result value display
            textViewResultValue.setText(formattedResult);

            // Update result card
            String resultText = String.format("%s %s = %s %s",
                    formattedInput, fromUnit, formattedResult, toUnit);

        } catch (NumberFormatException e) {
            textViewResultValue.setText("0");
        }
    }

    private double convertLength(double value, String from, String to) {
        // Convert to metres first (base unit)
        double metres = convertToMetres(value, from);

        // Convert from metres to target unit
        return convertFromMetres(metres, to);
    }

    private double convertToMetres(double value, String unit) {
        switch (unit) {
            case "mm":
                return value * 0.001;
            case "mi":
                return value * 1609.344;
            case "ft":
                return value * 0.3048;
            default:
                return value;
        }
    }

    private double convertFromMetres(double metres, String unit) {
        switch (unit) {
            case "mm":
                return metres / 0.001;
            case "mi":
                return metres / 1609.344;
            case "ft":
                return metres / 0.3048;
            default:
                return metres;
        }
    }

    private void clearAll() {
        editTextInput.setText("");
        spinnerFromUnit.setSelection(0);
        spinnerToUnit.setSelection(1);
        textViewResultValue.setText("0");
        editTextInput.requestFocus();
    }
}