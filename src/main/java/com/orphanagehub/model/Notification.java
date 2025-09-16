package com.orphanagehub.model;

import io.vavr.control.Option;
import java.time.LocalDateTime;

/**
 * Immutable Notification model representing the TblNotifications table.
 * Manages user notifications and alerts.
 * 
 * @author OrphanageHub Team
 * @version 2.0
 * @since 2025-09-06
 */
public record Notification(
    Integer notificationId,
    Integer userId,
    String type,
    String title,
    String message,
    String priority,
    String status,
    LocalDateTime createdDate,
    Option<LocalDateTime> readDate,
    Option<LocalDateTime> expiryDate,
    Option<String> actionUrl,
    Option<String> relatedEntityType,
    Option<Integer> relatedEntityId
) {
    
    public static final String TYPE_SYSTEM = "System";
    public static final String TYPE_DONATION = "Donation";
    public static final String TYPE_REQUEST = "Request";
    public static final String TYPE_VOLUNTEER = "Volunteer";
    public static final String TYPE_VERIFICATION = "Verification";
    
    public static final String PRIORITY_HIGH = "High";
    public static final String PRIORITY_NORMAL = "Normal";
    public static final String PRIORITY_LOW = "Low";
    
    public static final String STATUS_UNREAD = "Unread";
    public static final String STATUS_READ = "Read";
    public static final String STATUS_ARCHIVED = "Archived";
    
    /**
     * Creates a basic notification.
     */
    public static Notification create(Integer userId, String type, String title,
                                     String message, String priority) {
        return new Notification(
            null, userId, type, title, message, priority, STATUS_UNREAD,
            LocalDateTime.now(), Option.none(), Option.none(), Option.none(),
            Option.none(), Option.none()
        );
    }
    
    /**
     * Checks if notification is unread.
     */
    public boolean isUnread() {
        return STATUS_UNREAD.equalsIgnoreCase(status);
    }
    
    /**
     * Checks if notification has expired.
     */
    public boolean isExpired() {
        return expiryDate.map(exp -> LocalDateTime.now().isAfter(exp)).getOrElse(false);
    }
    
    /**
     * Marks notification as read.
     */
    public Notification markAsRead() {
        return new Notification(
            notificationId, userId, type, title, message, priority, STATUS_READ,
            createdDate, Option.of(LocalDateTime.now()), expiryDate, actionUrl,
            relatedEntityType, relatedEntityId
        );
    }
}