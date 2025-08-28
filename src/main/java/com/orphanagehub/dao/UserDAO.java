// Fixed UserDAO.java (Added missing methods: isUsernameTaken, isEmailTaken, save with string args; renamed getUserByUsername to findByUsername)
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

    // Existing saveUser (object-based)
    public void saveUser(User user) {
        String sql = "INSERT INTO TblUsers (UserID, Username, PasswordHash, Email, UserRole, DateRegistered) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserID() != null ? user.getUserID() : UUID.randomUUID().toString().substring(0, 10).toUpperCase());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getUserRole());
            pstmt.setTimestamp(6, new java.sql.Timestamp(user.getDateRegistered() != null ? user.getDateRegistered().getTime() : System.currentTimeMillis()));
            pstmt.executeUpdate();
            logger.info("User saved: {}", user.getUsername());
        } catch (SQLException e) {
            logger.error("Error saving user: {}", e.getMessage(), e);
        }
    }

    // New save method with string args (to match RegistrationService call)
    public void save(String username, String email, String fullName, String passwordHash, String userRole) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName); // Assuming fullName is stored; adjust if not in schema
        user.setPasswordHash(passwordHash);
        user.setUserRole(userRole);
        user.setDateRegistered(new Date());
        saveUser(user);
    }

    // Renamed to match AuthenticationService call
    public User findByUsername(String username) {
        String sql = "SELECT * FROM TblUsers WHERE Username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                    rs.getString("UserID"),
                    rs.getString("Username"),
                    rs.getString("PasswordHash"),
                    rs.getString("Email"),
                    rs.getString("UserRole"),
                    rs.getDate("DateRegistered"),
                    rs.getString("FullName"), // Adjust if fullName column exists
                    null // Other field if needed
                );
                return user;
            }
        } catch (SQLException e) {
            logger.error("Error fetching user: {}", e.getMessage(), e);
        }
        return null;
    }

    // New method for isUsernameTaken
    public boolean isUsernameTaken(String username) {
        String sql = "SELECT COUNT(*) FROM TblUsers WHERE Username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            logger.error("Error checking username: {}", e.getMessage(), e);
            return false;
        }
    }

    // New method for isEmailTaken
    public boolean isEmailTaken(String email) {
        String sql = "SELECT COUNT(*) FROM TblUsers WHERE Email = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            logger.error("Error checking email: {}", e.getMessage(), e);
            return false;
        }
    }
}