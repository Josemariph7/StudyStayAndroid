package com.example.studystayandroid.controller;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.studystayandroid.model.User;
import com.example.studystayandroid.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserController {

    private static final String URL_LOGIN = "http://" + Constants.IP + "/studystay/login.php";
    private static final String URL_REGISTER = "http://" + Constants.IP + "/studystay/createUser.php";
    private static final String URL_GET_USER = "http://" + Constants.IP + "/studystay/getUser.php";
    private static final String URL_UPDATE_USER = "http://" + Constants.IP + "/studystay/updateUser.php";
    private static final String URL_DELETE_USER = "http://" + Constants.IP + "/studystay/deleteUser.php";
    private static final String URL_GET_ALL_USERS = "http://" + Constants.IP + "/studystay/getAllUsers.php";
    private static final String USER_PREFS = "UserPrefs";
    private static final String USER_ID = "userId";

    private RequestQueue requestQueue;
    private Context context;

    public UserController(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public void login(String email, String password, final UserCallback callback) {
        String url = URL_LOGIN + "?email=" + email + "&password=" + password;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String status = response.getString("status");
                        if ("yes".equals(status)) {
                            Long userId = response.getLong("userId");
                            saveUserId(userId);
                            callback.onSuccess(userId);
                        } else {
                            callback.onError("Invalid credentials");
                        }
                    } catch (JSONException e) {
                        callback.onError(e.getMessage());
                    }
                }, error -> callback.onError(error.toString())
        );

        requestQueue.add(jsonObjectRequest);
    }

    public void register(User user, final UserCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_REGISTER,
                response -> {
                    if ("User created successfully".equals(response)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response);
                    }
                }, error -> callback.onError(error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", user.getName());
                params.put("lastName", user.getLastName());
                params.put("email", user.getEmail());
                params.put("password", user.getPassword());
                params.put("phone", user.getPhone());
                params.put("birthDate", user.getBirthDate().toString());
                params.put("gender", user.getGender().name());
                params.put("dni", user.getDni());
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    public void getUserById(Long userId, final UserCallback callback) {
        String url = URL_GET_USER + "?userId=" + userId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        User user = new User();
                        user.setUserId(response.getLong("UserId"));
                        user.setName(response.getString("Name"));
                        user.setLastName(response.getString("LastName"));
                        user.setEmail(response.getString("Email"));
                        user.setPhone(response.getString("Phone"));
                        user.setBirthDate(LocalDate.parse(response.getString("BirthDate")));
                        user.setRegistrationDate(LocalDateTime.parse(response.getString("RegistrationDate")));
                        user.setGender(User.Gender.valueOf(response.getString("Gender")));
                        user.setDni(response.getString("DNI"));
                        user.setBio(response.getString("Bio"));
                        user.setAdmin(response.getBoolean("isAdmin"));
                        callback.onSuccess(user);
                    } catch (JSONException e) {
                        callback.onError(e.getMessage());
                    }
                }, error -> callback.onError(error.toString())
        );

        requestQueue.add(jsonObjectRequest);
    }

    public void updateUser(User user, final UserCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPDATE_USER,
                response -> {
                    if ("User updated successfully".equals(response)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response);
                    }
                }, error -> callback.onError(error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userId", user.getUserId().toString());
                params.put("name", user.getName());
                params.put("lastName", user.getLastName());
                params.put("email", user.getEmail());
                params.put("phone", user.getPhone());
                params.put("birthDate", user.getBirthDate().toString());
                params.put("gender", user.getGender().name());
                params.put("dni", user.getDni());
                params.put("bio", user.getBio());
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    public void deleteUser(Long userId, final UserCallback callback) {
        String url = URL_DELETE_USER + "?userId=" + userId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    if ("User deleted successfully".equals(response)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response);
                    }
                }, error -> callback.onError(error.toString())
        );

        requestQueue.add(stringRequest);
    }

    public void getAllUsers(final UserListCallback callback) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL_GET_ALL_USERS, null,
                response -> {
                    try {
                        List<User> users = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject userObject = response.getJSONObject(i);
                            User user = new User();
                            user.setUserId(userObject.getLong("UserId"));
                            user.setName(userObject.getString("Name"));
                            user.setLastName(userObject.getString("LastName"));
                            user.setEmail(userObject.getString("Email"));
                            user.setPhone(userObject.getString("Phone"));
                            user.setBirthDate(LocalDate.parse(userObject.getString("BirthDate")));
                            user.setRegistrationDate(LocalDateTime.parse(userObject.getString("RegistrationDate")));
                            user.setGender(User.Gender.valueOf(userObject.getString("Gender")));
                            user.setDni(userObject.getString("DNI"));
                            user.setBio(userObject.getString("Bio"));
                            user.setAdmin(userObject.getBoolean("isAdmin"));
                            users.add(user);
                        }
                        callback.onSuccess(users);
                    } catch (JSONException e) {
                        callback.onError(e.getMessage());
                    }
                }, error -> callback.onError(error.toString())
        );

        requestQueue.add(jsonArrayRequest);
    }

    public void saveUserId(Long userId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(USER_ID, userId);
        editor.apply();
    }

    public Long getUserId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(USER_ID, -1);
    }

    public void clearUserId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(USER_ID);
        editor.apply();
    }

    public interface UserCallback {
        void onSuccess(Object result);
        void onError(String error);
    }

    public interface UserListCallback {
        void onSuccess(List<User> users);
        void onError(String error);
    }
}
