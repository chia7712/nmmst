package net.nmmst.media;

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
    public enum Mode{
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
    private BufferedImage image = null;
    private Mode mode = Mode.FULL_SCREEN;
    public BasePanel() {
    }
    public BasePanel(final BufferedImage writeImage) {
        image = writeImage;
    }
    public BasePanel(final BufferedImage writeImage, Mode newMode) {
        image = writeImage;
        mode = newMode;
    }
    public void setMode(Mode newMode) {
        mode = newMode;
    }
    public void write(BufferedImage writeImage) {
        if (image != null) {
            image = writeImage;
            repaint();
        }
    }
    public void clearImage() {
        image = null;
        repaint();
    }
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            final int width = image.getWidth();
            final int height = image.getHeight();
            if (width != -1 && height != -1) {
                final int panelW = getWidth();
                final int panelH = getHeight();  
                switch(mode) {
                    case FULL_SCREEN: {
                        final double scale = Math.min(
                                (double)panelW / (double)width,
                                (double)panelH / (double)height);
                        final int finalW = (int)(width * scale);
                        final int finalH = (int)(height * scale);
                        final int xAxis	= (int)((panelW - finalW) / 2);
                        final int yAxis	= (int)((panelH - finalH) / 2);
                        g.drawImage(image, xAxis, yAxis, finalW, finalH, null);
                        break;
                    }
                    case FILL: {
                        g.drawImage(image, 0, 0, panelW, panelH, null);
                        break;
                    }
                    case EXTENSION: {
                        final double scale = Math.max(
                                (double)panelW / (double)width,
                                (double)panelH / (double)height);
                        final int finalW = (int)(width * scale);
                        final int finalH = (int)(height * scale);
                        g.drawImage(image, 0, 0, finalW, finalH, null);
                        break;
                    }
                    default:
                        break;
                }
            }	 
            image.flush();
        }
    }
}
