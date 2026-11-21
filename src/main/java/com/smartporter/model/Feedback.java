package com.smartporter.model;

import java.sql.Timestamp;

public class Feedback {
    private int feedbackId;
    private int bookingId;
    private int passengerId;
    private int porterId;
    private int rating;
    private String comments;
    private Timestamp createdAt;

    public Feedback() {}

    public Feedback(int feedbackId, int bookingId, int passengerId, int porterId, int rating, String comments) {
        this.feedbackId = feedbackId;
        this.bookingId = bookingId;
        this.passengerId = passengerId;
        this.porterId = porterId;
        this.rating = rating;
        this.comments = comments;
    }

    public int getFeedbackId() { return feedbackId; }
    public void setFeedbackId(int feedbackId) { this.feedbackId = feedbackId; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getPassengerId() { return passengerId; }
    public void setPassengerId(int passengerId) { this.passengerId = passengerId; }

    public int getPorterId() { return porterId; }
    public void setPorterId(int porterId) { this.porterId = porterId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
