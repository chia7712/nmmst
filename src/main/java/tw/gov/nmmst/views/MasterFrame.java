package tw.gov.nmmst.views;

import java.awt.Dimension;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import tw.gov.spright.nmmst.NConstants;
/**
 * The master node is used for sending main operation for all video nodes.
 */
public final class MasterFrame {
    /**
     * Invokes a master frame.
     * @param args No use
     * @throws IOException If failed to open movie
     */
    public static void main(final String[] args) throws IOException {
        MasterFrameData frameData = new MasterFrameData();
        final int width = frameData.getNProperties().getInteger(
                NConstants.FRAME_WIDTH);
        final int height = frameData.getNProperties().getInteger(
                NConstants.FRAME_HEIGHT);
        final VideoFrame f = new VideoFrame(frameData);
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
     * Can't be instantiated with this ctor.
     */
    private MasterFrame() {
    }
}
