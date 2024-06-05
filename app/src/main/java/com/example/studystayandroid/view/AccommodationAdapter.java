package com.example.studystayandroid.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.studystayandroid.R;
import com.example.studystayandroid.model.Accommodation;
import com.example.studystayandroid.model.AccommodationPhoto;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class AccommodationAdapter extends RecyclerView.Adapter<AccommodationAdapter.AccommodationViewHolder> {

    private static List<Accommodation> accommodationList;
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public AccommodationAdapter(List<Accommodation> accommodationList) {
        this.accommodationList = accommodationList;
    }

    public interface OnItemClickListener {
        void onItemClick(Accommodation accommodation);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
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
        return new AccommodationViewHolder(view, listener, longClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AccommodationViewHolder holder, int position) {
        Accommodation accommodation = accommodationList.get(position);
        holder.address.setText(accommodation.getAddress());
        holder.city.setText(accommodation.getCity());
        holder.description.setText(accommodation.getDescription());
        holder.price.setText(String.format("$%.2f", accommodation.getPrice()));

        List<byte[]> photos = new ArrayList<>();
        for (AccommodationPhoto photo : accommodation.getPhotos()) {
            photos.add(photo.getPhotoData());
        }
        ImageCarouselAdapter adapter = new ImageCarouselAdapter(photos, holder.itemView.getContext());
        holder.carouselViewPager.setAdapter(adapter);
        holder.indicator.setViewPager(holder.carouselViewPager);
    }

    @Override
    public int getItemCount() {
        return accommodationList.size();
    }

    static class AccommodationViewHolder extends RecyclerView.ViewHolder {
        TextView address, city, description, price;
        ViewPager2 carouselViewPager;
        CircleIndicator3 indicator;

        AccommodationViewHolder(@NonNull View itemView, OnItemClickListener listener, OnItemLongClickListener longClickListener) {
            super(itemView);
            address = itemView.findViewById(R.id.address);
            city = itemView.findViewById(R.id.city);
            description = itemView.findViewById(R.id.description);
            price = itemView.findViewById(R.id.price);
            carouselViewPager = itemView.findViewById(R.id.carousel_view_pager);
            indicator = itemView.findViewById(R.id.indicator);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(accommodationList.get(position));
                }
            });

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
