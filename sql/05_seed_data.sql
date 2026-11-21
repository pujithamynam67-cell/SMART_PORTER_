-- =====================================================================
-- SMART PORTER - SMART LUGGAGE MANAGEMENT SYSTEM AT RAILWAY STATIONS
-- File: 05_seed_data.sql
-- Description: Realistic Seed Data for Testing and Demo
-- =====================================================================

USE `smart_porter`;

-- Default password hash for seed users ('password123' hashed with SHA-256):
-- ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f

-- 1. Insert Railway Stations
INSERT INTO `stations` (`station_id`, `station_code`, `station_name`, `city`, `state`, `total_platforms`, `base_porter_fare`, `per_kg_rate`) VALUES
(1, 'NDLS', 'New Delhi Railway Station', 'New Delhi', 'Delhi', 16, 120.00, 5.00),
(2, 'CSMT', 'Chhatrapati Shivaji Maharaj Terminus', 'Mumbai', 'Maharashtra', 18, 130.00, 6.00),
(3, 'HWH', 'Howrah Junction', 'Kolkata', 'West Bengal', 23, 110.00, 5.00),
(4, 'MAS', 'MGR Chennai Central', 'Chennai', 'Tamil Nadu', 12, 110.00, 5.00),
(5, 'SBC', 'KSR Bengaluru City Junction', 'Bengaluru', 'Karnataka', 10, 120.00, 6.00),
(6, 'HYB', 'Hyderabad Deccan (Nampally)', 'Hyderabad', 'Telangana', 10, 100.00, 5.00),
(7, 'PNBE', 'Patna Junction', 'Patna', 'Bihar', 10, 90.00, 4.50),
(8, 'ADI', 'Ahmedabad Junction', 'Ahmedabad', 'Gujarat', 12, 110.00, 5.50)
ON DUPLICATE KEY UPDATE `station_name` = VALUES(`station_name`);

-- 2. Insert Licensed Porters (Coolies)
INSERT INTO `porters` (`porter_id`, `badge_number`, `full_name`, `phone`, `password_hash`, `station_id`, `current_platform`, `availability_status`, `rating_avg`, `total_rides`, `total_earnings`) VALUES
(1, 'CK-101', 'Ramesh Kumar', '9876543210', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 1, 1, 'AVAILABLE', 4.90, 142, 28400.00),
(2, 'CK-102', 'Suresh Yadav', '9876543211', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 1, 3, 'AVAILABLE', 4.80, 98, 19600.00),
(3, 'CK-103', 'Mohammad Aslam', '9876543212', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 1, 8, 'AVAILABLE', 4.70, 65, 13000.00),
(4, 'CK-201', 'Vinod Patil', '9876543220', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 2, 2, 'AVAILABLE', 4.95, 210, 45200.00),
(5, 'CK-202', 'Santosh Shinde', '9876543221', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 2, 5, 'AVAILABLE', 4.65, 54, 10800.00),
(6, 'CK-301', 'Bimal Das', '9876543230', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 3, 9, 'AVAILABLE', 4.85, 120, 24000.00),
(7, 'CK-401', 'Murugan Shanmugam', '9876543240', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 4, 1, 'AVAILABLE', 4.90, 160, 32000.00),
(8, 'CK-501', 'Manjunath Gowda', '9876543250', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 5, 4, 'AVAILABLE', 4.92, 175, 36500.00),
(9, 'CK-601', 'K. Venkateshwarlu', '9876543260', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 6, 1, 'AVAILABLE', 4.88, 130, 26000.00)
ON DUPLICATE KEY UPDATE `full_name` = VALUES(`full_name`);

-- 3. Insert Passengers
INSERT INTO `passengers` (`passenger_id`, `full_name`, `phone`, `email`, `password_hash`, `gender`, `is_senior_citizen`) VALUES
(1, 'Rajesh Sharma', '9811002233', 'rajesh@example.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Male', FALSE),
(2, 'Priya Sundaram', '9822003344', 'priya@example.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Female', FALSE),
(3, 'Ananya Mukherjee', '9833004455', 'ananya@example.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Female', TRUE),
(4, 'Vikramaditya Singh', '9844005566', 'vikram@example.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Male', FALSE)
ON DUPLICATE KEY UPDATE `full_name` = VALUES(`full_name`);

-- 4. Insert Admin
INSERT INTO `admins` (`admin_id`, `username`, `email`, `password_hash`, `full_name`, `role`) VALUES
(1, 'admin', 'admin@smartporter.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Railway Station Duty Manager', 'SUPER_ADMIN')
ON DUPLICATE KEY UPDATE `username` = VALUES(`username`);

-- 5. Insert Sample Historical Bookings with Luggage & Payments
INSERT INTO `bookings` (`booking_id`, `booking_code`, `passenger_id`, `porter_id`, `station_id`, `train_number`, `train_name`, `coach_number`, `pickup_point`, `drop_point`, `booking_date`, `service_status`, `total_fare`, `completed_at`) VALUES
(1, 'SP-HIST-001', 1, 1, 1, '12004', 'Lucknow Shatabdi', 'C4', 'Paharganj Main Gate', 'Platform 1 - Coach C4', NOW() - INTERVAL 2 DAY, 'COMPLETED', 245.00, NOW() - INTERVAL 2 DAY + INTERVAL 30 MINUTE),
(2, 'SP-HIST-002', 2, 4, 2, '12138', 'Punjab Mail', 'B2', 'Suburban Concourse Gate', 'Platform 6 - Coach B2', NOW() - INTERVAL 1 DAY, 'COMPLETED', 310.00, NOW() - INTERVAL 1 DAY + INTERVAL 40 MINUTE),
(3, 'SP-HIST-003', 3, 8, 5, '12658', 'KSR Bengaluru - Chennai Mail', 'HA1', 'Main Entry Porch', 'Platform 1 - Coach HA1', NOW() - INTERVAL 5 HOUR, 'COMPLETED', 280.00, NOW() - INTERVAL 4 HOUR);

-- Luggage records
INSERT INTO `luggage` (`booking_id`, `luggage_type`, `quantity`, `total_weight_kg`, `is_fragile`, `notes`) VALUES
(1, 'Heavy Suitcase', 2, 25.00, FALSE, 'Heavy clothing suitcase'),
(2, 'Trolley Bag', 3, 30.00, TRUE, 'Contains glassware gifts'),
(3, 'Box/Carton', 2, 20.00, FALSE, 'Medicines and books');

-- Payment records
INSERT INTO `payments` (`booking_id`, `amount`, `payment_method`, `transaction_ref`, `payment_status`, `paid_at`) VALUES
(1, 245.00, 'UPI', 'TXN-SEED-001-UPI', 'SUCCESS', NOW() - INTERVAL 2 DAY),
(2, 310.00, 'CARD', 'TXN-SEED-002-CARD', 'SUCCESS', NOW() - INTERVAL 1 DAY),
(3, 280.00, 'UPI', 'TXN-SEED-003-UPI', 'SUCCESS', NOW() - INTERVAL 4 HOUR);

-- Feedback records
INSERT INTO `feedback` (`booking_id`, `passenger_id`, `porter_id`, `rating`, `comments`) VALUES
(1, 1, 1, 5, 'Extremely helpful! Carried both bags quickly up the overbridge to Platform 1.'),
(2, 2, 4, 5, 'Polite coolie, arrived within 2 minutes of booking near gate. Very safe with fragile items.'),
(3, 3, 8, 5, 'Helped my elderly mother board coach HA1 safely. Great initiative by Indian Railways!');
