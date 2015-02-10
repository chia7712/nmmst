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
import net.nmmst.request.Request;
import net.nmmst.request.SelectRequest;
import net.nmmst.tools.NMConstants;
import net.nmmst.tools.Ports;
import net.nmmst.tools.SerialStream;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class WheelTrigger implements ControlTrigger {
    private static final long VALID_PERIOD = 1 * 1000 * 1000;
    private static final long MIN_SELECT_TIME = 3 * 1000 * 1000;
    private static final long MAX_SELECT_TIME = 10 * 1000 * 1000;
    private static final float MAX_WHEEL_VALUE = 0.9f;
    private static final float MIN_WHEEL_VALUE = -0.9f;
    private final AtomicReference<Frame> frameRef = BufferFactory.getFrameRef();
    private long preTime = 0;
    private boolean isTimeToSelect() {
        Frame frame = frameRef.get();
        if (frame == null) {
            return false;
        }
        MovieAttribute attribute = frame.getMovieAttribute();
        final long currentTime = frame.getTimestamp();
        final long duration = attribute.getDuration() * 1000;
        final long diffTime = duration - currentTime;
        return !(diffTime <= MIN_SELECT_TIME || diffTime >= MAX_SELECT_TIME);
    }
    @Override
    public synchronized void triggerOff(Component component) {
        final long currentTime = System.currentTimeMillis() * 1000;
        if (currentTime - preTime < VALID_PERIOD) {
            return;
        }
        preTime = currentTime;
        
        boolean direction = false;
        if (component.getPollData() >= MAX_WHEEL_VALUE) {
            direction = true;
        } else if (component.getPollData() <= MIN_WHEEL_VALUE) {
            direction = false;
        } else {
            return;
        }
        if (isTimeToSelect()) {
            SerialStream client = null;
            try {
                Frame frame = frameRef.get();
                if (frame == null) {
                    return;
                }
                MovieAttribute attribute = frame.getMovieAttribute();
                int[] indexs = new int[2];
                boolean[] values = new boolean[2];
                if (attribute.getIndex() == 0) {
                    indexs[0] = 1;
                    indexs[1] = 2;
                } else if (attribute.getIndex() == 3) {
                    indexs[0] = 4;
                    indexs[1] = 5;
                } else {
                    return;
                }
                values[0] = !direction;
                values[1] = direction;
                client = new SerialStream(new Socket(NMConstants.MASTER_IP, Ports.REQUEST.get()));
                client.write(new Request(Request.Type.SELECT, new SelectRequest(indexs, values)));
            } catch(IOException e) { 
            } finally {
                if (client != null) {
                    client.close();
                }
            }
        }
    }
    @Override
    public Type getType() {
        return Controller.Type.WHEEL;
    }
	
}