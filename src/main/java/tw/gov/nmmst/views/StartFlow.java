package tw.gov.nmmst.views;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JOptionPane;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tw.gov.nmmst.NConstants;
import tw.gov.nmmst.media.MovieInfo;
import tw.gov.nmmst.NodeInformation;
import tw.gov.nmmst.utils.RegisterUtil;
import tw.gov.nmmst.NProperties;
import tw.gov.nmmst.controller.DioInterface;
import tw.gov.nmmst.media.MovieAttribute;
import tw.gov.nmmst.media.MovieInfo.PlayFlow;
import tw.gov.nmmst.utils.RequestUtil.Request;
import tw.gov.nmmst.utils.RequestUtil.RequestType;
import tw.gov.nmmst.utils.SerialStream;
/**
 * Start the flow to send the following command.
 * 1) Checks the buffer for all video nodes. If any node hava insufficient
 * buffer, this flow will be broke.
 * 2) Sends start request to all video nodes. If any connection error happen,
 * this flow will be broke.
 * 3) Sends command to invoke all light one by one.
 */
public class StartFlow {
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
     * @param nproperties NProperties
     * @param registerWatcher Watches the buffer status for all video nodes
     * @param movieInfo MovieInfo
     * @param dioInterface DioInterface
     */
    public StartFlow(
            final NProperties nproperties,
            final RegisterUtil.Watcher registerWatcher,
            final MovieInfo movieInfo,
            final DioInterface dioInterface) {
        properties = nproperties;
        watcher = registerWatcher;
        info = movieInfo;
        dio = dioInterface;
    }
    /**
     * Synchronously stop all action.
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
     * @param index The movie index
     */
    public final void setNextFlow(final int index) {
        if (flow != null) {
            flow.setNextFlow(index);
        }
    }
    /**
     * Returns {@code true} if non-start.
     * @return {@code true} if non-start
     */
    public final boolean isStart() {
        return started.get();
    }
    /**
     * Starts this flow.
     * @throws InterruptedException If this flow is broke
     * @throws IOException If failed to send request to any video nodes
     */
    public final void invokeStartThread()
            throws IOException, InterruptedException {
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
                SerialStream.sendAll(NodeInformation.getVideoNodes(properties),
                        new Request(RequestType.START), true);
                final int nanoToMicro = 1000;
                flow = info.createPlayFlow();
                while (flow.hasNext()) {
                    MovieAttribute attribute = flow.next();
                    final long startTime = System.nanoTime();
                    dio.light(attribute.getIndex());
                    LOG.info("dio index = " + attribute.getIndex());
                    TimeUnit.MICROSECONDS.sleep(
                    attribute.getDuration()
                        - (System.nanoTime() - startTime) / nanoToMicro);
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
