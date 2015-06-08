package net.nmmst.views;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.JPanel;
import net.nmmst.threads.Closer;
import net.nmmst.media.BasePanel;
import net.nmmst.NConstants;
import net.nmmst.NProperties;
import net.nmmst.controller.StickTrigger;
import net.nmmst.utils.Painter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Used in the control node for displaing multi-panels.
 * The corresponding panel will be shown when
 * the specified key had be pressed.
 */
public class MultiPanelController implements KeyListener {
    /**
     * Log.
     */
    private static final Logger LOG
        = LoggerFactory.getLogger(MultiPanelController.class);
    /**
     * Saves the multi-panels.
     */
    private final CardLayout cardLayout = new CardLayout();
    /**
     * A layout for all panels.
     */
    private final BasePanel mainPanel = new BasePanel();
    /**
     * Draws the dashboard image.
     */
    private final BasePanel dashboardPanel;
    /**
     * Draws the stop image.
     */
    private final BasePanel stopPanel;
    /**
     * Draws the snapshot images.
     */
    private final SnapshotPanel snapshotPanel;
    /**
     * The keys correspond to panels.
     */
    private final List<String> keyList;
    /**
     * Queues the request about swiching the panel.
     */
    private final BlockingQueue<String> keyQueue
            = new LinkedBlockingQueue();
    /**
     * Constructs a multi-panel controller with the stric trigger which provides
     * the snapshot images captured by user.
     * @param properties NProperties
     * @param closer Services the thread be invoked in this construction
     * @param videoPanel Draws the video image
     * @param stickTrigger Provides the snapshot images
     */
    public MultiPanelController(final NProperties properties,
            final Closer closer,
            final JPanel videoPanel,
            final StickTrigger stickTrigger) {
        dashboardPanel = new BasePanel(Painter.loadOrStringImage(
                properties,
                NConstants.IMAGE_CONTROL_DASHBOARD),
                BasePanel.Mode.FILL);
        stopPanel = new BasePanel(Painter.loadOrStringImage(
                properties,
                NConstants.IMAGE_CONTROL_STOP),
                BasePanel.Mode.FILL);
        snapshotPanel = new SnapshotPanel(properties);
        keyList = properties.getStrings(NConstants.CONTROL_KEYBOARD);
        final List<JPanel> panelList = new LinkedList();
        panelList.add(dashboardPanel);
        panelList.add(videoPanel);
        panelList.add(snapshotPanel);
        final int snapshotPanelIndex = panelList.size() - 1;
        panelList.add(stopPanel);
        mainPanel.setLayout(cardLayout);
        for (int index = 0; index != keyList.size()
                && index != panelList.size(); ++index) {
            mainPanel.add(panelList.get(index), keyList.get(index));
        }
        closer.invokeNewThread(() -> {
            try {
                String keyPress = keyQueue.take();
                if (keyList.stream().anyMatch(
                    key -> key.compareToIgnoreCase(keyPress) == 0)) {
                    if (keyPress.equals(keyList.get(snapshotPanelIndex))) {
                        JPanel panel = panelList.get(snapshotPanelIndex);
                        if (panel.getClass() == SnapshotPanel.class) {
                            ((SnapshotPanel) panel).addImages(
                                    stickTrigger.cloneSnapshot());
                        }
                    }
                    cardLayout.show(mainPanel, keyPress);
                }
            } catch (InterruptedException ex) {
                LOG.debug(ex.getMessage());
            }
        });
    }
    /**
     * Returns the main panel which maintains all panels in this object.
     * @return The main panel
     */
    public final BasePanel getPanel() {
        return mainPanel;
    }
    @Override
    public void keyTyped(final KeyEvent e) {
    }
    @Override
    public void keyPressed(final KeyEvent e) {
    }
    @Override
    public final void keyReleased(final KeyEvent e) {
        keyQueue.offer(String.valueOf(e.getKeyChar()));
    }
    /**
     * Draws all snapshot images in a grid layout.
     */
    private static class SnapshotPanel extends JPanel {
        /**
         * The initial image for replacing the empty snapshot.
         */
        private final BufferedImage initImage;
        /**
         * Row number.
         */
        private final int rowNnumber;
        /**
         * Column number.
         */
        private final int columnNumber;
        /**
         * The panels draw the snpahost image.
         */
        private final List<BasePanel> currentPanel = new LinkedList();
        /**
         * Constructs a snapshot panel with default row number and
         * column number from the {@link NProperties}.
         * @param properties NProperties
         */
        SnapshotPanel(final NProperties properties) {
            columnNumber = (int) Math.pow(properties.getDouble(
                    NConstants.SNAPSHOT_SCALE), -1);
            rowNnumber = columnNumber;
            initImage = Painter.getFillColor(
                properties.getInteger(NConstants.GENERATED_IMAGE_WIDTH),
                properties.getInteger(NConstants.GENERATED_IMAGE_HEIGHT),
                Color.BLACK);
            setLayout(new GridLayout(rowNnumber, columnNumber));
            for (int x = 0; x != columnNumber; ++x) {
                for (int y = 0; y != rowNnumber; ++y) {
                    BasePanel panel = new BasePanel(initImage);
                    add(panel, x * columnNumber + y);
                    currentPanel.add(panel);
                }
            }
        }
        /**
         * Adds the images.
         * @param images The images
         */
        void addImages(final List<BufferedImage> images) {
            synchronized (currentPanel) {
                for (int x = 0; x != columnNumber; ++x) {
                    for (int y = 0; y != rowNnumber; ++y) {
                        final int index = x * columnNumber + y;
                        if (index >= currentPanel.size()) {
                            return;
                        }
                        if (index >= images.size()) {
                            currentPanel.get(index).write(initImage);
                        } else {
                            currentPanel.get(index).write(images.get(index));
                        }
                    }
                }
            }
        }
    }
}
