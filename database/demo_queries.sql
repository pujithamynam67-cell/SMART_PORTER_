-- =====================================================================
-- SMART PORTER: ADVANCED DBMS DEMONSTRATION QUERIES
-- File: database/demo_queries.sql
-- Description: Practical SQL showcase for BTech DBMS Project Evaluation
-- =====================================================================

USE `smart_porter`;

-- 1. MULTI-TABLE INNER JOIN: Complete Booking Dossier
-- Combines Bookings, Passengers, Porters, Stations, Luggage, Payments
SELECT 
    b.booking_code,
    p.full_name AS passenger_name,
    p.phone AS passenger_phone,
    po.badge_number AS porter_badge,
    po.full_name AS porter_name,
    s.station_code,
    s.station_name,
    l.category AS luggage_category,
    l.bag_count,
    l.weight_kg,
    b.total_amount AS total_fare,
    py.payment_method,
    py.payment_status
FROM bookings b
INNER JOIN passengers p ON b.passenger_id = p.passenger_id
INNER JOIN porters po ON b.porter_id = po.porter_id
INNER JOIN stations s ON b.station_id = s.station_id
INNER JOIN luggage l ON b.booking_id = l.booking_id
INNER JOIN payments py ON b.booking_id = py.booking_id
ORDER BY b.booking_id DESC;

-- 2. LEFT JOIN: All Registered Stations and Their Active Bookings
SELECT 
    s.station_code,
    s.station_name,
    COUNT(b.booking_id) AS total_bookings_received,
    COALESCE(SUM(b.total_amount), 0.00) AS total_revenue
FROM stations s
LEFT JOIN bookings b ON s.station_id = b.station_id
GROUP BY s.station_id, s.station_code, s.station_name;

-- 3. RIGHT JOIN: Porters and their Availability Status (Even if unassigned)
SELECT 
    po.badge_number,
    po.full_name,
    pa.is_online,
    pa.is_busy,
    pa.current_platform
FROM porters po
RIGHT JOIN porter_availability pa ON po.porter_id = pa.porter_id
WHERE po.verification_status = 'APPROVED';

-- 4. NON-CORRELATED SUBQUERY: Porters with more completed trips than the National Average
SELECT 
    badge_number,
    full_name,
    rating_avg,
    total_completed_bookings,
    total_earnings
FROM porters
WHERE total_completed_bookings > (
    SELECT AVG(total_completed_bookings) FROM porters
)
ORDER BY total_completed_bookings DESC;

-- 5. CORRELATED SUBQUERY: Highest earning porter per railway station
SELECT 
    p1.station_id,
    s.station_name,
    p1.badge_number,
    p1.full_name,
    p1.total_earnings
FROM porters p1
JOIN stations s ON p1.station_id = s.station_id
WHERE p1.total_earnings = (
    SELECT MAX(p2.total_earnings)
    FROM porters p2
    WHERE p2.station_id = p1.station_id
);

-- 6. AGGREGATE FUNCTIONS WITH GROUP BY & HAVING:
-- Stations handling more than 50 kg of total luggage
SELECT 
    s.station_code,
    s.station_name,
    COUNT(b.booking_id) AS trips_count,
    SUM(l.weight_kg) AS total_luggage_weight_kg,
    AVG(b.total_amount) AS avg_fare_per_trip
FROM stations s
JOIN bookings b ON s.station_id = b.station_id
JOIN luggage l ON b.booking_id = l.booking_id
GROUP BY s.station_id, s.station_code, s.station_name
HAVING total_luggage_weight_kg > 20.00;

-- 7. DATABASE FUNCTION INVOCATION: Test Dynamic Fare Engine
SELECT 
    'NDLS' AS station,
    'Heavy Suitcase' AS category,
    35.00 AS weight_kg,
    2 AS bags,
    TRUE AS fragile,
    fn_calculate_luggage_fare(1, 'Heavy Suitcase', 35.00, 2, TRUE) AS calculated_fare;

-- 8. QUERYING DATABASE VIEWS:
SELECT * FROM vw_active_bookings;
SELECT * FROM vw_porter_earnings;
SELECT * FROM vw_station_performance;
SELECT * FROM vw_admin_booking_details LIMIT 5;

-- 9. DCL (Data Control Language) Examples:
-- GRANT SELECT, INSERT, UPDATE ON smart_porter.bookings TO 'railway_operator'@'localhost';
-- REVOKE DELETE ON smart_porter.* FROM 'railway_operator'@'localhost';

-- 10. TCL (Transaction Control Language) Demonstration:
-- START TRANSACTION;
-- UPDATE bookings SET booking_status = 'CANCELLED' WHERE booking_id = 999;
-- ROLLBACK;
