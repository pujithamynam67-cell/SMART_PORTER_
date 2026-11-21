package com.smartporter.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads environment variables dynamically from the backend .env or root .env file.
 * Never hardcodes database credentials or secrets.
 */
public class DotenvLoader {

    private static final Map<String, String> envMap = new HashMap<>();
    private static boolean loaded = false;

    public static synchronized void load() {
        if (loaded) return;

        String[] candidatePaths = {
            ".env",
            "backend/.env",
            "../.env",
            "../backend/.env",
            System.getProperty("user.dir") + File.separator + ".env",
            System.getProperty("user.dir") + File.separator + "backend" + File.separator + ".env"
        };

        File envFile = null;
        for (String path : candidatePaths) {
            File f = new File(path);
            if (f.exists() && f.isFile()) {
                envFile = f;
                break;
            }
        }

        if (envFile != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int eqIndex = line.indexOf('=');
                    if (eqIndex > 0) {
                        String key = line.substring(0, eqIndex).trim();
                        String value = line.substring(eqIndex + 1).trim();
                        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                            (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }
                        envMap.put(key, value);
                    }
                }
                System.out.println("[SmartPorter] Loaded .env config from: " + envFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("[SmartPorter] Warning: Failed to read .env: " + e.getMessage());
            }
        } else {
            System.out.println("[SmartPorter] No .env file found at candidate paths. Using system environment.");
        }

        loaded = true;
    }

    public static String get(String key) {
        if (!loaded) load();
        String val = envMap.get(key);
        if (val == null || val.trim().isEmpty()) {
            val = System.getenv(key);
        }
        return val;
    }

    public static String get(String key, String defaultValue) {
        String val = get(key);
        return (val != null && !val.trim().isEmpty()) ? val : defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
