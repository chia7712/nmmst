package codes.chia7712.nmmst.views;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JOptionPane;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import codes.chia7712.nmmst.NConstants;
import codes.chia7712.nmmst.media.MovieInfo;
import codes.chia7712.nmmst.NodeInformation;
import codes.chia7712.nmmst.utils.RegisterUtil;
import codes.chia7712.nmmst.NProperties;
import codes.chia7712.nmmst.controller.DioInterface;
import codes.chia7712.nmmst.media.MovieAttribute;
import codes.chia7712.nmmst.media.MovieInfo.PlayFlow;
import codes.chia7712.nmmst.utils.RequestUtil.Request;
import codes.chia7712.nmmst.utils.RequestUtil.RequestType;
import codes.chia7712.nmmst.utils.SerialStream;

/**
 * Start the flow to send the following command. 1) Checks the buffer for all
 * video nodes. If any node hava insufficient buffer, this flow will be broke.
 * 2) Sends start request to all video nodes. If any connection error happen,
 * this flow will be broke. 3) Sends command to invoke all light one by one.
 */
public class StartFlow {

  /**
   * Trigger the action in the flow end.
   */
  @FunctionalInterface
  public interface Trigger {

    /**
     * Trigger something.
     */
    void endFlow();
  }
  /**
   * Log.
   */
  private static final Log LOG
          = LogFactory.getLog(StartFlow.class);
  /**
   * Dio interface.
   */
  private final DioInterface dio;
  /**
   * Gets the request port from this properties.
   */
  private final NProperties properties;
  /**
   * Watches all buffer status.
   */
  private final RegisterUtil.Watcher watcher;
  /**
   * Start flag.
   */
  private final AtomicBoolean started = new AtomicBoolean(false);
  /**
   * All movies are used in this play.
   */
  private final MovieInfo info;
  /**
   * The play flow.
   */
  private PlayFlow flow;
  /**
   * Executes a thread for conrtrol the dio devices.
   */
  private ExecutorService service;

  /**
   * Constructs the start flow by specified movie order and dio.
   *
   * @param properties NProperties
   * @param watcher Watches the buffer status for all video nodes
   * @param info MovieInfo
   * @param dio DioInterface
   */
  public StartFlow(final NProperties properties, final RegisterUtil.Watcher watcher,
          final MovieInfo info, final DioInterface dio) {
    this.properties = properties;
    this.watcher = watcher;
    this.info = info;
    this.dio = dio;
  }

  /**
   * Synchronously stop all action.
   *
   * @throws InterruptedException If the sleep had be interrupted
   */
  public final void stopMasterPlay() throws InterruptedException {
    if (service != null) {
      service.shutdownNow();
      service.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
      service = null;
    }
  }

  /**
   * Sets a movie index to next order.
   *
   * @param index The movie index
   */
  public final void setNextFlow(final int index) {
    if (flow != null) {
      flow.setNextFlow(index);
    }
  }

  /**
   * Returns {@code true} if non-start.
   *
   * @return {@code true} if non-start
   */
  public final boolean isStart() {
    return started.get();
  }

  /**
   * Starts this flow.
   *
   * @param trigger To trigger the end
   * @throws InterruptedException If this flow is broke
   * @throws IOException If failed to send request to any video nodes
   */
  public final void invokeStartThread(final Trigger trigger)
          throws IOException, InterruptedException {
    //The fucking man make me to do this
    final long cutTime = 144 * 1000 * 1000;
    final int specifiedIndex = 6;
    if (!started.compareAndSet(false, true)) {
      return;
    }
    if (watcher.isBufferInsufficient()) {
      started.set(false);
      LOG.info("No enough memory for nodes");
      if (properties.getBoolean(NConstants.SHOW_WARN_WINDOWS)) {
        JOptionPane.showMessageDialog(null,
                "影片還在準備中...請晚點再播放");
      }
      return;
    }
    if (service != null) {
      service.shutdownNow();
    }
    service = Executors.newSingleThreadExecutor();
    service.execute(() -> {
      try {
        flow = info.createPlayFlow();
        SerialStream.sendAll(NodeInformation.getVideoNodes(properties),
                new Request(RequestType.START), true);
        final int nanoToMicro = 1000;
        while (flow.hasNext()) {
          MovieAttribute attribute = flow.next();
          final long startTime = System.nanoTime();
          dio.light(attribute.getIndex());
          LOG.info("dio index = " + attribute.getIndex());
          long sleepTime = attribute.getDuration()
                  - (System.nanoTime() - startTime)
                  / nanoToMicro;
          if (attribute.getIndex() == specifiedIndex) {
            sleepTime -= cutTime;
            LOG.info("Reach the specified index, set the "
                    + "sleep time to " + sleepTime + "microseconds");
          }
          TimeUnit.MICROSECONDS.sleep(sleepTime);
        }
        if (trigger != null) {
          trigger.endFlow();
        }
      } catch (InterruptedException e) {
        LOG.debug(e);
      } catch (IOException e) {
        LOG.error(e);
      } finally {
        started.set(false);
      }
    });
  }
}
