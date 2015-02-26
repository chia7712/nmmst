package net.nmmst.processor;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import net.nmmst.movie.Frame;
import net.nmmst.player.NodeInformation;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class LinearProcessor implements TimeFrameProcessor {
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
    private final NodeInformation.Location location;
    private final List<TimeLocation> timeRange = new LinkedList();
    public LinearProcessor(NodeInformation.Location location, Format format, TimeLocation ... timeLocations) {
        this.location = location; 
        timeRange.addAll(Arrays.asList(timeLocations));
        switch(location) {
            case LU:
                xEquation = new LinearEquationInTwo(1.0 - format.getOverlayX(), format.getScaleMaxX(), 1.0, format.getScaleMinX());
                yEquation = new LinearEquationInTwo(1.0 - format.getOverlayY(), format.getScaleMaxY(), 1.0, format.getScaleMinY());
                xInitV	= 1.0 - format.getOverlayX();
                xFinalV	= 1.0;
                yInitV	= 0;
                yFinalV	= 1.0;
                xInitH	= 0;
                xFinalH	= 1.0;
                yInitH	= 1.0 - format.getOverlayY();
                yFinalH	= 1.0;
                break;
            case RU:
                xEquation = new LinearEquationInTwo(0.0, format.getScaleMinX(), format.getOverlayX(), format.getScaleMaxX());
                yEquation = new LinearEquationInTwo(1.0 - format.getOverlayY(), format.getScaleMaxY(), 1.0, format.getScaleMinY());
                xInitV	= 0;
                xFinalV	= format.getOverlayX();
                yInitV	= 0;
                yFinalV	= 1.0;
                xInitH	= 0;
                xFinalH	= 1.0;
                yInitH	= 1.0 - format.getOverlayY();
                yFinalH	= 1.0;
                break;
            case LD:
                xEquation = new LinearEquationInTwo(1.0 - format.getOverlayX(), format.getScaleMaxX(), 1.0, format.getScaleMinX());
                yEquation = new LinearEquationInTwo(0.0, format.getScaleMinY(), format.getOverlayY(), format.getScaleMaxY());
                xInitV	= 1.0 - format.getOverlayX();
                xFinalV	= 1.0;
                yInitV	= 0;
                yFinalV	= 1.0;
                xInitH	= 0;
                xFinalH	= 1.0;
                yInitH	= 0;
                yFinalH	= format.getOverlayY();
                break;
            case RD:
                xEquation = new LinearEquationInTwo(0.0, format.getScaleMinX(), format.getOverlayX(), format.getScaleMaxX());
                yEquation = new LinearEquationInTwo(0.0, format.getScaleMinY(), format.getOverlayY(), format.getScaleMaxY());
                xInitV	= 0;
                xFinalV	= format.getOverlayX();
                yInitV	= 0;
                yFinalV	= 1.0;
                xInitH	= 0;
                xFinalH	= 1.0;
                yInitH	= 0;
                yFinalH	= format.getOverlayY();
                break;
            default:
                throw new IllegalArgumentException("A error location of LinearProcessor");
        }
    }
    public void process(BufferedImage image) {
        final byte[] data = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        //Vertical, use xEquation
        for (int x = (int) (width * xInitV); x != (int)(width * xFinalV); ++x) {
            final double weight = xEquation.getY((double)x / (double)width);
            if (weight >= 1.0) {
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
            if (weight >= 1.0) {
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
    public void setTimeLocation(List<TimeLocation> timeLocations){
        synchronized(timeRange) {
            timeRange.clear();
            timeRange.addAll(timeLocations);
        }
    }
    @Override
    public void process(Frame frame) {
        process(frame.getImage());
    }
    @Override
    public boolean needProcess(Frame frame) {
        synchronized(timeRange) {
            if (timeRange.isEmpty()) {
                return true;
            }
            return timeRange.stream().anyMatch((timeLocation) -> (timeLocation.include(frame)));
        }
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof LinearProcessor) {
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
    public static class Format implements Serializable {
        private static final long serialVersionUID = -2453141672510568349L;
        private final double overlayX;
        private final double overlayY;
        private final double scaleMinX;
        private final double scaleMaxX;
        private final double scaleMinY;
        private final double scaleMaxY;
        public Format(double overlayX, double overlayY, double scaleMinX, double scaleMaxX, double scaleMinY, double scaleMaxY) {
            this.overlayX = overlayX;
            this.overlayY = overlayY;
            this.scaleMinX = scaleMinX;
            this.scaleMaxX = scaleMaxX;
            this.scaleMinY = scaleMinY;
            this.scaleMaxY = scaleMaxY;
        }
        public double getOverlayX() {
            return overlayX;
        }
        public double getOverlayY() {
            return overlayY;
        }
        public double getScaleMinX() {
            return scaleMinX;
        }
        public double getScaleMaxX() {
            return scaleMaxX;
        }
        public double getScaleMinY() {
            return scaleMinY;
        }
        public double getScaleMaxY() {
            return scaleMaxY;
        }
    }
    private static class LinearEquationInTwo {
        private final double argA;
        private final double argB;
        public LinearEquationInTwo(double x1, double y1, double x2, double y2) {
            if (x1 == x2 && y1 == y2) {
                throw new IllegalArgumentException();
            } else if (x1 == x2) {
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
