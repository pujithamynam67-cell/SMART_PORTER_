package com.smartporter.controller;

import com.smartporter.model.Admin;
import com.smartporter.model.Passenger;
import com.smartporter.model.Porter;
import com.smartporter.service.AuthService;
import com.smartporter.util.JsonUtil;
import com.smartporter.util.ResponseUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null) path = "";

        try {
            Map<String, Object> body = JsonUtil.readBody(request);

            switch (path) {
                case "/register":
                    handlePassengerRegister(request, response, body);
                    break;
                case "/login":
                    handlePassengerLogin(request, response, body);
                    break;
                case "/porter-register":
                    handlePorterRegister(request, response, body);
                    break;
                case "/porter-login":
                    handlePorterLogin(request, response, body);
                    break;
                case "/admin-login":
                    handleAdminLogin(request, response, body);
                    break;
                case "/logout":
                    handleLogout(request, response);
                    break;
                default:
                    ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Auth endpoint not found: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        if ("/me".equals(path)) {
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("USER_TYPE") != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("userType", session.getAttribute("USER_TYPE"));
                data.put("user", session.getAttribute("USER_OBJ"));
                ResponseUtil.sendSuccess(response, "Session active", data);
            } else {
                ResponseUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "No active session");
            }
        } else {
            ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }

    private void handlePassengerRegister(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> body) throws Exception {
        Passenger p = new Passenger();
        p.setFullName((String) body.get("fullName"));
        p.setPhone((String) body.get("phone"));
        p.setEmail((String) body.get("email"));
        p.setGender((String) body.getOrDefault("gender", "Male"));
        p.setSeniorCitizen(Boolean.TRUE.equals(body.get("isSeniorCitizen")));
        String password = (String) body.get("password");

        Passenger created = authService.registerPassenger(p, password);
        HttpSession session = req.getSession(true);
        session.setAttribute("USER_TYPE", "PASSENGER");
        session.setAttribute("USER_ID", created.getPassengerId());
        session.setAttribute("USER_OBJ", created);

        ResponseUtil.sendSuccess(resp, "Passenger registered successfully", created);
    }

    private void handlePassengerLogin(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> body) throws Exception {
        String id = (String) body.get("emailOrPhone");
        String password = (String) body.get("password");

        Passenger p = authService.loginPassenger(id, password);
        if (p != null) {
            HttpSession session = req.getSession(true);
            session.setAttribute("USER_TYPE", "PASSENGER");
            session.setAttribute("USER_ID", p.getPassengerId());
            session.setAttribute("USER_OBJ", p);
            ResponseUtil.sendSuccess(resp, "Login successful", p);
        } else {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid phone/email or password.");
        }
    }

    private void handlePorterRegister(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> body) throws Exception {
        Porter p = new Porter();
        p.setBadgeNumber((String) body.get("badgeNumber"));
        p.setFullName((String) body.get("fullName"));
        p.setPhone((String) body.get("phone"));
        p.setEmail((String) body.get("email"));
        p.setStationId(((Number) body.get("stationId")).intValue());
        String password = (String) body.get("password");

        Porter created = authService.registerPorter(p, password);
        ResponseUtil.sendSuccess(resp, "Porter registration submitted. Awaiting station admin approval.", created);
    }

    private void handlePorterLogin(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> body) throws Exception {
        String id = (String) body.get("phoneOrBadge");
        String password = (String) body.get("password");

        Porter p = authService.loginPorter(id, password);
        if (p != null) {
            HttpSession session = req.getSession(true);
            session.setAttribute("USER_TYPE", "PORTER");
            session.setAttribute("USER_ID", p.getPorterId());
            session.setAttribute("USER_OBJ", p);
            ResponseUtil.sendSuccess(resp, "Porter login successful", p);
        } else {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid porter badge/phone or password.");
        }
    }

    private void handleAdminLogin(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> body) throws Exception {
        String username = (String) body.get("username");
        String password = (String) body.get("password");

        Admin a = authService.loginAdmin(username, password);
        if (a != null) {
            HttpSession session = req.getSession(true);
            session.setAttribute("USER_TYPE", "ADMIN");
            session.setAttribute("USER_ID", a.getAdminId());
            session.setAttribute("USER_OBJ", a);
            ResponseUtil.sendSuccess(resp, "Admin login successful", a);
        } else {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid admin credentials.");
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        ResponseUtil.sendSuccess(resp, "Logged out successfully", null);
    }
}
