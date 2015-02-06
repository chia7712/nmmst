package net.nmmst.register;

import java.io.Serializable;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class PlayerState implements Serializable {
    private static final long serialVersionUID = 7522152994360919226L;
    private final int frameBuffered;
    private final int sampleBuffered;
    private final int frameBufferSize;
    private final int sampleBufferSize;
    public PlayerState(int frameBuffered, int sampleBuffered, int frameBufferSize, int sampleBufferSize) {
        this.frameBuffered = frameBuffered;
        this.sampleBuffered = sampleBuffered;
        this.frameBufferSize = frameBufferSize;
        this.sampleBufferSize = sampleBufferSize;
    }
    public int getFrameBuffered() {
        return frameBuffered;
    }
    public int getSampleBuffered() {
        return sampleBuffered;
    }
    public int getFrameBufferSize() {
        return frameBufferSize;
    }
    public int getSampleBufferSize() {
        return sampleBufferSize;
    }
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        return str.append("Max Frames = ")
           .append(frameBufferSize)
           .append(", ")                
           .append("Max Samples = ")
           .append(sampleBufferSize)      
           .append(", ")       
           .append("Current Frames = ")
           .append(frameBuffered)      
           .append(", ")    
           .append("Current Samples = ")
           .append(sampleBuffered).toString();
    }
}
