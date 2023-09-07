package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.subhdroid.rpbs.R;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private ArrayList<SalonProductModel> products;
    private ArrayList<SalonProductModel> selectedProducts;

    public ProductAdapter(ArrayList<SalonProductModel> products) {
        this.products = products;
        selectedProducts = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_product, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.productName.setText(products.get(position).getProdName());
        holder.productPrice.setText(products.get(position).getProdPrice());
        holder.totalPrice.setText(products.get(position).getProdPrice());
        selectedProducts.get(position).setTotalPrice(products.get(position).getProdPrice());
        for (SalonProductModel prod : products) {
            if (prod.getProdName().equalsIgnoreCase(products.get(position).prodName)) {
                holder.checkBox.setChecked(true);
            }
        }
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedProducts.add(products.get(position));
                selectedProducts.get(position).setQuantity(holder.qty.getText().toString());
                selectedProducts.get(position).setTotalPrice(holder.totalPrice.getText().toString());
            } else {
                selectedProducts.remove(products.get(position));
            }
        });
        holder.productPrice.setOnKeyListener((view, i, keyEvent) -> {
            products.get(position).setProdPrice(holder.productPrice.getText().toString());
            int price = Integer.parseInt(holder.productPrice.getText().toString());
            int qnty = Integer.parseInt(holder.qty.getText().toString());
            holder.totalPrice.setText(String.valueOf(price * qnty));
            selectedProducts.get(position).setTotalPrice(holder.totalPrice.getText().toString());
            return false;
        });

        holder.decreaseQty.setOnClickListener(view -> {
            int qtyNo = Integer.parseInt(holder.qty.getText().toString());
            if (qtyNo > 1) {
                holder.qty.setText(String.valueOf(--qtyNo));
                float price = Float.parseFloat(holder.productPrice.getText().toString());
                holder.totalPrice.setText(String.valueOf(price * qtyNo));
                selectedProducts.get(position).setQuantity(holder.qty.getText().toString());
                selectedProducts.get(position).setTotalPrice(holder.totalPrice.getText().toString());
            }
        });
        holder.increaseQty.setOnClickListener(view -> {
            int qtyNo = Integer.parseInt(holder.qty.getText().toString());
            holder.qty.setText(String.valueOf(++qtyNo));
            float price = Float.parseFloat(holder.productPrice.getText().toString());
            holder.totalPrice.setText(String.valueOf(price * qtyNo));
            selectedProducts.get(position).setQuantity(holder.qty.getText().toString());
            selectedProducts.get(position).setTotalPrice(holder.totalPrice.getText().toString());
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public ArrayList<SalonProductModel> getSelectedProducts() {
        return selectedProducts;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName, totalPrice;
        CheckBox checkBox;
        EditText productPrice, qty;
        ImageButton increaseQty, decreaseQty;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            checkBox = itemView.findViewById(R.id.productCheckBox);
            productPrice = itemView.findViewById(R.id.productPrice);
            increaseQty = itemView.findViewById(R.id.increaseQty);
            decreaseQty = itemView.findViewById(R.id.decreaseQty);
            qty = itemView.findViewById(R.id.qty);
            totalPrice = itemView.findViewById(R.id.totalPrice);
        }
    }
}
