package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.subhdroid.rpbs.Customer.CustomerModel;
import com.subhdroid.rpbs.R;
import com.subhdroid.rpbs.Salon.SalonDashboard;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class SalonCustomers extends Fragment {

    ArrayList<CustomerModel> customersList;
    DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("customer");
    RecyclerView recyclerSalonCustomersView;
    LottieAnimationView loadingAnimation;
    RecyclerSalonCustomersAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_salon_customers, container, false);

        Dialog addCustomerDialog = new Dialog(getContext());
        addCustomerDialog.setContentView(R.layout.add_customer_dialog);
        getCustomers();

        SalonDashboard.removeToolbarChild();
        loadingAnimation = view.findViewById(R.id.loadingAnimation);
        loadingAnimation.setVisibility(View.VISIBLE);

        TextView addCustomerClose = addCustomerDialog.findViewById(R.id.addCustomerClose);
        addCustomerClose.setOnClickListener(view1 -> addCustomerDialog.dismiss());

        AppCompatButton newCustomerAddBtn = addCustomerDialog.findViewById(R.id.newCustomerAddBtn);

        Dialog addCustomerSuccessDialog = new Dialog(getContext());
        addCustomerSuccessDialog.setContentView(R.layout.new_customer_added_succes_dialog);
        addCustomerSuccessDialog.setCancelable(false);

        newCustomerAddBtn.setOnClickListener(view12 -> {
            addCustomerDialog.dismiss();
            addCustomerSuccessDialog.show();
        });

        AppCompatButton customerAddedOkBtn =
                addCustomerSuccessDialog.findViewById(R.id.customerAddedOkBtn);
        customerAddedOkBtn.setOnClickListener(view13 -> addCustomerSuccessDialog.dismiss());

        recyclerSalonCustomersView = view.findViewById(R.id.recyclerSalonCustomers);
        recyclerSalonCustomersView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    private void loadCustomers() {
        loadingAnimation.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> {
            loadingAnimation.setVisibility(View.GONE);
            adapter =
                    new RecyclerSalonCustomersAdapter(getContext(), customersList);
            recyclerSalonCustomersView.setAdapter(adapter);
        }, 2000);
    }

    private void getCustomers() {

        if (customerRef != null) {
            customerRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    HashMap<String, Array> dataMap = (HashMap<String, Array>) dataSnapshot.getValue();
                    if (dataMap != null) {
                        final int[] i = {0};
                        customersList = new ArrayList<>();
                        for (String key : dataMap.keySet()) {
                            customerRef.child(key).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    CustomerModel customer = snapshot.getValue(CustomerModel.class);
                                    i[0]++;
                                    customer.setId(key);
                                    customersList.add(customer);
                                    if (i[0] == dataMap.keySet().size()) {
                                        loadCustomers();
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
                    Toast.makeText(getContext(), "Fail to get data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}