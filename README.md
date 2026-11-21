# 🧳 Smart Porter – A Smart Luggage Management System at Railway Stations
> **A Rapido / Uber Equivalent for Railway Station Porters (Sahayaks/Coolies)**  
> Built for BTech Computer Science & Engineering (DBMS Mini-Project & Web Application)

---

## 📌 1. Project Overview
Passengers at busy railway stations in India frequently encounter hardships transporting heavy luggage across multiple platforms, pedestrian overbridges, and crowded stairs. This is particularly challenging for senior citizens, patients, and families traveling with infants or bulky parcels.

**Smart Porter** solves this by providing a real-time digital booking platform connecting passengers with licensed railway station porters (Coolies):
- **Dynamic Fare Engine**: Calculates fare automatically based on station base rate, total baggage weight (KG), bag quantity, fragile care surcharge, and senior citizen concessions (15% subsidy).
- **Uber/Rapido Matcher**: Matches the booking to the nearest available high-rated porter at that station.
- **Live Ride Progress**: Real-time status tracker (`REQUESTED` ➔ `ACCEPTED` ➔ `PICKED_UP` ➔ `IN_TRANSIT` ➔ `COMPLETED`).
- **Simulated Payment Gateway**: Supports UPI (QR / PhonePe / GPay), Cash to Porter, and Cards.
- **Porter Duty Console**: Allows porters to toggle availability (`ONLINE` / `OFFLINE`), receive trip alerts, and update job status.
- **Station Master & Admin Dashboard**: Real-time KPI metrics, station baggage load analytics, coolie leaderboard, and an interactive **DBMS Concepts Evaluation Console**.

---

## 🗄️ 2. Core DBMS Concepts Implemented & Demonstrated

| DBMS Concept | Implementation in Smart Porter |
| :--- | :--- |
| **DDL (Data Definition Language)** | Strict relational schemas for `stations`, `passengers`, `porters`, `bookings`, `luggage`, `payments`, `feedback`, `porter_earnings_log`. |
| **DML (Data Manipulation Language)** | Parameterized `INSERT`, `UPDATE`, `DELETE` operations via JDBC `PreparedStatement`. |
| **DQL (Data Query Language)** | Complex multi-table SELECT statements, filtering, and sorting. |
| **Integrity Constraints** | `PRIMARY KEY`, `FOREIGN KEY (ON DELETE CASCADE/RESTRICT)`, `UNIQUE` (phones, badge numbers, emails), `CHECK` (rating 1-5, weight > 0), `NOT NULL`. |
| **Normalization (1NF, 2NF, 3NF)** | **1NF**: Atomic attributes (no repeating groups).<br>**2NF**: Fully functional dependencies on composite/primary keys (luggage separated from bookings).<br>**3NF**: Transitive dependencies eliminated (station tariff separated from bookings). |
| **Multi-Table SQL Joins** | 6-table INNER and LEFT JOINs combining Bookings, Passengers, Porters, Stations, Luggage, and Payments. |
| **Correlated Subqueries** | Querying porters whose completed trips exceed the average trip volume at their station. |
| **Database Views** | `vw_active_bookings`, `vw_porter_leaderboard`, `vw_station_analytics`, `vw_passenger_ride_history`. |
| **Triggers** | `trg_validate_luggage_weight` (blocks invalid baggage weight).<br>`trg_after_booking_accepted` (auto marks porter 'BUSY').<br>`trg_after_booking_completed` (frees porter, calculates 90% net coolie share, logs audit).<br>`trg_update_porter_rating` (recalculates porter average rating on review). |
| **Stored Procedures** | `sp_create_smart_booking` (Atomic booking + luggage entry).<br>`sp_assign_nearest_porter` (Greedy porter assignment).<br>`sp_complete_booking_and_pay` (Completes trip and records transaction). |
| **TCL (ACID Transactions)** | Explicit `START TRANSACTION`, `COMMIT`, `ROLLBACK` in stored procedures to prevent inconsistent booking states. |

---

## ⚙️ 3. Environment Configuration (`.env`)

Database credentials are **never hardcoded** in Java source code. Configuration is dynamically loaded at startup using `DotenvLoader` from the `.env` file.

### `.env` File Structure:
```env
# APPLICATION DETAILS
APP_NAME=SmartPorter
APP_ENV=development
APP_PORT=8080
APP_BASE_URL=http://localhost:8080/SmartPorter

# MYSQL DATABASE DETAILS
DB_HOST=localhost
DB_PORT=3306
DB_NAME=smart_porter
DB_USERNAME=root
DB_PASSWORD=system

# JDBC CONFIGURATION
DB_DRIVER=com.mysql.cj.jdbc.Driver
DB_URL=jdbc:mysql://localhost:3306/smart_porter?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata

# SECURITY
BCRYPT_ROUNDS=12
SESSION_TIMEOUT=30

# PAYMENT
PAYMENT_MODE=DEMO

# LOGGING
LOG_LEVEL=INFO
```

> **Security Note**: `.env` is ignored by Git in `.gitignore`. A template file `.env.example` is provided for distribution.

---

## 🚀 4. How to Run the Project

### Option A: Standalone Runner (No external Tomcat installation required!)
1. Open PowerShell or Command Prompt in the project folder `c:\Users\pujitha\Desktop\smart_porter`.
2. Double-click or execute:
   ```cmd
   run.bat
   ```
   Or on PowerShell:
   ```powershell
   .\run.ps1
   ```
3. Open your browser and navigate to:
   - **`http://localhost:8080/SmartPorter/`** (or `http://localhost:8080/`)

---

### Option B: Apache Tomcat Web Deployment
1. Verify database credentials in `.env`.
2. Build the WAR package using Maven:
   ```cmd
   mvn clean package
   ```
3. Copy `target/SmartPorter.war` to your Apache Tomcat `webapps/` folder:
   ```cmd
   copy target\SmartPorter.war "C:\Program Files\Apache Software Foundation\Tomcat 10.0\webapps\"
   ```
4. Restart Apache Tomcat:
   ```cmd
   "C:\Program Files\Apache Software Foundation\Tomcat 10.0\bin\shutdown.bat"
   "C:\Program Files\Apache Software Foundation\Tomcat 10.0\bin\startup.bat"
   ```
5. Access at `http://localhost:8080/SmartPorter/`.

---

## 🧪 5. Automated Verification Suite
To run the automated end-to-end integration test suite verifying all 31 critical checkpoints:
```cmd
java -cp "bin;lib/*" test.TestSuite
```
Output:
```
==================================================================
  TEST SUMMARY: 31 / 31 PASSED (100%)
==================================================================
```

---

## 🔑 6. Default Demo Credentials

| Role | Username / ID | Password | Notes |
| :--- | :--- | :--- | :--- |
| **Passenger** | `9811002233` or `rajesh@example.com` | `password123` | Pre-registered passenger |
| **Porter (Coolie)** | Badge: `CK-101` or Phone: `9876543210` | `password123` | Ramesh Kumar (New Delhi Station) |
| **Porter (Coolie)** | Badge: `CK-201` or Phone: `9876543220` | `password123` | Vinod Patil (CSMT Mumbai) |
| **Admin** | `admin` | `password123` | Duty Manager Console |
