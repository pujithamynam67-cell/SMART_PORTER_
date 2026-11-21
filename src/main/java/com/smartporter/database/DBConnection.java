package com.smartporter.database;

import com.smartporter.config.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC Connection Factory for Smart Porter.
 * Uses dynamic credentials loaded from .env file via AppConfig.
 * Never hardcodes any passwords or connection strings.
 */
public class DBConnection {

    static {
        try {
            Class.forName(AppConfig.DB_DRIVER);
            System.out.println("[DBConnection] MySQL JDBC Driver loaded: " + AppConfig.DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] ERROR: MySQL JDBC Driver class not found: " + e.getMessage());
        }
    }

    /**
     * Obtains a fresh database connection using dynamic credentials.
     * @return Connection instance
     * @throws SQLException on connection error
     */
    public static Connection getConnection() throws SQLException {
        String url = AppConfig.getDbUrl();
        String username = AppConfig.DB_USERNAME;
        String password = AppConfig.DB_PASSWORD;
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Diagnostic helper to test the MySQL connection.
     * @return true if connection succeeds, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean valid = conn != null && !conn.isClosed();
            if (valid) {
                System.out.println("[DBConnection] SUCCESS: Connected to MySQL database '" 
                    + AppConfig.DB_NAME + "' at " + AppConfig.DB_HOST + ":" + AppConfig.DB_PORT);
            }
            return valid;
        } catch (SQLException e) {
            System.err.println("[DBConnection] FAILURE: Could not connect to MySQL database.");
            System.err.println("[DBConnection] Error: " + e.getMessage());
            System.err.println("[DBConnection] Check your .env configuration (DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD).");
            return false;
        }
    }
}
