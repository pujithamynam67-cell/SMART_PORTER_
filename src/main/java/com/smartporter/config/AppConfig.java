package com.smartporter.config;

/**
 * Central Application Configuration.
 * Dynamically loads values from .env via DotenvLoader.
 * NEVER hardcodes database credentials or environment secrets.
 */
public class AppConfig {

    static {
        DotenvLoader.load();
    }

    // Application Details
    public static final String APP_NAME = DotenvLoader.get("APP_NAME", "SmartPorter");
    public static final String APP_ENV = DotenvLoader.get("APP_ENV", "development");
    public static final int APP_PORT = DotenvLoader.getInt("APP_PORT", 8080);
    public static final String APP_BASE_URL = DotenvLoader.get("APP_BASE_URL", "http://localhost:8080/SmartPorter");

    // MySQL Database Details (Loaded Dynamically)
    public static final String DB_HOST = DotenvLoader.get("DB_HOST", "localhost");
    public static final int DB_PORT = DotenvLoader.getInt("DB_PORT", 3306);
    public static final String DB_NAME = DotenvLoader.get("DB_NAME", "smart_porter");
    public static final String DB_USERNAME = DotenvLoader.get("DB_USERNAME", "root");
    public static final String DB_PASSWORD = DotenvLoader.get("DB_PASSWORD", "");

    // JDBC Driver & URL
    public static final String DB_DRIVER = DotenvLoader.get("DB_DRIVER", "com.mysql.cj.jdbc.Driver");
    
    public static String getDbUrl() {
        String explicitUrl = DotenvLoader.get("DB_URL");
        if (explicitUrl != null && !explicitUrl.trim().isEmpty()) {
            return explicitUrl;
        }
        return String.format(
            "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata",
            DB_HOST, DB_PORT, DB_NAME
        );
    }

    // Security & Payments
    public static final int BCRYPT_ROUNDS = DotenvLoader.getInt("BCRYPT_ROUNDS", 12);
    public static final int SESSION_TIMEOUT = DotenvLoader.getInt("SESSION_TIMEOUT", 30);
    public static final String PAYMENT_MODE = DotenvLoader.get("PAYMENT_MODE", "DEMO");
    public static final String LOG_LEVEL = DotenvLoader.get("LOG_LEVEL", "INFO");

    public static void printConfigSummary() {
        System.out.println("==================================================");
        System.out.println("  " + APP_NAME + " - Smart Luggage Management System");
        System.out.println("==================================================");
        System.out.println(" Environment   : " + APP_ENV);
        System.out.println(" Server Port   : " + APP_PORT);
        System.out.println(" Base URL      : " + APP_BASE_URL);
        System.out.println(" DB Host:Port  : " + DB_HOST + ":" + DB_PORT);
        System.out.println(" DB Database   : " + DB_NAME);
        System.out.println(" DB Username   : " + DB_USERNAME);
        System.out.println(" DB Password   : " + (DB_PASSWORD.isEmpty() ? "[EMPTY]" : "******** (Protected)"));
        System.out.println(" JDBC Driver   : " + DB_DRIVER);
        System.out.println(" Payment Mode  : " + PAYMENT_MODE);
        System.out.println("==================================================");
    }
}
