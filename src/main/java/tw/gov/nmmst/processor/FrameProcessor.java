package tw.gov.nmmst.processor;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import tw.gov.nmmst.media.Frame;

/**
 * Processes the image in different phases.
 */
public interface FrameProcessor {

  /**
   * A empty processor.
   *
   * @return A do-nothing processor
   */
  static FrameProcessor empty() {
    return new FrameProcessor() {
    };
  }

  /**
   * Represents an ordered List of processor which will be evaluated.
   *
   * @param processors The processor to evaluate
   * @return An ordered list of processor
   */
  static FrameProcessor valueOf(final List<FrameProcessor> processors) {
    return new FrameProcessor() {
      @Override
      public void init() {
        processors.forEach(p -> p.init());
      }

      @Override
      public Optional<Frame> postDecodeFrame(final Frame frame) {
        Frame currentFrame = frame;
        for (FrameProcessor p : processors) {
          Optional<Frame> result = p.postDecodeFrame(currentFrame);
          if (result.isPresent()) {
            currentFrame = result.get();
          } else {
            return Optional.empty();
          }
        }
        return Optional.of(currentFrame);
      }

      @Override
      public Optional<BufferedImage> prePrintPanel(
              final BufferedImage image) {
        BufferedImage currentImage = image;
        for (FrameProcessor p : processors) {
          Optional<BufferedImage> result
                  = p.prePrintPanel(currentImage);
          if (result.isPresent()) {
            currentImage = result.get();
          } else {
            return Optional.empty();
          }
        }
        return Optional.of(currentImage);
      }

      @Override
      public Optional<BufferedImage> playOver(
              final BufferedImage image) {
        BufferedImage currentImage = image;
        for (FrameProcessor p : processors) {
          Optional<BufferedImage> result = p.playOver(currentImage);
          if (result.isPresent()) {
            currentImage = result.get();
          } else {
            return Optional.empty();
          }
        }
        return Optional.ofNullable(currentImage);
      }
    };
  }

  /**
   * Initialzes this processor.
   */
  default void init() {
  }

  /**
   * Called after decoding a frame.
   *
   * @param frame The frame to decode
   * @return Frame
   */
  default Optional<Frame> postDecodeFrame(Frame frame) {
    return Optional.of(frame);
  }

  /**
   * Called before drawing a image.
   *
   * @param image The image to draw
   * @return Image
   */
  default Optional<BufferedImage> prePrintPanel(BufferedImage image) {
    return Optional.of(image);
  }

  /**
   * Called for ending up the play.
   *
   * @param image The image to draw
   * @return Image
   */
  default Optional<BufferedImage> playOver(BufferedImage image) {
    return Optional.of(image);
  }
}
