package net.nmmst.controller;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.util.Pair;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.nmmst.processor.FrameProcessor;
import net.nmmst.NConstants;
import net.nmmst.NProperties;
import net.nmmst.utils.Painter;
/**
 * Draws the snapshots before printing frame on the panel.
 */
public class StickTrigger implements ControllerFactory.Trigger, FrameProcessor {
    /**
     * Supplies three modes for displaying the current snapshot.
     */
    private enum SnapshotMode {
        /**
         * Full.
         */
        FULL,
        /**
         * Normal. A small snapshot in the right-down.
         */
        NORMAL,
        /**
         * No snapshot.
         */
        NONE
    }
    /**
     * The captured snapshots.
     */
    private final List<BufferedImage> snapshots = new LinkedList();
    /**
     * Detects the vertical direction.
     */
    private final DirectionDetector verticalDetector;
    /**
     * Detects the horizontal direction.
     */
    private final DirectionDetector horizontalDetector;
    /**
     * The max number of captured snapshots.
     */
    private final int snapshotLimit;
    /**
     * The threshold value of press.
     */
    private final double stickPressValue;
    /**
     * The scale of snapshot.
     */
    private final double snapshotScale;
    /**
     * The index of current snapshot.
     */
    private final AtomicInteger snapshotIndex = new AtomicInteger();
    /**
     * The index of current mode.
     */
    private final AtomicInteger modeIndex
            = new AtomicInteger(SnapshotMode.NORMAL.ordinal());
    /**
     * Press flag.
     */
    private final AtomicBoolean pressed = new AtomicBoolean();
    /**
     * Constructs a strick trigger with specified properties.
     * @param properties NProperties
     */
    public StickTrigger(final NProperties properties) {
        final double stickMinValue
                = properties.getDouble(NConstants.STICK_MIN_VALUE);
        final double stickMaxValue
                = properties.getDouble(NConstants.STICK_MAX_VALUE);
        final double stickMinInitValue
                = properties.getDouble(NConstants.STICK_MIN_INIT_VALUE);
        final double stickMaxInitValue
                = properties.getDouble(NConstants.STICK_MAX_INIT_VALUE);
        verticalDetector = new DirectionDetector(
                new Pair(stickMinValue, stickMaxValue),
                new Pair(stickMinInitValue, stickMaxInitValue));
        horizontalDetector = new DirectionDetector(
                new Pair(stickMinValue, stickMaxValue),
                new Pair(stickMinInitValue, stickMaxInitValue));
        stickPressValue = properties.getDouble(NConstants.STICK_PRESS_VALUE);
        snapshotScale = properties.getDouble(NConstants.SNAPSHOT_SCALE);
        snapshotLimit = (int) Math.pow(Math.pow(snapshotScale, -1), 2);
    }
    /**
     * Clones the current snapshots.
     * @return A list of snapshots
     */
    public final List<BufferedImage> cloneSnapshot() {
        return new ArrayList(snapshots);
    }
    @Override
    public final void init() {
        snapshots.clear();
    }
    @Override
    public final Optional<BufferedImage> prePrintPanel(
            final BufferedImage image) {
        if (pressed.compareAndSet(true, false)
                && snapshots.size() < snapshotLimit) {
            snapshots.add(Painter.process(image, Painter.getCopyPainter()));
            snapshotIndex.set(snapshots.size() - 1);
        }
        if (snapshots.isEmpty()) {
            return Optional.of(image);
        }
        BufferedImage snapshot = snapshots.get(snapshotIndex.get());
        switch (SnapshotMode.values()[modeIndex.get()]) {
            case FULL:
                return Optional.of(snapshot);
            case NORMAL:
                Graphics2D g = (Graphics2D) image.getGraphics();
                g.drawImage(
                    snapshot,
                    Math.max(0, image.getWidth()
                        - (int) ((double) image.getWidth() * snapshotScale)),
                    Math.max(0, image.getHeight()
                        - (int) ((double) image.getHeight() * snapshotScale)),
                    image.getWidth(),
                    image.getHeight(),
                    0,
                    0,
                    snapshot.getWidth(),
                    snapshot.getHeight(),
                    null);
                g.dispose();
                return Optional.of(image);
            default:
                return Optional.of(image);
        }
    }
    @Override
    public final void triggerOff(final Component component) {
        if (component.getName().contains("X")) {
            switch (horizontalDetector.detect(component.getPollData())) {
                case LARGER:
                    snapshotIndex.accumulateAndGet(snapshotIndex.get() + 1,
                        (int a, int b) -> {
                        if (b >= snapshots.size()) {
                            return a;
                        }
                        return b;
                    });
                    break;
                case SMALLER:
                    snapshotIndex.accumulateAndGet(snapshotIndex.get() - 1,
                        (int a, int b) -> {
                        if (b < 0) {
                            return a;
                        }
                        return b;
                    });
                    break;
                default:
            }
        } else if (component.getName().contains("Y")) {
            switch (verticalDetector.detect(component.getPollData())) {
                case LARGER:
                    modeIndex.accumulateAndGet(modeIndex.get() + 1,
                        (int a, int b) -> {
                        if (b >= SnapshotMode.values().length) {
                            return a;
                        }
                        return b;
                    });
                    break;
                case SMALLER:
                    modeIndex.accumulateAndGet(modeIndex.get() - 1,
                        (int a, int b) -> {
                        if (b < 0) {
                            return a;
                        }
                        return b;
                    });
                    break;
                default:
            }
        } else if (component.getName().contains("s")) {
            pressed.set(component.getPollData() == stickPressValue);
        }
    }
    @Override
    public final Controller.Type getType() {
        return Controller.Type.STICK;
    }
}
