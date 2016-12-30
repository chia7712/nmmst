package tw.gov.nmmst.media;

import java.util.concurrent.TimeUnit;

/**
 * Controls the period between sequential frame.
 */
public final class Sleeper {

  /**
   * Scales up the time from nano to micro.
   */
  private static final long NANO_TO_MICRO = 1000;
  /**
   * Tolerance time.
   */
  private final long tolerance;
  /**
   * Time of first frame.
   */
  private long streamStartTime = 0;
  /**
   * Time of local clock.
   */
  private long clockStartTime = 0;

  /**
   * Constructs a sleeper with specified tolerence time.
   *
   * @param microTolerance Tolerance time
   */
  public Sleeper(final long microTolerance) {
    tolerance = microTolerance;
  }

  /**
   * Sleeps a while according to timestamp of frame.
   *
   * @param streamCurrentTime Timestamp of frame
   * @return The sleep time caused by this method
   * @throws InterruptedException If someone breaks up the sleep
   */
  public long sleepByTimeStamp(final long streamCurrentTime)
          throws InterruptedException {
    if (streamStartTime == 0) {
      clockStartTime = System.nanoTime();
      streamStartTime = streamCurrentTime;
      return 0;
    }
    final long clockTimeInterval
            = (System.nanoTime() - clockStartTime) / NANO_TO_MICRO;
    final long streamTimeInterval
            = (streamCurrentTime - streamStartTime);
    final long microsecondsToSleep
            = (streamTimeInterval - (clockTimeInterval + tolerance));
    if (microsecondsToSleep > 0) {
      TimeUnit.MICROSECONDS.sleep(microsecondsToSleep);
    }
    return microsecondsToSleep;
  }

  /**
   * Resets all time record.
   */
  public void reset() {
    clockStartTime = 0;
    streamStartTime = 0;
  }
}
