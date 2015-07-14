package tw.gov.nmmst.controller;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.util.Pair;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Controller.Type;
import tw.gov.nmmst.threads.Closer;
import tw.gov.nmmst.NConstants;
import tw.gov.nmmst.NProperties;
import tw.gov.nmmst.NodeInformation;
import tw.gov.nmmst.media.MediaWorker;
import tw.gov.nmmst.utils.RequestUtil.SelectRequest;
import tw.gov.nmmst.utils.SerialStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tw.gov.nmmst.threads.Taskable;
/**
 * A wheel trigger captures the wheel event and caluculate the change of wheel
 * direction. Any wheel direction may decide the next movie
 * if the moment of change is valid.
 */
public final class WheelTrigger implements ControllerFactory.Trigger {
    /**
     * Test select request.
     * @param args Args
     * @throws InterruptedException Any exception
     * @throws IOException Any exception
     */
    public static void main(final String[] args)
        throws InterruptedException, IOException {
        NProperties properties = new NProperties(new File("D:\\2.NProperties"));
        SelectRequest req = new SelectRequest(2);
        NodeInformation masterInformation
            = NodeInformation.getMasterNode(properties).get();
        SerialStream.send(masterInformation, req, properties);
    }
    /**
     * Log.
     */
    private static final Logger LOG
            = LoggerFactory.getLogger(WheelTrigger.class);
    /**
     * Detects the change of direction.
     */
    private final DirectionDetector detector;
    /**
     * The selectable timestamp must be within this period.
     */
    private final Pair<Long, Long> selectablePeriod;
    /**
     * Queues the requests.
     */
    private final BlockingQueue<DirectionDetector.Trend> trendQueue
            = new LinkedBlockingQueue();
    /**
     * The movie index and selectable indexes.
     */
    private final Map<Integer, Pair<Integer, Integer>> selectable;
    /**
     * Media work provides movie attribute to check
     * the validity to enable select function.
     */
    private final MediaWorker media;
    /**
     * Constructs a wheel trigger with the arguments of properties.
     * @param properties NProperties
     * @param closer Closer
     * @param mediaWorker Media worker
     */
    public WheelTrigger(
            final NProperties properties,
            final Closer closer,
            final MediaWorker mediaWorker) {
        detector = new DirectionDetector(
            new Pair(properties.getDouble(NConstants.WHEEL_MAX_VALUE),
                     properties.getDouble(NConstants.WHEEL_MIN_VALUE)),
            new Pair(properties.getDouble(NConstants.WHEEL_MAX_INIT_VALUE),
                     properties.getDouble(NConstants.WHEEL_MIN_INIT_VALUE)));
        selectablePeriod = new Pair(
            properties.getLong(
                    NConstants.WHEEL_ENABLE_MIN_MICROTIME_PERIOD),
            properties.getLong(
                    NConstants.WHEEL_ENABLE_MAX_MICROTIME_PERIOD));
        selectable = NProperties.stringToSelectable(
                properties.getString(NConstants.MOVIE_SELECT));
        media = mediaWorker;
        NodeInformation masterInformation
            = NodeInformation.getMasterNode(properties).get();
        closer.invokeNewThread(new Taskable() {
            /**
             * Records the selected index.
             */
            private final Set<Integer> hasDone = new TreeSet();
            /**
             * Last selected index.
             */
            private int lastIndex = 0;
            @Override
            public void work() {
                try {
                    Optional<SelectRequest> req = parseTrend(trendQueue.take());
                    if (req.isPresent() && lastIndex > req.get().getIndex()) {
                        hasDone.clear();
                    }
                    if (req.isPresent()
                       && !hasDone.contains(req.get().getIndex())) {
                        SerialStream.send(masterInformation,
                            req.get(), properties);
                        hasDone.add(req.get().getIndex());
                        lastIndex = req.get().getIndex();
                    }
                } catch (InterruptedException | IOException e) {
                    LOG.error(e.getMessage());
                } finally {
                    trendQueue.clear();
                }
            }
        }, null);
    }
    /**
     * @return Retrieves the inner queue
     */
    BlockingQueue<DirectionDetector.Trend> getQueue() {
        return trendQueue;
    }
    /**
     * Test purpose.
     * @param testMedia Media for test
     */
    WheelTrigger(final MediaWorker testMedia) {
        NProperties properties = new NProperties();
        detector = new DirectionDetector(
            new Pair(properties.getDouble(NConstants.WHEEL_MAX_VALUE),
                     properties.getDouble(NConstants.WHEEL_MIN_VALUE)),
            new Pair(properties.getDouble(NConstants.WHEEL_MAX_INIT_VALUE),
                     properties.getDouble(NConstants.WHEEL_MIN_INIT_VALUE)));
        selectablePeriod = new Pair(
            properties.getLong(
                    NConstants.WHEEL_ENABLE_MIN_MICROTIME_PERIOD),
            properties.getLong(
                    NConstants.WHEEL_ENABLE_MAX_MICROTIME_PERIOD));
        selectable = NProperties.stringToSelectable(
                properties.getString(NConstants.MOVIE_SELECT));
        media = testMedia;
    }
    /**
     * Parses the trend and some conditions for deciding the select request.
     * @param trend The wheel trend
     * @return Select request or none
     */
    private Optional<SelectRequest> parseTrend(
            final DirectionDetector.Trend trend) {
        final int currentIndex = media.getMovieBuffer().getCurrentMovieIndex();
        final int lastIndex = media.getMovieBuffer().getLastMovieIndex();
        if (currentIndex != lastIndex) {
            LOG.info("current index does not match last index");
            return Optional.empty();
        }
        Pair<Integer, Integer> selectableIndexes = selectable.get(currentIndex);
        if (selectableIndexes == null) {
            LOG.info("current index does not have selectable index");
            return Optional.empty();
        }
        final long currentTimestamp
            = media.getMovieBuffer().getCurrentTimestamp();
        final long currentDuration
            = media.getMovieBuffer().getCurrentDuration();
        final long diffTime = currentDuration - currentTimestamp;
        if (diffTime <= 0 || diffTime < selectablePeriod.getKey()
            || diffTime > selectablePeriod.getValue()) {
            LOG.info("current timestamp does not match period : ("
                + diffTime + ", " + selectablePeriod.getKey()
                + ", " + selectablePeriod.getValue() + ")");
            return Optional.empty();
        }
        int nextIndex;
        switch (trend) {
            case SMALLER:
                nextIndex = selectableIndexes.getKey();
                break;
            case LARGER:
                nextIndex = selectableIndexes.getValue();
                break;
            default:
                LOG.info("Poll data dose not be changed");
                return Optional.empty();
        }
        return Optional.of(new SelectRequest(nextIndex));
    }
    @Override
    public void triggerOff(final Component component) {
        trendQueue.offer(detector.detect(component.getPollData()));
    }
    @Override
    public Type getType() {
        return Controller.Type.WHEEL;
    }

}
