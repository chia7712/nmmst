package net.nmmst.player;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Speaker
{
    private final SourceDataLine line;
    public Speaker(AudioFormat audioFormat) throws LineUnavailableException
    {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(audioFormat);
        line.start();	
    }
    public void write(byte[] data)
    {
        int count = 0;
        while((count += line.write(data, count, data.length - count)) != data.length)
        {
            //DO NOTHING
        }
    }
    public AudioFormat getAudioFormat()
    {
        return line.getFormat();
    }
    public void drain()
    {
        line.drain();
    }	
    public void flush()
    {
        line.flush();
    }
    public void close()
    {
        line.drain();
        line.close();
    }
}
