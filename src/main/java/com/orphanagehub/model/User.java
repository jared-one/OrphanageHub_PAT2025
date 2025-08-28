/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class User {
    private String userId;
    private String username;
    private String passwordHash;
    private String email;
    private String fullName; // This was missing in some versions
    private String userRole;
    private String accountStatus;
    private LocalDateTime dateRegistered;

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
    public LocalDateTime getDateRegistered() { return dateRegistered; }
    public void setDateRegistered(LocalDateTime dateRegistered) { this.dateRegistered = dateRegistered; }
    
    // Compatibility method for old Timestamp usage
    public void setDateRegistered(Timestamp timestamp) {
        if (timestamp != null) {
            this.dateRegistered = timestamp.toLocalDateTime();
        }
    }
}
