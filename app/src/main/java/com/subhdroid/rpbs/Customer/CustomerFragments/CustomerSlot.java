package com.subhdroid.rpbs.Customer.CustomerFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.subhdroid.rpbs.R;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.RecyclerWeekSlotAdapter;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.SalonSlotsModel;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.SalonWeekModel;
import com.subhdroid.rpbs.Salon.SalonModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CustomerSlot extends Fragment {

    Handler handler = new Handler();
    SalonModel salonInformation;
    DatabaseReference slotsRef = FirebaseDatabase.getInstance().getReference("slots");
    DatabaseReference salonRef = FirebaseDatabase.getInstance().getReference("salon").child("information");
    ArrayList<SalonWeekModel> salonSlotList;
    RecyclerView recyclerSlots;
    LottieAnimationView loadingAnimation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_customer_slot, container, false);

        getSalonInformation();
        loadingAnimation = view.findViewById(R.id.loadingAnimation);
        loadingAnimation.setVisibility(View.VISIBLE);

        recyclerSlots = view.findViewById(R.id.recyclerSlots);
        recyclerSlots.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    private void loadSlots() {
        loadingAnimation.setVisibility(View.VISIBLE);
        recyclerSlots.setVisibility(View.GONE);
        handler.postDelayed(() -> {
            loadingAnimation.setVisibility(View.GONE);
            recyclerSlots.setVisibility(View.VISIBLE);
            RecyclerWeekSlotAdapter adapter = new RecyclerWeekSlotAdapter(getContext(),
                    salonSlotList);
            recyclerSlots.setAdapter(adapter);
        }, 2000);
    }


    private void updateSlot(ArrayList<SalonSlotsModel> slotList) {
        LocalDate localDate = LocalDate.now().minusDays(1);
        DayOfWeek yesterdayDayOfWeek = localDate.getDayOfWeek();
        String yesterdayName = yesterdayDayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM");
        String yesterdayDate = localDate.format(formatter);

        LocalDate futureLocalDate = LocalDate.now().plusDays(6);
        String futureDate = futureLocalDate.format(formatter);

        slotsRef.orderByChild("date").equalTo(yesterdayDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    SalonWeekModel weekModel = new SalonWeekModel(salonInformation.getOpeningTime(),
                            salonInformation.getClosingTime(), "30",
                            "Open",
                            futureDate,
                            slotList);

                    slotsRef.child(yesterdayName.toUpperCase()).setValue(weekModel);
                    handler.postDelayed(() -> getSalonSlots(), 2000);

                } else {
                    getSalonSlots();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }


    private ArrayList<SalonSlotsModel> createSlot() {
        ArrayList<SalonSlotsModel> slotList = new ArrayList<>();
        String openTimeString = salonInformation.getOpeningTime();
        String closeTimeString = salonInformation.getClosingTime();
        final String TIME_FORMAT = "hh:mm a";
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);

        try {
            int openHours;
            int openMinutes;

            int closeHours;
            int closeMinutes;
            Date date1 = timeFormat.parse(openTimeString);
            Date date2 = timeFormat.parse(closeTimeString);

            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(date1);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(date2);

            openHours = calendar1.get(Calendar.HOUR_OF_DAY);
            openMinutes = calendar1.get(Calendar.MINUTE);
            closeHours = calendar2.get(Calendar.HOUR_OF_DAY);
            closeMinutes = calendar2.get(Calendar.MINUTE);

            final int TIME_INTERVAL_MINUTES = salonInformation.getSlotTimePeriod();

            Calendar openingTime = Calendar.getInstance();
            openingTime.set(Calendar.HOUR_OF_DAY, openHours);
            openingTime.set(Calendar.MINUTE, openMinutes);
            openingTime.set(Calendar.SECOND, 0);

            Calendar closingTime = Calendar.getInstance();
            closingTime.set(Calendar.HOUR_OF_DAY, closeHours);
            closingTime.set(Calendar.MINUTE, closeMinutes);
            closingTime.set(Calendar.SECOND, 0);

            Calendar currentTime = openingTime;
            SimpleDateFormat timeFormatter = new SimpleDateFormat(TIME_FORMAT);

            String endTime = timeFormatter.format(closingTime.getTime());
            while (currentTime.before(closingTime)) {
                String timeSlot = timeFormatter.format(currentTime.getTime());
                currentTime.add(Calendar.MINUTE, TIME_INTERVAL_MINUTES);
                String timeSlot2 = timeFormatter.format(currentTime.getTime());

                Date d1 = timeFormatter.parse(timeSlot2);
                Date d2 = timeFormatter.parse(endTime);
                String slotTime = timeSlot + " - " + timeSlot2;
                if (d1.compareTo(d2) <= 0) {
                    ArrayList<String> emptyService = new ArrayList<>();
                    emptyService.add("Other");
                    SalonSlotsModel slotModel = new SalonSlotsModel(slotTime, emptyService, "Not booked yet");
                    slotList.add(slotModel);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return slotList;
    }

    public void getSalonSlots() {
        salonSlotList = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        LocalDate date = currentDate;
        for (int i = 0; i < 7; i++) {
            String day = date.getDayOfWeek().toString();
            date = date.plusDays(1);
            int finalI = i;
            slotsRef.child(day).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    SalonWeekModel salonSlot = snapshot.getValue(SalonWeekModel.class);
                    salonSlot.setDay(day);
                    salonSlotList.add(salonSlot);
                    if (finalI == 6) {
                        loadSlots();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void getSalonInformation() {

        salonRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                salonInformation = dataSnapshot.getValue(SalonModel.class);
                updateSlot(createSlot());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


}