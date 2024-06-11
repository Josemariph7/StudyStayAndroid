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
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.studystayandroid.model.User;
import com.example.studystayandroid.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar las operaciones relacionadas con los usuarios.
 */
public class UserController {

    private static final String URL_LOGIN = "http://" + Constants.IP + "/studystay/user/login.php";
    private static final String URL_REGISTER = "http://" + Constants.IP + "/studystay/user/createUser.php";
    private static final String URL_GET_USER = "http://" + Constants.IP + "/studystay/user/getUserById.php";
    private static final String URL_UPDATE_USER = "http://" + Constants.IP + "/studystay/user/updateUser.php";
    private static final String URL_UPDATE_USER_PASSWORD = "http://" + Constants.IP + "/studystay/user/updateUserPassword.php";
    private static final String URL_DELETE_USER = "http://" + Constants.IP + "/studystay/user/deleteUser.php";
    private static final String URL_GET_ALL_USERS = "http://" + Constants.IP + "/studystay/user/getAllUsers.php";
    private static final String URL_UPDATE_USER_PROFILE_PICTURE = "http://" + Constants.IP + "/studystay/user/updateUserProfilePicture.php";
    private static final String USER_PREFS = "UserPrefs";
    private static final String USER_ID = "userId";

    private RequestQueue requestQueue;
    private Context context;

    /**
     * Constructor para inicializar el controlador de usuarios.
     *
     * @param context el contexto de la aplicación
     */
    public UserController(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    /**
     * Inicia sesión con el correo electrónico y la contraseña proporcionados.
     *
     * @param email    el correo electrónico del usuario
     * @param password la contraseña del usuario
     * @param callback el callback para manejar la respuesta
     */
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
                            Log.d("UserController", "Login successful, userId: " + userId);
                        } else {
                            callback.onError("Invalid credentials");
                            Log.e("UserController", "Invalid credentials");
                        }
                    } catch (JSONException e) {
                        callback.onError(e.getMessage());
                        Log.e("UserController", "JSON error: " + e.getMessage());
                    }
                }, error -> {
            callback.onError(error.toString());
            Log.e("UserController", "Volley error: " + error.toString());
        }
        );

        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Registra un nuevo usuario.
     *
     * @param user     el usuario a ser registrado
     * @param callback el callback para manejar la respuesta
     */
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
                params.put("birthDate", user.getBirthDate() != null ? user.getBirthDate().toString() : "");
                params.put("registrationDate", user.getRegistrationDate() != null ? user.getRegistrationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
                params.put("gender", user.getGender().name());
                params.put("dni", user.getDni());
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    /**
     * Obtiene los detalles de un usuario por su ID.
     *
     * @param userId   el ID del usuario
     * @param callback el callback para manejar la respuesta
     */
    public void getUserById(Long userId, final UserCallback callback) {
        String url = URL_GET_USER + "?userId=" + userId;

        Log.d("UserController", "Getting user by ID: " + userId);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        User user = new User();
                        user.setUserId(response.getLong("UserId"));
                        user.setName(response.getString("Name"));
                        user.setLastName(response.getString("LastName"));
                        user.setEmail(response.getString("Email"));
                        user.setPhone(response.getString("Phone"));
                        user.setBirthDate(response.has("BirthDate") && !response.isNull("BirthDate") ? LocalDate.parse(response.getString("BirthDate")) : null);
                        user.setRegistrationDate(response.has("RegistrationDate") && !response.isNull("RegistrationDate") ? LocalDateTime.parse(response.getString("RegistrationDate"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
                        user.setGender(User.Gender.valueOf(response.getString("Gender").toUpperCase()));
                        user.setDni(response.getString("DNI"));
                        user.setBio(response.getString("Bio"));
                        int isAdminValue = response.getInt("isAdmin");
                        user.setAdmin(isAdminValue == 1);
                        if (response.has("ProfilePicture") && !response.isNull("ProfilePicture")) {
                            String profilePictureBase64 = response.getString("ProfilePicture");
                            byte[] profilePictureBytes = Base64.decode(profilePictureBase64, Base64.DEFAULT);
                            user.setProfilePicture(profilePictureBytes);
                            Log.d("UserController", "Profile picture decoded from base64");
                            Log.d("UserController", "Profile picture: " + user.getProfilePicture());
                        } else {
                            user.setProfilePicture(null);
                        }
                        callback.onSuccess(user);
                    } catch (JSONException | IllegalArgumentException e) {
                        Log.e("UserController", "Error parsing user details: " + e.getMessage());
                        callback.onError(e.getMessage());
                    }
                }, error -> {
            Log.e("UserController", "Error getting user by ID: " + error.toString());
            callback.onError(error.toString());
        }
        );

        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Actualiza los detalles de un usuario.
     *
     * @param user     el usuario a ser actualizado
     * @param callback el callback para manejar la respuesta
     */
    public void updateUser(User user, final UserCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPDATE_USER,
                response -> {
                    if ("User updated successfully".equals(response)) {
                        callback.onSuccess(user);
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
                params.put("bio", user.getBio() != null ? user.getBio() : "");
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }


    /**
     * Actualiza la contraseña de un usuario.
     *
     * @param userId          el ID del usuario
     * @param currentPassword la contraseña actual
     * @param newPassword     la nueva contraseña
     * @param callback        el callback para manejar la respuesta
     */
    public void updateUserPassword(Long userId, String currentPassword, String newPassword, final UserCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPDATE_USER_PASSWORD,
                response -> {
                    if ("Password updated successfully".equals(response)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response);
                    }
                }, error -> callback.onError(error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userId", userId.toString());
                params.put("currentPassword", currentPassword);
                params.put("newPassword", newPassword);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    /**
     * Elimina un usuario por su ID y verifica la contraseña.
     *
     * @param userId   el ID del usuario a ser eliminado
     * @param password la contraseña del usuario
     * @param callback el callback para manejar la respuesta
     */
    public void deleteUser(Long userId, String password, final UserCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_DELETE_USER,
                response -> {
                    if ("User deleted successfully".equals(response)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response);
                    }
                }, error -> callback.onError(error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userId", userId.toString());
                params.put("password", password);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }


    /**
     * Obtiene una lista de todos los usuarios desde el servidor.
     *
     * @param callback el callback para manejar la respuesta
     */
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

    /**
     * Actualiza la foto de perfil de un usuario.
     *
     * @param userId         el ID del usuario
     * @param profilePicture la nueva foto de perfil en formato byte[]
     * @param callback       el callback para manejar la respuesta
     */
    public void updateUserProfilePicture(Long userId, byte[] profilePicture, final UserCallback callback) {
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, URL_UPDATE_USER_PROFILE_PICTURE,
                response -> {
                    String resultResponse = new String(response.data);
                    if ("Profile picture updated successfully".equals(resultResponse)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(resultResponse);
                    }
                },
                error -> callback.onError(error.toString())) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userId", userId.toString());
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("profilePicture", new DataPart("profile_picture.jpg", profilePicture));
                return params;
            }
        };

        requestQueue.add(volleyMultipartRequest);
    }

    /**
     * Guarda el ID del usuario en las preferencias compartidas.
     *
     * @param userId el ID del usuario
     */
    public void saveUserId(Long userId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(USER_ID, userId);
        editor.apply();
    }

    /**
     * Obtiene el ID del usuario desde las preferencias compartidas.
     *
     * @return el ID del usuario guardado, o -1 si no se encuentra
     */
    public Long getUserId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(USER_ID, -1);
    }

    /**
     * Elimina el ID del usuario de las preferencias compartidas.
     */
    public void clearUserId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(USER_ID);
        editor.apply();
    }

    /**
     * Interfaz para manejar un solo usuario.
     */
    public interface UserCallback {
        void onSuccess(Object result);
        void onSuccess(User author);
        void onError(String error);
    }

    /**
     * Interfaz para manejar la lista de usuarios.
     */
    public interface UserListCallback {
        void onSuccess(List<User> users);
        void onError(String error);
    }
}
