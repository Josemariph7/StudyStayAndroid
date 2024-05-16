package com.example.studystayandroid.view;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studystayandroid.R;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referencia al TextView en el layout
        TextView textView = findViewById(R.id.textView);

        // Asigna el texto al TextView
        textView.setText("Profile!");
    }
}
