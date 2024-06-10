package com.example.studystayandroid.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.AccommodationReviewController;
import com.example.studystayandroid.controller.BookingController;
import com.example.studystayandroid.model.Accommodation;
import com.example.studystayandroid.model.AccommodationReview;
import com.example.studystayandroid.model.Booking;
import com.example.studystayandroid.model.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReviewFragment extends Fragment {

    private ListView reviewListView;
    private Button addReviewButton;
    private Button backButton;

    private Accommodation accommodation;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reviews, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        reviewListView = view.findViewById(R.id.review_list_view);
        addReviewButton = view.findViewById(R.id.add_review_button);
        backButton = view.findViewById(R.id.back_button);

        if (getArguments() != null) {
            accommodation = (Accommodation) getArguments().getSerializable("accommodation");
            currentUser = (User) getArguments().getSerializable("currentUser");
            Log.d("ReviewFragment", "Accommodation: " + accommodation);
            Log.d("ReviewFragment", "Current user: " + currentUser);
        }

        loadReviews();

        backButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        addReviewButton.setOnClickListener(v -> {
            checkUserBookingAndShowReviewDialog();
        });
    }

    private void loadReviews() {
        new AccommodationReviewController(requireContext()).getReviews(new AccommodationReviewController.ReviewListCallback() {
            @Override
            public void onSuccess(List<AccommodationReview> reviews) {
                List<AccommodationReview> filteredReviews = new ArrayList<>();
                for (AccommodationReview review : reviews) {
                    if (review.getAccommodation().getAccommodationId().equals(accommodation.getAccommodationId())) {
                        filteredReviews.add(review);
                    }
                }
                ReviewAdapter adapter = new ReviewAdapter(requireContext(), filteredReviews);
                reviewListView.setAdapter(adapter);

                reviewListView.setOnItemLongClickListener((parent, view, position, id) -> {
                    AccommodationReview review = filteredReviews.get(position);
                    showCustomReviewOptionsDialog(review);
                    return true;
                });
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error loading reviews: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddReviewDialog() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_review, null);

        TextInputEditText reviewContentEditText = dialogView.findViewById(R.id.editTextReviewContent);
        RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar_add);
        Button submitReviewButton = dialogView.findViewById(R.id.submitReviewButton);

        AlertDialog alertDialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        submitReviewButton.setOnClickListener(v -> {
            String content = reviewContentEditText.getText().toString().trim();
            float rating = ratingBar.getRating();

            if (content.isEmpty()) {
                reviewContentEditText.setError("Review content cannot be empty");
                return;
            }

            AccommodationReview review = new AccommodationReview(accommodation, currentUser, rating, content, LocalDateTime.now());

            new AccommodationReviewController(requireContext()).createReview(review, new AccommodationReviewController.ReviewCallback() {
                @Override
                public void onSuccess(Object result) {
                    Toast.makeText(getContext(), "Review added successfully", Toast.LENGTH_SHORT).show();
                    loadReviews();
                    alertDialog.dismiss();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Error adding review: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        alertDialog.show();
    }

    private void showCustomReviewOptionsDialog(AccommodationReview review) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_review_options, null);

        Button visitProfileButton = dialogView.findViewById(R.id.visit_profile_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        AlertDialog alertDialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        visitProfileButton.setOnClickListener(v -> {
            openUserProfile(review.getAuthor());
            alertDialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }

    private void openUserProfile(User user) {
        StrangeProfileFragment strangeProfileFragment = StrangeProfileFragment.newInstance(user);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, strangeProfileFragment)
                .addToBackStack(null)
                .commit();
    }

    private void showErrorDialog(String message) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_error_review, null);

        TextView errorMessageTextView = dialogView.findViewById(R.id.error_message);
        Button okButton = dialogView.findViewById(R.id.button_ok);

        AlertDialog alertDialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        errorMessageTextView.setText(message);

        okButton.setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }

    private void checkUserBookingAndShowReviewDialog() {
        BookingController bookingController = new BookingController(requireContext());

        bookingController.getBookings(new BookingController.BookingListCallback() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                boolean hasBooking = false;
                for (Booking booking : bookings) {
                    if (booking.getUser().getUserId().equals(currentUser.getUserId()) &&
                            booking.getAccommodation().getAccommodationId().equals(accommodation.getAccommodationId())) {
                        hasBooking = true;
                        break;
                    }
                }
                if (hasBooking) {
                    showAddReviewDialog();
                } else {
                    showErrorDialog("You need to have a booking to leave a review.");
                }
            }

            @Override
            public void onError(String error) {
                Log.e("ReviewFragment", "Error checking bookings: " + error);
                showErrorDialog("Error checking bookings.");
            }
        });
    }
}
