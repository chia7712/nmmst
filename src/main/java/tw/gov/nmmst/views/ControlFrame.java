package tw.gov.nmmst.views;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tw.gov.nmmst.NConstants;
import tw.gov.nmmst.controller.ControllerFactory;
import tw.gov.nmmst.controller.StickTrigger;
import tw.gov.nmmst.controller.WheelTrigger;
import tw.gov.nmmst.media.BasePanel;
import tw.gov.nmmst.media.MediaWorker;
import tw.gov.nmmst.utils.RegisterUtil;
/**
 * The control node plays the movies and interacts with user.
 */
public final class ControlFrame {
    /**
     * Log.
     */
    private static final Log LOG
            = LogFactory.getLog(ControlFrame.class);
    /**
     * Invokes a control frame.
     * @param args Properties path or no use
     * @throws IOException If failed to open movie
     */
    public static void main(final String[] args) throws IOException {
        File file = null;
        if (args.length == 1) {
            file = new File(args[0]);
        }
        if (file == null) {
            LOG.info("No found of configuration, use the default");
        } else {
            LOG.info("use the configuration : " + file.getPath());
        }
        ControlFrameData frameData = new ControlFrameData(file);
        final int width = frameData.getNProperties().getInteger(
                NConstants.FRAME_WIDTH);
        final int height = frameData.getNProperties().getInteger(
                NConstants.FRAME_HEIGHT);
        final JFrame f = new BaseFrame(frameData);
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
         * Media work.
         */
        private final MediaWorker media;
        /**
         * Displays the snapshot.
         */
        private final MultiPanelController panelController;
        /**
         * Constructs a data of control node.
         * @param file properties file
         * @throws IOException If failed to open movies.
         */
        ControlFrameData(final File file) throws IOException {
            super(file);
            List<ControllerFactory.Trigger> triggerList
                = new LinkedList();
            StickTrigger stickTrigger = null;
            if (getNProperties().getBoolean(NConstants.STICK_ENABLE)) {
                stickTrigger = new StickTrigger(getNProperties());
                triggerList.add(stickTrigger);
            }
            media = MediaWorker.createMediaWorker(
                getNProperties(), getCloser(), stickTrigger);
            if (getNProperties().getBoolean(NConstants.WHEEL_ENABLE)) {
                triggerList.add(new WheelTrigger(
                    getNProperties(), getCloser(), media));
            }
            ControllerFactory.invokeTriggers(
                getNProperties(),
                getCloser(),
                triggerList);
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
