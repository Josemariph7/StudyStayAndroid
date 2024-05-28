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
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.ConversationController;
import com.example.studystayandroid.model.Conversation;
import com.example.studystayandroid.model.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class StrangeProfileFragment extends Fragment {

    private static final String ARG_USER = "arg_user";

    private User otherUser;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView phoneTextView;
    private TextView birthDateTextView;
    private TextView bioTextView;
    private Button contactButton;
    private Long currentUserId;

    public StrangeProfileFragment() {
        // Required empty public constructor
    }

    public static StrangeProfileFragment newInstance(User user) {
        StrangeProfileFragment fragment = new StrangeProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
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
        birthDateTextView = view.findViewById(R.id.birthDateTextView);
        bioTextView = view.findViewById(R.id.bioTextView);
        contactButton = view.findViewById(R.id.ContactButton);
        ImageButton backButton = view.findViewById(R.id.button3);

        // Obtener el usuario actual de SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentUserId = sharedPreferences.getLong("userId", -1);

        if (getArguments() != null) {
            otherUser = (User) getArguments().getSerializable(ARG_USER);
        }

        if (currentUserId == -1 || otherUser == null) {
            Log.e("StrangeProfileFragment", "Error: Usuario no autenticado o usuario ajeno no especificado.");
            return;
        }

        updateProfileUI();

        contactButton.setOnClickListener(v -> showContactOptionsDialog());
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void updateProfileUI() {
        if (otherUser != null) {
            nameTextView.setText(otherUser.getName() + " " + otherUser.getLastName());
            emailTextView.setText(otherUser.getEmail());
            phoneTextView.setText(otherUser.getPhone());
            birthDateTextView.setText(otherUser.getBirthDate() != null ? otherUser.getBirthDate().toString() : "N/A");
            bioTextView.setText(otherUser.getBio() != null ? otherUser.getBio() : "N/A");
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
        ConversationController conversationController = new ConversationController(getContext());
        conversationController.getConversations(currentUserId, new ConversationController.ConversationListCallback() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                for (Conversation conversation : conversations) {
                    if ((conversation.getUser1Id().equals(currentUserId) && conversation.getUser2Id().equals(otherUser.getUserId())) ||
                            (conversation.getUser1Id().equals(otherUser.getUserId()) && conversation.getUser2Id().equals(currentUserId))) {
                        navigateToMessageFragment(conversation);
                        return;
                    }
                }
                // If no existing conversation, create a new one
                createNewConversation();
            }

            @Override
            public void onError(String error) {
                Log.e("StrangeProfileFragment", "Error fetching conversations: " + error);
                createNewConversation();
            }
        });
    }

    private void createNewConversation() {
        Conversation conversation = new Conversation(null, currentUserId, otherUser.getUserId(), new ArrayList<>());
        ConversationController conversationController = new ConversationController(getContext());
        conversationController.createConversation(conversation, new ConversationController.ConversationCallback() {
            @Override
            public void onSuccess(Conversation createdConversation) {
                if (createdConversation != null) {
                    navigateToMessageFragment(createdConversation);
                } else {
                    Log.e("StrangeProfileFragment", "Created conversation is null");
                }
            }

            @Override
            public void onSuccess(Object result) {
                // This can be left empty or used if necessary
            }

            @Override
            public void onError(String error) {
                Log.e("StrangeProfileFragment", "Error creating conversation: " + error);
            }
        });
    }

    private void navigateToMessageFragment(Conversation conversation) {
        MessageFragment messageFragment = MessageFragment.newInstance(conversation);
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, messageFragment)
                .addToBackStack(null)
                .commit();
    }


}
