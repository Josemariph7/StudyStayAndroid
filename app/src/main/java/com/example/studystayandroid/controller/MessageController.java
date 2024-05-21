package com.example.studystayandroid.controller;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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

public class MessageController {

    private static final String URL_GET_MESSAGES = "http://" + Constants.IP + "/studystay/message/getMessages.php";
    private static final String URL_CREATE_MESSAGE = "http://" + Constants.IP + "/studystay/message/createMessage.php";
    private static final String URL_DELETE_MESSAGE = "http://" + Constants.IP + "/studystay/message/deleteMessage.php";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RequestQueue requestQueue;
    private Context context;

    public MessageController(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public void getMessages(Long conversationId, final MessageListCallback callback) {
        String url = URL_GET_MESSAGES + "?conversationId=" + conversationId;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<Message> messages = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject messageObject = response.getJSONObject(i);
                            Long messageId = messageObject.getLong("MessageId");
                            Long senderId = messageObject.getLong("SenderId");
                            Long receiverId = messageObject.getLong("ReceiverId");
                            String content = messageObject.getString("Content");
                            String dateTimeString = messageObject.getString("DateTime");
                            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);

                            Message message = new Message(messageId, conversationId, senderId, receiverId, content, dateTime);
                            messages.add(message);
                        }
                        callback.onSuccess(messages);
                    } catch (JSONException e) {
                        callback.onError(e.getMessage());
                    }
                },
                error -> callback.onError(error.toString())
        );

        requestQueue.add(jsonArrayRequest);
    }

    public void createMessage(Message message, final MessageCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_CREATE_MESSAGE,
                response -> {
                    if ("Message created successfully".equals(response)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response);
                    }
                },
                error -> callback.onError(error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("conversationId", message.getConversationId().toString());
                params.put("senderId", message.getSenderId().toString());
                params.put("receiverId", message.getReceiverId().toString());
                params.put("content", message.getContent());
                params.put("dateTime", message.getDateTime().format(DATE_TIME_FORMATTER));
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    public void deleteMessage(Long messageId, final MessageCallback callback) {
        String url = URL_DELETE_MESSAGE + "?messageId=" + messageId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    if ("Message deleted successfully".equals(response)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response);
                    }
                },
                error -> callback.onError(error.toString())
        );

        requestQueue.add(stringRequest);
    }

    public interface MessageCallback {
        void onSuccess(Object result);
        void onError(String error);
    }

    public interface MessageListCallback {
        void onSuccess(List<Message> messages);
        void onError(String error);
    }
}
