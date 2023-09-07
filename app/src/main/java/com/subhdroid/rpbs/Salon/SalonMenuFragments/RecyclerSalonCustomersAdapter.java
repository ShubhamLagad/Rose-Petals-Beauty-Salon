package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.subhdroid.rpbs.Customer.CustomerModel;
import com.subhdroid.rpbs.R;

import java.util.ArrayList;

public class RecyclerSalonCustomersAdapter extends RecyclerView.Adapter<RecyclerSalonCustomersAdapter.ViewHolder> {

    Context context;
    ArrayList<CustomerModel> customersList;
    Dialog smsSendingDialog;

    RecyclerSalonCustomersAdapter(Context context, ArrayList<CustomerModel> customersList) {
        this.context = context;
        this.customersList = customersList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.salon_customer_row, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.rowCustomerNo.setText(String.valueOf(position + 1));
        holder.rowCustomerName.setText(customersList.get(position).getCustName());
        holder.rowCustomerPhone.setText(customersList.get(position).getCustPhone());
        if (customersList.get(position).getFcmToken().length() < 15) {
            holder.rowCustomerUses.setText("Not uses our application!");
        } else {
            holder.usesLL.setVisibility(View.GONE);
        }
        holder.makeRequestBtn.setOnClickListener(view -> {
            sendSMS(customersList.get(position).getCustPhone());
        });

        getLastVisit(holder.visitedAnimation, holder.rowCustomerVisit,
                customersList.get(position).getId());
    }

    private void sendSMS(String customerPhone) {
        smsSendingDialog = new Dialog(context);
        smsSendingDialog.setContentView(R.layout.sms_sending);
        AppCompatButton smsOkBtn = smsSendingDialog.findViewById(R.id.smsOkBtn);
        smsOkBtn.setVisibility(View.VISIBLE);
        smsOkBtn.setOnClickListener(view -> smsSendingDialog.dismiss());
        smsSendingDialog.show();
        new Handler().postDelayed(() -> {
            String message = "Hi Dear,\nWe are requesting you install" +
                    " our app " +
                    "and enjoy the best services of the Salon.\nLink:https://bit.ly/rpbp" +
                    "\n\nRose Petals Beauty Salon!";
            String message2 = "You are an existing customer of our salon.\n" +
                    "\n" +
                    "Your login credentials:\n" +
                    "Username: " + customerPhone + "\n" +
                    "Password: rpbs@2023\n" +
                    "\n" +
                    "Please update your profile after login.";
            try {
                SmsManager smsManager = SmsManager.getDefault();
                SmsManager smsManager2 = SmsManager.getDefault();
                smsManager.sendTextMessage(customerPhone, null, message, null, null);
                smsManager2.sendTextMessage(customerPhone, null, message2, null, null);
                Toast.makeText(context, "SMS sent successfully.", Toast.LENGTH_SHORT).show();
            } catch (SecurityException e) {
                Toast.makeText(context, "SMS sending failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (IllegalArgumentException e) {
                Toast.makeText(context, "SMS sending failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, "SMS sending failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            TextView smsMSG = smsSendingDialog.findViewById(R.id.sendingMsg);
            smsMSG.setText("Successfully send!!");
            smsMSG.setTextColor(context.getColor(R.color.success));
        }, 3000);


    }

    @Override
    public int getItemCount() {
        return customersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView rowCustomerUses, rowCustomerVisit, rowCustomerPhone, rowCustomerNo, rowCustomerName;
        AppCompatButton makeRequestBtn;
        LinearLayout usesLL;
        LottieAnimationView visitedAnimation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rowCustomerName = itemView.findViewById(R.id.rowCustomerName);
            rowCustomerNo = itemView.findViewById(R.id.rowCustomerNo);
            rowCustomerPhone = itemView.findViewById(R.id.rowCustomerPhone);
            rowCustomerVisit = itemView.findViewById(R.id.rowCustomerVisit);
            rowCustomerUses = itemView.findViewById(R.id.rowCustomerUses);
            makeRequestBtn = itemView.findViewById(R.id.makeRequestBtn);
            usesLL = itemView.findViewById(R.id.usesLL);
            visitedAnimation = itemView.findViewById(R.id.visitedAnimation);
        }
    }


    public void getLastVisit(LottieAnimationView visitedAnimation, TextView txtView, String key) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(
                "transactions").child(key);

        Query query = databaseReference.orderByChild("date").limitToLast(1);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        TransactionModel model = snapshot.getValue(TransactionModel.class);
                        new Handler().postDelayed(() -> {
                            visitedAnimation.setVisibility(View.GONE);
                            txtView.setVisibility(View.VISIBLE);
                            txtView.setText("Last visited on " + model.getDate());
                        }, 2000);

                    }
                }else {
                    visitedAnimation.setVisibility(View.GONE);
                    txtView.setVisibility(View.VISIBLE);
                    txtView.setText("Not visited yet ");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
            }
        });

    }
}
