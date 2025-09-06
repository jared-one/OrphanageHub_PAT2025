package com.orphanagehub.dao;

import com.orphanagehub.model.User;
import com.orphanagehub.dao.DatabaseManager;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.sql.*;
import java.util.UUID;

public class UserDAO {
    
    public Try<Void> create(User user) {
        return Try.run(() -> {
            String sql = "INSERT INTO TblUsers (UserID, Username, PasswordHash, Email, UserRole, DateRegistered, FullName, AccountStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, UUID.randomUUID().toString().substring(0, 10));
                ps.setString(2, user.username());
                ps.setString(3, user.passwordHash());
                ps.setString(4, user.email());
                ps.setString(5, user.userRole());
                ps.setTimestamp(6, user.dateRegistered());
                ps.setString(7, user.fullName());
                ps.setString(8, user.accountStatus());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    public Try<Option<User>> findByUsername(String username) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblUsers WHERE Username = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                return rs.next() ? Option.of(mapToUser(rs)) : Option.<User>none();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    public Try<Void> update(User user) {
        return Try.run(() -> {
            String sql = "UPDATE TblUsers SET Username=?, PasswordHash=?, Email=?, UserRole=?, FullName=?, AccountStatus=? WHERE UserID=?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.username());
                ps.setString(2, user.passwordHash());
                ps.setString(3, user.email());
                ps.setString(4, user.userRole());
                ps.setString(5, user.fullName());
                ps.setString(6, user.accountStatus());
                ps.setString(7, user.userId());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    public Try<Void> delete(String userId) {
        return Try.run(() -> {
            String sql = "DELETE FROM TblUsers WHERE UserID=?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    private static User mapToUser(ResultSet rs) throws SQLException {
        // Read FullName and AccountStatus with defaults if columns don't exist
        String fullName = null;
        String accountStatus = "Active";
        try {
            fullName = rs.getString("FullName");
        } catch (SQLException ignored) { }
        try {
            accountStatus = rs.getString("AccountStatus");
        } catch (SQLException ignored) { }
        
        return new User(
            rs.getString("UserID"),
            rs.getString("Username"),
            rs.getString("PasswordHash"),
            rs.getString("Email"),
            rs.getString("UserRole"),
            rs.getTimestamp("DateRegistered"),
            fullName,
            accountStatus
        );
    }
}