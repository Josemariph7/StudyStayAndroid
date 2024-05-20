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
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.studystayandroid.R;
import com.example.studystayandroid.model.Conversation;
import com.example.studystayandroid.model.Message;
import com.example.studystayandroid.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private Conversation selectedConversation;

    private static final String URL_CONVERSATIONS = "http://" + Constants.IP + "/studystay/getConversations.php";
    private static final String URL_MESSAGES = "http://" + Constants.IP + "/studystay/getMessages.php";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

        if (currentUserId == -1) {
            Log.e("ChatFragment", "Error: Usuario no autenticado.");
            return;
        }

        recyclerViewConversations = view.findViewById(R.id.recyclerViewConversations);
        recyclerViewConversations.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(getContext()));

        layoutMessages = view.findViewById(R.id.layoutMessages);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);

        conversationList = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(conversationList);
        recyclerViewConversations.setAdapter(conversationAdapter);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId);
        recyclerViewMessages.setAdapter(messageAdapter);

        fetchConversations();

        conversationAdapter.setOnItemClickListener(conversation -> {
            selectedConversation = conversation;
            fetchMessages(conversation.getConversationId());
            layoutMessages.setVisibility(View.VISIBLE);
        });

        buttonSend.setOnClickListener(v -> sendMessage());
    }

    private void fetchConversations() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        Log.d("ChatFragment", "Fetching conversations...");
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL_CONVERSATIONS,
                null,
                response -> {
                    Log.d("ChatFragment", "Response received: " + response.toString());
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject conversationObject = response.getJSONObject(i);

                            Long conversationId = conversationObject.getLong("ConversationId");
                            Long user1Id = conversationObject.getLong("User1Id");
                            Long user2Id = conversationObject.getLong("User2Id");

                            JSONArray messagesArray = conversationObject.getJSONArray("Messages");
                            List<Message> messages = new ArrayList<>();
                            for (int j = 0; j < messagesArray.length(); j++) {
                                JSONObject messageObject = messagesArray.getJSONObject(j);
                                Long messageId = messageObject.getLong("MessageId");
                                Long senderId = messageObject.getLong("SenderId");
                                Long receiverId = messageObject.getLong("ReceiverId");
                                String content = messageObject.getString("Content");
                                String dateTimeString = messageObject.getString("DateTime");
                                LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);

                                Message message = new Message(messageId, conversationId, senderId, receiverId, content, dateTime);
                                messages.add(message);
                            }
                            Conversation conversation = new Conversation(conversationId, user1Id, user2Id, messages);
                            conversationList.add(conversation);
                            Log.d("ChatFragment", "Conversation: " + conversation);
                        }
                        conversationAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("ChatFragment", "JSON parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("ChatFragment", "Volley error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("ChatFragment", "Error code: " + error.networkResponse.statusCode);
                        String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        Log.e("ChatFragment", "Error body: " + responseBody);
                    }
                }
        );

        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000, // Timeout in milliseconds
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonArrayRequest);
    }

    private void fetchMessages(Long conversationId) {
        messageList.clear();
        messageAdapter.notifyDataSetChanged();

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());

        String url = URL_MESSAGES + "?conversationId=" + conversationId;
        Log.d("ChatFragment", "Fetching messages with URL: " + url);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("ChatFragment", "Messages response received: " + response.toString());
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject messageObject = response.getJSONObject(i);

                            Long messageId = messageObject.getLong("MessageId");
                            Long senderId = messageObject.getLong("SenderId");
                            Long receiverId = messageObject.getLong("ReceiverId");
                            String content = messageObject.getString("Content");
                            String dateTimeString = messageObject.getString("DateTime");
                            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);

                            Message message = new Message(messageId, conversationId, senderId, receiverId, content, dateTime);
                            messageList.add(message);
                        }
                        messageAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("ChatFragment", "JSON parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("ChatFragment", "Volley error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("ChatFragment", "Error code: " + error.networkResponse.statusCode);
                        String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        Log.e("ChatFragment", "Error body: " + responseBody);
                    }
                }
        );

        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000, // Timeout in milliseconds
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonArrayRequest);
    }

    private void sendMessage() {
        String content = editTextMessage.getText().toString();
        if (content.isEmpty()) {
            return;
        }

        // Crear el mensaje y añadirlo a la lista
        Message message = new Message(null, selectedConversation.getConversationId(), currentUserId, selectedConversation.getUser2Id(), content, LocalDateTime.now());
        messageList.add(message);
        messageAdapter.notifyDataSetChanged();
        editTextMessage.setText("");

        // Aquí puedes enviar el mensaje al servidor para almacenarlo en la base de datos
    }
}