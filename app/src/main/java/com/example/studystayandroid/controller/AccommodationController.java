package com.example.studystayandroid.controller;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.studystayandroid.model.Accommodation;
import com.example.studystayandroid.model.User;
import com.example.studystayandroid.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccommodationController {
    private static final String URL_GET_ACCOMMODATIONS = "http://" + Constants.IP + "/studystay/accommodation/getAccommodations.php";
    private static final String URL_GET_ACCOMMODATION_BY_ID = "http://" + Constants.IP + "/studystay/accommodation/getAccommodationById.php";
    private static final String URL_CREATE_ACCOMMODATION = "http://" + Constants.IP + "/studystay/accommodation/createAccommodation.php";
    private static final String URL_UPDATE_ACCOMMODATION = "http://" + Constants.IP + "/studystay/accommodation/updateAccommodation.php";
    private static final String URL_DELETE_ACCOMMODATION = "http://" + Constants.IP + "/studystay/accommodation/deleteAccommodation.php";

    private RequestQueue requestQueue;
    private UserController userController;

    public AccommodationController(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        userController = new UserController(context);
    }

    public interface AccommodationListCallback {
        void onSuccess(List<Accommodation> accommodations);
        void onError(String error);
    }

    public interface AccommodationCallback {
        void onSuccess(Accommodation accommodation);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }

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
                            boolean availability = false;
                            if (accommodationObject.getInt("Availability")==1) {
                                availability=true;
                            }else if(accommodationObject.getInt("Availability")==0){
                                availability=false;
                            }
                            double rating = accommodationObject.getDouble("Rating");

                            boolean finalAvailability = availability;
                            userController.getUserById(ownerId, new UserController.UserCallback() {
                                @Override
                                public void onSuccess(Object result) {}
                                @Override
                                public void onSuccess(User user) {
                                    Accommodation accommodation = new Accommodation(user, address, city, price, description, capacity, services);
                                    accommodation.setAccommodationId(accommodationId);
                                    accommodation.setAvailability(finalAvailability);
                                    accommodation.setRating(rating);
                                    accommodations.add(accommodation);
                                    callback.onSuccess(accommodations);  // Ensure this is called after fetching user
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
                            boolean availability = accommodationObject.getBoolean("Availability");
                            double rating = accommodationObject.getDouble("Rating");
                            userController.getUserById(ownerId, new UserController.UserCallback() {
                                @Override
                                public void onSuccess(Object result) {}
                                @Override
                                public void onSuccess(User user) {
                                    Accommodation accommodation = new Accommodation(user, address, city, price, description, capacity, services);
                                    accommodation.setAccommodationId(accommodationId);
                                    accommodation.setAvailability(availability);
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

    public void createAccommodation(Accommodation accommodation, final SimpleCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_CREATE_ACCOMMODATION,
                response -> callback.onSuccess(),
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

    public void deleteAccommodation(Long id, final SimpleCallback callback) {
        String url = URL_DELETE_ACCOMMODATION + "?accommodationId=" + id;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> callback.onSuccess(),
                error -> callback.onError(error.toString()));
        requestQueue.add(stringRequest);
    }
}
