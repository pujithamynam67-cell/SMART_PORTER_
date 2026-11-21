package com.smartporter.model;

public class StationLocation {
    private int locationId;
    private int stationId;
    private String locationName;
    private String locationType;
    private Integer platformNumber;

    public StationLocation() {}

    public StationLocation(int locationId, int stationId, String locationName, String locationType, Integer platformNumber) {
        this.locationId = locationId;
        this.stationId = stationId;
        this.locationName = locationName;
        this.locationType = locationType;
        this.platformNumber = platformNumber;
    }

    public int getLocationId() { return locationId; }
    public void setLocationId(int locationId) { this.locationId = locationId; }

    public int getStationId() { return stationId; }
    public void setStationId(int stationId) { this.stationId = stationId; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getLocationType() { return locationType; }
    public void setLocationType(String locationType) { this.locationType = locationType; }

    public Integer getPlatformNumber() { return platformNumber; }
    public void setPlatformNumber(Integer platformNumber) { this.platformNumber = platformNumber; }
}
