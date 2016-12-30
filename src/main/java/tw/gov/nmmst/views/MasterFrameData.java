package tw.gov.nmmst.views;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tw.gov.nmmst.NConstants;
import tw.gov.nmmst.NProperties;
import tw.gov.nmmst.NodeInformation;
import tw.gov.nmmst.controller.DioFactory;
import tw.gov.nmmst.controller.DioInterface;
import tw.gov.nmmst.media.BasePanel;
import tw.gov.nmmst.media.MediaWorker;
import tw.gov.nmmst.media.MovieInfo;
import tw.gov.nmmst.threads.AtomicCloser;
import tw.gov.nmmst.threads.BaseTimer;
import tw.gov.nmmst.threads.Closer;
import tw.gov.nmmst.utils.Painter;
import tw.gov.nmmst.utils.ProjectorUtil;
import tw.gov.nmmst.utils.RegisterUtil;
import tw.gov.nmmst.utils.RequestUtil;
import tw.gov.nmmst.utils.RequestUtil.SelectRequest;
import tw.gov.nmmst.utils.RequestUtil.SetImageRequest;
import tw.gov.nmmst.utils.SerialStream;
import tw.gov.nmmst.utils.WolUtil;

/**
 * The data of master frame.
 */
public class MasterFrameData implements FrameData {

  /**
   * Log.
   */
  private static final Log LOG = LogFactory.getLog(MasterFrameData.class);
  /**
   * Message for resetting the hardware.
   */
  private static final String INIT_HARDWARE_MESSAGE
          = "確定重置硬體?此動作將耗費約60秒重置硬體，此期間請勿執行其他操作";
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
   * Deploys the panel.
   */
  private final PanelController panelController;
  /**
   * Digital I/O.
   */
  private final DioInterface dio;
  /**
   * Watchs the buffer status.
   */
  private final RegisterUtil.Watcher watcher;
  /**
   * Provides the movie duration.
   */
  private final MovieInfo order;
  /**
   * Start flow is used for triggering the digital I/O to play the audio.
   */
  private final StartFlow flow;
  /**
   * All video nodes.
   */
  private final Collection<NodeInformation> videoNodes;
  /**
   * Request functions.
   */
  private final Map<RequestUtil.RequestType, RequestFunction> functions
          = new TreeMap<>();
  /**
   * The snapshot from control node. A fucking game for the dead man.
   */
  private final List<BufferedImage> snapshots = new LinkedList<>();
  /**
   * Default snapshot.
   */
  private final BufferedImage defaultSnapshot;
  /**
   * The end snapshot.
   */
  private final BufferedImage endSnapshot;

  /**
   * Send the trailer snapshots to all fusion node.
   *
   * @param properties The system proeprties
   * @param snapshots The snapsthos to send
   * @param defaultSnapshot default snapshost to send
   * @param finalSnapshot The final snapshot to send
   */
  private static void sendTrailerSnapshot(final NProperties properties,
          final List<BufferedImage> snapshots,
          final BufferedImage defaultSnapshot,
          final BufferedImage finalSnapshot) {
    final int nodeNumber = 4;
    final long sleepSecondTime = properties.getInteger(
            NConstants.SEND_SNAPSHOTS_SECOND_PERIOD);
    List<List<BufferedImage>> imageGroup = new LinkedList<>();
    while (!snapshots.isEmpty()) {
      List<BufferedImage> images = new ArrayList<>(nodeNumber);
      for (int i = 0; i != nodeNumber; ++i) {
        if (snapshots.isEmpty()) {
          images.add(defaultSnapshot);
        } else {
          images.add(snapshots.remove(0));
        }
      }
      imageGroup.add(images);
    }
    LOG.info("total images: " + snapshots.size());
    try {
      for (List<BufferedImage> images : imageGroup) {
        SerialStream.sendAll(
                NodeInformation.getFusionVideoNodes(
                        properties),
                new RequestUtil.SetImageRequest(images),
                false);
        TimeUnit.SECONDS.sleep(sleepSecondTime);
      }
      SerialStream.sendAll(NodeInformation.getFusionVideoNodes(properties),
              new RequestUtil.Request(RequestUtil.RequestType.UNLOCK_IMAGE), true);
    } catch (InterruptedException | IOException e) {
      LOG.error("Failed to send snapshots to fusing node", e);
    }
  }

  /**
   * Synchronizes the copy for snapshots.
   *
   * @return A copy array of snapshots
   */
  private List<BufferedImage> copySnapshots() {
    synchronized (snapshots) {
      return new ArrayList<>(snapshots);
    }
  }

  /**
   * Constructs a master's data.
   *
   * @param file The input optional.
   * @throws IOException If failed to read optional from file
   */
  public MasterFrameData(final File file) throws IOException {
    properties = new NProperties(file);
    selfInformation = NodeInformation.getNodeInformationByAddress(properties);
    requestQueue = RequestUtil.createRemoteQueue(selfInformation, closer);
    panelController = new PanelController(properties, this);
    dio = DioFactory.getDefault(properties);
    watcher = RegisterUtil.createWatcher(closer,
            new BaseTimer(TimeUnit.SECONDS,
                    properties.getInteger(NConstants.SECOND_TIME_TO_REGISTER)),
            properties, panelController);
    order = new MovieInfo(properties);
    flow = new StartFlow(properties, watcher, order, dio);
    videoNodes = NodeInformation.getVideoNodes(properties);
    defaultSnapshot = Painter.getFillColor(
            properties.getInteger(NConstants.GENERATED_IMAGE_WIDTH),
            properties.getInteger(NConstants.GENERATED_IMAGE_HEIGHT),
            Color.BLACK);
    endSnapshot = Painter.getStringImage("The End",
            properties.getInteger(NConstants.GENERATED_IMAGE_WIDTH),
            properties.getInteger(NConstants.GENERATED_IMAGE_HEIGHT),
            properties.getInteger(NConstants.GENERATED_FONT_SIZE));
    Arrays.asList(RequestUtil.RequestType.values()).forEach(type -> {
      switch (type) {
        case START:
          functions.put(type, (data, previousReq, currentReq) -> {
            if (flow.isStart()) {
              JOptionPane.showMessageDialog(null, "正在播放");
            } else {
              synchronized (snapshots) {
                snapshots.clear();
              }
              flow.invokeStartThread(() -> {
                sendTrailerSnapshot(properties, copySnapshots(),
                        defaultSnapshot, endSnapshot);
              });
            }
          });
          break;
        case STOP:
          functions.put(type, (data, previousReq, currentReq) -> {
            flow.stopMasterPlay();
            SerialStream.sendAll(
                    NodeInformation.getVideoNodes(properties),
                    new RequestUtil.Request(
                            RequestUtil.RequestType.STOP), false);
            dio.lightOff();
          });
          break;
        case SELECT:
          functions.put(type, (data, previousReq, currentReq) -> {
            if (currentReq.getClass() == SelectRequest.class) {
              SelectRequest select = (SelectRequest) currentReq;
              if (!watcher.isConflictWithBuffer(
                      select.getIndex())) {
                SerialStream.sendAll(
                        videoNodes,
                        select, true);
                data.setNextFlow(select.getIndex());
              }
            }
          });
          break;
        case REBOOT:
          functions.put(type, (data, previousReq, currentReq) -> {
            if (flow.isStart()) {
              dio.lightOff();
            }
            SerialStream.sendAll(videoNodes,
                    new RequestUtil.Request(type), false);
          });
          break;
        case SHUTDOWN:
          functions.put(type, (data, previousReq, currentReq)
                  -> {
            SerialStream.sendAll(videoNodes,
                    new RequestUtil.Request(type), false);
            ProjectorUtil.enableAllMachine(properties, false);
          });
          break;
        case INIT:
          functions.put(type, (data, previousReq, currentReq)
                  -> {
            int value = JOptionPane.showConfirmDialog(null,
                    INIT_HARDWARE_MESSAGE, null,
                    JOptionPane.YES_NO_OPTION);
            if (value == JOptionPane.OK_OPTION) {
              flow.stopMasterPlay();
              SerialStream.sendAll(
                      NodeInformation.getVideoNodes(properties),
                      new RequestUtil.Request(
                              RequestUtil.RequestType.STOP), false);
              dio.lightWork();
              dio.initializeSubmarine();
              JOptionPane.showMessageDialog(null, "準備完成");
            }
          });
          break;
        case LIGHT_OFF:
          functions.put(type, (data, previousReq, currentReq)
                  -> {
            dio.triggerSunmarineLight();
          });
          break;
        case PARTY_1:
          functions.put(type, (data, previousReq, currentReq)
                  -> {
            if (flow.isStart()) {
              JOptionPane.showMessageDialog(null, "正在播放");
            } else {
              dio.lightParty1();
            }
          });
          break;
        case PARTY_2:
          functions.put(type, (data, previousReq, currentReq)
                  -> {
            if (flow.isStart()) {
              JOptionPane.showMessageDialog(null, "正在播放");
            } else {
              dio.lightParty2();
            }
          });
          break;
        case WOL:
          functions.put(type, (data, previousReq, currentReq)
                  -> {
            for (NodeInformation nodeInformation : videoNodes) {
              WolUtil.wakeup(new InetSocketAddress(
                      selfInformation.getIP(), 0),
                      NodeInformation.getBroadCast(
                              data.getNodeInformation()),
                      nodeInformation.getMac());
            }
            ProjectorUtil.enableAllMachine(properties, true);
          });
          break;
        case SET_IMAGE:
          functions.put(type, (data, previousReq, currentReq)
                  -> {
            if (currentReq.getClass() == SetImageRequest.class) {
              List<BufferedImage> images
                      = ((SetImageRequest) currentReq).getImage();
              synchronized (snapshots) {
                snapshots.clear();
                snapshots.addAll(images);
              }
              LOG.info("Receive the images:" + snapshots.size());
            }
          });
          break;
        default:
          break;
      }
    });
  }

  /**
   * Offers a request command.
   *
   * @param request Request
   */
  public final void offer(final RequestUtil.Request request) {
    requestQueue.offer(request);
  }

  @Override
  public final void setNextFlow(final int index) {
    flow.setNextFlow(index);
  }

  @Override
  public final Map<RequestUtil.RequestType, RequestFunction> getFunctions() {
    return functions;
  }

  @Override
  public final BlockingQueue<RequestUtil.Request> getQueue() {
    return requestQueue;
  }

  @Override
  public final BasePanel getMainPanel() {
    return panelController.getMainPanel();
  }

  @Override
  public final MediaWorker getMediaWorker() {
    throw new UnsupportedOperationException(
            "Not supported yet.");
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
