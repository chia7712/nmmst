package tw.gov.nmmst.controller;
import java.io.IOException;
import java.util.Map;
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
/**
 * A wheel trigger captures the wheel event and caluculate the change of wheel
 * direction. Any wheel direction may decide the next movie
 * if the moment of change is valid.
 */
public final class WheelTrigger implements ControllerFactory.Trigger {
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
    private final BlockingQueue<SelectRequest> selectQueue
            = new LinkedBlockingQueue();
    /**
     * Master information.
     */
    private final NodeInformation masterInformation;
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
        masterInformation = NodeInformation.getMasterNode(properties).get();
        closer.invokeNewThread(() -> {
            try {
                SelectRequest request = selectQueue.take();
                SerialStream.send(masterInformation, request, properties);
            } catch (InterruptedException | IOException e) {
                LOG.error(e.getMessage());
            }
        }, null);
    }
    @Override
    public void triggerOff(final Component component) {
        final int currentIndex = media.getMovieBuffer().getCurrentMovieIndex();
        final int lastIndex = media.getMovieBuffer().getLastMovieIndex();
        if (currentIndex != lastIndex) {
            return;
        }
        Pair<Integer, Integer> selectedIndex = selectable.get(currentIndex);
        if (selectedIndex == null) {
            return;
        }
        final long currentTimestamp
            = media.getMovieBuffer().getCurrentTimestamp();
        final long currentDuration
            = media.getMovieBuffer().getCurrentDuration();
        final long diffTime = currentDuration - currentTimestamp;
        if (diffTime < selectablePeriod.getKey()
            || diffTime > selectablePeriod.getValue()) {
            return;
        }
        int nextIndex;
        switch (detector.detect(component.getPollData())) {
            case SMALLER:
                nextIndex = selectedIndex.getKey();
                break;
            case LARGER:
                nextIndex = selectedIndex.getValue();
                break;
            default:
                return;
        }
        selectQueue.offer(new SelectRequest(nextIndex));
    }
    @Override
    public Type getType() {
        return Controller.Type.WHEEL;
    }
}
