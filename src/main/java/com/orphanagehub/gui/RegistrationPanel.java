package com.orphanagehub.gui;

import com.orphanagehub.service.RegistrationService;
import io.vavr.control.Try;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RegistrationPanel extends JPanel {
    private final OrphanageHubApp mainApp;
    private final RegistrationService registrationService;
    private String currentRole = "User";
    private JTextField txtUsername;
    private JTextField txtEmail;
    private JTextField txtFullName;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JComboBox<String> cmbOrphanage;
    private JCheckBox chkTerms;
    private JLabel lblTitle;
    private JLabel lblRoleIcon;
    private JPanel orphanagePanel;
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

    public RegistrationPanel(OrphanageHubApp app) {
        this.mainApp = app;
        this.registrationService = new RegistrationService();
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

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        titlePanel.setOpaque(false);

        lblRoleIcon = new JLabel("?");
        lblRoleIcon.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        lblRoleIcon.setForeground(TITLE_COLOR_DARK);
        titlePanel.add(lblRoleIcon);

        lblTitle = new JLabel("Register as " + currentRole);
        lblTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        lblTitle.setForeground(TITLE_COLOR_DARK);
        titlePanel.add(lblTitle);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 5, 20, 5);
        formPanel.add(titlePanel, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(6, 5, 6, 5);

        int gridY = 1;

        addFormField(formPanel, gbc, gridY++, "Username:", txtUsername = new JTextField(25));
        addFormField(formPanel, gbc, gridY++, "Email:", txtEmail = new JTextField(25));
        addFormField(formPanel, gbc, gridY++, "Full Name:", txtFullName = new JTextField(25));
        addFormField(formPanel, gbc, gridY++, "Password:", txtPassword = new JPasswordField(25));
        addFormField(formPanel, gbc, gridY++, "Confirm Password:", txtConfirmPassword = new JPasswordField(25));

        orphanagePanel = new JPanel(new BorderLayout(5, 0));
        orphanagePanel.setOpaque(false);
        JLabel lblOrphanage = new JLabel("Orphanage:");
        styleFormLabel(lblOrphanage);
        String[] orphanages = {"Select Orphanage...", "Hope Children's Home", "Bright Future Orphanage", "Little Angels Shelter"};
        cmbOrphanage = new JComboBox<>(orphanages);
        styleComboBox(cmbOrphanage);
        orphanagePanel.add(lblOrphanage, BorderLayout.WEST);
        orphanagePanel.add(cmbOrphanage, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(orphanagePanel, gbc);
        orphanagePanel.setVisible(false);

        chkTerms = new JCheckBox("I agree to the Terms of Service and Privacy Policy");
        styleCheckbox(chkTerms);
        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(15, 5, 15, 5);
        formPanel.add(chkTerms, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        buttonPanel.setOpaque(false);

        JButton btnRegister = new JButton("Register");
        styleActionButton(btnRegister, "Create your account");
        btnRegister.setBackground(BUTTON_REGISTER_BG);
        btnRegister.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnRegister.setBackground(BUTTON_REGISTER_HOVER_BG); }
            @Override public void mouseExited(MouseEvent e) { btnRegister.setBackground(BUTTON_REGISTER_BG); }
        });
        btnRegister.addActionListener(e -> registerAction());

        JButton btnBack = new JButton("Back");
        styleActionButton(btnBack, "Return to the welcome screen");
        btnBack.setBackground(BUTTON_BG_DARK.darker());
        btnBack.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnBack.setBackground(BUTTON_HOVER_BG_DARK); }
            @Override public void mouseExited(MouseEvent e) { btnBack.setBackground(BUTTON_BG_DARK.darker()); }
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

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = BUTTON_BG_DARK;
                this.trackColor = DARK_BG_END;
            }
            @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });
        verticalScrollBar.setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

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
        if (field instanceof JTextField) ((JTextField) field).setCaretColor(Color.LIGHT_GRAY);
        else if (field instanceof JPasswordField) ((JPasswordField) field).setCaretColor(Color.LIGHT_GRAY);
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

    private void registerAction() {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String fullName = txtFullName.getText().trim();
        char[] password = txtPassword.getPassword();
        char[] confirmPassword = txtConfirmPassword.getPassword();

        if (username.isEmpty() || email.isEmpty() || fullName.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!java.util.Arrays.equals(password, confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            java.util.Arrays.fill(password, '\0');
            java.util.Arrays.fill(confirmPassword, '\0');
            return;
        }
        if (currentRole.equals("OrphanageStaff") && cmbOrphanage.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Orphanage Staff must select an orphanage.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!chkTerms.isSelected()) {
            JOptionPane.showMessageDialog(this, "You must agree to the Terms of Service.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Using the correct method signature for RegistrationService
        registrationService.register(username, email, fullName, password, confirmPassword, currentRole)
            .onSuccess(user -> {
                // Clear sensitive data
                java.util.Arrays.fill(password, '\0');
                java.util.Arrays.fill(confirmPassword, '\0');
                
                JOptionPane.showMessageDialog(this, 
                    "Registration successful! Please login to continue.", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                mainApp.navigateTo(OrphanageHubApp.LOGIN_PANEL);
            })
            .onFailure(ex -> {
                // Clear sensitive data
                java.util.Arrays.fill(password, '\0');
                java.util.Arrays.fill(confirmPassword, '\0');
                
                JOptionPane.showMessageDialog(this, 
                    ex.getMessage(), 
                    "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
            });
    }

    private void clearForm() {
        txtUsername.setText("");
        txtEmail.setText("");
        txtFullName.setText("");
        txtPassword.setText("");
        txtConfirmPassword.setText("");
        chkTerms.setSelected(false);
        if (cmbOrphanage != null) {
            cmbOrphanage.setSelectedIndex(0);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        
        // Get role from HomePanel
        currentRole = mainApp.getSelectedRole();
        
        lblTitle.setText("Register as " + currentRole);

        switch (currentRole) {
            case "Donor":
                lblRoleIcon.setText("\u2764");
                lblRoleIcon.setForeground(new Color(255, 105, 180));
                break;
            case "OrphanageStaff":
                lblRoleIcon.setText("\u2302");
                lblRoleIcon.setForeground(new Color(135, 206, 250));
                break;
            case "Volunteer":
                lblRoleIcon.setText("\u2605");
                lblRoleIcon.setForeground(new Color(255, 215, 0));
                break;
            default:
                lblRoleIcon.setText("?");
                lblRoleIcon.setForeground(TITLE_COLOR_DARK);
                break;
        }

        boolean isStaff = currentRole.equals("OrphanageStaff");
        orphanagePanel.setVisible(isStaff);

        revalidate();
        repaint();
    }
}