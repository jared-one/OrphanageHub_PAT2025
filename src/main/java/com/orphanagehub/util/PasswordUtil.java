// Isolated Code: util/PasswordUtil.java (Based on Analysis - This is the Core Issue Source)
// Full Class Provided Below for Context; Error Originates in verifyPassword() Due to Malformed Stored Hashes

package com.orphanagehub.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // Hashes a plain-text password using BCrypt with default work factor (10)
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    // Verifies a plain-text candidate against a stored BCrypt hash
    // ISSUE HERE: If storedHash is not a valid BCrypt format (e.g., starts with $2a$ or $2b$), 
    // BCrypt.checkpw() throws IllegalArgumentException: "Invalid salt version"
    // Root Cause: Sample DB data uses placeholders like "hash_admin_pw" instead of real hashes.
    // Structure Dependency: Called from service/AuthenticationService or dao/UserDAO during login verification.
    // Relies on: org.mindrot.jbcrypt.BCrypt (from pom.xml).
    // Called by: LoginPanel's btnLogin ActionListener (after DB integration) → AuthenticationService.authenticate() → this.verifyPassword().
    // Impacts: If fails, JOptionPane shows error; navigation to dashboard blocked.
    public static boolean verifyPassword(String candidatePassword, String storedHash) {
        try {
            return BCrypt.checkpw(candidatePassword, storedHash);
        } catch (IllegalArgumentException e) {
            // Log the error (assuming SLF4J is set up as per pom.xml and previous AI analysis)
            // Logger logger = LoggerFactory.getLogger(PasswordUtil.class); // Add this
            // logger.error("Password verification failed: {}", e.getMessage());
            throw new RuntimeException("Login error: Invalid salt version"); // Matches screenshot error
        }
    }
}