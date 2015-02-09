package net.nmmst.controller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.nmmst.movie.Frame;
import net.nmmst.movie.MovieAttribute;
import net.nmmst.processor.FrameProcessor;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class OvalTrigger implements FrameProcessor, ControlTrigger {
    private static final int OVAL_PERIOD = 30;
    private static final Stroke STROKE = new BasicStroke(10);
    private static final Color DEFAULT_COLOR = Color.WHITE;
    private static final Color FOCUS_COLOR = Color.RED;
    private final Map<Integer, Set<OvalInformation>> ovalWrapper = new HashMap();
    private final AtomicInteger count = new AtomicInteger(10000000);
    private final AtomicBoolean pressed = new AtomicBoolean(false);
    private final Set<OvalInformation> pressedInformations = new HashSet();
    private int snapshotCount = 0;
    private OvalInformation curOvalInformation = null;
    public OvalTrigger() {
        List<OvalInformation> ovalInformations = OvalInformation.get();
        for (OvalInformation ovalInformation : ovalInformations) {
            System.out.println(ovalInformation);
            int index = ovalInformation.getIndex();
            if (ovalWrapper.containsKey(index)) {
                ovalWrapper.get(index).add(ovalInformation);
            } else {
                Set<OvalInformation> sets = new TreeSet();
                sets.add(ovalInformation);
                ovalWrapper.put(index, sets);
            }
        }
    }
    public void reset() {
        synchronized(pressedInformations) {
            pressedInformations.clear();
        }
        count.set(0);
        pressed.set(false);
    }
    public List<OvalInformation> getSnapshots() {
        synchronized(pressedInformations) {
            return new ArrayList(pressedInformations);
        }
    }
    @Override
    public void triggerOff(Component component) {
        if (component.getName().contains("X")) {
            if (component.getPollData() >= 1.0f) {
                count.incrementAndGet();
            }
            if (component.getPollData() <= -1.0f) {
                count.decrementAndGet();
            }
        }
        if (component.getName().contains("s")) {
            if (component.getPollData() == 1.0f) {
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
        return ovalWrapper.containsKey(frame.getMovieAttribute().getIndex());
    }
    private static List<OvalInformation> getValidOvalInformations(Frame frame, Map<Integer, Set<OvalInformation>> ovalWrapper) {
        List<OvalInformation> ovalInformations = new LinkedList();
        MovieAttribute attribute = frame.getMovieAttribute();
        if (!ovalWrapper.containsKey(attribute.getIndex())) {
            return ovalInformations;
        }
        for (OvalInformation ovalInformation : ovalWrapper.get(attribute.getIndex())) {
            if (ovalInformation.getMinMicroTime() > frame.getTimestamp() || ovalInformation.getMaxMicroTime() < frame.getTimestamp()) {
                continue;
            }
            ovalInformations.add(ovalInformation);
        }
        return ovalInformations;
    }
    @Override
    public void process(Frame frame) {
        int countSnapshot = count.get();
        boolean hasPressed = pressed.get();
        List<OvalInformation> ovalInformations = getValidOvalInformations(frame, ovalWrapper);
        for (int index = 0; index != ovalInformations.size(); ++index) {
            OvalInformation ovalInformation = ovalInformations.get(index);
            Graphics2D g = (Graphics2D)frame.getImage().getGraphics();
            g.setStroke(STROKE);
            //Draw the oval
            if (countSnapshot % ovalInformations.size() == index) {
                g.setColor(FOCUS_COLOR);
                if (hasPressed) {
                    synchronized(pressedInformations) {
                        if (pressedInformations.add(ovalInformation)) {
                            snapshotCount = OVAL_PERIOD;
                            curOvalInformation = ovalInformation;
                        }
                    }
                }
            } else {
                g.setColor(DEFAULT_COLOR);
            }
            //User capture the target, so we draw something on the frame
            if (snapshotCount > 0 && curOvalInformation != null) {
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

            }
            g.drawOval(ovalInformation.getX(), ovalInformation.getY(), ovalInformation.getDiameter(), ovalInformation.getDiameter());
            g.dispose();
        }
        pressed.set(false);
        --snapshotCount;
    }
}
