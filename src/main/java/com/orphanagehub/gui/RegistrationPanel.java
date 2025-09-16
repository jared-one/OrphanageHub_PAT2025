package com.orphanagehub.gui;

import com.orphanagehub.service.RegistrationService;
import com.orphanagehub.service.OrphanageService;
import com.orphanagehub.util.ValidationUtil;
import io.vavr.control.Try;
import io.vavr.control.Option;
import io.vavr.collection.List;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class RegistrationPanel extends JPanel {
    private final OrphanageHubApp mainApp;
    private final RegistrationService registrationService;
    private final OrphanageService orphanageService;
    private String currentRole = "Donor";
    
    // Form fields
    private JTextField txtUsername;
    private JTextField txtEmail;
    private JTextField txtFullName;
    private JTextField txtPhone;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JComboBox<String> cmbProvince;
    private JComboBox<String> cmbOrphanage;
    private JCheckBox chkTerms;
    private JCheckBox chkNewsletter;
    
    // UI components
    private JLabel lblTitle;
    private JLabel lblRoleIcon;
    private JPanel orphanagePanel;
    private JPanel phonePanel;
    private JPanel provincePanel;
    
    // Color constants
    private static final Color DARK_BG_START = new Color(45, 52, 54);
    private static final Color DARK_BG_END = new Color(35, 42, 44);
    private static final Color TITLE_COLOR_DARK = new Color(223, 230, 233);
    private static final Color TEXT_COLOR_DARK = new Color(200, 200, 200);
    private static final Color BORDER_COLOR_DARK = new Color(80, 80, 80);
    private static final Color INPUT_BG_DARK = new Color(60, 60, 60);
    private static final Color INPUT_FG_DARK = new Color(220, 220, 220);
    private static final Color INPUT_BORDER_DARK = new Color(90, 90, 90);
    private static final Color BUTTON_BG_DARK = new Color(99, 110, 114);
    private static final Color BUTTON_FG_DARK = Color.WHITE;
    private static final Color BUTTON_HOVER_BG_DARK = new Color(120, 130, 134);
    private static final Color BUTTON_REGISTER_BG = new Color(60, 179, 113);
    private static final Color BUTTON_REGISTER_HOVER_BG = new Color(70, 190, 123);
    private static final Color CHECKBOX_COLOR = new Color(180, 180, 180);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);

    public RegistrationPanel(OrphanageHubApp app) {
        this.mainApp = app;
        this.registrationService = new RegistrationService();
        this.orphanageService = new OrphanageService();
        setLayout(new BorderLayout());
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
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title Panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        titlePanel.setOpaque(false);
        
        lblRoleIcon = new JLabel("?");
        lblRoleIcon.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        lblRoleIcon.setForeground(TITLE_COLOR_DARK);
        titlePanel.add(lblRoleIcon);
        
        lblTitle = new JLabel("Register as " + getDisplayRoleName(currentRole));
        lblTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        lblTitle.setForeground(TITLE_COLOR_DARK);
        titlePanel.add(lblTitle);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 5, 20, 5);
        formPanel.add(titlePanel, gbc);
        
        // Reset constraints for form fields
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(6, 5, 6, 5);
        
        int gridY = 1;
        
        // Core fields
        addFormField(formPanel, gbc, gridY++, "Username:", txtUsername = new JTextField(25));
        addFormField(formPanel, gbc, gridY++, "Email:", txtEmail = new JTextField(25));
        addFormField(formPanel, gbc, gridY++, "Full Name:", txtFullName = new JTextField(25));
        
        // Phone Number
        phonePanel = new JPanel(new BorderLayout(5, 0));
        phonePanel.setOpaque(false);
        JLabel lblPhone = new JLabel("Phone Number:");
        styleFormLabel(lblPhone);
        txtPhone = new JTextField(25);
        styleTextField(txtPhone);
        txtPhone.setToolTipText("Format: 0821234567");
        phonePanel.add(lblPhone, BorderLayout.WEST);
        phonePanel.add(txtPhone, BorderLayout.CENTER);
        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(phonePanel, gbc);
        
        // Province
        provincePanel = new JPanel(new BorderLayout(5, 0));
        provincePanel.setOpaque(false);
        JLabel lblProvince = new JLabel("Province:");
        styleFormLabel(lblProvince);
        String[] provinces = {
            "Select Province...",
            "Eastern Cape", "Free State", "Gauteng", "KwaZulu-Natal",
            "Limpopo", "Mpumalanga", "North West", "Northern Cape", "Western Cape"
        };
        cmbProvince = new JComboBox<>(provinces);
        styleComboBox(cmbProvince);
        provincePanel.add(lblProvince, BorderLayout.WEST);
        provincePanel.add(cmbProvince, BorderLayout.CENTER);
        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(provincePanel, gbc);
        
        // Password fields
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        addFormField(formPanel, gbc, gridY++, "Password:", txtPassword = new JPasswordField(25));
        addFormField(formPanel, gbc, gridY++, "Confirm Password:", txtConfirmPassword = new JPasswordField(25));
        
        // Orphanage Selection (for Staff)
        orphanagePanel = new JPanel(new BorderLayout(5, 0));
        orphanagePanel.setOpaque(false);
        JLabel lblOrphanage = new JLabel("Orphanage:");
        styleFormLabel(lblOrphanage);
        cmbOrphanage = new JComboBox<>();
        styleComboBox(cmbOrphanage);
        orphanagePanel.add(lblOrphanage, BorderLayout.WEST);
        orphanagePanel.add(cmbOrphanage, BorderLayout.CENTER);
        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(orphanagePanel, gbc);
        orphanagePanel.setVisible(false);
        
        // Checkboxes
        chkTerms = new JCheckBox("I agree to the Terms of Service and Privacy Policy");
        styleCheckbox(chkTerms);
        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10, 5, 5, 5);
        formPanel.add(chkTerms, gbc);
        
        chkNewsletter = new JCheckBox("Send me updates about orphanage activities");
        styleCheckbox(chkNewsletter);
        gbc.gridy = gridY++;
        gbc.insets = new Insets(5, 5, 15, 5);
        formPanel.add(chkNewsletter, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        buttonPanel.setOpaque(false);
        
        JButton btnRegister = new JButton("Register");
        styleActionButton(btnRegister, "Create your account");
        btnRegister.setBackground(BUTTON_REGISTER_BG);
        btnRegister.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnRegister.setBackground(BUTTON_REGISTER_HOVER_BG);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnRegister.setBackground(BUTTON_REGISTER_BG);
            }
        });
        btnRegister.addActionListener(e -> performRegistration());
        
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
        
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnBack);
        
        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 5, 5, 5);
        formPanel.add(buttonPanel, gbc);
        
        // Scroll Pane
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        styleScrollBar(scrollPane.getVerticalScrollBar());
        
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Maps UI role names to database-compatible role values
     */
    private String mapRoleToDatabase(String uiRole) {
        return switch (uiRole) {
            case "OrphanageStaff" -> "OrphanageRep";  // Map to DB schema value
            case "Donor" -> "Donor";
            case "Volunteer" -> "Volunteer";
            case "Admin" -> "Admin";
            default -> {
                System.out.println("WARNING: Unknown UI role '" + uiRole + "', defaulting to Donor");
                yield "Donor";
            }
        };
    }

    /**
     * Gets display-friendly role name for UI
     */
    private String getDisplayRoleName(String role) {
        return switch (role) {
            case "OrphanageStaff", "OrphanageRep" -> "Orphanage Staff";
            case "Donor" -> "Donor";
            case "Volunteer" -> "Volunteer";
            case "Admin" -> "Administrator";
            default -> role;
        };
    }

    /**
     * Performs registration with FP-style error handling and EDT safety.
     */
    private void performRegistration() {
        // Validate on EDT first
        Try<Map<String, Object>> validationResult = validateForm();
        
        validationResult
            .flatMap(registrationData -> {
                // Get password for registration
                char[] password = txtPassword.getPassword();
                String passwordStr = new String(password);
                
                // Clear sensitive data immediately after use
                Arrays.fill(password, ' ');
                
                // Debug: Log what we're registering
                System.out.println("Registering user with role: " + registrationData.get("role"));
                
                // Perform registration asynchronously (off EDT)
                return Try.of(() -> 
                    CompletableFuture
                        .supplyAsync(() -> 
                            registrationService.registerWithExpandedData(
                                registrationData, 
                                passwordStr
                            ).get(), // Will throw if registration fails
                            Executors.newSingleThreadExecutor()
                        )
                        .get() // Block and get result
                );
            })
            .onSuccess(user -> {
                // Clear form and show success on EDT
                SwingUtilities.invokeLater(() -> {
                    System.out.println("Registration successful for user: " + user.username() + " with role: " + user.userRole());
                    clearFormSafely();
                    JOptionPane.showMessageDialog(this,
                        "Registration successful! Please login to continue.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    mainApp.navigateTo(OrphanageHubApp.LOGIN_PANEL);
                });
            })
            .onFailure(ex -> {
                // Show error on EDT
                SwingUtilities.invokeLater(() -> {
                    String errorMessage = ex.getMessage() != null 
                        ? ex.getMessage() 
                        : "Registration failed. Please try again.";
                    System.err.println("Registration failed: " + errorMessage);
                    JOptionPane.showMessageDialog(this,
                        errorMessage,
                        "Registration Error",
                        JOptionPane.ERROR_MESSAGE);
                });
            });
    }

    /**
     * Validates form input using FP-style with Try monad.
     * 
     * @return Try containing registration data map or validation error
     */
    private Try<Map<String, Object>> validateForm() {
        return Try.of(() -> {
            String username = txtUsername.getText().trim();
            String email = txtEmail.getText().trim();
            String fullName = txtFullName.getText().trim();
            String phone = txtPhone.getText().trim();
            String province = Option.of(cmbProvince.getSelectedItem())
                .map(Object::toString)
                .getOrElse("");
            char[] password = txtPassword.getPassword();
            char[] confirmPassword = txtConfirmPassword.getPassword();
            
            try {
                // Required field validation
                if (username.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
                    throw new IllegalArgumentException("Please fill in all required fields.");
                }
                
                // Email validation with FP style
                ValidationUtil.isValidEmail
                    .apply(email)
                    .filter(valid -> valid)
                    .getOrElseThrow(() -> 
                        new IllegalArgumentException("Please enter a valid email address."));
                
                // Username validation with FP style
                ValidationUtil.isValidUsername
                    .apply(username)
                    .filter(valid -> valid)
                    .getOrElseThrow(() -> 
                        new IllegalArgumentException("Username must be 3-20 characters, alphanumeric only."));
                
                // Phone validation (optional field)
                if (!phone.isEmpty()) {
                    ValidationUtil.isValidPhone
                        .apply(phone)
                        .filter(valid -> valid)
                        .getOrElseThrow(() -> 
                            new IllegalArgumentException("Please enter a valid South African phone number (e.g., 0821234567)."));
                }
                
                // Password match check
                if (!Arrays.equals(password, confirmPassword)) {
                    throw new IllegalArgumentException("Passwords do not match.");
                }
                
                // Password strength validation
                ValidationUtil.isStrongPassword
                    .apply(new String(password))
                    .filter(valid -> valid)
                    .getOrElseThrow(() -> 
                        new IllegalArgumentException(
                            "Password must be at least 8 characters with uppercase, lowercase, numbers, and special characters."));
                
                // Role-specific validation
                if ("OrphanageStaff".equals(currentRole)) {
                    if (cmbOrphanage.getSelectedIndex() <= 0) {
                        throw new IllegalArgumentException("Orphanage Staff must select an orphanage.");
                    }
                }
                
                // Province validation
                if (cmbProvince.getSelectedIndex() <= 0) {
                    throw new IllegalArgumentException("Please select your province.");
                }
                
                // Terms validation
                if (!chkTerms.isSelected()) {
                    throw new IllegalArgumentException("You must agree to the Terms of Service.");
                }
                
                // MAP UI ROLE TO DATABASE ROLE - CRITICAL FIX
                String dbRole = mapRoleToDatabase(currentRole);
                System.out.println("Mapping UI role '" + currentRole + "' to DB role '" + dbRole + "'");
                
                // Build registration data map
                Map<String, Object> registrationData = new HashMap<>();
                registrationData.put("username", username);
                registrationData.put("email", email);
                registrationData.put("fullName", fullName);
                registrationData.put("phone", phone);
                registrationData.put("province", province);
                registrationData.put("role", dbRole);  // USE MAPPED ROLE
                registrationData.put("newsletter", chkNewsletter.isSelected());
                
                if ("OrphanageStaff".equals(currentRole)) {
                    registrationData.put("orphanageName", cmbOrphanage.getSelectedItem());
                }
                
                return registrationData;
                
            } finally {
                // Always clear sensitive data
                Arrays.fill(password, ' ');
                Arrays.fill(confirmPassword, ' ');
            }
        });
    }

    /**
     * Clears form safely on EDT with null checks.
     */
    private void clearFormSafely() {
        Runnable clearTask = () -> {
            // Clear text fields
            Option.of(txtUsername).forEach(field -> field.setText(""));
            Option.of(txtEmail).forEach(field -> field.setText(""));
            Option.of(txtFullName).forEach(field -> field.setText(""));
            Option.of(txtPhone).forEach(field -> field.setText(""));
            Option.of(txtPassword).forEach(field -> field.setText(""));
            Option.of(txtConfirmPassword).forEach(field -> field.setText(""));
            
            // Clear checkboxes
            Option.of(chkTerms).forEach(cb -> cb.setSelected(false));
            Option.of(chkNewsletter).forEach(cb -> cb.setSelected(false));
            
            // Clear combo boxes safely
            Option.of(cmbOrphanage)
                .filter(cmb -> cmb.getItemCount() > 0)
                .forEach(cmb -> cmb.setSelectedIndex(0));
            
            Option.of(cmbProvince)
                .filter(cmb -> cmb.getItemCount() > 0)
                .forEach(cmb -> cmb.setSelectedIndex(0));
        };
        
        // Execute on EDT if not already on it
        if (SwingUtilities.isEventDispatchThread()) {
            clearTask.run();
        } else {
            SwingUtilities.invokeLater(clearTask);
        }
    }
	/**
 * Setter for current role—updates state and UI immediately (for navigation sync).
 */
public void setCurrentRole(String role) {
    if (role != null && !role.trim().isEmpty()) {
        this.currentRole = role;
        System.out.println("RegistrationPanel: Set current role via setter = " + role);
        
        // Refresh UI like in addNotify
        lblTitle.setText("Register as " + getDisplayRoleName(role));
        
        // Update role icon with pattern matching
        Option.of(role).forEach(r -> {
            switch (r) {
                case "Donor" -> {
                    lblRoleIcon.setText("\u2764");
                    lblRoleIcon.setForeground(new Color(255, 105, 180));
                }
                case "OrphanageStaff", "OrphanageRep" -> {
                    lblRoleIcon.setText("\u2302");
                    lblRoleIcon.setForeground(new Color(135, 206, 250));
                    loadOrphanagesSafely();
                }
                case "Volunteer" -> {
                    lblRoleIcon.setText("\u2605");
                    lblRoleIcon.setForeground(new Color(255, 215, 0));
                }
                default -> {
                    lblRoleIcon.setText("?");
                    lblRoleIcon.setForeground(TITLE_COLOR_DARK);
                }
            }
        });
        
        // Toggle orphanage panel visibility
        boolean isStaff = "OrphanageStaff".equals(role) || "OrphanageRep".equals(role);
        orphanagePanel.setVisible(isStaff);
        
        revalidate();
        repaint();
    }
}
    @Override
    public void addNotify() {
        super.addNotify();
        currentRole = mainApp.getLastSelectedRole();
        System.out.println("RegistrationPanel: Current role from app = " + currentRole);
        
        lblTitle.setText("Register as " + getDisplayRoleName(currentRole));
        
        // Update role icon with FP-style pattern matching simulation
        Option.of(currentRole)
            .forEach(role -> {
                switch (role) {
                    case "Donor" -> {
                        lblRoleIcon.setText("\u2764");
                        lblRoleIcon.setForeground(new Color(255, 105, 180));
                    }
                    case "OrphanageStaff", "OrphanageRep" -> {
                        lblRoleIcon.setText("\u2302");
                        lblRoleIcon.setForeground(new Color(135, 206, 250));
                        loadOrphanagesSafely();
                    }
                    case "Volunteer" -> {
                        lblRoleIcon.setText("\u2605");
                        lblRoleIcon.setForeground(new Color(255, 215, 0));
                    }
                    default -> {
                        lblRoleIcon.setText("?");
                        lblRoleIcon.setForeground(TITLE_COLOR_DARK);
                    }
                }
            });
        
        boolean isStaff = "OrphanageStaff".equals(currentRole) || "OrphanageRep".equals(currentRole);
        orphanagePanel.setVisible(isStaff);
        
        revalidate();
        repaint();
    }

    /**
     * Loads orphanages safely with proper EDT handling and fallback.
     */
    private void loadOrphanagesSafely() {
        orphanageService.getVerifiedOrphanages()
            .onSuccess(orphanages -> {
                SwingUtilities.invokeLater(() -> {
                    cmbOrphanage.removeAllItems();
                    // Always add placeholder first
                    cmbOrphanage.addItem("Select Orphanage...");
                    
                    // Add orphanages if available
                    Option.of(orphanages)
                        .filter(list -> !list.isEmpty())
                        .forEach(list -> 
                            list.forEach(o -> cmbOrphanage.addItem(o.name()))
                        );
                });
            })
            .onFailure(ex -> {
                System.err.println("Failed to load orphanages: " + ex.getMessage());
                // Ensure placeholder exists even on failure
                SwingUtilities.invokeLater(() -> {
                    if (cmbOrphanage.getItemCount() == 0) {
                        cmbOrphanage.addItem("Select Orphanage...");
                        cmbOrphanage.addItem("(Unable to load orphanages)");
                    }
                });
            });
    }

    // Styling methods remain the same...
    private void addFormField(JPanel panel, GridBagConstraints gbc, int gridY, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        styleFormLabel(label);
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(label, gbc);
        
        styleTextField(field);
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
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
        if (field instanceof JTextField tf) {
            tf.setCaretColor(Color.LIGHT_GRAY);
        } else if (field instanceof JPasswordField pf) {
            pf.setCaretColor(Color.LIGHT_GRAY);
        }
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        comboBox.setForeground(INPUT_FG_DARK);
        comboBox.setBackground(INPUT_BG_DARK);
        comboBox.setBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK, 1));
    }

    private void styleCheckbox(JCheckBox checkBox) {
        checkBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        checkBox.setForeground(CHECKBOX_COLOR);
        checkBox.setOpaque(false);
    }

    private void styleActionButton(JButton btn, String tooltip) {
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(130, 40));
        btn.setToolTipText(tooltip);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setForeground(BUTTON_FG_DARK);
        btn.setFocusPainted(false);
        btn.setBackground(BUTTON_BG_DARK);
        
        Border line = BorderFactory.createLineBorder(BUTTON_BG_DARK.darker());
        Border padding = new EmptyBorder(5, 15, 5, 15);
        btn.setBorder(new CompoundBorder(line, padding));
    }

    private void styleScrollBar(JScrollBar scrollBar) {
        scrollBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = BUTTON_BG_DARK;
                this.trackColor = DARK_BG_END;
            }
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });
        scrollBar.setUnitIncrement(16);
    }
}