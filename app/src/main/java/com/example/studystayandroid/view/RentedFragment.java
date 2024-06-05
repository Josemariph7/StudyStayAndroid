package com.example.studystayandroid.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.BookingController;
import com.example.studystayandroid.controller.AccommodationController;
import com.example.studystayandroid.model.Accommodation;
import com.example.studystayandroid.model.Booking;

import java.util.ArrayList;
import java.util.List;

public class RentedFragment extends Fragment {

    private RecyclerView recyclerView;
    private SimpleAccommodationAdapter adapter;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rented, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewRented);
        emptyView = view.findViewById(R.id.emptyViewRented);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SimpleAccommodationAdapter();
        recyclerView.setAdapter(adapter);

        getRentedAccommodations();
    }

    private void getRentedAccommodations() {
        BookingController bookingController = new BookingController(getContext());
        bookingController.getBookings(new BookingController.BookingListCallback() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                AccommodationController accommodationController = new AccommodationController(getContext());
                List<Accommodation> rentedAccommodations = new ArrayList<>();

                // Obtener el usuario actual de SharedPreferences
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", getContext().MODE_PRIVATE);
                Long currentUserId = sharedPreferences.getLong("userId", -1);
                if (currentUserId == -1) {
                    Log.e("RentedFragment", "Error: Usuario no autenticado.");
                    return;
                }

                for (Booking booking : bookings) {
                    if (booking.getUser().getUserId().equals(currentUserId)) {
                        accommodationController.getAccommodationById(booking.getAccommodation().getAccommodationId(), new AccommodationController.AccommodationCallback() {
                            @Override
                            public void onSuccess(Accommodation accommodation) {
                                rentedAccommodations.add(accommodation);
                                adapter.setAccommodations(rentedAccommodations);
                                updateUI(rentedAccommodations);
                            }

                            @Override
                            public void onError(String error) {
                                Log.e("RentedFragment", "Error fetching accommodation: " + error);
                            }

                            @Override
                            public void onSuccess(Object result) {
                                Accommodation accommodation = (Accommodation) result;
                                rentedAccommodations.add(accommodation);
                                adapter.setAccommodations(rentedAccommodations);
                                updateUI(rentedAccommodations);
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.e("RentedFragment", "Error fetching bookings: " + error);
            }
        });
    }

    private void updateUI(List<Accommodation> accommodations) {
        if (accommodations.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}
