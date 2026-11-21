package com.smartporter.model;

import java.sql.Timestamp;

public class Passenger {
    private int passengerId;
    private String fullName;
    private String phone;
    private String email;
    private transient String passwordHash;
    private String gender;
    private boolean isSeniorCitizen;
    private String status;
    private Timestamp createdAt;

    public Passenger() {}

    public int getPassengerId() { return passengerId; }
    public void setPassengerId(int passengerId) { this.passengerId = passengerId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public boolean isSeniorCitizen() { return isSeniorCitizen; }
    public void setSeniorCitizen(boolean seniorCitizen) { isSeniorCitizen = seniorCitizen; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
