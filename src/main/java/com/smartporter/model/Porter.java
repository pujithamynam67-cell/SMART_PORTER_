package com.smartporter.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Porter {
    private int porterId;
    private String badgeNumber;
    private String fullName;
    private String phone;
    private String passwordHash;
    private int stationId;
    private String stationName;
    private String stationCode;
    private int currentPlatform;
    private String availabilityStatus; // AVAILABLE, BUSY, OFFLINE
    private BigDecimal ratingAvg;
    private int totalRides;
    private BigDecimal totalEarnings;
    private Timestamp createdAt;

    public Porter() {}

    public Porter(int porterId, String badgeNumber, String fullName, String phone, String passwordHash, int stationId) {
        this.porterId = porterId;
        this.badgeNumber = badgeNumber;
        this.fullName = fullName;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.stationId = stationId;
        this.availabilityStatus = "AVAILABLE";
        this.ratingAvg = new BigDecimal("5.00");
        this.totalRides = 0;
        this.totalEarnings = BigDecimal.ZERO;
    }

    public int getPorterId() { return porterId; }
    public void setPorterId(int porterId) { this.porterId = porterId; }

    public String getBadgeNumber() { return badgeNumber; }
    public void setBadgeNumber(String badgeNumber) { this.badgeNumber = badgeNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public int getStationId() { return stationId; }
    public void setStationId(int stationId) { this.stationId = stationId; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getStationCode() { return stationCode; }
    public void setStationCode(String stationCode) { this.stationCode = stationCode; }

    public int getCurrentPlatform() { return currentPlatform; }
    public void setCurrentPlatform(int currentPlatform) { this.currentPlatform = currentPlatform; }

    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }

    public BigDecimal getRatingAvg() { return ratingAvg; }
    public void setRatingAvg(BigDecimal ratingAvg) { this.ratingAvg = ratingAvg; }

    public int getTotalRides() { return totalRides; }
    public void setTotalRides(int totalRides) { this.totalRides = totalRides; }

    public BigDecimal getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(BigDecimal totalEarnings) { this.totalEarnings = totalEarnings; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
