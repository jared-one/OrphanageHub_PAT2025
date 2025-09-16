package com.orphanagehub.model;

import io.vavr.control.Option;
import java.time.LocalDateTime;

/**
 * Immutable AuditLog model representing the TblAuditLog table.
 * Tracks all system actions for security and compliance.
 * 
 * @author OrphanageHub Team
 * @version 2.0
 * @since 2025-09-06
 */
public record AuditLog(
    Integer logId,
    Option<Integer> userId,
    Option<String> username,
    String action,
    String entityType,
    Option<String> entityId,
    Option<String> oldValue,
    Option<String> newValue,
    Option<String> ipAddress,
    Option<String> userAgent,
    Option<String> sessionId,
    LocalDateTime timestamp,
    boolean success,
    Option<String> errorMessage
) {
    
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_VERIFY = "VERIFY";
    public static final String ACTION_APPLY = "APPLY";
    public static final String ACTION_DONATE = "DONATE";
    
    /**
     * Creates an audit log entry.
     */
    public static AuditLog create(Integer userId, String username, String action,
                                 String entityType, String entityId, boolean success) {
        return new AuditLog(
            null, Option.of(userId), Option.of(username), action, entityType,
            Option.of(entityId), Option.none(), Option.none(), Option.none(),
            Option.none(), Option.none(), LocalDateTime.now(), success, Option.none()
        );
    }
    
    /**
     * Creates an error audit log.
     */
    public static AuditLog createError(Integer userId, String username, String action,
                                      String entityType, String error) {
        return new AuditLog(
            null, Option.of(userId), Option.of(username), action, entityType,
            Option.none(), Option.none(), Option.none(), Option.none(),
            Option.none(), Option.none(), LocalDateTime.now(), false, Option.of(error)
        );
    }

    public String details() {
        StringBuilder sb = new StringBuilder();
        newValue.peek(nv -> sb.append("New: ").append(nv));
        oldValue.peek(ov -> {
            if (sb.length() > 0) sb.append("; ");
            sb.append("Old: ").append(ov);
        });
        errorMessage.peek(em -> {
            if (sb.length() > 0) sb.append("; ");
            sb.append("Error: ").append(em);
        });
        return sb.toString();
    }
}