package com.orphanagehub.util;

import io.vavr.control.Option;
import io.vavr.control.Try;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import java.util.regex.Pattern;

/**
 * Utility for password hashing and verification using BCrypt.
 * Provides secure storage and comparison of passwords.
 */
public class PasswordUtil {

    private static final int WORK_FACTOR = 12; // Balanced security vs performance

    /**
     * Hashes a plain-text password using BCrypt.
     * @param plainPassword The password to hash (char[] for security).
     * @return The hashed password as a String.
     */
    public static String hash(char[] plainPassword) {
        return BCrypt.hashpw(new String(plainPassword), BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Verifies a plain-text password against a stored hash.
     * @param plainPassword The input password (char[]).
     * @param hashedPassword The stored hash.
     * @return True if matches, false otherwise.
     */
    public static boolean verify(char[] plainPassword, String hashedPassword) {
        return BCrypt.checkpw(new String(plainPassword), hashedPassword);
    }
}

