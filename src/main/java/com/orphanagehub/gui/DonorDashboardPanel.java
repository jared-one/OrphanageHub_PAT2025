package com.orphanagehub.gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

import com.orphanagehub.model.*;
import com.orphanagehub.service.*;
import com.orphanagehub.util.Logger;

/ **
 * Dashboard panel for donor users.
 * Allows donors to view orphanages and their resource needs.
 *  * PAT Rubric Coverage:
 * - 3.2: Separation of UI from business logic
 * - 3.3: Typed methods with parameters
 * - 3.5: Database querying operations
 * - 3.7: Donor functionality implementation
 * - 3.8: User-friendly donor interface
 * /
public class DonorDashboardPanel extends JPanel() {

 private OrphanageHubApp mainApp;
 private User donorUser;
 private JLabel userLabel;
 private JTable resultsTable;
 private DefaultTableModel tableModel;
 private JTextField txtSearch;
 private JComboBox<String> cmbLocation;
 private JComboBox<String> cmbCategory;
    
 // Services
 private DonorService donorService;
    
 // Color constants(same as other panels)
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
 private static final Color BUTTONSEARCH_BG = new Color(72, 149, 239);
 private static final Color BUTTONSEARCH_HOVER_BG = new Color(92, 169, 249);
 private static final Color BUTTONDONATEBG = new Color(60, 179, 113);
 private static final Color BUTTONDONATEHOVER_BG = new Color(70, 190, 123);

 public DonorDashboardPanel(OrphanageHubApp app) {
 this.mainApp = app;
 this.donorService = new DonorService();
 setLayout(new BorderLayout(0, 0) );
 initComponents();
 Logger.debug("DonorDashboard initialized");
 }

 / **
 * Sets the donor user and refreshes the display.
 * @param user The authenticated donor user
 * /
 public void setDonorUser(User user) {
 this.donorUser = user;
 Logger.info( "Donor user set: " + user.getUsername();
 refreshUI();
 loadOrphanageData();
 }

 private void refreshUI() {
 if(donorUser != null && userLabel != null) {
 userLabel.setText( "User: " + donorUser.getUsername();
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
 // Header Panel
 JPanel headerPanel = createHeaderPanel();
 add(headerPanel, BorderLayout.NORTH);

 // Main Content Area
 JPanel contentPanel = new JPanel(new BorderLayout(10, 15) );
 contentPanel.setOpaque(false);
 contentPanel.setBorder(new EmptyBorder(15, 20, 20, 20) );

 // Search/Filter Panel
 JPanel searchFilterPanel = createSearchFilterPanel();
 contentPanel.add(searchFilterPanel, BorderLayout.NORTH);

 // Results Table
 createResultsTable();
 JScrollPane scrollPane = new JScrollPane(resultsTable);
 styleScrollPane(scrollPane);
 contentPanel.add(scrollPane, BorderLayout.CENTER);

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
 JLabel iconLabel = new JLabel(" \u2764"); // Heart symbol;
 iconLabel.setFont(new Font( "Segoe UI Symbol", Font.BOLD, 22) );
 iconLabel.setForeground(new Color(255, 105, 180) );
 JLabel nameLabel = new JLabel("Donor Dashboard");
 nameLabel.setFont(new Font(Font.SANSSERIF, Font.BOLD, 20) );
 nameLabel.setForeground(TITLECOLOR_DARK);
 titleGroup.add(iconLabel);
 titleGroup.add(nameLabel);
 headerPanel.add(titleGroup, BorderLayout.WEST);

 JPanel userGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0) );
 userGroup.setOpaque(false);
        
 userLabel = new JLabel("User: Loading...");
 userLabel.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 14) );
 userLabel.setForeground(TEXTCOLOR_DARK);
        
 JButton btnLogout = new JButton("Logout");
 styleActionButton(btnLogout, "Logout and return to welcome screen" );
 btnLogout.setPreferredSize(new Dimension(100, 30) );
 btnLogout.setBackground(new Color(192, 57, 43) );
 btnLogout.addMouseListener(new MouseAdapter() {
 @Override public void mouseEntered(MouseEvent e) {  btnLogout.setBackground(new Color(231, 76, 60) );  }
 @Override public void mouseExited(MouseEvent e) {  btnLogout.setBackground(new Color(192, 57, 43) );  }
 });
 btnLogout.addActionListener(e -> ) {
 int confirm = JOptionPane.showConfirmDialog(this,;
 "Are you sure you want to logout?",
 "Confirm Logout",
 JOptionPane.YESNOOPTION);
 if(confirm == JOptionPane.YESOPTION) {
 mainApp.logout();
 }
 });
        
 userGroup.add(userLabel);
 userGroup.add(btnLogout);
 headerPanel.add(userGroup, BorderLayout.EAST);

 return headerPanel;
 }

 private JPanel createSearchFilterPanel() {
 JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5) );
 panel.setOpaque(false);

 JLabel lblSearch = new JLabel("Search:");
 styleFormLabel(lblSearch);
 txtSearch = new JTextField(20);
 styleTextField(txtSearch);

 JLabel lblFilterLocation = new JLabel("Location:");
 styleFormLabel(lblFilterLocation);
 cmbLocation = new JComboBox<>(new String[ ]{"Any Location"});
 styleComboBox(cmbLocation);

 JLabel lblFilterCategory = new JLabel("Need Category:");
 styleFormLabel(lblFilterCategory);
 cmbCategory = new JComboBox<>(new String[ ]{
 "Any Category", "Food", "Clothing", "Education", "Medical", "Funding", "Other"
 });
 styleComboBox(cmbCategory);

 JButton btnSearch = new JButton("Apply Filters");
 styleActionButton(btnSearch, "Find orphanages or requests matching criteria" );
 btnSearch.setBackground(BUTTONSEARCH_BG);
 btnSearch.addMouseListener(new MouseAdapter() {
 @Override public void mouseEntered(MouseEvent e) {  btnSearch.setBackground(BUTTONSEARCH_HOVER_BG);  }
 @Override public void mouseExited(MouseEvent e) {  btnSearch.setBackground(BUTTONSEARCH_BG);  }
 });
 btnSearch.addActionListener(e -> performSearch();

 panel.add(lblSearch);
 panel.add(txtSearch);
 panel.add(Box.createHorizontalStrut(10) );
 panel.add(lblFilterLocation);
 panel.add(cmbLocation);
 panel.add(Box.createHorizontalStrut(10) );
 panel.add(lblFilterCategory);
 panel.add(cmbCategory);
 panel.add(Box.createHorizontalStrut(15) );
 panel.add(btnSearch);

 return panel;
 }

 private void createResultsTable() {
 String[ ] columnNames = {"Orphanage Name", "Location", "Contact", "Current Needs", "Actions"};
 tableModel = new DefaultTableModel(columnNames, 0) {
 @Override
 public boolean isCellEditable(int row, int column) {
 return column == 4; // Only Actions column is editable;
 }
 };
        
 resultsTable = new JTable(tableModel);
 styleTable(resultsTable);
        
 // Add button renderer/editor for Actions column
 resultsTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer();
 resultsTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox() );
        
 // Adjust column widths
 resultsTable.getColumnModel().getColumn(0).setPreferredWidth(200);
 resultsTable.getColumnModel().getColumn(1).setPreferredWidth(120);
 resultsTable.getColumnModel().getColumn(2).setPreferredWidth(150);
 resultsTable.getColumnModel().getColumn(3).setPreferredWidth(250);
 resultsTable.getColumnModel().getColumn(4).setPreferredWidth(120);
 }

 / **
 * Loads orphanage data from the database.
 * PAT 3.5: Database query operation
 * /
 private void loadOrphanageData() {
 try {
 List<OrphanageInfo> orphanages = donorService.getVerifiedOrphanages();
 updateTable(orphanages);
            
 // Update location combo box
 updateLocationFilter(orphanages);
            
 } catch(ServiceException e) {
 Logger.error( "Failed to load orphanage data: " + e.getMessage();
 JOptionPane.showMessageDialog(this,
 "Error loading orphanage data:\n" + e.getMessage(),;
 "Database Error",
 JOptionPane.ERROR_MESSAGE);
 }
 }

 private void updateTable(List<OrphanageInfo> orphanages) {
 tableModel.setRowCount(0);
        
 for(OrphanageInfo info : orphanages) {
 Object[ ] row = {
 info.getName(),;
 info.getAddress(),;
 info.getContactPerson(),;
 info.getCurrentNeeds(),;
 "View Details"
 };
 tableModel.addRow(row);
 }
 }

 private void updateLocationFilter(List<OrphanageInfo> orphanages) {
 cmbLocation.removeAllItems();
 cmbLocation.addItem("Any Location");
        
 // Extract unique locations
 List<String> locations = new ArrayList<>();
 for(OrphanageInfo info : orphanages) {
 String location = extractCity(info.getAddress();
 if( !locations.contains(location) ) {
 locations.add(location);
 }
 }
        
 for(String location : locations) {
 cmbLocation.addItem(location);
 }
 }

 private String extractCity(String address) {
 // Simple extraction - assumes city is first part of address
 if(address != null && address.contains(",") ) {
 return address.split(",") [0].trim();
 }
 return address != null ? address : "Unknown";
 }

 private void performSearch() {
 String searchText = txtSearch.getText().trim();
 String location = (String) cmbLocation.getSelectedItem();
 String category = (String) cmbCategory.getSelectedItem();
        
 try {
 List<OrphanageInfo> results = donorService.searchOrphanages(;
 searchText, location, category);
 updateTable(results);
            
 Logger.info( "Search performed with " + results.size() + " results" );
            
 } catch(ServiceException e) {
 Logger.error( "Search failed: " + e.getMessage();
 JOptionPane.showMessageDialog(this,
 "Error performing search:\n" + e.getMessage(),;
 "Search Error",
 JOptionPane.ERROR_MESSAGE);
 }
 }

 / **
 * Shows detailed view of an orphanage and its needs.
 * /
 private void showOrphanageDetails(int row) {
 String orphanageName = (String) tableModel.getValueAt(row, 0);
        
 try {
 OrphanageInfo info = donorService.getOrphanageDetails(orphanageName);
 List<ResourceRequest> requests = donorService.getOrphanageRequests(info.getOrphanageId();
            
 // Create detail dialog
 JDialog dialog = new JDialog( (Frame) SwingUtilities.getWindowAncestor(this),;
 "Orphanage Details - " + orphanageName, true);
 dialog.setLayout(new BorderLayout();
            
 // Info panel
 JPanel infoPanel = new JPanel(new GridBagLayout();
 infoPanel.setBorder(new EmptyBorder(20, 20, 10, 20) );
 infoPanel.setBackground(DARKBGSTART);
            
 GridBagConstraints gbc = new GridBagConstraints();
 gbc.fill = GridBagConstraints.HORIZONTAL;
 gbc.insets = new Insets(3, 5, 3, 5);
            
 int row_idx = 0;
 addDetailField(infoPanel, gbc, row_idx++, "Name:", info.getName();
 addDetailField(infoPanel, gbc, row_idx++, "Address:", info.getAddress();
 addDetailField(infoPanel, gbc, row_idx++, "Contact:", info.getContactPerson();
 addDetailField(infoPanel, gbc, row_idx++, "Email:", info.getContactEmail();
 addDetailField(infoPanel, gbc, row_idx++, "Phone:", info.getContactPhone();
            
 dialog.add(infoPanel, BorderLayout.NORTH);
            
 // Requests table
 JPanel requestsPanel = new JPanel(new BorderLayout();
 requestsPanel.setBorder(new EmptyBorder(10, 20, 20, 20) );
 requestsPanel.setBackground(DARKBGSTART);
            
 JLabel lblRequests = new JLabel("Current Resource Needs:");
 lblRequests.setFont(new Font(Font.SANSSERIF, Font.BOLD, 16) );
 lblRequests.setForeground(TITLECOLOR_DARK);
 requestsPanel.add(lblRequests, BorderLayout.NORTH);
            
 String[ ] columns = {"Category", "Description", "Needed", "Fulfilled", "Urgency", "Donate"};
 DefaultTableModel model = new DefaultTableModel(columns, 0) {
 @Override
 public boolean isCellEditable(int r, int c) {
 return c == 5;
 }
 };
            
 for(ResourceRequest req : requests) {
 if("Open".equals(req.getStatus() ) ) {
 Object[ ] reqRow = {
 req.getItemCategory(),;
 req.getItemDescription(),;
 req.getQuantityNeeded(),;
 req.getQuantityFulfilled(),;
 req.getUrgency(),;
 "Donate"
 };
 model.addRow(reqRow);
 }
 }
            
 JTable requestTable = new JTable(model);
 styleTable(requestTable);
 requestTable.setRowHeight(30);
            
 // Add donate button to last column
 requestTable.getColumnModel().getColumn(5).setCellRenderer(new DonateButtonRenderer();
 requestTable.getColumnModel().getColumn(5).setCellEditor(;
 new DonateButtonEditor(new JCheckBox(), requests, info) );
            
 JScrollPane scrollPane = new JScrollPane(requestTable);
 styleScrollPane(scrollPane);
 scrollPane.setPreferredSize(new Dimension(700, 200) );
 requestsPanel.add(scrollPane, BorderLayout.CENTER);
            
 dialog.add(requestsPanel, BorderLayout.CENTER);
            
 // Close button
 JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT) );
 buttonPanel.setBackground(DARKBGSTART);
 JButton btnClose = new JButton("Close");
 styleActionButton(btnClose, "Close this window" );
 btnClose.addActionListener(e -> dialog.dispose();
 buttonPanel.add(btnClose);
 dialog.add(buttonPanel, BorderLayout.SOUTH);
            
 dialog.setSize(800, 500);
 dialog.setLocationRelativeTo(this);
 dialog.setVisible(true);
            
 } catch(ServiceException e) {
 Logger.error( "Failed to load orphanage details: " + e.getMessage();
 JOptionPane.showMessageDialog(this,
 "Error loading orphanage details:\n" + e.getMessage(),;
 "Error",
 JOptionPane.ERROR_MESSAGE);
 }
 }

 private void addDetailField(JPanel panel, GridBagConstraints gbc, int row,  String label, String value) {
 gbc.gridx = 0;
 gbc.gridy = row;
 gbc.weightx = 0;
 JLabel lbl = new JLabel(label);
 styleFormLabel(lbl);
 panel.add(lbl, gbc);
        
 gbc.gridx = 1;
 gbc.weightx = 1.0;
 JLabel val = new JLabel(value);
 val.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 14) );
 val.setForeground(INPUTFG_DARK);
 panel.add(val, gbc);
 }

 // Button renderer for Actions column
 class ButtonRenderer extends JButton implements TableCellRenderer() {
 public ButtonRenderer() {
 setOpaque(true);
 setFont(new Font(Font.SANSSERIF, Font.BOLD, 11) );
 setForeground(BUTTONFG_DARK);
 setBackground(BUTTONSEARCH_BG);
 setBorder(new EmptyBorder(2, 5, 2, 5) );
 setCursor(Cursor.getPredefinedCursor(Cursor.HANDCURSOR) );
 }
        
 @Override
 public Component getTableCellRendererComponent(JTable table, Object value,
 boolean isSelected, boolean hasFocus, int row, int column) {
 setText( (value == null) ? " " : value.toString();
 return this;
 }
 }

 // Button editor for Actions column
 class ButtonEditor extends DefaultCellEditor() {
 protected JButton button;
 private String label;
 private boolean isPushed;
 private int currentRow;

 public ButtonEditor(JCheckBox checkBox) {
 super(checkBox);
 button = new JButton();
 button.setOpaque(true);
 button.setFont(new Font(Font.SANSSERIF, Font.BOLD, 11) );
 button.setForeground(BUTTONFG_DARK);
 button.setBackground(BUTTONSEARCH_BG);
 button.setBorder(new EmptyBorder(2, 5, 2, 5) );
 button.addActionListener(e -> fireEditingStopped();
 }

 @Override
 public Component getTableCellEditorComponent(JTable table, Object value,
 boolean isSelected, int row, int column) {
 label = (value == null) ? " " : value.toString();
 button.setText(label);
 isPushed = true;
 currentRow = row;
 return button;
 }

 @Override
 public Object getCellEditorValue() {
 if(isPushed) {
 showOrphanageDetails(currentRow);
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

 // Donate button renderer
 class DonateButtonRenderer extends JButton implements TableCellRenderer() {
 public DonateButtonRenderer() {
 setOpaque(true);
 setFont(new Font(Font.SANSSERIF, Font.BOLD, 11) );
 setForeground(BUTTONFG_DARK);
 setBackground(BUTTONDONATEBG);
 setBorder(new EmptyBorder(2, 5, 2, 5) );
 }
        
 @Override
 public Component getTableCellRendererComponent(JTable table, Object value,
 boolean isSelected, boolean hasFocus, int row, int column) {
 setText("Donate");
 return this;
 }
 }

 // Donate button editor
 class DonateButtonEditor extends DefaultCellEditor() {
 protected JButton button;
 private boolean isPushed;
 private int currentRow;
 private List<ResourceRequest> requests;
 private OrphanageInfo orphanageInfo;

 public DonateButtonEditor(JCheckBox checkBox, List<ResourceRequest> requests,  OrphanageInfo info) {
 super(checkBox);
 this.requests = requests;
 this.orphanageInfo = info;
            
 button = new JButton();
 button.setOpaque(true);
 button.setFont(new Font(Font.SANSSERIF, Font.BOLD, 11) );
 button.setForeground(BUTTONFG_DARK);
 button.setBackground(BUTTONDONATEBG);
 button.addActionListener(e -> fireEditingStopped();
 }

 @Override
 public Component getTableCellEditorComponent(JTable table, Object value,
 boolean isSelected, int row, int column) {
 button.setText("Donate");
 isPushed = true;
 currentRow = row;
 return button;
 }

 @Override
 public Object getCellEditorValue() {
 if(isPushed && currentRow < requests.size() ) {
 showDonationDialog(requests.get(currentRow), orphanageInfo);
 }
 isPushed = false;
 return "Donate";
 }
 }

 private void showDonationDialog(ResourceRequest request, OrphanageInfo orphanage) {
 JDialog dialog = new JDialog( (Frame) SwingUtilities.getWindowAncestor(this),;
 "Make a Donation", true);
 dialog.setLayout(new BorderLayout();
        
 JPanel formPanel = new JPanel(new GridBagLayout();
 formPanel.setBorder(new EmptyBorder(20, 20, 20, 20) );
 formPanel.setBackground(DARKBGSTART);
        
 GridBagConstraints gbc = new GridBagConstraints();
 gbc.fill = GridBagConstraints.HORIZONTAL;
 gbc.insets = new Insets(5, 5, 5, 5);
        
 // Display request details
 int row = 0;
 addDonationField(formPanel, gbc, row++, "Orphanage:", orphanage.getName();
 addDonationField(formPanel, gbc, row++, "Item Needed:", request.getItemDescription();
 addDonationField(formPanel, gbc, row++, "Category:", request.getItemCategory();
 addDonationField(formPanel, gbc, row++, "Quantity Needed:",  String.valueOf(request.getQuantityNeeded() - request.getQuantityFulfilled() );
        
 // Donation amount input
 gbc.gridx = 0;
 gbc.gridy = row;
 JLabel lblAmount = new JLabel("Donation Quantity:");
 styleFormLabel(lblAmount);
 formPanel.add(lblAmount, gbc);
        
 gbc.gridx = 1;
 int maxDonation = request.getQuantityNeeded() - request.getQuantityFulfilled();
 SpinnerNumberModel model = new SpinnerNumberModel(1, 1, maxDonation, 1);
 JSpinner spnAmount = new JSpinner(model);
 styleSpinner(spnAmount);
 formPanel.add(spnAmount, gbc);
 row++;
        
 // Contact info
 gbc.gridx = 0;
 gbc.gridy = row;
 JLabel lblContact = new JLabel("Your Contact:");
 styleFormLabel(lblContact);
 formPanel.add(lblContact, gbc);
        
 gbc.gridx = 1;
 JTextField txtContact = new JTextField();
 styleTextField(txtContact);
 if(donorUser != null) {
 txtContact.setText(donorUser.getEmail();
 }
 formPanel.add(txtContact, gbc);
 row++;
        
 // Message
 gbc.gridx = 0;
 gbc.gridy = row;
 gbc.anchor = GridBagConstraints.NORTHWEST;
 JLabel lblMessage = new JLabel("Message:");
 styleFormLabel(lblMessage);
 formPanel.add(lblMessage, gbc);
        
 gbc.gridx = 1;
 gbc.fill = GridBagConstraints.BOTH;
 gbc.weighty = 1.0;
 JTextArea txtMessage = new JTextArea(3, 20);
 txtMessage.setLineWrap(true);
 txtMessage.setWrapStyleWord(true);
 styleTextArea(txtMessage);
 JScrollPane msgScroll = new JScrollPane(txtMessage);
 styleScrollPane(msgScroll);
 formPanel.add(msgScroll, gbc);
        
 dialog.add(formPanel, BorderLayout.CENTER);
        
 // Buttons
 JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT) );
 buttonPanel.setBackground(DARKBGSTART);
        
 JButton btnConfirm = new JButton("Confirm Donation");
 JButton btnCancel = new JButton("Cancel");
 styleActionButton(btnConfirm, "Confirm your donation pledge" );
 styleActionButton(btnCancel, "Cancel donation" );
        
 btnConfirm.setBackground(BUTTONDONATEBG);
 btnConfirm.addMouseListener(new MouseAdapter() {
 @Override public void mouseEntered(MouseEvent e) {
 btnConfirm.setBackground(BUTTONDONATEHOVER_BG);
 }
 @Override public void mouseExited(MouseEvent e) {
 btnConfirm.setBackground(BUTTONDONATEBG);
 }
 });
        
 btnConfirm.addActionListener(e -> ) {
 String contact = txtContact.getText().trim();
 if(contact.isEmpty() ) {
 JOptionPane.showMessageDialog(dialog,
 "Please provide contact information.",
 "Validation Error",
 JOptionPane.ERROR_MESSAGE);
 return;
 }
            
 try {
 // Record donation
 Donation donation = new Donation();
 donation.setDonorId(donorUser.getUserId();
 donation.setRequestId(request.getRequestID();
 donation.setQuantity( (Integer) spnAmount.getValue();
 donation.setContactInfo(contact);
 donation.setMessage(txtMessage.getText().trim();
                
 donorService.recordDonation(donation);
                
 JOptionPane.showMessageDialog(dialog,
 "Thank you for your donation! \n" +
 "The orphanage will contact you at: " + contact,
 "Donation Successful",
 JOptionPane.INFORMATIONMESSAGE);
                
 dialog.dispose();
 loadOrphanageData(); // Refresh data;
                
 } catch(ServiceException ex) {
 Logger.error( "Failed to record donation: " + ex.getMessage();
 JOptionPane.showMessageDialog(dialog,
 "Error recording donation:\n" + ex.getMessage(),;
 "Error",
 JOptionPane.ERROR_MESSAGE);
 }
 });
        
 btnCancel.addActionListener(e -> dialog.dispose();
        
 buttonPanel.add(btnConfirm);
 buttonPanel.add(btnCancel);
 dialog.add(buttonPanel, BorderLayout.SOUTH);
        
 dialog.setSize(500, 400);
 dialog.setLocationRelativeTo(this);
 dialog.setVisible(true);
 }

 private void addDonationField(JPanel panel, GridBagConstraints gbc, int row,
 String label, String value) {
 gbc.gridx = 0;
 gbc.gridy = row;
 gbc.weightx = 0;
 gbc.weighty = 0;
 gbc.fill = GridBagConstraints.HORIZONTAL;
 gbc.anchor = GridBagConstraints.EAST;
 JLabel lbl = new JLabel(label);
 styleFormLabel(lbl);
 panel.add(lbl, gbc);
        
 gbc.gridx = 1;
 gbc.weightx = 1.0;
 gbc.anchor = GridBagConstraints.WEST;
 JLabel val = new JLabel(value);
 val.setFont(new Font(Font.SANSSERIF, Font.BOLD, 14) );
 val.setForeground(INPUTFG_DARK);
 panel.add(val, gbc);
 }

 // Styling methods(similar to other panels)
 private void styleTable(JTable table) {
 table.setBackground(TABLECELLBG);
 table.setForeground(TABLECELLFG);
 table.setGridColor(TABLEGRIDCOLOR);
 table.setRowHeight(35);
 table.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 13) );
 table.setFillsViewportHeight(true);
 table.setSelectionBackground(TABLECELLSELECTED_BG);
 table.setSelectionForeground(TABLECELLSELECTED_FG);
        
 JTableHeader header = table.getTableHeader();
 header.setBackground(TABLEHEADER_BG);
 header.setForeground(TABLEHEADER_FG);
 header.setFont(new Font(Font.SANSSERIF, Font.BOLD, 14) );
 header.setBorder(BorderFactory.createLineBorder(BORDERCOLOR_DARK) );
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
 @Override protected void configureScrollBarColors() {
 this.thumbColor = BUTTONBG_DARK;
 this.trackColor = DARKBGEND;
 }
 @Override protected JButton createDecreaseButton(int o) { return createZeroButton(); }
 @Override protected JButton createIncreaseButton(int o) { return createZeroButton(); }
 private JButton createZeroButton() {
 JButton b = new JButton();
 b.setPreferredSize(new Dimension(0, 0) );
 return b;
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
 btn.setBorder(new CompoundBorder(
 BorderFactory.createLineBorder(BUTTONBG_DARK.darker(),;
 new EmptyBorder(6, 12, 6, 12) );
 }

 private void styleFormLabel(JLabel label) {
 label.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 13) );
 label.setForeground(TEXTCOLOR_DARK);
 }

 private void styleTextField(JTextField field) {
 field.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 13) );
 field.setForeground(INPUTFG_DARK);
 field.setBackground(INPUTBG_DARK);
 field.setBorder(new CompoundBorder(
 BorderFactory.createLineBorder(INPUTBORDER_DARK, 1),;
 new EmptyBorder(4, 6, 4, 6) );
 field.setCaretColor(Color.LIGHTGRAY);
 }

 private void styleTextArea(JTextArea area) {
 area.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 13) );
 area.setForeground(INPUTFG_DARK);
 area.setBackground(INPUTBG_DARK);
 area.setBorder(new EmptyBorder(5, 8, 5, 8) );
 area.setCaretColor(Color.LIGHTGRAY);
 }

 private void styleComboBox(JComboBox<?> comboBox) {
 comboBox.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 13) );
 comboBox.setForeground(INPUTFG_DARK);
 comboBox.setBackground(INPUTBG_DARK);
 comboBox.setBorder(BorderFactory.createLineBorder(INPUTBORDER_DARK, 1) );
 }

 private void styleSpinner(JSpinner spinner) {
 spinner.setFont(new Font(Font.SANSSERIF, Font.PLAIN, 14) );
 JComponent editor = spinner.getEditor();
 if(editor instanceof JSpinner.DefaultEditor) {
 JTextField textField = ( (JSpinner.DefaultEditor) editor).getTextField();
 textField.setForeground(INPUTFG_DARK);
 textField.setBackground(INPUTBG_DARK);
 textField.setCaretColor(Color.LIGHTGRAY);
 }
 }
))))))))))))))))))))))))))))))))))))))))
}