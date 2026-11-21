package com.smartporter.service;

import com.smartporter.dao.PricingRuleDAO;
import java.math.BigDecimal;
import java.sql.SQLException;

public class FareService {

    private final PricingRuleDAO pricingRuleDAO = new PricingRuleDAO();

    public BigDecimal calculateEstimate(int stationId, String category, double weightKg, int bagCount, boolean isFragile) throws SQLException {
        return pricingRuleDAO.calculateFare(stationId, category, weightKg, bagCount, isFragile);
    }
}
