package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.subhdroid.rpbs.R;

import java.util.ArrayList;
import java.util.List;


public class RecyclerSalonProductsAdapter extends RecyclerView.Adapter<RecyclerSalonProductsAdapter.ViewHolder> {
    Context context;
    ArrayList<SalonProductModel> productList;

    RecyclerSalonProductsAdapter(Context context, ArrayList<SalonProductModel> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public RecyclerSalonProductsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.salon_product_row, parent, false);
        RecyclerSalonProductsAdapter.ViewHolder viewHolder = new RecyclerSalonProductsAdapter.ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerSalonProductsAdapter.ViewHolder holder, int position) {

        holder.productName.setText(productList.get(position).getProdName());
        holder.productPrice.setText(productList.get(position).getProdPrice());
        double revCnt = Double.parseDouble(productList.get(position).getReview());

        if (productList.get(position).getDescription()!=null){
            holder.productDescription.setText(productList.get(position).getDescription());
        }else {
            holder.productDescription.setVisibility(View.GONE);
        }

        int pos=position;
        holder.productReview.setText(revCnt+" \u2605");
        if (revCnt<3){
            holder.productReview.setBackgroundColor(context.getResources().getColor(R.color.danger));
        }else if(revCnt>=4){
            holder.productReview.setBackgroundColor(context.getResources().getColor(R.color.success));
        }else {
            holder.productReview.setBackgroundColor(context.getResources().getColor(R.color.medium));
        }

        List<String> imageUrls = productList.get(position).getPhotos();
        CarouselImageAdapter adapter = new CarouselImageAdapter(context.getApplicationContext(),imageUrls);
        holder.viewPager.setAdapter(adapter);

        holder.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                int imageNumber = position + 1;
                int totalCount = adapter.getItemCount();

                String imageText = imageNumber + " of " + totalCount;
                holder.imageNumberTextView.setText(imageText);
            }
        });

        holder.productDeleteBtn.setOnClickListener(view -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle("Delete");
            alertDialog.setMessage("Do you want to delete product?");
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    deleteProduct(productList.get(pos).getId(),productList.get(pos).getPhotos());
                }
            });
            alertDialog.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
            alertDialog.show();
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productReview, imageNumberTextView,productDescription;
        ViewPager2 viewPager;
        ImageButton productDeleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            viewPager = itemView.findViewById(R.id.viewPager);
            productPrice = itemView.findViewById(R.id.productPrice);
            productReview = itemView.findViewById(R.id.productReview);
            imageNumberTextView = itemView.findViewById(R.id.imageNumberTextView);
            productDeleteBtn = itemView.findViewById(R.id.productDeleteBtn);
            productDescription = itemView.findViewById(R.id.productDescription);
        }
    }

    private void deleteProduct(String key,ArrayList<String> photos) {
        DatabaseReference productRef =
                FirebaseDatabase.getInstance().getReference("products").child(key);

        for (String url:photos) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
            storageRef.delete().addOnSuccessListener(aVoid -> Toast.makeText(context, "Image Deleted", Toast.LENGTH_SHORT).show()).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            });
        }

        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.getRef().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DB Error : ", error.toString());
            }
        });
    }
}
