package tw.gov.nmmst.media;

/**
 * Provides buffer metrics.
 */
public interface BufferMetrics {

  /**
   * Retrieves the bytes to allocate for all frames and samples.
   *
   * @return The total bytes size
   */
  long getHeapSize();

  /**
   * @return The size of buffered frames in this queue
   */
  int getFrameNumber();

  /**
   * @return The size of buffered samples in this queue
   */
  int getSampleNumber();

  /**
   * @return The capacity of this queue to save frames.
   */
  int getFrameCapacity();

  /**
   * @return The capacity of this queue to save samples.
   */
  int getSampleCapacity();

  /**
   * Retrieves the movie index from current frame.
   *
   * @return The index from current frame
   */
  int getCurrentMovieIndex();

  /**
   * Retrieves the micro timestamp from current frame.
   *
   * @return The micro timestamp from current frame
   */
  long getCurrentTimestamp();

  /**
   * Retrieves the movie duration from current frame.
   *
   * @return The movie duration from current frame
   */
  long getCurrentDuration();

  /**
   * Retrieves the movie index from last frame.
   *
   * @return The index from last frame
   */
  int getLastMovieIndex();

  /**
   * Retrieves the micro timestamp from last frame.
   *
   * @return The micro timestamp from last frame
   */
  long getLastTimestamp();

  /**
   * Retrieves the movie duration from last frame.
   *
   * @return The movie duration from last frame
   */
  long getLastDuration();
}
