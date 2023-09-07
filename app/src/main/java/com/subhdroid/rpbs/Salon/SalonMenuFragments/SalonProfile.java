package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.subhdroid.rpbs.R;
import com.subhdroid.rpbs.Salon.SalonDashboard;
import com.subhdroid.rpbs.Salon.SalonModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class SalonProfile extends Fragment {
    Handler handler = new Handler();
    EditText esalonName, eownerName, ephone, eemail, eaddress;
    TextView topenTime, tcloseTime, photoWarn;
    DatabaseReference salonRef = FirebaseDatabase.getInstance().getReference("salon").child(
            "information");
    SalonModel salon = new SalonModel();
    LinearLayout salonInformation;
    LottieAnimationView profileLoadingAnimation;
    AppCompatButton informationUpdateBtn;
    Dialog salonInformationDialog;
    AppCompatButton btnChoose, btnUpload, fillInfoBtn;
    CardView imageCardView;
    private final int PICK_IMAGE_REQUEST = 22;
    private ImageView imageView;
    private String imgUrl = "";
    boolean isAllFieldsChecked = false;
    private Uri filePath;
    int uploadFlag = 0;
    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
    Calendar time = Calendar.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_salon_profile, container, false);
        getSalonData();
        salonInformation = view.findViewById(R.id.salonInformation);
        profileLoadingAnimation = view.findViewById(R.id.profileLoadingAnimation);
        informationUpdateBtn = view.findViewById(R.id.informationUpdateBtn);
        SalonDashboard.removeToolbarChild();
        TextView salonName = view.findViewById(R.id.salonName);
        TextView salonOwner = view.findViewById(R.id.ownerName);
        TextView salonEmail = view.findViewById(R.id.salonEmail);
        TextView salonAddress = view.findViewById(R.id.salonAddress);
        TextView openTime = view.findViewById(R.id.openTime);
        TextView closeTime = view.findViewById(R.id.closeTime);
        TextView salonMobileNo = view.findViewById(R.id.salonMobileNo);
        ImageView salonImage = view.findViewById(R.id.salonImage);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                salonInformation.setVisibility(View.VISIBLE);
                profileLoadingAnimation.setVisibility(View.GONE);

                salonName.setText(": " + salon.getSalonName());
                salonOwner.setText(": " + salon.getOwnerName());
                salonMobileNo.setText(": " + salon.getPhone());
                salonAddress.setText(": " + salon.getAddress());
                openTime.setText(": " + salon.getOpeningTime());
                closeTime.setText(": " + salon.getClosingTime());
                salonEmail.setText(": " + salon.getEmail());
                imgUrl = salon.getPhoto();
                Picasso.get().load(salon.getPhoto()).into(salonImage);
            }
        }, 5000);

        salonInformationDialog = new Dialog(view.getContext());
        salonInformationDialog.setContentView(R.layout.salon_infromation);

        esalonName = salonInformationDialog.findViewById(R.id.salonName);
        eownerName = salonInformationDialog.findViewById(R.id.ownerName);
        ephone = salonInformationDialog.findViewById(R.id.phone);
        eemail = salonInformationDialog.findViewById(R.id.email);
        eaddress = salonInformationDialog.findViewById(R.id.address);
        // Opening time and closing time
        topenTime = salonInformationDialog.findViewById(R.id.openTime);
        tcloseTime = salonInformationDialog.findViewById(R.id.closeTime);
        photoWarn = salonInformationDialog.findViewById(R.id.photoWarn);
        btnChoose = salonInformationDialog.findViewById(R.id.btnChoose);
        btnUpload = salonInformationDialog.findViewById(R.id.btnUpload);
        imageView = salonInformationDialog.findViewById(R.id.imgView);
        imageCardView = salonInformationDialog.findViewById(R.id.imageCardView);

        informationUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salonInformationDialog.show();
                handler.postDelayed(() -> {
                    esalonName.setText(salon.getSalonName());
                    eownerName.setText(salon.getOwnerName());
                    ephone.setText(salon.getPhone());
                    eaddress.setText(salon.getAddress());
                    topenTime.setText(salon.getOpeningTime());
                    tcloseTime.setText(salon.getClosingTime());
                    eemail.setText(salon.getEmail());
                    imageCardView.setVisibility(View.VISIBLE);
                    Picasso.get().load(salon.getPhoto()).into(imageView);
                },2);
            }
        });

        salonInformationDialog.findViewById(R.id.submitBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CheckAllFields()){
                    updateSalonInformation();
                    salonInformationDialog.dismiss();
                    startActivity(new Intent(getContext(),SalonDashboard.class));
                }
            }
        });

        topenTime.setOnClickListener(v -> {

            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(view.getContext(),
                    (view2, hourOfDay, minute1) -> {

                        time.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        time.set(Calendar.MINUTE, minute1);
                        time.set(Calendar.SECOND, 0);
                        String formattedTime = timeFormat.format(time.getTime());
                        topenTime.setText(formattedTime);

                    }, hour, minute, false);
            timePickerDialog.show();
        });

        tcloseTime.setOnClickListener(v -> {

            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(view.getContext(),
                    (view2, hourOfDay, minute1) -> {
                        time.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        time.set(Calendar.MINUTE, minute1);
                        time.set(Calendar.SECOND, 0);
                        String formattedTime = timeFormat.format(time.getTime());
                        tcloseTime.setText(formattedTime);
                    }, hour, minute, false);
            timePickerDialog.show();
        });



        btnChoose.setOnClickListener(v -> SelectImage());
        btnUpload.setOnClickListener(v -> uploadImage());

        return view;
    }

    private void getSalonData() {

        salonRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SalonModel salonModel = dataSnapshot.getValue(SalonModel.class);
                salon.setSalonName(salonModel.getSalonName());
                salon.setOwnerName(salonModel.getOwnerName());
                salon.setAddress(salonModel.getAddress());
                salon.setClosingTime(salonModel.getClosingTime());
                salon.setOpeningTime(salonModel.getOpeningTime());
                salon.setPhone(salonModel.getPhone());
                salon.setEmail(salonModel.getEmail());
                salon.setPhoto(salonModel.getPhoto());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // Select Image method
    private void SelectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), PICK_IMAGE_REQUEST);
    }


    private void uploadImage() {
        if (filePath != null) {
            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            String timeStamp = new SimpleDateFormat("MMHHddmmssyyyy").format(new java.util.Date());
            StorageReference ref = FirebaseStorage.getInstance().getReference("SalonImages").child(timeStamp);

            ref.putFile(filePath).addOnSuccessListener(taskSnapshot -> {
                progressDialog.dismiss();
                StorageReference storageReference = FirebaseStorage.getInstance().getReference("SalonImages").child(
                        timeStamp);
                Task<Uri> urlTask = storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imgUrl = uri.toString();
                        Toast.makeText(getContext(), "Image Uploaded",
                                Toast.LENGTH_SHORT).show();
//                                    ImageView fimg = findViewById(R.id.fimg);
//                                    Picasso.get().load(imageUrl).into(fimg);
                    }
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }).addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                progressDialog.setMessage("Uploaded " + (int) progress + "%");
            });

            uploadFlag = 1;
            photoWarn.setVisibility(View.INVISIBLE);
        }
    }


    private void updateSalonInformation() {
        try {
            salonRef.child("address").setValue(eaddress.getText().toString());
            salonRef.child("closingTime").setValue(tcloseTime.getText().toString());
            salonRef.child("email").setValue(eemail.getText().toString());
            salonRef.child("openingTime").setValue(topenTime.getText().toString());
            salonRef.child("ownerName").setValue(eownerName.getText().toString());
            salonRef.child("phone").setValue(ephone.getText().toString());
            salonRef.child("photo").setValue(imgUrl.toString());
            salonRef.child("salonName").setValue(esalonName.getText().toString());
        } catch (Exception e) {
            Log.e("Data", "Error updating record: " + e.getMessage());
        }

    }

    private boolean CheckAllFields() {
        if (esalonName.length() == 0) {
            esalonName.setError("Salon name is required");
            esalonName.requestFocus();
            return false;
        }
        if (eownerName.length() == 0) {
            eownerName.setError("Pincode is required");
            eownerName.requestFocus();
            return false;
        }

        if (ephone.length() == 0) {
            ephone.setError("Phone is required");
            ephone.requestFocus();
            return false;
        }

        if (eemail.length() == 0) {
            eemail.setError("Email is required");
            eemail.requestFocus();
            return false;
        }

        if (topenTime.length() == 0) {
            topenTime.setError("Open time is required");
            topenTime.requestFocus();
            return false;
        }
        if (tcloseTime.length() == 0) {
            tcloseTime.setError("Close time is required");
            tcloseTime.requestFocus();
            return false;
        }
        if (eaddress.length() == 0) {
            eaddress.setError("Address is required");
            eaddress.requestFocus();
            return false;
        }
        return true;
    }
}