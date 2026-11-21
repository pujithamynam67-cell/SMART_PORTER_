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
    private int pickupLocationId;
    private String pickupLocationName;
    private int dropLocationId;
    private String dropLocationName;
    private String trainNumber;
    private String trainName;
    private String coachNumber;
    private String seatNumber;
    private String bookingStatus; // REQUESTED, ACCEPTED, PORTER_ARRIVING, SERVICE_STARTED, COMPLETED, CANCELLED
    private BigDecimal totalAmount;
    private Timestamp bookingTime;
    private Timestamp serviceStartTime;
    private Timestamp completedTime;
    private String cancellationReason;

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

    public int getPickupLocationId() { return pickupLocationId; }
    public void setPickupLocationId(int pickupLocationId) { this.pickupLocationId = pickupLocationId; }

    public String getPickupLocationName() { return pickupLocationName; }
    public void setPickupLocationName(String pickupLocationName) { this.pickupLocationName = pickupLocationName; }

    public int getDropLocationId() { return dropLocationId; }
    public void setDropLocationId(int dropLocationId) { this.dropLocationId = dropLocationId; }

    public String getDropLocationName() { return dropLocationName; }
    public void setDropLocationName(String dropLocationName) { this.dropLocationName = dropLocationName; }

    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }

    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { this.trainName = trainName; }

    public String getCoachNumber() { return coachNumber; }
    public void setCoachNumber(String coachNumber) { this.coachNumber = coachNumber; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Timestamp getBookingTime() { return bookingTime; }
    public void setBookingTime(Timestamp bookingTime) { this.bookingTime = bookingTime; }

    public Timestamp getServiceStartTime() { return serviceStartTime; }
    public void setServiceStartTime(Timestamp serviceStartTime) { this.serviceStartTime = serviceStartTime; }

    public Timestamp getCompletedTime() { return completedTime; }
    public void setCompletedTime(Timestamp completedTime) { this.completedTime = completedTime; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public Luggage getLuggage() { return luggage; }
    public void setLuggage(Luggage luggage) { this.luggage = luggage; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    public Feedback getFeedback() { return feedback; }
    public void setFeedback(Feedback feedback) { this.feedback = feedback; }
}
