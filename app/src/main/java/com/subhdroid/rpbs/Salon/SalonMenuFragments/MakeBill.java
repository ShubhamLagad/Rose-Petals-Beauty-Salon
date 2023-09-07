package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.subhdroid.rpbs.Customer.CustomerModel;
import com.subhdroid.rpbs.R;
import com.subhdroid.rpbs.Salon.SalonDashboard;

import org.apache.commons.text.WordUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MakeBill extends Fragment {
    LottieAnimationView allLoading;
    LinearLayout linearLayout;
    final Handler handler = new Handler();
    final int REQUEST_CODE = 123;
    private static final int REQUEST_SEND_SMS = 124;
    ImageView qrImageView;
    Bitmap qrCode;

    private AutoCompleteTextView productAutoCompleteTextView;
    private AutoCompleteTextView serviceAutoCompleteTextView;
    private RecyclerView productRecyclerView;
    private RecyclerView serviceRecyclerView;
    private ProductAdapter productAdapter;
    private ServiceAdapter serviceAdapter;
    AutoCompleteTextView customerName;
    AutoCompleteTextView customerPhone;
    ArrayList<SalonServiceModel> salonServicesList;
    DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("products");
    DatabaseReference servicesRef = FirebaseDatabase.getInstance().getReference("services");
    DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("customer");

    ArrayList<SalonProductModel> salonProductsList;
    List<String> products = new ArrayList<>();
    List<String> services = new ArrayList<>();
    List<String> custName = new ArrayList<>();
    List<String> custPhone = new ArrayList<>();
    ArrayList<CustomerModel> customers = new ArrayList<>();
    ArrayList<String> selProd = new ArrayList<>();
    ArrayList<String> selServ = new ArrayList<>();
    ArrayList<BillModel> billItemList = new ArrayList<>();
    Dialog smsSendingDialog;
    ProgressDialog submittingDialog;
    String imageUrl = "";
    int totalBill;
    String custKey = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_make_bill, container, false);
        getSalonProducts();
        getCustomers();
        handler.postDelayed(() -> getSalonServices(), 2000);


        SalonDashboard.removeToolbarChild();
        qrImageView = view.findViewById(R.id.paymentQR);
        allLoading = view.findViewById(R.id.allLoading);
        linearLayout = view.findViewById(R.id.linearLayout);
        allLoading.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.GONE);
        smsSendingDialog = new Dialog(getContext());
        smsSendingDialog.setContentView(R.layout.sms_sending);
        smsSendingDialog.setCancelable(false);

//        for product suggestions
        productAutoCompleteTextView = view.findViewById(R.id.productAutoCompleteTextView);
        productRecyclerView = view.findViewById(R.id.productRecyclerView);

        ArrayAdapter<String> suggestionProductAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, products);
        productAutoCompleteTextView.setAdapter(suggestionProductAdapter);

        ArrayList<SalonProductModel> clientProducts = new ArrayList<>();
        productAdapter = new ProductAdapter(clientProducts);
        productRecyclerView.setAdapter(productAdapter);
        productRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        productAutoCompleteTextView.setOnItemClickListener((parent, v, position, id) -> {
            String selectedProduct = suggestionProductAdapter.getItem(position);
            if (selectedProduct != null) {
                if (!checkProductExist(clientProducts, selectedProduct)) {
                    for (SalonProductModel salonProduct : salonProductsList) {
                        if (salonProduct.getProdName().equals(selectedProduct)) {
                            clientProducts.add(salonProduct);
                            productAdapter.getSelectedProducts().add(salonProduct);
                            productAdapter.notifyDataSetChanged();
                        }
                    }
                    productAutoCompleteTextView.setText("");
                }
            }
        });


//        for service suggestions
        serviceAutoCompleteTextView = view.findViewById(R.id.serviceAutoCompleteTextView);
        serviceRecyclerView = view.findViewById(R.id.serviceRecyclerView);

        ArrayAdapter<String> suggestionServiceAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, services);
        serviceAutoCompleteTextView.setAdapter(suggestionServiceAdapter);

        ArrayList<SalonServiceModel> clientService = new ArrayList<>();
        serviceAdapter = new ServiceAdapter(getContext(), clientService);
        serviceRecyclerView.setAdapter(serviceAdapter);
        serviceRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        serviceAutoCompleteTextView.setOnItemClickListener((parent, v, position, id) -> {
            String selectedService = suggestionServiceAdapter.getItem(position);
            if (selectedService != null) {
                if (!checkServiceExist(clientService, selectedService)) {
                    for (SalonServiceModel salonService : salonServicesList) {
                        if (salonService.getService_name().equals(selectedService)) {
                            clientService.add(salonService);
                            serviceAdapter.getSelectedServices().add(salonService);
                            serviceAdapter.notifyDataSetChanged();
                        }
                    }
                    serviceAutoCompleteTextView.setText("");
                }
            }
        });


        Button generateBill = view.findViewById(R.id.generateBill);
        Dialog add_customer_dialog = new Dialog(getContext());
        add_customer_dialog.setContentView(R.layout.add_customer_dialog);

        customerName = add_customer_dialog.findViewById(R.id.newCustomerName);
        customerPhone = add_customer_dialog.findViewById(R.id.newCustomerPhone);

        ArrayAdapter<String> custNameAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, custName);
        customerName.setAdapter(custNameAdapter);

        ArrayAdapter<String> custPhoneAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, custPhone);
        customerPhone.setAdapter(custPhoneAdapter);


        customerName.setOnItemClickListener((parent, v, position, id) -> {
            String selectedName = custNameAdapter.getItem(position);
            if (selectedName != null) {
                for (CustomerModel cust : customers) {
                    if (cust.getCustName().contains(selectedName)) {
                        custKey = cust.getId();
                        customerPhone.setText(cust.getCustPhone());
                    }
                }
            }
        });


        customerPhone.setOnItemClickListener((parent, v, position, id) -> {
            String selectedPhone = custPhoneAdapter.getItem(position);
            if (selectedPhone != null) {
                for (CustomerModel cust : customers) {
                    if (cust.getCustPhone().contains(selectedPhone)) {
                        custKey = cust.getId();
                        customerName.setText(cust.getCustName());
                    }
                }
            }
        });


        AppCompatButton addBtn = add_customer_dialog.findViewById(R.id.newCustomerAddBtn);

        generateBill.setOnClickListener(v -> {
            if (serviceAdapter.getSelectedServices().size() > 0 || productAdapter.getSelectedProducts().size() > 0) {
                add_customer_dialog.show();
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                alertDialog.setTitle("Warning");
                alertDialog.setMessage("Please select services or products");
                alertDialog.setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss());
                alertDialog.show();
            }
        });

        addBtn.setOnClickListener(v -> {
            billItemList = new ArrayList<>();
            if (CheckAllFields(customerName, customerPhone)) {
                add_customer_dialog.dismiss();
                RecyclerView billRecycler = view.findViewById(R.id.billRecycler);
                billRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

                ArrayList<SalonServiceModel> selectedService = serviceAdapter.getSelectedServices();
                ArrayList<SalonProductModel> selectedProducts = productAdapter.getSelectedProducts();
                selProd = new ArrayList<>();
                selServ = new ArrayList<>();
                int index = 0;
                float subTotal = 0;
                for (SalonServiceModel model : selectedService) {
                    index++;
                    billItemList.add(new BillModel(String.valueOf(index), model.getService_name(),
                            model.getPrice(), "-", model.getPrice()));
                    subTotal += Float.parseFloat(model.getPrice());
                    selServ.add(model.getService_name());
                }

                for (SalonProductModel model : selectedProducts) {
                    index++;
                    billItemList.add(new BillModel(String.valueOf(index), model.getProdName(),
                            model.getProdPrice(), model.getQuantity(), model.getTotalPrice()));
                    subTotal += Float.parseFloat(model.getTotalPrice());
                    selProd.add(model.getProdName());

                }
                int tax = 5;
                totalBill = (int) (subTotal + (subTotal * tax / 100));

                BillAdapter billAdapter = new BillAdapter(billItemList);
                billRecycler.setAdapter(billAdapter);

                TextView subTotalTv = view.findViewById(R.id.subTotal);
                subTotalTv.setText("Rs." + subTotal);
                TextView totalTv = view.findViewById(R.id.total);
                totalTv.setText("Rs." + totalBill);

                TextView clientName = view.findViewById(R.id.clientName);
                clientName.setText(WordUtils.capitalizeFully(customerName.getText().toString()));

                String billDateString =
                        new SimpleDateFormat("dd,MMM YYYY").format(new java.util.Date());
                String billTimeString =
                        new SimpleDateFormat("hh:mm a").format(new java.util.Date());
                TextView billDate = view.findViewById(R.id.billDate);
                billDate.setText("Date : " + billDateString);
                TextView billTime = view.findViewById(R.id.billTime);
                billTime.setText("Time : " + billTimeString);

                String transactionNote = "Payment for Salon";
                String qrData = "upi://pay?pa=8007878524@ybl&pn=Rose Petals " +
                        "Beauty Salon&tn=" + transactionNote + "&am=" + totalBill + "&cu=INR";

                qrCode = generateQRCode(qrData, 200, 200);
                qrImageView.setImageBitmap(qrCode);
                view.findViewById(R.id.bill).setVisibility(View.VISIBLE);
                view.findViewById(R.id.actionBtn).setVisibility(View.VISIBLE);
            }
        });

        qrImageView.setOnLongClickListener(view14 -> {
            Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.qr_viewer);
            ImageView iv = dialog.findViewById(R.id.qrViewerImg);
            iv.setImageBitmap(qrCode);
            dialog.show();
            return false;
        });

        add_customer_dialog.findViewById(R.id.addCustomerClose).setOnClickListener(view13 -> add_customer_dialog.dismiss());

        submittingDialog = new ProgressDialog(getContext());
        submittingDialog.setTitle("Submit");
        submittingDialog.setMessage("Submitting 0%");
        submittingDialog.setCancelable(false);

        view.findViewById(R.id.submitBtn).setOnClickListener(view15 -> {
            submittingDialog.show();
            View rootView = getView().getRootView();
            View specificView = rootView.findViewById(R.id.bill);
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                generatePDF(specificView);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        });
        return view;
    }

    private void sendGreetingSMS(String custPhone) {
        String message = "Hi Dear,\nWe provide our best services on our mobile app so " +
                "enjoy it by installing it!\nApp Link:https://bit.ly/rpbp\n\nUsername:" + custPhone + "\nPassword:rpbs@2023";
        TextView smsMSG = smsSendingDialog.findViewById(R.id.sendingMsg);
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(custPhone, null, message, null, null);
            smsMSG.setText("Successfully send!!");
            smsMSG.setTextColor(getContext().getResources().getColor(R.color.success));
        } catch (Exception e) {
            smsMSG.setText("Failed to send sms!");
            smsMSG.setTextColor(getContext().getColor(R.color.danger));
        }
        AppCompatButton smsOkBtn = smsSendingDialog.findViewById(R.id.smsOkBtn);
        smsOkBtn.setVisibility(View.VISIBLE);
        smsOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                smsSendingDialog.dismiss();
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.SalonFragmentContainer, new SalonProducts());
                ft.commit();
            }
        });
    }

    public void generatePDF(View specificView) {
        Bitmap bitmap = Bitmap.createBitmap(specificView.getWidth(), specificView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        specificView.draw(canvas);
        String filePath = Environment.getExternalStorageDirectory().getPath() + "/bill.jpg";
        try {
            FileOutputStream outputStream = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        uploadBill(filePath);
    }

    void uploadBill(String filePath) {
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        String timeStamp =
                new SimpleDateFormat("MMHHddmmssyyyy").format(new java.util.Date());
        final StorageReference storageRef = storage.getReference("SalonBills").child(timeStamp +
                ".jpg");
        final Uri fileUri = Uri.fromFile(new File(filePath));
        final UploadTask uploadTask = storageRef.putFile(fileUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(getContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();

            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                imageUrl = uri.toString();
                if (custKey == "") {
                    submittingDialog.setMessage("Submitted!");
                    submittingDialog.dismiss();
                    addNewCustomer(imageUrl);
                } else {
                    submittingDialog.setMessage("Submitted!");
                    addTransaction(imageUrl, custKey);
                    submittingDialog.dismiss();
                }
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setCancelable(false);
                alert.setTitle("Send SMS");
                alert.setMessage("Are you want to send bill to customer?");
                alert.setPositiveButton("Yes", (dialogInterface, i) ->
                {
                    smsSendingDialog.show();
                    new URLShortenTask().execute(imageUrl);
                });
                alert.setNegativeButton("No", (dialogInterface, i) -> {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.SalonFragmentContainer, new SalonBills());
                    ft.commit();
                });
                alert.show();

            }).addOnFailureListener(e -> {
                submittingDialog.setMessage("Failed to submit.");
                new Handler().postDelayed(() -> submittingDialog.dismiss(), 2000);

            });
        }).addOnFailureListener(e -> {
            submittingDialog.setMessage("Failed to upload bill.");
            new Handler().postDelayed(() -> submittingDialog.dismiss(), 2000);
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            submittingDialog.setMessage("Submitting " + (int) progress + "%");
        });
    }

    public void addNewCustomer(String imageUrl) {
        CustomerModel customerModel = new CustomerModel(customerName.getText().toString(),
                customerPhone.getText().toString(), "email", "rpbs@2023",
                "gender", "Token");

        String customerID = customerRef.push().getKey();
        customerRef.child(customerID).setValue(customerModel);
        addTransaction(imageUrl, customerID);
        sendGreetingSMS(customerPhone.getText().toString());
    }

    public void addTransaction(String bill, String key) {
        DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference(
                "transactions");
        String billDate =
                new SimpleDateFormat("dd,MMM YYYY").format(new java.util.Date());

        TransactionModel transactionModel = new TransactionModel(customerName.getText().toString(),
                customerPhone.getText().toString(), selServ, selProd, String.valueOf(totalBill),
                billDate, bill, "Pay");

        String transactionID = transactionRef.push().getKey();
        transactionRef.child(key).child(transactionID).setValue(transactionModel);
        custKey = "";
    }


    private Bitmap generateQRCode(String data, int width, int height) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height);

            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }


    private class URLShortenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            String apiKey = "f9c186497c7389ea1d7cf3d275dea2188c1c4992";

            try {
                URL apiURL = new URL("https://api-ssl.bitly.com/v4/shorten");
                HttpURLConnection connection = (HttpURLConnection) apiURL.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("long_url", url);

                connection.setDoOutput(true);
                connection.getOutputStream().write(jsonParam.toString().getBytes("UTF-8"));

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.getString("id");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                makeSMSRequest(result);

            } else {
                makeSMSRequest("See on App");
            }
        }
    }

    private void makeSMSRequest(String imageUrl) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.SEND_SMS},
                    REQUEST_SEND_SMS);
        } else {
            sendSMS(imageUrl);
        }
    }

    private void sendSMS(String imageUrl) {
        String phoneNumber = customerPhone.getText().toString();
        String message = "Thank you for visiting the salon! We hope you enjoyed your service" +
                ".\nYour Bill: " + imageUrl + "\nRegards,\nRose Petals Beauty Salon";
        TextView smsMSG = smsSendingDialog.findViewById(R.id.sendingMsg);

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(getContext(), "SMS sent successfully.", Toast.LENGTH_SHORT).show();
            smsMSG.setText("Successfully send!!");
            smsMSG.setTextColor(getContext().getResources().getColor(R.color.success));
        } catch (Exception e) {
            smsMSG.setText("Failed to send sms!");
            smsMSG.setTextColor(getContext().getColor(R.color.danger));
        }

        AppCompatButton smsOkBtn = smsSendingDialog.findViewById(R.id.smsOkBtn);
        smsOkBtn.setVisibility(View.VISIBLE);
        smsOkBtn.setOnClickListener(view -> {
            smsSendingDialog.dismiss();
            FragmentManager fm = getActivity().getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.SalonFragmentContainer, new SalonBills());
            ft.commit();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                View rootView = getView().getRootView();
                View specificView = rootView.findViewById(R.id.bill);
                generatePDF(specificView);
            } else {

            }
        } else if (requestCode == REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean checkProductExist(ArrayList<SalonProductModel> clientProducts,
                                     String selectedProduct) {
        for (SalonProductModel model : clientProducts) {
            if (model.getProdName().equals(selectedProduct)) {
                return true;
            }
        }
        return false;
    }


    public boolean checkServiceExist(ArrayList<SalonServiceModel> clientService,
                                     String selectedService) {
        for (SalonServiceModel model : clientService) {
            if (model.getService_name().equals(selectedService)) {
                return true;
            }
        }
        return false;
    }

    private boolean CheckAllFields(EditText custName, EditText custPhone) {
        if (custName.length() == 0) {
            custName.setError("Name is required");
            custName.requestFocus();
            return false;
        }

        if (custPhone.length() == 0) {
            custPhone.setError("Mobile number is required");
            custPhone.requestFocus();
            return false;
        }
        return true;
    }

    public void loadProductsAndServices() {
        allLoading.setVisibility(View.GONE);
        linearLayout.setVisibility(View.VISIBLE);
    }

    private void getSalonProducts() {
        if (productRef != null) {
            productRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    HashMap<String, Array> dataMap = (HashMap<String, Array>) dataSnapshot.getValue();
                    salonProductsList = new ArrayList<>();
                    if (dataMap != null) {
                        for (String key : dataMap.keySet()) {
                            productRef.child(key).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    SalonProductModel salonProducts =
                                            snapshot.getValue(SalonProductModel.class);
                                    salonProductsList.add(salonProducts);
                                    products.add(salonProducts.getProdName());
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
                    Toast.makeText(getContext(), "Fail to get data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showWarning() {
        allLoading.setVisibility(View.GONE);
        if (salonServicesList.size() == 0 && salonProductsList.size() == 0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Warning");
            alert.setMessage("Please add service or product for generate bill.");
            alert.setIcon(R.drawable.ic_warning_svgrepo_com);
            alert.setPositiveButton("Add Services", (dialogInterface, i) -> {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.SalonFragmentContainer, new SalonServices());
                ft.commit();
            });

            alert.setNegativeButton("Add Products", (dialogInterface, i) -> {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.SalonFragmentContainer, new SalonProducts());
                ft.commit();
            });
            alert.setCancelable(false);
            alert.show();
        } else {
            linearLayout.setVisibility(View.VISIBLE);
            if (salonProductsList.size() == 0) {
                productAutoCompleteTextView.setVisibility(View.GONE);
            }
            if (salonServicesList.size() == 0) {
                serviceAutoCompleteTextView.setVisibility(View.GONE);
            }
        }

    }

    private void getSalonServices() {

        if (servicesRef != null) {
            servicesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    HashMap<String, Array> dataMap = (HashMap<String, Array>) dataSnapshot.getValue();
                    salonServicesList = new ArrayList<>();
                    if (dataMap != null) {
                        final int[] i = {0};
                        for (String key : dataMap.keySet()) {
                            servicesRef.child(key).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    HashMap<String, Array> dataMap2 =
                                            (HashMap<String, Array>) snapshot.getValue();
                                    final int[] j = {0};
                                    i[0]++;
                                    for (String key2 : dataMap2.keySet()) {
                                        servicesRef.child(key).child(key2).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                                SalonServiceModel serviceModel =
                                                        snapshot2.getValue(SalonServiceModel.class);
                                                if (serviceModel != null) {
                                                    serviceModel.setService_type(key);
                                                    serviceModel.setId(key2);
                                                    salonServicesList.add(serviceModel);
                                                    services.add(serviceModel.getService_name());

                                                    j[0]++;
                                                    if (i[0] == dataMap.keySet().size() && j[0] == dataMap2.keySet().size()) {
                                                        loadProductsAndServices();
                                                    }
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
                                    Log.d("DB Error : ", error.toString());
                                }
                            });
                        }
                    } else {
                        showWarning();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Fail to get data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getCustomers() {

        if (customerRef != null) {
            customerRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    HashMap<String, Array> dataMap = (HashMap<String, Array>) dataSnapshot.getValue();
                    if (dataMap != null) {
                        final int[] i = {0};
                        for (String key : dataMap.keySet()) {
                            customerRef.child(key).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    CustomerModel customer = snapshot.getValue(CustomerModel.class);
                                    i[0]++;
                                    customer.setId(key);
                                    customers.add(customer);
                                    custName.add(customer.getCustName());
                                    custPhone.add(customer.getCustPhone());

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
                    Toast.makeText(getContext(), "Fail to get data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}