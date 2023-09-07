package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.subhdroid.rpbs.Salon.SalonDashboard;
import com.subhdroid.rpbs.R;
import com.subhdroid.rpbs.Salon.SalonModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class SalonSlots extends Fragment {

    Handler handler = new Handler();
    SalonModel salonInformation;
    DatabaseReference slotsRef = FirebaseDatabase.getInstance().getReference("slots");
    DatabaseReference salonRef = FirebaseDatabase.getInstance().getReference("salon").child("information");

    ArrayList<SalonWeekModel> salonSlotList;

    AppCompatButton manageSlotBtn, slotSubmitBtn;
    EditText slotTimePeriod;
    TextView salonOpenTime, salonCloseTime;
    Dialog slotDialog;

    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
    Calendar time = Calendar.getInstance();
    RecyclerView recyclerSalonSlots;
    LottieAnimationView loadingAnimation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_salon_slots, container, false);
        manageSlotBtn = view.findViewById(R.id.manageSlotBtn);
        getSalonInformation();
        slotDialog = new Dialog(getContext());
        slotDialog.setContentView(R.layout.manage_slot_dialog);

        slotTimePeriod = slotDialog.findViewById(R.id.slotTimePeriod);
        slotSubmitBtn = slotDialog.findViewById(R.id.slotSubmitBtn);
        salonOpenTime = slotDialog.findViewById(R.id.salonOpenTime);
        salonCloseTime = slotDialog.findViewById(R.id.salonCloseTime);

        salonOpenTime.setOnClickListener(v -> {

            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                    (vv, hourOfDay, minute1) -> {
                        time.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        time.set(Calendar.MINUTE, minute1);
                        time.set(Calendar.SECOND, 0);
                        String formattedTime = timeFormat.format(time.getTime());
                        salonOpenTime.setText(formattedTime);

                    }, hour, minute, false);
            timePickerDialog.show();
        });

        salonCloseTime.setOnClickListener(v -> {

            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                    (vv, hourOfDay, minute1) -> {
                        time.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        time.set(Calendar.MINUTE, minute1);
                        time.set(Calendar.SECOND, 0);
                        String formattedTime = timeFormat.format(time.getTime());
                        salonCloseTime.setText(formattedTime);
                    }, hour, minute, false);
            timePickerDialog.show();
        });

        slotDialog.findViewById(R.id.slotDecBtn).setOnClickListener(v -> {
            int timePeriod = Integer.parseInt(slotTimePeriod.getText().toString());
            if (timePeriod > 20) {
                slotTimePeriod.setText(String.valueOf(timePeriod - 5));
            }
        });

        slotDialog.findViewById(R.id.slotIncBtn).setOnClickListener(v -> {

            int timePeriod = Integer.parseInt(slotTimePeriod.getText().toString());
            if (timePeriod < 60) {
                slotTimePeriod.setText(String.valueOf(timePeriod + 5));
            }
        });

        manageSlotBtn.setOnClickListener(v -> {
            salonOpenTime.setText(salonInformation.getOpeningTime());
            salonCloseTime.setText(salonInformation.getClosingTime());
            slotDialog.show();
        });

        AtomicReference<String> day = new AtomicReference<>("Monday");

        RadioGroup radioGroup = slotDialog.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radioButton1:
                    day.set("Monday");
                    break;
                case R.id.radioButton2:
                    day.set("Tuesday");
                    break;
                case R.id.radioButton3:
                    day.set("Wednesday");
                    break;
                case R.id.radioButton4:
                    day.set("Thursday");
                    break;
                case R.id.radioButton5:
                    day.set("Friday");
                    break;
                case R.id.radioButton6:
                    day.set("Saturday");
                    break;
                case R.id.radioButton7:
                    day.set("Sunday");
                    break;
            }
        });
        slotSubmitBtn.setOnClickListener(v -> {
            LocalTime currentTime = LocalTime.now();
            int currentHour = currentTime.getHour();
            if (currentHour >= 21 && currentHour <= 23) {

                slotsRef.child(day.get().toUpperCase()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                            updateSlot(createSlot(), day.get(), getDate(day.get()));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("Error: " + databaseError.getMessage());
                    }
                });
            } else {
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle("Warning");
                alert.setIcon(R.drawable.ic_warning_svgrepo_com);
                alert.setMessage("You can update slot time period only between the 08:00pm to " +
                        "11:00 PM");
                alert.setPositiveButton("Ok", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    slotDialog.dismiss();
                });
                alert.show();
            }

        });

        SalonDashboard.removeToolbarChild();
        loadingAnimation = view.findViewById(R.id.loadingAnimation);
        loadingAnimation.setVisibility(View.VISIBLE);

        recyclerSalonSlots = view.findViewById(R.id.recyclerSalonSlots);
        recyclerSalonSlots.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    private void loadSlots() {
        loadingAnimation.setVisibility(View.VISIBLE);
        recyclerSalonSlots.setVisibility(View.GONE);
        handler.postDelayed(() -> {
            loadingAnimation.setVisibility(View.GONE);
            recyclerSalonSlots.setVisibility(View.VISIBLE);
            RecyclerWeekSlotAdapter adapter = new RecyclerWeekSlotAdapter(getContext(),
                    salonSlotList);
            recyclerSalonSlots.setAdapter(adapter);
        }, 2000);
    }


    public String getDate(String desiredDayName) {
        Calendar calendar = Calendar.getInstance();
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int desiredDayOfWeek = findDayOfWeek(desiredDayName);
        if (desiredDayOfWeek != -1) {
            int daysToAdd = (desiredDayOfWeek - currentDayOfWeek + 7) % 7;
            calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);
            String month = new SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.getTime());
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            String date = day + " " + month;
            return date;

        } else {
            return "12 Jan";
        }

    }


    private static int findDayOfWeek(String dayName) {
        String[] weekdays = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        for (int i = 0; i < weekdays.length; i++) {
            if (weekdays[i].equalsIgnoreCase(dayName)) {
                return i + 1;
            }
        }
        return -1;
    }

    private ArrayList<SalonSlotsModel> createSlot() {
        ArrayList<SalonSlotsModel> slotList = new ArrayList<>();
        String openTimeString = salonOpenTime.getText().toString();
        String closeTimeString = salonCloseTime.getText().toString();
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

            final int TIME_INTERVAL_MINUTES = Integer.parseInt(slotTimePeriod.getText().toString());

            Calendar openingTime = Calendar.getInstance();
            openingTime.set(Calendar.HOUR_OF_DAY, openHours);
            openingTime.set(Calendar.MINUTE, openMinutes);
            openingTime.set(Calendar.SECOND, 0);

            Calendar closingTime = Calendar.getInstance();
            closingTime.set(Calendar.HOUR_OF_DAY, closeHours);
            closingTime.set(Calendar.MINUTE, closeMinutes);
            closingTime.set(Calendar.SECOND, 0);

            // Generate the time slots
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
                Log.d("Log", "Slot : " + slotTime);
                if (d1.compareTo(d2) <= 0) {
                    ArrayList<String> emptyService = new ArrayList<>();
                    emptyService.add("Other");
                    SalonSlotsModel slotModel = new SalonSlotsModel(slotTime,emptyService,"Not booked yet");
                    slotList.add(slotModel);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.d("Log", "Length : " + slotList.size());
        return slotList;
    }


    private void updateSlot(ArrayList<SalonSlotsModel> slotList, String dayName, String date) {
        SalonWeekModel weekModel = new SalonWeekModel(salonOpenTime.getText().toString(),
                salonCloseTime.getText().toString(), slotTimePeriod.getText().toString(), "Open",
                date,
                slotList);

        if (slotsRef != null) {
            slotsRef.child(dayName.toUpperCase()).setValue(weekModel);
            slotDialog.dismiss();
        }

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
                    if (salonSlot != null) {
                        salonSlot.setDay(day);
                        salonSlotList.add(salonSlot);
                    }
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

    private void updateSingleSlot(ArrayList<SalonSlotsModel> slotList) {
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
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getSalonSlots();
                        }
                    }, 2000);

                } else {
                    getSalonSlots();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private ArrayList<SalonSlotsModel> createSingleSlot() {
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

            // Generate the time slots
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

    public void getSalonInformation() {

        salonRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                salonInformation = dataSnapshot.getValue(SalonModel.class);
                updateSingleSlot(createSingleSlot());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}