package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.subhdroid.rpbs.CloudMessage.FCMNotificationSender;
import com.subhdroid.rpbs.Customer.CustomerDashboard;
import com.subhdroid.rpbs.Customer.CustomerModel;
import com.subhdroid.rpbs.R;

import java.sql.Array;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class RecyclerWeekSlotAdapter extends RecyclerView.Adapter<RecyclerWeekSlotAdapter.ViewHolder> {
    DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("customer");

    Context context;
    ArrayList<SalonWeekModel> weekSlotList;

    public RecyclerWeekSlotAdapter(Context context,
                                   ArrayList<SalonWeekModel> weekSlotList) {
        this.context = context;
        this.weekSlotList = weekSlotList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.week_slot_row, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        if (today.getDayOfWeek().toString().contains(weekSlotList.get(position).getDay())){
            holder.rowDay.setText("TODAY");
        }else if (tomorrow.getDayOfWeek().toString().contains(weekSlotList.get(position).getDay())){
            holder.rowDay.setText("TOMORROW");
        }else {
            holder.rowDay.setText(weekSlotList.get(position).getDay());
        }
        holder.rowDate.setText(weekSlotList.get(position).getDate());
        holder.rowTiming.setText("Opens at "+weekSlotList.get(position).getOpenTime()+"\nClose at" +
                " " +weekSlotList.get(position).getCloseTime());

        RecyclerDaySlotAdapter adapter = new RecyclerDaySlotAdapter(context,
                weekSlotList.get(position).getSlotList(),weekSlotList.get(position).getDay(),
                weekSlotList.get(position).getDate());
        holder.weekSlotRecycler.setLayoutManager(new GridLayoutManager(context,2));
        holder.weekSlotRecycler.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {

        return weekSlotList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView rowDay, rowDate, rowTiming;
        RecyclerView weekSlotRecycler;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rowDay = itemView.findViewById(R.id.rowDay);
            rowDate = itemView.findViewById(R.id.rowDate);
            rowTiming = itemView.findViewById(R.id.rowTiming);
            weekSlotRecycler = itemView.findViewById(R.id.weekSlotRecycler);
        }
    }
}