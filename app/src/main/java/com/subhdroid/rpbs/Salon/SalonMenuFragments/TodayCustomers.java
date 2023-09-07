package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
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
import com.subhdroid.rpbs.Salon.SalonDashboard;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;

public class TodayCustomers extends Fragment {

    DatabaseReference slotsRef = FirebaseDatabase.getInstance().getReference("slots");
    DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("customer");
    RecyclerView recyclerTodayCustomers;
    RecyclerTodayCustomerAdapter adapter;
    SalonWeekModel weekModel;
    LottieAnimationView todayCustomerLoadingAnimation;
    TextView browCustomerNotFoundTxt;
    ArrayList<SalonSlotsModel> slotList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today_customer, container, false);
        getTodaysSlots();
        todayCustomerLoadingAnimation =
                view.findViewById(R.id.todayCustomerLoadingAnimation);
        browCustomerNotFoundTxt = view.findViewById(R.id.rowCustomerNotFoundTxt);
        SalonDashboard.removeToolbarChild();
        todayCustomerLoadingAnimation.setVisibility(View.VISIBLE);

        recyclerTodayCustomers = view.findViewById(R.id.recyclerTodayCustomers);
        recyclerTodayCustomers.setLayoutManager(new LinearLayoutManager(getContext()));

        AppCompatButton todayNotifyAllBtn = view.findViewById(R.id.todayNotifyAllBtn);

        todayNotifyAllBtn.setOnClickListener(view12 -> {
            int cnt = 0;
            for (SalonSlotsModel rec : slotList) {
                cnt++;

                if (!rec.getCustomerKey().contains("Not booked yet")) {
                    getCustomerToken(rec.getCustomerKey());
                }
                if (cnt == slotList.size()) {
                    Toast.makeText(getContext(), "Notification send to all Customers!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void loadSlots(ArrayList<SalonSlotsModel> list) {
        todayCustomerLoadingAnimation.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> {
            todayCustomerLoadingAnimation.setVisibility(View.GONE);
            adapter =
                    new RecyclerTodayCustomerAdapter(getContext(), list);
            recyclerTodayCustomers.setAdapter(adapter);
        }, 2000);
    }

    private void getTodaysSlots() {
        LocalDate localDate = LocalDate.now();
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        String dayName = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault());
        if (slotsRef != null) {
            slotsRef.child(dayName.toUpperCase()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        weekModel = dataSnapshot.getValue(SalonWeekModel.class);


                        int i = 0;
                        for (SalonSlotsModel model : weekModel.getSlotList()) {
                            if (!model.getCustomerKey().contains("Not booked yet")) {
                                slotList.add(model);
                            }
                            i++;
                            if (i == weekModel.getSlotList().size()) {
                                loadSlots(slotList);
                            }
                        }
                    } else {
                        todayCustomerLoadingAnimation.setVisibility(View.GONE);
                        browCustomerNotFoundTxt.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Fail to get data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
                Toast.makeText(getContext(), "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(String customerToken) {
        FirebaseMessaging.getInstance().subscribeToTopic("all");

        String userToken = customerToken;
//        String allUserToken = "/topics/all";
        FCMNotificationSender notificationSender = new FCMNotificationSender(userToken,
                "Remainder for your service", "You have booked a slot in our salon for you services, please help to arrive on time.",
                getContext(),
                CustomerDashboard.class);

        notificationSender.sendNotifications();
        Toast.makeText(getContext(), "Notification send one Customers!",
                Toast.LENGTH_SHORT).show();

    }

}