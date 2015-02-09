package net.nmmst.movie;

import javax.sound.sampled.AudioFormat;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public interface MovieAttribute {
    public int getIndex();
    public String getPath();
    public long getDuration();
    public AudioFormat getAutioFormat();
}
