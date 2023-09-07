package com.subhdroid.rpbs.Customer.CustomerFragments;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.subhdroid.rpbs.R;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.CarouselImageAdapter;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.SalonProductModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class CustomerProductsAdapter extends RecyclerView.Adapter<CustomerProductsAdapter.ViewHolder> {
    Context context;
    ArrayList<SalonProductModel> productList;
    private ImageView star1, star2, star3, star4, star5;
    double rating = 0.0;
    Dialog reviewDialog;

    CustomerProductsAdapter(Context context, ArrayList<SalonProductModel> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public CustomerProductsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.customer_products_row, parent, false);
        CustomerProductsAdapter.ViewHolder viewHolder = new CustomerProductsAdapter.ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerProductsAdapter.ViewHolder holder, int position) {

        holder.productName.setText(productList.get(position).getProdName());
        holder.productPrice.setText(productList.get(position).getProdPrice());
        double revCnt = Double.parseDouble(productList.get(position).getReview());

        int pos = position;
        holder.productReview.setText(revCnt + " \u2605");
        if (revCnt < 3) {
            holder.productReview.setBackgroundColor(context.getResources().getColor(R.color.danger));
        } else if (revCnt >= 4) {
            holder.productReview.setBackgroundColor(context.getResources().getColor(R.color.success));
        } else {
            holder.productReview.setBackgroundColor(context.getResources().getColor(R.color.medium));
        }

        List<String> imageUrls = productList.get(position).getPhotos();
        Picasso picasso = new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(context))
                .build();

        picasso.load(imageUrls.get(0))
                .resize(800, 0)  // Adjust the size to fit your needs
                .onlyScaleDown()     // Only scale down larger images
                .into(holder.imageView);


        holder.imageView.setOnClickListener(view -> {
            Dialog imageViewerDialog = new Dialog(context);
            imageViewerDialog.setContentView(R.layout.image_viewer_dialog);
            ViewPager2 viewPager = imageViewerDialog.findViewById(R.id.customerViewPager);
            CarouselImageAdapter adapter = new CarouselImageAdapter(context, imageUrls);
            viewPager.setAdapter(adapter);
            TextView txt = imageViewerDialog.findViewById(R.id.imageNumber);
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    int imageNumber = position + 1;
                    int totalCount = adapter.getItemCount();
                    String imageText = imageNumber + " of " + totalCount;
                    txt.setText(imageText);
                }
            });
            imageViewerDialog.show();

        });

        holder.customerProductRateBtn.setOnClickListener(view -> showReviewDialog(productList.get(pos).getProdName(), Double.parseDouble(productList.get(pos).getReview())
                , productList.get(pos).getId()));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productReview, imageNumberTextView;
        AppCompatButton customerProductRateBtn;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.customerProductName);
            productPrice = itemView.findViewById(R.id.customerProductPrice);
            productReview = itemView.findViewById(R.id.customerProductReview);
            imageNumberTextView = itemView.findViewById(R.id.imageNumber);
            customerProductRateBtn = itemView.findViewById(R.id.customerProductRateBtn);
            imageView = itemView.findViewById(R.id.productImage);
        }
    }

    public void showReviewDialog(String itemName, double reviewCnt, String key) {
        reviewDialog = new Dialog(context);
        reviewDialog.setContentView(R.layout.add_review_dialog);
        AppCompatButton submitBtn = reviewDialog.findViewById(R.id.reviewSubmitBtn);
        TextView itemNameTV = reviewDialog.findViewById(R.id.itemName);
        itemNameTV.setText(itemName);

        reviewDialog.findViewById(R.id.closBtn).setOnClickListener(v -> reviewDialog.dismiss());

        star1 = reviewDialog.findViewById(R.id.star1);
        star2 = reviewDialog.findViewById(R.id.star2);
        star3 = reviewDialog.findViewById(R.id.star3);
        star4 = reviewDialog.findViewById(R.id.star4);
        star5 = reviewDialog.findViewById(R.id.star5);

        star1.setOnClickListener(v -> {
            rating = 1.0;
            updateStarRating();
        });

        star2.setOnClickListener(v -> {
            rating = 2.0;
            updateStarRating();
        });

        star3.setOnClickListener(v -> {
            rating = 3.0;
            updateStarRating();
        });

        star4.setOnClickListener(v -> {
            rating = 4.0;
            updateStarRating();
        });

        star5.setOnClickListener(v -> {
            rating = 5.0;
            updateStarRating();
        });

        submitBtn.setOnClickListener(view -> {
            double finalReview = rating;

            if (reviewCnt > 0) {
                finalReview = (reviewCnt + rating) / 2;
            }
            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            String finalReview1 = decimalFormat.format(finalReview);
            addReview(finalReview1, key);
        });

        reviewDialog.show();

    }

    private void updateStarRating() {
        star1.setImageResource(R.drawable.empty_star);
        star2.setImageResource(R.drawable.empty_star);
        star3.setImageResource(R.drawable.empty_star);
        star4.setImageResource(R.drawable.empty_star);
        star5.setImageResource(R.drawable.empty_star);

        switch ((int) rating) {
            case 5:
                star5.setImageResource(R.drawable.fill_star);
            case 4:
                star4.setImageResource(R.drawable.fill_star);
            case 3:
                star3.setImageResource(R.drawable.fill_star);
            case 2:
                star2.setImageResource(R.drawable.fill_star);
            case 1:
                star1.setImageResource(R.drawable.fill_star);
                break;
            default:
                break;
        }
    }

    private void addReview(String review, String key) {
        DatabaseReference productRef =
                FirebaseDatabase.getInstance().getReference("products").child(key);
        productRef.child("review").setValue(review);
        reviewDialog.dismiss();
    }
}
