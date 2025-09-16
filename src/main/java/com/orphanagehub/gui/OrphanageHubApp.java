package com.orphanagehub.gui;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import io.vavr.control.Option;
import com.orphanagehub.util.SessionManager;

public class OrphanageHubApp extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    
    // Immutable role tracking using volatile for thread safety
    private volatile String lastSelectedRole = "Donor";

    // Panel Instances (lazy initialization)
    private HomePanel homePanel;
    private LoginPanel loginPanel;
    private RegistrationPanel registrationPanel;
    private OrphanageDashboardPanel orphanageDashboardPanel;
    private DonorDashboardPanel donorDashboardPanel;
    private VolunteerDashboardPanel volunteerDashboardPanel;
    private AdminDashboardPanel adminDashboardPanel;

    // Panel names
    public static final String HOME_PANEL = "Home";
    public static final String LOGIN_PANEL = "Login";
    public static final String REGISTRATION_PANEL = "Registration";
    public static final String ORPHANAGE_DASHBOARD_PANEL = "OrphanageDashboard";
    public static final String DONOR_DASHBOARD_PANEL = "DonorDashboard";
    public static final String VOLUNTEER_DASHBOARD_PANEL = "VolunteerDashboard";
    public static final String ADMIN_DASHBOARD_PANEL = "AdminDashboard";

    public OrphanageHubApp() {
        super("OrphanageHub");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set Nimbus Look and Feel for better dark theme support
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    
                    // Customize Nimbus colors for dark theme
                    UIManager.put("control", new Color(45, 52, 54));
                    UIManager.put("info", new Color(60, 60, 60));
                    UIManager.put("nimbusBase", new Color(35, 42, 44));
                    UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
                    UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
                    UIManager.put("nimbusFocus", new Color(115, 164, 209));
                    UIManager.put("nimbusGreen", new Color(176, 179, 50));
                    UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
                    UIManager.put("nimbusLightBackground", new Color(60, 60, 60));
                    UIManager.put("nimbusOrange", new Color(191, 98, 4));
                    UIManager.put("nimbusRed", new Color(169, 46, 34));
                    UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
                    UIManager.put("nimbusSelectionBackground", new Color(104, 93, 156));
                    UIManager.put("text", new Color(230, 230, 230));
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Cannot set Nimbus Look and Feel");
            // Fallback to system default but try to apply dark colors
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Ignore
            }
        }

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(35, 42, 44)); // Dark background for main panel
        
        initComponents();

        setPreferredSize(new Dimension(1000, 750));
        pack();
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Set application icon if available
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icons/app-icon.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // Icon not found, use default
        }
    }

    private void initComponents() {
        // Initialize core panels immediately
        homePanel = new HomePanel(this);
        loginPanel = new LoginPanel(this);
        registrationPanel = new RegistrationPanel(this);

        // Add core panels to the CardLayout container
        mainPanel.add(homePanel, HOME_PANEL);
        mainPanel.add(loginPanel, LOGIN_PANEL);
        mainPanel.add(registrationPanel, REGISTRATION_PANEL);

        setContentPane(mainPanel);
    }

    public void navigateTo(String panelName) {
        System.out.println("Navigating to: " + panelName);
        
        // FIXED: Sync role from HomePanel radios *just before* showing Registration
        if (REGISTRATION_PANEL.equals(panelName)) {
            String radioRole = Optional.ofNullable(homePanel)
                .map(HomePanel::getSelectedRole)
                .orElse(lastSelectedRole);
            setLastSelectedRole(radioRole);
            System.out.println("Synced role for Registration: " + radioRole);
            
            // FIXED: Explicitly update RegistrationPanel UI post-sync
            if (registrationPanel != null) {
                registrationPanel.setCurrentRole(radioRole);
            }
        }
        
        // Ensure panel exists before navigating
        if (panelName.equals(ORPHANAGE_DASHBOARD_PANEL) && orphanageDashboardPanel == null) {
            showDashboard(ORPHANAGE_DASHBOARD_PANEL);
            return;
        } else if (panelName.equals(DONOR_DASHBOARD_PANEL) && donorDashboardPanel == null) {
            showDashboard(DONOR_DASHBOARD_PANEL);
            return;
        } else if (panelName.equals(VOLUNTEER_DASHBOARD_PANEL) && volunteerDashboardPanel == null) {
            showDashboard(VOLUNTEER_DASHBOARD_PANEL);
            return;
        } else if (panelName.equals(ADMIN_DASHBOARD_PANEL) && adminDashboardPanel == null) {
            showDashboard(ADMIN_DASHBOARD_PANEL);
            return;
        }
        
        cardLayout.show(mainPanel, panelName);
    }

    // FIXED: Properly handle dashboard creation and navigation
    public void showDashboard(String panelName) {
        System.out.println("Attempting to show dashboard: " + panelName);
        
        // Check authorization for admin panel
        if (ADMIN_DASHBOARD_PANEL.equals(panelName)) {
            Option<Object> userRole = SessionManager.getInstance().getAttribute("userRole");
            if (!userRole.map(r -> "Admin".equals(r.toString())).getOrElse(false)) {
                JOptionPane.showMessageDialog(this, 
                    "Unauthorized access to admin panel", 
                    "Access Denied", 
                    JOptionPane.ERROR_MESSAGE);
                navigateTo(HOME_PANEL);
                return;
            }
        }
        
        boolean panelAdded = switch (panelName) {
            case ORPHANAGE_DASHBOARD_PANEL -> {
                if (orphanageDashboardPanel == null) {
                    System.out.println("Creating Orphanage Dashboard Panel...");
                    orphanageDashboardPanel = new OrphanageDashboardPanel(this);
                    mainPanel.add(orphanageDashboardPanel, ORPHANAGE_DASHBOARD_PANEL);
                    yield true;
                }
                yield false;
            }
            case DONOR_DASHBOARD_PANEL -> {
                if (donorDashboardPanel == null) {
                    System.out.println("Creating Donor Dashboard Panel...");
                    donorDashboardPanel = new DonorDashboardPanel(this);
                    mainPanel.add(donorDashboardPanel, DONOR_DASHBOARD_PANEL);
                    yield true;
                }
                yield false;
            }
            case VOLUNTEER_DASHBOARD_PANEL -> {
                if (volunteerDashboardPanel == null) {
                    System.out.println("Creating Volunteer Dashboard Panel...");
                    volunteerDashboardPanel = new VolunteerDashboardPanel(this);
                    mainPanel.add(volunteerDashboardPanel, VOLUNTEER_DASHBOARD_PANEL);
                    yield true;
                }
                yield false;
            }
            case ADMIN_DASHBOARD_PANEL -> {
                if (adminDashboardPanel == null) {
                    System.out.println("Creating Admin Dashboard Panel...");
                    adminDashboardPanel = new AdminDashboardPanel(this);
                    mainPanel.add(adminDashboardPanel, ADMIN_DASHBOARD_PANEL);
                    yield true;
                }
                yield false;
            }
            default -> {
                System.err.println("Error: Unknown dashboard panel: " + panelName);
                navigateTo(HOME_PANEL);
                yield false;
            }
        };

        if (panelAdded) {
            mainPanel.revalidate();
            mainPanel.repaint();
            System.out.println(panelName + " added and revalidated.");
        }

        navigateTo(panelName);
    }

    public String getSelectedRole() {
        return Optional.ofNullable(homePanel)
                .map(HomePanel::getSelectedRole)
                .orElse(lastSelectedRole);
    }

    public void setLastSelectedRole(String role) {
        if (role != null && !role.trim().isEmpty()) {
            this.lastSelectedRole = role;
            System.out.println("Role updated to: " + role);
        }
    }

    public String getLastSelectedRole() {
        return lastSelectedRole;
    }
    
    /**
     * Logout user and return to home
     */
    public void logout() {
        // Clear session
        SessionManager.getInstance().clear();
        
        // Clear dashboard panels to force recreation on next login
        orphanageDashboardPanel = null;
        donorDashboardPanel = null;
        volunteerDashboardPanel = null;
        adminDashboardPanel = null;
        
        // Navigate to home
        navigateTo(HOME_PANEL);
        
        JOptionPane.showMessageDialog(this, 
            "You have been successfully logged out.", 
            "Logout Successful", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show error message dialog
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, 
            message, 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show success message dialog
     */
    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, 
            message, 
            "Success", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show confirmation dialog
     */
    public boolean showConfirmation(String message) {
        int result = JOptionPane.showConfirmDialog(this, 
            message, 
            "Confirm", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    public static void main(String[] args) {
        // Set system properties for better rendering on high DPI displays
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("sun.java2d.uiScale", "1.0");
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Enable anti-aliasing for better text rendering
                System.setProperty("awt.useSystemAAFontSettings", "on");
                System.setProperty("swing.aatext", "true");
                
                OrphanageHubApp app = new OrphanageHubApp();
                app.setVisible(true);
                
                // Show welcome message
                System.out.println("OrphanageHub Application Started");
                System.out.println("================================");
                System.out.println("Ready to connect orphanages with donors and volunteers!");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Failed to start application: " + e.getMessage(), 
                    "Startup Error", 
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}