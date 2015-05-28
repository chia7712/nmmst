package net.nmmst.processor;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Optional;
import net.nmmst.media.Frame;
import net.nmmst.NodeInformation;
/**
 * Calculates the fusion range by linear format.
 * We decrease the RGB value for the image edge.
 * The processed edges for LU image are as follows:
 * <p>
 * |-------------------------------|-------------------------------|
 * |                              *|*                              |
 * |             LU               *|*             RU               |
 * |                              *|*                              |
 * |*******************************|*******************************|
 * |*******************************|*******************************|
 * |                              *|*                              |
 * |             LD               *|*             RD               |
 * |                              *|*                              |
 * |-------------------------------|-------------------------------|
 */
public class LinearProcessor implements FrameProcessor {
    /**
     * The start x coordinate from verical axis.
     */
    private final double xInitV;
    /**
     * The end x coordinate from verical axis.
     */
    private final double xFinalV;
    /**
     * The start y coordinate from verical axis.
     */
    private final double yInitV;
    /**
     * The end y coordinate from verical axis.
     */
    private final double yFinalV;
    /**
     * The start x coordinate from horizontal axis.
     */
    private final double xInitH;
    /**
     * The end x coordinate from horizontal axis.
     */
    private final double xFinalH;
    /**
     * The start y coordinate from horizontal axis.
     */
    private final double yInitH;
    /**
     * The end y coordinate from horizontal axis.
     */
    private final double yFinalH;
    /**
     * The equation for calculating the vertical edge.
     */
    private final LinearEquationInTwo xEquation;
    /**
     * The equation for calculating the horizontal edge.
     */
    private final LinearEquationInTwo yEquation;
    /**
     * The node location.
     */
    private final NodeInformation.Location location;
    public LinearProcessor(final NodeInformation.Location location,
            final Factor format) {
        this.location = location; 
        switch(location) {
            case LU:
                xEquation = new LinearEquationInTwo(
                        1.0 - format.getOverlayX(),
                        format.getScaleMaxX(),
                        1.0,
                        format.getScaleMinX());
                yEquation = new LinearEquationInTwo(
                        1.0 - format.getOverlayY(),
                        format.getScaleMaxY(),
                        1.0,
                        format.getScaleMinY());
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
                xEquation = new LinearEquationInTwo(
                        0.0,
                        format.getScaleMinX(),
                        format.getOverlayX(),
                        format.getScaleMaxX());
                yEquation = new LinearEquationInTwo(
                        1.0 - format.getOverlayY(),
                        format.getScaleMaxY(),
                        1.0,
                        format.getScaleMinY());
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
                xEquation = new LinearEquationInTwo(
                        1.0 - format.getOverlayX(),
                        format.getScaleMaxX(),
                        1.0,
                        format.getScaleMinX());
                yEquation = new LinearEquationInTwo(
                        0.0,
                        format.getScaleMinY(),
                        format.getOverlayY(),
                        format.getScaleMaxY());
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
                xEquation = new LinearEquationInTwo(
                        0.0,
                        format.getScaleMinX(),
                        format.getOverlayX(),
                        format.getScaleMaxX());
                yEquation = new LinearEquationInTwo(
                        0.0,
                        format.getScaleMinY(),
                        format.getOverlayY(),
                        format.getScaleMaxY());
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
                throw new IllegalArgumentException(
                    "A error location of LinearProcessor");
        }
    }
    /**
     * Fuses the edge for specified image. We rewrite the RGB value in
     * the source image instead of cloneing image.
     * @param image The source image is to be fused
     */
    public void process(final BufferedImage image) {
        final byte[] data
            = ((DataBufferByte)image.getRaster()
                                    .getDataBuffer())
                                    .getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        //Vertical, use xEquation
        for (int x = (int) (width * xInitV);
                x != (int)(width * xFinalV); ++x) {
            final double weight = xEquation.getY((double)x / (double)width);
            if (weight >= 1.0) {
                continue;
            }
            for (int y = (int) (height * yInitV);
                y != (int)(height * yFinalV); ++y) {
                final int rgbInit = (x + y * width) * 3;
                for (int rgbIndex = rgbInit;
                        rgbIndex != rgbInit + 3; ++rgbIndex) {
                    int value = (int)((data[rgbIndex] & 0xff) * weight);
                    data[rgbIndex] = (byte)value;
                }
            }
        }
        //horizontal, use yEquation
        for (int y = (int) (height * yInitH);
                y != (int)(height * yFinalH); ++y) {
            final double weight = yEquation.getY((double)y / (double)height);
            if (weight >= 1.0) {
                continue;
            }
            for (int x = (int) (width * xInitH);
                    x != (int)(width * xFinalH); ++x) {
                final int rgbInit = (x + y * width) * 3;
                for (int rgbIndex = rgbInit;
                    rgbIndex != rgbInit + 3; ++rgbIndex) {
                    int value = (int)((data[rgbIndex] & 0xff) * weight);
                    data[rgbIndex] = (byte)value;
                }
            }
        }
    }
    @Override
    public Optional<Frame> postDecodeFrame(final Frame frame) {
        if (frame != null) {
            process(frame.getImage());
        }
        return Optional.ofNullable(frame);
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
        return location.toString() + " " + getClass().getName();
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    /**
     * The factor of linear progrmmming is used for fusing the image edge. 
     */
    public static class Factor {
        /**
         * Overlay for x axis.
         */
        private final double overlayX;
        /**
         * Overlay for y axis.
         */
        private final double overlayY;
        /**
         * The scale of first x axis.
         */
        private final double scaleMinX;
        /**
         * The scale of end x axis.
         */
        private final double scaleMaxX;
        /**
         * The scale of first y axis.
         */
        private final double scaleMinY;
        /**
         * The scale of end y axis.
         */
        private final double scaleMaxY;
        public Factor(final double overlayX,
                final double overlayY,
                final double scaleMinX,
                final double scaleMaxX,
                final double scaleMinY,
                final double scaleMaxY) {
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
    /**
     * Calculates the RGB value for fusing the edge.
     * We use linear programming to evaluate the value.
     */
    private static class LinearEquationInTwo {
        /**
         * The arg A.
         */
        private final double argA;
        /**
         * The arg B.
         */
        private final double argB;
        /**
         * Constructs a linear equation for two specified coordinates.
         * The x represents the pixel location (x or y)
         * and the y represents the weight for RGB value.
         * @param x1 first x coordinate
         * @param y1 first y coordinate
         * @param x2 second x coordinate
         * @param y2 second y coordinate
         */
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
        /**
         * Calculate the weight for RGB value.
         * @param x The pixel location (x or y)
         * @return The weight for RGB value
         */
        public double getY(double x) {
            return argA * x + argB;
        }
    }
}
