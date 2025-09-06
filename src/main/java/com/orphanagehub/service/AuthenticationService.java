package com.orphanagehub.service;

import com.orphanagehub.dao.UserDAO;
import com.orphanagehub.model.User;
import com.orphanagehub.util.PasswordUtil;
import com.orphanagehub.util.SessionManager;
import com.orphanagehub.util.ValidationUtil;
import io.vavr.control.Try;

/**
 * Service for authentication operations.
 * Handles login and role retrieval with FP error handling.
 */
public class AuthenticationService {

    private final UserDAO userDAO = new UserDAO();

    /**
     * Authenticates a user with username and password.
     * @param username The username.
     * @param password The password (char[] for security).
     * @return Try<User> - the authenticated User on success, failure on error or invalid credentials.
     */
    public Try<User> authenticate(String username, char[] password) {
        return ValidationUtil.isValidUsername.apply(username)
                .flatMap(valid -> userDAO.findByUsername(username))
                .flatMap(optUser -> optUser.toTry(() -> new IllegalArgumentException("User not found")))
                .flatMap(user -> PasswordUtil.verify(password, user.passwordHash()) 
                        ? Try.success(user) 
                        : Try.failure(new IllegalArgumentException("Invalid password")));
    }

    /**
     * Gets the role for a username after authentication.
     * @param username The username.
     * @return Try<String> - the user's role on success, failure on error.
     */
    public Try<String> getUserRole(String username) {
        return userDAO.findByUsername(username)
                .map(optUser -> optUser.map(User::userRole))
                .flatMap(optRole -> optRole.toTry(() -> new IllegalArgumentException("Role not found")));
    }

    // Overload for role-based login (fixes earlier mismatch)
    public Try<User> authenticate(String username, char[] password, String expectedRole) {
        return authenticate(username, password)
                .flatMap(user -> user.userRole().equals(expectedRole) 
                        ? Try.success(user) 
                        : Try.failure(new IllegalArgumentException("Role mismatch")));
    }
}
