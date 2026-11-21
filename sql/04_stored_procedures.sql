-- =====================================================================
-- SMART PORTER - SMART LUGGAGE MANAGEMENT SYSTEM AT RAILWAY STATIONS
-- File: 04_stored_procedures.sql
-- Description: ACID Stored Procedures for Transactional Operations
-- =====================================================================

USE `smart_porter`;

DELIMITER $$

-- Procedure 1: Atomic Luggage Booking Creation with Dynamic Fare Calculation
DROP PROCEDURE IF EXISTS `sp_create_smart_booking`$$
CREATE PROCEDURE `sp_create_smart_booking`(
    IN p_passenger_id INT,
    IN p_station_id INT,
    IN p_train_number VARCHAR(20),
    IN p_train_name VARCHAR(100),
    IN p_coach_number VARCHAR(15),
    IN p_pickup_point VARCHAR(150),
    IN p_drop_point VARCHAR(150),
    IN p_luggage_type VARCHAR(50),
    IN p_quantity INT,
    IN p_weight_kg DECIMAL(6,2),
    IN p_is_fragile BOOLEAN,
    IN p_notes VARCHAR(255),
    OUT p_booking_id INT,
    OUT p_booking_code VARCHAR(30),
    OUT p_total_fare DECIMAL(8,2)
)
proc_label: BEGIN
    DECLARE v_base_fare DECIMAL(8,2) DEFAULT 100.00;
    DECLARE v_per_kg DECIMAL(6,2) DEFAULT 5.00;
    DECLARE v_calculated_fare DECIMAL(8,2) DEFAULT 0.00;
    DECLARE v_code VARCHAR(30);

    -- Error handler for rollback
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    -- Fetch station rates
    SELECT base_porter_fare, per_kg_rate 
    INTO v_base_fare, v_per_kg
    FROM stations 
    WHERE station_id = p_station_id;

    -- Dynamic Fare Formula: Base Fare + (Luggage Weight * Per Kg Rate) + (Quantity * 20) + Fragile fee
    SET v_calculated_fare = v_base_fare + (p_weight_kg * v_per_kg) + (p_quantity * 20.00);
    IF p_is_fragile = TRUE THEN
        SET v_calculated_fare = v_calculated_fare + 30.00;
    END IF;

    -- Generate unique booking code e.g. SP-2026-XXXX
    SET v_code = CONCAT('SP-', DATE_FORMAT(NOW(), '%Y%m%d'), '-', LPAD(FLOOR(RAND() * 10000), 4, '0'));

    START TRANSACTION;

    INSERT INTO bookings (
        booking_code, passenger_id, station_id, train_number, train_name,
        coach_number, pickup_point, drop_point, service_status, total_fare
    ) VALUES (
        v_code, p_passenger_id, p_station_id, p_train_number, p_train_name,
        p_coach_number, p_pickup_point, p_drop_point, 'REQUESTED', v_calculated_fare
    );

    SET p_booking_id = LAST_INSERT_ID();
    SET p_booking_code = v_code;
    SET p_total_fare = v_calculated_fare;

    INSERT INTO luggage (
        booking_id, luggage_type, quantity, total_weight_kg, is_fragile, notes
    ) VALUES (
        p_booking_id, p_luggage_type, p_quantity, p_weight_kg, p_is_fragile, p_notes
    );

    COMMIT;
END$$

-- Procedure 2: Smart Porter Matching and Assignment (Uber/Rapido Matcher)
DROP PROCEDURE IF EXISTS `sp_assign_nearest_porter`$$
CREATE PROCEDURE `sp_assign_nearest_porter`(
    IN p_booking_id INT,
    OUT p_assigned_porter_id INT,
    OUT p_porter_name VARCHAR(100),
    OUT p_badge_number VARCHAR(20)
)
BEGIN
    DECLARE v_station_id INT;
    DECLARE v_porter_id INT DEFAULT NULL;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    SELECT station_id INTO v_station_id FROM bookings WHERE booking_id = p_booking_id;

    START TRANSACTION;

    -- Select the highest rated available porter at this station (Lock for update)
    SELECT porter_id, full_name, badge_number
    INTO v_porter_id, p_porter_name, p_badge_number
    FROM porters
    WHERE station_id = v_station_id 
      AND availability_status = 'AVAILABLE'
    ORDER BY rating_avg DESC, total_rides ASC
    LIMIT 1
    FOR UPDATE;

    IF v_porter_id IS NOT NULL THEN
        UPDATE bookings 
        SET porter_id = v_porter_id,
            service_status = 'ACCEPTED',
            porter_assigned_at = NOW()
        WHERE booking_id = p_booking_id;

        -- Note: Trigger trg_after_booking_accepted will set porter to 'BUSY'
        SET p_assigned_porter_id = v_porter_id;
    ELSE
        SET p_assigned_porter_id = 0;
        SET p_porter_name = 'No Porter Available';
        SET p_badge_number = 'N/A';
    END IF;

    COMMIT;
END$$

-- Procedure 3: Complete Trip and Record Payment (ACID)
DROP PROCEDURE IF EXISTS `sp_complete_booking_and_pay`$$
CREATE PROCEDURE `sp_complete_booking_and_pay`(
    IN p_booking_id INT,
    IN p_payment_method VARCHAR(20),
    OUT p_result_code VARCHAR(20),
    OUT p_result_msg VARCHAR(100)
)
BEGIN
    DECLARE v_fare DECIMAL(8,2);
    DECLARE v_txn_ref VARCHAR(100);
    DECLARE v_existing_payment INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result_code = 'ERROR';
        SET p_result_msg = 'Transaction failed. Rolled back.';
    END;

    SELECT total_fare INTO v_fare FROM bookings WHERE booking_id = p_booking_id;
    SELECT COUNT(*) INTO v_existing_payment FROM payments WHERE booking_id = p_booking_id;

    START TRANSACTION;

    -- Update booking status to COMPLETED (triggers completion audit trigger)
    UPDATE bookings 
    SET service_status = 'COMPLETED',
        completed_at = NOW()
    WHERE booking_id = p_booking_id;

    SET v_txn_ref = CONCAT('TXN-', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), '-', FLOOR(RAND()*1000));

    IF v_existing_payment = 0 THEN
        INSERT INTO payments (
            booking_id, amount, payment_method, transaction_ref, payment_status, paid_at
        ) VALUES (
            p_booking_id, v_fare, p_payment_method, v_txn_ref, 'SUCCESS', NOW()
        );
    ELSE
        UPDATE payments 
        SET payment_status = 'SUCCESS',
            payment_method = p_payment_method,
            transaction_ref = v_txn_ref,
            paid_at = NOW()
        WHERE booking_id = p_booking_id;
    END IF;

    COMMIT;

    SET p_result_code = 'SUCCESS';
    SET p_result_msg = 'Luggage journey completed and payment recorded successfully.';
END$$

-- Procedure 4: Analytical Report on Porters Above Station Average (Demonstrates Subqueries)
DROP PROCEDURE IF EXISTS `sp_get_high_performing_porters`$$
CREATE PROCEDURE `sp_get_high_performing_porters`(
    IN p_station_id INT
)
BEGIN
    -- Subquery: Porters whose completed rides exceed the average completed rides at this station
    SELECT 
        po.porter_id,
        po.badge_number,
        po.full_name,
        po.total_rides,
        po.rating_avg,
        po.total_earnings
    FROM porters po
    WHERE po.station_id = p_station_id
      AND po.total_rides >= (
          SELECT AVG(sub.total_rides) 
          FROM porters sub 
          WHERE sub.station_id = p_station_id
      )
    ORDER BY po.total_rides DESC;
END$$

DELIMITER ;
