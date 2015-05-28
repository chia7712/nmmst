package net.nmmst.media;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
/**
 * Encapsulates the frame from movie.
 */
public class Frame {
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
    private final boolean end;
    public Frame() {
        imageSize = 0;
        time = 0;
        attribute = null;
        image = null;
        end = true;
    }
    /**
     * Constructs a frame for specified move attribute, timestamp and image.
     * @param movieAttribute The attribute of {@link MovieStream}
     * @param timestamp The micro timestamp
     * @param bufferedImage Image
     */
    public Frame(final MovieAttribute movieAttribute,
            final long timestamp, final BufferedImage bufferedImage) {
        attribute = movieAttribute;
        time = timestamp;
        image = bufferedImage;
        DataBuffer buff = image.getRaster().getDataBuffer();
        imageSize = buff.getSize()
                    * DataBuffer.getDataTypeSize(buff.getDataType())
                    / 8;
        end = false;
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
     * @return The micro timestamp 
     */
    public long getTimestamp() {
        return time;
    }
    /**
     * Indicates whether end.
     * @return True if end of movie
     */
    public boolean isEnd() {
        return end;
    }
}
