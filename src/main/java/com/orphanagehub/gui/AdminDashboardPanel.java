package com.orphanagehub.gui;

import com.orphanagehub.model.User; // <<< ADD THIS IMPORT;
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
import java.awt.event.ActionListener;
import java.util.function.BiConsumer;

public class AdminDashboardPanel extends JPanel() {

 private OrphanageHubApp mainApp;
 private User currentUser; // To store the logged-in user's data
 private JLabel userLabel; // To update the user's name on the display

 // Define Colors(Consider shared constants)
 private static final Color DARKBGSTART = new Color(45, 52, 54);
 private static final Color DARKBGEND = new Color(35, 42, 44);
 private static final Color TITLECOLOR_DARK = new Color(223, 230, 233);
 private static final Color TEXTCOLOR_DARK = new Color(200, 200, 200);
 private static final Color BORDERCOLOR_DARK = new Color(80, 80, 80);
 private static final Color INPUTBG_DARK = new Color(60, 60, 60);
 private static final Color INPUTFG_DARK = new Color(220, 220, 220);
 private static final Color INPUTBORDER_DARK = new Color(90, 90, 90);
 private static final Color BUTTONBG_DARK = new Color(99, 110, 114);
 private static final Color BUTTONFG_DARK = Color.WHITE;
 private static final Color BUTTONHOVER_BG_DARK = new Color(120, 130, 134);
 private static final Color TAB_BGSELECTED = new Color(70, 80, 82);
 private static final Color TAB_BG_UNSELECTED = new Color(55, 62, 64);
 private static final Color TAB_FG = TITLECOLOR_DARK;
 private static final Color TABLEHEADER_BG = new Color(65, 75, 77);
 private static final Color TABLEHEADER_FG = TITLECOLOR_DARK;
 private static final Color TABLEGRIDCOLOR = BORDERCOLOR_DARK;
 private static final Color TABLECELLBG = new Color(55, 62, 64);
 private static final Color TABLECELLFG = TEXTCOLOR_DARK;
 private static final Color TABLECELLSELECTED_BG = BUTTONBG_DARK;
 private static final Color TABLECELLSELECTED_FG = BUTTONFG_DARK;
 // Action Button Colors
 private static final Color BUTTONAPPROVEBG = new Color(60, 179, 113); // Green;
 private static final Color BUTTONAPPROVEHOVER_BG = new Color(70, 190, 123);
 private static final Color BUTTONREJECTBG = new Color(192, 57, 43); // Red;
 private static final Color BUTTONREJECTHOVER_BG = new Color(231, 76, 60);
 private static final Color BUTTONSUSPEND_BG = BUTTONREJECTBG; // Use same red for suspend;
 private static final Color BUTTONSUSPEND_HOVER_BG = BUTTONREJECTHOVER_BG;

 public AdminDashboardPanel(OrphanageHubApp app) {
 this.mainApp = app;
 setLayout(new BorderLayout(0, 0) );
 initComponents();
 }

 // - - - ADDED METHOD-- -
 / **
 * Sets the currently logged-in admin user for this panel.
 * This method updates the UI with the user's information.
 * @param user The logged-in admin.
 * /
 public void setAdminUser(User user) {
 this.currentUser = user;
 if(user != null) {
 this.userLabel.setText( "Admin User: " + user.getUsername();
 }
 }

 @Override
 protected void paintComponent(Graphics g) {
 super.paintComponent(g);
 Graphics2D g2d = (Graphics2D) g;
 g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUERENDER_QUALITY);
 GradientPaint gp = new GradientPaint(0, 0, DARKBGSTART, 0, getHeight(), DARKBGEND);
 g2d.setPaint(gp);
 g2d.fillRect(0, 0, getWidth(), getHeight();
 }

 private void initComponents() {
 // - - - Header Panel-- -
 JPanel headerPanel = createHeaderPanel();
 add(headerPanel, BorderLayout.NORTH);

 // - - - Tabbed Pane for Content-- -
 JTabbedPane tabbedPane = createTabbedPane();
 add(tabbedPane, BorderLayout.CENTER);
 }

 // - - - Helper Methods-- -
 private JPanel createHeaderPanel() {
 JPanel headerPanel = new JPanel(new BorderLayout(10, 0) );
 headerPanel.setOpaque(false);
 headerPanel.setBorder(new CompoundBorder(
 BorderFactory.createMatteBorder(0, 0, 1, 0, BORDERCOLOR_DARK),;
 new EmptyBorder(10, 20, 10, 20);
 ) );

 // Left side: Role Icon and Title
 JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0) );
 titleGroup.setOpaque(false);
 JLabel iconLabel = new JLabel(" \u2699"); // Gear symbol;
 iconLabel.setFont(new Font(Font.SANSSERIF, Font.BOLD, 22) );
 iconLabel.setForeground(TITLECOLOR_DARK);
 JLabel nameLabel = new JLabel("Administrator Dashboard");
 nameLabel.setFont(new Font(Font.SANSSERIF, Font.BOLD, 20) );
 nameLabel.setForeground(TITLECOLOR_DARK);
 titleGroup.add(iconLabel);
 titleGroup.add(nameLabel);
 headerPanel.add(titleGroup, BorderLayout.WEST);

 // Right side: User info and Logout Button
 JPanel userGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0) );
 userGroup.setOpaque(false);
        
 // Initialize the class field userLabel here
 userLabel = new JLabel("Welcome, Admin");
 userLabel.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 14) );
 userLabel.setForeground(TEXTCOLOR_DARK);

 JButton btnLogout = new JButton("Logout");
 styleActionButton(btnLogout, "Logout and return to welcome screen" );
 btnLogout.setPreferredSize(new Dimension(100, 30) );
 btnLogout.setBackground(BUTTONREJECTBG);
 btnLogout.addMouseListener(new MouseAdapter() {
 @Override public void mouseEntered(MouseEvent e) { btnLogout.setBackground(BUTTONREJECTHOVER_BG); }
 @Override public void mouseExited(MouseEvent e) { btnLogout.setBackground(BUTTONREJECTBG); }
 });
 btnLogout.addActionListener(e -> mainApp.logout();
 userGroup.add(userLabel);
 userGroup.add(btnLogout);
 headerPanel.add(userGroup, BorderLayout.EAST);

 return headerPanel;
 }

 private JTabbedPane createTabbedPane() {
 JTabbedPane tabbedPane = new JTabbedPane();
 tabbedPane.setOpaque(false);
 tabbedPane.setForeground(TAB_FG);
 tabbedPane.setFont(new Font(Font.SANSSERIF, Font.BOLD, 14) );
 tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
 @Override protected void installDefaults() { super.installDefaults(); lightHighlight=TAB_BGSELECTED; shadow=BORDERCOLOR_DARK; darkShadow=DARKBGEND; focus=TAB_BGSELECTED; }
 @Override protected void paintTabBackground(Graphics g, int p, int i, int x, int y, int w, int h, boolean s) { g.setColor(s ? TAB_BGSELECTED : TAB_BG_UNSELECTED); g.fillRoundRect(x, y, w, h+5, 5, 5); }
 @Override protected void paintTabBorder(Graphics g, int p, int i, int x, int y, int w, int h, boolean s) { / * Minimal border * / }
 @Override protected void paintContentBorder(Graphics g, int p, int i) { int w=tabPane.getWidth(); int h=tabPane.getHeight(); Insets ins=tabPane.getInsets(); int th=calculateTabAreaHeight(p, runCount, maxTabHeight); int x=ins.left; int y=ins.top+th- (lightHighlight.getAlpha()>0?1:0); int cw=w-ins.right-ins.left; int ch=h-ins.top-ins.bottom-y; g.setColor(BORDERCOLOR_DARK); g.drawRect(x, y, cw-1, ch-1); }
 });

 // Create and add tabs
 tabbedPane.addTab( "Orphanage Verification", createVerificationTab();
 tabbedPane.addTab( "User Management", createUserManagementTab();
 tabbedPane.addTab( "System Overview", createSystemOverviewTab();

 return tabbedPane;
 }

 // - - - Tab Creation Methods-- -
 private JPanel createVerificationTab() {
 JPanel panel = new JPanel(new BorderLayout(10, 10) );
 panel.setOpaque(false);
 panel.setBorder(new EmptyBorder(15, 15, 15, 15) );

 // Verification Table
 String[ ] columnNames = {"Orphanage Name", "Contact", "Email", "Registered", "Status", "Actions"};
 Object[ ] [ ] data = {
 {"New Hope Center", "Alice Smith", "alice@newhope.org", "2025-05-10", "Pending", "Verify"},
 {"Future Stars", "Bob Jones", "bob@futurestars.net", "2025-05-08", "Pending", "Verify"},
 {"Safe Haven Kids", "Charlie P.", "contact@safehaven.com", "2025-04-20", "Verified", "View"},
 {"Distant Dreams", "Diana Ross", "info@distdreams.org", "2025-05-11", "Pending", "Verify"}
 };

 JTable table = new JTable(data, columnNames) {
 @Override public boolean isCellEditable(int row, int column) { return column == 5; }
 };
 styleTable(table);

 // Action Column Renderer/Editor
 JPanel buttonPanelRenderer = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0) );
 buttonPanelRenderer.setOpaque(false);
 JButton approveBtnRend = new JButton(" \u2713"); // Check mark;
 JButton rejectBtnRend = new JButton(" \u2715"); // X mark;
 JButton detailsBtnRend = new JButton("..."); // Details;
 styleMiniButton(approveBtnRend, BUTTONAPPROVEBG);
 styleMiniButton(rejectBtnRend, BUTTONREJECTBG);
 styleMiniButton(detailsBtnRend, BUTTONBG_DARK);
 buttonPanelRenderer.add(approveBtnRend);
 buttonPanelRenderer.add(rejectBtnRend);
 buttonPanelRenderer.add(detailsBtnRend);

 table.getColumnModel().getColumn(5).setCellRenderer( (tbl, value, isSelected, hasFocus, row, column) -> buttonPanelRenderer);

 table.getColumnModel().getColumn(5).setCellEditor(new ActionPanelEditor(new JCheckBox(), (actionCommand, row) -> {
 String orphanageName = (String) table.getModel().getValueAt(row, 0);
 switch(actionCommand) {
 case "approve":
 JOptionPane.showMessageDialog(this, "Approve: " + orphanageName, "Approve", JOptionPane.INFORMATIONMESSAGE);
 break;
 case "reject":
 if(JOptionPane.showConfirmDialog(this, "Reject " + orphanageName + " ?", "Confirm Reject", JOptionPane.YESNOOPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YESOPTION) {
 JOptionPane.showMessageDialog(this, "Reject: " + orphanageName, "Reject", JOptionPane.INFORMATIONMESSAGE);
 }
 break;
 case "details":
 JOptionPane.showMessageDialog(this, "View Details for: " + orphanageName, "Details", JOptionPane.INFORMATIONMESSAGE);
 break;
 }
 }) );

 table.getColumnModel().getColumn(0).setPreferredWidth(180);
 table.getColumnModel().getColumn(1).setPreferredWidth(120);
 table.getColumnModel().getColumn(2).setPreferredWidth(180);
 table.getColumnModel().getColumn(3).setPreferredWidth(100);
 table.getColumnModel().getColumn(4).setPreferredWidth(80);
 table.getColumnModel().getColumn(5).setPreferredWidth(120);
 table.setRowHeight(approveBtnRend.getPreferredSize().height + 4);

 JScrollPane scrollPane = new JScrollPane(table);
 styleScrollPane(scrollPane);
 panel.add(scrollPane, BorderLayout.CENTER);

 return panel;
 }

 private JPanel createUserManagementTab() {
 JPanel panel = new JPanel(new BorderLayout(10, 10) );
 panel.setOpaque(false);
 panel.setBorder(new EmptyBorder(15, 15, 15, 15) );

 // User Search/Filter
 JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5) );
 searchPanel.setOpaque(false);

 JLabel lblSearchUser = new JLabel("Search User:");
 JTextField txtUserSearch = new JTextField(20);
 JLabel lblUserRole = new JLabel("Role:");
 JComboBox<String> cmbUserRole = new JComboBox<>(new String[ ]{"Any Role", "Admin", "OrphanageStaff", "Donor", "Volunteer"});
 JButton btnUserSearch = new JButton("Search");

 styleFormLabel(lblSearchUser);
 styleTextField(txtUserSearch);
 styleFormLabel(lblUserRole);
 styleComboBox(cmbUserRole);
 styleActionButton(btnUserSearch, "Find users" );

 searchPanel.add(lblSearchUser);
 searchPanel.add(txtUserSearch);
 searchPanel.add(lblUserRole);
 searchPanel.add(cmbUserRole);
 searchPanel.add(btnUserSearch);

 panel.add(searchPanel, BorderLayout.NORTH);

 // User Table
 String[ ] columnNames = {"Username", "Email", "Role", "Status", "Registered", "Actions"};
 Object[ ] [ ] data = {
 {"staff_user", "staff@example.com", "OrphanageStaff", "Active", "2025-01-15", "Manage"},
 {"donor_user", "donor@mail.net", "Donor", "Active", "2025-02-10", "Manage"},
 {"volunteerA", "vol@provider.org", "Volunteer", "Active", "2025-03-01", "Manage"},
 {"old_staff", "old@example.com", "OrphanageStaff", "Suspended", "2024-11-20", "Manage"},
 {"admin_user", "admin@orphanagehub.com", "Admin", "Active", "2024-10-01", "Manage"}
 };

 JTable table = new JTable(data, columnNames) {
 @Override public boolean isCellEditable(int row, int column) { return column == 5; }
 };
 styleTable(table);

 // Action Column Renderer/Editor(Similar to verification tab)
 JPanel userActionPanelRenderer = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0) );
 userActionPanelRenderer.setOpaque(false);
 JButton activateBtnRend = new JButton(" \u2713");
 JButton suspendBtnRend = new JButton(" \u2715");
 JButton viewBtnRend = new JButton("...");
 styleMiniButton(activateBtnRend, BUTTONAPPROVEBG);
 styleMiniButton(suspendBtnRend, BUTTONSUSPEND_BG);
 styleMiniButton(viewBtnRend, BUTTONBG_DARK);
 userActionPanelRenderer.add(activateBtnRend);
 userActionPanelRenderer.add(suspendBtnRend);
 userActionPanelRenderer.add(viewBtnRend);
        
 table.getColumnModel().getColumn(5).setCellRenderer( (tbl, value, isSelected, hasFocus, row, column) -> userActionPanelRenderer);
 // A CellEditor would be added here similarly to the verification tab to handle clicks.

 JScrollPane scrollPane = new JScrollPane(table);
 styleScrollPane(scrollPane);
 panel.add(scrollPane, BorderLayout.CENTER);
 return panel;
 }

 private JPanel createSystemOverviewTab() {
 JPanel panel = new JPanel();
 panel.setOpaque(false);
 panel.setLayout(new FlowLayout(FlowLayout.LEFT) );
 panel.setBorder(new EmptyBorder(15, 15, 15, 15) );
 JLabel comingSoonLabel = new JLabel("System Overview and Analytics - Coming Soon!");
 comingSoonLabel.setFont(new Font(Font.SANSSERIF, Font.BOLD, 18) );
 comingSoonLabel.setForeground(TEXTCOLOR_DARK);
 panel.add(comingSoonLabel);
 return panel;
 }

 // - - - Styling Helpers-- -
 private void styleFormLabel(JLabel label) { label.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 13) ); label.setForeground(TEXTCOLOR_DARK); }
 private void styleTextField(JTextField field) { field.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 13) ); field.setForeground(INPUTFG_DARK); field.setBackground(INPUTBG_DARK); Border p=new EmptyBorder(4,6,4,6); field.setBorder(new CompoundBorder(BorderFactory.createLineBorder(INPUTBORDER_DARK,1),p) ); field.setCaretColor(Color.LIGHTGRAY); }
 private void styleComboBox(JComboBox<?> comboBox) { comboBox.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 13) ); comboBox.setForeground(INPUTFG_DARK); comboBox.setBackground(INPUTBG_DARK); comboBox.setBorder(BorderFactory.createLineBorder(INPUTBORDER_DARK,1) ); for(Component c:comboBox.getComponents(){if(c instanceof JButton){( (JButton)c).setBackground(BUTTONBG_DARK);( (JButton)c).setBorder(BorderFactory.createEmptyBorder();break;}} Object p=comboBox.getUI().getAccessibleChild(comboBox,0); if(p instanceof JPopupMenu){JPopupMenu pm= (JPopupMenu)p;pm.setBorder(BorderFactory.createLineBorder(BORDERCOLOR_DARK) ); for(Component comp:pm.getComponents(){if(comp instanceof JScrollPane){JScrollPane sp= (JScrollPane)comp;sp.getViewport().setBackground(INPUTBG_DARK);applyScrollbarUI(sp.getVerticalScrollBar(); Component l=sp.getViewport().getView(); if(l instanceof JList){( (JList<?>)l).setBackground(INPUTBG_DARK);( (JList<?>)l).setForeground(INPUTFG_DARK);( (JList<?>)l).setSelectionBackground(BUTTONBG_DARK);( (JList<?>)l).setSelectionForeground(BUTTONFG_DARK);}}}}}
 private void styleTable(JTable table) { table.setBackground(TABLECELLBG); table.setForeground(TABLECELLFG); table.setGridColor(TABLEGRIDCOLOR); table.setRowHeight(28); table.setFont(new Font(Font.SANSSERIF,Font.PLAIN,13) ); table.setFillsViewportHeight(true); table.setSelectionBackground(TABLECELLSELECTED_BG); table.setSelectionForeground(TABLECELLSELECTED_FG); table.setShowGrid(true); table.setIntercellSpacing(new Dimension(0,1) ); JTableHeader h=table.getTableHeader(); h.setBackground(TABLEHEADER_BG); h.setForeground(TABLEHEADER_FG); h.setFont(new Font(Font.SANSSERIF,Font.BOLD,14) ); h.setBorder(BorderFactory.createLineBorder(BORDERCOLOR_DARK) ); h.setReorderingAllowed(true); h.setResizingAllowed(true); DefaultTableCellRenderer r=new DefaultTableCellRenderer(); r.setHorizontalAlignment(SwingConstants.LEFT); r.setVerticalAlignment(SwingConstants.CENTER); r.setBorder(new EmptyBorder(2,5,2,5) ); for(int i=0;i<table.getColumnCount() -1;i++){table.getColumnModel().getColumn(i).setCellRenderer(r);} }
 private void styleScrollPane(JScrollPane scrollPane) { scrollPane.setOpaque(false); scrollPane.getViewport().setOpaque(false); scrollPane.setBorder(BorderFactory.createLineBorder(BORDERCOLOR_DARK) ); applyScrollbarUI(scrollPane.getVerticalScrollBar(); applyScrollbarUI(scrollPane.getHorizontalScrollBar(); }
 private void applyScrollbarUI(JScrollBar scrollBar) { scrollBar.setUI(new BasicScrollBarUI() { @Override protected void configureScrollBarColors(){this.thumbColor=BUTTONBG_DARK; this.trackColor=DARKBGEND;} @Override protected JButton createDecreaseButton(int o){return createZeroButton();} @Override protected JButton createIncreaseButton(int o){return createZeroButton();} private JButton createZeroButton(){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0) );b.setMaximumSize(new Dimension(0,0) );b.setMinimumSize(new Dimension(0,0) );return b;} @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r){g.setColor(thumbColor);g.fillRect(r.x,r.y,r.width,r.height);} @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r){g.setColor(trackColor);g.fillRect(r.x,r.y,r.width,r.height);} }); scrollBar.setUnitIncrement(16); }
 private void styleActionButton(JButton btn, String tooltip) { btn.setFont(new Font(Font.SANSSERIF,Font.BOLD,12) ); btn.setToolTipText(tooltip); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HANDCURSOR) ); btn.setForeground(BUTTONFG_DARK); btn.setFocusPainted(false); btn.setBackground(BUTTONBG_DARK); Border p=new EmptyBorder(6,12,6,12); btn.setBorder(new CompoundBorder(BorderFactory.createLineBorder(BUTTONBG_DARK.darker(),p) ); btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){if(btn.getBackground().equals(BUTTONBG_DARK) ){btn.setBackground(BUTTONHOVER_BG_DARK);}}@Override public void mouseExited(MouseEvent e){if(btn.getBackground().equals(BUTTONHOVER_BG_DARK) ){btn.setBackground(BUTTONBG_DARK);}}}); }
 private void styleMiniButton(JButton btn, Color color) { btn.setFont(new Font(Font.SANSSERIF, Font.BOLD, 14) ); btn.setMargin(new Insets(0,0,0,0) ); btn.setBackground(color); btn.setForeground(BUTTONFG_DARK); btn.setFocusPainted(false); }

 // - - - Inner classes for Table Button-- -
 static class ActionPanelEditor extends DefaultCellEditor() {
 private final JPanel panel;
 private final BiConsumer<String, Integer> actionConsumer;
 private int editingRow;

 public ActionPanelEditor(JCheckBox checkBox, BiConsumer<String, Integer> actionConsumer) {
 super(checkBox);
 this.actionConsumer = actionConsumer;
 panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0) );
 panel.setOpaque(false);
            
 JButton approveBtn = new JButton(" \u2713");
 JButton rejectBtn = new JButton(" \u2715");
 JButton detailsBtn = new JButton("...");

 styleMiniButton(approveBtn, BUTTONAPPROVEBG);
 styleMiniButton(rejectBtn, BUTTONREJECTBG);
 styleMiniButton(detailsBtn, BUTTONBG_DARK);

 approveBtn.setActionCommand("approve");
 rejectBtn.setActionCommand("reject");
 detailsBtn.setActionCommand("details");

 ActionListener al = e -> {
 fireEditingStopped();
 actionConsumer.accept(e.getActionCommand(), editingRow);
 };
            
 approveBtn.addActionListener(al);
 rejectBtn.addActionListener(al);
 detailsBtn.addActionListener(al);

 panel.add(approveBtn);
 panel.add(rejectBtn);
 panel.add(detailsBtn);
 }

 @Override
 public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
 this.editingRow = row;
 return panel;
 }

 private static void styleMiniButton(JButton btn, Color color) {
 btn.setFont(new Font(Font.SANSSERIF, Font.BOLD, 14) );
 btn.setMargin(new Insets(0,0,0,0) );
 btn.setBackground(color);
 btn.setForeground(BUTTONFG_DARK);
 btn.setFocusPainted(false);
 btn.setCursor(new Cursor(Cursor.HANDCURSOR) );
 }
 }
}