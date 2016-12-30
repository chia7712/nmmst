package tw.gov.nmmst.controller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javafx.util.Pair;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Controller.Type;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tw.gov.nmmst.threads.Closer;
import tw.gov.nmmst.NConstants;
import tw.gov.nmmst.NProperties;
import tw.gov.nmmst.NodeInformation;
import tw.gov.nmmst.controller.DirectionDetector.Trend;
import tw.gov.nmmst.media.BufferMetrics;
import tw.gov.nmmst.processor.FrameProcessor;
import tw.gov.nmmst.utils.RequestUtil.SelectRequest;
import tw.gov.nmmst.utils.SerialStream;

/**
 * A wheel trigger captures the wheel event and caluculate the change of wheel
 * direction. Any wheel direction may decide the next movie if the moment of
 * change is valid.
 */
public final class WheelTrigger implements ControllerFactory.Trigger, FrameProcessor {

  private static final int DESC_SIZE = 100;
  private static final int LINE_SIZE = 15;
  /**
   * Log.
   */
  private static final Log LOG = LogFactory.getLog(WheelTrigger.class);
  /**
   * Detects the change of direction.
   */
  private final DirectionDetector detector;
  /**
   * The selectable timestamp must be within this period.
   */
  private final Pair<Long, Long> selectablePeriod;
  /**
   * Queues the requests.
   */
  private final BlockingQueue<DirectionDetector.Trend> trendQueue = new LinkedBlockingQueue<>();
  /**
   * The movie index and selectable indexes.
   */
  private final Map<Integer, Pair<Integer, Integer>> selectable;
  private final Map<Integer, Pair<LittleMovie, LittleMovie>> selectableLittleMovie = new TreeMap<>();
  /**
   * Media work provides movie attribute to check the validity to enable select
   * function.
   */
  private final BufferMetrics buffer;

  private final Set<Integer> hasSelectedIndex = new TreeSet<>();

  private volatile Trend currentTrend = Trend.SMALLER;

  /**
   * Constructs a wheel trigger with the arguments of properties.
   *
   * @param properties NProperties
   * @param closer Closer
   * @param buffer Media worker
   */
  public WheelTrigger(
          final NProperties properties,
          final Closer closer,
          final BufferMetrics buffer) {
    this.detector = new DirectionDetector(
            new Pair<>(properties.getDouble(NConstants.WHEEL_MAX_VALUE),
                    properties.getDouble(NConstants.WHEEL_MIN_VALUE)),
            new Pair<>(properties.getDouble(NConstants.WHEEL_MAX_INIT_VALUE),
                    properties.getDouble(NConstants.WHEEL_MIN_INIT_VALUE)));
    this.selectablePeriod = new Pair<>(
            properties.getLong(NConstants.WHEEL_ENABLE_MIN_MICROTIME_PERIOD),
            properties.getLong(NConstants.WHEEL_ENABLE_MAX_MICROTIME_PERIOD));
    this.selectable = NProperties.stringToSelectable(properties.getString(NConstants.MOVIE_SELECT));
    this.buffer = buffer;
    List<File> highlightMovies = properties.getStrings(NConstants.HIGHLIGHT_PATH)
            .stream().map(v -> new File(v)).collect(Collectors.toList());
    List<String> highlightDesc = new ArrayList<>(properties.getStrings(NConstants.HIGHLIGHT_DESCRIPTION));
    if (highlightMovies.size() != selectable.size() * 2
            || (highlightDesc.size() != highlightMovies.size())) {
      throw new RuntimeException(
              "Mismatch size. The selectable size is " + selectable.size()
              + ", the highlight movie size is " + highlightMovies.size()
              + ", the highlight Desc size is " + highlightDesc.size());
    }
    selectable.entrySet().forEach(entry -> {
      final File leftFile = highlightMovies.remove(0);
      final String leftDesc = highlightDesc.remove(0);
      final File rightFile = highlightMovies.remove(0);
      final String rightDesc = highlightDesc.remove(0);
      try {
        selectableLittleMovie.put(entry.getKey(), new Pair<>(
                new LittleMovie(leftFile,
                        0, 6, TimeUnit.SECONDS, 0.25, leftDesc),
                new LittleMovie(rightFile,
                        0, 6, TimeUnit.SECONDS, 0.25, rightDesc)
        ));
      } catch (IOException ex) {
        String msg = "Failed to load highlight movie:"
                + leftFile
                + ", "
                + rightFile;
        LOG.error(msg);
        throw new RuntimeException(msg);
      }
    });
    if (selectable.size() != selectableLittleMovie.size()) {
      throw new RuntimeException("Mismatch size. The selectable size is " + selectable.size()
              + ", the selectableLittleMovie size is " + selectableLittleMovie.size());
    }
    NodeInformation masterInfo = NodeInformation.getMasterNode(properties).get();
    closer.invokeNewThread(() -> {
      try {
        final int currentIndex = buffer.getCurrentMovieIndex();
        final int lastIndex = buffer.getLastMovieIndex();
        if (currentIndex != lastIndex) {
          return;
        }
        Pair<Integer, Integer> selectableIndexes = selectable.get(currentIndex);
        if (selectableIndexes == null) {
          return;
        }
        final long currentTimestamp = buffer.getCurrentTimestamp();
        final long currentDuration = buffer.getCurrentDuration();
        final long diffTime = currentDuration - currentTimestamp;
        if (diffTime >= 0 && diffTime < selectablePeriod.getKey()
                && !hasSelectedIndex.contains(currentIndex)) {
          hasSelectedIndex.add(currentIndex);
          SelectRequest request = null;
          switch (currentTrend) {
            case SMALLER:
              request = new SelectRequest(selectableIndexes.getKey());
              break;
            case LARGER:
              request = new SelectRequest(selectableIndexes.getValue());
              break;
            default:
              break;
          }
          if (request != null) {
            SerialStream.send(masterInfo, request);
          }
        }
      } catch (InterruptedException | IOException e) {
        LOG.error(e);
      }
    }, () -> TimeUnit.SECONDS.sleep(1));
  }

  /**
   * @return Retrieves the inner queue
   */
  BlockingQueue<DirectionDetector.Trend> getQueue() {
    return trendQueue;
  }

  @Override
  public void triggerOff(final Component component) {
    final long currentTimestamp = buffer.getCurrentTimestamp();
    final long currentDuration = buffer.getCurrentDuration();
    final long diffTime = currentDuration - currentTimestamp;
    if (diffTime <= 0 || diffTime < selectablePeriod.getKey()
            || diffTime > selectablePeriod.getValue()) {
      return;
    }
    switch (detector.detect(component.getPollData())) {
      case SMALLER:
        currentTrend = Trend.SMALLER;
        break;
      case LARGER:
        currentTrend = Trend.LARGER;
        break;
      default:
        break;
    }
  }

  @Override
  public Type getType() {
    return Controller.Type.WHEEL;
  }

  @Override
  public Optional<BufferedImage> playOver(BufferedImage image) {
    hasSelectedIndex.clear();
    return Optional.ofNullable(image);
  }

  @Override
  public Optional<BufferedImage> prePrintPanel(BufferedImage image) {
    Pair<LittleMovie, LittleMovie> movies = selectableLittleMovie.get(buffer.getCurrentMovieIndex());
    if (movies == null || image == null) {
      return Optional.ofNullable(image);
    }
    final long currentTimestamp = buffer.getCurrentTimestamp();
    final long currentDuration = buffer.getCurrentDuration();
    final long diffTime = currentDuration - currentTimestamp;
    if (diffTime <= 0 || diffTime < selectablePeriod.getKey()
            || diffTime > selectablePeriod.getValue()) {
      return Optional.ofNullable(image);
    }
    BufferedImage leftImage = movies.getKey().next().getImage();
    BufferedImage rightImage = movies.getValue().next().getImage();
    switch (currentTrend) {
      case SMALLER:
        image = writeLift(image, leftImage, true, movies.getKey().getDescription());
        image = writeRight(image, rightImage, false, movies.getValue().getDescription());
        break;
      case LARGER:
        image = writeLift(image, leftImage, false, movies.getKey().getDescription());
        image = writeRight(image, rightImage, true, movies.getValue().getDescription());

        break;
      default:
    }
    return Optional.of(image);
  }

  private static BufferedImage writeRight(final BufferedImage image,
          final BufferedImage snapshot, final boolean selected, final String str) {
    Graphics2D g = (Graphics2D) image.getGraphics();
    final Pair<Integer, Integer> start = new Pair<>(
            image.getWidth() / 2,
            image.getHeight() / 4);
    final Pair<Integer, Integer> end = new Pair<>(
            image.getWidth(),
            (image.getHeight() * 3) / 4);
    g.drawImage(
            snapshot,
            start.getKey(),
            start.getValue(),
            end.getKey(),
            end.getValue(),
            0,
            0,
            snapshot.getWidth(),
            snapshot.getHeight(),
            null);
    if (selected) {
      g.setColor(Color.RED);
      g.setStroke(new BasicStroke(LINE_SIZE));
      g.drawRect(
              start.getKey(),
              start.getValue(),
              image.getWidth() / 2,
              image.getHeight() / 2
      );
    }
    g.setFont(new Font("Serif", Font.BOLD, DESC_SIZE));
    g.drawString(str, getStringPosition(start.getKey(), end.getKey()), (image.getHeight() + end.getValue()) / 2);
    g.dispose();
    return image;
  }

  private static BufferedImage writeLift(final BufferedImage image,
          final BufferedImage snapshot, final boolean selected, final String str) {
    Graphics2D g = (Graphics2D) image.getGraphics();
    final Pair<Integer, Integer> start = new Pair<>(
            0,
            image.getHeight() / 4);
    final Pair<Integer, Integer> end = new Pair<>(
            image.getWidth() / 2,
            (image.getHeight() * 3) / 4);
    g.drawImage(
            snapshot,
            start.getKey(),
            start.getValue(),
            end.getKey(),
            end.getValue(),
            0,
            0,
            snapshot.getWidth(),
            snapshot.getHeight(),
            null);
    if (selected) {
      g.setColor(Color.RED);
      g.setStroke(new BasicStroke(LINE_SIZE));
      g.drawRect(
              start.getKey(),
              start.getValue(),
              image.getWidth() / 2,
              image.getHeight() / 2
      );
    }
    g.setFont(new Font("Serif", Font.BOLD, DESC_SIZE));
    g.drawString(str, getStringPosition(start.getKey(), end.getKey()), (image.getHeight() + end.getValue()) / 2);
    g.dispose();
    return image;
  }

  private static int getStringPosition(final int start, final int end) {
    return (int) ((double) (end + start) / 2.5f);
  }
}
