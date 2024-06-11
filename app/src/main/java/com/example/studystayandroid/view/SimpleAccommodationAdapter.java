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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studystayandroid.R;
import com.example.studystayandroid.model.Accommodation;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a simple list of accommodations in a RecyclerView.
 */
public class SimpleAccommodationAdapter extends RecyclerView.Adapter<SimpleAccommodationAdapter.ViewHolder> {

    private List<Accommodation> accommodations = new ArrayList<>();
    private OnItemClickListener listener;

    /**
     * Sets the list of accommodations to be displayed.
     *
     * @param accommodations The list of accommodations.
     */
    public void setAccommodations(List<Accommodation> accommodations) {
        this.accommodations = accommodations;
        notifyDataSetChanged();
    }

    /**
     * Sets the listener for item click events.
     *
     * @param listener The listener for item click events.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_accommodation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Accommodation accommodation = accommodations.get(position);
        holder.addressTextView.setText(accommodation.getAddress() + ", " + accommodation.getCity());
        holder.descriptionTextView.setText(accommodation.getDescription());
        holder.ownerTextView.setText(accommodation.getOwner().getName() + " " + accommodation.getOwner().getLastName());
        holder.priceTextView.setText(String.format("$%.2f", accommodation.getPrice()));
        holder.ratingTextView.setText(String.valueOf(accommodation.getRating()));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(accommodation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return accommodations.size();
    }

    /**
     * ViewHolder for displaying an accommodation item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView addressTextView, descriptionTextView, ownerTextView, priceTextView, ratingTextView;
        ImageView ratingImageView;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The view for the item.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            addressTextView = itemView.findViewById(R.id.address);
            descriptionTextView = itemView.findViewById(R.id.description);
            ownerTextView = itemView.findViewById(R.id.ownerName);
            priceTextView = itemView.findViewById(R.id.price);
            ratingTextView = itemView.findViewById(R.id.rating);
            ratingImageView = itemView.findViewById(R.id.imageView3);
        }
    }

    /**
     * Interface for item click events.
     */
    public interface OnItemClickListener {
        /**
         * Called when an item is clicked.
         *
         * @param accommodation The clicked accommodation.
         */
        void onItemClick(Accommodation accommodation);
    }
}
