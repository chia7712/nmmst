package net.nmmst.movie;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public interface MovieBuffer {
    public Frame readFrame() throws InterruptedException;
    public Sample readSample() throws InterruptedException;
    public void writeFrame(Frame frame) throws InterruptedException;
    public void writeSample(Sample sample) throws InterruptedException;
    public void setPause(boolean value);
    public boolean isPause();
    public boolean hadPause();
    public void clear();
    public long getHeapSize();
    public int getFrameNumber();
    public int getSampleNumber();
    public int getFrameQueueMaxLength();
    public int getSampleQueueMaxLength();
}
