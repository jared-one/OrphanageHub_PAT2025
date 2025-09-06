// src/main/java/com/orphanagehub/gui/HomePanel.java
package com.orphanagehub.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.function.Consumer;

public class HomePanel extends JPanel {
    private final OrphanageHubApp mainApp;
    private final JRadioButton radDonor;
    private final JRadioButton radOrphanageStaff;
    private final JRadioButton radVolunteer;
    private final JRadioButton radAdmin;
    private final ButtonGroup roleGroup;
    
    // Define colors locally for better encapsulation
    private static final Color DARK_BG_START = new Color(45, 52, 54);
    private static final Color DARK_BG_END = new Color(35, 42, 44);
    private static final Color TITLE_COLOR_DARK = new Color(223, 230, 233);
    private static final Color TEXT_COLOR_DARK = new Color(200, 200, 200);
    private static final Color BORDER_COLOR_DARK = new Color(80, 80, 80);
    private static final Color BUTTON_BG_DARK = new Color(99, 110, 114);
    private static final Color BUTTON_FG_DARK = Color.WHITE;
    private static final Color BUTTON_HOVER_BG_DARK = new Color(120, 130, 134);
    private static final Color FALLBACK_BG_DARK = new Color(60, 60, 60);

    public HomePanel(OrphanageHubApp app) {
        this.mainApp = app;
        setBorder(new EmptyBorder(30, 40, 30, 40));
        setLayout(new BorderLayout(20, 20));
        
        // Initialize radio buttons as final
        this.radDonor = new JRadioButton("Donor", true);
        this.radOrphanageStaff = new JRadioButton("Orphanage Staff");
        this.radVolunteer = new JRadioButton("Volunteer");
        this.radAdmin = new JRadioButton("Admin");
        this.roleGroup = new ButtonGroup();
        
        initComponents();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        GradientPaint gp = new GradientPaint(0, 0, DARK_BG_START, 0, getHeight(), DARK_BG_END);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void initComponents() {
        // Title
        JLabel lblTitle = new JLabel("Welcome to OrphanageHub", SwingConstants.CENTER);
        lblTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        lblTitle.setForeground(TITLE_COLOR_DARK);
        lblTitle.setBorder(new EmptyBorder(0, 0, 25, 0));
        add(lblTitle, BorderLayout.NORTH);
        
        // Center Panel with Image and Description
        JPanel centerPanel = new JPanel(new BorderLayout(30, 0));
        centerPanel.setOpaque(false);
        
        // Image Label
        JLabel lblImage = createImageLabel();
        centerPanel.add(lblImage, BorderLayout.WEST);
        
        // Description
        String htmlDesc = "<html><body style='width:350px; font-family: Sans-Serif; font-size: 14pt; color: rgb(200,200,200);'>"
                + "<p><b>A better world starts with care.</b></p>"
                + "<p>OrphanageHub connects orphanages with the donors and volunteers needed "
                + "to create lasting change for vulnerable children.</p>"
                + "</body></html>";
        JLabel lblDesc = new JLabel(htmlDesc);
        lblDesc.setVerticalAlignment(SwingConstants.CENTER);
        lblDesc.setHorizontalAlignment(SwingConstants.LEFT);
        lblDesc.setOpaque(false);
        lblDesc.setBorder(new EmptyBorder(0, 10, 0, 0));
        centerPanel.add(lblDesc, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // South Panel with Role Selection and Buttons
        JPanel southPanel = new JPanel(new BorderLayout(10, 20));
        southPanel.setOpaque(false);
        
        // Role Panel
        JPanel rolePanel = createRolePanel();
        southPanel.add(rolePanel, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = createButtonPanel();
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(southPanel, BorderLayout.SOUTH);
    }

    private JLabel createImageLabel() {
        JLabel lblImage = new JLabel();
        Dimension imageSize = new Dimension(220, 220);
        lblImage.setPreferredSize(imageSize);
        lblImage.setMinimumSize(imageSize);
        lblImage.setMaximumSize(imageSize);
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblImage.setVerticalAlignment(SwingConstants.CENTER);
        lblImage.setOpaque(false);
        
        URL imageURL = getClass().getResource("home.png");
        if (imageURL != null) {
            ImageIcon icon = new ImageIcon(imageURL);
            if (icon.getImageLoadStatus() == MediaTracker.COMPLETE && icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(imageSize.width, imageSize.height, Image.SCALE_SMOOTH);
                lblImage.setIcon(new ImageIcon(img));
                lblImage.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK, 1));
            } else {
                setFallbackImageStyleDark(lblImage);
            }
        } else {
            setFallbackImageStyleDark(lblImage);
        }
        return lblImage;
    }

    private JPanel createRolePanel() {
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        rolePanel.setOpaque(false);
        
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR_DARK),
                " Select Your Role ",
                TitledBorder.CENTER,
                TitledBorder.DEFAULT_POSITION,
                new Font(Font.SANS_SERIF, Font.BOLD, 15),
                TITLE_COLOR_DARK
        );
        rolePanel.setBorder(new CompoundBorder(titledBorder, new EmptyBorder(10, 10, 10, 10)));
        
        // Style radio buttons
        styleRadioButton(radDonor, "Select if you wish to donate or view needs.");
        styleRadioButton(radOrphanageStaff, "Select if you manage an orphanage profile.");
        styleRadioButton(radVolunteer, "Select if you want to find volunteer opportunities.");
        styleRadioButton(radAdmin, "Select if you are an administrator.");
        
        // Add to button group
        roleGroup.add(radDonor);
        roleGroup.add(radOrphanageStaff);
        roleGroup.add(radVolunteer);
        roleGroup.add(radAdmin);
        
        // Add role change listener using functional approach
        Consumer<JRadioButton> roleListener = button -> {
            if (button.isSelected()) {
                mainApp.setLastSelectedRole(getRoleFromButton(button));
            }
        };
        
        radDonor.addActionListener(e -> roleListener.accept(radDonor));
        radOrphanageStaff.addActionListener(e -> roleListener.accept(radOrphanageStaff));
        radVolunteer.addActionListener(e -> roleListener.accept(radVolunteer));
        radAdmin.addActionListener(e -> roleListener.accept(radAdmin));
        
        // Add to panel
        rolePanel.add(radDonor);
        rolePanel.add(radOrphanageStaff);
        rolePanel.add(radVolunteer);
        rolePanel.add(radAdmin);
        
        return rolePanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 10, 0));
        
        JButton btnLogin = new JButton("Login");
        JButton btnRegister = new JButton("Register");
        
        styleActionButton(btnLogin, "Proceed to login with your existing account.");
        styleActionButton(btnRegister, "Create a new account based on your selected role.");
        
        btnLogin.addActionListener(e -> mainApp.navigateTo(OrphanageHubApp.LOGIN_PANEL));
        btnRegister.addActionListener(e -> mainApp.navigateTo(OrphanageHubApp.REGISTRATION_PANEL));
        
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegister);
        
        return buttonPanel;
    }

    private void setFallbackImageStyleDark(JLabel label) {
        label.setText("<html><div style='text-align: center; color: #AAAAAA;'>Image<br>Not Found<br>(home.png)</div></html>");
        label.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 14));
        label.setForeground(new Color(170, 170, 170));
        label.setBorder(BorderFactory.createDashedBorder(BORDER_COLOR_DARK, 5, 5));
        label.setOpaque(true);
        label.setBackground(FALLBACK_BG_DARK);
    }

    private void styleRadioButton(JRadioButton rb, String tooltip) {
        rb.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        rb.setForeground(TEXT_COLOR_DARK);
        rb.setOpaque(false);
        rb.setToolTipText(tooltip);
        rb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleActionButton(JButton btn, String tooltip) {
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(130, 40));
        btn.setToolTipText(tooltip);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(BUTTON_BG_DARK);
        btn.setForeground(BUTTON_FG_DARK);
        btn.setFocusPainted(false);
        
        Border line = BorderFactory.createLineBorder(BUTTON_BG_DARK.darker());
        Border padding = new EmptyBorder(5, 15, 5, 15);
        btn.setBorder(new CompoundBorder(line, padding));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                btn.setBackground(BUTTON_HOVER_BG_DARK);
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                btn.setBackground(BUTTON_BG_DARK);
            }
        });
    }

    private String getRoleFromButton(JRadioButton button) {
        if (button == radDonor) return "Donor";
        if (button == radOrphanageStaff) return "OrphanageStaff";
        if (button == radVolunteer) return "Volunteer";
        if (button == radAdmin) return "Admin";
        return "Unknown";
    }

    public String getSelectedRole() {
        if (radDonor.isSelected()) return "Donor";
        if (radOrphanageStaff.isSelected()) return "OrphanageStaff";
        if (radVolunteer.isSelected()) return "Volunteer";
        if (radAdmin.isSelected()) return "Admin";
        return "Unknown";
    }
}