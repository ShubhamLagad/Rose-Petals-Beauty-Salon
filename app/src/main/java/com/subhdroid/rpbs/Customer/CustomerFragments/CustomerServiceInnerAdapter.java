package com.subhdroid.rpbs.Customer.CustomerFragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.subhdroid.rpbs.R;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.SalonServiceModel;

import java.util.ArrayList;

public class CustomerServiceInnerAdapter extends RecyclerView.Adapter<CustomerServiceInnerAdapter.ViewHolder> {

    Context context;
    ArrayList<SalonServiceModel> salonServicesArrayList;
    DatabaseReference serviceDetailsRef = FirebaseDatabase.getInstance().getReference(
            "serviceDetails");

    CustomerServiceInnerAdapter(Context context,
                                 ArrayList<SalonServiceModel> salonServicesArrayList) {
        this.context = context;
        this.salonServicesArrayList = salonServicesArrayList;
    }

    @NonNull
    @Override
    public CustomerServiceInnerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.customer_service_row, parent, false);
        CustomerServiceInnerAdapter.ViewHolder viewHolder = new CustomerServiceInnerAdapter.ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerServiceInnerAdapter.ViewHolder holder, int position) {

        getServiceDetails(salonServicesArrayList.get(position).getId(), position);

        holder.rowServiceName.setText(salonServicesArrayList.get(position).getService_name());
        holder.rowServiceTimePeriod.setText(salonServicesArrayList.get(position).getService_time_period() + " min");
        holder.rowServicePrice.setText("Rs."+salonServicesArrayList.get(position).getPrice());
        holder.rowServiceNo.setText(String.valueOf(position + 1));


        holder.rowViewDetailsArrowDown.setOnClickListener(v -> {
            holder.serviceDetailsCardView.setVisibility(View.VISIBLE);
            holder.rowViewDetailsArrowDown.setVisibility(View.GONE);
            holder.rowViewDetailsArrowUp.setVisibility(View.VISIBLE);
        });

        holder.rowViewDetailsArrowUp.setOnClickListener(v -> {
            holder.serviceDetailsCardView.setVisibility(View.GONE);
            holder.rowViewDetailsArrowUp.setVisibility(View.GONE);
            holder.rowViewDetailsArrowDown.setVisibility(View.VISIBLE);
        });

        if (salonServicesArrayList.get(position).getDetails()!=null){
            new Handler().postDelayed(() -> {

                holder.rowServiceDetails.setText(salonServicesArrayList.get(position).getDetails());
                int cnt = 0;
                if (salonServicesArrayList.get(position).getVideoLinks()!=null){
                    for (String url : salonServicesArrayList.get(position).getVideoLinks()) {

                        TextView textView = new TextView(context);

                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.setMargins(0, 11, 0, 0);
                        textView.setLayoutParams(layoutParams);
                        cnt++;

                        String linkText = "Video " + cnt + ": Click here to watch video.";
                        SpannableString spannableString = new SpannableString(linkText);

                        ClickableSpan clickableSpan = new ClickableSpan() {
                            @Override
                            public void onClick(View widget) {
                                Uri uri = Uri.parse(url);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                context.startActivity(intent);
                            }
                        };

                        spannableString.setSpan(clickableSpan, 0, linkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        textView.setText(spannableString);
                        textView.setMovementMethod(LinkMovementMethod.getInstance());

                        holder.rowServiceVideos.addView(textView);
                    }
                }else {
                    holder.rowServiceVideos.setVisibility(View.GONE);
                }

            }, 2000);
        }else {
            holder.detailsLL.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return salonServicesArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView rowServiceName, rowServicePrice, rowServiceNo , rowServiceTimePeriod;

        TextView rowServiceDetails;
        ImageButton rowViewDetailsArrowUp, rowViewDetailsArrowDown;
        LinearLayout rowServiceVideos,detailsLL;
        CardView serviceDetailsCardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rowServiceName = itemView.findViewById(R.id.customerRowServiceName);
            rowServiceTimePeriod = itemView.findViewById(R.id.customerRowServiceTimePeriod);
            rowServicePrice = itemView.findViewById(R.id.customerRowServicePrice);
            rowServiceNo = itemView.findViewById(R.id.customerRowServiceNo);

            rowServiceDetails = itemView.findViewById(R.id.serviceDetails);
            rowServiceVideos = itemView.findViewById(R.id.serviceVideos);
            rowViewDetailsArrowUp = itemView.findViewById(R.id.viewDetailsArrowUp);
            rowViewDetailsArrowDown = itemView.findViewById(R.id.viewDetailsArrowDown);
            serviceDetailsCardView = itemView.findViewById(R.id.rowServiceDetailsCardView);
            detailsLL = itemView.findViewById(R.id.detailsLL);
        }
    }




    private void getServiceDetails(String key, int position) {
        serviceDetailsRef.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SalonServiceModel salonServiceDetails =
                        snapshot.getValue(SalonServiceModel.class);
                if (salonServiceDetails!=null){
                    salonServicesArrayList.get(position).setDetails(salonServiceDetails.getDetails());
                    salonServicesArrayList.get(position).setVideoLinks(salonServiceDetails.getVideoLinks());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DB Error : ", error.toString());
            }
        });
    }

}