package net.nmmst.controller;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
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
 * Draws the oval before printing frame on the panel.
 */
public class StickTrigger implements ControlTrigger, FrameProcessor {
    private enum SnapshotMode {
        FULL,
        NORMAL,
        NONE
    }
    private final List<BufferedImage> snapshots = new LinkedList();
    private final DirectionDetector verticalDector;
    private final DirectionDetector horizontalDector;
    private final int snapshotLimit;
    private final double stickPressValue;
    private final double snapshotScale;
    private final AtomicInteger snapshotIndex = new AtomicInteger();
    private final AtomicInteger modeIndex
            = new AtomicInteger(SnapshotMode.NORMAL.ordinal());
    private final AtomicBoolean pressed = new AtomicBoolean();
    public StickTrigger(final NProperties properties) {
        final double stickMinValue
                = properties.getDouble(NConstants.STICK_MIN_VALUE);
        final double stickMaxValue
                = properties.getDouble(NConstants.STICK_MAX_VALUE);
        final double stickMinInitValue
                = properties.getDouble(NConstants.STICK_MIN_INIT_VALUE);
        final double stickMaxInitValue
                = properties.getDouble(NConstants.STICK_MAX_INIT_VALUE);
        verticalDector = new DirectionDetector(
                new Pair(stickMinValue, stickMaxValue),
                new Pair(stickMinInitValue, stickMaxInitValue));
        horizontalDector = new DirectionDetector(
                new Pair(stickMinValue, stickMaxValue),
                new Pair(stickMinInitValue, stickMaxInitValue));
        stickPressValue = properties.getDouble(NConstants.STICK_PRESS_VALUE);
        snapshotScale = properties.getDouble(NConstants.SNAPSHOT_SCALE);
        snapshotLimit = (int) Math.pow(Math.pow(snapshotScale, -1), 2);
    }
    public List<BufferedImage> cloneSnapshot() {
        return new ArrayList(snapshots);
    }
    @Override
    public void init() {
        snapshots.clear();
    }
    @Override
    public Optional<BufferedImage> prePrintPanel(final BufferedImage image) {
        if (pressed.compareAndSet(true, false)
                && snapshots.size() < snapshotLimit) {
            snapshots.add(Painter.process(image, Painter.getCopyPainter()));
            snapshotIndex.set(snapshots.size() - 1);
        }
        if (snapshots.isEmpty()) {
            return Optional.of(image);
        }
        BufferedImage snapshot = snapshots.get(snapshotIndex.get());
        switch(SnapshotMode.values()[modeIndex.get()]) {
            case FULL:
                return Optional.of(snapshot);
            case NORMAL:
                Graphics2D g = (Graphics2D)image.getGraphics();
                g.drawImage(
                    snapshot,
                    Math.max(0, image.getWidth()
                        - (int) ((double)image.getWidth() * snapshotScale)),
                    Math.max(0, image.getHeight()
                        - (int) ((double)image.getHeight() * snapshotScale)),
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
    public void triggerOff(Component component) {
        if (component.getName().contains("X")) {
            switch(horizontalDector.detect(component.getPollData())) {
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
            switch(verticalDector.detect(component.getPollData())) {
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
    public Controller.Type getType() {
        return Controller.Type.STICK;
    }
}