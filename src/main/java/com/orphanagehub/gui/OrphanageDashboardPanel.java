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

public class OrphanageDashboardPanel extends JPanel {
    private final OrphanageHubApp mainApp;
    private final OrphanageService orphanageService;
    private final DonorService donorService;
    private final VolunteerService volunteerService;
    
    private DefaultTableModel resourceModel;
    private DefaultTableModel donationModel;
    private DefaultTableModel volunteerModel;
    private DefaultTableModel notificationModel;
    
    private JTabbedPane tabbedPane;
    private JLabel lblActiveRequests;
    private JLabel lblPendingDonations;
    private JLabel lblActiveVolunteers;
    
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
    private static final Color ACCENT_COLOR_ORANGE = new Color(230, 145, 56);
    private static final Color ACCENT_COLOR_BLUE = new Color(72, 149, 239);
    private static final Color ACCENT_COLOR_GREEN = new Color(87, 190, 106);

    public OrphanageDashboardPanel(OrphanageHubApp app) {
        this.mainApp = app;
        this.orphanageService = new OrphanageService();
        this.donorService = new DonorService();
        this.volunteerService = new VolunteerService();
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
        JLabel iconLabel = new JLabel("\u2302"); // House symbol
        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        iconLabel.setForeground(new Color(135, 206, 250));
        
        String orphanageName = SessionManager.getInstance()
            .getAttribute("orphanageName")
            .map(Object::toString)
            .getOrElse("Orphanage Dashboard");
        
        JLabel nameLabel = new JLabel(orphanageName);
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        nameLabel.setForeground(TITLE_COLOR_DARK);
        titleGroup.add(iconLabel);
        titleGroup.add(nameLabel);
        headerPanel.add(titleGroup, BorderLayout.WEST);
        
        // User group
        JPanel userGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userGroup.setOpaque(false);
        
        String username = SessionManager.getInstance()
            .getAttribute("currentUsername")
            .map(Object::toString)
            .getOrElse("Staff User");
        
        JLabel userLabel = new JLabel("Staff: " + username);
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        userLabel.setForeground(TEXT_COLOR_DARK);
        
        JButton btnNotifications = new JButton("Notifications");
        styleActionButton(btnNotifications, "View notifications");
        btnNotifications.setPreferredSize(new Dimension(120, 30));
        btnNotifications.addActionListener(e -> showNotifications());
        
        JButton btnLogout = new JButton("Logout");
        styleActionButton(btnLogout, "Logout and return to welcome screen");
        btnLogout.setPreferredSize(new Dimension(100, 30));
        btnLogout.setBackground(new Color(192, 57, 43));
        btnLogout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnLogout.setBackground(new Color(231, 76, 60));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnLogout.setBackground(new Color(192, 57, 43));
            }
        });
        btnLogout.addActionListener(e -> {
            SessionManager.getInstance().clear();
            mainApp.navigateTo(OrphanageHubApp.HOME_PANEL);
        });
        
        userGroup.add(userLabel);
        userGroup.add(btnNotifications);
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
        
        pane.addTab("Overview", createOverviewTab());
        pane.addTab("Resource Requests", createResourceRequestsTab());
        pane.addTab("Donations", createDonationsTab());
        pane.addTab("Volunteers", createVolunteersTab());
        pane.addTab("Orphanage Profile", createProfileTab());
        
        return pane;
    }

    private JPanel createOverviewTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        // Stats cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        statsPanel.setOpaque(false);
        
        lblActiveRequests = new JLabel("0", SwingConstants.CENTER);
        lblPendingDonations = new JLabel("0", SwingConstants.CENTER);
        lblActiveVolunteers = new JLabel("0", SwingConstants.CENTER);
        
        statsPanel.add(createStatCard("Active Requests", lblActiveRequests, ACCENT_COLOR_ORANGE));
        statsPanel.add(createStatCard("Pending Donations", lblPendingDonations, ACCENT_COLOR_BLUE));
        statsPanel.add(createStatCard("Active Volunteers", lblActiveVolunteers, ACCENT_COLOR_GREEN));
        
        panel.add(statsPanel, BorderLayout.NORTH);
        
        // Recent activity
        JPanel activityPanel = new JPanel(new BorderLayout());
        activityPanel.setOpaque(false);
        activityPanel.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR_DARK),
            " Recent Activity ",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font(Font.SANS_SERIF, Font.BOLD, 14),
            TITLE_COLOR_DARK
        ));
        
        JTextArea activityLog = new JTextArea(10, 40);
        activityLog.setEditable(false);
        activityLog.setBackground(TABLE_CELL_BG);
        activityLog.setForeground(TABLE_CELL_FG);
        activityLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane activityScroll = new JScrollPane(activityLog);
        styleScrollPane(activityScroll);
        activityPanel.add(activityScroll, BorderLayout.CENTER);
        
        panel.add(activityPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(TAB_BG_UNSELECTED);
        card.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 3, 0, accentColor),
            new EmptyBorder(15, 20, 15, 20)
        ));
        
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
        valueLabel.setForeground(TITLE_COLOR_DARK);
        card.add(valueLabel, BorderLayout.CENTER);
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        titleLabel.setForeground(TEXT_COLOR_DARK);
        card.add(titleLabel, BorderLayout.SOUTH);
        
        return card;
    }

    private JPanel createResourceRequestsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);
        
        JButton btnAdd = new JButton("Add Request");
        JButton btnEdit = new JButton("Edit Selected");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnRefresh = new JButton("Refresh");
        
        styleActionButton(btnAdd, "Create a new resource request");
        styleActionButton(btnEdit, "Modify the selected request");
        styleActionButton(btnDelete, "Remove the selected request");
        styleActionButton(btnRefresh, "Refresh the list");
        
        btnDelete.setBackground(new Color(192, 57, 43));
        btnDelete.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnDelete.setBackground(new Color(231, 76, 60));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnDelete.setBackground(new Color(192, 57, 43));
            }
        });
        
        btnAdd.addActionListener(e -> showAddResourceDialog());
        btnEdit.addActionListener(e -> showEditResourceDialog());
        btnDelete.addActionListener(e -> deleteSelectedResource());
        btnRefresh.addActionListener(e -> loadResourceRequests());
        
        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {
            "ID", "Category", "Description", "Quantity Needed", 
            "Quantity Fulfilled", "Unit", "Urgency", "Status", "Estimated Value"
        };
        
        resourceModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable resourceTable = new JTable(resourceModel);
        styleTable(resourceTable);
        
        // Set column widths
        resourceTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        resourceTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        resourceTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        resourceTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        resourceTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        resourceTable.getColumnModel().getColumn(5).setPreferredWidth(60);
        resourceTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        resourceTable.getColumnModel().getColumn(7).setPreferredWidth(80);
        resourceTable.getColumnModel().getColumn(8).setPreferredWidth(100);
        
        JScrollPane scrollPane = new JScrollPane(resourceTable);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createDonationsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);
        
        JButton btnAcknowledge = new JButton("Acknowledge Selected");
        JButton btnExport = new JButton("Export Report");
        JButton btnRefresh = new JButton("Refresh");
        JButton btnThankYou = new JButton("Send Thank You");
        
        styleActionButton(btnAcknowledge, "Acknowledge receipt of donation");
        styleActionButton(btnExport, "Export donations to report");
        styleActionButton(btnRefresh, "Refresh donation list");
        styleActionButton(btnThankYou, "Send thank you message to donor");
        
        btnAcknowledge.addActionListener(e -> acknowledgeSelectedDonation());
        btnRefresh.addActionListener(e -> loadDonations());
        btnThankYou.addActionListener(e -> sendThankYouMessage());
        
        toolbar.add(btnAcknowledge);
        toolbar.add(btnThankYou);
        toolbar.add(btnExport);
        toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {
            "ID", "Donor Name", "Type", "Amount/Items", 
            "Payment Method", "Date", "Status", "Acknowledged", "Thank You Sent"
        };
        
        donationModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 7 || column == 8) return Boolean.class;
                return String.class;
            }
        };
        
        JTable donationTable = new JTable(donationModel);
        styleTable(donationTable);
        
        JScrollPane scrollPane = new JScrollPane(donationTable);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createVolunteersTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);
        
        JButton btnCreateOpportunity = new JButton("Create Opportunity");
        JButton btnViewApplications = new JButton("View Applications");
        JButton btnManageVolunteers = new JButton("Manage Volunteers");
        JButton btnRefresh = new JButton("Refresh");
        
        styleActionButton(btnCreateOpportunity, "Create new volunteer opportunity");
        styleActionButton(btnViewApplications, "Review volunteer applications");
        styleActionButton(btnManageVolunteers, "Manage active volunteers");
        styleActionButton(btnRefresh, "Refresh the list");
        
        btnCreateOpportunity.addActionListener(e -> showCreateOpportunityDialog());
        btnViewApplications.addActionListener(e -> showApplicationsDialog());
        btnRefresh.addActionListener(e -> loadVolunteerOpportunities());
        
        toolbar.add(btnCreateOpportunity);
        toolbar.add(btnViewApplications);
        toolbar.add(btnManageVolunteers);
        toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {
            "ID", "Title", "Description", "Skills Required", 
            "Time Commitment", "Slots Available", "Applications", "Status"
        };
        
        volunteerModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable volunteerTable = new JTable(volunteerModel);
        styleTable(volunteerTable);
        
        JScrollPane scrollPane = new JScrollPane(volunteerTable);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createProfileTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Load orphanage data
        String orphanageId = SessionManager.getInstance()
            .getAttribute("currentOrphanageId")
            .map(Object::toString)
            .getOrElse("");
        
        int row = 0;
        
        // Basic Information
        JLabel lblBasicInfo = new JLabel("Basic Information");
        lblBasicInfo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        lblBasicInfo.setForeground(TITLE_COLOR_DARK);
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        formPanel.add(lblBasicInfo, gbc);
        
        gbc.gridwidth = 1;
        
        // Name
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblName = new JLabel("Orphanage Name:");
        styleFormLabel(lblName);
        formPanel.add(lblName, gbc);
        
        gbc.gridx = 1;
        JTextField txtName = new JTextField(30);
        styleTextField(txtName);
        formPanel.add(txtName, gbc);
        row++;
        
        // Registration Number
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblRegNumber = new JLabel("Registration Number:");
        styleFormLabel(lblRegNumber);
        formPanel.add(lblRegNumber, gbc);
        
        gbc.gridx = 1;
        JTextField txtRegNumber = new JTextField(30);
        styleTextField(txtRegNumber);
        formPanel.add(txtRegNumber, gbc);
        row++;
        
        // Address
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblAddress = new JLabel("Address:");
        styleFormLabel(lblAddress);
        formPanel.add(lblAddress, gbc);
        
        gbc.gridx = 1;
        JTextArea txtAddress = new JTextArea(3, 30);
        styleTextArea(txtAddress);
        formPanel.add(new JScrollPane(txtAddress), gbc);
        row++;
        
        // Province
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblProvince = new JLabel("Province:");
        styleFormLabel(lblProvince);
        formPanel.add(lblProvince, gbc);
        
        gbc.gridx = 1;
        JComboBox<String> cmbProvince = new JComboBox<>(new String[]{
            "Eastern Cape", "Free State", "Gauteng", "KwaZulu-Natal",
            "Limpopo", "Mpumalanga", "North West", "Northern Cape", "Western Cape"
        });
        styleComboBox(cmbProvince);
        formPanel.add(cmbProvince, gbc);
        row++;
        
        // Contact Information
        row++;
        JLabel lblContactInfo = new JLabel("Contact Information");
        lblContactInfo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        lblContactInfo.setForeground(TITLE_COLOR_DARK);
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        formPanel.add(lblContactInfo, gbc);
        
        gbc.gridwidth = 1;
        
        // Contact Person
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblContact = new JLabel("Contact Person:");
        styleFormLabel(lblContact);
        formPanel.add(lblContact, gbc);
        
        gbc.gridx = 1;
        JTextField txtContact = new JTextField(30);
        styleTextField(txtContact);
        formPanel.add(txtContact, gbc);
        row++;
        
        // Phone
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblPhone = new JLabel("Phone Number:");
        styleFormLabel(lblPhone);
        formPanel.add(lblPhone, gbc);
        
        gbc.gridx = 1;
        JTextField txtPhone = new JTextField(30);
        styleTextField(txtPhone);
        formPanel.add(txtPhone, gbc);
        row++;
        
        // Email
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblEmail = new JLabel("Email:");
        styleFormLabel(lblEmail);
        formPanel.add(lblEmail, gbc);
        
        gbc.gridx = 1;
        JTextField txtEmail = new JTextField(30);
        styleTextField(txtEmail);
        formPanel.add(txtEmail, gbc);
        row++;
        
        // Banking Information
        row++;
        JLabel lblBankingInfo = new JLabel("Banking Information");
        lblBankingInfo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        lblBankingInfo.setForeground(TITLE_COLOR_DARK);
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        formPanel.add(lblBankingInfo, gbc);
        
        gbc.gridwidth = 1;
        
        // Bank Name
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblBank = new JLabel("Bank Name:");
        styleFormLabel(lblBank);
        formPanel.add(lblBank, gbc);
        
        gbc.gridx = 1;
        JTextField txtBank = new JTextField(30);
        styleTextField(txtBank);
        formPanel.add(txtBank, gbc);
        row++;
        
        // Account Number
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblAccount = new JLabel("Account Number:");
        styleFormLabel(lblAccount);
        formPanel.add(lblAccount, gbc);
        
        gbc.gridx = 1;
        JTextField txtAccount = new JTextField(30);
        styleTextField(txtAccount);
        formPanel.add(txtAccount, gbc);
        row++;
        
        // Save button
        row++;
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        JButton btnSave = new JButton("Save Profile");
        styleActionButton(btnSave, "Save profile changes");
        btnSave.addActionListener(e -> saveProfile());
        buttonPanel.add(btnSave);
        
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);
        
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void loadInitialData() {
        loadResourceRequests();
        loadDonations();
        loadVolunteerOpportunities();
        updateStatistics();
    }

    private void loadResourceRequests() {
        String orphanageId = SessionManager.getInstance()
            .getAttribute("currentOrphanageId")
            .map(Object::toString)
            .getOrElse("");
        
        if (!orphanageId.isEmpty()) {
            orphanageService.getResourceRequests(orphanageId)
                .onSuccess(requests -> {
                    SwingUtilities.invokeLater(() -> {
                        resourceModel.setRowCount(0);
                        requests.forEach(req -> {
                            resourceModel.addRow(new Object[]{
                                req.requestId(),
                                req.resourceType(),
                                req.resourceDescription(),
                                req.quantityNeeded(),
                                req.quantityFulfilled(),
                                req.unit().getOrElse(""),
                                req.urgencyLevel(),
                                req.status(),
                                req.estimatedValue().map(v -> "$" + v).getOrElse("")
                            });
                        });
                        
                        // Update stats
                        long activeCount = requests
                            .filter(r -> "Open".equalsIgnoreCase(r.status()))
                            .length();
                        lblActiveRequests.setText(String.valueOf(activeCount));
                    });
                })
                .onFailure(this::showErrorMessage);
        }
    }

    private void loadDonations() {
        String orphanageId = SessionManager.getInstance()
            .getAttribute("currentOrphanageId")
            .map(Object::toString)
            .getOrElse("");
        
        if (!orphanageId.isEmpty()) {
            donorService.getDonationsWithDonorForOrphanage(orphanageId)
                .onSuccess(donations -> {
                    SwingUtilities.invokeLater(() -> {
                        donationModel.setRowCount(0);
                        donations.forEach(dwd -> {
                            Donation d = dwd.donation();
                            donationModel.addRow(new Object[]{
                                d.donationId(),
                                dwd.donorName(),
                                d.donationType(),
                                d.amount().map(a -> "$" + a)
                                    .getOrElse(d.quantity().map(q -> q + " " + d.unit().getOrElse("items"))
                                .getOrElse("")),
                                d.paymentMethod().getOrElse("N/A"),
                                d.donationDate(),
                                d.status(),
                                d.isComplete(),
                                d.thankYouSent()
                            });
                        });
                        
                        // Update stats
                        long pendingCount = donations
                            .filter(dwd -> !dwd.donation().isComplete())
                            .length();
                        lblPendingDonations.setText(String.valueOf(pendingCount));
                    });
                })
                .onFailure(this::showErrorMessage);
        }
    }

    private void loadVolunteerOpportunities() {
        String orphanageId = SessionManager.getInstance()
            .getAttribute("currentOrphanageId")
            .map(Object::toString)
            .getOrElse("");
        
        if (!orphanageId.isEmpty()) {
            volunteerService.getOpportunitiesForOrphanage(orphanageId)
                .onSuccess(opportunities -> {
                    SwingUtilities.invokeLater(() -> {
                        volunteerModel.setRowCount(0);
                        opportunities.forEach(opp -> {
                            volunteerModel.addRow(new Object[]{
                                opp.opportunityId(),
                                opp.title(),
                                opp.description(),
                                opp.skillsRequired(),
                                opp.timeCommitment(),
                                opp.slotsAvailable(),
                                opp.applicationCount(),
                                opp.status()
                            });
                        });
                        
                        // Update stats
                        long activeCount = opportunities
                            .filter(o -> "Open".equalsIgnoreCase(o.status()))
                            .map(o -> o.applicationCount())
                            .sum().longValue();
                        lblActiveVolunteers.setText(String.valueOf(activeCount));
                    });
                })
                .onFailure(this::showErrorMessage);
        }
    }

    private void updateStatistics() {
        // Additional statistics updates if needed
    }

    private void showAddResourceDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Add Resource Request", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Category
        gbc.gridx = 0; gbc.gridy = row;
        dialog.add(new JLabel("Category:"), gbc);
        JComboBox<String> cmbCategory = new JComboBox<>(new String[]{
            "Food", "Clothing", "Education", "Medical", "Infrastructure", "Other"
        });
        gbc.gridx = 1;
        dialog.add(cmbCategory, gbc);
        row++;
        
        // Description
        gbc.gridx = 0; gbc.gridy = row;
        dialog.add(new JLabel("Description:"), gbc);
        JTextField txtDescription = new JTextField(25);
        gbc.gridx = 1;
        dialog.add(txtDescription, gbc);
        row++;
        
        // Quantity
        gbc.gridx = 0; gbc.gridy = row;
        dialog.add(new JLabel("Quantity Needed:"), gbc);
        JSpinner spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        gbc.gridx = 1;
        dialog.add(spnQuantity, gbc);
        row++;
        
        // Unit
        gbc.gridx = 0; gbc.gridy = row;
        dialog.add(new JLabel("Unit:"), gbc);
        JTextField txtUnit = new JTextField(25);
        txtUnit.setToolTipText("e.g., kg, pieces, boxes");
        gbc.gridx = 1;
        dialog.add(txtUnit, gbc);
        row++;
        
        // Urgency
        gbc.gridx = 0; gbc.gridy = row;
        dialog.add(new JLabel("Urgency:"), gbc);
        JComboBox<String> cmbUrgency = new JComboBox<>(new String[]{
            "Low", "Medium", "High", "Critical"
        });
        gbc.gridx = 1;
        dialog.add(cmbUrgency, gbc);
        row++;
        
        // Estimated Value
        gbc.gridx = 0; gbc.gridy = row;
        dialog.add(new JLabel("Estimated Value ($):"), gbc);
        JSpinner spnValue = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000000.0, 10.0));
        gbc.gridx = 1;
        dialog.add(spnValue, gbc);
        row++;
        
        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        
        btnSave.addActionListener(e -> {
            String orphanageId = SessionManager.getInstance()
                .getAttribute("currentOrphanageId")
                .map(Object::toString)
                .getOrElse("");
            
            String userId = SessionManager.getInstance()
                .getAttribute("currentUserId")
                .map(Object::toString)
                .getOrElse("");
            
            ResourceRequest request = ResourceRequest.createBasic(
    Integer.valueOf(orphanageId),
    (String) cmbCategory.getSelectedItem(),
    txtDescription.getText(),
    (Double) spnQuantity.getValue(),
    (String) cmbUrgency.getSelectedItem(),
    Integer.valueOf(userId)
);
            
            orphanageService.manageResourceRequest(request)
                .onSuccess(v -> {
                    dialog.dispose();
                    loadResourceRequests();
                    showSuccessMessage("Resource request added successfully!");
                })
                .onFailure(ex -> showErrorMessage(ex.getMessage()));
        });
        
        btnCancel.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);
        
        dialog.setVisible(true);
    }

    private void showEditResourceDialog() {
        // Similar to add dialog but with pre-filled values
    }

    private void deleteSelectedResource() {
        // Implementation for deleting selected resource
    }

    private void acknowledgeSelectedDonation() {
        // Implementation for acknowledging donation
    }

    private void sendThankYouMessage() {
        // Implementation for sending thank you message
    }

    private void showCreateOpportunityDialog() {
        // Similar dialog for creating volunteer opportunities
    }

    private void showApplicationsDialog() {
        // Dialog showing volunteer applications
    }

    private void showNotifications() {
        // Show notifications dialog
    }

    private void saveProfile() {
        // Save orphanage profile
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

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Styling methods
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