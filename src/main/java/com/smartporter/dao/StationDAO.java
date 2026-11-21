package com.smartporter.dao;

import com.smartporter.database.DBConnection;
import com.smartporter.model.Station;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StationDAO {

    public List<Station> getAll() throws SQLException {
        List<Station> stations = new ArrayList<>();
        String sql = "SELECT * FROM stations ORDER BY station_name ASC";
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
                s.setBasePorterFare(rs.getBigDecimal("base_porter_fare"));
                s.setPerKgRate(rs.getBigDecimal("per_kg_rate"));
                stations.add(s);
            }
        }
        return stations;
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
                    s.setBasePorterFare(rs.getBigDecimal("base_porter_fare"));
                    s.setPerKgRate(rs.getBigDecimal("per_kg_rate"));
                    return s;
                }
            }
        }
        return null;
    }
}
