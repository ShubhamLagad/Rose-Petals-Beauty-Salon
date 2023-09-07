package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import java.util.ArrayList;

public class SalonServiceModel {
    String service_type;
    String service_name;
    String service_time_period;
    String price;
    String id;

    String details;
    ArrayList<String> videoLinks;


    public SalonServiceModel(String service_type, String service_name, String service_time_period,
                             String price) {
        this.service_type = service_type;
        this.service_name = service_name;
        this.service_time_period = service_time_period;
        this.price = price;
    }
    public SalonServiceModel(String service_name, String service_time_period,
                             String price) {
        this.service_name = service_name;
        this.service_time_period = service_time_period;
        this.price = price;
    }
    SalonServiceModel(String details,ArrayList<String> videoLinks){
        this.details=details;
        this.videoLinks=videoLinks;
    }
    SalonServiceModel(){}

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public ArrayList<String> getVideoLinks() {
        return videoLinks;
    }

    public void setVideoLinks(ArrayList<String> videoLinks) {
        this.videoLinks = videoLinks;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getService_type() {
        return service_type;
    }

    public void setService_type(String service_type) {
        this.service_type = service_type;
    }

    public String getService_name() {
        return service_name;
    }

    public void setService_name(String service_name) {
        this.service_name = service_name;
    }

    public String getService_time_period() {
        return service_time_period;
    }

    public void setService_time_period(String service_time_period) {
        this.service_time_period = service_time_period;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
