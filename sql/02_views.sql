-- =====================================================================
-- SMART PORTER - SMART LUGGAGE MANAGEMENT SYSTEM AT RAILWAY STATIONS
-- File: 02_views.sql
-- Description: Database Views for Real-Time Monitoring and Analytics
-- =====================================================================

USE `smart_porter`;

-- View 1: Real-time Active Bookings (Rapido/Uber Live Feed)
CREATE OR REPLACE VIEW `vw_active_bookings` AS
SELECT 
    b.booking_id,
    b.booking_code,
    b.service_status,
    b.booking_date,
    p.full_name AS passenger_name,
    p.phone AS passenger_phone,
    s.station_code,
    s.station_name,
    b.train_number,
    b.coach_number,
    b.pickup_point,
    b.drop_point,
    b.total_fare,
    po.porter_id,
    po.badge_number AS porter_badge,
    po.full_name AS porter_name,
    po.phone AS porter_phone,
    l.luggage_type,
    l.quantity AS luggage_count,
    l.total_weight_kg
FROM bookings b
JOIN passengers p ON b.passenger_id = p.passenger_id
JOIN stations s ON b.station_id = s.station_id
LEFT JOIN porters po ON b.porter_id = po.porter_id
LEFT JOIN luggage l ON b.booking_id = l.booking_id
WHERE b.service_status IN ('REQUESTED', 'ACCEPTED', 'PICKED_UP', 'IN_TRANSIT');

-- View 2: Porter Leaderboard & Performance Metrics
CREATE OR REPLACE VIEW `vw_porter_leaderboard` AS
SELECT 
    po.porter_id,
    po.badge_number,
    po.full_name AS porter_name,
    s.station_name,
    s.station_code,
    po.availability_status,
    po.total_rides,
    po.rating_avg,
    po.total_earnings,
    COUNT(f.feedback_id) AS total_reviews_count,
    COALESCE(AVG(f.rating), po.rating_avg) AS calculated_rating
FROM porters po
JOIN stations s ON po.station_id = s.station_id
LEFT JOIN feedback f ON po.porter_id = f.porter_id
GROUP BY po.porter_id, po.badge_number, po.full_name, s.station_name, s.station_code, po.availability_status, po.total_rides, po.rating_avg, po.total_earnings
ORDER BY po.rating_avg DESC, po.total_rides DESC;

-- View 3: Railway Station Luggage & Revenue Analytics
CREATE OR REPLACE VIEW `vw_station_analytics` AS
SELECT 
    s.station_id,
    s.station_code,
    s.station_name,
    s.city,
    COUNT(DISTINCT po.porter_id) AS registered_porters,
    COUNT(DISTINCT CASE WHEN po.availability_status = 'AVAILABLE' THEN po.porter_id END) AS available_porters,
    COUNT(DISTINCT b.booking_id) AS total_bookings_handled,
    COALESCE(SUM(b.total_fare), 0.00) AS total_gross_revenue,
    COALESCE(SUM(l.total_weight_kg), 0.00) AS total_luggage_weight_kg,
    COALESCE(AVG(b.total_fare), 0.00) AS average_booking_value
FROM stations s
LEFT JOIN porters po ON s.station_id = po.station_id
LEFT JOIN bookings b ON s.station_id = b.station_id
LEFT JOIN luggage l ON b.booking_id = l.booking_id
GROUP BY s.station_id, s.station_code, s.station_name, s.city;

-- View 4: Consolidated Passenger Trip Details
CREATE OR REPLACE VIEW `vw_passenger_ride_history` AS
SELECT 
    b.booking_id,
    b.booking_code,
    b.passenger_id,
    p.full_name AS passenger_name,
    b.station_id,
    s.station_code,
    s.station_name,
    b.train_number,
    b.coach_number,
    b.pickup_point,
    b.drop_point,
    b.booking_date,
    b.service_status,
    b.total_fare,
    po.badge_number AS porter_badge,
    po.full_name AS porter_name,
    py.payment_status,
    py.payment_method,
    f.rating AS passenger_rating,
    f.comments AS passenger_review
FROM bookings b
JOIN passengers p ON b.passenger_id = p.passenger_id
JOIN stations s ON b.station_id = s.station_id
LEFT JOIN porters po ON b.porter_id = po.porter_id
LEFT JOIN payments py ON b.booking_id = py.booking_id
LEFT JOIN feedback f ON b.booking_id = f.booking_id;
