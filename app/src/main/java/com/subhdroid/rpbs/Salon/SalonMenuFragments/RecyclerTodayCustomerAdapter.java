package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import org.w3c.dom.Text;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class RecyclerTodayCustomerAdapter extends RecyclerView.Adapter<RecyclerTodayCustomerAdapter.ViewHolder> {

    Context context;
    ArrayList<SalonSlotsModel> slotList;
    DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("customer");
    DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("transactions");


    RecyclerTodayCustomerAdapter(Context context,
                                 ArrayList<SalonSlotsModel> slotList) {
        this.context = context;
        this.slotList = slotList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.todays_single_customer_row, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.rowTodaySrNo.setText(String.valueOf(position + 1));
        getCustomer(slotList.get(position).getCustomerKey(), holder.rowTodayCustomerName, holder.rowTodayCustMobile);
        getLastVisit("Last visited on "+slotList.get(position).getCustomerKey(),
                holder.rowTodayLastService);
        int pos = position;
        holder.rowTodayCustTiming.setText(slotList.get(position).getSlotTime());

        holder.rowTodayCustomerNotifyBtn.setOnClickListener(view -> getCustomerToken(slotList.get(pos).getCustomerKey()));
        holder.rowTodayCustMobile.setOnClickListener(v->{
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" +  holder.rowTodayCustMobile.getText().toString()));
            try {
               context.startActivity(callIntent);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        });

    }
    @Override
    public int getItemCount() {
        return slotList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView rowTodayCustomerName, rowTodayLastService, rowTodaySrNo,
                rowTodayCustomerNotifyBtn, rowTodayCustMobile,rowTodayCustTiming;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rowTodayCustomerName = itemView.findViewById(R.id.rowTodayCustomerName);
            rowTodayLastService = itemView.findViewById(R.id.rowTodayLastService);
            rowTodaySrNo = itemView.findViewById(R.id.rowTodaySrNo);
            rowTodayCustomerNotifyBtn = itemView.findViewById(R.id.rowTodayCustomerNotifyBtn);
            rowTodayCustMobile = itemView.findViewById(R.id.rowTodayCustMobile);
            rowTodayCustTiming = itemView.findViewById(R.id.rowTodayCustTiming);
        }
    }

    private void sendNotification(String customerToken) {
        FirebaseMessaging.getInstance().subscribeToTopic("all");

        String userToken = customerToken;
//        String allUserToken = "/topics/all";
        FCMNotificationSender notificationSender = new FCMNotificationSender(userToken,
                "Remainder for your service", "You have booked a slot in our salon for you services, please help to arrive on time.",
                context.getApplicationContext(),
                CustomerDashboard.class);

        notificationSender.sendNotifications();
        Toast.makeText(context.getApplicationContext(), "Notification send one Customers!",
                Toast.LENGTH_SHORT).show();

    }

    private void getCustomerToken(String custKey) {
        customerRef.child(custKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                CustomerModel customer = dataSnapshot.getValue(CustomerModel.class);
                    sendNotification(customer.getFcmToken());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context.getApplicationContext(), "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLastVisit(String custKey, TextView rowTodayLastService) {

        transactionRef.child(custKey).limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        TransactionModel lastRecord = childSnapshot.getValue(TransactionModel.class);
                        rowTodayLastService.setText(lastRecord.getDate());
                    }
                }else {
                    Animation anim = new AlphaAnimation(0.0f, 1.0f);
                    anim.setDuration(500); // Change blinking speed here (in milliseconds)
                    anim.setStartOffset(20);
                    anim.setRepeatMode(Animation.REVERSE);
                    anim.setRepeatCount(Animation.INFINITE);
                    rowTodayLastService.startAnimation(anim);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getCustomer(String custKey, TextView rowTodayCustomerName, TextView rowTodayCustMobile) {

        if (customerRef != null) {
            customerRef.child(custKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    CustomerModel customer = dataSnapshot.getValue(CustomerModel.class);
                    rowTodayCustomerName.setText(customer.getCustName());
                    rowTodayCustMobile.setText(customer.getCustPhone());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Fail to get data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
