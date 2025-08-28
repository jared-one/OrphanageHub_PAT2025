package com.orphanagehub.dao;

import com.orphanagehub.model.User;
import com.orphanagehub.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    public void save(String username, String email, String fullName, String passwordHash, String userRole) throws SQLException {
        String userId = "U" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String sql = "INSERT INTO TblUsers (UserID, Username, Email, FullName, PasswordHash, UserRole, DateRegistered, AccountStatus) " +
                     "VALUES (?, ?, ?, ?, ?, ?, NOW(), 'Active')";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, username);
            pstmt.setString(3, email);
            pstmt.setString(4, fullName);
            pstmt.setString(5, passwordHash);
            pstmt.setString(6, userRole);
            pstmt.executeUpdate();
            logger.info("User saved: {}", username);
        }
    }

    public boolean isUsernameTaken(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TblUsers WHERE Username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean isEmailTaken(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TblUsers WHERE Email = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM TblUsers WHERE Username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getString("UserID"),
                        rs.getString("Username"),
                        rs.getString("PasswordHash"),
                        rs.getString("Email"),
                        rs.getString("FullName"),
                        rs.getString("UserRole"),
                        rs.getTimestamp("DateRegistered"),
                        rs.getString("AccountStatus")
                    );
                }
                return null;
            }
        }
    }
}