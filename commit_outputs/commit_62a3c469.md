# Commit 3 — 62a3c469

- Full hash: `62a3c46922f2fb4392f2c309b0a9b904042fc0cf`
- Author: jared-one
- Date: 2025-08-28 11:40:36 +0200
- Message: my latest changes [2]

Generated: 2025-09-05T17:39:38.566103

---
## Files changed
- all_java_code.txt
- grouped_errors.txt
- src/main/java/com/orphanagehub/dao/DatabaseManager.java
- src/main/java/com/orphanagehub/dao/OrphanageDAO.java
- src/main/java/com/orphanagehub/dao/UserDAO.java
- src/main/java/com/orphanagehub/gui/RegistrationPanel.java
- src/main/java/com/orphanagehub/model/Orphanage.java
- src/main/java/com/orphanagehub/model/User.java
- src/main/java/com/orphanagehub/service/AuthenticationService.java
- src/main/java/com/orphanagehub/service/RegistrationService.java
- src/main/java/com/orphanagehub/tools/DbDoctor.java
- src/main/java/com/orphanagehub/tools/DbShell.java
- src/main/java/com/orphanagehub/tools/DbTest.java

---
### all_java_code.txt
```
╔══════════════════════════════════════════════════════════════════════════╗
║                    ORPHANAGEHUB PROJECT - JAVA SOURCE CODE                  ║
║                         Generated: Thu Aug 28 10:51:37 AM SAST 2025                    ║
╚══════════════════════════════════════════════════════════════════════════╝

════════════════════════════════════════════════════════════════════════════
▶ FILE: AdminDashboardPanel.java (root)
▶ PATH: ./AdminDashboardPanel.java
▶ SIZE: 386 lines
────────────────────────────────────────────────────────────────────────────

     1	package com.orphanagehub.gui;
     2	
     3	import javax.swing.*;
     4	import javax.swing.border.Border;
     5	import javax.swing.border.CompoundBorder;
     6	import javax.swing.border.EmptyBorder;
     7	import javax.swing.plaf.basic.BasicComboBoxUI;
     8	import javax.swing.plaf.basic.BasicScrollBarUI;
     9	import javax.swing.table.DefaultTableCellRenderer;
    10	import javax.swing.table.JTableHeader;
    11	import java.awt.*;
    12	import java.awt.event.MouseAdapter;
    13	import java.awt.event.MouseEvent;
    14	import java.awt.geom.Point2D;
    15	import java.awt.event.ActionListener; // Keep this import
    16	
    17	public class AdminDashboardPanel extends JPanel {
    18	
    19	    private OrphanageHubApp mainApp;
    20	    private String adminUsername = "admin_user"; // Placeholder
    21	
    22	    // Define Colors (Consider shared constants)
    23	    private static final Color DARK_BG_START = new Color(45, 52, 54);
    24	    private static final Color DARK_BG_END = new Color(35, 42, 44);
    25	    private static final Color TITLE_COLOR_DARK = new Color(223, 230, 233);
    26	    private static final Color TEXT_COLOR_DARK = new Color(200, 200, 200);
    27	    private static final Color BORDER_COLOR_DARK = new Color(80, 80, 80);
    28	    private static final Color INPUT_BG_DARK = new Color(60, 60, 60);
    29	    private static final Color INPUT_FG_DARK = new Color(220, 220, 220);
    30	    private static final Color INPUT_BORDER_DARK = new Color(90, 90, 90);
    31	    private static final Color BUTTON_BG_DARK = new Color(99, 110, 114);
    32	    private static final Color BUTTON_FG_DARK = Color.WHITE;
    33	    private static final Color BUTTON_HOVER_BG_DARK = new Color(120, 130, 134);
    34	    private static final Color TAB_BG_SELECTED = new Color(70, 80, 82);
    35	    private static final Color TAB_BG_UNSELECTED = new Color(55, 62, 64);
    36	    private static final Color TAB_FG = TITLE_COLOR_DARK;
    37	    private static final Color TABLE_HEADER_BG = new Color(65, 75, 77);
    38	    private static final Color TABLE_HEADER_FG = TITLE_COLOR_DARK;
    39	    private static final Color TABLE_GRID_COLOR = BORDER_COLOR_DARK;
    40	    private static final Color TABLE_CELL_BG = new Color(55, 62, 64);
    41	    private static final Color TABLE_CELL_FG = TEXT_COLOR_DARK;
    42	    private static final Color TABLE_CELL_SELECTED_BG = BUTTON_BG_DARK;
    43	    private static final Color TABLE_CELL_SELECTED_FG = BUTTON_FG_DARK;
    44	    // Action Button Colors
    45	    private static final Color BUTTON_APPROVE_BG = new Color(60, 179, 113); // Green
    46	    private static final Color BUTTON_APPROVE_HOVER_BG = new Color(70, 190, 123);
    47	    private static final Color BUTTON_REJECT_BG = new Color(192, 57, 43); // Red
    48	    private static final Color BUTTON_REJECT_HOVER_BG = new Color(231, 76, 60);
    49	    private static final Color BUTTON_SUSPEND_BG = BUTTON_REJECT_BG; // Use same red for suspend
    50	    private static final Color BUTTON_SUSPEND_HOVER_BG = BUTTON_REJECT_HOVER_BG;
    51	
    52	
    53	    public AdminDashboardPanel(OrphanageHubApp app) {
    54	        this.mainApp = app;
    55	        setLayout(new BorderLayout(0, 0));
    56	        initComponents();
    57	    }
    58	
    59	    @Override
    60	    protected void paintComponent(Graphics g) {
    61	        super.paintComponent(g);
    62	        Graphics2D g2d = (Graphics2D) g;
    63	        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    64	        GradientPaint gp = new GradientPaint(0, 0, DARK_BG_START, 0, getHeight(), DARK_BG_END);
    65	        g2d.setPaint(gp);
    66	        g2d.fillRect(0, 0, getWidth(), getHeight());
    67	    }
    68	
    69	    private void initComponents() {
    70	        // --- Header Panel ---
    71	        JPanel headerPanel = createHeaderPanel();
    72	        add(headerPanel, BorderLayout.NORTH);
    73	
    74	        // --- Tabbed Pane for Content ---
    75	        JTabbedPane tabbedPane = createTabbedPane();
    76	        add(tabbedPane, BorderLayout.CENTER);
    77	    }
    78	
    79	    // --- Helper Methods ---
    80	
    81	    private JPanel createHeaderPanel() {
    82	        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
    83	        headerPanel.setOpaque(false);
    84	        headerPanel.setBorder(new CompoundBorder(
    85	                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR_DARK),
    86	                new EmptyBorder(10, 20, 10, 20)
    87	        ));
    88	
    89	        // Left side: Role Icon and Title
    90	        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    91	        titleGroup.setOpaque(false);
    92	        JLabel iconLabel = new JLabel("\u2699"); // Gear symbol
    93	        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
    94	        iconLabel.setForeground(TITLE_COLOR_DARK); // Standard title color for gear
    95	        JLabel nameLabel = new JLabel("Administrator Dashboard");
    96	        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
    97	        nameLabel.setForeground(TITLE_COLOR_DARK);
    98	        titleGroup.add(iconLabel);
    99	        titleGroup.add(nameLabel);
   100	        headerPanel.add(titleGroup, BorderLayout.WEST);
   101	
   102	        // Right side: User info and Logout Button
   103	        JPanel userGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
   104	        userGroup.setOpaque(false);
   105	        JLabel userLabel = new JLabel("Admin User: " + adminUsername);
   106	        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
   107	        userLabel.setForeground(TEXT_COLOR_DARK);
   108	        JButton btnLogout = new JButton("Logout");
   109	        styleActionButton(btnLogout, "Logout and return to welcome screen");
   110	        btnLogout.setPreferredSize(new Dimension(100, 30));
   111	        btnLogout.setBackground(BUTTON_REJECT_BG); // Use red for admin logout too?
   112	        btnLogout.addMouseListener(new MouseAdapter() {
   113	             @Override public void mouseEntered(MouseEvent e) { btnLogout.setBackground(BUTTON_REJECT_HOVER_BG); }
   114	             @Override public void mouseExited(MouseEvent e) { btnLogout.setBackground(BUTTON_REJECT_BG); }
   115	        });
   116	        btnLogout.addActionListener(e -> mainApp.navigateTo(OrphanageHubApp.HOME_PANEL));
   117	        userGroup.add(userLabel);
   118	        userGroup.add(btnLogout);
   119	        headerPanel.add(userGroup, BorderLayout.EAST);
   120	
   121	        return headerPanel;
   122	    }
   123	
   124	     private JTabbedPane createTabbedPane() {
   125	        JTabbedPane tabbedPane = new JTabbedPane();
   126	        tabbedPane.setOpaque(false);
   127	        tabbedPane.setForeground(TAB_FG);
   128	        tabbedPane.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
   129	        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() { // Copied UI styling
   130	             @Override protected void installDefaults() { super.installDefaults(); lightHighlight=TAB_BG_SELECTED; shadow=BORDER_COLOR_DARK; darkShadow=DARK_BG_END; focus=TAB_BG_SELECTED; }
   131	             @Override protected void paintTabBackground(Graphics g, int p, int i, int x, int y, int w, int h, boolean s) { g.setColor(s ? TAB_BG_SELECTED : TAB_BG_UNSELECTED); g.fillRoundRect(x, y, w, h+5, 5, 5); }
   132	             @Override protected void paintTabBorder(Graphics g, int p, int i, int x, int y, int w, int h, boolean s) { /* Minimal border */ }
   133	             @Override protected void paintContentBorder(Graphics g, int p, int i) { int w=tabPane.getWidth(); int h=tabPane.getHeight(); Insets ins=tabPane.getInsets(); int th=calculateTabAreaHeight(p, runCount, maxTabHeight); int x=ins.left; int y=ins.top+th-(lightHighlight.getAlpha()>0?1:0); int cw=w-ins.right-ins.left; int ch=h-ins.top-ins.bottom-y; g.setColor(BORDER_COLOR_DARK); g.drawRect(x, y, cw-1, ch-1); }
   134	        });
   135	
   136	        // Create and add tabs
   137	        tabbedPane.addTab("Orphanage Verification", createVerificationTab());
   138	        tabbedPane.addTab("User Management", createUserManagementTab());
   139	        tabbedPane.addTab("System Overview", createSystemOverviewTab());
   140	
   141	        return tabbedPane;
   142	    }
   143	
   144	    // --- Tab Creation Methods ---
   145	
   146	    private JPanel createVerificationTab() {
   147	        JPanel panel = new JPanel(new BorderLayout(10, 10));
   148	        panel.setOpaque(false);
   149	        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
   150	
   151	        // Optional: Add filter for status (Pending, Verified, Rejected) later
   152	
   153	        // --- Verification Table ---
   154	        String[] columnNames = {"Orphanage Name", "Contact", "Email", "Registered", "Status", "Actions"};
   155	        Object[][] data = { // Placeholder data
   156	            {"New Hope Center", "Alice Smith", "alice@newhope.org", "2024-05-10", "Pending", "Verify"},
   157	            {"Future Stars", "Bob Jones", "bob@futurestars.net", "2024-05-08", "Pending", "Verify"},
   158	            {"Safe Haven Kids", "Charlie P.", "contact@safehaven.com", "2024-04-20", "Verified", "View"},
   159	            {"Distant Dreams", "Diana Ross", "info@distdreams.org", "2024-05-11", "Pending", "Verify"}
   160	        };
   161	
   162	        JTable table = new JTable(data, columnNames) {
   163	            @Override public boolean isCellEditable(int row, int column) { return column == 5; } // Action column
   164	        };
   165	        styleTable(table);
   166	
   167	        // --- Action Column Renderer/Editor ---
   168	        JPanel buttonPanelRenderer = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
   169	        buttonPanelRenderer.setOpaque(false);
   170	        JButton approveBtnRend = new JButton("âœ“"); // Check mark
   171	        JButton rejectBtnRend = new JButton("âœ•"); // X mark
   172	        JButton detailsBtnRend = new JButton("..."); // Details
   173	        styleMiniButton(approveBtnRend, BUTTON_APPROVE_BG);
   174	        styleMiniButton(rejectBtnRend, BUTTON_REJECT_BG);
   175	        styleMiniButton(detailsBtnRend, BUTTON_BG_DARK);
   176	        buttonPanelRenderer.add(approveBtnRend);
   177	        buttonPanelRenderer.add(rejectBtnRend);
   178	        buttonPanelRenderer.add(detailsBtnRend);
   179	
   180	        table.getColumnModel().getColumn(5).setCellRenderer((tbl, value, isSelected, hasFocus, row, column) -> buttonPanelRenderer);
   181	
   182	        table.getColumnModel().getColumn(5).setCellEditor(new ActionPanelEditor(new JCheckBox(), (actionCommand, row) -> {
   183	            String orphanageName = (String) table.getModel().getValueAt(row, 0);
   184	            switch(actionCommand) {
   185	                case "approve":
   186	                    JOptionPane.showMessageDialog(this, "Approve: " + orphanageName + "\n(Logic TBD)", "Approve", JOptionPane.INFORMATION_MESSAGE);
   187	                    // Update table model status to "Verified"
   188	                    break;
   189	                case "reject":
   190	                     if (JOptionPane.showConfirmDialog(this, "Reject " + orphanageName + "?", "Confirm Reject", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
   191	                         JOptionPane.showMessageDialog(this, "Reject: " + orphanageName + "\n(Logic TBD)", "Reject", JOptionPane.INFORMATION_MESSAGE);
   192	                         // Update table model status to "Rejected"
   193	                     }
   194	                    break;
   195	                case "details":
   196	                    JOptionPane.showMessageDialog(this, "View Details for: " + orphanageName + "\n(Logic TBD)", "Details", JOptionPane.INFORMATION_MESSAGE);
   197	                    break;
   198	            }
   199	        }));
   200	
   201	
   202	        // Adjust column widths
   203	        table.getColumnModel().getColumn(0).setPreferredWidth(180); // Name
   204	        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Contact
   205	        table.getColumnModel().getColumn(2).setPreferredWidth(180); // Email
   206	        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Registered
   207	        table.getColumnModel().getColumn(4).setPreferredWidth(80);  // Status
   208	        table.getColumnModel().getColumn(5).setPreferredWidth(120); // Actions (needs space for buttons)
   209	        table.setRowHeight(approveBtnRend.getPreferredSize().height + 4); // Set row height based on buttons
   210	
   211	        JScrollPane scrollPane = new JScrollPane(table);
   212	        styleScrollPane(scrollPane);
   213	        panel.add(scrollPane, BorderLayout.CENTER);
   214	
   215	        return panel;
   216	    }
   217	
   218	    // *** CORRECTED METHOD ***
   219	    private JPanel createUserManagementTab() {
   220	        JPanel panel = new JPanel(new BorderLayout(10, 10));
   221	        panel.setOpaque(false);
   222	        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
   223	
   224	        // --- User Search/Filter (Optional) ---
   225	        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
   226	        searchPanel.setOpaque(false);
   227	
   228	        // Create components first
   229	        JLabel lblSearchUser = new JLabel("Search User:");
   230	        JTextField txtUserSearch = new JTextField(20);
   231	        JLabel lblUserRole = new JLabel("Role:"); // *** STORE LABEL IN VARIABLE ***
   232	        JComboBox<String> cmbUserRole = new JComboBox<>(new String[]{"Any Role", "Admin", "OrphanageStaff", "Donor", "Volunteer"});
   233	        JButton btnUserSearch = new JButton("Search");
   234	
   235	        // Style components
   236	        styleFormLabel(lblSearchUser);
   237	        styleTextField(txtUserSearch);
   238	        styleFormLabel(lblUserRole); // *** STYLE USING VARIABLE ***
   239	        styleComboBox(cmbUserRole);
   240	        styleActionButton(btnUserSearch, "Find users");
   241	        // Add search action listener later
   242	
   243	        // Add components to panel in order
   244	        searchPanel.add(lblSearchUser);
   245	        searchPanel.add(txtUserSearch);
   246	        searchPanel.add(lblUserRole); // *** ADD LABEL ***
   247	        searchPanel.add(cmbUserRole); // *** ADD COMBOBOX ***
   248	        searchPanel.add(btnUserSearch);
   249	
   250	        panel.add(searchPanel, BorderLayout.NORTH);
   251	
   252	
   253	        // --- User Table ---
   254	        String[] columnNames = {"Username", "Email", "Role", "Status", "Registered", "Actions"};
   255	        Object[][] data = { // Placeholder data
   256	            {"staff_user", "staff@example.com", "OrphanageStaff", "Active", "2024-01-15", "Manage"},
   257	            {"donor_user", "donor@mail.net", "Donor", "Active", "2024-02-10", "Manage"},
   258	            {"volunteer_A", "vol@provider.org", "Volunteer", "Active", "2024-03-01", "Manage"},
   259	            {"old_staff", "old@example.com", "OrphanageStaff", "Suspended", "2023-11-20", "Manage"},
   260	            {"admin_user", "admin@orphanagehub.com", "Admin", "Active", "2023-10-01", "Manage"}
   261	        };
   262	
   263	        JTable table = new JTable(data, columnNames) {
   264	            @Override public boolean isCellEditable(int row, int column) { return column == 5; } // Action column
   265	        };
   266	        styleTable(table);
   267	
   268	        // --- Action Column Renderer/Editor ---
   269	        JPanel userActionPanelRenderer = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
   270	        userActionPanelRenderer.setOpaque(false);
   271	        JButton activateBtnRend = new JButton("âœ“");
   272	        JButton suspendBtnRend = new JButton("âœ•");
   273	        JButton viewBtnRend = new JButton("...");
   274	        styleMiniButton(activateBtnRend, BUTTON_APPROVE_BG);
   275	        styleMiniButton(suspendBtnRend, BUTTON_SUSPEND_BG);
   276	        styleMiniButton(viewBtnRend, BUTTON_BG_DARK);
   277	        userActionPanelRenderer.add(activateBtnRend);
   278	        userActionPanelRenderer.add(suspendBtnRend);
   279	        userActionPanelRenderer.add(viewBtnRend);
   280	
   281	         table.getColumnModel().getColumn(5).setCellRenderer((tbl, value, isSelected, hasFocus, row, column) -> {
   282	             String currentStatus = (String) tbl.getValueAt(row, 3);
   283	             activateBtnRend.setVisible("Suspended".equals(currentStatus));
   284	             suspendBtnRend.setVisible("Active".equals(currentStatus));
   285	             String username = (String) tbl.getValueAt(row, 0);
   286	             if (username.equals(adminUsername)) {
   287	                 activateBtnRend.setVisible(false);
   288	                 suspendBtnRend.setVisible(false);
   289	             }
   290	            return userActionPanelRenderer;
   291	        });
   292	
   293	        table.getColumnModel().getColumn(5).setCellEditor(new ActionPanelEditor(new JCheckBox(), (actionCommand, row) -> {
   294	             String username = (String) table.getModel().getValueAt(row, 0);
   295	             if (username.equals(adminUsername)) return;
   296	
   297	             String currentStatus = (String) table.getModel().getValueAt(row, 3);
   298	             switch(actionCommand) {
   299	                 case "activate":
   300	                      if ("Suspended".equals(currentStatus)) {
   301	                          JOptionPane.showMessageDialog(this, "Activate User: " + username + "\n(Logic TBD)", "Activate", JOptionPane.INFORMATION_MESSAGE);
   302	                      }
   303	                     break;
   304	                 case "suspend":
   305	                     if ("Active".equals(currentStatus)) {
   306	                         if (JOptionPane.showConfirmDialog(this, "Suspend User: " + username + "?", "Confirm Suspend", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
   307	                             JOptionPane.showMessageDialog(this, "Suspend User: " + username + "\n(Logic TBD)", "Suspend", JOptionPane.INFORMATION_MESSAGE);
   308	                         }
   309	                     }
   310	                     break;
   311	                 case "view":
   312	                     JOptionPane.showMessageDialog(this, "View User Profile: " + username + "\n(Logic TBD)", "View User", JOptionPane.INFORMATION_MESSAGE);
   313	                     break;
   314	             }
   315	         }));
   316	
   317	
   318	        // Adjust column widths
   319	        table.getColumnModel().getColumn(0).setPreferredWidth(120);
   320	        table.getColumnModel().getColumn(1).setPreferredWidth(180);
   321	        table.getColumnModel().getColumn(2).setPreferredWidth(100);
   322	        table.getColumnModel().getColumn(3).setPreferredWidth(80);
   323	        table.getColumnModel().getColumn(4).setPreferredWidth(100);
   324	        table.getColumnModel().getColumn(5).setPreferredWidth(120);
   325	        table.setRowHeight(activateBtnRend.getPreferredSize().height + 4);
   326	
   327	        JScrollPane scrollPane = new JScrollPane(table);
   328	        styleScrollPane(scrollPane);
   329	        panel.add(scrollPane, BorderLayout.CENTER);
   330	
   331	        return panel;
   332	    }
   333	    // *** END OF CORRECTED METHOD ***
   334	
   335	     private JPanel createSystemOverviewTab() {
   336	        JPanel panel = new JPanel();
   337	        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Vertical layout
   338	        panel.setOpaque(false);
   339	        panel.setBorder(new EmptyBorder(25, 30, 25, 30));
   340	
   341	        panel.add(createOverviewStat("Total Registered Users:", "157"));
   342	        panel.add(Box.createVerticalStrut(10));
   343	        panel.add(createOverviewStat("Verified Orphanages:", "34"));
   344	        panel.add(Box.createVerticalStrut(10));
   345	        panel.add(createOverviewStat("Pending Verification:", "3"));
   346	        panel.add(Box.createVerticalStrut(10));
   347	        panel.add(createOverviewStat("Open Resource Requests:", "48"));
   348	        panel.add(Box.createVerticalStrut(10));
   349	        panel.add(createOverviewStat("Active Volunteers:", "22"));
   350	        panel.add(Box.createVerticalGlue()); // Pushes stats to the top
   351	
   352	        return panel;
   353	    }
   354	
   355	    // Helper for overview stats labels
   356	    private Component createOverviewStat(String labelText, String valueText) {
   357	        JPanel statPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
   358	        statPanel.setOpaque(false);
   359	        JLabel label = new JLabel(labelText);
   360	        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
   361	        label.setForeground(TEXT_COLOR_DARK);
   362	        JLabel value = new JLabel(valueText);
   363	        value.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
   364	        value.setForeground(TITLE_COLOR_DARK);
   365	        statPanel.add(label);
   366	        statPanel.add(value);
   367	        return statPanel;
   368	    }
   369	
   370	
   371	    // --- Styling Helpers (Unchanged) ---
   372	    private void styleFormLabel(JLabel label) { label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); label.setForeground(TEXT_COLOR_DARK); }
   373	    private void styleTextField(JTextField field) { field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); field.setForeground(INPUT_FG_DARK); field.setBackground(INPUT_BG_DARK); Border p=new EmptyBorder(4,6,4,6); field.setBorder(new CompoundBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK,1),p)); field.setCaretColor(Color.LIGHT_GRAY); }
   374	    private void styleComboBox(JComboBox<?> comboBox) { comboBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); comboBox.setForeground(INPUT_FG_DARK); comboBox.setBackground(INPUT_BG_DARK); comboBox.setBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK,1)); for(Component c:comboBox.getComponents()){if(c instanceof JButton){((JButton)c).setBackground(BUTTON_BG_DARK);((JButton)c).setBorder(BorderFactory.createEmptyBorder());break;}} Object p=comboBox.getUI().getAccessibleChild(comboBox,0); if(p instanceof JPopupMenu){JPopupMenu pm=(JPopupMenu)p;pm.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); for(Component comp:pm.getComponents()){if(comp instanceof JScrollPane){JScrollPane sp=(JScrollPane)comp;sp.getViewport().setBackground(INPUT_BG_DARK);applyScrollbarUI(sp.getVerticalScrollBar()); Component l=sp.getViewport().getView(); if(l instanceof JList){((JList<?>)l).setBackground(INPUT_BG_DARK);((JList<?>)l).setForeground(INPUT_FG_DARK);((JList<?>)l).setSelectionBackground(BUTTON_BG_DARK);((JList<?>)l).setSelectionForeground(BUTTON_FG_DARK);}}}}}
   375	    private void styleTable(JTable table) { table.setBackground(TABLE_CELL_BG); table.setForeground(TABLE_CELL_FG); table.setGridColor(TABLE_GRID_COLOR); table.setRowHeight(28); table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); table.setFillsViewportHeight(true); table.setSelectionBackground(TABLE_CELL_SELECTED_BG); table.setSelectionForeground(TABLE_CELL_SELECTED_FG); table.setShowGrid(true); table.setIntercellSpacing(new Dimension(0,1)); JTableHeader h=table.getTableHeader(); h.setBackground(TABLE_HEADER_BG); h.setForeground(TABLE_HEADER_FG); h.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14)); h.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); h.setReorderingAllowed(true); h.setResizingAllowed(true); DefaultTableCellRenderer r=new DefaultTableCellRenderer(); r.setHorizontalAlignment(SwingConstants.LEFT); r.setVerticalAlignment(SwingConstants.CENTER); r.setBorder(new EmptyBorder(2,5,2,5)); for(int i=0; i<table.getColumnCount()-1; i++){table.getColumnModel().getColumn(i).setCellRenderer(r);} }
   376	    private void styleScrollPane(JScrollPane scrollPane) { scrollPane.setOpaque(false); scrollPane.getViewport().setOpaque(false); scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); applyScrollbarUI(scrollPane.getVerticalScrollBar()); applyScrollbarUI(scrollPane.getHorizontalScrollBar()); }
   377	    private void applyScrollbarUI(JScrollBar scrollBar) { scrollBar.setUI(new BasicScrollBarUI() { @Override protected void configureScrollBarColors(){this.thumbColor=BUTTON_BG_DARK; this.trackColor=DARK_BG_END;} @Override protected JButton createDecreaseButton(int o){return createZeroButton();} @Override protected JButton createIncreaseButton(int o){return createZeroButton();} private JButton createZeroButton(){JButton b=new JButton(); b.setPreferredSize(new Dimension(0,0)); b.setMaximumSize(new Dimension(0,0)); b.setMinimumSize(new Dimension(0,0)); return b;} @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r){g.setColor(thumbColor); g.fillRect(r.x,r.y,r.width,r.height);} @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r){g.setColor(trackColor); g.fillRect(r.x,r.y,r.width,r.height);} }); scrollBar.setUnitIncrement(16); }
   378	    private void styleActionButton(JButton btn, String tooltip) { btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12)); btn.setToolTipText(tooltip); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setForeground(BUTTON_FG_DARK); btn.setFocusPainted(false); btn.setBackground(BUTTON_BG_DARK); Border p=new EmptyBorder(6,12,6,12); btn.setBorder(new CompoundBorder(BorderFactory.createLineBorder(BUTTON_BG_DARK.darker()),p)); btn.addMouseListener(new MouseAdapter() { @Override public void mouseEntered(MouseEvent e){if(btn.getBackground().equals(BUTTON_BG_DARK)){btn.setBackground(BUTTON_HOVER_BG_DARK);}} @Override public void mouseExited(MouseEvent e){if(btn.getBackground().equals(BUTTON_HOVER_BG_DARK)){btn.setBackground(BUTTON_BG_DARK);}} }); }
   379	    private void styleMiniButton(JButton btn, Color bg) { btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14)); btn.setMargin(new Insets(0, 2, 0, 2)); btn.setFocusPainted(false); btn.setBackground(bg); btn.setForeground(BUTTON_FG_DARK); btn.setBorder(BorderFactory.createLineBorder(bg.darker())); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
   380	
   381	
   382	    // --- Inner classes for Table Multi-Button Actions (Unchanged) ---
   383	    static class ActionPanelRenderer implements javax.swing.table.TableCellRenderer { private JPanel panel; public ActionPanelRenderer(JPanel buttonPanel){this.panel=buttonPanel;} @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c){return panel;} }
   384	    static class ActionPanelEditor extends DefaultCellEditor { private JPanel panel; private RowActionCallback callback; private int editingRow; interface RowActionCallback{void onAction(String command,int row);} public ActionPanelEditor(JCheckBox c, RowActionCallback cb){super(c);this.callback=cb;panel=new JPanel(new FlowLayout(FlowLayout.CENTER,3,0));panel.setOpaque(true);panel.setBackground(TABLE_CELL_BG); JButton b1=new JButton("âœ“");JButton b2=new JButton("âœ•");JButton b3=new JButton("..."); styleMiniButtonStatic(b1,BUTTON_APPROVE_BG);b1.setActionCommand("approve");styleMiniButtonStatic(b2,BUTTON_REJECT_BG);b2.setActionCommand("reject");styleMiniButtonStatic(b3,BUTTON_BG_DARK);b3.setActionCommand("view"); ActionListener l=e->{if(callback!=null){callback.onAction(e.getActionCommand(),editingRow);}fireEditingStopped();}; b1.addActionListener(l);b2.addActionListener(l);b3.addActionListener(l); panel.add(b1);panel.add(b2);panel.add(b3);} @Override public Component getTableCellEditorComponent(JTable t,Object v,boolean s,int r,int c){this.editingRow=r;String status="";String context=t.getColumnName(c); if(c==5&&t.getColumnName(5).equals("Actions")&&t.getModel().getRowCount()>r){ if(t.getColumnCount()>4 && t.getColumnName(4).equals("Status")){status=(String)t.getValueAt(r,4);((JButton)panel.getComponent(0)).setActionCommand("approve");((JButton)panel.getComponent(1)).setActionCommand("reject");((JButton)panel.getComponent(2)).setActionCommand("details");panel.getComponent(0).setVisible("Pending".equals(status));panel.getComponent(1).setVisible("Pending".equals(status));panel.getComponent(2).setVisible(true);} else if(t.getColumnCount()>3 && t.getColumnName(3).equals("Status")){status=(String)t.getValueAt(r,3);String u=(String)t.getValueAt(r,0);boolean self=u.equals("admin_user");((JButton)panel.getComponent(0)).setActionCommand("activate");((JButton)panel.getComponent(1)).setActionCommand("suspend");((JButton)panel.getComponent(2)).setActionCommand("view");panel.getComponent(0).setVisible("Suspended".equals(status)&&!self);panel.getComponent(1).setVisible("Active".equals(status)&&!self);panel.getComponent(2).setVisible(true);}} panel.setBackground(s?TABLE_CELL_SELECTED_BG:TABLE_CELL_BG); return panel;} @Override public Object getCellEditorValue(){return"";} @Override public boolean stopCellEditing(){return super.stopCellEditing();} @Override protected void fireEditingStopped(){super.fireEditingStopped();} private static void styleMiniButtonStatic(JButton btn,Color bg){btn.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14));btn.setMargin(new Insets(0,2,0,2));btn.setFocusPainted(false);btn.setBackground(bg);btn.setForeground(BUTTON_FG_DARK);btn.setBorder(BorderFactory.createLineBorder(bg.darker()));btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));} }
   385	
   386	} // End of AdminDashboardPanel class

════════════════════════════════════════════════════════════════════════════
▶ FILE: DonorDashboardPanel.java (root)
▶ PATH: ./DonorDashboardPanel.java
▶ SIZE: 230 lines
────────────────────────────────────────────────────────────────────────────

     1	package com.orphanagehub.gui;
     2	
     3	import javax.swing.*;
     4	import javax.swing.border.Border;
     5	import javax.swing.border.CompoundBorder;
     6	import javax.swing.border.EmptyBorder;
     7	import javax.swing.plaf.basic.BasicComboBoxUI; // For potential combo box arrow styling
     8	import javax.swing.plaf.basic.BasicScrollBarUI;
     9	import javax.swing.table.DefaultTableCellRenderer;
    10	import javax.swing.table.JTableHeader;
    11	import java.awt.*;
    12	import java.awt.event.MouseAdapter;
    13	import java.awt.event.MouseEvent;
    14	import java.awt.geom.Point2D;
    15	
    16	public class DonorDashboardPanel extends JPanel {
    17	
    18	    private OrphanageHubApp mainApp;
    19	    private String donorUsername = "donor_user"; // Placeholder
    20	
    21	    // --- Colors (Same as AdminDashboardPanel) ---
    22	    private static final Color DARK_BG_START = new Color(45, 52, 54);
    23	    private static final Color DARK_BG_END = new Color(35, 42, 44);
    24	    private static final Color TITLE_COLOR_DARK = new Color(223, 230, 233);
    25	    private static final Color TEXT_COLOR_DARK = new Color(200, 200, 200);
    26	    private static final Color BORDER_COLOR_DARK = new Color(80, 80, 80);
    27	    private static final Color INPUT_BG_DARK = new Color(60, 60, 60);
    28	    private static final Color INPUT_FG_DARK = new Color(220, 220, 220);
    29	    private static final Color INPUT_BORDER_DARK = new Color(90, 90, 90);
    30	    private static final Color BUTTON_BG_DARK = new Color(99, 110, 114);
    31	    private static final Color BUTTON_FG_DARK = Color.WHITE;
    32	    private static final Color BUTTON_HOVER_BG_DARK = new Color(120, 130, 134);
    33	    private static final Color TABLE_HEADER_BG = new Color(65, 75, 77);
    34	    private static final Color TABLE_HEADER_FG = TITLE_COLOR_DARK;
    35	    private static final Color TABLE_GRID_COLOR = BORDER_COLOR_DARK;
    36	    private static final Color TABLE_CELL_BG = new Color(55, 62, 64);
    37	    private static final Color TABLE_CELL_FG = TEXT_COLOR_DARK;
    38	    private static final Color TABLE_CELL_SELECTED_BG = BUTTON_BG_DARK;
    39	    private static final Color TABLE_CELL_SELECTED_FG = BUTTON_FG_DARK;
    40	    private static final Color BUTTON_SEARCH_BG = new Color(72, 149, 239); // Blueish search button
    41	    private static final Color BUTTON_SEARCH_HOVER_BG = new Color(92, 169, 249);
    42	
    43	
    44	    public DonorDashboardPanel(OrphanageHubApp app) {
    45	        this.mainApp = app;
    46	        setLayout(new BorderLayout(0, 0));
    47	        initComponents();
    48	    }
    49	
    50	    @Override
    51	    protected void paintComponent(Graphics g) {
    52	        super.paintComponent(g);
    53	        Graphics2D g2d = (Graphics2D) g;
    54	        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    55	        GradientPaint gp = new GradientPaint(0, 0, DARK_BG_START, 0, getHeight(), DARK_BG_END);
    56	        g2d.setPaint(gp);
    57	        g2d.fillRect(0, 0, getWidth(), getHeight());
    58	    }
    59	
    60	    private void initComponents() {
    61	        // --- Header Panel ---
    62	        JPanel headerPanel = createHeaderPanel();
    63	        add(headerPanel, BorderLayout.NORTH);
    64	
    65	        // --- Main Content Area (Search + Table) ---
    66	        JPanel contentPanel = new JPanel(new BorderLayout(10, 15));
    67	        contentPanel.setOpaque(false);
    68	        contentPanel.setBorder(new EmptyBorder(15, 20, 20, 20)); // Padding for content area
    69	
    70	        // --- Search/Filter Panel ---
    71	        JPanel searchFilterPanel = createSearchFilterPanel();
    72	        contentPanel.add(searchFilterPanel, BorderLayout.NORTH);
    73	
    74	        // --- Results Table ---
    75	        JTable resultsTable = createResultsTable(); // Using placeholder data
    76	        JScrollPane scrollPane = new JScrollPane(resultsTable);
    77	        styleScrollPane(scrollPane); // Apply dark theme styling
    78	        contentPanel.add(scrollPane, BorderLayout.CENTER);
    79	
    80	        add(contentPanel, BorderLayout.CENTER);
    81	    }
    82	
    83	    // --- Helper Methods ---
    84	
    85	    private JPanel createHeaderPanel() {
    86	        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
    87	        headerPanel.setOpaque(false);
    88	        headerPanel.setBorder(new CompoundBorder(
    89	                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR_DARK),
    90	                new EmptyBorder(10, 20, 10, 20)
    91	        ));
    92	
    93	        // Left side: Role Icon and Title
    94	        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    95	        titleGroup.setOpaque(false);
    96	        JLabel iconLabel = new JLabel("\uD83E\uDEC2"); // Coin symbol (U+1FA99) - may depend on font support
    97	        iconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 22)); // Use font known for symbols
    98	        iconLabel.setForeground(new Color(255, 215, 0)); // Gold color for Donor icon
    99	        JLabel nameLabel = new JLabel("Donor Dashboard");
   100	        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
   101	        nameLabel.setForeground(TITLE_COLOR_DARK);
   102	        titleGroup.add(iconLabel);
   103	        titleGroup.add(nameLabel);
   104	        headerPanel.add(titleGroup, BorderLayout.WEST);
   105	
   106	        // Right side: User info and Logout Button
   107	        JPanel userGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
   108	        userGroup.setOpaque(false);
   109	        JLabel userLabel = new JLabel("User: " + donorUsername);
   110	        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
   111	        userLabel.setForeground(TEXT_COLOR_DARK);
   112	        JButton btnLogout = new JButton("Logout");
   113	        styleActionButton(btnLogout, "Logout and return to welcome screen");
   114	        btnLogout.setPreferredSize(new Dimension(100, 30));
   115	        btnLogout.setBackground(new Color(192, 57, 43)); // Reddish logout
   116	        btnLogout.addMouseListener(new MouseAdapter() {
   117	             @Override public void mouseEntered(MouseEvent e) { btnLogout.setBackground(new Color(231, 76, 60)); }
   118	             @Override public void mouseExited(MouseEvent e) { btnLogout.setBackground(new Color(192, 57, 43)); }
   119	        });
   120	        btnLogout.addActionListener(e -> mainApp.navigateTo(OrphanageHubApp.HOME_PANEL));
   121	        userGroup.add(userLabel);
   122	        userGroup.add(btnLogout);
   123	        headerPanel.add(userGroup, BorderLayout.EAST);
   124	
   125	        return headerPanel;
   126	    }
   127	
   128	    private JPanel createSearchFilterPanel() {
   129	        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
   130	        panel.setOpaque(false);
   131	
   132	        JLabel lblSearch = new JLabel("Search:");
   133	        styleFormLabel(lblSearch);
   134	        JTextField txtSearch = new JTextField(20);
   135	        styleTextField(txtSearch);
   136	
   137	        JLabel lblFilterLocation = new JLabel("Location:");
   138	        styleFormLabel(lblFilterLocation);
   139	        String[] locations = {"Any Location", "City A", "City B", "Region C"}; // Placeholders
   140	        JComboBox<String> cmbLocation = new JComboBox<>(locations);
   141	        styleComboBox(cmbLocation);
   142	
   143	        JLabel lblFilterCategory = new JLabel("Need Category:");
   144	        styleFormLabel(lblFilterCategory);
   145	        String[] categories = {"Any Category", "Food", "Clothing", "Education", "Medical", "Funding"}; // Placeholders
   146	        JComboBox<String> cmbCategory = new JComboBox<>(categories);
   147	        styleComboBox(cmbCategory);
   148	
   149	        JButton btnSearch = new JButton("Apply Filters");
   150	        styleActionButton(btnSearch, "Find orphanages or requests matching criteria");
   151	        // Custom style for search button
   152	        btnSearch.setBackground(BUTTON_SEARCH_BG);
   153	        btnSearch.addMouseListener(new MouseAdapter() {
   154	             @Override public void mouseEntered(MouseEvent e) { btnSearch.setBackground(BUTTON_SEARCH_HOVER_BG); }
   155	             @Override public void mouseExited(MouseEvent e) { btnSearch.setBackground(BUTTON_SEARCH_BG); }
   156	        });
   157	        btnSearch.addActionListener(e -> {
   158	             // Placeholder action
   159	             JOptionPane.showMessageDialog(this, "Search/Filter logic not implemented.", "Search", JOptionPane.INFORMATION_MESSAGE);
   160	        });
   161	
   162	
   163	        panel.add(lblSearch);
   164	        panel.add(txtSearch);
   165	        panel.add(Box.createHorizontalStrut(10)); // Spacer
   166	        panel.add(lblFilterLocation);
   167	        panel.add(cmbLocation);
   168	        panel.add(Box.createHorizontalStrut(10)); // Spacer
   169	        panel.add(lblFilterCategory);
   170	        panel.add(cmbCategory);
   171	        panel.add(Box.createHorizontalStrut(15)); // Spacer
   172	        panel.add(btnSearch);
   173	
   174	        return panel;
   175	    }
   176	
   177	     private JTable createResultsTable() {
   178	        // Placeholder: Table showing orphanages
   179	        String[] columnNames = {"Orphanage Name", "Location", "Key Needs", "Actions"};
   180	        Object[][] data = {
   181	                {"Hope Children's Home", "City A", "Food, Winter Clothing", "View Details"},
   182	                {"Bright Future Orphanage", "City B", "School Supplies, Funding", "View Details"},
   183	                {"Little Angels Shelter", "City A", "Medical Supplies", "View Details"},
   184	                {"Sunshine House", "Region C", "Food, Volunteers", "View Details"},
   185	                {"New Dawn Center", "City B", "Clothing (All Ages)", "View Details"}
   186	        };
   187	
   188	        JTable table = new JTable(data, columnNames) {
   189	             @Override
   190	             public boolean isCellEditable(int row, int column) {
   191	                return column == 3; // Allow interaction only on the last column
   192	             }
   193	        };
   194	
   195	        styleTable(table);
   196	
   197	        // Add button renderer/editor for the "Actions" column
   198	        table.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer(BUTTON_SEARCH_BG));
   199	        // *** CORRECTED LAMBDA HERE (no 'e' parameter) ***
   200	        table.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox(), BUTTON_SEARCH_BG, () -> { // Changed e -> () ->
   201	             int selectedRow = table.convertRowIndexToModel(table.getEditingRow());
   202	             String orphanageName = (String) table.getModel().getValueAt(selectedRow, 0);
   203	             JOptionPane.showMessageDialog(this, "View Details for: " + orphanageName + "\n(Functionality not implemented)", "View Details", JOptionPane.INFORMATION_MESSAGE);
   204	         }));
   205	
   206	        // Adjust column widths
   207	        table.getColumnModel().getColumn(0).setPreferredWidth(200); // Name
   208	        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Location
   209	        table.getColumnModel().getColumn(2).setPreferredWidth(250); // Needs
   210	        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Actions
   211	
   212	        return table;
   213	    }
   214	
   215	
   216	    // --- Styling Helpers (Unchanged from previous version) ---
   217	    private void styleFormLabel(JLabel label) { /* ... */ label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); label.setForeground(TEXT_COLOR_DARK); }
   218	    private void styleTextField(JTextField field) { /* ... */ field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); field.setForeground(INPUT_FG_DARK); field.setBackground(INPUT_BG_DARK); Border p=new EmptyBorder(4,6,4,6); field.setBorder(new CompoundBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK,1),p)); field.setCaretColor(Color.LIGHT_GRAY); }
   219	    private void styleComboBox(JComboBox<?> comboBox) { /* ... */ comboBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); comboBox.setForeground(INPUT_FG_DARK); comboBox.setBackground(INPUT_BG_DARK); comboBox.setBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK,1)); for(Component c:comboBox.getComponents()){if(c instanceof JButton){((JButton)c).setBackground(BUTTON_BG_DARK);((JButton)c).setBorder(BorderFactory.createEmptyBorder());break;}} Object p=comboBox.getUI().getAccessibleChild(comboBox,0); if(p instanceof JPopupMenu){JPopupMenu pm=(JPopupMenu)p;pm.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); for(Component comp:pm.getComponents()){if(comp instanceof JScrollPane){JScrollPane sp=(JScrollPane)comp;sp.getViewport().setBackground(INPUT_BG_DARK);applyScrollbarUI(sp.getVerticalScrollBar()); Component l=sp.getViewport().getView(); if(l instanceof JList){((JList<?>)l).setBackground(INPUT_BG_DARK);((JList<?>)l).setForeground(INPUT_FG_DARK);((JList<?>)l).setSelectionBackground(BUTTON_BG_DARK);((JList<?>)l).setSelectionForeground(BUTTON_FG_DARK);}}}}}
   220	    private void styleTable(JTable table) { /* ... */ table.setBackground(TABLE_CELL_BG); table.setForeground(TABLE_CELL_FG); table.setGridColor(TABLE_GRID_COLOR); table.setRowHeight(28); table.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,13)); table.setFillsViewportHeight(true); table.setSelectionBackground(TABLE_CELL_SELECTED_BG); table.setSelectionForeground(TABLE_CELL_SELECTED_FG); table.setShowGrid(true); table.setIntercellSpacing(new Dimension(0,1)); JTableHeader h=table.getTableHeader(); h.setBackground(TABLE_HEADER_BG); h.setForeground(TABLE_HEADER_FG); h.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14)); h.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); h.setReorderingAllowed(true); h.setResizingAllowed(true); DefaultTableCellRenderer r=new DefaultTableCellRenderer(); r.setHorizontalAlignment(SwingConstants.LEFT); r.setVerticalAlignment(SwingConstants.CENTER); r.setBorder(new EmptyBorder(2,5,2,5)); for(int i=0;i<table.getColumnCount()-1;i++){table.getColumnModel().getColumn(i).setCellRenderer(r);} }
   221	    private void styleScrollPane(JScrollPane scrollPane) { /* ... */ scrollPane.setOpaque(false); scrollPane.getViewport().setOpaque(false); scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); applyScrollbarUI(scrollPane.getVerticalScrollBar()); applyScrollbarUI(scrollPane.getHorizontalScrollBar()); }
   222	    private void applyScrollbarUI(JScrollBar scrollBar) { /* ... */ scrollBar.setUI(new BasicScrollBarUI() { @Override protected void configureScrollBarColors(){this.thumbColor=BUTTON_BG_DARK; this.trackColor=DARK_BG_END;} @Override protected JButton createDecreaseButton(int o){return createZeroButton();} @Override protected JButton createIncreaseButton(int o){return createZeroButton();} private JButton createZeroButton(){JButton b=new JButton(); b.setPreferredSize(new Dimension(0,0)); b.setMaximumSize(new Dimension(0,0)); b.setMinimumSize(new Dimension(0,0));return b;} @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r){g.setColor(thumbColor);g.fillRect(r.x,r.y,r.width,r.height);} @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r){g.setColor(trackColor);g.fillRect(r.x,r.y,r.width,r.height);} }); scrollBar.setUnitIncrement(16); }
   223	    private void styleActionButton(JButton btn, String tooltip) { /* ... */ btn.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12)); btn.setToolTipText(tooltip); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setForeground(BUTTON_FG_DARK); btn.setFocusPainted(false); btn.setBackground(BUTTON_BG_DARK); Border p=new EmptyBorder(6,12,6,12); btn.setBorder(new CompoundBorder(BorderFactory.createLineBorder(BUTTON_BG_DARK.darker()),p)); btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){if(btn.getBackground().equals(BUTTON_BG_DARK)){btn.setBackground(BUTTON_HOVER_BG_DARK);}}@Override public void mouseExited(MouseEvent e){if(btn.getBackground().equals(BUTTON_HOVER_BG_DARK)){btn.setBackground(BUTTON_BG_DARK);}}}); }
   224	
   225	    // --- Inner classes for Table Button (Unchanged) ---
   226	    static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer { /* ... */ private Color defaultBg; public ButtonRenderer(Color background){setOpaque(true);this.defaultBg=background;setForeground(BUTTON_FG_DARK);setBackground(defaultBg);setBorder(new EmptyBorder(2,5,2,5));setFont(new Font(Font.SANS_SERIF,Font.BOLD,11));} @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){setText((v==null)?"":v.toString());setBackground(s?defaultBg.brighter():defaultBg);return this;} }
   227	    static class ButtonEditor extends DefaultCellEditor { /* ... */ protected JButton button; private String label; private boolean isPushed; private Runnable action; private Color bgColor; public ButtonEditor(JCheckBox c,Color bg,Runnable act){super(c);this.action=act;this.bgColor=bg;button=new JButton();button.setOpaque(true);button.setForeground(BUTTON_FG_DARK);button.setBackground(bgColor);button.setBorder(new EmptyBorder(2,5,2,5));button.setFont(new Font(Font.SANS_SERIF,Font.BOLD,11));button.addActionListener(e->fireEditingStopped());} @Override public Component getTableCellEditorComponent(JTable t,Object v,boolean s,int r,int c){label=(v==null)?"":v.toString();button.setText(label);isPushed=true;return button;} @Override public Object getCellEditorValue(){if(isPushed&&action!=null){action.run();}isPushed=false;return label;} @Override public boolean stopCellEditing(){isPushed=false;return super.stopCellEditing();} @Override protected void fireEditingStopped(){super.fireEditingStopped();} }
   228	
   229	    // --- Integration Notes (Unchanged) ---
   230	}

════════════════════════════════════════════════════════════════════════════
▶ FILE: HomePanel.java (root)
▶ PATH: ./HomePanel.java
▶ SIZE: 216 lines
────────────────────────────────────────────────────────────────────────────

     1	package com.orphanagehub.gui;
     2	
     3	import javax.swing.*;
     4	import javax.swing.border.Border;
     5	import javax.swing.border.CompoundBorder;
     6	import javax.swing.border.EmptyBorder;
     7	import javax.swing.border.TitledBorder;
     8	import java.awt.*;
     9	import java.awt.event.MouseAdapter;
    10	import java.awt.event.MouseEvent;
    11	import java.awt.geom.Point2D; // Keep this import
    12	import java.net.URL;         // *** RE-ADDED for Image Loading ***
    13	
    14	public class HomePanel extends JPanel {
    15	
    16	    private OrphanageHubApp mainApp;
    17	    private JRadioButton rbDonor;
    18	    private JRadioButton rbStaff;
    19	    private JRadioButton rbVolunteer;
    20	    private ButtonGroup roleGroup;
    21	
    22	    // Define Colors for a Sleek Dark Theme
    23	    private static final Color DARK_BG_START = new Color(45, 52, 54);    // Dark Grey/Blue Start
    24	    private static final Color DARK_BG_END = new Color(35, 42, 44);      // Slightly Darker End
    25	    private static final Color TITLE_COLOR_DARK = new Color(223, 230, 233); // Light Grey for Titles
    26	    private static final Color TEXT_COLOR_DARK = new Color(200, 200, 200); // Slightly dimmer Grey for Text
    27	    private static final Color BORDER_COLOR_DARK = new Color(80, 80, 80);    // Darker Border
    28	    private static final Color BUTTON_BG_DARK = new Color(99, 110, 114);    // Muted Grey/Blue Button
    29	    private static final Color BUTTON_FG_DARK = Color.WHITE;
    30	    private static final Color BUTTON_HOVER_BG_DARK = new Color(120, 130, 134); // Lighter Hover
    31	    private static final Color FALLBACK_BG_DARK = new Color(60, 60, 60);     // Background for image fallback
    32	
    33	    public HomePanel(OrphanageHubApp app) {
    34	        this.mainApp = app;
    35	        setBorder(new EmptyBorder(30, 40, 30, 40));
    36	        setLayout(new BorderLayout(20, 20));
    37	        initComponents();
    38	    }
    39	
    40	    @Override
    41	    protected void paintComponent(Graphics g) {
    42	        super.paintComponent(g);
    43	        Graphics2D g2d = (Graphics2D) g;
    44	        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    45	        GradientPaint gp = new GradientPaint(0, 0, DARK_BG_START, 0, getHeight(), DARK_BG_END);
    46	        g2d.setPaint(gp);
    47	        g2d.fillRect(0, 0, getWidth(), getHeight());
    48	    }
    49	
    50	    private void initComponents() {
    51	
    52	        // --- North: Title ---
    53	        JLabel lblTitle = new JLabel("Welcome to OrphanageHub", SwingConstants.CENTER);
    54	        lblTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
    55	        lblTitle.setForeground(TITLE_COLOR_DARK);
    56	        lblTitle.setBorder(new EmptyBorder(0, 0, 25, 0));
    57	        add(lblTitle, BorderLayout.NORTH);
    58	
    59	        // --- Center: Image and Description ---
    60	        JPanel centerPanel = new JPanel(new BorderLayout(30, 0)); // Gap between image and text
    61	        centerPanel.setOpaque(false); // Crucial: Make center panel transparent
    62	
    63	        // *** Image Loading and Styling (Reintroduced) ***
    64	        JLabel lblImage = new JLabel();
    65	        Dimension imageSize = new Dimension(220, 220); // Define image size
    66	        lblImage.setPreferredSize(imageSize);
    67	        lblImage.setMinimumSize(imageSize); // Prevent shrinking
    68	        lblImage.setMaximumSize(imageSize); // Prevent expanding
    69	        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
    70	        lblImage.setVerticalAlignment(SwingConstants.CENTER);
    71	        lblImage.setOpaque(false); // Image label itself is transparent
    72	
    73	        URL imageURL = getClass().getResource("home.png"); // Load image relative to class file
    74	        if (imageURL != null) {
    75	            try {
    76	                ImageIcon icon = new ImageIcon(imageURL);
    77	                if (icon.getImageLoadStatus() == MediaTracker.COMPLETE && icon.getIconWidth() > 0) {
    78	                    Image img = icon.getImage().getScaledInstance(imageSize.width, imageSize.height, Image.SCALE_SMOOTH);
    79	                    lblImage.setIcon(new ImageIcon(img));
    80	                    // Add a subtle border *only* if image loads successfully
    81	                    lblImage.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK, 1));
    82	                } else {
    83	                    throw new Exception("Image loading failed or width is zero.");
    84	                }
    85	            } catch (Exception e) {
    86	                 System.err.println("ERROR: Failed to load or scale home.png: " + e.getMessage());
    87	                 setFallbackImageStyleDark(lblImage); // Use dark theme fallback
    88	            }
    89	        } else {
    90	            System.err.println("Warning: home.png not found in classpath relative to HomePanel.class.");
    91	            setFallbackImageStyleDark(lblImage); // Use dark theme fallback
    92	        }
    93	        centerPanel.add(lblImage, BorderLayout.WEST); // Add image to the left
    94	
    95	        // Description Text
    96	        String htmlDesc = "<html><body style='width:350px; font-family: Sans-Serif; font-size: 14pt; color: rgb(200,200,200);'>" // Adjusted width
    97	                + "<p><b>A better world starts with care.</b></p>"
    98	                + "<p>OrphanageHub connects orphanages with the donors and volunteers needed "
    99	                + "to create lasting change for vulnerable children.</p>"
   100	                + "</body></html>";
   101	        JLabel lblDesc = new JLabel(htmlDesc);
   102	        lblDesc.setVerticalAlignment(SwingConstants.CENTER); // Center text vertically relative to image
   103	        lblDesc.setHorizontalAlignment(SwingConstants.LEFT);   // Align text left
   104	        lblDesc.setOpaque(false); // Make label transparent
   105	        lblDesc.setBorder(new EmptyBorder(0, 10, 0, 0)); // Add slight left padding for text
   106	        centerPanel.add(lblDesc, BorderLayout.CENTER); // Add description next to image
   107	
   108	        add(centerPanel, BorderLayout.CENTER); // Add the combined panel to main layout
   109	
   110	
   111	        // --- South: Role Selection and Actions --- (Structure remains the same)
   112	        JPanel southPanel = new JPanel(new BorderLayout(10, 20));
   113	        southPanel.setOpaque(false);
   114	
   115	        // Role Selection Panel
   116	        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
   117	        rolePanel.setOpaque(false);
   118	        TitledBorder titledBorder = BorderFactory.createTitledBorder(
   119	                BorderFactory.createLineBorder(BORDER_COLOR_DARK),
   120	                " Select Your Role ",
   121	                TitledBorder.CENTER,
   122	                TitledBorder.DEFAULT_POSITION,
   123	                new Font(Font.SANS_SERIF, Font.BOLD, 15),
   124	                TITLE_COLOR_DARK
   125	        );
   126	        rolePanel.setBorder(new CompoundBorder(titledBorder, new EmptyBorder(10, 10, 10, 10)));
   127	
   128	
   129	        rbDonor = new JRadioButton("Donor", true);
   130	        rbStaff = new JRadioButton("Orphanage Staff");
   131	        rbVolunteer = new JRadioButton("Volunteer");
   132	        styleRadioButton(rbDonor, "Select if you wish to donate or view needs.");
   133	        styleRadioButton(rbStaff, "Select if you manage an orphanage profile.");
   134	        styleRadioButton(rbVolunteer, "Select if you want to find volunteer opportunities.");
   135	
   136	        roleGroup = new ButtonGroup();
   137	        roleGroup.add(rbDonor);
   138	        roleGroup.add(rbStaff);
   139	        roleGroup.add(rbVolunteer);
   140	
   141	        rolePanel.add(rbDonor);
   142	        rolePanel.add(rbStaff);
   143	        rolePanel.add(rbVolunteer);
   144	        southPanel.add(rolePanel, BorderLayout.CENTER);
   145	
   146	        // Action Buttons Panel
   147	        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5));
   148	        buttonPanel.setOpaque(false);
   149	        buttonPanel.setBorder(new EmptyBorder(15, 0, 10, 0));
   150	
   151	        JButton btnLogin = new JButton("Login");
   152	        JButton btnRegister = new JButton("Register");
   153	        styleActionButton(btnLogin, "Proceed to login with your existing account.");
   154	        styleActionButton(btnRegister, "Create a new account based on your selected role.");
   155	
   156	        btnLogin.addActionListener(e -> mainApp.navigateTo(OrphanageHubApp.LOGIN_PANEL));
   157	        btnRegister.addActionListener(e -> mainApp.navigateTo(OrphanageHubApp.REGISTRATION_PANEL));
   158	
   159	        buttonPanel.add(btnLogin);
   160	        buttonPanel.add(btnRegister);
   161	        southPanel.add(buttonPanel, BorderLayout.SOUTH);
   162	
   163	        add(southPanel, BorderLayout.SOUTH);
   164	    }
   165	
   166	    // *** Fallback method adapted for dark theme ***
   167	    private void setFallbackImageStyleDark(JLabel label) {
   168	        label.setText("<html><div style='text-align: center; color: #AAAAAA;'>Image<br>Not Found<br>(home.png)</div></html>"); // Lighter grey text
   169	        label.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 14));
   170	        label.setForeground(new Color(170, 170, 170)); // Match text color in HTML
   171	        label.setBorder(BorderFactory.createDashedBorder(BORDER_COLOR_DARK, 5, 5)); // Use dark border color
   172	        label.setOpaque(true); // Make background visible for border
   173	        label.setBackground(FALLBACK_BG_DARK); // Dark background for placeholder
   174	    }
   175	
   176	
   177	    private void styleRadioButton(JRadioButton rb, String tooltip) {
   178	        rb.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
   179	        rb.setForeground(TEXT_COLOR_DARK);
   180	        rb.setOpaque(false);
   181	        rb.setToolTipText(tooltip);
   182	        rb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
   183	    }
   184	
   185	    private void styleActionButton(JButton btn, String tooltip) {
   186	        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
   187	        btn.setPreferredSize(new Dimension(130, 40));
   188	        btn.setToolTipText(tooltip);
   189	        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
   190	        btn.setBackground(BUTTON_BG_DARK);
   191	        btn.setForeground(BUTTON_FG_DARK);
   192	        btn.setFocusPainted(false);
   193	
   194	        Border line = BorderFactory.createLineBorder(BUTTON_BG_DARK.darker());
   195	        Border padding = new EmptyBorder(5, 15, 5, 15);
   196	        btn.setBorder(new CompoundBorder(line, padding));
   197	
   198	        btn.addMouseListener(new MouseAdapter() {
   199	            @Override
   200	            public void mouseEntered(MouseEvent evt) {
   201	                btn.setBackground(BUTTON_HOVER_BG_DARK);
   202	            }
   203	            @Override
   204	            public void mouseExited(MouseEvent evt) {
   205	                btn.setBackground(BUTTON_BG_DARK);
   206	            }
   207	        });
   208	    }
   209	
   210	    public String getSelectedRole() {
   211	        if (rbDonor.isSelected()) return "Donor";
   212	        if (rbStaff.isSelected()) return "OrphanageStaff";
   213	        if (rbVolunteer.isSelected()) return "Volunteer";
   214	        return "Unknown";
   215	    }
   216	}

════════════════════════════════════════════════════════════════════════════
▶ FILE: LoginPanel.java (root)
▶ PATH: ./LoginPanel.java
▶ SIZE: 151 lines
────────────────────────────────────────────────────────────────────────────

     1	package com.orphanagehub.gui;
     2	
     3	import javax.swing.*;
     4	import javax.swing.border.Border;
     5	import javax.swing.border.CompoundBorder;
     6	import javax.swing.border.EmptyBorder;
     7	import java.awt.*;
     8	import java.awt.event.MouseAdapter;
     9	import java.awt.event.MouseEvent;
    10	import java.awt.geom.Point2D; // Keep this import
    11	
    12	public class LoginPanel extends JPanel {
    13	
    14	    private OrphanageHubApp mainApp;
    15	    private JTextField txtUsername;
    16	    private JPasswordField txtPassword;
    17	
    18	    // --- Colors (Consider shared constants class) ---
    19	    private static final Color DARK_BG_START = new Color(45, 52, 54);
    20	    private static final Color DARK_BG_END = new Color(35, 42, 44);
    21	    private static final Color TITLE_COLOR_DARK = new Color(223, 230, 233);
    22	    private static final Color TEXT_COLOR_DARK = new Color(200, 200, 200);
    23	    private static final Color BORDER_COLOR_DARK = new Color(80, 80, 80);
    24	    private static final Color INPUT_BG_DARK = new Color(60, 60, 60);
    25	    private static final Color INPUT_FG_DARK = new Color(220, 220, 220);
    26	    private static final Color INPUT_BORDER_DARK = new Color(90, 90, 90);
    27	    private static final Color BUTTON_BG_DARK = new Color(99, 110, 114);
    28	    private static final Color BUTTON_FG_DARK = Color.WHITE;
    29	    private static final Color BUTTON_HOVER_BG_DARK = new Color(120, 130, 134);
    30	    private static final Color LINK_COLOR = new Color(100, 180, 255);
    31	
    32	    public LoginPanel(OrphanageHubApp app) {
    33	        this.mainApp = app;
    34	        setLayout(new GridBagLayout());
    35	        setBorder(new EmptyBorder(40, 60, 40, 60));
    36	        initComponents();
    37	    }
    38	
    39	    @Override
    40	    protected void paintComponent(Graphics g) {
    41	        super.paintComponent(g);
    42	        Graphics2D g2d = (Graphics2D) g;
    43	        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    44	        GradientPaint gp = new GradientPaint(0, 0, DARK_BG_START, 0, getHeight(), DARK_BG_END);
    45	        g2d.setPaint(gp);
    46	        g2d.fillRect(0, 0, getWidth(), getHeight());
    47	    }
    48	
    49	    private void initComponents() {
    50	        GridBagConstraints gbc = new GridBagConstraints();
    51	        gbc.fill = GridBagConstraints.HORIZONTAL;
    52	        gbc.insets = new Insets(5, 5, 5, 5);
    53	
    54	        // --- Title ---
    55	        JLabel lblTitle = new JLabel("User Login", SwingConstants.CENTER);
    56	        lblTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
    57	        lblTitle.setForeground(TITLE_COLOR_DARK);
    58	        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.insets = new Insets(0, 5, 25, 5);
    59	        add(lblTitle, gbc);
    60	        gbc.gridwidth = 1; gbc.insets = new Insets(8, 5, 8, 5);
    61	
    62	        // --- Username ---
    63	        JLabel lblUsername = new JLabel("Username:"); styleFormLabel(lblUsername);
    64	        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    65	        add(lblUsername, gbc);
    66	        txtUsername = new JTextField(20); styleTextField(txtUsername);
    67	        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    68	        add(txtUsername, gbc);
    69	
    70	        // --- Password ---
    71	        JLabel lblPassword = new JLabel("Password:"); styleFormLabel(lblPassword);
    72	        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
    73	        add(lblPassword, gbc);
    74	        txtPassword = new JPasswordField(20); styleTextField(txtPassword);
    75	        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    76	        add(txtPassword, gbc);
    77	
    78	        // --- Login Button ---
    79	        JButton btnLogin = new JButton("Login"); styleActionButton(btnLogin, "Authenticate and access your dashboard");
    80	        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER; gbc.insets = new Insets(20, 5, 10, 5);
    81	        add(btnLogin, gbc);
    82	
    83	        // --- Links Panel ---
    84	        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0)); linksPanel.setOpaque(false);
    85	        JLabel lblForgotPassword = createHyperlinkLabel("Forgot Password?"); lblForgotPassword.setToolTipText("Click here to reset your password");
    86	        lblForgotPassword.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { JOptionPane.showMessageDialog(LoginPanel.this, "Password reset functionality not yet implemented.", "Forgot Password", JOptionPane.INFORMATION_MESSAGE); }});
    87	        JLabel lblRegister = createHyperlinkLabel("Need an account? Register"); lblRegister.setToolTipText("Click here to go to the registration page");
    88	        lblRegister.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { mainApp.navigateTo(OrphanageHubApp.REGISTRATION_PANEL); }});
    89	        linksPanel.add(lblForgotPassword); linksPanel.add(lblRegister);
    90	        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.insets = new Insets(5, 5, 15, 5);
    91	        add(linksPanel, gbc);
    92	
    93	        // --- Back Button ---
    94	        JButton btnBack = new JButton("Back"); styleActionButton(btnBack, "Return to the welcome screen"); btnBack.setBackground(BUTTON_BG_DARK.darker());
    95	        btnBack.addMouseListener(new MouseAdapter() { @Override public void mouseEntered(MouseEvent e) { btnBack.setBackground(BUTTON_HOVER_BG_DARK); } @Override public void mouseExited(MouseEvent e) { btnBack.setBackground(BUTTON_BG_DARK.darker()); }});
    96	        btnBack.addActionListener(e -> mainApp.navigateTo(OrphanageHubApp.HOME_PANEL));
    97	        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.insets = new Insets(10, 5, 5, 5);
    98	        add(btnBack, gbc);
    99	
   100	        // *** FULLY UPDATED Action Listener for Login Button ***
   101	        btnLogin.addActionListener(e -> {
   102	            String username = txtUsername.getText().trim(); // Trim input
   103	            String password = new String(txtPassword.getPassword());
   104	
   105	            // --- Real authentication logic will replace this block ---
   106	            boolean loginSuccess = false;
   107	            String targetDashboard = OrphanageHubApp.HOME_PANEL; // Default fallback
   108	
   109	            // Placeholder Credentials Check (CASE-SENSITIVE)
   110	            if (username.equals("staff") && password.equals("pass")) {
   111	                loginSuccess = true;
   112	                targetDashboard = OrphanageHubApp.ORPHANAGE_DASHBOARD_PANEL;
   113	                System.out.println("Attempting login for Staff...");
   114	            } else if (username.equals("donor") && password.equals("pass")) {
   115	                loginSuccess = true;
   116	                targetDashboard = OrphanageHubApp.DONOR_DASHBOARD_PANEL;
   117	                System.out.println("Attempting login for Donor...");
   118	            } else if (username.equals("volunteer") && password.equals("pass")) {
   119	                loginSuccess = true;
   120	                targetDashboard = OrphanageHubApp.VOLUNTEER_DASHBOARD_PANEL;
   121	                System.out.println("Attempting login for Volunteer...");
   122	            } else if (username.equals("admin") && password.equals("pass")) {
   123	                loginSuccess = true;
   124	                targetDashboard = OrphanageHubApp.ADMIN_DASHBOARD_PANEL;
   125	                System.out.println("Attempting login for Admin...");
   126	            }
   127	            // --- End of placeholder logic ---
   128	
   129	            if (loginSuccess) {
   130	                System.out.println("Login Success! Target: " + targetDashboard);
   131	                // Use showDashboard for all dashboard panels
   132	                mainApp.showDashboard(targetDashboard);
   133	            } else {
   134	                System.out.println("Login Failed for user: " + username);
   135	                // Provide more helpful hint including all placeholder users
   136	                JOptionPane.showMessageDialog(LoginPanel.this,
   137	                        "Invalid Username or Password.\n(Hints: staff/pass, donor/pass, volunteer/pass, admin/pass)",
   138	                        "Login Failed", JOptionPane.ERROR_MESSAGE);
   139	                txtPassword.setText(""); // Clear password field
   140	                txtUsername.requestFocusInWindow(); // Focus username field
   141	            }
   142	        });
   143	        // *** END OF UPDATED Action Listener ***
   144	    }
   145	
   146	    // --- Styling Helper Methods (Unchanged) ---
   147	    private void styleFormLabel(JLabel label) { label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14)); label.setForeground(TEXT_COLOR_DARK); }
   148	    private void styleTextField(JComponent field) { field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14)); field.setForeground(INPUT_FG_DARK); field.setBackground(INPUT_BG_DARK); Border p=new EmptyBorder(5,8,5,8); field.setBorder(new CompoundBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK,1),p)); if(field instanceof JTextField)((JTextField)field).setCaretColor(Color.LIGHT_GRAY); else if(field instanceof JPasswordField)((JPasswordField)field).setCaretColor(Color.LIGHT_GRAY); }
   149	    private JLabel createHyperlinkLabel(String text) { JLabel l=new JLabel("<html><u>"+text+"</u></html>"); l.setForeground(LINK_COLOR); l.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12)); l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return l; }
   150	    private void styleActionButton(JButton btn, String tooltip) { btn.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14)); btn.setPreferredSize(new Dimension(130,40)); btn.setToolTipText(tooltip); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setBackground(BUTTON_BG_DARK); btn.setForeground(BUTTON_FG_DARK); btn.setFocusPainted(false); Border l=BorderFactory.createLineBorder(BUTTON_BG_DARK.darker()); Border p=new EmptyBorder(5,15,5,15); btn.setBorder(new CompoundBorder(l,p)); btn.addMouseListener(new MouseAdapter() { @Override public void mouseEntered(MouseEvent e){if(btn.getBackground().equals(BUTTON_BG_DARK)){btn.setBackground(BUTTON_HOVER_BG_DARK);}} @Override public void mouseExited(MouseEvent e){if(btn.getBackground().equals(BUTTON_HOVER_BG_DARK)){btn.setBackground(BUTTON_BG_DARK);}} }); }
   151	}

════════════════════════════════════════════════════════════════════════════
▶ FILE: OrphanageDashboardPanel.java (root)
▶ PATH: ./OrphanageDashboardPanel.java
▶ SIZE: 407 lines
────────────────────────────────────────────────────────────────────────────

     1	package com.orphanagehub.gui;
     2	
     3	import javax.swing.*;
     4	import javax.swing.border.Border;
     5	import javax.swing.border.CompoundBorder;
     6	import javax.swing.border.EmptyBorder;
     7	import javax.swing.plaf.basic.BasicScrollBarUI; // For scrollbar styling
     8	import javax.swing.table.DefaultTableCellRenderer; // For table cell styling
     9	import javax.swing.table.JTableHeader; // For table header styling
    10	import java.awt.*;
    11	import java.awt.event.MouseAdapter;
    12	import java.awt.event.MouseEvent;
    13	import java.awt.geom.Point2D;
    14	
    15	// --- NOTE: This code is identical to the previous step ---
    16	// --- It is included here only for completeness      ---
    17	
    18	public class OrphanageDashboardPanel extends JPanel {
    19	
    20	    private OrphanageHubApp mainApp;
    21	    private String orphanageName = "Hope Children's Home"; // Placeholder
    22	    private String staffUsername = "staff_user"; // Placeholder
    23	
    24	    // Define Colors (Consider moving to a shared constants class/interface)
    25	    private static final Color DARK_BG_START = new Color(45, 52, 54);
    26	    private static final Color DARK_BG_END = new Color(35, 42, 44);
    27	    private static final Color TITLE_COLOR_DARK = new Color(223, 230, 233);
    28	    private static final Color TEXT_COLOR_DARK = new Color(200, 200, 200);
    29	    private static final Color BORDER_COLOR_DARK = new Color(80, 80, 80);
    30	    private static final Color BUTTON_BG_DARK = new Color(99, 110, 114);
    31	    private static final Color BUTTON_FG_DARK = Color.WHITE;
    32	    private static final Color BUTTON_HOVER_BG_DARK = new Color(120, 130, 134);
    33	    private static final Color TAB_BG_SELECTED = new Color(70, 80, 82); // Slightly lighter for selected tab
    34	    private static final Color TAB_BG_UNSELECTED = new Color(55, 62, 64);
    35	    private static final Color TAB_FG = TITLE_COLOR_DARK;
    36	    private static final Color TABLE_HEADER_BG = new Color(65, 75, 77);
    37	    private static final Color TABLE_HEADER_FG = TITLE_COLOR_DARK;
    38	    private static final Color TABLE_GRID_COLOR = BORDER_COLOR_DARK;
    39	    private static final Color TABLE_CELL_BG = new Color(55, 62, 64);
    40	    private static final Color TABLE_CELL_FG = TEXT_COLOR_DARK;
    41	    private static final Color TABLE_CELL_SELECTED_BG = BUTTON_BG_DARK;
    42	    private static final Color TABLE_CELL_SELECTED_FG = BUTTON_FG_DARK;
    43	    private static final Color ACCENT_COLOR_ORANGE = new Color(230, 145, 56); // Accent for stats
    44	    private static final Color ACCENT_COLOR_BLUE = new Color(72, 149, 239);
    45	    private static final Color ACCENT_COLOR_GREEN = new Color(87, 190, 106);
    46	
    47	
    48	    public OrphanageDashboardPanel(OrphanageHubApp app) {
    49	        this.mainApp = app;
    50	        setLayout(new BorderLayout(0, 0)); // No gaps for seamless gradient
    51	        initComponents();
    52	    }
    53	
    54	    @Override
    55	    protected void paintComponent(Graphics g) {
    56	        super.paintComponent(g);
    57	        Graphics2D g2d = (Graphics2D) g;
    58	        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    59	        GradientPaint gp = new GradientPaint(0, 0, DARK_BG_START, 0, getHeight(), DARK_BG_END);
    60	        g2d.setPaint(gp);
    61	        g2d.fillRect(0, 0, getWidth(), getHeight());
    62	    }
    63	
    64	    private void initComponents() {
    65	        // --- Header Panel ---
    66	        JPanel headerPanel = createHeaderPanel();
    67	        add(headerPanel, BorderLayout.NORTH);
    68	
    69	        // --- Tabbed Pane for Content ---
    70	        JTabbedPane tabbedPane = createTabbedPane();
    71	        add(tabbedPane, BorderLayout.CENTER);
    72	    }
    73	
    74	    // --- Helper Methods ---
    75	
    76	    private JPanel createHeaderPanel() {
    77	        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
    78	        headerPanel.setOpaque(false); // Show gradient background
    79	        headerPanel.setBorder(new CompoundBorder(
    80	                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR_DARK), // Bottom border
    81	                new EmptyBorder(10, 20, 10, 20) // Padding
    82	        ));
    83	
    84	        // Left side: Orphanage Name and Role Icon
    85	        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    86	        titleGroup.setOpaque(false);
    87	        JLabel iconLabel = new JLabel("\u2302"); // House symbol
    88	        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
    89	        iconLabel.setForeground(new Color(135, 206, 250)); // Light Sky Blue (match registration)
    90	        JLabel nameLabel = new JLabel(orphanageName); // Placeholder name
    91	        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
    92	        nameLabel.setForeground(TITLE_COLOR_DARK);
    93	        titleGroup.add(iconLabel);
    94	        titleGroup.add(nameLabel);
    95	        headerPanel.add(titleGroup, BorderLayout.WEST);
    96	
    97	        // Right side: User info and Logout Button
    98	        JPanel userGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
    99	        userGroup.setOpaque(false);
   100	        JLabel userLabel = new JLabel("User: " + staffUsername); // Placeholder user
   101	        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
   102	        userLabel.setForeground(TEXT_COLOR_DARK);
   103	        JButton btnLogout = new JButton("Logout");
   104	        styleActionButton(btnLogout, "Logout and return to welcome screen");
   105	        btnLogout.setPreferredSize(new Dimension(100, 30)); // Smaller button
   106	        btnLogout.setBackground(new Color(192, 57, 43)); // Reddish logout button
   107	        btnLogout.addMouseListener(new MouseAdapter() { // Custom hover/exit for logout
   108	             @Override public void mouseEntered(MouseEvent e) { btnLogout.setBackground(new Color(231, 76, 60)); }
   109	             @Override public void mouseExited(MouseEvent e) { btnLogout.setBackground(new Color(192, 57, 43)); }
   110	        });
   111	        btnLogout.addActionListener(e -> {
   112	            // Placeholder: Add confirmation dialog?
   113	            mainApp.navigateTo(OrphanageHubApp.HOME_PANEL);
   114	        });
   115	        userGroup.add(userLabel);
   116	        userGroup.add(btnLogout);
   117	        headerPanel.add(userGroup, BorderLayout.EAST);
   118	
   119	        return headerPanel;
   120	    }
   121	
   122	    private JTabbedPane createTabbedPane() {
   123	        JTabbedPane tabbedPane = new JTabbedPane();
   124	        tabbedPane.setOpaque(false); // Show gradient through tab area background
   125	        tabbedPane.setForeground(TAB_FG); // Text color for tabs
   126	        tabbedPane.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
   127	
   128	        // Apply custom UI for tab styling (more control than basic setBackground/Foreground)
   129	        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
   130	            @Override
   131	            protected void installDefaults() {
   132	                super.installDefaults();
   133	                // Use defined colors
   134	                lightHighlight = TAB_BG_SELECTED; // Color for selected tab border top/left
   135	                shadow = BORDER_COLOR_DARK;      // Color for unselected tab border bottom/right
   136	                darkShadow = DARK_BG_END;        // Outer border color maybe?
   137	                focus = TAB_BG_SELECTED;         // Focus indicator color
   138	            }
   139	
   140	            @Override
   141	            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
   142	                g.setColor(isSelected ? TAB_BG_SELECTED : TAB_BG_UNSELECTED);
   143	                // Paint a slightly rounded rectangle for the tab background
   144	                 switch (tabPlacement) {
   145	                    case TOP:
   146	                    default:
   147	                        g.fillRoundRect(x, y, w, h + 5, 5, 5); // Extend height slightly for overlap look
   148	                        break;
   149	                    // Add cases for other placements if needed
   150	                 }
   151	            }
   152	
   153	             @Override
   154	             protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
   155	                 // Don't paint the default border, or paint a minimal one
   156	                 g.setColor(BORDER_COLOR_DARK);
   157	                  switch (tabPlacement) {
   158	                    case TOP:
   159	                    default:
   160	                         if (isSelected) {
   161	                             // No border needed for selected? Or just bottom?
   162	                             // g.drawLine(x, y + h, x + w, y + h); // Bottom line only for selected
   163	                         } else {
   164	                              // Maybe a top line for unselected?
   165	                              // g.drawLine(x, y, x + w -1 , y);
   166	                         }
   167	                         break;
   168	                  }
   169	             }
   170	
   171	            @Override
   172	            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
   173	                 // Paint a border around the content area to match tabs
   174	                 int width = tabPane.getWidth();
   175	                 int height = tabPane.getHeight();
   176	                 Insets insets = tabPane.getInsets();
   177	                 // Insets tabAreaInsets = getTabAreaInsets(tabPlacement); // Not needed directly
   178	
   179	                 int tabAreaHeight = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
   180	                 int x = insets.left;
   181	                 // Adjusted y calculation based on how BasicTabbedPaneUI calculates content border y
   182	                 int y = insets.top + tabAreaHeight - (lightHighlight.getAlpha() > 0 ? 1 : 0); // Approximate adjustment
   183	                 int w = width - insets.right - insets.left;
   184	                 int h = height - insets.top - insets.bottom - y;
   185	
   186	                 g.setColor(BORDER_COLOR_DARK); // Use border color
   187	                 g.drawRect(x, y, w - 1, h - 1); // Draw border around content
   188	            }
   189	        });
   190	
   191	
   192	        // Create and add tabs
   193	        tabbedPane.addTab("Overview", createOverviewTab());
   194	        tabbedPane.addTab("Resource Requests", createResourceRequestsTab());
   195	        tabbedPane.addTab("Donations", createPlaceholderTab("Donations Management"));
   196	        tabbedPane.addTab("Volunteers", createPlaceholderTab("Volunteer Management"));
   197	        tabbedPane.addTab("Orphanage Profile", createPlaceholderTab("Orphanage Profile Editor"));
   198	
   199	        return tabbedPane;
   200	    }
   201	
   202	    private JPanel createOverviewTab() {
   203	        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 20)); // Grid for stat cards
   204	        panel.setOpaque(false);
   205	        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
   206	
   207	        panel.add(createStatCard("Active Requests", "12", ACCENT_COLOR_ORANGE));
   208	        panel.add(createStatCard("Pending Donations", "3", ACCENT_COLOR_BLUE));
   209	        panel.add(createStatCard("Active Volunteers", "8", ACCENT_COLOR_GREEN));
   210	
   211	        return panel;
   212	    }
   213	
   214	    private JPanel createStatCard(String title, String value, Color accentColor) {
   215	        JPanel card = new JPanel(new BorderLayout(5, 5));
   216	        card.setBackground(TAB_BG_UNSELECTED); // Use tab background
   217	        card.setBorder(new CompoundBorder(
   218	                BorderFactory.createMatteBorder(0, 0, 3, 0, accentColor), // Accent color bottom border
   219	                new EmptyBorder(15, 20, 15, 20) // Padding
   220	        ));
   221	
   222	        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
   223	        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
   224	        valueLabel.setForeground(TITLE_COLOR_DARK);
   225	        card.add(valueLabel, BorderLayout.CENTER);
   226	
   227	        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
   228	        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
   229	        titleLabel.setForeground(TEXT_COLOR_DARK);
   230	        card.add(titleLabel, BorderLayout.SOUTH);
   231	
   232	        return card;
   233	    }
   234	
   235	
   236	    private JPanel createResourceRequestsTab() {
   237	        JPanel panel = new JPanel(new BorderLayout(10, 10));
   238	        panel.setOpaque(false); // Let tab content area show background if needed
   239	        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
   240	
   241	        // --- Toolbar ---
   242	        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
   243	        toolbar.setOpaque(false);
   244	        JButton btnAdd = new JButton("Add Request");
   245	        JButton btnEdit = new JButton("Edit Selected");
   246	        JButton btnDelete = new JButton("Delete Selected");
   247	        styleActionButton(btnAdd, "Create a new resource request");
   248	        styleActionButton(btnEdit, "Modify the selected request");
   249	        styleActionButton(btnDelete, "Remove the selected request");
   250	        // Distinguish delete button maybe?
   251	        btnDelete.setBackground(new Color(192, 57, 43)); // Reddish
   252	        btnDelete.addMouseListener(new MouseAdapter() {
   253	            @Override public void mouseEntered(MouseEvent e) { btnDelete.setBackground(new Color(231, 76, 60)); }
   254	            @Override public void mouseExited(MouseEvent e) { btnDelete.setBackground(new Color(192, 57, 43)); }
   255	        });
   256	
   257	        toolbar.add(btnAdd);
   258	        toolbar.add(btnEdit);
   259	        toolbar.add(btnDelete);
   260	        panel.add(toolbar, BorderLayout.NORTH);
   261	
   262	        // --- Table ---
   263	        String[] columnNames = {"ID", "Category", "Description", "Needed", "Fulfilled", "Urgency", "Status"};
   264	        Object[][] data = { // Placeholder data
   265	                {"REQ001", "Food", "Rice (50kg bags)", 10, 4, "High", "Open"},
   266	                {"REQ002", "Clothing", "Winter jackets (S)", 15, 15, "Medium", "Fulfilled"},
   267	                {"REQ003", "Education", "Notebooks", 50, 20, "Low", "Open"},
   268	                {"REQ004", "Medical", "First Aid Kits", 5, 1, "High", "Open"},
   269	                {"REQ005", "Funding", "Roof Repair", 1, 0, "Urgent", "Open"}
   270	        };
   271	        JTable table = new JTable(data, columnNames);
   272	        styleTable(table); // Apply dark theme styling
   273	
   274	        JScrollPane scrollPane = new JScrollPane(table);
   275	        styleScrollPane(scrollPane); // Apply dark theme styling
   276	        panel.add(scrollPane, BorderLayout.CENTER);
   277	
   278	        return panel;
   279	    }
   280	
   281	    // Generic placeholder tab content
   282	    private JPanel createPlaceholderTab(String title) {
   283	        JPanel panel = new JPanel(new GridBagLayout()); // Use GBL to center content
   284	        panel.setOpaque(false);
   285	        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
   286	        JLabel label = new JLabel(title + " - Content Area", SwingConstants.CENTER);
   287	        label.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 18));
   288	        label.setForeground(TEXT_COLOR_DARK);
   289	        panel.add(label); // Add centered label
   290	        return panel;
   291	    }
   292	
   293	    // Helper method to style JTable for dark theme
   294	    private void styleTable(JTable table) {
   295	        table.setBackground(TABLE_CELL_BG);
   296	        table.setForeground(TABLE_CELL_FG);
   297	        table.setGridColor(TABLE_GRID_COLOR);
   298	        table.setRowHeight(28); // Increased row height
   299	        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
   300	        table.setFillsViewportHeight(true); // Table fills scrollpane height
   301	
   302	        // Selection colors
   303	        table.setSelectionBackground(TABLE_CELL_SELECTED_BG);
   304	        table.setSelectionForeground(TABLE_CELL_SELECTED_FG);
   305	
   306	        // Header styling
   307	        JTableHeader header = table.getTableHeader();
   308	        header.setBackground(TABLE_HEADER_BG);
   309	        header.setForeground(TABLE_HEADER_FG);
   310	        header.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
   311	        header.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); // Border for header
   312	        // Prevent column reordering/resizing (optional)
   313	        // header.setReorderingAllowed(false);
   314	        // header.setResizingAllowed(false);
   315	
   316	        // Cell renderer (optional - for padding or specific alignment)
   317	         DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
   318	         centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
   319	         DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
   320	         leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
   321	
   322	         table.getColumnModel().getColumn(0).setPreferredWidth(60); // ID
   323	         table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
   324	         table.getColumnModel().getColumn(1).setPreferredWidth(100); // Category
   325	         table.getColumnModel().getColumn(2).setPreferredWidth(250); // Description
   326	         table.getColumnModel().getColumn(3).setPreferredWidth(80); // Needed
   327	         table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
   328	         table.getColumnModel().getColumn(4).setPreferredWidth(80); // Fulfilled
   329	         table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
   330	         table.getColumnModel().getColumn(5).setPreferredWidth(100); // Urgency
   331	         table.getColumnModel().getColumn(6).setPreferredWidth(100); // Status
   332	    }
   333	
   334	    // Helper method to style JScrollPane for dark theme
   335	    private void styleScrollPane(JScrollPane scrollPane) {
   336	        scrollPane.setOpaque(false);
   337	        scrollPane.getViewport().setOpaque(false);
   338	        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); // Border for scrollpane
   339	
   340	        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
   341	        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
   342	
   343	        applyScrollbarUI(verticalScrollBar);
   344	        applyScrollbarUI(horizontalScrollBar);
   345	    }
   346	
   347	    // Helper to apply consistent scrollbar UI
   348	    private void applyScrollbarUI(JScrollBar scrollBar) {
   349	         scrollBar.setUI(new BasicScrollBarUI() {
   350	            @Override protected void configureScrollBarColors() {
   351	                this.thumbColor = BUTTON_BG_DARK;
   352	                this.trackColor = DARK_BG_END;
   353	                this.thumbDarkShadowColor = this.thumbColor.darker();
   354	                this.thumbHighlightColor = this.thumbColor.brighter();
   355	            }
   356	             @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
   357	             @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
   358	             private JButton createZeroButton() {
   359	                 JButton button = new JButton();
   360	                 button.setPreferredSize(new Dimension(0, 0));
   361	                 button.setMinimumSize(new Dimension(0, 0));
   362	                 button.setMaximumSize(new Dimension(0, 0));
   363	                 return button;
   364	             }
   365	             // Optional: Make thumb borderless or match thumb color
   366	             @Override protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
   367	                 g.setColor(thumbColor);
   368	                 g.fillRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);
   369	             }
   370	             // Optional: Make track match background more closely
   371	             @Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
   372	                 g.setColor(trackColor);
   373	                 g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
   374	             }
   375	        });
   376	        scrollBar.setUnitIncrement(16);
   377	    }
   378	
   379	
   380	    // Reusable action button styling method
   381	    private void styleActionButton(JButton btn, String tooltip) {
   382	        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12)); // Slightly smaller font for toolbar
   383	        btn.setToolTipText(tooltip);
   384	        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
   385	        btn.setForeground(BUTTON_FG_DARK);
   386	        btn.setFocusPainted(false);
   387	        btn.setBackground(BUTTON_BG_DARK); // Default background
   388	
   389	        // Padding inside button
   390	        Border padding = new EmptyBorder(6, 12, 6, 12);
   391	        btn.setBorder(new CompoundBorder(
   392	                BorderFactory.createLineBorder(BUTTON_BG_DARK.darker()), // Subtle border
   393	                padding));
   394	
   395	        // Default hover listener (can be overridden)
   396	        btn.addMouseListener(new MouseAdapter() {
   397	            @Override public void mouseEntered(MouseEvent e) {
   398	                if (!btn.getBackground().equals(new Color(192, 57, 43))) // Don't override delete/logout hover
   399	                    btn.setBackground(BUTTON_HOVER_BG_DARK);
   400	            }
   401	            @Override public void mouseExited(MouseEvent e) {
   402	                 if (!btn.getBackground().equals(new Color(192, 57, 43))) // Don't override delete/logout exit
   403	                    btn.setBackground(BUTTON_BG_DARK);
   404	            }
   405	        });
   406	    }
   407	}

════════════════════════════════════════════════════════════════════════════
▶ FILE: OrphanageHubApp.java (root)
▶ PATH: ./OrphanageHubApp.java
▶ SIZE: 154 lines
────────────────────────────────────────────────────────────────────────────

     1	package com.orphanagehub.gui;
     2	
     3	import javax.swing.*;
     4	import java.awt.*;
     5	
     6	public class OrphanageHubApp extends JFrame {
     7	
     8	    private CardLayout cardLayout;
     9	    private JPanel mainPanel;
    10	
    11	    // Panel Instances (keep references)
    12	    private HomePanel homePanel;
    13	    private LoginPanel loginPanel;
    14	    private RegistrationPanel registrationPanel;
    15	    private OrphanageDashboardPanel orphanageDashboardPanel;
    16	    private DonorDashboardPanel donorDashboardPanel;         // Added reference
    17	    private VolunteerDashboardPanel volunteerDashboardPanel; // Added reference
    18	    private AdminDashboardPanel adminDashboardPanel;         // Added reference
    19	
    20	    // Panel names for CardLayout
    21	    public static final String HOME_PANEL = "Home";
    22	    public static final String LOGIN_PANEL = "Login";
    23	    public static final String REGISTRATION_PANEL = "Registration";
    24	    public static final String ORPHANAGE_DASHBOARD_PANEL = "OrphanageDashboard";
    25	    public static final String DONOR_DASHBOARD_PANEL = "DonorDashboard";         // Added constant
    26	    public static final String VOLUNTEER_DASHBOARD_PANEL = "VolunteerDashboard"; // Added constant
    27	    public static final String ADMIN_DASHBOARD_PANEL = "AdminDashboard";         // Added constant
    28	
    29	    public OrphanageHubApp() {
    30	        super("OrphanageHub");
    31	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    32	
    33	        // Set Nimbus Look and Feel
    34	        try {
    35	            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
    36	                if ("Nimbus".equals(info.getName())) {
    37	                    UIManager.setLookAndFeel(info.getClassName());
    38	                    break;
    39	                }
    40	            }
    41	        } catch (Exception e) {
    42	            System.err.println("CRITICAL FAILURE: Cannot set Nimbus Look and Feel. UI may appear incorrect.");
    43	        }
    44	
    45	        initComponents(); // Initialize components and layout
    46	
    47	        // Set initial size
    48	        setPreferredSize(new Dimension(900, 700)); // Increased default size for dashboards
    49	        pack();
    50	        setMinimumSize(new Dimension(750, 550)); // Adjusted minimum size
    51	        setLocationRelativeTo(null);
    52	        setResizable(true);
    53	    }
    54	
    55	    private void initComponents() {
    56	        cardLayout = new CardLayout();
    57	        mainPanel = new JPanel(cardLayout);
    58	
    59	        // Instantiate CORE panels immediately
    60	        homePanel = new HomePanel(this);
    61	        loginPanel = new LoginPanel(this);
    62	        registrationPanel = new RegistrationPanel(this);
    63	        // Dashboard panels are instantiated on demand via showDashboard()
    64	
    65	        // Add core panels to the CardLayout container
    66	        mainPanel.add(homePanel, HOME_PANEL);
    67	        mainPanel.add(loginPanel, LOGIN_PANEL);
    68	        mainPanel.add(registrationPanel, REGISTRATION_PANEL);
    69	        // Dashboard panels are added later
    70	
    71	        setContentPane(mainPanel);
    72	    }
    73	
    74	    // --- Navigation Methods ---
    75	
    76	    /**
    77	     * Navigates directly to a panel already added to the CardLayout.
    78	     * @param panelName The name constant of the panel to show.
    79	     */
    80	    public void navigateTo(String panelName) {
    81	        System.out.println("Navigating to: " + panelName); // Debug
    82	        cardLayout.show(mainPanel, panelName);
    83	    }
    84	
    85	    /**
    86	     * Creates (if necessary) and navigates to a dashboard panel.
    87	     * Handles lazy instantiation of dashboard panels.
    88	     * @param panelName The name constant of the dashboard panel to show.
    89	     */
    90	    public void showDashboard(String panelName) {
    91	        System.out.println("Attempting to show dashboard: " + panelName); // Debug
    92	        boolean panelAdded = false; // Flag to track if a panel was added
    93	
    94	        // Ensure dashboard panels are created and added before showing
    95	        if (panelName.equals(ORPHANAGE_DASHBOARD_PANEL)) {
    96	            if (orphanageDashboardPanel == null) {
    97	                System.out.println("Creating Orphanage Dashboard Panel...");
    98	                orphanageDashboardPanel = new OrphanageDashboardPanel(this);
    99	                mainPanel.add(orphanageDashboardPanel, ORPHANAGE_DASHBOARD_PANEL);
   100	                panelAdded = true;
   101	            }
   102	            // Add logic later to pass actual user/orphanage data
   103	        } else if (panelName.equals(DONOR_DASHBOARD_PANEL)) {
   104	            if (donorDashboardPanel == null) {
   105	                System.out.println("Creating Donor Dashboard Panel...");
   106	                donorDashboardPanel = new DonorDashboardPanel(this);
   107	                mainPanel.add(donorDashboardPanel, DONOR_DASHBOARD_PANEL);
   108	                panelAdded = true;
   109	            }
   110	            // Add logic later to pass donor-specific data
   111	        } else if (panelName.equals(VOLUNTEER_DASHBOARD_PANEL)) {
   112	            if (volunteerDashboardPanel == null) {
   113	                System.out.println("Creating Volunteer Dashboard Panel...");
   114	                volunteerDashboardPanel = new VolunteerDashboardPanel(this);
   115	                mainPanel.add(volunteerDashboardPanel, VOLUNTEER_DASHBOARD_PANEL);
   116	                panelAdded = true;
   117	            }
   118	            // Add logic later to pass volunteer-specific data
   119	        } else if (panelName.equals(ADMIN_DASHBOARD_PANEL)) {
   120	            if (adminDashboardPanel == null) {
   121	                System.out.println("Creating Admin Dashboard Panel...");
   122	                adminDashboardPanel = new AdminDashboardPanel(this);
   123	                mainPanel.add(adminDashboardPanel, ADMIN_DASHBOARD_PANEL);
   124	                panelAdded = true;
   125	            }
   126	            // Add logic later to pass admin-specific data
   127	        } else {
   128	            System.err.println("Error: Attempted to show unknown or unsupported dashboard panel: " + panelName);
   129	            navigateTo(HOME_PANEL); // Fallback to home screen
   130	            return; // Exit early if panel name is invalid
   131	        }
   132	
   133	        // Revalidate the main panel *if* a new component was actually added
   134	        if (panelAdded) {
   135	            mainPanel.revalidate();
   136	            System.out.println(panelName + " Added and Revalidated.");
   137	        }
   138	
   139	        navigateTo(panelName); // Navigate to the requested panel
   140	    }
   141	
   142	    // Method for panels to get the selected role from HomePanel
   143	    public String getSelectedRole() {
   144	        return (homePanel != null) ? homePanel.getSelectedRole() : "Unknown";
   145	    }
   146	
   147	
   148	    public static void main(String[] args) {
   149	        SwingUtilities.invokeLater(() -> {
   150	            OrphanageHubApp app = new OrphanageHubApp();
   151	            app.setVisible(true);
   152	        });
   153	    }
   154	}

════════════════════════════════════════════════════════════════════════════
▶ FILE: RegistrationPanel.java (root)
▶ PATH: ./RegistrationPanel.java
▶ SIZE: 394 lines
────────────────────────────────────────────────────────────────────────────

     1	package com.orphanagehub.gui;
     2	
     3	import javax.swing.*;
     4	import javax.swing.border.Border;
     5	import javax.swing.border.CompoundBorder;
     6	import javax.swing.border.EmptyBorder;
     7	import javax.swing.border.LineBorder; // Use LineBorder explicitly
     8	import java.awt.*;
     9	import java.awt.event.MouseAdapter;
    10	import java.awt.event.MouseEvent;
    11	import java.awt.geom.Point2D; // Keep this import
    12	
    13	public class RegistrationPanel extends JPanel {
    14	
    15	    private OrphanageHubApp mainApp;
    16	    private String currentRole = "User"; // Default role
    17	
    18	    // Input Fields
    19	    private JTextField txtUsername;
    20	    private JTextField txtEmail;
    21	    private JTextField txtFullName;
    22	    private JPasswordField txtPassword;
    23	    private JPasswordField txtConfirmPassword;
    24	    private JComboBox<String> cmbOrphanage; // Conditional field
    25	    private JCheckBox chkTerms;
    26	
    27	    // Components that need updating based on role
    28	    private JLabel lblTitle;
    29	    private JLabel lblRoleIcon; // Placeholder for role icon
    30	    private JPanel orphanagePanel; // Panel holding the orphanage combo box
    31	
    32	    // Re-define colors (Consider a shared constants interface/class later)
    33	    private static final Color DARK_BG_START = new Color(45, 52, 54);
    34	    private static final Color DARK_BG_END = new Color(35, 42, 44);
    35	    private static final Color TITLE_COLOR_DARK = new Color(223, 230, 233);
    36	    private static final Color TEXT_COLOR_DARK = new Color(200, 200, 200);
    37	    private static final Color BORDER_COLOR_DARK = new Color(80, 80, 80);
    38	    private static final Color INPUT_BG_DARK = new Color(60, 60, 60);
    39	    private static final Color INPUT_FG_DARK = new Color(220, 220, 220);
    40	    private static final Color INPUT_BORDER_DARK = new Color(90, 90, 90);
    41	    private static final Color BUTTON_BG_DARK = new Color(99, 110, 114);
    42	    private static final Color BUTTON_FG_DARK = Color.WHITE;
    43	    private static final Color BUTTON_HOVER_BG_DARK = new Color(120, 130, 134);
    44	    private static final Color BUTTON_REGISTER_BG = new Color(60, 179, 113); // Medium Sea Green
    45	    private static final Color BUTTON_REGISTER_HOVER_BG = new Color(70, 190, 123);
    46	    private static final Color CHECKBOX_COLOR = new Color(180, 180, 180);
    47	
    48	    public RegistrationPanel(OrphanageHubApp app) {
    49	        this.mainApp = app;
    50	        setLayout(new BorderLayout()); // Main panel uses BorderLayout for scrollpane
    51	        // Don't set border here, set on the inner form panel
    52	        initComponents();
    53	    }
    54	
    55	    @Override
    56	    protected void paintComponent(Graphics g) {
    57	        super.paintComponent(g);
    58	        Graphics2D g2d = (Graphics2D) g;
    59	        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    60	        GradientPaint gp = new GradientPaint(0, 0, DARK_BG_START, 0, getHeight(), DARK_BG_END);
    61	        g2d.setPaint(gp);
    62	        g2d.fillRect(0, 0, getWidth(), getHeight());
    63	    }
    64	
    65	    private void initComponents() {
    66	        // Panel to hold the actual form elements using GridBagLayout
    67	        JPanel formPanel = new JPanel(new GridBagLayout());
    68	        formPanel.setOpaque(false); // Make form panel transparent
    69	        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30)); // Padding inside scroll pane
    70	
    71	        GridBagConstraints gbc = new GridBagConstraints();
    72	        gbc.fill = GridBagConstraints.HORIZONTAL;
    73	        gbc.insets = new Insets(5, 5, 5, 5);
    74	
    75	        // --- Title and Role Icon ---
    76	        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
    77	        titlePanel.setOpaque(false);
    78	
    79	        // Placeholder for Role Icon (using text symbol)
    80	        lblRoleIcon = new JLabel("?"); // Placeholder symbol
    81	        lblRoleIcon.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
    82	        lblRoleIcon.setForeground(TITLE_COLOR_DARK);
    83	        titlePanel.add(lblRoleIcon);
    84	
    85	        lblTitle = new JLabel("Register as " + currentRole); // Title updated in addNotify
    86	        lblTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
    87	        lblTitle.setForeground(TITLE_COLOR_DARK);
    88	        titlePanel.add(lblTitle);
    89	
    90	        gbc.gridx = 0;
    91	        gbc.gridy = 0;
    92	        gbc.gridwidth = 2;
    93	        gbc.weightx = 1.0;
    94	        gbc.insets = new Insets(0, 5, 20, 5); // Bottom margin
    95	        formPanel.add(titlePanel, gbc);
    96	
    97	        // Reset constraints for form fields
    98	        gbc.gridwidth = 1;
    99	        gbc.anchor = GridBagConstraints.EAST;
   100	        gbc.fill = GridBagConstraints.NONE;
   101	        gbc.weightx = 0;
   102	        gbc.insets = new Insets(6, 5, 6, 5); // Regular spacing
   103	
   104	        // --- Form Fields ---
   105	        int gridY = 1; // Start grid row counter
   106	
   107	        // Username
   108	        addFormField(formPanel, gbc, gridY++, "Username:", txtUsername = new JTextField(25));
   109	        // Email
   110	        addFormField(formPanel, gbc, gridY++, "Email:", txtEmail = new JTextField(25));
   111	        // Full Name
   112	        addFormField(formPanel, gbc, gridY++, "Full Name:", txtFullName = new JTextField(25));
   113	        // Password
   114	        addFormField(formPanel, gbc, gridY++, "Password:", txtPassword = new JPasswordField(25));
   115	        // Confirm Password
   116	        addFormField(formPanel, gbc, gridY++, "Confirm Password:", txtConfirmPassword = new JPasswordField(25));
   117	
   118	        // --- Conditional Orphanage Selection (for Staff) ---
   119	        orphanagePanel = new JPanel(new BorderLayout(5, 0)); // Use BorderLayout for label and combo
   120	        orphanagePanel.setOpaque(false);
   121	        JLabel lblOrphanage = new JLabel("Orphanage:");
   122	        styleFormLabel(lblOrphanage);
   123	        // Simulate orphanage list (replace with DB query later)
   124	        String[] orphanages = {"Select Orphanage...", "Hope Children's Home", "Bright Future Orphanage", "Little Angels Shelter"};
   125	        cmbOrphanage = new JComboBox<>(orphanages);
   126	        styleComboBox(cmbOrphanage); // Apply styling
   127	        orphanagePanel.add(lblOrphanage, BorderLayout.WEST);
   128	        orphanagePanel.add(cmbOrphanage, BorderLayout.CENTER);
   129	
   130	        gbc.gridx = 0;
   131	        gbc.gridy = gridY++; // Assign current gridY, then increment
   132	        gbc.gridwidth = 2; // Span both columns
   133	        gbc.fill = GridBagConstraints.HORIZONTAL;
   134	        formPanel.add(orphanagePanel, gbc);
   135	        orphanagePanel.setVisible(false); // Initially hidden
   136	
   137	        // --- Terms and Conditions Checkbox ---
   138	        chkTerms = new JCheckBox("I agree to the Terms of Service and Privacy Policy");
   139	        styleCheckbox(chkTerms);
   140	        gbc.gridx = 0;
   141	        gbc.gridy = gridY++;
   142	        gbc.gridwidth = 2;
   143	        gbc.anchor = GridBagConstraints.CENTER;
   144	        gbc.fill = GridBagConstraints.NONE;
   145	        gbc.insets = new Insets(15, 5, 15, 5);
   146	        formPanel.add(chkTerms, gbc);
   147	
   148	        // --- Action Buttons Panel ---
   149	        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
   150	        buttonPanel.setOpaque(false);
   151	
   152	        JButton btnRegister = new JButton("Register");
   153	        styleActionButton(btnRegister, "Create your account");
   154	        // Specific styling for primary action button
   155	        btnRegister.setBackground(BUTTON_REGISTER_BG);
   156	        btnRegister.addMouseListener(new MouseAdapter() { // Override hover for specific color
   157	             @Override public void mouseEntered(MouseEvent e) { btnRegister.setBackground(BUTTON_REGISTER_HOVER_BG); }
   158	             @Override public void mouseExited(MouseEvent e) { btnRegister.setBackground(BUTTON_REGISTER_BG); }
   159	        });
   160	        btnRegister.addActionListener(e -> registerAction()); // Placeholder action
   161	
   162	        JButton btnBack = new JButton("Back");
   163	        styleActionButton(btnBack, "Return to the welcome screen");
   164	        btnBack.setBackground(BUTTON_BG_DARK.darker()); // Keep Back button distinct
   165	         btnBack.addMouseListener(new MouseAdapter() { // Custom hover for Back button
   166	             @Override public void mouseEntered(MouseEvent e) { btnBack.setBackground(BUTTON_HOVER_BG_DARK); }
   167	             @Override public void mouseExited(MouseEvent e) { btnBack.setBackground(BUTTON_BG_DARK.darker()); }
   168	        });
   169	        btnBack.addActionListener(e -> mainApp.navigateTo(OrphanageHubApp.HOME_PANEL));
   170	
   171	        buttonPanel.add(btnRegister);
   172	        buttonPanel.add(btnBack);
   173	
   174	        gbc.gridx = 0;
   175	        gbc.gridy = gridY++;
   176	        gbc.gridwidth = 2;
   177	        gbc.anchor = GridBagConstraints.CENTER;
   178	        gbc.insets = new Insets(10, 5, 5, 5);
   179	        formPanel.add(buttonPanel, gbc);
   180	
   181	        // --- Scroll Pane Setup ---
   182	        JScrollPane scrollPane = new JScrollPane(formPanel);
   183	        scrollPane.setOpaque(false); // Show main panel gradient
   184	        scrollPane.getViewport().setOpaque(false); // Show main panel gradient
   185	        scrollPane.setBorder(null); // No border for the scroll pane itself
   186	        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // No horizontal scroll
   187	        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
   188	
   189	        // Optional: Style the scrollbar (can be Look and Feel dependent)
   190	        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
   191	        verticalScrollBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
   192	            @Override protected void configureScrollBarColors() {
   193	                this.thumbColor = BUTTON_BG_DARK; // Use button color for thumb
   194	                this.trackColor = DARK_BG_END;    // Use gradient end for track
   195	            }
   196	             @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
   197	             @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
   198	             private JButton createZeroButton() { // Hide arrow buttons
   199	                 JButton button = new JButton();
   200	                 button.setPreferredSize(new Dimension(0, 0));
   201	                 button.setMinimumSize(new Dimension(0, 0));
   202	                 button.setMaximumSize(new Dimension(0, 0));
   203	                 return button;
   204	             }
   205	        });
   206	        verticalScrollBar.setUnitIncrement(16); // Adjust scroll speed
   207	
   208	        // Add the scroll pane to the main RegistrationPanel
   209	        add(scrollPane, BorderLayout.CENTER);
   210	    }
   211	
   212	    // Helper to add label and field to the form panel
   213	    private void addFormField(JPanel panel, GridBagConstraints gbc, int gridY, String labelText, JComponent field) {
   214	        JLabel label = new JLabel(labelText);
   215	        styleFormLabel(label);
   216	        gbc.gridx = 0;
   217	        gbc.gridy = gridY;
   218	        gbc.anchor = GridBagConstraints.EAST;
   219	        gbc.fill = GridBagConstraints.NONE;
   220	        gbc.weightx = 0;
   221	        panel.add(label, gbc);
   222	
   223	        styleTextField(field); // Apply common styling
   224	        gbc.gridx = 1;
   225	        gbc.gridy = gridY;
   226	        gbc.anchor = GridBagConstraints.WEST;
   227	        gbc.fill = GridBagConstraints.HORIZONTAL;
   228	        gbc.weightx = 1.0;
   229	        panel.add(field, gbc);
   230	    }
   231	
   232	    // Helper to style form labels
   233	    private void styleFormLabel(JLabel label) {
   234	        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
   235	        label.setForeground(TEXT_COLOR_DARK);
   236	    }
   237	
   238	    // Helper to style text/password fields
   239	    private void styleTextField(JComponent field) {
   240	        field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
   241	        field.setForeground(INPUT_FG_DARK);
   242	        field.setBackground(INPUT_BG_DARK);
   243	        Border padding = new EmptyBorder(5, 8, 5, 8);
   244	        field.setBorder(new CompoundBorder(
   245	                BorderFactory.createLineBorder(INPUT_BORDER_DARK, 1),
   246	                padding
   247	        ));
   248	        if (field instanceof JTextField) ((JTextField) field).setCaretColor(Color.LIGHT_GRAY);
   249	        else if (field instanceof JPasswordField) ((JPasswordField) field).setCaretColor(Color.LIGHT_GRAY);
   250	    }
   251	
   252	     // Helper to style combo boxes
   253	    private void styleComboBox(JComboBox<?> comboBox) {
   254	        comboBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
   255	        comboBox.setForeground(INPUT_FG_DARK);
   256	        comboBox.setBackground(INPUT_BG_DARK);
   257	        // Border needs careful handling with ComboBox UI - simple line border might suffice
   258	        comboBox.setBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK, 1));
   259	        // Try to make dropdown match (highly L&F dependent)
   260	        Object popup = comboBox.getUI().getAccessibleChild(comboBox, 0);
   261	        if (popup instanceof JPopupMenu) {
   262	            JPopupMenu popupMenu = (JPopupMenu) popup;
   263	            popupMenu.setBorder(new LineBorder(BORDER_COLOR_DARK));
   264	            Component[] components = popupMenu.getComponents();
   265	             for (Component comp : components) { // Style the scroller and list within the popup
   266	                 if (comp instanceof JScrollPane) {
   267	                     JScrollPane scrollPane = (JScrollPane) comp;
   268	                     scrollPane.getViewport().setBackground(INPUT_BG_DARK);
   269	                     scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() { // Basic styling
   270	                         @Override protected void configureScrollBarColors() {this.thumbColor = BUTTON_BG_DARK; this.trackColor = DARK_BG_END;}
   271	                         @Override protected JButton createDecreaseButton(int o) { return createZeroButton(); }
   272	                         @Override protected JButton createIncreaseButton(int o) { return createZeroButton(); }
   273	                         private JButton createZeroButton() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
   274	                     });
   275	                     Component list = scrollPane.getViewport().getView();
   276	                     if (list instanceof JList) {
   277	                         ((JList<?>)list).setBackground(INPUT_BG_DARK);
   278	                         ((JList<?>)list).setForeground(INPUT_FG_DARK);
   279	                         ((JList<?>)list).setSelectionBackground(BUTTON_BG_DARK);
   280	                         ((JList<?>)list).setSelectionForeground(BUTTON_FG_DARK);
   281	                     }
   282	                 }
   283	             }
   284	        }
   285	    }
   286	
   287	    // Helper to style checkboxes
   288	    private void styleCheckbox(JCheckBox checkBox) {
   289	        checkBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
   290	        checkBox.setForeground(CHECKBOX_COLOR);
   291	        checkBox.setOpaque(false);
   292	        // Optional: could try to customize the check icon color if needed
   293	    }
   294	
   295	
   296	    // Adapted from LoginPanel - Consider moving to a utility class later
   297	    private void styleActionButton(JButton btn, String tooltip) {
   298	        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
   299	        btn.setPreferredSize(new Dimension(130, 40));
   300	        btn.setToolTipText(tooltip);
   301	        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
   302	        btn.setForeground(BUTTON_FG_DARK);
   303	        btn.setFocusPainted(false);
   304	
   305	        // Default background set here, can be overridden for specific buttons
   306	        btn.setBackground(BUTTON_BG_DARK);
   307	
   308	        Border line = BorderFactory.createLineBorder(BUTTON_BG_DARK.darker());
   309	        Border padding = new EmptyBorder(5, 15, 5, 15);
   310	        btn.setBorder(new CompoundBorder(line, padding));
   311	
   312	        // Default hover/exit listener (can be overridden for specific buttons)
   313	        btn.addMouseListener(new MouseAdapter() {
   314	            @Override
   315	            public void mouseEntered(MouseEvent evt) {
   316	                 if (!btn.getBackground().equals(BUTTON_REGISTER_BG)) // Don't override register button hover here
   317	                    btn.setBackground(BUTTON_HOVER_BG_DARK);
   318	            }
   319	            @Override
   320	            public void mouseExited(MouseEvent evt) {
   321	                 if (!btn.getBackground().equals(BUTTON_REGISTER_BG)) // Don't override register button exit here
   322	                    btn.setBackground(BUTTON_BG_DARK);
   323	            }
   324	        });
   325	    }
   326	
   327	    // Placeholder for registration logic
   328	    private void registerAction() {
   329	         // Simple validation example
   330	        if (txtUsername.getText().trim().isEmpty() ||
   331	            txtEmail.getText().trim().isEmpty() ||
   332	            new String(txtPassword.getPassword()).isEmpty()) {
   333	             JOptionPane.showMessageDialog(this, "Please fill in Username, Email, and Password.", "Registration Error", JOptionPane.ERROR_MESSAGE);
   334	             return;
   335	        }
   336	        if (!new String(txtPassword.getPassword()).equals(new String(txtConfirmPassword.getPassword()))) {
   337	             JOptionPane.showMessageDialog(this, "Passwords do not match.", "Registration Error", JOptionPane.ERROR_MESSAGE);
   338	             return;
   339	        }
   340	         if (currentRole.equals("OrphanageStaff") && cmbOrphanage.getSelectedIndex() <= 0) {
   341	             JOptionPane.showMessageDialog(this, "Orphanage Staff must select an orphanage.", "Registration Error", JOptionPane.ERROR_MESSAGE);
   342	             return;
   343	         }
   344	        if (!chkTerms.isSelected()) {
   345	            JOptionPane.showMessageDialog(this, "You must agree to the Terms of Service.", "Registration Error", JOptionPane.ERROR_MESSAGE);
   346	            return;
   347	        }
   348	
   349	        // Placeholder success message
   350	        JOptionPane.showMessageDialog(this,
   351	                "Registration attempt for " + txtUsername.getText() + " as " + currentRole + ".\n(Backend logic not implemented)",
   352	                "Registration Attempt", JOptionPane.INFORMATION_MESSAGE);
   353	
   354	        // Optionally navigate back home or to login after successful placeholder registration
   355	        // mainApp.navigateTo(OrphanageHubApp.LOGIN_PANEL);
   356	    }
   357	
   358	
   359	    // Override addNotify to update role-specific elements when panel becomes visible
   360	    @Override
   361	    public void addNotify() {
   362	        super.addNotify();
   363	        currentRole = mainApp.getSelectedRole(); // Get role selected on Home screen
   364	        lblTitle.setText("Register as " + currentRole);
   365	
   366	        // Update role icon placeholder text/symbol
   367	        switch (currentRole) {
   368	            case "Donor":
   369	                lblRoleIcon.setText("\u2764"); // Heavy Black Heart symbol
   370	                lblRoleIcon.setForeground(new Color(255, 105, 180)); // Pinkish
   371	                break;
   372	            case "OrphanageStaff":
   373	                lblRoleIcon.setText("\u2302"); // House symbol
   374	                lblRoleIcon.setForeground(new Color(135, 206, 250)); // Light Sky Blue
   375	                break;
   376	            case "Volunteer":
   377	                lblRoleIcon.setText("\u2605"); // Black Star symbol
   378	                lblRoleIcon.setForeground(new Color(255, 215, 0)); // Gold
   379	                 break;
   380	            default:
   381	                lblRoleIcon.setText("?");
   382	                lblRoleIcon.setForeground(TITLE_COLOR_DARK);
   383	                break;
   384	        }
   385	
   386	        // Show/hide orphanage selection based on role
   387	        boolean isStaff = currentRole.equals("OrphanageStaff");
   388	        orphanagePanel.setVisible(isStaff);
   389	
   390	        // Request layout update if visibility changed
   391	        revalidate();
   392	        repaint();
   393	    }
   394	}

════════════════════════════════════════════════════════════════════════════
▶ FILE: VolunteerDashboardPanel.java (root)
▶ PATH: ./VolunteerDashboardPanel.java
▶ SIZE: 252 lines
────────────────────────────────────────────────────────────────────────────

     1	package com.orphanagehub.gui;
     2	
     3	import javax.swing.*;
     4	import javax.swing.border.Border;
     5	import javax.swing.border.CompoundBorder;
     6	import javax.swing.border.EmptyBorder;
     7	import javax.swing.border.TitledBorder; // For potential status panel
     8	import javax.swing.plaf.basic.BasicComboBoxUI;
     9	import javax.swing.plaf.basic.BasicScrollBarUI;
    10	import javax.swing.table.DefaultTableCellRenderer;
    11	import javax.swing.table.JTableHeader;
    12	import java.awt.*;
    13	import java.awt.event.MouseAdapter;
    14	import java.awt.event.MouseEvent;
    15	import java.awt.geom.Point2D;
    16	
    17	public class VolunteerDashboardPanel extends JPanel {
    18	
    19	    private OrphanageHubApp mainApp;
    20	    private String volunteerUsername = "volunteer_user"; // Placeholder
    21	
    22	    // --- Colors (Same as AdminDashboardPanel) ---
    23	    private static final Color DARK_BG_START = new Color(45, 52, 54);
    24	    private static final Color DARK_BG_END = new Color(35, 42, 44);
    25	    private static final Color TITLE_COLOR_DARK = new Color(223, 230, 233);
    26	    private static final Color TEXT_COLOR_DARK = new Color(200, 200, 200);
    27	    private static final Color BORDER_COLOR_DARK = new Color(80, 80, 80);
    28	    private static final Color INPUT_BG_DARK = new Color(60, 60, 60);
    29	    private static final Color INPUT_FG_DARK = new Color(220, 220, 220);
    30	    private static final Color INPUT_BORDER_DARK = new Color(90, 90, 90);
    31	    private static final Color BUTTON_BG_DARK = new Color(99, 110, 114);
    32	    private static final Color BUTTON_FG_DARK = Color.WHITE;
    33	    private static final Color BUTTON_HOVER_BG_DARK = new Color(120, 130, 134);
    34	    private static final Color TABLE_HEADER_BG = new Color(65, 75, 77);
    35	    private static final Color TABLE_HEADER_FG = TITLE_COLOR_DARK;
    36	    private static final Color TABLE_GRID_COLOR = BORDER_COLOR_DARK;
    37	    private static final Color TABLE_CELL_BG = new Color(55, 62, 64);
    38	    private static final Color TABLE_CELL_FG = TEXT_COLOR_DARK;
    39	    private static final Color TABLE_CELL_SELECTED_BG = BUTTON_BG_DARK;
    40	    private static final Color TABLE_CELL_SELECTED_FG = BUTTON_FG_DARK;
    41	    private static final Color BUTTON_APPLY_BG = new Color(87, 190, 106); // Greenish apply button
    42	    private static final Color BUTTON_APPLY_HOVER_BG = new Color(97, 200, 116);
    43	
    44	
    45	    public VolunteerDashboardPanel(OrphanageHubApp app) {
    46	        this.mainApp = app;
    47	        setLayout(new BorderLayout(0, 0));
    48	        initComponents();
    49	    }
    50	
    51	    @Override
    52	    protected void paintComponent(Graphics g) {
    53	        super.paintComponent(g);
    54	        Graphics2D g2d = (Graphics2D) g;
    55	        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    56	        GradientPaint gp = new GradientPaint(0, 0, DARK_BG_START, 0, getHeight(), DARK_BG_END);
    57	        g2d.setPaint(gp);
    58	        g2d.fillRect(0, 0, getWidth(), getHeight());
    59	    }
    60	
    61	    private void initComponents() {
    62	        // --- Header Panel ---
    63	        JPanel headerPanel = createHeaderPanel();
    64	        add(headerPanel, BorderLayout.NORTH);
    65	
    66	        // --- Main Content Area (Search + Table + Status) ---
    67	        JPanel contentPanel = new JPanel(new BorderLayout(10, 15));
    68	        contentPanel.setOpaque(false);
    69	        contentPanel.setBorder(new EmptyBorder(15, 20, 20, 20));
    70	
    71	        // --- Search/Filter Panel ---
    72	        JPanel searchFilterPanel = createSearchFilterPanel();
    73	        contentPanel.add(searchFilterPanel, BorderLayout.NORTH);
    74	
    75	        // --- Opportunities Table ---
    76	        JTable opportunitiesTable = createOpportunitiesTable();
    77	        JScrollPane scrollPane = new JScrollPane(opportunitiesTable);
    78	        styleScrollPane(scrollPane);
    79	        contentPanel.add(scrollPane, BorderLayout.CENTER);
    80	
    81	        // --- Application Status Panel (Placeholder) ---
    82	        JPanel statusPanel = createStatusPanel();
    83	        contentPanel.add(statusPanel, BorderLayout.SOUTH);
    84	
    85	
    86	        add(contentPanel, BorderLayout.CENTER);
    87	    }
    88	
    89	    // --- Helper Methods ---
    90	
    91	    private JPanel createHeaderPanel() {
    92	        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
    93	        headerPanel.setOpaque(false);
    94	        headerPanel.setBorder(new CompoundBorder(
    95	                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR_DARK),
    96	                new EmptyBorder(10, 20, 10, 20)
    97	        ));
    98	
    99	        // Left side: Role Icon and Title
   100	        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
   101	        titleGroup.setOpaque(false);
   102	        JLabel iconLabel = new JLabel("\u2605"); // Star symbol (match registration)
   103	        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
   104	        iconLabel.setForeground(new Color(255, 215, 0)); // Gold color
   105	        JLabel nameLabel = new JLabel("Volunteer Dashboard");
   106	        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
   107	        nameLabel.setForeground(TITLE_COLOR_DARK);
   108	        titleGroup.add(iconLabel);
   109	        titleGroup.add(nameLabel);
   110	        headerPanel.add(titleGroup, BorderLayout.WEST);
   111	
   112	        // Right side: User info and Logout Button
   113	        JPanel userGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
   114	        userGroup.setOpaque(false);
   115	        JLabel userLabel = new JLabel("User: " + volunteerUsername);
   116	        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
   117	        userLabel.setForeground(TEXT_COLOR_DARK);
   118	        JButton btnLogout = new JButton("Logout");
   119	        styleActionButton(btnLogout, "Logout and return to welcome screen");
   120	        btnLogout.setPreferredSize(new Dimension(100, 30));
   121	        btnLogout.setBackground(new Color(192, 57, 43)); // Reddish logout
   122	        btnLogout.addMouseListener(new MouseAdapter() {
   123	             @Override public void mouseEntered(MouseEvent e) { btnLogout.setBackground(new Color(231, 76, 60)); }
   124	             @Override public void mouseExited(MouseEvent e) { btnLogout.setBackground(new Color(192, 57, 43)); }
   125	        });
   126	        btnLogout.addActionListener(e -> mainApp.navigateTo(OrphanageHubApp.HOME_PANEL));
   127	        userGroup.add(userLabel);
   128	        userGroup.add(btnLogout);
   129	        headerPanel.add(userGroup, BorderLayout.EAST);
   130	
   131	        return headerPanel;
   132	    }
   133	
   134	    private JPanel createSearchFilterPanel() {
   135	        // Similar structure to Donor search, different fields
   136	        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
   137	        panel.setOpaque(false);
   138	
   139	        JLabel lblFilterLocation = new JLabel("Location:");
   140	        styleFormLabel(lblFilterLocation);
   141	        String[] locations = {"Any Location", "City A", "City B", "Region C"}; // Placeholders
   142	        JComboBox<String> cmbLocation = new JComboBox<>(locations);
   143	        styleComboBox(cmbLocation);
   144	
   145	        JLabel lblFilterSkills = new JLabel("Skills:");
   146	        styleFormLabel(lblFilterSkills);
   147	        JTextField txtSkills = new JTextField(15); // Text field for skills keywords
   148	        styleTextField(txtSkills);
   149	
   150	        JLabel lblFilterTime = new JLabel("Commitment:");
   151	        styleFormLabel(lblFilterTime);
   152	        String[] times = {"Any Time", "Weekends", "Weekdays", "Flexible", "Event-Based"}; // Placeholders
   153	        JComboBox<String> cmbTime = new JComboBox<>(times);
   154	        styleComboBox(cmbTime);
   155	
   156	        JButton btnSearch = new JButton("Find Opportunities");
   157	        styleActionButton(btnSearch, "Search for volunteer roles matching criteria");
   158	        // Use default button style or a specific search color? Default for now.
   159	        btnSearch.addActionListener(e -> {
   160	             JOptionPane.showMessageDialog(this, "Search logic not implemented.", "Search", JOptionPane.INFORMATION_MESSAGE);
   161	        });
   162	
   163	        panel.add(lblFilterLocation);
   164	        panel.add(cmbLocation);
   165	        panel.add(Box.createHorizontalStrut(10));
   166	        panel.add(lblFilterSkills);
   167	        panel.add(txtSkills);
   168	        panel.add(Box.createHorizontalStrut(10));
   169	        panel.add(lblFilterTime);
   170	        panel.add(cmbTime);
   171	        panel.add(Box.createHorizontalStrut(15));
   172	        panel.add(btnSearch);
   173	
   174	        return panel;
   175	    }
   176	
   177	     private JTable createOpportunitiesTable() {
   178	        String[] columnNames = {"Orphanage", "Opportunity", "Location", "Skills Needed", "Time Commitment", "Action"};
   179	        Object[][] data = { // Placeholder data
   180	                {"Hope Children's Home", "Weekend Tutor", "City A", "Teaching, Patience", "Weekends", "Apply"},
   181	                {"Bright Future Orphanage", "Event Helper", "City B", "Organizing, Energetic", "Event-Based", "Apply"},
   182	                {"Little Angels Shelter", "After-School Care", "City A", "Childcare, First Aid", "Weekdays", "Applied"}, // Example status
   183	                {"Sunshine House", "Gardening Assistant", "Region C", "Gardening", "Flexible", "Apply"},
   184	                {"Hope Children's Home", "Reading Buddy", "City A", "Reading, Communication", "Weekdays", "Apply"}
   185	        };
   186	
   187	        JTable table = new JTable(data, columnNames) {
   188	             @Override
   189	             public boolean isCellEditable(int row, int column) {
   190	                 // Allow interaction only on the last column if the text is "Apply"
   191	                 return column == 5 && "Apply".equals(getValueAt(row, column));
   192	             }
   193	        };
   194	
   195	        styleTable(table);
   196	
   197	        // Add button renderer/editor for the "Action" column
   198	        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer(BUTTON_APPLY_BG));
   199	        // *** CORRECTED LAMBDA HERE (no 'e' parameter) ***
   200	        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox(), BUTTON_APPLY_BG, () -> { // Changed e -> () ->
   201	             int selectedRow = table.convertRowIndexToModel(table.getEditingRow());
   202	             String oppName = (String) table.getModel().getValueAt(selectedRow, 1);
   203	             String orphName = (String) table.getModel().getValueAt(selectedRow, 0);
   204	             JOptionPane.showMessageDialog(this, "Apply for: " + oppName + " at " + orphName + "\n(Functionality not implemented)", "Apply", JOptionPane.INFORMATION_MESSAGE);
   205	             // Ideally, update the cell value to "Applied" or "Pending" after successful action
   206	             // table.getModel().setValueAt("Applied", selectedRow, 5); // Requires DefaultTableModel
   207	         }));
   208	
   209	
   210	        // Adjust column widths
   211	        table.getColumnModel().getColumn(0).setPreferredWidth(150); // Orphanage
   212	        table.getColumnModel().getColumn(1).setPreferredWidth(150); // Opportunity
   213	        table.getColumnModel().getColumn(2).setPreferredWidth(100); // Location
   214	        table.getColumnModel().getColumn(3).setPreferredWidth(180); // Skills
   215	        table.getColumnModel().getColumn(4).setPreferredWidth(120); // Time
   216	        table.getColumnModel().getColumn(5).setPreferredWidth(90);  // Action
   217	
   218	        return table;
   219	    }
   220	
   221	    private JPanel createStatusPanel() {
   222	        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
   223	        panel.setOpaque(false);
   224	        panel.setBorder(new CompoundBorder(
   225	            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR_DARK), // Top border separator
   226	            new EmptyBorder(10, 5, 5, 5) // Padding
   227	        ));
   228	
   229	        JLabel lblStatus = new JLabel("Your Applications: 1 Pending (Little Angels Shelter)"); // Placeholder text
   230	        lblStatus.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 13));
   231	        lblStatus.setForeground(TEXT_COLOR_DARK);
   232	        panel.add(lblStatus);
   233	
   234	        return panel;
   235	    }
   236	
   237	
   238	    // --- Styling Helpers (Unchanged from previous version) ---
   239	    private void styleFormLabel(JLabel label) { /* ... */ label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); label.setForeground(TEXT_COLOR_DARK); }
   240	    private void styleTextField(JTextField field) { /* ... */ field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); field.setForeground(INPUT_FG_DARK); field.setBackground(INPUT_BG_DARK); Border p=new EmptyBorder(4,6,4,6); field.setBorder(new CompoundBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK,1),p)); field.setCaretColor(Color.LIGHT_GRAY); }
   241	    private void styleComboBox(JComboBox<?> comboBox) { /* ... */ comboBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); comboBox.setForeground(INPUT_FG_DARK); comboBox.setBackground(INPUT_BG_DARK); comboBox.setBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK,1)); for(Component c:comboBox.getComponents()){if(c instanceof JButton){((JButton)c).setBackground(BUTTON_BG_DARK);((JButton)c).setBorder(BorderFactory.createEmptyBorder());break;}} Object p=comboBox.getUI().getAccessibleChild(comboBox,0); if(p instanceof JPopupMenu){JPopupMenu pm=(JPopupMenu)p;pm.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); for(Component comp:pm.getComponents()){if(comp instanceof JScrollPane){JScrollPane sp=(JScrollPane)comp;sp.getViewport().setBackground(INPUT_BG_DARK);applyScrollbarUI(sp.getVerticalScrollBar()); Component l=sp.getViewport().getView(); if(l instanceof JList){((JList<?>)l).setBackground(INPUT_BG_DARK);((JList<?>)l).setForeground(INPUT_FG_DARK);((JList<?>)l).setSelectionBackground(BUTTON_BG_DARK);((JList<?>)l).setSelectionForeground(BUTTON_FG_DARK);}}}}}
   242	    private void styleTable(JTable table) { /* ... */ table.setBackground(TABLE_CELL_BG); table.setForeground(TABLE_CELL_FG); table.setGridColor(TABLE_GRID_COLOR); table.setRowHeight(28); table.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,13)); table.setFillsViewportHeight(true); table.setSelectionBackground(TABLE_CELL_SELECTED_BG); table.setSelectionForeground(TABLE_CELL_SELECTED_FG); table.setShowGrid(true); table.setIntercellSpacing(new Dimension(0,1)); JTableHeader h=table.getTableHeader(); h.setBackground(TABLE_HEADER_BG); h.setForeground(TABLE_HEADER_FG); h.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14)); h.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); h.setReorderingAllowed(true); h.setResizingAllowed(true); DefaultTableCellRenderer r=new DefaultTableCellRenderer(); r.setHorizontalAlignment(SwingConstants.LEFT); r.setVerticalAlignment(SwingConstants.CENTER); r.setBorder(new EmptyBorder(2,5,2,5)); for(int i=0;i<table.getColumnCount()-1;i++){table.getColumnModel().getColumn(i).setCellRenderer(r);} }
   243	    private void styleScrollPane(JScrollPane scrollPane) { /* ... */ scrollPane.setOpaque(false); scrollPane.getViewport().setOpaque(false); scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); applyScrollbarUI(scrollPane.getVerticalScrollBar()); applyScrollbarUI(scrollPane.getHorizontalScrollBar()); }
   244	    private void applyScrollbarUI(JScrollBar scrollBar) { /* ... */ scrollBar.setUI(new BasicScrollBarUI() { @Override protected void configureScrollBarColors(){this.thumbColor=BUTTON_BG_DARK; this.trackColor=DARK_BG_END;} @Override protected JButton createDecreaseButton(int o){return createZeroButton();} @ Override protected JButton createIncreaseButton(int o){return createZeroButton();} private JButton createZeroButton(){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));b.setMaximumSize(new Dimension(0,0));b.setMinimumSize(new Dimension(0,0));return b;} @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r){g.setColor(thumbColor);g.fillRect(r.x,r.y,r.width,r.height);} @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r){g.setColor(trackColor);g.fillRect(r.x,r.y,r.width,r.height);} }); scrollBar.setUnitIncrement(16); }
   245	    private void styleActionButton(JButton btn, String tooltip) { /* ... */ btn.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12)); btn.setToolTipText(tooltip); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setForeground(BUTTON_FG_DARK); btn.setFocusPainted(false); btn.setBackground(BUTTON_BG_DARK); Border p=new EmptyBorder(6,12,6,12); btn.setBorder(new CompoundBorder(BorderFactory.createLineBorder(BUTTON_BG_DARK.darker()),p)); btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){if(btn.getBackground().equals(BUTTON_BG_DARK)){btn.setBackground(BUTTON_HOVER_BG_DARK);}} @Override public void mouseExited(MouseEvent e){if(btn.getBackground().equals(BUTTON_HOVER_BG_DARK)){btn.setBackground(BUTTON_BG_DARK);}}}); }
   246	
   247	    // --- Inner classes for Table Button (Unchanged) ---
   248	    static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer { /* ... */ private Color defaultBg; public ButtonRenderer(Color background){setOpaque(true);this.defaultBg=background;setForeground(BUTTON_FG_DARK);setBackground(defaultBg);setBorder(new EmptyBorder(2,5,2,5));setFont(new Font(Font.SANS_SERIF,Font.BOLD,11));} @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){setText((v==null)?"":v.toString());setBackground(s?defaultBg.brighter():defaultBg);return this;} }
   249	    static class ButtonEditor extends DefaultCellEditor { /* ... */ protected JButton button; private String label; private boolean isPushed; private Runnable action; private Color bgColor; public ButtonEditor(JCheckBox c,Color bg,Runnable act){super(c);this.action=act;this.bgColor=bg;button=new JButton();button.setOpaque(true);button.setForeground(BUTTON_FG_DARK);button.setBackground(bgColor);button.setBorder(new EmptyBorder(2,5,2,5));button.setFont(new Font(Font.SANS_SERIF,Font.BOLD,11));button.addActionListener(e->fireEditingStopped());} @Override public Component getTableCellEditorComponent(JTable t,Object v,boolean s,int r,int c){label=(v==null)?"":v.toString();button.setText(label);isPushed=true;return button;} @Override public Object getCellEditorValue(){if(isPushed&&action!=null){action.run();}isPushed=false;return label;} @Override public boolean stopCellEditing(){isPushed=false;return super.stopCellEditing();} @Override protected void fireEditingStopped(){super.fireEditingStopped();} }
   250	
   251	     // --- Integration Notes (Unchanged) ---
   252	}

╔══════════════════════════════════════════════════════════════════════════╗
║                            END OF SOURCE CODE                              ║
╚══════════════════════════════════════════════════════════════════════════╝

```
---
### grouped_errors.txt
```
======================================================================
COMPILATION ERROR REPORT
Generated: 2025-08-28 11:33:06
Project Root: /home/jared/OrphanageHub_PAT2025
======================================================================

Total Syntax Errors: 5
Files Affected: 2
Error Types: 2

📁 ERRORS BY FILE (sorted by count):
======================================================================

src/main/java/com/orphanagehub/dao/OrphanageDAO.java (4 errors):
  ❌ Line 36, Col 40: cannot find symbol
     └─ Class: com.orphanagehub.dao.OrphanageDAO
  ❌ Line 39, Col 40: cannot find symbol
     └─ Class: com.orphanagehub.dao.OrphanageDAO
  ❌ Line 45, Col 44: cannot find symbol
     └─ Class: com.orphanagehub.dao.OrphanageDAO
  ❌ Line 62, Col 39: constructor Orphanage in class com.orphanagehub.model.Orphanage cannot be applied to given types;
     └─ Class: com.orphanagehub.dao.OrphanageDAO

src/main/java/com/orphanagehub/service/OrphanageService.java (1 error):
  ❌ Line 16, Col 47: cannot find symbol
     └─ Class: com.orphanagehub.service.OrphanageService

======================================================================
📊 ERROR TYPE SUMMARY (sorted by frequency):
----------------------------------------
  Symbol Not Found: 4 occurrences
    → Affects 2 files
  Constructor Mismatch: 1 occurrence
    → Affects 1 file

⚠️ OTHER ERRORS (build/config):
----------------------------------------
  •   symbol:   method getContactPerson()
  •   location: variable orphanage of type com.orphanagehub.model.Orphanage
  •   symbol:   method getVerificationStatus()
  •   required: no arguments
  •   found:    java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String
  •   reason: actual and formal argument lists differ in length
  •   symbol:   method findByStaffUserId(java.lang.String)
  •   location: variable orphanageDAO of type com.orphanagehub.dao.OrphanageDAO

======================================================================
📝 SOURCE CODE OF FILES WITH ERRORS:
======================================================================

╔══ OrphanageDAO.java ══╗
Path: src/main/java/com/orphanagehub/dao/OrphanageDAO.java
----------------------------------------------------------------------

📍 Error at line 36: cannot find symbol
──────────────────────────────────────────────────
      33: 
      34:             stmt.setString(1, orphanage.getName());
      35:             stmt.setString(2, orphanage.getAddress());
>>>   36:             stmt.setString(3, orphanage.getContactPerson());
                                                ^ Error here
      37:             stmt.setString(4, orphanage.getContactEmail());
      38:             stmt.setString(5, orphanage.getContactPhone());
      39:             stmt.setString(6, orphanage.getVerificationStatus());

📍 Error at line 45: cannot find symbol
──────────────────────────────────────────────────
      42:                 stmt.setString(7, orphanage.getOrphanageId());
      43:             } else {
      44:                 stmt.setString(1, orphanage.getOrphanageId());
>>>   45:                 stmt.setString(7, orphanage.getVerificationStatus());
                                                    ^ Error here
      46:             }
      47: 
      48:             stmt.executeUpdate();

📍 Error at line 62: constructor Orphanage in class com.orphanagehub.model.Orphanage cannot be applied to given types;
──────────────────────────────────────────────────
      59:              ResultSet rs = stmt.executeQuery()) {
      60:             
      61:             while (rs.next()) {
>>>   62:                 Orphanage orphanage = new Orphanage(
                                               ^ Error here
      63:                     rs.getString("OrphanageID"),
      64:                     rs.getString("Name"),
      65:                     rs.getString("Address"),
╚════════════════════════════════════════════════════════════════════╝

╔══ OrphanageService.java ══╗
Path: src/main/java/com/orphanagehub/service/OrphanageService.java
----------------------------------------------------------------------

📍 Error at line 16: cannot find symbol
──────────────────────────────────────────────────
      13: 
      14:     public Orphanage getOrphanageForStaff(String userId) throws ServiceException {
      15:         try {
>>>   16:             Orphanage orphanage = orphanageDAO.findByStaffUserId(userId);
                                                       ^ Error here
      17:             if (orphanage == null) {
      18:                 throw new ServiceException("No orphanage associated with this staff user.");
      19:             }
╚════════════════════════════════════════════════════════════════════╝

======================================================================
🔍 RAW LOG DUMP:
======================================================================
[INFO] Scanning for projects...
[INFO] 
[INFO] -------------------< com.orphanagehub:OrphanageHub >--------------------
[INFO] Building OrphanageHub 1.0.0
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ OrphanageHub ---
[INFO] Copying 5 resources from src/main/resources to target/classes
[INFO] 
[INFO] --- compiler:3.13.0:compile (default-compile) @ OrphanageHub ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 35 source files with javac [debug release 17] to target/classes
[INFO] -------------------------------------------------------------
[ERROR] COMPILATION ERROR : 
[INFO] -------------------------------------------------------------
[ERROR] /home/jared/OrphanageHub_PAT2025/src/main/java/com/orphanagehub/dao/OrphanageDAO.java:[36,40] cannot find symbol
  symbol:   method getContactPerson()
  location: variable orphanage of type com.orphanagehub.model.Orphanage
[ERROR] /home/jared/OrphanageHub_PAT2025/src/main/java/com/orphanagehub/dao/OrphanageDAO.java:[39,40] cannot find symbol
  symbol:   method getVerificationStatus()
  location: variable orphanage of type com.orphanagehub.model.Orphanage
[ERROR] /home/jared/OrphanageHub_PAT2025/src/main/java/com/orphanagehub/dao/OrphanageDAO.java:[45,44] cannot find symbol
  symbol:   method getVerificationStatus()
  location: variable orphanage of type com.orphanagehub.model.Orphanage
[ERROR] /home/jared/OrphanageHub_PAT2025/src/main/java/com/orphanagehub/dao/OrphanageDAO.java:[62,39] constructor Orphanage in class com.orphanagehub.model.Orphanage cannot be applied to given types;
  required: no arguments
  found:    java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String
  reason: actual and formal argument lists differ in length
[ERROR] /home/jared/OrphanageHub_PAT2025/src/main/java/com/orphanagehub/service/OrphanageService.java:[16,47] cannot find symbol
  symbol:   method findByStaffUserId(java.lang.String)
  location: variable orphanageDAO of type com.orphanagehub.dao.OrphanageDAO
[INFO] 5 errors 
[INFO] -------------------------------------------------------------
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.784 s
[INFO] Finished at: 2025-08-28T11:33:06+02:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.13.0:compile (default-compile) on project OrphanageHub: Compilation failure: Compilation failure: 
[ERROR] /home/jared/OrphanageHub_PAT2025/src/main/java/com/orphanagehub/dao/OrphanageDAO.java:[36,40] cannot find symbol
[ERROR]   symbol:   method getContactPerson()
[ERROR]   location: variable orphanage of type com.orphanagehub.model.Orphanage
[ERROR] /home/jared/OrphanageHub_PAT2025/src/main/java/com/orphanagehub/dao/OrphanageDAO.java:[39,40] cannot find symbol
[ERROR]   symbol:   method getVerificationStatus()
[ERROR]   location: variable orphanage of type com.orphanagehub.model.Orphanage
[ERROR] /home/jared/OrphanageHub_PAT2025/src/main/java/com/orphanagehub/dao/OrphanageDAO.java:[45,44] cannot find symbol
[ERROR]   symbol:   method getVerificationStatus()
[ERROR]   location: variable orphanage of type com.orphanagehub.model.Orphanage
[ERROR] /home/jared/OrphanageHub_PAT2025/src/main/java/com/orphanagehub/dao/OrphanageDAO.java:[62,39] constructor Orphanage in class com.orphanagehub.model.Orphanage cannot be applied to given types;
[ERROR]   required: no arguments
[ERROR]   found:    java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String
[ERROR]   reason: actual and formal argument lists differ in length
[ERROR] /home/jared/OrphanageHub_PAT2025/src/main/java/com/orphanagehub/service/OrphanageService.java:[16,47] cannot find symbol
[ERROR]   symbol:   method findByStaffUserId(java.lang.String)
[ERROR]   location: variable orphanageDAO of type com.orphanagehub.dao.OrphanageDAO
[ERROR] -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException

```
---
### src/main/java/com/orphanagehub/dao/DatabaseManager.java
```
package com.orphanagehub.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String DB_PATH = "db/OrphanageHub.accdb";
    private static final String CONNECTION_STRING = "jdbc:ucanaccess://" + DB_PATH + ";immediatelyReleaseResources=true";

    static {
        try {
            initializeDatabase();
        } catch (SQLException e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_STRING);
    }

    private static void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // Check if FullName column exists in TblUsers, add if not
            if (!columnExists(conn, "TblUsers", "FullName")) {
                stmt.execute("ALTER TABLE TblUsers ADD COLUMN FullName TEXT(100)");
                logger.info("Added FullName column to TblUsers");
            }

            // Check if AccountStatus column exists in TblUsers, add if not
            if (!columnExists(conn, "TblUsers", "AccountStatus")) {
                stmt.execute("ALTER TABLE TblUsers ADD COLUMN AccountStatus TEXT(20) DEFAULT 'Active'");
                logger.info("Added AccountStatus column to TblUsers");
            }
        }
    }

    public static void verifyTables() throws SQLException {
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            
            // Verify TblUsers
            try (ResultSet tables = meta.getTables(null, null, "TblUsers", null)) {
                if (!tables.next()) {
                    logger.warn("TblUsers table does not exist");
                    return;
                }
            }
            
            // Verify required columns in TblUsers
            String[] requiredColumns = {"UserID", "Username", "PasswordHash", "Email", "UserRole", "DateRegistered", "FullName", "AccountStatus"};
            for (String column : requiredColumns) {
                if (!columnExists(conn, "TblUsers", column)) {
                    logger.warn("Missing column in TblUsers: " + column);
                }
            }
            
            logger.info("Tables verified successfully");
        }
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet columns = meta.getColumns(null, null, tableName, columnName)) {
            return columns.next();
        }
    }
}
```
---
### src/main/java/com/orphanagehub/dao/OrphanageDAO.java
```
package com.orphanagehub.dao;

import com.orphanagehub.model.Orphanage;
import com.orphanagehub.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrphanageDAO {
    private static final Logger logger = LoggerFactory.getLogger(OrphanageDAO.class);

    public void save(Orphanage orphanage) throws SQLException {
        String sql;
        if (orphanage.getOrphanageId() == null) {
            orphanage.setOrphanageId("ORPH" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
            orphanage.setVerificationStatus("PENDING");
            sql = "INSERT INTO TblOrphanages (OrphanageID, UserID, Name, Address, ContactPerson, ContactEmail, ContactPhone, VerificationStatus) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE TblOrphanages SET UserID = ?, Name = ?, Address = ?, ContactPerson = ?, ContactEmail = ?, ContactPhone = ?, VerificationStatus = ? " +
                  "WHERE OrphanageID = ?";
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orphanage.getUserId());
            stmt.setString(2, orphanage.getName());
            stmt.setString(3, orphanage.getAddress());
            stmt.setString(4, orphanage.getContactPerson());
            stmt.setString(5, orphanage.getContactEmail());
            stmt.setString(6, orphanage.getContactPhone());
            stmt.setString(7, orphanage.getVerificationStatus());

            if (orphanage.getOrphanageId() != null) {
                stmt.setString(8, orphanage.getOrphanageId());
            } else {
                stmt.setString(1, orphanage.getOrphanageId());
                stmt.setString(2, orphanage.getUserId());
                stmt.setString(8, orphanage.getVerificationStatus());
            }

            stmt.executeUpdate();
            logger.info("Orphanage saved: {}", orphanage.getName());
        }
    }

    public List<Orphanage> findAll() throws SQLException {
        List<Orphanage> orphanages = new ArrayList<>();
        String sql = "SELECT * FROM TblOrphanages";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Orphanage orphanage = new Orphanage(
                    rs.getString("OrphanageID"),
                    rs.getString("UserID"),
                    rs.getString("Name"),
                    rs.getString("Address"),
                    rs.getString("ContactPerson"),
                    rs.getString("ContactEmail"),
                    rs.getString("ContactPhone"),
                    rs.getString("VerificationStatus")
                );
                orphanages.add(orphanage);
            }
        }
        return orphanages;
    }

    public Orphanage findByStaffUserId(String userId) throws SQLException {
        String sql = "SELECT * FROM TblOrphanages WHERE UserID = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Orphanage(
                        rs.getString("OrphanageID"),
                        rs.getString("UserID"),
                        rs.getString("Name"),
                        rs.getString("Address"),
                        rs.getString("ContactPerson"),
                        rs.getString("ContactEmail"),
                        rs.getString("ContactPhone"),
                        rs.getString("VerificationStatus")
                    );
                }
                return null;
            }
        }
    }

    // Add more methods as needed...
}
```
---
### src/main/java/com/orphanagehub/dao/UserDAO.java
```
package com.orphanagehub.dao;

import com.orphanagehub.model.User;
import com.orphanagehub.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    public void save(String username, String email, String fullName, String passwordHash, String userRole) throws SQLException {
        String userId = "U" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String sql = "INSERT INTO TblUsers (UserID, Username, Email, FullName, PasswordHash, UserRole, DateRegistered, AccountStatus) " +
                     "VALUES (?, ?, ?, ?, ?, ?, NOW(), 'Active')";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, username);
            pstmt.setString(3, email);
            pstmt.setString(4, fullName);
            pstmt.setString(5, passwordHash);
            pstmt.setString(6, userRole);
            pstmt.executeUpdate();
            logger.info("User saved: {}", username);
        }
    }

    public boolean isUsernameTaken(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TblUsers WHERE Username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean isEmailTaken(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TblUsers WHERE Email = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM TblUsers WHERE Username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getString("UserID"),
                        rs.getString("Username"),
                        rs.getString("PasswordHash"),
                        rs.getString("Email"),
                        rs.getString("FullName"),
                        rs.getString("UserRole"),
                        rs.getTimestamp("DateRegistered"),
                        rs.getString("AccountStatus")
                    );
                }
                return null;
            }
        }
    }
}
```
---
### src/main/java/com/orphanagehub/gui/RegistrationPanel.java
```
package com.orphanagehub.gui;

import com.orphanagehub.service.RegistrationService;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

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
        btnRegister.addActionListener(e -> registerAction()); // Updated action

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
                this.trackColor = DARK_BG_END;    // Use gradient end for track
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

    // Updated registration logic
    private void registerAction() {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String fullName = txtFullName.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

         // Simple validation example
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
             JOptionPane.showMessageDialog(this, "Please fill in Username, Email, and Password.", "Registration Error", JOptionPane.ERROR_MESSAGE);
             return;
        }
        if (!password.equals(confirmPassword)) {
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

        try {
            new RegistrationService().registerUser(username, email, fullName, password, confirmPassword, currentRole);
            JOptionPane.showMessageDialog(this, "Registration successful for " + username + " as " + currentRole, "Success", JOptionPane.INFORMATION_MESSAGE);
            mainApp.navigateTo(OrphanageHubApp.LOGIN_PANEL);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Registration failed due to a database error.", "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
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
                lblRoleIcon.setText("\u2764"); // Heavy Black Heart symbol
                lblRoleIcon.setForeground(new Color(255, 105, 180)); // Pinkish
                break;
            case "OrphanageStaff":
                lblRoleIcon.setText("\u2302"); // House symbol
                lblRoleIcon.setForeground(new Color(135, 206, 250)); // Light Sky Blue
                break;
            case "Volunteer":
                lblRoleIcon.setText("\u2605"); // Black Star symbol
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
}
```
---
### src/main/java/com/orphanagehub/model/Orphanage.java
```
package com.orphanagehub.model;

public class Orphanage {
    private String orphanageId;
    private String userId;
    private String name;
    private String address;
    private String contactPerson;
    private String contactEmail;
    private String contactPhone;
    private String verificationStatus;

    public Orphanage(String orphanageId, String userId, String name, String address,
                     String contactPerson, String contactEmail, String contactPhone,
                     String verificationStatus) {
        this.orphanageId = orphanageId;
        this.userId = userId;
        this.name = name;
        this.address = address;
        this.contactPerson = contactPerson;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.verificationStatus = verificationStatus;
    }

    // Getters and Setters
    public String getOrphanageId() { return orphanageId; }
    public void setOrphanageId(String orphanageId) { this.orphanageId = orphanageId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
}
```
---
### src/main/java/com/orphanagehub/model/User.java
```
package com.orphanagehub.model;

import java.util.Date;

public class User {
    private String userId;
    private String username;
    private String passwordHash;
    private String email;
    private String fullName;
    private String userRole;
    private Date dateRegistered;
    private String accountStatus;

    public User(String userId, String username, String passwordHash, String email, String fullName,
                String userRole, Date dateRegistered, String accountStatus) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.fullName = fullName;
        this.userRole = userRole;
        this.dateRegistered = dateRegistered;
        this.accountStatus = accountStatus;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getUserRole() { return userRole; }
    public Date getDateRegistered() { return dateRegistered; }
    public String getAccountStatus() { return accountStatus; }
}
```
---
### src/main/java/com/orphanagehub/service/AuthenticationService.java
```
package com.orphanagehub.service;

import com.orphanagehub.dao.UserDAO;
import com.orphanagehub.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserDAO userDAO = new UserDAO();

    public User authenticate(String username, String password) throws SQLException {
        User user = userDAO.findByUsername(username);
        if (user == null) {
            return null;
        }

        String inputHash = hashPassword(password);
        if (inputHash.equals(user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Password hashing failed", e);
            throw new RuntimeException("Failed to hash password");
        }
    }
}
```
---
### src/main/java/com/orphanagehub/service/RegistrationService.java
```
package com.orphanagehub.service;

import com.orphanagehub.dao.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class RegistrationService {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);
    private final UserDAO userDAO = new UserDAO();

    public void registerUser(String username, String email, String fullName, String password, String confirmPassword, String userRole) throws SQLException {
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (userDAO.isUsernameTaken(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userDAO.isEmailTaken(email)) {
            throw new IllegalArgumentException("Email is already taken");
        }

        String passwordHash = hashPassword(password);
        userDAO.save(username, email, fullName, passwordHash, userRole);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Password hashing failed", e);
            throw new RuntimeException("Failed to hash password");
        }
    }
}
```
---
### src/main/java/com/orphanagehub/tools/DbDoctor.java
```
package com.orphanagehub.tools;

import com.orphanagehub.util.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;

public class DbDoctor {
    public static void main(String[] args) {
        System.out.println("🩺 Checking database connectivity...");
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println(
                        "\n✅ SUCCESS: Connection to the database was established successfully.");
            } else {
                System.out.println("\n❌ ERROR: Failed to connect to the database.");
            }
        } catch (SQLException e) {
            System.out.println("\n❌ ERROR: Database connection failed.");
            e.printStackTrace();
        }
    }
}
```
---
### src/main/java/com/orphanagehub/tools/DbShell.java
```
package com.orphanagehub.tools;

import com.orphanagehub.util.DatabaseManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbShell {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java DbShell \"YOUR_SQL_QUERY\"");
            return;
        }
        String query = args[0];
        System.out.println("Executing: " + query);
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            boolean hasResultSet = stmt.execute(query);
            if (hasResultSet) {
                ResultSet rs = stmt.getResultSet();
                while (rs.next()) {
                    // Print first column as example
                    System.out.println(rs.getString(1));
                }
            } else {
                System.out.println("Update count: " + stmt.getUpdateCount());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```
---
### src/main/java/com/orphanagehub/tools/DbTest.java
```
package com.orphanagehub.tools;

import com.orphanagehub.util.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;

public class DbTest {
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        
        try (Connection conn = DatabaseManager.getConnection()) {
            System.out.println("✓ Connection successful");
            
            DatabaseManager.verifyTables();
            System.out.println("✓ Tables verified");
            
        } catch (SQLException e) {
            System.out.println("✗ Connection failed: " + e.getMessage());
        }
    }
}
```