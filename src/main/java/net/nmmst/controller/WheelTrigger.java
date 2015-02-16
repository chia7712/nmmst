package net.nmmst.controller;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Controller.Type;
import net.nmmst.movie.BufferFactory;
import net.nmmst.movie.Frame;
import net.nmmst.movie.MovieAttribute;
import net.nmmst.player.NodeInformation;
import net.nmmst.request.Request;
import net.nmmst.request.SelectRequest;
import net.nmmst.tools.Ports;
import net.nmmst.tools.SerialStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class WheelTrigger implements ControlTrigger {
    private static final Logger LOG = LoggerFactory.getLogger(WheelTrigger.class);  
    private static final long VALID_PERIOD = 1 * 1000 * 1000;
    private static final long MIN_SELECT_TIME = 3 * 1000 * 1000;
    private static final long MAX_SELECT_TIME = 10 * 1000 * 1000;
    private static final float MAX_WHEEL_VALUE = 0.9f;
    private static final float MIN_WHEEL_VALUE = -0.9f;
    private final NodeInformation masterInformation = NodeInformation.getMasterNode();
    private final AtomicReference<Frame> frameRef = BufferFactory.getFrameRef();
    private long preTime = 0;
    private final boolean[] beDecided = new boolean[2];
    private boolean isTimeToSelect() {
        Frame frame = frameRef.get();
        if (frame == null) {
            return false;
        }
        MovieAttribute attribute = frame.getMovieAttribute();
        if (attribute.getIndex() != 0 && attribute.getIndex() != 3) {
            return false;
        }
        final long currentTime = frame.getTimestamp();
        final long duration = attribute.getDuration() * 1000;
        final long diffTime = duration - currentTime;
        return !(diffTime <= MIN_SELECT_TIME || diffTime >= MAX_SELECT_TIME);
    }
    public void resetDecided() {
        beDecided[0] = false;
        beDecided[1] = false;
    }
    @Override
    public synchronized void triggerOff(Component component) {
        final long currentTime = System.currentTimeMillis() * 1000;
        if (currentTime - preTime < VALID_PERIOD) {
            return;
        }
        preTime = currentTime;
        boolean direction;
        if (component.getPollData() >= MAX_WHEEL_VALUE) {
            direction = true;
        } else if (component.getPollData() <= MIN_WHEEL_VALUE) {
            direction = false;
        } else {
            return;
        }
        if (isTimeToSelect() && (!beDecided[0] || !beDecided[1])) {
            Frame frame = frameRef.get();
            if (frame == null) {
                return;
            }
            MovieAttribute attribute = frame.getMovieAttribute();
            int[] indexs = new int[2];
            boolean[] values = new boolean[2];
            if (attribute.getIndex() == 0 && !beDecided[0]) {
                indexs[0] = 1;
                indexs[1] = 2;
                beDecided[0] = true;
            } else if (attribute.getIndex() == 3 && !beDecided[1]) {
                indexs[0] = 4;
                indexs[1] = 5;
                beDecided[1] = true;
            } else {
                return;
            }
            values[0] = !direction;
            values[1] = direction;
            try (SerialStream client = new SerialStream(new Socket(masterInformation.getIP(), Ports.REQUEST_MASTER.get()))) {
                client.write(new Request(Request.Type.SELECT, new SelectRequest(indexs, values)));
            } catch(IOException e) { 
                LOG.error(e.getMessage());
            }
        }
    }
    @Override
    public Type getType() {
        return Controller.Type.WHEEL;
    }
	
}