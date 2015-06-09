package tw.gov.nmmst.processor;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Optional;
import tw.gov.nmmst.media.Frame;
import tw.gov.spright.nmmst.NodeInformation;
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
    /**
     * Constructs a linear processor for specified location and factor.
     * @param nodeLocation Node location
     * @param factor The format factor
     */
    public LinearProcessor(final NodeInformation.Location nodeLocation,
            final Factor factor) {
        location = nodeLocation;
        switch (nodeLocation) {
            case LU:
                xEquation = new LinearEquationInTwo(
                        1.0 - factor.getOverlayX(),
                        factor.getScaleMaxX(),
                        1.0,
                        factor.getScaleMinX());
                yEquation = new LinearEquationInTwo(
                        1.0 - factor.getOverlayY(),
                        factor.getScaleMaxY(),
                        1.0,
                        factor.getScaleMinY());
                xInitV = 1.0 - factor.getOverlayX();
                xFinalV = 1.0;
                yInitV = 0;
                yFinalV = 1.0;
                xInitH = 0;
                xFinalH = 1.0;
                yInitH = 1.0 - factor.getOverlayY();
                yFinalH = 1.0;
                break;
            case RU:
                xEquation = new LinearEquationInTwo(
                        0.0,
                        factor.getScaleMinX(),
                        factor.getOverlayX(),
                        factor.getScaleMaxX());
                yEquation = new LinearEquationInTwo(
                        1.0 - factor.getOverlayY(),
                        factor.getScaleMaxY(),
                        1.0,
                        factor.getScaleMinY());
                xInitV = 0;
                xFinalV = factor.getOverlayX();
                yInitV = 0;
                yFinalV = 1.0;
                xInitH = 0;
                xFinalH = 1.0;
                yInitH = 1.0 - factor.getOverlayY();
                yFinalH = 1.0;
                break;
            case LD:
                xEquation = new LinearEquationInTwo(
                        1.0 - factor.getOverlayX(),
                        factor.getScaleMaxX(),
                        1.0,
                        factor.getScaleMinX());
                yEquation = new LinearEquationInTwo(
                        0.0,
                        factor.getScaleMinY(),
                        factor.getOverlayY(),
                        factor.getScaleMaxY());
                xInitV = 1.0 - factor.getOverlayX();
                xFinalV = 1.0;
                yInitV = 0;
                yFinalV = 1.0;
                xInitH = 0;
                xFinalH = 1.0;
                yInitH = 0;
                yFinalH = factor.getOverlayY();
                break;
            case RD:
                xEquation = new LinearEquationInTwo(
                        0.0,
                        factor.getScaleMinX(),
                        factor.getOverlayX(),
                        factor.getScaleMaxX());
                yEquation = new LinearEquationInTwo(
                        0.0,
                        factor.getScaleMinY(),
                        factor.getOverlayY(),
                        factor.getScaleMaxY());
                xInitV = 0;
                xFinalV = factor.getOverlayX();
                yInitV = 0;
                yFinalV = 1.0;
                xInitH = 0;
                xFinalH = 1.0;
                yInitH = 0;
                yFinalH = factor.getOverlayY();
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
    public final void process(final BufferedImage image) {
        final byte[] data
            = ((DataBufferByte) image.getRaster()
                                    .getDataBuffer())
                                    .getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        //Vertical, use xEquation
        for (int x = (int) (width * xInitV);
                x != (int) (width * xFinalV); ++x) {
            final double weight = xEquation.getY((double) x / (double) width);
            if (weight >= 1.0) {
                continue;
            }
            for (int y = (int) (height * yInitV);
                y != (int) (height * yFinalV); ++y) {
                final int rgbInit = (x + y * width) * 3;
                for (int rgbIndex = rgbInit;
                        rgbIndex != rgbInit + 3; ++rgbIndex) {
                    int value = (int) ((data[rgbIndex] & 0xff) * weight);
                    data[rgbIndex] = (byte) value;
                }
            }
        }
        //horizontal, use yEquation
        for (int y = (int) (height * yInitH);
                y != (int) (height * yFinalH); ++y) {
            final double weight = yEquation.getY((double) y / (double) height);
            if (weight >= 1.0) {
                continue;
            }
            for (int x = (int) (width * xInitH);
                    x != (int) (width * xFinalH); ++x) {
                final int rgbInit = (x + y * width) * 3;
                for (int rgbIndex = rgbInit;
                    rgbIndex != rgbInit + 3; ++rgbIndex) {
                    int value = (int) ((data[rgbIndex] & 0xff) * weight);
                    data[rgbIndex] = (byte) value;
                }
            }
        }
    }
    @Override
    public final Optional<Frame> postDecodeFrame(final Frame frame) {
        if (frame != null) {
            process(frame.getImage());
        }
        return Optional.ofNullable(frame);
    }
    @Override
    public final boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != LinearProcessor.class) {
            return false;
        }
        return ((LinearProcessor) obj).location == location;
    }
    @Override
    public final String toString() {
        return location.toString() + " " + getClass().getName();
    }
    @Override
    public final int hashCode() {
        return toString().hashCode();
    }
    /**
     * The factor of linear progrmmming is used for fusing the image edge.
     * Each argument is represented as following:
     * min scale        max scale
     *     |-------------|
     *         overlay
     */
    public static class Factor {
        /**
         * Overlay for x axis.
         */
        private final double coverX;
        /**
         * Overlay for y axis.
         */
        private final double coverY;
        /**
         * The scale of first x axis.
         */
        private final double minX;
        /**
         * The scale of end x axis.
         */
        private final double maxX;
        /**
         * The scale of first y axis.
         */
        private final double minY;
        /**
         * The scale of end y axis.
         */
        private final double maxY;
        /**
         * Constructs a factor for individual parameters.
         * @param overlayX Overlay range for x axis
         * @param overlayY Overlay range for y axis
         * @param scaleMinX The min scale of x axis
         * @param scaleMaxX The max scale of x axis
         * @param scaleMinY The min scale of y axis
         * @param scaleMaxY The max scale of y axis
         */
        public Factor(final double overlayX,
                final double overlayY,
                final double scaleMinX,
                final double scaleMaxX,
                final double scaleMinY,
                final double scaleMaxY) {
            coverX = overlayX;
            coverY = overlayY;
            minX = scaleMinX;
            maxX = scaleMaxX;
            minY = scaleMinY;
            maxY = scaleMaxY;
        }
        /**
         * @return Overlay for x axis
         */
        public final double getOverlayX() {
            return coverX;
        }
        /**
         * @return Overlay for y axis
         */
        public final double getOverlayY() {
            return coverY;
        }
        /**
         * @return The min scale of x axis
         */
        public final double getScaleMinX() {
            return minX;
        }
        /**
         * @return The max scale of x axis
         */
        public final double getScaleMaxX() {
            return maxX;
        }
        /**
         * @return The min scale of y axis
         */
        public final double getScaleMinY() {
            return minY;
        }
        /**
         * @return The max scale of y axis
         */
        public final double getScaleMaxY() {
            return maxY;
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
        public LinearEquationInTwo(final double x1, final double y1,
            final double x2, final double y2) {
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
        public double getY(final double x) {
            return argA * x + argB;
        }
    }
}
