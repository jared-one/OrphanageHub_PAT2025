package com.orphanagehub.gui;

import com.orphanagehub.util.SessionManager;
import io.vavr.control.Try;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VolunteerDashboardPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private final transient OrphanageHubApp mainApp;
    
    private JLabel userLabel;
    private DefaultTableModel opportunitiesModel;
    private DefaultTableModel applicationsModel;
    private JTextField txtSkills;
    private JComboBox<String> cmbLocation;
    private JComboBox<String> cmbTime;
    
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
    private static final Color TABLE_HEADER_BG = new Color(65, 75, 77);
    private static final Color TABLE_HEADER_FG = TITLE_COLOR_DARK;
    private static final Color TABLE_GRID_COLOR = BORDER_COLOR_DARK;
    private static final Color TABLE_CELL_BG = new Color(55, 62, 64);
    private static final Color TABLE_CELL_FG = TEXT_COLOR_DARK;
    private static final Color TABLE_CELL_SELECTED_BG = BUTTON_BG_DARK;
    private static final Color TABLE_CELL_SELECTED_FG = BUTTON_FG_DARK;
    private static final Color BUTTON_APPLY_BG = new Color(87, 190, 106);
    private static final Color BUTTON_APPLY_HOVER_BG = new Color(97, 200, 116);

    public VolunteerDashboardPanel(OrphanageHubApp app) {
        this.mainApp = app;
        setLayout(new BorderLayout(0, 0));
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
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setOpaque(false);
        tabbedPane.addTab("Browse Opportunities", createBrowseTab());
        tabbedPane.addTab("My Applications", createApplicationsTab());
        tabbedPane.addTab("Profile", createProfileTab());
        add(tabbedPane, BorderLayout.CENTER);
        
        SwingUtilities.invokeLater(() -> {
            updateUserLabel();
            loadOpportunities();
            loadApplications();
        });
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR_DARK),
                new EmptyBorder(10, 20, 10, 20)
        ));
        
        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleGroup.setOpaque(false);
        JLabel iconLabel = new JLabel("\u2605");
        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        iconLabel.setForeground(new Color(255, 215, 0));
        JLabel nameLabel = new JLabel("Volunteer Dashboard");
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        nameLabel.setForeground(TITLE_COLOR_DARK);
        titleGroup.add(iconLabel);
        titleGroup.add(nameLabel);
        headerPanel.add(titleGroup, BorderLayout.WEST);
        
        JPanel userGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userGroup.setOpaque(false);
        userLabel = new JLabel("Welcome, Volunteer");
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        userLabel.setForeground(TEXT_COLOR_DARK);
        
        JButton btnLogout = new JButton("Logout");
        styleActionButton(btnLogout, "Logout and return to welcome screen");
        btnLogout.setPreferredSize(new Dimension(100, 30));
        btnLogout.setBackground(new Color(192, 57, 43));
        btnLogout.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnLogout.setBackground(new Color(231, 76, 60)); }
            @Override public void mouseExited(MouseEvent e) { btnLogout.setBackground(new Color(192, 57, 43)); }
        });
        btnLogout.addActionListener(e -> mainApp.navigateTo("Home"));
        
        userGroup.add(userLabel);
        userGroup.add(btnLogout);
        headerPanel.add(userGroup, BorderLayout.EAST);
        return headerPanel;
    }

    private void updateUserLabel() {
        SessionManager.getInstance().getAttribute("currentUser")
            .map(user -> "Welcome, " + user.toString())
            .peek(userLabel::setText)
            .onEmpty(() -> userLabel.setText("Welcome, Volunteer"));
    }

    private JPanel createBrowseTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 20, 20, 20));
        
        JPanel searchFilterPanel = createSearchFilterPanel();
        panel.add(searchFilterPanel, BorderLayout.NORTH);
        
        String[] columnNames = {"Orphanage", "Opportunity", "Location", "Skills Needed", "Time Commitment", "Status"};
        opportunitiesModel = new DefaultTableModel(columnNames, 0);
        JTable opportunitiesTable = new JTable(opportunitiesModel);
        styleTable(opportunitiesTable);
        
        opportunitiesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = opportunitiesTable.getSelectedRow();
                    if (row >= 0) {
                        showApplyDialog(row);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(opportunitiesTable);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton btnApply = new JButton("Apply to Selected");
        styleActionButton(btnApply, "Apply to the selected opportunity");
        btnApply.setBackground(BUTTON_APPLY_BG);
        btnApply.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnApply.setBackground(BUTTON_APPLY_HOVER_BG); }
            @Override public void mouseExited(MouseEvent e) { btnApply.setBackground(BUTTON_APPLY_BG); }
        });
        btnApply.addActionListener(e -> {
            int row = opportunitiesTable.getSelectedRow();
            if (row >= 0) {
                showApplyDialog(row);
            } else {
                showErrorMessage("Please select an opportunity to apply");
            }
        });
        
        buttonPanel.add(btnApply);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createSearchFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setOpaque(false);
        
        JLabel lblLocation = new JLabel("Location:");
        styleFormLabel(lblLocation);
        cmbLocation = new JComboBox<>(new String[]{"Any Location", "City A", "City B", "Region C"});
        styleComboBox(cmbLocation);
        
        JLabel lblSkills = new JLabel("Skills:");
        styleFormLabel(lblSkills);
        txtSkills = new JTextField(15);
        styleTextField(txtSkills);
        
        JLabel lblTime = new JLabel("Commitment:");
        styleFormLabel(lblTime);
        cmbTime = new JComboBox<>(new String[]{"Any Time", "Weekends", "Weekdays", "Flexible", "Event-Based"});
        styleComboBox(cmbTime);
        
        JButton btnSearch = new JButton("Find Opportunities");
        styleActionButton(btnSearch, "Search for volunteer roles matching criteria");
        btnSearch.addActionListener(e -> performSearch());
        
        panel.add(lblLocation);
        panel.add(cmbLocation);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(lblSkills);
        panel.add(txtSkills);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(lblTime);
        panel.add(cmbTime);
        panel.add(Box.createHorizontalStrut(15));
        panel.add(btnSearch);
        
        return panel;
    }

    private JPanel createApplicationsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        String[] columnNames = {"Application Date", "Orphanage", "Opportunity", "Status", "Interview Date", "Actions"};
        applicationsModel = new DefaultTableModel(columnNames, 0);
        JTable applicationsTable = new JTable(applicationsModel);
        styleTable(applicationsTable);
        
        JScrollPane scrollPane = new JScrollPane(applicationsTable);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setOpaque(false);
        statusPanel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR_DARK),
            new EmptyBorder(10, 5, 5, 5)
        ));
        
        JLabel lblStatus = new JLabel("Total Applications: 0 | Pending: 0 | Accepted: 0 | Rejected: 0");
        lblStatus.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 13));
        lblStatus.setForeground(TEXT_COLOR_DARK);
        statusPanel.add(lblStatus);
        panel.add(statusPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createProfileTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField nameField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextArea skillsArea = new JTextArea(3, 20);
        JTextArea experienceArea = new JTextArea(4, 20);
        JComboBox<String> availabilityCombo = new JComboBox<>(new String[]{"Weekends", "Weekdays", "Flexible", "Event-Based"});
        
        int row = 0;
        
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Skills:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(skillsArea), gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Experience:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(experienceArea), gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Availability:"), gbc);
        gbc.gridx = 1;
        panel.add(availabilityCombo, gbc);
        
        row++;
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save Profile");
        styleActionButton(saveButton, "Save your profile changes");
        saveButton.addActionListener(e -> saveProfile());
        buttonPanel.add(saveButton);
        
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        styleTextField(nameField);
        styleTextField(emailField);
        styleTextArea(skillsArea);
        styleTextArea(experienceArea);
        styleComboBox(availabilityCombo);
        
        return panel;
    }

    private void loadOpportunities() {
        opportunitiesModel.setRowCount(0);
        opportunitiesModel.addRow(new Object[]{
            "Hope Children's Home", "Weekend Tutor", "City A", "Teaching", "Weekends", "Open"
        });
        opportunitiesModel.addRow(new Object[]{
            "Bright Future", "Event Helper", "City B", "Organizing", "Event-Based", "Open"
        });
    }

    private void loadApplications() {
        applicationsModel.setRowCount(0);
        applicationsModel.addRow(new Object[]{
            "2024-01-15", "Hope Children's Home", "Weekend Tutor", "Pending", "TBD", "View"
        });
    }

    private void performSearch() {
        loadOpportunities();
    }

    private void showApplyDialog(int row) {
        String orphanage = opportunitiesModel.getValueAt(row, 0).toString();
        String opportunity = opportunitiesModel.getValueAt(row, 1).toString();
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Apply for Opportunity", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JLabel titleLabel = new JLabel("Applying for: " + opportunity);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        JLabel orphanageLabel = new JLabel("At: " + orphanage);
        
        JTextArea coverLetterArea = new JTextArea(8, 30);
        JTextField availabilityField = new JTextField(20);
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        dialog.add(titleLabel, gbc);
        
        gbc.gridy = 1;
        dialog.add(orphanageLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Cover Letter:"), gbc);
        gbc.gridx = 1;
        dialog.add(new JScrollPane(coverLetterArea), gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Availability:"), gbc);
        gbc.gridx = 1;
        dialog.add(availabilityField, gbc);
        
        JPanel buttonPanel = new JPanel();
        JButton applyButton = new JButton("Submit Application");
        JButton cancelButton = new JButton("Cancel");
        
        applyButton.addActionListener(e -> {
            submitApplication(orphanage, opportunity, coverLetterArea.getText(), availabilityField.getText());
            dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);
        
        dialog.setVisible(true);
    }

    private void submitApplication(String orphanage, String opportunity, String coverLetter, String availability) {
        showSuccessMessage("Application submitted successfully!");
        loadApplications();
    }

    private void saveProfile() {
        showSuccessMessage("Profile saved successfully!");
    }

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void styleFormLabel(JLabel label) {
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        label.setForeground(TEXT_COLOR_DARK);
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        field.setForeground(INPUT_FG_DARK);
        field.setBackground(INPUT_BG_DARK);
        Border padding = new EmptyBorder(4, 6, 4, 6);
        field.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(INPUT_BORDER_DARK, 1),
            padding
        ));
        field.setCaretColor(Color.LIGHT_GRAY);
    }

    private void styleTextArea(JTextArea area) {
        area.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        area.setForeground(INPUT_FG_DARK);
        area.setBackground(INPUT_BG_DARK);
        area.setBorder(new EmptyBorder(4, 6, 4, 6));
        area.setCaretColor(Color.LIGHT_GRAY);
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        comboBox.setForeground(INPUT_FG_DARK);
        comboBox.setBackground(INPUT_BG_DARK);
        comboBox.setBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK, 1));
    }

    private void styleTable(JTable table) {
        table.setBackground(TABLE_CELL_BG);
        table.setForeground(TABLE_CELL_FG);
        table.setGridColor(TABLE_GRID_COLOR);
        table.setRowHeight(28);
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(TABLE_CELL_SELECTED_BG);
        table.setSelectionForeground(TABLE_CELL_SELECTED_FG);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TABLE_HEADER_FG);
        header.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        header.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK));
        
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.LEFT);
        renderer.setVerticalAlignment(SwingConstants.CENTER);
        renderer.setBorder(new EmptyBorder(2, 5, 2, 5));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK));
        applyScrollbarUI(scrollPane.getVerticalScrollBar());
        applyScrollbarUI(scrollPane.getHorizontalScrollBar());
    }

    private void applyScrollbarUI(JScrollBar scrollBar) {
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = BUTTON_BG_DARK;
                this.trackColor = DARK_BG_END;
            }
            @Override protected JButton createDecreaseButton(int o) { return createZeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return createZeroButton(); }
            private JButton createZeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
        scrollBar.setUnitIncrement(16);
    }

    private void styleActionButton(JButton btn, String tooltip) {
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btn.setToolTipText(tooltip);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setForeground(BUTTON_FG_DARK);
        btn.setFocusPainted(false);
        btn.setBackground(BUTTON_BG_DARK);
        Border padding = new EmptyBorder(6, 12, 6, 12);
        btn.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(BUTTON_BG_DARK.darker()),
            padding
        ));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn.getBackground().equals(BUTTON_BG_DARK)) {
                    btn.setBackground(BUTTON_HOVER_BG_DARK);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn.getBackground().equals(BUTTON_HOVER_BG_DARK)) {
                    btn.setBackground(BUTTON_BG_DARK);
                }
            }
        });
    }
}