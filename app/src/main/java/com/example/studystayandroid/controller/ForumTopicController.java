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

public class ForumTopicController {

    private static final String URL_GET_TOPICS = "http://" + Constants.IP + "/studystay/forum/getTopics.php";
    private static final String URL_CREATE_TOPIC = "http://" + Constants.IP + "/studystay/forum/createTopic.php";
    private static final String URL_UPDATE_TOPIC = "http://" + Constants.IP + "/studystay/forum/updateTopic.php";
    private static final String URL_DELETE_TOPIC = "http://" + Constants.IP + "/studystay/forum/deleteTopic.php";
    private static final String URL_GET_TOPIC_BY_ID = "http://" + Constants.IP + "/studystay/forum/getTopicById.php";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RequestQueue requestQueue;
    private Context context;

    public ForumTopicController(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

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
                                    ForumTopic topic = new ForumTopic(title, description, author, dateTime);
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
                                    ForumTopic topic = new ForumTopic(title, description, author, dateTime);
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

    public interface TopicCallback {
        void onSuccess(Object result);
        void onError(String error);
    }

    public interface TopicListCallback {
        void onSuccess(List<ForumTopic> topics);
        void onError(String error);
    }
}
