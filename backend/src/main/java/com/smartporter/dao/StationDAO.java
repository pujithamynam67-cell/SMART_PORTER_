package com.smartporter.dao;

import com.smartporter.model.Station;
import com.smartporter.model.StationLocation;
import com.smartporter.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StationDAO {

    public List<Station> getAll() throws SQLException {
        List<Station> list = new ArrayList<>();
        String sql = "SELECT * FROM stations WHERE is_active = TRUE ORDER BY station_name ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Station s = new Station();
                s.setStationId(rs.getInt("station_id"));
                s.setStationCode(rs.getString("station_code"));
                s.setStationName(rs.getString("station_name"));
                s.setCity(rs.getString("city"));
                s.setState(rs.getString("state"));
                s.setTotalPlatforms(rs.getInt("total_platforms"));
                s.setActive(rs.getBoolean("is_active"));
                list.add(s);
            }
        }
        for (Station s : list) {
            s.setLocations(getLocationsByStation(s.getStationId()));
        }
        return list;
    }

    public Station getById(int stationId) throws SQLException {
        String sql = "SELECT * FROM stations WHERE station_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Station s = new Station();
                    s.setStationId(rs.getInt("station_id"));
                    s.setStationCode(rs.getString("station_code"));
                    s.setStationName(rs.getString("station_name"));
                    s.setCity(rs.getString("city"));
                    s.setState(rs.getString("state"));
                    s.setTotalPlatforms(rs.getInt("total_platforms"));
                    s.setActive(rs.getBoolean("is_active"));
                    s.setLocations(getLocationsByStation(stationId));
                    return s;
                }
            }
        }
        return null;
    }

    public List<StationLocation> getLocationsByStation(int stationId) throws SQLException {
        List<StationLocation> list = new ArrayList<>();
        String sql = "SELECT * FROM station_locations WHERE station_id = ? ORDER BY location_type, location_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StationLocation loc = new StationLocation();
                    loc.setLocationId(rs.getInt("location_id"));
                    loc.setStationId(rs.getInt("station_id"));
                    loc.setLocationName(rs.getString("location_name"));
                    loc.setLocationType(rs.getString("location_type"));
                    loc.setPlatformNumber((Integer) rs.getObject("platform_number"));
                    list.add(loc);
                }
            }
        }
        return list;
    }

    public Station createStation(Station s) throws SQLException {
        String sql = "INSERT INTO stations (station_code, station_name, city, state, total_platforms, is_active) VALUES (?, ?, ?, ?, ?, TRUE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getStationCode().toUpperCase());
            ps.setString(2, s.getStationName());
            ps.setString(3, s.getCity());
            ps.setString(4, s.getState());
            ps.setInt(5, s.getTotalPlatforms() > 0 ? s.getTotalPlatforms() : 10);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) s.setStationId(rs.getInt(1));
            }
            return s;
        }
    }
}
