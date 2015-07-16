package tw.gov.nmmst.views;

import tw.gov.nmmst.utils.RequestUtil.Request;
/**
 * This function interface is used for handling
 * with {@link net.nmmst.utils.RequestUtil.Request}.
 */
public interface RequestFunction {
    /**
     * Handles with a request.
     * @param frameData The data is owned by a frame
     * @param previousReq The previous request
     * @param currentReq The current request
     * @throws Exception If any error happens
     */
    void work(FrameData frameData, Request previousReq,
            Request currentReq) throws Exception;
}
