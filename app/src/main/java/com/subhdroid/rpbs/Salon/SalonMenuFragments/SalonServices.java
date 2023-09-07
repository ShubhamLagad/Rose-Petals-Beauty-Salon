package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.subhdroid.rpbs.R;
import com.subhdroid.rpbs.Salon.SalonDashboard;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class SalonServices extends Fragment {
    Handler handler = new Handler();
    RecyclerView recyclerSalonServices;
    LottieAnimationView serviceLoadingAnimation;
    EditText addServiceName;
    EditText addServicePrice;
    EditText addServiceTimePeriod;
    Dialog addServiceDialog;
    TextView noServiceTxt;
    RecyclerSalonServiceDetailsAdapter serviceAdapter;
    HashMap<String, ArrayList<SalonServiceModel>> salonServicesList;
    ArrayList<String> serviceHeaderList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_salon_services, container, false);

        SalonDashboard.removeToolbarChild();
        SalonDashboard activity = (SalonDashboard) getActivity();
        Toolbar customToolbar = activity.findViewById(R.id.toolbar);
        customToolbar.setTitle("Service");


        SalonDashboard.removeToolbarChild();
        AppCompatButton addServiceBtn = new AppCompatButton(getContext());
        addServiceBtn.setText("Add Service");
        addServiceBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        addServiceBtn.setTextColor(getContext().getColor(R.color.icon_color));
        addServiceBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_add_24, 0, 0, 0);
        addServiceBtn.setCompoundDrawablePadding(5);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(150, 0, 0, 0);
        addServiceBtn.setLayoutParams(layoutParams);
        customToolbar.addView(addServiceBtn);

        serviceLoadingAnimation = view.findViewById(R.id.serviceLoadingAnimation);
        noServiceTxt = view.findViewById(R.id.noServiceTxt);
        serviceLoadingAnimation.setVisibility(View.VISIBLE);
        getSalonServices();


        addServiceDialog = new Dialog(getContext());
        addServiceDialog.setContentView(R.layout.add_service_dialog);

        AutoCompleteTextView addServiceType = addServiceDialog.findViewById(R.id.addServiceType);

        addServiceBtn.setOnClickListener(view1 -> {
            addServiceDialog.show();
            ArrayAdapter<String> adapter = new ArrayAdapter<String>
                    (getContext(), android.R.layout.select_dialog_item, serviceHeaderList);
            addServiceType.setThreshold(1);
            addServiceType.setAdapter(adapter);
        });

        recyclerSalonServices = view.findViewById(R.id.recyclerSalonServices);
        recyclerSalonServices.setLayoutManager(new LinearLayoutManager(getContext()));

        AppCompatButton addServiceAddBtn = addServiceDialog.findViewById(R.id.addServiceAddBtn);

        addServiceAddBtn.setOnClickListener(view12 -> {
            addServiceName = addServiceDialog.findViewById(R.id.addServiceName);
            addServiceTimePeriod = addServiceDialog.findViewById(R.id.addServiceTimePeriod);
            addServicePrice = addServiceDialog.findViewById(R.id.addServicePrice);

            if (CheckAllFields()) {
                addNewService(new SalonServiceModel(addServiceType.getText().toString(),
                        addServiceName.getText().toString(),
                        addServiceTimePeriod.getText().toString(),
                        addServicePrice.getText().toString()));
            }
        });

        addServiceDialog.findViewById(R.id.addServiceClose).setOnClickListener(view13 -> addServiceDialog.dismiss());

        return view;
    }

    public void loadServices() {
        serviceLoadingAnimation.setVisibility(View.VISIBLE);
        handler.postDelayed(() -> {
            serviceLoadingAnimation.setVisibility(View.GONE);
            serviceAdapter =
                    new RecyclerSalonServiceDetailsAdapter(getContext(), salonServicesList);
            recyclerSalonServices.setAdapter(serviceAdapter);
        }, 4000);
    }

    private void getSalonServices() {

        DatabaseReference servicesRef = FirebaseDatabase.getInstance().getReference("services");
        if (servicesRef != null) {
            servicesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    HashMap<String, Array> dataMap = (HashMap<String, Array>) dataSnapshot.getValue();
                    if (dataMap != null) {
                        salonServicesList = new HashMap<>();
                        final int[] i = {0};
                        serviceHeaderList = new ArrayList<>();
                        for (String key : dataMap.keySet()) {
                            serviceHeaderList.add(key);
                            servicesRef.child(key).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    HashMap<String, Array> dataMap2 =
                                            (HashMap<String, Array>) snapshot.getValue();
                                    ArrayList<SalonServiceModel> serviceDetails = new ArrayList<>();
                                    if (dataMap2 != null) {
                                        i[0]++;
                                        final int[] j = {0};
                                        for (String key2 : dataMap2.keySet()) {
                                            servicesRef.child(key).child(key2).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                                    SalonServiceModel serviceModel =
                                                            snapshot2.getValue(SalonServiceModel.class);
                                                    if (serviceModel != null) {
                                                        serviceModel.setService_type(key);
                                                        serviceModel.setId(key2);
                                                        serviceDetails.add(serviceModel);
                                                        j[0]++;
                                                        if (i[0] == dataMap.keySet().size() && j[0] == dataMap2.keySet().size()) {
                                                            loadServices();
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Log.d("DB Error : ", error.toString());
                                                }
                                            });
                                        }
                                        salonServicesList.put(key, serviceDetails);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d("DB Error : ", error.toString());
                                }
                            });
                        }
                    } else {
                        noServiceTxt.setVisibility(View.VISIBLE);
                        serviceLoadingAnimation.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Fail to get data.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            noServiceTxt.setVisibility(View.VISIBLE);
        }
    }


    private void addNewService(SalonServiceModel newService) {
        DatabaseReference serviceRef = FirebaseDatabase.getInstance().getReference("services");
        String serviceID = serviceRef.push().getKey();
        SalonServiceModel serviceModel = new SalonServiceModel(newService.getService_name(),
                newService.getService_time_period(), newService.getPrice());

        serviceRef.child(newService.getService_type()).child(serviceID).setValue(serviceModel);
        addServiceName.setText("");
        addServicePrice.setText("");
        addServiceTimePeriod.setText("");
        addServiceDialog.dismiss();

    }


    private boolean CheckAllFields() {
        if (addServiceName.length() == 0) {
            addServiceName.setError("Name is required");
            addServiceName.requestFocus();
            return false;
        }

        if (addServicePrice.length() == 0) {
            addServicePrice.setError("Price is required");
            addServicePrice.requestFocus();
            return false;
        }
        return true;
    }

}