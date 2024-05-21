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
import com.example.studystayandroid.model.Booking;
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

public class BookingController {

    private static final String URL_GET_BOOKINGS = "http://" + Constants.IP + "/studystay/booking/getBookings.php";
    private static final String URL_CREATE_BOOKING = "http://" + Constants.IP + "/studystay/booking/createBooking.php";
    private static final String URL_UPDATE_BOOKING = "http://" + Constants.IP + "/studystay/booking/updateBooking.php";
    private static final String URL_DELETE_BOOKING = "http://" + Constants.IP + "/studystay/booking/deleteBooking.php";
    private static final String URL_GET_BOOKING_BY_ID = "http://" + Constants.IP + "/studystay/booking/getBookingById.php";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RequestQueue requestQueue;
    private Context context;

    public BookingController(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public void getBookings(final BookingListCallback callback) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL_GET_BOOKINGS, null,
                response -> {
                    try {
                        List<Booking> bookings = new ArrayList<>();
                        UserController userController = new UserController(context);
                        AccommodationController accommodationController = new AccommodationController(context);
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject bookingObject = response.getJSONObject(i);
                            Long bookingId = bookingObject.getLong("BookingId");
                            Long accommodationId = bookingObject.getLong("AccommodationId");
                            Long userId = bookingObject.getLong("UserId");
                            String startDateString = bookingObject.getString("StartDate");
                            String endDateString = bookingObject.getString("EndDate");
                            String statusString = bookingObject.getString("Status");

                            LocalDateTime startDate = LocalDateTime.parse(startDateString, DATE_TIME_FORMATTER);
                            LocalDateTime endDate = LocalDateTime.parse(endDateString, DATE_TIME_FORMATTER);
                            Booking.BookingStatus status = Booking.BookingStatus.valueOf(statusString);

                            userController.getUserById(userId, new UserController.UserCallback() {
                                @Override
                                public void onSuccess(Object result) {}
                                @Override
                                public void onSuccess(User user) {
                                    accommodationController.getAccommodationById(accommodationId, new AccommodationController.AccommodationCallback() {
                                        @Override
                                        public void onSuccess(Accommodation accommodation) {
                                            Booking booking = new Booking(accommodation, user, startDate, endDate, status);
                                            booking.setBookingId(bookingId);
                                            bookings.add(booking);
                                            if (bookings.size() == response.length()) {
                                                callback.onSuccess(bookings);
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

    public void getBookingById(Long bookingId, final BookingCallback callback) {
        String url = URL_GET_BOOKING_BY_ID + "?bookingId=" + bookingId;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            JSONObject bookingObject = response.getJSONObject(0);
                            Long id = bookingObject.getLong("BookingId");
                            Long accommodationId = bookingObject.getLong("AccommodationId");
                            Long userId = bookingObject.getLong("UserId");
                            String startDateString = bookingObject.getString("StartDate");
                            String endDateString = bookingObject.getString("EndDate");
                            String statusString = bookingObject.getString("Status");

                            LocalDateTime startDate = LocalDateTime.parse(startDateString, DATE_TIME_FORMATTER);
                            LocalDateTime endDate = LocalDateTime.parse(endDateString, DATE_TIME_FORMATTER);
                            Booking.BookingStatus status = Booking.BookingStatus.valueOf(statusString);

                            UserController userController = new UserController(context);
                            userController.getUserById(userId, new UserController.UserCallback() {
                                @Override
                                public void onSuccess(Object result) {}
                                @Override
                                public void onSuccess(User user) {
                                    AccommodationController accommodationController = new AccommodationController(context);
                                    accommodationController.getAccommodationById(accommodationId, new AccommodationController.AccommodationCallback() {
                                        @Override
                                        public void onSuccess(Accommodation accommodation) {
                                            Booking booking = new Booking(accommodation, user, startDate, endDate, status);
                                            booking.setBookingId(id);
                                            callback.onSuccess(booking);
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
                            callback.onError("Booking not found");
                        }
                    } catch (JSONException e) {
                        callback.onError(e.getMessage());
                    }
                },
                error -> callback.onError(error.toString())
        );

        requestQueue.add(jsonArrayRequest);
    }

    public void createBooking(Booking booking, final BookingCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_CREATE_BOOKING,
                response -> {
                    if ("Booking created successfully".equals(response)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response);
                    }
                },
                error -> callback.onError(error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("accommodationId", booking.getAccommodation().getAccommodationId().toString());
                params.put("userId", booking.getUser().getUserId().toString());
                params.put("startDate", booking.getStartDate().format(DATE_TIME_FORMATTER));
                params.put("endDate", booking.getEndDate().format(DATE_TIME_FORMATTER));
                params.put("status", booking.getStatus().name());
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    public void updateBooking(Booking booking, final BookingCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPDATE_BOOKING,
                response -> {
                    if ("Booking updated successfully".equals(response)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response);
                    }
                },
                error -> callback.onError(error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("bookingId", booking.getBookingId().toString());
                params.put("accommodationId", booking.getAccommodation().getAccommodationId().toString());
                params.put("userId", booking.getUser().getUserId().toString());
                params.put("startDate", booking.getStartDate().format(DATE_TIME_FORMATTER));
                params.put("endDate", booking.getEndDate().format(DATE_TIME_FORMATTER));
                params.put("status", booking.getStatus().name());
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    public void deleteBooking(Long bookingId, final BookingCallback callback) {
        String url = URL_DELETE_BOOKING + "?bookingId=" + bookingId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    if ("Booking deleted successfully".equals(response)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response);
                    }
                },
                error -> callback.onError(error.toString())
        );

        requestQueue.add(stringRequest);
    }

    public interface BookingCallback {
        void onSuccess(Object result);
        void onError(String error);
    }

    public interface BookingListCallback {
        void onSuccess(List<Booking> bookings);
        void onError(String error);
    }
}
