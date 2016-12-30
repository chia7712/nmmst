package codes.chia7712.nmmst.views;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import codes.chia7712.nmmst.threads.AtomicCloser;
import codes.chia7712.nmmst.threads.Closer;
import codes.chia7712.nmmst.NodeInformation;
import codes.chia7712.nmmst.NProperties;
import codes.chia7712.nmmst.utils.RequestUtil;
import codes.chia7712.nmmst.utils.RequestUtil.Request;
import codes.chia7712.nmmst.utils.RequestUtil.RequestType;
import codes.chia7712.nmmst.utils.RequestUtil.SelectRequest;
import codes.chia7712.nmmst.utils.WindowsUtil;

/**
 * Base frame data is made up of {@link NProperties},
 * {@link Closer}, {@link NodeInformation},
 * {@link Request} queue and {@link RequestFunction}.
 */
public abstract class VideoData implements FrameData {

  private static final Log LOG = LogFactory.getLog(VideoData.class);
  /**
   * NProperties.
   */
  private final NProperties properties;
  /**
   * Closer.
   */
  private final Closer closer = new AtomicCloser();
  /**
   * Master information.
   */
  private final NodeInformation selfInformation;
  /**
   * Request queue.
   */
  private final BlockingQueue<RequestUtil.Request> requestQueue;
  /**
   * Request functions.
   */
  private final Map<RequestUtil.RequestType, RequestFunction> functions
          = new TreeMap<>();

  /**
   * Constructs a data of base frame.
   *
   * @throws IOException If failed to open movies
   */
  public VideoData() throws IOException {
    this(null);
  }

  /**
   * Constructs a data of base frame.
   *
   * @param file The initial properties
   * @throws IOException If failed to open movies
   */
  public VideoData(final File file) throws IOException {
    properties = new NProperties(file);
    selfInformation = NodeInformation.getNodeInformationByAddress(properties);
    requestQueue = RequestUtil.createRemoteQueue(selfInformation, closer);
    functions.put(RequestType.START, (data, previousReq, currentReq)
            -> {
      data.getMediaWorker().getPanel().unlockImage();
      data.getMediaWorker().setPause(false);
    });
    functions.put(RequestType.STOP, (data, previousReq, currentReq)
            -> data.getMediaWorker().stopAsync());
    functions.put(RequestType.PAUSE, (data, previousReq, currentReq)
            -> data.getMediaWorker().setPause(true));
    functions.put(RequestType.SELECT, (data, previousReq, currentReq)
            -> {
      if (currentReq.getClass() == SelectRequest.class) {
        SelectRequest select
                = (SelectRequest) currentReq;
        data.getMediaWorker().setNextFlow(
                select.getIndex());
        LOG.info("set next index:" + select.getIndex());
      }
    });
    functions.put(RequestType.REBOOT, (data, previousReq, currentReq)
            -> WindowsUtil.reboot());
    functions.put(RequestType.SHUTDOWN, (data, previousReq, currentReq)
            -> WindowsUtil.shutdown());
  }

  @Override
  public final Map<RequestUtil.RequestType, RequestFunction> getFunctions() {
    return functions;
  }

  @Override
  public final BlockingQueue<Request> getQueue() {
    return requestQueue;
  }

  @Override
  public final Closer getCloser() {
    return closer;
  }

  @Override
  public final NodeInformation getNodeInformation() {
    return selfInformation;
  }

  @Override
  public final NProperties getNProperties() {
    return properties;
  }
}
