package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.subhdroid.rpbs.R;
import com.subhdroid.rpbs.Salon.SalonDashboard;

import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class SalonProducts extends Fragment {
    LottieAnimationView productLoadingAnimation;
    RecyclerView productRecycler;
    EditText addProductName;
    EditText addProductPrice;
    EditText addProductDescription;
    Dialog addProductDialog;
    TextView productWarning;

    StorageReference fileRef =
            FirebaseStorage.getInstance().getReference("ProductImages");
    private final int PICK_IMAGE_REQUEST = 22;
    TextView photoWarn;
    private ArrayList<String> imgUrl ;
    int uploadFlag = 0;
    ArrayList<Uri> filePath ;

    AppCompatButton btnChoose, btnUpload;
    CardView imageCardView;
    Handler handler = new Handler();
    ArrayList<SalonProductModel> salonProductsList;
    RecyclerSalonProductsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_salon_products, container, false);
        productLoadingAnimation = view.findViewById(R.id.productLoadingAnimation);
        productLoadingAnimation.setVisibility(View.VISIBLE);
        productRecycler = view.findViewById(R.id.salonProductsRecyclerView);
        productWarning = view.findViewById(R.id.productWarning);
        getSalonProducts();

        SalonDashboard activity = (SalonDashboard) getActivity();
        Toolbar customToolbar = activity.findViewById(R.id.toolbar);

        SalonDashboard.removeToolbarChild();
        customToolbar.setTitle("Products");
        AppCompatButton addProductBtn = new AppCompatButton(getContext());
        addProductBtn.setText("Add Products");
        addProductBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        addProductBtn.setTextColor(getContext().getColor(R.color.icon_color));
        addProductBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_add_24, 0, 0, 0);
        addProductBtn.setCompoundDrawablePadding(5);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(150, 0, 0, 0);
        addProductBtn.setLayoutParams(layoutParams);
        customToolbar.addView(addProductBtn);

        addProductDialog = new Dialog(getContext());
        addProductDialog.setContentView(R.layout.add_product_dialog);

        addProductBtn.setOnClickListener(view1 -> {
            addProductDialog.show();
            filePath = new ArrayList<>();
            imgUrl = new ArrayList<>();
        });
        addProductName = addProductDialog.findViewById(R.id.addProductName);
        addProductDescription = addProductDialog.findViewById(R.id.addProductDescription);
        addProductPrice = addProductDialog.findViewById(R.id.addProductPrice);

        btnChoose = addProductDialog.findViewById(R.id.addProductChooseBtn);
        btnUpload = addProductDialog.findViewById(R.id.addProductUploadBtn);
        imageCardView = addProductDialog.findViewById(R.id.imageCardView);
        photoWarn = addProductDialog.findViewById(R.id.photoWarn);
        btnChoose.setOnClickListener(v -> SelectImage());
        btnUpload.setOnClickListener(v -> uploadImage());

        addProductDialog.findViewById(R.id.addProductAddBtn).setOnClickListener(view12 -> {
            if (CheckAllFields()) {
                addNewProduct(new SalonProductModel(addProductName.getText().toString(),
                        addProductPrice.getText().toString(),
                        addProductDescription.getText().toString(), imgUrl, "0"));
            }
        });

        addProductDialog.findViewById(R.id.addProductClose).setOnClickListener(view13 -> addProductDialog.dismiss());
        productRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2));

        return view;
    }

    private void addNewProduct(SalonProductModel newProduct) {
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("products");
        String productID = productRef.push().getKey();
        SalonProductModel productModel = new SalonProductModel(newProduct.getProdName(),
                newProduct.getProdPrice(), newProduct.getDescription(), newProduct.getPhotos(),
                newProduct.getReview());

        productRef.child(productID).setValue(productModel);
        addProductName.setText("");
        addProductDescription.setText("");
        addProductPrice.setText("");
        addProductDialog.dismiss();
    }

    private boolean CheckAllFields() {
        if (addProductName.length() == 0) {
            addProductName.setError("Name is required");
            addProductName.requestFocus();
            return false;
        }

        if (addProductPrice.length() == 0) {
            addProductPrice.setError("Price is required");
            addProductPrice.requestFocus();
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

    private void SelectImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri imageUri = clipData.getItemAt(i).getUri();
                    filePath.add(imageUri);
                }
            } else {
                Uri imageUri = data.getData();
                filePath.add(imageUri);
            }

        }
    }

    private void uploadImage() {
        if (filePath.size() > 0) {

            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            int i = 1;
            for (Uri link : filePath) {

                String timeStamp =
                        new SimpleDateFormat("MMHHddmmssyyyy").format(new java.util.Date()) + i++;

                fileRef.child(timeStamp).putFile(link).addOnSuccessListener(taskSnapshot -> {
                    progressDialog.dismiss();

                    fileRef.child(timeStamp).getDownloadUrl().addOnSuccessListener(uri -> {
                        imgUrl.add(uri.toString());
                        Toast.makeText(getContext(), "Image Uploaded", Toast.LENGTH_SHORT).show();
                    });
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                });
            }
            uploadFlag = 1;
            photoWarn.setVisibility(View.INVISIBLE);
        }
    }

    public void loadProducts() {
        productLoadingAnimation.setVisibility(View.VISIBLE);
        handler.postDelayed(() -> {
            productLoadingAnimation.setVisibility(View.GONE);
            adapter =
                    new RecyclerSalonProductsAdapter(getContext(), salonProductsList);
            productRecycler.setAdapter(adapter);
        }, 4000);

    }

    private void getSalonProducts() {
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("products");
        if (productRef != null) {
            productRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    HashMap<String, Array> dataMap = (HashMap<String, Array>) dataSnapshot.getValue();
                    if (dataMap != null) {

                        salonProductsList = new ArrayList<>();
                        final int[] i = {0};
                        for (String key : dataMap.keySet()) {
                            productRef.child(key).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    SalonProductModel salonProducts =
                                            snapshot.getValue(SalonProductModel.class);

                                    if (salonProducts != null) {
                                        salonProducts.setId(key);
                                        salonProductsList.add(salonProducts);
                                    }
                                    i[0]++;
                                    if (i[0] == dataMap.keySet().size()) {
                                        productWarning.setVisibility(View.GONE);
                                        loadProducts();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d("DB Error : ", error.toString());
                                }
                            });
                        }
                    } else {
                        productWarning.setVisibility(View.VISIBLE);
                        productLoadingAnimation.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Fail to get data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}