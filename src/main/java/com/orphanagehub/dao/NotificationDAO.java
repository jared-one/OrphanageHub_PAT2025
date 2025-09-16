package com.orphanagehub.dao;

import com.orphanagehub.model.Notification;
import io.vavr.control.Try;
import io.vavr.control.Option;
import io.vavr.collection.List;
import java.sql.*;
import java.time.LocalDateTime;

public class NotificationDAO {
    
    public static class TYPE_SYSTEM {
        public static final String TYPE_SYSTEM = "SYSTEM";
    }
    
    public Try<Void> create(Notification notification) {
        return Try.run(() -> {
            String sql = "INSERT INTO TblNotifications (UserID, Type, Title, Message, " +
                        "Priority, Status, CreatedDate) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, notification.userId());
                ps.setString(2, notification.type());
                ps.setString(3, notification.title());
                ps.setString(4, notification.message());
                ps.setString(5, notification.priority());
                ps.setString(6, notification.status());
                ps.setTimestamp(7, Timestamp.valueOf(notification.createdDate()));
                ps.executeUpdate();
            }
        });
    }
    
    public Try<List<Integer>> createBulk(List<Integer> userIds, String type, String title, 
                                         String message, String priority) {
        return Try.of(() -> {
            List<Integer> createdIds = List.empty();
            String sql = "INSERT INTO TblNotifications (UserID, Type, Title, Message, " +
                        "Priority, Status, CreatedDate) VALUES (?, ?, ?, ?, ?, 'Unread', ?)";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                for (Integer userId : userIds) {
                    ps.setInt(1, userId);
                    ps.setString(2, type);
                    ps.setString(3, title);
                    ps.setString(4, message);
                    ps.setString(5, priority);
                    ps.setTimestamp(6, now);
                    ps.addBatch();
                }
                ps.executeBatch();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    while (keys.next()) {
                        createdIds = createdIds.append(keys.getInt(1));
                    }
                }
                return createdIds;
            }
        });
    }
    
    public Try<Option<Notification>> findById(Integer id) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblNotifications WHERE NotificationID = ?";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Option.of(mapToNotification(rs)) : Option.none();
                }
            }
        });
    }
    
    public Try<List<Notification>> findByUserId(Integer userId) {
        return Try.of(() -> {
            String sql = "SELECT * FROM TblNotifications WHERE UserID = ? ORDER BY CreatedDate DESC";
            try (Connection conn = DatabaseManager.getConnection().get();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Notification> notifications = List.empty();
                    while (rs.next()) {
                        notifications = notifications.append(mapToNotification(rs));
                    }
                    return notifications;
                }
            }
        });
    }
    
    private Notification mapToNotification(ResultSet rs) throws SQLException {
        return new Notification(
            rs.getInt("NotificationID"),
            rs.getInt("UserID"),
            rs.getString("Type"),
            rs.getString("Title"),
            rs.getString("Message"),
            rs.getString("Priority"),
            rs.getString("Status"),
            rs.getTimestamp("CreatedDate").toLocalDateTime(),
            rs.getTimestamp("ReadDate") != null ? 
                Option.of(rs.getTimestamp("ReadDate").toLocalDateTime()) : Option.none(),
            rs.getTimestamp("ExpiryDate") != null ? 
                Option.of(rs.getTimestamp("ExpiryDate").toLocalDateTime()) : Option.none(),
            Option.of(rs.getString("ActionURL")),
            Option.of(rs.getString("RelatedEntityType")),
            Option.of(rs.getObject("RelatedEntityID", Integer.class))
        );
    }
}