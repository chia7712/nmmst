package net.nmmst.controller;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.util.Pair;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Controller.Type;
import net.nmmst.threads.Closer;
import net.nmmst.NConstants;
import net.nmmst.NProperties;
import net.nmmst.NodeInformation;
import net.nmmst.media.MediaWorker;
import net.nmmst.utils.RequestUtil.SelectRequest;
import net.nmmst.utils.SerialStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WheelTrigger implements ControlTrigger {
    private static final Logger LOG
            = LoggerFactory.getLogger(WheelTrigger.class);
    private final DirectionDetector detector;
    private final Pair<Long, Long> selectablePeriod;
    private final BlockingQueue<SelectRequest> selectQueue
            = new LinkedBlockingQueue();
    private final NodeInformation masterInformation;
    private final Map<Integer, Pair<Integer, Integer>> selectable;
    private final MediaWorker media;
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
    public void triggerOff(Component component) {
        final int currentIndex = media.getMovieBuffer().getCurrentMovieIndex();
        final int lastIndex = media.getMovieBuffer().getLastMovieIndex();
        if (currentIndex != lastIndex) {
            return;
        }
        Pair<Integer, Integer> selectableIndex = selectable.get(currentIndex);
        if (selectableIndex == null) {
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
        switch(detector.detect(component.getPollData())) {
            case SMALLER:
                nextIndex = selectableIndex.getKey();
                break;
            case LARGER:
                nextIndex = selectableIndex.getValue();
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