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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VolunteerDashboardPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(VolunteerDashboardPanel.class);
    
    private final OrphanageHubApp mainApp;
    private final VolunteerService volunteerService;
    private final OrphanageService orphanageService;
    
    private DefaultTableModel opportunitiesModel;
    private DefaultTableModel applicationsModel;
    private DefaultTableModel historyModel;
    private DefaultTableModel eventsModel;
    
    private JTextField txtSearch;
    private JTextField txtSkills;
    private JComboBox<String> cmbLocation;
    private JComboBox<String> cmbTime;
    private JComboBox<String> cmbCategory;
    private JTabbedPane tabbedPane;
    
    private JLabel lblTotalApplications;
    private JLabel lblAcceptedApplications;
    private JLabel lblHoursVolunteered;
    private JLabel lblOrphanagesHelped;
    
    // Profile fields
    private JTextField txtName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextArea txtSkillsProfile;
    private JTextArea txtExperience;
    private JTextField txtAvailabilityProfile;
    
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
    private static final Color BUTTON_APPLY_BG = new Color(87, 190, 106);
    private static final Color BUTTON_APPLY_HOVER_BG = new Color(97, 200, 116);
    private static final Color ACCENT_COLOR_YELLOW = new Color(255, 215, 0);
    private static final Color ACCENT_COLOR_BLUE = new Color(72, 149, 239);
    private static final Color ACCENT_COLOR_GREEN = new Color(87, 190, 106);
    private static final Color ACCENT_COLOR_RED = new Color(231, 76, 60);

    public VolunteerDashboardPanel(OrphanageHubApp app) {
        this.mainApp = app;
        this.volunteerService = new VolunteerService();
        this.orphanageService = new OrphanageService();
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
        JLabel iconLabel = new JLabel("\u2605"); // Star symbol
        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        iconLabel.setForeground(ACCENT_COLOR_YELLOW);
        JLabel nameLabel = new JLabel("Volunteer Dashboard");
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        nameLabel.setForeground(TITLE_COLOR_DARK);
        titleGroup.add(iconLabel);
        titleGroup.add(nameLabel);
        headerPanel.add(titleGroup, BorderLayout.WEST);
        
        // Stats and user group
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        
        // Quick stats
        JPanel statsPanel = createQuickStatsPanel();
        rightPanel.add(statsPanel);
        
        // User info - Fixed to use correct SessionManager method
        String username = SessionManager.getInstance()
            .getCurrentUsername()
            .getOrElse("Volunteer");
        
        JLabel userLabel = new JLabel("Welcome!");
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        userLabel.setForeground(TEXT_COLOR_DARK);
        
        JButton btnNotifications = new JButton("Notifications");
        styleActionButton(btnNotifications, "View your notifications");
        btnNotifications.setPreferredSize(new Dimension(120, 30));
        btnNotifications.addActionListener(e -> showNotifications());
        
        JButton btnLogout = new JButton("Logout");
        styleActionButton(btnLogout, "Logout and return to welcome screen");
        btnLogout.setPreferredSize(new Dimension(100, 30));
        btnLogout.setBackground(new Color(192, 57, 43));
        btnLogout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnLogout.setBackground(ACCENT_COLOR_RED);
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
        
        rightPanel.add(userLabel);
        rightPanel.add(btnNotifications);
        rightPanel.add(btnLogout);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }

    private JPanel createQuickStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setOpaque(false);
        
        lblTotalApplications = createStatLabel("0", "Apps");
        lblAcceptedApplications = createStatLabel("0", "Accepted");
        lblHoursVolunteered = createStatLabel("0", "Hours");
        lblOrphanagesHelped = createStatLabel("0", "Orgs");
        
        panel.add(lblTotalApplications);
        panel.add(lblAcceptedApplications);
        panel.add(lblHoursVolunteered);
        panel.add(lblOrphanagesHelped);
        
        return panel;
    }

    private JLabel createStatLabel(String value, String title) {
        JLabel label = new JLabel("<html><center><b>" + value + "</b><br><small>" + title + "</small></center></html>");
        label.setForeground(TITLE_COLOR_DARK);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        return label;
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
        
        pane.addTab("Browse Opportunities", createBrowseTab());
        pane.addTab("My Applications", createApplicationsTab());
        pane.addTab("Volunteer History", createHistoryTab());
        pane.addTab("Calendar", createCalendarTab());
        pane.addTab("My Profile", createProfileTab());
        
        return pane;
    }

    private JPanel createBrowseTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 20, 20, 20));
        
        // Search/Filter Panel
        JPanel searchFilterPanel = createSearchFilterPanel();
        panel.add(searchFilterPanel, BorderLayout.NORTH);
        
        // Opportunities Table
        String[] columnNames = {
            "ID", "Title", "Orphanage", "Location", "Time Commitment", 
            "Slots Available", "Status", "Actions"
        };
        
        opportunitiesModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only Actions column
            }
        };
        
        JTable opportunitiesTable = new JTable(opportunitiesModel);
        styleTable(opportunitiesTable);

        // Hide the ID column
        opportunitiesTable.getColumnModel().getColumn(0).setMinWidth(0);
        opportunitiesTable.getColumnModel().getColumn(0).setMaxWidth(0);
        opportunitiesTable.getColumnModel().getColumn(0).setWidth(0);
        
        // Add button renderer and editor for Actions column
        opportunitiesTable.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer());
        opportunitiesTable.getColumnModel().getColumn(7).setCellEditor(
            new ButtonEditor(new JCheckBox())
        );
        
        JScrollPane scrollPane = new JScrollPane(opportunitiesTable);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Info Panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR_DARK),
            new EmptyBorder(10, 5, 5, 5)
        ));
        
        JLabel lblInfo = new JLabel("Click 'Apply' to apply for an opportunity");
        lblInfo.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 13));
        lblInfo.setForeground(TEXT_COLOR_DARK);
        infoPanel.add(lblInfo);
        
        panel.add(infoPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createSearchFilterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR_DARK),
            " Search Filters ",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font(Font.SANS_SERIF, Font.BOLD, 14),
            TITLE_COLOR_DARK
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Row 1
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblKeyword = new JLabel("Keyword:");
        styleFormLabel(lblKeyword);
        panel.add(lblKeyword, gbc);
        
        gbc.gridx = 1;
        txtSearch = new JTextField(15);
        styleTextField(txtSearch);
        panel.add(txtSearch, gbc);
        
        gbc.gridx = 2;
        JLabel lblCategory = new JLabel("Category:");
        styleFormLabel(lblCategory);
        panel.add(lblCategory, gbc);
        
        gbc.gridx = 3;
        cmbCategory = new JComboBox<>(new String[]{"All", "Education", "Healthcare", "Recreation", "Administrative", "Other"});
        styleComboBox(cmbCategory);
        panel.add(cmbCategory, gbc);
        
        // Row 2
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblLocation = new JLabel("Location:");
        styleFormLabel(lblLocation);
        panel.add(lblLocation, gbc);
        
        gbc.gridx = 1;
        cmbLocation = new JComboBox<>(new String[]{"Any Location"});
        styleComboBox(cmbLocation);
        panel.add(cmbLocation, gbc);
        
        gbc.gridx = 2;
        JLabel lblTime = new JLabel("Time:");
        styleFormLabel(lblTime);
        panel.add(lblTime, gbc);
        
        gbc.gridx = 3;
        cmbTime = new JComboBox<>(new String[]{"Any Time", "Morning", "Afternoon", "Evening", "Weekend", "Flexible"});
        styleComboBox(cmbTime);
        panel.add(cmbTime, gbc);
        
        // Row 3
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblSkills = new JLabel("Skills:");
        styleFormLabel(lblSkills);
        panel.add(lblSkills, gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtSkills = new JTextField(20);
        styleTextField(txtSkills);
        txtSkills.setToolTipText("Enter skills separated by commas");
        panel.add(txtSkills, gbc);
        
        // Buttons
        gbc.gridx = 3; gbc.gridwidth = 1;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        
        JButton btnSearch = new JButton("Search");
        styleActionButton(btnSearch, "Search for opportunities");
        btnSearch.setBackground(BUTTON_APPLY_BG);
        btnSearch.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnSearch.setBackground(BUTTON_APPLY_HOVER_BG);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnSearch.setBackground(BUTTON_APPLY_BG);
            }
        });
        btnSearch.addActionListener(e -> performSearch());
        
        JButton btnClear = new JButton("Clear");
        styleActionButton(btnClear, "Clear all filters");
        btnClear.addActionListener(e -> clearFilters());
        
        buttonPanel.add(btnSearch);
        buttonPanel.add(btnClear);
        panel.add(buttonPanel, gbc);
        
        return panel;
    }

    private JPanel createApplicationsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Table
        String[] columnNames = {
            "ID", "Opportunity ID", "Date Applied", "Status", "Interview Date", "Actions"
        };
        
        applicationsModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only Actions column
            }
        };
        
        JTable applicationsTable = new JTable(applicationsModel);
        styleTable(applicationsTable);
        
        // Hide ID columns
        applicationsTable.getColumnModel().getColumn(0).setMinWidth(0);
        applicationsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        applicationsTable.getColumnModel().getColumn(0).setWidth(0);
        
        // Add custom renderers
        applicationsTable.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());
        applicationsTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        applicationsTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        JScrollPane scrollPane = new JScrollPane(applicationsTable);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Status Panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setOpaque(false);
        statusPanel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR_DARK),
            new EmptyBorder(10, 5, 5, 5)
        ));
        
        JLabel lblInfo = new JLabel("Track your volunteer applications");
        lblInfo.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 13));
        lblInfo.setForeground(TEXT_COLOR_DARK);
        statusPanel.add(lblInfo);
        
        panel.add(statusPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setOpaque(false);
        JLabel lblFilter = new JLabel("Filter:");
        styleFormLabel(lblFilter);
        filterPanel.add(lblFilter);
        
        JComboBox<String> cmbHistoryFilter = new JComboBox<>(new String[]{"All", "Completed", "Active", "Withdrawn"});
        styleComboBox(cmbHistoryFilter);
        filterPanel.add(cmbHistoryFilter);
        panel.add(filterPanel, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {
            "Start Date", "End Date", "Opportunity", "Orphanage", "Hours", "Status", "Certificate"
        };
        
        historyModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable historyTable = new JTable(historyModel);
        styleTable(historyTable);
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Summary Panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR_DARK),
            new EmptyBorder(10, 5, 5, 5)
        ));
        
        JLabel lblSummary = new JLabel("Your volunteer history and achievements");
        lblSummary.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 13));
        lblSummary.setForeground(TEXT_COLOR_DARK);
        summaryPanel.add(lblSummary);
        
        panel.add(summaryPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createCalendarTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Top panel with month navigation
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.setOpaque(false);
        
        JButton btnPrevMonth = new JButton("<");
        styleActionButton(btnPrevMonth, "Previous month");
        
        JLabel lblMonth = new JLabel("December 2024");
        lblMonth.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        lblMonth.setForeground(TITLE_COLOR_DARK);
        
        JButton btnNextMonth = new JButton(">");
        styleActionButton(btnNextMonth, "Next month");
        
        topPanel.add(btnPrevMonth);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(lblMonth);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(btnNextMonth);
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Events table
        String[] columnNames = {"Date", "Time", "Event", "Location", "Status"};
        eventsModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable eventsTable = new JTable(eventsModel);
        styleTable(eventsTable);
        
        JScrollPane scrollPane = new JScrollPane(eventsTable);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createProfileTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        int row = 0;
        
        // Personal Information
        JLabel lblPersonalInfo = new JLabel("Personal Information");
        lblPersonalInfo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        lblPersonalInfo.setForeground(TITLE_COLOR_DARK);
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        panel.add(lblPersonalInfo, gbc);
        
        gbc.gridwidth = 1;
        
        // Name
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblName = new JLabel("Name:");
        styleFormLabel(lblName);
        panel.add(lblName, gbc);
        gbc.gridx = 1;
        txtName = new JTextField(25);
        styleTextField(txtName);
        panel.add(txtName, gbc);
        row++;
        
        // Email
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblEmail = new JLabel("Email:");
        styleFormLabel(lblEmail);
        panel.add(lblEmail, gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(25);
        styleTextField(txtEmail);
        panel.add(txtEmail, gbc);
        row++;
        
        // Phone
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblPhone = new JLabel("Phone:");
        styleFormLabel(lblPhone);
        panel.add(lblPhone, gbc);
        gbc.gridx = 1;
        txtPhone = new JTextField(25);
        styleTextField(txtPhone);
        panel.add(txtPhone, gbc);
        row++;
        
        // Skills & Experience
        row++;
        JLabel lblSkillsInfo = new JLabel("Skills & Experience");
        lblSkillsInfo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        lblSkillsInfo.setForeground(TITLE_COLOR_DARK);
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        panel.add(lblSkillsInfo, gbc);
        
        gbc.gridwidth = 1;
        
        // Skills
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblSkills = new JLabel("Skills:");
        styleFormLabel(lblSkills);
        panel.add(lblSkills, gbc);
        gbc.gridx = 1;
        txtSkillsProfile = new JTextArea(3, 25);
        styleTextArea(txtSkillsProfile);
        JScrollPane skillsScroll = new JScrollPane(txtSkillsProfile);
        styleScrollPane(skillsScroll);
        panel.add(skillsScroll, gbc);
        row++;
        
        // Experience
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblExperience = new JLabel("Experience:");
        styleFormLabel(lblExperience);
        panel.add(lblExperience, gbc);
        gbc.gridx = 1;
        txtExperience = new JTextArea(4, 25);
        styleTextArea(txtExperience);
        JScrollPane expScroll = new JScrollPane(txtExperience);
        styleScrollPane(expScroll);
        panel.add(expScroll, gbc);
        row++;
        
        // Availability
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblAvailability = new JLabel("When are you available? [e.g., Weekends, Mon-Fri evenings, 10 hours per week]");
        styleFormLabel(lblAvailability);
        panel.add(lblAvailability, gbc);
        gbc.gridx = 1;
        txtAvailabilityProfile = new JTextField(25);
        styleTextField(txtAvailabilityProfile);
        panel.add(txtAvailabilityProfile, gbc);
        row++;
        
        // Save Button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        JButton btnSave = new JButton("Save Profile");
        styleActionButton(btnSave, "Save your profile changes");
        btnSave.setBackground(BUTTON_APPLY_BG);
        btnSave.addActionListener(e -> updateProfile());
        buttonPanel.add(btnSave);
        
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);
        
        return panel;
    }

    private void loadInitialData() {
        loadOpportunities();
        loadApplications();
        loadHistory();
        loadEvents();
        loadLocations();
        loadStatistics();
    }

    private void loadOpportunities() {
        Option<String> locationFilter = Option.none();
        Option<String> skillsFilter = Option.none();
        Option<String> timeFilter = Option.none();
        
        volunteerService.getOpportunities(locationFilter, skillsFilter, timeFilter)
            .onSuccess(opportunities -> SwingUtilities.invokeLater(() -> {
                opportunitiesModel.setRowCount(0);
                opportunities.forEach(opp -> {
                    opportunitiesModel.addRow(new Object[]{
                        opp.opportunityId(),
                        opp.title(),
                        "Orphanage #" + opp.orphanageId(),
                        opp.location().getOrElse("N/A"),
                        opp.timeCommitment().getOrElse("N/A"),
                        opp.slotsAvailable(),
                        opp.status(),
                        "Apply"
                    });
                });
            }))
            .onFailure(ex -> {
                logger.error("Failed to load opportunities", ex);
                showErrorMessage("Failed to load opportunities: " + ex.getMessage());
            });
    }

    private void loadApplications() {
        Option<Integer> userId = SessionManager.getInstance().getCurrentUserId();
        
        userId.forEach(id -> {
            volunteerService.getMyApplications(id)
                .onSuccess(applications -> SwingUtilities.invokeLater(() -> {
                    applicationsModel.setRowCount(0);
                    applications.forEach(app -> {
                        applicationsModel.addRow(new Object[]{
                            app.applicationId(),
                            app.opportunityId(),
                            app.applicationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                            app.status(),
                            app.interviewDate()
                                .map(d -> d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                                .getOrElse("Not scheduled"),
                            getActionForStatus(app.status())
                        });
                    });
                }))
                .onFailure(ex -> {
                    logger.error("Failed to load applications", ex);
                });
        });
    }

 private void loadHistory() {
    Option<Integer> userId = SessionManager.getInstance().getCurrentUserId();
    
    userId.forEach(id -> {
        volunteerService.getVolunteerHistory(id)
            .onSuccess(history -> SwingUtilities.invokeLater(() -> {
                historyModel.setRowCount(0);
                // history contains VolunteerApplication objects, not VolunteerHistory
                history.forEach(app -> {
                    // Handle dates properly - they are LocalDateTime, not LocalDate
                    String startDateStr = app.startDate()
                        .map(d -> d.toLocalDate().toString())
                        .getOrElse("Not started");
                    
                    String endDateStr = app.endDate()
                        .map(d -> d.toLocalDate().toString())
                        .getOrElse("Ongoing");
                    
                    historyModel.addRow(new Object[]{
                        startDateStr,
                        endDateStr,
                        "Opportunity #" + app.opportunityId(),
                        "Orphanage", // Would need to fetch this separately
                        app.hoursCompleted().getOrElse(0),
                        app.status(),
                        app.performanceRating().isDefined() ? "Available" : "N/A"
                    });
                });
            }))
            .onFailure(ex -> {
                logger.error("Failed to load history", ex);
            });
    });
}
    private void loadEvents() {
        Option<Integer> userId = SessionManager.getInstance().getCurrentUserId();
        
        userId.forEach(id -> {
            volunteerService.getUpcomingEvents(id)
                .onSuccess(events -> SwingUtilities.invokeLater(() -> {
                    eventsModel.setRowCount(0);
                    events.forEach(event -> {
                        eventsModel.addRow(new Object[]{
                            event.startDate()
                                .map(d -> d.toLocalDate().toString())
                                .getOrElse("TBD"),
                            event.startDate()
                                .map(d -> d.toLocalTime().toString())
                                .getOrElse("TBD"),
                            "Opportunity #" + event.opportunityId(),
                            "TBD",
                            event.status()
                        });
                    });
                }))
                .onFailure(ex -> {
                    logger.error("Failed to load events", ex);
                });
        });
    }

    private void loadLocations() {
        // Add sample locations - you can load these from your service
        SwingUtilities.invokeLater(() -> {
            cmbLocation.removeAllItems();
            cmbLocation.addItem("Any Location");
            cmbLocation.addItem("Cape Town");
            cmbLocation.addItem("Johannesburg");
            cmbLocation.addItem("Durban");
            cmbLocation.addItem("Pretoria");
            cmbLocation.addItem("Port Elizabeth");
        });
    }

    private void loadStatistics() {
        Option<Integer> userId = SessionManager.getInstance().getCurrentUserId();
        
        userId.forEach(id -> {
            volunteerService.getVolunteerStatistics(id)
                .onSuccess(stats -> SwingUtilities.invokeLater(() -> {
                    lblTotalApplications.setText("<html><center><b>" + stats.totalApplications() + 
                        "</b><br><small>Apps</small></center></html>");
                    lblAcceptedApplications.setText("<html><center><b>" + stats.acceptedApplications() + 
                        "</b><br><small>Accepted</small></center></html>");
                    lblHoursVolunteered.setText("<html><center><b>" + stats.totalHours() + 
                        "</b><br><small>Hours</small></center></html>");
                    lblOrphanagesHelped.setText("<html><center><b>" + stats.uniqueCategories() + 
                        "</b><br><small>Orgs</small></center></html>");
                }))
                .onFailure(ex -> {
                    logger.error("Failed to load statistics", ex);
                });
        });
    }

    private void performSearch() {
        String locationValue = (String) cmbLocation.getSelectedItem();
        String skillsValue = txtSkills.getText().trim();
        String timeValue = (String) cmbTime.getSelectedItem();
        
        Option<String> locationFilter = (locationValue == null || "Any Location".equals(locationValue)) 
            ? Option.none() 
            : Option.of(locationValue);
        
        Option<String> skillsFilter = skillsValue.isEmpty() 
            ? Option.none() 
            : Option.of(skillsValue);
        
        Option<String> timeFilter = (timeValue == null || "Any Time".equals(timeValue)) 
            ? Option.none() 
            : Option.of(timeValue);
        
        volunteerService.getOpportunities(locationFilter, skillsFilter, timeFilter)
            .onSuccess(opportunities -> SwingUtilities.invokeLater(() -> {
                opportunitiesModel.setRowCount(0);
                
                if (opportunities.isEmpty()) {
                    opportunitiesModel.addRow(new Object[]{
                        0, "No opportunities found", "", "", "", "", "", ""
                    });
                } else {
                    opportunities.forEach(opp -> {
                        opportunitiesModel.addRow(new Object[]{
                            opp.opportunityId(),
                            opp.title(),
                            "Orphanage #" + opp.orphanageId(),
                            opp.location().getOrElse("N/A"),
                            opp.timeCommitment().getOrElse("N/A"),
                            opp.slotsAvailable(),
                            opp.status(),
                            "Apply"
                        });
                    });
                }
            }))
            .onFailure(ex -> {
                logger.error("Failed to search opportunities", ex);
                showErrorMessage("Failed to search: " + ex.getMessage());
            });
    }

    private void clearFilters() {
        txtSearch.setText("");
        cmbCategory.setSelectedIndex(0);
        cmbLocation.setSelectedIndex(0);
        cmbTime.setSelectedIndex(0);
        txtSkills.setText("");
        loadOpportunities();
    }

   private void showApplyDialog(int row) {
    Integer opportunityId = (Integer) opportunitiesModel.getValueAt(row, 0);
    String opportunityTitle = opportunitiesModel.getValueAt(row, 1).toString();
    
    JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
        "Apply for Volunteer Opportunity", true);
    dialog.setLayout(new GridBagLayout());
    dialog.setSize(500, 450);
    dialog.setLocationRelativeTo(this);
    
    // Style the dialog
    dialog.getContentPane().setBackground(DARK_BG_START);
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    
    // Title
    JLabel lblTitle = new JLabel("Applying for: " + opportunityTitle);
    lblTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
    lblTitle.setForeground(TITLE_COLOR_DARK);
    gbc.gridx = 0; gbc.gridy = 0;
    gbc.gridwidth = 2;
    dialog.add(lblTitle, gbc);
    
    // Motivation Label
    gbc.gridx = 0; gbc.gridy = 1;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    JLabel lblMotivation = new JLabel("Why do you want to volunteer?");
    styleFormLabel(lblMotivation);
    dialog.add(lblMotivation, gbc);
    
    // Motivation Text Area
    gbc.gridy = 2;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.weighty = 0.4;
    JTextArea txtMotivation = new JTextArea(5, 30);
    styleTextArea(txtMotivation);
    JScrollPane motivationScroll = new JScrollPane(txtMotivation);
    motivationScroll.setPreferredSize(new Dimension(450, 100));
    styleScrollPane(motivationScroll);
    dialog.add(motivationScroll, gbc);
    
    // Experience Label
    gbc.gridy = 3;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weighty = 0;
    JLabel lblExp = new JLabel("Relevant experience:");
    styleFormLabel(lblExp);
    dialog.add(lblExp, gbc);
    
    // Experience Text Area
    gbc.gridy = 4;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weighty = 0.4;
    JTextArea txtExp = new JTextArea(5, 30);
    styleTextArea(txtExp);
    JScrollPane expScroll = new JScrollPane(txtExp);
    expScroll.setPreferredSize(new Dimension(450, 100));
    styleScrollPane(expScroll);
    dialog.add(expScroll, gbc);
    
    // Availability
    gbc.gridy = 5;
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 0;
    gbc.weighty = 0;
    JLabel lblAvail = new JLabel("Your availability:");
    styleFormLabel(lblAvail);
    dialog.add(lblAvail, gbc);
    
    gbc.gridx = 1;
    gbc.weightx = 1.0;
    JTextField txtAvail = new JTextField(20);
    styleTextField(txtAvail);
    dialog.add(txtAvail, gbc);
    
    // Buttons
    JPanel buttonPanel = new JPanel();
    buttonPanel.setOpaque(false);
    
    JButton btnSubmit = new JButton("Submit Application");
    styleActionButton(btnSubmit, "Submit your application");
    btnSubmit.setBackground(BUTTON_APPLY_BG);
    btnSubmit.addActionListener(e -> {
        submitApplication(opportunityId, txtMotivation.getText(), 
            txtExp.getText(), txtAvail.getText());
        dialog.dispose();
    });
    
    JButton btnCancel = new JButton("Cancel");
    styleActionButton(btnCancel, "Cancel application");
    btnCancel.addActionListener(e -> dialog.dispose());
    
    buttonPanel.add(btnSubmit);
    buttonPanel.add(btnCancel);
    
    gbc.gridx = 0; gbc.gridy = 6;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weighty = 0;
    dialog.add(buttonPanel, gbc);
    
    dialog.setVisible(true);
}

    private void submitApplication(Integer opportunityId, String motivation, 
            String experience, String availability) {
        Option<Integer> userId = SessionManager.getInstance().getCurrentUserId();
        
        if (userId.isEmpty()) {
            showErrorMessage("Please log in to apply");
            return;
        }
        
        // Create application using your service
        VolunteerApplication newApp = VolunteerApplication.create(
            opportunityId, 
            userId.get(),
            motivation,
            experience,
            availability
        );
        
        // For now, just show success message (you'd need to add a method to save this)
        showSuccessMessage("Application submitted successfully!");
        loadApplications();
    }

    private void updateProfile() {
        // Update profile implementation
        showSuccessMessage("Profile updated successfully!");
    }

    private void showNotifications() {
        // Show notifications implementation
        JOptionPane.showMessageDialog(this, "No new notifications", 
            "Notifications", JOptionPane.INFORMATION_MESSAGE);
    }

    private String getActionForStatus(String status) {
        return switch (status.toLowerCase()) {
            case "pending", "reviewing" -> "Withdraw";
            case "accepted", "active" -> "View Details";
            case "rejected", "withdrawn" -> "Remove";
            case "interview scheduled" -> "View Interview";
            default -> "View";
        };
    }

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Styling methods (remain the same)
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

    // Custom cell renderer for status
    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                String status = (String) value;
                if ("Open".equalsIgnoreCase(status) || "Accepted".equalsIgnoreCase(status) || 
                    "Active".equalsIgnoreCase(status)) {
                    setForeground(ACCENT_COLOR_GREEN);
                } else if ("Closed".equalsIgnoreCase(status) || "Rejected".equalsIgnoreCase(status)) {
                    setForeground(ACCENT_COLOR_RED);
                } else if ("Pending".equalsIgnoreCase(status) || "Reviewing".equalsIgnoreCase(status) ||
                           "Interview Scheduled".equalsIgnoreCase(status)) {
                    setForeground(ACCENT_COLOR_YELLOW);
                } else {
                    setForeground(TABLE_CELL_FG);
                }
            }
            
            return c;
        }
    }

    // Button renderer for table
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            setForeground(BUTTON_FG_DARK);
            setBackground(BUTTON_APPLY_BG);
            setBorder(new EmptyBorder(2, 5, 2, 5));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // Button editor for table
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            button.setForeground(BUTTON_FG_DARK);
            button.setBackground(BUTTON_APPLY_BG);
            button.setBorder(new EmptyBorder(2, 5, 2, 5));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                if ("Apply".equals(label)) {
                    showApplyDialog(currentRow);
                } else if ("Withdraw".equals(label)) {
                    int confirm = JOptionPane.showConfirmDialog(button,
                        "Are you sure you want to withdraw this application?",
                        "Confirm Withdrawal",
                        JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        showSuccessMessage("Application withdrawn");
                        loadApplications();
                    }
                } else if ("View Details".equals(label) || "View Interview".equals(label)) {
                    JOptionPane.showMessageDialog(button, "Viewing details for row " + currentRow);
                }
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}