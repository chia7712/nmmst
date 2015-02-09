package net.nmmst.tools;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public abstract class Painter {
    public static BufferedImage loadOrStringImage(File file, String string, int width, int height) {
        BufferedImage image = null; 
        if (file.exists()) {
            try {
                image = ImageIO.read(file);
            } catch (IOException e) {
            }
        }
        if (image == null) {
            image = Painter.getStringImage(string, width, height);
        }
        return image;
    }
    public abstract BufferedImage paint(BufferedImage oriImage);
    public static BufferedImage process(BufferedImage oriImage, Painter ... painters) {
        BufferedImage dst_image = newCopyPainter().paint(oriImage);
        for (Painter filter : painters) {
            dst_image = filter.paint(dst_image);
        }
        return dst_image;
    }
    public static Painter newTypePainter(final int imageType) {
        return new Painter() {
            @Override
            @SuppressWarnings("empty-statement")
            public BufferedImage paint(BufferedImage oriImage) {
                final int width = oriImage.getWidth();
                final int height = oriImage.getHeight();
                BufferedImage dst_image = new BufferedImage(width, height, imageType);
                Graphics2D g2d = (Graphics2D)dst_image.createGraphics();
                while (!g2d.drawImage(oriImage, 0, 0, width, height, null));
                g2d.dispose();
                return dst_image;
            }
        };
    }
    public static Painter newScalePainter(final double scale) {
        return new Painter() {
            @Override
            @SuppressWarnings("empty-statement")
            public BufferedImage paint(BufferedImage oriImage) {
                final int width = (int)((double)oriImage.getWidth() * scale);
                final int height = (int)((double)oriImage.getHeight() * scale);
                BufferedImage dst_image = new BufferedImage(width, height, oriImage.getType());
                Graphics2D g2d = (Graphics2D)dst_image.createGraphics();
                while (!g2d.drawImage(oriImage, 0, 0, width, height, null));
                g2d.dispose();
                return dst_image;
            }
        };
    }
    public static Painter newMirrorPainter() {
        return new Painter() {
            @Override
            @SuppressWarnings("empty-statement")
            public BufferedImage paint(BufferedImage oriImage) {
                final int width = oriImage.getWidth();
                final int height = oriImage.getHeight();
                BufferedImage dst_image = new BufferedImage(width, height, oriImage.getType());
                Graphics2D g2d = (Graphics2D)dst_image.createGraphics();
                while (!g2d.drawImage(oriImage, 0, 0, width - 1, height - 1, width - 1, 0, 0, height - 1, null));
                g2d.dispose();
                return dst_image;
            }

        };
    }
    public static Painter newScalePainter(final int maxLength) {
        return new Painter() {
            @Override
            @SuppressWarnings("empty-statement")
            public BufferedImage paint(BufferedImage oriImage) {
                final double scale = Math.min((double)(maxLength) / (double)oriImage.getWidth(), (double)(maxLength) / (double)oriImage.getHeight());
                final int width = (int)((double)oriImage.getWidth() * scale);
                final int height = (int)((double)oriImage.getHeight() * scale);
                BufferedImage dst_image = new BufferedImage(width, height, oriImage.getType());
                Graphics2D g2d = (Graphics2D)dst_image.createGraphics();
                while (!g2d.drawImage(oriImage, 0, 0, width, height, null));
                g2d.dispose();
                return dst_image;
            }
        };
    }
    public static Painter newScalePainter(final int dst_width, final int dst_height) {
        return new Painter() {
            @Override
            @SuppressWarnings("empty-statement")
            public BufferedImage paint(BufferedImage oriImage)  {
                BufferedImage dst_image = new BufferedImage(dst_width, dst_height, oriImage.getType());
                Graphics2D g2d = (Graphics2D)dst_image.createGraphics();
                while (!g2d.drawImage(oriImage, 0, 0, dst_width, dst_height, null));
                g2d.dispose();
                return dst_image;
            }
        };
    }
    private static Painter newCopyPainter() {
        return new Painter() {
            @Override
            @SuppressWarnings("empty-statement")
            public BufferedImage paint(BufferedImage oriImage) { 
                final int width = oriImage.getWidth();
                final int height = oriImage.getHeight();
                BufferedImage dst_image = new BufferedImage(width, height, oriImage.getType());
                Graphics2D g2d = (Graphics2D)dst_image.createGraphics();
                while (!g2d.drawImage(oriImage, 0, 0, width, height, null));
                g2d.dispose();
                return dst_image;
            }
        };
    }
    public static BufferedImage getStringImage(String str, int width, int height, int fontSize) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = (Graphics2D)image.createGraphics();
        g2d.setFont(new Font("Serif", Font.BOLD, fontSize));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(str, (image.getWidth() - fm.stringWidth(str)) / 2, image.getHeight() / 2);
        g2d.dispose();
        return image;
    }
    public static BufferedImage getStringImage(String str, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = (Graphics2D)image.createGraphics();
        g2d.setFont(new Font("Serif", Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(str, (image.getWidth() - fm.stringWidth(str)) / 2, image.getHeight() / 2);
        g2d.dispose();
        return image;
    }
    public static BufferedImage getStringImage(BufferedImage image, String str, int size) {
        image = Painter.process(image);
        Graphics2D g2d = (Graphics2D)image.createGraphics();
        g2d.setFont(new Font("Serif", Font.BOLD, size));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(str, (image.getWidth() - fm.stringWidth(str)) / 2, image.getHeight() / 2);
        g2d.dispose();
        return image;
    }
    public static BufferedImage fillColor(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = (Graphics2D)image.getGraphics();
        g.setColor(color);
        g.fill(new Rectangle(width, height));
        return image;
    }
}

