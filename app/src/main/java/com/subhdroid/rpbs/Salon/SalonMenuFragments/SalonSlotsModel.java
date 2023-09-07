package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import java.util.ArrayList;

public class SalonSlotsModel {
    String slotTime;
    ArrayList<String> service;
    String customerKey;
    String dayName;
    String date;
    String id;

    public SalonSlotsModel(String slotTime, ArrayList<String> service, String customerKey) {
        this.slotTime = slotTime;
        this.service = service;
        this.customerKey = customerKey;
    }

    public SalonSlotsModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSlotTime() {
        return slotTime;
    }

    public void setSlotTime(String slotTime) {
        this.slotTime = slotTime;
    }

    public ArrayList<String> getService() {
        return service;
    }

    public void setService(ArrayList<String> service) {
        this.service = service;
    }

    public String getCustomerKey() {
        return customerKey;
    }

    public void setCustomerKey(String customerKey) {
        this.customerKey = customerKey;
    }
}
