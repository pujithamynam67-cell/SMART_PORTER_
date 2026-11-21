package com.smartporter.dao;

import com.smartporter.database.DBConnection;
import com.smartporter.model.Feedback;

import java.sql.*;

public class FeedbackDAO {

    public Feedback submitFeedback(int bookingId, int passengerId, int porterId, int rating, String comments) throws SQLException {
        // Note: trg_update_porter_rating trigger will automatically recalculate porter average rating
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
                Feedback f = new Feedback(rs.next() ? rs.getInt(1) : 0, bookingId, passengerId, porterId, rating, comments);
                return f;
            }
        }
    }
}
