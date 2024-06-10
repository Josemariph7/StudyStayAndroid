package com.example.studystayandroid.controller;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.studystayandroid.model.Accommodation;
import com.example.studystayandroid.model.AccommodationPhoto;
import com.example.studystayandroid.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar las operaciones relacionadas con las fotos de alojamiento.
 */
public class AccommodationPhotoController {

    private static final String URL_GET_PHOTOS = "http://" + Constants.IP + "/studystay/photo/getPhotos.php";
    private static final String URL_CREATE_PHOTO = "http://" + Constants.IP + "/studystay/photo/createPhoto.php";
    private static final String URL_DELETE_PHOTO = "http://" + Constants.IP + "/studystay/photo/deletePhoto.php";
    private static final String URL_GET_PHOTO_BY_ID = "http://" + Constants.IP + "/studystay/photo/getPhotoById.php";
    private static final String URL_UPDATE_PHOTO = "http://" + Constants.IP + "/studystay/photo/updatePhoto.php";

    private RequestQueue requestQueue;
    private Context context;

    /**
     * Constructor para inicializar el controlador de fotos de alojamiento.
     *
     * @param context el contexto de la aplicación
     */
    public AccommodationPhotoController(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    /**
     * Obtiene la lista de fotos desde el servidor.
     *
     * @param callback el callback para manejar la respuesta
     */
    public void getPhotos(final PhotoListCallback callback) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL_GET_PHOTOS, null,
                response -> {
                    try {
                        List<AccommodationPhoto> photos = new ArrayList<>();
                        Log.d("AccommodationPhotoController", "Response: " + response.toString());
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject photoObject = response.getJSONObject(i);
                            Long photoId = photoObject.getLong("PhotoId");
                            Long accommodationId = photoObject.getLong("AccommodationId");
                            byte[] photoData = photoObject.getString("PhotoData").getBytes();

                            AccommodationController accommodationController = new AccommodationController(context);
                            accommodationController.getAccommodationById(accommodationId, new AccommodationController.AccommodationCallback() {
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
                                    Log.e("AccommodationPhotoController", "Error getting accommodation by ID: " + error);
                                    callback.onError(error);
                                }

                                @Override
                                public void onSuccess(Object result) {
                                    // Not used
                                }
                            });
                        }
                    } catch (JSONException e) {
                        Log.e("AccommodationPhotoController", "JSON error: " + e.getMessage());
                        callback.onError(e.getMessage());
                    }
                },
                error -> {
                    Log.e("AccommodationPhotoController", "Volley error: " + error.toString());
                    callback.onError(error.toString());
                }
        );

        requestQueue.add(jsonArrayRequest);
    }




    /**
     * Obtiene una foto específica por su ID.
     *
     * @param photoId el ID de la foto
     * @param callback el callback para manejar la respuesta
     */
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

    /**
     * Actualiza una foto existente.
     *
     * @param photo la foto a ser actualizada
     * @param callback el callback para manejar la respuesta
     */
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

    /**
     * Crea una nueva foto.
     *
     * @param photo la foto a ser creada
     * @param callback el callback para manejar la respuesta
     */
    public void createPhoto(AccommodationPhoto photo, final PhotoCallback callback) {
        if (photo.getAccommodation() == null || photo.getAccommodation().getAccommodationId() == null) {
            callback.onError("Accommodation ID is null");
            return;
        }

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, URL_CREATE_PHOTO,
                response -> {
                    try {
                        String result = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                        if ("Photo created successfully".equals(result)) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(result);
                        }
                    } catch (UnsupportedEncodingException e) {
                        callback.onError(e.getMessage());
                    }
                },
                error -> callback.onError(error.toString())) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("accommodationId", photo.getAccommodation().getAccommodationId().toString());
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("photoData", new DataPart("image.jpg", photo.getPhotoData(), "image/jpeg"));
                return params;
            }
        };

        requestQueue.add(multipartRequest);
    }




    /**
     * Elimina una foto por su ID.
     *
     * @param photoId el ID de la foto a ser eliminada
     * @param callback el callback para manejar la respuesta
     */
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

    /**
     * Interfaz para manejar una sola foto.
     */
    public interface PhotoCallback {
        void onSuccess(Object result);
        void onError(String error);
    }

    /**
     * Interfaz para manejar la lista de fotos.
     */
    public interface PhotoListCallback {
        void onSuccess(List<AccommodationPhoto> photos);
        void onError(String error);
    }
}
