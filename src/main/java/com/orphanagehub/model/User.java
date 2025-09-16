package com.orphanagehub.model;

import io.vavr.control.Option;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Immutable User model representing the TblUsers table.
 * Uses Option for nullable fields and records for immutability.
 * 
 * @author OrphanageHub Team
 * @version 2.0
 * @since 2025-09-06
 */
public record User(
    Integer userId,
    String username,
    String passwordHash,
    String email,
    String userRole,
    LocalDateTime dateRegistered,
    Option<LocalDateTime> lastLogin,
    Option<String> fullName,
    Option<String> phoneNumber,
    Option<String> idNumber,
    Option<LocalDate> dateOfBirth,
    Option<String> address,
    Option<String> city,
    Option<String> province,
    Option<String> postalCode,
    String accountStatus,
    boolean emailVerified,
    Option<String> verificationToken,
    Option<String> passwordResetToken,
    Option<LocalDateTime> passwordResetExpiry,
    Option<String> profilePicture,
    Option<String> bio,
    Option<String> createdBy,
    Option<LocalDateTime> modifiedDate,
    Option<String> modifiedBy
) {
    
    /**
     * Creates a User with minimal required fields.
     */
    public static User createBasic(Integer userId, String username, String passwordHash, 
                                  String email, String userRole) {
        return new User(
            userId, username, passwordHash, email, userRole,
            LocalDateTime.now(), Option.none(), Option.none(), Option.none(),
            Option.none(), Option.none(), Option.none(), Option.none(),
            Option.none(), Option.none(), "Active", false, Option.none(),
            Option.none(), Option.none(), Option.none(), Option.none(),
            Option.none(), Option.none(), Option.none()
        );
    }
    
    /**
     * Gets formatted user details.
     * @return Formatted string with user info
     */
    public String getDetails() {
        return String.format("User: %s (%s), Email: %s, Role: %s, Status: %s",
            username,
            fullName.getOrElse(username),
            email,
            userRole,
            accountStatus
        );
    }
    
    /**
     * Gets display name (full name if available, otherwise username).
     */
    public String getDisplayName() {
        return fullName.getOrElse(username);
    }
    
    /**
     * Checks if user is active.
     */
    public boolean isActive() {
        return "Active".equalsIgnoreCase(accountStatus);
    }
    
    /**
     * Checks if user has a specific role.
     */
    public boolean hasRole(String role) {
        return userRole != null && userRole.equalsIgnoreCase(role);
    }
    
    /**
     * Checks if user is staff (OrphanageRep or OrphanageStaff).
     */
    public boolean isStaff() {
        return hasRole("OrphanageRep") || hasRole("OrphanageStaff") || hasRole("Staff");
    }
    
    /**
     * Checks if password reset token is valid.
     */
    public boolean isPasswordResetTokenValid() {
        return passwordResetToken.isDefined() && 
               passwordResetExpiry.map(exp -> exp.isAfter(LocalDateTime.now())).getOrElse(false);
    }
    
    // Immutable update methods
    public User withAccountStatus(String newStatus) {
        return new User(userId, username, passwordHash, email, userRole, dateRegistered,
            lastLogin, fullName, phoneNumber, idNumber, dateOfBirth, address, city,
            province, postalCode, newStatus, emailVerified, verificationToken,
            passwordResetToken, passwordResetExpiry, profilePicture, bio, createdBy,
            modifiedDate, modifiedBy);
    }
    
    public User withLastLogin(LocalDateTime login) {
        return new User(userId, username, passwordHash, email, userRole, dateRegistered,
            Option.of(login), fullName, phoneNumber, idNumber, dateOfBirth, address, city,
            province, postalCode, accountStatus, emailVerified, verificationToken,
            passwordResetToken, passwordResetExpiry, profilePicture, bio, createdBy,
            modifiedDate, modifiedBy);
    }
    
    public User withEmailVerified(boolean verified) {
        return new User(userId, username, passwordHash, email, userRole, dateRegistered,
            lastLogin, fullName, phoneNumber, idNumber, dateOfBirth, address, city,
            province, postalCode, accountStatus, verified, Option.none(),
            passwordResetToken, passwordResetExpiry, profilePicture, bio, createdBy,
            modifiedDate, modifiedBy);
    }
    
    public User withPasswordResetToken(String token, LocalDateTime expiry) {
        return new User(userId, username, passwordHash, email, userRole, dateRegistered,
            lastLogin, fullName, phoneNumber, idNumber, dateOfBirth, address, city,
            province, postalCode, accountStatus, emailVerified, verificationToken,
            Option.of(token), Option.of(expiry), profilePicture, bio, createdBy,
            modifiedDate, modifiedBy);
    }
    
    public User clearPasswordResetToken() {
        return new User(userId, username, passwordHash, email, userRole, dateRegistered,
            lastLogin, fullName, phoneNumber, idNumber, dateOfBirth, address, city,
            province, postalCode, accountStatus, emailVerified, verificationToken,
            Option.none(), Option.none(), profilePicture, bio, createdBy,
            modifiedDate, modifiedBy);
    }
}