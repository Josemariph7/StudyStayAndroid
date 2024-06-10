package com.example.studystayandroid.view;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studystayandroid.R;

import java.util.List;

public class ImageCarouselAdapter extends RecyclerView.Adapter<ImageCarouselAdapter.ImageViewHolder> {

    private List<byte[]> photoDataList;
    private Context context;

    public ImageCarouselAdapter(List<byte[]> photoDataList, Context context) {
        this.photoDataList = photoDataList;
        this.context = context;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        byte[] photoData = photoDataList.get(position);
        Glide.with(context)
                .asBitmap()
                .load(photoData)
                .error(R.drawable.accommodation) // Asegúrate de tener una imagen de error o por defecto
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return photoDataList.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }
    }
}
