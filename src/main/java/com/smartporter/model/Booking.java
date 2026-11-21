package com.smartporter.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Booking {
    private int bookingId;
    private String bookingCode;
    private int passengerId;
    private String passengerName;
    private String passengerPhone;
    private Integer porterId;
    private String porterName;
    private String porterBadge;
    private String porterPhone;
    private int stationId;
    private String stationName;
    private String stationCode;
    private String trainNumber;
    private String trainName;
    private String coachNumber;
    private String pickupPoint;
    private String dropPoint;
    private Timestamp bookingDate;
    private String serviceStatus; // REQUESTED, ACCEPTED, PICKED_UP, IN_TRANSIT, COMPLETED, CANCELLED
    private BigDecimal totalFare;
    private Timestamp porterAssignedAt;
    private Timestamp completedAt;
    
    // Luggage details attached
    private Luggage luggage;
    private Payment payment;
    private Feedback feedback;

    public Booking() {}

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }

    public int getPassengerId() { return passengerId; }
    public void setPassengerId(int passengerId) { this.passengerId = passengerId; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public String getPassengerPhone() { return passengerPhone; }
    public void setPassengerPhone(String passengerPhone) { this.passengerPhone = passengerPhone; }

    public Integer getPorterId() { return porterId; }
    public void setPorterId(Integer porterId) { this.porterId = porterId; }

    public String getPorterName() { return porterName; }
    public void setPorterName(String porterName) { this.porterName = porterName; }

    public String getPorterBadge() { return porterBadge; }
    public void setPorterBadge(String porterBadge) { this.porterBadge = porterBadge; }

    public String getPorterPhone() { return porterPhone; }
    public void setPorterPhone(String porterPhone) { this.porterPhone = porterPhone; }

    public int getStationId() { return stationId; }
    public void setStationId(int stationId) { this.stationId = stationId; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getStationCode() { return stationCode; }
    public void setStationCode(String stationCode) { this.stationCode = stationCode; }

    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }

    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { this.trainName = trainName; }

    public String getCoachNumber() { return coachNumber; }
    public void setCoachNumber(String coachNumber) { this.coachNumber = coachNumber; }

    public String getPickupPoint() { return pickupPoint; }
    public void setPickupPoint(String pickupPoint) { this.pickupPoint = pickupPoint; }

    public String getDropPoint() { return dropPoint; }
    public void setDropPoint(String dropPoint) { this.dropPoint = dropPoint; }

    public Timestamp getBookingDate() { return bookingDate; }
    public void setBookingDate(Timestamp bookingDate) { this.bookingDate = bookingDate; }

    public String getServiceStatus() { return serviceStatus; }
    public void setServiceStatus(String serviceStatus) { this.serviceStatus = serviceStatus; }

    public BigDecimal getTotalFare() { return totalFare; }
    public void setTotalFare(BigDecimal totalFare) { this.totalFare = totalFare; }

    public Timestamp getPorterAssignedAt() { return porterAssignedAt; }
    public void setPorterAssignedAt(Timestamp porterAssignedAt) { this.porterAssignedAt = porterAssignedAt; }

    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }

    public Luggage getLuggage() { return luggage; }
    public void setLuggage(Luggage luggage) { this.luggage = luggage; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    public Feedback getFeedback() { return feedback; }
    public void setFeedback(Feedback feedback) { this.feedback = feedback; }
}
