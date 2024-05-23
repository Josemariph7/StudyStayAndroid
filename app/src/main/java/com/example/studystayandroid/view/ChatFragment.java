package com.example.studystayandroid.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.ConversationController;
import com.example.studystayandroid.controller.MessageController;
import com.example.studystayandroid.model.Conversation;
import com.example.studystayandroid.model.Message;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerViewConversations;
    private RecyclerView recyclerViewMessages;
    private ConversationAdapter conversationAdapter;
    private MessageAdapter messageAdapter;
    private List<Conversation> conversationList;
    private List<Message> messageList;

    private LinearLayout layoutMessages;
    private EditText editTextMessage;
    private Button buttonSend;
    private ImageButton buttonBack;

    private Conversation selectedConversation;
    private LinearLayout messageInputLayout;

    private MessageController messageController;
    private ConversationController conversationController;

    private Long currentUserId; // ID del usuario actual

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

        // Obtener el ID del usuario actual desde SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentUserId = sharedPreferences.getLong("userId", -1);
        if (getActivity() != null) {
            getActivity().setTitle("StudyStay - Chats");
        }
        if (currentUserId == -1) {
            Log.e("ChatFragment", "Error: Usuario no autenticado.");
            return;
        }

        messageController = new MessageController(requireContext());
        conversationController = new ConversationController(requireContext());
        messageInputLayout = new LinearLayout(requireContext());

        recyclerViewConversations = view.findViewById(R.id.recyclerViewConversations);
        recyclerViewConversations.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(getContext()));

        messageInputLayout = view.findViewById(R.id.messageInputLayout);
        messageInputLayout.setVisibility(View.GONE);

        layoutMessages = view.findViewById(R.id.layoutMessages);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);
        buttonBack = view.findViewById(R.id.buttonBack);

        conversationList = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(conversationList, requireContext());
        recyclerViewConversations.setAdapter(conversationAdapter);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId, requireContext());
        recyclerViewMessages.setAdapter(messageAdapter);

        fetchConversations();

        conversationAdapter.setOnItemClickListener(conversation -> {
            selectedConversation = conversation;
            fetchMessages(conversation.getConversationId());
            layoutMessages.setVisibility(View.VISIBLE);
            recyclerViewConversations.setVisibility(View.GONE);
            buttonBack.setVisibility(View.VISIBLE);
            messageInputLayout.setVisibility(View.VISIBLE);
        });

        buttonSend.setOnClickListener(v -> sendMessage());

        buttonBack.setOnClickListener(v -> {
            layoutMessages.setVisibility(View.GONE);
            recyclerViewConversations.setVisibility(View.VISIBLE);
            buttonBack.setVisibility(View.GONE);
        });
    }

    private void fetchConversations() {
        conversationController.getConversations(currentUserId, new ConversationController.ConversationListCallback() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                conversationList.clear();
                conversationList.addAll(conversations);
                conversationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Log.e("ChatFragment", "Error fetching conversations: " + error);
            }
        });
    }

    private void fetchMessages(Long conversationId) {
        messageController.getMessages(conversationId, new MessageController.MessageListCallback() {
            @Override
            public void onSuccess(List<Message> messages) {
                messageList.clear();
                messageList.addAll(messages);
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Log.e("ChatFragment", "Error fetching messages: " + error);
            }
        });
    }

    private void sendMessage() {
        String content = editTextMessage.getText().toString();
        if (content.isEmpty()) {
            return;
        }

        Message message = new Message(null, selectedConversation.getConversationId(), currentUserId, selectedConversation.getUser2Id(), content, LocalDateTime.now());
        messageList.add(message);
        messageAdapter.notifyDataSetChanged();
        editTextMessage.setText("");

        messageController.createMessage(message, new MessageController.MessageCallback() {
            @Override
            public void onSuccess(Object result) {
                // Mensaje enviado con éxito
            }

            @Override
            public void onError(String error) {
                Log.e("ChatFragment", "Error sending message: " + error);
            }
        });
    }
}
