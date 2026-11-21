-- =====================================================================
-- SMART PORTER: RELATIONAL DATABASE VIEWS
-- File: database/views.sql
-- =====================================================================

USE `smart_porter`;

-- View 1: Active Luggage Bookings (Real-time Live Monitor)
CREATE OR REPLACE VIEW `vw_active_bookings` AS
SELECT 
    b.booking_id,
    b.booking_code,
    b.booking_status,
    b.booking_time,
    b.total_amount,
    p.passenger_id,
    p.full_name AS passenger_name,
    p.phone AS passenger_phone,
    s.station_id,
    s.station_code,
    s.station_name,
    loc_from.location_name AS pickup_location,
    loc_to.location_name AS drop_location,
    b.train_number,
    b.coach_number,
    po.porter_id,
    po.badge_number AS porter_badge,
    po.full_name AS porter_name,
    po.phone AS porter_phone,
    l.category AS luggage_category,
    l.bag_count,
    l.weight_kg,
    l.is_fragile
FROM bookings b
JOIN passengers p ON b.passenger_id = p.passenger_id
JOIN stations s ON b.station_id = s.station_id
JOIN station_locations loc_from ON b.pickup_location_id = loc_from.location_id
JOIN station_locations loc_to ON b.drop_location_id = loc_to.location_id
LEFT JOIN porters po ON b.porter_id = po.porter_id
LEFT JOIN luggage l ON b.booking_id = l.booking_id
WHERE b.booking_status IN ('REQUESTED', 'ACCEPTED', 'PORTER_ARRIVING', 'SERVICE_STARTED');

-- View 2: Porter Performance & Earnings Summary
CREATE OR REPLACE VIEW `vw_porter_earnings` AS
SELECT 
    po.porter_id,
    po.badge_number,
    po.full_name AS porter_name,
    po.phone AS porter_phone,
    s.station_code,
    s.station_name,
    po.verification_status,
    pa.is_online,
    pa.is_busy,
    pa.current_platform,
    po.total_completed_bookings,
    po.total_earnings,
    po.rating_avg,
    COUNT(f.feedback_id) AS total_reviews
FROM porters po
JOIN stations s ON po.station_id = s.station_id
LEFT JOIN porter_availability pa ON po.porter_id = pa.porter_id
LEFT JOIN feedback f ON po.porter_id = f.porter_id
GROUP BY po.porter_id, po.badge_number, po.full_name, po.phone, s.station_code, s.station_name, 
         po.verification_status, pa.is_online, pa.is_busy, pa.current_platform, 
         po.total_completed_bookings, po.total_earnings, po.rating_avg;

-- View 3: Station Level Aggregation & Baggage Load
CREATE OR REPLACE VIEW `vw_station_performance` AS
SELECT 
    s.station_id,
    s.station_code,
    s.station_name,
    s.city,
    s.state,
    COUNT(DISTINCT po.porter_id) AS total_porters_registered,
    COUNT(DISTINCT CASE WHEN pa.is_online = TRUE AND pa.is_busy = FALSE THEN po.porter_id END) AS available_porters_count,
    COUNT(DISTINCT b.booking_id) AS total_bookings_handled,
    COALESCE(SUM(b.total_amount), 0.00) AS total_gross_revenue,
    COALESCE(SUM(l.weight_kg), 0.00) AS total_baggage_weight_kg,
    COALESCE(AVG(b.total_amount), 0.00) AS avg_booking_ticket_size
FROM stations s
LEFT JOIN porters po ON s.station_id = po.station_id
LEFT JOIN porter_availability pa ON po.porter_id = pa.porter_id
LEFT JOIN bookings b ON s.station_id = b.station_id
LEFT JOIN luggage l ON b.booking_id = l.booking_id
GROUP BY s.station_id, s.station_code, s.station_name, s.city, s.state;

-- View 4: Comprehensive Admin Relational Audit View
CREATE OR REPLACE VIEW `vw_admin_booking_details` AS
SELECT 
    b.booking_id,
    b.booking_code,
    b.booking_status,
    b.booking_time,
    b.total_amount,
    p.passenger_id,
    p.full_name AS passenger_name,
    p.phone AS passenger_phone,
    p.email AS passenger_email,
    s.station_code,
    s.station_name,
    loc_from.location_name AS pickup_point,
    loc_to.location_name AS drop_point,
    b.train_number,
    b.coach_number,
    po.porter_id,
    po.badge_number AS porter_badge,
    po.full_name AS porter_name,
    l.category AS luggage_category,
    l.bag_count,
    l.weight_kg,
    py.payment_status,
    py.payment_method,
    py.transaction_reference,
    f.rating AS passenger_rating,
    f.comments AS passenger_review
FROM bookings b
JOIN passengers p ON b.passenger_id = p.passenger_id
JOIN stations s ON b.station_id = s.station_id
JOIN station_locations loc_from ON b.pickup_location_id = loc_from.location_id
JOIN station_locations loc_to ON b.drop_location_id = loc_to.location_id
LEFT JOIN porters po ON b.porter_id = po.porter_id
LEFT JOIN luggage l ON b.booking_id = l.booking_id
LEFT JOIN payments py ON b.booking_id = py.booking_id
LEFT JOIN feedback f ON b.booking_id = f.booking_id;
