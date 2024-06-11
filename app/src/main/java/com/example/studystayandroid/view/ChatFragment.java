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

/**
 * Fragmento para mostrar y gestionar las conversaciones de chat.
 */
public class ChatFragment extends Fragment {

    private RecyclerView recyclerViewConversations;
    private ConversationAdapter conversationAdapter;
    private List<Conversation> conversationList;

    private ConversationController conversationController;
    private Long currentUserId;

    /**
     * Constructor público y vacío requerido.
     */
    public ChatFragment() {
        // Constructor vacío requerido
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

        conversationAdapter.setOnItemClickListener(this::openMessageFragment);
        conversationAdapter.setOnItemLongClickListener(this::showConversationOptions);
    }

    /**
     * Obtiene las conversaciones del usuario actual.
     */
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

    /**
     * Muestra las opciones para la conversación seleccionada.
     *
     * @param conversation La conversación seleccionada.
     */
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

    /**
     * Elimina la conversación seleccionada.
     *
     * @param conversation La conversación a eliminar.
     */
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

    /**
     * Abre el perfil del usuario en la conversación.
     *
     * @param conversation La conversación seleccionada.
     */
    private void openUserProfile(Conversation conversation) {
        Long otherUserId = conversation.getUser1Id().equals(currentUserId) ? conversation.getUser2Id() : conversation.getUser1Id();
        UserController userController = new UserController(getContext());
        userController.getUserById(otherUserId, new UserController.UserCallback() {
            @Override
            public void onSuccess(Object result) {
                User user = (User) result;
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

    /**
     * Abre el fragmento de mensajes para la conversación seleccionada.
     *
     * @param conversation La conversación seleccionada.
     */
    private void openMessageFragment(Conversation conversation) {
        MessageFragment messageFragment = MessageFragment.newInstance(conversation);
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, messageFragment)
                .addToBackStack(null)
                .commit();
    }
}
