-- =====================================================================
-- SMART PORTER - SMART LUGGAGE MANAGEMENT SYSTEM AT RAILWAY STATIONS
-- File: 03_triggers.sql
-- Description: Automated Business Rules and Integrity via Triggers
-- =====================================================================

USE `smart_porter`;

DELIMITER $$

-- Trigger 1: Validate Luggage Weight before insertion (Integrity Check)
DROP TRIGGER IF EXISTS `trg_validate_luggage_weight`$$
CREATE TRIGGER `trg_validate_luggage_weight`
BEFORE INSERT ON `luggage`
FOR EACH ROW
BEGIN
    IF NEW.total_weight_kg <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Validation Error: Luggage weight must be greater than 0 kg.';
    END IF;
    IF NEW.total_weight_kg > 250.00 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Validation Error: Luggage weight exceeds maximum allowed porter capacity of 250 kg.';
    END IF;
    IF NEW.quantity <= 0 THEN
        SET NEW.quantity = 1;
    END IF;
END$$

-- Trigger 2: Automatic Porter Status update when booking is Accepted
DROP TRIGGER IF EXISTS `trg_after_booking_accepted`$$
CREATE TRIGGER `trg_after_booking_accepted`
AFTER UPDATE ON `bookings`
FOR EACH ROW
BEGIN
    -- If status transitioned from REQUESTED to ACCEPTED and porter is assigned
    IF OLD.service_status != 'ACCEPTED' AND NEW.service_status = 'ACCEPTED' AND NEW.porter_id IS NOT NULL THEN
        UPDATE porters 
        SET availability_status = 'BUSY'
        WHERE porter_id = NEW.porter_id;
    END IF;
END$$

-- Trigger 3: Automatic Earning, Trip Increment & Porter Availability when booking is Completed
DROP TRIGGER IF EXISTS `trg_after_booking_completed`$$
CREATE TRIGGER `trg_after_booking_completed`
AFTER UPDATE ON `bookings`
FOR EACH ROW
BEGIN
    DECLARE v_fare DECIMAL(8,2);
    DECLARE v_commission DECIMAL(8,2);
    DECLARE v_net_earning DECIMAL(8,2);

    IF OLD.service_status != 'COMPLETED' AND NEW.service_status = 'COMPLETED' AND NEW.porter_id IS NOT NULL THEN
        SET v_fare = NEW.total_fare;
        -- 10% platform/railway station fee, 90% direct to coolie/porter
        SET v_commission = ROUND(v_fare * 0.10, 2);
        SET v_net_earning = v_fare - v_commission;

        -- 1. Free up the porter and update stats
        UPDATE porters 
        SET availability_status = 'AVAILABLE',
            total_rides = total_rides + 1,
            total_earnings = total_earnings + v_net_earning
        WHERE porter_id = NEW.porter_id;

        -- 2. Insert audit log record into porter_earnings_log
        INSERT INTO porter_earnings_log (porter_id, booking_id, fare_amount, commission_fee, net_porter_earning)
        VALUES (NEW.porter_id, NEW.booking_id, v_fare, v_commission, v_net_earning);
    END IF;

    -- If booking was cancelled, make porter available again if assigned
    IF OLD.service_status != 'CANCELLED' AND NEW.service_status = 'CANCELLED' AND NEW.porter_id IS NOT NULL THEN
        UPDATE porters 
        SET availability_status = 'AVAILABLE'
        WHERE porter_id = NEW.porter_id;
    END IF;
END$$

-- Trigger 4: Automatic Porter Rating Recalculation upon new Feedback
DROP TRIGGER IF EXISTS `trg_update_porter_rating`$$
CREATE TRIGGER `trg_update_porter_rating`
AFTER INSERT ON `feedback`
FOR EACH ROW
BEGIN
    DECLARE v_new_avg DECIMAL(3,2);

    SELECT ROUND(AVG(rating), 2) INTO v_new_avg
    FROM feedback
    WHERE porter_id = NEW.porter_id;

    UPDATE porters 
    SET rating_avg = v_new_avg
    WHERE porter_id = NEW.porter_id;
END$$

DELIMITER ;
