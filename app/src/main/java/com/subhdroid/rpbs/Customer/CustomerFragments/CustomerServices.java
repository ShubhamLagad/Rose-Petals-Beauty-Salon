package com.subhdroid.rpbs.Customer.CustomerFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.subhdroid.rpbs.R;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.SalonServiceModel;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;


public class CustomerServices extends Fragment {

    Handler handler = new Handler();
    RecyclerView customerServicesRecycler;
    LottieAnimationView customerServiceLoadingAnimation;
    CustomerServiceAdapter serviceAdapter;
    HashMap<String, ArrayList<SalonServiceModel>> salonServicesList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_services, container, false);


        customerServiceLoadingAnimation = view.findViewById(R.id.customerServiceLoadingAnimation);
        customerServiceLoadingAnimation.setVisibility(View.VISIBLE);
        getSalonServices();

        customerServicesRecycler = view.findViewById(R.id.customerServicesRecycler);
        customerServicesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    public void loadServices() {
        handler.postDelayed(() -> {
            customerServiceLoadingAnimation.setVisibility(View.GONE);
            serviceAdapter =
                    new CustomerServiceAdapter(getContext(), salonServicesList);
            customerServicesRecycler.setAdapter(serviceAdapter);
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
                        for (String key : dataMap.keySet()) {
                            servicesRef.child(key).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    HashMap<String, Array> dataMap2 =
                                            (HashMap<String, Array>) snapshot.getValue();
                                    ArrayList<SalonServiceModel> serviceDetails = new ArrayList<>();
                                    if (dataMap2 != null) {

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
                    }
                    loadServices();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Fail to get data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}