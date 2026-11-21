-- =====================================================================
-- SMART PORTER: RELATIONAL DATABASE SCHEMA (3NF NORMALIZED)
-- File: database/schema.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `smart_porter` 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `smart_porter`;

-- Drop existing tables in reverse dependency order if needed
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `complaints`;
DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `booking_status_history`;
DROP TABLE IF EXISTS `feedback`;
DROP TABLE IF EXISTS `payments`;
DROP TABLE IF EXISTS `luggage`;
DROP TABLE IF EXISTS `bookings`;
DROP TABLE IF EXISTS `porter_availability`;
DROP TABLE IF EXISTS `pricing_rules`;
DROP TABLE IF EXISTS `station_locations`;
DROP TABLE IF EXISTS `stations`;
DROP TABLE IF EXISTS `admins`;
DROP TABLE IF EXISTS `porters`;
DROP TABLE IF EXISTS `passengers`;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Passengers Table
CREATE TABLE `passengers` (
    `passenger_id` INT AUTO_INCREMENT PRIMARY KEY,
    `full_name` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(15) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `gender` ENUM('Male', 'Female', 'Other') DEFAULT 'Male',
    `is_senior_citizen` BOOLEAN DEFAULT FALSE,
    `status` ENUM('ACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Railway Stations Table
CREATE TABLE `stations` (
    `station_id` INT AUTO_INCREMENT PRIMARY KEY,
    `station_code` VARCHAR(10) NOT NULL UNIQUE,
    `station_name` VARCHAR(100) NOT NULL,
    `city` VARCHAR(50) NOT NULL,
    `state` VARCHAR(50) NOT NULL,
    `total_platforms` INT NOT NULL DEFAULT 10,
    `is_active` BOOLEAN DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. Station Locations Table (Gates, Platforms, FOBs, Waiting Halls)
CREATE TABLE `station_locations` (
    `location_id` INT AUTO_INCREMENT PRIMARY KEY,
    `station_id` INT NOT NULL,
    `location_name` VARCHAR(120) NOT NULL,
    `location_type` ENUM('GATE', 'PLATFORM', 'PARKING', 'LOUNGE', 'OVERBRIDGE', 'EXIT') NOT NULL,
    `platform_number` INT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_loc_station` FOREIGN KEY (`station_id`) 
        REFERENCES `stations`(`station_id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Porters Table (Sahayaks/Coolies)
CREATE TABLE `porters` (
    `porter_id` INT AUTO_INCREMENT PRIMARY KEY,
    `badge_number` VARCHAR(30) NOT NULL UNIQUE,
    `full_name` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(15) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `station_id` INT NOT NULL,
    `verification_status` ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    `rating_avg` DECIMAL(3,2) DEFAULT 5.00,
    `total_completed_bookings` INT DEFAULT 0,
    `total_earnings` DECIMAL(10,2) DEFAULT 0.00,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_porter_station` FOREIGN KEY (`station_id`) 
        REFERENCES `stations`(`station_id`) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 5. Porter Availability Table (Online/Busy State & Heartbeat)
CREATE TABLE `porter_availability` (
    `availability_id` INT AUTO_INCREMENT PRIMARY KEY,
    `porter_id` INT NOT NULL UNIQUE,
    `is_online` BOOLEAN DEFAULT FALSE,
    `is_busy` BOOLEAN DEFAULT FALSE,
    `current_platform` INT DEFAULT 1,
    `last_heartbeat` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_avail_porter` FOREIGN KEY (`porter_id`) 
        REFERENCES `porters`(`porter_id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. Admins Table
CREATE TABLE `admins` (
    `admin_id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `role` VARCHAR(50) DEFAULT 'ADMIN',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 7. Pricing Rules Table
CREATE TABLE `pricing_rules` (
    `rule_id` INT AUTO_INCREMENT PRIMARY KEY,
    `station_id` INT NULL, -- NULL represents Global Default Rule
    `luggage_category` VARCHAR(50) NOT NULL,
    `base_charge` DECIMAL(8,2) NOT NULL DEFAULT 100.00,
    `rate_per_kg` DECIMAL(6,2) NOT NULL DEFAULT 4.00,
    `fragile_surcharge` DECIMAL(6,2) NOT NULL DEFAULT 30.00,
    `platform_change_fee` DECIMAL(6,2) NOT NULL DEFAULT 20.00,
    `is_active` BOOLEAN DEFAULT TRUE,
    CONSTRAINT `fk_rule_station` FOREIGN KEY (`station_id`) 
        REFERENCES `stations`(`station_id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 8. Bookings Table (Central Relational Entity)
CREATE TABLE `bookings` (
    `booking_id` INT AUTO_INCREMENT PRIMARY KEY,
    `booking_code` VARCHAR(35) NOT NULL UNIQUE,
    `passenger_id` INT NOT NULL,
    `porter_id` INT NULL,
    `station_id` INT NOT NULL,
    `pickup_location_id` INT NOT NULL,
    `drop_location_id` INT NOT NULL,
    `train_number` VARCHAR(30) NULL,
    `train_name` VARCHAR(100) NULL,
    `coach_number` VARCHAR(20) NULL,
    `seat_number` VARCHAR(20) NULL,
    `booking_status` ENUM('REQUESTED', 'ACCEPTED', 'PORTER_ARRIVING', 'SERVICE_STARTED', 'COMPLETED', 'CANCELLED') DEFAULT 'REQUESTED',
    `total_amount` DECIMAL(8,2) NOT NULL DEFAULT 0.00,
    `booking_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `service_start_time` DATETIME NULL,
    `completed_time` DATETIME NULL,
    `cancellation_reason` VARCHAR(255) NULL,
    CONSTRAINT `fk_bk_passenger` FOREIGN KEY (`passenger_id`) 
        REFERENCES `passengers`(`passenger_id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_bk_porter` FOREIGN KEY (`porter_id`) 
        REFERENCES `porters`(`porter_id`) ON DELETE SET NULL,
    CONSTRAINT `fk_bk_station` FOREIGN KEY (`station_id`) 
        REFERENCES `stations`(`station_id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_bk_pickup` FOREIGN KEY (`pickup_location_id`) 
        REFERENCES `station_locations`(`location_id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_bk_drop` FOREIGN KEY (`drop_location_id`) 
        REFERENCES `station_locations`(`location_id`) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 9. Luggage Table
CREATE TABLE `luggage` (
    `luggage_id` INT AUTO_INCREMENT PRIMARY KEY,
    `booking_id` INT NOT NULL,
    `category` VARCHAR(50) NOT NULL,
    `bag_count` INT NOT NULL DEFAULT 1,
    `weight_kg` DECIMAL(6,2) NOT NULL,
    `is_fragile` BOOLEAN DEFAULT FALSE,
    `special_instructions` VARCHAR(255) NULL,
    CONSTRAINT `fk_luggage_booking` FOREIGN KEY (`booking_id`) 
        REFERENCES `bookings`(`booking_id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 10. Payments Table
CREATE TABLE `payments` (
    `payment_id` INT AUTO_INCREMENT PRIMARY KEY,
    `booking_id` INT NOT NULL UNIQUE,
    `amount` DECIMAL(8,2) NOT NULL,
    `payment_method` ENUM('CASH', 'ONLINE_DEMO', 'UPI', 'CARD') DEFAULT 'ONLINE_DEMO',
    `transaction_reference` VARCHAR(100) NOT NULL UNIQUE,
    `payment_status` ENUM('PENDING', 'SUCCESS', 'FAILED') DEFAULT 'PENDING',
    `payment_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_payment_booking` FOREIGN KEY (`booking_id`) 
        REFERENCES `bookings`(`booking_id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 11. Feedback Table
CREATE TABLE `feedback` (
    `feedback_id` INT AUTO_INCREMENT PRIMARY KEY,
    `booking_id` INT NOT NULL UNIQUE,
    `passenger_id` INT NOT NULL,
    `porter_id` INT NOT NULL,
    `rating` INT NOT NULL,
    `comments` TEXT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `chk_rating_val` CHECK (`rating` BETWEEN 1 AND 5),
    CONSTRAINT `fk_fb_booking` FOREIGN KEY (`booking_id`) 
        REFERENCES `bookings`(`booking_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_fb_passenger` FOREIGN KEY (`passenger_id`) 
        REFERENCES `passengers`(`passenger_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_fb_porter` FOREIGN KEY (`porter_id`) 
        REFERENCES `porters`(`porter_id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 12. Booking Status History (Audit Trail)
CREATE TABLE `booking_status_history` (
    `history_id` INT AUTO_INCREMENT PRIMARY KEY,
    `booking_id` INT NOT NULL,
    `status` VARCHAR(50) NOT NULL,
    `changed_by_role` ENUM('PASSENGER', 'PORTER', 'ADMIN', 'SYSTEM') DEFAULT 'SYSTEM',
    `changed_by_id` INT NULL,
    `notes` VARCHAR(255) NULL,
    `changed_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_hist_booking` FOREIGN KEY (`booking_id`) 
        REFERENCES `bookings`(`booking_id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 13. Notifications Table
CREATE TABLE `notifications` (
    `notification_id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_role` ENUM('PASSENGER', 'PORTER', 'ADMIN') NOT NULL,
    `user_id` INT NOT NULL,
    `title` VARCHAR(150) NOT NULL,
    `message` TEXT NOT NULL,
    `is_read` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 14. Complaints Table
CREATE TABLE `complaints` (
    `complaint_id` INT AUTO_INCREMENT PRIMARY KEY,
    `booking_id` INT NULL,
    `filed_by_role` ENUM('PASSENGER', 'PORTER') NOT NULL,
    `filed_by_id` INT NOT NULL,
    `subject` VARCHAR(150) NOT NULL,
    `description` TEXT NOT NULL,
    `status` ENUM('OPEN', 'IN_INVESTIGATION', 'RESOLVED', 'CLOSED') DEFAULT 'OPEN',
    `admin_notes` TEXT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_comp_booking` FOREIGN KEY (`booking_id`) 
        REFERENCES `bookings`(`booking_id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Indexes for optimal lookup and joins
CREATE INDEX `idx_bk_station_status` ON `bookings` (`station_id`, `booking_status`);
CREATE INDEX `idx_bk_passenger` ON `bookings` (`passenger_id`);
CREATE INDEX `idx_bk_porter` ON `bookings` (`porter_id`);
CREATE INDEX `idx_loc_station` ON `station_locations` (`station_id`);
CREATE INDEX `idx_avail_porter` ON `porter_availability` (`porter_id`, `is_online`, `is_busy`);
CREATE INDEX `idx_notif_user` ON `notifications` (`user_role`, `user_id`, `is_read`);
