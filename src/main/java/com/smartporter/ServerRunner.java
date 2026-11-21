package com.smartporter;

import com.smartporter.config.AppConfig;
import com.smartporter.dao.*;
import com.smartporter.database.DBConnection;
import com.smartporter.model.*;
import com.smartporter.service.FareCalculator;
import com.smartporter.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.*;

public class ServerRunner {

    private static final PassengerDAO passengerDAO = new PassengerDAO();
    private static final PorterDAO porterDAO = new PorterDAO();
    private static final StationDAO stationDAO = new StationDAO();
    private static final BookingDAO bookingDAO = new BookingDAO();
    private static final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private static final AdminDAO adminDAO = new AdminDAO();

    public static void main(String[] args) throws IOException {
        AppConfig.printConfigSummary();

        // Test Database Connectivity
        boolean dbOk = DBConnection.testConnection();
        if (!dbOk) {
            System.err.println("------------------------------------------------------------------");
            System.err.println("WARNING: Database connection failed!");
            System.err.println("Please ensure MySQL is running on " + AppConfig.DB_HOST + ":" + AppConfig.DB_PORT);
            System.err.println("And verify credentials in your .env file.");
            System.err.println("Run 'mysql -u root -psystem < sql/init_database.sql' to initialize.");
            System.err.println("------------------------------------------------------------------");
        } else {
            System.out.println("[SmartPorter] Database connection verified successfully!");
        }

        int port = AppConfig.APP_PORT;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Handler for /SmartPorter/ and root /
        ApiAndStaticDispatcher dispatcher = new ApiAndStaticDispatcher();
        server.createContext("/SmartPorter", dispatcher);
        server.createContext("/", dispatcher);

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();

        System.out.println("\n>>> Smart Porter Server is LIVE!");
        System.out.println(">>> Access Passenger/Porter/Admin UI at: http://localhost:" + port + "/SmartPorter/");
        System.out.println(">>> Alternative root access at:        http://localhost:" + port + "/");
        System.out.println("------------------------------------------------------------------");
    }

    static class ApiAndStaticDispatcher implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String uri = exchange.getRequestURI().toString();

            // Strip context path
            String path = uri;
            if (path.startsWith("/SmartPorter")) {
                path = path.substring("/SmartPorter".length());
            }
            int queryIndex = path.indexOf('?');
            String query = (queryIndex >= 0) ? path.substring(queryIndex + 1) : "";
            if (queryIndex >= 0) {
                path = path.substring(0, queryIndex);
            }

            // CORS headers
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                if (path.startsWith("/api/")) {
                    handleApi(exchange, method, path, query);
                } else {
                    serveStatic(exchange, path);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, 500, Map.of("success", false, "error", e.getMessage()));
            }
        }

        private void handleApi(HttpExchange exchange, String method, String path, String query) throws Exception {
            Map<String, String> queryParams = parseQueryParams(query);

            if (path.equals("/api/auth/register-passenger") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                Passenger p = new Passenger();
                p.setFullName((String) body.get("fullName"));
                p.setPhone((String) body.get("phone"));
                p.setEmail((String) body.get("email"));
                p.setGender((String) body.getOrDefault("gender", "Male"));
                p.setSeniorCitizen(Boolean.TRUE.equals(body.get("isSeniorCitizen")));
                String password = (String) body.get("password");

                Passenger created = passengerDAO.register(p, password);
                sendJsonResponse(exchange, 200, Map.of("success", true, "passenger", created));
                return;
            }

            if (path.equals("/api/auth/login-passenger") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                String emailOrPhone = (String) body.get("emailOrPhone");
                String password = (String) body.get("password");

                Passenger p = passengerDAO.authenticate(emailOrPhone, password);
                if (p != null) {
                    sendJsonResponse(exchange, 200, Map.of("success", true, "userType", "PASSENGER", "passenger", p));
                } else {
                    sendJsonResponse(exchange, 401, Map.of("success", false, "message", "Invalid passenger phone/email or password."));
                }
                return;
            }

            if (path.equals("/api/auth/register-porter") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                Porter p = new Porter();
                p.setBadgeNumber((String) body.get("badgeNumber"));
                p.setFullName((String) body.get("fullName"));
                p.setPhone((String) body.get("phone"));
                p.setStationId(((Number) body.get("stationId")).intValue());
                p.setCurrentPlatform(((Number) body.getOrDefault("currentPlatform", 1)).intValue());
                String password = (String) body.get("password");

                Porter created = porterDAO.register(p, password);
                sendJsonResponse(exchange, 200, Map.of("success", true, "porter", created));
                return;
            }

            if (path.equals("/api/auth/login-porter") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                String phoneOrBadge = (String) body.get("phoneOrBadge");
                String password = (String) body.get("password");

                Porter p = porterDAO.authenticate(phoneOrBadge, password);
                if (p != null) {
                    sendJsonResponse(exchange, 200, Map.of("success", true, "userType", "PORTER", "porter", p));
                } else {
                    sendJsonResponse(exchange, 401, Map.of("success", false, "message", "Invalid porter badge/phone or password."));
                }
                return;
            }

            if (path.equals("/api/auth/login-admin") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                String username = (String) body.get("username");
                String password = (String) body.get("password");

                boolean ok = adminDAO.authenticate(username, password);
                if (ok) {
                    sendJsonResponse(exchange, 200, Map.of("success", true, "userType", "ADMIN", "username", username));
                } else {
                    sendJsonResponse(exchange, 401, Map.of("success", false, "message", "Invalid admin credentials."));
                }
                return;
            }

            if (path.equals("/api/stations") && "GET".equalsIgnoreCase(method)) {
                List<Station> stations = stationDAO.getAll();
                sendJsonResponse(exchange, 200, Map.of("success", true, "stations", stations));
                return;
            }

            if (path.equals("/api/porters") && "GET".equalsIgnoreCase(method)) {
                int stationId = queryParams.containsKey("stationId") ? Integer.parseInt(queryParams.get("stationId")) : 1;
                boolean availableOnly = Boolean.parseBoolean(queryParams.getOrDefault("availableOnly", "false"));
                List<Porter> porters = porterDAO.getByStation(stationId, availableOnly);
                sendJsonResponse(exchange, 200, Map.of("success", true, "porters", porters));
                return;
            }

            if (path.equals("/api/porters/status") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                int porterId = ((Number) body.get("porterId")).intValue();
                String status = (String) body.get("status");
                boolean ok = porterDAO.updateStatus(porterId, status);
                sendJsonResponse(exchange, 200, Map.of("success", ok, "status", status));
                return;
            }

            if (path.equals("/api/bookings/estimate-fare") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                int stationId = ((Number) body.get("stationId")).intValue();
                double weightKg = ((Number) body.get("weightKg")).doubleValue();
                int quantity = ((Number) body.getOrDefault("quantity", 1)).intValue();
                boolean isFragile = Boolean.TRUE.equals(body.get("isFragile"));
                boolean isSenior = Boolean.TRUE.equals(body.get("isSeniorCitizen"));

                Station st = stationDAO.getById(stationId);
                if (st != null) {
                    Map<String, Object> estimate = FareCalculator.estimateFare(st, weightKg, quantity, isFragile, isSenior);
                    sendJsonResponse(exchange, 200, Map.of("success", true, "estimate", estimate));
                } else {
                    sendJsonResponse(exchange, 404, Map.of("success", false, "message", "Station not found"));
                }
                return;
            }

            if (path.equals("/api/bookings/create") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                int passengerId = ((Number) body.get("passengerId")).intValue();
                int stationId = ((Number) body.get("stationId")).intValue();
                String trainNumber = (String) body.get("trainNumber");
                String trainName = (String) body.get("trainName");
                String coachNumber = (String) body.get("coachNumber");
                String pickupPoint = (String) body.get("pickupPoint");
                String dropPoint = (String) body.get("dropPoint");
                String luggageType = (String) body.get("luggageType");
                int quantity = ((Number) body.getOrDefault("quantity", 1)).intValue();
                BigDecimal weight = new BigDecimal(body.get("weightKg").toString());
                boolean isFragile = Boolean.TRUE.equals(body.get("isFragile"));
                String notes = (String) body.get("notes");

                // Call stored procedure
                Map<String, Object> result = bookingDAO.createBooking(
                        passengerId, stationId, trainNumber, trainName, coachNumber,
                        pickupPoint, dropPoint, luggageType, quantity, weight, isFragile, notes
                );
                sendJsonResponse(exchange, 200, Map.of("success", true, "booking", result));
                return;
            }

            if (path.equals("/api/bookings/assign-porter") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                int bookingId = ((Number) body.get("bookingId")).intValue();
                Map<String, Object> matchResult = bookingDAO.assignPorter(bookingId);
                sendJsonResponse(exchange, 200, Map.of("success", true, "match", matchResult));
                return;
            }

            if (path.equals("/api/bookings/update-status") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                int bookingId = ((Number) body.get("bookingId")).intValue();
                String status = (String) body.get("status");
                boolean ok = bookingDAO.updateStatus(bookingId, status);
                sendJsonResponse(exchange, 200, Map.of("success", ok, "status", status));
                return;
            }

            if (path.equals("/api/bookings/active") && "GET".equalsIgnoreCase(method)) {
                List<Map<String, Object>> active = bookingDAO.getActiveBookings();
                sendJsonResponse(exchange, 200, Map.of("success", true, "activeBookings", active));
                return;
            }

            if (path.equals("/api/bookings/passenger") && "GET".equalsIgnoreCase(method)) {
                int pid = Integer.parseInt(queryParams.get("passengerId"));
                List<Map<String, Object>> history = bookingDAO.getBookingsByPassenger(pid);
                sendJsonResponse(exchange, 200, Map.of("success", true, "history", history));
                return;
            }

            if (path.equals("/api/bookings/porter") && "GET".equalsIgnoreCase(method)) {
                int porterId = Integer.parseInt(queryParams.get("porterId"));
                List<Map<String, Object>> jobs = bookingDAO.getBookingsByPorter(porterId);
                sendJsonResponse(exchange, 200, Map.of("success", true, "jobs", jobs));
                return;
            }

            if (path.equals("/api/bookings/detail") && "GET".equalsIgnoreCase(method)) {
                int bookingId = Integer.parseInt(queryParams.get("bookingId"));
                Map<String, Object> detail = bookingDAO.getBookingById(bookingId);
                if (detail != null) {
                    sendJsonResponse(exchange, 200, Map.of("success", true, "booking", detail));
                } else {
                    sendJsonResponse(exchange, 404, Map.of("success", false, "message", "Booking not found"));
                }
                return;
            }

            if (path.equals("/api/bookings/complete-pay") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                int bookingId = ((Number) body.get("bookingId")).intValue();
                String paymentMethod = (String) body.getOrDefault("paymentMethod", "UPI");
                Map<String, Object> payResult = bookingDAO.completeBookingAndPay(bookingId, paymentMethod);
                sendJsonResponse(exchange, 200, Map.of("success", true, "payment", payResult));
                return;
            }

            if (path.equals("/api/feedback/submit") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                int bookingId = ((Number) body.get("bookingId")).intValue();
                int passengerId = ((Number) body.get("passengerId")).intValue();
                int porterId = ((Number) body.get("porterId")).intValue();
                int rating = ((Number) body.get("rating")).intValue();
                String comments = (String) body.get("comments");

                Feedback f = feedbackDAO.submitFeedback(bookingId, passengerId, porterId, rating, comments);
                sendJsonResponse(exchange, 200, Map.of("success", true, "feedback", f));
                return;
            }

            if (path.equals("/api/admin/metrics") && "GET".equalsIgnoreCase(method)) {
                Map<String, Object> metrics = adminDAO.getSystemMetrics();
                sendJsonResponse(exchange, 200, Map.of("success", true, "metrics", metrics));
                return;
            }

            if (path.equals("/api/admin/analytics") && "GET".equalsIgnoreCase(method)) {
                List<Map<String, Object>> analytics = adminDAO.getStationAnalytics();
                sendJsonResponse(exchange, 200, Map.of("success", true, "stationAnalytics", analytics));
                return;
            }

            if (path.equals("/api/admin/leaderboard") && "GET".equalsIgnoreCase(method)) {
                List<Map<String, Object>> leaderboard = porterDAO.getLeaderboard();
                sendJsonResponse(exchange, 200, Map.of("success", true, "leaderboard", leaderboard));
                return;
            }

            if (path.equals("/api/admin/dbms-demo") && "GET".equalsIgnoreCase(method)) {
                String concept = queryParams.getOrDefault("concept", "JOINS");
                Map<String, Object> demoData = adminDAO.runDbmsConceptDemo(concept);
                sendJsonResponse(exchange, 200, Map.of("success", true, "demo", demoData));
                return;
            }

            sendJsonResponse(exchange, 404, Map.of("success", false, "message", "Endpoint not found: " + path));
        }

        private void serveStatic(HttpExchange exchange, String path) throws IOException {
            if (path == null || path.isEmpty() || path.equals("/")) {
                path = "/index.html";
            }

            // Look in src/main/webapp first
            File file = new File("src/main/webapp" + path);
            if (!file.exists() || file.isDirectory()) {
                file = new File("src/main/webapp/index.html");
            }

            if (!file.exists()) {
                String notFound = "<h1>404 Not Found - Smart Porter Webapp</h1>";
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes(StandardCharsets.UTF_8));
                }
                return;
            }

            String mimeType = "text/html";
            if (path.endsWith(".css")) mimeType = "text/css";
            else if (path.endsWith(".js")) mimeType = "application/javascript";
            else if (path.endsWith(".json")) mimeType = "application/json";
            else if (path.endsWith(".png")) mimeType = "image/png";
            else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) mimeType = "image/jpeg";
            else if (path.endsWith(".svg")) mimeType = "image/svg+xml";

            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", mimeType + "; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private Map<String, Object> readJsonBody(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int read;
            while ((read = is.read(buf)) != -1) {
                baos.write(buf, 0, read);
            }
            String json = baos.toString(StandardCharsets.UTF_8);
            return JsonUtil.parseJsonObject(json);
        }

        private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
            String json = JsonUtil.toJson(data);
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private Map<String, String> parseQueryParams(String query) {
            Map<String, String> params = new HashMap<>();
            if (query == null || query.isEmpty()) return params;
            String[] pairs = query.split("&");
            for (String p : pairs) {
                int idx = p.indexOf('=');
                if (idx > 0) {
                    try {
                        String key = URLDecoder.decode(p.substring(0, idx), StandardCharsets.UTF_8);
                        String val = URLDecoder.decode(p.substring(idx + 1), StandardCharsets.UTF_8);
                        params.put(key, val);
                    } catch (Exception ignored) {}
                }
            }
            return params;
        }
    }
}
