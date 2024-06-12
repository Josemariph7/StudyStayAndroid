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
import com.example.studystayandroid.model.Accommodation;
import com.example.studystayandroid.model.User;
import com.example.studystayandroid.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar las operaciones relacionadas con los alojamientos.
 */
public class AccommodationController {
    private static final String URL_GET_ACCOMMODATIONS = "http://" + Constants.IP + "/studystay/accommodation/getAccommodations.php";
    private static final String URL_GET_ACCOMMODATION_BY_ID = "http://" + Constants.IP + "/studystay/accommodation/getAccommodationById.php";
    private static final String URL_CREATE_ACCOMMODATION = "http://" + Constants.IP + "/studystay/accommodation/createAccommodation.php";
    private static final String URL_UPDATE_ACCOMMODATION = "http://" + Constants.IP + "/studystay/accommodation/updateAccommodation.php";
    private static final String URL_DELETE_ACCOMMODATION = "http://" + Constants.IP + "/studystay/accommodation/deleteAccommodation.php";

    private RequestQueue requestQueue;
    private UserController userController;

    /**
     * Constructor para inicializar el controlador de alojamientos.
     *
     * @param context el contexto de la aplicación
     */
    public AccommodationController(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        userController = new UserController(context);
    }

    /**
     * Interfaz para manejar la lista de alojamientos.
     */
    public interface AccommodationListCallback {
        void onSuccess(List<Accommodation> accommodations);
        void onError(String error);
    }

    /**
     * Interfaz para manejar un único alojamiento.
     */
    public interface AccommodationCallback {
        void onSuccess(Object result);
        void onSuccess(Accommodation accommodation);
        void onError(String error);
    }

    /**
     * Interfaz para manejar operaciones simples con alojamiento.
     */
    public interface SimpleCallback {
        void onSuccess(Accommodation accommodation);
        void onSuccess();
        void onError(String error);
    }

    /**
     * Obtiene la lista de alojamientos desde el servidor.
     *
     * @param callback el callback para manejar la respuesta
     */
    public void getAccommodations(final AccommodationListCallback callback) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL_GET_ACCOMMODATIONS, null,
                response -> {
                    try {
                        List<Accommodation> accommodations = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject accommodationObject = response.getJSONObject(i);
                            Long accommodationId = accommodationObject.getLong("AccommodationId");
                            Long ownerId = accommodationObject.getLong("OwnerId");
                            String address = accommodationObject.getString("Address");
                            String city = accommodationObject.getString("City");
                            BigDecimal price = new BigDecimal(accommodationObject.getString("Price"));
                            String description = accommodationObject.getString("Description");
                            int capacity = accommodationObject.getInt("Capacity");
                            String services = accommodationObject.getString("Services");
                            boolean availability = accommodationObject.getInt("Availability") == 1;
                            double rating = accommodationObject.getDouble("Rating");

                            boolean finalAvailability = availability;
                            userController.getUserById(ownerId, new UserController.UserCallback() {
                                @Override
                                public void onSuccess(Object result) {
                                    User user = (User) result;
                                    Accommodation accommodation = new Accommodation(user, address, city, price, description, capacity, services);
                                    accommodation.setAccommodationId(accommodationId);
                                    accommodation.setAvailability(finalAvailability);
                                    accommodation.setRating(rating);
                                    accommodations.add(accommodation);
                                    callback.onSuccess(accommodations);
                                }

                                @Override
                                public void onSuccess(User user) {
                                    Accommodation accommodation = new Accommodation(user, address, city, price, description, capacity, services);
                                    accommodation.setAccommodationId(accommodationId);
                                    accommodation.setAvailability(finalAvailability);
                                    accommodation.setRating(rating);
                                    accommodations.add(accommodation);
                                    callback.onSuccess(accommodations);
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
     * Obtiene un alojamiento específico por su ID.
     *
     * @param id el ID del alojamiento
     * @param callback el callback para manejar la respuesta
     */
    public void getAccommodationById(Long id, final AccommodationCallback callback) {
        String url = URL_GET_ACCOMMODATION_BY_ID + "?accommodationId=" + id;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            JSONObject accommodationObject = response.getJSONObject(0);
                            Long accommodationId = accommodationObject.getLong("AccommodationId");
                            Long ownerId = accommodationObject.getLong("OwnerId");
                            String address = accommodationObject.getString("Address");
                            String city = accommodationObject.getString("City");
                            BigDecimal price = new BigDecimal(accommodationObject.getString("Price"));
                            String description = accommodationObject.getString("Description");
                            int capacity = accommodationObject.getInt("Capacity");
                            String services = accommodationObject.getString("Services");
                            boolean availability = accommodationObject.getInt("Availability") == 1;
                            double rating = accommodationObject.getDouble("Rating");
                            boolean finalAvailability = availability;
                            userController.getUserById(ownerId, new UserController.UserCallback() {
                                @Override
                                public void onSuccess(Object result) {
                                    User user = (User) result;
                                    Accommodation accommodation = new Accommodation(user, address, city, price, description, capacity, services);
                                    accommodation.setAccommodationId(accommodationId);
                                    accommodation.setAvailability(finalAvailability);
                                    accommodation.setRating(rating);
                                    callback.onSuccess(accommodation);
                                }

                                @Override
                                public void onSuccess(User user) {
                                    Accommodation accommodation = new Accommodation(user, address, city, price, description, capacity, services);
                                    accommodation.setAccommodationId(accommodationId);
                                    accommodation.setAvailability(finalAvailability);
                                    accommodation.setRating(rating);
                                    callback.onSuccess(accommodation);
                                }

                                @Override
                                public void onError(String error) {
                                    callback.onError(error);
                                }
                            });
                        } else {
                            callback.onError("Accommodation not found");
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
     * Crea un nuevo alojamiento.
     *
     * @param accommodation el alojamiento a ser creado
     * @param callback el callback para manejar la respuesta
     */
    public void createAccommodation(Accommodation accommodation, final AccommodationCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_CREATE_ACCOMMODATION,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            long accommodationId = jsonResponse.getLong("accommodationId");
                            accommodation.setAccommodationId(accommodationId);
                            callback.onSuccess(accommodation);
                        } else {
                            callback.onError(jsonResponse.getString("message"));
                        }
                    } catch (JSONException e) {
                        callback.onError(e.getMessage());
                    }
                },
                error -> callback.onError(error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("ownerId", accommodation.getOwner().getUserId().toString());
                params.put("address", accommodation.getAddress());
                params.put("city", accommodation.getCity());
                params.put("price", accommodation.getPrice().toString());
                params.put("description", accommodation.getDescription());
                params.put("capacity", String.valueOf(accommodation.getCapacity()));
                params.put("services", accommodation.getServices());
                params.put("availability", String.valueOf(accommodation.isAvailability()));
                params.put("rating", String.valueOf(accommodation.getRating()));
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }


    /**
     * Actualiza un alojamiento existente.
     *
     * @param accommodation el alojamiento a ser actualizado
     * @param callback el callback para manejar la respuesta
     */
    public void updateAccommodation(Accommodation accommodation, final SimpleCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPDATE_ACCOMMODATION,
                response -> callback.onSuccess(),
                error -> callback.onError(error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("accommodationId", accommodation.getAccommodationId().toString());
                params.put("ownerId", accommodation.getOwner().getUserId().toString());
                params.put("address", accommodation.getAddress());
                params.put("city", accommodation.getCity());
                params.put("price", accommodation.getPrice().toString());
                params.put("description", accommodation.getDescription());
                params.put("capacity", String.valueOf(accommodation.getCapacity()));
                params.put("services", accommodation.getServices());
                params.put("availability", String.valueOf(accommodation.isAvailability()));
                params.put("rating", String.valueOf(accommodation.getRating()));
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    /**
     * Elimina un alojamiento por su ID.
     *
     * @param id el ID del alojamiento a ser eliminado
     * @param callback el callback para manejar la respuesta
     */
    public void deleteAccommodation(Long id, final AccommodationCallback callback) {
        String url = URL_DELETE_ACCOMMODATION + "?accommodationId=" + id;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> callback.onSuccess(new Accommodation()),
                error -> callback.onError(error.toString()));
        requestQueue.add(stringRequest);
    }
}
