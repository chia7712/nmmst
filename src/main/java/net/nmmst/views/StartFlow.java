package net.nmmst.views;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.nmmst.media.MovieInfo;
import net.nmmst.NodeInformation;
import net.nmmst.utils.RegisterUtil;
import net.nmmst.NConstants;
import net.nmmst.NProperties;
import net.nmmst.controller.DioInterface;
import net.nmmst.media.MovieAttribute;
import net.nmmst.media.MovieInfo.PlayFlow;
import net.nmmst.utils.RequestUtil.Request;
import net.nmmst.utils.RequestUtil.RequestType;
import net.nmmst.utils.SerialStream;
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
     * Dio interface.
     */
    private final DioInterface dio;
    private final NProperties properties;
    private final RegisterUtil.Watcher watcher;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final MovieInfo order;
    private PlayFlow flow;
    private ExecutorService service;
    /**
     * Constructs the start flow by specified movie order and dio.
     * @param nproperties NProperties
     * @param registerWatcher Watches the buffer status for all video nodes
     * @param movieOrder MovieInfo
     * @param dioInterface DioInterface
     */
    public StartFlow(
            final NProperties nproperties,
            final RegisterUtil.Watcher registerWatcher,
            final MovieInfo movieOrder,
            final DioInterface dioInterface) {
        properties = nproperties;
        watcher = registerWatcher;
        order = movieOrder;
        dio = dioInterface;
    }
    public void stop() throws InterruptedException {
        if (service != null) {
            service.shutdownNow();
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            service = null;
        }
    }
    public void setNextFlow(int index) {
        if (flow != null) {
            flow.setNextFlow(index);
        }
    }
    public boolean isStart() {
        return started.get();
    }
    /**
     * Starts this flow.
     * @return True if all condition are pass. Otherwise it returns false
     * @throws InterruptedException If this flow is broke
     * @throws IOException If failed to send request to any video nodes
     */
    public boolean start()
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
                flow = order.createPlayFlow();
                while (flow.hasNext()) {
                    MovieAttribute attribute = flow.next();
                    final long startTime = System.nanoTime();
                    dio.light(attribute.getIndex());
                    final long spendTime = System.nanoTime() - startTime;
                    TimeUnit.MICROSECONDS.sleep(
                    attribute.getDuration() - (spendTime / 1000));
                }
            } catch (InterruptedException e) {
            } finally {
                started.set(false);
            }
        });
        return true;
    }
}
