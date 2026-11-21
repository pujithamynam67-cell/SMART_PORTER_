-- =====================================================================
-- SMART PORTER: STORED DATABASE FUNCTIONS
-- File: database/functions.sql
-- =====================================================================

USE `smart_porter`;

DELIMITER $$

DROP FUNCTION IF EXISTS `fn_calculate_luggage_fare`$$
CREATE FUNCTION `fn_calculate_luggage_fare`(
    p_station_id INT,
    p_category VARCHAR(50),
    p_weight_kg DECIMAL(6,2),
    p_bag_count INT,
    p_is_fragile BOOLEAN
) RETURNS DECIMAL(8,2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_base DECIMAL(8,2) DEFAULT 100.00;
    DECLARE v_rate_kg DECIMAL(6,2) DEFAULT 4.00;
    DECLARE v_fragile_fee DECIMAL(6,2) DEFAULT 30.00;
    DECLARE v_platform_fee DECIMAL(6,2) DEFAULT 20.00;
    DECLARE v_total DECIMAL(8,2) DEFAULT 0.00;

    -- Lookup rule for station & category or global fallback
    SELECT base_charge, rate_per_kg, fragile_surcharge, platform_change_fee
    INTO v_base, v_rate_kg, v_fragile_fee, v_platform_fee
    FROM pricing_rules
    WHERE (station_id = p_station_id OR station_id IS NULL)
      AND (luggage_category = p_category OR luggage_category = 'Standard Bag')
      AND is_active = TRUE
    ORDER BY station_id DESC
    LIMIT 1;

    -- Formula: Base + (Weight * Rate/KG) + ((BagCount - 1) * HandlingFee) + (Fragile Fee if any)
    SET v_total = v_base + (p_weight_kg * v_rate_kg);
    
    IF p_bag_count > 1 THEN
        SET v_total = v_total + ((p_bag_count - 1) * 25.00);
    END IF;

    IF p_is_fragile = TRUE THEN
        SET v_total = v_total + v_fragile_fee;
    END IF;

    RETURN ROUND(v_total, 2);
END$$

DELIMITER ;
