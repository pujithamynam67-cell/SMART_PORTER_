-- =====================================================================
-- SMART PORTER: REALISTIC SEED DATA (INDIAN RAILWAY NETWORK)
-- File: database/sample_data.sql
-- =====================================================================

USE `smart_porter`;

-- Standard BCrypt Hash for 'password123' (12 rounds)
-- $2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

-- 1. Railway Stations
INSERT INTO `stations` (`station_id`, `station_code`, `station_name`, `city`, `state`, `total_platforms`, `is_active`) VALUES
(1, 'NDLS', 'New Delhi Railway Station', 'New Delhi', 'Delhi', 16, TRUE),
(2, 'CSMT', 'Chhatrapati Shivaji Maharaj Terminus', 'Mumbai', 'Maharashtra', 18, TRUE),
(3, 'HWH', 'Howrah Junction', 'Kolkata', 'West Bengal', 23, TRUE),
(4, 'MAS', 'MGR Chennai Central', 'Chennai', 'Tamil Nadu', 12, TRUE),
(5, 'SBC', 'KSR Bengaluru City Junction', 'Bengaluru', 'Karnataka', 10, TRUE),
(6, 'HYB', 'Hyderabad Deccan (Nampally)', 'Hyderabad', 'Telangana', 10, TRUE),
(7, 'PNBE', 'Patna Junction', 'Patna', 'Bihar', 10, TRUE),
(8, 'ADI', 'Ahmedabad Junction', 'Ahmedabad', 'Gujarat', 12, TRUE)
ON DUPLICATE KEY UPDATE `station_name` = VALUES(`station_name`);

-- 2. Station Locations (Key Pickup & Drop Points Inside Stations)
INSERT INTO `station_locations` (`location_id`, `station_id`, `location_name`, `location_type`, `platform_number`) VALUES
-- NDLS
(1, 1, 'Main Paharganj Entrance Gate 1', 'GATE', NULL),
(2, 1, 'Ajmeri Gate Concourse (Metro Side)', 'GATE', NULL),
(3, 1, 'Prepaid Auto / Taxi Stand North', 'PARKING', NULL),
(4, 1, 'Platform 1 - AC Waiting Lounge Area', 'PLATFORM', 1),
(5, 1, 'Platform 3 - Foot Overbridge Stairs', 'OVERBRIDGE', 3),
(6, 1, 'Platform 8 - Mid Train Position', 'PLATFORM', 8),
(7, 1, 'Platform 16 - Ajmeri Gate Escalator', 'PLATFORM', 16),
-- CSMT
(8, 2, 'Main Heritage Concourse Gate 1', 'GATE', NULL),
(9, 2, 'Suburban Cab Stand Gate 3', 'PARKING', NULL),
(10, 2, 'Platform 1 - Long Distance Express Mid', 'PLATFORM', 1),
(11, 2, 'Platform 8 - Central Hall FOB', 'PLATFORM', 8),
-- HWH
(12, 3, 'Old Complex Main Portico Gate', 'GATE', NULL),
(13, 3, 'Platform 8 - Howrah Shatabdi Berth', 'PLATFORM', 8),
(14, 3, 'Platform 18 - New Complex FOB', 'OVERBRIDGE', 18),
-- MAS
(15, 4, 'Main Central Porch Gate', 'GATE', NULL),
(16, 4, 'Platform 1 - Vande Bharat Departure Bay', 'PLATFORM', 1),
-- SBC
(17, 5, 'Main Majestic Concourse Entry', 'GATE', NULL),
(18, 5, 'Platform 1 - Mail Express Boarding Bay', 'PLATFORM', 1),
-- HYB
(19, 6, 'Nampally Station Front Porch', 'GATE', NULL),
(20, 6, 'Platform 1 - Godavari Exp Bay', 'PLATFORM', 1)
ON DUPLICATE KEY UPDATE `location_name` = VALUES(`location_name`);

-- 3. Pricing Rules
INSERT INTO `pricing_rules` (`rule_id`, `station_id`, `luggage_category`, `base_charge`, `rate_per_kg`, `fragile_surcharge`, `platform_change_fee`, `is_active`) VALUES
(1, NULL, 'Standard Bag', 100.00, 4.00, 30.00, 20.00, TRUE),
(2, NULL, 'Trolley Case', 120.00, 5.00, 30.00, 20.00, TRUE),
(3, NULL, 'Heavy Suitcase', 140.00, 6.00, 40.00, 25.00, TRUE),
(4, NULL, 'Fragile Carton', 150.00, 5.50, 50.00, 25.00, TRUE),
(5, 1, 'Standard Bag', 120.00, 5.00, 30.00, 20.00, TRUE), -- NDLS specific
(6, 2, 'Standard Bag', 130.00, 5.50, 30.00, 25.00, TRUE)  -- CSMT specific
ON DUPLICATE KEY UPDATE `base_charge` = VALUES(`base_charge`);

-- 4. Admins
INSERT INTO `admins` (`admin_id`, `username`, `email`, `password_hash`, `full_name`, `role`) VALUES
(1, 'admin', 'admin@smartporter.com', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'IRCTC Station Duty Controller', 'ADMIN')
ON DUPLICATE KEY UPDATE `username` = VALUES(`username`);

-- 5. Passengers
INSERT INTO `passengers` (`passenger_id`, `full_name`, `phone`, `email`, `password_hash`, `gender`, `is_senior_citizen`, `status`) VALUES
(1, 'Rajesh Sharma', '9811002233', 'rajesh@example.com', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Male', FALSE, 'ACTIVE'),
(2, 'Priya Sundaram', '9822003344', 'priya@example.com', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Female', FALSE, 'ACTIVE'),
(3, 'Ananya Mukherjee', '9833004455', 'ananya@example.com', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Female', TRUE, 'ACTIVE'),
(4, 'Vikramaditya Singh', '9844005566', 'vikram@example.com', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Male', FALSE, 'ACTIVE')
ON DUPLICATE KEY UPDATE `full_name` = VALUES(`full_name`);

-- 6. Porters (Sahayaks/Coolies)
INSERT INTO `porters` (`porter_id`, `badge_number`, `full_name`, `phone`, `email`, `password_hash`, `station_id`, `verification_status`, `rating_avg`, `total_completed_bookings`, `total_earnings`) VALUES
(1, 'CK-101', 'Ramesh Kumar', '9876543210', 'ramesh@porter.in', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 'APPROVED', 4.90, 142, 28400.00),
(2, 'CK-102', 'Suresh Yadav', '9876543211', 'suresh@porter.in', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 'APPROVED', 4.80, 98, 19600.00),
(3, 'CK-103', 'Mohammad Aslam', '9876543212', 'aslam@porter.in', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 'APPROVED', 4.70, 65, 13000.00),
(4, 'CK-201', 'Vinod Patil', '9876543220', 'vinod@porter.in', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 2, 'APPROVED', 4.95, 210, 45200.00),
(5, 'CK-202', 'Santosh Shinde', '9876543221', 'santosh@porter.in', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 2, 'APPROVED', 4.65, 54, 10800.00),
(6, 'CK-301', 'Bimal Das', '9876543230', 'bimal@porter.in', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 3, 'APPROVED', 4.85, 120, 24000.00),
(7, 'CK-401', 'Murugan Shanmugam', '9876543240', 'murugan@porter.in', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 4, 'APPROVED', 4.90, 160, 32000.00),
(8, 'CK-501', 'Manjunath Gowda', '9876543250', 'manjunath@porter.in', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 5, 'APPROVED', 4.92, 175, 36500.00),
(9, 'CK-999', 'Dinesh Rawat (Pending Review)', '9876543299', 'dinesh@porter.in', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 'PENDING', 5.00, 0, 0.00)
ON DUPLICATE KEY UPDATE `full_name` = VALUES(`full_name`);

-- 7. Porter Availability (Set active porters online)
INSERT INTO `porter_availability` (`porter_id`, `is_online`, `is_busy`, `current_platform`) VALUES
(1, TRUE, FALSE, 1),
(2, TRUE, FALSE, 3),
(3, TRUE, FALSE, 8),
(4, TRUE, FALSE, 1),
(5, FALSE, FALSE, 5),
(6, TRUE, FALSE, 8),
(7, TRUE, FALSE, 1),
(8, TRUE, FALSE, 1),
(9, FALSE, FALSE, 1)
ON DUPLICATE KEY UPDATE `is_online` = VALUES(`is_online`), `is_busy` = VALUES(`is_busy`);

-- 8. Sample Historical Bookings
INSERT INTO `bookings` (`booking_id`, `booking_code`, `passenger_id`, `porter_id`, `station_id`, `pickup_location_id`, `drop_location_id`, `train_number`, `train_name`, `coach_number`, `seat_number`, `booking_status`, `total_amount`, `booking_time`, `completed_time`) VALUES
(1, 'SP-2026-HIST01', 1, 1, 1, 1, 4, '12004', 'Lucknow Shatabdi', 'C4', '42', 'COMPLETED', 245.00, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY + INTERVAL 30 MINUTE),
(2, 'SP-2026-HIST02', 2, 4, 2, 8, 10, '12138', 'Punjab Mail', 'B2', '18', 'COMPLETED', 310.00, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY + INTERVAL 40 MINUTE),
(3, 'SP-2026-HIST03', 3, 8, 5, 17, 18, '12658', 'Bengaluru Chennai Mail', 'HA1', '4', 'COMPLETED', 280.00, NOW() - INTERVAL 5 HOUR, NOW() - INTERVAL 4 HOUR);

-- Luggage records
INSERT INTO `luggage` (`booking_id`, `category`, `bag_count`, `weight_kg`, `is_fragile`, `special_instructions`) VALUES
(1, 'Heavy Suitcase', 2, 25.00, FALSE, 'Heavy clothing bags'),
(2, 'Trolley Case', 3, 30.00, TRUE, 'Contains glass souvenir gifts'),
(3, 'Standard Bag', 2, 20.00, FALSE, 'Medicines and books');

-- Payments records
INSERT INTO `payments` (`booking_id`, `amount`, `payment_method`, `transaction_reference`, `payment_status`, `payment_date`) VALUES
(1, 245.00, 'UPI', 'TXN-SP-2026-0001', 'SUCCESS', NOW() - INTERVAL 2 DAY),
(2, 310.00, 'CARD', 'TXN-SP-2026-0002', 'SUCCESS', NOW() - INTERVAL 1 DAY),
(3, 280.00, 'ONLINE_DEMO', 'TXN-SP-2026-0003', 'SUCCESS', NOW() - INTERVAL 4 HOUR);

-- Feedback records
INSERT INTO `feedback` (`booking_id`, `passenger_id`, `porter_id`, `rating`, `comments`) VALUES
(1, 1, 1, 5, 'Ramesh was swift and carried luggage with great care up the stairs!'),
(2, 2, 4, 5, 'Very polite and helpful coolie at Mumbai CSMT.'),
(3, 3, 8, 5, 'Assisted my elderly mother very gently. Thank you Smart Porter!');

-- Booking Status History records
INSERT INTO `booking_status_history` (`booking_id`, `status`, `changed_by_role`, `changed_by_id`, `notes`) VALUES
(1, 'REQUESTED', 'PASSENGER', 1, 'Initial request'),
(1, 'ACCEPTED', 'PORTER', 1, 'Accepted by Ramesh'),
(1, 'SERVICE_STARTED', 'PORTER', 1, 'Luggage loaded'),
(1, 'COMPLETED', 'PORTER', 1, 'Trip finished successfully');
