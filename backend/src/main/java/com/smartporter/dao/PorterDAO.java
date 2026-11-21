package com.smartporter.dao;

import com.smartporter.model.Porter;
import com.smartporter.util.DBConnection;
import com.smartporter.util.PasswordUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PorterDAO {

    public Porter register(Porter p, String rawPassword) throws SQLException {
        String sql = "INSERT INTO porters (badge_number, full_name, phone, email, password_hash, station_id, verification_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'PENDING')";
        String hash = PasswordUtil.hashPassword(rawPassword);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getBadgeNumber());
            ps.setString(2, p.getFullName());
            ps.setString(3, p.getPhone());
            ps.setString(4, p.getEmail());
            ps.setString(5, hash);
            ps.setInt(6, p.getStationId());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setPorterId(rs.getInt(1));
                }
            }
            p.setVerificationStatus("PENDING");
            return p;
        }
    }

    public Porter authenticate(String phoneOrBadge, String rawPassword) throws SQLException {
        String sql = "SELECT p.*, s.station_name, s.station_code, pa.is_online, pa.is_busy, pa.current_platform " +
                     "FROM porters p " +
                     "JOIN stations s ON p.station_id = s.station_id " +
                     "LEFT JOIN porter_availability pa ON p.porter_id = pa.porter_id " +
                     "WHERE p.phone = ? OR p.badge_number = ? OR p.email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phoneOrBadge);
            ps.setString(2, phoneOrBadge);
            ps.setString(3, phoneOrBadge);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.checkPassword(rawPassword, storedHash)) {
                        Porter p = new Porter();
                        p.setPorterId(rs.getInt("porter_id"));
                        p.setBadgeNumber(rs.getString("badge_number"));
                        p.setFullName(rs.getString("full_name"));
                        p.setPhone(rs.getString("phone"));
                        p.setEmail(rs.getString("email"));
                        p.setStationId(rs.getInt("station_id"));
                        p.setStationName(rs.getString("station_name"));
                        p.setStationCode(rs.getString("station_code"));
                        p.setVerificationStatus(rs.getString("verification_status"));
                        p.setRatingAvg(rs.getBigDecimal("rating_avg"));
                        p.setTotalCompletedBookings(rs.getInt("total_completed_bookings"));
                        p.setTotalEarnings(rs.getBigDecimal("total_earnings"));
                        p.setOnline(rs.getBoolean("is_online"));
                        p.setBusy(rs.getBoolean("is_busy"));
                        p.setCurrentPlatform(rs.getInt("current_platform"));
                        return p;
                    }
                }
            }
        }
        return null;
    }

    public Porter getById(int porterId) throws SQLException {
        String sql = "SELECT p.*, s.station_name, s.station_code, pa.is_online, pa.is_busy, pa.current_platform " +
                     "FROM porters p " +
                     "JOIN stations s ON p.station_id = s.station_id " +
                     "LEFT JOIN porter_availability pa ON p.porter_id = pa.porter_id " +
                     "WHERE p.porter_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, porterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Porter p = new Porter();
                    p.setPorterId(rs.getInt("porter_id"));
                    p.setBadgeNumber(rs.getString("badge_number"));
                    p.setFullName(rs.getString("full_name"));
                    p.setPhone(rs.getString("phone"));
                    p.setEmail(rs.getString("email"));
                    p.setStationId(rs.getInt("station_id"));
                    p.setStationName(rs.getString("station_name"));
                    p.setStationCode(rs.getString("station_code"));
                    p.setVerificationStatus(rs.getString("verification_status"));
                    p.setRatingAvg(rs.getBigDecimal("rating_avg"));
                    p.setTotalCompletedBookings(rs.getInt("total_completed_bookings"));
                    p.setTotalEarnings(rs.getBigDecimal("total_earnings"));
                    p.setOnline(rs.getBoolean("is_online"));
                    p.setBusy(rs.getBoolean("is_busy"));
                    p.setCurrentPlatform(rs.getInt("current_platform"));
                    return p;
                }
            }
        }
        return null;
    }

    public List<Porter> getAvailablePorters(int stationId) throws SQLException {
        List<Porter> list = new ArrayList<>();
        String callSql = "{CALL sp_get_available_porters(?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(callSql)) {
            cs.setInt(1, stationId);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    Porter p = new Porter();
                    p.setPorterId(rs.getInt("porter_id"));
                    p.setBadgeNumber(rs.getString("badge_number"));
                    p.setFullName(rs.getString("full_name"));
                    p.setPhone(rs.getString("phone"));
                    p.setRatingAvg(rs.getBigDecimal("rating_avg"));
                    p.setTotalCompletedBookings(rs.getInt("total_completed_bookings"));
                    p.setCurrentPlatform(rs.getInt("current_platform"));
                    p.setOnline(rs.getBoolean("is_online"));
                    p.setBusy(rs.getBoolean("is_busy"));
                    p.setStationName(rs.getString("station_name"));
                    p.setStationCode(rs.getString("station_code"));
                    list.add(p);
                }
            }
        }
        return list;
    }

    public boolean updateAvailability(int porterId, boolean isOnline, int platform) throws SQLException {
        String sql = "INSERT INTO porter_availability (porter_id, is_online, is_busy, current_platform, last_heartbeat) " +
                     "VALUES (?, ?, FALSE, ?, NOW()) " +
                     "ON DUPLICATE KEY UPDATE is_online = VALUES(is_online), current_platform = VALUES(current_platform), last_heartbeat = NOW()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, porterId);
            ps.setBoolean(2, isOnline);
            ps.setInt(3, platform > 0 ? platform : 1);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateVerification(int porterId, String status) throws SQLException {
        String sql = "UPDATE porters SET verification_status = ? WHERE porter_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, porterId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Porter> getAll() throws SQLException {
        List<Porter> list = new ArrayList<>();
        String sql = "SELECT p.*, s.station_name, s.station_code, pa.is_online, pa.is_busy, pa.current_platform " +
                     "FROM porters p " +
                     "JOIN stations s ON p.station_id = s.station_id " +
                     "LEFT JOIN porter_availability pa ON p.porter_id = pa.porter_id " +
                     "ORDER BY p.porter_id DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Porter p = new Porter();
                p.setPorterId(rs.getInt("porter_id"));
                p.setBadgeNumber(rs.getString("badge_number"));
                p.setFullName(rs.getString("full_name"));
                p.setPhone(rs.getString("phone"));
                p.setEmail(rs.getString("email"));
                p.setStationId(rs.getInt("station_id"));
                p.setStationName(rs.getString("station_name"));
                p.setStationCode(rs.getString("station_code"));
                p.setVerificationStatus(rs.getString("verification_status"));
                p.setRatingAvg(rs.getBigDecimal("rating_avg"));
                p.setTotalCompletedBookings(rs.getInt("total_completed_bookings"));
                p.setTotalEarnings(rs.getBigDecimal("total_earnings"));
                p.setOnline(rs.getBoolean("is_online"));
                p.setBusy(rs.getBoolean("is_busy"));
                p.setCurrentPlatform(rs.getInt("current_platform"));
                list.add(p);
            }
        }
        return list;
    }

    public boolean deletePorter(int porterId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM porter_availability WHERE porter_id = ?")) {
                ps1.setInt(1, porterId);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement("UPDATE bookings SET porter_id = NULL WHERE porter_id = ?")) {
                ps2.setInt(1, porterId);
                ps2.executeUpdate();
            }
            try (PreparedStatement ps3 = conn.prepareStatement("DELETE FROM porters WHERE porter_id = ?")) {
                ps3.setInt(1, porterId);
                return ps3.executeUpdate() > 0;
            }
        }
    }
}
