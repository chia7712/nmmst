package tw.gov.nmmst.views;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOG
            = LoggerFactory.getLogger(StartFlow.class);
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
    public final void stop() throws InterruptedException {
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
     * @return {@code true} if all condition are pass.
     * Otherwise it returns {@code false}
     * @throws InterruptedException If this flow is broke
     * @throws IOException If failed to send request to any video nodes
     */
    public final boolean start()
            throws IOException, InterruptedException {
        if (!started.compareAndSet(false, true)) {
            return false;
        }
        if (watcher.isBufferInsufficient()) {
            started.set(false);
            return false;
        }
        if (!SerialStream.sendAll(NodeInformation.getVideoNodes(properties),
                new Request(RequestType.START))) {
            started.set(false);
            return false;
        }
        if (service != null) {
            service.shutdownNow();
        }
        service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            try {
                final int nanoToMicro = 1000;
                flow = info.createPlayFlow();
                while (flow.hasNext()) {
                    MovieAttribute attribute = flow.next();
                    final long startTime = System.nanoTime();
                    dio.light(attribute.getIndex());
                    final long spendTime = System.nanoTime() - startTime;
                    TimeUnit.MICROSECONDS.sleep(
                    attribute.getDuration() - (spendTime / nanoToMicro));
                }
            } catch (InterruptedException e) {
                LOG.debug(e.getMessage());
            } finally {
                started.set(false);
            }
        });
        return true;
    }
}
