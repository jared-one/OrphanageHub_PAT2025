package com.orphanagehub.model;

import io.vavr.control.Option;

/**
 * Represents a User.
 * Immutable record for safety.
 */
public record User(String userId, String username, String passwordHash, String email, String userRole, java.sql.Timestamp dateRegistered, String fullName, String accountStatus) {

    /**
     * Gets the user's full details as a string.
     * @return Formatted details.
     */
    public String getDetails() {
        return "User: " + username + " (" + userRole + "), Email: " + email;
    }

    public User withAccountStatus(String newStatus) {
        return new User(userId, username, passwordHash, email, userRole, dateRegistered, fullName, newStatus);
    }
}
