package com.itheima.bigevent.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Password hashes are BCrypt. Existing MD5 rows are upgraded at the next successful login. */
public final class PasswordUtil {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {}

    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String storedHash) {
        if (storedHash == null || storedHash.isBlank()) return false;
        return isBcrypt(storedHash)
                ? ENCODER.matches(rawPassword, storedHash)
                : Md5Util.getMD5String(rawPassword).equalsIgnoreCase(storedHash);
    }

    public static boolean needsUpgrade(String storedHash) {
        return storedHash != null && !storedHash.isBlank() && !isBcrypt(storedHash);
    }

    private static boolean isBcrypt(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
