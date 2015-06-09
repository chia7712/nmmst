package tw.gov.nmmst.controller;

import javafx.util.Pair;

/**
 * Detects the direction for changed value.
 * (min init, max init) → The interval for initial value
 *             min           min    max           max
 *            value          init   init          value
 * -------------|-------------|------|-------------|-------------
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
    private final double maxValue;
    /**
     * Min threshold.
     */
    private final double minValue;
    /**
     * Max init value.
     */
    private final double maxInit;
    /**
     * Min init value.
     */
    private final double minInit;
    /**
     * Initialized flag.
     */
    private boolean initialized = true;
    /**
     * A detector with specified thresholds.
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
    }
    /**
     * Detects current trend.
     * @param value Current value
     * @return The trend
     */
    public final Trend detect(final double value) {
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
