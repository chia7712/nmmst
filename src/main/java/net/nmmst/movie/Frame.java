package net.nmmst.movie;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class Frame {
    private final MovieAttribute attribute;
    private final BufferedImage image;
    private final long timestamp;
    private final long imageSize;
    public Frame(MovieAttribute attribute, long timestamp, BufferedImage image) {
        this.attribute	= attribute;
        this.timestamp 	= timestamp;
        this.image = image;
        if (image == null) {
            this.imageSize = 0;
        } else {
            DataBuffer buff = image.getRaster().getDataBuffer();
            this.imageSize = buff.getSize() * DataBuffer.getDataTypeSize(buff.getDataType()) / 8;
        }
            

    }
    public MovieAttribute getMovieAttribute() {
        return attribute;
    }
    public long getHeapSize() {
        return imageSize;
    }
    public BufferedImage getImage() {
        return image;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public static Frame newNullFrame() {
        return new Frame(null, -1, null);
    }
}
