/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.service;

import com.orphanagehub.dao.UserDAO;
import com.orphanagehub.model.User;
import com.orphanagehub.util.PasswordUtil;
import com.orphanagehub.util.ValidationUtil;
import java.sql.SQLException;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    public User authenticate(String username, String password) throws ServiceException {
        if (!ValidationUtil.isNonEmpty(username) || !ValidationUtil.isNonEmpty(password)) {
            throw new ServiceException("Username and password are required.");
        }
        try {
            User user = userDAO.findByUsername(username);
            if (user == null) throw new ServiceException("Invalid username or password.");
            String hash = PasswordUtil.sha256(password);
            if (!hash.equals(user.getPasswordHash()))
                throw new ServiceException("Invalid username or password.");
            if (!"Active".equalsIgnoreCase(user.getAccountStatus())) {
                throw new ServiceException("This account has been suspended.");
            }
            return user;
        } catch (SQLException e) {
            throw new ServiceException("A database error occurred during login.", e);
        }
    }
}
