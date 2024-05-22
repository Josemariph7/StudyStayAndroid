package com.example.studystayandroid.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.UserController;
import com.example.studystayandroid.model.User;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.regex.Pattern;

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
    private TextView birthDateEditText;
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
        birthDateEditText = findViewById(R.id.editTextBirthDate);
        spinnerGender = findViewById(R.id.spinnerGender);
        getWindow().setStatusBarColor(getResources().getColor(R.color.statusBarColor));


        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateSignUpFields()) {
                    String name = nameEditTextSignUp.getEditText().getText().toString();
                    String lastName = surnamesEditTextSignUp.getEditText().getText().toString();
                    String email = emailEditTextSignUp.getEditText().getText().toString();
                    String password = passwordEditTextSignUp.getEditText().getText().toString();
                    String phone = phoneEditTextSignUp.getEditText().getText().toString();
                    String dni = dniEditTextSignUp.getEditText().getText().toString();
                    String gender = spinnerGender.getSelectedItem().toString().toUpperCase();
                    String birthDateString = birthDateEditText.getText().toString();

                    LocalDate birthDate = null;
                    if (!birthDateString.isEmpty()) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
                        birthDate = LocalDate.parse(birthDateString, formatter);
                    }

                    User newUser = new User();
                    newUser.setName(name);
                    newUser.setLastName(lastName);
                    newUser.setEmail(email);
                    newUser.setPassword(password);
                    newUser.setPhone(phone);
                    newUser.setDni(dni);
                    newUser.setGender(User.Gender.valueOf(gender));
                    newUser.setBirthDate(birthDate);

                    LocalDateTime currentDateTime = LocalDateTime.now();
                    newUser.setRegistrationDate(currentDateTime);

                    userController.register(newUser, new UserController.UserCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            Toast.makeText(MainActivity.this, "You have been successfully registered.", Toast.LENGTH_SHORT).show();
                            clearSignUpFields();
                        }

                        @Override
                        public void onSuccess(User author) {}

                        @Override
                        public void onError(String error) {
                            Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateLoginFields()) {
                    String email = emailEditText.getEditText().getText().toString();
                    String password = passwordEditText.getEditText().getText().toString();
                    userController.login(email, password, new UserController.UserCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            Long userId = (Long) result;
                            Log.d("Login", "Login successful, userId: " + userId);
                            userController.getUserById(userId, new UserController.UserCallback() {
                                @Override
                                public void onSuccess(Object result) {
                                    User user = (User) result;
                                    byte[] userPhotoBytes = user.getProfilePicture();
                                    String userPhotoBase64 = "";

                                    if (userPhotoBytes != null) {
                                        userPhotoBase64 = Base64.encodeToString(userPhotoBytes, Base64.DEFAULT);
                                    }

                                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putLong("userId", userId);
                                    editor.putString("userName", user.getName());
                                    editor.putString("userEmail", user.getEmail());
                                    editor.putString("userPhoto", userPhotoBase64.isEmpty() ? "default" : userPhotoBase64);
                                    editor.apply();

                                    Log.d("Login", "User details saved to SharedPreferences");

                                    Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                                    startActivity(intent);
                                }

                                @Override
                                public void onSuccess(User author) {
                                    User user = (User) author;
                                    byte[] userPhotoBytes = user.getProfilePicture();
                                    String userPhotoBase64 = "";

                                    if (userPhotoBytes != null) {
                                        userPhotoBase64 = Base64.encodeToString(userPhotoBytes, Base64.DEFAULT);
                                    }

                                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putLong("userId", userId);
                                    editor.putString("userName", user.getName());
                                    editor.putString("userEmail", user.getEmail());
                                    editor.putString("userPhoto", userPhotoBase64.isEmpty() ? "default" : userPhotoBase64);
                                    editor.apply();

                                    Log.d("Login", "User details saved to SharedPreferences");


                                    Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                                    startActivity(intent);
                                }

                                @Override
                                public void onError(String error) {
                                    Log.e("Login", "Error fetching user details: " + error);
                                }
                            });
                        }

                        @Override
                        public void onSuccess(User author) {}

                        @Override
                        public void onError(String error) {
                            Log.e("Login", "Invalid credentials: " + error);
                        }
                    });
                }
            }
        });

        birthDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
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

    private boolean validateSignUpFields() {
        boolean isValid = true;

        if (!nameEditTextSignUp.getEditText().getText().toString().matches("^[a-zA-Z\\s]+$")) {
            nameEditTextSignUp.setError("Valid name is required");
            isValid = false;
        } else {
            nameEditTextSignUp.setError("A-z Characters only");
        }

        if (!surnamesEditTextSignUp.getEditText().getText().toString().matches("^[a-zA-Z\\s]+$")) {
            surnamesEditTextSignUp.setError("Valid surnames are required");
            isValid = false;
        } else {
            surnamesEditTextSignUp.setError(null);
        }

        if (!emailEditTextSignUp.getEditText().getText().toString().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            emailEditTextSignUp.setError("Valid email is required");
            isValid = false;
        } else {
            emailEditTextSignUp.setError(null);
        }

        if (passwordEditTextSignUp.getEditText().getText().toString().length() < 6) {
            passwordEditTextSignUp.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            passwordEditTextSignUp.setError(null);
        }

        if (!dniEditTextSignUp.getEditText().getText().toString().matches("^[0-9]{8}[A-Za-z]$")) {
            dniEditTextSignUp.setError("Valid DNI is required");
            isValid = false;
        } else {
            dniEditTextSignUp.setError(null);
        }

        if (!phoneEditTextSignUp.getEditText().getText().toString().matches("^[0-9]{9}$")) {
            phoneEditTextSignUp.setError("Valid phone number is required");
            isValid = false;
        } else {
            phoneEditTextSignUp.setError(null);
        }

        if (TextUtils.isEmpty(birthDateEditText.getText().toString())) {
            birthDateEditText.setError("Birth date is required");
            isValid = false;
        } else {
            birthDateEditText.setError(null);
        }

        return isValid;
    }


    private boolean validateLoginFields() {
        boolean isValid = true;

        if (TextUtils.isEmpty(emailEditText.getEditText().getText().toString())) {
            emailEditText.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailEditText.getEditText().getText().toString()).matches()) {
            emailEditText.setError("Enter a valid email");
            isValid = false;
        } else {
            emailEditText.setError(null);
        }

        if (TextUtils.isEmpty(passwordEditText.getEditText().getText().toString())) {
            passwordEditText.setError("Password is required");
            isValid = false;
        } else {
            passwordEditText.setError(null);
        }

        return isValid;
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                MainActivity.this,
                R.style.CustomDatePickerDialog,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    birthDateEditText.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void clearSignUpFields() {
        nameEditTextSignUp.getEditText().setText("");
        surnamesEditTextSignUp.getEditText().setText("");
        emailEditTextSignUp.getEditText().setText("");
        passwordEditTextSignUp.getEditText().setText("");
        phoneEditTextSignUp.getEditText().setText("");
        dniEditTextSignUp.getEditText().setText("");
        birthDateEditText.setText("");
    }
}
