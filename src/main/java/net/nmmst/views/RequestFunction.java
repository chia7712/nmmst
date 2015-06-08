package net.nmmst.views;

import net.nmmst.utils.RequestUtil.Request;
/**
 * This function interface is used for handling
 * with {@link net.nmmst.utils.RequestUtil.Request}.
 */
public interface RequestFunction {
    /**
     * Handles with a request.
     * @param frameData The data is owned by a frame
     * @param request The request
     * @throws Exception If any error happens
     */
    void work(FrameData frameData, Request request) throws Exception;
}
