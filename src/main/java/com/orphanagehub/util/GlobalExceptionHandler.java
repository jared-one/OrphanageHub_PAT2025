package com.orphanagehub.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Global handler for uncaught exceptions.
 * Logs and shows user-friendly messages.
 */
public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.error("Uncaught exception in thread: {}", t.getName(), e);
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "An unexpected error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
    }

    /**
     * Installs the global handler.
     */
    public static void install() {
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
    }
}
