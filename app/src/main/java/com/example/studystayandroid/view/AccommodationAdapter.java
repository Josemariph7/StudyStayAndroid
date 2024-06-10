package com.example.studystayandroid.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.AccommodationPhotoController;
import com.example.studystayandroid.model.Accommodation;
import com.example.studystayandroid.model.AccommodationPhoto;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class AccommodationAdapter extends RecyclerView.Adapter<AccommodationAdapter.AccommodationViewHolder> {

    private static List<Accommodation> accommodationList;
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;
    private byte[] defaultImageBytes;

    public AccommodationAdapter(List<Accommodation> accommodationList, Context context) {
        this.accommodationList = accommodationList;
        this.defaultImageBytes = getDefaultImageBytes(context);
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

    private byte[] getDefaultImageBytes(Context context) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.accommodation);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    @NonNull
    @Override
    public AccommodationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_accommodation, parent, false);
        return new AccommodationViewHolder(view, listener, longClickListener);
    }

    public void onBindViewHolder(@NonNull AccommodationViewHolder holder, int position) {
        Accommodation accommodation = accommodationList.get(position);
        holder.address.setText(accommodation.getAddress());
        holder.city.setText(accommodation.getCity());
        holder.description.setText(accommodation.getDescription());
        holder.price.setText(String.format("$%.2f", accommodation.getPrice()));

        AccommodationPhotoController controller = new AccommodationPhotoController(holder.itemView.getContext());
        controller.getPhotos(new AccommodationPhotoController.PhotoListCallback() {
            @Override
            public void onSuccess(List<AccommodationPhoto> photos) {
                List<byte[]> photoDataList = new ArrayList<>();
                for (AccommodationPhoto photo : photos) {
                    if (photo.getAccommodation().getAccommodationId().equals(accommodation.getAccommodationId())) {
                        photoDataList.add(Base64.decode(photo.getPhotoData(), Base64.DEFAULT));
                    }
                }
                if (photoDataList.isEmpty()) {
                    Log.d("AccommodationAdapter", "No photos found for accommodation ID: " + accommodation.getAccommodationId());
                    holder.carouselViewPager.setAdapter(new ImageCarouselAdapter(Collections.singletonList(defaultImageBytes), holder.itemView.getContext()));
                } else {
                    Log.d("AccommodationAdapter", "Photos found for accommodation ID: " + accommodation.getAccommodationId());
                    ImageCarouselAdapter adapter = new ImageCarouselAdapter(photoDataList, holder.itemView.getContext());
                    holder.carouselViewPager.setAdapter(adapter);
                    holder.indicator.setViewPager(holder.carouselViewPager);
                }
            }

            @Override
            public void onError(String error) {
                Log.e("AccommodationAdapter", "Error loading photos: " + error);
                holder.carouselViewPager.setAdapter(new ImageCarouselAdapter(Collections.singletonList(defaultImageBytes), holder.itemView.getContext()));
            }
        });
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
