package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import java.util.ArrayList;

public class SalonWeekModel {

    String openTime;
    String closeTime;
    String slotTimePeriod;
    String status;
    String day;
    String date;
    ArrayList<SalonSlotsModel> slotList;

    public SalonWeekModel(String openTime,
                          String closeTime,
                          String slotTimePeriod,
                          String status,
                          String date,
                          ArrayList<SalonSlotsModel> slotList) {
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.slotTimePeriod = slotTimePeriod;
        this.status = status;
        this.slotList = slotList;
        this.date = date;
    }

    SalonWeekModel() {
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public String getSlotTimePeriod() {
        return slotTimePeriod;
    }

    public void setSlotTimePeriod(String slotTimePeriod) {
        this.slotTimePeriod = slotTimePeriod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<SalonSlotsModel> getSlotList() {
        return slotList;
    }

    public void setSlotList(ArrayList<SalonSlotsModel> slotList) {
        this.slotList = slotList;
    }
}
