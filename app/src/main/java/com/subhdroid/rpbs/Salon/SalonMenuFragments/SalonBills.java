package com.subhdroid.rpbs.Salon.SalonMenuFragments;

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
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.subhdroid.rpbs.Customer.CustomerFragments.BillRecyclerAdapter;
import com.subhdroid.rpbs.R;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class SalonBills extends Fragment {

    ArrayList<TransactionModel> billList;
    LottieAnimationView salonBillLoading;
    BillRecyclerAdapter adapter;
    RecyclerView salonBillRecycler;
    TextView billWarning;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_salon_bills, container, false);
        getCustomerBills();
        billWarning = view.findViewById(R.id.billWarning);
        salonBillLoading = view.findViewById(R.id.salonBillLoading);
        salonBillLoading.setVisibility(View.VISIBLE);

        salonBillRecycler = view.findViewById(R.id.salonBillRecycler);
        salonBillRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    private void getCustomerBills() {
        DatabaseReference tranRef =
                FirebaseDatabase.getInstance().getReference("transactions");

        tranRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Array> dataMap = (HashMap<String, Array>) dataSnapshot.getValue();
                if (dataMap != null) {
                    final int[] i = {0};
                    billList = new ArrayList<>();
                    for (String key : dataMap.keySet()) {
                        tranRef.child(key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                HashMap<String, Array> dataMap2 =
                                        (HashMap<String, Array>) snapshot.getValue();
                                if (dataMap2 != null) {
                                    i[0]++;
                                    final int[] j = {0};
                                    for (String key2 : dataMap2.keySet()) {
                                        tranRef.child(key).child(key2).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                                if (snapshot2.exists()){
                                                    TransactionModel transaction =
                                                            snapshot2.getValue(TransactionModel.class);
                                                        transaction.setId(key2);
                                                        transaction.setCustKey(key);
                                                        billList.add(transaction);
                                                        j[0]++;
                                                        if (i[0] == dataMap.keySet().size() && j[0] == dataMap2.keySet().size()) {
                                                            loadBills();
                                                        }
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
                                Log.d("DB Error : ", error.toString());
                            }
                        });
                    }
                } else {
                    billWarning.setVisibility(View.VISIBLE);
                    salonBillLoading.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void loadBills() {
        salonBillLoading.setVisibility(View.VISIBLE);
        billWarning.setVisibility(View.GONE);
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            salonBillLoading.setVisibility(View.GONE);
            adapter = new BillRecyclerAdapter(getContext(), billList);
            salonBillRecycler.setAdapter(adapter);
        }, 1000);
    }
}