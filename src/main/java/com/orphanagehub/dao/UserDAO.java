package com.orphanagehub.dao;

import com.orphanagehub.model.User;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.sql.*;
import java.time.LocalDateTime;

public class UserDAO {
    
    public Try<User> create(User user) {
        return Try.of(() -> {
            String sql = "INSERT INTO TblUsers (Username, PasswordHash, Email, UserRole, " +
                        "DateRegistered, FullName, AccountStatus, EmailVerified) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                ps.setString(1, user.username());
                ps.setString(2, user.passwordHash());
                ps.setString(3, user.email());
                ps.setString(4, user.userRole());
                ps.setTimestamp(5, Timestamp.valueOf(user.dateRegistered()));
                ps.setString(6, user.fullName().getOrNull());
                ps.setString(7, user.accountStatus());
                ps.setBoolean(8, user.emailVerified());
                
                ps.executeUpdate();
                
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return findById(keys.getInt(1)).get().get();
                    }
                }
                return user;
            }
        });
    }
    
    public Try<Option<User>> findById(Integer userId) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblUsers WHERE UserID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Option.of(mapToUser(rs)) : Option.<User>none();
                }
            }
        });
    }
    
    public Try<Option<User>> findByUsername(String username) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblUsers WHERE Username = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Option.of(mapToUser(rs)) : Option.<User>none();
                }
            }
        });
    }
    
    public Try<List<User>> findAll() {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblUsers ORDER BY Username";
            try (Connection conn = DatabaseManager.getConnection().get();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                List<User> users = List.empty();
                while (rs.next()) {
                    users = users.append(mapToUser(rs));
                }
                return users;
            }
        });
    }
    
    public Try<List<User>> findAllActive() {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblUsers WHERE AccountStatus = 'Active' ORDER BY Username";
            try (Connection conn = DatabaseManager.getConnection().get();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                List<User> users = List.empty();
                while (rs.next()) {
                    users = users.append(mapToUser(rs));
                }
                return users;
            }
        });
    }
    
    public Try<List<User>> findByRole(String role) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblUsers WHERE UserRole = ? ORDER BY Username";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, role);
                try (ResultSet rs = ps.executeQuery()) {
                    List<User> users = List.empty();
                    while (rs.next()) {
                        users = users.append(mapToUser(rs));
                    }
                    return users;
                }
            }
        });
    }
    
    public Try<Void> update(User user) {
        return Try.run(() -> {
            String sql = "UPDATE TblUsers SET Username = ?, Email = ?, UserRole = ?, " +
                        "FullName = ?, AccountStatus = ?, ModifiedDate = ? WHERE UserID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.username());
                ps.setString(2, user.email());
                ps.setString(3, user.userRole());
                ps.setString(4, user.fullName().getOrNull());
                ps.setString(5, user.accountStatus());
                ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(7, user.userId());
                ps.executeUpdate();
            }
        });
    }
    
    public Try<Void> updatePassword(Integer userId, String passwordHash) {
        return Try.run(() -> {
            String sql = "UPDATE TblUsers SET PasswordHash = ?, ModifiedDate = ? WHERE UserID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, passwordHash);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(3, userId);
                ps.executeUpdate();
            }
        });
    }
    
    public Try<Void> updateLastLogin(Integer userId) {
        return Try.run(() -> {
            String sql = "UPDATE TblUsers SET LastLogin = ? WHERE UserID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(2, userId);
                ps.executeUpdate();
            }
        });
    }
    
    public Try<Void> delete(Integer userId) {
        return Try.run(() -> {
            String sql = "DELETE FROM TblUsers WHERE UserID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
        });
    }
    
    private static User mapToUser(ResultSet rs) throws SQLException {
    return new User(
        rs.getObject("UserID", Integer.class),
        rs.getString("Username"),
        rs.getString("PasswordHash"),
        rs.getString("Email"),
        rs.getString("UserRole"),
        getLocalDateTime(rs, "DateRegistered"),
        Option.of(rs.getTimestamp("LastLogin"))
            .map(Timestamp::toLocalDateTime),
        Option.of(rs.getString("FullName")),
        Option.of(rs.getString("PhoneNumber")),
        Option.of(rs.getString("IDNumber")),
        Option.of(rs.getDate("DateOfBirth"))
            .map(Date::toLocalDate),
        Option.of(rs.getString("Address")),
        Option.of(rs.getString("City")),
        Option.of(rs.getString("Province")),
        Option.of(rs.getString("PostalCode")),
        Option.of(rs.getString("AccountStatus")).getOrElse("Active"),
        getBoolean(rs, "EmailVerified"),
        Option.of(rs.getString("VerificationToken")),
        Option.of(rs.getString("PasswordResetToken")),
        Option.of(rs.getTimestamp("PasswordResetExpiry"))
            .map(Timestamp::toLocalDateTime),
        Option.of(rs.getString("ProfilePicture")),
        Option.of(rs.getString("Bio")),
        Option.of(rs.getString("CreatedBy")),
        Option.of(rs.getTimestamp("ModifiedDate"))
            .map(Timestamp::toLocalDateTime),
        Option.of(rs.getString("ModifiedBy"))
    );
}


    
    private static LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
    }
    
    private static boolean getBoolean(ResultSet rs, String column) {
        try {
            return rs.getBoolean(column);
        } catch (SQLException e) {
            return false;
        }
    }
}