// src/main/java/com/orphanagehub/gui/OrphanageHubApp.java
package com.orphanagehub.gui;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class OrphanageHubApp extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    
    // Immutable role tracking using Optional for null safety
    private volatile String lastSelectedRole = "Donor"; // Default role

    // Panel Instances (keep references)
    private HomePanel homePanel;
    private LoginPanel loginPanel;
    private RegistrationPanel registrationPanel;
    private OrphanageDashboardPanel orphanageDashboardPanel;
    private DonorDashboardPanel donorDashboardPanel;
    private VolunteerDashboardPanel volunteerDashboardPanel;
    private AdminDashboardPanel adminDashboardPanel;

    // Panel names for CardLayout
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

        // Set Nimbus Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("CRITICAL FAILURE: Cannot set Nimbus Look and Feel. UI may appear incorrect.");
        }

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        initComponents(); // Initialize components and layout

        // Set initial size
        setPreferredSize(new Dimension(900, 700));
        pack();
        setMinimumSize(new Dimension(750, 550));
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private void initComponents() {
        // Instantiate CORE panels immediately
        homePanel = new HomePanel(this);
        loginPanel = new LoginPanel(this);
        registrationPanel = new RegistrationPanel(this);
        // Dashboard panels are instantiated on demand via showDashboard()

        // Add core panels to the CardLayout container
        mainPanel.add(homePanel, HOME_PANEL);
        mainPanel.add(loginPanel, LOGIN_PANEL);
        mainPanel.add(registrationPanel, REGISTRATION_PANEL);

        setContentPane(mainPanel);
    }

    /**
     * Navigates directly to a panel already added to the CardLayout.
     * @param panelName The name constant of the panel to show.
     */
    public void navigateTo(String panelName) {
        System.out.println("Navigating to: " + panelName); // Debug
        cardLayout.show(mainPanel, panelName);
    }

    /**
     * Creates (if necessary) and navigates to a dashboard panel.
     * Handles lazy instantiation of dashboard panels.
     * @param panelName The name constant of the dashboard panel to show.
     */
    public void showDashboard(String panelName) {
        System.out.println("Attempting to show dashboard: " + panelName); // Debug
        
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
                System.err.println("Error: Attempted to show unknown or unsupported dashboard panel: " + panelName);
                navigateTo(HOME_PANEL); // Fallback to home screen
                yield false;
            }
        };

        // Revalidate the main panel if a new component was actually added
        if (panelAdded) {
            mainPanel.revalidate();
            System.out.println(panelName + " Added and Revalidated.");
        }

        navigateTo(panelName); // Navigate to the requested panel
    }

    /**
     * Gets the selected role from HomePanel in a null-safe manner.
     * @return The selected role or "Unknown" if homePanel is null.
     */
    public String getSelectedRole() {
        return Optional.ofNullable(homePanel)
                .map(HomePanel::getSelectedRole)
                .orElse(lastSelectedRole); // Use last selected role as fallback
    }

    /**
     * Sets the last selected role for persistence across panel changes.
     * Thread-safe using volatile field.
     * @param role The role to remember.
     */
    public void setLastSelectedRole(String role) {
        if (role != null && !role.trim().isEmpty()) {
            this.lastSelectedRole = role;
            System.out.println("Role updated to: " + role); // Debug
        }
    }

    /**
     * Gets the last selected role.
     * @return The last selected role, never null.
     */
    public String getLastSelectedRole() {
        return lastSelectedRole;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            OrphanageHubApp app = new OrphanageHubApp();
            app.setVisible(true);
        });
    }
}