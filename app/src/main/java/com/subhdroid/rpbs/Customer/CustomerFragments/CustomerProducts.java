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
import com.subhdroid.rpbs.Salon.SalonMenuFragments.SalonProductModel;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class CustomerProducts extends Fragment {
    RecyclerView customerProductsRecycler;
    ArrayList<SalonProductModel> productsList;
    LottieAnimationView customerProductLoadingAnimation;
    CustomerProductsAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_products, container, false);

        getSalonProducts();
        customerProductLoadingAnimation = view.findViewById(R.id.customerProductLoadingAnimation);
        customerProductLoadingAnimation.setVisibility(View.VISIBLE);
        customerProductsRecycler = view.findViewById(R.id.customerProductsRecycler);
        customerProductsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    public void loadProducts() {
        customerProductLoadingAnimation.setVisibility(View.VISIBLE);
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            customerProductLoadingAnimation.setVisibility(View.GONE);
            adapter = new CustomerProductsAdapter(getContext(), productsList);
            customerProductsRecycler.setAdapter(adapter);
        }, 1000);

    }


    private void getSalonProducts() {
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("products");
        if (productRef != null) {
            productRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    HashMap<String, Array> dataMap = (HashMap<String, Array>) dataSnapshot.getValue();
                    if (dataMap != null) {
                        productsList = new ArrayList<>();
                        final int[] i = {0};
                        for (String key : dataMap.keySet()) {
                            productRef.child(key).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    SalonProductModel salonProducts =
                                            snapshot.getValue(SalonProductModel.class);

                                    if (salonProducts != null) {
                                        salonProducts.setId(key);
                                        productsList.add(salonProducts);
                                    }
                                    i[0]++;
                                    if (i[0] == dataMap.keySet().size()) {
                                        loadProducts();
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