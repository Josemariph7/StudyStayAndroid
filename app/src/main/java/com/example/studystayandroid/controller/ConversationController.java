package com.example.studystayandroid.controller;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.studystayandroid.model.Conversation;
import com.example.studystayandroid.model.Message;
import com.example.studystayandroid.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversationController {

    private static final String URL_GET_CONVERSATIONS = "http://" + Constants.IP + "/studystay/conversation/getConversations.php";
    private static final String URL_CREATE_CONVERSATION = "http://" + Constants.IP + "/studystay/conversation/createConversation.php";
    private static final String URL_DELETE_CONVERSATION = "http://" + Constants.IP + "/studystay/conversation/deleteConversation.php";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RequestQueue requestQueue;
    private Context context;

    public ConversationController(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public void getConversations(Long userId, final ConversationListCallback callback) {
        String url = URL_GET_CONVERSATIONS + "?userId=" + userId;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<Conversation> conversations = new ArrayList<>();
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
                            conversations.add(conversation);
                        }
                        callback.onSuccess(conversations);
                    } catch (JSONException e) {
                        callback.onError(e.getMessage());
                    }
                },
                error -> callback.onError(error.toString())
        );

        requestQueue.add(jsonArrayRequest);
    }

    public void createConversation(Conversation conversation, final ConversationCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_CREATE_CONVERSATION,
                response -> {
                    if ("Conversation created successfully".equals(response)) {
                        callback.onSuccess(conversation);
                    } else {
                        callback.onError(response);
                    }
                },
                error -> callback.onError(error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user1Id", conversation.getUser1Id().toString());
                params.put("user2Id", conversation.getUser2Id().toString());
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    public void deleteConversation(Long conversationId, final ConversationCallback callback) {
        String url = URL_DELETE_CONVERSATION + "?conversationId=" + conversationId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    if ("Conversation deleted successfully".equals(response)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response);
                    }
                },
                error -> callback.onError(error.toString())
        );

        requestQueue.add(stringRequest);
    }

    public interface ConversationCallback {
        void onSuccess(Conversation createdConversation);

        void onSuccess(Object result);
        void onError(String error);
    }

    public interface ConversationListCallback {
        void onSuccess(List<Conversation> conversations);
        void onError(String error);
    }
}
