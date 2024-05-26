package com.example.studystayandroid.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.MessageController;
import com.example.studystayandroid.controller.UserController;
import com.example.studystayandroid.model.Conversation;
import com.example.studystayandroid.model.Message;
import com.example.studystayandroid.model.User;

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
    private ImageButton button3;
    private ImageView imageViewProfile;
    private TextView textViewUser;

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
        button3 = view.findViewById(R.id.button3);
        imageViewProfile = view.findViewById(R.id.imageView2);
        textViewUser = view.findViewById(R.id.textViewUser);

        buttonSend.setOnClickListener(v -> sendMessage());

        button3.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        fetchMessages(conversation.getConversationId());
        loadUserProfile();
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

    private void loadUserProfile() {
        Long otherUserId = conversation.getUser2Id();
        if (otherUserId.equals(currentUserId)) {
            otherUserId = conversation.getUser1Id();
        }

        UserController userController = new UserController(getContext());
        Long finalOtherUserId = otherUserId;
        userController.getUserById(otherUserId, new UserController.UserCallback() {
            @Override
            public void onSuccess(Object result) {

            }

            @Override
            public void onSuccess(User user) {
                textViewUser.setText(user.getName() + " " + user.getLastName());
                byte[] profileImageBytes = user.getProfilePicture();
                if (profileImageBytes != null && profileImageBytes.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(profileImageBytes, 0, profileImageBytes.length);
                    imageViewProfile.setImageBitmap(bitmap);
                } else {
                    imageViewProfile.setImageResource(R.drawable.defaultprofile);
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onError(String error) {
                textViewUser.setText("User " + finalOtherUserId);
                imageViewProfile.setImageResource(R.drawable.defaultprofile);
            }
        });
    }
}
