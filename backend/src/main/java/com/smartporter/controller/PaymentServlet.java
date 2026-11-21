package com.smartporter.controller;

import com.smartporter.model.Payment;
import com.smartporter.service.BookingService;
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

@WebServlet("/api/payments/*")
public class PaymentServlet extends HttpServlet {

    private final BookingService bookingService = new BookingService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        try {
            if ("/demo".equals(path) || path == null || path.isEmpty()) {
                Map<String, Object> body = JsonUtil.readBody(request);
                int bookingId = ((Number) body.get("bookingId")).intValue();
                BigDecimal amount = new BigDecimal(body.get("amount").toString());
                String method = (String) body.getOrDefault("paymentMethod", "ONLINE_DEMO");

                Payment p = bookingService.processPayment(bookingId, amount, method);
                ResponseUtil.sendSuccess(response, "Demo payment completed and receipt generated", p);
                return;
            }

            ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Payment endpoint not found");
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
