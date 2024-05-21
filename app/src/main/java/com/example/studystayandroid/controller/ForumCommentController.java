package com.example.studystayandroid.controller;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.studystayandroid.model.ForumComment;
import com.example.studystayandroid.model.ForumTopic;
import com.example.studystayandroid.model.User;
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

public class ForumCommentController {

    private static final String URL_GET_COMMENTS = "http://" + Constants.IP + "/studystay/comment/getComments.php";
    private static final String URL_CREATE_COMMENT = "http://" + Constants.IP + "/studystay/comment/createComment.php";
    private static final String URL_UPDATE_COMMENT = "http://" + Constants.IP + "/studystay/comment/updateComment.php";
    private static final String URL_DELETE_COMMENT = "http://" + Constants.IP + "/studystay/comment/deleteComment.php";
    private static final String URL_GET_COMMENT_BY_ID = "http://" + Constants.IP + "/studystay/comment/getCommentById.php";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RequestQueue requestQueue;
    private Context context;

    public ForumCommentController(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

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

    public interface CommentCallback {
        void onSuccess(Object result);
        void onError(String error);
    }

    public interface CommentListCallback {
        void onSuccess(List<ForumComment> comments);
        void onError(String error);
    }
}
