// src/main/java/com/orphanagehub/gui/OrphanageDashboardPanel.java
package com.orphanagehub.gui;

import com.orphanagehub.dao.ResourceRequestDAO;
import com.orphanagehub.model.ResourceRequest;
import com.orphanagehub.service.OrphanageService;
import com.orphanagehub.service.ServiceException;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.sql.SQLException;
import java.util.List;

public class OrphanageDashboardPanel extends JPanel {
    private OrphanageHubApp mainApp;
    private String orphanageName = "Hope Children's Home";
    private String staffUsername = "staff_user";
    private static final Color DARK_BG_START = new Color(45, 52, 54);
    private static final Color DARK_BG_END = new Color(35, 42, 44);
    private static final Color TITLE_COLOR_DARK = new Color(223, 230, 233);
    private static final Color TEXT_COLOR_DARK = new Color(200, 200, 200);
    private static final Color BORDER_COLOR_DARK = new Color(80, 80, 80);
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
    private static final Color ACCENT_COLOR_ORANGE = new Color(230, 145, 56);
    private static final Color ACCENT_COLOR_BLUE = new Color(72, 149, 239);
    private static final Color ACCENT_COLOR_GREEN = new Color(87, 190, 106);

    public OrphanageDashboardPanel(OrphanageHubApp app) {
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
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        JTabbedPane tabbedPane = createTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
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
        JLabel iconLabel = new JLabel("\u2302");
        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        iconLabel.setForeground(new Color(135, 206, 250));
        JLabel nameLabel = new JLabel(orphanageName);
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        nameLabel.setForeground(TITLE_COLOR_DARK);
        titleGroup.add(iconLabel);
        titleGroup.add(nameLabel);
        headerPanel.add(titleGroup, BorderLayout.WEST);
        JPanel userGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userGroup.setOpaque(false);
        JLabel userLabel = new JLabel("User: " + staffUsername);
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        userLabel.setForeground(TEXT_COLOR_DARK);
        JButton btnLogout = new JButton("Logout");
        styleActionButton(btnLogout, "Logout and return to welcome screen");
        btnLogout.setPreferredSize(new Dimension(100, 30));
        btnLogout.setBackground(new Color(192, 57, 43));
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

    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setOpaque(false);
        tabbedPane.setForeground(TAB_FG);
        tabbedPane.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override protected void installDefaults() {
                super.installDefaults();
                lightHighlight = TAB_BG_SELECTED;
                shadow = BORDER_COLOR_DARK;
                darkShadow = DARK_BG_END;
                focus = TAB_BG_SELECTED;
            }
            @Override protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                g.setColor(isSelected ? TAB_BG_SELECTED : TAB_BG_UNSELECTED);
                switch (tabPlacement) {
                    case TOP:
                    default:
                        g.fillRoundRect(x, y, w, h + 5, 5, 5);
                        break;
                }
            }
            @Override protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) { }
            @Override protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                int width = tabPane.getWidth();
                int height = tabPane.getHeight();
                Insets insets = tabPane.getInsets();
                int tabAreaHeight = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                int x = insets.left;
                int y = insets.top + tabAreaHeight - (lightHighlight.getAlpha() > 0 ? 1 : 0);
                int w = width - insets.right - insets.left;
                int h = height - insets.top - insets.bottom - y;
                g.setColor(BORDER_COLOR_DARK);
                g.drawRect(x, y, w - 1, h - 1);
            }
        });
        tabbedPane.addTab("Overview", createOverviewTab());
        tabbedPane.addTab("Resource Requests", createResourceRequestsTab());
        tabbedPane.addTab("Donations", createPlaceholderTab("Donations Management"));
        tabbedPane.addTab("Volunteers", createPlaceholderTab("Volunteer Management"));
        tabbedPane.addTab("Orphanage Profile", createPlaceholderTab("Orphanage Profile Editor"));
        return tabbedPane;
    }

    private JPanel createOverviewTab() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 20));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        panel.add(createStatCard("Active Requests", "12", ACCENT_COLOR_ORANGE));
        panel.add(createStatCard("Pending Donations", "3", ACCENT_COLOR_BLUE));
        panel.add(createStatCard("Active Volunteers", "8", ACCENT_COLOR_GREEN));
        return panel;
    }

    private JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(TAB_BG_UNSELECTED);
        card.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, accentColor),
                new EmptyBorder(15, 20, 15, 20)
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
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);
        JButton btnAdd = new JButton("Add Request");
        JButton btnEdit = new JButton("Edit Selected");
        JButton btnDelete = new JButton("Delete Selected");
        styleActionButton(btnAdd, "Create a new resource request");
        styleActionButton(btnEdit, "Modify the selected request");
        styleActionButton(btnDelete, "Remove the selected request");
        btnDelete.setBackground(new Color(192, 57, 43));
        btnDelete.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnDelete.setBackground(new Color(231, 76, 60)); }
            @Override public void mouseExited(MouseEvent e) { btnDelete.setBackground(new Color(192, 57, 43)); }
        });
        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        panel.add(toolbar, BorderLayout.NORTH);
        String[] columnNames = {"ID", "Category", "Description", "Needed", "Fulfilled", "Urgency", "Status"};
        DefaultTableModel model = new DefaultTableModel(new Object[0][0], columnNames);
        JTable table = new JTable(model);
        styleTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        ResourceRequestDAO dao = new ResourceRequestDAO();
        try {
            List<ResourceRequest> requests = dao.findByOrphanageId("orphanageId_placeholder");
            model.setRowCount(0);
            for (ResourceRequest r : requests) {
                model.addRow(new Object[]{r.getId(), r.getCategory(), r.getDescription(), r.getNeeded(), r.getFulfilled(), r.getUrgency(), r.getStatus()});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load resource requests: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return panel;
    }

    private JPanel createPlaceholderTab(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel label = new JLabel(title + " - Content Area", SwingConstants.CENTER);
        label.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 18));
        label.setForeground(TEXT_COLOR_DARK);
        panel.add(label);
        return panel;
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
        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TABLE_HEADER_FG);
        header.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        header.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK));
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(250);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK));
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
        applyScrollbarUI(verticalScrollBar);
        applyScrollbarUI(horizontalScrollBar);
    }

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
             @Override protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                 g.setColor(thumbColor);
                 g.fillRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);
             }
             @Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                 g.setColor(trackColor);
                 g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
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
                padding));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                    btn.setBackground(BUTTON_HOVER_BG_DARK);
            }
            @Override public void mouseExited(MouseEvent e) {
                    btn.setBackground(BUTTON_BG_DARK);
            }
        });
    }
}