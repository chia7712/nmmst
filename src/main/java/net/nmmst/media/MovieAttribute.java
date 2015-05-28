package net.nmmst.media;

import java.io.File;
import javax.sound.sampled.AudioFormat;

/**
 * The information of movie file.
 */
public interface MovieAttribute {
    /**
     * @return The index for this movie
     */
    public int getIndex();
    /**
     * @return The local file for this movie
     */
    public File getFile();
    /**
     * @return The duration for this movie. The time scale is microtime.
     */
    public long getDuration();
    /**
     * @return The audio format from this movie
     */
    public AudioFormat getAudioFormat();
}
