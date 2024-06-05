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
import com.example.studystayandroid.controller.AccommodationController;
import com.example.studystayandroid.model.Accommodation;

import java.util.ArrayList;
import java.util.List;

public class ListedFragment extends Fragment {

    private RecyclerView recyclerView;
    private SimpleAccommodationAdapter adapter;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewListed);
        emptyView = view.findViewById(R.id.emptyViewListed);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SimpleAccommodationAdapter();
        recyclerView.setAdapter(adapter);

        getListedAccommodations();
    }

    private void getListedAccommodations() {
        AccommodationController accommodationController = new AccommodationController(getContext());
        accommodationController.getAccommodations(new AccommodationController.AccommodationListCallback() {
            @Override
            public void onSuccess(List<Accommodation> accommodations) {
                List<Accommodation> listedAccommodations = new ArrayList<>();

                // Obtener el usuario actual de SharedPreferences
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", getContext().MODE_PRIVATE);
                Long currentUserId = sharedPreferences.getLong("userId", -1);
                if (currentUserId == -1) {
                    Log.e("ListedFragment", "Error: Usuario no autenticado.");
                    return;
                }

                for (Accommodation accommodation : accommodations) {
                    if (accommodation.getOwner().getUserId().equals(currentUserId)) {
                        listedAccommodations.add(accommodation);
                    }
                }

                adapter.setAccommodations(listedAccommodations);
                updateUI(listedAccommodations);
            }

            @Override
            public void onError(String error) {
                Log.e("ListedFragment", "Error fetching accommodations: " + error);
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
