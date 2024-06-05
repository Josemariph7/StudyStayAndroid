package com.example.studystayandroid.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.UserController;
import com.example.studystayandroid.model.User;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private Button loginButton;
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
                            showSuccessDialog("You have been successfully registered.");
                            clearSignUpFields();
                        }

                        @Override
                        public void onSuccess(User author) {
                            showSuccessDialog("You have been successfully registered.");
                            clearSignUpFields();
                        }

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

                                }

                                @Override
                                public void onSuccess(User author) {
                                    Log.d("Login", "Fetching user details successful");
                                    User user = (User) author;
                                    byte[] userPhotoBytes = user.getProfilePicture();
                                    ImageView imageView = findViewById(R.id.imageView);

                                    if (userPhotoBytes != null && userPhotoBytes.length > 0) {
                                        String userPhotoBase64 = Base64.encodeToString(userPhotoBytes, Base64.DEFAULT);
                                        Log.d("Login", "User photo encoded to base64");

                                        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                        Log.d("Login", "Getting SharedPreferences");
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        Log.d("Login", "Getting SharedPreferences editor");
                                        editor.putLong("userId", userId);
                                        editor.putString("userName", user.getName());
                                        editor.putString("userEmail", user.getEmail());
                                        editor.putString("userPhoto", userPhotoBase64.isEmpty() ? "default" : userPhotoBase64);
                                        Log.d("Login", "Saving photo to SharedPreferences");
                                        editor.apply();
                                        Log.d("Login", "User details saved to SharedPreferences");

                                        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                                        Log.d("Login", "Creating intent for DashboardActivity");
                                        startActivity(intent);
                                        Log.d("Login", "Starting DashboardActivity");
                                    } else {
                                        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                                        Log.d("Login", "Creating intent for DashboardActivity");
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onError(String error) {
                                    Log.e("Login", "Error fetching user details: " + error);
                                }
                            });
                        }

                        @Override
                        public void onSuccess(User author) {

                            Log.d("Login", "Login successful, userId: " + author.getUserId());
                            userController.getUserById(author.getUserId(), new UserController.UserCallback() {
                                @Override
                                public void onSuccess(Object result) {

                                }

                                @Override
                                public void onSuccess(User author) {
                                    Log.d("Login", "Fetching user details successful");
                                    byte[] userPhotoBytes = author.getProfilePicture();
                                    ImageView imageView = findViewById(R.id.imageView);

                                    if (userPhotoBytes != null && userPhotoBytes.length > 0) {

                                        String userPhotoBase64 = Base64.encodeToString(userPhotoBytes, Base64.DEFAULT);
                                        Log.d("Login", "User photo encoded to base64");

                                        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                        Log.d("Login", "Getting SharedPreferences");
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        Log.d("Login", "Getting SharedPreferences editor");
                                        editor.putLong("userId", author.getUserId());
                                        editor.putString("userName", author.getName());
                                        editor.putString("userEmail", author.getEmail());
                                        editor.putString("userPhoto", userPhotoBase64.isEmpty() ? "default" : userPhotoBase64);
                                        Log.d("Login", "Saving photo to SharedPreferences");
                                        editor.apply();
                                        Log.d("Login", "User details saved to SharedPreferences");

                                        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                                        Log.d("Login", "Creating intent for DashboardActivity");
                                        startActivity(intent);
                                        Log.d("Login", "Starting DashboardActivity");
                                    } else {
                                        Log.d("Login", "User photo is null or empty");
                                        // Manejar el caso cuando la imagen de usuario es null o vacía
                                        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putLong("userId", author.getUserId());
                                        editor.putString("userName", author.getName());
                                        editor.putString("userEmail", author.getEmail());
                                        editor.putString("userPhoto", "default");
                                        editor.apply();

                                        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onError(String error) {
                                    Log.e("Login", "Error fetching user details: " + error);
                                }
                            });
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("Login", "Invalid credentials: " + error);
                            showErrorDialog("Invalid email or password. Please try again.");
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

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_array,
                R.layout.spinner_item
        );

        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

// Establecer el color del texto para el primer elemento
        spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) {
                    ((TextView) parentView.getChildAt(0)).setTextColor(Color.GRAY);
                } else {
                    ((TextView) parentView.getChildAt(0)).setTextColor(getResources().getColor(R.color.SadBlue));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }
    }

    private void showSuccessDialog(String message) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_error, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);

        dialogTitle.setText("Success");
        dialogMessage.setText(message);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        buttonConfirm.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    private void showErrorDialog(String message) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_error, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);

        dialogTitle.setText("Error");
        dialogMessage.setText(message);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        buttonConfirm.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    private boolean validateSignUpFields() {
        boolean isValid = true;

        if (!nameEditTextSignUp.getEditText().getText().toString().matches("^[a-zA-Z\\s]+$")) {
            nameEditTextSignUp.setError("Valid name is required");
            isValid = false;
        }else{
            nameEditTextSignUp.setError(null);
        }
        if(TextUtils.isEmpty(nameEditTextSignUp.getEditText().getText().toString())) {
            nameEditTextSignUp.setError("Name is required");
            isValid = false;
        }

        if (!surnamesEditTextSignUp.getEditText().getText().toString().matches("^[a-zA-Z\\s]+$")) {
            surnamesEditTextSignUp.setError("Valid surnames are required");
            isValid = false;
        }else{
            surnamesEditTextSignUp.setError(null);
        }
        if(TextUtils.isEmpty(surnamesEditTextSignUp.getEditText().getText().toString())) {
            surnamesEditTextSignUp.setError("Surnames are required");
        }

        if (!emailEditTextSignUp.getEditText().getText().toString().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            emailEditTextSignUp.setError("Introduce a valid email format");
            isValid = false;
        } else {
            emailEditTextSignUp.setError(null);
        }
        if(TextUtils.isEmpty(emailEditTextSignUp.getEditText().getText().toString())) {
            emailEditTextSignUp.setError("Email is required");
            isValid = false;
        }

        if (passwordEditTextSignUp.getEditText().getText().toString().length() < 6) {
            passwordEditTextSignUp.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            passwordEditTextSignUp.setError(null);
        }
        if(TextUtils.isEmpty(passwordEditTextSignUp.getEditText().getText().toString())) {
            passwordEditTextSignUp.setError("Password is required");
            isValid = false;
        }

        if (!dniEditTextSignUp.getEditText().getText().toString().matches("^[0-9]{8}[A-Za-z]$")) {
            dniEditTextSignUp.setError("Introduce a valid DNI format");
            isValid = false;
        } else {
            dniEditTextSignUp.setError(null);
        }
        if(TextUtils.isEmpty(dniEditTextSignUp.getEditText().getText().toString())) {
            dniEditTextSignUp.setError("DNI is required");
            isValid = false;
        }

        if (!phoneEditTextSignUp.getEditText().getText().toString().matches("^[0-9]{9}$")) {
            phoneEditTextSignUp.setError("Valid phone number is required");
            isValid = false;
        } else {
            phoneEditTextSignUp.setError(null);
        }
        if(TextUtils.isEmpty(phoneEditTextSignUp.getEditText().getText().toString())) {
            phoneEditTextSignUp.setError("Phone is required");
            isValid = false;
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
        } else if(passwordEditText.getEditText().getText().toString().length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
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
