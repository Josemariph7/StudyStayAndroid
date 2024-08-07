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
import com.example.studystayandroid.model.ForumComment;
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
 * Controlador para gestionar las operaciones relacionadas con los comentarios del foro.
 */
public class ForumCommentController {

    private static final String URL_GET_COMMENTS = "http://" + Constants.IP + "/studystay/comment/getComments.php";
    private static final String URL_CREATE_COMMENT = "http://" + Constants.IP + "/studystay/comment/createComment.php";
    private static final String URL_UPDATE_COMMENT = "http://" + Constants.IP + "/studystay/comment/updateComment.php";
    private static final String URL_DELETE_COMMENT = "http://" + Constants.IP + "/studystay/comment/deleteComment.php";
    private static final String URL_GET_COMMENT_BY_ID = "http://" + Constants.IP + "/studystay/comment/getCommentById.php";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RequestQueue requestQueue;
    private Context context;

    /**
     * Constructor para inicializar el controlador de comentarios del foro.
     *
     * @param context el contexto de la aplicación
     */
    public ForumCommentController(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    /**
     * Obtiene la lista de comentarios de un tema específico desde el servidor.
     *
     * @param topicId  el ID del tema del foro
     * @param callback el callback para manejar la respuesta
     */
    public void getComments(Long topicId, final CommentListCallback callback) {
        String url = URL_GET_COMMENTS + "?topicId=" + topicId;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<ForumComment> comments = new ArrayList<>();
                        UserController userController = new UserController(context);
                        ForumTopicController forumTopicController = new ForumTopicController(context);
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject commentObject = response.getJSONObject(i);
                            Long commentId = commentObject.getLong("CommentId");
                            Long topicId1 = commentObject.getLong("TopicId");
                            Long authorId = commentObject.getLong("AuthorId");
                            String content = commentObject.getString("Content");
                            String dateTimeString = commentObject.getString("DateTime");
                            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);
                            userController.getUserById(authorId, new UserController.UserCallback() {
                                @Override
                                public void onSuccess(Object result) {}
                                @Override
                                public void onSuccess(User author) {
                                    forumTopicController.getTopicById(topicId1, new ForumTopicController.TopicCallback() {
                                        @Override
                                        public void onSuccess(ForumTopic topic) {
                                            ForumComment comment = new ForumComment(commentId, topic, author, content, dateTime);
                                            comments.add(comment);
                                            if (comments.size() == response.length()) {
                                                callback.onSuccess(comments);
                                            }
                                        }

                                        @Override
                                        public void onSuccess(Object result) {
                                            ForumTopic topic = (ForumTopic) result;
                                            ForumComment comment = new ForumComment(commentId, topic, author, content, dateTime);
                                            comments.add(comment);
                                            if (comments.size() == response.length()) {
                                                callback.onSuccess(comments);
                                            }
                                        }
                                        @Override
                                        public void onError(String error) {
                                            callback.onError(error);
                                        }
                                    });
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
     * Obtiene un comentario específico por su ID.
     *
     * @param commentId el ID del comentario
     * @param callback  el callback para manejar la respuesta
     */
    public void getCommentById(Long commentId, final CommentCallback callback) {
        String url = URL_GET_COMMENT_BY_ID + "?commentId=" + commentId;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            JSONObject commentObject = response.getJSONObject(0);
                            Long id = commentObject.getLong("CommentId");
                            Long topicId = commentObject.getLong("TopicId");
                            Long authorId = commentObject.getLong("AuthorId");
                            String content = commentObject.getString("Content");
                            String dateTimeString = commentObject.getString("DateTime");
                            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);

                            UserController userController = new UserController(context);
                            userController.getUserById(authorId, new UserController.UserCallback() {
                                @Override
                                public void onSuccess(Object result) {}
                                @Override
                                public void onSuccess(User author) {
                                    ForumTopicController forumTopicController = new ForumTopicController(context);
                                    forumTopicController.getTopicById(topicId, new ForumTopicController.TopicCallback() {
                                        @Override
                                        public void onSuccess(ForumTopic topic) {
                                            ForumComment comment = new ForumComment(id, topic, author, content, dateTime);
                                            callback.onSuccess(comment);
                                        }

                                        @Override
                                        public void onSuccess(Object result) {
                                            ForumTopic topic = (ForumTopic) result;
                                            ForumComment comment = new ForumComment(id, topic, author, content, dateTime);
                                            callback.onSuccess(comment);
                                        }
                                        @Override
                                        public void onError(String error) {
                                            callback.onError(error);
                                        }
                                    });
                                }

                                @Override
                                public void onError(String error) {
                                    callback.onError(error);
                                }
                            });
                        } else {
                            callback.onError("Comment not found");
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
     * Crea un nuevo comentario.
     *
     * @param comment  el comentario a ser creado
     * @param callback el callback para manejar la respuesta
     */
    public void createComment(ForumComment comment, final CommentCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_CREATE_COMMENT,
                response -> {
                    if ("Comment created successfully".equals(response)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response);
                    }
                },
                error -> callback.onError(error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("topicId", comment.getTopic().getTopicId().toString());
                params.put("authorId", comment.getAuthor().getUserId().toString());
                params.put("content", comment.getContent());
                params.put("dateTime", comment.getDateTime().format(DATE_TIME_FORMATTER));
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    /**
     * Actualiza un comentario existente.
     *
     * @param comment  el comentario a ser actualizado
     * @param callback el callback para manejar la respuesta
     */
    public void updateComment(ForumComment comment, final CommentCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPDATE_COMMENT,
                response -> {
                    if ("Comment updated successfully".equals(response)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response);
                    }
                },
                error -> callback.onError(error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("commentId", comment.getCommentId().toString());
                params.put("topicId", comment.getTopic().getTopicId().toString());
                params.put("authorId", comment.getAuthor().getUserId().toString());
                params.put("content", comment.getContent());
                params.put("dateTime", comment.getDateTime().format(DATE_TIME_FORMATTER));
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    /**
     * Elimina un comentario por su ID.
     *
     * @param commentId el ID del comentario a ser eliminado
     * @param callback  el callback para manejar la respuesta
     */
    public void deleteComment(Long commentId, final CommentCallback callback) {
        String url = URL_DELETE_COMMENT + "?commentId=" + commentId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    if ("Comment deleted successfully".equals(response)) {
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
     * Interfaz para manejar un solo comentario.
     */
    public interface CommentCallback {
        void onSuccess(ForumComment comment);
        void onSuccess(Object result);
        void onError(String error);
    }

    /**
     * Interfaz para manejar la lista de comentarios.
     */
    public interface CommentListCallback {
        void onSuccess(List<ForumComment> comments);
        void onError(String error);
    }
}
