package com.orphanagehub.gui;

// PAT 3.2: Import backend and model classes
import com.orphanagehub.model.User;
import com.orphanagehub.service.AuthService;
import com.orphanagehub.service.ServiceException;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginPanel extends JPanel() {

 private final OrphanageHubApp mainApp;
 private final JTextField txtUsername;
 private final JPasswordField txtPassword;
    
 // PAT 3.2: Reference to the backend service layer
 private final AuthService authService;

 // - - - Colors(Unchanged) - - -
 private static final Color DARKBGSTART = new Color(45, 52, 54);
 private static final Color DARKBGEND = new Color(35, 42, 44);
 private static final Color TITLECOLOR_DARK = new Color(223, 230, 233);
 private static final Color TEXTCOLOR_DARK = new Color(200, 200, 200);
 private static final Color BORDERCOLOR_DARK = new Color(80, 80, 80);
 private static final Color INPUTBG_DARK = new Color(60, 60, 60);
 private static final Color INPUTFG_DARK = new Color(220, 220, 220);
 private static final Color INPUTBORDER_DARK = new Color(90, 90, 90);
 private static final Color BUTTONBG_DARK = new Color(99, 110, 114);
 private static final Color BUTTONFG_DARK = Color.WHITE;
 private static final Color BUTTONHOVER_BG_DARK = new Color(120, 130, 134);
 private static final Color LINKCOLOR = new Color(100, 180, 255);

 public LoginPanel(OrphanageHubApp app) {
 this.mainApp = app;
 // PAT 3.2: Instantiate the service, not the DAO, in the UI layer
 this.authService = new AuthService();  setLayout(new GridBagLayout();
 setBorder(new EmptyBorder(40, 60, 40, 60) );
        
 // Initialize components
 txtUsername = new JTextField(20);
 txtPassword = new JPasswordField(20);
        
 initComponents();
 }

 @Override
 protected void paintComponent(Graphics g) {
 super.paintComponent(g);
 Graphics2D g2d = (Graphics2D) g;
 g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUERENDER_QUALITY);
 GradientPaint gp = new GradientPaint(0, 0, DARKBGSTART, 0, getHeight(), DARKBGEND);
 g2d.setPaint(gp);
 g2d.fillRect(0, 0, getWidth(), getHeight();
 }

 private void initComponents() {
 GridBagConstraints gbc = new GridBagConstraints();
 gbc.fill = GridBagConstraints.HORIZONTAL;
 gbc.insets = new Insets(5, 5, 5, 5);

 // - - - Title-- -
 JLabel lblTitle = new JLabel( "User Login", SwingConstants.CENTER);
 lblTitle.setFont(new Font(Font.SANSSERIF, Font.BOLD, 28) );
 lblTitle.setForeground(TITLECOLOR_DARK);
 gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.insets = new Insets(0, 5, 25, 5);
 add(lblTitle, gbc);
 gbc.gridwidth = 1; gbc.insets = new Insets(8, 5, 8, 5);

 // - - - Username-- -
 JLabel lblUsername = new JLabel("Username:"); styleFormLabel(lblUsername);
 gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
 add(lblUsername, gbc);
 styleTextField(txtUsername);
 gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
 add(txtUsername, gbc);

 // - - - Password-- -
 JLabel lblPassword = new JLabel("Password:"); styleFormLabel(lblPassword);
 gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
 add(lblPassword, gbc);
 styleTextField(txtPassword);
 gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
 add(txtPassword, gbc);

 // - - - Login Button-- -
 JButton btnLogin = new JButton("Login"); styleActionButton(btnLogin, "Authenticate and access your dashboard" );
 gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER; gbc.insets = new Insets(20, 5, 10, 5);
 add(btnLogin, gbc);

 // - - - Links Panel(Unchanged) - - -
 JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0) ); linksPanel.setOpaque(false);
 JLabel lblForgotPassword = createHyperlinkLabel("Forgot Password?"); lblForgotPassword.setToolTipText("Click here to reset your password");
 lblForgotPassword.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { JOptionPane.showMessageDialog(LoginPanel.this, "Password reset functionality not yet implemented.", "Forgot Password", JOptionPane.INFORMATIONMESSAGE); }});
 JLabel lblRegister = createHyperlinkLabel("Need an account? Register"); lblRegister.setToolTipText("Click here to go to the registration page");
 lblRegister.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { mainApp.navigateTo(OrphanageHubApp.REGISTRATIONPANEL); }});
 linksPanel.add(lblForgotPassword); linksPanel.add(lblRegister);
 gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.insets = new Insets(5, 5, 15, 5);
 add(linksPanel, gbc);

 // - - - Back Button(Unchanged) - - -
 JButton btnBack = new JButton("Back"); styleActionButton(btnBack, "Return to the welcome screen" ); btnBack.setBackground(BUTTONBG_DARK.darker();
 btnBack.addMouseListener(new MouseAdapter() { @Override public void mouseEntered(MouseEvent e) { btnBack.setBackground(BUTTONHOVER_BG_DARK); } @Override public void mouseExited(MouseEvent e) { btnBack.setBackground(BUTTONBG_DARK.darker(); }});
 btnBack.addActionListener(e -> mainApp.navigateTo(OrphanageHubApp.HOMEPANEL) );
 gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.insets = new Insets(10, 5, 5, 5);
 add(btnBack, gbc);

 // * ** REAL DATABASE-DRIVEN LOGIN LOGIC * **
 btnLogin.addActionListener(e -> performLogin();
 // Allow login on pressing Enter in password field
 txtPassword.addActionListener(e -> performLogin();
 }

 / **
 * Handles the login process by validating input and calling the backend service.
 * This method demonstrates separation of concerns and defensive programming.
 * PAT Rubric: 3.2, 3.3, 3.5, 3.6, 3.7
 * /
 private void performLogin() {
 // PAT 3.6: Defensive Programming - Trim input to handle whitespace
 String username = txtUsername.getText().trim();
 String password = new String(txtPassword.getPassword();

 try {
 // PAT 3.3 & 3.5: Call the backend service to authenticate against the database.
 // The UI does not know about SQL; it only knows about the AuthService.
 User user = authService.authenticate(username, password);

 // If authentication is successful, user object is returned.
 mainApp.setCurrentUser(user); // Store the logged-in user's data in the main app;

 String role = user.getUserRole();
 String targetPanel;

 // PAT 3.8(UX): Navigate to the correct dashboard based on the user's role from the DB.
 // This fulfills the program flow designed in Phase 2.
 switch(role) {
 case "Admin":
 targetPanel = OrphanageHubApp.ADMINDASHBOARDPANEL;
 break;
 case "OrphanageStaff":
 targetPanel = OrphanageHubApp.ORPHANAGEDASHBOARDPANEL;
 break;
 case "Donor":
 targetPanel = OrphanageHubApp.DONOR_DASHBOARDPANEL;
 break;
 case "Volunteer":
 targetPanel = OrphanageHubApp.VOLUNTEER_DASHBOARDPANEL;
 break;
 default:
 // This is a defensive catch-all for unexpected data.
 throw new ServiceException( "Unknown user role found in database: " + role);
 }

 mainApp.showDashboard(targetPanel);
 // Clear fields for security and usability when the user logs out and returns.
 txtUsername.setText(" ");
 txtPassword.setText(" ");

 } catch(ServiceException ex) {
 // PAT 3.6: Display a clear, user-friendly error message from the service layer.
 JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
 txtPassword.setText(" "); // Clear password field on failure;
 txtUsername.requestFocusInWindow(); // Set focus back to username;
 }
 }

 // - - - Styling Helper Methods(Unchanged) - - -
 private void styleFormLabel(JLabel label) { label.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 14) ); label.setForeground(TEXTCOLOR_DARK); }
 private void styleTextField(JComponent field) { field.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 14) ); field.setForeground(INPUTFG_DARK); field.setBackground(INPUTBG_DARK); Border p=new EmptyBorder(5,8,5,8); field.setBorder(new CompoundBorder(BorderFactory.createLineBorder(INPUTBORDER_DARK,1),p) ); if(field instanceof JTextField) ( (JTextField)field).setCaretColor(Color.LIGHTGRAY); else if(field instanceof JPasswordField) ( (JPasswordField)field).setCaretColor(Color.LIGHTGRAY); }
 private JLabel createHyperlinkLabel(String text) { JLabel l=new JLabel(" <html><u>"+text+" </u></html>"); l.setForeground(LINKCOLOR); l.setFont(new Font(Font.SANSSERIF,Font.PLAIN,12) ); l.setCursor(Cursor.getPredefinedCursor(Cursor.HANDCURSOR) ); return l; }
 private void styleActionButton(JButton btn, String tooltip) { btn.setFont(new Font(Font.SANSSERIF,Font.BOLD,14) ); btn.setPreferredSize(new Dimension(130,40) ); btn.setToolTipText(tooltip); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HANDCURSOR) ); btn.setBackground(BUTTONBG_DARK); btn.setForeground(BUTTONFG_DARK); btn.setFocusPainted(false); Border l=BorderFactory.createLineBorder(BUTTONBG_DARK.darker(); Border p=new EmptyBorder(5,15,5,15); btn.setBorder(new CompoundBorder(l,p) ); btn.addMouseListener(new MouseAdapter() { @Override public void mouseEntered(MouseEvent e){if(btn.getBackground().equals(BUTTONBG_DARK) ){btn.setBackground(BUTTONHOVER_BG_DARK);}} @Override public void mouseExited(MouseEvent e){if(btn.getBackground().equals(BUTTONHOVER_BG_DARK) ){btn.setBackground(BUTTONBG_DARK);}} }); }
))))))))
}