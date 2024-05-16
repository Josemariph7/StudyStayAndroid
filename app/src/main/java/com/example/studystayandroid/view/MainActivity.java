package com.example.studystayandroid.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.studystayandroid.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.LogInButton);
        emailEditText = findViewById(R.id.textFieldEmail);
        passwordEditText = findViewById(R.id.textFieldPassword);

        surnamesEditTextSignUp = findViewById(R.id.textFieldSurnamesignUp);
        nameEditTextSignUp  = findViewById(R.id.textFieldNameSignUp);
        passwordEditTextSignUp = findViewById(R.id.textFieldPassword);
        emailEditTextSignUp = findViewById(R.id.textFieldEmailSignUp);
        dniEditTextSignUp = findViewById(R.id.textFieldDNISignUp);
        phoneEditTextSignUp = findViewById(R.id.textFieldPhoneSignUp);
        spinnerGender = findViewById(R.id.spinnerGender);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditTextSignUp.getEditText().getText().toString();
                String passwordSignUp = passwordEditText.getEditText().getText().toString();
                String emailSignUp = emailEditText.getEditText().getText().toString();
                String phone = phoneEditTextSignUp.getEditText().getText().toString();
                String dni = dniEditTextSignUp.getEditText().getText().toString();
                String surnames = surnamesEditTextSignUp.getEditText().getText().toString();
                //String birthDate = "1990-01-01";
                String gender = spinnerGender.getSelectedItem().toString();
                register(name, surnames,emailSignUp,passwordSignUp,phone,dni,null,gender);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getEditText().getText().toString();
                String password = passwordEditText.getEditText().getText().toString();
                login(email, password);
            }
        });

        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

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

    private void login(String email, String password) {
        // URL del script PHP para el inicio de sesión
        String url = "http://192.168.238.26/studystay/login.php";
        // Crear la solicitud HTTP POST
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Manejar la respuesta del servidor
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.has("error")) {
                                // Mostrar mensaje de error si hay un error en la respuesta
                                Toast.makeText(MainActivity.this, jsonResponse.getString("error"), Toast.LENGTH_SHORT).show();
                            } else {
                                // Inicio de sesión exitoso, navegar a la siguiente actividad
                                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Manejar errores de la solicitud HTTP
                        String errorMessage = "Error: " + error.toString();
                        if (error.networkResponse != null) {
                            errorMessage += "\nStatus Code: " + error.networkResponse.statusCode;
                            if (error.networkResponse.data != null) {
                                try {
                                    String responseBody = new String(error.networkResponse.data, "utf-8");
                                    errorMessage += "\nResponse Body: " + responseBody;
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };
        // Agregar la solicitud a la cola de solicitudes de Volley
        Volley.newRequestQueue(this).add(stringRequest);
    }


    private void register(String name, String lastName, String email, String password, String phone, String birthDate, String gender, String dni) {
        String url = "http://192.168.238.26/studystay/createUser.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Manejar la respuesta del servidor
                        if (response.equals("User created successfully")) {
                            // Usuario creado con éxito, puedes mostrar un mensaje o realizar otras acciones si es necesario
                            Toast.makeText(MainActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            // Error al crear el usuario, muestra un mensaje de error
                            Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Manejar errores de la solicitud HTTP
                        Toast.makeText(MainActivity.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("lastName", lastName);
                params.put("email", email);
                params.put("password", password);
                params.put("phone", phone);
                params.put("birthDate", birthDate);
                params.put("gender", gender);
                params.put("dni", dni);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}
