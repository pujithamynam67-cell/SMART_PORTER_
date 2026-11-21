package com.smartporter.service;

import com.smartporter.dao.AdminDAO;
import com.smartporter.dao.BookingDAO;
import com.smartporter.dao.PassengerDAO;
import com.smartporter.dao.PorterDAO;
import com.smartporter.dao.StationDAO;
import com.smartporter.model.Booking;
import com.smartporter.model.Passenger;
import com.smartporter.model.Porter;
import com.smartporter.model.Station;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AdminService {

    private final AdminDAO adminDAO = new AdminDAO();
    private final PassengerDAO passengerDAO = new PassengerDAO();
    private final PorterDAO porterDAO = new PorterDAO();
    private final StationDAO stationDAO = new StationDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    public Map<String, Object> getDashboardStats() throws SQLException {
        return adminDAO.getDashboardStatistics();
    }

    public List<Passenger> getAllPassengers() throws SQLException {
        return passengerDAO.getAll();
    }

    public List<Porter> getAllPorters() throws SQLException {
        return porterDAO.getAll();
    }

    public boolean approvePorter(int porterId, String status) throws SQLException {
        return porterDAO.updateVerification(porterId, status);
    }

    public List<Station> getAllStations() throws SQLException {
        return stationDAO.getAll();
    }

    public Station addStation(Station s) throws SQLException {
        return stationDAO.createStation(s);
    }

    public List<Booking> getAllBookings() throws SQLException {
        return bookingDAO.getAll();
    }

    public Map<String, Object> runDemoQuery(String concept) throws SQLException {
        return adminDAO.executeDemoQuery(concept);
    }
}
