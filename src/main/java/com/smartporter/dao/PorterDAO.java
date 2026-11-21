package com.smartporter.dao;

import com.smartporter.database.DBConnection;
import com.smartporter.model.Porter;
import com.smartporter.util.SecurityUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PorterDAO {

    public Porter register(Porter p, String rawPassword) throws SQLException {
        String sql = "INSERT INTO porters (badge_number, full_name, phone, password_hash, station_id, current_platform, availability_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'AVAILABLE')";
        String hash = SecurityUtil.hashPassword(rawPassword);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getBadgeNumber());
            ps.setString(2, p.getFullName());
            ps.setString(3, p.getPhone());
            ps.setString(4, hash);
            ps.setInt(5, p.getStationId());
            ps.setInt(6, p.getCurrentPlatform() > 0 ? p.getCurrentPlatform() : 1);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setPorterId(rs.getInt(1));
                }
            }
            p.setPasswordHash(null);
            return p;
        }
    }

    public Porter authenticate(String phoneOrBadge, String rawPassword) throws SQLException {
        String sql = "SELECT p.*, s.station_name, s.station_code " +
                     "FROM porters p " +
                     "JOIN stations s ON p.station_id = s.station_id " +
                     "WHERE p.phone = ? OR p.badge_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phoneOrBadge);
            ps.setString(2, phoneOrBadge);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (SecurityUtil.verifyPassword(rawPassword, storedHash)) {
                        Porter p = new Porter();
                        p.setPorterId(rs.getInt("porter_id"));
                        p.setBadgeNumber(rs.getString("badge_number"));
                        p.setFullName(rs.getString("full_name"));
                        p.setPhone(rs.getString("phone"));
                        p.setStationId(rs.getInt("station_id"));
                        p.setStationName(rs.getString("station_name"));
                        p.setStationCode(rs.getString("station_code"));
                        p.setCurrentPlatform(rs.getInt("current_platform"));
                        p.setAvailabilityStatus(rs.getString("availability_status"));
                        p.setRatingAvg(rs.getBigDecimal("rating_avg"));
                        p.setTotalRides(rs.getInt("total_rides"));
                        p.setTotalEarnings(rs.getBigDecimal("total_earnings"));
                        return p;
                    }
                }
            }
        }
        return null;
    }

    public Porter getById(int porterId) throws SQLException {
        String sql = "SELECT p.*, s.station_name, s.station_code " +
                     "FROM porters p " +
                     "JOIN stations s ON p.station_id = s.station_id " +
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
                    p.setStationId(rs.getInt("station_id"));
                    p.setStationName(rs.getString("station_name"));
                    p.setStationCode(rs.getString("station_code"));
                    p.setCurrentPlatform(rs.getInt("current_platform"));
                    p.setAvailabilityStatus(rs.getString("availability_status"));
                    p.setRatingAvg(rs.getBigDecimal("rating_avg"));
                    p.setTotalRides(rs.getInt("total_rides"));
                    p.setTotalEarnings(rs.getBigDecimal("total_earnings"));
                    return p;
                }
            }
        }
        return null;
    }

    public List<Porter> getByStation(int stationId, boolean availableOnly) throws SQLException {
        List<Porter> list = new ArrayList<>();
        String sql = "SELECT p.*, s.station_name, s.station_code FROM porters p " +
                     "JOIN stations s ON p.station_id = s.station_id " +
                     "WHERE p.station_id = ?" + (availableOnly ? " AND p.availability_status = 'AVAILABLE'" : "") +
                     " ORDER BY p.rating_avg DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Porter p = new Porter();
                    p.setPorterId(rs.getInt("porter_id"));
                    p.setBadgeNumber(rs.getString("badge_number"));
                    p.setFullName(rs.getString("full_name"));
                    p.setPhone(rs.getString("phone"));
                    p.setStationId(rs.getInt("station_id"));
                    p.setStationName(rs.getString("station_name"));
                    p.setStationCode(rs.getString("station_code"));
                    p.setCurrentPlatform(rs.getInt("current_platform"));
                    p.setAvailabilityStatus(rs.getString("availability_status"));
                    p.setRatingAvg(rs.getBigDecimal("rating_avg"));
                    p.setTotalRides(rs.getInt("total_rides"));
                    p.setTotalEarnings(rs.getBigDecimal("total_earnings"));
                    list.add(p);
                }
            }
        }
        return list;
    }

    public boolean updateStatus(int porterId, String status) throws SQLException {
        String sql = "UPDATE porters SET availability_status = ? WHERE porter_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, porterId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePlatform(int porterId, int platform) throws SQLException {
        String sql = "UPDATE porters SET current_platform = ? WHERE porter_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, platform);
            ps.setInt(2, porterId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Map<String, Object>> getLeaderboard() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_porter_leaderboard LIMIT 20";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("porterId", rs.getInt("porter_id"));
                map.put("badgeNumber", rs.getString("badge_number"));
                map.put("porterName", rs.getString("porter_name"));
                map.put("stationName", rs.getString("station_name"));
                map.put("stationCode", rs.getString("station_code"));
                map.put("status", rs.getString("availability_status"));
                map.put("totalRides", rs.getInt("total_rides"));
                map.put("rating", rs.getBigDecimal("rating_avg"));
                map.put("earnings", rs.getBigDecimal("total_earnings"));
                map.put("reviewsCount", rs.getInt("total_reviews_count"));
                list.add(map);
            }
        }
        return list;
    }
}
