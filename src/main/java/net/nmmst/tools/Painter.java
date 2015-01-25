package net.nmmst.tools;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public abstract class Painter 
{
    public abstract BufferedImage paint(BufferedImage ori_image);
    public static BufferedImage process(BufferedImage ori_image, Painter ... painters)
    {
        BufferedImage dst_image = newCopyPainter().paint(ori_image);
        for(Painter filter : painters)
        {
            dst_image = filter.paint(dst_image);
        }
        return dst_image;
    }
    public static Painter newTypePainter(final int image_type)
    {
        return new Painter()
        {
            @Override
            public BufferedImage paint(BufferedImage ori_image) 
            {
                final int width 	= ori_image.getWidth();
                final int height	= ori_image.getHeight();
                BufferedImage dst_image = new BufferedImage(width, height, image_type);
                Graphics2D g2d = (Graphics2D)dst_image.createGraphics();
                while(!g2d.drawImage(ori_image, 0, 0, width, height, null));
                g2d.dispose();
                return dst_image;
            }
        };
    }
    public static Painter newScalePainter(final double scale)
    {
        return new Painter()
        {
            @Override
            public BufferedImage paint(BufferedImage ori_image) 
            {
                final int width = (int)((double)ori_image.getWidth() * scale);
                final int height = (int)((double)ori_image.getHeight() * scale);
                BufferedImage dst_image = new BufferedImage(width, height, ori_image.getType());
                Graphics2D g2d = (Graphics2D)dst_image.createGraphics();
                while(!g2d.drawImage(ori_image, 0, 0, width, height, null));
                g2d.dispose();
                return dst_image;
            }
        };
    }
    public static Painter newMirrorPainter()
    {
        return new Painter()
        {

            @Override
            public BufferedImage paint(BufferedImage ori_image) 
            {
                final int width = ori_image.getWidth();
                final int height = ori_image.getHeight();
                BufferedImage dst_image = new BufferedImage(width, height, ori_image.getType());
                Graphics2D g2d = (Graphics2D)dst_image.createGraphics();
                while(!g2d.drawImage(ori_image, 0, 0, width - 1, height - 1, width - 1, 0, 0, height - 1, null));
                g2d.dispose();
                return dst_image;
            }

        };
    }
    public static Painter newScalePainter(final int max_length)
    {
        return new Painter()
        {

            @Override
            public BufferedImage paint(BufferedImage ori_image) 
            {
                final double scale = Math.min((double)(max_length) / (double)ori_image.getWidth(), (double)(max_length) / (double)ori_image.getHeight());
                final int width = (int)((double)ori_image.getWidth() * scale);
                final int height = (int)((double)ori_image.getHeight() * scale);
                BufferedImage dst_image = new BufferedImage(width, height, ori_image.getType());
                Graphics2D g2d = (Graphics2D)dst_image.createGraphics();
                while(!g2d.drawImage(ori_image, 0, 0, width, height, null));
                g2d.dispose();
                return dst_image;
            }
        };
    }
    public static Painter newScalePainter(final int dst_width, final int dst_height)
    {
        return new Painter()
        {

            @Override
            public BufferedImage paint(BufferedImage ori_image) 
            {
                BufferedImage dst_image = new BufferedImage(dst_width, dst_height, ori_image.getType());
                Graphics2D g2d = (Graphics2D)dst_image.createGraphics();
                while(!g2d.drawImage(ori_image, 0, 0, dst_width, dst_height, null));
                g2d.dispose();
                return dst_image;
            }

        };

    }
    private static Painter newCopyPainter()
    {
        return new Painter()
        {
            @Override
            public BufferedImage paint(BufferedImage ori_image) 
            {
                final int width = ori_image.getWidth();
                final int height = ori_image.getHeight();
                BufferedImage dst_image = new BufferedImage(width, height, ori_image.getType());
                Graphics2D g2d = (Graphics2D)dst_image.createGraphics();
                while(!g2d.drawImage(ori_image, 0, 0, width, height, null));
                g2d.dispose();
                return dst_image;
            }
        };
    }
    public static BufferedImage string(String str, int width, int height)
    {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = (Graphics2D)image.createGraphics();
        g2d.setFont(new Font("Serif", Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(str, (image.getWidth() - fm.stringWidth(str)) / 2, image.getHeight() / 2);
        g2d.dispose();
        return image;
    }
    public static BufferedImage string(BufferedImage image, String str, int size)
    {
        image = Painter.process(image);
        Graphics2D g2d = (Graphics2D)image.createGraphics();
        g2d.setFont(new Font("Serif", Font.BOLD, size));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(str, (image.getWidth() - fm.stringWidth(str)) / 2, image.getHeight() / 2);
        g2d.dispose();
        return image;
    }
    public static BufferedImage fillColor(int width, int height, Color color)
    {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = (Graphics2D)image.getGraphics();
        g.setColor(color);
        g.fill(new Rectangle(width, height));
        return image;
    }
}

