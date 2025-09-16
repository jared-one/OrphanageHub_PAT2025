package com.orphanagehub.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Enhanced global exception handler with detailed logging and user-friendly messages.
 * Handles both EDT and non-EDT exceptions gracefully.
 * 
 * @author OrphanageHub Team
 * @version 2.0
 * @since 2025-09-06
 */
public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Error categories for better handling
    private enum ErrorCategory {
        DATABASE("Database Error", "A database operation failed. Please try again or contact support."),
        AUTHENTICATION("Authentication Error", "There was a problem with your login. Please try again."),
        VALIDATION("Validation Error", "Please check your input and try again."),
        NETWORK("Network Error", "Cannot connect to the server. Please check your connection."),
        PERMISSION("Permission Error", "You don't have permission to perform this action."),
        GENERAL("Application Error", "An unexpected error occurred. Please restart the application if the problem persists.");
        
        final String title;
        final String message;
        
        ErrorCategory(String title, String message) {
            this.title = title;
            this.message = message;
        }
    }
    
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        // Log the full exception details
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        logger.error("[{}] Uncaught exception in thread: {}", timestamp, t.getName(), e);
        
        // Determine error category
        ErrorCategory category = categorizeError(e);
        
        // Get stack trace for detailed logging
        String stackTrace = getStackTraceString(e);
        logger.error("Full stack trace:\n{}", stackTrace);
        
        // Log to audit if it's a critical error
        if (isCriticalError(e)) {
            logToAudit(t, e, timestamp);
        }
        
        // Show user-friendly error dialog on EDT
        SwingUtilities.invokeLater(() -> showErrorDialog(category, e));
        
        // For critical errors, offer to restart
        if (isCriticalError(e)) {
            offerRestart();
        }
    }
    
    /**
     * Installs the global exception handler for all threads.
     */
    public static void install() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        
        // Set for all threads
        Thread.setDefaultUncaughtExceptionHandler(handler);
        
        // Also set for AWT/Swing exceptions
        System.setProperty("sun.awt.exception.handler", GlobalExceptionHandler.class.getName());
        
        // Install EDT-specific handler
        SwingUtilities.invokeLater(() -> {
            Thread.currentThread().setUncaughtExceptionHandler(handler);
        });
        
        logger.info("Global exception handler installed successfully");
    }
    
    /**
     * Categorizes the exception for appropriate user messaging.
     */
    private ErrorCategory categorizeError(Throwable e) {
        String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        String className = e.getClass().getSimpleName().toLowerCase();
        
        if (className.contains("sql") || className.contains("database") || 
            message.contains("database") || message.contains("connection")) {
            return ErrorCategory.DATABASE;
        }
        
        if (className.contains("auth") || message.contains("password") || 
            message.contains("login") || message.contains("unauthorized")) {
            return ErrorCategory.AUTHENTICATION;
        }
        
        if (className.contains("validation") || className.contains("illegal") || 
            message.contains("invalid") || message.contains("required")) {
            return ErrorCategory.VALIDATION;
        }
        
        if (className.contains("network") || className.contains("socket") || 
            message.contains("timeout") || message.contains("connect")) {
            return ErrorCategory.NETWORK;
        }
        
        if (className.contains("security") || className.contains("access") || 
            message.contains("permission") || message.contains("denied")) {
            return ErrorCategory.PERMISSION;
        }
        
        return ErrorCategory.GENERAL;
    }
    
    /**
     * Determines if an error is critical and requires special handling.
     */
    private boolean isCriticalError(Throwable e) {
        return e instanceof OutOfMemoryError ||
               e instanceof StackOverflowError ||
               e instanceof ThreadDeath ||
               e instanceof VirtualMachineError;
    }
    
    /**
     * Gets the full stack trace as a string.
     */
    private String getStackTraceString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * Logs critical errors to audit system.
     */
    private void logToAudit(Thread t, Throwable e, String timestamp) {
        try {
            // This would integrate with AuditService when available
            String auditMessage = String.format(
                "CRITICAL ERROR - Thread: %s, Error: %s, Message: %s, Time: %s",
                t.getName(), e.getClass().getName(), e.getMessage(), timestamp
            );
            logger.error("AUDIT: {}", auditMessage);
        } catch (Exception ex) {
            logger.error("Failed to log to audit", ex);
        }
    }
    
    /**
     * Shows a user-friendly error dialog.
     */
    private void showErrorDialog(ErrorCategory category, Throwable e) {
        String details = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        
        String message = String.format(
            "%s\n\nDetails: %s\n\nError Code: %s-%d\n\nPlease check the logs for more information.",
            category.message,
            details,
            category.name(),
            System.currentTimeMillis() % 10000
        );
        
        JOptionPane.showMessageDialog(
            getActiveWindow(),
            message,
            category.title,
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Gets the currently active window for dialog parent.
     */
    private Window getActiveWindow() {
        for (Window window : Window.getWindows()) {
            if (window.isActive()) {
                return window;
            }
        }
        return null;
    }
    
    /**
     * Offers to restart the application after critical error.
     */
    private void offerRestart() {
        SwingUtilities.invokeLater(() -> {
            int result = JOptionPane.showConfirmDialog(
                getActiveWindow(),
                "The application encountered a critical error and may be unstable.\n" +
                "Would you like to restart the application?",
                "Critical Error - Restart Required",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                restartApplication();
            }
        });
    }
    
    /**
     * Attempts to restart the application.
     */
    private void restartApplication() {
        try {
            // Save session state if possible
            SessionManager.getInstance().invalidate();
            
            // Schedule restart
            logger.info("Attempting application restart...");
            
            // This would need to be implemented based on how the app is launched
            // For now, just exit
            System.exit(1);
        } catch (Exception e) {
            logger.error("Failed to restart application", e);
            System.exit(1);
        }
    }
    
    /**
     * Handles exceptions from Swing/AWT (called via reflection).
     */
    public void handle(Throwable t) {
        uncaughtException(Thread.currentThread(), t);
    }
}