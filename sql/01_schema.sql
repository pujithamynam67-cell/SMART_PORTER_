-- =====================================================================
-- SMART PORTER - SMART LUGGAGE MANAGEMENT SYSTEM AT RAILWAY STATIONS
-- File: 01_schema.sql
-- Description: DDL definitions for Core Database Tables and Constraints
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `smart_porter` 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `smart_porter`;

-- 1. Railway Stations Table
CREATE TABLE IF NOT EXISTS `stations` (
    `station_id` INT AUTO_INCREMENT PRIMARY KEY,
    `station_code` VARCHAR(10) NOT NULL UNIQUE,
    `station_name` VARCHAR(100) NOT NULL,
    `city` VARCHAR(50) NOT NULL,
    `state` VARCHAR(50) NOT NULL,
    `total_platforms` INT NOT NULL DEFAULT 10,
    `base_porter_fare` DECIMAL(8,2) NOT NULL DEFAULT 100.00,
    `per_kg_rate` DECIMAL(6,2) NOT NULL DEFAULT 5.00,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Passengers Table
CREATE TABLE IF NOT EXISTS `passengers` (
    `passenger_id` INT AUTO_INCREMENT PRIMARY KEY,
    `full_name` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(15) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `gender` ENUM('Male', 'Female', 'Other') DEFAULT 'Male',
    `is_senior_citizen` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. Porters (Coolies / Sahayaks) Table
CREATE TABLE IF NOT EXISTS `porters` (
    `porter_id` INT AUTO_INCREMENT PRIMARY KEY,
    `badge_number` VARCHAR(20) NOT NULL UNIQUE,
    `full_name` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(15) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `station_id` INT NOT NULL,
    `current_platform` INT DEFAULT 1,
    `availability_status` ENUM('AVAILABLE', 'BUSY', 'OFFLINE') DEFAULT 'AVAILABLE',
    `rating_avg` DECIMAL(3,2) DEFAULT 5.00,
    `total_rides` INT DEFAULT 0,
    `total_earnings` DECIMAL(10,2) DEFAULT 0.00,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_porter_station` FOREIGN KEY (`station_id`) 
        REFERENCES `stations`(`station_id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Bookings Table (Central Entity)
CREATE TABLE IF NOT EXISTS `bookings` (
    `booking_id` INT AUTO_INCREMENT PRIMARY KEY,
    `booking_code` VARCHAR(30) NOT NULL UNIQUE,
    `passenger_id` INT NOT NULL,
    `porter_id` INT NULL,
    `station_id` INT NOT NULL,
    `train_number` VARCHAR(20) NULL,
    `train_name` VARCHAR(100) NULL,
    `coach_number` VARCHAR(15) NULL,
    `pickup_point` VARCHAR(150) NOT NULL,
    `drop_point` VARCHAR(150) NOT NULL,
    `booking_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `service_status` ENUM('REQUESTED', 'ACCEPTED', 'PICKED_UP', 'IN_TRANSIT', 'COMPLETED', 'CANCELLED') DEFAULT 'REQUESTED',
    `total_fare` DECIMAL(8,2) NOT NULL DEFAULT 0.00,
    `porter_assigned_at` DATETIME NULL,
    `completed_at` DATETIME NULL,
    CONSTRAINT `fk_booking_passenger` FOREIGN KEY (`passenger_id`) 
        REFERENCES `passengers`(`passenger_id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_booking_porter` FOREIGN KEY (`porter_id`) 
        REFERENCES `porters`(`porter_id`) ON DELETE SET NULL,
    CONSTRAINT `fk_booking_station` FOREIGN KEY (`station_id`) 
        REFERENCES `stations`(`station_id`) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 5. Luggage Table
CREATE TABLE IF NOT EXISTS `luggage` (
    `luggage_id` INT AUTO_INCREMENT PRIMARY KEY,
    `booking_id` INT NOT NULL,
    `luggage_type` ENUM('Trolley Bag', 'Duffle Bag', 'Heavy Suitcase', 'Backpack', 'Box/Carton') NOT NULL,
    `quantity` INT NOT NULL DEFAULT 1,
    `total_weight_kg` DECIMAL(6,2) NOT NULL,
    `is_fragile` BOOLEAN DEFAULT FALSE,
    `notes` VARCHAR(255) NULL,
    CONSTRAINT `fk_luggage_booking` FOREIGN KEY (`booking_id`) 
        REFERENCES `bookings`(`booking_id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. Payments Table
CREATE TABLE IF NOT EXISTS `payments` (
    `payment_id` INT AUTO_INCREMENT PRIMARY KEY,
    `booking_id` INT NOT NULL UNIQUE,
    `amount` DECIMAL(8,2) NOT NULL,
    `payment_method` ENUM('UPI', 'CARD', 'CASH', 'WALLET') NOT NULL DEFAULT 'UPI',
    `transaction_ref` VARCHAR(100) NOT NULL UNIQUE,
    `payment_status` ENUM('PENDING', 'SUCCESS', 'FAILED') DEFAULT 'PENDING',
    `paid_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_payment_booking` FOREIGN KEY (`booking_id`) 
        REFERENCES `bookings`(`booking_id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. Feedback and Ratings Table
CREATE TABLE IF NOT EXISTS `feedback` (
    `feedback_id` INT AUTO_INCREMENT PRIMARY KEY,
    `booking_id` INT NOT NULL UNIQUE,
    `passenger_id` INT NOT NULL,
    `porter_id` INT NOT NULL,
    `rating` INT NOT NULL,
    `comments` TEXT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `chk_rating_range` CHECK (`rating` BETWEEN 1 AND 5),
    CONSTRAINT `fk_feedback_booking` FOREIGN KEY (`booking_id`) 
        REFERENCES `bookings`(`booking_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_feedback_passenger` FOREIGN KEY (`passenger_id`) 
        REFERENCES `passengers`(`passenger_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_feedback_porter` FOREIGN KEY (`porter_id`) 
        REFERENCES `porters`(`porter_id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 8. Administrators Table
CREATE TABLE IF NOT EXISTS `admins` (
    `admin_id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `role` VARCHAR(50) DEFAULT 'SUPER_ADMIN',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 9. Porter Earnings Audit Log Table (Trigger Target)
CREATE TABLE IF NOT EXISTS `porter_earnings_log` (
    `log_id` INT AUTO_INCREMENT PRIMARY KEY,
    `porter_id` INT NOT NULL,
    `booking_id` INT NOT NULL,
    `fare_amount` DECIMAL(8,2) NOT NULL,
    `commission_fee` DECIMAL(8,2) NOT NULL,
    `net_porter_earning` DECIMAL(8,2) NOT NULL,
    `logged_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_log_porter` FOREIGN KEY (`porter_id`) 
        REFERENCES `porters`(`porter_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_log_booking` FOREIGN KEY (`booking_id`) 
        REFERENCES `bookings`(`booking_id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Indexing for high-performance joins and queries (Rapido/Uber grade)
CREATE INDEX `idx_porter_station_status` ON `porters` (`station_id`, `availability_status`);
CREATE INDEX `idx_booking_status` ON `bookings` (`service_status`);
CREATE INDEX `idx_booking_passenger` ON `bookings` (`passenger_id`);
CREATE INDEX `idx_booking_porter` ON `bookings` (`porter_id`);
