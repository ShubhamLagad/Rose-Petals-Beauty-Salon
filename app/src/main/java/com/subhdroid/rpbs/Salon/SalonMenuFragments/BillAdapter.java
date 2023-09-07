package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.subhdroid.rpbs.R;

import java.util.ArrayList;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.ViewHolder> {

    private ArrayList<BillModel> billList;

    public BillAdapter(ArrayList<BillModel> billList) {
        this.billList = billList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.bill_table_row, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.srNo.setText(billList.get(position).getSrNo());
        holder.itemName.setText(billList.get(position).getItemName());
        holder.itemPrice.setText(billList.get(position).getPrice());
        holder.itemQuantity.setText(billList.get(position).getQuantity());
        holder.itemTotalPrice.setText(billList.get(position).getTotal());

    }

    @Override
    public int getItemCount() {
        return billList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView srNo, itemName, itemPrice, itemQuantity, itemTotalPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            srNo = itemView.findViewById(R.id.srNo);
            itemName = itemView.findViewById(R.id.itemName);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemQuantity = itemView.findViewById(R.id.itemQuantity);
            itemTotalPrice = itemView.findViewById(R.id.itemTotalPrice);
        }
    }
}
