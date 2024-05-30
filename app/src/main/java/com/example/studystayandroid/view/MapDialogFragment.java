package com.example.studystayandroid.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.studystayandroid.controller.GeocodingHelper;
import com.example.studystayandroid.R;
import com.example.studystayandroid.model.Accommodation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapDialogFragment extends DialogFragment implements OnMapReadyCallback {

    private MapView mapView;
    private List<Accommodation> accommodations;
    private GeocodingHelper geocodingHelper;

    public MapDialogFragment(List<Accommodation> accommodations) {
        this.accommodations = accommodations;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_map, container, false);
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        geocodingHelper = new GeocodingHelper(getContext());
        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (Accommodation accommodation : accommodations) {
            LatLng latLng = geocodingHelper.getLatLngFromAddress(accommodation.getCity(), accommodation.getAddress());
            if (latLng != null) {
                googleMap.addMarker(new MarkerOptions().position(latLng).title(accommodation.getDescription()));
                builder.include(latLng);
            }
        }

        // Ajustar la cámara para mostrar todos los marcadores
        googleMap.setOnMapLoadedCallback(() -> {
            LatLngBounds bounds = builder.build();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
