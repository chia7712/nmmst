package tw.gov.nmmst.media;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
/**
 * Support three mode for showing the image on the panel.
 */
public class BasePanel extends JPanel {
    /**
     * Indicates how to paint specified image on the panel.
     */
    public enum Mode {
        /**
         * Full screen.
         */
        FULL_SCREEN,
        /**
         * Fill screen.
         */
        FILL,
        /**
         * Extends the image.
         */
        EXTENSION
    };
    /**
     * The image to draw.
     */
    private BufferedImage image = null;
    /**
     * The display mode.
     */
    private Mode mode = Mode.FULL_SCREEN;
    /**
     * Constructs a empty panel.
     */
    public BasePanel() {
    }
    /**
     * Constructs a panel with initial image.
     * @param writeImage The image to draw
     */
    public BasePanel(final BufferedImage writeImage) {
        image = writeImage;
    }
    /**
     * Constructs a panel with initial image and mode.
     * @param writeImage The image to draw
     * @param newMode The mode indicates the appearance for drawing image
     */
    public BasePanel(final BufferedImage writeImage, final Mode newMode) {
        image = writeImage;
        mode = newMode;
    }
    /**
     * Sets a new mode for this panel.
     * It does not repaint the image right now.
     * @param newMode New mode
     */
    public final void setMode(final Mode newMode) {
        mode = newMode;
    }
    /**
     * Draws a new image right now.
     * @param writeImage The image to draw
     */
    public final void write(final BufferedImage writeImage) {
        if (image != null) {
            image = writeImage;
            repaint();
        }
    }
    /**
     * Cleans the panle right now.
     */
    public final void clearImage() {
        image = null;
        repaint();
    }
    @Override
    public final void paintComponent(final Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            final int width = image.getWidth();
            final int height = image.getHeight();
            if (width != -1 && height != -1) {
                int xAxis = 0;
                int yAxis = 0;
                int finalW, finalH;
                switch (mode) {
                    case FULL_SCREEN:
                        double minScale = Math.min(
                                (double) getWidth() / (double) width,
                                (double) getHeight() / (double) height);
                        finalW = (int) (width * minScale);
                        finalH = (int) (height * minScale);
                        xAxis = (int) ((getWidth() - finalW) / 2);
                        yAxis = (int) ((getHeight() - finalH) / 2);
                        break;
                    case FILL:
                        finalW = getWidth();
                        finalH = getHeight();
                        break;
                    case EXTENSION:
                        double maxScale = Math.max(
                                (double) getWidth() / (double) width,
                                (double) getHeight() / (double) height);
                        finalW = (int) (width * maxScale);
                        finalH = (int) (height * maxScale);
                        break;
                    default:
                        return;
                }
                g.drawImage(image, xAxis, yAxis, finalW, finalH, null);
            }
            image.flush();
        }
    }
}
