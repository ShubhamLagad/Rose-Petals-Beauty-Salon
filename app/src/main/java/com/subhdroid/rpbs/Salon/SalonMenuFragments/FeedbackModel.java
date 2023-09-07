package com.subhdroid.rpbs.Salon.SalonMenuFragments;

public class FeedbackModel {
    String feedbackName;
    String comment;

    public FeedbackModel(String feedbackName, String comment){
        this.comment = comment;
        this.feedbackName = feedbackName;
    }
    FeedbackModel(){}

    public String getFeedbackName() {
        return feedbackName;
    }

    public void setFeedbackName(String feedbackName) {
        this.feedbackName = feedbackName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
