package com.smartporter.dao;

import com.smartporter.database.DBConnection;
import com.smartporter.model.Payment;

import java.math.BigDecimal;
import java.sql.*;

public class PaymentDAO {

    public Payment recordPayment(int bookingId, BigDecimal amount, String method, String status) throws SQLException {
        String sql = "INSERT INTO payments (booking_id, amount, payment_method, transaction_ref, payment_status, paid_at) " +
                     "VALUES (?, ?, ?, ?, ?, NOW()) " +
                     "ON DUPLICATE KEY UPDATE payment_status = VALUES(payment_status), " +
                     "payment_method = VALUES(payment_method), paid_at = NOW()";

        String txnRef = "TXN-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 900 + 100);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bookingId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, method);
            ps.setString(4, txnRef);
            ps.setString(5, status);
            ps.executeUpdate();

            Payment p = new Payment();
            p.setBookingId(bookingId);
            p.setAmount(amount);
            p.setPaymentMethod(method);
            p.setTransactionRef(txnRef);
            p.setPaymentStatus(status);
            return p;
        }
    }
}
