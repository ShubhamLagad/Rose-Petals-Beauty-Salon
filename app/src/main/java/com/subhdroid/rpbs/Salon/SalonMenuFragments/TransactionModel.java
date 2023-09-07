package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import java.util.ArrayList;

public class TransactionModel {
    String customerName;
    String mobile;
    ArrayList<String> services;
    ArrayList<String> products;
    String expense;
    String date;
    String bill;
    String customerToken;
    String id;
    String payment;
    String custKey;

    public TransactionModel(String customerName, String mobile, ArrayList<String> selServ,
                            ArrayList<String> selProd, String totalBill, String billDate,
                            String bill,String payment) {
        this.customerName = customerName;
        this.mobile = mobile;
        this.services = selServ;
        this.products = selProd;
        this.expense = totalBill;
        this.date = billDate;
        this.bill = bill;
        this.payment = payment;
    }

    TransactionModel(){}

    public String getCustKey() {
        return custKey;
    }

    public void setCustKey(String custKey) {
        this.custKey = custKey;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBill() {
        return bill;
    }

    public void setBill(String bill) {
        this.bill = bill;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public ArrayList<String> getServices() {
        return services;
    }

    public void setServices(ArrayList<String> services) {
        this.services = services;
    }

    public ArrayList<String> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<String> products) {
        this.products = products;
    }

    public String getExpense() {
        return expense;
    }

    public void setExpense(String expense) {
        this.expense = expense;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCustomerToken() {
        return customerToken;
    }

    public void setCustomerToken(String customerToken) {
        this.customerToken = customerToken;
    }
}
