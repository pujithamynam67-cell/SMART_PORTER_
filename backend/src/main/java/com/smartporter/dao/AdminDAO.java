package com.smartporter.dao;

import com.smartporter.model.Admin;
import com.smartporter.util.DBConnection;
import com.smartporter.util.PasswordUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class AdminDAO {

    public Admin authenticate(String username, String rawPassword) throws SQLException {
        String sql = "SELECT * FROM admins WHERE username = ? OR email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.checkPassword(rawPassword, storedHash)) {
                        Admin a = new Admin();
                        a.setAdminId(rs.getInt("admin_id"));
                        a.setUsername(rs.getString("username"));
                        a.setEmail(rs.getString("email"));
                        a.setFullName(rs.getString("full_name"));
                        a.setRole(rs.getString("role"));
                        return a;
                    }
                }
            }
        }
        return null;
    }

    public Map<String, Object> getDashboardStatistics() throws SQLException {
        Map<String, Object> map = new HashMap<>();
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM passengers) AS total_passengers, " +
                "(SELECT COUNT(*) FROM porters) AS total_porters, " +
                "(SELECT COUNT(*) FROM porters WHERE verification_status = 'APPROVED') AS approved_porters, " +
                "(SELECT COUNT(*) FROM porters WHERE verification_status = 'PENDING') AS pending_porters, " +
                "(SELECT COUNT(*) FROM stations WHERE is_active = TRUE) AS total_stations, " +
                "(SELECT COUNT(*) FROM bookings) AS total_bookings, " +
                "(SELECT COUNT(*) FROM bookings WHERE booking_status IN ('REQUESTED', 'ACCEPTED', 'PORTER_ARRIVING', 'SERVICE_STARTED')) AS active_bookings, " +
                "(SELECT COUNT(*) FROM bookings WHERE booking_status = 'COMPLETED') AS completed_bookings, " +
                "(SELECT COUNT(*) FROM bookings WHERE booking_status = 'CANCELLED') AS cancelled_bookings, " +
                "(SELECT COALESCE(SUM(amount), 0.00) FROM payments WHERE payment_status = 'SUCCESS') AS total_revenue";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                map.put("totalPassengers", rs.getInt("total_passengers"));
                map.put("totalPorters", rs.getInt("total_porters"));
                map.put("approvedPorters", rs.getInt("approved_porters"));
                map.put("pendingPorters", rs.getInt("pending_porters"));
                map.put("totalStations", rs.getInt("total_stations"));
                map.put("totalBookings", rs.getInt("total_bookings"));
                map.put("activeBookings", rs.getInt("active_bookings"));
                map.put("completedBookings", rs.getInt("completed_bookings"));
                map.put("cancelledBookings", rs.getInt("cancelled_bookings"));
                map.put("totalRevenue", rs.getBigDecimal("total_revenue"));
            }
        }
        return map;
    }

    public Map<String, Object> executeDemoQuery(String concept) throws SQLException {
        Map<String, Object> response = new HashMap<>();
        String title;
        String sql;

        switch (concept != null ? concept.toUpperCase() : "JOINS") {
            case "JOINS":
                title = "Multi-Table INNER JOIN (Bookings + Passengers + Porters + Stations + Luggage + Payments)";
                sql = "SELECT b.booking_code, p.full_name AS passenger, po.badge_number AS porter_badge, " +
                      "s.station_code, l.category AS luggage, l.weight_kg, b.total_amount, py.payment_method, py.payment_status " +
                      "FROM bookings b " +
                      "JOIN passengers p ON b.passenger_id = p.passenger_id " +
                      "JOIN porters po ON b.porter_id = po.porter_id " +
                      "JOIN stations s ON b.station_id = s.station_id " +
                      "JOIN luggage l ON b.booking_id = l.booking_id " +
                      "JOIN payments py ON b.booking_id = py.booking_id " +
                      "ORDER BY b.booking_id DESC LIMIT 10";
                break;
            case "SUBQUERIES":
                title = "Subquery with Comparison: Porters Above Station Average Trips";
                sql = "SELECT po.badge_number, po.full_name, s.station_name, po.total_completed_bookings, po.rating_avg, po.total_earnings " +
                      "FROM porters po " +
                      "JOIN stations s ON po.station_id = s.station_id " +
                      "WHERE po.total_completed_bookings >= ( " +
                      "    SELECT AVG(sub.total_completed_bookings) FROM porters sub WHERE sub.station_id = po.station_id " +
                      ") ORDER BY po.total_completed_bookings DESC";
                break;
            case "AGGREGATES":
                title = "Aggregate Functions (SUM, AVG, COUNT, MAX) Grouped by Railway Station";
                sql = "SELECT s.station_code, s.station_name, " +
                      "COUNT(b.booking_id) AS total_trips, " +
                      "COALESCE(SUM(b.total_amount), 0) AS total_revenue, " +
                      "COALESCE(AVG(b.total_amount), 0) AS avg_fare, " +
                      "COALESCE(MAX(b.total_amount), 0) AS max_fare " +
                      "FROM stations s " +
                      "LEFT JOIN bookings b ON s.station_id = b.station_id " +
                      "GROUP BY s.station_id, s.station_code, s.station_name " +
                      "ORDER BY total_revenue DESC";
                break;
            case "VIEWS":
                title = "Querying Database View: vw_station_performance";
                sql = "SELECT * FROM vw_station_performance";
                break;
            case "TRIGGERS":
            default:
                title = "Database Status Audit Trail (booking_status_history)";
                sql = "SELECT h.history_id, b.booking_code, h.status, h.changed_by_role, h.notes, h.changed_at " +
                      "FROM booking_status_history h " +
                      "JOIN bookings b ON h.booking_id = b.booking_id " +
                      "ORDER BY h.history_id DESC LIMIT 10";
                break;
        }

        response.put("concept", concept);
        response.put("title", title);
        response.put("sql", sql);

        List<String> columns = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int count = meta.getColumnCount();
            for (int i = 1; i <= count; i++) {
                columns.add(meta.getColumnLabel(i));
            }
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= count; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                rows.add(row);
            }
        }

        response.put("columns", columns);
        response.put("rows", rows);
        return response;
    }
}
