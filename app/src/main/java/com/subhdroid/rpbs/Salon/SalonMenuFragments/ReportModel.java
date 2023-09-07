package com.subhdroid.rpbs.Salon.SalonMenuFragments;

public class ReportModel {
    String day;
    String date;
    int count;
    static int weekIncome=0;
    ReportModel(String day,String date,int count){
        this.day = day;
        this.date = date;
        this.count = count;
    }
    ReportModel(){}

    public static int getWeekIncome() {
        return weekIncome;
    }

    public static void setWeekIncome(int weekIncome) {
        ReportModel.weekIncome = weekIncome;
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
