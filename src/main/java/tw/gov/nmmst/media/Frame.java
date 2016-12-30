package tw.gov.nmmst.media;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

/**
 * Encapsulates the frame from movie.
 */
public final class Frame {

  /**
   * Converts bit to byte.
   */
  private static final int BIT_TO_BYTES = 8;
  /**
   * MovieAttribute.
   */
  private final MovieAttribute attribute;
  /**
   * BufferedImage.
   */
  private final BufferedImage image;
  /**
   * Timestamp.
   */
  private final long time;
  /**
   * Heapsize of image.
   */
  private final long imageSize;

  /**
   * Constructs a frame for specified move attribute, timestamp and image.
   *
   * @param attribute The attribute of {@link MovieStream}
   * @param time The micro timestamp
   * @param image Image
   */
  public Frame(final MovieAttribute attribute,
          final long time, final BufferedImage image) {
    this.attribute = attribute;
    this.time = time;
    this.image = image;
    DataBuffer buff = image.getRaster().getDataBuffer();
    this.imageSize = buff.getSize()
            * DataBuffer.getDataTypeSize(buff.getDataType())
            / BIT_TO_BYTES;
  }

  /**
   * @return A reference to movie attribute
   */
  public MovieAttribute getMovieAttribute() {
    return attribute;
  }

  /**
   * @return This size of image
   */
  public long getHeapSize() {
    return imageSize;
  }

  /**
   * @return This image is got from movie
   */
  public BufferedImage getImage() {
    return image;
  }

  /**
   * The micro timestamp for this frame.
   *
   * @return The micro timestamp
   */
  public long getTimestamp() {
    return time;
  }
}
