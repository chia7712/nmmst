package tw.gov.nmmst.controller;

import java.util.concurrent.atomic.AtomicInteger;
import javafx.util.Pair;

/**
 * Detects the direction for changed value. (min init, max init) → The interval
 * for initial value min min max max value init init value
 * -------------|-------------|------|-------------|-------------
 *
 * @see WheelTrigger
 * @see OvalTrigger
 */
public class DirectionDetector {

  /**
   * Value trend.
   */
  public enum Trend {
    /**
     * Larger.
     */
    LARGER,
    /**
     * Smaller.
     */
    SMALLER,
    /**
     * No change.
     */
    NONE;
  }
  /**
   * Max threshold.
   */
  private double maxValue;
  /**
   * Min threshold.
   */
  private double minValue;
  /**
   * Max init value.
   */
  private double maxInit;
  /**
   * Min init value.
   */
  private double minInit;
  /**
   * Initialized flag.
   */
  private boolean initialized = true;
  private int sampleFreq;
  private final AtomicInteger actionCount = new AtomicInteger(0);
  /**
   * A lock for synchronizing the direction.
   */
  private final Object lock = new Object();

  /**
   * A detector with specified thresholds.
   *
   * @param directionLimit Max and min threshold
   * @param initLimit Intial limit
   */
  public DirectionDetector(final Pair<Double, Double> directionLimit,
          final Pair<Double, Double> initLimit) {
    maxValue = Math.max(directionLimit.getKey(),
            directionLimit.getValue());
    minValue = Math.min(directionLimit.getKey(),
            directionLimit.getValue());
    maxInit = Math.max(initLimit.getKey(),
            initLimit.getValue());
    minInit = Math.min(initLimit.getKey(),
            initLimit.getValue());
    sampleFreq = 1;
  }

  /**
   * A detector with specified thresholds.
   *
   * @param directionLimit Max and min threshold
   * @param initLimit Intial limit
   * @param sampleFreq
   */
  public DirectionDetector(final Pair<Double, Double> directionLimit,
          final Pair<Double, Double> initLimit, final int sampleFreq) {
    maxValue = Math.max(directionLimit.getKey(),
            directionLimit.getValue());
    minValue = Math.min(directionLimit.getKey(),
            directionLimit.getValue());
    maxInit = Math.max(initLimit.getKey(),
            initLimit.getValue());
    minInit = Math.min(initLimit.getKey(),
            initLimit.getValue());
    this.sampleFreq = sampleFreq;
  }

  public void setSampleFreq(final int freq) {
    synchronized (lock) {
      sampleFreq = freq;
    }
  }

  public void setMinInitValue(final double value) {
    synchronized (lock) {
      minInit = value;
    }
  }

  public void setMaxInitValue(final double value) {
    synchronized (lock) {
      maxInit = value;
    }
  }

  public void setMinValue(final double value) {
    synchronized (lock) {
      minValue = value;
    }
  }

  public void setMaxValue(final double value) {
    synchronized (lock) {
      maxValue = value;
    }
  }

  /**
   * Detects current trend.
   *
   * @param value Current value
   * @return The trend
   */
  public final Trend detect(final double value) {
    if (actionCount.getAndIncrement() % sampleFreq != 0) {
      return Trend.NONE;
    }
    synchronized (lock) {
      if (initialized && value >= maxValue) {
        initialized = false;
        return Trend.LARGER;
      } else if (initialized && value <= minValue) {
        initialized = false;
        return Trend.SMALLER;
      } else if (value <= maxInit && value >= minInit) {
        initialized = true;
        return Trend.NONE;
      } else {
        return Trend.NONE;
      }
    }
  }
}
