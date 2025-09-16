package com.orphanagehub.gui;

import com.orphanagehub.model.*;
import com.orphanagehub.service.*;
import com.orphanagehub.util.SessionManager;
import io.vavr.control.Try;
import io.vavr.control.Option;
import io.vavr.collection.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

public class AdminDashboardPanel extends JPanel {
    private final OrphanageHubApp mainApp;
    private final AdminService adminService;
    private final OrphanageService orphanageService;
    private final UserService userService;
    private final AuditService auditService;
    
    private DefaultTableModel verificationModel;
    private DefaultTableModel userModel;
    private DefaultTableModel auditModel;
    private DefaultTableModel reportModel;
    
    private JTextField txtUserSearch;
    private JComboBox<String> cmbUserRole;
    private JComboBox<String> cmbUserStatus;
    private JTabbedPane tabbedPane;
    
    // Statistics labels
    private JLabel lblTotalUsers;
    private JLabel lblVerifiedOrphanages;
    private JLabel lblPendingVerifications;
    private JLabel lblActiveRequests;
    private JLabel lblTotalDonations;
    private JLabel lblActiveVolunteers;
    
    // Color constants (same as other panels)
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
    private static final Color TAB_BG_SELECTED = new Color(70, 80, 82);
    private static final Color TAB_BG_UNSELECTED = new Color(55, 62, 64);
    private static final Color TAB_FG = TITLE_COLOR_DARK;
    private static final Color TABLE_HEADER_BG = new Color(65, 75, 77);
    private static final Color TABLE_HEADER_FG = TITLE_COLOR_DARK;
    private static final Color TABLE_GRID_COLOR = BORDER_COLOR_DARK;
    private static final Color TABLE_CELL_BG = new Color(55, 62, 64);
    private static final Color TABLE_CELL_FG = TEXT_COLOR_DARK;
    private static final Color TABLE_CELL_SELECTED_BG = BUTTON_BG_DARK;
    private static final Color TABLE_CELL_SELECTED_FG = BUTTON_FG_DARK;
    private static final Color BUTTON_APPROVE_BG = new Color(60, 179, 113);
    private static final Color BUTTON_APPROVE_HOVER_BG = new Color(70, 190, 123);
    private static final Color BUTTON_REJECT_BG = new Color(192, 57, 43);
    private static final Color BUTTON_REJECT_HOVER_BG = new Color(231, 76, 60);

    public AdminDashboardPanel(OrphanageHubApp app) {
        this.mainApp = app;
        this.adminService = new AdminService();
        this.orphanageService = new OrphanageService();
        this.userService = new UserService();
        this.auditService = new AuditService();
        setLayout(new BorderLayout(0, 0));
        initComponents();
        loadInitialData();
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
        
        tabbedPane = createTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR_DARK),
            new EmptyBorder(10, 20, 10, 20)
        ));
        
        // Title group
        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleGroup.setOpaque(false);
        JLabel iconLabel = new JLabel("\u2699"); // Gear symbol
        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        iconLabel.setForeground(TITLE_COLOR_DARK);
        JLabel nameLabel = new JLabel("Administrator Dashboard");
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        nameLabel.setForeground(TITLE_COLOR_DARK);
        titleGroup.add(iconLabel);
        titleGroup.add(nameLabel);
        headerPanel.add(titleGroup, BorderLayout.WEST);
        
        // User group
        JPanel userGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userGroup.setOpaque(false);
        
        String adminUsername = SessionManager.getInstance()
            .getAttribute("currentUsername")
            .map(Object::toString)
            .getOrElse("Administrator");
        
        JLabel userLabel = new JLabel("Admin: " + adminUsername);
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        userLabel.setForeground(TEXT_COLOR_DARK);
        
        JButton btnBackup = new JButton("Backup");
        styleActionButton(btnBackup, "Backup system data");
        btnBackup.setPreferredSize(new Dimension(100, 30));
        btnBackup.addActionListener(e -> performBackup());
        
        JButton btnLogout = new JButton("Logout");
        styleActionButton(btnLogout, "Logout and return to welcome screen");
        btnLogout.setPreferredSize(new Dimension(100, 30));
        btnLogout.setBackground(BUTTON_REJECT_BG);
        btnLogout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnLogout.setBackground(BUTTON_REJECT_HOVER_BG);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnLogout.setBackground(BUTTON_REJECT_BG);
            }
        });
        btnLogout.addActionListener(e -> {
            SessionManager.getInstance().clear();
            mainApp.navigateTo(OrphanageHubApp.HOME_PANEL);
        });
        
        userGroup.add(userLabel);
        userGroup.add(btnBackup);
        userGroup.add(btnLogout);
        headerPanel.add(userGroup, BorderLayout.EAST);
        
        return headerPanel;
    }

    private JTabbedPane createTabbedPane() {
        JTabbedPane pane = new JTabbedPane();
        pane.setOpaque(false);
        pane.setForeground(TAB_FG);
        pane.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        // Custom UI for tabs
        pane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                lightHighlight = TAB_BG_SELECTED;
                shadow = BORDER_COLOR_DARK;
                darkShadow = DARK_BG_END;
                focus = TAB_BG_SELECTED;
            }
            
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                    int x, int y, int w, int h, boolean isSelected) {
                g.setColor(isSelected ? TAB_BG_SELECTED : TAB_BG_UNSELECTED);
                g.fillRoundRect(x, y, w, h + 5, 5, 5);
            }
            
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                    int x, int y, int w, int h, boolean isSelected) {
                // No border
            }
            
            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                int width = tabPane.getWidth();
                int height = tabPane.getHeight();
                Insets insets = tabPane.getInsets();
                int tabAreaHeight = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                int x = insets.left;
                int y = insets.top + tabAreaHeight - 1;
                int w = width - insets.right - insets.left;
                int h = height - insets.top - insets.bottom - y;
                g.setColor(BORDER_COLOR_DARK);
                g.drawRect(x, y, w - 1, h - 1);
            }
        });
        
        pane.addTab("System Overview", createOverviewTab());
        pane.addTab("Orphanage Verification", createVerificationTab());
        pane.addTab("User Management", createUserManagementTab());
        pane.addTab("Reports", createReportsTab());
        pane.addTab("Audit Log", createAuditLogTab());
        pane.addTab("System Settings", createSettingsTab());
        
        return pane;
    }

    private JPanel createOverviewTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        // Statistics Grid
        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 20, 20));
        statsGrid.setOpaque(false);
        
        lblTotalUsers = new JLabel("0", SwingConstants.CENTER);
        lblVerifiedOrphanages = new JLabel("0", SwingConstants.CENTER);
        lblPendingVerifications = new JLabel("0", SwingConstants.CENTER);
        lblActiveRequests = new JLabel("0", SwingConstants.CENTER);
        lblTotalDonations = new JLabel("$0", SwingConstants.CENTER);
        lblActiveVolunteers = new JLabel("0", SwingConstants.CENTER);
        
        statsGrid.add(createStatCard("Total Users", lblTotalUsers, new Color(72, 149, 239)));
        statsGrid.add(createStatCard("Verified Orphanages", lblVerifiedOrphanages, new Color(87, 190, 106)));
        statsGrid.add(createStatCard("Pending Verifications", lblPendingVerifications, new Color(230, 145, 56)));
        statsGrid.add(createStatCard("Active Requests", lblActiveRequests, new Color(155, 89, 182)));
        statsGrid.add(createStatCard("Total Donations", lblTotalDonations, new Color(52, 152, 219)));
        statsGrid.add(createStatCard("Active Volunteers", lblActiveVolunteers, new Color(241, 196, 15)));
        
        panel.add(statsGrid, BorderLayout.NORTH);
        
        // Recent Activity Log
        JPanel activityPanel = new JPanel(new BorderLayout());
        activityPanel.setOpaque(false);
        activityPanel.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR_DARK),
            " Recent System Activity ",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font(Font.SANS_SERIF, Font.BOLD, 14),
            TITLE_COLOR_DARK
        ));
        
        JTextArea activityLog = new JTextArea(15, 50);
        activityLog.setEditable(false);
        activityLog.setBackground(TABLE_CELL_BG);
        activityLog.setForeground(TABLE_CELL_FG);
        activityLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane activityScroll = new JScrollPane(activityLog);
        styleScrollPane(activityScroll);
        activityPanel.add(activityScroll, BorderLayout.CENTER);
        
        panel.add(activityPanel, BorderLayout.CENTER);
        
        // Quick Actions Panel
        JPanel quickActionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        quickActionsPanel.setOpaque(false);
        quickActionsPanel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR_DARK),
            new EmptyBorder(10, 5, 5, 5)
        ));
        
        JButton btnGenerateReport = new JButton("Generate Reports");
        JButton btnSystemHealth = new JButton("System Health");
        JButton btnExportData = new JButton("Export Data");
        JButton btnImportData = new JButton("Import Data");
        
        styleActionButton(btnGenerateReport, "Generate system reports");
        styleActionButton(btnSystemHealth, "Check system health");
        styleActionButton(btnExportData, "Export system data");
        styleActionButton(btnImportData, "Import data");
        
        btnGenerateReport.addActionListener(e -> generateReports());
        btnSystemHealth.addActionListener(e -> checkSystemHealth());
        btnExportData.addActionListener(e -> exportData());
        btnImportData.addActionListener(e -> importData());
        
        quickActionsPanel.add(btnGenerateReport);
        quickActionsPanel.add(btnSystemHealth);
        quickActionsPanel.add(btnExportData);
        quickActionsPanel.add(btnImportData);
        
        panel.add(quickActionsPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(TAB_BG_UNSELECTED);
        card.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 3, 0, accentColor),
            new EmptyBorder(15, 20, 15, 20)
        ));
        
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        valueLabel.setForeground(TITLE_COLOR_DARK);
        card.add(valueLabel, BorderLayout.CENTER);
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        titleLabel.setForeground(TEXT_COLOR_DARK);
        card.add(titleLabel, BorderLayout.SOUTH);
        
        return card;
    }

    private JPanel createVerificationTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);
        
        JButton btnRefresh = new JButton("Refresh");
        JButton btnExportList = new JButton("Export List");
        JButton btnBulkApprove = new JButton("Bulk Approve");
        
        styleActionButton(btnRefresh, "Refresh verification list");
        styleActionButton(btnExportList, "Export to CSV");
        styleActionButton(btnBulkApprove, "Approve selected orphanages");
        
        btnRefresh.addActionListener(e -> loadVerificationQueue());
        btnExportList.addActionListener(e -> exportVerificationList());
        btnBulkApprove.addActionListener(e -> bulkApprove());
        
        toolbar.add(btnRefresh);
        toolbar.add(btnExportList);
        toolbar.add(btnBulkApprove);
        panel.add(toolbar, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {
            "Select", "Orphanage Name", "Registration #", "Contact Person", 
            "Email", "Phone", "Province", "Date Applied", "Status", "Actions"
        };
        
        verificationModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return Boolean.class;
                return String.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 9;
            }
        };
        
        JTable verificationTable = new JTable(verificationModel);
        styleTable(verificationTable);
        
        // Add button renderer and editor for Actions column
        verificationTable.getColumnModel().getColumn(9).setCellRenderer(new ButtonPanelRenderer());
        verificationTable.getColumnModel().getColumn(9).setCellEditor(new ButtonPanelEditor());
        
        // Set column widths
        verificationTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        verificationTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        verificationTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        verificationTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        verificationTable.getColumnModel().getColumn(4).setPreferredWidth(180);
        verificationTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        verificationTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        verificationTable.getColumnModel().getColumn(7).setPreferredWidth(100);
        verificationTable.getColumnModel().getColumn(8).setPreferredWidth(80);
        verificationTable.getColumnModel().getColumn(9).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(verificationTable);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createUserManagementTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setOpaque(false);
        
        JLabel lblSearchUser = new JLabel("Search:");
        styleFormLabel(lblSearchUser);
        txtUserSearch = new JTextField(20);
        styleTextField(txtUserSearch);
        
        JLabel lblUserRole = new JLabel("Role:");
        styleFormLabel(lblUserRole);
        cmbUserRole = new JComboBox<>(new String[]{
            "All Roles", "Admin", "OrphanageStaff", "Donor", "Volunteer"
        });
        styleComboBox(cmbUserRole);
        
        JLabel lblUserStatus = new JLabel("Status:");
        styleFormLabel(lblUserStatus);
        cmbUserStatus = new JComboBox<>(new String[]{
            "All", "Active", "Suspended", "Pending"
        });
        styleComboBox(cmbUserStatus);
        
        JButton btnUserSearch = new JButton("Search");
        JButton btnAddUser = new JButton("Add User");
        
        styleActionButton(btnUserSearch, "Search users");
        styleActionButton(btnAddUser, "Add new user");
        
        btnUserSearch.addActionListener(e -> searchUsers());
        btnAddUser.addActionListener(e -> showAddUserDialog());
        
        searchPanel.add(lblSearchUser);
        searchPanel.add(txtUserSearch);
        searchPanel.add(lblUserRole);
        searchPanel.add(cmbUserRole);
        searchPanel.add(lblUserStatus);
        searchPanel.add(cmbUserStatus);
        searchPanel.add(btnUserSearch);
        searchPanel.add(btnAddUser);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {
            "Username", "Full Name", "Email", "Role", "Status", 
            "Last Login", "Registered", "Actions"
        };
        
        userModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };
        
        JTable userTable = new JTable(userModel);
        styleTable(userTable);
        
        // Add button renderer and editor for Actions column
        userTable.getColumnModel().getColumn(7).setCellRenderer(new ButtonPanelRenderer());
        userTable.getColumnModel().getColumn(7).setCellEditor(new ButtonPanelEditor());
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createReportsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Report Types Panel
        JPanel reportTypesPanel = new JPanel(new GridLayout(3, 3, 15, 15));
        reportTypesPanel.setOpaque(false);
        reportTypesPanel.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR_DARK),
            " Available Reports ",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font(Font.SANS_SERIF, Font.BOLD, 14),
            TITLE_COLOR_DARK
        ));
        
        // Report buttons
        String[] reportTypes = {
            "User Statistics", "Donation Summary", "Orphanage Overview",
            "Volunteer Activity", "Resource Requests", "Financial Report",
            "System Usage", "Audit Summary", "Custom Report"
        };
        
        for (String reportType : reportTypes) {
            JButton btnReport = new JButton(reportType);
            styleActionButton(btnReport, "Generate " + reportType);
            btnReport.addActionListener(e -> generateReport(reportType));
            reportTypesPanel.add(btnReport);
        }
        
        panel.add(reportTypesPanel, BorderLayout.NORTH);
        
        // Report Preview Panel
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setOpaque(false);
        previewPanel.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR_DARK),
            " Report Preview ",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font(Font.SANS_SERIF, Font.BOLD, 14),
            TITLE_COLOR_DARK
        ));
        
        JTextArea reportPreview = new JTextArea(15, 50);
        reportPreview.setEditable(false);
        reportPreview.setBackground(TABLE_CELL_BG);
        reportPreview.setForeground(TABLE_CELL_FG);
        reportPreview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane previewScroll = new JScrollPane(reportPreview);
        styleScrollPane(previewScroll);
        previewPanel.add(previewScroll, BorderLayout.CENTER);
        
        panel.add(previewPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createAuditLogTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setOpaque(false);
        
        JLabel lblDateFrom = new JLabel("From:");
        JTextField txtDateFrom = new JTextField(10);
        JLabel lblDateTo = new JLabel("To:");
        JTextField txtDateTo = new JTextField(10);
        JLabel lblAction = new JLabel("Action:");
        JComboBox<String> cmbAction = new JComboBox<>(new String[]{
            "All", "Login", "Logout", "Create", "Update", "Delete", "Verify"
        });
        JButton btnFilterAudit = new JButton("Filter");
        
        styleFormLabel(lblDateFrom);
        styleTextField(txtDateFrom);
        styleFormLabel(lblDateTo);
        styleTextField(txtDateTo);
        styleFormLabel(lblAction);
        styleComboBox(cmbAction);
        styleActionButton(btnFilterAudit, "Filter audit log");
        
        filterPanel.add(lblDateFrom);
        filterPanel.add(txtDateFrom);
        filterPanel.add(lblDateTo);
        filterPanel.add(txtDateTo);
        filterPanel.add(lblAction);
        filterPanel.add(cmbAction);
        filterPanel.add(btnFilterAudit);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {
            "Timestamp", "User", "Action", "Entity Type", 
            "Entity ID", "Details", "IP Address"
        };
        
        auditModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable auditTable = new JTable(auditModel);
        styleTable(auditTable);
        
        JScrollPane scrollPane = new JScrollPane(auditTable);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createSettingsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // System Settings
        JLabel lblSystemSettings = new JLabel("System Settings");
        lblSystemSettings.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        lblSystemSettings.setForeground(TITLE_COLOR_DARK);
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        settingsPanel.add(lblSystemSettings, gbc);
        
        gbc.gridwidth = 1;
        
        // Session Timeout
        gbc.gridx = 0; gbc.gridy = row;
        settingsPanel.add(new JLabel("Session Timeout (minutes):"), gbc);
        gbc.gridx = 1;
        JSpinner spnTimeout = new JSpinner(new SpinnerNumberModel(30, 5, 120, 5));
        settingsPanel.add(spnTimeout, gbc);
        row++;
        
        // Max Login Attempts
        gbc.gridx = 0; gbc.gridy = row;
        settingsPanel.add(new JLabel("Max Login Attempts:"), gbc);
        gbc.gridx = 1;
        JSpinner spnMaxAttempts = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        settingsPanel.add(spnMaxAttempts, gbc);
        row++;
        
        // Email Settings
        row++;
        JLabel lblEmailSettings = new JLabel("Email Settings");
        lblEmailSettings.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        lblEmailSettings.setForeground(TITLE_COLOR_DARK);
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        settingsPanel.add(lblEmailSettings, gbc);
        
        gbc.gridwidth = 1;
        
        // SMTP Server
        gbc.gridx = 0; gbc.gridy = row;
        settingsPanel.add(new JLabel("SMTP Server:"), gbc);
        gbc.gridx = 1;
        JTextField txtSmtpServer = new JTextField(25);
        styleTextField(txtSmtpServer);
        settingsPanel.add(txtSmtpServer, gbc);
        row++;
        
        // SMTP Port
        gbc.gridx = 0; gbc.gridy = row;
        settingsPanel.add(new JLabel("SMTP Port:"), gbc);
        gbc.gridx = 1;
        JTextField txtSmtpPort = new JTextField(25);
        styleTextField(txtSmtpPort);
        settingsPanel.add(txtSmtpPort, gbc);
        row++;
        
        // Save Button
        row++;
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        JButton btnSaveSettings = new JButton("Save Settings");
        styleActionButton(btnSaveSettings, "Save system settings");
        btnSaveSettings.addActionListener(e -> saveSettings());
        buttonPanel.add(btnSaveSettings);
        
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        settingsPanel.add(buttonPanel, gbc);
        
        JScrollPane scrollPane = new JScrollPane(settingsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    // Implementation methods
    private void loadInitialData() {
        loadSystemStatistics();
        loadVerificationQueue();
        loadUsers();
        loadAuditLog();
    }

    private void loadSystemStatistics() {
        adminService.getSystemStatistics()
        .onSuccess(stats -> {
            SwingUtilities.invokeLater(() -> {
                lblTotalUsers.setText(String.valueOf(stats.totalUsers()));
                lblVerifiedOrphanages.setText(String.valueOf(stats.totalOrphanages()));
                lblPendingVerifications.setText(String.valueOf(stats.pendingVerifications()));
                lblActiveRequests.setText(String.valueOf(stats.openRequests()));
                lblTotalDonations.setText(String.format("R%.2f", stats.totalDonations()));
                lblActiveVolunteers.setText(String.valueOf(stats.volunteers()));
            });
        })
        .onFailure(this::showErrorMessage);
    }

    private void loadVerificationQueue() {
        adminService.getPendingVerifications()
            .onSuccess(orphanages -> {
                SwingUtilities.invokeLater(() -> {
                    verificationModel.setRowCount(0);
                    orphanages.forEach(o -> {
                        verificationModel.addRow(new Object[]{
                            false, // Checkbox
                            o.name(),
                            o.registrationNumber().getOrElse(""),
                            o.contactPerson(),
                            o.email(),
                            o.phoneNumber(),
                            o.province(),
                            o.dateRegistered(),
                            o.verificationStatus(),
                            "Actions"
                        });
                    });
                });
            })
            .onFailure(this::showErrorMessage);
    }

    private void loadUsers() {
        userService.getAllUsers()
            .onSuccess(users -> {
                SwingUtilities.invokeLater(() -> {
                    userModel.setRowCount(0);
                    users.forEach(u -> {
                        userModel.addRow(new Object[]{
                            u.username(),
                            u.fullName(),
                            u.email(),
                            u.userRole(),
                            u.accountStatus(),
                            u.lastLogin().map(Object::toString).getOrElse("Never"),
                            u.dateRegistered(),
                            "Actions"
                        });
                    });
                });
            })
            .onFailure(this::showErrorMessage);
    }

    private void loadAuditLog() {
        auditService.getRecentAuditLogs(100)
            .onSuccess(logs -> {
                SwingUtilities.invokeLater(() -> {
                    auditModel.setRowCount(0);
                    logs.forEach(log -> {
                        auditModel.addRow(new Object[]{
                            log.timestamp(),
                            log.username(),
                            log.action(),
                            log.entityType(),
                            log.entityId(),
                            log.details(),
                            log.ipAddress().getOrElse("")
                        });
                    });
                });
            })
            .onFailure(this::showErrorMessage);
    }

    // All other helper methods...
    private void performBackup() {
        adminService.backupDatabase()
            .onSuccess(path -> showSuccessMessage("Backup created at: " + path))
            .onFailure(this::showErrorMessage);
    }

    private void generateReport(String reportType) {
    // Convert string to enum and create basic parameters
    AdminService.ReportType type = AdminService.ReportType.valueOf(reportType.toUpperCase());
    AdminService.ReportParameters params = AdminService.ReportParameters.basic(
        LocalDateTime.now().minusMonths(1), 
        LocalDateTime.now()
    );
    Integer adminId = SessionManager.getInstance()
        .getCurrentUserId()
        .getOrElse(1);
    
    adminService.generateReport(type, params, adminId)
        .onSuccess(report -> {
            // Show report
            Try.of(() -> {
                JasperPrint print = (JasperPrint) JRLoader.loadObjectFromFile(report);
                JasperViewer.viewReport(print, false);
                return null;
            });
        })
        .onFailure(this::showErrorMessage);
}

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorMessage(Throwable error) {
        JOptionPane.showMessageDialog(this,
            "Error: " + error.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }

    // Styling methods (same as other panels)
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
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = BUTTON_BG_DARK;
                this.trackColor = DARK_BG_END;
            }
            
            @Override
            protected JButton createDecreaseButton(int o) {
                return createZeroButton();
            }
            
            @Override
            protected JButton createIncreaseButton(int o) {
                return createZeroButton();
            }
            
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
    }

    // Other implementation methods...
    private void searchUsers() { /* Implementation */ }
    private void showAddUserDialog() { /* Implementation */ }
    private void generateReports() { /* Implementation */ }
    private void checkSystemHealth() { /* Implementation */ }
    private void exportData() { /* Implementation */ }
    private void importData() { /* Implementation */ }
    private void exportVerificationList() { /* Implementation */ }
    private void bulkApprove() { /* Implementation */ }
    private void saveSettings() { /* Implementation */ }

    // Custom button panel renderer and editor classes
    class ButtonPanelRenderer extends JPanel implements TableCellRenderer {
        public ButtonPanelRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected, boolean hasFocus,
                                                     int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            return this;
        }
    }

    class ButtonPanelEditor extends DefaultCellEditor {
        public ButtonPanelEditor() {
            super(new JCheckBox());
        }
    }
}