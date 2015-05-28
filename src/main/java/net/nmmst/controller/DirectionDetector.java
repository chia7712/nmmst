package net.nmmst.controller;

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
    public enum Mode {
        LARGER,
        SMALLER,
        NONE;
    }
    private final double maxValue;
    private final double minValue;
    private final double maxInit;
    private final double minInit;
    private boolean initialized = true;
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
    public Mode detect(double value) {
        if (initialized && value >= maxValue) {
            initialized = false;
            return Mode.LARGER;
        } else if (initialized && value <= minValue) {
            initialized = false;
            return Mode.SMALLER;
        } else if (value <= maxInit && value >= minInit) {
            initialized = true;
            return Mode.NONE;
        } else {
            return Mode.NONE;
        }
    }
}
