package com.smartporter.dao;

import com.smartporter.model.Passenger;
import com.smartporter.util.DBConnection;
import com.smartporter.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PassengerDAO {

    public Passenger register(Passenger p, String rawPassword) throws SQLException {
        String sql = "INSERT INTO passengers (full_name, phone, email, password_hash, gender, is_senior_citizen) VALUES (?, ?, ?, ?, ?, ?)";
        String hash = PasswordUtil.hashPassword(rawPassword);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getFullName());
            ps.setString(2, p.getPhone());
            ps.setString(3, p.getEmail());
            ps.setString(4, hash);
            ps.setString(5, p.getGender() != null ? p.getGender() : "Male");
            ps.setBoolean(6, p.isSeniorCitizen());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setPassengerId(rs.getInt(1));
                }
            }
            return p;
        }
    }

    public Passenger authenticate(String emailOrPhone, String rawPassword) throws SQLException {
        String sql = "SELECT * FROM passengers WHERE (email = ? OR phone = ?) AND status = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emailOrPhone);
            ps.setString(2, emailOrPhone);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.checkPassword(rawPassword, storedHash)) {
                        Passenger p = new Passenger();
                        p.setPassengerId(rs.getInt("passenger_id"));
                        p.setFullName(rs.getString("full_name"));
                        p.setPhone(rs.getString("phone"));
                        p.setEmail(rs.getString("email"));
                        p.setGender(rs.getString("gender"));
                        p.setSeniorCitizen(rs.getBoolean("is_senior_citizen"));
                        p.setStatus(rs.getString("status"));
                        p.setCreatedAt(rs.getTimestamp("created_at"));
                        return p;
                    }
                }
            }
        }
        return null;
    }

    public Passenger getById(int id) throws SQLException {
        String sql = "SELECT * FROM passengers WHERE passenger_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Passenger p = new Passenger();
                    p.setPassengerId(rs.getInt("passenger_id"));
                    p.setFullName(rs.getString("full_name"));
                    p.setPhone(rs.getString("phone"));
                    p.setEmail(rs.getString("email"));
                    p.setGender(rs.getString("gender"));
                    p.setSeniorCitizen(rs.getBoolean("is_senior_citizen"));
                    p.setStatus(rs.getString("status"));
                    p.setCreatedAt(rs.getTimestamp("created_at"));
                    return p;
                }
            }
        }
        return null;
    }

    public List<Passenger> getAll() throws SQLException {
        List<Passenger> list = new ArrayList<>();
        String sql = "SELECT passenger_id, full_name, phone, email, gender, is_senior_citizen, status, created_at FROM passengers ORDER BY passenger_id DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Passenger p = new Passenger();
                p.setPassengerId(rs.getInt("passenger_id"));
                p.setFullName(rs.getString("full_name"));
                p.setPhone(rs.getString("phone"));
                p.setEmail(rs.getString("email"));
                p.setGender(rs.getString("gender"));
                p.setSeniorCitizen(rs.getBoolean("is_senior_citizen"));
                p.setStatus(rs.getString("status"));
                p.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(p);
            }
        }
        return list;
    }
}
