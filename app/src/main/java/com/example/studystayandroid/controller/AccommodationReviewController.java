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
import com.example.studystayandroid.model.AccommodationReview;
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
 * Controlador para gestionar las operaciones relacionadas con las reseñas de alojamiento.
 */
public class AccommodationReviewController {

    private static final String URL_GET_REVIEWS = "http://" + Constants.IP + "/studystay/review/getReviews.php";
    private static final String URL_CREATE_REVIEW = "http://" + Constants.IP + "/studystay/review/createReview.php";
    private static final String URL_UPDATE_REVIEW = "http://" + Constants.IP + "/studystay/review/updateReview.php";
    private static final String URL_DELETE_REVIEW = "http://" + Constants.IP + "/studystay/review/deleteReview.php";
    private static final String URL_GET_REVIEW_BY_ID = "http://" + Constants.IP + "/studystay/review/getReviewById.php";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RequestQueue requestQueue;
    private Context context;

    /**
     * Constructor para inicializar el controlador de reseñas de alojamiento.
     *
     * @param context el contexto de la aplicación
     */
    public AccommodationReviewController(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    /**
     * Obtiene la lista de reseñas desde el servidor.
     *
     * @param callback el callback para manejar la respuesta
     */
    public void getReviews(final ReviewListCallback callback) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL_GET_REVIEWS, null,
                response -> {
                    try {
                        List<AccommodationReview> reviews = new ArrayList<>();
                        UserController userController = new UserController(context);
                        AccommodationController accommodationController = new AccommodationController(context);
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject reviewObject = response.getJSONObject(i);
                            Long reviewId = reviewObject.getLong("ReviewId");
                            Long accommodationId = reviewObject.getLong("AccommodationId");
                            Long authorId = reviewObject.getLong("AuthorId");
                            double rating = reviewObject.getDouble("Rating");
                            String comment = reviewObject.getString("Comment");
                            String dateTimeString = reviewObject.getString("DateTime");
                            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);
                            userController.getUserById(authorId, new UserController.UserCallback() {
                                @Override
                                public void onSuccess(Object result) {}
                                @Override
                                public void onSuccess(User author) {
                                    accommodationController.getAccommodationById(accommodationId, new AccommodationController.AccommodationCallback() {
                                        @Override
                                        public void onSuccess(Object result) {
                                            Accommodation accommodation = (Accommodation) result;
                                            AccommodationReview review = new AccommodationReview(accommodation, author, rating, comment, dateTime);
                                            review.setReviewId(reviewId);
                                            reviews.add(review);
                                            if (reviews.size() == response.length()) {
                                                callback.onSuccess(reviews);
                                            }
                                        }

                                        @Override
                                        public void onSuccess(Accommodation accommodation) {
                                            AccommodationReview review = new AccommodationReview(accommodation, author, rating, comment, dateTime);
                                            review.setReviewId(reviewId);
                                            reviews.add(review);
                                            if (reviews.size() == response.length()) {
                                                callback.onSuccess(reviews);
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
     * Obtiene una reseña específica por su ID.
     *
     * @param reviewId el ID de la reseña
     * @param callback el callback para manejar la respuesta
     */
    public void getReviewById(Long reviewId, final ReviewCallback callback) {
        String url = URL_GET_REVIEW_BY_ID + "?reviewId=" + reviewId;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            JSONObject reviewObject = response.getJSONObject(0);
                            Long id = reviewObject.getLong("ReviewId");
                            Long accommodationId = reviewObject.getLong("AccommodationId");
                            Long authorId = reviewObject.getLong("AuthorId");
                            double rating = reviewObject.getDouble("Rating");
                            String comment = reviewObject.getString("Comment");
                            String dateTimeString = reviewObject.getString("DateTime");
                            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);
                            UserController userController = new UserController(context);
                            userController.getUserById(authorId, new UserController.UserCallback() {
                                @Override
                                public void onSuccess(Object result) {}
                                @Override
                                public void onSuccess(User author) {
                                    AccommodationController accommodationController = new AccommodationController(context);
                                    accommodationController.getAccommodationById(accommodationId, new AccommodationController.AccommodationCallback() {
                                        @Override
                                        public void onSuccess(Object result) {
                                            Accommodation accommodation = (Accommodation) result;
                                            AccommodationReview review = new AccommodationReview(accommodation, author, rating, comment, dateTime);
                                            review.setReviewId(id);
                                            callback.onSuccess(review);
                                        }

                                        @Override
                                        public void onSuccess(Accommodation accommodation) {
                                            AccommodationReview review = new AccommodationReview(accommodation, author, rating, comment, dateTime);
                                            review.setReviewId(id);
                                            callback.onSuccess(review);
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
                            callback.onError("Review not found");
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
     * Crea una nueva reseña.
     *
     * @param review la reseña a ser creada
     * @param callback el callback para manejar la respuesta
     */
    public void createReview(AccommodationReview review, final ReviewCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_CREATE_REVIEW,
                response -> {
                    if ("Review created successfully".equals(response)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response);
                    }
                },
                error -> callback.onError(error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("accommodationId", review.getAccommodation().getAccommodationId().toString());
                params.put("authorId", review.getAuthor().getUserId().toString());
                params.put("rating", String.valueOf(review.getRating()));
                params.put("comment", review.getComment());
                params.put("dateTime", review.getDateTime().format(DATE_TIME_FORMATTER));
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    /**
     * Actualiza una reseña existente.
     *
     * @param review la reseña a ser actualizada
     * @param callback el callback para manejar la respuesta
     */
    public void updateReview(AccommodationReview review, final ReviewCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPDATE_REVIEW,
                response -> {
                    if ("Review updated successfully".equals(response)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response);
                    }
                },
                error -> callback.onError(error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("reviewId", review.getReviewId().toString());
                params.put("accommodationId", review.getAccommodation().getAccommodationId().toString());
                params.put("authorId", review.getAuthor().getUserId().toString());
                params.put("rating", String.valueOf(review.getRating()));
                params.put("comment", review.getComment());
                params.put("dateTime", review.getDateTime().format(DATE_TIME_FORMATTER));
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    /**
     * Elimina una reseña por su ID.
     *
     * @param reviewId el ID de la reseña a ser eliminada
     * @param callback el callback para manejar la respuesta
     */
    public void deleteReview(Long reviewId, final ReviewCallback callback) {
        String url = URL_DELETE_REVIEW + "?reviewId=" + reviewId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    if ("Review deleted successfully".equals(response)) {
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
     * Interfaz para manejar una sola reseña.
     */
    public interface ReviewCallback {
        void onSuccess(Object result);
        void onError(String error);
    }

    /**
     * Interfaz para manejar la lista de reseñas.
     */
    public interface ReviewListCallback {
        void onSuccess(List<AccommodationReview> reviews);
        void onError(String error);
    }
}