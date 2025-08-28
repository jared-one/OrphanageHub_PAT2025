import os

file_contents = {
    "AdminDashboardPanel.java": """package com.orphanagehub.gui;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.event.ActionListener; // Keep this import
public class AdminDashboardPanel extends JPanel {
    private OrphanageHubApp mainApp;
    private String adminUsername = "admin_user"; // Placeholder
    // Define Colors (Consider shared constants)
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
    private static final Color TABLE_CELL_FG = new Color(200, 200, 200);
    private static final Color TABLE_CELL_SELECTED_BG = BUTTON_BG_DARK;
    private static final Color TABLE_CELL_SELECTED_FG = BUTTON_FG_DARK;
    // Action Button Colors
    private static final Color BUTTON_APPROVE_BG = new Color(60, 179, 113); // Green
    private static final Color BUTTON_APPROVE_HOVER_BG = new Color(70, 190, 123);
    private static final Color BUTTON_REJECT_BG = new Color(192, 57, 43); // Red
    private static final Color BUTTON_REJECT_HOVER_BG = new Color(231, 76, 60);
    private static final Color BUTTON_SUSPEND_BG = BUTTON_REJECT_BG; // Use same red for suspend
    private static final Color BUTTON_SUSPEND_HOVER_BG = BUTTON_REJECT_HOVER_BG;
    public AdminDashboardPanel(OrphanageHubApp app) {
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
        // --- Header Panel ---
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        // --- Tabbed Pane for Content ---
        JTabbedPane tabbedPane = createTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
    }
    // --- Helper Methods ---
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR_DARK),
                new EmptyBorder(10, 20, 10, 20)
        ));
        // Left side: Role Icon and Title
        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleGroup.setOpaque(false);
        JLabel iconLabel = new JLabel("\\u2699"); // Gear symbol
        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        iconLabel.setForeground(TITLE_COLOR_DARK); // Standard title color for gear
        JLabel nameLabel = new JLabel("Administrator Dashboard");
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        nameLabel.setForeground(TITLE_COLOR_DARK);
        titleGroup.add(iconLabel);
        titleGroup.add(nameLabel);
        headerPanel.add(titleGroup, BorderLayout.WEST);
        // Right side: User info and Logout Button
        JPanel userGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userGroup.setOpaque(false);
        JLabel userLabel = new JLabel("Admin User: " + adminUsername);
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        userLabel.setForeground(TEXT_COLOR_DARK);
        JButton btnLogout = new JButton("Logout");
        styleActionButton(btnLogout, "Logout and return to welcome screen");
        btnLogout.setPreferredSize(new Dimension(100, 30));
        btnLogout.setBackground(BUTTON_REJECT_BG); // Use red for admin logout too?
        btnLogout.addMouseListener(new MouseAdapter() {
             @Override public void mouseEntered(MouseEvent e) { btnLogout.setBackground(BUTTON_REJECT_HOVER_BG); }
             @Override public void mouseExited(MouseEvent e) { btnLogout.setBackground(BUTTON_REJECT_BG); }
        });
        btnLogout.addActionListener(e -> mainApp.navigateTo(OrphanageHubApp.HOME_PANEL));
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
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() { // Copied UI styling
             @Override protected void installDefaults() { super.installDefaults(); lightHighlight=TAB_BG_SELECTED; shadow=BORDER_COLOR_DARK; darkShadow=DARK_BG_END; focus=TAB_BG_SELECTED; }
             @Override protected void paintTabBackground(Graphics g, int p, int i, int x, int y, int w, int h, boolean s) { g.setColor(s ? TAB_BG_SELECTED : TAB_BG_UNSELECTED); g.fillRoundRect(x, y, w, h+5, 5, 5); }
             @Override protected void paintTabBorder(Graphics g, int p, int i, int x, int y, int w, int h, boolean s) { /* Minimal border */ }
             @Override protected void paintContentBorder(Graphics g, int p, int i) { int w=tabPane.getWidth(); int h=tabPane.getHeight(); Insets ins=tabPane.getInsets(); int th=calculateTabAreaHeight(p, runCount, maxTabHeight); int x=ins.left; int y=ins.top+th-(lightHighlight.getAlpha()>0?1:0); int cw=w-ins.right-ins.left; int ch=h-ins.top-ins.bottom-y; g.setColor(BORDER_COLOR_DARK); g.drawRect(x, y, cw-1, ch-1); }
        });
        // Create and add tabs
        tabbedPane.addTab("Orphanage Verification", createVerificationTab());
        tabbedPane.addTab("User Management", createUserManagementTab());
        tabbedPane.addTab("System Overview", createSystemOverviewTab());
        return tabbedPane;
    }
    // --- Tab Creation Methods ---
    private JPanel createVerificationTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        // Optional: Add filter for status (Pending, Verified, Rejected) later
        // --- Verification Table ---
        String[] columnNames = {"Orphanage Name", "Contact", "Email", "Registered", "Status", "Actions"};
        Object[][] data = { // Placeholder data
            {"New Hope Center", "Alice Smith", "alice@newhope.org", "2024-05-10", "Pending", "Verify"},
            {"Future Stars", "Bob Jones", "bob@futurestars.net", "2024-05-08", "Pending", "Verify"},
            {"Safe Haven Kids", "Charlie P.", "contact@safehaven.com", "2024-04-20", "Verified", "View"},
            {"Distant Dreams", "Diana Ross", "info@distdreams.org", "2024-05-11", "Pending", "Verify"}
        };
        JTable table = new JTable(data, columnNames) {
            @Override public boolean isCellEditable(int row, int column) { return column == 5; } // Action column
        };
        styleTable(table);
        // --- Action Column Renderer/Editor ---
        JPanel buttonPanelRenderer = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
        buttonPanelRenderer.setOpaque(false);
        JButton approveBtnRend = new JButton("âœ“"); // Check mark
        JButton rejectBtnRend = new JButton("âœ•"); // X mark
        JButton detailsBtnRend = new JButton("..."); // Details
        styleMiniButton(approveBtnRend, BUTTON_APPROVE_BG);
        styleMiniButton(rejectBtnRend, BUTTON_REJECT_BG);
        styleMiniButton(detailsBtnRend, BUTTON_BG_DARK);
        buttonPanelRenderer.add(approveBtnRend);
        buttonPanelRenderer.add(rejectBtnRend);
        buttonPanelRenderer.add(detailsBtnRend);
        table.getColumnModel().getColumn(5).setCellRenderer((tbl, value, isSelected, hasFocus, row, column) -> buttonPanelRenderer);
        table.getColumnModel().getColumn(5).setCellEditor(new ActionPanelEditor(new JCheckBox(), (actionCommand, row) -> {
            String orphanageName = (String) table.getModel().getValueAt(row, 0);
            switch(actionCommand) {
                case "approve":
                    JOptionPane.showMessageDialog(this, "Approve: " + orphanageName + "\\n(Logic TBD)", "Approve", JOptionPane.INFORMATION_MESSAGE);
                    // Update table model status to "Verified"
                    break;
                case "reject":
                     if (JOptionPane.showConfirmDialog(this, "Reject " + orphanageName + "?", "Confirm Reject", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                         JOptionPane.showMessageDialog(this, "Reject: " + orphanageName + "\\n(Logic TBD)", "Reject", JOptionPane.INFORMATION_MESSAGE);
                         // Update table model status to "Rejected"
                     }
                    break;
                case "details":
                    JOptionPane.showMessageDialog(this, "View Details for: " + orphanageName + "\\n(Logic TBD)", "Details", JOptionPane.INFORMATION_MESSAGE);
                    break;
            }
        }));
        // Adjust column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(180); // Name
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Contact
        table.getColumnModel().getColumn(2).setPreferredWidth(180); // Email
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Registered
        table.getColumnModel().getColumn(4).setPreferredWidth(80); // Status
        table.getColumnModel().getColumn(5).setPreferredWidth(120); // Actions (needs space for buttons)
        table.setRowHeight(approveBtnRend.getPreferredSize().height + 4); // Set row height based on buttons
        JScrollPane scrollPane = new JScrollPane(table);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    // *** CORRECTED METHOD ***
    private JPanel createUserManagementTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        // --- User Search/Filter (Optional) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setOpaque(false);
        // Create components first
        JLabel lblSearchUser = new JLabel("Search User:");
        JTextField txtUserSearch = new JTextField(20);
        JLabel lblUserRole = new JLabel("Role:"); // *** STORE LABEL IN VARIABLE ***
        JComboBox<String> cmbUserRole = new JComboBox<>(new String[]{"Any Role", "Admin", "OrphanageStaff", "Donor", "Volunteer"});
        JButton btnUserSearch = new JButton("Search");
        // Style components
        styleFormLabel(lblSearchUser);
        styleTextField(txtUserSearch);
        styleFormLabel(lblUserRole); // *** STYLE USING VARIABLE ***
        styleComboBox(cmbUserRole);
        styleActionButton(btnUserSearch, "Find users");
        // Add search action listener later
        // Add components to panel in order
        searchPanel.add(lblSearchUser);
        searchPanel.add(txtUserSearch);
        searchPanel.add(lblUserRole); // *** ADD LABEL ***
        searchPanel.add(cmbUserRole); // *** ADD COMBOBOX ***
        searchPanel.add(btnUserSearch);
        panel.add(searchPanel, BorderLayout.NORTH);
        // --- User Table ---
        String[] columnNames = {"Username", "Email", "Role", "Status", "Registered", "Actions"};
        Object[][] data = { // Placeholder data
            {"staff_user", "staff@example.com", "OrphanageStaff", "Active", "2024-01-15", "Manage"},
            {"donor_user", "donor@mail.net", "Donor", "Active", "2024-02-10", "Manage"},
            {"volunteer_A", "vol@provider.org", "Volunteer", "Active", "2024-03-01", "Manage"},
            {"old_staff", "old@example.com", "OrphanageStaff", "Suspended", "2023-11-20", "Manage"},
            {"admin_user", "admin@orphanagehub.com", "Admin", "Active", "2023-10-01", "Manage"}
        };
        JTable table = new JTable(data, columnNames) {
            @Override public boolean isCellEditable(int row, int column) { return column == 5; } // Action column
        };
        styleTable(table);
        // --- Action Column Renderer/Editor ---
        JPanel userActionPanelRenderer = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
        userActionPanelRenderer.setOpaque(false);
        JButton activateBtnRend = new JButton("âœ“");
        JButton suspendBtnRend = new JButton("âœ•");
        JButton viewBtnRend = new JButton("...");
        styleMiniButton(activateBtnRend, BUTTON_APPROVE_BG);
        styleMiniButton(suspendBtnRend, BUTTON_SUSPEND_BG);
        styleMiniButton(viewBtnRend, BUTTON_BG_DARK);
        userActionPanelRenderer.add(activateBtnRend);
        userActionPanelRenderer.add(suspendBtnRend);
        userActionPanelRenderer.add(viewBtnRend);
         table.getColumnModel().getColumn(5).setCellRenderer((tbl, value, isSelected, hasFocus, row, column) -> {
             String currentStatus = (String) tbl.getValueAt(row, 3);
             activateBtnRend.setVisible("Suspended".equals(currentStatus));
             suspendBtnRend.setVisible("Active".equals(currentStatus));
             String username = (String) tbl.getValueAt(row, 0);
             if (username.equals(adminUsername)) {
                 activateBtnRend.setVisible(false);
                 suspendBtnRend.setVisible(false);
             }
            return userActionPanelRenderer;
        });
        table.getColumnModel().getColumn(5).setCellEditor(new ActionPanelEditor(new JCheckBox(), (actionCommand, row) -> {
             String username = (String) table.getModel().getValueAt(row, 0);
             if (username.equals(adminUsername)) return;
             String currentStatus = (String) table.getModel().getValueAt(row, 3);
             switch(actionCommand) {
                 case "activate":
                      if ("Suspended".equals(currentStatus)) {
                          JOptionPane.showMessageDialog(this, "Activate User: " + username + "\\n(Logic TBD)", "Activate", JOptionPane.INFORMATION_MESSAGE);
                      }
                     break;
                 case "suspend":
                     if ("Active".equals(currentStatus)) {
                         if (JOptionPane.showConfirmDialog(this, "Suspend User: " + username + "?", "Confirm Suspend", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                             JOptionPane.showMessageDialog(this, "Suspend User: " + username + "\\n(Logic TBD)", "Suspend", JOptionPane.INFORMATION_MESSAGE);
                         }
                     }
                     break;
                 case "view":
                     JOptionPane.showMessageDialog(this, "View User Profile: " + username + "\\n(Logic TBD)", "View User", JOptionPane.INFORMATION_MESSAGE);
                     break;
             }
         }));
        // Adjust column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.setRowHeight(activateBtnRend.getPreferredSize().height + 4);
        JScrollPane scrollPane = new JScrollPane(table);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    // *** END OF CORRECTED METHOD ***
     private JPanel createSystemOverviewTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Vertical layout
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
        panel.add(Box.createVerticalGlue()); // Pushes stats to the top
        return panel;
    }
    // Helper for overview stats labels
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
    // --- Styling Helpers (Unchanged) ---
    private void styleFormLabel(JLabel label) { label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); label.setForeground(TEXT_COLOR_DARK); }
    private void styleTextField(JTextField field) { field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); field.setForeground(INPUT_FG_DARK); field.setBackground(INPUT_BG_DARK); Border p=new EmptyBorder(4,6,4,6); field.setBorder(new CompoundBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK,1),p)); field.setCaretColor(Color.LIGHT_GRAY); }
    private void styleComboBox(JComboBox<?> comboBox) { comboBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); comboBox.setForeground(INPUT_FG_DARK); comboBox.setBackground(INPUT_BG_DARK); comboBox.setBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK,1)); for(Component c:comboBox.getComponents()){if(c instanceof JButton){((JButton)c).setBackground(BUTTON_BG_DARK);((JButton)c).setBorder(BorderFactory.createEmptyBorder());break;}} Object p=comboBox.getUI().getAccessibleChild(comboBox,0); if(p instanceof JPopupMenu){JPopupMenu pm=(JPopupMenu)p;pm.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); for(Component comp:pm.getComponents()){if(comp instanceof JScrollPane){JScrollPane sp=(JScrollPane)comp;sp.getViewport().setBackground(INPUT_BG_DARK);applyScrollbarUI(sp.getVerticalScrollBar()); Component l=sp.getViewport().getView(); if(l instanceof JList){((JList<?>)l).setBackground(INPUT_BG_DARK);((JList<?>)l).setForeground(INPUT_FG_DARK);((JList<?>)l).setSelectionBackground(BUTTON_BG_DARK);((JList<?>)l).setSelectionForeground(BUTTON_FG_DARK);}}}}}
    private void styleTable(JTable table) { table.setBackground(TABLE_CELL_BG); table.setForeground(TABLE_CELL_FG); table.setGridColor(TABLE_GRID_COLOR); table.setRowHeight(28); table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); table.setFillsViewportHeight(true); table.setSelectionBackground(TABLE_CELL_SELECTED_BG); table.setSelectionForeground(TABLE_CELL_SELECTED_FG); table.setShowGrid(true); table.setIntercellSpacing(new Dimension(0,1)); JTableHeader h=table.getTableHeader(); h.setBackground(TABLE_HEADER_BG); h.setForeground(TABLE_HEADER_FG); h.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14)); h.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); h.setReorderingAllowed(true); h.setResizingAllowed(true); DefaultTableCellRenderer r=new DefaultTableCellRenderer(); r.setHorizontalAlignment(SwingConstants.LEFT); r.setVerticalAlignment(SwingConstants.CENTER); r.setBorder(new EmptyBorder(2,5,2,5)); for(int i=0; i<table.getColumnCount()-1; i++){table.getColumnModel().getColumn(i).setCellRenderer(r);} }
    private void styleScrollPane(JScrollPane scrollPane) { scrollPane.setOpaque(false); scrollPane.getViewport().setOpaque(false); scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); applyScrollbarUI(scrollPane.getVerticalScrollBar()); applyScrollbarUI(scrollPane.getHorizontalScrollBar()); }
    private void applyScrollbarUI(JScrollBar scrollBar) { scrollBar.setUI(new BasicScrollBarUI() { @Override protected void configureScrollBarColors(){this.thumbColor=BUTTON_BG_DARK; this.trackColor=DARK_BG_END;} @Override protected JButton createDecreaseButton(int o){return createZeroButton();} @Override protected JButton createIncreaseButton(int o){return createZeroButton();} private JButton createZeroButton(){JButton b=new JButton(); b.setPreferredSize(new Dimension(0,0)); b.setMaximumSize(new Dimension(0,0)); b.setMinimumSize(new Dimension(0,0)); return b;} @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r){g.setColor(thumbColor); g.fillRect(r.x,r.y,r.width,r.height);} @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r){g.setColor(trackColor); g.fillRect(r.x,r.y,r.width,r.height);} }); scrollBar.setUnitIncrement(16); }
    private void styleActionButton(JButton btn, String tooltip) { btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12)); btn.setToolTipText(tooltip); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setForeground(BUTTON_FG_DARK); btn.setFocusPainted(false); btn.setBackground(BUTTON_BG_DARK); Border p=new EmptyBorder(6,12,6,12); btn.setBorder(new CompoundBorder(BorderFactory.createLineBorder(BUTTON_BG_DARK.darker()),p)); btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){if(btn.getBackground().equals(BUTTON_BG_DARK)){btn.setBackground(BUTTON_HOVER_BG_DARK);}}@Override public void mouseExited(MouseEvent e){if(btn.getBackground().equals(BUTTON_HOVER_BG_DARK)){btn.setBackground(BUTTON_BG_DARK);}}}); }
    // --- Inner classes for Table Multi-Button Actions (Unchanged) ---
    static class ActionPanelRenderer implements javax.swing.table.TableCellRenderer { private JPanel panel; public ActionPanelRenderer(JPanel buttonPanel){this.panel=buttonPanel;} @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c){return panel;} }
    static class ActionPanelEditor extends DefaultCellEditor { private JPanel panel; private RowActionCallback callback; private int editingRow; interface RowActionCallback{void onAction(String command,int row);} public ActionPanelEditor(JCheckBox c, RowActionCallback cb){super(c);this.callback=cb;panel=new JPanel(new FlowLayout(FlowLayout.CENTER,3,0));panel.setOpaque(true);panel.setBackground(TABLE_CELL_BG); JButton b1=new JButton("âœ“");JButton b2=new JButton("âœ•");JButton b3=new JButton("..."); styleMiniButtonStatic(b1,BUTTON_APPROVE_BG);b1.setActionCommand("approve");styleMiniButtonStatic(b2,BUTTON_REJECT_BG);b2.setActionCommand("reject");styleMiniButtonStatic(b3,BUTTON_BG_DARK);b3.setActionCommand("view"); ActionListener l=e->{if(callback!=null){callback.onAction(e.getActionCommand(),editingRow);}fireEditingStopped();}; b1.addActionListener(l);b2.addActionListener(l);b3.addActionListener(l); panel.add(b1);panel.add(b2);panel.add(b3);} @Override public Component getTableCellEditorComponent(JTable t,Object v,boolean s,int r,int c){this.editingRow=r;String status="";String context=t.getColumnName(c); if(c==5&&t.getColumnName(5).equals("Actions")&&t.getModel().getRowCount()>r){ if(t.getColumnCount()>4 && t.getColumnName(4).equals("Status")){status=(String)t.getValueAt(r,4);((JButton)panel.getComponent(0)).setActionCommand("approve");((JButton)panel.getComponent(1)).setActionCommand("reject");((JButton)panel.getComponent(2)).setActionCommand("details");panel.getComponent(0).setVisible("Pending".equals(status));panel.getComponent(1).setVisible("Pending".equals(status));panel.getComponent(2).setVisible(true);} else if(t.getColumnCount()>3 && t.getColumnName(3).equals("Status")){status=(String)t.getValueAt(r,3);String u=(String)t.getValueAt(r,0);boolean self=u.equals("admin_user");((JButton)panel.getComponent(0)).setActionCommand("activate");((JButton)panel.getComponent(1)).setActionCommand("suspend");((JButton)panel.getComponent(2)).setActionCommand("view");panel.getComponent(0).setVisible("Suspended".equals(status)&&!self);panel.getComponent(1).setVisible("Active".equals(status)&&!self);panel.getComponent(2).setVisible(true);}} panel.setBackground(s?TABLE_CELL_SELECTED_BG:TABLE_CELL_BG); return panel;} @Override public Object getCellEditorValue(){return"";} @Override public boolean stopCellEditing(){return super.stopCellEditing();} @Override protected void fireEditingStopped(){super.fireEditingStopped();} private static void styleMiniButtonStatic(JButton btn,Color bg){btn.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14));btn.setMargin(new Insets(0,2,0,2));btn.setFocusPainted(false);btn.setBackground(bg);btn.setForeground(BUTTON_FG_DARK);btn.setBorder(BorderFactory.createLineBorder(bg.darker()));btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));} }
} // End of AdminDashboardPanel class""",
    "DonorDashboardPanel.java": """package com.orphanagehub.gui;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI; // For potential combo box arrow styling
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
public class DonorDashboardPanel extends JPanel {
    private OrphanageHubApp mainApp;
    private String donorUsername = "donor_user"; // Placeholder
    // --- Colors (Same as AdminDashboardPanel) ---
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
    private static final Color BUTTON_SEARCH_BG = new Color(72, 149, 239); // Blueish search button
    private static final Color BUTTON_SEARCH_HOVER_BG = new Color(92, 169, 249);
    public DonorDashboardPanel(OrphanageHubApp app) {
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
        // --- Header Panel ---
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        // --- Main Content Area (Search + Table) ---
        JPanel contentPanel = new JPanel(new BorderLayout(10, 15));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(15, 20, 20, 20)); // Padding for content area
        // --- Search/Filter Panel ---
        JPanel searchFilterPanel = createSearchFilterPanel();
        contentPanel.add(searchFilterPanel, BorderLayout.NORTH);
        // --- Results Table ---
        JTable resultsTable = createResultsTable(); // Using placeholder data
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        styleScrollPane(scrollPane); // Apply dark theme styling
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }
    // --- Helper Methods ---
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR_DARK),
                new EmptyBorder(10, 20, 10, 20)
        ));
        // Left side: Role Icon and Title
        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleGroup.setOpaque(false);
        JLabel iconLabel = new JLabel("\\uD83E\\uDEC2"); // Coin symbol (U+1FA99) - may depend on font support
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 22)); // Use font known for symbols
        iconLabel.setForeground(new Color(255, 215, 0)); // Gold color for Donor icon
        JLabel nameLabel = new JLabel("Donor Dashboard");
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        nameLabel.setForeground(TITLE_COLOR_DARK);
        titleGroup.add(iconLabel);
        titleGroup.add(nameLabel);
        headerPanel.add(titleGroup, BorderLayout.WEST);
        // Right side: User info and Logout Button
        JPanel userGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userGroup.setOpaque(false);
        JLabel userLabel = new JLabel("User: " + donorUsername);
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        userLabel.setForeground(TEXT_COLOR_DARK);
        JButton btnLogout = new JButton("Logout");
        styleActionButton(btnLogout, "Logout and return to welcome screen");
        btnLogout.setPreferredSize(new Dimension(100, 30));
        btnLogout.setBackground(new Color(192, 57, 43)); // Reddish logout
        btnLogout.addMouseListener(new MouseAdapter() {
             @Override public void mouseEntered(MouseEvent e) { btnLogout.setBackground(new Color(231, 76, 60)); }
             @Override public void mouseExited(MouseEvent e) { btnLogout.setBackground(new Color(192, 57, 43)); }
        });
        btnLogout.addActionListener(e -> mainApp.navigateTo(OrphanageHubApp.HOME_PANEL));
        userGroup.add(userLabel);
        userGroup.add(btnLogout);
        headerPanel.add(userGroup, BorderLayout.EAST);
        return headerPanel;
    }
    private JPanel createSearchFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setOpaque(false);
        JLabel lblSearch = new JLabel("Search:");
        styleFormLabel(lblSearch);
        JTextField txtSearch = new JTextField(20);
        styleTextField(txtSearch);
        JLabel lblFilterLocation = new JLabel("Location:");
        styleFormLabel(lblFilterLocation);
        String[] locations = {"Any Location", "City A", "City B", "Region C"}; // Placeholders
        JComboBox<String> cmbLocation = new JComboBox<>(locations);
        styleComboBox(cmbLocation);
        JLabel lblFilterCategory = new JLabel("Need Category:");
        styleFormLabel(lblFilterCategory);
        String[] categories = {"Any Category", "Food", "Clothing", "Education", "Medical", "Funding"}; // Placeholders
        JComboBox<String> cmbCategory = new JComboBox<>(categories);
        styleComboBox(cmbCategory);
        JButton btnSearch = new JButton("Apply Filters");
        styleActionButton(btnSearch, "Find orphanages or requests matching criteria");
        // Custom style for search button
        btnSearch.setBackground(BUTTON_SEARCH_BG);
        btnSearch.addMouseListener(new MouseAdapter() {
             @Override public void mouseEntered(MouseEvent e) { btnSearch.setBackground(BUTTON_SEARCH_HOVER_BG); }
             @Override public void mouseExited(MouseEvent e) { btnSearch.setBackground(BUTTON_SEARCH_BG); }
        });
        btnSearch.addActionListener(e -> {
             // Placeholder action
             JOptionPane.showMessageDialog(this, "Search/Filter logic not implemented.", "Search", JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(lblSearch);
        panel.add(txtSearch);
        panel.add(Box.createHorizontalStrut(10)); // Spacer
        panel.add(lblFilterLocation);
        panel.add(cmbLocation);
        panel.add(Box.createHorizontalStrut(10)); // Spacer
        panel.add(lblFilterCategory);
        panel.add(cmbCategory);
        panel.add(Box.createHorizontalStrut(15)); // Spacer
        panel.add(btnSearch);
        return panel;
    }
     private JTable createResultsTable() {
        // Placeholder: Table showing orphanages
        String[] columnNames = {"Orphanage Name", "Location", "Key Needs", "Actions"};
        Object[][] data = {
                {"Hope Children's Home", "City A", "Food, Winter Clothing", "View Details"},
                {"Bright Future Orphanage", "City B", "School Supplies, Funding", "View Details"},
                {"Little Angels Shelter", "City A", "Medical Supplies", "View Details"},
                {"Sunshine House", "Region C", "Food, Volunteers", "View Details"},
                {"New Dawn Center", "City B", "Clothing (All Ages)", "View Details"}
        };
        JTable table = new JTable(data, columnNames) {
             @Override
             public boolean isCellEditable(int row, int column) {
                return column == 3; // Allow interaction only on the last column
             }
        };
        styleTable(table);
        // Add button renderer/editor for the "Actions" column
        table.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer(BUTTON_SEARCH_BG));
        // *** CORRECTED LAMBDA HERE (no 'e' parameter) ***
        table.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox(), BUTTON_SEARCH_BG, () -> { // Changed e -> () ->
             int selectedRow = table.convertRowIndexToModel(table.getEditingRow());
             String orphanageName = (String) table.getModel().getValueAt(selectedRow, 0);
             JOptionPane.showMessageDialog(this, "View Details for: " + orphanageName + "\\n(Functionality not implemented)", "View Details", JOptionPane.INFORMATION_MESSAGE);
         }));
        // Adjust column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(200); // Name
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Location
        table.getColumnModel().getColumn(2).setPreferredWidth(250); // Needs
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Actions
        return table;
    }
    // --- Styling Helpers (Unchanged from previous version) ---
    private void styleFormLabel(JLabel label) { /* ... */ label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); label.setForeground(TEXT_COLOR_DARK); }
    private void styleTextField(JTextField field) { /* ... */ field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); field.setForeground(INPUT_FG_DARK); field.setBackground(INPUT_BG_DARK); Border p=new EmptyBorder(4,6,4,6); field.setBorder(new CompoundBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK,1),p)); field.setCaretColor(Color.LIGHT_GRAY); }
    private void styleComboBox(JComboBox<?> comboBox) { /* ... */ comboBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); comboBox.setForeground(INPUT_FG_DARK); comboBox.setBackground(INPUT_BG_DARK); comboBox.setBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK,1)); for(Component c:comboBox.getComponents()){if(c instanceof JButton){((JButton)c).setBackground(BUTTON_BG_DARK);((JButton)c).setBorder(BorderFactory.createEmptyBorder());break;}} Object p=comboBox.getUI().getAccessibleChild(comboBox,0); if(p instanceof JPopupMenu){JPopupMenu pm=(JPopupMenu)p;pm.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); for(Component comp:pm.getComponents()){if(comp instanceof JScrollPane){JScrollPane sp=(JScrollPane)comp;sp.getViewport().setBackground(INPUT_BG_DARK);applyScrollbarUI(sp.getVerticalScrollBar()); Component l=sp.getViewport().getView(); if(l instanceof JList){((JList<?>)l).setBackground(INPUT_BG_DARK);((JList<?>)l).setForeground(INPUT_FG_DARK);((JList<?>)l).setSelectionBackground(BUTTON_BG_DARK);((JList<?>)l).setSelectionForeground(BUTTON_FG_DARK);}}}}}
    private void styleTable(JTable table) { /* ... */ table.setBackground(TABLE_CELL_BG); table.setForeground(TABLE_CELL_FG); table.setGridColor(TABLE_GRID_COLOR); table.setRowHeight(28); table.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,13)); table.setFillsViewportHeight(true); table.setSelectionBackground(TABLE_CELL_SELECTED_BG); table.setSelectionForeground(TABLE_CELL_SELECTED_FG); table.setShowGrid(true); table.setIntercellSpacing(new Dimension(0,1)); JTableHeader h=table.getTableHeader(); h.setBackground(TABLE_HEADER_BG); h.setForeground(TABLE_HEADER_FG); h.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14)); h.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); h.setReorderingAllowed(true); h.setResizingAllowed(true); DefaultTableCellRenderer r=new DefaultTableCellRenderer(); r.setHorizontalAlignment(SwingConstants.LEFT); r.setVerticalAlignment(SwingConstants.CENTER); r.setBorder(new EmptyBorder(2,5,2,5)); for(int i=0;i<table.getColumnCount()-1;i++){table.getColumnModel().getColumn(i).setCellRenderer(r);} }
    private void styleScrollPane(JScrollPane scrollPane) { /* ... */ scrollPane.setOpaque(false); scrollPane.getViewport().setOpaque(false); scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); applyScrollbarUI(scrollPane.getVerticalScrollBar()); applyScrollbarUI(scrollPane.getHorizontalScrollBar()); }
    private void applyScrollbarUI(JScrollBar scrollBar) { /* ... */ scrollBar.setUI(new BasicScrollBarUI() { @Override protected void configureScrollBarColors(){this.thumbColor=BUTTON_BG_DARK; this.trackColor=DARK_BG_END;} @Override protected JButton createDecreaseButton(int o){return createZeroButton();} @Override protected JButton createIncreaseButton(int o){return createZeroButton();} private JButton createZeroButton(){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));b.setMaximumSize(new Dimension(0,0));b.setMinimumSize(new Dimension(0,0));return b;} @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r){g.setColor(thumbColor);g.fillRect(r.x,r.y,r.width,r.height);} @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r){g.setColor(trackColor);g.fillRect(r.x,r.y,r.width,r.height);} }); scrollBar.setUnitIncrement(16); }
    private void styleActionButton(JButton btn, String tooltip) { /* ... */ btn.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12)); btn.setToolTipText(tooltip); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setForeground(BUTTON_FG_DARK); btn.setFocusPainted(false); btn.setBackground(BUTTON_BG_DARK); Border p=new EmptyBorder(6,12,6,12); btn.setBorder(new CompoundBorder(BorderFactory.createLineBorder(BUTTON_BG_DARK.darker()),p)); btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){if(btn.getBackground().equals(BUTTON_BG_DARK)){btn.setBackground(BUTTON_HOVER_BG_DARK);}}@Override public void mouseExited(MouseEvent e){if(btn.getBackground().equals(BUTTON_HOVER_BG_DARK)){btn.setBackground(BUTTON_BG_DARK);}}}); }
    // --- Inner classes for Table Button (Unchanged) ---
    static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer { /* ... */ private Color defaultBg; public ButtonRenderer(Color background){setOpaque(true);this.defaultBg=background;setForeground(BUTTON_FG_DARK);setBackground(defaultBg);setBorder(new EmptyBorder(2,5,2,5));setFont(new Font(Font.SANS_SERIF,Font.BOLD,11));} @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){setText((v==null)?"":v.toString());setBackground(s?defaultBg.brighter():defaultBg);return this;} }
    static class ButtonEditor extends DefaultCellEditor { /* ... */ protected JButton button; private String label; private boolean isPushed; private Runnable action; private Color bgColor; public ButtonEditor(JCheckBox c,Color bg,Runnable act){super(c);this.action=act;this.bgColor=bg;button=new JButton();button.setOpaque(true);button.setForeground(BUTTON_FG_DARK);button.setBackground(bgColor);button.setBorder(new EmptyBorder(2,5,2,5));button.setFont(new Font(Font.SANS_SERIF,Font.BOLD,11));button.addActionListener(e->fireEditingStopped());} @Override public Component getTableCellEditorComponent(JTable t,Object v,boolean s,int r,int c){label=(v==null)?"":v.toString();button.setText(label);isPushed=true;return button;} @Override public Object getCellEditorValue(){if(isPushed&&action!=null){action.run();}isPushed=false;return label;} @Override public boolean stopCellEditing(){isPushed=false;return super.stopCellEditing();} @Override protected void fireEditingStopped(){super.fireEditingStopped();} }
    // --- Integration Notes (Unchanged) ---
}""",
    "HomePanel.java": """package com.orphanagehub.gui;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D; // Keep this import
import java.net.URL; // *** RE-ADDED for Image Loading ***
public class HomePanel extends JPanel {
    private OrphanageHubApp mainApp;
    private JRadioButton rbDonor;
    private JRadioButton rbStaff;
    private JRadioButton rbVolunteer;
    private ButtonGroup roleGroup;
    // Define Colors for a Sleek Dark Theme
    private static final Color DARK_BG_START = new Color(45, 52, 54); // Dark Grey/Blue Start
    private static final Color DARK_BG_END = new Color(35, 42, 44); // Slightly Darker End
    private static final Color TITLE_COLOR_DARK = new Color(223, 230, 233); // Light Grey for Titles
    private static final Color TEXT_COLOR_DARK = new Color(200, 200, 200); // Slightly dimmer Grey for Text
    private static final Color BORDER_COLOR_DARK = new Color(80, 80, 80); // Darker Border
    private static final Color BUTTON_BG_DARK = new Color(99, 110, 114); // Muted Grey/Blue Button
    private static final Color BUTTON_FG_DARK = Color.WHITE;
    private static final Color BUTTON_HOVER_BG_DARK = new Color(120, 130, 134); // Lighter Hover
    private static final Color FALLBACK_BG_DARK = new Color(60, 60, 60); // Background for image fallback
    public HomePanel(OrphanageHubApp app) {
        this.mainApp = app;
        setBorder(new EmptyBorder(30, 40, 30, 40));
        setLayout(new BorderLayout(20, 20));
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
        // --- North: Title ---
        JLabel lblTitle = new JLabel("Welcome to OrphanageHub", SwingConstants.CENTER);
        lblTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        lblTitle.setForeground(TITLE_COLOR_DARK);
        lblTitle.setBorder(new EmptyBorder(0, 0, 25, 0));
        add(lblTitle, BorderLayout.NORTH);
        // --- Center: Image and Description ---
        JPanel centerPanel = new JPanel(new BorderLayout(30, 0)); // Gap between image and text
        centerPanel.setOpaque(false); // Crucial: Make center panel transparent
        // *** Image Loading and Styling (Reintroduced) ***
        JLabel lblImage = new JLabel();
        Dimension imageSize = new Dimension(220, 220); // Define image size
        lblImage.setPreferredSize(imageSize);
        lblImage.setMinimumSize(imageSize); // Prevent shrinking
        lblImage.setMaximumSize(imageSize); // Prevent expanding
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblImage.setVerticalAlignment(SwingConstants.CENTER);
        lblImage.setOpaque(false); // Image label itself is transparent
        URL imageURL = getClass().getResource("home.png"); // Load image relative to class file
        if (imageURL != null) {
            try {
                ImageIcon icon = new ImageIcon(imageURL);
                if (icon.getImageLoadStatus() == MediaTracker.COMPLETE && icon.getIconWidth() > 0) {
                    Image img = icon.getImage().getScaledInstance(imageSize.width, imageSize.height, Image.SCALE_SMOOTH);
                    lblImage.setIcon(new ImageIcon(img));
                    // Add a subtle border *only* if image loads successfully
                    lblImage.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK, 1));
                } else {
                    throw new Exception("Image loading failed or width is zero.");
                }
            } catch (Exception e) {
                 System.err.println("ERROR: Failed to load or scale home.png: " + e.getMessage());
                 setFallbackImageStyleDark(lblImage); // Use dark theme fallback
            }
        } else {
            System.err.println("Warning: home.png not found in classpath relative to HomePanel.class.");
            setFallbackImageStyleDark(lblImage); // Use dark theme fallback
        }
        centerPanel.add(lblImage, BorderLayout.WEST); // Add image to the left
        // Description Text
        String htmlDesc = "<html><body style='width:350px; font-family: Sans-Serif; font-size: 14pt; color: rgb(200,200,200);'>" // Adjusted width
                + "<p><b>A better world starts with care.</b></p>"
                + "<p>OrphanageHub connects orphanages with the donors and volunteers needed "
                + "to create lasting change for vulnerable children.</p>"
                + "</body></html>";
        JLabel lblDesc = new JLabel(htmlDesc);
        lblDesc.setVerticalAlignment(SwingConstants.CENTER); // Center text vertically relative to image
        lblDesc.setHorizontalAlignment(SwingConstants.LEFT); // Align text left
        lblDesc.setOpaque(false); // Make label transparent
        lblDesc.setBorder(new EmptyBorder(0, 10, 0, 0)); // Add slight left padding for text
        centerPanel.add(lblDesc, BorderLayout.CENTER); // Add description next to image
        add(centerPanel, BorderLayout.CENTER); // Add the combined panel to main layout
        // --- South: Role Selection and Actions --- (Structure remains the same)
        JPanel southPanel = new JPanel(new BorderLayout(10, 20));
        southPanel.setOpaque(false);
        // Role Selection Panel
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
        rbDonor = new JRadioButton("Donor", true);
        rbStaff = new JRadioButton("Orphanage Staff");
        rbVolunteer = new JRadioButton("Volunteer");
        styleRadioButton(rbDonor, "Select if you wish to donate or view needs.");
        styleRadioButton(rbStaff, "Select if you manage an orphanage profile.");
        styleRadioButton(rbVolunteer, "Select if you want to find volunteer opportunities.");
        roleGroup = new ButtonGroup();
        roleGroup.add(rbDonor);
        roleGroup.add(rbStaff);
        roleGroup.add(rbVolunteer);
        rolePanel.add(rbDonor);
        rolePanel.add(rbStaff);
        rolePanel.add(rbVolunteer);
        southPanel.add(rolePanel, BorderLayout.CENTER);
        // Action Buttons Panel
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
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);
    }
    // *** Fallback method adapted for dark theme ***
    private void setFallbackImageStyleDark(JLabel label) {
        label.setText("<html><div style='text-align: center; color: #AAAAAA;'>Image<br>Not Found<br>(home.png)</div></html>"); // Lighter grey text
        label.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 14));
        label.setForeground(new Color(170, 170, 170)); // Match text color in HTML
        label.setBorder(BorderFactory.createDashedBorder(BORDER_COLOR_DARK, 5, 5)); // Use dark border color
        label.setOpaque(true); // Make background visible for border
        label.setBackground(FALLBACK_BG_DARK); // Dark background for placeholder
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
    public String getSelectedRole() {
        if (rbDonor.isSelected()) return "Donor";
        if (rbStaff.isSelected()) return "OrphanageStaff";
        if (rbVolunteer.isSelected()) return "Volunteer";
        return "Unknown";
    }
}""",
    "LoginPanel.java": """package com.orphanagehub.gui;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D; // Keep this import
public class LoginPanel extends JPanel {
    private OrphanageHubApp mainApp;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    // --- Colors (Consider shared constants class) ---
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
    private static final Color LINK_COLOR = new Color(100, 180, 255);
    public LoginPanel(OrphanageHubApp app) {
        this.mainApp = app;
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
        // --- Title ---
        JLabel lblTitle = new JLabel("User Login", SwingConstants.CENTER);
        lblTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        lblTitle.setForeground(TITLE_COLOR_DARK);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.insets = new Insets(0, 5, 25, 5);
        add(lblTitle, gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(8, 5, 8, 5);
        // --- Username ---
        JLabel lblUsername = new JLabel("Username:"); styleFormLabel(lblUsername);
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        add(lblUsername, gbc);
        txtUsername = new JTextField(20); styleTextField(txtUsername);
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        add(txtUsername, gbc);
        // --- Password ---
        JLabel lblPassword = new JLabel("Password:"); styleFormLabel(lblPassword);
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        add(lblPassword, gbc);
        txtPassword = new JPasswordField(20); styleTextField(txtPassword);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        add(txtPassword, gbc);
        // --- Login Button ---
        JButton btnLogin = new JButton("Login"); styleActionButton(btnLogin, "Authenticate and access your dashboard");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER; gbc.insets = new Insets(20, 5, 10, 5);
        add(btnLogin, gbc);
        // --- Links Panel ---
        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0)); linksPanel.setOpaque(false);
        JLabel lblForgotPassword = createHyperlinkLabel("Forgot Password?"); lblForgotPassword.setToolTipText("Click here to reset your password");
        lblForgotPassword.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { JOptionPane.showMessageDialog(LoginPanel.this, "Password reset functionality not yet implemented.", "Forgot Password", JOptionPane.INFORMATION_MESSAGE); }});
        JLabel lblRegister = createHyperlinkLabel("Need an account? Register"); lblRegister.setToolTipText("Click here to go to the registration page");
        lblRegister.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { mainApp.navigateTo(OrphanageHubApp.REGISTRATION_PANEL); }});
        linksPanel.add(lblForgotPassword); linksPanel.add(lblRegister);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.insets = new Insets(5, 5, 15, 5);
        add(linksPanel, gbc);
        // --- Back Button ---
        JButton btnBack = new JButton("Back"); styleActionButton(btnBack, "Return to the welcome screen"); btnBack.setBackground(BUTTON_BG_DARK.darker());
        btnBack.addMouseListener(new MouseAdapter() { @Override public void mouseEntered(MouseEvent e) { btnBack.setBackground(BUTTON_HOVER_BG_DARK); } @Override public void mouseExited(MouseEvent e) { btnBack.setBackground(BUTTON_BG_DARK.darker()); }});
        btnBack.addActionListener(e -> mainApp.navigateTo(OrphanageHubApp.HOME_PANEL));
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.insets = new Insets(10, 5, 5, 5);
        add(btnBack, gbc);
        // *** FULLY UPDATED Action Listener for Login Button ***
        btnLogin.addActionListener(e -> {
            String username = txtUsername.getText().trim(); // Trim input
            String password = new String(txtPassword.getPassword());
            // --- Real authentication logic will replace this block ---
            boolean loginSuccess = false;
            String targetDashboard = OrphanageHubApp.HOME_PANEL; // Default fallback
            // Placeholder Credentials Check (CASE-SENSITIVE)
            if (username.equals("staff") && password.equals("pass")) {
                loginSuccess = true;
                targetDashboard = OrphanageHubApp.ORPHANAGE_DASHBOARD_PANEL;
                System.out.println("Attempting login for Staff...");
            } else if (username.equals("donor") && password.equals("pass")) {
                loginSuccess = true;
                targetDashboard = OrphanageHubApp.DONOR_DASHBOARD_PANEL;
                System.out.println("Attempting login for Donor...");
            } else if (username.equals("volunteer") && password.equals("pass")) {
                loginSuccess = true;
                targetDashboard = OrphanageHubApp.VOLUNTEER_DASHBOARD_PANEL;
                System.out.println("Attempting login for Volunteer...");
            } else if (username.equals("admin") && password.equals("pass")) {
                loginSuccess = true;
                targetDashboard = OrphanageHubApp.ADMIN_DASHBOARD_PANEL;
                System.out.println("Attempting login for Admin...");
            }
            // --- End of placeholder logic ---
            if (loginSuccess) {
                System.out.println("Login Success! Target: " + targetDashboard);
                // Use showDashboard for all dashboard panels
                mainApp.showDashboard(targetDashboard);
            } else {
                System.out.println("Login Failed for user: " + username);
                // Provide more helpful hint including all placeholder users
                JOptionPane.showMessageDialog(LoginPanel.this,
                        "Invalid Username or Password.\\n(Hints: staff/pass, donor/pass, volunteer/pass, admin/pass)",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText(""); // Clear password field
                txtUsername.requestFocusInWindow(); // Focus username field
            }
        });
        // *** END OF UPDATED Action Listener ***
    }
    // --- Styling Helper Methods (Unchanged) ---
    private void styleFormLabel(JLabel label) { label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14)); label.setForeground(TEXT_COLOR_DARK); }
    private void styleTextField(JComponent field) { field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14)); field.setForeground(INPUT_FG_DARK); field.setBackground(INPUT_BG_DARK); Border p=new EmptyBorder(5,8,5,8); field.setBorder(new CompoundBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK,1),p)); if(field instanceof JTextField)((JTextField)field).setCaretColor(Color.LIGHT_GRAY); else if(field instanceof JPasswordField)((JPasswordField)field).setCaretColor(Color.LIGHT_GRAY); }
    private JLabel createHyperlinkLabel(String text) { JLabel l=new JLabel("<html><u>"+text+"</u></html>"); l.setForeground(LINK_COLOR); l.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12)); l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return l; }
    private void styleActionButton(JButton btn, String tooltip) { btn.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14)); btn.setPreferredSize(new Dimension(130,40)); btn.setToolTipText(tooltip); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setBackground(BUTTON_BG_DARK); btn.setForeground(BUTTON_FG_DARK); btn.setFocusPainted(false); Border l=BorderFactory.createLineBorder(BUTTON_BG_DARK.darker()); Border p=new EmptyBorder(5,15,5,15); btn.setBorder(new CompoundBorder(l,p)); btn.addMouseListener(new MouseAdapter() { @Override public void mouseEntered(MouseEvent e){if(btn.getBackground().equals(BUTTON_BG_DARK)){btn.setBackground(BUTTON_HOVER_BG_DARK);}} @Override public void mouseExited(MouseEvent e){if(btn.getBackground().equals(BUTTON_HOVER_BG_DARK)){btn.setBackground(BUTTON_BG_DARK);}} }); }
}""",
    "OrphanageDashboardPanel.java": """package com.orphanagehub.gui;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI; // For scrollbar styling
import javax.swing.table.DefaultTableCellRenderer; // For table cell styling
import javax.swing.table.JTableHeader; // For table header styling
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
// --- NOTE: This code is identical to the previous step ---
// --- It is included here only for completeness ---
public class OrphanageDashboardPanel extends JPanel {
    private OrphanageHubApp mainApp;
    private String orphanageName = "Hope Children's Home"; // Placeholder
    private String staffUsername = "staff_user"; // Placeholder
    // Define Colors (Consider moving to a shared constants class/interface)
    private static final Color DARK_BG_START = new Color(45, 52, 54);
    private static final Color DARK_BG_END = new Color(35, 42, 44);
    private static final Color TITLE_COLOR_DARK = new Color(223, 230, 233);
    private static final Color TEXT_COLOR_DARK = new Color(200, 200, 200);
    private static final Color BORDER_COLOR_DARK = new Color(80, 80, 80);
    private static final Color BUTTON_BG_DARK = new Color(99, 110, 114);
    private static final Color BUTTON_FG_DARK = Color.WHITE;
    private static final Color BUTTON_HOVER_BG_DARK = new Color(120, 130, 134);
    private static final Color TAB_BG_SELECTED = new Color(70, 80, 82); // Slightly lighter for selected tab
    private static final Color TAB_BG_UNSELECTED = new Color(55, 62, 64);
    private static final Color TAB_FG = TITLE_COLOR_DARK;
    private static final Color TABLE_HEADER_BG = new Color(65, 75, 77);
    private static final Color TABLE_HEADER_FG = TITLE_COLOR_DARK;
    private static final Color TABLE_GRID_COLOR = BORDER_COLOR_DARK;
    private static final Color TABLE_CELL_BG = new Color(55, 62, 64);
    private static final Color TABLE_CELL_FG = TEXT_COLOR_DARK;
    private static final Color TABLE_CELL_SELECTED_BG = BUTTON_BG_DARK;
    private static final Color TABLE_CELL_SELECTED_FG = BUTTON_FG_DARK;
    private static final Color ACCENT_COLOR_ORANGE = new Color(230, 145, 56); // Accent for stats
    private static final Color ACCENT_COLOR_BLUE = new Color(72, 149, 239);
    private static final Color ACCENT_COLOR_GREEN = new Color(87, 190, 106);
    public OrphanageDashboardPanel(OrphanageHubApp app) {
        this.mainApp = app;
        setLayout(new BorderLayout(0, 0)); // No gaps for seamless gradient
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
        // --- Header Panel ---
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        // --- Tabbed Pane for Content ---
        JTabbedPane tabbedPane = createTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
    }
    // --- Helper Methods ---
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false); // Show gradient background
        headerPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR_DARK), // Bottom border
                new EmptyBorder(10, 20, 10, 20) // Padding
        ));
        // Left side: Orphanage Name and Role Icon
        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleGroup.setOpaque(false);
        JLabel iconLabel = new JLabel("\\u2302"); // House symbol
        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        iconLabel.setForeground(new Color(135, 206, 250)); // Light Sky Blue (match registration)
        JLabel nameLabel = new JLabel(orphanageName); // Placeholder name
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        nameLabel.setForeground(TITLE_COLOR_DARK);
        titleGroup.add(iconLabel);
        titleGroup.add(nameLabel);
        headerPanel.add(titleGroup, BorderLayout.WEST);
        // Right side: User info and Logout Button
        JPanel userGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userGroup.setOpaque(false);
        JLabel userLabel = new JLabel("User: " + staffUsername); // Placeholder user
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        userLabel.setForeground(TEXT_COLOR_DARK);
        JButton btnLogout = new JButton("Logout");
        styleActionButton(btnLogout, "Logout and return to welcome screen");
        btnLogout.setPreferredSize(new Dimension(100, 30)); // Smaller button
        btnLogout.setBackground(new Color(192, 57, 43)); // Reddish logout button
        btnLogout.addMouseListener(new MouseAdapter() { // Custom hover/exit for logout
             @Override public void mouseEntered(MouseEvent e) { btnLogout.setBackground(new Color(231, 76, 60)); }
             @Override public void mouseExited(MouseEvent e) { btnLogout.setBackground(new Color(192, 57, 43)); }
        });
        btnLogout.addActionListener(e -> {
            // Placeholder: Add confirmation dialog?
            mainApp.navigateTo(OrphanageHubApp.HOME_PANEL);
        });
        userGroup.add(userLabel);
        userGroup.add(btnLogout);
        headerPanel.add(userGroup, BorderLayout.EAST);
        return headerPanel;
    }
    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setOpaque(false); // Show gradient through tab area background
        tabbedPane.setForeground(TAB_FG); // Text color for tabs
        tabbedPane.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        // Apply custom UI for tab styling (more control than basic setBackground/Foreground)
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                // Use defined colors
                lightHighlight = TAB_BG_SELECTED; // Color for selected tab border top/left
                shadow = BORDER_COLOR_DARK; // Color for unselected tab border bottom/right
                darkShadow = DARK_BG_END; // Outer border color maybe?
                focus = TAB_BG_SELECTED; // Focus indicator color
            }
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                g.setColor(isSelected ? TAB_BG_SELECTED : TAB_BG_UNSELECTED);
                // Paint a slightly rounded rectangle for the tab background
                 switch (tabPlacement) {
                    case TOP:
                    default:
                        g.fillRoundRect(x, y, w, h + 5, 5, 5); // Extend height slightly for overlap look
                        break;
                    // Add cases for other placements if needed
                 }
            }
             @Override
             protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                 // Don't paint the default border, or paint a minimal one
                 g.setColor(BORDER_COLOR_DARK);
                  switch (tabPlacement) {
                    case TOP:
                    default:
                         if (isSelected) {
                             // No border needed for selected? Or just bottom?
                             // g.drawLine(x, y + h, x + w, y + h); // Bottom line only for selected
                         } else {
                              // Maybe a top line for unselected?
                              // g.drawLine(x, y, x + w -1 , y);
                         }
                         break;
                  }
             }
            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                 // Paint a border around the content area to match tabs
                 int width = tabPane.getWidth();
                 int height = tabPane.getHeight();
                 Insets insets = tabPane.getInsets();
                 // Insets tabAreaInsets = getTabAreaInsets(tabPlacement); // Not needed directly
                 int tabAreaHeight = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                 int x = insets.left;
                 // Adjusted y calculation based on how BasicTabbedPaneUI calculates content border y
                 int y = insets.top + tabAreaHeight - (lightHighlight.getAlpha() > 0 ? 1 : 0); // Approximate adjustment
                 int w = width - insets.right - insets.left;
                 int h = height - insets.top - insets.bottom - y;
                 g.setColor(BORDER_COLOR_DARK); // Use border color
                 g.drawRect(x, y, w - 1, h - 1); // Draw border around content
            }
        });
        // Create and add tabs
        tabbedPane.addTab("Overview", createOverviewTab());
        tabbedPane.addTab("Resource Requests", createResourceRequestsTab());
        tabbedPane.addTab("Donations", createPlaceholderTab("Donations Management"));
        tabbedPane.addTab("Volunteers", createPlaceholderTab("Volunteer Management"));
        tabbedPane.addTab("Orphanage Profile", createPlaceholderTab("Orphanage Profile Editor"));
        return tabbedPane;
    }
    private JPanel createOverviewTab() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 20)); // Grid for stat cards
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        panel.add(createStatCard("Active Requests", "12", ACCENT_COLOR_ORANGE));
        panel.add(createStatCard("Pending Donations", "3", ACCENT_COLOR_BLUE));
        panel.add(createStatCard("Active Volunteers", "8", ACCENT_COLOR_GREEN));
        return panel;
    }
    private JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(TAB_BG_UNSELECTED); // Use tab background
        card.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, accentColor), // Accent color bottom border
                new EmptyBorder(15, 20, 15, 20) // Padding
        ));
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
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
        panel.setOpaque(false); // Let tab content area show background if needed
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        // --- Toolbar ---
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);
        JButton btnAdd = new JButton("Add Request");
        JButton btnEdit = new JButton("Edit Selected");
        JButton btnDelete = new JButton("Delete Selected");
        styleActionButton(btnAdd, "Create a new resource request");
        styleActionButton(btnEdit, "Modify the selected request");
        styleActionButton(btnDelete, "Remove the selected request");
        // Distinguish delete button maybe?
        btnDelete.setBackground(new Color(192, 57, 43)); // Reddish
        btnDelete.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnDelete.setBackground(new Color(231, 76, 60)); }
            @Override public void mouseExited(MouseEvent e) { btnDelete.setBackground(new Color(192, 57, 43)); }
        });
        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        panel.add(toolbar, BorderLayout.NORTH);
        // --- Table ---
        String[] columnNames = {"ID", "Category", "Description", "Needed", "Fulfilled", "Urgency", "Status"};
        Object[][] data = { // Placeholder data
                {"REQ001", "Food", "Rice (50kg bags)", 10, 4, "High", "Open"},
                {"REQ002", "Clothing", "Winter jackets (S)", 15, 15, "Medium", "Fulfilled"},
                {"REQ003", "Education", "Notebooks", 50, 20, "Low", "Open"},
                {"REQ004", "Medical", "First Aid Kits", 5, 1, "High", "Open"},
                {"REQ005", "Funding", "Roof Repair", 1, 0, "Urgent", "Open"}
        };
        JTable table = new JTable(data, columnNames);
        styleTable(table); // Apply dark theme styling
        JScrollPane scrollPane = new JScrollPane(table);
        styleScrollPane(scrollPane); // Apply dark theme styling
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    // Generic placeholder tab content
    private JPanel createPlaceholderTab(String title) {
        JPanel panel = new JPanel(new GridBagLayout()); // Use GBL to center content
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel label = new JLabel(title + " - Content Area", SwingConstants.CENTER);
        label.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 18));
        label.setForeground(TEXT_COLOR_DARK);
        panel.add(label); // Add centered label
        return panel;
    }
    // Helper method to style JTable for dark theme
    private void styleTable(JTable table) {
        table.setBackground(TABLE_CELL_BG);
        table.setForeground(TABLE_CELL_FG);
        table.setGridColor(TABLE_GRID_COLOR);
        table.setRowHeight(28); // Increased row height
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        table.setFillsViewportHeight(true); // Table fills scrollpane height
        // Selection colors
        table.setSelectionBackground(TABLE_CELL_SELECTED_BG);
        table.setSelectionForeground(TABLE_CELL_SELECTED_FG);
        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TABLE_HEADER_FG);
        header.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        header.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); // Border for header
        // Prevent column reordering/resizing (optional)
        // header.setReorderingAllowed(false);
        // header.setResizingAllowed(false);
        // Cell renderer (optional - for padding or specific alignment)
         DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
         centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
         DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
         leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
         table.getColumnModel().getColumn(0).setPreferredWidth(60); // ID
         table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
         table.getColumnModel().getColumn(1).setPreferredWidth(100); // Category
         table.getColumnModel().getColumn(2).setPreferredWidth(250); // Description
         table.getColumnModel().getColumn(3).setPreferredWidth(80); // Needed
         table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
         table.getColumnModel().getColumn(4).setPreferredWidth(80); // Fulfilled
         table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
         table.getColumnModel().getColumn(5).setPreferredWidth(100); // Urgency
         table.getColumnModel().getColumn(6).setPreferredWidth(100); // Status
    }
    // Helper method to style JScrollPane for dark theme
    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); // Border for scrollpane
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
        applyScrollbarUI(verticalScrollBar);
        applyScrollbarUI(horizontalScrollBar);
    }
    // Helper to apply consistent scrollbar UI
    private void applyScrollbarUI(JScrollBar scrollBar) {
         scrollBar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = BUTTON_BG_DARK;
                this.trackColor = DARK_BG_END;
                this.thumbDarkShadowColor = this.thumbColor.darker();
                this.thumbHighlightColor = this.thumbColor.brighter();
            }
             @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
             @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
             private JButton createZeroButton() {
                 JButton button = new JButton();
                 button.setPreferredSize(new Dimension(0, 0));
                 button.setMinimumSize(new Dimension(0, 0));
                 button.setMaximumSize(new Dimension(0, 0));
                 return button;
             }
             // Optional: Make thumb borderless or match thumb color
             @Override protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                 g.setColor(thumbColor);
                 g.fillRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);
             }
             // Optional: Make track match background more closely
             @Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                 g.setColor(trackColor);
                 g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
             }
        });
        scrollBar.setUnitIncrement(16);
    }
    // Reusable action button styling method
    private void styleActionButton(JButton btn, String tooltip) {
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12)); // Slightly smaller font for toolbar
        btn.setToolTipText(tooltip);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setForeground(BUTTON_FG_DARK);
        btn.setFocusPainted(false);
        btn.setBackground(BUTTON_BG_DARK); // Default background
        // Padding inside button
        Border padding = new EmptyBorder(6, 12, 6, 12);
        btn.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(BUTTON_BG_DARK.darker()), // Subtle border
                padding));
        // Default hover listener (can be overridden)
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!btn.getBackground().equals(new Color(192, 57, 43))) // Don't override delete/logout hover
                    btn.setBackground(BUTTON_HOVER_BG_DARK);
            }
            @Override public void mouseExited(MouseEvent e) {
                 if (!btn.getBackground().equals(new Color(192, 57, 43))) // Don't override delete/logout exit
                    btn.setBackground(BUTTON_BG_DARK);
            }
        });
    }
}""",
    "OrphanageHubApp.java": """package com.orphanagehub.gui;
import javax.swing.*;
import java.awt.*;
public class OrphanageHubApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    // Panel Instances (keep references)
    private HomePanel homePanel;
    private LoginPanel loginPanel;
    private RegistrationPanel registrationPanel;
    private OrphanageDashboardPanel orphanageDashboardPanel;
    private DonorDashboardPanel donorDashboardPanel; // Added reference
    private VolunteerDashboardPanel volunteerDashboardPanel; // Added reference
    private AdminDashboardPanel adminDashboardPanel; // Added reference
    // Panel names for CardLayout
    public static final String HOME_PANEL = "Home";
    public static final String LOGIN_PANEL = "Login";
    public static final String REGISTRATION_PANEL = "Registration";
    public static final String ORPHANAGE_DASHBOARD_PANEL = "OrphanageDashboard";
    public static final String DONOR_DASHBOARD_PANEL = "DonorDashboard"; // Added constant
    public static final String VOLUNTEER_DASHBOARD_PANEL = "VolunteerDashboard"; // Added constant
    public static final String ADMIN_DASHBOARD_PANEL = "AdminDashboard"; // Added constant
    public OrphanageHubApp() {
        super("OrphanageHub");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Set Nimbus Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("CRITICAL FAILURE: Cannot set Nimbus Look and Feel. UI may appear incorrect.");
        }
        initComponents(); // Initialize components and layout
        // Set initial size
        setPreferredSize(new Dimension(900, 700)); // Increased default size for dashboards
        pack();
        setMinimumSize(new Dimension(750, 550)); // Adjusted minimum size
        setLocationRelativeTo(null);
        setResizable(true);
    }
    private void initComponents() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        // Instantiate CORE panels immediately
        homePanel = new HomePanel(this);
        loginPanel = new LoginPanel(this);
        registrationPanel = new RegistrationPanel(this);
        // Dashboard panels are instantiated on demand via showDashboard()
        // Add core panels to the CardLayout container
        mainPanel.add(homePanel, HOME_PANEL);
        mainPanel.add(loginPanel, LOGIN_PANEL);
        mainPanel.add(registrationPanel, REGISTRATION_PANEL);
        // Dashboard panels are added later
        setContentPane(mainPanel);
    }
    // --- Navigation Methods ---
    /**
     * Navigates directly to a panel already added to the CardLayout.
     * @param panelName The name constant of the panel to show.
     */
    public void navigateTo(String panelName) {
        System.out.println("Navigating to: " + panelName); // Debug
        cardLayout.show(mainPanel, panelName);
    }
    /**
     * Creates (if necessary) and navigates to a dashboard panel.
     * Handles lazy instantiation of dashboard panels.
     * @param panelName The name constant of the dashboard panel to show.
     */
    public void showDashboard(String panelName) {
        System.out.println("Attempting to show dashboard: " + panelName); // Debug
        boolean panelAdded = false; // Flag to track if a panel was added
        // Ensure dashboard panels are created and added before showing
        if (panelName.equals(ORPHANAGE_DASHBOARD_PANEL)) {
            if (orphanageDashboardPanel == null) {
                System.out.println("Creating Orphanage Dashboard Panel...");
                orphanageDashboardPanel = new OrphanageDashboardPanel(this);
                mainPanel.add(orphanageDashboardPanel, ORPHANAGE_DASHBOARD_PANEL);
                panelAdded = true;
            }
            // Add logic later to pass actual user/orphanage data
        } else if (panelName.equals(DONOR_DASHBOARD_PANEL)) {
            if (donorDashboardPanel == null) {
                System.out.println("Creating Donor Dashboard Panel...");
                donorDashboardPanel = new DonorDashboardPanel(this);
                mainPanel.add(donorDashboardPanel, DONOR_DASHBOARD_PANEL);
                panelAdded = true;
            }
            // Add logic later to pass donor-specific data
        } else if (panelName.equals(VOLUNTEER_DASHBOARD_PANEL)) {
            if (volunteerDashboardPanel == null) {
                System.out.println("Creating Volunteer Dashboard Panel...");
                volunteerDashboardPanel = new VolunteerDashboardPanel(this);
                mainPanel.add(volunteerDashboardPanel, VOLUNTEER_DASHBOARD_PANEL);
                panelAdded = true;
            }
            // Add logic later to pass volunteer-specific data
        } else if (panelName.equals(ADMIN_DASHBOARD_PANEL)) {
            if (adminDashboardPanel == null) {
                System.out.println("Creating Admin Dashboard Panel...");
                adminDashboardPanel = new AdminDashboardPanel(this);
                mainPanel.add(adminDashboardPanel, ADMIN_DASHBOARD_PANEL);
                panelAdded = true;
            }
            // Add logic later to pass admin-specific data
        } else {
            System.err.println("Error: Attempted to show unknown or unsupported dashboard panel: " + panelName);
            navigateTo(HOME_PANEL); // Fallback to home screen
            return; // Exit early if panel name is invalid
        }
        // Revalidate the main panel *if* a new component was actually added
        if (panelAdded) {
            mainPanel.revalidate();
            System.out.println(panelName + " Added and Revalidated.");
        }
        navigateTo(panelName); // Navigate to the requested panel
    }
    // Method for panels to get the selected role from HomePanel
    public String getSelectedRole() {
        return (homePanel != null) ? homePanel.getSelectedRole() : "Unknown";
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            OrphanageHubApp app = new OrphanageHubApp();
            app.setVisible(true);
        });
    }
}""",
    "RegistrationPanel.java": """package com.orphanagehub.gui;
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
                this.trackColor = DARK_BG_END; // Use gradient end for track
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
                "Registration attempt for " + txtUsername.getText() + " as " + currentRole + ".\\n(Backend logic not implemented)",
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
                lblRoleIcon.setText("\\u2764"); // Heavy Black Heart symbol
                lblRoleIcon.setForeground(new Color(255, 105, 180)); // Pinkish
                break;
            case "OrphanageStaff":
                lblRoleIcon.setText("\\u2302"); // House symbol
                lblRoleIcon.setForeground(new Color(135, 206, 250)); // Light Sky Blue
                break;
            case "Volunteer":
                lblRoleIcon.setText("\\u2605"); // Black Star symbol
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
}""",
    "VolunteerDashboardPanel.java": """package com.orphanagehub.gui;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder; // For potential status panel
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
public class VolunteerDashboardPanel extends JPanel {
    private OrphanageHubApp mainApp;
    private String volunteerUsername = "volunteer_user"; // Placeholder
    // --- Colors (Same as AdminDashboardPanel) ---
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
    private static final Color BUTTON_APPLY_BG = new Color(87, 190, 106); // Greenish apply button
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
        // --- Header Panel ---
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        // --- Main Content Area (Search + Table + Status) ---
        JPanel contentPanel = new JPanel(new BorderLayout(10, 15));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(15, 20, 20, 20));
        // --- Search/Filter Panel ---
        JPanel searchFilterPanel = createSearchFilterPanel();
        contentPanel.add(searchFilterPanel, BorderLayout.NORTH);
        // --- Opportunities Table ---
        JTable opportunitiesTable = createOpportunitiesTable();
        JScrollPane scrollPane = new JScrollPane(opportunitiesTable);
        styleScrollPane(scrollPane);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        // --- Application Status Panel (Placeholder) ---
        JPanel statusPanel = createStatusPanel();
        contentPanel.add(statusPanel, BorderLayout.SOUTH);
        add(contentPanel, BorderLayout.CENTER);
    }
    // --- Helper Methods ---
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR_DARK),
                new EmptyBorder(10, 20, 10, 20)
        ));
        // Left side: Role Icon and Title
        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleGroup.setOpaque(false);
        JLabel iconLabel = new JLabel("\\u2605"); // Star symbol (match registration)
        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        iconLabel.setForeground(new Color(255, 215, 0)); // Gold color
        JLabel nameLabel = new JLabel("Volunteer Dashboard");
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        nameLabel.setForeground(TITLE_COLOR_DARK);
        titleGroup.add(iconLabel);
        titleGroup.add(nameLabel);
        headerPanel.add(titleGroup, BorderLayout.WEST);
        // Right side: User info and Logout Button
        JPanel userGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userGroup.setOpaque(false);
        JLabel userLabel = new JLabel("User: " + volunteerUsername);
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        userLabel.setForeground(TEXT_COLOR_DARK);
        JButton btnLogout = new JButton("Logout");
        styleActionButton(btnLogout, "Logout and return to welcome screen");
        btnLogout.setPreferredSize(new Dimension(100, 30));
        btnLogout.setBackground(new Color(192, 57, 43)); // Reddish logout
        btnLogout.addMouseListener(new MouseAdapter() {
             @Override public void mouseEntered(MouseEvent e) { btnLogout.setBackground(new Color(231, 76, 60)); }
             @Override public void mouseExited(MouseEvent e) { btnLogout.setBackground(new Color(192, 57, 43)); }
        });
        btnLogout.addActionListener(e -> mainApp.navigateTo(OrphanageHubApp.HOME_PANEL));
        userGroup.add(userLabel);
        userGroup.add(btnLogout);
        headerPanel.add(userGroup, BorderLayout.EAST);
        return headerPanel;
    }
    private JPanel createSearchFilterPanel() {
        // Similar structure to Donor search, different fields
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setOpaque(false);
        JLabel lblFilterLocation = new JLabel("Location:");
        styleFormLabel(lblFilterLocation);
        String[] locations = {"Any Location", "City A", "City B", "Region C"}; // Placeholders
        JComboBox<String> cmbLocation = new JComboBox<>(locations);
        styleComboBox(cmbLocation);
        JLabel lblFilterSkills = new JLabel("Skills:");
        styleFormLabel(lblFilterSkills);
        JTextField txtSkills = new JTextField(15); // Text field for skills keywords
        styleTextField(txtSkills);
        JLabel lblFilterTime = new JLabel("Commitment:");
        styleFormLabel(lblFilterTime);
        String[] times = {"Any Time", "Weekends", "Weekdays", "Flexible", "Event-Based"}; // Placeholders
        JComboBox<String> cmbTime = new JComboBox<>(times);
        styleComboBox(cmbTime);
        JButton btnSearch = new JButton("Find Opportunities");
        styleActionButton(btnSearch, "Search for volunteer roles matching criteria");
        // Use default button style or a specific search color? Default for now.
        btnSearch.addActionListener(e -> {
             JOptionPane.showMessageDialog(this, "Search logic not implemented.", "Search", JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(lblFilterLocation);
        panel.add(cmbLocation);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(lblFilterSkills);
        panel.add(txtSkills);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(lblFilterTime);
        panel.add(cmbTime);
        panel.add(Box.createHorizontalStrut(15));
        panel.add(btnSearch);
        return panel;
    }
     private JTable createOpportunitiesTable() {
        String[] columnNames = {"Orphanage", "Opportunity", "Location", "Skills Needed", "Time Commitment", "Action"};
        Object[][] data = { // Placeholder data
                {"Hope Children's Home", "Weekend Tutor", "City A", "Teaching, Patience", "Weekends", "Apply"},
                {"Bright Future Orphanage", "Event Helper", "City B", "Organizing, Energetic", "Event-Based", "Apply"},
                {"Little Angels Shelter", "After-School Care", "City A", "Childcare, First Aid", "Weekdays", "Applied"}, // Example status
                {"Sunshine House", "Gardening Assistant", "Region C", "Gardening", "Flexible", "Apply"},
                {"Hope Children's Home", "Reading Buddy", "City A", "Reading, Communication", "Weekdays", "Apply"}
        };
        JTable table = new JTable(data, columnNames) {
             @Override
             public boolean isCellEditable(int row, int column) {
                 // Allow interaction only on the last column if the text is "Apply"
                 return column == 5 && "Apply".equals(getValueAt(row, column));
             }
        };
        styleTable(table);
        // Add button renderer/editor for the "Action" column
        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer(BUTTON_APPLY_BG));
        // *** CORRECTED LAMBDA HERE (no 'e' parameter) ***
        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox(), BUTTON_APPLY_BG, () -> { // Changed e -> () ->
             int selectedRow = table.convertRowIndexToModel(table.getEditingRow());
             String oppName = (String) table.getModel().getValueAt(selectedRow, 1);
             String orphName = (String) table.getModel().getValueAt(selectedRow, 0);
             JOptionPane.showMessageDialog(this, "Apply for: " + oppName + " at " + orphName + "\\n(Functionality not implemented)", "Apply", JOptionPane.INFORMATION_MESSAGE);
             // Ideally, update the cell value to "Applied" or "Pending" after successful action
             // table.getModel().setValueAt("Applied", selectedRow, 5); // Requires DefaultTableModel
         }));
        // Adjust column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(150); // Orphanage
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // Opportunity
        table.getColumnModel().getColumn(2).setPreferredWidth(100); // Location
        table.getColumnModel().getColumn(3).setPreferredWidth(180); // Skills
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // Time
        table.getColumnModel().getColumn(5).setPreferredWidth(90); // Action
        return table;
    }
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        panel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR_DARK), // Top border separator
            new EmptyBorder(10, 5, 5, 5) // Padding
        ));
        JLabel lblStatus = new JLabel("Your Applications: 1 Pending (Little Angels Shelter)"); // Placeholder text
        lblStatus.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 13));
        lblStatus.setForeground(TEXT_COLOR_DARK);
        panel.add(lblStatus);
        return panel;
    }
    // --- Styling Helpers (Unchanged from previous version) ---
    private void styleFormLabel(JLabel label) { /* ... */ label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); label.setForeground(TEXT_COLOR_DARK); }
    private void styleTextField(JTextField field) { /* ... */ field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); field.setForeground(INPUT_FG_DARK); field.setBackground(INPUT_BG_DARK); Border p=new EmptyBorder(4,6,4,6); field.setBorder(new CompoundBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK,1),p)); field.setCaretColor(Color.LIGHT_GRAY); }
    private void styleComboBox(JComboBox<?> comboBox) { /* ... */ comboBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); comboBox.setForeground(INPUT_FG_DARK); comboBox.setBackground(INPUT_BG_DARK); comboBox.setBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK,1)); for(Component c:comboBox.getComponents()){if(c instanceof JButton){((JButton)c).setBackground(BUTTON_BG_DARK);((JButton)c).setBorder(BorderFactory.createEmptyBorder());break;}} Object p=comboBox.getUI().getAccessibleChild(comboBox,0); if(p instanceof JPopupMenu){JPopupMenu pm=(JPopupMenu)p;pm.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); for(Component comp:pm.getComponents()){if(comp instanceof JScrollPane){JScrollPane sp=(JScrollPane)comp;sp.getViewport().setBackground(INPUT_BG_DARK);applyScrollbarUI(sp.getVerticalScrollBar()); Component l=sp.getViewport().getView(); if(l instanceof JList){((JList<?>)l).setBackground(INPUT_BG_DARK);((JList<?>)l).setForeground(INPUT_FG_DARK);((JList<?>)l).setSelectionBackground(BUTTON_BG_DARK);((JList<?>)l).setSelectionForeground(BUTTON_FG_DARK);}}}}}
    private void styleTable(JTable table) { /* ... */ table.setBackground(TABLE_CELL_BG); table.setForeground(TABLE_CELL_FG); table.setGridColor(TABLE_GRID_COLOR); table.setRowHeight(28); table.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,13)); table.setFillsViewportHeight(true); table.setSelectionBackground(TABLE_CELL_SELECTED_BG); table.setSelectionForeground(TABLE_CELL_SELECTED_FG); table.setShowGrid(true); table.setIntercellSpacing(new Dimension(0,1)); JTableHeader h=table.getTableHeader(); h.setBackground(TABLE_HEADER_BG); h.setForeground(TABLE_HEADER_FG); h.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14)); h.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); h.setReorderingAllowed(true); h.setResizingAllowed(true); DefaultTableCellRenderer r=new DefaultTableCellRenderer(); r.setHorizontalAlignment(SwingConstants.LEFT); r.setVerticalAlignment(SwingConstants.CENTER); r.setBorder(new EmptyBorder(2,5,2,5)); for(int i=0;i<table.getColumnCount()-1;i++){table.getColumnModel().getColumn(i).setCellRenderer(r);} }
    private void styleScrollPane(JScrollPane scrollPane) { /* ... */ scrollPane.setOpaque(false); scrollPane.getViewport().setOpaque(false); scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); applyScrollbarUI(scrollPane.getVerticalScrollBar()); applyScrollbarUI(scrollPane.getHorizontalScrollBar()); }
    private void applyScrollbarUI(JScrollBar scrollBar) { /* ... */ scrollBar.setUI(new BasicScrollBarUI() { @Override protected void configureScrollBarColors(){this.thumbColor=BUTTON_BG_DARK; this.trackColor=DARK_BG_END;} @Override protected JButton createDecreaseButton(int o){return createZeroButton();} @Override protected JButton createIncreaseButton(int o){return createZeroButton();} private JButton createZeroButton(){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));b.setMaximumSize(new Dimension(0,0));b.setMinimumSize(new Dimension(0,0));return b;} @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r){g.setColor(thumbColor);g.fillRect(r.x,r.y,r.width,r.height);} @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r){g.setColor(trackColor);g.fillRect(r.x,r.y,r.width,r.height);} }); scrollBar.setUnitIncrement(16); }
    private void styleActionButton(JButton btn, String tooltip) { /* ... */ btn.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12)); btn.setToolTipText(tooltip); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setForeground(BUTTON_FG_DARK); btn.setFocusPainted(false); btn.setBackground(BUTTON_BG_DARK); Border p=new EmptyBorder(6,12,6,12); btn.setBorder(new CompoundBorder(BorderFactory.createLineBorder(BUTTON_BG_DARK.darker()),p)); btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){if(btn.getBackground().equals(BUTTON_BG_DARK)){btn.setBackground(BUTTON_HOVER_BG_DARK);}}@Override public void mouseExited(MouseEvent e){if(btn.getBackground().equals(BUTTON_HOVER_BG_DARK)){btn.setBackground(BUTTON_BG_DARK);}}}); }
    // --- Inner classes for Table Button (Unchanged) ---
    static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer { /* ... */ private Color defaultBg; public ButtonRenderer(Color background){setOpaque(true);this.defaultBg=background;setForeground(BUTTON_FG_DARK);setBackground(defaultBg);setBorder(new EmptyBorder(2,5,2,5));setFont(new Font(Font.SANS_SERIF,Font.BOLD,11));} @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){setText(v==null?"":v.toString());setBackground(s?defaultBg.brighter():defaultBg);return this;} }
    static class ButtonEditor extends DefaultCellEditor { /* ... */ protected JButton button; private String label; private boolean isPushed; private Runnable action; private Color bgColor; public ButtonEditor(JCheckBox c,Color bg,Runnable act){super(c);this.action=act;this.bgColor=bg;button=new JButton();button.setOpaque(true);button.setForeground(BUTTON_FG_DARK);button.setBackground(bgColor);button.setBorder(new EmptyBorder(2,5,2,5));button.setFont(new Font(Font.SANS_SERIF,Font.BOLD,11));button.addActionListener(e->fireEditingStopped());} @Override public Component getTableCellEditorComponent(JTable t,Object v,boolean s,int r,int c){label=(v==null)?"":v.toString();button.setText(label);isPushed=true;return button;} @Override public Object getCellEditorValue(){if(isPushed&&action!=null){action.run();}isPushed=false;return label;} @Override public boolean stopCellEditing(){isPushed=false;return super.stopCellEditing();} @Override protected void fireEditingStopped(){super.fireEditingStopped();} }
     // --- Integration Notes (Unchanged) ---
}"""
}

target_dir = "/home/jared/OrphanageHub_PAT2025/src/main/java/com/orphanagehub/gui"

if not os.path.exists(target_dir):
    print(f"Directory does not exist: {target_dir}")
else:
    for filename, content in file_contents.items():
        filepath = os.path.join(target_dir, filename)
        with open(filepath, 'w') as f:
            f.write(content)
        print(f"Restored {filename}")
