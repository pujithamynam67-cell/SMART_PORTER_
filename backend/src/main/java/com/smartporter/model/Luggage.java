package com.smartporter.model;

import java.math.BigDecimal;

public class Luggage {
    private int luggageId;
    private int bookingId;
    private String category;
    private int bagCount;
    private BigDecimal weightKg;
    private boolean isFragile;
    private String specialInstructions;

    public Luggage() {}

    public int getLuggageId() { return luggageId; }
    public void setLuggageId(int luggageId) { this.luggageId = luggageId; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getBagCount() { return bagCount; }
    public void setBagCount(int bagCount) { this.bagCount = bagCount; }

    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }

    public boolean isFragile() { return isFragile; }
    public void setFragile(boolean fragile) { isFragile = fragile; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
}
