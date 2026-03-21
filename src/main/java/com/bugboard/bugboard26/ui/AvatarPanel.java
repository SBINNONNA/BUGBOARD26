package com.bugboard.bugboard26.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class AvatarPanel extends JPanel {

    private static String        currentAvatarUrl = null;
    private static BufferedImage cachedImage      = null; // ← cache statica

    private final int size;

    // ── chiamato da ProfiloDialog dopo upload ──
    public static void setAvatarUrl(String url) {
        if (url == null || url.equals(currentAvatarUrl)) return;
        currentAvatarUrl = url;
        cachedImage      = null;
        new Thread(() -> {
            try {
                BufferedImage img = ImageIO.read(new URL(url));
                if (img != null) {
                    cachedImage = img;
                    // ← sostituisci il lambda problematico con questo
                    SwingUtilities.invokeLater(() -> {
                        for (Window w : Window.getWindows()) {
                            w.repaint();
                        }
                    });
                }
            } catch (Exception ignored) {}
        }).start();
    }


    public AvatarPanel(int size) {
        this.size = size;
        setPreferredSize(new Dimension(size, size));
        setOpaque(false);

        // se URL già impostato ma immagine non ancora caricata
        if (currentAvatarUrl != null && cachedImage == null) {
            new Thread(() -> {
                try {
                    BufferedImage img = ImageIO.read(new URL(currentAvatarUrl));
                    if (img != null) {
                        cachedImage = img;
                        SwingUtilities.invokeLater(this::repaint);
                    }
                } catch (Exception ignored) {}
            }).start();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create(); // ← create() per non sporcare il contesto
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // sfondo circolare
        g2.setColor(new Color(100, 40, 160));
        g2.fillOval(0, 0, size, size);

        if (cachedImage != null) {
            // ritaglia in cerchio
            g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, size, size));
            g2.drawImage(cachedImage, 0, 0, size, size, this);
        } else {
            // fallback: lettera U
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, size / 2));
            FontMetrics fm = g2.getFontMetrics();
            String letter = "U";
            g2.drawString(letter,
                    (size - fm.stringWidth(letter)) / 2,
                    (size - fm.getHeight()) / 2 + fm.getAscent());
        }
        g2.dispose();
    }
}
