package com.orphanagehub.dao;

import com.orphanagehub.model.AuditLog;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.sql.*;
import java.time.LocalDateTime;

public class AuditLogDAO {
    
    public static class ACTION {
        public static final String CREATE = "CREATE";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
        public static final String DONATE = "DONATE";
    }
    
    public Try<Void> logSuccess(Integer userId, String username, String action, 
                               String entityType, String entityId) {
        return Try.run(() -> {
            String sql = "INSERT INTO TblAuditLog (UserID, Username, Action, EntityType, " +
                        "EntityID, Timestamp, Success) VALUES (?, ?, ?, ?, ?, ?, 1)";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, userId);
                ps.setString(2, username);
                ps.setString(3, action);
                ps.setString(4, entityType);
                ps.setString(5, entityId);
                ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }
        });
    }
    
    public Try<Void> logFailure(Integer userId, String username, String action, 
                               String entityType, String message) {
        return Try.run(() -> {
            String sql = "INSERT INTO TblAuditLog (UserID, Username, Action, EntityType, " +
                        "ErrorMessage, Timestamp, Success) VALUES (?, ?, ?, ?, ?, ?, 0)";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, userId);
                ps.setString(2, username);
                ps.setString(3, action);
                ps.setString(4, entityType);
                ps.setString(5, message);
                ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }
        });
    }
    
    public Try<Void> log(String action, Integer userId, String details) {
        return logSuccess(userId, null, action, "System", details);
    }
    
    public Try<List<AuditLog>> findByUserId(Integer userId, LocalDateTime from, LocalDateTime to) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblAuditLog WHERE UserID = ? AND Timestamp BETWEEN ? AND ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setTimestamp(2, Timestamp.valueOf(from));
                ps.setTimestamp(3, Timestamp.valueOf(to));
                try (ResultSet rs = ps.executeQuery()) {
                    List<AuditLog> logs = List.empty();
                    while (rs.next()) {
                        logs = logs.append(mapToAuditLog(rs));
                    }
                    return logs;
                }
            }
        });
    }
    
    public Try<List<AuditLog>> findByAction(String action, LocalDateTime from, LocalDateTime to) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblAuditLog WHERE Action = ? AND Timestamp BETWEEN ? AND ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, action);
                ps.setTimestamp(2, Timestamp.valueOf(from));
                ps.setTimestamp(3, Timestamp.valueOf(to));
                try (ResultSet rs = ps.executeQuery()) {
                    List<AuditLog> logs = List.empty();
                    while (rs.next()) {
                        logs = logs.append(mapToAuditLog(rs));
                    }
                    return logs;
                }
            }
        });
    }
    
    public Try<List<AuditLog>> findByEntity(String entityType, String entityId) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblAuditLog WHERE EntityType = ? AND EntityID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, entityType);
                ps.setString(2, entityId);
                try (ResultSet rs = ps.executeQuery()) {
                    List<AuditLog> logs = List.empty();
                    while (rs.next()) {
                        logs = logs.append(mapToAuditLog(rs));
                    }
                    return logs;
                }
            }
        });
    }
    
    public Try<List<AuditLog>> findAll(LocalDateTime from, LocalDateTime to) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblAuditLog WHERE Timestamp BETWEEN ? AND ? ORDER BY Timestamp DESC";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(from));
                ps.setTimestamp(2, Timestamp.valueOf(to));
                try (ResultSet rs = ps.executeQuery()) {
                    List<AuditLog> logs = List.empty();
                    while (rs.next()) {
                        logs = logs.append(mapToAuditLog(rs));
                    }
                    return logs;
                }
            }
        });
    }
    
    public Try<List<AuditLog>> findByUser(Integer userId) {
        return findByUserId(userId, LocalDateTime.now().minusMonths(1), LocalDateTime.now());
    }
    
    public Try<List<AuditLog>> getRecentLogs(int limit) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblAuditLog ORDER BY Timestamp DESC LIMIT ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    List<AuditLog> logs = List.empty();
                    while (rs.next()) {
                        logs = logs.append(mapToAuditLog(rs));
                    }
                    return logs;
                }
            }
        });
    }
    
 private AuditLog mapToAuditLog(ResultSet rs) throws SQLException {
    Integer userId = rs.getObject("UserID", Integer.class);
    return new AuditLog(
        rs.getInt("LogID"),
        Option.of(userId),
        Option.of(rs.getString("Username")),
        rs.getString("Action"),
        rs.getString("EntityType"),
        Option.of(rs.getString("EntityID")),
        Option.of(rs.getString("OldValue")),
        Option.of(rs.getString("NewValue")),
        Option.of(rs.getString("IPAddress")),
        Option.of(rs.getString("UserAgent")),
        Option.of(rs.getString("SessionID")),
        rs.getTimestamp("Timestamp").toLocalDateTime(),
        rs.getBoolean("Success"),
        Option.of(rs.getString("ErrorMessage"))
    );
}
}