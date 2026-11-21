package com.smartporter.controller;

import com.smartporter.dao.StationDAO;
import com.smartporter.model.Station;
import com.smartporter.model.StationLocation;
import com.smartporter.util.ResponseUtil;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/stations/*")
public class StationServlet extends HttpServlet {

    private final StationDAO stationDAO = new StationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        try {
            if (path == null || path.equals("/") || path.isEmpty()) {
                List<Station> stations = stationDAO.getAll();
                ResponseUtil.sendSuccess(response, "Stations fetched", stations);
                return;
            }

            // e.g. /api/stations/1/locations
            String[] parts = path.split("/");
            if (parts.length >= 3 && "locations".equalsIgnoreCase(parts[2])) {
                int stationId = Integer.parseInt(parts[1]);
                List<StationLocation> locations = stationDAO.getLocationsByStation(stationId);
                ResponseUtil.sendSuccess(response, "Locations fetched", locations);
                return;
            }

            if (parts.length >= 2) {
                int stationId = Integer.parseInt(parts[1]);
                Station st = stationDAO.getById(stationId);
                if (st != null) {
                    ResponseUtil.sendSuccess(response, "Station fetched", st);
                } else {
                    ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Station not found");
                }
                return;
            }

            ResponseUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid station path");
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
