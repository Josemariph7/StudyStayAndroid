package com.example.studystayandroid.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studystayandroid.R;
import com.example.studystayandroid.model.Accommodation;

import java.util.ArrayList;
import java.util.List;

public class RentedFragment extends Fragment {

    private RecyclerView recyclerView;
    private AccommodationAdapter adapter; // Assume you have an adapter for your RecyclerView

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rented, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewRented);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Initialize and set your adapter
        adapter = new AccommodationAdapter(getRentedAccommodations());
        recyclerView.setAdapter(adapter);
    }

    private List<Accommodation> getRentedAccommodations() {
        // Implement this method to return the list of rented accommodations
        return new ArrayList<>();
    }
}
