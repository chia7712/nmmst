package net.nmmst.tools;



import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class BasicPanel extends JPanel {
    public enum Mode{FULL_SCREEN, FILL, EXTENSION};
    private static final long serialVersionUID 	= -4475038995493795754L;
    private BufferedImage image = null;
    private Mode mode = Mode.FULL_SCREEN;
    public BasicPanel() {}
    public BasicPanel(BufferedImage image) {
        this.image = image;
    }
    public BasicPanel(BufferedImage image, Mode mode)
    {
        this.image 	= image;
        this.mode	= mode;
    }
    public void setMode(Mode mode)
    {
        this.mode = mode;
    }
    public void write(BufferedImage image)
    {
        if(image != null)
        {
            this.image = image;
            repaint();
        }
    }
    public void clearImage()
    {
        image = null;
        repaint();
    }
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if(image != null)
        {
            final int image_w = image.getWidth();
            final int image_h = image.getHeight();
            if(image_w != -1 && image_h != -1)
            {
                final int panelW = getWidth();
                final int panelH = getHeight();  
                switch(mode)
                {
                    case FULL_SCREEN:
                    {
                        final double 	scale 	= Math.min((double)panelW / (double)image_w, (double)panelH / (double)image_h);
                        final int 		final_w = (int)(image_w * scale);
                        final int 		final_h = (int)(image_h * scale);
                        final int		x_axis	= (int)((panelW - final_w) / 2);
                        final int		y_axis	= (int)((panelH - final_h) / 2);
                        g.drawImage(image, x_axis, y_axis, final_w, final_h, null);
                        break;
                    }
                    case FILL:
                    {
                        g.drawImage(image, 0, 0, panelW, panelH, null);
                        break;
                    }
                    case EXTENSION:
                    {
                        final double 	scale 	= Math.max((double)panelW / (double)image_w, (double)panelH / (double)image_h);
                        final int 		final_w = (int)(image_w * scale);
                        final int 		final_h = (int)(image_h * scale);
                        g.drawImage(image, 0, 0, final_w, final_h, null);
                        break;
                    }
                    default:
                        break;
                }
            }	 
        }
    }
}
