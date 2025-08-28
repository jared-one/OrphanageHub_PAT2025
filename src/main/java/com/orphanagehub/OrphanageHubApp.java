/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub;

/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class OrphanageHubApp {
    private static final SimpleLogger logger = new SimpleLogger(OrphanageHubApp.class.getName());

    public static void main(String[] args) {
        // Register global exception handler FIRST
        GlobalExceptionHandler.register();

        // Install EDT guard for development
        EdtGuard.install();

        logger.info("Starting OrphanageHub application...");

        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.warn("Could not set system look and feel: " + e.getMessage());
        }

        // Launch the application on the EDT
        SwingUtilities.invokeLater(
                () -> {
                    try {
                        LoginFrame loginFrame = new LoginFrame();
                        loginFrame.setVisible(true);
                        logger.info("Application UI launched successfully");
                    } catch (Exception e) {
                        logger.error("Failed to launch application UI: " + e.getMessage());
                    }
                });
    }
}

// Simple Logger implementation (replacing SLF4J)
class SimpleLogger {
    private final String className;
    private static final Logger javaLogger = Logger.getLogger("OrphanageHub");

    public SimpleLogger(String className) {
        this.className = className;
    }

    public void info(String message) {
        System.out.println("[INFO] " + className + ": " + message);
        javaLogger.log(Level.INFO, message);
    }

    public void warn(String message) {
        System.out.println("[WARN] " + className + ": " + message);
        javaLogger.log(Level.WARNING, message);
    }

    public void error(String message) {
        System.err.println("[ERROR] " + className + ": " + message);
        javaLogger.log(Level.SEVERE, message);
    }

    public void debug(String message) {
        System.out.println("[DEBUG] " + className + ": " + message);
        javaLogger.log(Level.FINE, message);
    }
}

// Global Exception Handler utility class
class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final SimpleLogger logger =
            new SimpleLogger(GlobalExceptionHandler.class.getName());

    public static void register() {
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
        logger.info("Global exception handler registered");
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        logger.error(
                "Uncaught exception in thread " + thread.getName() + ": " + throwable.getMessage());
        throwable.printStackTrace();

        // Show error dialog to user
        SwingUtilities.invokeLater(
                () -> {
                    JOptionPane.showMessageDialog(
                            null,
                            "An unexpected error occurred:\n" + throwable.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                });
    }
}

// EDT Guard utility class for development
class EdtGuard {
    private static final SimpleLogger logger = new SimpleLogger(EdtGuard.class.getName());
    private static boolean installed = false;

    public static void install() {
        if (installed) {
            return;
        }

        // In development mode, check for EDT violations
        if (isDevelopmentMode()) {
            RepaintManager.setCurrentManager(new CheckThreadViolationRepaintManager());
            logger.info("EDT Guard installed - checking for thread violations");
        }
        installed = true;
    }

    private static boolean isDevelopmentMode() {
        // You can check for a system property or environment variable
        String mode = System.getProperty("app.mode", "development");
        return "development".equals(mode);
    }

    // Custom RepaintManager to detect EDT violations
    static class CheckThreadViolationRepaintManager extends RepaintManager {
        @Override
        public synchronized void addInvalidComponent(JComponent component) {
            checkThreadViolations(component);
            super.addInvalidComponent(component);
        }

        @Override
        public void addDirtyRegion(JComponent component, int x, int y, int w, int h) {
            checkThreadViolations(component);
            super.addDirtyRegion(component, x, y, w, h);
        }

        private void checkThreadViolations(JComponent component) {
            if (!SwingUtilities.isEventDispatchThread()) {
                logger.warn("EDT violation detected! Component: " + component.getClass().getName());
                Thread.dumpStack();
            }
        }
    }
}

// Login Frame implementation
class LoginFrame extends JFrame {
    private static final SimpleLogger logger = new SimpleLogger(LoginFrame.class.getName());
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;

    public LoginFrame() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("OrphanageHub - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Create main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create header panel
        JPanel headerPanel = new JPanel();
        JLabel titleLabel = new JLabel("Welcome to OrphanageHub");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titleLabel);

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Username label and field
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        formPanel.add(usernameField, gbc);

        // Password label and field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField, gbc);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        loginButton = new JButton("Login");
        cancelButton = new JButton("Cancel");

        loginButton.addActionListener(this::handleLogin);
        cancelButton.addActionListener(e -> System.exit(0));

        // Add keyboard support
        getRootPane().setDefaultButton(loginButton);

        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);

        // Add panels to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        add(mainPanel);

        // Pack and center the window
        pack();
        setLocationRelativeTo(null);

        // Focus username field
        SwingUtilities.invokeLater(() -> usernameField.requestFocusInWindow());

        logger.info("Login frame initialized");
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter both username and password",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Simple validation (in real app, this would check against database)
        if ("admin".equals(username) && "admin123".equals(password)) {
            logger.info("Login successful for user: " + username);
            JOptionPane.showMessageDialog(
                    this,
                    "Login successful! Welcome " + username,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            // Here you would typically open the main application window
            // For now, we'll just show a simple main window
            SwingUtilities.invokeLater(
                    () -> {
                        this.dispose();
                        MainWindow mainWindow = new MainWindow();
                        mainWindow.setVisible(true);
                    });
        } else {
            logger.warn("Login failed for user: " + username);
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid username or password",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        }
    }
}

// Simple Main Window (placeholder for the actual application)
class MainWindow extends JFrame {
    private static final SimpleLogger logger = new SimpleLogger(MainWindow.class.getName());

    public MainWindow() {
        setTitle("OrphanageHub - Main Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create menu bar
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(
                e -> {
                    JOptionPane.showMessageDialog(
                            this,
                            "OrphanageHub v1.0\n© 2025 Jared Wisdom\nAll Rights Reserved",
                            "About",
                            JOptionPane.INFORMATION_MESSAGE);
                });
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        // Create main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel welcomeLabel =
                new JLabel("Welcome to OrphanageHub Management System", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        contentPanel.add(welcomeLabel, BorderLayout.CENTER);

        add(contentPanel);

        logger.info("Main window initialized");
    }
}
