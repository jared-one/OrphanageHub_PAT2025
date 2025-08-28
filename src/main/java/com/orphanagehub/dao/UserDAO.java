/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.dao;

import com.orphanagehub.model.User;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UserDAO {

    private Connection getConnection() throws SQLException {
        String dbPath = System.getProperty("user.dir") + "/db/OrphanageHub.accdb";
        return DriverManager.getConnection("jdbc:ucanaccess://" + dbPath);
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM TblUsers WHERE Username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getString("UserID"));
                    user.setUsername(rs.getString("Username"));
                    user.setEmail(rs.getString("Email"));
                    user.setFullName(rs.getString("FullName"));
                    user.setPasswordHash(rs.getString("PasswordHash"));
                    user.setUserRole(rs.getString("UserRole"));
                    user.setDateRegistered(rs.getTimestamp("DateRegistered").toLocalDateTime());
                    user.setAccountStatus(rs.getString("AccountStatus"));
                    return user;
                }
                return null;
            }
        }
    }

    public boolean isFieldTaken(String field, String value) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TblUsers WHERE " + field + " = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public User save(User user) throws SQLException {
        String sql = "INSERT INTO TblUsers (UserID, Username, Email, FullName, PasswordHash, UserRole, DateRegistered, AccountStatus) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUserId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getPasswordHash());
            ps.setString(6, user.getUserRole());
            ps.setTimestamp(7, Timestamp.valueOf(user.getDateRegistered()));
            ps.setString(8, user.getAccountStatus());
            ps.executeUpdate();
            return user;
        }
    }
}