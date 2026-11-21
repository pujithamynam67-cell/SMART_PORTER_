package com.smartporter.dao;

import com.smartporter.model.Booking;
import com.smartporter.model.Luggage;
import com.smartporter.model.Payment;
import com.smartporter.model.Feedback;
import com.smartporter.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingDAO {

    public Map<String, Object> createBooking(
            int passengerId, int stationId, int pickupLocId, int dropLocId,
            String trainNo, String trainName, String coach, String seat,
            String category, int bagCount, BigDecimal weightKg, boolean isFragile, String notes, int porterId) throws SQLException {

        String callSql = "{CALL sp_create_smart_booking(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        Map<String, Object> result = new HashMap<>();

        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(callSql)) {

            cs.setInt(1, passengerId);
            cs.setInt(2, stationId);
            cs.setInt(3, pickupLocId);
            cs.setInt(4, dropLocId);
            cs.setString(5, trainNo);
            cs.setString(6, trainName);
            cs.setString(7, coach);
            cs.setString(8, seat);
            cs.setString(9, category);
            cs.setInt(10, bagCount);
            cs.setBigDecimal(11, weightKg);
            cs.setBoolean(12, isFragile);
            cs.setString(13, notes);
            cs.setInt(14, porterId);

            cs.registerOutParameter(15, Types.INTEGER);    // p_booking_id
            cs.registerOutParameter(16, Types.VARCHAR);    // p_booking_code
            cs.registerOutParameter(17, Types.DECIMAL);    // p_total_amount

            cs.execute();

            int bookingId = cs.getInt(15);
            String bookingCode = cs.getString(16);
            BigDecimal totalAmount = cs.getBigDecimal(17);

            result.put("bookingId", bookingId);
            result.put("bookingCode", bookingCode);
            result.put("totalAmount", totalAmount);
            result.put("status", "REQUESTED");
        }
        return result;
    }

    public Map<String, Object> porterAcceptBooking(int bookingId, int porterId) throws SQLException {
        String callSql = "{CALL sp_porter_accept_booking(?, ?, ?, ?)}";
        Map<String, Object> map = new HashMap<>();
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(callSql)) {
            cs.setInt(1, bookingId);
            cs.setInt(2, porterId);
            cs.registerOutParameter(3, Types.BOOLEAN);
            cs.registerOutParameter(4, Types.VARCHAR);
            cs.execute();

            map.put("success", cs.getBoolean(3));
            map.put("message", cs.getString(4));
        }
        return map;
    }

    public Map<String, Object> updateStatus(int bookingId, String newStatus, String actingRole, int actingId, String notes) throws SQLException {
        String callSql = "{CALL sp_update_booking_status(?, ?, ?, ?, ?, ?, ?)}";
        Map<String, Object> map = new HashMap<>();
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(callSql)) {
            cs.setInt(1, bookingId);
            cs.setString(2, newStatus);
            cs.setString(3, actingRole);
            cs.setInt(4, actingId);
            cs.setString(5, notes);
            cs.registerOutParameter(6, Types.BOOLEAN);
            cs.registerOutParameter(7, Types.VARCHAR);
            cs.execute();

            map.put("success", cs.getBoolean(6));
            map.put("message", cs.getString(7));
        }
        return map;
    }

    public Booking getById(int bookingId) throws SQLException {
        String sql = "SELECT * FROM vw_admin_booking_details WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBooking(rs);
                }
            }
        }
        return null;
    }

    public List<Booking> getByPassenger(int passengerId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_admin_booking_details WHERE passenger_id = ? ORDER BY booking_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, passengerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToBooking(rs));
                }
            }
        }
        return list;
    }

    public List<Booking> getRequestsForStation(int stationId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_active_bookings WHERE station_id = ? AND booking_status = 'REQUESTED' ORDER BY booking_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = new Booking();
                    b.setBookingId(rs.getInt("booking_id"));
                    b.setBookingCode(rs.getString("booking_code"));
                    b.setBookingStatus(rs.getString("booking_status"));
                    b.setBookingTime(rs.getTimestamp("booking_time"));
                    b.setTotalAmount(rs.getBigDecimal("total_amount"));
                    b.setPassengerId(rs.getInt("passenger_id"));
                    b.setPassengerName(rs.getString("passenger_name"));
                    b.setPassengerPhone(rs.getString("passenger_phone"));
                    b.setStationId(rs.getInt("station_id"));
                    b.setStationCode(rs.getString("station_code"));
                    b.setStationName(rs.getString("station_name"));
                    b.setPickupLocationName(rs.getString("pickup_location"));
                    b.setDropLocationName(rs.getString("drop_location"));
                    b.setTrainNumber(rs.getString("train_number"));
                    b.setCoachNumber(rs.getString("coach_number"));

                    Luggage l = new Luggage();
                    l.setCategory(rs.getString("luggage_category"));
                    l.setBagCount(rs.getInt("bag_count"));
                    l.setWeightKg(rs.getBigDecimal("weight_kg"));
                    l.setFragile(rs.getBoolean("is_fragile"));
                    b.setLuggage(l);

                    list.add(b);
                }
            }
        }
        return list;
    }

    public List<Booking> getByPorter(int porterId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_admin_booking_details WHERE porter_id = ? ORDER BY booking_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, porterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToBooking(rs));
                }
            }
        }
        return list;
    }

    public List<Booking> getAll() throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_admin_booking_details ORDER BY booking_id DESC LIMIT 50";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToBooking(rs));
            }
        }
        return list;
    }

    private Booking mapRowToBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setBookingCode(rs.getString("booking_code"));
        b.setBookingStatus(rs.getString("booking_status"));
        b.setBookingTime(rs.getTimestamp("booking_time"));
        b.setTotalAmount(rs.getBigDecimal("total_amount"));
        b.setPassengerId(rs.getInt("passenger_id"));
        b.setPassengerName(rs.getString("passenger_name"));
        b.setPassengerPhone(rs.getString("passenger_phone"));
        b.setStationCode(rs.getString("station_code"));
        b.setStationName(rs.getString("station_name"));
        b.setPickupLocationName(rs.getString("pickup_point"));
        b.setDropLocationName(rs.getString("drop_point"));
        b.setTrainNumber(rs.getString("train_number"));
        b.setCoachNumber(rs.getString("coach_number"));

        Integer porterId = (Integer) rs.getObject("porter_id");
        if (porterId != null) {
            b.setPorterId(porterId);
            b.setPorterBadge(rs.getString("porter_badge"));
            b.setPorterName(rs.getString("porter_name"));
        }

        Luggage l = new Luggage();
        l.setCategory(rs.getString("luggage_category"));
        l.setBagCount(rs.getInt("bag_count"));
        l.setWeightKg(rs.getBigDecimal("weight_kg"));
        b.setLuggage(l);

        String payStatus = rs.getString("payment_status");
        if (payStatus != null) {
            Payment p = new Payment();
            p.setPaymentStatus(payStatus);
            p.setPaymentMethod(rs.getString("payment_method"));
            p.setTransactionReference(rs.getString("transaction_reference"));
            b.setPayment(p);
        }

        Integer rating = (Integer) rs.getObject("passenger_rating");
        if (rating != null) {
            Feedback f = new Feedback();
            f.setRating(rating);
            f.setComments(rs.getString("passenger_review"));
            b.setFeedback(f);
        }

        return b;
    }
}
