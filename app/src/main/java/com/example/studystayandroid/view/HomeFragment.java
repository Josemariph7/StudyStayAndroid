package com.example.studystayandroid.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.AccommodationController;
import com.example.studystayandroid.controller.UserController;
import com.example.studystayandroid.model.Accommodation;
import com.example.studystayandroid.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private AccommodationAdapter adapter;
    private List<Accommodation> accommodationList;
    private AccommodationController accommodationController;
    private UserController userController;

    private static final String URL_ACCOMMODATIONS = "http://" + Constants.IP + "/studystay/getAccommodations.php";

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getActivity() != null) {
            getActivity().setTitle("StudyStay - Accommodations");
        }
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        accommodationController = new AccommodationController(requireContext());
        userController = new UserController(requireContext());

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        accommodationList = new ArrayList<>();
        adapter = new AccommodationAdapter(accommodationList);
        recyclerView.setAdapter(adapter);

        fetchAccommodations();
    }

    private void fetchAccommodations() {
        accommodationController.getAccommodations(new AccommodationController.AccommodationListCallback() {
            @Override
            public void onSuccess(List<Accommodation> accommodations) {
                accommodationList.clear();
                accommodationList.addAll(accommodations);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Log.e("HomeFragment", "Error fetching accommodations: " + error);
            }
        });
    }
}