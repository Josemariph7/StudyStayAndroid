/*
 * StudyStay © 2024
 *
 * All rights reserved.
 *
 * This software and associated documentation files (the "Software") are owned by StudyStay. Unauthorized copying, distribution, or modification of this Software is strictly prohibited.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this Software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * StudyStay
 * José María Pozo Hidalgo
 * Email: josemariph7@gmail.com
 *
 *
 */

package com.example.studystayandroid.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.AccommodationController;
import com.example.studystayandroid.controller.BookingController;
import com.example.studystayandroid.controller.ConversationController;
import com.example.studystayandroid.model.Accommodation;
import com.example.studystayandroid.model.AccommodationPhoto;
import com.example.studystayandroid.model.Booking;
import com.example.studystayandroid.model.Conversation;
import com.example.studystayandroid.model.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to display a list of accommodations rented by the current user.
 */
public class RentedFragment extends Fragment {

    private RecyclerView recyclerView;
    private SimpleAccommodationAdapter adapter;
    private TextView emptyView;
    private User currentUser;

    /**
     * Creates a new instance of RentedFragment with the specified user.
     *
     * @param user The current user.
     * @return A new instance of RentedFragment.
     */
    public static RentedFragment newInstance(User user) {
        RentedFragment fragment = new RentedFragment();
        Bundle args = new Bundle();
        args.putSerializable("currentUser", user);
        fragment.setArguments(args);
        return fragment;
    }

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
        adapter.setOnItemClickListener(this::showAccommodationDetailDialog);

        getRentedAccommodations();
    }

    /**
     * Retrieves the list of accommodations rented by the current user.
     */
    private void getRentedAccommodations() {
        if (getArguments() != null) {
            currentUser = (User) getArguments().getSerializable("currentUser");
        }

        BookingController bookingController = new BookingController(getContext());
        bookingController.getBookings(new BookingController.BookingListCallback() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                AccommodationController accommodationController = new AccommodationController(getContext());
                List<Accommodation> rentedAccommodations = new ArrayList<>();

                for (Booking booking : bookings) {
                    if (booking.getUser().getUserId().equals(currentUser.getUserId())) {
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

    /**
     * Updates the UI based on the list of rented accommodations.
     *
     * @param accommodations The list of rented accommodations.
     */
    private void updateUI(List<Accommodation> accommodations) {
        if (accommodations.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    /**
     * Shows a detailed dialog for the selected accommodation.
     *
     * @param accommodation The selected accommodation.
     */
    private void showAccommodationDetailDialog(Accommodation accommodation) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_rented_accommodation_detail, null);

        Button backButton = dialogView.findViewById(R.id.back_button);
        ViewPager2 imageCarousel = dialogView.findViewById(R.id.imageCarousel);
        TextView addressTextView = dialogView.findViewById(R.id.address);
        TextView descriptionTextView = dialogView.findViewById(R.id.description);
        TextView rating = dialogView.findViewById(R.id.rating);
        TextView availabilityTextView = dialogView.findViewById(R.id.availability);
        TextView priceTextView = dialogView.findViewById(R.id.price);
        TextView ownerTextView = dialogView.findViewById(R.id.ownerName);
        Button contactButton = dialogView.findViewById(R.id.contactButton);
        Button mapButton = dialogView.findViewById(R.id.map_button2);
        Button reviewButton = dialogView.findViewById(R.id.reviewButton);

        List<byte[]> photos = new ArrayList<>();
        for (AccommodationPhoto photo : accommodation.getPhotos()) {
            photos.add(photo.getPhotoData());
        }
        ImageCarouselAdapter adapter = new ImageCarouselAdapter(photos, getContext());
        imageCarousel.setAdapter(adapter);

        rating.setText(String.valueOf(accommodation.getRating()));
        availabilityTextView.setText(accommodation.isAvailability() ? "Available" : "Not Available");
        ownerTextView.setText("Owned by " + accommodation.getOwner().getName() + " " + accommodation.getOwner().getLastName());
        addressTextView.setText(accommodation.getAddress() + ", " + accommodation.getCity());
        descriptionTextView.setText(accommodation.getDescription());
        priceTextView.setText(String.format("€%.2f", accommodation.getPrice()));

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        backButton.setOnClickListener(v -> dialog.dismiss());

        mapButton.setOnClickListener(v -> showAccommodationOnMap(accommodation));

        reviewButton.setOnClickListener(v -> {
            ReviewFragment reviewFragment = new ReviewFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("accommodation", accommodation);
            bundle.putSerializable("currentUser", currentUser);
            reviewFragment.setArguments(bundle);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, reviewFragment)
                    .addToBackStack(null)
                    .commit();
            dialog.dismiss();
        });

        contactButton.setOnClickListener(v -> showContactOptions(accommodation.getOwner(), dialog));

        dialog.show();
    }

    /**
     * Shows a dialog with contact options for the accommodation owner.
     *
     * @param owner The owner of the accommodation.
     * @param parentDialog The parent dialog to be dismissed after showing the contact options.
     */
    private void showContactOptions(User owner, AlertDialog parentDialog) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_contact_options, null);

        Button callButton = dialogView.findViewById(R.id.callButton);
        Button chatButton = dialogView.findViewById(R.id.detailedAccommodationButton);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
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

    /**
     * Creates a conversation and opens the message fragment.
     *
     * @param owner The owner of the accommodation.
     * @param parentDialog The parent dialog to be dismissed after creating the conversation.
     */
    private void createConversationAndOpenMessageFragment(User owner, AlertDialog parentDialog) {
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
                Log.e("RentedFragment", "Error fetching conversations: " + error);
                createNewConversation(owner, parentDialog);
            }
        });
    }

    /**
     * Creates a new conversation between the current user and the owner.
     *
     * @param owner The owner of the accommodation.
     * @param parentDialog The parent dialog to be dismissed after creating the conversation.
     */
    private void createNewConversation(User owner, AlertDialog parentDialog) {
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
                // Handle any optional logic if needed
            }

            @Override
            public void onError(String error) {
                Log.e("RentedFragment", "Error creating conversation: " + error);
            }
        });
    }

    /**
     * Opens the MessageFragment for the given conversation.
     *
     * @param conversation The conversation to open.
     */
    private void openMessageFragment(Conversation conversation) {
        MessageFragment messageFragment = MessageFragment.newInstance(conversation);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, messageFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Shows the accommodation on a map.
     *
     * @param accommodation The accommodation to show on the map.
     */
    private void showAccommodationOnMap(Accommodation accommodation) {
        List<Accommodation> singleAccommodationList = new ArrayList<>();
        singleAccommodationList.add(accommodation);
        MapDialogFragment mapDialogFragment = new MapDialogFragment(singleAccommodationList);
        mapDialogFragment.show(getParentFragmentManager(), "MapDialogFragment");
    }
}
