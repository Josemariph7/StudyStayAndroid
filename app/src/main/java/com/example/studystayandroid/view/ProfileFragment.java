package com.example.studystayandroid.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.UserController;
import com.example.studystayandroid.model.User;

public class ProfileFragment extends Fragment {

    private User currentUser;
    private UserController userController;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView phoneTextView;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cambiar el título de la ActionBar
        if (getActivity() != null) {
            getActivity().setTitle("StudyStay - Profile");
        }

        // Inicializar vistas
        nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);

        // Obtener el usuario actual de SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        Long userId = sharedPreferences.getLong("userId", -1);
        if (userId == -1) {
            Log.e("ProfileFragment", "Error: Usuario no autenticado.");
            return;
        }

        // Inicializar UserController
        userController = new UserController(getActivity());
        userController.getUserById(userId, new UserController.UserCallback() {
            @Override
            public void onSuccess(Object result) {

            }

            @Override
            public void onSuccess(User user) {
                currentUser = user;
                updateProfileUI();
            }

            @Override
            public void onError(String error) {
                Log.e("ProfileFragment", "Error al cargar el usuario: " + error);
            }
        });
    }

    private void updateProfileUI() {
        if (currentUser != null) {
            nameTextView.setText(currentUser.getName() + " " + currentUser.getLastName());
            emailTextView.setText(currentUser.getEmail());
            phoneTextView.setText(currentUser.getPhone());
        }
    }
}
