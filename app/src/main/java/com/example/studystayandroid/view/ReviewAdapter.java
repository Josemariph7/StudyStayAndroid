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

/**
 * Adapter to display a list of reviews for an accommodation.
 */
public class ReviewAdapter extends ArrayAdapter<AccommodationReview> {

    /**
     * Constructor for the ReviewAdapter.
     *
     * @param context The context in which the adapter is used.
     * @param objects The list of AccommodationReview objects to display.
     */
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
