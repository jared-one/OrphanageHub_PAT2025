/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.service;

import com.orphanagehub.dao.UserDAO;
import com.orphanagehub.model.User;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserDAO userDAO = new UserDAO();

    public User authenticate(String username, String password) throws ServiceException {
        try {
            User user = userDAO.findByUsername(username);
            if (user == null) {
                return null;
            }
            // TODO: Proper password verification
            if (user.getPasswordHash().equals(String.valueOf(password.hashCode()))) {
                return user;
            }
            return null;
        } catch (SQLException e) {
            logger.error("Database error during authentication", e);
            throw new ServiceException("Authentication failed due to database error.");
        }
    }
}
