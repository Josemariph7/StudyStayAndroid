package com.example.studystayandroid.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.AccommodationController;
import com.example.studystayandroid.controller.ConversationController;
import com.example.studystayandroid.controller.UserController;
import com.example.studystayandroid.model.Accommodation;
import com.example.studystayandroid.model.AccommodationPhoto;
import com.example.studystayandroid.model.Conversation;
import com.example.studystayandroid.model.User;
import com.example.studystayandroid.utils.Constants;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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

        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.cities_array,
                R.layout.spinner_item);

        cityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        filterCitySpinner.setAdapter(cityAdapter);

        ArrayAdapter<CharSequence> capacityAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.capacity_array,
                R.layout.spinner_item);

        capacityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        filterCapacitySpinner.setAdapter(capacityAdapter);

        adapter.setOnItemClickListener(this::showAccommodationDetailDialog);
        adapter.setOnItemLongClickListener(this::showAccommodationOptionsDialog);

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
        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.cities_array,
                android.R.layout.simple_spinner_item);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterCitySpinner.setAdapter(cityAdapter);

        ArrayAdapter<CharSequence> capacityAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.capacity_array,
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
        if (!selectedCapacityStr.equals("All")) {
            try {
                selectedCapacity = Integer.parseInt(selectedCapacityStr);
            } catch (NumberFormatException e) {
                selectedCapacity = null;
            }
        }

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

    private void showAccommodationDetailDialog(Accommodation accommodation) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_accommodation_detail, null);

        ImageButton backButton = dialogView.findViewById(R.id.backButton);
        ViewPager imageCarousel = dialogView.findViewById(R.id.imageCarousel);
        TextView addressTextView = dialogView.findViewById(R.id.address);
        TextView cityTextView = dialogView.findViewById(R.id.city);
        TextView descriptionTextView = dialogView.findViewById(R.id.description);
        TextView priceTextView = dialogView.findViewById(R.id.price);
        Button bookButton = dialogView.findViewById(R.id.bookButton);
        Button contactButton = dialogView.findViewById(R.id.contactButton);

        List<byte[]> photos = new ArrayList<>();
        for (AccommodationPhoto photo : accommodation.getPhotos()) {
            photos.add(photo.getPhotoData());
        }
        ImageCarouselAdapter adapter = new ImageCarouselAdapter(photos, getContext());
        imageCarousel.setAdapter(adapter);

        addressTextView.setText(accommodation.getAddress());
        cityTextView.setText(accommodation.getCity());
        descriptionTextView.setText(accommodation.getDescription());
        priceTextView.setText(String.format("$%.2f", accommodation.getPrice()));

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        backButton.setOnClickListener(v -> dialog.dismiss());

        bookButton.setOnClickListener(v -> {
            // Open the booking fragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new BookingFragment())
                    .addToBackStack(null)
                    .commit();
            dialog.dismiss();
        });

        contactButton.setOnClickListener(v -> showContactOptions(accommodation.getOwner()));

        dialog.show();
    }

    private void showAccommodationOptionsDialog(Accommodation accommodation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose an option")
                .setItems(new String[]{"View Owner Profile", "View on Map"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            openOwnerProfile(accommodation.getOwner());
                            break;
                        case 1:
                            showAccommodationOnMap(accommodation);
                            break;
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

    private void showContactOptions(User owner) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_contact_options, null);

        Button callButton = dialogView.findViewById(R.id.callButton);
        Button chatButton = dialogView.findViewById(R.id.chatButton);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        callButton.setOnClickListener(v -> {
            // Code to initiate a call
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + owner.getPhone()));
            startActivity(intent);
            dialog.dismiss();
        });

        chatButton.setOnClickListener(v -> {
            createConversationAndOpenMessageFragment(owner);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void createConversationAndOpenMessageFragment(User owner) {
        ConversationController conversationController = new ConversationController(getContext());
        conversationController.getConversations(currentUser.getUserId(), new ConversationController.ConversationListCallback() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                for (Conversation conversation : conversations) {
                    if ((conversation.getUser1Id().equals(currentUser.getUserId()) && conversation.getUser2Id().equals(owner.getUserId())) ||
                            (conversation.getUser1Id().equals(owner.getUserId()) && conversation.getUser2Id().equals(currentUser.getUserId()))) {
                        navigateToMessageFragment(conversation);
                        return;
                    }
                }
                // If no existing conversation, create a new one
                createNewConversation(owner);
            }

            @Override
            public void onError(String error) {
                Log.e("AccommodationsFragment", "Error fetching conversations: " + error);
                createNewConversation(owner);
            }
        });
    }

    private void createNewConversation(User owner) {
        Conversation newConversation = new Conversation();
        newConversation.setUser1Id(currentUser.getUserId());
        newConversation.setUser2Id(owner.getUserId());
        newConversation.setMessages(new ArrayList<>());

        ConversationController conversationController = new ConversationController(requireContext());
        conversationController.createConversation(newConversation, new ConversationController.ConversationCallback() {
            @Override
            public void onSuccess(Conversation createdConversation) {
                navigateToMessageFragment(createdConversation);
            }

            @Override
            public void onSuccess(Object result) {
                // Handle the generic case if needed
            }

            @Override
            public void onError(String error) {
                Log.e("AccommodationsFragment", "Error creating conversation: " + error);
            }
        });
    }

    private void navigateToMessageFragment(Conversation conversation) {
        MessageFragment messageFragment = MessageFragment.newInstance(conversation);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, messageFragment)
                .addToBackStack(null)
                .commit();
    }
}
