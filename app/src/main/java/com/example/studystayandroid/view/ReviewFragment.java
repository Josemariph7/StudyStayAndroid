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

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
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

/**
 * Fragment to display and manage reviews for an accommodation.
 */
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

    /**
     * Loads the reviews for the current accommodation and sets them to the ListView.
     */
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

    /**
     * Shows a dialog for the user to add a new review.
     */
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

    /**
     * Shows a custom dialog with options for the selected review.
     *
     * @param review The selected review.
     */
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

    /**
     * Opens the profile of the specified user.
     *
     * @param user The user whose profile is to be opened.
     */
    private void openUserProfile(User user) {
        StrangeProfileFragment strangeProfileFragment = StrangeProfileFragment.newInstance(user);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, strangeProfileFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Shows an error dialog with the specified message.
     *
     * @param message The error message.
     */
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

    /**
     * Checks if the user has a booking for the accommodation and shows the review dialog if they do.
     */
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
