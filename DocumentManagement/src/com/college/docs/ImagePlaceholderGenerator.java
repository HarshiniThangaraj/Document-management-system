package com.college.docs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImagePlaceholderGenerator {

    /**
     * Generate a simple document-style PNG placeholder if the file does not exist.
     * @param path path to write (e.g., "resources/document.png")
     */
    public static void generateIfMissing(String path) {
        try {
            File f = new File(path);
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            if (f.exists()) return;

            int w = 400;
            int h = 520;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Background (transparent)
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g.setColor(new Color(255, 255, 255));
            g.fillRoundRect(20, 20, w - 40, h - 40, 16, 16);

            // Folded corner
            Polygon fold = new Polygon();
            fold.addPoint(w - 60, 20);
            fold.addPoint(w - 20, 60);
            fold.addPoint(w - 20, 20);
            g.setColor(new Color(235, 235, 235));
            g.fill(fold);

            // Border
            g.setColor(new Color(200, 200, 200));
            g.setStroke(new BasicStroke(3));
            g.drawRoundRect(20, 20, w - 40, h - 40, 16, 16);

            // Horizontal lines to mimic text
            g.setStroke(new BasicStroke(2));
            g.setColor(new Color(180, 180, 180));
            int startY = 80;
            for (int i = 0; i < 8; i++) {
                int y = startY + i * 44;
                int margin = 40;
                int lineW = w - margin * 2;
                if (i == 0) lineW = (int) (lineW * 0.7);
                g.drawLine(margin, y, margin + lineW, y);
            }

            g.dispose();
            ImageIO.write(img, "png", f);
        } catch (Exception e) {
            System.err.println("Could not generate placeholder image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generate a simple gradient background (JPEG) if missing.
     * @param path path to write (e.g., "resources/background.jpg")
     */
    public static void generateBackgroundIfMissing(String path) {
        try {
            File f = new File(path);
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            if (f.exists()) return;

            int w = 1920;
            int h = 1080;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Vertical gradient from deep blue to lighter teal
            GradientPaint gp = new GradientPaint(0, 0, new Color(18, 64, 114), 0, h, new Color(46, 134, 193));
            g.setPaint(gp);
            g.fillRect(0, 0, w, h);

            // Soft vignette for depth
            for (int i = 0; i < 200; i++) {
                float alpha = (float) (i / 400.0);
                g.setColor(new Color(0, 0, 0, (int) (alpha * 255)));
                int inset = i * 4;
                g.drawRect(inset, inset, w - inset * 2 - 1, h - inset * 2 - 1);
            }

            g.dispose();
            ImageIO.write(img, "jpg", f);
        } catch (Exception e) {
            System.err.println("Could not generate background image: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
