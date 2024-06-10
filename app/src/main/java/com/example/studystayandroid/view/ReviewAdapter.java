package com.example.studystayandroid.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.studystayandroid.R;
import com.example.studystayandroid.model.AccommodationReview;

import java.util.List;

public class ReviewAdapter extends ArrayAdapter<AccommodationReview> {

    public ReviewAdapter(@NonNull Context context, @NonNull List<AccommodationReview> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_review, parent, false);
        }

        AccommodationReview review = getItem(position);

        TextView reviewerNameTextView = convertView.findViewById(R.id.reviewer_name);
        TextView reviewContentTextView = convertView.findViewById(R.id.review_content);
        RatingBar ratingBar = convertView.findViewById(R.id.rating_bar);

        if (review != null) {
            reviewerNameTextView.setText(review.getAuthor().getName());
            reviewContentTextView.setText(review.getComment());
            ratingBar.setRating((float) review.getRating());
            ratingBar.setIsIndicator(true);
        }

        return convertView;
    }
}
