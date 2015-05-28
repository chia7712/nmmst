package net.nmmst.views;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import net.nmmst.NProperties;
import net.nmmst.NodeInformation;
import net.nmmst.media.BasePanel;
import net.nmmst.media.MediaWorker;
import net.nmmst.threads.Closer;
import net.nmmst.utils.RequestUtil;

public interface FrameData {
    public Map<RequestUtil.RequestType, RequestFunction> getFunctions();
    public BlockingQueue<RequestUtil.Request> getQueue();
    public BasePanel getMainPanel();
    public MediaWorker getMediaWorker();
    public Closer getCloser();
    public NodeInformation getNodeInformation();
    public NProperties getNProperties();
}
