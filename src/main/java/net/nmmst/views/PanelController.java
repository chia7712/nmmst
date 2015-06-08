package net.nmmst.views;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import javax.swing.JButton;
import javax.swing.JPanel;
import net.nmmst.NConstants;
import net.nmmst.NProperties;
import net.nmmst.media.BasePanel;
import net.nmmst.utils.Painter;
import net.nmmst.utils.RequestUtil;
/**
 * Maintains all the button and image for {@link MasterFrame}.
 */
public class PanelController {
    /**
     * This panel will send a
     * {@link net.nmmst.utils.RequestUtil.RequestType#START}
     * request if this panel is pressed.
     */
    private final BasePanel startPanel;
    /**
     * This panel will send a
     * {@link net.nmmst.utils.RequestUtil.RequestType#STOP}
     * request if this panel is pressed.
     */
    private final BasePanel stopPanel;
    /**
     * This panel will send a
     * {@link net.nmmst.utils.RequestUtil.RequestType#REBOOT}
     * request if this panel is pressed.
     */
    private final BasePanel rebootPanel;
    /**
     * The base panel for showing the background image.
     */
    private final BasePanel backgroundPanel;
    /**
     * This panel will send a
     * {@link net.nmmst.utils.RequestUtil.RequestType#FUSION_TEST}
     * request if this panel is pressed.
     */
    private final BasePanel testPanel;
    /**
     * This panel will send a
     * {@link net.nmmst.utils.RequestUtil.RequestType#INIT}
     * request if this panel is pressed.
     */
    private final JButton initButton = new JButton("展演準備");
    /**
     * This panel will send a
     * {@link net.nmmst.utils.RequestUtil.RequestType#SHUTDOWN}
     * request if this panel is pressed.
     */
    private final JButton shurdownButton = new JButton("睡覺");
    /**
     * This panel will send a
     * {@link net.nmmst.utils.RequestUtil.RequestType#WOL}
     * request if this panel is pressed.
     */
    private final JButton wakeupButton = new JButton("起床");
    /**
     * This panel will send a
     * {@link net.nmmst.utils.RequestUtil.RequestType#PARTY_1}
     * request if this panel is pressed.
     */
    private final JButton party1Button = new JButton("宴會一");
    /**
     * This panel will send a
     * {@link net.nmmst.utils.RequestUtil.RequestType#PARTY_2}
     * request if this panel is pressed.
     */
    private final JButton party2Button = new JButton("宴會二");
    /**
     * This panel will send a
     * {@link net.nmmst.utils.RequestUtil.RequestType#LIGHT_OFF}
     * request if this panel is pressed.
     */
    private final JButton lightOffButton = new JButton("關閉燈光");
    /**
     * All components are to set listener.
     */
    private final List<Component> componentList
            = new LinkedList();
    /**
     * Adds the listener to panel.
     * @param panel The panel to set
     * @param listener The listener to add
     */
    private void addListener(final JPanel panel,
            final MouseReleasedListener listener) {
        panel.addMouseListener(listener);
        componentList.add(panel);
    }
    /**
     * Adds the listener to button.
     * @param button The button to set
     * @param listener The listener to add
     */
    private void addListener(final JButton button,
            final ActionListener listener) {
        button.addActionListener(listener);
        componentList.add(button);
    }
    /**
     * Gets the main panel.
     * @return The main panel
     */
    public final BasePanel getMainPanel() {
        return backgroundPanel;
    }
    /**
     * Constructs the panel controller for displaing the master node.
     * @param properties NProperties
     * @param requestQueue The request queue
     */
    public PanelController(final NProperties properties,
            final BlockingQueue<RequestUtil.Request> requestQueue) {
        final Dimension startDim = new Dimension(400, 400);
        final Dimension stopDim = new Dimension(100, 100);
        final Dimension rebootDim = new Dimension(100, 100);
        final Dimension testDim = new Dimension(100, 100);
        startPanel = new BasePanel(Painter.loadOrStringImage(
            properties, NConstants.IMAGE_MASTER_START),
            BasePanel.Mode.FILL);
        startPanel.setPreferredSize(startDim);
        addListener(startPanel, event -> requestQueue.offer(
            new RequestUtil.Request(RequestUtil.RequestType.START)));
        stopPanel = new BasePanel(Painter.loadOrStringImage(
            properties, NConstants.IMAGE_MASTER_STOP),
            BasePanel.Mode.FILL);
        stopPanel.setPreferredSize(stopDim);
        addListener(stopPanel, event -> requestQueue.offer(
            new RequestUtil.Request(RequestUtil.RequestType.STOP)));
        rebootPanel = new BasePanel(Painter.loadOrStringImage(
            properties, NConstants.IMAGE_MASTER_REFRESH),
            BasePanel.Mode.FILL);
        rebootPanel.setPreferredSize(rebootDim);
        addListener(rebootPanel, event
            -> requestQueue.offer(new RequestUtil.Request(
                RequestUtil.RequestType.REBOOT)));
        testPanel = new BasePanel(Painter.loadOrStringImage(
            properties, NConstants.IMAGE_MASTER_TEST),
            BasePanel.Mode.FILL);
        testPanel.setPreferredSize(testDim);
        addListener(testPanel, event -> {
        });
        addListener(initButton, event
            -> requestQueue.offer(new RequestUtil.Request(
                RequestUtil.RequestType.INIT)));
        addListener(shurdownButton, event
            -> requestQueue.offer(new RequestUtil.Request(
                RequestUtil.RequestType.SHUTDOWN)));
        addListener(wakeupButton, event
            -> requestQueue.offer(new RequestUtil.Request(
                RequestUtil.RequestType.WOL)));
        addListener(party1Button, event
            -> requestQueue.offer(new RequestUtil.Request(
                RequestUtil.RequestType.PARTY_1)));
        addListener(party2Button, event
            -> requestQueue.offer(new RequestUtil.Request(
                RequestUtil.RequestType.PARTY_2)));
        addListener(lightOffButton, event
            -> requestQueue.offer(new RequestUtil.Request(
                RequestUtil.RequestType.LIGHT_OFF)));
        backgroundPanel = new BasePanel(Painter.loadOrStringImage(
            properties, NConstants.IMAGE_MASTER_BACKGROUND),
            BasePanel.Mode.FILL);
        componentList.stream().forEach(backgroundPanel::add);
    }
    /**
     * A wrapper for implementing th functional interface.
     */
    private interface MouseReleasedListener extends MouseListener {
        @Override
        default void mouseClicked(MouseEvent e) {
        }
        @Override
        default void mousePressed(MouseEvent e) {
        }
        @Override
        default void mouseEntered(MouseEvent e) {
        }
        @Override
        default void mouseExited(MouseEvent e) {
        }
    }
}
