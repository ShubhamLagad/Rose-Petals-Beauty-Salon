package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.subhdroid.rpbs.R;

import java.util.ArrayList;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {

    private ArrayList<SalonServiceModel> services;
    private ArrayList<SalonServiceModel> selectedServices;
    Context context;
    public ServiceAdapter(Context context,ArrayList<SalonServiceModel> services) {
        this.services = services;
        this.context = context;
        selectedServices = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_service, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.serviceName.setText(services.get(position).getService_name());
        holder.serviceRate.setText(services.get(position).getPrice());
        SharedPreferences pref = context.getSharedPreferences("Customer", Context.MODE_PRIVATE);
        if (pref.getBoolean("CustomerLoggedIn", false)) {
            holder.serviceRate.setEnabled(false);
        }
        for (SalonServiceModel service : services) {
            if (service.getService_name().equalsIgnoreCase(services.get(position).getService_name())) {
                holder.checkBox.setChecked(true);
            }
        }

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedServices.add(services.get(position));
            } else {
                selectedServices.remove(services.get(position));
            }
        });

        holder.serviceRate.setOnKeyListener((view, i, keyEvent) -> {
            services.get(position).setPrice(holder.serviceRate.getText().toString());
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public ArrayList<SalonServiceModel> getSelectedServices() {
        return selectedServices;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView serviceName;
        CheckBox checkBox;
        EditText serviceRate;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.serviceName);
            checkBox = itemView.findViewById(R.id.serviceCheckBox);
            serviceRate = itemView.findViewById(R.id.serviceRate);
        }
    }
}
