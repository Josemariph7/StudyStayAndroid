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

/**
 * Adaptador para mostrar una lista de alojamientos en un RecyclerView.
 */
public class AccommodationAdapter extends RecyclerView.Adapter<AccommodationAdapter.AccommodationViewHolder> {

    private static List<Accommodation> accommodationList;
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;
    private byte[] defaultImageBytes;

    /**
     * Constructor para el AccommodationAdapter.
     *
     * @param accommodationList Lista de alojamientos a mostrar.
     * @param context           Contexto para acceder a los recursos.
     */
    public AccommodationAdapter(List<Accommodation> accommodationList, Context context) {
        this.accommodationList = accommodationList;
        this.defaultImageBytes = getDefaultImageBytes(context);
    }

    /**
     * Interfaz para el listener de clic en un item.
     */
    public interface OnItemClickListener {
        void onItemClick(Accommodation accommodation);
    }

    /**
     * Configura el listener de clic en un item.
     *
     * @param listener Listener a configurar.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Interfaz para el listener de clic largo en un item.
     */
    public interface OnItemLongClickListener {
        void onItemLongClick(Accommodation accommodation);
    }

    /**
     * Configura el listener de clic largo en un item.
     *
     * @param listener Listener a configurar.
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    /**
     * Obtiene los bytes de la imagen por defecto desde los recursos.
     *
     * @param context Contexto para acceder a los recursos.
     * @return Array de bytes de la imagen por defecto.
     */
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

    @Override
    public void onBindViewHolder(@NonNull AccommodationViewHolder holder, int position) {
        Accommodation accommodation = accommodationList.get(position);
        holder.address.setText(accommodation.getAddress());
        holder.city.setText(accommodation.getCity());
        holder.description.setText(accommodation.getDescription());
        holder.price.setText(String.format("$%.2f", accommodation.getPrice()));

        // Cargar fotos del alojamiento
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

    /**
     * ViewHolder para los items de alojamiento.
     */
    static class AccommodationViewHolder extends RecyclerView.ViewHolder {
        TextView address, city, description, price;
        ViewPager2 carouselViewPager;
        CircleIndicator3 indicator;

        /**
         * Constructor para el AccommodationViewHolder.
         *
         * @param itemView         Vista del item.
         * @param listener         Listener de clic en el item.
         * @param longClickListener Listener de clic largo en el item.
         */
        AccommodationViewHolder(@NonNull View itemView, OnItemClickListener listener, OnItemLongClickListener longClickListener) {
            super(itemView);
            address = itemView.findViewById(R.id.address);
            city = itemView.findViewById(R.id.city);
            description = itemView.findViewById(R.id.description);
            price = itemView.findViewById(R.id.price);
            carouselViewPager = itemView.findViewById(R.id.carousel_view_pager);
            indicator = itemView.findViewById(R.id.indicator);

            // Configurar el listener de clic en el item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(accommodationList.get(position));
                }
            });

            // Configurar el listener de clic largo en el item
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
