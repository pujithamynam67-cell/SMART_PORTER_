-- =====================================================================
-- MASTER INITIALIZATION SCRIPT FOR SMART PORTER
-- File: init_database.sql
-- Run this script in MySQL Workbench or via CLI:
-- mysql -u root -psystem < sql/init_database.sql
-- =====================================================================

DROP DATABASE IF EXISTS `smart_porter`;
CREATE DATABASE `smart_porter` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `smart_porter`;

-- 1. Tables
SOURCE sql/01_schema.sql;

-- 2. Views
SOURCE sql/02_views.sql;

-- 3. Triggers
SOURCE sql/03_triggers.sql;

-- 4. Stored Procedures
SOURCE sql/04_stored_procedures.sql;

-- 5. Seed Data
SOURCE sql/05_seed_data.sql;

-- Verify setup
SELECT 'SMART PORTER DATABASE INITIALIZATION COMPLETED SUCCESSFULLY' AS status;
SHOW TABLES;
