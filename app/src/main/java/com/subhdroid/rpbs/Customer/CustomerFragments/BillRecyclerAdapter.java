package com.subhdroid.rpbs.Customer.CustomerFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.subhdroid.rpbs.R;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.TransactionModel;

import java.util.ArrayList;


public class BillRecyclerAdapter extends RecyclerView.Adapter<BillRecyclerAdapter.ViewHolder> {
    Context context;
    ArrayList<TransactionModel> billList;

    public BillRecyclerAdapter(Context context,
                               ArrayList<TransactionModel> billList) {
        this.context = context;
        this.billList = billList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.customer_bill_row, parent, false);
        BillRecyclerAdapter.ViewHolder viewHolder = new BillRecyclerAdapter.ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BillRecyclerAdapter.ViewHolder holder, int position) {

        holder.customerRowBillNo.setText(String.valueOf(position + 1));
        holder.customerRowBillDate.setText(billList.get(position).getDate());
        holder.customerRowBillExpense.setText(billList.get(position).getExpense());
        int pos = position;
        SharedPreferences pref = context.getSharedPreferences("Salon", Context.MODE_PRIVATE);
        if (pref.getBoolean("salonLoggedIn", false)) {
            holder.payBtnCard.setVisibility(View.GONE);
            holder.paymentStatus.setVisibility(View.VISIBLE);
            holder.changePaymentStatus.setVisibility(View.VISIBLE);

            if (billList.get(pos).getPayment().contains("Pay")) {
                holder.paymentStatus.setTextColor(context.getColor(R.color.danger));
                Animation anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(500);
                anim.setStartOffset(20);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(Animation.INFINITE);
                holder.paymentStatus.startAnimation(anim);
            } else {
                holder.paymentStatus.setTextColor(context.getColor(R.color.success));
                holder.paymentStatus.setText(billList.get(pos).getPayment());
                holder.changePaymentStatus.setVisibility(View.GONE);
            }
            holder.changePaymentStatusBtn.setOnClickListener(v -> {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle("Alert");
                alert.setMessage("Please make sure payment is received.");
                alert.setPositiveButton("Received", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    changeStatus(billList.get(pos).getCustKey(), billList.get(pos).getId());
                });
                alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
                alert.show();
            });
        }

        SharedPreferences custPref = context.getSharedPreferences("Customer", Context.MODE_PRIVATE);
        if (custPref.getBoolean("CustomerLoggedIn", false)) {
            if (billList.get(pos).getPayment().contains("Paid")) {
                holder.paymentStatus.setText("Paid");
                holder.paymentStatus.setTextColor(context.getColor(R.color.success));
                holder.payBtnCard.setVisibility(View.GONE);
            } else {
                holder.paymentStatus.setTextColor(context.getColor(R.color.danger));
                Animation anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(500);
                anim.setStartOffset(20);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(Animation.INFINITE);
                holder.paymentStatus.startAnimation(anim);
            }
        }

        holder.viewBillBtn.setOnClickListener(view -> {
            Dialog billViewDialog = new Dialog(context);
            billViewDialog.setContentView(R.layout.bill_viewer);
            ImageView billImage = billViewDialog.findViewById(R.id.billImage);
            Picasso.get().load(billList.get(pos).getBill()).into(billImage);
            billViewDialog.show();
            billViewDialog.findViewById(R.id.dismissBtn).setOnClickListener(v -> billViewDialog.dismiss());
        });
        holder.payBtn.setText(billList.get(position).getPayment());
        holder.payBtn.setOnClickListener(view -> {
            Dialog paymentDialog = new Dialog(context);
            paymentDialog.setContentView(R.layout.payment_dialog);
            TextView amt = paymentDialog.findViewById(R.id.amount);
            amt.setText("Payment Amount: \u20B9" + billList.get(pos).getExpense());
            paymentDialog.findViewById(R.id.googlePayBtn).setOnClickListener(v -> {
                String GOOGLE_PAY_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user";
                Uri uri =
                        new Uri.Builder()
                                .scheme("upi")
                                .authority("pay")
                                .appendQueryParameter("pa", "shubhamlagad2000@okaxis")
                                .appendQueryParameter("pn", "Rose Petals Beauty Salon")
                                .appendQueryParameter("mc", "BCR2DN4TUL6LXRQ4")
                                .appendQueryParameter("tn", "Service pay")
                                .appendQueryParameter("am", billList.get(pos).getExpense())
                                .appendQueryParameter("cu", "INR")
                                .build();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);
                intent.setPackage(GOOGLE_PAY_PACKAGE_NAME);
                context.startActivity(intent);
            });

            paymentDialog.findViewById(R.id.phonePeBtn).setOnClickListener(v -> {
                String PHONE_PAY_PACKAGE_NAME = "com.phonepe.app";
                Uri uri =
                        new Uri.Builder()
                                .scheme("upi")
                                .authority("pay")
                                .appendQueryParameter("pa", "8007878524@ybl")
                                .appendQueryParameter("pn", "Rose Petals Beauty Salon")
                                .appendQueryParameter("tn", "Service pay")
                                .appendQueryParameter("am", billList.get(pos).getExpense())
                                .appendQueryParameter("cu", "INR")
                                .build();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);
                intent.setPackage(PHONE_PAY_PACKAGE_NAME);
                context.startActivity(intent);
            });

            paymentDialog.findViewById(R.id.cancelBtn).setOnClickListener(v -> paymentDialog.dismiss());
            paymentDialog.show();

        });

    }

    private void changeStatus(String custKey, String id) {
        DatabaseReference tranRef =
                FirebaseDatabase.getInstance().getReference("transactions").child(custKey).child(id);
        tranRef.child("payment").setValue("Paid");
    }

    @Override
    public int getItemCount() {
        return billList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView customerRowBillNo, customerRowBillDate, customerRowBillExpense, paymentStatus;
        CardView payBtnCard, changePaymentStatus;
        AppCompatButton payBtn, viewBillBtn, changePaymentStatusBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            customerRowBillNo = itemView.findViewById(R.id.customerRowBillNo);
            customerRowBillDate = itemView.findViewById(R.id.customerRowBillDate);
            customerRowBillExpense = itemView.findViewById(R.id.customerRowBillExpense);
            viewBillBtn = itemView.findViewById(R.id.viewBillBtn);
            payBtnCard = itemView.findViewById(R.id.payBtnCard);
            payBtn = itemView.findViewById(R.id.payBtn);
            paymentStatus = itemView.findViewById(R.id.paymentStatus);
            changePaymentStatus = itemView.findViewById(R.id.changePaymentStatus);
            changePaymentStatusBtn = itemView.findViewById(R.id.changePaymentStatusBtn);
        }
    }
}
