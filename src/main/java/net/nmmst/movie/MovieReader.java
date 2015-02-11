package net.nmmst.movie;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.nmmst.processor.FrameProcessor;
import net.nmmst.tools.BackedRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class MovieReader implements BackedRunner {
    private static final Logger LOG = LoggerFactory.getLogger(MovieReader.class);
    private final MovieOrder movieOrder;
    private final MovieBuffer buffer = BufferFactory.getMovieBuffer();
    private final AtomicBoolean close = new AtomicBoolean(false);
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final AtomicReference<MovieStream> movieStreamRef = new AtomicReference();
    private final FrameProcessor processor;
    public MovieReader(MovieOrder movieOrder) {
        this(movieOrder, null);
    }
    public MovieReader(MovieOrder movieOrder, FrameProcessor processor) {
        this.movieOrder = movieOrder;
        this.processor = processor;
    }
    @Override
    public void close() {
        close.set(true);
    }
    @Override
    public boolean isClosed() {
        return isClosed.get();
    }
    @Override
    public void run() {
        try {
            movieStreamRef.set(movieOrder.getNextMovieStream());
            while (movieStreamRef.get() != null && !close.get() && !Thread.interrupted()) {
                MovieStream.Type type = movieStreamRef.get().readNextType();
                switch(type) {
                    case VIDEO: {
                        Frame frame = movieStreamRef.get().getFrame();
                        if (frame == null) {
                            break;
                        }
                        if (processor != null && processor.needProcess(frame)) {
                            processor.process(frame);
                        }
                        buffer.writeFrame(frame);
                        break;
                    }
                    case AUDIO: {
                        Sample sample = movieStreamRef.get().getSample();
                        if (sample == null) {
                            break;
                        }
                        buffer.writeSample(sample);
                        break;
                    }
                    case EOF:
                        movieStreamRef.set(movieOrder.getNextMovieStream());
                        break;
                    default:
                        break;
                }
            }
            if (movieStreamRef.get() == null) {
                buffer.writeFrame(Frame.newNullFrame());
                buffer.writeSample(Sample.newNullSample());
            }
        } catch(IOException e) {
            LOG.error(e.getMessage());
        } catch(InterruptedException e) {
            LOG.error("MovieReader is interrupted");
        } finally {
            isClosed.set(true);
        }
    }
}
