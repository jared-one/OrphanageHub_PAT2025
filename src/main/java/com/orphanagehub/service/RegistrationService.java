// Fixed RegistrationService.java (Adjusted to use User object in save; added fullName handling if needed)
package com.orphanagehub.service;

import com.orphanagehub.dao.UserDAO;
import com.orphanagehub.model.User;
import com.orphanagehub.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrationService {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);
    private UserDAO userDAO = new UserDAO();

    public void registerUser(String username, String email, String fullName, String password, String confirmPassword, String userRole) {
        // Validation
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (userDAO.isUsernameTaken(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userDAO.isEmailTaken(email)) {
            throw new IllegalArgumentException("Email is already taken");
        }

        String passwordHash = PasswordUtil.hashPassword(password);

        // Create User and save
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(passwordHash);
        user.setUserRole(userRole);
        userDAO.saveUser(user); // Use object-based save
    }
}