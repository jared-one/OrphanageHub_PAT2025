package com.orphanagehub.service;

import com.orphanagehub.dao.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class RegistrationService {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);
    private final UserDAO userDAO = new UserDAO();

    public void registerUser(String username, String email, String fullName, String password, String confirmPassword, String userRole) throws SQLException {
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (userDAO.isUsernameTaken(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userDAO.isEmailTaken(email)) {
            throw new IllegalArgumentException("Email is already taken");
        }

        String passwordHash = hashPassword(password);
        userDAO.save(username, email, fullName, passwordHash, userRole);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Password hashing failed", e);
            throw new RuntimeException("Failed to hash password");
        }
    }
}