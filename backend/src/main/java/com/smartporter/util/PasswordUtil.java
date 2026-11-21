package com.smartporter.util;

import com.smartporter.config.AppConfig;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) return "";
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(AppConfig.BCRYPT_ROUNDS));
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || hashedPassword.isEmpty()) return false;
        try {
            if (hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$") || hashedPassword.startsWith("$2y$")) {
                return BCrypt.checkpw(plainPassword, hashedPassword);
            }
            // Fallback for legacy plain or simple hashes
            return plainPassword.equals(hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
