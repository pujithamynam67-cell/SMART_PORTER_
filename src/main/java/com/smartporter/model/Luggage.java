package com.smartporter.model;

import java.math.BigDecimal;

public class Luggage {
    private int luggageId;
    private int bookingId;
    private String luggageType;
    private int quantity;
    private BigDecimal totalWeightKg;
    private boolean isFragile;
    private String notes;

    public Luggage() {}

    public Luggage(int luggageId, int bookingId, String luggageType, int quantity, BigDecimal totalWeightKg, boolean isFragile, String notes) {
        this.luggageId = luggageId;
        this.bookingId = bookingId;
        this.luggageType = luggageType;
        this.quantity = quantity;
        this.totalWeightKg = totalWeightKg;
        this.isFragile = isFragile;
        this.notes = notes;
    }

    public int getLuggageId() { return luggageId; }
    public void setLuggageId(int luggageId) { this.luggageId = luggageId; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public String getLuggageType() { return luggageType; }
    public void setLuggageType(String luggageType) { this.luggageType = luggageType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getTotalWeightKg() { return totalWeightKg; }
    public void setTotalWeightKg(BigDecimal totalWeightKg) { this.totalWeightKg = totalWeightKg; }

    public boolean isFragile() { return isFragile; }
    public void setFragile(boolean fragile) { isFragile = fragile; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
