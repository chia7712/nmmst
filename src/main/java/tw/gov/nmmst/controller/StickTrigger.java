package tw.gov.nmmst.controller;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javafx.util.Pair;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tw.gov.nmmst.processor.FrameProcessor;
import tw.gov.nmmst.NConstants;
import tw.gov.nmmst.NProperties;
import tw.gov.nmmst.NodeInformation;
import tw.gov.nmmst.threads.Closer;
import tw.gov.nmmst.utils.Painter;
import tw.gov.nmmst.utils.RequestUtil;
import tw.gov.nmmst.utils.SerialStream;
/**
 * Draws the snapshots before printing frame on the panel.
 */
public class StickTrigger implements ControllerFactory.Trigger, FrameProcessor {
    /**
     * Log.
     */
    private static final Log LOG = LogFactory.getLog(StickTrigger.class);
    /**
     * The period to refresh the selected string.
     * Default value is one second.
     */
    private static final long REFRESH_PERIOD = 1000;
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
    private final List<BufferedImage> snapshots;
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
    
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    private final BlockingQueue<Object> outsideNotify = new ArrayBlockingQueue<>(1);
    private final BlockingQueue<Object> insideNotify = new ArrayBlockingQueue<>(1);
    private final boolean enableScalable;
    private final boolean enableSelectable;
    /**
     * Constructs a strick trigger with specified properties.
     * @param properties NProperties
     * @param closer Close the errand
     */
    public StickTrigger(final NProperties properties, final Closer closer) {
        final double stickMinValue
                = properties.getDouble(NConstants.STICK_MIN_VALUE);
        final double stickMaxValue
                = properties.getDouble(NConstants.STICK_MAX_VALUE);
        final double stickMinInitValue
                = properties.getDouble(NConstants.STICK_MIN_INIT_VALUE);
        final double stickMaxInitValue
                = properties.getDouble(NConstants.STICK_MAX_INIT_VALUE);
        enableScalable = properties.getBoolean(NConstants.ENABLE_SCALABLE_SNAPSHOT);
        enableSelectable = properties.getBoolean(NConstants.ENABLE_SELECTABLE_SNAPSHOT);
        verticalDetector = new DirectionDetector(
                new Pair<>(stickMinValue, stickMaxValue),
                new Pair<>(stickMinInitValue, stickMaxInitValue));
        horizontalDetector = new DirectionDetector(
                new Pair<>(stickMinValue, stickMaxValue),
                new Pair<>(stickMinInitValue, stickMaxInitValue));
        stickPressValue = properties.getDouble(NConstants.STICK_PRESS_VALUE);
        snapshotScale = properties.getDouble(NConstants.SNAPSHOT_SCALE);
        snapshotLimit = (int) Math.pow(Math.pow(snapshotScale, -1), 2);
        LOG.info("The snapshot limit is " + snapshotLimit);
        snapshots = new ArrayList<>(snapshotLimit);
        closer.invokeNewThread(() -> {
            try {
                insideNotify.take();
                sendSnapshot(properties, cloneSnapshot());
            } catch (InterruptedException e) {
                LOG.debug(e);
            }
        });
    }
    public final void waitForChange() throws InterruptedException {
        outsideNotify.take();
    }
    /**
     * Clones the current snapshots.
     * @return A list of snapshots
     */
    public final List<BufferedImage> cloneSnapshot() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(snapshots);
        } finally {
            lock.readLock().unlock();
        }
    }
    @Override
    public final void init() {
        lock.writeLock().lock();
        try {
            snapshots.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
    @Override
    public final Optional<BufferedImage> prePrintPanel(
            final BufferedImage image) {
        if (pressed.compareAndSet(true, false)) {
            lock.writeLock().lock();
            try {
                if (snapshots.size() >= snapshotLimit && !snapshots.isEmpty()) {
                    snapshots.remove(0);
                }
                snapshots.add(Painter.process(image, Painter.getCopyPainter()));
                snapshotIndex.set(snapshots.size() - 1);
                Object obj = new Object();
                outsideNotify.offer(obj);
                insideNotify.offer(obj);
            } finally {
                lock.writeLock().unlock();
            }
        }
        BufferedImage snapshot;
        lock.readLock().lock();
        try {
            if (snapshots.isEmpty()) {
                return Optional.of(image);
            }
            if (enableSelectable) {
                snapshot = snapshots.get(snapshotIndex.get() % snapshots.size());
            } else {
                snapshot = snapshots.get(snapshots.size() - 1);
            }
        } finally {
            lock.readLock().unlock();
        }
        SnapshotMode currentMode;
        if (enableScalable) {
            currentMode = SnapshotMode.values()[modeIndex.get()];
        } else {
            currentMode = SnapshotMode.NORMAL;
        }
        switch (currentMode) {
            case FULL:
                return Optional.of(snapshot);
            case NORMAL:
                Graphics2D g = (Graphics2D) image.getGraphics();
                g.drawImage(
                    snapshot,
                    Math.max(0, image.getWidth() - (int) ((double) snapshot.getWidth() * snapshotScale)),
                    Math.max(0, image.getHeight() - (int) ((double) snapshot.getHeight() * snapshotScale)),
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
                    snapshotIndex.incrementAndGet();
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
    /**
     * Sends the snapshot request to master node.
     * @param properties System properties
     * @param images Stick image
     */
    private static void sendSnapshot(final NProperties properties,
        final List<BufferedImage> images) {
        if (images.isEmpty()) {
            return;
        }
        Optional<NodeInformation> node = NodeInformation.getMasterNode(properties);
        try {
            if (node.isPresent()) {
                SerialStream.send(node.get(),
                    new RequestUtil.SetImageRequest(images));
            }
        } catch (IOException | InterruptedException e) {
            LOG.error(e);
        }
    }
}
