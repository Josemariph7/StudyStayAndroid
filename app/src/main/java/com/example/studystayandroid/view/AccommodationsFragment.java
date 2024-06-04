package com.example.studystayandroid.view;

import android.annotation.SuppressLint;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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

        Button backButton = dialogView.findViewById(R.id.back_button);
        ViewPager2 imageCarousel = dialogView.findViewById(R.id.imageCarousel);
        TextView addressTextView = dialogView.findViewById(R.id.address);
        TextView descriptionTextView = dialogView.findViewById(R.id.description);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView rating=dialogView.findViewById(R.id.rating);
        TextView availabilityTextView = dialogView.findViewById(R.id.availability);
        TextView priceTextView = dialogView.findViewById(R.id.price);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView ownerTextView = dialogView.findViewById(R.id.ownerName);
        Button bookButton = dialogView.findViewById(R.id.bookButton);
        Button contactButton = dialogView.findViewById(R.id.contactButton);

        List<byte[]> photos = new ArrayList<>();
        for (AccommodationPhoto photo : accommodation.getPhotos()) {
            photos.add(photo.getPhotoData());
        }
        ImageCarouselAdapter adapter = new ImageCarouselAdapter(photos, getContext());
        imageCarousel.setAdapter(adapter);
        rating.setText(String.valueOf(accommodation.getRating()));
        availabilityTextView.setText(accommodation.isAvailability() ? "Available" : "Not Available");
        ownerTextView.setText("Owned by "+accommodation.getOwner().getName()+" "+accommodation.getOwner().getLastName());
        addressTextView.setText(accommodation.getAddress()+", "+accommodation.getCity());
        descriptionTextView.setText(accommodation.getDescription());
        priceTextView.setText(String.format("€%.2f", accommodation.getPrice()));

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        backButton.setOnClickListener(v -> dialog.dismiss());

        bookButton.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new BookingFragment())
                    .addToBackStack(null)
                    .commit();
            dialog.dismiss();
        });

        contactButton.setOnClickListener(v -> {
            showContactOptions(accommodation.getOwner(), dialog);
        });

        dialog.show();
    }

    private void showAccommodationOptionsDialog(Accommodation accommodation) {
        if (accommodation.getOwner().getUserId().equals(currentUser.getUserId())) {
            showDeleteConfirmationDialog(accommodation);
        } else {
            showOptionsDialog(accommodation);
        }
    }

    private void showOptionsDialog(Accommodation accommodation) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_accommodation_options, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        Button viewProfileButton = dialogView.findViewById(R.id.viewProfileButton);
        Button viewMapButton = dialogView.findViewById(R.id.viewMapButton);

        dialogTitle.setText("Choose an option");

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        viewProfileButton.setOnClickListener(v -> {
            openOwnerProfile(accommodation.getOwner());
            dialog.dismiss();
        });

        viewMapButton.setOnClickListener(v -> {
            showAccommodationOnMap(accommodation);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDeleteConfirmationDialog(Accommodation accommodation) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_delete_confirmation, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);

        dialogTitle.setText("Confirm Delete");
        dialogMessage.setText("Are you sure you want to delete this accommodation?");

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonConfirm.setOnClickListener(v -> {
            deleteAccommodation(accommodation);
            dialog.dismiss();
        });

        dialog.show();
    }


    private void deleteAccommodation(Accommodation accommodation) {
        accommodationController.deleteAccommodation(accommodation.getAccommodationId(), new AccommodationController.AccommodationCallback() {
            @Override
            public void onSuccess(Object result) {
                fetchAccommodations(); // Refresh the list after deletion
            }

            @Override
            public void onSuccess(Accommodation accommodation) {
                fetchAccommodations();
            }

            @Override
            public void onError(String error) {
                Log.e("AccommodationsFragment", "Error deleting accommodation: " + error);
            }
        });
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

    private void showContactOptions(User owner, androidx.appcompat.app.AlertDialog parentDialog) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_contact_options, null);

        Button callButton = dialogView.findViewById(R.id.callButton);
        Button chatButton = dialogView.findViewById(R.id.chatButton);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        callButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + owner.getPhone()));
            startActivity(intent);
            dialog.dismiss();
            parentDialog.dismiss(); // Close the parent dialog as well
        });

        chatButton.setOnClickListener(v -> {
            createConversationAndOpenMessageFragment(owner, parentDialog);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void createConversationAndOpenMessageFragment(User owner, androidx.appcompat.app.AlertDialog parentDialog) {
        ConversationController conversationController = new ConversationController(getContext());
        conversationController.getConversations(currentUser.getUserId(), new ConversationController.ConversationListCallback() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                for (Conversation conversation : conversations) {
                    if ((conversation.getUser1Id().equals(currentUser.getUserId()) && conversation.getUser2Id().equals(owner.getUserId())) ||
                            (conversation.getUser1Id().equals(owner.getUserId()) && conversation.getUser2Id().equals(currentUser.getUserId()))) {
                        openMessageFragment(conversation);
                        parentDialog.dismiss(); // Close the parent dialog
                        return;
                    }
                }
                createNewConversation(owner, parentDialog);
            }

            @Override
            public void onError(String error) {
                Log.e("AccommodationsFragment", "Error fetching conversations: " + error);
                createNewConversation(owner, parentDialog);
            }
        });
    }

    private void createNewConversation(User owner, androidx.appcompat.app.AlertDialog parentDialog) {
        Conversation newConversation = new Conversation(null, currentUser.getUserId(), owner.getUserId(), new ArrayList<>());
        ConversationController conversationController = new ConversationController(getContext());
        conversationController.createConversation(newConversation, new ConversationController.ConversationCallback() {
            @Override
            public void onSuccess(Conversation createdConversation) {
                openMessageFragment(createdConversation);
                parentDialog.dismiss(); // Close the parent dialog
            }

            @Override
            public void onSuccess(Object result) {
                // Handle any additional logic if needed
            }

            @Override
            public void onError(String error) {
                Log.e("AccommodationsFragment", "Error creating conversation: " + error);
            }
        });
    }

    private void openMessageFragment(Conversation conversation) {
        MessageFragment messageFragment = MessageFragment.newInstance(conversation);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, messageFragment)
                .addToBackStack(null)
                .commit();
    }
}
