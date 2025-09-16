package com.orphanagehub.util;

import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.collection.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Thread-safe session manager with timeout support and hybrid roles.
 * Implements singleton pattern for global session state management.
 * 
 * FIXED: Removed infinite recursion, maintained backward compatibility
 * 
 * @author OrphanageHub Team
 * @version 3.0
 */
public class SessionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    
    // Singleton instance
    private static volatile SessionManager instance;
    
    // Thread-safe storage for session attributes
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    
    // Session metadata - made volatile for thread safety
    private volatile LocalDateTime createdAt;
    private volatile LocalDateTime lastAccessedAt;
    private static final long SESSION_TIMEOUT_MINUTES = 30;
    
    // Session cleanup scheduler
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
        r -> {
            Thread t = new Thread(r, "SessionManager-Cleanup");
            t.setDaemon(true);
            return t;
        }
    );
    
    // Session constants
    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String USER_ROLE = "userRole";
    public static final String USER_ROLES = "userRoles";
    public static final String FULL_NAME = "fullName";
    public static final String EMAIL = "email";
    public static final String ORPHANAGE_ID = "orphanageId";
    public static final String IS_AUTHENTICATED = "isAuthenticated";
    public static final String LOGIN_TIME = "loginTime";
    public static final String LAST_ACTIVITY = "lastActivity";
    
    // Flag to prevent recursive updates
    private final ThreadLocal<Boolean> isUpdatingActivity = ThreadLocal.withInitial(() -> false);
    
    /**
     * Private constructor for singleton pattern.
     */
    private SessionManager() {
        this.createdAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
        
        // Start session timeout checker
        scheduler.scheduleAtFixedRate(this::checkTimeout, 1, 1, TimeUnit.MINUTES);
        
        logger.info("SessionManager initialized");
    }
    
    /**
     * Gets the singleton instance (thread-safe double-checked locking).
     * MAINTAINS ORIGINAL API - returns SessionManager directly, not Try<SessionManager>
     * 
     * @return The SessionManager instance
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Sets a session attribute.
     * FIXED: Prevents infinite recursion with ThreadLocal flag
     * 
     * @param key The attribute key
     * @param value The attribute value
     */
    public void setAttribute(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Session key cannot be null");
        }
        
        attributes.put(key, value);
        
        // Only update last accessed if not the LAST_ACTIVITY key itself and not already updating
        if (!LAST_ACTIVITY.equals(key) && !isUpdatingActivity.get()) {
            updateLastAccessed();
        }
        
        logger.debug("Session attribute set: {} = {}", key, value);
    }
    
    /**
     * Gets a session attribute wrapped in Option for null safety.
     * 
     * @param key The attribute key
     * @return Option containing the value or None if not found
     */
    public Option<Object> getAttribute(String key) {
        if (!isUpdatingActivity.get()) {
            updateLastAccessed();
        }
        return Option.of(attributes.get(key));
    }
    
    /**
     * Gets a typed session attribute.
     * 
     * @param key The attribute key
     * @param type The expected type class
     * @param <T> The type parameter
     * @return Option containing the typed value or None if not found/wrong type
     */
    @SuppressWarnings("unchecked")
    public <T> Option<T> getAttribute(String key, Class<T> type) {
        return getAttribute(key)
            .filter(type::isInstance)
            .map(value -> (T) value);
    }
    
    /**
     * Removes a session attribute.
     * 
     * @param key The attribute key
     * @return The removed value wrapped in Option
     */
    public Option<Object> removeAttribute(String key) {
        updateLastAccessed();
        return Option.of(attributes.remove(key));
    }
    
    /**
     * Checks if an attribute exists.
     * 
     * @param key The attribute key
     * @return True if attribute exists
     */
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }
    
    /**
     * Gets the current user ID.
     * 
     * @return Option containing user ID or None if not logged in
     */
    public Option<Integer> getCurrentUserId() {
        return getAttribute(USER_ID, Integer.class);
    }
    
    /**
     * Gets the current username.
     * 
     * @return Option containing username or None if not logged in
     */
    public Option<String> getCurrentUsername() {
        return getAttribute(USERNAME, String.class);
    }
    
    /**
     * Gets the primary user role.
     * 
     * @return Option containing role or None if not logged in
     */
    public Option<String> getUserRole() {
        return getAttribute(USER_ROLE, String.class);
    }
    
    /**
     * Gets all user roles (for hybrid role support).
     * 
     * @return List of roles (empty if not logged in)
     */
    @SuppressWarnings("unchecked")
    public List<String> getUserRoles() {
        return getAttribute(USER_ROLES)
            .map(roles -> {
                if (roles instanceof List) {
                    return (List<String>) roles;
                } else if (roles instanceof java.util.List) {
                    // Convert java.util.List to Vavr List
                    return List.ofAll((java.util.List<String>) roles);
                } else {
                    return List.<String>empty();
                }
            })
            .getOrElse(List.empty());
    }
    
    /**
     * Checks if user has a specific role.
     * 
     * @param role The role to check
     * @return True if user has the role
     */
    public boolean hasRole(String role) {
        // Check primary role
        if (getUserRole().map(r -> r.equals(role)).getOrElse(false)) {
            return true;
        }
        // Check additional roles
        return getUserRoles().contains(role);
    }
    
    /**
     * Checks if user is authenticated.
     * 
     * @return True if authenticated
     */
    public boolean isAuthenticated() {
        return getAttribute(IS_AUTHENTICATED, Boolean.class)
            .getOrElse(false);
    }
    
    /**
     * Sets user authentication data.
     * 
     * @param userId The user ID
     * @param username The username
     * @param role The primary role
     * @param roles All roles (for hybrid support)
     * @param fullName The user's full name
     * @param email The user's email
     */
    public void setUserData(Integer userId, String username, String role, 
                           List<String> roles, String fullName, String email) {
        setAttribute(USER_ID, userId);
        setAttribute(USERNAME, username);
        setAttribute(USER_ROLE, role);
        setAttribute(USER_ROLES, roles);
        setAttribute(FULL_NAME, fullName);
        setAttribute(EMAIL, email);
        setAttribute(IS_AUTHENTICATED, true);
        setAttribute(LOGIN_TIME, LocalDateTime.now());
        
        logger.info("User authenticated: {} (ID: {}, Roles: {})", username, userId, roles);
    }
    
    /**
     * Sets orphanage association for staff users.
     * 
     * @param orphanageId The orphanage ID
     */
    public void setOrphanageId(Integer orphanageId) {
        setAttribute(ORPHANAGE_ID, orphanageId);
    }
    
    /**
     * Gets the associated orphanage ID.
     * 
     * @return Option containing orphanage ID or None
     */
    public Option<Integer> getOrphanageId() {
        return getAttribute(ORPHANAGE_ID, Integer.class);
    }
    
    /**
     * Clears all session data (logout).
     */
    public void clear() {
        String username = getCurrentUsername().getOrElse("Unknown");
        attributes.clear();
        createdAt = LocalDateTime.now();
        lastAccessedAt = LocalDateTime.now();
        
        logger.info("Session cleared for user: {}", username);
    }
    
    /**
     * Invalidates the session completely.
     * Made synchronized to prevent concurrent issues
     */
    public synchronized void invalidate() {
        clear();
        
        // Shutdown scheduler safely
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                scheduler.shutdownNow();
            }
        }
        
        instance = null;
        logger.info("Session invalidated");
    }
    
    /**
     * Gets session creation time.
     * 
     * @return The creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Gets last accessed time.
     * 
     * @return The last access timestamp
     */
    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }
    
    /**
     * Checks if session has timed out.
     * 
     * @return True if timed out
     */
    public boolean isTimedOut() {
        return lastAccessedAt.plusMinutes(SESSION_TIMEOUT_MINUTES)
            .isBefore(LocalDateTime.now());
    }
    
    /**
     * Updates last accessed time.
     * FIXED: Uses ThreadLocal flag to prevent recursion
     */
    private void updateLastAccessed() {
        // Set flag to prevent recursion
        isUpdatingActivity.set(true);
        try {
            lastAccessedAt = LocalDateTime.now();
            // Direct put to attributes map, not setAttribute
            attributes.put(LAST_ACTIVITY, lastAccessedAt);
        } finally {
            // Always clear the flag
            isUpdatingActivity.set(false);
        }
    }
    
    /**
     * Checks for session timeout and clears if expired.
     */
    private void checkTimeout() {
        try {
            if (isAuthenticated() && isTimedOut()) {
                logger.warn("Session timed out for user: {}", getCurrentUsername().getOrElse("Unknown"));
                clear();
            }
        } catch (Exception ex) {
            logger.error("Error checking session timeout", ex);
        }
    }
    
    /**
     * Gets session info for debugging.
     * 
     * @return Session information string
     */
    public String getSessionInfo() {
        return String.format(
            "Session[created=%s, lastAccessed=%s, authenticated=%s, user=%s, attributes=%d]",
            createdAt, lastAccessedAt, isAuthenticated(),
            getCurrentUsername().getOrElse("None"),
            attributes.size()
        );
    }
    
    /**
     * Gets remaining session time in minutes.
     * 
     * @return Option containing remaining minutes or None if not authenticated
     */
    public Option<Long> getRemainingSessionTime() {
        if (!isAuthenticated()) {
            return Option.none();
        }
        
        LocalDateTime timeout = lastAccessedAt.plusMinutes(SESSION_TIMEOUT_MINUTES);
        long remaining = ChronoUnit.MINUTES.between(LocalDateTime.now(), timeout);
        return Option.of(Math.max(0, remaining));
    }
}