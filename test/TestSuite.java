package test;

import com.smartporter.config.AppConfig;
import com.smartporter.dao.*;
import com.smartporter.database.DBConnection;
import com.smartporter.model.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * End-to-End Automated Test Suite for Smart Porter.
 * Tests:
 * 1. Dynamic .env Configuration (Never hardcoded credentials)
 * 2. Database Connectivity
 * 3. Passenger Registration & Authentication
 * 4. Porter Registration & Duty Toggle
 * 5. Dynamic Booking Creation (sp_create_smart_booking)
 * 6. Porter Match & Assignment (sp_assign_nearest_porter)
 * 7. Real-Time Status Progression & Triggers (trg_after_booking_accepted)
 * 8. Trip Completion & Payment (sp_complete_booking_and_pay)
 * 9. Feedback & Trigger Rating Recalculation (trg_update_porter_rating)
 * 10. Admin Metrics & Views (vw_station_analytics & vw_porter_leaderboard)
 * 11. DBMS Luggage Weight Validation Trigger (trg_validate_luggage_weight)
 */
public class TestSuite {

    private static int passedTests = 0;
    private static int totalTests = 0;

    private static void assertTest(String testName, boolean condition) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } else {
            System.err.println("  [FAIL] " + testName);
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  SMART PORTER - END-TO-END AUTOMATED VERIFICATION SUITE");
        System.out.println("==================================================================");

        // Test 1: Configuration & .env loading
        System.out.println("\n--- 1. Testing Dynamic Configuration & .env ---");
        assertTest("DB_HOST dynamically loaded", "localhost".equals(AppConfig.DB_HOST));
        assertTest("DB_NAME dynamically loaded", "smart_porter".equals(AppConfig.DB_NAME));
        assertTest("DB_USERNAME dynamically loaded", "root".equals(AppConfig.DB_USERNAME));
        assertTest("DB_PASSWORD dynamically loaded", AppConfig.DB_PASSWORD != null && !AppConfig.DB_PASSWORD.isEmpty());
        assertTest("JDBC Driver loaded", AppConfig.DB_DRIVER != null);

        // Test 2: Database Connection
        System.out.println("\n--- 2. Testing Database Connectivity ---");
        boolean dbConnected = DBConnection.testConnection();
        assertTest("MySQL Connection Established", dbConnected);
        if (!dbConnected) {
            System.err.println("Database is not accessible. Aborting remaining tests.");
            return;
        }

        PassengerDAO passengerDAO = new PassengerDAO();
        PorterDAO porterDAO = new PorterDAO();
        StationDAO stationDAO = new StationDAO();
        BookingDAO bookingDAO = new BookingDAO();
        FeedbackDAO feedbackDAO = new FeedbackDAO();
        AdminDAO adminDAO = new AdminDAO();

        try {
            // Test 3: Stations Lookup
            System.out.println("\n--- 3. Testing Stations Catalog ---");
            List<Station> stations = stationDAO.getAll();
            assertTest("Station list not empty (at least 6 stations)", stations != null && stations.size() >= 6);
            Station ndls = stationDAO.getById(1);
            assertTest("NDLS station retrieved", ndls != null && "NDLS".equals(ndls.getStationCode()));

            // Test 4: Passenger Registration & Auth
            System.out.println("\n--- 4. Testing Passenger Registration & Login ---");
            String testPhone = "99" + (int)(Math.random() * 90000000 + 10000000);
            String testEmail = "testuser" + System.currentTimeMillis() + "@railtest.in";

            Passenger newPass = new Passenger(0, "Test Passenger Auto", testPhone, testEmail, "", "Male", false);
            Passenger registered = passengerDAO.register(newPass, "secretPassword456");
            assertTest("Passenger registered with auto-generated ID", registered.getPassengerId() > 0);

            Passenger authenticated = passengerDAO.authenticate(testEmail, "secretPassword456");
            assertTest("Passenger authenticated successfully", authenticated != null && authenticated.getPassengerId() == registered.getPassengerId());

            Passenger failedAuth = passengerDAO.authenticate(testEmail, "wrongPassword");
            assertTest("Invalid password rejected", failedAuth == null);

            // Test 5: Porter Authentication & Duty Status Toggle
            System.out.println("\n--- 5. Testing Porter Duty & Status ---");
            Porter porter = porterDAO.authenticate("CK-101", "password123");
            assertTest("Porter CK-101 authenticated", porter != null && "CK-101".equals(porter.getBadgeNumber()));

            boolean toggledOffline = porterDAO.updateStatus(porter.getPorterId(), "OFFLINE");
            assertTest("Porter switched to OFFLINE", toggledOffline);

            boolean toggledAvailable = porterDAO.updateStatus(porter.getPorterId(), "AVAILABLE");
            assertTest("Porter switched back to AVAILABLE", toggledAvailable);

            // Test 6: Create Booking via Stored Procedure
            System.out.println("\n--- 6. Testing Luggage Booking Creation (sp_create_smart_booking) ---");
            Map<String, Object> bookingRes = bookingDAO.createBooking(
                    registered.getPassengerId(),
                    1, // NDLS
                    "12952",
                    "Mumbai Rajdhani",
                    "B3 - Seat 41",
                    "Main Entrance Gate 1",
                    "Platform 1 - Coach B3",
                    "Heavy Suitcase",
                    2,
                    new BigDecimal("35.00"),
                    false,
                    "Automated integration test booking"
            );
            assertTest("Booking created with code", bookingRes.containsKey("bookingCode") && bookingRes.get("bookingCode").toString().startsWith("SP-"));
            int testBookingId = (Integer) bookingRes.get("bookingId");
            BigDecimal totalFare = (BigDecimal) bookingRes.get("totalFare");
            assertTest("Total fare dynamically computed (> 200)", totalFare != null && totalFare.compareTo(new BigDecimal("200")) > 0);

            // Test 7: Match & Assign Porter (sp_assign_nearest_porter)
            System.out.println("\n--- 7. Testing Smart Porter Assignment (sp_assign_nearest_porter) ---");
            Map<String, Object> matchRes = bookingDAO.assignPorter(testBookingId);
            assertTest("Porter assigned to booking", Boolean.TRUE.equals(matchRes.get("assigned")));
            int assignedPorterId = (Integer) matchRes.get("assignedPorterId");
            assertTest("Assigned Porter ID valid", assignedPorterId > 0);

            // Test 8: Verify Trigger trg_after_booking_accepted
            System.out.println("\n--- 8. Testing Trigger: trg_after_booking_accepted ---");
            Porter assignedPorter = porterDAO.getById(assignedPorterId);
            assertTest("Porter status automatically set to BUSY by Trigger", "BUSY".equals(assignedPorter.getAvailabilityStatus()));

            // Test 9: Real-time Status Progression
            System.out.println("\n--- 9. Testing Status Progression (PICKED_UP -> IN_TRANSIT) ---");
            boolean statusPicked = bookingDAO.updateStatus(testBookingId, "PICKED_UP");
            assertTest("Status updated to PICKED_UP", statusPicked);

            boolean statusTransit = bookingDAO.updateStatus(testBookingId, "IN_TRANSIT");
            assertTest("Status updated to IN_TRANSIT", statusTransit);

            // Test 10: Complete Trip & Pay (sp_complete_booking_and_pay & Trigger trg_after_booking_completed)
            System.out.println("\n--- 10. Testing Trip Completion, Payment & Completion Trigger ---");
            Map<String, Object> payRes = bookingDAO.completeBookingAndPay(testBookingId, "UPI");
            assertTest("Trip completed and payment recorded", "SUCCESS".equals(payRes.get("resultCode")));

            Porter freedPorter = porterDAO.getById(assignedPorterId);
            assertTest("Porter status automatically returned to AVAILABLE by Trigger", "AVAILABLE".equals(freedPorter.getAvailabilityStatus()));
            assertTest("Porter completed trips incremented by Trigger", freedPorter.getTotalRides() > 0);

            // Test 11: Feedback Submission & Trigger trg_update_porter_rating
            System.out.println("\n--- 11. Testing Rating & Trigger: trg_update_porter_rating ---");
            Feedback feedback = feedbackDAO.submitFeedback(testBookingId, registered.getPassengerId(), assignedPorterId, 5, "Outstanding coolie service!");
            assertTest("Feedback recorded", feedback != null);

            Porter ratedPorter = porterDAO.getById(assignedPorterId);
            assertTest("Porter average rating recalculated by Trigger", ratedPorter.getRatingAvg() != null && ratedPorter.getRatingAvg().compareTo(BigDecimal.ZERO) > 0);

            // Test 12: Admin Analytics & Database Views
            System.out.println("\n--- 12. Testing Admin Analytics & Views ---");
            Map<String, Object> metrics = adminDAO.getSystemMetrics();
            assertTest("Total bookings metric positive", (Integer) metrics.get("totalBookings") > 0);
            assertTest("Total revenue metric positive", ((BigDecimal) metrics.get("totalRevenue")).compareTo(BigDecimal.ZERO) > 0);

            List<Map<String, Object>> stationAnalytics = adminDAO.getStationAnalytics();
            assertTest("Station analytics view (vw_station_analytics) populated", !stationAnalytics.isEmpty());

            List<Map<String, Object>> leaderboard = porterDAO.getLeaderboard();
            assertTest("Porter leaderboard view (vw_porter_leaderboard) populated", !leaderboard.isEmpty());

            // Test 13: DBMS Luggage Weight Validation Trigger (trg_validate_luggage_weight)
            System.out.println("\n--- 13. Testing DBMS Integrity Trigger: trg_validate_luggage_weight ---");
            boolean triggerBlockedIllegalWeight = false;
            try {
                bookingDAO.createBooking(
                        registered.getPassengerId(),
                        1,
                        "12952",
                        "Test Train",
                        "B1",
                        "Gate 1",
                        "Platform 1",
                        "Box/Carton",
                        1,
                        new BigDecimal("-10.00"), // ILLEGAL negative weight
                        false,
                        "Illegal weight test"
                );
            } catch (SQLException ex) {
                if (ex.getMessage().contains("Validation Error") || ex.getSQLState().equals("45000")) {
                    triggerBlockedIllegalWeight = true;
                }
            }
            assertTest("Trigger trg_validate_luggage_weight rejected negative luggage weight", triggerBlockedIllegalWeight);

        } catch (Exception e) {
            e.printStackTrace();
            assertTest("Unexpected Exception in test suite: " + e.getMessage(), false);
        }

        System.out.println("\n==================================================================");
        System.out.println("  TEST SUMMARY: " + passedTests + " / " + totalTests + " PASSED (" + (passedTests * 100 / totalTests) + "%)");
        System.out.println("==================================================================");
    }
}
