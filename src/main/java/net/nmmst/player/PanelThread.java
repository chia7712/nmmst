package net.nmmst.player;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.nmmst.movie.BufferFactory;
import net.nmmst.movie.Frame;
import net.nmmst.movie.MovieAttribute;
import net.nmmst.movie.MovieBuffer;
import net.nmmst.processor.FrameProcessor;
import net.nmmst.tools.BasicPanel;
import net.nmmst.tools.Closure;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class PanelThread implements Closure {
    private final BasicPanel panel;
    private final Sleeper sleeper = new Sleeper(0);
    private final MovieBuffer buffer = BufferFactory.getMovieBuffer();
    private final AtomicBoolean close = new AtomicBoolean(false);
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final AtomicReference<Frame> frameRef = BufferFactory.getFrameRef();
    private final FrameProcessor processor;
    public PanelThread(BasicPanel panel) {
        this(panel, null);
    }
    public PanelThread(BasicPanel panel, FrameProcessor processor) {
        this.panel = panel;
        this.processor = processor;
    }
    @Override
    public void run() {
        try {
            MovieAttribute attribute = null;
            while (!close.get() && !Thread.interrupted()) {
                Frame frame = buffer.readFrame();
                if (frame.getImage() == null) {
                    break;
                }
                if(attribute == null || frame.getMovieAttribute().getIndex() != attribute.getIndex()) {
                    attribute = frame.getMovieAttribute();
                    sleeper.reset();
                }
                if (buffer.hadPause()) {
                    sleeper.reset();
                }
                sleeper.sleepByTimeStamp(frame.getTimestamp());
                frameRef.set(frame);
                if(processor != null && processor.needProcess(frame)) {
                    processor.process(frame);
                }
                panel.write(frame.getImage());
            }
        } catch(InterruptedException e) {
            //TODO
        } finally {
            isClosed.set(true);
        }
    }
    @Override
    public void close() {
        close.set(true);
    }
    @Override
    public boolean isClosed() {
        return isClosed.get();
    }
}
