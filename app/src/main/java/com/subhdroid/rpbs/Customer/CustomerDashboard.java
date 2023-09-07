package com.subhdroid.rpbs.Customer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.subhdroid.rpbs.Customer.CustomerFragments.CustomerHome;
import com.subhdroid.rpbs.R;

public class CustomerDashboard extends AppCompatActivity {
    public static TextView backBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);
        loadFragment(new CustomerHome());
        backBtn = findViewById(R.id.backBtn);
        backBtn.setVisibility(View.GONE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment(new CustomerHome());
                backBtn.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onBackPressed() {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(CustomerDashboard.this);
            alertDialog.setTitle("Exit");
            alertDialog.setMessage("Do you want to exit app?");

            alertDialog.setPositiveButton("Yes", (dialogInterface, i) -> finishAffinity());

            alertDialog.setNeutralButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
            alertDialog.show();
    }

    public void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.customerFragmentContainer, fragment);
        ft.commit();
    }

}