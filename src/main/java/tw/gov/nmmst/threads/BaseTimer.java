package tw.gov.nmmst.threads;

import java.util.concurrent.TimeUnit;

/**
 * Provides the sleep function.
 */
public class BaseTimer implements Timer {

  /**
   * Time unit.
   */
  private final TimeUnit unit;
  /**
   * Sleep time.
   */
  private final int time;

  /**
   * Constructs a timer with specified sleep time and time unit.
   *
   * @param sleepTime Sleep time
   * @param timeUnit Time unit
   */
  public BaseTimer(final int sleepTime, final TimeUnit timeUnit) {
    this(timeUnit, sleepTime);
  }

  /**
   * Constructs a timer with specified sleep time and time unit.
   *
   * @param sleepTime Sleep time
   * @param timeUnit Time unit
   */
  public BaseTimer(final TimeUnit timeUnit, final int sleepTime) {
    unit = timeUnit;
    time = sleepTime;
  }

  @Override
  public final void sleep() throws InterruptedException {
    unit.sleep(time);
  }
}
