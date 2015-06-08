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
 * Utility mehods is used for painting some image when no specified image
 * in local disk.
 */
public interface Painter {
    /**
     * Loads a image from the local file.
     * The path is got from {@link NProperties} for the
     * specified key. If the path doesn't exisit, a image
     * drawed with the file name will return.
     * @param properties NProperties provides the default set
     * @param key The string to write
     * @return A image loaded from local file,
     * or a image drawed with file name
     */
    static BufferedImage loadOrStringImage(
            final NProperties properties, final String key) {
        return loadOrStringImage(
            new File(properties.getString(key)),
            properties.getInteger(NConstants.GENERATED_IMAGE_WIDTH),
            properties.getInteger(NConstants.GENERATED_IMAGE_HEIGHT),
            properties.getInteger(NConstants.GENERATED_FONT_SIZE));
    }
    /**
     * Loads a image from the local file.
     * The path is got from {@link NProperties} for the
     * specified key. If the path doesn't exisit, a image
     * drawed with the file name will return.
     * @param file The local file
     * @param width Image width
     * @param height Image height
     * @param fontSize Font size
     * @return A image loaded from local file,
     * or a image drawed with file name
     */
    static BufferedImage loadOrStringImage(final File file,
                                                 final int width,
                                                 final int height,
                                                 final int fontSize) {
        return loadOrStringImage(file,
                              file.getName(),
                              width,
                              height,
                              fontSize);
    }
    /**
     * Loads a image from the local file.
     * The path is got from {@link NProperties} for
     * the specified key. If the path doesn't exisit, a image
     * drawed with the file name will return.
     * @param file The local file
     * @param string The default string to draw
     * @param width Image width
     * @param height Image height
     * @param fontSize Font size
     * @return A image loaded from local file,
     * or a image drawed with file name
     */
    static BufferedImage loadOrStringImage(final File file,
                                                 final String string,
                                                 final int width,
                                                 final int height,
                                                 final int fontSize) {
        BufferedImage image = null;
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            return Painter.getStringImage(string, width, height, fontSize);
        }
    }
    /**
     * Draws some on the ori image.
     * @param oriImage Source image
     * @return The image is processed
     */
    BufferedImage paint(final BufferedImage oriImage);
    /**
     * Processes the image by a painter.
     * @param oriImage Source image
     * @param painter A painter is used for processing the image
     * @return The image is processed
     */
    static BufferedImage process(final BufferedImage oriImage,
                                       final Painter painter) {
        return process(oriImage, Arrays.asList(painter));
    }
    /**
     * Processes the image by the painters.
     * @param oriImage Source image
     * @param painters A list of painter is used for processing the image
     * @return The image is processed
     */
    static BufferedImage process(final BufferedImage oriImage,
                                       final List<Painter> painters) {
        BufferedImage dstImage = oriImage;
        for (Painter painter : painters) {
            dstImage = painter.paint(dstImage);
        }
        return dstImage;
    }
    /**
     * Creates a painter for cloneing the image.
     * @return A painter is able to clone image
     */
    static Painter getCopyPainter() {
        return (final BufferedImage oriImage) -> {
            int width = oriImage.getWidth();
            int height = oriImage.getHeight();
            BufferedImage dstImage = new BufferedImage(
                    width, height, oriImage.getType());
            Graphics2D g2d = (Graphics2D) dstImage.createGraphics();
            while (true) {
                if (g2d.drawImage(oriImage, 0, 0, width, height, null)) {
                    break;
                }
            }
            g2d.dispose();
            return dstImage;
        };
    }
    /**
     * Creates a image drawed a <code>string</code>.
     * @param str The string to draw
     * @param width Image width
     * @param height Image height
     * @param fontSize String size
     * @return A image drawed a <code>string</code>.
     */
    static BufferedImage getStringImage(final String str,
            final int width, final int height, final int fontSize) {
        BufferedImage image = new BufferedImage(
                width, height, BufferedImage.TYPE_3BYTE_BGR);
        return getStringImage(image, str, fontSize);
    }
    /**
     * Draws the <code>string</code> on the image.
     * @param image Source image
     * @param str String to draw
     * @param fontSize Font size
     * @return A image with specified string
     */
    static BufferedImage getStringImage(final BufferedImage image,
            final String str, final int fontSize) {
        Graphics2D g2d = (Graphics2D) image.createGraphics();
        g2d.setFont(new Font("Serif", Font.BOLD, fontSize));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(str,
                       (image.getWidth() - fm.stringWidth(str)) / 2,
                       image.getHeight() / 2);
        g2d.dispose();
        return image;
    }
    /**
     * Creates a image with pure color.
     * @param width Image width
     * @param height Image height
     * @param color The color to draw
     * @return A image with pure color
     */
     static BufferedImage getFillColor(final int width, final int height,
            final Color color) {
        BufferedImage image = new BufferedImage(
                width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(color);
        g.fill(new Rectangle(width, height));
        return image;
    }
}

