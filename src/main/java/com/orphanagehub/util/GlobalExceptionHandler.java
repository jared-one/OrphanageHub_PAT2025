/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.util;

/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */

import java.lang.Thread.UncaughtExceptionHandler;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalExceptionHandler implements UncaughtExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.error("FATAL: Uncaught exception in thread '{}'", t.getName(), e);

        final String errorMessage =
                String.format(
                        "A critical and unexpected error occurred: %s%n%n"
                                + "The application may be unstable. Please see 'logs/app.log' for full details.%n"
                                + "Error Type: %s",
                        e.getMessage(), e.getClass().getSimpleName());

        JOptionPane.showMessageDialog(
                null, errorMessage, "Application Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void register() {
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
        System.setProperty("sun.awt.exception.handler", GlobalExceptionHandler.class.getName());
        logger.info("Global exception handler registered.");
    }
}
