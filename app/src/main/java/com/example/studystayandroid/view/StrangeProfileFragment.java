/*
 * StudyStay © 2024
 *
 * All rights reserved.
 *
 * This software and associated documentation files (the "Software") are owned by StudyStay. Unauthorized copying, distribution, or modification of this Software is strictly prohibited.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this Software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * StudyStay
 * José María Pozo Hidalgo
 * Email: josemariph7@gmail.com
 *
 *
 */

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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.ConversationController;
import com.example.studystayandroid.model.Conversation;
import com.example.studystayandroid.model.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to display the profile of a user that is not the current user.
 */
public class StrangeProfileFragment extends Fragment {

    private static final String ARG_USER = "arg_user";

    private User otherUser;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView phoneTextView;
    private TextView birthDateTextView;
    private TextView bioTextView;
    private ImageView profileImageView;
    private Button contactButton;
    private Long currentUserId;

    /**
     * Required empty public constructor.
     */
    public StrangeProfileFragment() {
    }

    /**
     * Create a new instance of StrangeProfileFragment.
     * @param user The user whose profile will be displayed.
     * @return A new instance of StrangeProfileFragment.
     */
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

        // Change ActionBar title
        if (getActivity() != null) {
            getActivity().setTitle("StudyStay - Profile");
        }

        // Initialize views
        nameTextView = view.findViewById(R.id.nameTextViewProfile);
        emailTextView = view.findViewById(R.id.emailTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);
        birthDateTextView = view.findViewById(R.id.birthDateTextView);
        bioTextView = view.findViewById(R.id.bioTextView);
        profileImageView = view.findViewById(R.id.profileImageView);
        contactButton = view.findViewById(R.id.ContactButton);
        ImageButton backButton = view.findViewById(R.id.button3);

        // Get current user from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentUserId = sharedPreferences.getLong("userId", -1);

        if (getArguments() != null) {
            otherUser = (User) getArguments().getSerializable(ARG_USER);
        }

        if (currentUserId == -1 || otherUser == null) {
            Log.e("StrangeProfileFragment", "Error: Unauthenticated user or no other user specified.");
            return;
        }

        updateProfileUI();

        contactButton.setOnClickListener(v -> showContactOptionsDialog());
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    /**
     * Update the UI with the details of the other user.
     */
    private void updateProfileUI() {
        if (otherUser != null) {
            nameTextView.setText(otherUser.getName() + " " + otherUser.getLastName());
            emailTextView.setText(otherUser.getEmail());
            phoneTextView.setText(otherUser.getPhone());
            birthDateTextView.setText(otherUser.getBirthDate() != null ? otherUser.getBirthDate().toString() : "N/A");

            if (otherUser.getBio() != null && !otherUser.getBio().isEmpty() && !otherUser.getBio().equals("null")) {
                bioTextView.setText(otherUser.getBio());
            } else {
                bioTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }

            byte[] profileImageBytes = otherUser.getProfilePicture();
            if (profileImageBytes != null && profileImageBytes.length > 0) {
                Glide.with(requireContext())
                        .asBitmap()
                        .load(profileImageBytes)
                        .transform(new CircleCrop())
                        .into(profileImageView);
            } else {
                Glide.with(requireContext())
                        .load(R.drawable.defaultprofile)
                        .transform(new CircleCrop())
                        .into(profileImageView);
            }
        }
    }

    /**
     * Show a dialog with contact options (Call or Message).
     */
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

    /**
     * Create a new conversation or find an existing one, then open the message fragment.
     */
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

    /**
     * Create a new conversation between the current user and the other user.
     */
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

    /**
     * Navigate to the MessageFragment for the given conversation.
     * @param conversation The conversation to open.
     */
    private void navigateToMessageFragment(Conversation conversation) {
        MessageFragment messageFragment = MessageFragment.newInstance(conversation);
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, messageFragment)
                .addToBackStack(null)
                .commit();
    }
}
