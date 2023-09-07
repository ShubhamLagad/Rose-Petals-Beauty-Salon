package com.subhdroid.rpbs.Salon;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.subhdroid.rpbs.Login;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.Feedbacks;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.SalonBills;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.TodayCustomers;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.SalonCustomers;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.SalonProducts;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.SalonProfile;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.MakeBill;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.SalonReport;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.SalonServices;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.SalonSlots;
import com.subhdroid.rpbs.R;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.SalonSlotsModel;
import com.subhdroid.rpbs.Salon.SalonMenuFragments.SalonWeekModel;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SalonDashboard extends AppCompatActivity {

    public static Toolbar toolbar;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    LinearLayout warning;
    DatabaseReference salonRef = FirebaseDatabase.getInstance().getReference("salon").child(
            "information");
    DatabaseReference slotsRef = FirebaseDatabase.getInstance().getReference("slots");
    Dialog salonInformationDialog;
    EditText salonName, ownerName, phone, email, address;
    TextView openTime, closeTime, photoWarn;
    private final int PICK_IMAGE_REQUEST = 22;
    private ImageView imageView;
    private String imgUrl = "";
    boolean isAllFieldsChecked = false;
    int uploadFlag = 0;
    public String salonToken = "";

    private Uri filePath;

    AppCompatButton btnChoose, btnUpload, fillInfoBtn;
    CardView imageCardView;

    Boolean infoFlag = false;
    LottieAnimationView loadingAnimation;
    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
    Calendar time = Calendar.getInstance();
    ArrayList<SalonSlotsModel> slotList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salon_dashboard);
        getSalonInformation();
        getToken();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        loadingAnimation = findViewById(R.id.loadingAnimation);
        warning = findViewById(R.id.warning);
        fillInfoBtn = findViewById(R.id.fillInfoBtn);

        salonInformationDialog = new Dialog(SalonDashboard.this);
        salonInformationDialog.setContentView(R.layout.salon_infromation);

        fillInfoBtn.setOnClickListener(view -> salonInformationDialog.show());

        salonName = salonInformationDialog.findViewById(R.id.salonName);
        ownerName = salonInformationDialog.findViewById(R.id.ownerName);
        phone = salonInformationDialog.findViewById(R.id.phone);
        email = salonInformationDialog.findViewById(R.id.email);
        address = salonInformationDialog.findViewById(R.id.address);
        // Opening time and closing time
        openTime = salonInformationDialog.findViewById(R.id.openTime);
        closeTime = salonInformationDialog.findViewById(R.id.closeTime);
        photoWarn = salonInformationDialog.findViewById(R.id.photoWarn);


        openTime.setOnClickListener(v -> {

            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(SalonDashboard.this,
                    (view, hourOfDay, minute1) -> {
                        time.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        time.set(Calendar.MINUTE, minute1);
                        time.set(Calendar.SECOND, 0);
                        String formattedTime = timeFormat.format(time.getTime());
                        openTime.setText(formattedTime);

                    }, hour, minute, false);
            timePickerDialog.show();
        });

        closeTime.setOnClickListener(v -> {

            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(SalonDashboard.this,
                    (view, hourOfDay, minute1) -> {
                        time.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        time.set(Calendar.MINUTE, minute1);
                        time.set(Calendar.SECOND, 0);
                        String formattedTime = timeFormat.format(time.getTime());
                        closeTime.setText(formattedTime);
                    }, hour, minute, false);
            timePickerDialog.show();
        });


        btnChoose = salonInformationDialog.findViewById(R.id.btnChoose);
        btnUpload = salonInformationDialog.findViewById(R.id.btnUpload);
        imageView = salonInformationDialog.findViewById(R.id.imgView);
        imageCardView = salonInformationDialog.findViewById(R.id.imageCardView);
        btnChoose.setOnClickListener(view -> SelectImage());
        btnUpload.setOnClickListener(view -> uploadImage());

        Dialog successDialog = new Dialog(SalonDashboard.this);
        successDialog.setContentView(R.layout.new_customer_added_succes_dialog);
        successDialog.setCancelable(false);
        salonInformationDialog.findViewById(R.id.submitBtn).setOnClickListener(view -> {

            isAllFieldsChecked = CheckAllFields();

            if (isAllFieldsChecked) {
                insertRecord();
                warning.setVisibility(View.GONE);
                successDialog.show();
            }
        });
        successDialog.findViewById(R.id.customerAddedOkBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                successDialog.dismiss();
                Intent intent = new Intent(SalonDashboard.this, SalonDashboard.class);
                startActivity(intent);
            }
        });

        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigationView);
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar.setTitle("Dashboard");
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.openDrawer, R.string.closeDrawer);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        drawerLayout.setVisibility(View.GONE);

        if (infoFlag) {
            loadFragment(new SalonReport(), 0);
        }
        if (!infoFlag) {
            warning.setVisibility(View.VISIBLE);
        }
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.salonProfile) {
                loadFragment(new SalonProfile(), 1);
                toolbar.setTitle("Profile");
            } else if (id == R.id.salonCustomerItem) {
                loadFragment(new SalonCustomers(), 1);
                toolbar.setTitle("Customers");
            } else if (id == R.id.salonDashboardItem) {
                loadFragment(new SalonReport(), 1);
                toolbar.setTitle("Dashboard");
            } else if (id == R.id.salonSlotsItem) {
                loadFragment(new SalonSlots(), 1);
                toolbar.setTitle("Time Slots");
            } else if (id == R.id.salonBill) {
                loadFragment(new MakeBill(), 1);
                toolbar.setTitle("Make Bill");
            } else if (id == R.id.salonServiceItem) {
                loadFragment(new SalonServices(), 1);
                toolbar.setTitle("Services");
            } else if (id == R.id.salonTodayCustmer) {
                loadFragment(new TodayCustomers(), 1);
                toolbar.setTitle("Today's Customers");
            } else if (id == R.id.salonProduct) {
                loadFragment(new SalonProducts(), 1);
                toolbar.setTitle("Products");
            }else if (id == R.id.salonTotalBills) {
                loadFragment(new SalonBills(), 1);
                toolbar.setTitle("Bills");
            }else if (id == R.id.salonFeedback) {
                loadFragment(new Feedbacks(), 1);
                toolbar.setTitle("Feedbacks");
            } else if (id == R.id.salonLogout) {
                logOut();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(SalonDashboard.this);
            alertDialog.setTitle("Exit");
            alertDialog.setMessage("Do you want to exit app?");

            alertDialog.setPositiveButton("Yes", (dialogInterface, i) -> finishAffinity());

            alertDialog.setNeutralButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
            alertDialog.show();
        }
    }

    public void loadFragment(Fragment fragment, int flag) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (flag == 0) {
            ft.add(R.id.SalonFragmentContainer, fragment);
        } else {
            ft.replace(R.id.SalonFragmentContainer, fragment);
        }
        ft.commit();
    }


    private void logOut() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SalonDashboard.this);
        alertDialog.setTitle("Logout");
        alertDialog.setMessage("Do you want to logout?");

        alertDialog.setPositiveButton("Yes", (dialogInterface, i) -> {
            SharedPreferences pref = getSharedPreferences("Salon", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("salonLoggedIn", false);
            editor.apply();
            Intent intent = new Intent(SalonDashboard.this, Login.class);
            startActivity(intent);
        });

        alertDialog.setNeutralButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

        alertDialog.show();
    }

    public void getSalonInformation() {

        salonRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SalonModel salonInformation = dataSnapshot.getValue(SalonModel.class);
                loadingAnimation.setVisibility(View.GONE);
                if (salonInformation == null) {
                    salonInformationDialog.show();
                } else {
                    infoFlag = true;
                    warning.setVisibility(View.GONE);
                    drawerLayout.setVisibility(View.VISIBLE);
                    loadFragment(new SalonReport(), 0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void SelectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
                btnChoose.setText("Choose another image");
                imageCardView.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        if (filePath != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
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
                        Toast.makeText(SalonDashboard.this, "Image Uploaded",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(SalonDashboard.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }).addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                progressDialog.setMessage("Uploaded " + (int) progress + "%");
            });

            uploadFlag = 1;
            photoWarn.setVisibility(View.INVISIBLE);
        }
    }


    private void insertRecord() {
        SalonModel SalonModel = new SalonModel(salonName.getText().toString(),
                ownerName.getText().toString(), phone.getText().toString(), email.getText().toString(), openTime.getText().toString(), closeTime.getText().toString(),
                address.getText().toString(), imgUrl, salonToken);

        salonRef.setValue(SalonModel);
        infoFlag = true;


        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            DayOfWeek futureDayOfWeek = date.getDayOfWeek();
            String dayName = futureDayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM");
            String formattedDate = date.format(formatter);

            addNewSlot(createSlot(), dayName, formattedDate);
            if (i == 6) {
                drawerLayout.setVisibility(View.VISIBLE);
                loadFragment(new SalonReport(), 0);
            }
        }

    }

    private boolean CheckAllFields() {
        if (salonName.length() == 0) {
            salonName.setError("Salon name is required");
            salonName.requestFocus();
            return false;
        }
        if (ownerName.length() == 0) {
            ownerName.setError("Pincode is required");
            ownerName.requestFocus();
            return false;
        }

        if (phone.length() == 0) {
            phone.setError("Phone is required");
            phone.requestFocus();
            return false;
        }

        if (email.length() == 0) {
            email.setError("Email is required");
            email.requestFocus();
            return false;
        }

        if (openTime.length() == 0) {
            openTime.setError("Open time is required");
            openTime.requestFocus();
            return false;
        }
        if (closeTime.length() == 0) {
            closeTime.setError("Close time is required");
            closeTime.requestFocus();
            return false;
        }
        if (address.length() == 0) {
            address.setError("Address is required");
            address.requestFocus();
            return false;
        }

        if (uploadFlag == 0) {
            btnChoose.setError("Required");
            photoWarn.setVisibility(View.VISIBLE);
            btnChoose.requestFocus();
            return false;
        }
        return true;
    }

    private String getToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d("Log", "Fetching FCM registration token failed",
                                task.getException());
                        return;
                    }
                    String token = task.getResult();
                    salonToken = token;
                });
        return salonToken;
    }


    public static void removeToolbarChild() {
        int lastIndex = toolbar.getChildCount() - 1;
        if (lastIndex > 1) {
            View lastView = toolbar.getChildAt(lastIndex);
            toolbar.removeView(lastView);
        }
    }


    private void addNewSlot(ArrayList<SalonSlotsModel> slotList, String dayName, String date) {
        SalonWeekModel weekModel = new SalonWeekModel(openTime.getText().toString(),
                closeTime.getText().toString(), "60", "Open",
                date,
                slotList);
        slotsRef.child(dayName.toUpperCase()).setValue(weekModel);
    }

    private ArrayList<SalonSlotsModel> createSlot() {
        ArrayList<SalonSlotsModel> slotList = new ArrayList<>();
        String openTimeString = openTime.getText().toString();
        String closeTimeString = closeTime.getText().toString();
        final String TIME_FORMAT = "hh:mm a";
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);

        try {
            int openHours;
            int openMinutes;

            int closeHours;
            int closeMinutes;
            Date date1 = timeFormat.parse(openTimeString);
            Date date2 = timeFormat.parse(closeTimeString);

            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(date1);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(date2);

            openHours = calendar1.get(Calendar.HOUR_OF_DAY);
            openMinutes = calendar1.get(Calendar.MINUTE);
            closeHours = calendar2.get(Calendar.HOUR_OF_DAY);
            closeMinutes = calendar2.get(Calendar.MINUTE);

            final int TIME_INTERVAL_MINUTES = 60;

            Calendar openingTime = Calendar.getInstance();
            openingTime.set(Calendar.HOUR_OF_DAY, openHours);
            openingTime.set(Calendar.MINUTE, openMinutes);
            openingTime.set(Calendar.SECOND, 0);

            Calendar closingTime = Calendar.getInstance();
            closingTime.set(Calendar.HOUR_OF_DAY, closeHours);
            closingTime.set(Calendar.MINUTE, closeMinutes);
            closingTime.set(Calendar.SECOND, 0);

            Calendar currentTime = openingTime;
            SimpleDateFormat timeFormatter = new SimpleDateFormat(TIME_FORMAT);

            String endTime = timeFormatter.format(closingTime.getTime());
            while (currentTime.before(closingTime)) {
                String timeSlot = timeFormatter.format(currentTime.getTime());
                currentTime.add(Calendar.MINUTE, TIME_INTERVAL_MINUTES);
                String timeSlot2 = timeFormatter.format(currentTime.getTime());

                Date d1 = timeFormatter.parse(timeSlot2);
                Date d2 = timeFormatter.parse(endTime);
                String slotTime = timeSlot + " - " + timeSlot2;
                if (d1.compareTo(d2) <= 0) {
                    ArrayList<String> emptyService = new ArrayList<>();
                    emptyService.add("Other");
                    SalonSlotsModel slotModel = new SalonSlotsModel(slotTime,emptyService,"Not booked yet");
                    slotList.add(slotModel);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return slotList;
    }
}