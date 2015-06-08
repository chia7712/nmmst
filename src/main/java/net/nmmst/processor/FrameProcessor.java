package net.nmmst.processor;

import java.awt.image.BufferedImage;
import java.util.Optional;
import net.nmmst.media.Frame;
/**
 * Processes the image in different phases.
 */
public interface FrameProcessor {
    /**
     * Initialzes this processor.
     */
    default void init() {
    }
    /**
     * Called after decoding a frame.
     * @param frame The frame to decode
     * @return Frame
     */
    default Optional<Frame> postDecodeFrame(Frame frame) {
        return Optional.ofNullable(frame);
    }
    /**
     * Called before drawing a image.
     * @param image The image to draw
     * @return Image
     */
    default Optional<BufferedImage> prePrintPanel(BufferedImage image) {
        return Optional.ofNullable(image);
    }
    /**
     * Called for ending up the play.
     * @param image The image to draw
     * @return Image
     */
    default Optional<BufferedImage> playOver(BufferedImage image) {
        return Optional.ofNullable(image);
    }
}
