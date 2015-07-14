package tw.gov.nmmst.views;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import tw.gov.nmmst.threads.AtomicCloser;
import tw.gov.nmmst.threads.Closer;
import tw.gov.nmmst.NodeInformation;
import tw.gov.nmmst.NProperties;
import tw.gov.nmmst.utils.RequestUtil;
import tw.gov.nmmst.utils.RequestUtil.Request;
import tw.gov.nmmst.utils.RequestUtil.RequestType;
import tw.gov.nmmst.utils.RequestUtil.SelectRequest;
import tw.gov.nmmst.utils.WindowsUtil;
/**
 * Base frame data is made up of {@link NProperties},
 * {@link Closer}, {@link NodeInformation},
 * {@link Request} queue and {@link RequestFunction}.
 */
public abstract class VideoData implements FrameData {
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
            = new TreeMap();
    /**
     * Constructs a data of base frame.
     * @throws IOException If failed to open movies
     */
    public VideoData() throws IOException {
        this(null);
    }
    /**
     * Constructs a data of base frame.
     * @param file The initial properties
     * @throws IOException If failed to open movies
     */
    public VideoData(final File file) throws IOException {
        if (file == null) {
            properties = new NProperties();
        } else {
            properties = new NProperties(file);
        }
        selfInformation
            = NodeInformation.getNodeInformationByAddress(properties);
        requestQueue
            = RequestUtil.createRemoteQueue(selfInformation, closer);
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
