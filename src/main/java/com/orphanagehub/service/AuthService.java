package com.orphanagehub.service;

import com.orphanagehub.dao.UserDAO;
import com.orphanagehub.model.User;
import io.vavr.control.Try;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private UserDAO userDAO = new UserDAO();
    
    public Try<User> authenticate(String username, String password) {
        return userDAO.findByUsername(username)
            .flatMap(opt -> opt.toTry(() -> new IllegalArgumentException("User not found")))
            .filter(user -> BCrypt.checkpw(password, user.passwordHash()))
            .onFailure(ex -> logger.warn("Authentication failed for user: {}", username));
    }
    
    public Try<String> getUserRole(String username) {
        return userDAO.findByUsername(username)
            .flatMap(opt -> opt.toTry(() -> new IllegalArgumentException("User not found")))
            .map(User::userRole);
    }
}
