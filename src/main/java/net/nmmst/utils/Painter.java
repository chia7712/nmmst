package net.nmmst.utils;

import net.nmmst.NProperties;
import net.nmmst.NConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
/**
 * Utility mehods is used for painting some image when no specified image in local disk.
 */
public interface Painter {
    public static BufferedImage loadOrStringImage(
            final NProperties properties, final String key) {
        return loadOrStringImage(
            new File(properties.getString(key)),
            properties.getInteger(NConstants.GENERATED_IMAGE_WIDTH),
            properties.getInteger(NConstants.GENERATED_IMAGE_HEIGHT),
            properties.getInteger(NConstants.GENERATED_FONT_SIZE));
    }
    public static BufferedImage loadOrStringImage(final File file,
                                                 final int width,
                                                 final int height,
                                                 final int fontSize) {
        return loadOrStringImage(file,
                              file.getName(),
                              width,
                              height,
                              fontSize);
    }
    public static BufferedImage loadOrStringImage(final File file,
                                                 final String string,
                                                 final int width,
                                                 final int height,
                                                 final int fontSize) {
        BufferedImage image = null; 
        if (file.exists()) {
            try {
                image = ImageIO.read(file);
            } catch (IOException e) {
            }
        }
        if (image == null) {
            image = Painter.getStringImage(string, width, height, fontSize);
        }
        return image;
    }
    public abstract BufferedImage paint(final BufferedImage oriImage);
    public static BufferedImage process(final BufferedImage oriImage,
                                       final Painter painter) {
        return process(oriImage, Arrays.asList(painter));
    }
    public static BufferedImage process(final BufferedImage oriImage,
                                       final List<Painter> painters) {
        BufferedImage dstImage = oriImage;
        for (Painter filter : painters) {
            dstImage = filter.paint(dstImage);
        }
        return dstImage;
    }
    public static Painter getTypePainter(final int imageType) {
        return (BufferedImage oriImage) -> {
            final int width = oriImage.getWidth();
            final int height = oriImage.getHeight();
            BufferedImage dstImage = new BufferedImage(
                    width, height, imageType);
            Graphics2D g2d = (Graphics2D)dstImage.createGraphics();
            while (true) {
                if (g2d.drawImage(oriImage, 0, 0, width, height, null)) {
                    break;
                }
            }
            g2d.dispose();
            return dstImage;
        };
    }

    public static Painter getMirrorPainter() {
        return (BufferedImage oriImage) -> {
            final int width = oriImage.getWidth();
            final int height = oriImage.getHeight();
            BufferedImage dstImage = new BufferedImage(
                    width, height, oriImage.getType());
            Graphics2D g2d = (Graphics2D)dstImage.createGraphics();
            while (true) {
                if (g2d.drawImage(oriImage,
                        0,
                        0,
                        width - 1,
                        height - 1,
                        width - 1,
                        0,
                        0,
                        height - 1,
                        null)) {
                    break;
                }
            }
            g2d.dispose();
            return dstImage;
        };
    }
    public static Painter getScalePainter(final double scale) {
        return (BufferedImage oriImage) -> {
            final int width = (int)((double)oriImage.getWidth() * scale);
            final int height = (int)((double)oriImage.getHeight() * scale);
            BufferedImage dstImage = new BufferedImage(
                    width, height, oriImage.getType());
            Graphics2D g2d = (Graphics2D)dstImage.createGraphics();
            while (true) {
                if (g2d.drawImage(oriImage, 0, 0, width, height, null)) {
                    break;
                }
            }
            g2d.dispose();
            return dstImage;
        };
    }
    public static Painter getScalePainter(final int maxLength) {
        return (BufferedImage oriImage) -> {
            final double scale = Math.min(
                    (double)(maxLength) / (double)oriImage.getWidth(),
                    (double)(maxLength) / (double)oriImage.getHeight());
            final int width
                    = (int)((double)oriImage.getWidth() * scale);
            final int height
                    = (int)((double)oriImage.getHeight() * scale);
            BufferedImage dstImage = new BufferedImage(
                    width, height, oriImage.getType());
            Graphics2D g2d = (Graphics2D)dstImage.createGraphics();
            while (true) {
                if (g2d.drawImage(oriImage, 0, 0, width, height, null)) {
                    break;
                }
            }
            g2d.dispose();
            return dstImage;
        };
    }
    public static Painter getScalePainter(final int dstWidth,
                                        final int dstHeight) {
        return (BufferedImage oriImage) -> {
            BufferedImage dstImage = new BufferedImage(
                    dstWidth, dstHeight, oriImage.getType());
            Graphics2D g2d = (Graphics2D)dstImage.createGraphics();
            while (true) {
                if (g2d.drawImage(oriImage,
                        0,
                        0,
                        dstWidth,
                        dstHeight,
                        null)) {
                    break;
                }
            }
            g2d.dispose();
            return dstImage;
        };
    }
    public static Painter getCopyPainter() {
        return (final BufferedImage oriImage) -> {
            final int width = oriImage.getWidth();
            final int height = oriImage.getHeight();
            BufferedImage dstImage = new BufferedImage(
                    width, height, oriImage.getType());
            Graphics2D g2d = (Graphics2D)dstImage.createGraphics();
            while (true) {
                if (g2d.drawImage(oriImage, 0, 0, width, height, null)) {
                    break;
                }
            }
            g2d.dispose();
            return dstImage;
        };
    }
    public static BufferedImage getStringImage(final String str,
            final int width, final int height, final int fontSize) {
        BufferedImage image = new BufferedImage(
                width, height, BufferedImage.TYPE_3BYTE_BGR);
        return getStringImage(image, str, fontSize);
    }
    public static BufferedImage getStringImage(final BufferedImage image,
            final String str, final int fontSize) {
        Graphics2D g2d = (Graphics2D)image.createGraphics();
        g2d.setFont(new Font("Serif", Font.BOLD, fontSize));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(str,
                       (image.getWidth() - fm.stringWidth(str)) / 2,
                       image.getHeight() / 2);
        g2d.dispose();
        return image;
    }
    public static BufferedImage getFillColor(final int width, final int height,
            final Color color) {
        BufferedImage image = new BufferedImage(
                width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = (Graphics2D)image.getGraphics();
        g.setColor(color);
        g.fill(new Rectangle(width, height));
        return image;
    }
}

