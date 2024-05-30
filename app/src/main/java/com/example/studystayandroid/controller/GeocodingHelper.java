package com.example.studystayandroid.controller;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeocodingHelper {
    private Context context;

    public GeocodingHelper(Context context) {
        this.context = context;
    }

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
