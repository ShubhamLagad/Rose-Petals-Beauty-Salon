package com.subhdroid.rpbs.Salon;

public class SalonModel {
    String SalonName;
    String ownerName;
    String phone;
    String email;
    String openingTime;
    String closingTime;
    String address;
    String photo;
    String token;
    int slotTimePeriod = 30;


    public SalonModel(String SalonName, String ownerName, String phone, String email, String openingTime, String closingTime,
                        String address, String photo, String token) {
        this.SalonName = SalonName;
        this.ownerName = ownerName;
        this.phone = phone;
        this.email = email;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.address = address;
        this.photo = photo;
        this.token = token;
    }

    public SalonModel() {
    }

    public int getSlotTimePeriod() {
        return slotTimePeriod;
    }

    public void setSlotTimePeriod(int slotTimePeriod) {
        this.slotTimePeriod = slotTimePeriod;
    }

    public String getSalonName() {
        return SalonName;
    }

    public void setSalonName(String SalonName) {
        this.SalonName = SalonName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
