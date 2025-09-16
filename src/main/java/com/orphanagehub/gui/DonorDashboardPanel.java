package com.orphanagehub.gui;

import com.orphanagehub.model.*;
import com.orphanagehub.service.*;
import com.orphanagehub.dao.OrphanageDAO;
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
import java.util.Map;
import java.util.HashMap;

public class DonorDashboardPanel extends JPanel {
    private final OrphanageHubApp mainApp;
    private final DonorService donorService;
    private final OrphanageDAO orphanageDAO;
    
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbLocation;
    private JComboBox<String> cmbCategory;
    private JLabel lblDonationCount;
    private JLabel lblTotalAmount;
    
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
    private static final Color TABLE_HEADER_BG = new Color(65, 75, 77);
    private static final Color TABLE_HEADER_FG = TITLE_COLOR_DARK;
    private static final Color TABLE_GRID_COLOR = BORDER_COLOR_DARK;
    private static final Color TABLE_CELL_BG = new Color(55, 62, 64);
    private static final Color TABLE_CELL_FG = TEXT_COLOR_DARK;
    private static final Color TABLE_CELL_SELECTED_BG = BUTTON_BG_DARK;
    private static final Color TABLE_CELL_SELECTED_FG = BUTTON_FG_DARK;
    private static final Color BUTTON_SEARCH_BG = new Color(72, 149, 239);
    private static final Color BUTTON_SEARCH_HOVER_BG = new Color(92, 169, 249);
    private static final Color BUTTON_DONATE_BG = new Color(60, 179, 113);
    private static final Color BUTTON_DONATE_HOVER_BG = new Color(70, 190, 123);

    public DonorDashboardPanel(OrphanageHubApp app) {
        this.mainApp = app;
        this.donorService = new DonorService();
        this.orphanageDAO = new OrphanageDAO();
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
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        JPanel contentPanel = new JPanel(new BorderLayout(10, 15));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(15, 20, 20, 20));
        
        // Search and filter panel
        JPanel searchFilterPanel = createSearchFilterPanel();
        contentPanel.add(searchFilterPanel, BorderLayout.NORTH);
        
        // Results table
        resultsTable = createResultsTable();
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        styleScrollPane(scrollPane);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Stats panel
        JPanel statsPanel = createStatsPanel();
        contentPanel.add(statsPanel, BorderLayout.SOUTH);
        
        add(contentPanel, BorderLayout.CENTER);
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
        JLabel iconLabel = new JLabel("\uD83E\uDEC2"); // Heart hands emoji
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 22));
        iconLabel.setForeground(new Color(255, 215, 0));
        JLabel nameLabel = new JLabel("Donor Dashboard");
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
            .getOrElse("Donor");
        
        JLabel userLabel = new JLabel("Welcome! ");
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        userLabel.setForeground(TEXT_COLOR_DARK);
        
        JButton btnHistory = new JButton("My Donations");
        styleActionButton(btnHistory, "View your donation history");
        btnHistory.setPreferredSize(new Dimension(120, 30));
        btnHistory.addActionListener(e -> showDonationHistory());
        
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
        userGroup.add(btnHistory);
        userGroup.add(btnLogout);
        headerPanel.add(userGroup, BorderLayout.EAST);
        
        return headerPanel;
    }

    private JPanel createSearchFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setOpaque(false);
        
        JLabel lblSearch = new JLabel("Search:");
        styleFormLabel(lblSearch);
        txtSearch = new JTextField(20);
        styleTextField(txtSearch);
        
        JLabel lblFilterLocation = new JLabel("Location:");
        styleFormLabel(lblFilterLocation);
        cmbLocation = new JComboBox<>(new String[]{"Any Location"});
        styleComboBox(cmbLocation);
        
        JLabel lblFilterCategory = new JLabel("Need Category:");
        styleFormLabel(lblFilterCategory);
        cmbCategory = new JComboBox<>(new String[]{
            "Any Category", "Food", "Clothing", "Education", 
            "Medical", "Funding", "Infrastructure", "Other"
        });
        styleComboBox(cmbCategory);
        
        JButton btnSearch = new JButton("Apply Filters");
        styleActionButton(btnSearch, "Find orphanages or requests matching criteria");
        btnSearch.setBackground(BUTTON_SEARCH_BG);
        btnSearch.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnSearch.setBackground(BUTTON_SEARCH_HOVER_BG);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnSearch.setBackground(BUTTON_SEARCH_BG);
            }
        });
        btnSearch.addActionListener(e -> performSearch());
        
        JButton btnRefresh = new JButton("Refresh");
        styleActionButton(btnRefresh, "Reload orphanage data");
        btnRefresh.addActionListener(e -> loadOrphanageData());
        
        panel.add(lblSearch);
        panel.add(txtSearch);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(lblFilterLocation);
        panel.add(cmbLocation);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(lblFilterCategory);
        panel.add(cmbCategory);
        panel.add(Box.createHorizontalStrut(15));
        panel.add(btnSearch);
        panel.add(btnRefresh);
        
        return panel;
    }

    private JTable createResultsTable() {
        String[] columnNames = {
            "Orphanage Name", "Location", "Province", "Key Needs", 
            "Urgency", "Verified", "Actions"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Actions column is editable
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 5) return Boolean.class; // Verified column
                return String.class;
            }
        };
        
        JTable table = new JTable(tableModel);
        styleTable(table);
        
        // Add button renderer and editor for Actions column
        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(
            new ButtonEditor(new JCheckBox())
        );
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(250);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(120);
        
        return table;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        panel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR_DARK),
            new EmptyBorder(10, 5, 5, 5)
        ));
        
        lblDonationCount = new JLabel("Total Donations: 0");
        lblDonationCount.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 13));
        lblDonationCount.setForeground(TEXT_COLOR_DARK);
        
        lblTotalAmount = new JLabel("Total Amount: $0.00");
        lblTotalAmount.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 13));
        lblTotalAmount.setForeground(TEXT_COLOR_DARK);
        
        panel.add(lblDonationCount);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(lblTotalAmount);
        
        return panel;
    }

    private void loadInitialData() {
        loadOrphanageData();
        loadLocations();
        updateDonationStats();
    }

    private void loadOrphanageData() {
        orphanageDAO.findAll()
            .map(List::ofAll)
            .onSuccess(orphanages -> {
                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    
                    orphanages
                        .filter(o -> "Verified".equalsIgnoreCase(o.verificationStatus()))
                        .forEach(orphanage -> {
                            // Get resource requests for this orphanage
                            donorService.getOrphanageDetails(orphanage.orphanageId())
                                .onSuccess(details -> {
                                    List<ResourceRequest> needs = details.currentNeeds();
                                    String keyNeeds = needs
                                        .take(3)
                                        .map(r -> r.resourceDescription())
                                        .mkString(", ");
                                    
                                    String urgency = needs
                                        .filter(r -> "High".equalsIgnoreCase(r.urgencyLevel()) || 
                                                    "Critical".equalsIgnoreCase(r.urgencyLevel()))
                                        .isEmpty() ? "Normal" : "High";
                                    
                                    tableModel.addRow(new Object[]{
                                        orphanage.name(),
                                        orphanage.address(),
                                        orphanage.province(),
                                        keyNeeds.isEmpty() ? "Various needs" : keyNeeds,
                                        urgency,
                                        true, // Verified
                                        "View Details"
                                    });
                                });
                        });
                });
            })
            .onFailure(ex -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "Failed to load orphanages: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                });
            });
    }

    private void loadLocations() {
        orphanageDAO.findAll()
            .map(List::ofAll)
            .onSuccess(orphanages -> {
                List<String> locations = orphanages
                    .map(o -> o.province()) 
                    .distinct()
                    .sorted();
                
                SwingUtilities.invokeLater(() -> {
                    cmbLocation.removeAllItems();
                    cmbLocation.addItem("Any Location");
                    locations.forEach(loc -> cmbLocation.addItem(loc));
                });
            });
    }

    private void updateDonationStats() {
        String donorIdStr = SessionManager.getInstance()
            .getAttribute("currentUserId")
            .map(Object::toString)
            .getOrElse("");
        
        if (!donorIdStr.isEmpty()) {
            Try.of(() -> Integer.valueOf(donorIdStr))
                .flatMap(donorService::getDonorStatistics)
                .onSuccess(stats -> {
                    SwingUtilities.invokeLater(() -> {
                        lblDonationCount.setText("Total Donations: " + stats.totalDonations());
                        lblTotalAmount.setText("Total Amount: $" + String.format("%.2f", stats.totalAmount()));
                    });
                });
        }
    }

    private void performSearch() {
        String searchText = txtSearch.getText().toLowerCase();
        String location = (String) cmbLocation.getSelectedItem();
        String category = (String) cmbCategory.getSelectedItem();
        
        // Filter table based on search criteria
        // This could be enhanced with actual service calls
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            String name = tableModel.getValueAt(i, 0).toString().toLowerCase();
            String loc = tableModel.getValueAt(i, 2).toString();
            String needs = tableModel.getValueAt(i, 3).toString().toLowerCase();
            
            boolean matches = true;
            
            if (!searchText.isEmpty() && !name.contains(searchText)) {
                matches = false;
            }
            
            if (!"Any Location".equals(location) && !loc.equals(location)) {
                matches = false;
            }
            
            if (!"Any Category".equals(category) && !needs.contains(category.toLowerCase())) {
                matches = false;
            }
            
            if (!matches) {
                tableModel.removeRow(i);
            }
        }
    }

    private void showDonationHistory() {
        // Implementation for donation history dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "My Donation History", true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        
        // Add donation history table
        // This would load actual donation history from the service
        
        dialog.setVisible(true);
    }

    private void showOrphanageDetails(int row) {
        String orphanageName = tableModel.getValueAt(row, 0).toString();
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Orphanage Details: " + orphanageName, true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        
        // Create donation form
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Add orphanage details and donation form
        // This would be populated with actual data
        
        dialog.add(panel);
        dialog.setVisible(true);
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

    // Button renderer for table
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            setForeground(BUTTON_FG_DARK);
            setBackground(BUTTON_DONATE_BG);
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
            button.setBackground(BUTTON_DONATE_BG);
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
}