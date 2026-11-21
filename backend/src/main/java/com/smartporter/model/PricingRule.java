package com.smartporter.model;

import java.math.BigDecimal;

public class PricingRule {
    private int ruleId;
    private Integer stationId;
    private String luggageCategory;
    private BigDecimal baseCharge;
    private BigDecimal ratePerKg;
    private BigDecimal fragileSurcharge;
    private BigDecimal platformChangeFee;
    private boolean isActive;

    public PricingRule() {}

    public int getRuleId() { return ruleId; }
    public void setRuleId(int ruleId) { this.ruleId = ruleId; }

    public Integer getStationId() { return stationId; }
    public void setStationId(Integer stationId) { this.stationId = stationId; }

    public String getLuggageCategory() { return luggageCategory; }
    public void setLuggageCategory(String luggageCategory) { this.luggageCategory = luggageCategory; }

    public BigDecimal getBaseCharge() { return baseCharge; }
    public void setBaseCharge(BigDecimal baseCharge) { this.baseCharge = baseCharge; }

    public BigDecimal getRatePerKg() { return ratePerKg; }
    public void setRatePerKg(BigDecimal ratePerKg) { this.ratePerKg = ratePerKg; }

    public BigDecimal getFragileSurcharge() { return fragileSurcharge; }
    public void setFragileSurcharge(BigDecimal fragileSurcharge) { this.fragileSurcharge = fragileSurcharge; }

    public BigDecimal getPlatformChangeFee() { return platformChangeFee; }
    public void setPlatformChangeFee(BigDecimal platformChangeFee) { this.platformChangeFee = platformChangeFee; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
