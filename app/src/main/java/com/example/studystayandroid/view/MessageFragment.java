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
import com.example.studystayandroid.controller.MessageController;
import com.example.studystayandroid.model.Conversation;
import com.example.studystayandroid.model.Message;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageFragment extends Fragment {

    private static final String ARG_CONVERSATION = "arg_conversation";

    private Conversation conversation;
    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private LinearLayout messageInputLayout;
    private EditText editTextMessage;
    private Button buttonSend;
    private ImageButton buttonBack;

    private MessageController messageController;
    private Long currentUserId;

    public static MessageFragment newInstance(Conversation conversation) {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONVERSATION, conversation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            conversation = (Conversation) getArguments().getSerializable(ARG_CONVERSATION);
        }
        messageController = new MessageController(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentUserId = sharedPreferences.getLong("userId", -1);
        if (getActivity() != null) {
            getActivity().setTitle("StudyStay - Messages");
        }
        if (currentUserId == -1) {
            Log.e("MessageFragment", "Error: Usuario no autenticado.");
            return;
        }

        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(getContext()));

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId, requireContext());
        recyclerViewMessages.setAdapter(messageAdapter);

        messageInputLayout = view.findViewById(R.id.messageInputLayout);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);
        buttonBack = view.findViewById(R.id.buttonBack);

        buttonSend.setOnClickListener(v -> sendMessage());

        buttonBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        fetchMessages(conversation.getConversationId());
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
                Log.e("MessageFragment", "Error fetching messages: " + error);
            }
        });
    }

    private void sendMessage() {
        String content = editTextMessage.getText().toString();
        if (content.isEmpty()) {
            return;
        }

        Message message = new Message(null, conversation.getConversationId(), currentUserId, conversation.getUser2Id(), content, LocalDateTime.now());
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
                Log.e("MessageFragment", "Error sending message: " + error);
            }
        });
    }
}
