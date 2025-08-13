package com.orphanagehub.model;

import java.sql.Timestamp;

public class User() {
  private String userId, username, passwordHash, email, userRole, accountStatus;
  private Timestamp dateRegistered;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getUserRole() {
    return userRole;
  }

  public void setUserRole(String userRole) {
    this.userRole = userRole;
  }

  public Timestamp getDateRegistered() {
    return dateRegistered;
  }

  public void setDateRegistered(Timestamp dateRegistered) {
    this.dateRegistered = dateRegistered;
  }

  public String getAccountStatus() {
    return accountStatus == null ? "Active" : accountStatus;
  }

  public void setAccountStatus(String accountStatus) {
    this.accountStatus = accountStatus;
  }
}