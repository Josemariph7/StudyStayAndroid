package com.example.studystayandroid.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studystayandroid.R;
import com.example.studystayandroid.model.Accommodation;

import java.util.List;

public class AccommodationAdapter extends RecyclerView.Adapter<AccommodationAdapter.AccommodationViewHolder> {

    private static List<Accommodation> accommodationList;
    private OnItemLongClickListener longClickListener;

    public AccommodationAdapter(List<Accommodation> accommodationList) {
        this.accommodationList = accommodationList;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Accommodation accommodation);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public AccommodationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_accommodation, parent, false);
        return new AccommodationViewHolder(view, longClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AccommodationViewHolder holder, int position) {
        Accommodation accommodation = accommodationList.get(position);
        holder.address.setText(accommodation.getAddress());
        holder.city.setText(accommodation.getCity());
        holder.description.setText(accommodation.getDescription());
        holder.price.setText(String.format("$%.2f", accommodation.getPrice()));
        // Cargar la imagen por defecto
        Glide.with(holder.itemView.getContext())
                .load(R.drawable.defaultaccommodation)
                .into(holder.carouselImageView);

        // Cargar la imagen del carrusel
        if (accommodation.getPhotos() != null && !accommodation.getPhotos().isEmpty()) {
            byte[] photoBytes = accommodation.getPhotos().get(0).getPhotoData();
            Glide.with(holder.itemView.getContext())
                    .asBitmap()
                    .load(photoBytes)
                    .placeholder(R.drawable.defaultaccommodation)
                    .into(holder.carouselImageView);
        }
    }

    @Override
    public int getItemCount() {
        return accommodationList.size();
    }

    static class AccommodationViewHolder extends RecyclerView.ViewHolder {
        TextView address, city, description, price;
        ImageView carouselImageView;

        AccommodationViewHolder(@NonNull View itemView, OnItemLongClickListener longClickListener) {
            super(itemView);
            address = itemView.findViewById(R.id.address);
            city = itemView.findViewById(R.id.city);
            description = itemView.findViewById(R.id.description);
            price = itemView.findViewById(R.id.price);
            carouselImageView = itemView.findViewById(R.id.carousel_image_view);

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        longClickListener.onItemLongClick(accommodationList.get(position));
                    }
                }
                return true;
            });
        }
    }
}
