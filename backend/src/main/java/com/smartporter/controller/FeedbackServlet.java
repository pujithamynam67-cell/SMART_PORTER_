package com.smartporter.controller;

import com.smartporter.model.Feedback;
import com.smartporter.service.BookingService;
import com.smartporter.util.JsonUtil;
import com.smartporter.util.ResponseUtil;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/feedback/*")
public class FeedbackServlet extends HttpServlet {

    private final BookingService bookingService = new BookingService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Map<String, Object> body = JsonUtil.readBody(request);
            int bookingId = ((Number) body.get("bookingId")).intValue();
            int passengerId = ((Number) body.get("passengerId")).intValue();
            int porterId = ((Number) body.get("porterId")).intValue();
            int rating = ((Number) body.get("rating")).intValue();
            String comments = (String) body.getOrDefault("comments", "");

            Feedback f = bookingService.submitFeedback(bookingId, passengerId, porterId, rating, comments);
            ResponseUtil.sendSuccess(response, "Feedback submitted successfully. Porter rating updated via trigger.", f);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
