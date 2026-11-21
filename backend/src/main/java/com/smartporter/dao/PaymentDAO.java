package com.smartporter.dao;

import com.smartporter.model.Payment;
import com.smartporter.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;

public class PaymentDAO {

    public Payment processPayment(int bookingId, BigDecimal amount, String method) throws SQLException {
        String txnRef = "TXN-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 9000 + 1000);
        String sql = "INSERT INTO payments (booking_id, amount, payment_method, transaction_reference, payment_status, payment_date) " +
                     "VALUES (?, ?, ?, ?, 'SUCCESS', NOW()) " +
                     "ON DUPLICATE KEY UPDATE amount = VALUES(amount), payment_method = VALUES(payment_method), " +
                     "transaction_reference = VALUES(transaction_reference), payment_status = 'SUCCESS', payment_date = NOW()";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bookingId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, method != null ? method : "ONLINE_DEMO");
            ps.setString(4, txnRef);
            ps.executeUpdate();

            Payment p = new Payment();
            p.setBookingId(bookingId);
            p.setAmount(amount);
            p.setPaymentMethod(method);
            p.setTransactionReference(txnRef);
            p.setPaymentStatus("SUCCESS");
            return p;
        }
    }

    public Payment getByBookingId(int bookingId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Payment p = new Payment();
                    p.setPaymentId(rs.getInt("payment_id"));
                    p.setBookingId(rs.getInt("booking_id"));
                    p.setAmount(rs.getBigDecimal("amount"));
                    p.setPaymentMethod(rs.getString("payment_method"));
                    p.setTransactionReference(rs.getString("transaction_reference"));
                    p.setPaymentStatus(rs.getString("payment_status"));
                    p.setPaymentDate(rs.getTimestamp("payment_date"));
                    return p;
                }
            }
        }
        return null;
    }
}
