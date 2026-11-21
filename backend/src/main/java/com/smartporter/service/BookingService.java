package com.smartporter.service;

import com.smartporter.dao.BookingDAO;
import com.smartporter.dao.PaymentDAO;
import com.smartporter.dao.FeedbackDAO;
import com.smartporter.model.Booking;
import com.smartporter.model.Feedback;
import com.smartporter.model.Payment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class BookingService {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    public Map<String, Object> createBooking(
            int passengerId, int stationId, int pickupLocId, int dropLocId,
            String trainNo, String trainName, String coach, String seat,
            String category, int bagCount, BigDecimal weightKg, boolean isFragile, String notes, int porterId) throws SQLException {
        return bookingDAO.createBooking(passengerId, stationId, pickupLocId, dropLocId, trainNo, trainName, coach, seat, category, bagCount, weightKg, isFragile, notes, porterId);
    }

    public Booking getBooking(int bookingId) throws SQLException {
        return bookingDAO.getById(bookingId);
    }

    public List<Booking> getPassengerBookings(int passengerId) throws SQLException {
        return bookingDAO.getByPassenger(passengerId);
    }

    public List<Booking> getPorterRequests(int stationId) throws SQLException {
        return bookingDAO.getRequestsForStation(stationId);
    }

    public List<Booking> getPorterBookings(int porterId) throws SQLException {
        return bookingDAO.getByPorter(porterId);
    }

    public Map<String, Object> porterAcceptBooking(int bookingId, int porterId) throws SQLException {
        return bookingDAO.porterAcceptBooking(bookingId, porterId);
    }

    public Map<String, Object> updateStatus(int bookingId, String newStatus, String actingRole, int actingId, String notes) throws SQLException {
        return bookingDAO.updateStatus(bookingId, newStatus, actingRole, actingId, notes);
    }

    public Payment processPayment(int bookingId, BigDecimal amount, String method) throws SQLException {
        Payment p = paymentDAO.processPayment(bookingId, amount, method);
        bookingDAO.updateStatus(bookingId, "COMPLETED", "PASSENGER", 0, "Payment processed successfully");
        return p;
    }

    public Feedback submitFeedback(int bookingId, int passengerId, int porterId, int rating, String comments) throws SQLException {
        return feedbackDAO.submitFeedback(bookingId, passengerId, porterId, rating, comments);
    }
}
