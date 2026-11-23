package com.smartporter;

import com.smartporter.config.AppConfig;
import com.smartporter.dao.*;
import com.smartporter.model.*;
import com.smartporter.service.*;
import com.smartporter.util.DBConnection;
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
import java.util.*;

public class ServerRunner {

    private static final AuthService authService = new AuthService();
    private static final StationDAO stationDAO = new StationDAO();
    private static final BookingService bookingService = new BookingService();
    private static final PorterService porterService = new PorterService();
    private static final FareService fareService = new FareService();
    private static final AdminService adminService = new AdminService();

    public static void main(String[] args) throws IOException {
        System.out.println("==================================================");
        System.out.println("  " + AppConfig.APP_NAME + " - Full-Stack Application Server");
        System.out.println("==================================================");
        System.out.println(" Environment   : " + AppConfig.APP_ENV);
        System.out.println(" Server Port   : " + AppConfig.APP_PORT);
        System.out.println(" Base URL      : " + AppConfig.APP_BASE_URL);
        System.out.println(" DB Database   : " + AppConfig.DB_NAME);
        System.out.println(" DB Username   : " + AppConfig.DB_USERNAME);
        System.out.println("==================================================");

        boolean dbOk = DBConnection.testConnection();
        if (dbOk) {
            System.out.println("[ServerRunner] Database connection to MySQL verified!");
        } else {
            System.err.println("[ServerRunner] WARNING: Could not connect to MySQL. Verify .env credentials.");
        }

        int port = AppConfig.APP_PORT;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        ApiAndStaticDispatcher dispatcher = new ApiAndStaticDispatcher();
        server.createContext("/SmartPorter", dispatcher);
        server.createContext("/", dispatcher);

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();

        System.out.println("\n>>> Smart Porter Website is LIVE!");
        System.out.println(">>> Open: http://localhost:" + port + "/SmartPorter/");
        System.out.println(">>> Root: http://localhost:" + port + "/");
        System.out.println("--------------------------------------------------");
    }

    static class ApiAndStaticDispatcher implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String uri = exchange.getRequestURI().toString();

            String path = uri;
            if (path.startsWith("/SmartPorter")) {
                path = path.substring("/SmartPorter".length());
            }
            int queryIndex = path.indexOf('?');
            String query = (queryIndex >= 0) ? path.substring(queryIndex + 1) : "";
            if (queryIndex >= 0) {
                path = path.substring(0, queryIndex);
            }

            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
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
                sendJsonResponse(exchange, 500, Map.of("success", false, "message", e.getMessage()));
            }
        }

        private void handleApi(HttpExchange exchange, String method, String path, String query) throws Exception {
            Map<String, String> queryParams = parseQueryParams(query);

            // AUTH
            if (path.equals("/api/auth/register") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                Passenger p = new Passenger();
                p.setFullName((String) body.get("fullName"));
                p.setPhone((String) body.get("phone"));
                p.setEmail((String) body.get("email"));
                p.setGender((String) body.getOrDefault("gender", "Male"));
                p.setSeniorCitizen(Boolean.TRUE.equals(body.get("isSeniorCitizen")));
                Passenger created = authService.registerPassenger(p, (String) body.get("password"));
                sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Registration successful", "data", created));
                return;
            }

            if (path.equals("/api/auth/login") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                Passenger p = authService.loginPassenger((String) body.get("emailOrPhone"), (String) body.get("password"));
                if (p != null) {
                    sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Login successful", "data", p, "userType", "PASSENGER"));
                } else {
                    sendJsonResponse(exchange, 401, Map.of("success", false, "message", "Invalid passenger credentials"));
                }
                return;
            }

            if (path.equals("/api/auth/porter-register") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                Porter p = new Porter();
                p.setBadgeNumber((String) body.get("badgeNumber"));
                p.setFullName((String) body.get("fullName"));
                p.setPhone((String) body.get("phone"));
                p.setEmail((String) body.get("email"));
                p.setStationId(((Number) body.get("stationId")).intValue());
                Porter created = authService.registerPorter(p, (String) body.get("password"));
                sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Registration submitted for admin review", "data", created));
                return;
            }

            if (path.equals("/api/auth/porter-login") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                Porter p = authService.loginPorter((String) body.get("phoneOrBadge"), (String) body.get("password"));
                if (p != null) {
                    sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Login successful", "data", p, "userType", "PORTER"));
                } else {
                    sendJsonResponse(exchange, 401, Map.of("success", false, "message", "Invalid porter badge/phone or password"));
                }
                return;
            }

            if (path.equals("/api/auth/admin-login") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                Admin a = authService.loginAdmin((String) body.get("username"), (String) body.get("password"));
                if (a != null) {
                    sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Admin login successful", "data", a, "userType", "ADMIN"));
                } else {
                    sendJsonResponse(exchange, 401, Map.of("success", false, "message", "Invalid admin credentials"));
                }
                return;
            }

            if (path.equals("/api/auth/logout") && "POST".equalsIgnoreCase(method)) {
                sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Logged out"));
                return;
            }

            // STATIONS
            if (path.equals("/api/stations") && "GET".equalsIgnoreCase(method)) {
                List<Station> stations = stationDAO.getAll();
                sendJsonResponse(exchange, 200, Map.of("success", true, "data", stations));
                return;
            }

            if (path.startsWith("/api/stations/") && path.endsWith("/locations") && "GET".equalsIgnoreCase(method)) {
                String[] parts = path.split("/");
                int stationId = Integer.parseInt(parts[3]);
                List<StationLocation> locations = stationDAO.getLocationsByStation(stationId);
                sendJsonResponse(exchange, 200, Map.of("success", true, "data", locations));
                return;
            }

            // BOOKINGS
            if (path.equals("/api/bookings/estimate") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                int stationId = ((Number) body.get("stationId")).intValue();
                String category = (String) body.getOrDefault("category", "Standard Bag");
                double weight = ((Number) body.get("weightKg")).doubleValue();
                int bagCount = ((Number) body.getOrDefault("bagCount", 1)).intValue();
                boolean isFragile = Boolean.TRUE.equals(body.get("isFragile"));
                BigDecimal fare = fareService.calculateEstimate(stationId, category, weight, bagCount, isFragile);
                sendJsonResponse(exchange, 200, Map.of("success", true, "data", Map.of("estimatedFare", fare)));
                return;
            }

            if (path.equals("/api/bookings") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                int passengerId = ((Number) body.getOrDefault("passengerId", 1)).intValue();
                int stationId = ((Number) body.get("stationId")).intValue();
                int pickupLocId = ((Number) body.get("pickupLocationId")).intValue();
                int dropLocId = ((Number) body.get("dropLocationId")).intValue();
                String trainNo = (String) body.getOrDefault("trainNumber", "");
                String trainName = (String) body.getOrDefault("trainName", "");
                String coach = (String) body.getOrDefault("coachNumber", "");
                String seat = (String) body.getOrDefault("seatNumber", "");
                String category = (String) body.getOrDefault("category", "Standard Bag");
                int bagCount = ((Number) body.getOrDefault("bagCount", 1)).intValue();
                BigDecimal weight = new BigDecimal(body.get("weightKg").toString());
                boolean isFragile = Boolean.TRUE.equals(body.get("isFragile"));
                String notes = (String) body.getOrDefault("specialInstructions", "");
                int porterId = body.containsKey("porterId") && body.get("porterId") != null ? ((Number) body.get("porterId")).intValue() : 0;

                Map<String, Object> res = bookingService.createBooking(
                        passengerId, stationId, pickupLocId, dropLocId,
                        trainNo, trainName, coach, seat,
                        category, bagCount, weight, isFragile, notes, porterId
                );
                sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Booking created", "data", res));
                return;
            }

            if (path.equals("/api/bookings/history") && "GET".equalsIgnoreCase(method)) {
                int pid = queryParams.containsKey("passengerId") ? Integer.parseInt(queryParams.get("passengerId")) : 1;
                List<Booking> history = bookingService.getPassengerBookings(pid);
                sendJsonResponse(exchange, 200, Map.of("success", true, "data", history));
                return;
            }

            if (path.startsWith("/api/bookings/") && "GET".equalsIgnoreCase(method)) {
                String idStr = path.substring("/api/bookings/".length());
                int bookingId = Integer.parseInt(idStr);
                Booking b = bookingService.getBooking(bookingId);
                if (b != null) {
                    sendJsonResponse(exchange, 200, Map.of("success", true, "data", b));
                } else {
                    sendJsonResponse(exchange, 404, Map.of("success", false, "message", "Booking not found"));
                }
                return;
            }

            if (path.matches("/api/bookings/\\d+/cancel") && ("PUT".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method))) {
                String[] parts = path.split("/");
                int bookingId = Integer.parseInt(parts[3]);
                Map<String, Object> body = readJsonBody(exchange);
                String reason = (String) body.getOrDefault("reason", "Cancelled by passenger");
                Map<String, Object> res = bookingService.updateStatus(bookingId, "CANCELLED", "PASSENGER", 0, reason);
                sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Booking cancelled", "data", res));
                return;
            }

            // PORTERS
            if (path.equals("/api/porters/available") && "GET".equalsIgnoreCase(method)) {
                int stationId = Integer.parseInt(queryParams.getOrDefault("stationId", "1"));
                List<Porter> porters = porterService.getAvailablePorters(stationId);
                sendJsonResponse(exchange, 200, Map.of("success", true, "data", porters));
                return;
            }

            if (path.equals("/api/porters/requests") && "GET".equalsIgnoreCase(method)) {
                int stationId = Integer.parseInt(queryParams.getOrDefault("stationId", "1"));
                List<Booking> requests = bookingService.getPorterRequests(stationId);
                sendJsonResponse(exchange, 200, Map.of("success", true, "data", requests));
                return;
            }

            if (path.equals("/api/porters/history") && "GET".equalsIgnoreCase(method)) {
                int porterId = Integer.parseInt(queryParams.getOrDefault("porterId", "1"));
                List<Booking> jobs = bookingService.getPorterBookings(porterId);
                sendJsonResponse(exchange, 200, Map.of("success", true, "data", jobs));
                return;
            }

            if (path.equals("/api/porters/availability") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                int porterId = ((Number) body.getOrDefault("porterId", 1)).intValue();
                boolean isOnline = Boolean.TRUE.equals(body.get("isOnline"));
                int platform = ((Number) body.getOrDefault("platform", 1)).intValue();
                boolean ok = porterService.updateAvailability(porterId, isOnline, platform);
                sendJsonResponse(exchange, 200, Map.of("success", ok, "data", Map.of("isOnline", isOnline, "platform", platform)));
                return;
            }

            if (path.matches("/api/porters/bookings/\\d+/accept") && ("PUT".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method))) {
                String[] parts = path.split("/");
                int bookingId = Integer.parseInt(parts[4]);
                Map<String, Object> body = readJsonBody(exchange);
                int porterId = ((Number) body.getOrDefault("porterId", 1)).intValue();
                Map<String, Object> res = bookingService.porterAcceptBooking(bookingId, porterId);
                sendJsonResponse(exchange, 200, Map.of("success", res.get("success"), "message", res.get("message"), "data", res));
                return;
            }

            if (path.matches("/api/porters/bookings/\\d+/status") && ("PUT".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method))) {
                String[] parts = path.split("/");
                int bookingId = Integer.parseInt(parts[4]);
                Map<String, Object> body = readJsonBody(exchange);
                int porterId = ((Number) body.getOrDefault("porterId", 1)).intValue();
                String status = (String) body.get("status");
                String notes = (String) body.getOrDefault("notes", "Status updated by porter");
                Map<String, Object> res = bookingService.updateStatus(bookingId, status, "PORTER", porterId, notes);
                sendJsonResponse(exchange, 200, Map.of("success", res.get("success"), "message", res.get("message"), "data", res));
                return;
            }

            // PAYMENTS
            if (path.equals("/api/payments/demo") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                int bookingId = ((Number) body.get("bookingId")).intValue();
                BigDecimal amount = new BigDecimal(body.get("amount").toString());
                String methodChoice = (String) body.getOrDefault("paymentMethod", "ONLINE_DEMO");
                Payment p = bookingService.processPayment(bookingId, amount, methodChoice);
                sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Payment processed successfully", "data", p));
                return;
            }

            // FEEDBACK
            if (path.equals("/api/feedback") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                int bookingId = ((Number) body.get("bookingId")).intValue();
                int passengerId = ((Number) body.get("passengerId")).intValue();
                int porterId = ((Number) body.get("porterId")).intValue();
                int rating = ((Number) body.get("rating")).intValue();
                String comments = (String) body.getOrDefault("comments", "");
                Feedback f = bookingService.submitFeedback(bookingId, passengerId, porterId, rating, comments);
                sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Feedback recorded", "data", f));
                return;
            }

            // ADMIN
            if (path.equals("/api/admin/dashboard") && "GET".equalsIgnoreCase(method)) {
                Map<String, Object> stats = adminService.getDashboardStats();
                sendJsonResponse(exchange, 200, Map.of("success", true, "data", stats));
                return;
            }

            if (path.equals("/api/admin/passengers") && "GET".equalsIgnoreCase(method)) {
                List<Passenger> passengers = adminService.getAllPassengers();
                sendJsonResponse(exchange, 200, Map.of("success", true, "data", passengers));
                return;
            }

            if (path.equals("/api/admin/porters") && "GET".equalsIgnoreCase(method)) {
                List<Porter> porters = adminService.getAllPorters();
                sendJsonResponse(exchange, 200, Map.of("success", true, "data", porters));
                return;
            }

            if (path.equals("/api/admin/bookings") && "GET".equalsIgnoreCase(method)) {
                List<Booking> bookings = adminService.getAllBookings();
                sendJsonResponse(exchange, 200, Map.of("success", true, "data", bookings));
                return;
            }

            if (path.equals("/api/admin/stations") && "POST".equalsIgnoreCase(method)) {
                Map<String, Object> body = readJsonBody(exchange);
                Station s = new Station();
                s.setStationCode((String) body.get("stationCode"));
                s.setStationName((String) body.get("stationName"));
                s.setCity((String) body.get("city"));
                s.setState((String) body.get("state"));
                s.setTotalPlatforms(body.containsKey("totalPlatforms") ? ((Number) body.get("totalPlatforms")).intValue() : 10);
                Station created = adminService.addStation(s);
                sendJsonResponse(exchange, 200, Map.of("success", true, "data", created));
                return;
            }

            if (path.matches("/api/admin/porters/\\d+/approve") && ("PUT".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method))) {
                String[] parts = path.split("/");
                int porterId = Integer.parseInt(parts[4]);
                Map<String, Object> body = readJsonBody(exchange);
                String status = (String) body.getOrDefault("status", "APPROVED");
                boolean ok = adminService.approvePorter(porterId, status);
                sendJsonResponse(exchange, 200, Map.of("success", ok, "message", "Porter status set to " + status));
                return;
            }

            if (path.equals("/api/admin/dbms-demo") && "GET".equalsIgnoreCase(method)) {
                String concept = queryParams.getOrDefault("concept", "JOINS");
                Map<String, Object> demo = adminService.runDemoQuery(concept);
                sendJsonResponse(exchange, 200, Map.of("success", true, "data", demo));
                return;
            }

            sendJsonResponse(exchange, 404, Map.of("success", false, "message", "Unknown endpoint: " + path));
        }

        private void serveStatic(HttpExchange exchange, String path) throws IOException {
            if (path == null || path.isEmpty() || path.equals("/")) {
                path = "/index.html";
            }

            // Strip leading /frontend if present so /frontend/assets/... resolves to /assets/...
            String relPath = path;
            if (relPath.startsWith("/frontend/")) {
                relPath = relPath.substring("/frontend".length());
            } else if (relPath.equals("/frontend")) {
                relPath = "/index.html";
            }

            String[] candidates = {
                "frontend" + relPath,
                "frontend" + path,
                "../frontend" + relPath,
                "backend/src/main/webapp" + path,
                "src/main/webapp" + path
            };

            File file = null;
            for (String c : candidates) {
                File f = new File(c);
                if (f.exists() && !f.isDirectory()) {
                    file = f;
                    break;
                }
            }

            if (file == null) {
                File f = new File("frontend/index.html");
                if (f.exists()) file = f;
                else {
                    File f2 = new File("src/main/webapp/index.html");
                    if (f2.exists()) file = f2;
                }
            }

            if (file == null || !file.exists()) {
                String msg = "<h1>404 Not Found - Smart Porter</h1><p>Requested: " + path + "</p>";
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(404, msg.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(msg.getBytes(StandardCharsets.UTF_8));
                }
                return;
            }

            String mime = "text/html";
            if (path.endsWith(".css")) mime = "text/css";
            else if (path.endsWith(".js")) mime = "application/javascript";
            else if (path.endsWith(".json")) mime = "application/json";
            else if (path.endsWith(".png")) mime = "image/png";
            else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) mime = "image/jpeg";
            else if (path.endsWith(".svg")) mime = "image/svg+xml";

            byte[] b = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", mime + "; charset=UTF-8");
            exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
            exchange.sendResponseHeaders(200, b.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(b);
            }
        }

        private Map<String, Object> readJsonBody(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int read;
            while ((read = is.read(buf)) != -1) baos.write(buf, 0, read);
            String json = baos.toString(StandardCharsets.UTF_8).trim();
            if (json.isEmpty()) return new HashMap<>();
            return JsonUtil.parseMap(json);
        }

        private void sendJsonResponse(HttpExchange exchange, int status, Object data) throws IOException {
            String json = JsonUtil.toJson(data);
            byte[] b = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(status, b.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(b);
            }
        }

        private Map<String, String> parseQueryParams(String query) {
            Map<String, String> params = new HashMap<>();
            if (query == null || query.isEmpty()) return params;
            for (String pair : query.split("&")) {
                int idx = pair.indexOf('=');
                if (idx > 0) {
                    try {
                        String k = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                        String v = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                        params.put(k, v);
                    } catch (Exception ignored) {}
                }
            }
            return params;
        }
    }
}
