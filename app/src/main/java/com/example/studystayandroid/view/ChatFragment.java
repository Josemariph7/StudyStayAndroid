package com.example.studystayandroid.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.ConversationController;
import com.example.studystayandroid.controller.UserController;
import com.example.studystayandroid.model.Conversation;
import com.example.studystayandroid.model.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerViewConversations;
    private ConversationAdapter conversationAdapter;
    private List<Conversation> conversationList;

    private ConversationController conversationController;
    private Long currentUserId;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentUserId = sharedPreferences.getLong("userId", -1);
        if (getActivity() != null) {
            getActivity().setTitle("StudyStay - Chats");
        }
        if (currentUserId == -1) {
            Log.e("ChatFragment", "Error: Usuario no autenticado.");
            return;
        }

        conversationController = new ConversationController(requireContext());

        recyclerViewConversations = view.findViewById(R.id.recyclerViewConversations);
        recyclerViewConversations.setLayoutManager(new LinearLayoutManager(getContext()));

        conversationList = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(conversationList, currentUserId, requireContext());
        recyclerViewConversations.setAdapter(conversationAdapter);

        fetchConversations();

        conversationAdapter.setOnItemClickListener(conversation -> openMessageFragment(conversation));
        conversationAdapter.setOnItemLongClickListener(conversation -> showConversationOptions(conversation));
    }

    private void fetchConversations() {
        conversationController.getConversations(currentUserId, new ConversationController.ConversationListCallback() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                conversationList.clear();
                for (Conversation conversation : conversations) {
                    if (Objects.equals(conversation.getUser1Id(), currentUserId) || Objects.equals(conversation.getUser2Id(), currentUserId)) {
                        conversationList.add(conversation);
                    }
                }
                // Ordenar por el último mensaje
                conversationList.sort((c1, c2) -> {
                    if (c1.getMessages().isEmpty() && c2.getMessages().isEmpty()) {
                        return 0;
                    } else if (c1.getMessages().isEmpty()) {
                        return 1;
                    } else if (c2.getMessages().isEmpty()) {
                        return -1;
                    } else {
                        return c2.getMessages().get(c2.getMessages().size() - 1).getDateTime().compareTo(c1.getMessages().get(c1.getMessages().size() - 1).getDateTime());
                    }
                });
                conversationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Log.e("ChatFragment", "Error fetching conversations: " + error);
            }
        });
    }

    private void showConversationOptions(Conversation conversation) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_options, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Options");

        Button buttonDelete = dialogView.findViewById(R.id.buttonOption1);
        buttonDelete.setText("Delete Conversation");
        Button buttonViewProfile = dialogView.findViewById(R.id.buttonOption2);
        buttonViewProfile.setText("View User Profile");
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        buttonDelete.setOnClickListener(v -> {
            deleteConversation(conversation);
            dialog.dismiss();
        });

        buttonViewProfile.setOnClickListener(v -> {
            openUserProfile(conversation);
            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void deleteConversation(Conversation conversation) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_delete_confirmation, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Delete Conversation");
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        dialogMessage.setText("Are you sure you want to delete this conversation?");
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        buttonConfirm.setOnClickListener(v -> {
            conversationController.deleteConversation(conversation.getConversationId(), new ConversationController.ConversationCallback() {
                @Override
                public void onSuccess(Conversation result) {
                    fetchConversations();
                    dialog.dismiss();
                }

                @Override
                public void onSuccess(Object result) {
                    fetchConversations();
                    dialog.dismiss();
                }

                @Override
                public void onError(String error) {
                    Log.e("ChatFragment", "Error deleting conversation: " + error);
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private void openUserProfile(Conversation conversation) {
        Long otherUserId = conversation.getUser1Id().equals(currentUserId) ? conversation.getUser2Id() : conversation.getUser1Id();
        UserController userController = new UserController(getContext());
        userController.getUserById(otherUserId, new UserController.UserCallback() {
            @Override
            public void onSuccess(Object result) {
                User user=(User) result;
                StrangeProfileFragment profileFragment = StrangeProfileFragment.newInstance(user);
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, profileFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onSuccess(User user) {
                StrangeProfileFragment profileFragment = StrangeProfileFragment.newInstance(user);
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, profileFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onError(String error) {
                Log.e("ChatFragment", "Error fetching user profile: " + error);
            }
        });
    }

    private void openMessageFragment(Conversation conversation) {
        MessageFragment messageFragment = MessageFragment.newInstance(conversation);
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, messageFragment)
                .addToBackStack(null)
                .commit();
    }


}
