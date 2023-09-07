package com.subhdroid.rpbs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.subhdroid.rpbs.Customer.CustomerModel;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Registration extends AppCompatActivity {
    boolean isAllFieldsChecked = false;
    public String customerToken = "";

    private static ArrayList<String> customerList;
    DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("customer");

    TextView signInTxt;
    EditText custName, custPhone, custEmail, custPassword, custCPassword, custGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Set Salon type
        String[] SalonThreeTypes = {"Men", "Women"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, SalonThreeTypes);
        AutoCompleteTextView textView = findViewById(R.id.custGender);
        textView.setThreshold(3);
        textView.setAdapter(adapter);

        getAllCustomers();
        getToken();

        signInTxt = findViewById(R.id.signInTxt);
        custName = findViewById(R.id.custName);
        custPhone = findViewById(R.id.custPhone);
        custEmail = findViewById(R.id.custEmail);
        custPassword = findViewById(R.id.custPassword);
        custCPassword = findViewById(R.id.custCPassword);
        custGender = findViewById(R.id.custGender);

        AppCompatButton signUpBtn = findViewById(R.id.customerSignInBtn);


        Dialog successDialog = new Dialog(Registration.this);
        successDialog.setContentView(R.layout.registration_success_dialog);
        successDialog.setCancelable(false);

        AlertDialog.Builder alreadyExistsDialog = new AlertDialog.Builder(Registration.this);
        alreadyExistsDialog.setTitle("Alert");
        alreadyExistsDialog.setMessage("Account already exists?");
        alreadyExistsDialog.setCancelable(false);
        alreadyExistsDialog.setPositiveButton("Login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
                Intent intent = new Intent(Registration.this, Login.class);
                startActivity(intent);
            }
        });

        alreadyExistsDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                custEmail.requestFocus();
            }
        });

        signUpBtn.setOnClickListener(view -> {

            isAllFieldsChecked = CheckAllFields();

            if (isAllFieldsChecked) {
                if (checkExistingUser(custEmail.getText().toString())) {
                    alreadyExistsDialog.show();

                } else {
                    insertRecord();
                    successDialog.show();
                }
            }
        });

        AppCompatButton okBtn = successDialog.findViewById(R.id.okBtn);
        okBtn.setOnClickListener(view -> {
            successDialog.dismiss();
            Intent intent = new Intent(Registration.this, Login.class);
            startActivity(intent);
        });

        signInTxt.setOnClickListener(view -> {
            Intent intent = new Intent(Registration.this, Login.class);
            startActivity(intent);
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent intent = new Intent(Registration.this, Login.class);
        startActivity(intent);
    }

    private void insertRecord() {

        CustomerModel customerModel = new CustomerModel(custName.getText().toString(), custPhone.getText().toString(),
                custEmail.getText().toString(), custCPassword.getText().toString(),
                custGender.getText().toString(), getToken());

        String customerID = customerRef.push().getKey();

        customerRef.child(customerID).setValue(customerModel);
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

                    Log.d("Log ", "token in fun : " + token);
                    Toast.makeText(Registration.this, "Success", Toast.LENGTH_SHORT).show();
                });
        return customerToken;
    }


    private boolean CheckAllFields() {
        if (custName.length() == 0) {
            custName.setError("Name is required");
            custName.requestFocus();
            return false;
        }

        if (custPhone.length() == 0) {
            custPhone.setError("Phone is required");
            custPhone.requestFocus();
            return false;
        }

        if (custEmail.length() == 0) {
            custEmail.setError("Email is required");
            custEmail.requestFocus();
            return false;
        }
        if (custPassword.length() == 0) {
            custPassword.setError("Password is required");
            custPassword.requestFocus();
            return false;
        } else if (custPassword.length() < 8) {
            custPassword.setError("Password must be minimum 8 characters");
            custPassword.requestFocus();
            return false;
        }

        if (!((custCPassword.getText().toString()).equals((custCPassword.getText().toString())))) {
            custCPassword.setError("Password must be same");
            custCPassword.requestFocus();
            return false;
        }

        if (custGender.length() == 0 && (custGender.length() != 3 || custGender.length() != 5)) {
            custGender.setError("Gender is required");
            custGender.requestFocus();
            return false;
        }
        return true;
    }


    private boolean checkExistingUser(String username) {
        for (String email : customerList) {
            if (username.equals(email)) {
                return true;
            }
        }
        return false;
    }


    public void getAllCustomers() {
        customerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                customerList = new ArrayList<>();
                HashMap<String, Array> dataMap = (HashMap<String, Array>) dataSnapshot.getValue();
                if (dataMap != null) {
                    for (String key : dataMap.keySet()) {
                        customerRef.child(key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                CustomerModel customer = snapshot.getValue(CustomerModel.class);
                                customerList.add(customer.getCustEmail());
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
                Toast.makeText(Registration.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}