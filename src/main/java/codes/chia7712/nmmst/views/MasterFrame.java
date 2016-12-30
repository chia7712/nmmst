package codes.chia7712.nmmst.views;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import codes.chia7712.nmmst.NConstants;

/**
 * The master node is used for sending main operation for all video nodes.
 */
public final class MasterFrame {

  /**
   * Log.
   */
  private static final Log LOG
          = LogFactory.getLog(MasterFrame.class);

  /**
   * Invokes a master frame.
   *
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
    MasterFrameData frameData = new MasterFrameData(file);
    final int width = frameData.getNProperties().getInteger(
            NConstants.FRAME_WIDTH);
    final int height = frameData.getNProperties().getInteger(
            NConstants.FRAME_HEIGHT);
    final JFrame f = new BaseFrame(frameData);
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
