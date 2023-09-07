package com.subhdroid.rpbs.Salon.SalonMenuFragments;

import java.util.ArrayList;

public class SalonProductModel {
    String prodName;
    String prodPrice;
    String description;
    ArrayList<String> photos;
    String review;
    String totalPrice;
    String quantity;
    String id;


    public SalonProductModel(String prodName,
                             String prodPrice,
                             String description, ArrayList<String> photos,
                             String review) {

        this.prodName = prodName;
        this.prodPrice = prodPrice;
        this.description = description;
        this.photos = photos;
        this.review = review;
    }
    public SalonProductModel(String prodName,
                             String prodPrice,
                             String description, ArrayList<String> photos,
                             String review,String id) {

        this.prodName = prodName;
        this.prodPrice = prodPrice;
        this.description = description;
        this.photos = photos;
        this.review = review;
        this.id = id;
    }

    SalonProductModel() {
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public ArrayList<String> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<String> photos) {
        this.photos = photos;
    }

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public String getProdPrice() {
        return prodPrice;
    }

    public void setProdPrice(String prodPrice) {
        this.prodPrice = prodPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

}
