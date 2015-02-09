package net.nmmst.movie;

import java.awt.image.BufferedImage;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class Frame {
    private final MovieAttribute attribute;
    private final BufferedImage image;
    private final long timestamp;
    public Frame(MovieAttribute attribute, long timestamp, BufferedImage image) {
        this.attribute	= attribute;
        this.timestamp 	= timestamp;
        this.image = image;
    }
    public MovieAttribute getMovieAttribute() {
        return attribute;
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
