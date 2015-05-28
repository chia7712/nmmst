package net.nmmst.media;

/**
 * Buffers the {@link Sample} and the {@link Frame}.
  */
public interface MovieBuffer extends BufferMetrics {
    /**
     * Retrieves and removes the head of this queue, waiting if necessary
     * until an frame becomes available.
     * @return The frame from the head of this queue
     * @throws InterruptedException If interrupted while waiting
     */
    public Frame readFrame() throws InterruptedException;
    /**
     * Retrieves and removes the head of this queue, waiting if necessary
     * until an sample becomes available.
     * @return The sample from the head of this queue
     * @throws InterruptedException If interrupted while waiting
     */
    public Sample readSample() throws InterruptedException;
    /**
     * Inserts the specified frame into this queue, waiting if necessary
     * for space to become available.
     * @param frame The frame to add
     * @throws InterruptedException If interrupted while waiting
     */
    public void writeFrame(Frame frame) throws InterruptedException;
    /**
     * Inserts the specified sample into this queue, waiting if necessary
     * for space to become available.
     * @param sample The sample to add
     * @throws InterruptedException If interrupted while waiting
     */
    public void writeSample(Sample sample) throws InterruptedException;
    /**
     * Pauses the {@link #readFrame()} and {@link  #readSample()}.
     * @param value Pause if value is true; Otherwise, no pause
     */
    public void setPause(boolean value);
    /**
     * @return True if this queue has be paused or false otherwise
     */
    public boolean isPause();
    /**
     * @return True if this queue had be paused or false otherwise
     */
    public boolean hadPause();
    /**
     * Removes all of the frames and samples from this collection
     * The collection will be empty after this method returns.
     */
    public void clear();
}
