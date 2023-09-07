package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.subhdroid.rpbs.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Feedbacks extends Fragment {
    DatabaseReference feedbackRef = FirebaseDatabase.getInstance().getReference("feedback");

    RecyclerView feedbacksRecycler;
    ArrayList<FeedbackModel> feedbackList;
    TextView feedbackWarning;
    LottieAnimationView feedbackLoading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedbacks, container, false);

        feedbackWarning = view.findViewById(R.id.feedbackWarning);
        feedbackLoading = view.findViewById(R.id.feedbackLoading);
        feedbackLoading.setVisibility(View.VISIBLE);
        feedbacksRecycler = view.findViewById(R.id.feedbacksRecycler);
        feedbacksRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        getFeedbacks();

        return view;
    }

    private void loadFeedbacks() {
        feedbackLoading.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> {
            feedbackLoading.setVisibility(View.GONE);
            FeedbackAdapter adapter = new FeedbackAdapter(getContext(), feedbackList);
            feedbacksRecycler.setAdapter(adapter);
        }, 2000);


    }

    private void getFeedbacks() {
        feedbackRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, Array> hashMap = (HashMap<String, Array>) snapshot.getValue();
                feedbackList = new ArrayList<>();
                if (hashMap != null) {
                    feedbackWarning.setVisibility(View.GONE);
                    final int[] i = {0};
                    for (String key : hashMap.keySet()) {
                        feedbackRef.child(key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                feedbackList.add(snapshot.getValue(FeedbackModel.class));
                                i[0]++;
                                if (i[0] == hashMap.keySet().size()) {
                                    loadFeedbacks();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                } else {
                    feedbackWarning.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}