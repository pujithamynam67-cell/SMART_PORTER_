package com.smartporter.dao;

import com.smartporter.database.DBConnection;
import com.smartporter.util.SecurityUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class AdminDAO {

    public boolean authenticate(String username, String rawPassword) throws SQLException {
        String sql = "SELECT password_hash FROM admins WHERE username = ? OR email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    return SecurityUtil.verifyPassword(rawPassword, storedHash);
                }
            }
        }
        return false;
    }

    public Map<String, Object> getSystemMetrics() throws SQLException {
        Map<String, Object> metrics = new HashMap<>();
        String sql = "SELECT " +
                     "(SELECT COUNT(*) FROM passengers) AS total_passengers, " +
                     "(SELECT COUNT(*) FROM porters) AS total_porters, " +
                     "(SELECT COUNT(*) FROM porters WHERE availability_status = 'AVAILABLE') AS available_porters, " +
                     "(SELECT COUNT(*) FROM bookings) AS total_bookings, " +
                     "(SELECT COUNT(*) FROM bookings WHERE service_status = 'COMPLETED') AS completed_bookings, " +
                     "(SELECT COUNT(*) FROM bookings WHERE service_status IN ('REQUESTED', 'ACCEPTED', 'PICKED_UP', 'IN_TRANSIT')) AS active_bookings, " +
                     "(SELECT COALESCE(SUM(amount), 0) FROM payments WHERE payment_status = 'SUCCESS') AS total_revenue, " +
                     "(SELECT COALESCE(AVG(rating), 5.0) FROM feedback) AS platform_avg_rating";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                metrics.put("totalPassengers", rs.getInt("total_passengers"));
                metrics.put("totalPorters", rs.getInt("total_porters"));
                metrics.put("availablePorters", rs.getInt("available_porters"));
                metrics.put("totalBookings", rs.getInt("total_bookings"));
                metrics.put("completedBookings", rs.getInt("completed_bookings"));
                metrics.put("activeBookings", rs.getInt("active_bookings"));
                metrics.put("totalRevenue", rs.getBigDecimal("total_revenue"));
                metrics.put("platformAvgRating", rs.getDouble("platform_avg_rating"));
            }
        }
        return metrics;
    }

    public List<Map<String, Object>> getStationAnalytics() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_station_analytics ORDER BY total_bookings_handled DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("stationId", rs.getInt("station_id"));
                m.put("stationCode", rs.getString("station_code"));
                m.put("stationName", rs.getString("station_name"));
                m.put("city", rs.getString("city"));
                m.put("registeredPorters", rs.getInt("registered_porters"));
                m.put("availablePorters", rs.getInt("available_porters"));
                m.put("totalBookings", rs.getInt("total_bookings_handled"));
                m.put("grossRevenue", rs.getBigDecimal("total_gross_revenue"));
                m.put("luggageWeightKg", rs.getBigDecimal("total_luggage_weight_kg"));
                m.put("avgBookingValue", rs.getBigDecimal("average_booking_value"));
                list.add(m);
            }
        }
        return list;
    }

    /**
     * Executes queries showcasing core DBMS concepts for evaluation/demonstration.
     */
    public Map<String, Object> runDbmsConceptDemo(String concept) throws SQLException {
        Map<String, Object> response = new HashMap<>();
        String queryTitle = "";
        String sql = "";

        switch (concept != null ? concept.toUpperCase() : "JOINS") {
            case "JOINS":
                queryTitle = "Multi-Table INNER JOIN (Bookings + Passengers + Porters + Stations + Payments + Luggage)";
                sql = "SELECT b.booking_code, p.full_name AS passenger, po.badge_number AS porter_badge, " +
                      "s.station_code, l.luggage_type, l.total_weight_kg, b.total_fare, py.payment_method, py.payment_status " +
                      "FROM bookings b " +
                      "JOIN passengers p ON b.passenger_id = p.passenger_id " +
                      "JOIN porters po ON b.porter_id = po.porter_id " +
                      "JOIN stations s ON b.station_id = s.station_id " +
                      "JOIN luggage l ON b.booking_id = l.booking_id " +
                      "JOIN payments py ON b.booking_id = py.booking_id " +
                      "ORDER BY b.booking_id DESC LIMIT 10";
                break;

            case "SUBQUERIES":
                queryTitle = "Subquery with Comparison: Porters with more completed trips than Station Average";
                sql = "SELECT po.badge_number, po.full_name, s.station_name, po.total_rides, po.rating_avg, po.total_earnings " +
                      "FROM porters po " +
                      "JOIN stations s ON po.station_id = s.station_id " +
                      "WHERE po.total_rides >= ( " +
                      "    SELECT AVG(sub.total_rides) FROM porters sub WHERE sub.station_id = po.station_id " +
                      ") ORDER BY po.total_rides DESC";
                break;

            case "AGGREGATES":
                queryTitle = "Aggregate Functions (COUNT, SUM, AVG, MAX, MIN) Grouped by Railway Station";
                sql = "SELECT s.station_code, s.station_name, " +
                      "COUNT(b.booking_id) AS total_trips, " +
                      "COALESCE(SUM(b.total_fare), 0) AS gross_earnings, " +
                      "COALESCE(AVG(b.total_fare), 0) AS avg_fare, " +
                      "COALESCE(MAX(b.total_fare), 0) AS max_fare, " +
                      "COALESCE(MIN(b.total_fare), 0) AS min_fare " +
                      "FROM stations s " +
                      "LEFT JOIN bookings b ON s.station_id = b.station_id " +
                      "GROUP BY s.station_id, s.station_code, s.station_name " +
                      "ORDER BY gross_earnings DESC";
                break;

            case "TRIGGERS":
                queryTitle = "Audit Trail Generated Automatically by Database Triggers (porter_earnings_log)";
                sql = "SELECT l.log_id, po.badge_number, po.full_name, b.booking_code, " +
                      "l.fare_amount, l.commission_fee, l.net_porter_earning, l.logged_at " +
                      "FROM porter_earnings_log l " +
                      "JOIN porters po ON l.porter_id = po.porter_id " +
                      "JOIN bookings b ON l.booking_id = b.booking_id " +
                      "ORDER BY l.log_id DESC LIMIT 10";
                break;

            case "VIEWS":
            default:
                queryTitle = "Database View Query: vw_porter_leaderboard";
                sql = "SELECT * FROM vw_porter_leaderboard LIMIT 10";
                break;
        }

        response.put("concept", concept);
        queryTitle = queryTitle.replace("'", "");
        response.put("title", queryTitle);
        response.put("sqlExecuted", sql);

        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                columnNames.add(meta.getColumnLabel(i));
            }

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                rows.add(row);
            }
        }

        response.put("columns", columnNames);
        response.put("rows", rows);
        return response;
    }
}
