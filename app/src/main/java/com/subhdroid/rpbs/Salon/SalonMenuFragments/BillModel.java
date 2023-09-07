package com.subhdroid.rpbs.Salon.SalonMenuFragments;

public class BillModel {
    String srNo;
    String itemName;
    String price;
    String quantity;
    String total;
    String subTotal;
    String tax;
    String finalTotal;

    BillModel(String srNo,
              String itemName,
              String price,
              String quantity,
              String total) {
        this.srNo = srNo;
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
        this.total = total;
    }

    public String getSrNo() {
        return srNo;
    }

    public void setSrNo(String srNo) {
        this.srNo = srNo;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(String subTotal) {
        this.subTotal = subTotal;
    }

    public String getTax() {
        return tax;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

    public String getFinalTotal() {
        return finalTotal;
    }

    public void setFinalTotal(String finalTotal) {
        this.finalTotal = finalTotal;
    }
}
