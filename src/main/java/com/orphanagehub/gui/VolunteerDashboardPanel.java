// src/main/java/com/orphanagehub/gui/VolunteerDashboardPanel.java
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

public class VolunteerDashboardPanel extends JPanel {
    private OrphanageHubApp mainApp;
    private String volunteerUsername = "volunteer_user";
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
    private static final Color BUTTON_APPLY_BG = new Color(87, 190, 106);
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
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        JPanel contentPanel = new JPanel(new BorderLayout(10, 15));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(15, 20, 20, 20));
        JPanel searchFilterPanel = createSearchFilterPanel();
        contentPanel.add(searchFilterPanel, BorderLayout.NORTH);
        JTable opportunitiesTable = createOpportunitiesTable();
        JScrollPane scrollPane = new JScrollPane(opportunitiesTable);
        styleScrollPane(scrollPane);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        JPanel statusPanel = createStatusPanel();
        contentPanel.add(statusPanel, BorderLayout.SOUTH);
        add(contentPanel, BorderLayout.CENTER);
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
        JLabel iconLabel = new JLabel("\u2605");
        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        iconLabel.setForeground(new Color(255, 215, 0));
        JLabel nameLabel = new JLabel("Volunteer Dashboard");
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        nameLabel.setForeground(TITLE_COLOR_DARK);
        titleGroup.add(iconLabel);
        titleGroup.add(nameLabel);
        headerPanel.add(titleGroup, BorderLayout.WEST);
        JPanel userGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userGroup.setOpaque(false);
        JLabel userLabel = new JLabel("User: " + volunteerUsername);
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

    private JPanel createSearchFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setOpaque(false);
        JLabel lblFilterLocation = new JLabel("Location:");
        styleFormLabel(lblFilterLocation);
        String[] locations = {"Any Location", "City A", "City B", "Region C"};
        JComboBox<String> cmbLocation = new JComboBox<>(locations);
        styleComboBox(cmbLocation);
        JLabel lblFilterSkills = new JLabel("Skills:");
        styleFormLabel(lblFilterSkills);
        JTextField txtSkills = new JTextField(15);
        styleTextField(txtSkills);
        JLabel lblFilterTime = new JLabel("Commitment:");
        styleFormLabel(lblFilterTime);
        String[] times = {"Any Time", "Weekends", "Weekdays", "Flexible", "Event-Based"};
        JComboBox<String> cmbTime = new JComboBox<>(times);
        styleComboBox(cmbTime);
        JButton btnSearch = new JButton("Find Opportunities");
        styleActionButton(btnSearch, "Search for volunteer roles matching criteria");
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
        Object[][] data = {
                {"Hope Children's Home", "Weekend Tutor", "City A", "Teaching, Patience", "Weekends", "Apply"},
                {"Bright Future Orphanage", "Event Helper", "City B", "Organizing, Energetic", "Event-Based", "Apply"},
                {"Little Angels Shelter", "After-School Care", "City A", "Childcare, First Aid", "Weekdays", "Applied"},
                {"Sunshine House", "Gardening Assistant", "Region C", "Gardening", "Flexible", "Apply"},
                {"Hope Children's Home", "Reading Buddy", "City A", "Reading, Communication", "Weekdays", "Apply"}
        };
        JTable table = new JTable(data, columnNames) {
             @Override
             public boolean isCellEditable(int row, int column) {
                 return column == 5 && "Apply".equals(getValueAt(row, column));
             }
        };
        styleTable(table);
        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer(BUTTON_APPLY_BG));
        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox(), BUTTON_APPLY_BG, () -> {
             int selectedRow = table.convertRowIndexToModel(table.getEditingRow());
             String oppName = (String) table.getModel().getValueAt(selectedRow, 1);
             String orphName = (String) table.getModel().getValueAt(selectedRow, 0);
             JOptionPane.showMessageDialog(this, "Apply for: " + oppName + " at " + orphName, "Apply", JOptionPane.INFORMATION_MESSAGE);
         }));
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        return table;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        panel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR_DARK),
            new EmptyBorder(10, 5, 5, 5)
        ));
        JLabel lblStatus = new JLabel("Your Applications: 1 Pending (Little Angels Shelter)");
        lblStatus.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 13));
        lblStatus.setForeground(TEXT_COLOR_DARK);
        panel.add(lblStatus);
        return panel;
    }

    private void styleFormLabel(JLabel label) { label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); label.setForeground(TEXT_COLOR_DARK); }
    private void styleTextField(JTextField field) { field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); field.setForeground(INPUT_FG_DARK); field.setBackground(INPUT_BG_DARK); Border p=new EmptyBorder(4,6,4,6); field.setBorder(new CompoundBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK,1),p)); field.setCaretColor(Color.LIGHT_GRAY); }
    private void styleComboBox(JComboBox<?> comboBox) { comboBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); comboBox.setForeground(INPUT_FG_DARK); comboBox.setBackground(INPUT_BG_DARK); comboBox.setBorder(BorderFactory.createLineBorder(INPUT_BORDER_DARK,1)); Object p=comboBox.getUI().getAccessibleChild(comboBox,0); if(p instanceof JPopupMenu){JPopupMenu pm=(JPopupMenu)p;pm.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); for(Component comp:pm.getComponents()){if(comp instanceof JScrollPane){JScrollPane sp=(JScrollPane)comp;sp.getViewport().setBackground(INPUT_BG_DARK);applyScrollbarUI(sp.getVerticalScrollBar()); Component l=sp.getViewport().getView(); if(l instanceof JList){((JList<?>)l).setBackground(INPUT_BG_DARK);((JList<?>)l).setForeground(INPUT_FG_DARK);((JList<?>)l).setSelectionBackground(BUTTON_BG_DARK);((JList<?>)l).setSelectionForeground(BUTTON_FG_DARK);}}}}}
    private void styleTable(JTable table) { table.setBackground(TABLE_CELL_BG); table.setForeground(TABLE_CELL_FG); table.setGridColor(TABLE_GRID_COLOR); table.setRowHeight(28); table.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,13)); table.setFillsViewportHeight(true); table.setSelectionBackground(TABLE_CELL_SELECTED_BG); table.setSelectionForeground(TABLE_CELL_SELECTED_FG); table.setShowGrid(true); table.setIntercellSpacing(new Dimension(0,1)); JTableHeader h=table.getTableHeader(); h.setBackground(TABLE_HEADER_BG); h.setForeground(TABLE_HEADER_FG); h.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14)); h.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); h.setReorderingAllowed(true); h.setResizingAllowed(true); DefaultTableCellRenderer r=new DefaultTableCellRenderer(); r.setHorizontalAlignment(SwingConstants.LEFT); r.setVerticalAlignment(SwingConstants.CENTER); r.setBorder(new EmptyBorder(2,5,2,5)); for(int i=0;i<table.getColumnCount()-1;i++){table.getColumnModel().getColumn(i).setCellRenderer(r);} }
    private void styleScrollPane(JScrollPane scrollPane) { scrollPane.setOpaque(false); scrollPane.getViewport().setOpaque(false); scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR_DARK)); applyScrollbarUI(scrollPane.getVerticalScrollBar()); applyScrollbarUI(scrollPane.getHorizontalScrollBar()); }
    private void applyScrollbarUI(JScrollBar scrollBar) { scrollBar.setUI(new BasicScrollBarUI() { @Override protected void configureScrollBarColors(){this.thumbColor=BUTTON_BG_DARK; this.trackColor=DARK_BG_END;} @Override protected JButton createDecreaseButton(int o){return createZeroButton();} @Override protected JButton createIncreaseButton(int o){return createZeroButton();} private JButton createZeroButton(){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));b.setMaximumSize(new Dimension(0,0));b.setMinimumSize(new Dimension(0,0));return b;} @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r){g.setColor(thumbColor);g.fillRect(r.x,r.y,r.width,r.height);} @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r){g.setColor(trackColor);g.fillRect(r.x,r.y,r.width,r.height);} }); scrollBar.setUnitIncrement(16); }
    private void styleActionButton(JButton btn, String tooltip) { btn.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12)); btn.setToolTipText(tooltip); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setForeground(BUTTON_FG_DARK); btn.setFocusPainted(false); btn.setBackground(BUTTON_BG_DARK); Border p=new EmptyBorder(6,12,6,12); btn.setBorder(new CompoundBorder(BorderFactory.createLineBorder(BUTTON_BG_DARK.darker()),p)); btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){if(btn.getBackground().equals(BUTTON_BG_DARK)){btn.setBackground(BUTTON_HOVER_BG_DARK);}} @Override public void mouseExited(MouseEvent e){if(btn.getBackground().equals(BUTTON_HOVER_BG_DARK)){btn.setBackground(BUTTON_BG_DARK);}}}); }

    static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        private Color defaultBg;
        public ButtonRenderer(Color background){
            setOpaque(true);
            this.defaultBg=background;
            setForeground(BUTTON_FG_DARK);
            setBackground(defaultBg);
            setBorder(new EmptyBorder(2,5,2,5));
            setFont(new Font(Font.SANS_SERIF,Font.BOLD,11));
        }
        @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){setText((v==null)?"":v.toString());setBackground(s?defaultBg.brighter():defaultBg);return this;}
    }

    static class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private Runnable action;
        private Color bgColor;
        public ButtonEditor(JCheckBox c,Color bg,Runnable act){
            super(c);
            this.action=act;
            this.bgColor=bg;
            button=new JButton();
            button.setOpaque(true);
            button.setForeground(BUTTON_FG_DARK);
            button.setBackground(bgColor);
            button.setBorder(new EmptyBorder(2,5,2,5));
            button.setFont(new Font(Font.SANS_SERIF,Font.BOLD,11));
            button.addActionListener(e->fireEditingStopped());
        }
        @Override public Component getTableCellEditorComponent(JTable t,Object v,boolean s,int r,int c){label=(v==null)?"":v.toString();button.setText(label);isPushed=true;return button;}
        @Override public Object getCellEditorValue(){if(isPushed&&action!=null){action.run();}isPushed=false;return label;}
        @Override public boolean stopCellEditing(){isPushed=false;return super.stopCellEditing();}
        @Override protected void fireEditingStopped(){super.fireEditingStopped();}
    }
}