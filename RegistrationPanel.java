package com.orphanagehub.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder; // Use LineBorder explicitly
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D; // Keep this import

public class RegistrationPanel extends JPanel {

    private OrphanageHubApp mainApp;
    private String currentRole = "User"; // Default role

    // Input Fields
    private JTextField txtUsername;
    private JTextField txtEmail;
    private JTextField txtFullName;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JComboBox<String> cmbOrphanage; // Conditional field
    private JCheckBox chkTerms;

    // Components that need updating based on role
    private JLabel lblTitle;
    private JLabel lblRoleIcon; // Placeholder for role icon
    private JPanel orphanagePanel; // Panel holding the orphanage combo box

    // Re-define colors (Consider a shared constants interface/class later)
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
    private static final Color BUTTON_REGISTER_BG = new Color(60, 179, 113); // Medium Sea Green
    private static final Color BUTTON_REGISTER_HOVER_BG = new Color(70, 190, 123);
    private static final Color CHECKBOX_COLOR = new Color(180, 180, 180);

    public RegistrationPanel(OrphanageHubApp app) {
        this.mainApp = app;
        setLayout(new BorderLayout()); // Main panel uses BorderLayout for scrollpane
        // Don't set border here, set on the inner form panel
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
        // Panel to hold the actual form elements using GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false); // Make form panel transparent
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30)); // Padding inside scroll pane

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // --- Title and Role Icon ---
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        titlePanel.setOpaque(false);

        // Placeholder for Role Icon (using text symbol)
        lblRoleIcon = new JLabel("?"); // Placeholder symbol
        lblRoleIcon.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        lblRoleIcon.setForeground(TITLE_COLOR_DARK);
        titlePanel.add(lblRoleIcon);

        lblTitle = new JLabel("Register as " + currentRole); // Title updated in addNotify
        lblTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        lblTitle.setForeground(TITLE_COLOR_DARK);
        titlePanel.add(lblTitle);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 5, 20, 5); // Bottom margin
        formPanel.add(titlePanel, gbc);

        // Reset constraints for form fields
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(6, 5, 6, 5); // Regular spacing

        // --- Form Fields ---
        int gridY = 1; // Start grid row counter

        // Username
        addFormField(formPanel, gbc, gridY++, "Username:", txtUsername = new JTextField(25));
        // Email
        addFormField(formPanel, gbc, gridY++, "Email:", txtEmail = new JTextField(25));
        // Full Name
        addFormField(formPanel, gbc, gridY++, "Full Name:", txtFullName = new JTextField(25));
        // Password
        addFormField(formPanel, gbc, gridY++, "Password:", txtPassword = new JPasswordField(25));
        // Confirm Password
        addFormField(formPanel, gbc, gridY++, "Confirm Password:", txtConfirmPassword = new JPasswordField(25));

        // --- Conditional Orphanage Selection (for Staff) ---
        orphanagePanel = new JPanel(new BorderLayout(5, 0)); // Use BorderLayout for label and combo
        orphanagePanel.setOpaque(false);
        JLabel lblOrphanage = new JLabel("Orphanage:");
        styleFormLabel(lblOrphanage);
        // Simulate orphanage list (replace with DB query later)
        String[] orphanages = {"Select Orphanage...", "Hope Children's Home", "Bright Future Orphanage", "Little Angels Shelter"};
        cmbOrphanage = new JComboBox<>(orphanages);
        styleComboBox(cmbOrphanage); // Apply styling
        orphanagePanel.add(lblOrphanage, BorderLayout.WEST);
        orphanagePanel.add(cmbOrphanage, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = gridY++; // Assign current gridY, then increment
        gbc.gridwidth = 2; // Span both columns
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(orphanagePanel, gbc);
        orphanagePanel.setVisible(false); // Initially hidden

        // --- Terms and Conditions Checkbox ---
        chkTerms = new JCheckBox("I agree to the Terms of Service and Privacy Policy");
        styleCheckbox(chkTerms);
        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(15, 5, 15, 5);
        formPanel.add(chkTerms, gbc);

        // --- Action Buttons Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        buttonPanel.setOpaque(false);

        JButton btnRegister = new JButton("Register");
        styleActionButton(btnRegister, "Create your account");
        // Specific styling for primary action button
        btnRegister.setBackground(BUTTON_REGISTER_BG);
        btnRegister.addMouseListener(new MouseAdapter() { // Override hover for specific color
             @Override public void mouseEntered(MouseEvent e) { btnRegister.setBackground(BUTTON_REGISTER_HOVER_BG); }
             @Override public void mouseExited(MouseEvent e) { btnRegister.setBackground(BUTTON_REGISTER_BG); }
        });
        btnRegister.addActionListener(e -> registerAction()); // Placeholder action

        JButton btnBack = new JButton("Back");
        styleActionButton(btnBack, "Return to the welcome screen");
        btnBack.setBackground(BUTTON_BG_DARK.darker()); // Keep Back button distinct
         btnBack.addMouseListener(new MouseAdapter() { // Custom hover for Back button
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

        // --- Scroll Pane Setup ---
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setOpaque(false); // Show main panel gradient
        scrollPane.getViewport().setOpaque(false); // Show main panel gradient
        scrollPane.setBorder(null); // No border for the scroll pane itself
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // No horizontal scroll
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Optional: Style the scrollbar (can be Look and Feel dependent)
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = BUTTON_BG_DARK; // Use button color for thumb
                this.trackColor = DARK_BG_END;    // Use gradient end for track
            }
             @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
             @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
             private JButton createZeroButton() { // Hide arrow buttons
                 JButton button = new JButton();
                 button.setPreferredSize(new Dimension(0, 0));
                 button.setMinimumSize(new Dimension(0, 0));
                 button.setMaximumSize(new Dimension(0, 0));
                 return button;
             }
        });
        verticalScrollBar.setUnitIncrement(16); // Adjust scroll speed

        // Add the scroll pane to the main RegistrationPanel
        add(scrollPane, BorderLayout.CENTER);
    }

    // Helper to add label and field to the form panel
    private void addFormField(JPanel panel, GridBagConstraints gbc, int gridY, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        styleFormLabel(label);
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(label, gbc);

        styleTextField(field); // Apply common styling
        gbc.gridx = 1;
        gbc.gridy = gridY;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    // Helper to style form labels
    private void styleFormLabel(JLabel label) {
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        label.setForeground(TEXT_COLOR_DARK);
    }

    // Helper to style text/password fields
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

     // Helper to style combo boxes
    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        comboBox.setForeground(INPUT_FG_DARK);
        comboBox.setBackground(INPUT_BG_DARK);
        // Border needs careful handling with ComboBox UI - simple line border might suffice
        comboBox.setBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK, 1));
        // Try to make dropdown match (highly L&F dependent)
        Object popup = comboBox.getUI().getAccessibleChild(comboBox, 0);
        if (popup instanceof JPopupMenu) {
            JPopupMenu popupMenu = (JPopupMenu) popup;
            popupMenu.setBorder(new LineBorder(BORDER_COLOR_DARK));
            Component[] components = popupMenu.getComponents();
             for (Component comp : components) { // Style the scroller and list within the popup
                 if (comp instanceof JScrollPane) {
                     JScrollPane scrollPane = (JScrollPane) comp;
                     scrollPane.getViewport().setBackground(INPUT_BG_DARK);
                     scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() { // Basic styling
                         @Override protected void configureScrollBarColors() {this.thumbColor = BUTTON_BG_DARK; this.trackColor = DARK_BG_END;}
                         @Override protected JButton createDecreaseButton(int o) { return createZeroButton(); }
                         @Override protected JButton createIncreaseButton(int o) { return createZeroButton(); }
                         private JButton createZeroButton() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
                     });
                     Component list = scrollPane.getViewport().getView();
                     if (list instanceof JList) {
                         ((JList<?>)list).setBackground(INPUT_BG_DARK);
                         ((JList<?>)list).setForeground(INPUT_FG_DARK);
                         ((JList<?>)list).setSelectionBackground(BUTTON_BG_DARK);
                         ((JList<?>)list).setSelectionForeground(BUTTON_FG_DARK);
                     }
                 }
             }
        }
    }

    // Helper to style checkboxes
    private void styleCheckbox(JCheckBox checkBox) {
        checkBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        checkBox.setForeground(CHECKBOX_COLOR);
        checkBox.setOpaque(false);
        // Optional: could try to customize the check icon color if needed
    }


    // Adapted from LoginPanel - Consider moving to a utility class later
    private void styleActionButton(JButton btn, String tooltip) {
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(130, 40));
        btn.setToolTipText(tooltip);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setForeground(BUTTON_FG_DARK);
        btn.setFocusPainted(false);

        // Default background set here, can be overridden for specific buttons
        btn.setBackground(BUTTON_BG_DARK);

        Border line = BorderFactory.createLineBorder(BUTTON_BG_DARK.darker());
        Border padding = new EmptyBorder(5, 15, 5, 15);
        btn.setBorder(new CompoundBorder(line, padding));

        // Default hover/exit listener (can be overridden for specific buttons)
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                 if (!btn.getBackground().equals(BUTTON_REGISTER_BG)) // Don't override register button hover here
                    btn.setBackground(BUTTON_HOVER_BG_DARK);
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                 if (!btn.getBackground().equals(BUTTON_REGISTER_BG)) // Don't override register button exit here
                    btn.setBackground(BUTTON_BG_DARK);
            }
        });
    }

    // Placeholder for registration logic
    private void registerAction() {
         // Simple validation example
        if (txtUsername.getText().trim().isEmpty() ||
            txtEmail.getText().trim().isEmpty() ||
            new String(txtPassword.getPassword()).isEmpty()) {
             JOptionPane.showMessageDialog(this, "Please fill in Username, Email, and Password.", "Registration Error", JOptionPane.ERROR_MESSAGE);
             return;
        }
        if (!new String(txtPassword.getPassword()).equals(new String(txtConfirmPassword.getPassword()))) {
             JOptionPane.showMessageDialog(this, "Passwords do not match.", "Registration Error", JOptionPane.ERROR_MESSAGE);
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

        // Placeholder success message
        JOptionPane.showMessageDialog(this,
                "Registration attempt for " + txtUsername.getText() + " as " + currentRole + ".\n(Backend logic not implemented)",
                "Registration Attempt", JOptionPane.INFORMATION_MESSAGE);

        // Optionally navigate back home or to login after successful placeholder registration
        // mainApp.navigateTo(OrphanageHubApp.LOGIN_PANEL);
    }


    // Override addNotify to update role-specific elements when panel becomes visible
    @Override
    public void addNotify() {
        super.addNotify();
        currentRole = mainApp.getSelectedRole(); // Get role selected on Home screen
        lblTitle.setText("Register as " + currentRole);

        // Update role icon placeholder text/symbol
        switch (currentRole) {
            case "Donor":
                lblRoleIcon.setText("\u2764"); // Heavy Black Heart symbol
                lblRoleIcon.setForeground(new Color(255, 105, 180)); // Pinkish
                break;
            case "OrphanageStaff":
                lblRoleIcon.setText("\u2302"); // House symbol
                lblRoleIcon.setForeground(new Color(135, 206, 250)); // Light Sky Blue
                break;
            case "Volunteer":
                lblRoleIcon.setText("\u2605"); // Black Star symbol
                lblRoleIcon.setForeground(new Color(255, 215, 0)); // Gold
                 break;
            default:
                lblRoleIcon.setText("?");
                lblRoleIcon.setForeground(TITLE_COLOR_DARK);
                break;
        }

        // Show/hide orphanage selection based on role
        boolean isStaff = currentRole.equals("OrphanageStaff");
        orphanagePanel.setVisible(isStaff);

        // Request layout update if visibility changed
        revalidate();
        repaint();
    }
}
