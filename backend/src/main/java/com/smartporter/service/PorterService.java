package com.smartporter.service;

import com.smartporter.dao.PorterDAO;
import com.smartporter.model.Porter;

import java.sql.SQLException;
import java.util.List;

public class PorterService {

    private final PorterDAO porterDAO = new PorterDAO();

    public List<Porter> getAvailablePorters(int stationId) throws SQLException {
        return porterDAO.getAvailablePorters(stationId);
    }

    public boolean updateAvailability(int porterId, boolean isOnline, int platform) throws SQLException {
        return porterDAO.updateAvailability(porterId, isOnline, platform);
    }

    public Porter getProfile(int porterId) throws SQLException {
        return porterDAO.getById(porterId);
    }

    public boolean deletePorter(int porterId) throws SQLException {
        return porterDAO.deletePorter(porterId);
    }
}
