package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.subhdroid.rpbs.R;

import java.util.ArrayList;

public class RecyclerSalonServicesAdapter extends RecyclerView.Adapter<RecyclerSalonServicesAdapter.ViewHolder> {

    Context context;
    ArrayList<SalonServiceModel> salonServicesArrayList;
    private LinearLayout container;
    private AppCompatButton addButton;
    private AppCompatButton submitButton;
    private ArrayList<String> links;
    EditText details;
    Dialog addDetailsDialog;
    DatabaseReference serviceDetailsRef = FirebaseDatabase.getInstance().getReference(
            "serviceDetails");

    boolean urlFlag = false;

    RecyclerSalonServicesAdapter(Context context,
                                 ArrayList<SalonServiceModel> salonServicesArrayList) {
        this.context = context;
        this.salonServicesArrayList = salonServicesArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.salon_service_row, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        getServiceDetails(salonServicesArrayList.get(position).getId(), position,
                holder.serviceDetailsLL,holder.addDetailsBtn);
        holder.rowServiceName.setText(salonServicesArrayList.get(position).getService_name());
        holder.rowServiceTimePeriod.setText(salonServicesArrayList.get(position).getService_time_period() + " min");
        holder.rowServicePrice.setText("Rs." + salonServicesArrayList.get(position).getPrice());
        holder.rowServiceNo.setText(String.valueOf(position + 1));
        int pos = position;

        holder.rowServiceDeleteBtn.setOnClickListener(view -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle("Delete");
            alertDialog.setMessage("Do you want to delete service?");
            alertDialog.setPositiveButton("Yes", (dialogInterface, i) -> deleteService(salonServicesArrayList.get(pos).getId(),
                    salonServicesArrayList.get(pos).getService_type()));
            alertDialog.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
            alertDialog.show();
        });

        holder.addDetailsBtn.setOnClickListener(v -> showAddDetailsDialog(salonServicesArrayList.get(pos).getId()));

        holder.rowViewDetailsArrowDown.setOnClickListener(v -> {
            holder.serviceDetailsCardView.setVisibility(View.VISIBLE);
            holder.rowViewDetailsArrowDown.setVisibility(View.GONE);
            holder.rowViewDetailsArrowUp.setVisibility(View.VISIBLE);
        });

        holder.rowViewDetailsArrowUp.setOnClickListener(v -> {
            holder.serviceDetailsCardView.setVisibility(View.GONE);
            holder.rowViewDetailsArrowUp.setVisibility(View.GONE);
            holder.rowViewDetailsArrowDown.setVisibility(View.VISIBLE);
        });

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (salonServicesArrayList.get(position).getDetails()!=null){
                holder.rowServiceDetails.setText(salonServicesArrayList.get(position).getDetails());
                int cnt = 0;
                if (salonServicesArrayList.get(position).getVideoLinks()!=null){
                    if(salonServicesArrayList.get(position).getVideoLinks().size()>0){
                        for (String url : salonServicesArrayList.get(position).getVideoLinks()) {

                            TextView textView = new TextView(context);

                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            layoutParams.setMargins(0, 11, 0, 0);
                            textView.setLayoutParams(layoutParams);
                            cnt++;

                            String linkText = "Video " + cnt + ": Click here to watch video.";
                            SpannableString spannableString = new SpannableString(linkText);

                            ClickableSpan clickableSpan = new ClickableSpan() {
                                @Override
                                public void onClick(View widget) {
                                    Uri uri = Uri.parse(url);
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    context.startActivity(intent);
                                }
                            };

                            spannableString.setSpan(clickableSpan, 0, linkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            textView.setText(spannableString);
                            textView.setMovementMethod(LinkMovementMethod.getInstance());

                            holder.rowServiceVideos.addView(textView);
                        }
                    }
                }else {
                    holder.rowServiceVideos.setVisibility(View.GONE);
                }


            }

        }, 2000);
    }

    private void showAddDetailsDialog(String key) {

        addDetailsDialog = new Dialog(context);
        addDetailsDialog.setContentView(R.layout.service_detail_dialog);
        addDetailsDialog.findViewById(R.id.serviceDetailsCloseBtn).setOnClickListener(v -> addDetailsDialog.dismiss());

        container = addDetailsDialog.findViewById(R.id.container);
        addButton = addDetailsDialog.findViewById(R.id.addButton);
        submitButton = addDetailsDialog.findViewById(R.id.submitButton);

        details = addDetailsDialog.findViewById(R.id.details);


        ImageButton deleteLinkBtn = addDetailsDialog.findViewById(R.id.deleteLink);
        deleteLinkBtn.setOnClickListener(v -> deleteLink());

        links = new ArrayList<>();

        addButton.setOnClickListener(v -> addEditText());

        submitButton.setOnClickListener(v -> {
            if (CheckAllFields()) {
                submitDetails(key);
            }
        });

        addDetailsDialog.show();
    }

    private void addEditText() {
        EditText editText = new EditText(context);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        editText.setHint("Enter a link");

        container.addView(editText);
    }

    private void deleteLink() {
        if (container.getChildCount() > 1) {
            container.removeViewAt(container.getChildCount() - 1);
        }
    }

    private void checkValidLinks() {

        for (int i = 0; i < container.getChildCount(); i++) {
            View view = container.getChildAt(i);
            if (view instanceof EditText) {
                EditText editText = (EditText) view;

                String url = editText.getText().toString().trim();
                if(url.length()==0){
                    urlFlag = true;
                }else if (!isValidUrl(url)) {
                    editText.setError("Invalid URL format");
                    urlFlag = false;
                    editText.requestFocus();
                } else {
                    editText.setError(null);
                    urlFlag = true;
                }

            }
        }
    }

    private boolean isValidUrl(String url) {
        return Patterns.WEB_URL.matcher(url).matches();
    }

    private void submitDetails(String key) {
        checkValidLinks();
        if (urlFlag) {

            links.clear();
            for (int i = 0; i < container.getChildCount(); i++) {
                View view = container.getChildAt(i);
                if (view instanceof EditText) {
                    EditText editText = (EditText) view;
                    String link = editText.getText().toString().trim();
                    if (!link.isEmpty()) {
                        links.add(link);
                    }
                }
            }

            uploadToDatabase(links, details.getText().toString(), key);

            for (String link : links) {
                Log.d("Link", link);
            }
        }

    }

    private void uploadToDatabase(ArrayList<String> links, String details, String key) {
        SalonServiceModel model = new SalonServiceModel(details, links);
        serviceDetailsRef.child(key).setValue(model);
        addDetailsDialog.dismiss();
    }


    private boolean CheckAllFields() {
        if (details.length() == 0) {
            details.setError("Details is required");
            details.requestFocus();
            return false;
        }
        return true;
    }


    @Override
    public int getItemCount() {
        return salonServicesArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView rowServiceName, rowServicePrice, rowServiceNo, rowServiceTimePeriod, rowServiceDetails;
        ImageButton rowServiceDeleteBtn, rowViewDetailsArrowUp, rowViewDetailsArrowDown;
        AppCompatButton addDetailsBtn;
        LinearLayout rowServiceVideos,serviceDetailsLL;
        CardView serviceDetailsCardView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rowServiceName = itemView.findViewById(R.id.rowServiceName);
            rowServiceTimePeriod = itemView.findViewById(R.id.rowServiceTimePeriod);
            rowServicePrice = itemView.findViewById(R.id.rowServicePrice);
            rowServiceNo = itemView.findViewById(R.id.rowServiceNo);
            rowServiceDeleteBtn = itemView.findViewById(R.id.rowServiceDeleteBtn);
            addDetailsBtn = itemView.findViewById(R.id.addDetailsBtn);
            rowServiceDetails = itemView.findViewById(R.id.rowServiceDetails);
            rowServiceVideos = itemView.findViewById(R.id.rowServiceVideos);
            rowViewDetailsArrowUp = itemView.findViewById(R.id.rowViewDetailsArrowUp);
            rowViewDetailsArrowDown = itemView.findViewById(R.id.rowViewDetailsArrowDown);
            serviceDetailsCardView = itemView.findViewById(R.id.serviceDetailsCardView);
            serviceDetailsLL = itemView.findViewById(R.id.serviceDetailsLL);
        }
    }


    private void deleteService(String id, String serviceType) {
        DatabaseReference servicesRef =
                FirebaseDatabase.getInstance().getReference("services").child(serviceType).child(id);

        servicesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.getRef().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DB Error : ", error.toString());
            }
        });
    }


    private void getServiceDetails(String key, int position,LinearLayout ll,AppCompatButton btn) {
        serviceDetailsRef.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SalonServiceModel salonServiceDetails =
                        snapshot.getValue(SalonServiceModel.class);
                if (salonServiceDetails!=null){
                    ll.setVisibility(View.VISIBLE);
                    btn.setVisibility(View.GONE);
                    salonServicesArrayList.get(position).setDetails(salonServiceDetails.getDetails());
                    salonServicesArrayList.get(position).setVideoLinks(salonServiceDetails.getVideoLinks());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DB Error : ", error.toString());
            }
        });
    }
}