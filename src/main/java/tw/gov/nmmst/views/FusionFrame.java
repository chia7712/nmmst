package tw.gov.nmmst.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tw.gov.nmmst.NConstants;
import tw.gov.nmmst.media.MediaWorker;
import tw.gov.nmmst.processor.ProcessorFactory;
import tw.gov.nmmst.media.BasePanel;
import tw.gov.nmmst.media.BufferFactory;
import tw.gov.nmmst.media.MovieInfo;
import tw.gov.nmmst.processor.FrameProcessor;
import tw.gov.nmmst.processor.LinearProcessor;
import tw.gov.nmmst.utils.Painter;
import tw.gov.nmmst.utils.RegisterUtil;
import tw.gov.nmmst.utils.RequestUtil;
import tw.gov.nmmst.utils.RequestUtil.FusionTestRequest;
import tw.gov.nmmst.utils.RequestUtil.Request;
import tw.gov.nmmst.utils.RequestUtil.RequestType;
import tw.gov.nmmst.utils.RequestUtil.SetImageRequest;

/**
 * The fusion node plays the movies with fusing the image edge.
 */
public final class FusionFrame {

  /**
   * Log.
   */
  private static final Log LOG
          = LogFactory.getLog(FusionFrame.class);

  /**
   * Invokes a fusion frame.
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
      file = new File(FusionFrame.class.getName());
      LOG.info("No found of configuration, use the default and save the default properties in " + file.getAbsolutePath());
    } else {
      LOG.info("use the configuration : " + file.getPath());
    }
    FusionFrameData frameData = new FusionFrameData(file);
    final int width = frameData.getNProperties().getInteger(
            NConstants.FRAME_WIDTH);
    final int height = frameData.getNProperties().getInteger(
            NConstants.FRAME_HEIGHT);
    final JFrame f = new BaseFrame(frameData);
    final Point point = new Point(16, 16);
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
   * Data of fusion nodes.
   */
  private static class FusionFrameData extends VideoData {

    /**
     * Media components.
     */
    private final MediaWorker media;
    private final TrailerProcessor trailerProcessor;

    /**
     * Constructs the data with specified media.
     *
     * @param file The initial properties
     * @throws IOException If failed to open movies
     */
    FusionFrameData(final File file) throws IOException {
      super(file);
      BufferedImage init = Painter.loadOrStringImage(
              getNProperties(),
              NConstants.IMAGE_CONTROL_DASHBOARD);
      trailerProcessor = new TrailerProcessor("The End",
              getNProperties().getInteger(NConstants.GENERATED_FONT_SIZE),
              Color.WHITE);
      media = MediaWorker.newBuilder()
              .setBasePanel(new BasePanel(BasePanel.Mode.FILL))
              .setBufferedImage(init)
              .setCloser(getCloser())
              .setFrameProcessor(FrameProcessor.valueOf(Arrays.asList(
                      ProcessorFactory.createFrameProcessor(getNodeInformation().getLocation()),
                      trailerProcessor
              )))
              .setMovieBuffer(BufferFactory.createMovieBuffer(getNProperties()))
              .setMovieInfo(new MovieInfo(getNProperties()))
              .build();
      RegisterUtil.invokeReporter(getCloser(),
              getNodeInformation(), media.getMovieBuffer());
      getFunctions().put(RequestType.FUSION_TEST,
              (FrameData data, Request previousReq, Request currentReq)
              -> {
        if (currentReq.getClass() == FusionTestRequest.class) {
          FusionTestRequest fusionReq
                  = (RequestUtil.FusionTestRequest) currentReq;
          BufferedImage image = fusionReq.getImage();
          LinearProcessor processor
                  = new LinearProcessor(
                          data.getNodeInformation().getLocation(),
                          fusionReq.getFactor());
          processor.process(image);
          data.getMainPanel().write(image);
        }
      });
      getFunctions().put(RequestType.SET_IMAGE,
              (FrameData data, Request previousReq, Request currentReq)
              -> {
        if (currentReq.getClass() == SetImageRequest.class) {
          SetImageRequest fusionReq = (SetImageRequest) currentReq;
          List<BufferedImage> images = fusionReq.getImage();
          if (images.isEmpty()) {
            return;
          }
          int index = getNodeInformation()
                  .getLocation()
                  .ordinal();
          if (index >= images.size()) {
            index = 0;
          }
          data.getMainPanel().writeAndLock(images.get(index));
        }
      });
      getFunctions().put(RequestType.UNLOCK_IMAGE,
              (FrameData data, Request previousReq, Request currentReq)
              -> {
        data.getMediaWorker().getPanel().unlockImage();
        trailerProcessor.enableTrailer(true);
      });
      getFunctions().compute(RequestType.START, (k, v) -> {
        return (FrameData data, Request previousReq, Request currentReq) -> {
          trailerProcessor.enableTrailer(false);
          if (v != null) {
            v.work(data, previousReq, currentReq);
          }
        };
      });
      getFunctions().compute(RequestType.STOP, (k, v) -> {
        return (FrameData data, Request previousReq, Request currentReq) -> {
          trailerProcessor.enableTrailer(false);
          if (v != null) {
            v.work(data, previousReq, currentReq);
          }
        };
      });
    }

    @Override
    public BasePanel getMainPanel() {
      return media.getPanel();
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
  private FusionFrame() {
  }

  private static class TrailerProcessor implements FrameProcessor {

    private final int fontSize;
    private final String endString;
    private final Color color;
    private final AtomicBoolean isTrailer = new AtomicBoolean(false);

    TrailerProcessor(final String endString, final int fontSize, final Color color) {
      this.fontSize = fontSize;
      this.endString = endString;
      this.color = color;
    }

    void enableTrailer(final boolean v) {
      isTrailer.set(v);
    }

    @Override
    public Optional<BufferedImage> prePrintPanel(BufferedImage image) {
      if (image == null || !isTrailer.get()) {
        return Optional.ofNullable(image);
      }
      return Optional.of(Painter.getStringImage(image, endString, fontSize, color));
    }
  }
}
