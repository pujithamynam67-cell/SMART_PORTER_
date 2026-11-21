package com.smartporter.dao;

import com.smartporter.model.PricingRule;
import com.smartporter.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PricingRuleDAO {

    public List<PricingRule> getAll() throws SQLException {
        List<PricingRule> list = new ArrayList<>();
        String sql = "SELECT * FROM pricing_rules WHERE is_active = TRUE ORDER BY rule_id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                PricingRule r = new PricingRule();
                r.setRuleId(rs.getInt("rule_id"));
                r.setStationId((Integer) rs.getObject("station_id"));
                r.setLuggageCategory(rs.getString("luggage_category"));
                r.setBaseCharge(rs.getBigDecimal("base_charge"));
                r.setRatePerKg(rs.getBigDecimal("rate_per_kg"));
                r.setFragileSurcharge(rs.getBigDecimal("fragile_surcharge"));
                r.setPlatformChangeFee(rs.getBigDecimal("platform_change_fee"));
                r.setActive(rs.getBoolean("is_active"));
                list.add(r);
            }
        }
        return list;
    }

    public BigDecimal calculateFare(int stationId, String category, double weightKg, int bagCount, boolean isFragile) throws SQLException {
        String sql = "SELECT fn_calculate_luggage_fare(?, ?, ?, ?, ?) AS fare";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stationId);
            ps.setString(2, category);
            ps.setBigDecimal(3, BigDecimal.valueOf(weightKg));
            ps.setInt(4, bagCount);
            ps.setBoolean(5, isFragile);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("fare");
                }
            }
        }
        return BigDecimal.valueOf(150.00);
    }
}
