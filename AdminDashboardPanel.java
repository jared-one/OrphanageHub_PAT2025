package com.orphanagehub.gui;

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
    private static final Color TABLE_CELL_FG = TEXT_COLOR_DARK;
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
        JLabel iconLabel = new JLabel("\u2699"); // Gear symbol
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
                    JOptionPane.showMessageDialog(this, "Approve: " + orphanageName + "\n(Logic TBD)", "Approve", JOptionPane.INFORMATION_MESSAGE);
                    // Update table model status to "Verified"
                    break;
                case "reject":
                     if (JOptionPane.showConfirmDialog(this, "Reject " + orphanageName + "?", "Confirm Reject", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                         JOptionPane.showMessageDialog(this, "Reject: " + orphanageName + "\n(Logic TBD)", "Reject", JOptionPane.INFORMATION_MESSAGE);
                         // Update table model status to "Rejected"
                     }
                    break;
                case "details":
                    JOptionPane.showMessageDialog(this, "View Details for: " + orphanageName + "\n(Logic TBD)", "Details", JOptionPane.INFORMATION_MESSAGE);
                    break;
            }
        }));


        // Adjust column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(180); // Name
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Contact
        table.getColumnModel().getColumn(2).setPreferredWidth(180); // Email
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Registered
        table.getColumnModel().getColumn(4).setPreferredWidth(80);  // Status
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
                          JOptionPane.showMessageDialog(this, "Activate User: " + username + "\n(Logic TBD)", "Activate", JOptionPane.INFORMATION_MESSAGE);
                      }
                     break;
                 case "suspend":
                     if ("Active".equals(currentStatus)) {
                         if (JOptionPane.showConfirmDialog(this, "Suspend User: " + username + "?", "Confirm Suspend", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                             JOptionPane.showMessageDialog(this, "Suspend User: " + username + "\n(Logic TBD)", "Suspend", JOptionPane.INFORMATION_MESSAGE);
                         }
                     }
                     break;
                 case "view":
                     JOptionPane.showMessageDialog(this, "View User Profile: " + username + "\n(Logic TBD)", "View User", JOptionPane.INFORMATION_MESSAGE);
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
    private void styleActionButton(JButton btn, String tooltip) { btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12)); btn.setToolTipText(tooltip); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setForeground(BUTTON_FG_DARK); btn.setFocusPainted(false); btn.setBackground(BUTTON_BG_DARK); Border p=new EmptyBorder(6,12,6,12); btn.setBorder(new CompoundBorder(BorderFactory.createLineBorder(BUTTON_BG_DARK.darker()),p)); btn.addMouseListener(new MouseAdapter() { @Override public void mouseEntered(MouseEvent e){if(btn.getBackground().equals(BUTTON_BG_DARK)){btn.setBackground(BUTTON_HOVER_BG_DARK);}} @Override public void mouseExited(MouseEvent e){if(btn.getBackground().equals(BUTTON_HOVER_BG_DARK)){btn.setBackground(BUTTON_BG_DARK);}} }); }
    private void styleMiniButton(JButton btn, Color bg) { btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14)); btn.setMargin(new Insets(0, 2, 0, 2)); btn.setFocusPainted(false); btn.setBackground(bg); btn.setForeground(BUTTON_FG_DARK); btn.setBorder(BorderFactory.createLineBorder(bg.darker())); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }


    // --- Inner classes for Table Multi-Button Actions (Unchanged) ---
    static class ActionPanelRenderer implements javax.swing.table.TableCellRenderer { private JPanel panel; public ActionPanelRenderer(JPanel buttonPanel){this.panel=buttonPanel;} @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c){return panel;} }
    static class ActionPanelEditor extends DefaultCellEditor { private JPanel panel; private RowActionCallback callback; private int editingRow; interface RowActionCallback{void onAction(String command,int row);} public ActionPanelEditor(JCheckBox c, RowActionCallback cb){super(c);this.callback=cb;panel=new JPanel(new FlowLayout(FlowLayout.CENTER,3,0));panel.setOpaque(true);panel.setBackground(TABLE_CELL_BG); JButton b1=new JButton("âœ“");JButton b2=new JButton("âœ•");JButton b3=new JButton("..."); styleMiniButtonStatic(b1,BUTTON_APPROVE_BG);b1.setActionCommand("approve");styleMiniButtonStatic(b2,BUTTON_REJECT_BG);b2.setActionCommand("reject");styleMiniButtonStatic(b3,BUTTON_BG_DARK);b3.setActionCommand("view"); ActionListener l=e->{if(callback!=null){callback.onAction(e.getActionCommand(),editingRow);}fireEditingStopped();}; b1.addActionListener(l);b2.addActionListener(l);b3.addActionListener(l); panel.add(b1);panel.add(b2);panel.add(b3);} @Override public Component getTableCellEditorComponent(JTable t,Object v,boolean s,int r,int c){this.editingRow=r;String status="";String context=t.getColumnName(c); if(c==5&&t.getColumnName(5).equals("Actions")&&t.getModel().getRowCount()>r){ if(t.getColumnCount()>4 && t.getColumnName(4).equals("Status")){status=(String)t.getValueAt(r,4);((JButton)panel.getComponent(0)).setActionCommand("approve");((JButton)panel.getComponent(1)).setActionCommand("reject");((JButton)panel.getComponent(2)).setActionCommand("details");panel.getComponent(0).setVisible("Pending".equals(status));panel.getComponent(1).setVisible("Pending".equals(status));panel.getComponent(2).setVisible(true);} else if(t.getColumnCount()>3 && t.getColumnName(3).equals("Status")){status=(String)t.getValueAt(r,3);String u=(String)t.getValueAt(r,0);boolean self=u.equals("admin_user");((JButton)panel.getComponent(0)).setActionCommand("activate");((JButton)panel.getComponent(1)).setActionCommand("suspend");((JButton)panel.getComponent(2)).setActionCommand("view");panel.getComponent(0).setVisible("Suspended".equals(status)&&!self);panel.getComponent(1).setVisible("Active".equals(status)&&!self);panel.getComponent(2).setVisible(true);}} panel.setBackground(s?TABLE_CELL_SELECTED_BG:TABLE_CELL_BG); return panel;} @Override public Object getCellEditorValue(){return"";} @Override public boolean stopCellEditing(){return super.stopCellEditing();} @Override protected void fireEditingStopped(){super.fireEditingStopped();} private static void styleMiniButtonStatic(JButton btn,Color bg){btn.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14));btn.setMargin(new Insets(0,2,0,2));btn.setFocusPainted(false);btn.setBackground(bg);btn.setForeground(BUTTON_FG_DARK);btn.setBorder(BorderFactory.createLineBorder(bg.darker()));btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));} }

} // End of AdminDashboardPanel class
