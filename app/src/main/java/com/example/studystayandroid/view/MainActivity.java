package com.example.studystayandroid.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.UserController;
import com.example.studystayandroid.model.User;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;

public class MainActivity extends AppCompatActivity {
    private Button loginButton;
    private Button registerButton;
    private TextInputLayout emailEditText;
    private TextInputLayout passwordEditText;
    private TextInputLayout surnamesEditTextSignUp;
    private TextInputLayout nameEditTextSignUp;
    private TextInputLayout passwordEditTextSignUp;
    private TextInputLayout emailEditTextSignUp;
    private TextInputLayout dniEditTextSignUp;
    private TextInputLayout phoneEditTextSignUp;
    private Spinner spinnerGender;

    private BottomSheetBehavior bottomSheetBehavior;
    private UserController userController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userController = new UserController(this);

        loginButton = findViewById(R.id.LogInButton);
        emailEditText = findViewById(R.id.textFieldEmail);
        passwordEditText = findViewById(R.id.textFieldPassword);

        surnamesEditTextSignUp = findViewById(R.id.textFieldSurnamesignUp);
        nameEditTextSignUp = findViewById(R.id.textFieldNameSignUp);
        passwordEditTextSignUp = findViewById(R.id.textFieldPasswordSignUp);
        emailEditTextSignUp = findViewById(R.id.textFieldEmailSignUp);
        dniEditTextSignUp = findViewById(R.id.textFieldDNISignUp);
        phoneEditTextSignUp = findViewById(R.id.textFieldPhoneSignUp);
        spinnerGender = findViewById(R.id.spinnerGender);

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditTextSignUp.getEditText().getText().toString();
                String lastName = surnamesEditTextSignUp.getEditText().getText().toString();
                String email = emailEditTextSignUp.getEditText().getText().toString();
                String password = passwordEditTextSignUp.getEditText().getText().toString();
                String phone = phoneEditTextSignUp.getEditText().getText().toString();
                String dni = dniEditTextSignUp.getEditText().getText().toString();
                String gender = spinnerGender.getSelectedItem().toString();
                LocalDate birthDate = LocalDate.now(); // Aquí deberías obtener la fecha de nacimiento correcta

                User newUser = new User();
                newUser.setName(name);
                newUser.setLastName(lastName);
                newUser.setEmail(email);
                newUser.setPassword(password);
                newUser.setPhone(phone);
                newUser.setDni(dni);
                newUser.setGender(User.Gender.valueOf(gender));
                newUser.setBirthDate(birthDate);

                userController.register(newUser, new UserController.UserCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        Toast.makeText(MainActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onSuccess(User author) {}
                    @Override
                    public void onError(String error) {
                        Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getEditText().getText().toString();
                String password = passwordEditText.getEditText().getText().toString();
                userController.login(email, password, new UserController.UserCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        Long userId = (Long) result;
                        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putLong("userId", userId);
                        editor.apply();

                        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                        startActivity(intent);
                    }
                    @Override
                    public void onSuccess(User author) {}
                    @Override
                    public void onError(String error) {
                        Toast.makeText(MainActivity.this, "Usuario o contraseña inválidos.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        int peekHeight = (int) (getResources().getDisplayMetrics().heightPixels * 0.11);
        bottomSheetBehavior.setPeekHeight(peekHeight);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setSkipCollapsed(false);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"Male", "Female", "Other"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = findViewById(R.id.spinnerGender);
        spinner.setAdapter(adapter);
    }
}
