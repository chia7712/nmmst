package net.nmmst.views;

import net.nmmst.utils.RequestUtil.Request;

public interface RequestFunction {
    public void work(FrameData frameData,
            Request request) throws Exception;
}
