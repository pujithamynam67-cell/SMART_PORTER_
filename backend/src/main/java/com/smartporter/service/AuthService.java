package com.smartporter.service;

import com.smartporter.dao.AdminDAO;
import com.smartporter.dao.PassengerDAO;
import com.smartporter.dao.PorterDAO;
import com.smartporter.model.Admin;
import com.smartporter.model.Passenger;
import com.smartporter.model.Porter;

import java.sql.SQLException;

public class AuthService {

    private final PassengerDAO passengerDAO = new PassengerDAO();
    private final PorterDAO porterDAO = new PorterDAO();
    private final AdminDAO adminDAO = new AdminDAO();

    public Passenger registerPassenger(Passenger p, String rawPassword) throws SQLException {
        return passengerDAO.register(p, rawPassword);
    }

    public Passenger loginPassenger(String emailOrPhone, String rawPassword) throws SQLException {
        return passengerDAO.authenticate(emailOrPhone, rawPassword);
    }

    public Porter registerPorter(Porter p, String rawPassword) throws SQLException {
        return porterDAO.register(p, rawPassword);
    }

    public Porter loginPorter(String phoneOrBadge, String rawPassword) throws SQLException {
        return porterDAO.authenticate(phoneOrBadge, rawPassword);
    }

    public Admin loginAdmin(String username, String rawPassword) throws SQLException {
        return adminDAO.authenticate(username, rawPassword);
    }
}
