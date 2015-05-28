package net.nmmst.media;

/**
 * Provides buffer metrics.
 */
public interface BufferMetrics {
    /**
     * Retrieves the bytes to allocate for all frames and samples.
     * @return The total bytes size
     */
    public long getHeapSize();
    /**
     * @return The size of buffered frames in this queue
     */
    public int getFrameNumber();
    /**
     * @return The size of buffered samples in this queue
     */
    public int getSampleNumber();
    /**
     * @return The capacity of this queue to save frames.
     */
    public int getFrameCapacity();
    /**
     * @return The capacity of this queue to save samples.
     */
    public int getSampleCapacity();
    /**
     * Retrieves the movie index from current frame. 
     * @return The index from current frame
     */
    public int getCurrentMovieIndex();
    /**
     * Retrieves the micro timestamp from current frame.
     * @return The micro timestamp from current frame
     */
    public long getCurrentTimestamp();
    /**
     * Retrieves the movie duration from current frame.
     * @return The movie duration from current frame
     */
    public long getCurrentDuration();
    /**
     * Retrieves the movie index from last frame. 
     * @return The index from last frame
     */
    public int getLastMovieIndex();
    /**
     * Retrieves the micro timestamp from last frame.
     * @return The micro timestamp from last frame
     */
    public long getLastTimestamp();
    /**
     * Retrieves the movie duration from last frame.
     * @return The movie duration from last frame
     */
    public long getLastDuration();
}
