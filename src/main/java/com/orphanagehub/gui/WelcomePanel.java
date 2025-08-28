/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WelcomePanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(WelcomePanel.class);

    private static final Color DARKBGSTART = new Color(45, 52, 54);
    private static final Color DARKBGEND = new Color(35, 42, 44);
    private static final Color TITLECOLOR_DARK = new Color(223, 230, 233);
    private static final Color BUTTONBG_DARK = new Color(99, 110, 114);
    private static final Color BUTTONFG_DARK = Color.WHITE;
    private static final Color BUTTONHOVER_BG_DARK = new Color(120, 130, 134);

    public WelcomePanel(Runnable registerAction, Runnable loginAction) {
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        contentPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Welcome to OrphanageHub");
        lblTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        lblTitle.setForeground(TITLECOLOR_DARK);

        JButton btnRegister = createButton("Register", registerAction);
        JButton btnLogin = createButton("Login", loginAction);

        contentPanel.add(lblTitle);
        contentPanel.add(btnRegister);
        contentPanel.add(btnLogin);

        add(contentPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        GradientPaint gp = new GradientPaint(0, 0, DARKBGSTART, 0, getHeight(), DARKBGEND);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private JButton createButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        button.setPreferredSize(new Dimension(150, 50));
        button.setForeground(BUTTONFG_DARK);
        button.setBackground(BUTTONBG_DARK);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new CompoundBorder(BorderFactory.createLineBorder(BUTTONBG_DARK.darker()), new EmptyBorder(5, 15, 5, 15)));
        button.addActionListener(e -> action.run());
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BUTTONHOVER_BG_DARK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(BUTTONBG_DARK);
            }
        });
        return button;
    }
}
