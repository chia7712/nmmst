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
import net.nmmst.controller.ControlEvent;
import net.nmmst.controller.StickTrigger;
import net.nmmst.controller.WheelTrigger;
import net.nmmst.media.BasePanel;
import net.nmmst.media.MediaWorker;
import net.nmmst.utils.RegisterUtil;

public class ControlFrame {
    public static void main(String[] args) throws IOException {
        ControlFrameData frameData = new ControlFrameData();
        final int width = frameData.getNProperties().getInteger(
                NConstants.FRAME_WIDTH);
        final int height = frameData.getNProperties().getInteger(
                NConstants.FRAME_HEIGHT);
        final JFrame f = new BaseFrame(frameData);
        f.addKeyListener(frameData.getKeyListener());
        f.setCursor(f.getToolkit().createCustomCursor(
                new ImageIcon("").getImage(),new Point(16, 16),""));
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
    public static class ControlFrameData extends BaseFrameData {
        private final StickTrigger stickTrigger;
        private final MediaWorker media;
        private final WheelTrigger wheelTrigger;
        private final MultiPanelController panelController;
        public ControlFrameData() throws IOException {
            stickTrigger = new StickTrigger(getNProperties());
            media = MediaWorker.createMediaWorker(
                getNProperties(), getCloser(), stickTrigger);
            wheelTrigger = new WheelTrigger(getNProperties(), getCloser(),
                media);
            getCloser().invokeNewThread(ControlEvent.createControlEvent(
                    getNProperties(),
                    Arrays.asList(stickTrigger, wheelTrigger)));
            panelController = new MultiPanelController(
                    getNProperties(), getCloser(), media.getPanel(),
                    stickTrigger);
            RegisterUtil.invokeReporter(getCloser(),
                    getNodeInformation(), media.getMovieBuffer());
        }
        public KeyListener getKeyListener() {
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
    }
}
