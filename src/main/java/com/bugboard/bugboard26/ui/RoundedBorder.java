package com.bugboard.bugboard26.ui;

import javax.swing.border.Border;
import java.awt.*;

public class RoundedBorder implements Border {
    private final int radius;
    private final Color color;
    public RoundedBorder(int radius, Color color) {
        this.radius = radius;
        this.color  = color;
    }
    public Insets getBorderInsets(Component c) { return new Insets(radius/2, radius/2, radius/2, radius/2); }
    public boolean isBorderOpaque() { return false; }
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.drawRoundRect(x, y, w-1, h-1, radius, radius);
        g2.dispose();
    }
}
