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

public class MultiPanelController implements KeyListener {
    private final CardLayout cardLayout = new CardLayout();
    private final BasePanel mainPanel = new BasePanel();
    private final BasePanel dashboardPanel;
    private final BasePanel stopPanel;
    private final SnapshotPanel snapshotPanel;
    private final List<String> keyList;
    private final BlockingQueue<String> keyQueue
            = new LinkedBlockingQueue();
    public MultiPanelController(final NProperties properties,
            final Closer closer,
            final JPanel extraPanel,
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
        panelList.add(extraPanel);
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
                        if (panel instanceof SnapshotPanel) {
                            ((SnapshotPanel)panel).addImages(
                                    stickTrigger.cloneSnapshot());
                        }
                    }
                    cardLayout.show(mainPanel, keyPress);
                }
            } catch (InterruptedException ex) {
            }
        });
        
    }
    public BasePanel getPanel() {
        return mainPanel;
    }
    @Override
    public void keyTyped(KeyEvent e) {
    }
    @Override
    public void keyPressed(KeyEvent e) {
    }
    @Override
    public void keyReleased(KeyEvent e) {
        keyQueue.offer(String.valueOf(e.getKeyChar()));
    }
    private static class SnapshotPanel extends JPanel {
        private final BufferedImage initImage;
        private final int rowNnumber;
        private final int columnNumber;
        private final List<BasePanel> currentPanel = new LinkedList();
        public SnapshotPanel(final NProperties properties) {
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
        public void addImages(List<BufferedImage> images) {
            synchronized(currentPanel) {
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
