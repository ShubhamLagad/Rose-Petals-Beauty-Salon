package com.subhdroid.rpbs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.subhdroid.rpbs.Customer.CustomerDashboard;
import com.subhdroid.rpbs.Customer.CustomerModel;
import com.subhdroid.rpbs.Salon.SalonDashboard;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;


public class Login extends AppCompatActivity {
    TextView signUpTxt;
    AppCompatButton loginBtn;
    EditText username, password;
    public String customerToken = "";
    private static ArrayList<CustomerModel> customerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getToken();
        getAllCustomers();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        signUpTxt = findViewById(R.id.signUpTxt);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);

        signUpTxt.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, Registration.class);
            startActivity(intent);
        });

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Login.this);
        alertDialog.setTitle("Error");
        alertDialog.setMessage("Invalid credentials?\nPlease try again.");

        alertDialog.setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss());


        loginBtn.setOnClickListener(view -> {

            if (CheckAllFields()) {
                String key = checkValidCustomer(username.getText().toString(),
                        password.getText().toString());
                if (username.getText().toString().contains("salon@gmail.com") & password.getText().toString().contains("salon@2000")) {
                    SharedPreferences pref = getSharedPreferences("Salon", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("salonLoggedIn", true);
                    editor.apply();
                    Intent intent = new Intent(Login.this, SalonDashboard.class);
                    startActivity(intent);
                } else if (key != null) {
                    SharedPreferences pref = getSharedPreferences("Customer", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("CustomerLoggedIn", true);
                    editor.putString("key", key);
                    editor.apply();

                    updateToken(username.getText().toString());
                    Intent intent = new Intent(Login.this, CustomerDashboard.class);
                    startActivity(intent);
                } else {
                    alertDialog.show();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Login.this);
        alertDialog.setTitle("Exit");
        alertDialog.setMessage("Do you want to exit app?");

        alertDialog.setPositiveButton("Yes", (dialogInterface, i) -> finishAffinity());

        alertDialog.setNeutralButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

        alertDialog.show();

    }

    private boolean CheckAllFields() {

        if (username.length() == 0) {
            username.setError("Username is required");
            username.requestFocus();
            return false;
        }
        if (password.length() == 0) {
            password.setError("Password is required");
            password.requestFocus();
            return false;
        }

        return true;
    }

    private String checkValidCustomer(String username, String password) {
        if (customerList.size() != 0) {
            for (CustomerModel customer : customerList) {
                if ((username.equals(customer.getCustEmail())||username.equals(customer.getCustPhone())) && password.equals(customer.getCustPassword())) {
                    return customer.getId();
                }
            }
        }

        return null;
    }


    public void getAllCustomers() {
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("customer");
        customerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Array> dataMap = (HashMap<String, Array>) dataSnapshot.getValue();
                customerList = new ArrayList<>();
                if (dataMap != null) {
                    for (String key : dataMap.keySet()) {
                        customerRef.child(key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                CustomerModel customer = snapshot.getValue(CustomerModel.class);
                                customer.setId(key);
                                customerList.add(customer);
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
                Toast.makeText(Login.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private String getToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d("Log", "Fetching FCM registration token failed",
                                task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    String token = task.getResult();
                    customerToken = token;
                });
        return customerToken;
    }

    private void updateToken(String email) {
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("customer");
        customerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Array> dataMap = (HashMap<String, Array>) dataSnapshot.getValue();
                for (String key : dataMap.keySet()) {

                    customerRef.child(key).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            CustomerModel customer = snapshot.getValue(CustomerModel.class);

                            if (email.equals(customer.getCustEmail())) {
                                customerRef.child(key).child("fcmToken").setValue(customerToken);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d("DB Error : ", error.toString());
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Login.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });


    }
}