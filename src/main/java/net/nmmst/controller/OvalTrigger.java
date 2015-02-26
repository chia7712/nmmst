package net.nmmst.controller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.nmmst.movie.BufferFactory;
import net.nmmst.movie.Frame;
import net.nmmst.movie.MovieAttribute;
import net.nmmst.player.NodeInformation;
import net.nmmst.tools.SerialStream;
import net.nmmst.processor.FrameProcessor;
import net.nmmst.request.Request;
import net.nmmst.tools.NMConstants;
import net.nmmst.tools.Ports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class OvalTrigger implements FrameProcessor, ControlTrigger {
    private static final Stroke STROKE = new BasicStroke(10);
    private static final Color DEFAULT_COLOR = Color.WHITE;
    private static final Color FOCUS_COLOR = Color.RED;
    private static final Logger LOG = LoggerFactory.getLogger(OvalTrigger.class);  
    private final Map<Integer, List<OvalInformation>> ovalWrapper = new TreeMap();
    private final AtomicInteger count = new AtomicInteger(10000000);
    private final AtomicBoolean pressed = new AtomicBoolean(false);
    private final List<OvalInformation> snapshots = BufferFactory.getSnapshots();
    private final NodeInformation masterInformation = NodeInformation.getMasterNode();
    private int specificFrameTime = 0;
    private OvalInformation curOvalInformation = null;
    public OvalTrigger() {
        List<OvalInformation> ovalInformations = OvalInformation.get();
        ovalInformations.stream().forEach((ovalInformation) -> {
            int movieIndex = ovalInformation.getMovieIndex();
            if (ovalWrapper.containsKey(movieIndex)) {
                ovalWrapper.get(movieIndex).add(ovalInformation);
            } else {
                List<OvalInformation> ovals = new LinkedList();
                ovals.add(ovalInformation);
                ovalWrapper.put(movieIndex, ovals);
            }
        });
    }
    @Override
    public void triggerOff(Component component) {
        if (component.getName().contains("X")) {
            if (component.getPollData() >= NMConstants.WHEEL_MAX_LIMIT) {
                count.incrementAndGet();
            }
            if (component.getPollData() <= NMConstants.WHEEL_MIN_LIMIT) {
                count.decrementAndGet();
            }
        }
        if (component.getName().contains("s")) {
            if (component.getPollData() == NMConstants.PRESS_LIMIT) {
                pressed.set(true);
            } else {
                pressed.set(false);
            }
        }
    }
    @Override
    public Controller.Type getType() {
        return Controller.Type.STICK;
    }

    @Override
    public boolean needProcess(Frame frame) {
        return snapshots.size() < NMConstants.MAX_SNAPSHOTS && ovalWrapper.containsKey(frame.getMovieAttribute().getIndex());
    }
    private static List<OvalInformation> getValidOvalInformations(Frame frame, Map<Integer, List<OvalInformation>> ovalWrapper) {
        List<OvalInformation> ovalInformations = new LinkedList();
        MovieAttribute attribute = frame.getMovieAttribute();
        if (!ovalWrapper.containsKey(attribute.getIndex())) {
            return ovalInformations;
        }
        ovalWrapper.get(attribute.getIndex())
                .stream()
                .filter((ovalInformation) -> !(ovalInformation.getMinMicroTime() > frame.getTimestamp() || ovalInformation.getMaxMicroTime() < frame.getTimestamp()))
                .forEach((ovalInformation) -> {
            ovalInformations.add(ovalInformation);
        });
        return ovalInformations;
    }
    private void syncImage(OvalInformation ovalInformation) {
        try (SerialStream client = new SerialStream(new Socket(masterInformation.getIP(), Ports.REQUEST_MASTER.get()))) {
            client.write(new Request(Request.Type.ADD_SNAPSHOTS, new Integer[]{ovalInformation.getNumber()}));
        } catch(IOException e) { 
            LOG.error(e.getMessage());
        }
    }
    @Override
    public void process(Frame frame) {
        int countSnapshot = count.get();
        boolean hasPressed = pressed.get();
        List<OvalInformation> ovalInformations = getValidOvalInformations(frame, ovalWrapper);
        Graphics2D g = (Graphics2D)frame.getImage().getGraphics();
        for (int index = 0; index != ovalInformations.size(); ++index) {
            OvalInformation ovalInformation = ovalInformations.get(index);
            g.setStroke(STROKE);
            //Draw the oval
            if (countSnapshot % ovalInformations.size() == index) {
                g.setColor(FOCUS_COLOR);
                if (hasPressed) {
                    if (snapshots.add(ovalInformation)) {
                        syncImage(ovalInformation);
                        specificFrameTime = NMConstants.SPECIFIC_FRAME_TIME;
                        curOvalInformation = ovalInformation;
                    }
                }
            } else {
                g.setColor(DEFAULT_COLOR);
            }
            //User capture the target, so we draw something on the frame
            if (specificFrameTime > 0 && curOvalInformation != null) {
                BufferedImage snapshotImage = curOvalInformation.getImage();
                g.drawImage(
                    snapshotImage, 
                    frame.getImage().getWidth() - 300, 
                    frame.getImage().getHeight() - 300, 
                    frame.getImage().getWidth(), 
                    frame.getImage().getHeight(),
                    0, 
                    0, 
                    snapshotImage.getWidth(), 
                    snapshotImage.getHeight(), 
                    null);
                --specificFrameTime;
            }
            g.drawOval(ovalInformation.getX(), ovalInformation.getY(), ovalInformation.getDiameter(), ovalInformation.getDiameter());
        }
        g.dispose();
        pressed.set(false);
    }
}
