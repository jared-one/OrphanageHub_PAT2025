package com.orphanagehub.gui;

import com.orphanagehub.model.User;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VolunteerDashboardPanel extends JPanel() {

 private OrphanageHubApp mainApp;
 private User currentUser; // To store the logged-in user's data
 private JLabel userLabel; // To update the user's name on the display

 // - - - Colors(Same as AdminDashboardPanel) - - -
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
 private static final Color TABLEHEADER_BG = new Color(65, 75, 77);
 private static final Color TABLEHEADER_FG = TITLECOLOR_DARK;
 private static final Color TABLEGRIDCOLOR = BORDERCOLOR_DARK;
 private static final Color TABLECELLBG = new Color(55, 62, 64);
 private static final Color TABLECELLFG = TEXTCOLOR_DARK;
 private static final Color TABLECELLSELECTED_BG = BUTTONBG_DARK;
 private static final Color TABLECELLSELECTED_FG = BUTTONFG_DARK;
 private static final Color BUTTONAPPLY_BG = new Color(87, 190, 106);
 private static final Color BUTTONAPPLY_HOVER_BG = new Color(97, 200, 116);

 public VolunteerDashboardPanel(OrphanageHubApp app) {
 this.mainApp = app;
 setLayout(new BorderLayout(0, 0) );
 initComponents();
 }

 / **
 * Sets the currently logged-in volunteer user for this panel.
 * This method updates the UI with the user's information.
 * @param user The logged-in volunteer.
 * /
 public void setVolunteerUser(User user) {
 this.currentUser = user;
 if(user != null) {
 this.userLabel.setText( "User: " + user.getUsername();
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

 // - - - Main Content Area(Search + Table + Status) - - -
 JPanel contentPanel = new JPanel(new BorderLayout(10, 15) );
 contentPanel.setOpaque(false);
 contentPanel.setBorder(new EmptyBorder(15, 20, 20, 20) );

 // - - - Search/Filter Panel-- -
 JPanel searchFilterPanel = createSearchFilterPanel();
 contentPanel.add(searchFilterPanel, BorderLayout.NORTH);

 // - - - Opportunities Table-- -
 JTable opportunitiesTable = createOpportunitiesTable();
 JScrollPane scrollPane = new JScrollPane(opportunitiesTable);
 styleScrollPane(scrollPane);
 contentPanel.add(scrollPane, BorderLayout.CENTER);

 // - - - Application Status Panel(Placeholder) - - -
 JPanel statusPanel = createStatusPanel();
 contentPanel.add(statusPanel, BorderLayout.SOUTH);

 add(contentPanel, BorderLayout.CENTER);
 }

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
 JLabel iconLabel = new JLabel(" \u2605"); // Star symbol;
 iconLabel.setFont(new Font(Font.SANSSERIF, Font.BOLD, 22) );
 iconLabel.setForeground(new Color(255, 215, 0) ); // Gold color;
 JLabel nameLabel = new JLabel("Volunteer Dashboard");
 nameLabel.setFont(new Font(Font.SANSSERIF, Font.BOLD, 20) );
 nameLabel.setForeground(TITLECOLOR_DARK);
 titleGroup.add(iconLabel);
 titleGroup.add(nameLabel);
 headerPanel.add(titleGroup, BorderLayout.WEST);

 // Right side: User info and Logout Button
 JPanel userGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0) );
 userGroup.setOpaque(false);

 userLabel = new JLabel("Welcome, Volunteer");
 userLabel.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 14) );
 userLabel.setForeground(TEXTCOLOR_DARK);

 JButton btnLogout = new JButton("Logout");
 styleActionButton(btnLogout, "Logout and return to welcome screen" );
 btnLogout.setPreferredSize(new Dimension(100, 30) );
 btnLogout.setBackground(new Color(192, 57, 43) ); // Reddish logout;
 btnLogout.addMouseListener(new MouseAdapter() {
 @Override public void mouseEntered(MouseEvent e) { btnLogout.setBackground(new Color(231, 76, 60) ); }
 @Override public void mouseExited(MouseEvent e) { btnLogout.setBackground(new Color(192, 57, 43) ); }
 });
 btnLogout.addActionListener(e -> mainApp.logout();

 userGroup.add(userLabel);
 userGroup.add(btnLogout);
 headerPanel.add(userGroup, BorderLayout.EAST);

 return headerPanel;
 }

 private JPanel createSearchFilterPanel() {
 JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5) );
 panel.setOpaque(false);

 JLabel lblFilterLocation = new JLabel("Location:");
 styleFormLabel(lblFilterLocation);
 String[ ] locations = {"Any Location", "City A", "City B", "Region C"}; // Placeholders;
 JComboBox<String> cmbLocation = new JComboBox<>(locations);
 styleComboBox(cmbLocation);

 JLabel lblFilterSkills = new JLabel("Skills:");
 styleFormLabel(lblFilterSkills);
 JTextField txtSkills = new JTextField(15); // Text field for skills keywords;
 styleTextField(txtSkills);

 JLabel lblFilterTime = new JLabel("Commitment:");
 styleFormLabel(lblFilterTime);
 String[ ] times = {"Any Time", "Weekends", "Weekdays", "Flexible", "Event-Based"}; // Placeholders;
 JComboBox<String> cmbTime = new JComboBox<>(times);
 styleComboBox(cmbTime);

 JButton btnSearch = new JButton("Find Opportunities");
 styleActionButton(btnSearch, "Search for volunteer roles matching criteria" );
 btnSearch.addActionListener(e ->
 JOptionPane.showMessageDialog(this, "Search logic not implemented.", "Search", JOptionPane.INFORMATIONMESSAGE);
 );

 panel.add(lblFilterLocation);
 panel.add(cmbLocation);
 panel.add(Box.createHorizontalStrut(10) );
 panel.add(lblFilterSkills);
 panel.add(txtSkills);
 panel.add(Box.createHorizontalStrut(10) );
 panel.add(lblFilterTime);
 panel.add(cmbTime);
 panel.add(Box.createHorizontalStrut(15) );
 panel.add(btnSearch);

 return panel;
 }

 private JTable createOpportunitiesTable() {
 String[ ] columnNames = {"Orphanage", "Opportunity", "Location", "Skills Needed", "Time Commitment", "Action"};
 Object[ ] [ ] data = {
 {"Hope Children's Home", "Weekend Tutor", "City A", "Teaching, Patience", "Weekends", "Apply"},
 {"Bright Future Orphanage", "Event Helper", "City B", "Organizing, Energetic", "Event-Based", "Apply"},
 {"Little Angels Shelter", "After-School Care", "City A", "Childcare, First Aid", "Weekdays", "Applied"},
 {"Sunshine House", "Gardening Assistant", "Region C", "Gardening", "Flexible", "Apply"},
 {"Hope Children's Home", "Reading Buddy", "City A", "Reading, Communication", "Weekdays", "Apply"}
 };

 JTable table = new JTable(data, columnNames) {
 @Override
 public boolean isCellEditable(int row, int column) {
 // Allow interaction only on the last column if the text is "Apply"
 return column == 5 && "Apply".equals(getValueAt(row, column) );
 }
 };

 styleTable(table);

 // Add button renderer/editor for the "Action" column
 table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer(BUTTONAPPLY_BG) );
 table.getColumnModel().getColumn(5).setCellEditor(;
 new ButtonEditor(new JCheckBox(), BUTTONAPPLY_BG, () -> {
 int selectedRow = table.convertRowIndexToModel(table.getEditingRow();
 String oppName = (String) table.getModel().getValueAt(selectedRow, 1);
 String orphName = (String) table.getModel().getValueAt(selectedRow, 0);
 JOptionPane.showMessageDialog(
 this,
 "Apply for: " + oppName + " at " + orphName + " \n(Functionality not implemented) ",;
 "Apply",
 JOptionPane.INFORMATIONMESSAGE
 );
 // Example to update status using DefaultTableModel:
 // ( (DefaultTableModel) table.getModel().setValueAt( "Applied", selectedRow, 5);
 })
 );

 // Adjust column widths
 table.getColumnModel().getColumn(0).setPreferredWidth(150); // Orphanage;
 table.getColumnModel().getColumn(1).setPreferredWidth(150); // Opportunity;
 table.getColumnModel().getColumn(2).setPreferredWidth(100); // Location;
 table.getColumnModel().getColumn(3).setPreferredWidth(180); // Skills;
 table.getColumnModel().getColumn(4).setPreferredWidth(120); // Time;
 table.getColumnModel().getColumn(5).setPreferredWidth(90); // Action;

 return table;
 }

 private JPanel createStatusPanel() {
 JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT) );
 panel.setOpaque(false);
 panel.setBorder(new CompoundBorder(
 BorderFactory.createMatteBorder(1, 0, 0, 0, BORDERCOLOR_DARK), // Top border separator;
 new EmptyBorder(10, 5, 5, 5) // Padding;
 ) );

 JLabel lblStatus = new JLabel("Your Applications: 1 Pending(Little Angels Shelter)"); // Placeholder text;
 lblStatus.setFont(new Font(Font.SANSSERIF, Font.ITALIC, 13) );
 lblStatus.setForeground(TEXTCOLOR_DARK);
 panel.add(lblStatus);

 return panel;
 }

 // - - - Styling Helpers-- -
 private void styleFormLabel(JLabel label) {
 label.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 13) );
 label.setForeground(TEXTCOLOR_DARK);
 }

 private void styleTextField(JTextField field) {
 field.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 13) );
 field.setForeground(INPUTFG_DARK);
 field.setBackground(INPUTBG_DARK);
 Border padding = new EmptyBorder(4, 6, 4, 6);
 field.setBorder(new CompoundBorder(BorderFactory.createLineBorder(INPUTBORDER_DARK, 1), padding) );
 field.setCaretColor(Color.LIGHTGRAY);
 }

 private void styleComboBox(JComboBox<?> comboBox) {
 comboBox.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 13) );
 comboBox.setForeground(INPUTFG_DARK);
 comboBox.setBackground(INPUTBG_DARK);
 comboBox.setBorder(BorderFactory.createLineBorder(INPUTBORDER_DARK, 1) );

 for(Component c : comboBox.getComponents() ) {
 if(c instanceof JButton) {
 ( (JButton) c).setBackground(BUTTONBG_DARK);
 ( (JButton) c).setBorder(BorderFactory.createEmptyBorder();
 break;
 }
 }

 Object p = comboBox.getUI().getAccessibleChild(comboBox, 0);
 if(p instanceof JPopupMenu) {
 JPopupMenu pm = (JPopupMenu) p;
 pm.setBorder(BorderFactory.createLineBorder(BORDERCOLOR_DARK) );
 for(Component comp : pm.getComponents() ) {
 if(comp instanceof JScrollPane) {
 JScrollPane sp = (JScrollPane) comp;
 sp.getViewport().setBackground(INPUTBG_DARK);
 applyScrollbarUI(sp.getVerticalScrollBar();
 Component l = sp.getViewport().getView();
 if(l instanceof JList) {
 @SuppressWarnings("rawtypes")
 JList list = (JList) l;
 list.setBackground(INPUTBG_DARK);
 list.setForeground(INPUTFG_DARK);
 list.setSelectionBackground(BUTTONBG_DARK);
 list.setSelectionForeground(BUTTONFG_DARK);
 }
 }
 }
 }
 }

 private void styleTable(JTable table) {
 table.setBackground(TABLECELLBG);
 table.setForeground(TABLECELLFG);
 table.setGridColor(TABLEGRIDCOLOR);
 table.setRowHeight(28);
 table.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 13) );
 table.setFillsViewportHeight(true);
 table.setSelectionBackground(TABLECELLSELECTED_BG);
 table.setSelectionForeground(TABLECELLSELECTED_FG);
 table.setShowGrid(true);
 table.setIntercellSpacing(new Dimension(0, 1) );

 JTableHeader h = table.getTableHeader();
 h.setBackground(TABLEHEADER_BG);
 h.setForeground(TABLEHEADER_FG);
 h.setFont(new Font(Font.SANSSERIF, Font.BOLD, 14) );
 h.setBorder(BorderFactory.createLineBorder(BORDERCOLOR_DARK) );
 h.setReorderingAllowed(true);
 h.setResizingAllowed(true);

 DefaultTableCellRenderer r = new DefaultTableCellRenderer();
 r.setHorizontalAlignment(SwingConstants.LEFT);
 r.setVerticalAlignment(SwingConstants.CENTER);
 r.setBorder(new EmptyBorder(2, 5, 2, 5) );
 for(int i = 0; i < table.getColumnCount() - 1; i++) {
 table.getColumnModel().getColumn(i).setCellRenderer(r);
 }
 }

 private void styleScrollPane(JScrollPane scrollPane) {
 scrollPane.setOpaque(false);
 scrollPane.getViewport().setOpaque(false);
 scrollPane.setBorder(BorderFactory.createLineBorder(BORDERCOLOR_DARK) );
 applyScrollbarUI(scrollPane.getVerticalScrollBar();
 applyScrollbarUI(scrollPane.getHorizontalScrollBar();
 }

 private void applyScrollbarUI(JScrollBar scrollBar) {
 scrollBar.setUI(new BasicScrollBarUI() {
 @Override
 protected void configureScrollBarColors() {
 this.thumbColor = BUTTONBG_DARK;
 this.trackColor = DARKBGEND;
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
 JButton b = new JButton();
 b.setPreferredSize(new Dimension(0, 0) );
 b.setMaximumSize(new Dimension(0, 0) );
 b.setMinimumSize(new Dimension(0, 0) );
 return b;
 }

 @Override
 protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
 g.setColor(thumbColor);
 g.fillRect(r.x, r.y, r.width, r.height);
 }

 @Override
 protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
 g.setColor(trackColor);
 g.fillRect(r.x, r.y, r.width, r.height);
 }
 });
 scrollBar.setUnitIncrement(16);
 }

 private void styleActionButton(JButton btn, String tooltip) {
 btn.setFont(new Font(Font.SANSSERIF, Font.BOLD, 12) );
 btn.setToolTipText(tooltip);
 btn.setCursor(Cursor.getPredefinedCursor(Cursor.HANDCURSOR) );
 btn.setForeground(BUTTONFG_DARK);
 btn.setFocusPainted(false);
 btn.setBackground(BUTTONBG_DARK);
 Border padding = new EmptyBorder(6, 12, 6, 12);
 btn.setBorder(new CompoundBorder(BorderFactory.createLineBorder(BUTTONBG_DARK.darker(), padding) );
 btn.addMouseListener(new MouseAdapter() {
 @Override public void mouseEntered(MouseEvent e) {
 if(btn.getBackground().equals(BUTTONBG_DARK) ) {
 btn.setBackground(BUTTONHOVER_BG_DARK);
 }
 }
 @Override public void mouseExited(MouseEvent e) {
 if(btn.getBackground().equals(BUTTONHOVER_BG_DARK) ) {
 btn.setBackground(BUTTONBG_DARK);
 }
 }
 });
 }

 // - - - Inner classes for Table Button-- -
 static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
 private final Color defaultBg;
 public ButtonRenderer(Color background) {
 setOpaque(true);
 this.defaultBg = background;
 setForeground(BUTTONFG_DARK);
 setBackground(defaultBg);
 setBorder(new EmptyBorder(2, 5, 2, 5) );
 setFont(new Font(Font.SANSSERIF, Font.BOLD, 11) );
 }
 @Override
 public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
 setText(v == null ? " " : v.toString();
 setBackground(s ? defaultBg.brighter() : defaultBg);
 return this;
 }
 }

 static class ButtonEditor extends DefaultCellEditor() {
 protected JButton button;
 private String label;
 private boolean isPushed;
 private final Runnable action;
 private final Color bgColor;

 public ButtonEditor(JCheckBox c, Color bg, Runnable act) {
 super(c);
 this.action = act;
 this.bgColor = bg;
 button = new JButton();
 button.setOpaque(true);
 button.setForeground(BUTTONFG_DARK);
 button.setBackground(bgColor);
 button.setBorder(new EmptyBorder(2, 5, 2, 5) );
 button.setFont(new Font(Font.SANSSERIF, Font.BOLD, 11) );
 button.addActionListener(e -> fireEditingStopped();
 }

 @Override
 public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
 label = (v == null) ? " " : v.toString();
 button.setText(label);
 isPushed = true;
 return button;
 }

 @Override
 public Object getCellEditorValue() {
 if(isPushed && action != null) {
 action.run();
 }
 isPushed = false;
 return label;
 }

 @Override
 public boolean stopCellEditing() {
 isPushed = false;
 return super.stopCellEditing();
 }

 @Override
 protected void fireEditingStopped() {
 super.fireEditingStopped();
 }
 }
}