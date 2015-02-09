package net.nmmst.processor;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.Serializable;

import net.nmmst.movie.Frame;
import net.nmmst.player.PlayerInformation;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class LinearProcessor implements FrameProcessor {
    private final double xInitV;
    private final double xFinalV;
    private final double yInitV;
    private final double yFinalV;
    private final double xInitH;
    private final double xFinalH;
    private final double yInitH;
    private final double yFinalH;
    private final LinearEquationInTwo xEquation;
    private final LinearEquationInTwo  yEquation;
    private final PlayerInformation.Location location;
    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(obj instanceof LinearProcessor) {
            if (((LinearProcessor) obj).location == location) {
                return true;
            }
        }
        return false;
    }
    @Override
    public String toString() {
        return location.toString() + " LinearIFrameProcessor";
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    public LinearProcessor(PlayerInformation.Location location, Format format) {
        this.location = location; 
        switch(location) {
            case LU:
                xEquation = new LinearEquationInTwo(1.0 - format.getXOverlay(), format.getXScaleMax(), 1.0, format.getXScaleMin());
                yEquation = new LinearEquationInTwo(1.0 - format.getYOverlay(), format.getYScaleMax(), 1.0, format.getYScaleMin());
                xInitV	= 1.0 - format.getXOverlay();
                xFinalV	= 1.0;
                yInitV	= 0;
                yFinalV	= 1.0;
                xInitH	= 0;
                xFinalH	= 1.0;
                yInitH	= 1.0 - format.getYOverlay();
                yFinalH	= 1.0;
                break;
            case RU:
                xEquation = new LinearEquationInTwo(0.0, format.getXScaleMin(), format.getXOverlay(), format.getXScaleMax());
                yEquation = new LinearEquationInTwo(1.0 - format.getYOverlay(), format.getYScaleMax(), 1.0, format.getYScaleMin());
                xInitV	= 0;
                xFinalV	= format.getXOverlay();
                yInitV	= 0;
                yFinalV	= 1.0;
                xInitH	= 0;
                xFinalH	= 1.0;
                yInitH	= 1.0 - format.getYOverlay();
                yFinalH	= 1.0;
                break;
            case LD:
                xEquation = new LinearEquationInTwo(1.0 - format.getXOverlay(), format.getXScaleMax(), 1.0, format.getXScaleMin());
                yEquation = new LinearEquationInTwo(0.0, format.getYScaleMin(), format.getYOverlay(), format.getYScaleMax());
                xInitV	= 1.0 - format.getXOverlay();
                xFinalV	= 1.0;
                yInitV	= 0;
                yFinalV	= 1.0;
                xInitH	= 0;
                xFinalH	= 1.0;
                yInitH	= 0;
                yFinalH	= format.getYOverlay();
                break;
            case RD:
                xEquation = new LinearEquationInTwo(0.0, format.getXScaleMin(), format.getXOverlay(), format.getXScaleMax());
                yEquation = new LinearEquationInTwo(0.0, format.getYScaleMin(), format.getYOverlay(), format.getYScaleMax());
                xInitV	= 0;
                xFinalV	= format.getXOverlay();
                yInitV	= 0;
                yFinalV	= 1.0;
                xInitH	= 0;
                xFinalH	= 1.0;
                yInitH	= 0;
                yFinalH	= format.getYOverlay();
                break;
            default:
                xEquation = null;
                yEquation = null;
                xInitV	= 0;
                xFinalV	= 0;
                yInitV	= 0;
                yFinalV	= 0;
                xInitH	= 0;
                xFinalH	= 0;
                yInitH	= 0;
                yFinalH	= 0;
                break;
        }
    }
    public void process(BufferedImage image) {
        final byte[] data = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        //Vertical, use xEquation
        for (int x = (int) (width * xInitV); x != (int)(width * xFinalV); ++x) {
            final double weight = xEquation.getY((double)x / (double)width);
            if(weight >= 1.0) {
                continue;
            }
            for (int y = (int) (height * yInitV); y != (int)(height * yFinalV); ++y) {
                final int rgb_init = (x + y * width) * 3;
                for (int rgb_index = rgb_init; rgb_index != rgb_init + 3; ++rgb_index) {
                    int value = (int)((data[rgb_index] & 0xff) * weight);
                    data[rgb_index] = (byte)value;
                }
            }
        }
        //horizontal, use yEquation
        for (int y = (int) (height * yInitH); y != (int)(height * yFinalH); ++y) {
            final double weight = yEquation.getY((double)y / (double)height);
            if(weight >= 1.0) {
                continue;
            }
            for (int x = (int) (width * xInitH); x != (int)(width * xFinalH); ++x) {
                final int rgb_init = (x + y * width) * 3;
                for (int rgb_index = rgb_init; rgb_index != rgb_init + 3; ++rgb_index) {
                    int value = (int)((data[rgb_index] & 0xff) * weight);
                    data[rgb_index] = (byte)value;
                }
            }
        }
    }
    @Override
    public synchronized void process(Frame frame) {
        process(frame.getImage());
    }
    @Override
    public boolean needProcess(Frame frame) {
        return true;
    }
    public static class Format implements Serializable {
        private static final long serialVersionUID = -2453141672510568349L;
        private final double x_overlay;
        private final double y_overlay;
        private final double x_scale_min;
        private final double x_scale_max;
        private final double y_scale_min;
        private final double y_scale_max;
        public Format(double x_overlay, double y_overlay, double x_scale_min, double x_scale_max, double y_scale_min, double y_scale_max) {
            this.x_overlay = x_overlay;
            this.y_overlay = y_overlay;
            this.x_scale_min = x_scale_min;
            this.x_scale_max = x_scale_max;
            this.y_scale_min = y_scale_min;
            this.y_scale_max = y_scale_max;
        }
        public double getXOverlay() {
            return x_overlay;
        }
        public double getYOverlay() {
            return y_overlay;
        }
        public double getXScaleMin() {
            return x_scale_min;
        }
        public double getXScaleMax() {
            return x_scale_max;
        }
        public double getYScaleMin() {
            return y_scale_min;
        }
        public double getYScaleMax() {
            return y_scale_max;
        }
    }
    private static class LinearEquationInTwo {
        private final double argA;
        private final double argB;
        public LinearEquationInTwo(double x1, double y1, double x2, double y2) {
            if(x1 == x2 && y1 == y2) {
                throw new IllegalArgumentException();
            } else if(x1 == x2) {
                argA = 0;
                argB = 0;
            } else {
                argA = (y1 - y2) / (x1 - x2);
                argB = y1 - argA * x1;
            }
        }
        public double getY(double x) {
            return argA * x + argB;
        }
    }
}
