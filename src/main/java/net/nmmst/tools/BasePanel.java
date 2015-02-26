package net.nmmst.tools;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class BasePanel extends JPanel {
    public enum Mode{FULL_SCREEN, FILL, EXTENSION};
    private static final long serialVersionUID 	= -4475038995493795754L;
    private BufferedImage image = null;
    private Mode mode = Mode.FULL_SCREEN;
    public BasePanel() {}
    public BasePanel(BufferedImage image) {
        this.image = image;
    }
    public BasePanel(BufferedImage image, Mode mode) {
        this.image = image;
        this.mode = mode;
    }
    public void setMode(Mode mode) {
        this.mode = mode;
    }
    public void write(BufferedImage image) {
        if (image != null) {
            this.image = image;
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
                        final double scale = Math.min((double)panelW / (double)width, (double)panelH / (double)height);
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
                        final double scale = Math.max((double)panelW / (double)width, (double)panelH / (double)height);
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
