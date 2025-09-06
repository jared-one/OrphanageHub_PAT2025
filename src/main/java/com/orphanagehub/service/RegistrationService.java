package com.orphanagehub.service;

import com.orphanagehub.dao.UserDAO;
import com.orphanagehub.model.User;
import com.orphanagehub.util.PasswordUtil;
import com.orphanagehub.util.ValidationUtil;
import io.vavr.control.Try;

/**
 * Service for registration operations.
 * Handles user creation with checks.
 */
public class RegistrationService {

    private final UserDAO userDAO = new UserDAO();

    /**
     * Registers a new user.
     * @param username Username.
     * @param email Email.
     * @param fullName Full name (unused here; extend User if needed).
     * @param password Password (char[]).
     * @param confirmPassword Confirmation (char[]).
     * @param role Role.
     * @return Try<User> - created User on success, failure on error (e.g., mismatch, taken).
     */
    public Try<User> register(String username, String email, String fullName, char[] password, char[] confirmPassword, String role) {
        return Try.sequence(io.vavr.collection.List.of(
                ValidationUtil.isValidUsername.apply(username),
                ValidationUtil.isValidEmail.apply(email),
                Try.of(() -> java.util.Arrays.equals(password, confirmPassword) ? true : Try.failure(new IllegalArgumentException("Passwords mismatch")).get())
        )).flatMap(seq -> isUsernameAvailable(username))
          .flatMap(avail -> avail ? isEmailAvailable(email) : Try.failure(new IllegalArgumentException("Username taken")))
          .flatMap(avail -> avail ? Try.success(true) : Try.failure(new IllegalArgumentException("Email taken")))
          .map(valid -> new User(null, username, PasswordUtil.hash(password), email, role, new java.sql.Timestamp(System.currentTimeMillis()), fullName, "Active"))
          .flatMap(user -> userDAO.create(user).map(v -> user))
          .flatMap(user -> userDAO.findByUsername(user.username()).map(opt -> opt.getOrElse((User)null))); // Return created
    }

    /**
     * Checks if username is available.
     * @param username The username.
     * @return Try<Boolean> - true if available.
     */
    public Try<Boolean> isUsernameAvailable(String username) {
        return userDAO.findByUsername(username).map(opt -> opt.isEmpty());
    }

    /**
     * Checks if email is available.
     * @param email The email.
     * @return Try<Boolean> - true if available.
     */
    public Try<Boolean> isEmailAvailable(String email) {
        // Assume add findByEmail to UserDAO
        return Try.success(true); // Stub; implement similarly
    }
}
