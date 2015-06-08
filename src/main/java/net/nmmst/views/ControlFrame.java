package net.nmmst.views;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.nmmst.NConstants;
import net.nmmst.controller.ControllerFactory;
import net.nmmst.controller.StickTrigger;
import net.nmmst.controller.WheelTrigger;
import net.nmmst.media.BasePanel;
import net.nmmst.media.MediaWorker;
import net.nmmst.utils.RegisterUtil;
/**
 * The control node plays the movies and interacts with user.
 */
public final class ControlFrame {
    /**
     * Invokes a control frame.
     * @param args No use
     * @throws IOException If failed to open movie
     */
    public static void main(final String[] args) throws IOException {
        ControlFrameData frameData = new ControlFrameData();
        final int width = frameData.getNProperties().getInteger(
                NConstants.FRAME_WIDTH);
        final int height = frameData.getNProperties().getInteger(
                NConstants.FRAME_HEIGHT);
        final JFrame f = new VideoFrame(frameData);
        final Point point = new Point(16, 16);
        f.addKeyListener(frameData.getKeyListener());
        f.setCursor(f.getToolkit().createCustomCursor(
                new ImageIcon("").getImage(), point, ""));
        SwingUtilities.invokeLater(() -> {
            if (width <= 0 || height <= 0) {
                f.setExtendedState(JFrame.MAXIMIZED_BOTH);
            } else {
                f.setSize(new Dimension(width, height));
            }
            f.requestFocusInWindow();
            f.setUndecorated(true);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setVisible(true);
        });
    }
    /**
     * The data for control node. It provide a non-fusion media,
     * wheel and stick trigger which provide the interaction with user.
     */
    private static class ControlFrameData extends VideoData {
        /**
         * Captures the stick event and interacts with user
         * for playing the snapshot.
         */
        private final StickTrigger stickTrigger;
        /**
         * Media work.
         */
        private final MediaWorker media;
        /**
         * Captures the wheel event and sends the
         * {@link net.nmmst.utils.RequestUtil.SelectRequest} to master.
         */
        private final WheelTrigger wheelTrigger;
        /**
         * Displays the snapshot.
         */
        private final MultiPanelController panelController;
        /**
         * Constructs a data of control node.
         * @throws IOException If failed to open movies.
         */
        ControlFrameData() throws IOException {
            stickTrigger = new StickTrigger(getNProperties());
            media = MediaWorker.createMediaWorker(
                getNProperties(), getCloser(), stickTrigger);
            wheelTrigger = new WheelTrigger(getNProperties(), getCloser(),
                media);
            ControllerFactory.invokeTriggers(
                getNProperties(),
                getCloser(),
                Arrays.asList(stickTrigger, wheelTrigger));
            panelController = new MultiPanelController(
                    getNProperties(), getCloser(), media.getPanel(),
                    stickTrigger);
            RegisterUtil.invokeReporter(getCloser(),
                    getNodeInformation(), media.getMovieBuffer());
        }
        /**
         * Retrieves the key listener whihc is come from {@link StickTrigger}
         * and {@link WheelTrigger}.
         * @return The key listener
         */
        KeyListener getKeyListener() {
            return panelController;
        }
        @Override
        public BasePanel getMainPanel() {
            return panelController.getPanel();
        }
        @Override
        public MediaWorker getMediaWorker() {
            return media;
        }
        @Override
        public void setNextFlow(final int index) {
            media.setNextFlow(index);
        }
    }
    /**
     * Can't be instantiated with this ctor.
     */
    private ControlFrame() {
    }
}
