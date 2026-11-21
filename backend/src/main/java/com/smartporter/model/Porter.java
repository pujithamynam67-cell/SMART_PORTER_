package com.smartporter.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Porter {
    private int porterId;
    private String badgeNumber;
    private String fullName;
    private String phone;
    private String email;
    private transient String passwordHash;
    private int stationId;
    private String stationName;
    private String stationCode;
    private String verificationStatus; // PENDING, APPROVED, REJECTED
    private BigDecimal ratingAvg;
    private int totalCompletedBookings;
    private BigDecimal totalEarnings;
    private boolean isOnline;
    private boolean isBusy;
    private int currentPlatform;
    private Timestamp createdAt;

    public Porter() {}

    public int getPorterId() { return porterId; }
    public void setPorterId(int porterId) { this.porterId = porterId; }

    public String getBadgeNumber() { return badgeNumber; }
    public void setBadgeNumber(String badgeNumber) { this.badgeNumber = badgeNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public int getStationId() { return stationId; }
    public void setStationId(int stationId) { this.stationId = stationId; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getStationCode() { return stationCode; }
    public void setStationCode(String stationCode) { this.stationCode = stationCode; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public BigDecimal getRatingAvg() { return ratingAvg; }
    public void setRatingAvg(BigDecimal ratingAvg) { this.ratingAvg = ratingAvg; }

    public int getTotalCompletedBookings() { return totalCompletedBookings; }
    public void setTotalCompletedBookings(int totalCompletedBookings) { this.totalCompletedBookings = totalCompletedBookings; }

    public BigDecimal getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(BigDecimal totalEarnings) { this.totalEarnings = totalEarnings; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public boolean isBusy() { return isBusy; }
    public void setBusy(boolean busy) { isBusy = busy; }

    public int getCurrentPlatform() { return currentPlatform; }
    public void setCurrentPlatform(int currentPlatform) { this.currentPlatform = currentPlatform; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
