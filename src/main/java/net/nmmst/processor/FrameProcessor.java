package net.nmmst.processor;

import net.nmmst.movie.Frame;



public interface FrameProcessor 
{
    public boolean needProcess(Frame frame);
    public void process(Frame frame);
}
