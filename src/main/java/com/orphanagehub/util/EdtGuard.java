package com.orphanagehub.util;

import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Component;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Enhanced Event Dispatch Thread (EDT) guard for thread-safe Swing operations.
 * Ensures all UI updates happen on the EDT and provides utilities for async operations.
 * 
 * @author OrphanageHub Team
 * @version 2.0
 * @since 2025-09-06
 */
public class EdtGuard {
    
    private static final Logger logger = LoggerFactory.getLogger(EdtGuard.class);
    private static final long DEFAULT_TIMEOUT_SECONDS = 30;
    
    /**
     * Runs code on the EDT, either immediately if already on EDT or via invokeLater.
     * 
     * @param runnable The code to run
     */
    public static void runOnEdt(Runnable runnable) {
        if (runnable == null) {
            logger.warn("Attempted to run null runnable on EDT");
            return;
        }
        
        if (SwingUtilities.isEventDispatchThread()) {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.error("Error executing on EDT", e);
                throw e;
            }
        } else {
            SwingUtilities.invokeLater(() -> {
                try {
                    runnable.run();
                } catch (Exception e) {
                    logger.error("Error executing on EDT via invokeLater", e);
                }
            });
        }
    }
    
    /**
     * Runs code on the EDT and waits for completion.
     * 
     * @param runnable The code to run
     * @throws RuntimeException if execution fails
     */
    public static void runOnEdtAndWait(Runnable runnable) {
        if (runnable == null) {
            logger.warn("Attempted to run null runnable on EDT");
            return;
        }
        
        if (SwingUtilities.isEventDispatchThread()) {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.error("Error executing on EDT", e);
                throw new RuntimeException("EDT execution failed", e);
            }
        } else {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        logger.error("Error executing on EDT via invokeAndWait", e);
                        throw new RuntimeException("EDT execution failed", e);
                    }
                });
            } catch (Exception e) {
                logger.error("Failed to execute on EDT and wait", e);
                throw new RuntimeException("EDT execution failed", e);
            }
        }
    }
    
    /**
     * Runs code off the EDT asynchronously.
     * 
     * @param runnable The code to run
     * @return CompletableFuture for chaining
     */
    public static CompletableFuture<Void> runOffEdt(Runnable runnable) {
        if (runnable == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.error("Error executing off EDT", e);
                throw new RuntimeException("Off-EDT execution failed", e);
            }
        });
    }
    
    /**
     * Runs code off EDT with timeout.
     * 
     * @param runnable The code to run
     * @param timeout Timeout value
     * @param unit Timeout unit
     * @return CompletableFuture for chaining
     */
    public static CompletableFuture<Void> runOffEdtWithTimeout(Runnable runnable, long timeout, TimeUnit unit) {
        return runOffEdt(runnable)
            .orTimeout(timeout, unit)
            .exceptionally(throwable -> {
                if (throwable instanceof TimeoutException) {
                    logger.error("Task timed out after {} {}", timeout, unit);
                } else {
                    logger.error("Task failed", throwable);
                }
                return null;
            });
    }
    
    /**
     * Ensures current thread is EDT, throws if not.
     * 
     * @throws IllegalStateException if not on EDT
     */
    public static void requireEdt() {
        if (!SwingUtilities.isEventDispatchThread()) {
            String message = "This operation must be performed on the Event Dispatch Thread";
            logger.error(message);
            throw new IllegalStateException(message);
        }
    }
    
    /**
     * Ensures current thread is NOT EDT, throws if it is.
     * 
     * @throws IllegalStateException if on EDT
     */
    public static void requireNotEdt() {
        if (SwingUtilities.isEventDispatchThread()) {
            String message = "This operation must NOT be performed on the Event Dispatch Thread";
            logger.error(message);
            throw new IllegalStateException(message);
        }
    }
    
    /**
     * Checks if currently on EDT.
     * 
     * @return true if on EDT
     */
    public static boolean isOnEdt() {
        return SwingUtilities.isEventDispatchThread();
    }
    
    /**
     * Updates a Swing component safely.
     * 
     * @param component The component to update
     * @param updater The update logic
     */
    public static void updateComponent(JComponent component, Runnable updater) {
        if (component == null || updater == null) {
            logger.warn("Null component or updater provided");
            return;
        }
        
        runOnEdt(() -> {
            try {
                updater.run();
                component.revalidate();
                component.repaint();
            } catch (Exception e) {
                logger.error("Failed to update component: {}", component.getClass().getSimpleName(), e);
            }
        });
    }
    
    /**
     * Shows a dialog safely on EDT.
     * 
     * @param parent Parent component
     * @param message Dialog message
     * @param title Dialog title
     * @param messageType JOptionPane message type
     */
    public static void showDialog(Component parent, String message, String title, int messageType) {
        runOnEdt(() -> JOptionPane.showMessageDialog(parent, message, title, messageType));
    }
    
    /**
     * Installs EDT violation detector for development.
     */
    public static void installViolationDetector() {
        String env = System.getProperty("app.env", "production");
        
        if (!"development".equalsIgnoreCase(env) && !"dev".equalsIgnoreCase(env)) {
            logger.info("EDT violation detector not installed (production mode)");
            return;
        }
        
        RepaintManager.setCurrentManager(new RepaintManager() {
            @Override
            public void addInvalidComponent(JComponent component) {
                checkEDTViolation();
                super.addInvalidComponent(component);
            }
            
            @Override
            public void addDirtyRegion(JComponent component, int x, int y, int w, int h) {
                checkEDTViolation();
                super.addDirtyRegion(component, x, y, w, h);
            }
            
            private void checkEDTViolation() {
                if (!SwingUtilities.isEventDispatchThread()) {
                    Exception e = new Exception("EDT Violation Detected!");
                    logger.warn("EDT VIOLATION: UI operation outside Event Dispatch Thread", e);
                }
            }
        });
        
        logger.info("EDT violation detector installed for development");
    }
}