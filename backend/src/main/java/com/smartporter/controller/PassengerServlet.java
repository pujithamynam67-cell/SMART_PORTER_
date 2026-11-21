package com.smartporter.controller;

import com.smartporter.dao.PassengerDAO;
import com.smartporter.model.Passenger;
import com.smartporter.service.BookingService;
import com.smartporter.util.ResponseUtil;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/api/passengers/*")
public class PassengerServlet extends HttpServlet {

    private final PassengerDAO passengerDAO = new PassengerDAO();
    private final BookingService bookingService = new BookingService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null) path = "";

        try {
            int passengerId = resolvePassengerId(request);
            if (passengerId <= 0) {
                ResponseUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Passenger not authenticated");
                return;
            }

            if ("/profile".equals(path)) {
                Passenger p = passengerDAO.getById(passengerId);
                ResponseUtil.sendSuccess(response, "Profile fetched", p);
            } else if ("/history".equals(path)) {
                ResponseUtil.sendSuccess(response, "Booking history fetched", bookingService.getPassengerBookings(passengerId));
            } else {
                ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private int resolvePassengerId(HttpServletRequest request) {
        String param = request.getParameter("passengerId");
        if (param != null && !param.isEmpty()) {
            return Integer.parseInt(param);
        }
        HttpSession session = request.getSession(false);
        if (session != null && "PASSENGER".equals(session.getAttribute("USER_TYPE"))) {
            return (Integer) session.getAttribute("USER_ID");
        }
        return 1; // Default demo passenger fallback
    }
}
