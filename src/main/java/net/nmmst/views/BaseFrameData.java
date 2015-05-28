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

public abstract class BaseFrameData implements FrameData {
    private final NProperties properties = new NProperties();
    private final Closer closer = new AtomicCloser();
    private final NodeInformation selfInformation
            = NodeInformation.getNodeInformationByAddress(properties);
    private final BlockingQueue<Request> requestQueue
            = RequestUtil.createRemoteQueue(selfInformation, closer);
    private final Map<RequestUtil.RequestType, RequestFunction> functions
            = new TreeMap();
    public BaseFrameData() throws IOException {
        
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
                            if (request instanceof SelectRequest) {
                                SelectRequest select = (SelectRequest)request;
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
