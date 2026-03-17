package com.bugboard.bugboard26.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class LogoPanel extends JPanel {

    private final boolean large;

    public LogoPanel(boolean large) {
        this.large = large;
        int size = large ? 120 : 44;
        setPreferredSize(new Dimension(large ? 160 : 100, size));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int fontSize = large ? 48 : 20;

        // Glow effect
        for (int i = 8; i > 0; i--) {
            g2.setColor(new Color(160, 32, 240, 15 * i));
            g2.setFont(new Font("SansSerif", Font.BOLD, fontSize + i));
            FontMetrics fm = g2.getFontMetrics();
            int x = (w - fm.stringWidth("B") * 2) / 2;
            int y = h / 2 + fm.getAscent() / 2 - (large ? 15 : 6);
            g2.drawString("B", x, y);
        }

        // "B" normale (sinistra)
        g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        int totalW = fm.stringWidth("B") + fm.stringWidth("Я") + (large ? 4 : 2);
        int startX = (w - totalW) / 2;
        int baseY = h / 2 + fm.getAscent() / 2 - (large ? 15 : 6);

        // Gradient viola per "B"
        GradientPaint gp = new GradientPaint(
                startX, baseY - fontSize, new Color(180, 80, 255),
                startX, baseY, new Color(120, 0, 200)
        );
        g2.setPaint(gp);
        g2.drawString("B", startX, baseY);

        // "Я" speculare (destra) — disegnato con AffineTransform
        int bWidth = fm.stringWidth("B");
        AffineTransform old = g2.getTransform();
        g2.translate(startX + bWidth * 2 + (large ? 4 : 2), 0);
        g2.scale(-1, 1);
        GradientPaint gp2 = new GradientPaint(
                0, baseY - fontSize, new Color(200, 100, 255),
                0, baseY, new Color(140, 20, 220)
        );
        g2.setPaint(gp2);
        g2.drawString("B", 0, baseY);
        g2.setTransform(old);

        // "26" in bianco/viola chiaro
        int numSize = large ? 22 : 10;
        g2.setFont(new Font("SansSerif", Font.BOLD, numSize));
        FontMetrics fm2 = g2.getFontMetrics();
        String num = "26";
        int numX = (w - fm2.stringWidth(num)) / 2;
        int numY = baseY + (large ? 28 : 12);
        g2.setColor(new Color(230, 200, 255));
        g2.drawString(num, numX, numY);

        g2.dispose();
    }
}
