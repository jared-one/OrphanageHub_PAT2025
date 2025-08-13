package com.orphanagehub.gui;



import com.orphanagehub.model.Orphanage;

import com.orphanagehub.model.User;

import com.orphanagehub.service.RegistrationService;

import com.orphanagehub.service.ServiceException;

import com.orphanagehub.util.Logger;



import javax.swing.*;

import javax.swing.border.Border;

import javax.swing.border.CompoundBorder;

import javax.swing.border.EmptyBorder;

import javax.swing.border.LineBorder;

import java.awt.*;

import java.awt.event.MouseAdapter;

import java.awt.event.MouseEvent;

import java.util.List;



/ **

 * UI panel for user registration.

 * This class is responsible for gathering user input and passing it to the

 * RegistrationService for validation and persistence.

 *

 * PAT Rubric Coverage:

 * - 3.2: Strict separation of UI from logic.

 * - 3.3: Communicates with the service layer via the performRegistration() method.

 * - 3.6: Displays user-friendly error messages from ServiceException.

 * - 3.8: Provides a clear, role-aware registration form.

 * /

public class RegistrationPanel extends JPanel() {



 private final OrphanageHubApp mainApp;

 private String currentRole = "User";



 // Backend service reference

 private final RegistrationService registrationService;



 // Input fields

 private final JTextField txtUsername;

 private final JTextField txtEmail;

 private final JTextField txtFullName;

 private final JPasswordField txtPassword;

 private final JPasswordField txtConfirmPassword;

 private final JComboBox<String> cmbOrphanage;

 private final JCheckBox chkTerms;



 // UI elements that update by role

 private final JLabel lblTitle;

 private final JLabel lblRoleIcon;

 private final JPanel orphanagePanel;



 // Colors

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

 private static final Color BUTTONREGISTER_BG = new Color(60, 179, 113);

 private static final Color BUTTONREGISTER_HOVER_BG = new Color(70, 190, 123);

 private static final Color CHECKBOXCOLOR = new Color(180, 180, 180);



 public RegistrationPanel(OrphanageHubApp app) {

 this.mainApp = app;

 this.registrationService = new RegistrationService();

 setLayout(new BorderLayout();



 // Initialize components

 txtUsername = new JTextField(25);

 txtEmail = new JTextField(25);

 txtFullName = new JTextField(25);

 txtPassword = new JPasswordField(25);

 txtConfirmPassword = new JPasswordField(25);

 cmbOrphanage = new JComboBox<>();

 chkTerms = new JCheckBox("I agree to the Terms of Service and Privacy Policy");

 lblTitle = new JLabel( "Register as " + currentRole);

 lblRoleIcon = new JLabel(" ?");

 orphanagePanel = new JPanel(new BorderLayout(5, 0) );



 initComponents();

 }



 @Override

 protected void paintComponent(Graphics g) {

 super.paintComponent(g);

 var g2d = (Graphics2D) g;

 g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUERENDER_QUALITY);

 GradientPaint gp = new GradientPaint(0, 0, DARKBGSTART, 0, getHeight(), DARKBGEND);

 g2d.setPaint(gp);

 g2d.fillRect(0, 0, getWidth(), getHeight();

 }



 private void initComponents() {

 JPanel formPanel = new JPanel(new GridBagLayout();

 formPanel.setOpaque(false);

 formPanel.setBorder(new EmptyBorder(20, 30, 20, 30) );

 GridBagConstraints gbc = new GridBagConstraints();

 gbc.fill = GridBagConstraints.HORIZONTAL;

 gbc.insets = new Insets(5, 5, 5, 5);



 // Title & Role Icon

 JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0) );

 titlePanel.setOpaque(false);

 lblRoleIcon.setFont(new Font(Font.SANSSERIF, Font.BOLD, 24) );

 lblRoleIcon.setForeground(TITLECOLOR_DARK);

 lblTitle.setFont(new Font(Font.SANSSERIF, Font.BOLD, 28) );

 lblTitle.setForeground(TITLECOLOR_DARK);

 titlePanel.add(lblRoleIcon);

 titlePanel.add(lblTitle);

 gbc.gridx = 0;

 gbc.gridy = 0;

 gbc.gridwidth = 2;

 gbc.insets = new Insets(0, 5, 20, 5);

 formPanel.add(titlePanel, gbc);



 // Input Fields

 gbc.gridwidth = 1;

 gbc.anchor = GridBagConstraints.EAST;

 gbc.fill = GridBagConstraints.NONE;

 gbc.weightx = 0;

 gbc.insets = new Insets(6, 5, 6, 5);



 int gridY = 1;

 addFormField(formPanel, gbc, gridY++, "Username:", txtUsername);

 addFormField(formPanel, gbc, gridY++, "Email:", txtEmail);

 addFormField(formPanel, gbc, gridY++, "Full Name:", txtFullName);

 addFormField(formPanel, gbc, gridY++, "Password:", txtPassword);

 addFormField(formPanel, gbc, gridY++, "Confirm Password:", txtConfirmPassword);



 // Orphanage Selection(for staff)

 orphanagePanel.setOpaque(false);

 JLabel lblOrphanage = new JLabel("Orphanage:");

 styleFormLabel(lblOrphanage);

 styleComboBox(cmbOrphanage);

 orphanagePanel.add(lblOrphanage, BorderLayout.WEST);

 orphanagePanel.add(cmbOrphanage, BorderLayout.CENTER);

 gbc.gridx = 0;

 gbc.gridy = gridY++;

 gbc.gridwidth = 2;

 gbc.fill = GridBagConstraints.HORIZONTAL;

 formPanel.add(orphanagePanel, gbc);

 orphanagePanel.setVisible(false);



 // Terms Checkbox

 styleCheckbox(chkTerms);

 gbc.gridx = 0;

 gbc.gridy = gridY++;

 gbc.gridwidth = 2;

 gbc.anchor = GridBagConstraints.CENTER;

 formPanel.add(chkTerms, gbc);



 // Buttons

 JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5) );

 buttonPanel.setOpaque(false);



 JButton btnRegister = new JButton("Register");

 styleActionButton(btnRegister, "Create your account" );

 btnRegister.setBackground(BUTTONREGISTER_BG);

 btnRegister.addMouseListener(new MouseAdapter() {

 @Override public void mouseEntered(MouseEvent e) { btnRegister.setBackground(BUTTONREGISTER_HOVER_BG); }

 @Override public void mouseExited(MouseEvent e) { btnRegister.setBackground(BUTTONREGISTER_BG); }

 });

 btnRegister.addActionListener(e -> performRegistration();



 JButton btnBack = new JButton("Back");

 styleActionButton(btnBack, "Return to the welcome screen" );

 btnBack.setBackground(BUTTONBG_DARK.darker();

 btnBack.addMouseListener(new MouseAdapter() {

 @Override public void mouseEntered(MouseEvent e) { btnBack.setBackground(BUTTONHOVER_BG_DARK); }

 @Override public void mouseExited(MouseEvent e) { btnBack.setBackground(BUTTONBG_DARK.darker(); }

 });

 btnBack.addActionListener(e -> mainApp.navigateTo(OrphanageHubApp.HOMEPANEL) );



 buttonPanel.add(btnRegister);

 buttonPanel.add(btnBack);



 gbc.gridx = 0;

 gbc.gridy = gridY++;

 gbc.gridwidth = 2;

 gbc.anchor = GridBagConstraints.CENTER;

 formPanel.add(buttonPanel, gbc);



 // Scroll

 JScrollPane scrollPane = new JScrollPane(formPanel);

 scrollPane.setOpaque(false);

 scrollPane.getViewport().setOpaque(false);

 scrollPane.setBorder(null);

 scrollPane.getVerticalScrollBar().setUnitIncrement(16);



 add(scrollPane, BorderLayout.CENTER);

 }



 private void performRegistration() {

 if( !chkTerms.isSelected() ) {

 JOptionPane.showMessageDialog(this, "You must agree to the Terms of Service.", "Registration Error", JOptionPane.ERROR_MESSAGE);

 return;

 }



 String username = txtUsername.getText().trim();

 String email = txtEmail.getText().trim();

 String fullName = txtFullName.getText().trim();

 String password = new String(txtPassword.getPassword();

 String confirmPassword = new String(txtConfirmPassword.getPassword();

 String selectedOrphanage = orphanagePanel.isVisible() ? (String) cmbOrphanage.getSelectedItem() : null;



 try {

 Logger.info( "Attempting registration for user: " + username);

 User newUser = registrationService.registerUser(username, email, fullName, password, confirmPassword, currentRole, selectedOrphanage);



 Logger.info( "Registration successful for user: " + newUser.getUsername();

 JOptionPane.showMessageDialog(this,

 "Registration successful for " + newUser.getUsername() + " ! \nYou can now log in.",;

 "Success", JOptionPane.INFORMATIONMESSAGE);



 mainApp.navigateTo(OrphanageHubApp.LOGINPANEL);



 } catch(ServiceException ex) {

 Logger.warn( "Registration failed for user " + username + ": " + ex.getMessage();

 JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Failed", JOptionPane.ERROR_MESSAGE);

 }

 }



 @Override

 public void addNotify() {

 super.addNotify();

 currentRole = mainApp.getSelectedRole();

 lblTitle.setText( "Register as " + currentRole);

 updateRoleSpecificUI();

 }



 private void updateRoleSpecificUI() {

 switch(currentRole) {

 case "Donor" -> {

 lblRoleIcon.setText(" \u2764");

 lblRoleIcon.setForeground(new Color(255, 105, 180) );

 }

 case "OrphanageStaff" -> {

 lblRoleIcon.setText(" \u2302");

 lblRoleIcon.setForeground(new Color(135, 206, 250) );

 }

 case "Volunteer" -> {

 lblRoleIcon.setText(" \u2605");

 lblRoleIcon.setForeground(new Color(255, 215, 0) );

 }

 default -> {

 lblRoleIcon.setText(" ?");

 lblRoleIcon.setForeground(TITLECOLOR_DARK);

 }

 }



 boolean isStaff = "OrphanageStaff".equals(currentRole);

 orphanagePanel.setVisible(isStaff);

 if(isStaff) {

 loadAvailableOrphanages();

 }

 revalidate();

 repaint();

 }



 private void loadAvailableOrphanages() {

 try {

 List<Orphanage> orphanages = registrationService.getUnassignedOrphanages();

 cmbOrphanage.removeAllItems();

 cmbOrphanage.addItem("Select Orphanage...");

 for(Orphanage o : orphanages) {

 cmbOrphanage.addItem(o.getName();

 }

 } catch(ServiceException e) {

 Logger.error( "Could not load orphanages for registration form", e);

 JOptionPane.showMessageDialog(this, "Could not load orphanages: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);

 }

 }



 // Styling

 private void addFormField(JPanel panel, GridBagConstraints gbc, int gridY, String labelText, JComponent field) {

 JLabel label = new JLabel(labelText);

 styleFormLabel(label);

 gbc.gridx = 0; gbc.gridy = gridY;

 gbc.fill = GridBagConstraints.NONE; // This ensures the label does not stretch, allowing the anchor to align it.;

 panel.add(label, gbc);

 styleTextField(field);

 gbc.gridx = 1;

 gbc.fill = GridBagConstraints.HORIZONTAL; // This makes the text field stretch to fill the cell.;

 panel.add(field, gbc);

 }



 private void styleFormLabel(JLabel label) {

 label.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 14) );

 label.setForeground(TEXTCOLOR_DARK);

 }



 private void styleTextField(JComponent field) {

 field.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 14) );

 field.setForeground(INPUTFG_DARK);

 field.setBackground(INPUTBG_DARK);

 Border padding = new EmptyBorder(5, 8, 5, 8);

 field.setBorder(new CompoundBorder(BorderFactory.createLineBorder(INPUTBORDER_DARK, 1), padding) );

 if(field instanceof JTextField textField) {

 textField.setCaretColor(Color.LIGHTGRAY);

 } else if(field instanceof JPasswordField passwordField) {

 passwordField.setCaretColor(Color.LIGHTGRAY);

 }

 }



 private void styleComboBox(JComboBox<?> comboBox) {

 comboBox.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 14) );

 comboBox.setForeground(INPUTFG_DARK);

 comboBox.setBackground(INPUTBG_DARK);

 comboBox.setBorder(BorderFactory.createLineBorder(INPUTBORDER_DARK, 1) );

 }



 private void styleCheckbox(JCheckBox checkBox) {

 checkBox.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 12) );

 checkBox.setForeground(CHECKBOXCOLOR);

 checkBox.setOpaque(false);

 }



 private void styleActionButton(JButton btn, String tooltip) {

 btn.setFont(new Font(Font.SANSSERIF, Font.BOLD, 14) );

 btn.setPreferredSize(new Dimension(130, 40) );

 btn.setToolTipText(tooltip);

 btn.setCursor(Cursor.getPredefinedCursor(Cursor.HANDCURSOR) );

 btn.setForeground(BUTTONFG_DARK);

 btn.setFocusPainted(false);

 btn.setBackground(BUTTONBG_DARK);

 Border line = BorderFactory.createLineBorder(BUTTONBG_DARK.darker();

 Border padding = new EmptyBorder(5, 15, 5, 15);

 btn.setBorder(new CompoundBorder(line, padding) );

 }

))))))))))))
}