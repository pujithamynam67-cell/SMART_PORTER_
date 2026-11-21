package com.smartporter.dao;

import com.smartporter.database.DBConnection;
import com.smartporter.model.Booking;
import com.smartporter.model.Luggage;
import com.smartporter.model.Payment;
import com.smartporter.model.Feedback;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingDAO {

    /**
     * Executes Stored Procedure: sp_create_smart_booking with ACID transaction.
     */
    public Map<String, Object> createBooking(
            int passengerId, int stationId, String trainNumber, String trainName,
            String coachNumber, String pickupPoint, String dropPoint,
            String luggageType, int quantity, BigDecimal weightKg, boolean isFragile, String notes) throws SQLException {

        String callSql = "{CALL sp_create_smart_booking(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        Map<String, Object> result = new HashMap<>();

        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(callSql)) {

            cs.setInt(1, passengerId);
            cs.setInt(2, stationId);
            cs.setString(3, trainNumber);
            cs.setString(4, trainName);
            cs.setString(5, coachNumber);
            cs.setString(6, pickupPoint);
            cs.setString(7, dropPoint);
            cs.setString(8, luggageType);
            cs.setInt(9, quantity);
            cs.setBigDecimal(10, weightKg);
            cs.setBoolean(11, isFragile);
            cs.setString(12, notes);

            // Register OUT parameters
            cs.registerOutParameter(13, Types.INTEGER);    // p_booking_id
            cs.registerOutParameter(14, Types.VARCHAR);    // p_booking_code
            cs.registerOutParameter(15, Types.DECIMAL);    // p_total_fare

            cs.execute();

            int bookingId = cs.getInt(13);
            String bookingCode = cs.getString(14);
            BigDecimal totalFare = cs.getBigDecimal(15);

            result.put("bookingId", bookingId);
            result.put("bookingCode", bookingCode);
            result.put("totalFare", totalFare);
            result.put("status", "REQUESTED");
        }
        return result;
    }

    /**
     * Executes Stored Procedure: sp_assign_nearest_porter to match high-rated available porter.
     */
    public Map<String, Object> assignPorter(int bookingId) throws SQLException {
        String callSql = "{CALL sp_assign_nearest_porter(?, ?, ?, ?)}";
        Map<String, Object> result = new HashMap<>();

        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(callSql)) {

            cs.setInt(1, bookingId);
            cs.registerOutParameter(2, Types.INTEGER); // p_assigned_porter_id
            cs.registerOutParameter(3, Types.VARCHAR); // p_porter_name
            cs.registerOutParameter(4, Types.VARCHAR); // p_badge_number

            cs.execute();

            int assignedPorterId = cs.getInt(2);
            String porterName = cs.getString(3);
            String badgeNumber = cs.getString(4);

            result.put("bookingId", bookingId);
            result.put("assignedPorterId", assignedPorterId);
            result.put("porterName", porterName);
            result.put("badgeNumber", badgeNumber);
            result.put("assigned", assignedPorterId > 0);
        }
        return result;
    }

    /**
     * Executes Stored Procedure: sp_complete_booking_and_pay.
     */
    public Map<String, Object> completeBookingAndPay(int bookingId, String paymentMethod) throws SQLException {
        String callSql = "{CALL sp_complete_booking_and_pay(?, ?, ?, ?)}";
        Map<String, Object> result = new HashMap<>();

        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(callSql)) {

            cs.setInt(1, bookingId);
            cs.setString(2, paymentMethod != null ? paymentMethod : "UPI");
            cs.registerOutParameter(3, Types.VARCHAR); // p_result_code
            cs.registerOutParameter(4, Types.VARCHAR); // p_result_msg

            cs.execute();

            result.put("resultCode", cs.getString(3));
            result.put("resultMsg", cs.getString(4));
        }
        return result;
    }

    public boolean updateStatus(int bookingId, String status) throws SQLException {
        String sql = "UPDATE bookings SET service_status = ? WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Map<String, Object>> getActiveBookings() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_active_bookings ORDER BY booking_id DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("bookingId", rs.getInt("booking_id"));
                m.put("bookingCode", rs.getString("booking_code"));
                m.put("status", rs.getString("service_status"));
                m.put("bookingDate", rs.getString("booking_date"));
                m.put("passengerName", rs.getString("passenger_name"));
                m.put("passengerPhone", rs.getString("passenger_phone"));
                m.put("stationCode", rs.getString("station_code"));
                m.put("stationName", rs.getString("station_name"));
                m.put("trainNumber", rs.getString("train_number"));
                m.put("coachNumber", rs.getString("coach_number"));
                m.put("pickupPoint", rs.getString("pickup_point"));
                m.put("dropPoint", rs.getString("drop_point"));
                m.put("totalFare", rs.getBigDecimal("total_fare"));
                m.put("porterId", rs.getObject("porter_id"));
                m.put("porterBadge", rs.getString("porter_badge"));
                m.put("porterName", rs.getString("porter_name"));
                m.put("porterPhone", rs.getString("porter_phone"));
                m.put("luggageType", rs.getString("luggage_type"));
                m.put("luggageCount", rs.getInt("luggage_count"));
                m.put("weightKg", rs.getBigDecimal("total_weight_kg"));
                list.add(m);
            }
        }
        return list;
    }

    public List<Map<String, Object>> getBookingsByPassenger(int passengerId) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_passenger_ride_history WHERE passenger_id = ? ORDER BY booking_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, passengerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("bookingId", rs.getInt("booking_id"));
                    m.put("bookingCode", rs.getString("booking_code"));
                    m.put("stationCode", rs.getString("station_code"));
                    m.put("stationName", rs.getString("station_name"));
                    m.put("trainNumber", rs.getString("train_number"));
                    m.put("coachNumber", rs.getString("coach_number"));
                    m.put("pickupPoint", rs.getString("pickup_point"));
                    m.put("dropPoint", rs.getString("drop_point"));
                    m.put("bookingDate", rs.getString("booking_date"));
                    m.put("status", rs.getString("service_status"));
                    m.put("totalFare", rs.getBigDecimal("total_fare"));
                    m.put("porterBadge", rs.getString("porter_badge"));
                    m.put("porterName", rs.getString("porter_name"));
                    m.put("paymentStatus", rs.getString("payment_status"));
                    m.put("paymentMethod", rs.getString("payment_method"));
                    m.put("rating", rs.getObject("passenger_rating"));
                    m.put("comments", rs.getString("passenger_review"));
                    list.add(m);
                }
            }
        }
        return list;
    }

    public List<Map<String, Object>> getBookingsByPorter(int porterId) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT b.*, p.full_name AS passenger_name, p.phone AS passenger_phone, " +
                     "s.station_code, s.station_name, l.luggage_type, l.quantity, l.total_weight_kg, " +
                     "py.payment_status, py.payment_method " +
                     "FROM bookings b " +
                     "JOIN passengers p ON b.passenger_id = p.passenger_id " +
                     "JOIN stations s ON b.station_id = s.station_id " +
                     "LEFT JOIN luggage l ON b.booking_id = l.booking_id " +
                     "LEFT JOIN payments py ON b.booking_id = py.booking_id " +
                     "WHERE b.porter_id = ? " +
                     "ORDER BY b.booking_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, porterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("bookingId", rs.getInt("booking_id"));
                    m.put("bookingCode", rs.getString("booking_code"));
                    m.put("status", rs.getString("service_status"));
                    m.put("bookingDate", rs.getString("booking_date"));
                    m.put("passengerName", rs.getString("passenger_name"));
                    m.put("passengerPhone", rs.getString("passenger_phone"));
                    m.put("stationName", rs.getString("station_name"));
                    m.put("stationCode", rs.getString("station_code"));
                    m.put("trainNumber", rs.getString("train_number"));
                    m.put("coachNumber", rs.getString("coach_number"));
                    m.put("pickupPoint", rs.getString("pickup_point"));
                    m.put("dropPoint", rs.getString("drop_point"));
                    m.put("totalFare", rs.getBigDecimal("total_fare"));
                    m.put("luggageType", rs.getString("luggage_type"));
                    m.put("quantity", rs.getInt("quantity"));
                    m.put("weightKg", rs.getBigDecimal("total_weight_kg"));
                    m.put("paymentStatus", rs.getString("payment_status"));
                    list.add(m);
                }
            }
        }
        return list;
    }

    public Map<String, Object> getBookingById(int bookingId) throws SQLException {
        String sql = "SELECT b.*, p.full_name AS passenger_name, p.phone AS passenger_phone, " +
                     "po.badge_number, po.full_name AS porter_name, po.phone AS porter_phone, " +
                     "s.station_code, s.station_name, l.luggage_type, l.quantity, l.total_weight_kg, l.is_fragile, " +
                     "py.payment_status, py.payment_method, f.rating, f.comments " +
                     "FROM bookings b " +
                     "JOIN passengers p ON b.passenger_id = p.passenger_id " +
                     "JOIN stations s ON b.station_id = s.station_id " +
                     "LEFT JOIN porters po ON b.porter_id = po.porter_id " +
                     "LEFT JOIN luggage l ON b.booking_id = l.booking_id " +
                     "LEFT JOIN payments py ON b.booking_id = py.booking_id " +
                     "LEFT JOIN feedback f ON b.booking_id = f.booking_id " +
                     "WHERE b.booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("bookingId", rs.getInt("booking_id"));
                    m.put("bookingCode", rs.getString("booking_code"));
                    m.put("passengerId", rs.getInt("passenger_id"));
                    m.put("passengerName", rs.getString("passenger_name"));
                    m.put("passengerPhone", rs.getString("passenger_phone"));
                    m.put("porterId", rs.getObject("porter_id"));
                    m.put("porterBadge", rs.getString("badge_number"));
                    m.put("porterName", rs.getString("porter_name"));
                    m.put("porterPhone", rs.getString("porter_phone"));
                    m.put("stationId", rs.getInt("station_id"));
                    m.put("stationName", rs.getString("station_name"));
                    m.put("stationCode", rs.getString("station_code"));
                    m.put("trainNumber", rs.getString("train_number"));
                    m.put("coachNumber", rs.getString("coach_number"));
                    m.put("pickupPoint", rs.getString("pickup_point"));
                    m.put("dropPoint", rs.getString("drop_point"));
                    m.put("serviceStatus", rs.getString("service_status"));
                    m.put("totalFare", rs.getBigDecimal("total_fare"));
                    m.put("luggageType", rs.getString("luggage_type"));
                    m.put("quantity", rs.getInt("quantity"));
                    m.put("weightKg", rs.getBigDecimal("total_weight_kg"));
                    m.put("isFragile", rs.getBoolean("is_fragile"));
                    m.put("paymentStatus", rs.getString("payment_status"));
                    m.put("paymentMethod", rs.getString("payment_method"));
                    m.put("rating", rs.getObject("rating"));
                    m.put("comments", rs.getString("comments"));
                    return m;
                }
            }
        }
        return null;
    }
}
