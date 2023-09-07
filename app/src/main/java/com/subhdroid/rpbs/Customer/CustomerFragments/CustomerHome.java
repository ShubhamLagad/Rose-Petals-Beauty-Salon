package com.subhdroid.rpbs.Customer.CustomerFragments;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.subhdroid.rpbs.Customer.CustomerDashboard;
import com.subhdroid.rpbs.Customer.CustomerModel;
import com.subhdroid.rpbs.Login;
import com.subhdroid.rpbs.R;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.FeedbackModel;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.SalonSlotsModel;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.SalonWeekModel;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.TransactionModel;
import com.subhdroid.rpbs.Salon.SalonModel;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class CustomerHome extends Fragment {
    TextView billCnt;
    CardView cardBillCnt;
    DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("customer");
    DatabaseReference salonRef = FirebaseDatabase.getInstance().getReference("salon").child(
            "information");
    LottieAnimationView salonInfoLoading;
    LinearLayout yourProfile;
    TextView salonName, salonOwnerName, openTime, closeTime, salonMobile, salonEmail, salonAddress;
    ImageView salonImage;
    CardView salonInfo;
    Dialog customerInfoDialog, feedbackDialog;
    EditText customerName, customerEmail, customerPhone, customerPassword, feedbackName, comment;
    ImageButton customerInfoClose, instagram, whatsapp, youtube, feedbackCloseBtn;
    AppCompatButton updateBtn, submitBtn, feedbackSubmitButton;
    ArrayList<SalonSlotsModel> slotList;
    CardView yourBooking;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_customer_home, container, false);
        SharedPreferences pref = getActivity().getSharedPreferences("Customer",
                Context.MODE_PRIVATE);
        String key = pref.getString("key", "key");
        getCustomerBills(key);
        getSalonInformation();
        getYourBooking(key);

        yourBooking = view.findViewById(R.id.yourBooking);
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(700);
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        yourBooking.startAnimation(anim);

        yourBooking.setOnClickListener(v -> {
            Dialog yourSlotDialog = new Dialog(getContext());
            yourSlotDialog.setContentView(R.layout.your_slot_dialog);
            RecyclerView yourSlotRecycler = yourSlotDialog.findViewById(R.id.yourSlotRecycler);

            yourSlotDialog.findViewById(R.id.yourSlotClose).setOnClickListener(vv -> yourSlotDialog.dismiss());
            yourSlotRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

            YourSlotAdapter yourSlotAdapter = new YourSlotAdapter(getContext(), slotList);
            yourSlotRecycler.setAdapter(yourSlotAdapter);

            yourSlotDialog.show();
        });

        view.findViewById(R.id.map).setOnClickListener(v -> {
            String locationUri = "geo:18.95974665307978,74.53335672951056?z=21";
            Uri gmmIntentUri = Uri.parse(locationUri);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Toast.makeText(getContext(), "Google Maps app is not installed.", Toast.LENGTH_SHORT).show();
            }
        });
        salonInfoLoading = view.findViewById(R.id.salonInfoLoading);
        salonInfoLoading.setVisibility(View.VISIBLE);

        instagram = view.findViewById(R.id.instagram);
        whatsapp = view.findViewById(R.id.whatsapp);
        youtube = view.findViewById(R.id.youtube);

        instagram.setOnClickListener(v -> {
            String instagramProfile = "pratibha_palaskr_makeup_artist";
            try {
                getActivity().getPackageManager().getApplicationInfo("com.instagram.android", 0);
                Uri uri = Uri.parse("http://instagram.com/_u/" + instagramProfile);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.instagram.android");
                startActivity(intent);
            } catch (PackageManager.NameNotFoundException e) {
                Uri uri = Uri.parse("http://instagram.com/" + instagramProfile);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }

        });

        youtube.setOnClickListener(v -> {
            String youtubeChannelId = "@palaskarpratibha4006"; // Replace CHANNEL_ID with the actual YouTube channel ID

            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/channel/" + youtubeChannelId));
                intent.setPackage("com.google.android.youtube");
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/channel/" + youtubeChannelId));
                startActivity(intent);
            }

        });

        whatsapp.setOnClickListener(v -> {
            String phoneNumber = "8007878524";

            try {
                PackageManager packageManager = getActivity().getPackageManager();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String url = "https://api.whatsapp.com/send?phone=" + phoneNumber;
                intent.setPackage("com.whatsapp");
                intent.setData(Uri.parse(url));
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Please install whatsapp", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        yourProfile = view.findViewById(R.id.yourProfile);
        salonName = view.findViewById(R.id.salonName);
        salonOwnerName = view.findViewById(R.id.salonOwnerName);
        openTime = view.findViewById(R.id.openTime);
        closeTime = view.findViewById(R.id.closeTime);
        salonMobile = view.findViewById(R.id.salonMobile);
        salonEmail = view.findViewById(R.id.salonEmail);
        salonAddress = view.findViewById(R.id.salonAddress);
        salonImage = view.findViewById(R.id.salonImage);
        salonInfo = view.findViewById(R.id.salonInfo);

        customerInfoDialog = new Dialog(getContext());
        customerInfoDialog.setContentView(R.layout.customer_info_dialog);
        customerName = customerInfoDialog.findViewById(R.id.customerName);
        customerEmail = customerInfoDialog.findViewById(R.id.customerEmail);
        customerPhone = customerInfoDialog.findViewById(R.id.customerPhone);
        customerPassword = customerInfoDialog.findViewById(R.id.customerPassword);
        customerInfoDialog.findViewById(R.id.customerLogout).setOnClickListener(v -> logOut());

        customerInfoClose = customerInfoDialog.findViewById(R.id.customerInfoClose);
        customerInfoClose.setOnClickListener(v -> {
            customerInfoDialog.dismiss();
            submitBtn.setVisibility(View.GONE);
            customerName.setEnabled(false);
            customerEmail.setEnabled(false);
            customerPassword.setEnabled(false);
            customerPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            customerPhone.setEnabled(false);
        });

        submitBtn = customerInfoDialog.findViewById(R.id.submitBtn);
        updateBtn = customerInfoDialog.findViewById(R.id.updateBtn);
        updateBtn.setOnClickListener(v -> {
            submitBtn.setVisibility(View.VISIBLE);
            customerName.setEnabled(true);
            customerEmail.setEnabled(true);
            customerPassword.setEnabled(true);
            customerPassword.setInputType(InputType.TYPE_CLASS_TEXT);
            customerPhone.setEnabled(true);
        });
        submitBtn.setOnClickListener(v -> {

            if (checkInfoFields()) {
                submitBtn.setVisibility(View.GONE);
                customerName.setEnabled(false);
                customerEmail.setEnabled(false);
                customerPassword.setEnabled(false);
                customerPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                customerPhone.setEnabled(false);
                updateCustomerInfo(key);
            }
        });


        feedbackDialog = new Dialog(getContext());
        feedbackDialog.setContentView(R.layout.feedback_dialog);
        feedbackName = feedbackDialog.findViewById(R.id.feedbackName);
        comment = feedbackDialog.findViewById(R.id.comment);
        feedbackSubmitButton = feedbackDialog.findViewById(R.id.feedbackSubmitButton);
        feedbackDialog.findViewById(R.id.feedbackCloseBtn).setOnClickListener(v -> feedbackDialog.dismiss());


        feedbackSubmitButton.setOnClickListener(v -> {
            if (CheckAllFields()) {
                insertFeedback(feedbackName.getText().toString(), comment.getText().toString());
            }
        });

        view.findViewById(R.id.feedback).setOnClickListener(v -> feedbackDialog.show());

        view.findViewById(R.id.products).setOnClickListener(v -> {
            loadFragment(new CustomerProducts());
            CustomerDashboard.backBtn.setVisibility(View.VISIBLE);
        });
        view.findViewById(R.id.services).setOnClickListener(v -> {
            loadFragment(new CustomerServices());
            CustomerDashboard.backBtn.setVisibility(View.VISIBLE);
        });
        billCnt = view.findViewById(R.id.billCnt);
        cardBillCnt = view.findViewById(R.id.cardBillCnt);
        view.findViewById(R.id.billCard).setOnClickListener(v -> {
            loadFragment(new CustomerBills());
            CustomerDashboard.backBtn.setVisibility(View.VISIBLE);
        });


        view.findViewById(R.id.bookSlot).setOnClickListener(v -> {
            loadFragment(new CustomerSlot());
            CustomerDashboard.backBtn.setVisibility(View.VISIBLE);
        });
        view.findViewById(R.id.bookSlotBtn).setOnClickListener(v -> {
            loadFragment(new CustomerSlot());
            CustomerDashboard.backBtn.setVisibility(View.VISIBLE);
        });

        yourProfile.setOnClickListener(v -> {
            getCustomerDetails(key);
        });
        return view;
    }

    private void getYourBooking(String custKey) {
        DatabaseReference slotRef = FirebaseDatabase.getInstance().getReference("slots");
        slotRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, Array> dataMap = (HashMap<String, Array>) snapshot.getValue();
                if (dataMap != null) {
                    slotList = new ArrayList<>();
                    for (String dayName : dataMap.keySet()) {
                        slotRef.child(dayName).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                SalonWeekModel weekModel = snapshot.getValue(SalonWeekModel.class);
                                int pos = 0;
                                for (SalonSlotsModel slotModel : weekModel.getSlotList()) {

                                    if (slotModel.getCustomerKey().contains(custKey)) {
                                        slotModel.setDate(weekModel.getDate());
                                        slotModel.setDayName(dayName);
                                        slotModel.setId(String.valueOf(pos));
                                        slotList.add(slotModel);
                                        yourBooking.setVisibility(View.VISIBLE);
                                    }
                                    pos++;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean checkInfoFields() {
        if (customerName.length() == 0) {
            customerName.setError("Name is required");
            customerName.requestFocus();
            return false;
        }

        if (customerPhone.length() < 10) {
            customerPhone.setError("Phone is required");
            customerPhone.requestFocus();
            return false;
        }

        if (customerEmail.length() == 0) {
            customerEmail.setError("Email is required");
            customerEmail.requestFocus();
            return false;
        }
        if (customerPassword.length() == 0) {
            customerPassword.setError("Password is required");
            customerPassword.requestFocus();
            return false;
        } else if (customerPassword.length() < 8) {
            customerPassword.setError("Password must be minimum 8 characters");
            customerPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void insertFeedback(String name, String comment) {
        DatabaseReference feedbackRef = FirebaseDatabase.getInstance().getReference("feedback");

        FeedbackModel model = new FeedbackModel(name, comment);

        String feedbackID = feedbackRef.push().getKey();

        feedbackRef.child(feedbackID).setValue(model);
        feedbackDialog.dismiss();
        Toast.makeText(getContext(), "Feedback submitted!", Toast.LENGTH_LONG).show();
    }


    private boolean CheckAllFields() {
        if (feedbackName.length() == 0) {
            feedbackName.setError("Name is required");
            feedbackName.requestFocus();
            return false;
        }

        if (comment.length() == 0) {
            comment.setError("Message is required");
            comment.requestFocus();
            return false;
        }
        return true;
    }


    private void updateCustomerInfo(String key) {
        customerRef.child(key).child("custName").setValue(customerName.getText().toString());
        customerRef.child(key).child("custEmail").setValue(customerEmail.getText().toString());
        customerRef.child(key).child("custPassword").setValue(customerPassword.getText().toString());
        customerRef.child(key).child("custPhone").setValue(customerPhone.getText().toString());
        customerInfoDialog.dismiss();
        Toast.makeText(getContext(), "Information updated", Toast.LENGTH_LONG).show();
    }

    private void getCustomerDetails(String key) {
        customerRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                CustomerModel model = snapshot.getValue(CustomerModel.class);
                customerName.setText(model.getCustName());
                customerEmail.setText(model.getCustEmail());
                customerPhone.setText(model.getCustPhone());
                customerPassword.setText(model.getCustPassword());
                customerInfoDialog.show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void loadFragment(Fragment fragment) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.customerFragmentContainer, fragment);
        ft.commit();
    }

    private void logOut() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("Logout");
        alertDialog.setMessage("Do you want to logout?");

        alertDialog.setPositiveButton("Yes", (dialogInterface, i) -> {
            SharedPreferences pref = getActivity().getSharedPreferences("Customer",
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();

            editor.putBoolean("CustomerLoggedIn", false);
            editor.putString("key", "");
            editor.apply();

            Intent intent = new Intent(getContext(), Login.class);
            startActivity(intent);
        });

        alertDialog.setNeutralButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

        alertDialog.show();
    }

    private void getCustomerBills(String key) {
        DatabaseReference tranRef = FirebaseDatabase.getInstance().getReference("transactions").child(key);

        if (tranRef != null) {
            tranRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    HashMap<String, Array> dataMap = (HashMap<String, Array>) dataSnapshot.getValue();
                    if (dataMap != null) {
                        final int[] i = {0};
                        final int[] payBills = {0};
                        for (String key : dataMap.keySet()) {
                            tranRef.child(key).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    TransactionModel transaction =
                                            snapshot.getValue(TransactionModel.class);

                                    if (transaction.getPayment().equals("Pay")) {
                                        payBills[0]++;
                                    }

                                    i[0]++;
                                    if (i[0] == dataMap.keySet().size()) {
                                        new Handler().postDelayed(() -> loadBillsCnt(payBills[0]), 1000);
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

    private void loadBillsCnt(int payBill) {
        cardBillCnt.setVisibility(View.GONE);
        if (payBill > 0) {
            cardBillCnt.setVisibility(View.VISIBLE);
            billCnt.setText(String.valueOf(payBill));

        }
    }

    public void getSalonInformation() {

        salonRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SalonModel salonInformation = dataSnapshot.getValue(SalonModel.class);
                salonInfoLoading.setVisibility(View.GONE);
                salonInfo.setVisibility(View.VISIBLE);
                salonName.setText(salonInformation.getSalonName());
                salonOwnerName.setText(salonInformation.getOwnerName());
                openTime.setText(salonInformation.getOpeningTime());
                closeTime.setText(salonInformation.getClosingTime());
                salonMobile.setText(salonInformation.getPhone());
                salonEmail.setText(salonInformation.getEmail());
                salonAddress.setText(salonInformation.getAddress());
//                Picasso picasso = new Picasso.Builder(getContext())
//                        .downloader(new OkHttp3Downloader(getContext()))
//                        .build();
//                picasso.load(salonInformation.getPhoto())
//                        .resize(800, 0)
//                        .onlyScaleDown()
//                        .into(salonImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}