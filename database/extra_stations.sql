-- =====================================================================
-- SMART PORTER: EXTRA STATIONS, LOCATIONS & EXPANDED PRICING
-- File: database/extra_stations.sql
-- Run AFTER sample_data.sql to expand the station network to 25 stations
-- =====================================================================

USE `smart_porter`;

-- ----------------------------------------------------------------
-- 1. Additional Railway Stations (IDs 9-25)
-- ----------------------------------------------------------------
INSERT INTO `stations` (`station_id`, `station_code`, `station_name`, `city`, `state`, `total_platforms`, `is_active`) VALUES
(9,  'AGC',  'Agra Cantt Railway Station',         'Agra',           'Uttar Pradesh',  6,  TRUE),
(10, 'BSB',  'Varanasi Junction',                  'Varanasi',       'Uttar Pradesh',  9,  TRUE),
(11, 'LKO',  'Lucknow Charbagh Junction',           'Lucknow',        'Uttar Pradesh',  12, TRUE),
(12, 'JP',   'Jaipur Junction',                    'Jaipur',         'Rajasthan',      8,  TRUE),
(13, 'BPL',  'Bhopal Junction',                    'Bhopal',         'Madhya Pradesh', 8,  TRUE),
(14, 'NGP',  'Nagpur Junction',                    'Nagpur',         'Maharashtra',    8,  TRUE),
(15, 'SC',   'Secunderabad Junction',               'Hyderabad',      'Telangana',      10, TRUE),
(16, 'MYS',  'Mysuru Junction',                    'Mysuru',         'Karnataka',      6,  TRUE),
(17, 'CDG',  'Chandigarh Railway Station',          'Chandigarh',     'Chandigarh',    4,  TRUE),
(18, 'BRC',  'Vadodara Junction',                   'Vadodara',       'Gujarat',        7,  TRUE),
(19, 'VSKP', 'Visakhapatnam Railway Station',       'Visakhapatnam',  'Andhra Pradesh', 7,  TRUE),
(20, 'SLM',  'Salem Junction',                     'Salem',          'Tamil Nadu',     5,  TRUE),
(21, 'GWL',  'Gwalior Junction',                   'Gwalior',        'Madhya Pradesh', 6,  TRUE),
(22, 'UDZ',  'Udaipur City Railway Station',        'Udaipur',        'Rajasthan',      3,  TRUE),
(23, 'PUNE', 'Pune Junction',                      'Pune',           'Maharashtra',    6,  TRUE),
(24, 'JAT',  'Jammu Tawi Railway Station',          'Jammu',          'Jammu & Kashmir',4, TRUE),
(25, 'GHY',  'Guwahati Railway Station',            'Guwahati',       'Assam',          5,  TRUE)
ON DUPLICATE KEY UPDATE `station_name` = VALUES(`station_name`);

-- ----------------------------------------------------------------
-- 2. Station Locations for new stations
-- ----------------------------------------------------------------
INSERT IGNORE INTO `station_locations` (`station_id`, `location_name`, `location_type`, `platform_number`) VALUES
-- AGC (9)
(9,  'Main Entry Gate - Agra Cantt',         'GATE',       NULL),
(9,  'Platform 1 - Rajdhani Departure Bay',  'PLATFORM',   1),
(9,  'Platform 4 - Taj Express Bay',          'PLATFORM',   4),
(9,  'FOB Stairs Side-B',                    'OVERBRIDGE', NULL),
-- BSB (10)
(10, 'Kashi Vishwanath Pilgrim Gate',        'GATE',       NULL),
(10, 'Platform 1 - Kashi Express Bay',        'PLATFORM',   1),
(10, 'Platform 5 - Prayagraj Train Area',     'PLATFORM',   5),
(10, 'Cloak Room Counter',                   'WAITING',    NULL),
-- LKO (11)
(11, 'Charbagh Main Concourse Entry',        'GATE',       NULL),
(11, 'Platform 1 - Lucknow Mail Side',       'PLATFORM',   1),
(11, 'Platform 6 - Gomti Express Bay',       'PLATFORM',   6),
(11, 'Prepaid Taxi Stand',                   'PARKING',    NULL),
-- JP (12)
(12, 'Sindhi Camp Gate Entry',               'GATE',       NULL),
(12, 'Platform 1 - Pink City Express Bay',   'PLATFORM',   1),
(12, 'Platform 3 - Ajmer Shatabdi Area',     'PLATFORM',   3),
-- BPL (13)
(13, 'Station Road Main Gate',               'GATE',       NULL),
(13, 'Platform 1 - Shatabdi Departure',      'PLATFORM',   1),
(13, 'Platform 5 - Gondwana Express Bay',    'PLATFORM',   5),
-- NGP (14)
(14, 'Sitabuldi Gate - Main Entry',          'GATE',       NULL),
(14, 'Platform 1 - Vidarbha Express Bay',    'PLATFORM',   1),
(14, 'Platform 4 - Central India Junction',  'PLATFORM',   4),
-- SC (15)
(15, 'Secunderabad North Gate Entry',        'GATE',       NULL),
(15, 'Platform 1 - Golconda Express Bay',    'PLATFORM',   1),
(15, 'Platform 5 - Hyderabad Intercity',     'PLATFORM',   5),
-- MYS (16)
(16, 'Main Mysuru Heritage Gate',            'GATE',       NULL),
(16, 'Platform 1 - Chamundi Express Bay',    'PLATFORM',   1),
-- CDG (17)
(17, 'Chandigarh Station Main Gate',         'GATE',       NULL),
(17, 'Platform 1 - Himalayan Queen Bay',     'PLATFORM',   1),
(17, 'Platform 3 - Shatabdi Express Bay',    'PLATFORM',   3),
-- BRC (18)
(18, 'Vadodara Station Main Entrance',       'GATE',       NULL),
(18, 'Platform 1 - Vadodara Express Bay',    'PLATFORM',   1),
(18, 'Platform 3 - Saurashtra Mail Bay',     'PLATFORM',   3),
-- VSKP (19)
(19, 'Vizag Station Seashore Gate',          'GATE',       NULL),
(19, 'Platform 1 - East Coast Express Bay',  'PLATFORM',   1),
(19, 'Platform 3 - Visakha Express Bay',     'PLATFORM',   3),
-- SLM (20)
(20, 'Salem Junction Main Gate',             'GATE',       NULL),
(20, 'Platform 1 - Intercity Express Bay',   'PLATFORM',   1),
-- GWL (21)
(21, 'Gwalior Station Heritage Gate',        'GATE',       NULL),
(21, 'Platform 1 - Shatabdi/Rajdhani Bay',   'PLATFORM',   1),
(21, 'Platform 4 - MP Sampark Express Bay',  'PLATFORM',   4),
-- UDZ (22)
(22, 'Udaipur City Station Entrance',        'GATE',       NULL),
(22, 'Platform 1 - Mewar Express Bay',       'PLATFORM',   1),
-- PUNE (23)
(23, 'Pune Station Main Concourse Gate',     'GATE',       NULL),
(23, 'Platform 1 - Deccan Queen Bay',        'PLATFORM',   1),
(23, 'Platform 4 - Sahyadri Express Bay',    'PLATFORM',   4),
(23, 'Prepaid Auto / Cab Stand',             'PARKING',    NULL),
-- JAT (24)
(24, 'Jammu Tawi Main Gate',                 'GATE',       NULL),
(24, 'Platform 1 - J&K Express Bay',         'PLATFORM',   1),
(24, 'Platform 2 - Jammu Mail Bay',          'PLATFORM',   2),
-- GHY (25)
(25, 'Guwahati Station River-Side Gate',     'GATE',       NULL),
(25, 'Platform 1 - Brahmaputra Mail Bay',    'PLATFORM',   1),
(25, 'Platform 3 - NE Express Bay',          'PLATFORM',   3);

-- ----------------------------------------------------------------
-- 3. Expanded Pricing Rules (new luggage categories)
-- ----------------------------------------------------------------
INSERT IGNORE INTO `pricing_rules` (`station_id`, `luggage_category`, `base_charge`, `rate_per_kg`, `fragile_surcharge`, `platform_change_fee`, `is_active`) VALUES
(NULL, 'Backpack',           60.00,  2.00, 15.00, 15.00, TRUE),
(NULL, 'Cabin Bag',          80.00,  3.00, 20.00, 15.00, TRUE),
(NULL, 'Bicycle Parts',     250.00,  8.00, 60.00, 50.00, TRUE),
(NULL, 'Electronics Box',   180.00,  7.00, 80.00, 40.00, TRUE),
(NULL, 'Medical Equipment', 200.00,  6.00, 60.00, 40.00, TRUE),
(NULL, 'Group Baggage',     300.00,  5.00, 50.00, 30.00, TRUE),
(NULL, 'Oversized Parcel',  200.00,  7.00, 60.00, 35.00, TRUE);

-- ----------------------------------------------------------------
-- 4. Additional Porters for new stations
-- ----------------------------------------------------------------
INSERT IGNORE INTO `porters` (`badge_number`, `full_name`, `phone`, `email`, `password_hash`, `station_id`, `verification_status`, `rating_avg`, `total_completed_bookings`, `total_earnings`) VALUES
('CK-601', 'Raju Verma',           '9876543260', 'raju@porter.in',    '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 11, 'APPROVED', 4.75, 88,  17600.00),
('CK-602', 'Ajay Tiwari',          '9876543261', 'ajay@porter.in',    '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 11, 'APPROVED', 4.60, 55,  11000.00),
('CK-701', 'Govind Rathore',       '9876543270', 'govind@porter.in',  '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 12, 'APPROVED', 4.85, 110, 22000.00),
('CK-801', 'Ashok Pande',          '9876543280', 'ashok@porter.in',   '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 13, 'APPROVED', 4.70, 70,  14000.00),
('CK-901', 'Narayan Bhattacharya', '9876543290', 'narayan@porter.in', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 23, 'APPROVED', 4.80, 95,  19000.00),
('CK-902', 'Sanjay Mane',          '9876543291', 'sanjay@porter.in',  '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 23, 'APPROVED', 4.65, 60,  12000.00);

-- Porter availability for new porters (set online)
INSERT IGNORE INTO `porter_availability` (`porter_id`, `is_online`, `is_busy`, `current_platform`)
SELECT porter_id, TRUE, FALSE, 1 FROM porters 
WHERE badge_number IN ('CK-601','CK-602','CK-701','CK-801','CK-901','CK-902');

SELECT CONCAT('Stations: ', COUNT(*)) AS summary FROM stations
UNION ALL
SELECT CONCAT('Pricing Rules: ', COUNT(*)) FROM pricing_rules
UNION ALL
SELECT CONCAT('Porters: ', COUNT(*)) FROM porters;
