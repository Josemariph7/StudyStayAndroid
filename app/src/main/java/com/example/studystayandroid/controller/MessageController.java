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

package com.example.studystayandroid.controller;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.studystayandroid.model.Message;
import com.example.studystayandroid.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar las operaciones relacionadas con los mensajes.
 */
public class MessageController {

    private static final String URL_GET_MESSAGES = "http://" + Constants.IP + "/studystay/message/getMessages.php";
    private static final String URL_CREATE_MESSAGE = "http://" + Constants.IP + "/studystay/message/createMessage.php";
    private static final String URL_DELETE_MESSAGE = "http://" + Constants.IP + "/studystay/message/deleteMessage.php";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RequestQueue requestQueue;
    private Context context;

    /**
     * Constructor para inicializar el controlador de mensajes.
     *
     * @param context el contexto de la aplicación
     */
    public MessageController(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    /**
     * Obtiene la lista de mensajes de una conversación específica desde el servidor.
     *
     * @param conversationId el ID de la conversación
     * @param callback       el callback para manejar la respuesta
     */
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

    /**
     * Crea un nuevo mensaje.
     *
     * @param message  el mensaje a ser creado
     * @param callback el callback para manejar la respuesta
     */
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

    /**
     * Elimina un mensaje por su ID.
     *
     * @param messageId el ID del mensaje a ser eliminado
     * @param callback  el callback para manejar la respuesta
     */
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

    /**
     * Interfaz para manejar un solo mensaje.
     */
    public interface MessageCallback {
        void onSuccess(Object result);
        void onError(String error);
    }

    /**
     * Interfaz para manejar la lista de mensajes.
     */
    public interface MessageListCallback {
        void onSuccess(List<Message> messages);
        void onError(String error);
    }
}
