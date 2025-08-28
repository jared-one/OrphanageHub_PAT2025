package com.orphanagehub.gui;

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
// --- It is included here only for completeness      ---

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
        JLabel iconLabel = new JLabel("\u2302"); // House symbol
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
                shadow = BORDER_COLOR_DARK;      // Color for unselected tab border bottom/right
                darkShadow = DARK_BG_END;        // Outer border color maybe?
                focus = TAB_BG_SELECTED;         // Focus indicator color
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
}
