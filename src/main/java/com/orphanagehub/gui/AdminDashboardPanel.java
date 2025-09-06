package com.orphanagehub.gui;

import com.orphanagehub.service.AdminService;
import com.orphanagehub.util.SessionManager;
import io.vavr.control.Try;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

public class AdminDashboardPanel extends JPanel {
    private final OrphanageHubApp mainApp;
    private final AdminService adminService;
    private DefaultTableModel verificationModel;
    private DefaultTableModel userModel;
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
    private static final Color BUTTON_SUSPEND_BG = BUTTON_REJECT_BG;
    private static final Color BUTTON_SUSPEND_HOVER_BG = BUTTON_REJECT_HOVER_BG;

    public AdminDashboardPanel(OrphanageHubApp app) {
        this.mainApp = app;
        this.adminService = new AdminService();
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
        JTabbedPane tabbedPane = createTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
        loadData();
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
        JLabel iconLabel = new JLabel("\u2699");
        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        iconLabel.setForeground(TITLE_COLOR_DARK);
        JLabel nameLabel = new JLabel("Administrator Dashboard");
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        nameLabel.setForeground(TITLE_COLOR_DARK);
        titleGroup.add(iconLabel);
        titleGroup.add(nameLabel);
        headerPanel.add(titleGroup, BorderLayout.WEST);
        
        JPanel userGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userGroup.setOpaque(false);
        
        String adminUsername = SessionManager.getInstance()
            .getAttribute("currentUser")
            .map(Object::toString)
            .getOrElse("Admin");
        
        JLabel userLabel = new JLabel("Admin User: " + adminUsername);
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        userLabel.setForeground(TEXT_COLOR_DARK);
        
        JButton btnLogout = new JButton("Logout");
        styleActionButton(btnLogout, "Logout and return to welcome screen");
        btnLogout.setPreferredSize(new Dimension(100, 30));
        btnLogout.setBackground(BUTTON_REJECT_BG);
        btnLogout.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnLogout.setBackground(BUTTON_REJECT_HOVER_BG); }
            @Override public void mouseExited(MouseEvent e) { btnLogout.setBackground(BUTTON_REJECT_BG); }
        });
        btnLogout.addActionListener(e -> {
            SessionManager.getInstance().clear();
            mainApp.navigateTo("Home");
        });
        
        userGroup.add(userLabel);
        userGroup.add(btnLogout);
        headerPanel.add(userGroup, BorderLayout.EAST);
        
        return headerPanel;
    }

    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setOpaque(false);
        tabbedPane.setForeground(TAB_FG);
        tabbedPane.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        tabbedPane.addTab("Orphanage Verification", createVerificationTab());
        tabbedPane.addTab("User Management", createUserManagementTab());
        tabbedPane.addTab("System Overview", createSystemOverviewTab());
        
        return tabbedPane;
    }

    private JPanel createVerificationTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        String[] columnNames = {"Orphanage Name", "Contact", "Email", "Registered", "Status", "Actions"};
        verificationModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(verificationModel) {
            @Override public boolean isCellEditable(int row, int column) { return column == 5; }
        };
        styleTable(table);
        
        JScrollPane scrollPane = new JScrollPane(table);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createUserManagementTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setOpaque(false);
        
        JLabel lblSearchUser = new JLabel("Search User:");
        JTextField txtUserSearch = new JTextField(20);
        JLabel lblUserRole = new JLabel("Role:");
        JComboBox<String> cmbUserRole = new JComboBox<>(new String[]{"Any Role", "Admin", "OrphanageStaff", "Donor", "Volunteer"});
        JButton btnUserSearch = new JButton("Search");
        
        styleFormLabel(lblSearchUser);
        styleTextField(txtUserSearch);
        styleFormLabel(lblUserRole);
        styleComboBox(cmbUserRole);
        styleActionButton(btnUserSearch, "Find users");
        
        searchPanel.add(lblSearchUser);
        searchPanel.add(txtUserSearch);
        searchPanel.add(lblUserRole);
        searchPanel.add(cmbUserRole);
        searchPanel.add(btnUserSearch);
        panel.add(searchPanel, BorderLayout.NORTH);
        
        String[] columnNames = {"Username", "Email", "Role", "Status", "Registered", "Actions"};
        userModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(userModel) {
            @Override public boolean isCellEditable(int row, int column) { return column == 5; }
        };
        styleTable(table);
        
        JScrollPane scrollPane = new JScrollPane(table);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createSystemOverviewTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        panel.add(createOverviewStat("Total Registered Users:", "157"));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createOverviewStat("Verified Orphanages:", "34"));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createOverviewStat("Pending Verification:", "3"));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createOverviewStat("Open Resource Requests:", "48"));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createOverviewStat("Active Volunteers:", "22"));
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }

    private Component createOverviewStat(String labelText, String valueText) {
        JPanel statPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statPanel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        label.setForeground(TEXT_COLOR_DARK);
        JLabel value = new JLabel(valueText);
        value.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        value.setForeground(TITLE_COLOR_DARK);
        statPanel.add(label);
        statPanel.add(value);
        return statPanel;
    }

    private void loadData() {
        // Load mock data for verification
        verificationModel.setRowCount(0);
        verificationModel.addRow(new Object[]{"New Hope Center", "Alice Smith", "alice@newhope.org", "2024-05-10", "Pending", "Actions"});
        verificationModel.addRow(new Object[]{"Future Stars", "Bob Jones", "bob@futurestars.net", "2024-05-08", "Pending", "Actions"});
        verificationModel.addRow(new Object[]{"Safe Haven Kids", "Charlie P.", "contact@safehaven.com", "2024-04-20", "Verified", "Actions"});
        
        // Load mock data for users
        userModel.setRowCount(0);
        userModel.addRow(new Object[]{"staff_user", "staff@example.com", "OrphanageStaff", "Active", "2024-01-15", "Actions"});
        userModel.addRow(new Object[]{"donor_user", "donor@mail.net", "Donor", "Active", "2024-02-10", "Actions"});
        userModel.addRow(new Object[]{"volunteer_A", "vol@provider.org", "Volunteer", "Active", "2024-03-01", "Actions"});
        userModel.addRow(new Object[]{"admin_user", "admin@orphanagehub.com", "Admin", "Active", "2023-10-01", "Actions"});
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, 
            "Error loading data: " + message, 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
    }

    private void styleFormLabel(JLabel label) { 
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); 
        label.setForeground(TEXT_COLOR_DARK); 
    }
    
    private void styleTextField(JTextField field) { 
        field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); 
        field.setForeground(INPUT_FG_DARK); 
        field.setBackground(INPUT_BG_DARK); 
        Border p=new EmptyBorder(4,6,4,6); 
        field.setBorder(new CompoundBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK,1),p)); 
        field.setCaretColor(Color.LIGHT_GRAY); 
    }
    
    private void styleComboBox(JComboBox<?> comboBox) { 
        comboBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); 
        comboBox.setForeground(INPUT_FG_DARK); 
        comboBox.setBackground(INPUT_BG_DARK); 
        comboBox.setBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK,1)); 
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
        table.setIntercellSpacing(new Dimension(0,1)); 
        JTableHeader h=table.getTableHeader(); 
        h.setBackground(TABLE_HEADER_BG); 
        h.setForeground(TABLE_HEADER_FG); 
        h.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14)); 
        h.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); 
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
            @Override protected void configureScrollBarColors(){
                this.thumbColor=BUTTON_BG_DARK; 
                this.trackColor=DARK_BG_END;
            } 
            @Override protected JButton createDecreaseButton(int o){return createZeroButton();} 
            @Override protected JButton createIncreaseButton(int o){return createZeroButton();} 
            private JButton createZeroButton(){
                JButton b=new JButton(); 
                b.setPreferredSize(new Dimension(0,0)); 
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
        Border p=new EmptyBorder(6,12,6,12); 
        btn.setBorder(new CompoundBorder(BorderFactory.createLineBorder(BUTTON_BG_DARK.darker()),p)); 
        btn.addMouseListener(new MouseAdapter() { 
            @Override public void mouseEntered(MouseEvent e){
                if(btn.getBackground().equals(BUTTON_BG_DARK)){
                    btn.setBackground(BUTTON_HOVER_BG_DARK);
                }
            } 
            @Override public void mouseExited(MouseEvent e){
                if(btn.getBackground().equals(BUTTON_HOVER_BG_DARK)){
                    btn.setBackground(BUTTON_BG_DARK);
                }
            } 
        }); 
    }
}