// Fixed AuthService.java (Changed getUserByUsername to findByUsername to match DAO)
package com.orphanagehub.service;

import com.orphanagehub.dao.UserDAO;
import com.orphanagehub.model.User;
import com.orphanagehub.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private UserDAO userDAO = new UserDAO();

    public boolean authenticate(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user == null) {
            logger.warn("Login failed: User not found - {}", username);
            return false;
        }
        boolean valid = PasswordUtil.verifyPassword(password, user.getPasswordHash());
        if (valid) {
            logger.info("Login success for user: {}", username);
        } else {
            logger.warn("Login failed: Invalid password for {}", username);
        }
        return valid;
    }

    public String getUserRole(String username) {
        User user = userDAO.findByUsername(username);
        return (user != null) ? user.getUserRole() : null;
    }
}