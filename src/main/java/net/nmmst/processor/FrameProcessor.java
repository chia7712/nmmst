package net.nmmst.processor;

import java.util.List;
import net.nmmst.movie.Frame;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public interface FrameProcessor {
    public boolean needProcess(Frame frame);
    public void process(Frame frame);
}
