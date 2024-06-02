package com.example.studystayandroid.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.AccommodationController;
import com.example.studystayandroid.controller.UserController;
import com.example.studystayandroid.model.Accommodation;
import com.example.studystayandroid.model.User;
import com.example.studystayandroid.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class AccommodationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AccommodationAdapter adapter;
    private List<Accommodation> accommodationList;
    private AccommodationController accommodationController;
    private UserController userController;
    private User currentUser;

    private Spinner filterCitySpinner;
    private Spinner filterCapacitySpinner;
    private CheckBox filterAvailability;
    private Button filterButton;
    private Button mapButton;
    private Button addAccommodationButton;
    private Button applyFiltersButton;
    private LinearLayout filtersContainer;

    private static final String URL_ACCOMMODATIONS = "http://" + Constants.IP + "/studystay/getAccommodations.php";

    public AccommodationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getActivity() != null) {
            getActivity().setTitle("StudyStay - Accommodations");
        }
        return inflater.inflate(R.layout.fragment_accommodations, container, false);
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

        filterCitySpinner = view.findViewById(R.id.filter_city_spinner);
        filterCapacitySpinner = view.findViewById(R.id.filter_capacity_spinner);
        filterAvailability = view.findViewById(R.id.filter_availability);
        filterButton = view.findViewById(R.id.filter_button);
        mapButton = view.findViewById(R.id.map_button);
        addAccommodationButton = view.findViewById(R.id.add_accommodation_button);
        applyFiltersButton = view.findViewById(R.id.apply_filters_button);
        filtersContainer = view.findViewById(R.id.filters_container);

        filterButton.setOnClickListener(v -> toggleFiltersVisibility());
        mapButton.setOnClickListener(v -> openMap());
        addAccommodationButton.setOnClickListener(v -> openAddAccommodationFragment());
        applyFiltersButton.setOnClickListener(v -> applyFilters());

        setupSpinners();
        fetchAccommodations();

        // Configurar OnItemLongClickListener
        adapter.setOnItemLongClickListener(this::showAccommodationOptionsDialog);

        // Retrieve currentUser from arguments
        if (getArguments() != null) {
            currentUser = (User) getArguments().getSerializable("currentUser");
        }
    }



    private void toggleFiltersVisibility() {
        if (filtersContainer.getVisibility() == View.VISIBLE) {
            filtersContainer.setVisibility(View.GONE);
        } else {
            filtersContainer.setVisibility(View.VISIBLE);
        }
    }

    private void setupSpinners() {
        // Set up city spinner
        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.cities_array, // Assuming you have a string array resource named "cities_array"
                android.R.layout.simple_spinner_item);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterCitySpinner.setAdapter(cityAdapter);

        // Set up capacity spinner
        ArrayAdapter<CharSequence> capacityAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.capacity_array, // Assuming you have a string array resource named "capacity_array"
                android.R.layout.simple_spinner_item);
        capacityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterCapacitySpinner.setAdapter(capacityAdapter);
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
                Log.e("AccommodationsFragment", "Error fetching accommodations: " + error);
            }
        });
    }

    private void applyFilters() {
        String selectedCity = filterCitySpinner.getSelectedItem().toString();
        String selectedCapacityStr = filterCapacitySpinner.getSelectedItem().toString();
        boolean isAvailable = filterAvailability.isChecked();

        Integer selectedCapacity = null;
        if (!selectedCapacityStr.equals("All")) { // Assuming "All" is an option in your spinner
            try {
                selectedCapacity = Integer.parseInt(selectedCapacityStr);
            } catch (NumberFormatException e) {
                selectedCapacity = null; // If parsing fails, treat as if "All" was selected
            }
        }

        // Apply filters to fetch and display filtered accommodations
        Integer finalSelectedCapacity = selectedCapacity;
        accommodationController.getAccommodations(new AccommodationController.AccommodationListCallback() {
            @Override
            public void onSuccess(List<Accommodation> accommodations) {
                List<Accommodation> filteredList = new ArrayList<>();
                for (Accommodation accommodation : accommodations) {

                    boolean matchesCity = selectedCity.equals("All") || accommodation.getCity().equals(selectedCity);
                    boolean matchesCapacity = finalSelectedCapacity == null || accommodation.getCapacity() == finalSelectedCapacity;
                    boolean matchesAvailability = accommodation.isAvailability() == isAvailable;

                    if (matchesCity && matchesCapacity && matchesAvailability) {
                        filteredList.add(accommodation);
                    }
                }
                accommodationList.clear();
                accommodationList.addAll(filteredList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Log.e("AccommodationsFragment", "Error applying filters: " + error);
            }
        });
    }


    private void openMap() {
        MapDialogFragment mapDialogFragment = new MapDialogFragment(accommodationList);
        mapDialogFragment.show(getParentFragmentManager(), "MapDialogFragment");
    }

    private void openAddAccommodationFragment() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AddAccommodationFragment())
                .addToBackStack(null)
                .commit();
    }

    // Añadir este método a tu AccommodationsFragment
    private void showAccommodationOptionsDialog(Accommodation accommodation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose an option")
                .setItems(new String[]{"View Owner Profile", "View on Map"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // View Owner Profile
                                openOwnerProfile(accommodation.getOwner());
                                break;
                            case 1:
                                // View on Map
                                showAccommodationOnMap(accommodation);
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    private void openOwnerProfile(User owner) {
        StrangeProfileFragment fragment = StrangeProfileFragment.newInstance(owner);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showAccommodationOnMap(Accommodation accommodation) {
        List<Accommodation> singleAccommodationList = new ArrayList<>();
        singleAccommodationList.add(accommodation);
        MapDialogFragment mapDialogFragment = new MapDialogFragment(singleAccommodationList);
        mapDialogFragment.show(getParentFragmentManager(), "MapDialogFragment");
    }
}