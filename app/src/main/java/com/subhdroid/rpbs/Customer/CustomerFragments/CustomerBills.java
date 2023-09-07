package com.subhdroid.rpbs.Customer.CustomerFragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.subhdroid.rpbs.R;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.TransactionModel;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;


public class CustomerBills extends Fragment {

    ArrayList<TransactionModel> billList;
    LottieAnimationView customerBillLoadingAnimation;
    BillRecyclerAdapter adapter;
    RecyclerView customerBillRecycler;
    TextView billWarning;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_bills, container, false);

        SharedPreferences pref = getContext().getSharedPreferences("Customer", Context.MODE_PRIVATE);
        String custKey = pref.getString("key", "customer");
        getCustomerBills(custKey);
        billWarning = view.findViewById(R.id.billWarning);
        customerBillLoadingAnimation = view.findViewById(R.id.customerBillLoadingAnimation);
        customerBillLoadingAnimation.setVisibility(View.VISIBLE);

        customerBillRecycler = view.findViewById(R.id.customerBillRecycler);
        customerBillRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    private void getCustomerBills(String key) {
        DatabaseReference tranRef =
                FirebaseDatabase.getInstance().getReference("transactions").child(key);

        if (tranRef != null) {
            tranRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    HashMap<String, Array> dataMap = (HashMap<String, Array>) dataSnapshot.getValue();
                    if (dataMap != null) {
                        billList = new ArrayList<>();
                        final int[] i = {0};
                        for (String key : dataMap.keySet()) {
                            tranRef.child(key).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    TransactionModel transaction =
                                            snapshot.getValue(TransactionModel.class);

                                    if (transaction != null) {
                                        transaction.setId(key);
                                        billList.add(transaction);
                                    }
                                    i[0]++;
                                    if (i[0] == dataMap.keySet().size()) {
                                        loadBills();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });

                        }
                    } else {
                        billWarning.setVisibility(View.VISIBLE);
                        customerBillLoadingAnimation.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Fail to get data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadBills() {
        customerBillLoadingAnimation.setVisibility(View.VISIBLE);
        billWarning.setVisibility(View.GONE);
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            customerBillLoadingAnimation.setVisibility(View.GONE);
            adapter = new BillRecyclerAdapter(getContext(), billList);
            customerBillRecycler.setAdapter(adapter);
        }, 1000);
    }
}