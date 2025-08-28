package com.orphanagehub.model;

import java.util.Date;

public class User {
    private String userId;
    private String username;
    private String passwordHash;
    private String email;
    private String fullName;
    private String userRole;
    private Date dateRegistered;
    private String accountStatus;

    public User(String userId, String username, String passwordHash, String email, String fullName,
                String userRole, Date dateRegistered, String accountStatus) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.fullName = fullName;
        this.userRole = userRole;
        this.dateRegistered = dateRegistered;
        this.accountStatus = accountStatus;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getUserRole() { return userRole; }
    public Date getDateRegistered() { return dateRegistered; }
    public String getAccountStatus() { return accountStatus; }
}