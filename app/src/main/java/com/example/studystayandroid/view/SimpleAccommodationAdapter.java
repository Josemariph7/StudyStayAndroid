// SimpleAccommodationAdapter.java
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

public class SimpleAccommodationAdapter extends RecyclerView.Adapter<SimpleAccommodationAdapter.ViewHolder> {

    private List<Accommodation> accommodations = new ArrayList<>();
    private OnItemClickListener listener;

    public void setAccommodations(List<Accommodation> accommodations) {
        this.accommodations = accommodations;
        notifyDataSetChanged();
    }

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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView addressTextView, descriptionTextView, ownerTextView, priceTextView, ratingTextView;
        ImageView ratingImageView;

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

    public interface OnItemClickListener {
        void onItemClick(Accommodation accommodation);
    }
}
