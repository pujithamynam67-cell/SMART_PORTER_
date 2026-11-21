package com.smartporter.controller;

import com.smartporter.model.Booking;
import com.smartporter.model.Porter;
import com.smartporter.service.BookingService;
import com.smartporter.service.PorterService;
import com.smartporter.util.JsonUtil;
import com.smartporter.util.ResponseUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/api/porters/*")
public class PorterServlet extends HttpServlet {

    private final PorterService porterService = new PorterService();
    private final BookingService bookingService = new BookingService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null) path = "";

        try {
            if ("/available".equals(path)) {
                int stationId = Integer.parseInt(request.getParameter("stationId"));
                List<Porter> porters = porterService.getAvailablePorters(stationId);
                ResponseUtil.sendSuccess(response, "Available porters fetched", porters);
                return;
            }

            if ("/requests".equals(path)) {
                int stationId = Integer.parseInt(request.getParameter("stationId"));
                List<Booking> requests = bookingService.getPorterRequests(stationId);
                ResponseUtil.sendSuccess(response, "Incoming requests fetched", requests);
                return;
            }

            if ("/history".equals(path)) {
                int porterId = resolvePorterId(request);
                List<Booking> jobs = bookingService.getPorterBookings(porterId);
                ResponseUtil.sendSuccess(response, "Porter jobs fetched", jobs);
                return;
            }

            if ("/profile".equals(path)) {
                int porterId = resolvePorterId(request);
                Porter p = porterService.getProfile(porterId);
                ResponseUtil.sendSuccess(response, "Porter profile fetched", p);
                return;
            }

            ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Porter endpoint not found");
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        try {
            if ("/availability".equals(path)) {
                Map<String, Object> body = JsonUtil.readBody(request);
                int porterId = body.containsKey("porterId") ? ((Number) body.get("porterId")).intValue() : resolvePorterId(request);
                boolean isOnline = Boolean.TRUE.equals(body.get("isOnline"));
                int platform = body.containsKey("platform") ? ((Number) body.get("platform")).intValue() : 1;

                boolean ok = porterService.updateAvailability(porterId, isOnline, platform);
                ResponseUtil.sendSuccess(response, "Availability updated", Map.of("isOnline", isOnline, "platform", platform));
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
            String[] parts = path.split("/"); // e.g. /bookings/123/accept or /bookings/123/status
            if (parts.length >= 4 && "bookings".equalsIgnoreCase(parts[1])) {
                int bookingId = Integer.parseInt(parts[2]);
                String action = parts[3];
                Map<String, Object> body = JsonUtil.readBody(request);
                int porterId = body.containsKey("porterId") ? ((Number) body.get("porterId")).intValue() : resolvePorterId(request);

                if ("accept".equalsIgnoreCase(action)) {
                    Map<String, Object> res = bookingService.porterAcceptBooking(bookingId, porterId);
                    ResponseUtil.sendSuccess(response, (String) res.get("message"), res);
                    return;
                }

                if ("reject".equalsIgnoreCase(action)) {
                    ResponseUtil.sendSuccess(response, "Booking rejected by porter", null);
                    return;
                }

                if ("status".equalsIgnoreCase(action)) {
                    String status = (String) body.get("status");
                    String notes = (String) body.getOrDefault("notes", "Status updated by porter");
                    Map<String, Object> res = bookingService.updateStatus(bookingId, status, "PORTER", porterId, notes);
                    ResponseUtil.sendSuccess(response, (String) res.get("message"), res);
                    return;
                }
            }

            ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "PUT endpoint not found");
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int porterId = resolvePorterId(request);
            boolean ok = porterService.deletePorter(porterId);
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            ResponseUtil.sendSuccess(response, "Porter account deleted successfully", Map.of("porterId", porterId));
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private int resolvePorterId(HttpServletRequest request) {
        String param = request.getParameter("porterId");
        if (param != null && !param.isEmpty()) {
            return Integer.parseInt(param);
        }
        HttpSession session = request.getSession(false);
        if (session != null && "PORTER".equals(session.getAttribute("USER_TYPE"))) {
            return (Integer) session.getAttribute("USER_ID");
        }
        return 1; // Default seed porter (CK-101)
    }
}
