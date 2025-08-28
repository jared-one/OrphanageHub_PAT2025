/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.service;

import com.orphanagehub.dao.OrphanageDAO;
import com.orphanagehub.dao.UserDAO;  // Added missing import
import com.orphanagehub.model.Orphanage;
import com.orphanagehub.model.User;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrationService {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private final UserDAO userDAO = new UserDAO();
    private final OrphanageDAO orphanageDAO = new OrphanageDAO();

    public User registerUser(
            String username,
            String email,
            String fullName,
            String password,
            String confirmPassword,
            String role,
            String orphanageName)
            throws ServiceException {

        // Validation
        validateRegistrationInput(username, email, password, confirmPassword, role, orphanageName);

        try {
            // Check for existing username
            if (userDAO.isFieldTaken("Username", username)) {
                throw new ServiceException("Username '" + username + "' is already taken.");
            }

            // Check for existing email
            if (userDAO.isFieldTaken("Email", email)) {
                throw new ServiceException("Email '" + email + "' is already registered.");
            }

            // Create and save user
            User user = new User();
            user.setUserId(UUID.randomUUID().toString());
            user.setUsername(username);
            user.setEmail(email);
            user.setFullName(fullName);
            user.setPasswordHash(hashPassword(password));
            user.setUserRole(role);
            user.setDateRegistered(LocalDateTime.now());
            user.setAccountStatus("ACTIVE");

            User savedUser = userDAO.save(user);
            logger.info("User registered successfully: {}", savedUser.getUsername());

            // If staff, associate with orphanage
            if ("OrphanageStaff".equals(role) && orphanageName != null) {
                logger.info("Staff user {} associated with orphanage: {}", username, orphanageName);
            }

            return savedUser;

        } catch (SQLException e) {
            logger.error("Database error during registration", e);
            throw new ServiceException("Registration failed due to a database error.");
        }
    }

    private void validateRegistrationInput(
            String username,
            String email,
            String password,
            String confirmPassword,
            String role,
            String orphanageName)
            throws ServiceException {

        // Username validation
        if (username == null || username.trim().isEmpty()) {
            throw new ServiceException("Username is required.");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new ServiceException("Username must be between 3 and 50 characters.");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new ServiceException("Username can only contain letters, numbers, and underscores.");
        }

        // Email validation
        if (email == null || email.trim().isEmpty()) {
            throw new ServiceException("Email is required.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ServiceException("Please enter a valid email address.");
        }

        // Password validation
        if (password == null || password.isEmpty()) {
            throw new ServiceException("Password is required.");
        }
        if (password.length() < 8) {
            throw new ServiceException("Password must be at least 8 characters long.");
        }
        if (!password.equals(confirmPassword)) {
            throw new ServiceException("Passwords do not match.");
        }

        // Role validation
        if (role == null || role.trim().isEmpty()) {
            throw new ServiceException("User role is required.");
        }
        if (!isValidRole(role)) {
            throw new ServiceException("Invalid user role: " + role);
        }

        // Orphanage validation for staff
        if ("OrphanageStaff".equals(role)) {
            if (orphanageName == null || orphanageName.trim().isEmpty() || 
                "Select Orphanage...".equals(orphanageName)) {
                throw new ServiceException("Please select an orphanage for staff registration.");
            }
        }
    }

    private boolean isValidRole(String role) {
        return "Donor".equals(role) || 
               "OrphanageStaff".equals(role) || 
               "Volunteer".equals(role) || 
               "User".equals(role);
    }

    private String hashPassword(String password) {
        // TODO: Implement proper password hashing (e.g., BCrypt)
        // For now, returning a simple hash
        return String.valueOf(password.hashCode());
    }

    public List<Orphanage> getUnassignedOrphanages() throws ServiceException {
        try {
            return orphanageDAO.findAll();
        } catch (SQLException e) {
            logger.error("Error fetching orphanages", e);
            throw new ServiceException("Could not load orphanages from database.");
        }
    }
}