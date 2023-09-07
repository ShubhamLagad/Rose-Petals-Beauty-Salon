package com.subhdroid.rpbs.Customer.CustomerFragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.subhdroid.rpbs.R;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.SalonServiceModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomerServiceAdapter extends RecyclerView.Adapter<CustomerServiceAdapter.ViewHolder> {
    HashMap<String, ArrayList<SalonServiceModel>> salonServiceList;
    Context context;

    CustomerServiceAdapter(Context context, HashMap<String,
            ArrayList<SalonServiceModel>> salonServiceList) {
        this.context = context;
        this.salonServiceList = salonServiceList;
    }

    @NonNull
    @Override
    public CustomerServiceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.customer_service_details, parent, false);
        CustomerServiceAdapter.ViewHolder viewHolder = new CustomerServiceAdapter.ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerServiceAdapter.ViewHolder holder, int position) {
        List<String> list = new ArrayList<>(salonServiceList.keySet());
        holder.customerServiceType.setText(list.get(position));
        CustomerServiceInnerAdapter adapter =
                new CustomerServiceInnerAdapter(context,
                        salonServiceList.get(list.get(position)));

        holder.customerServicesDetailsRecycler.setLayoutManager(new LinearLayoutManager(context));
        holder.customerServicesDetailsRecycler.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return salonServiceList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerView customerServicesDetailsRecycler;
        TextView customerServiceType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            customerServiceType = itemView.findViewById(R.id.customerServiceType);
            customerServicesDetailsRecycler = itemView.findViewById(R.id.customerServicesDetailsRecycler);
        }
    }
}
