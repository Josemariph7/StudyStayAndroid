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
import com.example.studystayandroid.model.ForumTopic;
import com.example.studystayandroid.model.User;
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
 * Controlador para gestionar las operaciones relacionadas con los temas del foro.
 */
public class ForumTopicController {

    private static final String URL_GET_TOPICS = "http://" + Constants.IP + "/studystay/forum/getTopics.php";
    private static final String URL_CREATE_TOPIC = "http://" + Constants.IP + "/studystay/forum/createTopic.php";
    private static final String URL_UPDATE_TOPIC = "http://" + Constants.IP + "/studystay/forum/updateTopic.php";
    private static final String URL_DELETE_TOPIC = "http://" + Constants.IP + "/studystay/forum/deleteTopic.php";
    private static final String URL_GET_TOPIC_BY_ID = "http://" + Constants.IP + "/studystay/forum/getTopicById.php";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RequestQueue requestQueue;
    private Context context;

    /**
     * Constructor para inicializar el controlador de temas del foro.
     *
     * @param context el contexto de la aplicación
     */
    public ForumTopicController(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    /**
     * Obtiene la lista de temas del foro desde el servidor.
     *
     * @param callback el callback para manejar la respuesta
     */
    public void getTopics(final TopicListCallback callback) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL_GET_TOPICS, null,
                response -> {
                    try {
                        List<ForumTopic> topics = new ArrayList<>();
                        UserController userController = new UserController(context);
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject topicObject = response.getJSONObject(i);
                            Long topicId = topicObject.getLong("TopicId");
                            String title = topicObject.getString("Title");
                            String description = topicObject.getString("Description");
                            Long authorId = topicObject.getLong("AuthorId");
                            String dateTimeString = topicObject.getString("DateTime");
                            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);

                            userController.getUserById(authorId, new UserController.UserCallback() {
                                @Override
                                public void onSuccess(Object result) {}
                                @Override
                                public void onSuccess(User author) {
                                    ForumTopic topic = new ForumTopic(topicId, title, description, author, dateTime);
                                    topics.add(topic);
                                    if (topics.size() == response.length()) {
                                        callback.onSuccess(topics);
                                    }
                                }
                                @Override
                                public void onError(String error) {
                                    callback.onError(error);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        callback.onError(e.getMessage());
                    }
                },
                error -> callback.onError(error.toString())
        );

        requestQueue.add(jsonArrayRequest);
    }

    /**
     * Crea un nuevo tema del foro.
     *
     * @param topic    el tema del foro a ser creado
     * @param callback el callback para manejar la respuesta
     */
    public void createTopic(ForumTopic topic, final TopicCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_CREATE_TOPIC,
                response -> {
                    if ("Topic created successfully".equals(response)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response);
                    }
                },
                error -> callback.onError(error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("title", topic.getTitle());
                params.put("description", topic.getDescription());
                params.put("authorId", topic.getAuthor().getUserId().toString());
                params.put("dateTime", topic.getDateTime().format(DATE_TIME_FORMATTER));
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    /**
     * Actualiza un tema del foro existente.
     *
     * @param topic    el tema del foro a ser actualizado
     * @param callback el callback para manejar la respuesta
     */
    public void updateTopic(ForumTopic topic, final TopicCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPDATE_TOPIC,
                response -> {
                    if ("Topic updated successfully".equals(response)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response);
                    }
                },
                error -> callback.onError(error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("topicId", topic.getTopicId().toString());
                params.put("title", topic.getTitle());
                params.put("description", topic.getDescription());
                params.put("authorId", topic.getAuthor().getUserId().toString());
                params.put("dateTime", topic.getDateTime().format(DATE_TIME_FORMATTER));
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    /**
     * Elimina un tema del foro por su ID.
     *
     * @param topicId  el ID del tema del foro a ser eliminado
     * @param callback el callback para manejar la respuesta
     */
    public void deleteTopic(Long topicId, final TopicCallback callback) {
        String url = URL_DELETE_TOPIC + "?topicId=" + topicId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    if ("Topic deleted successfully".equals(response)) {
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
     * Obtiene un tema del foro específico por su ID.
     *
     * @param topicId  el ID del tema del foro
     * @param callback el callback para manejar la respuesta
     */
    public void getTopicById(Long topicId, final TopicCallback callback) {
        String url = URL_GET_TOPIC_BY_ID + "?topicId=" + topicId;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            JSONObject topicObject = response.getJSONObject(0);
                            Long id = topicObject.getLong("TopicId");
                            String title = topicObject.getString("Title");
                            String description = topicObject.getString("Description");
                            Long authorId = topicObject.getLong("AuthorId");
                            String dateTimeString = topicObject.getString("DateTime");
                            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);

                            UserController userController = new UserController(context);
                            userController.getUserById(authorId, new UserController.UserCallback() {
                                @Override
                                public void onSuccess(Object result) {}
                                @Override
                                public void onSuccess(User author) {
                                    ForumTopic topic = new ForumTopic(id, title, description, author, dateTime);
                                    callback.onSuccess(topic);
                                }
                                @Override
                                public void onError(String error) {
                                    callback.onError(error);
                                }
                            });
                        } else {
                            callback.onError("Topic not found");
                        }
                    } catch (JSONException e) {
                        callback.onError(e.getMessage());
                    }
                },
                error -> callback.onError(error.toString())
        );

        requestQueue.add(jsonArrayRequest);
    }

    /**
     * Interfaz para manejar un solo tema del foro.
     */
    public interface TopicCallback {
        void onSuccess(ForumTopic topic);
        void onSuccess(Object result);
        void onError(String error);
    }

    /**
     * Interfaz para manejar la lista de temas del foro.
     */
    public interface TopicListCallback {
        void onSuccess(List<ForumTopic> topics);
        void onError(String error);
    }
}
