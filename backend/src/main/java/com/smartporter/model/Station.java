package com.smartporter.model;

import java.util.ArrayList;
import java.util.List;

public class Station {
    private int stationId;
    private String stationCode;
    private String stationName;
    private String city;
    private String state;
    private int totalPlatforms;
    private boolean isActive;
    private List<StationLocation> locations = new ArrayList<>();

    public Station() {}

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

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public List<StationLocation> getLocations() { return locations; }
    public void setLocations(List<StationLocation> locations) { this.locations = locations; }
}
