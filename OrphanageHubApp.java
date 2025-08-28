package com.orphanagehub.gui;

import javax.swing.*;
import java.awt.*;

public class OrphanageHubApp extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Panel Instances (keep references)
    private HomePanel homePanel;
    private LoginPanel loginPanel;
    private RegistrationPanel registrationPanel;
    private OrphanageDashboardPanel orphanageDashboardPanel;
    private DonorDashboardPanel donorDashboardPanel;         // Added reference
    private VolunteerDashboardPanel volunteerDashboardPanel; // Added reference
    private AdminDashboardPanel adminDashboardPanel;         // Added reference

    // Panel names for CardLayout
    public static final String HOME_PANEL = "Home";
    public static final String LOGIN_PANEL = "Login";
    public static final String REGISTRATION_PANEL = "Registration";
    public static final String ORPHANAGE_DASHBOARD_PANEL = "OrphanageDashboard";
    public static final String DONOR_DASHBOARD_PANEL = "DonorDashboard";         // Added constant
    public static final String VOLUNTEER_DASHBOARD_PANEL = "VolunteerDashboard"; // Added constant
    public static final String ADMIN_DASHBOARD_PANEL = "AdminDashboard";         // Added constant

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

        initComponents(); // Initialize components and layout

        // Set initial size
        setPreferredSize(new Dimension(900, 700)); // Increased default size for dashboards
        pack();
        setMinimumSize(new Dimension(750, 550)); // Adjusted minimum size
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private void initComponents() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Instantiate CORE panels immediately
        homePanel = new HomePanel(this);
        loginPanel = new LoginPanel(this);
        registrationPanel = new RegistrationPanel(this);
        // Dashboard panels are instantiated on demand via showDashboard()

        // Add core panels to the CardLayout container
        mainPanel.add(homePanel, HOME_PANEL);
        mainPanel.add(loginPanel, LOGIN_PANEL);
        mainPanel.add(registrationPanel, REGISTRATION_PANEL);
        // Dashboard panels are added later

        setContentPane(mainPanel);
    }

    // --- Navigation Methods ---

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
        boolean panelAdded = false; // Flag to track if a panel was added

        // Ensure dashboard panels are created and added before showing
        if (panelName.equals(ORPHANAGE_DASHBOARD_PANEL)) {
            if (orphanageDashboardPanel == null) {
                System.out.println("Creating Orphanage Dashboard Panel...");
                orphanageDashboardPanel = new OrphanageDashboardPanel(this);
                mainPanel.add(orphanageDashboardPanel, ORPHANAGE_DASHBOARD_PANEL);
                panelAdded = true;
            }
            // Add logic later to pass actual user/orphanage data
        } else if (panelName.equals(DONOR_DASHBOARD_PANEL)) {
            if (donorDashboardPanel == null) {
                System.out.println("Creating Donor Dashboard Panel...");
                donorDashboardPanel = new DonorDashboardPanel(this);
                mainPanel.add(donorDashboardPanel, DONOR_DASHBOARD_PANEL);
                panelAdded = true;
            }
            // Add logic later to pass donor-specific data
        } else if (panelName.equals(VOLUNTEER_DASHBOARD_PANEL)) {
            if (volunteerDashboardPanel == null) {
                System.out.println("Creating Volunteer Dashboard Panel...");
                volunteerDashboardPanel = new VolunteerDashboardPanel(this);
                mainPanel.add(volunteerDashboardPanel, VOLUNTEER_DASHBOARD_PANEL);
                panelAdded = true;
            }
            // Add logic later to pass volunteer-specific data
        } else if (panelName.equals(ADMIN_DASHBOARD_PANEL)) {
            if (adminDashboardPanel == null) {
                System.out.println("Creating Admin Dashboard Panel...");
                adminDashboardPanel = new AdminDashboardPanel(this);
                mainPanel.add(adminDashboardPanel, ADMIN_DASHBOARD_PANEL);
                panelAdded = true;
            }
            // Add logic later to pass admin-specific data
        } else {
            System.err.println("Error: Attempted to show unknown or unsupported dashboard panel: " + panelName);
            navigateTo(HOME_PANEL); // Fallback to home screen
            return; // Exit early if panel name is invalid
        }

        // Revalidate the main panel *if* a new component was actually added
        if (panelAdded) {
            mainPanel.revalidate();
            System.out.println(panelName + " Added and Revalidated.");
        }

        navigateTo(panelName); // Navigate to the requested panel
    }

    // Method for panels to get the selected role from HomePanel
    public String getSelectedRole() {
        return (homePanel != null) ? homePanel.getSelectedRole() : "Unknown";
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            OrphanageHubApp app = new OrphanageHubApp();
            app.setVisible(true);
        });
    }
}
