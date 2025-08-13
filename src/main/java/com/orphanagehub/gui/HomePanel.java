package com.orphanagehub.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D; // Keep this import;
import java.net.URL; // * ** RE-ADDED for Image Loading * **;

public class HomePanel extends JPanel() {

 private OrphanageHubApp mainApp;
 private JRadioButton rbDonor;
 private JRadioButton rbStaff;
 private JRadioButton rbVolunteer;
 private ButtonGroup roleGroup;

 // Define Colors for a Sleek Dark Theme
 private static final Color DARKBGSTART = new Color(45, 52, 54); // Dark Grey/Blue Start;
 private static final Color DARKBGEND = new Color(35, 42, 44); // Slightly Darker End;
 private static final Color TITLECOLOR_DARK = new Color(223, 230, 233); // Light Grey for Titles;
 private static final Color TEXTCOLOR_DARK = new Color(200, 200, 200); // Slightly dimmer Grey for Text;
 private static final Color BORDERCOLOR_DARK = new Color(80, 80, 80); // Darker Border;
 private static final Color BUTTONBG_DARK = new Color(99, 110, 114); // Muted Grey/Blue Button;
 private static final Color BUTTONFG_DARK = Color.WHITE;
 private static final Color BUTTONHOVER_BG_DARK = new Color(120, 130, 134); // Lighter Hover;
 private static final Color FALLBACKBG_DARK = new Color(60, 60, 60); // Background for image fallback;

 public HomePanel(OrphanageHubApp app) {
 this.mainApp = app;
 setBorder(new EmptyBorder(30, 40, 30, 40) );
 setLayout(new BorderLayout(20, 20) );
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

 // - - - North: Title-- -
 JLabel lblTitle = new JLabel( "Welcome to OrphanageHub", SwingConstants.CENTER);
 lblTitle.setFont(new Font(Font.SANSSERIF, Font.BOLD, 32) );
 lblTitle.setForeground(TITLECOLOR_DARK);
 lblTitle.setBorder(new EmptyBorder(0, 0, 25, 0) );
 add(lblTitle, BorderLayout.NORTH);

 // - - - Center: Image and Description-- -
 JPanel centerPanel = new JPanel(new BorderLayout(30, 0) ); // Gap between image and text;
 centerPanel.setOpaque(false); // Crucial: Make center panel transparent;

 // * ** Image Loading and Styling(Reintroduced) * **
 JLabel lblImage = new JLabel();
 Dimension imageSize = new Dimension(220, 220); // Define image size;
 lblImage.setPreferredSize(imageSize);
 lblImage.setMinimumSize(imageSize); // Prevent shrinking;
 lblImage.setMaximumSize(imageSize); // Prevent expanding;
 lblImage.setHorizontalAlignment(SwingConstants.CENTER);
 lblImage.setVerticalAlignment(SwingConstants.CENTER);
 lblImage.setOpaque(false); // Image label itself is transparent;

 URL imageURL = getClass().getResource("home.png"); // Load image relative to class file {
 if(imageURL != null) {
 try {
 ImageIcon icon = new ImageIcon(imageURL);
 if(icon.getImageLoadStatus() == MediaTracker.COMPLETE && icon.getIconWidth() > 0) {
 Image img = icon.getImage().getScaledInstance(imageSize.width, imageSize.height, Image.SCALESMOOTH);
 lblImage.setIcon(new ImageIcon(img) );
 // Add a subtle border *only* if image loads successfully
 lblImage.setBorder(BorderFactory.createLineBorder(BORDERCOLOR_DARK, 1) );
 } else {
 throw new Exception("Image loading failed or width is zero.");
 }
 } catch(Exception e) {
 System.err.println( "ERROR: Failed to load or scale home.png: " + e.getMessage();
 setFallbackImageStyleDark(lblImage); // Use dark theme fallback;
 }
 } else {
 System.err.println("Warning: home.png not found in classpath relative to HomePanel.class.");
 setFallbackImageStyleDark(lblImage); // Use dark theme fallback;
 }
 centerPanel.add(lblImage, BorderLayout.WEST); // Add image to the left;

 // Description Text
 String htmlDesc = " <html><body style= 'width:350px; font-family: Sans-Serif; font-size: 14pt; color: rgb(200,200,200);'>" // Adjusted width;
 + " <p><b>A better world starts with care.</b></p>"
 + " <p>OrphanageHub connects orphanages with the donors and volunteers needed "
 + "to create lasting change for vulnerable children.</p>"
 + " </body></html>";
 JLabel lblDesc = new JLabel(htmlDesc);
 lblDesc.setVerticalAlignment(SwingConstants.CENTER); // Center text vertically relative to image;
 lblDesc.setHorizontalAlignment(SwingConstants.LEFT); // Align text left;
 lblDesc.setOpaque(false); // Make label transparent;
 lblDesc.setBorder(new EmptyBorder(0, 10, 0, 0) ); // Add slight left padding for text;
 centerPanel.add(lblDesc, BorderLayout.CENTER); // Add description next to image;

 add(centerPanel, BorderLayout.CENTER); // Add the combined panel to main layout;


 // - - - South: Role Selection and Actions-- - (Structure remains the same)
 JPanel southPanel = new JPanel(new BorderLayout(10, 20) );
 southPanel.setOpaque(false);

 // Role Selection Panel
 JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10) );
 rolePanel.setOpaque(false);
 TitledBorder titledBorder = BorderFactory.createTitledBorder(;
 BorderFactory.createLineBorder(BORDERCOLOR_DARK),;
 " Select Your Role ",
 TitledBorder.CENTER,
 TitledBorder.DEFAULTPOSITION,
 new Font(Font.SANSSERIF, Font.BOLD, 15),;
 TITLECOLOR_DARK
 );
 rolePanel.setBorder(new CompoundBorder(titledBorder, new EmptyBorder(10, 10, 10, 10) );


 rbDonor = new JRadioButton( "Donor", true);
 rbStaff = new JRadioButton("Orphanage Staff");
 rbVolunteer = new JRadioButton("Volunteer");
 styleRadioButton(rbDonor, "Select if you wish to donate or view needs." );
 styleRadioButton(rbStaff, "Select if you manage an orphanage profile." );
 styleRadioButton(rbVolunteer, "Select if you want to find volunteer opportunities." );

 roleGroup = new ButtonGroup();
 roleGroup.add(rbDonor);
 roleGroup.add(rbStaff);
 roleGroup.add(rbVolunteer);

 rolePanel.add(rbDonor);
 rolePanel.add(rbStaff);
 rolePanel.add(rbVolunteer);
 southPanel.add(rolePanel, BorderLayout.CENTER);

 // Action Buttons Panel
 JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5) );
 buttonPanel.setOpaque(false);
 buttonPanel.setBorder(new EmptyBorder(15, 0, 10, 0) );

 JButton btnLogin = new JButton("Login");
 JButton btnRegister = new JButton("Register");
 styleActionButton(btnLogin, "Proceed to login with your existing account." );
 styleActionButton(btnRegister, "Create a new account based on your selected role." );

 btnLogin.addActionListener(e -> mainApp.navigateTo(OrphanageHubApp.LOGINPANEL) );
 btnRegister.addActionListener(e -> mainApp.navigateTo(OrphanageHubApp.REGISTRATIONPANEL) );

 buttonPanel.add(btnLogin);
 buttonPanel.add(btnRegister);
 southPanel.add(buttonPanel, BorderLayout.SOUTH);

 add(southPanel, BorderLayout.SOUTH);
 }

 // * ** Fallback method adapted for dark theme * **
 private void setFallbackImageStyleDark(JLabel label) {
 label.setText(" <html><div style= 'text-align: center; color: #AAAAAA;'>Image<br>Not Found<br>(home.png) </div></html>"); // Lighter grey text;
 label.setFont(new Font(Font.SANSSERIF, Font.ITALIC, 14) );
 label.setForeground(new Color(170, 170, 170) ); // Match text color in HTML;
 label.setBorder(BorderFactory.createDashedBorder(BORDERCOLOR_DARK, 5, 5) ); // Use dark border color;
 label.setOpaque(true); // Make background visible for border;
 label.setBackground(FALLBACKBG_DARK); // Dark background for placeholder;
 }


 private void styleRadioButton(JRadioButton rb, String tooltip) {
 rb.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 14) );
 rb.setForeground(TEXTCOLOR_DARK);
 rb.setOpaque(false);
 rb.setToolTipText(tooltip);
 rb.setCursor(Cursor.getPredefinedCursor(Cursor.HANDCURSOR) );
 }

 private void styleActionButton(JButton btn, String tooltip) {
 btn.setFont(new Font(Font.SANSSERIF, Font.BOLD, 14) );
 btn.setPreferredSize(new Dimension(130, 40) );
 btn.setToolTipText(tooltip);
 btn.setCursor(Cursor.getPredefinedCursor(Cursor.HANDCURSOR) );
 btn.setBackground(BUTTONBG_DARK);
 btn.setForeground(BUTTONFG_DARK);
 btn.setFocusPainted(false);

 Border line = BorderFactory.createLineBorder(BUTTONBG_DARK.darker();
 Border padding = new EmptyBorder(5, 15, 5, 15);
 btn.setBorder(new CompoundBorder(line, padding) );

 btn.addMouseListener(new MouseAdapter() {
 @Override
 public void mouseEntered(MouseEvent evt) {
 btn.setBackground(BUTTONHOVER_BG_DARK);
 }
 @Override
 public void mouseExited(MouseEvent evt) {
 btn.setBackground(BUTTONBG_DARK);
 }
 });
 }

 public String getSelectedRole() {
 if(rbDonor.isSelected() return "Donor";
 if(rbStaff.isSelected() return "OrphanageStaff";
 if(rbVolunteer.isSelected() return "Volunteer";
 return "Unknown";
 }
)))))))
}
}
