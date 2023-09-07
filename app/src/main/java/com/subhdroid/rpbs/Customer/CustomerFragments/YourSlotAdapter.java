package com.subhdroid.rpbs.Customer.CustomerFragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.subhdroid.rpbs.R;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.SalonSlotsModel;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class YourSlotAdapter extends RecyclerView.Adapter<YourSlotAdapter.ViewHolder> {
    Context context;
    ArrayList<SalonSlotsModel> slotList;

    YourSlotAdapter(Context context, ArrayList<SalonSlotsModel> slotList) {
        this.context = context;
        this.slotList = slotList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.your_slot_row, parent, false);
        YourSlotAdapter.ViewHolder viewHolder = new YourSlotAdapter.ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int pos = position;
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        if (today.getDayOfWeek().toString().equalsIgnoreCase(slotList.get(position).getDayName())) {
            holder.yourSlotDay.setText("TODAY");
        } else if (tomorrow.getDayOfWeek().toString().equalsIgnoreCase(slotList.get(position).getDayName())) {
            holder.yourSlotDay.setText("TOMORROW");
        } else {
            holder.yourSlotDay.setText(slotList.get(position).getDayName());
        }
        holder.yourSlotDate.setText(slotList.get(position).getDate());
        holder.yourSlotTime.setText(slotList.get(position).getSlotTime());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1,
                slotList.get(position).getService());
        holder.yourSlotServicesList.setAdapter(adapter);

        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter;
        String slotTime;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            formatter = DateTimeFormatter.ofPattern("hh:mm a");
            slotTime = slotList.get(pos).getSlotTime().split("-")[0].trim().toLowerCase();
        }else {
            formatter = DateTimeFormatter.ofPattern("h:mm a");
            slotTime = slotList.get(pos).getSlotTime().split("-")[0].trim();
        }

        LocalTime time = LocalTime.parse(slotTime, formatter);

        Duration duration = Duration.between(currentTime, time);
        int minutes = (int) duration.toMinutes();
        if (minutes < 0) {
            holder.cancelBookingBtn.setVisibility(View.GONE);
        }

        holder.cancelBookingBtn.setOnClickListener(vv -> {
            LocalDate todayDate = LocalDate.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM");
            String formattedDate = todayDate.format(dateFormatter);

            if (minutes <= 60 && formattedDate.contains(slotList.get(pos).getDate())) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle("Warning");
                alert.setMessage("You can't cancel slot before 1 Hrs.\nPlease contact salon.");
                alert.setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss());
                alert.show();
            } else {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle("Cancel Booking");
                alert.setMessage("Are sure want to cancel booking");
                alert.setPositiveButton("Yes", (dialogInterface, i) -> cancelBooking(slotList.get(pos).getId(), slotList.get(pos).getDayName(),
                        slotList.get(pos).getSlotTime()));
                alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
                alert.show();
            }
        });

    }

    private void cancelBooking(String id, String day, String slotTime) {
        DatabaseReference slotRef =
                FirebaseDatabase.getInstance().getReference("slots").child(day).child("slotList").child(id);
        ArrayList<String> emptyService = new ArrayList<>();
        emptyService.add("Other");
        SalonSlotsModel slotModel = new SalonSlotsModel(slotTime, emptyService, "Not booked yet");
        slotRef.setValue(slotModel);
    }

    @Override
    public int getItemCount() {
        return slotList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView yourSlotDay, yourSlotDate, yourSlotTime;
        ListView yourSlotServicesList;
        AppCompatButton cancelBookingBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            yourSlotDay = itemView.findViewById(R.id.yourSlotDay);
            yourSlotDate = itemView.findViewById(R.id.yourSlotDate);
            yourSlotTime = itemView.findViewById(R.id.yourSlotTime);
            yourSlotServicesList = itemView.findViewById(R.id.yourSlotServicesList);
            cancelBookingBtn = itemView.findViewById(R.id.cancelBookingBtn);
        }
    }
}
