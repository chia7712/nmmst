package net.nmmst.views;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.nmmst.NConstants;
/**
 * The master node is used for sending main operation for all video nodes.
 */
public class MasterFrame {
    public static void main(String[] args) throws Exception {
        MasterFrameData frameData = new MasterFrameData();
        final int width = frameData.getNProperties().getInteger(
                NConstants.FRAME_WIDTH);
        final int height = frameData.getNProperties().getInteger(
                NConstants.FRAME_HEIGHT);
        final BaseFrame f = new BaseFrame(frameData);
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
}
