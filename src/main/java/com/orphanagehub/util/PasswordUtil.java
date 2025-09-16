package com.orphanagehub.util;

import io.vavr.control.Try;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Enhanced password utility with BCrypt hashing and security features.
 * Provides secure password hashing, verification, and strength checking.
 * 
 * @author OrphanageHub Team
 * @version 2.0
 * @since 2025-09-06
 */
public class PasswordUtil {

    private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);
    
    // BCrypt work factor - 12 for good balance of security and performance
    private static final int WORK_FACTOR = 12;
    
    // Password complexity requirements
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    
    // Pattern for password strength validation
    private static final Pattern STRONG_PASSWORD = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$"
    );

    /**
     * Hashes a plain-text password using BCrypt.
     * 
     * @param plainPassword The password to hash (char[] for security)
     * @return The hashed password as a String
     */
    public static String hash(char[] plainPassword) {
        if (plainPassword == null || plainPassword.length == 0) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        try {
            String password = new String(plainPassword);
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt(WORK_FACTOR));
            
            // Clear the temporary string from memory
            password = null;
            
            logger.debug("Password hashed successfully");
            return hashed;
        } catch (Exception e) {
            logger.error("Error hashing password", e);
            throw new RuntimeException("Failed to hash password", e);
        } finally {
            // Clear the char array for security
            java.util.Arrays.fill(plainPassword, '\0');
        }
    }

    /**
     * Hashes a plain-text password using BCrypt (String version for compatibility).
     * 
     * @param plainPassword The password to hash
     * @return The hashed password as a String
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return hash(plainPassword.toCharArray());
    }

    /**
     * Verifies a plain-text password against a stored BCrypt hash.
     * Handles both BCrypt formats and legacy placeholders gracefully.
     * 
     * @param plainPassword The input password (char[])
     * @param hashedPassword The stored hash
     * @return True if matches, false otherwise
     */
    public static boolean verify(char[] plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.length == 0 || hashedPassword == null) {
            return false;
        }
        
        try {
            // Check if it's a valid BCrypt hash format
            if (!isValidBCryptHash(hashedPassword)) {
                logger.warn("Invalid BCrypt hash format encountered");
                return false;
            }
            
            String password = new String(plainPassword);
            boolean matches = BCrypt.checkpw(password, hashedPassword);
            
            // Clear the temporary string
            password = null;
            
            return matches;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid salt version in stored hash", e);
            return false;
        } catch (Exception e) {
            logger.error("Error verifying password", e);
            return false;
        } finally {
            // Clear the char array for security
            java.util.Arrays.fill(plainPassword, '\0');
        }
    }

    /**
     * Verifies a plain-text password against a stored BCrypt hash (String version).
     * 
     * @param plainPassword The input password
     * @param hashedPassword The stored hash
     * @return True if matches, false otherwise
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return verify(plainPassword.toCharArray(), hashedPassword);
    }

    /**
     * Checks if a password meets strength requirements.
     * 
     * @param password The password to check
     * @return True if password is strong, false otherwise
     */
    public static boolean isStrong(char[] password) {
        if (password == null || password.length < MIN_LENGTH || password.length > MAX_LENGTH) {
            return false;
        }
        
        String passwordStr = new String(password);
        boolean isStrong = STRONG_PASSWORD.matcher(passwordStr).matches();
        
        // Clear the temporary string
        passwordStr = null;
        
        return isStrong;
    }

    /**
     * Checks if a password meets strength requirements (String version).
     * 
     * @param password The password to check
     * @return True if password is strong, false otherwise
     */
    public static boolean isStrong(String password) {
        if (password == null) return false;
        return isStrong(password.toCharArray());
    }

    /**
     * Generates a secure random password reset token.
     * 
     * @return A secure random token as a base64 string
     */
    public static String generateResetToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Generates a secure random verification token for email verification.
     * 
     * @return A secure random token as a base64 string
     */
    public static String generateVerificationToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Validates if a string is a valid BCrypt hash.
     * 
     * @param hash The string to validate
     * @return True if valid BCrypt hash format
     */
    private static boolean isValidBCryptHash(String hash) {
        if (hash == null || hash.length() < 60) {
            return false;
        }
        // BCrypt hashes start with $2a$, $2b$, $2x$, or $2y$
        return hash.matches("^\\$2[abxy]\\$\\d{2}\\$.{53}$");
    }

    /**
     * Gets password strength score (0-100).
     * 
     * @param password The password to score
     * @return Score from 0 (weakest) to 100 (strongest)
     */
    public static int getStrengthScore(char[] password) {
        if (password == null || password.length == 0) return 0;
        
        int score = 0;
        String pwd = new String(password);
        
        // Length score (max 30 points)
        score += Math.min(password.length * 2, 30);
        
        // Character variety (max 40 points)
        if (pwd.matches(".*[a-z].*")) score += 10;
        if (pwd.matches(".*[A-Z].*")) score += 10;
        if (pwd.matches(".*\\d.*")) score += 10;
        if (pwd.matches(".*[@$!%*?&#].*")) score += 10;
        
        // Complexity (max 30 points)
        if (password.length >= 12) score += 10;
        if (password.length >= 16) score += 10;
        if (!pwd.matches(".*(.)(\\1{2,}).*")) score += 10; // No repeated chars
        
        // Clear temporary string
        pwd = null;
        
        return Math.min(score, 100);
    }

    /**
     * Provides user-friendly password strength message.
     * 
     * @param score The strength score (0-100)
     * @return Human-readable strength message
     */
    public static String getStrengthMessage(int score) {
        if (score < 20) return "Very Weak";
        if (score < 40) return "Weak";
        if (score < 60) return "Fair";
        if (score < 80) return "Good";
        return "Strong";
    }
}