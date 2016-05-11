package tw.gov.nmmst.controller;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
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
import tw.gov.nmmst.threads.Taskable;
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
     * Press flag.
     */
    private final AtomicBoolean pressed = new AtomicBoolean();
    
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    private final BlockingQueue<Object> outsideNotify = new ArrayBlockingQueue<>(1);
    private final BlockingQueue<Object> insideNotify = new ArrayBlockingQueue<>(1);
    private final CircularNumber horizontalIndex = new CircularNumber();
    private final CircularNumber verticalIndex = new CircularNumber(SnapshotMode.NORMAL.ordinal());
    private final boolean enableScalable;
    private final boolean enableSelectable;
    private final ArgumentFileRunnable verticalFile;

    /**
     * Constructs a strick trigger with specified properties.
     * @param properties NProperties
     * @param closer Close the errand
     */
    public StickTrigger(final NProperties properties, final Closer closer) {
        enableScalable = properties.getBoolean(NConstants.ENABLE_SCALABLE_SNAPSHOT);
        enableSelectable = properties.getBoolean(NConstants.ENABLE_SELECTABLE_SNAPSHOT);
        verticalDetector = new DirectionDetector(
                new Pair<>(properties.getDouble(NConstants.STICK_VERTICAL_MIN_VALUE),
                        properties.getDouble(NConstants.STICK_VERTICAL_MAX_VALUE)),
                new Pair<>(properties.getDouble(NConstants.STICK_VERTICAL_MIN_INIT_VALUE),
                        properties.getDouble(NConstants.STICK_VERTICAL_MAX_INIT_VALUE)),
                properties.getInteger(NConstants.STICK_VERTICAL_SAMPLE));
        horizontalDetector = new DirectionDetector(
                new Pair<>(properties.getDouble(NConstants.STICK_HORIZONTAL_MIN_VALUE),
                        properties.getDouble(NConstants.STICK_HORIZONTAL_MAX_VALUE)),
                new Pair<>(properties.getDouble(NConstants.STICK_HORIZONTAL_MIN_INIT_VALUE),
                        properties.getDouble(NConstants.STICK_HORIZONTAL_MAX_INIT_VALUE)),
                properties.getInteger(NConstants.STICK_HORIZONTAL_SAMPLE));
        verticalFile = create(verticalDetector);
        stickPressValue = properties.getDouble(NConstants.STICK_PRESS_VALUE);
        snapshotScale = properties.getDouble(NConstants.SNAPSHOT_SCALE);
        snapshotLimit = (int) Math.pow(Math.pow(snapshotScale, -1), 2);
        LOG.info("The snapshot limit is " + snapshotLimit);
        snapshots = new ArrayList<>(snapshotLimit);
        if (verticalFile != null) {
            closer.invokeNewThread(verticalFile);
        }

        closer.invokeNewThread(() -> {
            try {
                insideNotify.take();
                sendSnapshot(properties, cloneSnapshot());
            } catch (InterruptedException e) {
                LOG.debug(e);
            }
        });
    }
    private static ArgumentFileRunnable create(DirectionDetector dd) {
        try {
            LOG.info("Reconfigurable on D:, key, vp");
            return new ArgumentFileRunnable(Paths.get("D:\\"), "key", new File("D:\\vp"), (k ,v) -> {
                if (k.equalsIgnoreCase(NConstants.STICK_VERTICAL_MIN_VALUE)) {
                    dd.setMinValue(v);
                } else if (k.equalsIgnoreCase(NConstants.STICK_VERTICAL_MAX_VALUE)) {
                    dd.setMaxValue(v);
                } else if (k.equalsIgnoreCase(NConstants.STICK_VERTICAL_MIN_INIT_VALUE)) {
                    dd.setMinInitValue(v);
                } else if (k.equalsIgnoreCase(NConstants.STICK_VERTICAL_MAX_INIT_VALUE)) {
                    dd.setMaxInitValue(v);
                }
            });
        } catch (IOException e) {
            LOG.error(e);
            return null;
        }
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
                horizontalIndex.set(snapshots.size() - 1);
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
                snapshot = snapshots.get(horizontalIndex.get(snapshots.size()));
            } else {
                snapshot = snapshots.get(snapshots.size() - 1);
            }
        } finally {
            lock.readLock().unlock();
        }
        SnapshotMode currentMode;
        if (enableScalable) {
            currentMode = SnapshotMode.values()[verticalIndex.get(SnapshotMode.values().length)];
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
        final float data = component.getPollData();
        final String name = component.getName();
        if (name.contains("X")) {
            switch (horizontalDetector.detect(data)) {
                case LARGER:
                    horizontalIndex.next();
                    break;
                case SMALLER:
                    horizontalIndex.back();
                    break;
                default:
            }
        } else if (name.contains("Y")) {
            switch (verticalDetector.detect(data)) {
                case LARGER:
                    verticalIndex.next();
                    break;
                case SMALLER:
                    verticalIndex.back();
                    break;
                default:
            }
        } else if (name.contains("s")) {
            pressed.set(data == stickPressValue);
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
    private static class ArgumentFileRunnable implements Taskable {
        private final WatchService watcher = FileSystems.getDefault().newWatchService();
        private final Path watchedDir;
        private final String keyFileName;
        private final File contentFile;
        private final BiConsumer<String, Double> consumer;
        ArgumentFileRunnable(final Path watchedDir, final String keyFileName,
            final File contentFile, final BiConsumer<String, Double> consumer) throws IOException {
            this.watchedDir = watchedDir;
            watchedDir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            this.keyFileName = keyFileName;
            this.contentFile = contentFile;
            this.consumer = consumer;
        }
        @Override
        public void close() throws IOException {
            watcher.close();
        }
        @Override
        public void work() throws Exception {
            WatchKey key = watcher.take();
            Optional<Path> triggerFile = key.pollEvents().stream()
                    .filter(v -> v.kind() == ENTRY_CREATE)
                    .filter(v -> v.context() instanceof Path)
                    .map(v -> (Path) v.context())
                    .filter(v -> v.getFileName().toString().equals(keyFileName))
                    .findFirst();

            boolean reset = key.reset();
            if (!reset) {
                return;
            }
            if (!triggerFile.isPresent() || !contentFile.exists()) {
                return;
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(contentFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] args = line.split("=");
                    if (args.length != 2) {
                        continue;
                    }
                    try {
                        double v = Double.valueOf(args[1]);
                        consumer.accept(args[0], v);
                    } catch (NumberFormatException e) {
                    }
                }
            } catch (IOException e) {
                LOG.error(e);
            }
            File f = new File(watchedDir.toString(), triggerFile.get().toString());
            if (f.exists() && f.isFile()) {
                f.delete();
            }
        }
    }
}
