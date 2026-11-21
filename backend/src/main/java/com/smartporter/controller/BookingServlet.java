package com.smartporter.controller;

import com.smartporter.model.Booking;
import com.smartporter.service.BookingService;
import com.smartporter.service.FareService;
import com.smartporter.util.JsonUtil;
import com.smartporter.util.ResponseUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/api/bookings/*")
public class BookingServlet extends HttpServlet {

    private final BookingService bookingService = new BookingService();
    private final FareService fareService = new FareService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null) path = "";

        try {
            Map<String, Object> body = JsonUtil.readBody(request);

            if ("/estimate".equals(path)) {
                int stationId = ((Number) body.get("stationId")).intValue();
                String category = (String) body.getOrDefault("category", "Standard Bag");
                double weight = ((Number) body.get("weightKg")).doubleValue();
                int bagCount = ((Number) body.getOrDefault("bagCount", 1)).intValue();
                boolean isFragile = Boolean.TRUE.equals(body.get("isFragile"));

                BigDecimal fare = fareService.calculateEstimate(stationId, category, weight, bagCount, isFragile);
                ResponseUtil.sendSuccess(response, "Fare estimated", Map.of("estimatedFare", fare));
                return;
            }

            if (path.isEmpty() || "/".equals(path)) {
                int passengerId = resolvePassengerId(request, body);
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

                Map<String, Object> result = bookingService.createBooking(
                        passengerId, stationId, pickupLocId, dropLocId,
                        trainNo, trainName, coach, seat,
                        category, bagCount, weight, isFragile, notes, porterId
                );

                ResponseUtil.sendSuccess(response, "Luggage booking created successfully", result);
                return;
            }

            ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + path);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null) path = "";

        try {
            if ("/history".equals(path)) {
                int passengerId = resolvePassengerId(request, null);
                ResponseUtil.sendSuccess(response, "Booking history fetched", bookingService.getPassengerBookings(passengerId));
                return;
            }

            if (path.startsWith("/")) {
                String idStr = path.substring(1);
                int bookingId = Integer.parseInt(idStr);
                Booking b = bookingService.getBooking(bookingId);
                if (b != null) {
                    ResponseUtil.sendSuccess(response, "Booking details fetched", b);
                } else {
                    ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Booking not found");
                }
                return;
            }

            ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null) path = "";

        try {
            // e.g. /123/cancel
            String[] parts = path.split("/");
            if (parts.length >= 3 && "cancel".equalsIgnoreCase(parts[2])) {
                int bookingId = Integer.parseInt(parts[1]);
                Map<String, Object> body = JsonUtil.readBody(request);
                String reason = (String) body.getOrDefault("reason", "Cancelled by passenger");

                Map<String, Object> res = bookingService.updateStatus(bookingId, "CANCELLED", "PASSENGER", 0, reason);
                ResponseUtil.sendSuccess(response, "Booking cancelled", res);
                return;
            }

            ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "PUT endpoint not found");
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private int resolvePassengerId(HttpServletRequest request, Map<String, Object> body) {
        if (body != null && body.containsKey("passengerId") && body.get("passengerId") != null) {
            return ((Number) body.get("passengerId")).intValue();
        }
        String param = request.getParameter("passengerId");
        if (param != null && !param.isEmpty()) {
            return Integer.parseInt(param);
        }
        HttpSession session = request.getSession(false);
        if (session != null && "PASSENGER".equals(session.getAttribute("USER_TYPE"))) {
            return (Integer) session.getAttribute("USER_ID");
        }
        return 1; // Default seed passenger
    }
}
