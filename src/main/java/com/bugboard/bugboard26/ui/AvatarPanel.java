package com.bugboard.bugboard26.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;



public class AvatarPanel extends JPanel {

    private BufferedImage avatarImage = null;
    private final int size;

    public AvatarPanel(int size) {
        this.size = size;
        setOpaque(false);
        setPreferredSize(new Dimension(size, size));
        try {
            // Carica da src/main/resources/images/avatar.png
            var stream = getClass().getResourceAsStream("/images/avatar.png");
            if (stream != null) avatarImage = ImageIO.read(stream);
        } catch (Exception ignored) {}
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Ellipse2D circle = new Ellipse2D.Float(2, 2, size - 4, size - 4);
        g2.setClip(circle);

        if (avatarImage != null) {
            g2.drawImage(avatarImage, 2, 2, size - 4, size - 4, null);
        } else {
            // Avatar generico
            g2.setColor(new Color(200, 170, 240));
            g2.fill(circle);
            g2.setColor(new Color(255, 255, 255, 180));
            // Testa
            g2.fillOval(size/2 - 14, size/2 - 20, 28, 28);
            // Corpo
            g2.fillOval(size/2 - 22, size/2 + 8, 44, 36);
        }

        g2.setClip(null);
        // Bordo bianco
        g2.setColor(new Color(255, 255, 255, 180));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval(2, 2, size - 4, size - 4);
        g2.dispose();
    }
}
