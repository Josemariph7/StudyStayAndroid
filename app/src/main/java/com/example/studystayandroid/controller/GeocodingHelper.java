package com.example.studystayandroid.controller;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Clase auxiliar para realizar operaciones de geocodificación.
 */
public class GeocodingHelper {
    private Context context;

    /**
     * Constructor para inicializar el GeocodingHelper.
     *
     * @param context el contexto de la aplicación
     */
    public GeocodingHelper(Context context) {
        this.context = context;
    }

    /**
     * Obtiene las coordenadas LatLng a partir de una ciudad y una dirección.
     *
     * @param city    la ciudad de la dirección
     * @param address la dirección
     * @return un objeto LatLng con las coordenadas de la dirección, o null si no se pudo obtener
     */
    public LatLng getLatLngFromAddress(String city, String address) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(city + " " + address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException e) {
            Log.e("GeocodingHelper", "Geocoding failed", e);
        }
        return null;
    }
}
