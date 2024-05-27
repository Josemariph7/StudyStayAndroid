package com.example.studystayandroid.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.ConversationController;
import com.example.studystayandroid.controller.UserController;
import com.example.studystayandroid.model.Conversation;
import com.example.studystayandroid.model.Message;
import com.example.studystayandroid.model.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StrangeProfileFragment extends Fragment {

    private User otherUser;
    private UserController userController;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView phoneTextView;
    private Button contactButton;
    private Long currentUserId;

    public StrangeProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_stranger, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cambiar el título de la ActionBar
        if (getActivity() != null) {
            getActivity().setTitle("StudyStay - Profile");
        }

        // Inicializar vistas
        nameTextView = view.findViewById(R.id.nameTextViewProfile);
        emailTextView = view.findViewById(R.id.emailTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);
        contactButton = view.findViewById(R.id.ContactButton);

        // Obtener el usuario actual de SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentUserId = sharedPreferences.getLong("userId", -1);
        Long otherUserId = getArguments() != null ? getArguments().getLong("otherUserId", -1) : -1;
        if (currentUserId == -1 || otherUserId == -1) {
            Log.e("StrangeProfileFragment", "Error: Usuario no autenticado o usuario ajeno no especificado.");
            return;
        }

        // Inicializar UserController
        userController = new UserController(getActivity());
        userController.getUserById(otherUserId, new UserController.UserCallback() {
            @Override
            public void onSuccess(Object result) {

            }

            @Override
            public void onSuccess(User user) {
                otherUser = user;
                updateProfileUI();
            }

            @Override
            public void onError(String error) {
                Log.e("StrangeProfileFragment", "Error al cargar el usuario: " + error);
            }
        });

        contactButton.setOnClickListener(v -> showContactOptionsDialog());
    }

    private void updateProfileUI() {
        if (otherUser != null) {
            nameTextView.setText(otherUser.getName() + " " + otherUser.getLastName());
            emailTextView.setText(otherUser.getEmail());
            phoneTextView.setText(otherUser.getPhone());
        }
    }

    private void showContactOptionsDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Contact Options")
                .setItems(new String[]{"Call", "Message"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // Call
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + otherUser.getPhone()));
                            startActivity(intent);
                            break;
                        case 1:
                            // Message
                            createConversationAndOpenMessageFragment();
                            break;
                    }
                })
                .show();
    }

    private void createConversationAndOpenMessageFragment() {
        Conversation conversation = new Conversation(null, currentUserId, otherUser.getUserId(), new ArrayList<>());
        // Assume ConversationController exists and works similar to UserController
        ConversationController conversationController = new ConversationController(getContext());
        conversationController.createConversation(conversation, new ConversationController.ConversationCallback() {
            @Override
            public void onSuccess(Conversation createdConversation) {
                MessageFragment messageFragment = MessageFragment.newInstance(createdConversation);
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, messageFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onSuccess(Object result) {

            }

            @Override
            public void onError(String error) {
                Log.e("StrangeProfileFragment", "Error creating conversation: " + error);
            }
        });
    }
}
