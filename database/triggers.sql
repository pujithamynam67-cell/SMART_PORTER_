-- =====================================================================
-- SMART PORTER: DATABASE TRIGGERS
-- File: database/triggers.sql
-- =====================================================================

USE `smart_porter`;

DELIMITER $$

-- Trigger 1: Auto-create Porter Availability Record upon Registration
DROP TRIGGER IF EXISTS `trg_after_porter_registered`$$
CREATE TRIGGER `trg_after_porter_registered`
AFTER INSERT ON `porters`
FOR EACH ROW
BEGIN
    INSERT INTO `porter_availability` (`porter_id`, `is_online`, `is_busy`, `current_platform`)
    VALUES (NEW.porter_id, FALSE, FALSE, 1);
END$$

-- Trigger 2: Integrity Validation for Luggage Attributes
DROP TRIGGER IF EXISTS `trg_validate_luggage`$$
CREATE TRIGGER `trg_validate_luggage`
BEFORE INSERT ON `luggage`
FOR EACH ROW
BEGIN
    IF NEW.weight_kg <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Validation Error: Luggage weight must be greater than 0 kg.';
    END IF;
    IF NEW.weight_kg > 250.00 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Validation Error: Baggage exceeds maximum safe coolie handling weight limit (250 kg).';
    END IF;
    IF NEW.bag_count <= 0 THEN
        SET NEW.bag_count = 1;
    END IF;
END$$

-- Trigger 3: Dynamically Recalculate Porter Average Rating
DROP TRIGGER IF EXISTS `trg_after_feedback_inserted`$$
CREATE TRIGGER `trg_after_feedback_inserted`
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
