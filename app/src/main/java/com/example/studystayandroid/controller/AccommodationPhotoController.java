package com.example.studystayandroid.controller;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.studystayandroid.model.Accommodation;
import com.example.studystayandroid.model.AccommodationPhoto;
import com.example.studystayandroid.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccommodationPhotoController {

    private static final String URL_GET_PHOTOS = "http://" + Constants.IP + "/studystay/photo/getPhotos.php";
    private static final String URL_CREATE_PHOTO = "http://" + Constants.IP + "/studystay/photo/createPhoto.php";
    private static final String URL_DELETE_PHOTO = "http://" + Constants.IP + "/studystay/photo/deletePhoto.php";
    private static final String URL_GET_PHOTO_BY_ID = "http://" + Constants.IP + "/studystay/photo/getPhotoById.php";
    private static final String URL_UPDATE_PHOTO = "http://" + Constants.IP + "/studystay/photo/updatePhoto.php";

    private RequestQueue requestQueue;
    private Context context;

    public AccommodationPhotoController(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public void getPhotos(final PhotoListCallback callback) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL_GET_PHOTOS, null,
                response -> {
                    try {
                        List<AccommodationPhoto> photos = new ArrayList<>();
                        AccommodationController accommodationController = new AccommodationController(context);
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject photoObject = response.getJSONObject(i);
                            Long photoId = photoObject.getLong("PhotoId");
                            Long accommodationId = photoObject.getLong("AccommodationId");
                            byte[] photoData = photoObject.getString("PhotoData").getBytes();

                            accommodationController.getAccommodationById(accommodationId, new AccommodationController.AccommodationCallback() {
                                @Override
                                public void onSuccess(Object result) {
                                    Accommodation accommodation = (Accommodation) result;
                                    AccommodationPhoto photo = new AccommodationPhoto(accommodation, photoData);
                                    photo.setPhotoId(photoId);
                                    photos.add(photo);
                                    if (photos.size() == response.length()) {
                                        callback.onSuccess(photos);
                                    }
                                }

                                @Override
                                public void onSuccess(Accommodation accommodation) {
                                    AccommodationPhoto photo = new AccommodationPhoto(accommodation, photoData);
                                    photo.setPhotoId(photoId);
                                    photos.add(photo);
                                    if (photos.size() == response.length()) {
                                        callback.onSuccess(photos);
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

    public void getPhotoById(Long photoId, final PhotoCallback callback) {
        String url = URL_GET_PHOTO_BY_ID + "?photoId=" + photoId;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            JSONObject photoObject = response.getJSONObject(0);
                            Long id = photoObject.getLong("PhotoId");
                            Long accommodationId = photoObject.getLong("AccommodationId");
                            byte[] photoData = photoObject.getString("PhotoData").getBytes();

                            AccommodationController accommodationController = new AccommodationController(context);
                            accommodationController.getAccommodationById(accommodationId, new AccommodationController.AccommodationCallback() {
                                @Override
                                public void onSuccess(Object result) {
                                    Accommodation accommodation = (Accommodation) result;
                                    AccommodationPhoto photo = new AccommodationPhoto(accommodation, photoData);
                                    photo.setPhotoId(id);
                                    callback.onSuccess(photo);
                                }

                                @Override
                                public void onSuccess(Accommodation accommodation) {
                                    AccommodationPhoto photo = new AccommodationPhoto(accommodation, photoData);
                                    photo.setPhotoId(id);
                                    callback.onSuccess(photo);
                                }
                                @Override
                                public void onError(String error) {
                                    callback.onError(error);
                                }
                            });
                        } else {
                            callback.onError("Photo not found");
                        }
                    } catch (JSONException e) {
                        callback.onError(e.getMessage());
                    }
                },
                error -> callback.onError(error.toString())
        );
        requestQueue.add(jsonArrayRequest);
    }

    public void updatePhoto(AccommodationPhoto photo, final PhotoCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPDATE_PHOTO,
                response -> {
                    if ("Photo updated successfully".equals(response)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response);
                    }
                },
                error -> callback.onError(error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("photoId", photo.getPhotoId().toString());
                params.put("accommodationId", photo.getAccommodation().getAccommodationId().toString());
                params.put("photoData", new String(photo.getPhotoData()));
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    public void createPhoto(AccommodationPhoto photo, final PhotoCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_CREATE_PHOTO,
                response -> {
                    if ("Photo created successfully".equals(response)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response);
                    }
                },
                error -> callback.onError(error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("accommodationId", photo.getAccommodation().getAccommodationId().toString());
                params.put("photoData", new String(photo.getPhotoData()));
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public void deletePhoto(Long photoId, final PhotoCallback callback) {
        String url = URL_DELETE_PHOTO + "?photoId=" + photoId;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    if ("Photo deleted successfully".equals(response)) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response);
                    }
                },
                error -> callback.onError(error.toString())
        );
        requestQueue.add(stringRequest);
    }

    public interface PhotoCallback {
        void onSuccess(Object result);
        void onError(String error);
    }

    public interface PhotoListCallback {
        void onSuccess(List<AccommodationPhoto> photos);
        void onError(String error);
    }
}
