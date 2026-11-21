package com.smartporter.util;

import com.smartporter.config.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    static {
        try {
            Class.forName(AppConfig.DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] Error: Could not find MySQL driver class: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            AppConfig.getDbUrl(),
            AppConfig.DB_USERNAME,
            AppConfig.DB_PASSWORD
        );
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[DBConnection] Connection failed: " + e.getMessage());
            return false;
        }
    }
}
