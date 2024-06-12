package com.example.studystayandroid.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
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
    private static final int POLLING_INTERVAL_MS = 1000;

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

    private Handler handler;
    private Runnable pollingRunnable;

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

        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages);
        messageInputLayout = view.findViewById(R.id.messageInputLayout);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);
        button3 = view.findViewById(R.id.button3);
        imageViewProfile = view.findViewById(R.id.imageView2);
        textViewUser = view.findViewById(R.id.textViewUser);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentUserId = sharedPreferences.getLong("userId", -1);
        if (getActivity() != null) {
            getActivity().setTitle("StudyStay - Messages");
        }
        if (currentUserId == -1) {
            Log.e("MessageFragment", "Error: Usuario no autenticado.");
            return;
        }

        textViewUser.setOnClickListener(v -> openUserProfile());
        imageViewProfile.setOnClickListener(v -> openUserProfile());

        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId, requireContext());
        recyclerViewMessages.setAdapter(messageAdapter);

        buttonSend.setOnClickListener(v -> sendMessage());

        button3.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        fetchMessages(conversation.getConversationId());
        loadUserProfile();

        // Inicializar el Handler y el Runnable para el polling
        handler = new Handler();
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                fetchMessages(conversation.getConversationId());
                handler.postDelayed(this, POLLING_INTERVAL_MS);
            }
        };

        startPolling();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopPolling();
    }

    private void startPolling() {
        handler.post(pollingRunnable);
    }

    private void stopPolling() {
        handler.removeCallbacks(pollingRunnable);
    }

    private void openUserProfile() {
        Long otherUserId = conversation.getUser2Id().equals(currentUserId) ? conversation.getUser1Id() : conversation.getUser2Id();
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
                Log.e("MessageFragment", "Error fetching user profile: " + error);
            }
        });
    }

    private void scrollToBottom() {
        if (messageList.size() > 0) {
            recyclerViewMessages.smoothScrollToPosition(messageList.size() - 1);
        }
    }

    private void fetchMessages(Long conversationId) {
        messageController.getMessages(conversationId, new MessageController.MessageListCallback() {
            @Override
            public void onSuccess(List<Message> messages) {
                messageList.clear();
                messageList.addAll(messages);
                messageAdapter.notifyDataSetChanged();
                scrollToBottom(); // Asegúrate de hacer scroll al final después de cargar los mensajes
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
        scrollToBottom(); // Asegúrate de hacer scroll al final después de enviar un mensaje
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
                User user = (User) result;
                textViewUser.setText(user.getName() + " " + user.getLastName());
                byte[] profileImageBytes = user.getProfilePicture();
                if (profileImageBytes != null && profileImageBytes.length > 0) {
                    Glide.with(requireContext())
                            .asBitmap()
                            .load(profileImageBytes)
                            .transform(new CircleCrop())
                            .into(imageViewProfile);
                } else {
                    Glide.with(requireContext())
                            .load(R.drawable.defaultprofile)
                            .transform(new CircleCrop())
                            .into(imageViewProfile);
                }
            }

            @Override
            public void onSuccess(User user) {
                textViewUser.setText(user.getName() + " " + user.getLastName());
                byte[] profileImageBytes = user.getProfilePicture();
                if (profileImageBytes != null && profileImageBytes.length > 0) {
                    Glide.with(requireContext())
                            .asBitmap()
                            .load(profileImageBytes)
                            .transform(new CircleCrop())
                            .into(imageViewProfile);
                } else {
                    Glide.with(requireContext())
                            .load(R.drawable.defaultprofile)
                            .transform(new CircleCrop())
                            .into(imageViewProfile);
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
