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

public class AccommodationReviewController {

    private static final String URL_GET_REVIEWS = "http://" + Constants.IP + "/studystay/review/getReviews.php";
    private static final String URL_CREATE_REVIEW = "http://" + Constants.IP + "/studystay/review/createReview.php";
    private static final String URL_UPDATE_REVIEW = "http://" + Constants.IP + "/studystay/review/updateReview.php";
    private static final String URL_DELETE_REVIEW = "http://" + Constants.IP + "/studystay/review/deleteReview.php";
    private static final String URL_GET_REVIEW_BY_ID = "http://" + Constants.IP + "/studystay/review/getReviewById.php";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RequestQueue requestQueue;
    private Context context;

    public AccommodationReviewController(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

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

    public interface ReviewCallback {
        void onSuccess(Object result);
        void onError(String error);
    }

    public interface ReviewListCallback {
        void onSuccess(List<AccommodationReview> reviews);
        void onError(String error);
    }
}
