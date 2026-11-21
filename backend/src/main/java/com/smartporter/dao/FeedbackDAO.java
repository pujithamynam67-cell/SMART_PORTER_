package com.smartporter.dao;

import com.smartporter.model.Feedback;
import com.smartporter.util.DBConnection;

import java.sql.*;

public class FeedbackDAO {

    public Feedback submitFeedback(int bookingId, int passengerId, int porterId, int rating, String comments) throws SQLException {
        // trg_after_feedback_inserted trigger will automatically recalculate porter average rating
        String sql = "INSERT INTO feedback (booking_id, passenger_id, porter_id, rating, comments, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, passengerId);
            ps.setInt(3, porterId);
            ps.setInt(4, rating);
            ps.setString(5, comments);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                Feedback f = new Feedback();
                if (rs.next()) f.setFeedbackId(rs.getInt(1));
                f.setBookingId(bookingId);
                f.setPassengerId(passengerId);
                f.setPorterId(porterId);
                f.setRating(rating);
                f.setComments(comments);
                return f;
            }
        }
    }
}
