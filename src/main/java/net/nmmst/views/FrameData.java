package net.nmmst.views;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import net.nmmst.NProperties;
import net.nmmst.NodeInformation;
import net.nmmst.media.BasePanel;
import net.nmmst.media.MediaWorker;
import net.nmmst.threads.Closer;
import net.nmmst.utils.RequestUtil;
/**
 * The data of frame.
 */
public interface FrameData {
    /**
     * Returns the request functions. The request function will be executed
     * with specified request type.
     * @return The request functions.
     */
    Map<RequestUtil.RequestType, RequestFunction> getFunctions();
    /**
     * Retrieves a request queue which provides user to add request.
     * @return A request queue
     */
    BlockingQueue<RequestUtil.Request> getQueue();
    /**
     * Retrieves a main panel added to JFrame.
     * @return A main panel
     */
    BasePanel getMainPanel();
    /**
     * Retrieves a media work which provides the control methods.
     * @return A media work
     */
    MediaWorker getMediaWorker();
    /**
     * Retrieves a closer.
     * @return A closer
     */
    Closer getCloser();
    /**
     * The node information for this node.
     * @return Node information
     */
    NodeInformation getNodeInformation();
    /**
     * Program configuration.
     * @return NProperties
     */
    NProperties getNProperties();
    /**
     * Sets a movie index to next order.
     * @param index The movie index
     */
    void setNextFlow(final int index);
}
