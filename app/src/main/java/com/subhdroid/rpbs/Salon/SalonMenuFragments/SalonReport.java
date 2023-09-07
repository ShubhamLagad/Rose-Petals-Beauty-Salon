package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.subhdroid.rpbs.R;
import com.subhdroid.rpbs.Salon.SalonDashboard;

import java.sql.Array;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SalonReport extends Fragment {
    ArrayList<ReportModel> weekdayReportList;
    BarChart weekBarChart;
    LottieAnimationView weekLoading;
    DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("transactions");
    BarDataSet weekBarDataSet;
    TextView weekIncome, productCount, serviceCount, customerCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_salon_report, container, false);
        SalonDashboard.removeToolbarChild();
        weekBarChart = view.findViewById(R.id.weekBarChart);
        weekIncome = view.findViewById(R.id.weekIncome);
        weekLoading = view.findViewById(R.id.weekLoading);
        customerCount = view.findViewById(R.id.customerCount);
        serviceCount = view.findViewById(R.id.serviceCount);
        productCount = view.findViewById(R.id.productCount);
        weekLoading.setVisibility(View.VISIBLE);

        LocalDate today = LocalDate.now();
        weekdayReportList = new ArrayList<>();
        for (int i = 7; i >= 1; i--) {
            LocalDate date = today.minusDays(i);
            weekdayReportList.add(new ReportModel(date.getDayOfWeek().toString().substring(0, 3),
                    date.format(DateTimeFormatter.ofPattern("dd,MMM YYYY")), 0));
            if (i == 1) {
                getReports();
                getProductCount();
                getServiceCount();
                getCustomerCount();
            }
        }

        return view;
    }


    public void loadWeekReport() {
        weekIncome.setText(String.valueOf(ReportModel.getWeekIncome()));
        weekLoading.setVisibility(View.GONE);
        weekBarChart.setVisibility(View.VISIBLE);
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        final ArrayList<String> weekXAxisLabel = new ArrayList<>();
        int i = 0;
        for (ReportModel model : weekdayReportList) {
            BarEntry barentry = new BarEntry(i++, model.getCount());
            barEntries.add(barentry);
            weekXAxisLabel.add(model.getDay());

        }
        weekBarDataSet = new BarDataSet(barEntries, "Customers");
        weekBarDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        weekBarDataSet.setDrawValues(false);
        weekBarChart.animateY(3000);
        weekBarChart.getDescription().setText("Weekly Customer chart");
        weekBarChart.getDescription().setTextColor(R.color.icon_color);

        weekBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(weekXAxisLabel));
        weekBarChart.setData(new BarData(weekBarDataSet));
    }

    public void getReports() {
        ReportModel.weekIncome = 0;
        for (ReportModel reportModel : weekdayReportList) {
            transactionRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    HashMap<String, Array> dataMap2 = (HashMap<String, Array>) snapshot.getValue();
                    final int[] i = {0};
                    for (String custKey : dataMap2.keySet()) {

                        transactionRef.child(custKey).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                HashMap<String, Array> dataMap = (HashMap<String, Array>) snapshot.getValue();
                                final int[] j = {0};
                                i[0]++;
                                for (String key : dataMap.keySet()) {

                                    transactionRef.child(custKey).child(key).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            TransactionModel model =
                                                    snapshot.getValue(TransactionModel.class);
                                            if (model.getDate().contains(reportModel.getDate())) {
                                                reportModel.count++;
                                                ReportModel.weekIncome += Integer.parseInt(model.getExpense());
                                            }
                                            j[0]++;
                                            if (i[0] == dataMap2.keySet().size() && j[0] == dataMap.keySet().size()) {
                                                loadWeekReport();
                                            }
                                            Log.d("Log", "Key : " + model.getDate());
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    public void getProductCount() {
        DatabaseReference prodRef = FirebaseDatabase.getInstance().getReference("products");
        prodRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = (int) snapshot.getChildrenCount();
                Log.d("Log", "Pro : " + count);
                for (int i = 1; i <= count; i++) {
                    int finalI = i;
                    new Handler().postDelayed(() -> productCount.setText(String.valueOf(finalI)), 500);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void getCustomerCount() {
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("customer");
        customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = (int) snapshot.getChildrenCount();
                Log.d("Log", "cust : " + count);
                for (int i = 1; i <= count; i++) {

                    int finalI = i;
                    new Handler().postDelayed(() -> customerCount.setText(String.valueOf(finalI)), 500);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void getServiceCount() {
        DatabaseReference serviceRef = FirebaseDatabase.getInstance().getReference("services");
        serviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, Array> dataMap = (HashMap<String, Array>) snapshot.getValue();
                final int[] count = {0};
                int i = 0;
                for (String key : dataMap.keySet()) {
                    i++;
                    int finalI = i;
                    serviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            count[0] += (int) snapshot.getChildrenCount();
                            Log.d("Log", "serv : " + count[0]);
                            if (finalI == dataMap.keySet().size()) {
                                for (int k = 1; k <= count[0]; k++) {
                                    int finalk = k;
                                    new Handler().postDelayed(() -> serviceCount.setText(String.valueOf(finalk)), 500);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}