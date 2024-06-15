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
import com.example.studystayandroid.model.Booking;
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
 * Controlador para gestionar las operaciones relacionadas con las reservas de alojamiento.
 */
public class BookingController {

    private static final String URL_GET_BOOKINGS = "http://" + Constants.IP + "/studystay/booking/getBookings.php";
    private static final String URL_CREATE_BOOKING = "http://" + Constants.IP + "/studystay/booking/createBooking.php";
    private static final String URL_UPDATE_BOOKING = "http://" + Constants.IP + "/studystay/booking/updateBooking.php";
    private static final String URL_DELETE_BOOKING = "http://" + Constants.IP + "/studystay/booking/deleteBooking.php";
    private static final String URL_GET_BOOKING_BY_ID = "http://" + Constants.IP + "/studystay/booking/getBookingById.php";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RequestQueue requestQueue;
    private Context context;

    /**
     * Constructor para inicializar el controlador de reservas de alojamiento.
     *
     * @param context el contexto de la aplicación
     */
    public BookingController(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    /**
     * Obtiene la lista de reservas desde el servidor.
     *
     * @param callback el callback para manejar la respuesta
     */
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
                                        public void onSuccess(Object result) {
                                            Accommodation accommodation = (Accommodation) result;
                                            Booking booking = new Booking(accommodation, user, startDate, endDate, status);
                                            booking.setBookingId(bookingId);
                                            bookings.add(booking);
                                            if (bookings.size() == response.length()) {
                                                callback.onSuccess(bookings);
                                            }
                                        }

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

    /**
     * Obtiene una reserva específica por su ID.
     *
     * @param bookingId el ID de la reserva
     * @param callback el callback para manejar la respuesta
     */
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
                                        public void onSuccess(Object result) {
                                            Accommodation accommodation = (Accommodation) result;
                                            Booking booking = new Booking(accommodation, user, startDate, endDate, status);
                                            booking.setBookingId(id);
                                            callback.onSuccess(booking);
                                        }

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

    /**
     * Crea una nueva reserva.
     *
     * @param booking la reserva a ser creada
     * @param callback el callback para manejar la respuesta
     */
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
                params.put("status", "PENDING");
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    /**
     * Actualiza una reserva existente.
     *
     * @param booking la reserva a ser actualizada
     * @param callback el callback para manejar la respuesta
     */
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

    /**
     * Elimina una reserva por su ID.
     *
     * @param bookingId el ID de la reserva a ser eliminada
     * @param callback el callback para manejar la respuesta
     */
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

    /**
     * Interfaz para manejar una sola reserva.
     */
    public interface BookingCallback {
        void onSuccess(Object result);
        void onError(String error);
    }

    /**
     * Interfaz para manejar la lista de reservas.
     */
    public interface BookingListCallback {
        void onSuccess(List<Booking> bookings);
        void onError(String error);
    }
}
