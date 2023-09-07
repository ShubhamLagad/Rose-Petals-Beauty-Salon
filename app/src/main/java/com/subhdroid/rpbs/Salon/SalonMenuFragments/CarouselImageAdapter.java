package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.subhdroid.rpbs.R;

import java.util.List;

public class CarouselImageAdapter extends RecyclerView.Adapter<CarouselImageAdapter.ViewHolder> {
    private List<String> imageUrls;
    Context context;


    public CarouselImageAdapter(Context context,List<String> imageUrls) {
        this.imageUrls = imageUrls;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        // Zoom out
        holder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        Picasso picasso = new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(context))
                .build();

        picasso.load(imageUrl)
                .resize(800, 0)  // Adjust the size to fit your needs
                .onlyScaleDown()     // Only scale down larger images
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
