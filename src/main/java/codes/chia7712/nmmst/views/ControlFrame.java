package codes.chia7712.nmmst.views;

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
import codes.chia7712.nmmst.NConstants;
import codes.chia7712.nmmst.NProperties;
import codes.chia7712.nmmst.controller.ControllerFactory;
import codes.chia7712.nmmst.controller.StickTrigger;
import codes.chia7712.nmmst.controller.WheelTrigger;
import codes.chia7712.nmmst.media.BasePanel;
import codes.chia7712.nmmst.media.BufferFactory;
import codes.chia7712.nmmst.media.BufferMetrics;
import codes.chia7712.nmmst.media.MediaWorker;
import codes.chia7712.nmmst.media.MovieBuffer;
import codes.chia7712.nmmst.media.MovieInfo;
import codes.chia7712.nmmst.processor.FrameProcessor;
import codes.chia7712.nmmst.threads.Closer;
import codes.chia7712.nmmst.utils.Painter;
import codes.chia7712.nmmst.utils.RegisterUtil;

/**
 * The control node plays the movies and interacts with user.
 */
public final class ControlFrame {

  private static final Log LOG = LogFactory.getLog(ControlFrame.class);

  /**
   * Invokes a control frame.
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
      file = new File(ControlFrame.class.getName());
      LOG.info("No found of configuration, use the default and save the default properties in " + file.getAbsolutePath());
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
   * The data for control node. It provide a non-fusion media, wheel and stick
   * trigger which provide the interaction with user.
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
     *
     * @param file properties file
     * @throws IOException If failed to open movies.
     */
    ControlFrameData(final File file) throws IOException {
      super(file);
      MovieBuffer buffer = BufferFactory.createMovieBuffer(getNProperties());
      List<ControllerFactory.Trigger> triggerList = new LinkedList<>();
      List<FrameProcessor> processorList = new LinkedList<>();
      StickTrigger stickTrigger = createStickTrigger(getNProperties(), getCloser());
      if (stickTrigger != null) {
        processorList.add(stickTrigger);
        triggerList.add(stickTrigger);
      }
      WheelTrigger wheelTrigger = createWheelTrigger(getNProperties(), getCloser(), buffer);
      if (wheelTrigger != null) {
        processorList.add(wheelTrigger);
        triggerList.add(wheelTrigger);
      }
      media = MediaWorker.newBuilder()
              .setBasePanel(new BasePanel(BasePanel.Mode.FILL))
              .setBufferedImage(Painter.getStringImage("Coming Soon",
                      getNProperties().getInteger(NConstants.GENERATED_IMAGE_WIDTH),
                      getNProperties().getInteger(NConstants.GENERATED_IMAGE_HEIGHT),
                      getNProperties().getInteger(NConstants.GENERATED_FONT_SIZE)))
              .setCloser(getCloser())
              .setFrameProcessor(FrameProcessor.valueOf(processorList))
              .setMovieBuffer(buffer)
              .setMovieInfo(new MovieInfo(getNProperties()))
              .build();
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
     * @param property The app properties
     * @param closer Close the errand thread
     * @param buffer Save the current frame
     * @return A stick trigger or null
     */
    private static WheelTrigger createWheelTrigger(final NProperties property,
            final Closer closer, final BufferMetrics buffer) {
      if (property.getBoolean(NConstants.WHEEL_ENABLE)) {
        return new WheelTrigger(property, closer, buffer);
      }
      return null;
    }

    /**
     * @param property The app properties
     * @param closer Close the errand thread
     * @return A stick trigger or null
     */
    private static StickTrigger createStickTrigger(final NProperties property,
            final Closer closer) {
      if (property.getBoolean(NConstants.STICK_ENABLE)) {
        return new StickTrigger(property, closer);
      }
      return null;
    }

    /**
     * Retrieves the key listener whihc is come from {@link StickTrigger} and
     * {@link WheelTrigger}.
     *
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
