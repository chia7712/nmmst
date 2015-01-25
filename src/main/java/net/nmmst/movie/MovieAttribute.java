package net.nmmst.movie;

import javax.sound.sampled.AudioFormat;

public interface MovieAttribute 
{
    public int getIndex();
    public String getPath();
    public long getDuration();
    public AudioFormat getAutioFormat();
}
