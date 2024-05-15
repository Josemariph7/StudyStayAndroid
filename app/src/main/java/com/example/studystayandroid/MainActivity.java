package com.example.studystayandroid;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {
    private BottomSheetBehavior bottomSheetBehavior;
    private Button loginButton;
    private TextInputLayout passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        loginButton = (Button) findViewById(R.id.LogInButton);
        passwordEditText = (TextInputLayout) findViewById(R.id.textFieldPassword);

        // Establecer la altura de despliegue inicial al 33% de la pantalla
        int peekHeight = (int) (getResources().getDisplayMetrics().heightPixels * 0.11);
        bottomSheetBehavior.setPeekHeight(peekHeight);

        // Establecer que el BottomSheet no sea ocultable y no se pueda colapsar
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setSkipCollapsed(false);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"Male", "Female", "Other"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = findViewById(R.id.spinnerGender);
        spinner.setAdapter(adapter);
    }
}