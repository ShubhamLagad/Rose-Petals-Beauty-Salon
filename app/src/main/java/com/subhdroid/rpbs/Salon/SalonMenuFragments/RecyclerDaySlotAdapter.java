package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.subhdroid.rpbs.Salon.SalonDashboard;
import com.subhdroid.rpbs.Salon.SalonModel;

import java.sql.Array;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecyclerDaySlotAdapter extends RecyclerView.Adapter<RecyclerDaySlotAdapter.ViewHolder> {
    DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("customer");
    ServiceAdapter serviceAdapter;
    DatabaseReference servicesRef = FirebaseDatabase.getInstance().getReference("services");
    DatabaseReference slotsRef = FirebaseDatabase.getInstance().getReference("slots");
    DatabaseReference salonRef = FirebaseDatabase.getInstance().getReference("salon");
    SalonModel salonInformation;
    List<String> services ;
    ArrayList<SalonServiceModel> salonServicesList;

    Context context;
    ArrayList<SalonSlotsModel> daySlotList;
    CustomerModel customerModel;
    String dayName;
    String date;
    TextView slotName, slotPhone;
    Dialog slotCustomerDialog;
    public RecyclerDaySlotAdapter(Context context,
                                  ArrayList<SalonSlotsModel> daySlotList, String dayName,
                                  String date) {
        this.context = context;
        this.daySlotList = daySlotList;
        this.dayName = dayName;
        this.date = date;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.day_slot_row, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        getSalonInformation();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.slotTiming.setText(daySlotList.get(position).getSlotTime());
        int pos = position;

        if (!daySlotList.get(position).getCustomerKey().contains("Not booked yet")) {

            holder.slotTiming.setBackground(context.getDrawable(R.drawable.booked_slot_format));
            holder.slotTiming.setTextColor(context.getColor(R.color.muted_text));

            SharedPreferences pref = context.getSharedPreferences("Salon", Context.MODE_PRIVATE);
            if (pref.getBoolean("salonLoggedIn", false)) {
                holder.slotTiming.setOnClickListener(v -> {

                    getSlotCustomerDetails(daySlotList.get(position).getCustomerKey());

                    slotCustomerDialog = new Dialog(context);
                    slotCustomerDialog.setContentView(R.layout.slot_customer_dialog);

                    slotCustomerDialog.findViewById(R.id.slotClose).setOnClickListener(s -> slotCustomerDialog.dismiss());

                    TextView slotDay = slotCustomerDialog.findViewById(R.id.slotDay);
                    slotDay.setText(dayName);
                    TextView slotDate = slotCustomerDialog.findViewById(R.id.slotDate);
                    slotDate.setText(date);

                    ListView servicesList = slotCustomerDialog.findViewById(R.id.servicesList);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                            android.R.layout.simple_list_item_1, daySlotList.get(position).getService());
                    servicesList.setAdapter(adapter);

                    slotName = slotCustomerDialog.findViewById(R.id.slotName);
                    slotPhone = slotCustomerDialog.findViewById(R.id.slotPhone);

                });

            }
        } else {
            SharedPreferences pref = context.getSharedPreferences("Customer", Context.MODE_PRIVATE);
            String custKey = pref.getString("key", "customer");
            int slotPosition = position;
            if (pref.getBoolean("CustomerLoggedIn", false)) {

                holder.slotTiming.setOnClickListener(vi -> {

                    LocalTime currentTime= LocalTime.now();
                    String slotTime;
                    DateTimeFormatter formatter;
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                         formatter = DateTimeFormatter.ofPattern("hh:mm a");
                         slotTime = daySlotList.get(pos).getSlotTime().split("-")[0].trim().toLowerCase();
                    }else {
                         formatter = DateTimeFormatter.ofPattern("h:mm a");
                         slotTime = daySlotList.get(pos).getSlotTime().split("-")[0].trim();
                    }

                    LocalTime time = LocalTime.parse(slotTime, formatter);

                    LocalDate today = LocalDate.now();

                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM");
                    String formattedDate = today.format(dateFormatter);

                    if (time.isBefore(currentTime)&& formattedDate.contains(date)){

                        AlertDialog.Builder alert = new AlertDialog.Builder(context);
                        alert.setTitle("Warning");
                        alert.setMessage("Slot time period expired!");
                        alert.setIcon(context.getDrawable(R.drawable.ic_warning_svgrepo_com));
                        alert.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        alert.show();

                    }else {
                        services= new ArrayList<>();
                        salonServicesList = new ArrayList<>();
                    getSalonServices();
                    getCustomerDetails(custKey);
                        Dialog slotBookDialog = new Dialog(context);
                        slotBookDialog.setContentView(R.layout.slot_booking_dialog);
                    AutoCompleteTextView chooseServiceTextView =
                            slotBookDialog.findViewById(R.id.chooseService);
                    RecyclerView serviceRecyclerView = slotBookDialog.findViewById(R.id.serviceView);

                    ArrayAdapter<String> suggestionServiceAdapter = new ArrayAdapter<>(context,
                            android.R.layout.simple_dropdown_item_1line, services);
                    chooseServiceTextView.setAdapter(suggestionServiceAdapter);

                    ArrayList<SalonServiceModel> clientService = new ArrayList<>();
                    serviceAdapter = new ServiceAdapter(context,clientService);
                    serviceRecyclerView.setAdapter(serviceAdapter);
                    serviceRecyclerView.setLayoutManager(new LinearLayoutManager(context));

                    chooseServiceTextView.setOnItemClickListener((parent, v, index, id) -> {

                        String selectedService = suggestionServiceAdapter.getItem(index);
                        if (selectedService != null) {
                            if (!checkServiceExist(clientService, selectedService)) {
                                for (SalonServiceModel salonService : salonServicesList) {
                                    if (salonService.getService_name().equals(selectedService)) {
                                        clientService.add(salonService);
                                        serviceAdapter.getSelectedServices().add(salonService);
                                        serviceAdapter.notifyDataSetChanged();
                                    }
                                }
                                chooseServiceTextView.setText("");
                            }
                        }
                    });

                    slotBookDialog.findViewById(R.id.dialogBookSlotBtn).setOnClickListener(v -> {
                        ArrayList<SalonServiceModel> selectedService = serviceAdapter.getSelectedServices();
                        ArrayList<String> selServ = new ArrayList<>();
                        for (SalonServiceModel model : selectedService) {
                            selServ.add(model.getService_name());
                        }

                        SalonSlotsModel model = new SalonSlotsModel(daySlotList.get(slotPosition).getSlotTime(),
                                selServ, custKey);
                        slotsRef.child(dayName).child("slotList").child(String.valueOf(slotPosition)).setValue(model);

                        slotBookDialog.dismiss();
                        notifySalon(daySlotList.get(slotPosition).getSlotTime());
                    });

                    slotBookDialog.show();
                    slotBookDialog.findViewById(R.id.bookSlotClose).setOnClickListener(vv -> slotBookDialog.dismiss());
                    }
                    });
            }
        }
    }

    private void getSlotCustomerDetails(String customerKey) {

        customerRef.child(customerKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                CustomerModel customer = dataSnapshot.getValue(CustomerModel.class);
                customerModel = customer;
                slotName.setText(customer.getCustName());
                slotPhone.setText(customer.getCustPhone());
                slotCustomerDialog.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context.getApplicationContext(), "Fail to get data.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean checkServiceExist(ArrayList<SalonServiceModel> clientService,
                                     String selectedService) {
        for (SalonServiceModel model : clientService) {
            if (model.getService_name().equals(selectedService)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {

        return daySlotList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView slotTiming;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            slotTiming = itemView.findViewById(R.id.slotTiming);
        }
    }

    private void getCustomerDetails(String custKey) {
        customerRef.child(custKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                CustomerModel customer = dataSnapshot.getValue(CustomerModel.class);
                customerModel = customer;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context.getApplicationContext(), "Fail to get data.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void notifyCustomer(String customerToken, String customerName, String slotTime) {
        FirebaseMessaging.getInstance().subscribeToTopic("all");

        Toast.makeText(context, "Notification send!", Toast.LENGTH_SHORT).show();
        FCMNotificationSender notificationSender = new FCMNotificationSender(customerToken,
                "Hi," + customerName,
                "You have booked your slot on time \n" + slotTime + " please visit our Salon on " +
                        "time and " +
                        "do your services, \nThank You!",
                context.getApplicationContext(),
                CustomerDashboard.class);

        notificationSender.sendNotifications();
    }

    private void notifySalon(String slotTime) {
        FirebaseMessaging.getInstance().subscribeToTopic("all");

        Toast.makeText(context, "Notification send!", Toast.LENGTH_SHORT).show();
        FCMNotificationSender notificationSender = new FCMNotificationSender(salonInformation.getToken(),
                "Slot Booked",
                customerModel.getCustName() + " have booked slot on\n" + dayName + ", " + date + "\nThe " +
                        "slot" +
                        " timing is " + slotTime + "\nPlease check it!",
                context.getApplicationContext(),
                SalonDashboard.class);

        notificationSender.sendNotifications();
    }

    public void getSalonInformation() {

        salonRef.child("information").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                salonInformation = dataSnapshot.getValue(SalonModel.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getSalonServices() {
        if (servicesRef != null) {
            servicesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    HashMap<String, Array> dataMap = (HashMap<String, Array>) dataSnapshot.getValue();

                    if (dataMap != null) {
                        for (String key : dataMap.keySet()) {
                            servicesRef.child(key).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    HashMap<String, Array> dataMap2 =
                                            (HashMap<String, Array>) snapshot.getValue();
                                    for (String key2 : dataMap2.keySet()) {
                                        servicesRef.child(key).child(key2).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                                SalonServiceModel serviceModel =
                                                        snapshot2.getValue(SalonServiceModel.class);
                                                if (serviceModel != null) {
                                                    serviceModel.setService_type(key);
                                                    serviceModel.setId(key2);
                                                    salonServicesList.add(serviceModel);
                                                    services.add(serviceModel.getService_name());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.d("DB Error : ", error.toString());
                                            }
                                        });
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d("DB Error : ", error.toString());
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Fail to get data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}