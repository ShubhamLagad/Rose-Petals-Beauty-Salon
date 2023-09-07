package com.subhdroid.rpbs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;

import com.subhdroid.rpbs.Customer.CustomerDashboard;
import com.subhdroid.rpbs.Salon.SalonDashboard;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent;

                SharedPreferences customerPref = getSharedPreferences("Customer", MODE_PRIVATE);
                SharedPreferences salonPref = getSharedPreferences("Salon", MODE_PRIVATE);

                if (customerPref.getBoolean("CustomerLoggedIn", false)) {

                    intent = new Intent(SplashActivity.this, CustomerDashboard.class);

                } else if (salonPref.getBoolean("salonLoggedIn", false)) {

                    intent = new Intent(SplashActivity.this, SalonDashboard.class);

                }  else {

                    intent = new Intent(SplashActivity.this, Login.class);

                }

                startActivity(intent);
                finish();

            }
        }, 1000);
    }
}