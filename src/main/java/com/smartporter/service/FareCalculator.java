package com.smartporter.service;

import com.smartporter.model.Station;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class FareCalculator {

    public static Map<String, Object> estimateFare(Station station, double weightKg, int quantity, boolean isFragile, boolean isSeniorCitizen) {
        BigDecimal baseFare = station.getBasePorterFare();
        BigDecimal perKgRate = station.getPerKgRate();

        BigDecimal weightCharge = perKgRate.multiply(BigDecimal.valueOf(weightKg));
        BigDecimal bagHandlingFee = BigDecimal.valueOf(Math.max(1, quantity) * 20.00);
        BigDecimal fragileSurcharge = isFragile ? BigDecimal.valueOf(30.00) : BigDecimal.ZERO;

        BigDecimal subtotal = baseFare.add(weightCharge).add(bagHandlingFee).add(fragileSurcharge);

        // 15% Senior Citizen Concession
        BigDecimal discount = BigDecimal.ZERO;
        if (isSeniorCitizen) {
            discount = subtotal.multiply(BigDecimal.valueOf(0.15)).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalFare = subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("baseFare", baseFare);
        breakdown.put("perKgRate", perKgRate);
        breakdown.put("weightCharge", weightCharge.setScale(2, RoundingMode.HALF_UP));
        breakdown.put("bagHandlingFee", bagHandlingFee);
        breakdown.put("fragileSurcharge", fragileSurcharge);
        breakdown.put("subtotal", subtotal.setScale(2, RoundingMode.HALF_UP));
        breakdown.put("seniorCitizenDiscount", discount);
        breakdown.put("totalFare", totalFare);

        return breakdown;
    }
}
