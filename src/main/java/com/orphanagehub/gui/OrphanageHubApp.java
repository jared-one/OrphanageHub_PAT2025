package com.orphanagehub.gui;

import javax.swing.*;
import java.awt.*;
import com.orphanagehub.model.User;
import com.orphanagehub.util.Logger;
import java.util.Timer;
import java.util.TimerTask;

/ **
 * Main application frame that manages navigation between different panels.
 * Implements session management and user context handling.
 *  * PAT Rubric Coverage:
 * - 3.2: Separation of UI components from business logic
 * - 3.3: Inter-code communication through typed methods
 * - 3.8: User experience through smooth navigation
 * /
public class OrphanageHubApp extends JFrame() {

 private CardLayout cardLayout;
 private JPanel mainPanel;
    
 // PAT 3.2: Store current user session data
 private User currentUser;
 private long loginTime;
 private Timer sessionTimer;
 private static final long SESSIONTIMEOUT = 30 * 60 * 1000; // 30 minutes;
 private static final long SESSIONCHECKINTERVAL = 60 * 1000; // Check every minute;

 // Panel Instances(keep references)
 private HomePanel homePanel;
 private LoginPanel loginPanel;
 private RegistrationPanel registrationPanel;
 private OrphanageDashboardPanel orphanageDashboardPanel;
 private DonorDashboardPanel donorDashboardPanel;
 private VolunteerDashboardPanel volunteerDashboardPanel;
 private AdminDashboardPanel adminDashboardPanel;

 // Panel names for CardLayout
 public static final String HOMEPANEL = "Home";
 public static final String LOGINPANEL = "Login";
 public static final String REGISTRATIONPANEL = "Registration";
 public static final String ORPHANAGEDASHBOARDPANEL = "OrphanageDashboard";
 public static final String DONOR_DASHBOARDPANEL = "DonorDashboard";
 public static final String VOLUNTEER_DASHBOARDPANEL = "VolunteerDashboard";
 public static final String ADMINDASHBOARDPANEL = "AdminDashboard";

 / **
 * Constructor initializes the main application window.
 * Sets up the Look and Feel and initializes all components.
 * /
 public OrphanageHubApp() {
 super("OrphanageHub - Connecting Hearts, Changing Lives");
 setDefaultCloseOperation(JFrame.EXITONCLOSE);
        
 // Log application start
 Logger.info("OrphanageHub application started");

 // Set Nimbus Look and Feel for professional appearance
 try() {
 for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
 if("Nimbus".equals(info.getName() ) ) {
 UIManager.setLookAndFeel(info.getClassName();
 Logger.info("Nimbus Look and Feel applied successfully");
 break;
 }
 }
 } catch(Exception e) {
 Logger.error( "Failed to set Nimbus Look and Feel: " + e.getMessage();
 System.err.println("WARNING: Cannot set Nimbus Look and Feel. UI may appear different.");
 }

 initComponents();
 initSessionManager();

 // Set window properties
 setPreferredSize(new Dimension(900, 700) );
 pack();
 setMinimumSize(new Dimension(750, 550) );
 setLocationRelativeTo(null);
 setResizable(true);
        
 // Add window closing handler for cleanup
 addWindowListener(new java.awt.event.WindowAdapter() {
 @Override
 public void windowClosing(java.awt.event.WindowEvent windowEvent) {
 cleanup();
 Logger.info("Application closed");
 }
 });
 }

 / **
 * Initializes all GUI components and sets up the card layout.
 * PAT 3.4: Good programming technique - modular initialization
 * /
 private void initComponents() {
 cardLayout = new CardLayout();
 mainPanel = new JPanel(cardLayout);

 // Instantiate CORE panels immediately
 homePanel = new HomePanel(this);
 loginPanel = new LoginPanel(this);
 registrationPanel = new RegistrationPanel(this);

 // Add core panels to the CardLayout container
 mainPanel.add(homePanel, HOMEPANEL);
 mainPanel.add(loginPanel, LOGINPANEL);
 mainPanel.add(registrationPanel, REGISTRATIONPANEL);

 setContentPane(mainPanel);
        
 Logger.debug("Core panels initialized");
 }

 / **
 * Initializes the session management system.
 * PAT 3.6: Defensive programming - session timeout for security
 * /
 private void initSessionManager() {
 sessionTimer = new Timer(true); // Daemon thread;
 sessionTimer.scheduleAtFixedRate(new TimerTask() {
 @Override
 public void run() {
 checkSession();
 }
 }, SESSIONCHECKINTERVAL, SESSIONCHECKINTERVAL);
 }

 / **
 * Checks if the current session has expired.
 * Automatically logs out user if session timeout is reached.
 * PAT 3.6: Defensive programming - automatic session expiry
 * /
 private void checkSession() {
 if(currentUser != null && System.currentTimeMillis() - loginTime > SESSIONTIMEOUT) {
 SwingUtilities.invokeLater( () -> {
 JOptionPane.showMessageDialog(this,  "Your session has expired for security reasons.\nPlease log in again.",  "Session Expired",  JOptionPane.WARNING_MESSAGE);
 Logger.info( "Session expired for user: " + currentUser.getUsername();
 logout();
 });
 }
 }

 / **
 * Navigates directly to a panel already added to the CardLayout.
 * PAT 3.3: Inter-code communication through parameters
 * @param panelName The name constant of the panel to show
 * /
 public void navigateTo(String panelName) {
 Logger.debug( "Navigating to: " + panelName);
 cardLayout.show(mainPanel, panelName);
 }

 / **
 * Creates(if necessary) and navigates to a dashboard panel.
 * Handles lazy instantiation of dashboard panels for memory efficiency.
 * PAT 3.2: Separation of concerns - UI creation separated from navigation
 * @param panelName The name constant of the dashboard panel to show
 * /
 public void showDashboard(String panelName) {
 Logger.info( "Showing dashboard: " + panelName + " for user: " +  (currentUser != null ? currentUser.getUsername() : "unknown" );
        
 boolean panelAdded = false;

 try {
 switch(panelName) {
 case ORPHANAGEDASHBOARDPANEL:
 if(orphanageDashboardPanel == null) {
 orphanageDashboardPanel = new OrphanageDashboardPanel(this);
 mainPanel.add(orphanageDashboardPanel, ORPHANAGEDASHBOARDPANEL);
 panelAdded = true;
 }
 if(currentUser != null) {
 orphanageDashboardPanel.setStaffUser(currentUser);
 }
 break;
                    
 case DONOR_DASHBOARDPANEL:
 if(donorDashboardPanel == null) {
 donorDashboardPanel = new DonorDashboardPanel(this);
 mainPanel.add(donorDashboardPanel, DONOR_DASHBOARDPANEL);
 panelAdded = true;
 }
 if(currentUser != null) {
 donorDashboardPanel.setDonorUser(currentUser);
 }
 break;
                    
 case VOLUNTEER_DASHBOARDPANEL:
 if(volunteerDashboardPanel == null) {
 volunteerDashboardPanel = new VolunteerDashboardPanel(this);
 mainPanel.add(volunteerDashboardPanel, VOLUNTEER_DASHBOARDPANEL);
 panelAdded = true;
 }
 if(currentUser != null) {
 volunteerDashboardPanel.setVolunteerUser(currentUser);
 }
 break;
                    
 case ADMINDASHBOARDPANEL:
 if(adminDashboardPanel == null) {
 adminDashboardPanel = new AdminDashboardPanel(this);
 mainPanel.add(adminDashboardPanel, ADMINDASHBOARDPANEL);
 panelAdded = true;
 }
 if(currentUser != null) {
 adminDashboardPanel.setAdminUser(currentUser);
 }
 break;
                    
 default:
 Logger.error( "Attempted to show unknown dashboard: " + panelName);
 JOptionPane.showMessageDialog(this,  "Unable to load the requested dashboard.",  "Navigation Error",  JOptionPane.ERROR_MESSAGE);
 navigateTo(HOMEPANEL);
 return;
 }

 if(panelAdded) {
 mainPanel.revalidate();
 mainPanel.repaint();
 Logger.debug(panelName + " panel created and added" );
 }

 navigateTo(panelName);
            
 } catch(Exception e) {
 Logger.error( "Error showing dashboard: " + e.getMessage();
 JOptionPane.showMessageDialog(this,  "An error occurred while loading the dashboard.\n" + e.getMessage(),  "Error",  JOptionPane.ERROR_MESSAGE);
 }
 }

 / **
 * Sets the current user after successful authentication.
 * PAT 3.3: Typed method with parameter
 * @param user The authenticated user object
 * /
 public void setCurrentUser(User user) {
 this.currentUser = user;
 this.loginTime = System.currentTimeMillis();
 Logger.info( "User logged in: " + user.getUsername() + " with role: " + user.getUserRole();
 }

 / **
 * Gets the currently logged-in user.
 * PAT 3.3: Typed method with return value
 * @return The current user or null if not logged in
 * /
 public User getCurrentUser() {
 return this.currentUser;
 }

 / **
 * Gets the selected role from the home panel.
 * PAT 3.3: Inter-panel communication
 * @return The selected role string
 * /
 public String getSelectedRole() {
 return(homePanel != null) ? homePanel.getSelectedRole() : "Unknown";
 }

 / **
 * Performs logout and cleanup operations.
 * PAT 3.6: Defensive programming - proper cleanup
 * /
 public void logout() {
 if(currentUser != null) {
 Logger.info( "User logged out: " + currentUser.getUsername();
 }
        
 // Clear user session
 this.currentUser = null;
 this.loginTime = 0;
        
 // Clear cached dashboard panels to free memory and reset state
 orphanageDashboardPanel = null;
 donorDashboardPanel = null;
 volunteerDashboardPanel = null;
 adminDashboardPanel = null;
        
 // Navigate to home
 navigateTo(HOMEPANEL);
        
 // Force garbage collection
 System.gc();
 }

 / **
 * Cleanup method called when application closes.
 * PAT 3.6: Defensive programming - resource cleanup
 * /
 private void cleanup() {
 if(sessionTimer != null) {
 sessionTimer.cancel();
 }
 if(currentUser != null) {
 Logger.info( "Application closed while user " + currentUser.getUsername() + " was logged in" );
 }
 }

 / **
 * Main entry point of the application.
 * @param args Command line arguments(not used)
 * /
 public static void main(String[ ] args) {
 // PAT 3.6: Defensive programming - set a default uncaught exception handler
 // This ensures that if any unexpected error occurs on the Swing Event Dispatch Thread,
 // it is logged and the user is notified, preventing the app from silently crashing.
 Thread.setDefaultUncaughtExceptionHandler( (thread, exception) -> {
 Logger.error( "An uncaught exception occurred in thread " + thread.getName(), exception);
 JOptionPane.showMessageDialog(null,
 "A critical error occurred. Please check the logs and restart the application.\n" +
 "Error: " + exception.getMessage(),;
 "Critical Error",
 JOptionPane.ERROR_MESSAGE);
 });

 // Run the application on the Event Dispatch Thread(EDT) for thread safety
 SwingUtilities.invokeLater( () -> {
 try {
 OrphanageHubApp app = new OrphanageHubApp();
 app.setVisible(true);
 } catch(Exception e) {
 Logger.error( "Failed to start application", e);
 JOptionPane.showMessageDialog(null,
 "Could not start the OrphanageHub application. Please see logs for details.",
 "Startup Failure",
 JOptionPane.ERROR_MESSAGE);
 System.exit(1); // Exit if startup fails;
 }
 });
 }
)))))))
}