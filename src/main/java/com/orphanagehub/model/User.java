// Fixed User.java (Added no-arg constructor; confirmed setters)
package com.orphanagehub.model;

import java.util.Date;

public class User {
    private String userID;
    private String username;
    private String passwordHash;
    private String email;
    private String userRole;
    private Date dateRegistered;
    private String fullName; // Added if needed for registration
    private String otherField; // Placeholder for 8th arg if schema has more; adjust based on actual schema

    // No-arg constructor
    public User() {}

    // Full constructor (matching potential 8 args; adjust as per schema)
    public User(String userID, String username, String passwordHash, String email, String userRole, Date dateRegistered, String fullName, String otherField) {
        this.userID = userID;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.userRole = userRole;
        this.dateRegistered = dateRegistered;
        this.fullName = fullName;
        this.otherField = otherField;
    }

    // Getters and Setters (ensured all exist)
    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public Date getDateRegistered() { return dateRegistered; }
    public void setDateRegistered(Date dateRegistered) { this.dateRegistered = dateRegistered; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getOtherField() { return otherField; }
    public void setOtherField(String otherField) { this.otherField = otherField; }
}