package com.smartporter.model;

import java.math.BigDecimal;

public class Station {
    private int stationId;
    private String stationCode;
    private String stationName;
    private String city;
    private String state;
    private int totalPlatforms;
    private BigDecimal basePorterFare;
    private BigDecimal perKgRate;

    public Station() {}

    public Station(int stationId, String stationCode, String stationName, String city, String state, int totalPlatforms, BigDecimal basePorterFare, BigDecimal perKgRate) {
        this.stationId = stationId;
        this.stationCode = stationCode;
        this.stationName = stationName;
        this.city = city;
        this.state = state;
        this.totalPlatforms = totalPlatforms;
        this.basePorterFare = basePorterFare;
        this.perKgRate = perKgRate;
    }

    public int getStationId() { return stationId; }
    public void setStationId(int stationId) { this.stationId = stationId; }

    public String getStationCode() { return stationCode; }
    public void setStationCode(String stationCode) { this.stationCode = stationCode; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public int getTotalPlatforms() { return totalPlatforms; }
    public void setTotalPlatforms(int totalPlatforms) { this.totalPlatforms = totalPlatforms; }

    public BigDecimal getBasePorterFare() { return basePorterFare; }
    public void setBasePorterFare(BigDecimal basePorterFare) { this.basePorterFare = basePorterFare; }

    public BigDecimal getPerKgRate() { return perKgRate; }
    public void setPerKgRate(BigDecimal perKgRate) { this.perKgRate = perKgRate; }
}
