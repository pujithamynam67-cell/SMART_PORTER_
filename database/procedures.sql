-- =====================================================================
-- SMART PORTER: STORED PROCEDURES (ACID TRANSACTIONS)
-- File: database/procedures.sql
-- =====================================================================

USE `smart_porter`;

DELIMITER $$

-- Procedure 1: Create Booking with Luggage & Status Audit
DROP PROCEDURE IF EXISTS `sp_create_smart_booking`$$
CREATE PROCEDURE `sp_create_smart_booking`(
    IN p_passenger_id INT,
    IN p_station_id INT,
    IN p_pickup_loc_id INT,
    IN p_drop_loc_id INT,
    IN p_train_no VARCHAR(30),
    IN p_train_name VARCHAR(100),
    IN p_coach VARCHAR(20),
    IN p_seat VARCHAR(20),
    IN p_category VARCHAR(50),
    IN p_bag_count INT,
    IN p_weight_kg DECIMAL(6,2),
    IN p_is_fragile BOOLEAN,
    IN p_notes VARCHAR(255),
    IN p_requested_porter_id INT,
    OUT p_booking_id INT,
    OUT p_booking_code VARCHAR(35),
    OUT p_total_amount DECIMAL(8,2)
)
proc_label: BEGIN
    DECLARE v_code VARCHAR(35);
    DECLARE v_calculated_fare DECIMAL(8,2);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    -- Calculate fare using stored function
    SET v_calculated_fare = fn_calculate_luggage_fare(p_station_id, p_category, p_weight_kg, p_bag_count, p_is_fragile);
    SET v_code = CONCAT('SP-', DATE_FORMAT(NOW(), '%Y%m%d'), '-', LPAD(FLOOR(RAND() * 100000), 5, '0'));

    START TRANSACTION;

    INSERT INTO bookings (
        booking_code, passenger_id, porter_id, station_id,
        pickup_location_id, drop_location_id, train_number, train_name,
        coach_number, seat_number, booking_status, total_amount, booking_time
    ) VALUES (
        v_code, p_passenger_id, IF(p_requested_porter_id > 0, p_requested_porter_id, NULL),
        p_station_id, p_pickup_loc_id, p_drop_loc_id, p_train_no, p_train_name,
        p_coach, p_seat, 'REQUESTED', v_calculated_fare, NOW()
    );

    SET p_booking_id = LAST_INSERT_ID();
    SET p_booking_code = v_code;
    SET p_total_amount = v_calculated_fare;

    -- Insert Luggage
    INSERT INTO luggage (
        booking_id, category, bag_count, weight_kg, is_fragile, special_instructions
    ) VALUES (
        p_booking_id, p_category, p_bag_count, p_weight_kg, p_is_fragile, p_notes
    );

    -- Log status history
    INSERT INTO booking_status_history (
        booking_id, status, changed_by_role, changed_by_id, notes
    ) VALUES (
        p_booking_id, 'REQUESTED', 'PASSENGER', p_passenger_id, 'Luggage booking requested by passenger'
    );

    -- Notify porters at this station
    IF p_requested_porter_id > 0 THEN
        INSERT INTO notifications (user_role, user_id, title, message)
        VALUES ('PORTER', p_requested_porter_id, 'New Luggage Booking Request', 
                CONCAT('Direct booking request ', v_code, ' received. Fare: Rs.', v_calculated_fare));
    END IF;

    COMMIT;
END$$

-- Procedure 2: Find Available Porters at Station
DROP PROCEDURE IF EXISTS `sp_get_available_porters`$$
CREATE PROCEDURE `sp_get_available_porters`(
    IN p_station_id INT
)
BEGIN
    SELECT 
        po.porter_id,
        po.badge_number,
        po.full_name,
        po.phone,
        po.rating_avg,
        po.total_completed_bookings,
        pa.current_platform,
        pa.is_online,
        pa.is_busy,
        s.station_name,
        s.station_code
    FROM porters po
    JOIN porter_availability pa ON po.porter_id = pa.porter_id
    JOIN stations s ON po.station_id = s.station_id
    WHERE po.station_id = p_station_id
      AND po.verification_status = 'APPROVED'
      AND pa.is_online = TRUE
      AND pa.is_busy = FALSE
    ORDER BY po.rating_avg DESC, po.total_completed_bookings DESC;
END$$

-- Procedure 3: Update Booking Status (Transactional with Porter State Synchronization)
DROP PROCEDURE IF EXISTS `sp_update_booking_status`$$
CREATE PROCEDURE `sp_update_booking_status`(
    IN p_booking_id INT,
    IN p_new_status VARCHAR(50),
    IN p_acting_role VARCHAR(20),
    IN p_acting_id INT,
    IN p_notes VARCHAR(255),
    OUT p_success BOOLEAN,
    OUT p_message VARCHAR(100)
)
proc_label: BEGIN
    DECLARE v_porter_id INT DEFAULT NULL;
    DECLARE v_current_status VARCHAR(50);
    DECLARE v_fare DECIMAL(8,2);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_success = FALSE;
        SET p_message = 'Database error occurred during status update.';
    END;

    SELECT porter_id, booking_status, total_amount
    INTO v_porter_id, v_current_status, v_fare
    FROM bookings
    WHERE booking_id = p_booking_id;

    IF v_current_status IS NULL THEN
        SET p_success = FALSE;
        SET p_message = 'Booking record not found.';
        LEAVE proc_label;
    END IF;

    START TRANSACTION;

    -- Update booking status
    UPDATE bookings 
    SET booking_status = p_new_status,
        service_start_time = IF(p_new_status = 'SERVICE_STARTED' AND service_start_time IS NULL, NOW(), service_start_time),
        completed_time = IF(p_new_status = 'COMPLETED', NOW(), completed_time)
    WHERE booking_id = p_booking_id;

    -- Record Audit Log
    INSERT INTO booking_status_history (
        booking_id, status, changed_by_role, changed_by_id, notes
    ) VALUES (
        p_booking_id, p_new_status, p_acting_role, p_acting_id, p_notes
    );

    -- Manage Porter Busy/Available Flag
    IF v_porter_id IS NOT NULL THEN
        IF p_new_status IN ('ACCEPTED', 'PORTER_ARRIVING', 'SERVICE_STARTED') THEN
            UPDATE porter_availability SET is_busy = TRUE WHERE porter_id = v_porter_id;
        ELSEIF p_new_status IN ('COMPLETED', 'CANCELLED') THEN
            UPDATE porter_availability SET is_busy = FALSE WHERE porter_id = v_porter_id;
            
            IF p_new_status = 'COMPLETED' THEN
                -- Credit 90% net coolie share
                UPDATE porters 
                SET total_completed_bookings = total_completed_bookings + 1,
                    total_earnings = total_earnings + (v_fare * 0.90)
                WHERE porter_id = v_porter_id;
            END IF;
        END IF;
    END IF;

    COMMIT;
    SET p_success = TRUE;
    SET p_message = CONCAT('Status updated successfully to ', p_new_status);
END$$

-- Procedure 4: Porter Accepts Booking with Row-Level Locking
DROP PROCEDURE IF EXISTS `sp_porter_accept_booking`$$
CREATE PROCEDURE `sp_porter_accept_booking`(
    IN p_booking_id INT,
    IN p_porter_id INT,
    OUT p_success BOOLEAN,
    OUT p_message VARCHAR(100)
)
proc_label: BEGIN
    DECLARE v_status VARCHAR(50);
    DECLARE v_current_porter INT;
    DECLARE v_is_busy BOOLEAN;
    DECLARE v_verification VARCHAR(20);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_success = FALSE;
        SET p_message = 'Transaction rolled back due to error.';
    END;

    START TRANSACTION;

    -- Verify porter is approved and not already busy
    SELECT verification_status INTO v_verification FROM porters WHERE porter_id = p_porter_id;
    IF v_verification != 'APPROVED' THEN
        ROLLBACK;
        SET p_success = FALSE;
        SET p_message = 'Only approved porters can accept bookings.';
        LEAVE proc_label;
    END IF;

    SELECT is_busy INTO v_is_busy FROM porter_availability WHERE porter_id = p_porter_id FOR UPDATE;
    IF v_is_busy = TRUE THEN
        ROLLBACK;
        SET p_success = FALSE;
        SET p_message = 'You already have an active luggage booking in progress.';
        LEAVE proc_label;
    END IF;

    -- Lock the booking row
    SELECT booking_status, porter_id INTO v_status, v_current_porter
    FROM bookings 
    WHERE booking_id = p_booking_id 
    FOR UPDATE;

    IF v_status != 'REQUESTED' THEN
        ROLLBACK;
        SET p_success = FALSE;
        SET p_message = 'This booking is no longer in REQUESTED state.';
        LEAVE proc_label;
    END IF;

    -- Assign porter and accept
    UPDATE bookings 
    SET porter_id = p_porter_id,
        booking_status = 'ACCEPTED'
    WHERE booking_id = p_booking_id;

    UPDATE porter_availability 
    SET is_busy = TRUE 
    WHERE porter_id = p_porter_id;

    INSERT INTO booking_status_history (
        booking_id, status, changed_by_role, changed_by_id, notes
    ) VALUES (
        p_booking_id, 'ACCEPTED', 'PORTER', p_porter_id, 'Booking accepted by porter'
    );

    COMMIT;
    SET p_success = TRUE;
    SET p_message = 'Booking accepted successfully.';
END$$

DELIMITER ;
