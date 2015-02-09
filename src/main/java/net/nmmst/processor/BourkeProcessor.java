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
public class BourkeProcessor implements FrameProcessor {
    private final double xMinV;
    private final double xMaxV;
    private final double yMinV;
    private final double yMaxV;
    private final double xMinH;
    private final double xMaxH;
    private final double yMinH;
    private final double yMaxH;
    private final double curvature;
    private final double gamma;
    private final PlayerInformation.Location location;
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof BourkeProcessor) {
            if (((BourkeProcessor) obj).location == location) {
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
    public BourkeProcessor(PlayerInformation.Location location, Format format) {
        this.location = location; 
        this.curvature = format.getCurvature();
        this.gamma = format.getGamme();
        switch(location) {
            case LU:
                xMinV = 1.0 - format.getXOverlay();
                xMaxV = 1.0;
                yMinV = 0;
                yMaxV = 1.0;
                xMinH = 0;
                xMaxH = 1.0;
                yMinH = 1.0 - format.getYOverlay();
                yMaxH = 1.0;
                break;
            case RU:
                xMinV = 0;
                xMaxV = format.getXOverlay();
                yMinV = 0;
                yMaxV = 1.0;
                xMinH = 0;
                xMaxH = 1.0;
                yMinH = 1.0 - format.getYOverlay();
                yMaxH = 1.0;
                break;
            case LD:
                xMinV = 1.0 - format.getXOverlay();
                xMaxV = 1.0;
                yMinV = 0;
                yMaxV = 1.0;
                xMinH = 0;
                xMaxH = 1.0;
                yMinH = 0;
                yMaxH = format.getYOverlay();
                break;
            case RD:
                xMinV = 0;
                xMaxV = format.getXOverlay();
                yMinV = 0;
                yMaxV = 1.0;
                xMinH = 0;
                xMaxH = 1.0;
                yMinH = 0;
                yMaxH = format.getYOverlay();
                break;
            default:
                throw new IllegalArgumentException();
        }
    }
    public void process(BufferedImage image) {
        final byte[] data = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
        final int width	= image.getWidth();
        final int height = image.getHeight();

        for (int x = (int) (width * xMinV); x != (int)(width * xMaxV); ++x) {
            double normalization = normalized((double)x / (double)width, xMaxV, xMinV);		
            if (normalization > 1 || normalization < 0) {
                continue;
            }
            switch(location) {
                case LD:
                    break;
                case LU:
                    break;
                case RD:
                    normalization = 1.0 - normalization;
                    break;
                case RU:
                    normalization = 1.0 - normalization;
                    break;
                default:
                    break;
            }
            double weight = 0;
            if (normalization >= 0.5) {
                weight = rightEquation(normalization);
            } else {
                weight = leftEquation(normalization);
            }
            if (weight >= 1 || weight < 0) {
                continue;
            }
            for (int y = (int) (height * yMinV); y != (int)(height * yMaxV); ++y) {
                final int rgb_init = (x + y * width) * 3;
                for (int rgb_index = rgb_init; rgb_index != rgb_init + 3; ++rgb_index) {
                    int value = (int)((data[rgb_index] & 0xff) * weight);
                    data[rgb_index] = (byte)value;
                }
            }
        }
        for (int y = (int) (height * yMinH); y != (int)(height * yMaxH); ++y) {
            double normalization = normalized((double)y / (double)height, yMaxH, yMinH);
            double weight = 0;
            if (normalization > 1) {
                continue;
            } else if (normalization > 0.5) {
                weight = rightEquation(normalization);
            } else if (normalization >= 0) {
                weight = leftEquation(normalization);
            } else {
                continue;
            }
            if (weight >= 1) {
                continue;
            }
            for (int x = (int) (width * xMinH); x != (int)(width * xMaxH); ++x) {
                final int rgb_init = (x + y * width) * 3;
                for (int rgb_index = rgb_init; rgb_index != rgb_init + 3; ++rgb_index) {
                    int value = (int)((data[rgb_index] & 0xff) * weight);
                    data[rgb_index] = (byte)value;
                }
            }
        }
    }
    private double leftEquation(double value) {
        if (value < 0 || value >= 0.5) {
            System.out.println(value);
            throw new IllegalArgumentException();
        }
        return gamma * Math.pow(2 * value, curvature);
    }
    private double rightEquation(double value) {
        if (value < 0.5 || value > 1) {
            System.out.println(value);
            throw new IllegalArgumentException();
        }
        return 1 - (1 - gamma) * Math.pow((2 * (1 - value)), curvature);
    }
    private static double normalized(double value, double max, double min) {
        //return value;
        return (value - min) / (max - min);
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
        private final double xOverlay;
        private final double yOverlay;
        private final double curvature;
        private final double gamma;
        public Format(double xOverlay, double yOverlay, double curvature, double gamma) {
            this.xOverlay = xOverlay;
            this.yOverlay = yOverlay;
            this.curvature = curvature;
            this.gamma = gamma;
        }
        public double getYOverlay() {
            return yOverlay;
        }
        public double getXOverlay() {
            return xOverlay;
        }
        public double getCurvature() {
            return curvature;
        }
        public double getGamme() {
            return gamma;
        }
    }
}
