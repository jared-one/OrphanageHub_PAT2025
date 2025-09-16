package com.orphanagehub.gui;

import com.orphanagehub.model.User;
import com.orphanagehub.service.AuthService;
import com.orphanagehub.service.OrphanageService;
import com.orphanagehub.util.SessionManager;
import io.vavr.control.Try;
import io.vavr.collection.List;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class LoginPanel extends JPanel {
    private final OrphanageHubApp mainApp;
    private final AuthService authService;
    private final OrphanageService orphanageService;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    
    // Color constants
    private static final Color DARK_BG_START = new Color(45, 52, 54);
    private static final Color DARK_BG_END = new Color(35, 42, 44);
    private static final Color TITLE_COLOR_DARK = new Color(223, 230, 233);
    private static final Color TEXT_COLOR_DARK = new Color(200, 200, 200);
    private static final Color INPUT_BG_DARK = new Color(60, 60, 60);
    private static final Color INPUT_FG_DARK = new Color(220, 220, 220);
    private static final Color INPUT_BORDER_DARK = new Color(90, 90, 90);
    private static final Color BUTTON_BG_DARK = new Color(99, 110, 114);
    private static final Color BUTTON_FG_DARK = Color.WHITE;
    private static final Color BUTTON_HOVER_BG_DARK = new Color(120, 130, 134);
    private static final Color LINK_COLOR = new Color(100, 180, 255);

    public LoginPanel(OrphanageHubApp app) {
        this.mainApp = app;
        this.authService = new AuthService();
        this.orphanageService = new OrphanageService();
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(40, 60, 40, 60));
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
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title
        JLabel lblTitle = new JLabel("User Login", SwingConstants.CENTER);
        lblTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        lblTitle.setForeground(TITLE_COLOR_DARK);
        gbc.gridx = 0; 
        gbc.gridy = 0; 
        gbc.gridwidth = 2; 
        gbc.weightx = 1.0; 
        gbc.insets = new Insets(0, 5, 25, 5);
        add(lblTitle, gbc);
        
        gbc.gridwidth = 1; 
        gbc.insets = new Insets(8, 5, 8, 5);

        // Username
        JLabel lblUsername = new JLabel("Username:");
        styleFormLabel(lblUsername);
        gbc.gridx = 0; 
        gbc.gridy = 1; 
        gbc.anchor = GridBagConstraints.EAST; 
        gbc.fill = GridBagConstraints.NONE; 
        gbc.weightx = 0;
        add(lblUsername, gbc);
        
        txtUsername = new JTextField(20);
        styleTextField(txtUsername);
        gbc.gridx = 1; 
        gbc.gridy = 1; 
        gbc.anchor = GridBagConstraints.WEST; 
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.weightx = 1.0;
        add(txtUsername, gbc);

        // Password
        JLabel lblPassword = new JLabel("Password:");
        styleFormLabel(lblPassword);
        gbc.gridx = 0; 
        gbc.gridy = 2; 
        gbc.anchor = GridBagConstraints.EAST; 
        gbc.fill = GridBagConstraints.NONE; 
        gbc.weightx = 0;
        add(lblPassword, gbc);
        
        txtPassword = new JPasswordField(20);
        styleTextField(txtPassword);
        gbc.gridx = 1; 
        gbc.gridy = 2; 
        gbc.anchor = GridBagConstraints.WEST; 
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.weightx = 1.0;
        add(txtPassword, gbc);

        // Login Button
        JButton btnLogin = new JButton("Login");
        styleActionButton(btnLogin, "Authenticate and access your dashboard");
        gbc.gridx = 0; 
        gbc.gridy = 3; 
        gbc.gridwidth = 2; 
        gbc.fill = GridBagConstraints.NONE; 
        gbc.anchor = GridBagConstraints.CENTER; 
        gbc.insets = new Insets(20, 5, 10, 5);
        add(btnLogin, gbc);

        // Links Panel
        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        linksPanel.setOpaque(false);
        
        JLabel lblForgotPassword = createHyperlinkLabel("Forgot Password?");
        lblForgotPassword.setToolTipText("Click here to reset your password");
        lblForgotPassword.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(LoginPanel.this, 
                    "Password reset functionality coming soon.", 
                    "Forgot Password", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        JLabel lblRegister = createHyperlinkLabel("Need an account? Register");
        lblRegister.setToolTipText("Click here to go to the registration page");
        lblRegister.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainApp.navigateTo(OrphanageHubApp.REGISTRATION_PANEL);
            }
        });
        
        linksPanel.add(lblForgotPassword);
        linksPanel.add(lblRegister);
        gbc.gridx = 0; 
        gbc.gridy = 4; 
        gbc.gridwidth = 2; 
        gbc.anchor = GridBagConstraints.CENTER; 
        gbc.insets = new Insets(5, 5, 15, 5);
        add(linksPanel, gbc);

        // Back Button
        JButton btnBack = new JButton("Back");
        styleActionButton(btnBack, "Return to the welcome screen");
        btnBack.setBackground(BUTTON_BG_DARK.darker());
        btnBack.addMouseListener(new MouseAdapter() {
            @Override 
            public void mouseEntered(MouseEvent e) { 
                btnBack.setBackground(BUTTON_HOVER_BG_DARK); 
            }
            @Override 
            public void mouseExited(MouseEvent e) { 
                btnBack.setBackground(BUTTON_BG_DARK.darker()); 
            }
        });
        btnBack.addActionListener(e -> mainApp.navigateTo(OrphanageHubApp.HOME_PANEL));
        gbc.gridx = 0; 
        gbc.gridy = 5; 
        gbc.gridwidth = 2; 
        gbc.anchor = GridBagConstraints.CENTER; 
        gbc.insets = new Insets(10, 5, 5, 5);
        add(btnBack, gbc);

        // Login action with functional error handling
        btnLogin.addActionListener(e -> handleLogin());
    }
    
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        char[] password = txtPassword.getPassword();
        
        if (username.isEmpty() || password.length == 0) {
            showErrorMessage("Username and Password are required.");
            return;
        }
        
        // Use Try monad for functional error handling
        Try<User> authResult = authService.authenticate(username, new String(password));
        
        authResult
            .onSuccess(user -> {
                System.out.println("Login successful for user: " + user.username() + " with role: " + user.userRole());
                
                // Store user in session using constants
                SessionManager session = SessionManager.getInstance();
                session.setAttribute("currentUser", user);
                session.setAttribute(SessionManager.USER_ID, user.userId());
                session.setAttribute(SessionManager.USERNAME, user.username());
                session.setAttribute(SessionManager.USER_ROLE, user.userRole());
                
                // Multi-role support: Store as List (for future expansion)
                session.setAttribute(SessionManager.USER_ROLES, List.of(user.userRole()));
                session.setAttribute(SessionManager.IS_AUTHENTICATED, true);
                session.setAttribute(SessionManager.LOGIN_TIME, java.time.LocalDateTime.now());
                
                // Store full name and email if available
                user.fullName().forEach(name -> session.setAttribute(SessionManager.FULL_NAME, name));
                session.setAttribute(SessionManager.EMAIL, user.email());
                
                // Store orphanage ID if staff/rep
                if (List.of("OrphanageRep", "Staff", "OrphanageStaff").contains(user.userRole())) {
                    orphanageService.getOrphanageByUserId(user.userId())
                        .onSuccess(optOrphanage -> {
                            optOrphanage.peek(orphanage -> {
                                session.setAttribute(SessionManager.ORPHANAGE_ID, orphanage.orphanageId());
                                System.out.println("Set orphanage ID: " + orphanage.orphanageId());
                            });
                        })
                        .onFailure(ex -> {
                            System.err.println("Failed to load orphanage for staff user: " + ex.getMessage());
                        });
                }
                
                // Navigate to appropriate dashboard
                String target = getDashboardForRole(user.userRole());
                System.out.println("Navigating to dashboard: " + target);
                
                SwingUtilities.invokeLater(() -> {
                    clearPasswordField();
                    mainApp.showDashboard(target);
                });
            })
            .onFailure(ex -> {
                SwingUtilities.invokeLater(() -> {
                    showErrorMessage("Invalid credentials or authentication error: " + ex.getMessage());
                    clearPasswordField();
                    txtUsername.requestFocusInWindow();
                });
            });
        
        // Clear password array for security
        Arrays.fill(password, ' ');
    }
    
    /**
     * Maps database role to appropriate dashboard panel.
     * FIXED: Handles all DB role variants correctly
     */
    private String getDashboardForRole(String role) {
        System.out.println("getDashboardForRole: Mapping role '" + role + "'");
        
        return switch (role) {
            case "OrphanageRep", "Staff", "OrphanageStaff" -> {
                System.out.println("  -> Orphanage Dashboard");
                yield OrphanageHubApp.ORPHANAGE_DASHBOARD_PANEL;
            }
            case "Donor" -> {
                System.out.println("  -> Donor Dashboard");
                yield OrphanageHubApp.DONOR_DASHBOARD_PANEL;
            }
            case "Volunteer" -> {
                System.out.println("  -> Volunteer Dashboard");
                yield OrphanageHubApp.VOLUNTEER_DASHBOARD_PANEL;
            }
            case "Admin" -> {
                System.out.println("  -> Admin Dashboard");
                yield OrphanageHubApp.ADMIN_DASHBOARD_PANEL;
            }
            default -> {
                System.err.println("WARNING: Unknown role '" + role + "', defaulting to Donor Dashboard");
                yield OrphanageHubApp.DONOR_DASHBOARD_PANEL;
            }
        };
    }
    
    private void clearPasswordField() {
        txtPassword.setText("");
    }
    
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Login Error", JOptionPane.ERROR_MESSAGE);
    }

    private void styleFormLabel(JLabel label) {
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        label.setForeground(TEXT_COLOR_DARK);
    }
    
    private void styleTextField(JComponent field) {
        field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        field.setForeground(INPUT_FG_DARK);
        field.setBackground(INPUT_BG_DARK);
        Border padding = new EmptyBorder(5, 8, 5, 8);
        field.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(INPUT_BORDER_DARK, 1), 
            padding
        ));
        if (field instanceof JTextField) {
            ((JTextField) field).setCaretColor(Color.LIGHT_GRAY);
        } else if (field instanceof JPasswordField) {
            ((JPasswordField) field).setCaretColor(Color.LIGHT_GRAY);
        }
    }
    
    private JLabel createHyperlinkLabel(String text) {
        JLabel label = new JLabel("<html><u>" + text + "</u></html>");
        label.setForeground(LINK_COLOR);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return label;
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
            public void mouseEntered(MouseEvent e) {
                if (btn.getBackground().equals(BUTTON_BG_DARK)) {
                    btn.setBackground(BUTTON_HOVER_BG_DARK);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.getBackground().equals(BUTTON_HOVER_BG_DARK)) {
                    btn.setBackground(BUTTON_BG_DARK);
                }
            }
        });
    }
}