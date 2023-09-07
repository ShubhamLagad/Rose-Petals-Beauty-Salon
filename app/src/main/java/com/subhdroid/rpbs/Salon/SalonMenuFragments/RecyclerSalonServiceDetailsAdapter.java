package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.subhdroid.rpbs.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecyclerSalonServiceDetailsAdapter extends RecyclerView.Adapter<RecyclerSalonServiceDetailsAdapter.ViewHolder> {
    HashMap<String, ArrayList<SalonServiceModel>> salonServiceList;
    Context context;
    RecyclerSalonServicesAdapter adapter;

    RecyclerSalonServiceDetailsAdapter(Context context, HashMap<String,
            ArrayList<SalonServiceModel>> salonServiceList) {
        this.context = context;
        this.salonServiceList = salonServiceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.salon_service_details, parent, false);
        RecyclerSalonServiceDetailsAdapter.ViewHolder viewHolder = new RecyclerSalonServiceDetailsAdapter.ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        List<String> list = new ArrayList<>(salonServiceList.keySet());
        holder.serviceType.setText(list.get(position));
        adapter =
                new RecyclerSalonServicesAdapter(context,
                        salonServiceList.get(list.get(position)));

        holder.recyclerSalonServicesDetails.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerSalonServicesDetails.setAdapter(adapter);
    }


    @Override
    public int getItemCount() {
        return salonServiceList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerSalonServicesDetails;
        TextView serviceType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            serviceType = itemView.findViewById(R.id.serviceType);
            recyclerSalonServicesDetails = itemView.findViewById(R.id.recyclerSalonServicesDetails);
        }
    }
}
