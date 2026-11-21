package com.smartporter.controller;

import com.smartporter.model.Station;
import com.smartporter.service.AdminService;
import com.smartporter.util.JsonUtil;
import com.smartporter.util.ResponseUtil;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null) path = "";

        try {
            switch (path) {
                case "/dashboard":
                    ResponseUtil.sendSuccess(response, "Admin metrics", adminService.getDashboardStats());
                    break;
                case "/passengers":
                    ResponseUtil.sendSuccess(response, "Passengers list", adminService.getAllPassengers());
                    break;
                case "/porters":
                    ResponseUtil.sendSuccess(response, "Porters list", adminService.getAllPorters());
                    break;
                case "/bookings":
                    ResponseUtil.sendSuccess(response, "All bookings list", adminService.getAllBookings());
                    break;
                case "/stations":
                    ResponseUtil.sendSuccess(response, "All stations list", adminService.getAllStations());
                    break;
                case "/dbms-demo":
                    String concept = request.getParameter("concept");
                    ResponseUtil.sendSuccess(response, "DBMS Demo executed", adminService.runDemoQuery(concept));
                    break;
                default:
                    ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Admin endpoint not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        try {
            if ("/stations".equals(path)) {
                Map<String, Object> body = JsonUtil.readBody(request);
                Station s = new Station();
                s.setStationCode((String) body.get("stationCode"));
                s.setStationName((String) body.get("stationName"));
                s.setCity((String) body.get("city"));
                s.setState((String) body.get("state"));
                s.setTotalPlatforms(body.containsKey("totalPlatforms") ? ((Number) body.get("totalPlatforms")).intValue() : 10);

                Station created = adminService.addStation(s);
                ResponseUtil.sendSuccess(response, "Station added successfully", created);
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
            // e.g. /porters/123/approve
            String[] parts = path.split("/");
            if (parts.length >= 4 && "porters".equalsIgnoreCase(parts[1])) {
                int porterId = Integer.parseInt(parts[2]);
                String action = parts[3]; // approve or reject
                String status = "reject".equalsIgnoreCase(action) ? "REJECTED" : "APPROVED";

                boolean ok = adminService.approvePorter(porterId, status);
                ResponseUtil.sendSuccess(response, "Porter verification status updated to " + status, Map.of("porterId", porterId, "status", status));
                return;
            }

            ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "PUT endpoint not found");
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
