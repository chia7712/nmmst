package net.nmmst.views;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import net.nmmst.threads.AtomicCloser;
import net.nmmst.threads.Closer;
import net.nmmst.NodeInformation;
import net.nmmst.NProperties;
import net.nmmst.utils.RequestUtil;
import net.nmmst.utils.RequestUtil.Request;
import net.nmmst.utils.RequestUtil.RequestType;
import net.nmmst.utils.RequestUtil.SelectRequest;
import net.nmmst.utils.WindowsUtil;
/**
 * Base frame data is made up of {@link NProperties},
 * {@link Closer}, {@link NodeInformation},
 * {@link Request} queue and {@link RequestFunction}.
 */
public abstract class VideoData implements FrameData {
    /**
     * NProperties.
     */
    private final NProperties properties = new NProperties();
    /**
     * Closer.
     */
    private final Closer closer = new AtomicCloser();
    /**
     * Master information.
     */
    private final NodeInformation selfInformation
        = NodeInformation.getNodeInformationByAddress(properties);
    /**
     * Request queue.
     */
    private final BlockingQueue<RequestUtil.Request> requestQueue
        = RequestUtil.createRemoteQueue(selfInformation, closer);
    /**
     * Request functions.
     */
    private final Map<RequestUtil.RequestType, RequestFunction> functions
            = new TreeMap();
    /**
     * Constructs a data of base frame.
     * @throws IOException If failed to open movie
     */
    public VideoData() throws IOException {
        Arrays.asList(RequestType.values()).stream().forEach(type -> {
            switch (type) {
                case START:
                    functions.put(type, (data, request)
                        -> data.getMediaWorker().setPause(false));
                    break;
                case STOP:
                    functions.put(type, (data, request)
                        -> data.getMediaWorker().stopAsync());
                    break;
                case PAUSE:
                    functions.put(type, (data, request)
                        -> data.getMediaWorker().setPause(true));
                    break;
                case SELECT:
                    functions.put(type, (data, request)
                        -> {
                            if (request.getClass() == SelectRequest.class) {
                                SelectRequest select = (SelectRequest) request;
                                data.getMediaWorker().setNextFlow(
                                    select.getIndex());
                            }
                        });
                    break;
                case REBOOT:
                    functions.put(type, (data, request)
                        -> WindowsUtil.reboot());
                    break;
                case SHUTDOWN:
                    functions.put(type, (data, request)
                        -> WindowsUtil.shutdown());
                    break;
                default:
                    break;
            }
        });
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
