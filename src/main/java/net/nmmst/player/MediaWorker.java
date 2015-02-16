package net.nmmst.player;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import net.nmmst.movie.BufferFactory;
import net.nmmst.movie.Frame;
import net.nmmst.movie.MovieAttribute;
import net.nmmst.movie.MovieBuffer;
import net.nmmst.movie.MovieOrder;
import net.nmmst.movie.MovieStream;
import net.nmmst.movie.Sample;
import net.nmmst.processor.FrameProcessor;
import net.nmmst.tools.AtomicCloser;
import net.nmmst.tools.BackedRunner;
import net.nmmst.tools.BasePanel;
import net.nmmst.tools.BaseTimer;
import net.nmmst.tools.Closer;
import net.nmmst.tools.NMConstants;
import net.nmmst.tools.Painter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class MediaWorker extends BackedRunner {
    public interface EofTrigger {
        public void process(BasePanel panel);
    }
    private static final Logger LOG = LoggerFactory.getLogger(MediaWorker.class);
    private final BufferedImage initImage = Painter.getFillColor(
            NMConstants.IMAGE_WIDTH, 
            NMConstants.IMAGE_HEIGHT, 
            Color.BLACK);
    private final MovieBuffer buffer = BufferFactory.getMovieBuffer();
    private final FrameProcessor panelProcessor;
    private final FrameProcessor readerProcessor;
    private final BasePanel panel = new BasePanel(initImage, BasePanel.Mode.FILL);
    private final MovieOrder movieOrder = MovieOrder.get();
    private final Object lock = new Object();
    private final Speaker speaker;
    private final EofTrigger eofTriggger;
    private ExecutorService service;
    private Closer curCloser;
    public MediaWorker(Closer closer, FrameProcessor readerProcessor, FrameProcessor panelProcessor, EofTrigger eofTriggger) throws LineUnavailableException, IOException {
        super(closer);
        this.speaker = new Speaker(getAudioFormat(movieOrder));
        this.panelProcessor = panelProcessor;
        this.readerProcessor = readerProcessor;
        this.eofTriggger = eofTriggger;
        buffer.setPause(true);
        Executors.newSingleThreadExecutor().execute(new Checker(closer));
    }
    public void setMovieEnable(int index, boolean value) {
        movieOrder.setEnable(index, value);
    }
    public void setPause(boolean enable) {
        buffer.setPause(enable);
    }
    public BasePanel getPanel() {
        return panel;
    }
    private static AudioFormat getAudioFormat(MovieOrder movieOrder) {
        return movieOrder.getMovieAttribute()[0].getAutioFormat();
    }
    @Override
    protected void work() {
        try {
            service.shutdown();
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            if (eofTriggger == null) {
                panel.write(initImage);
            } else {
                eofTriggger.process(panel);
            }
            buffer.setPause(true);
            internalInit();
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }
    }
    public void stop() {
        synchronized(lock) {
            if (!curCloser.isClosed()) {
                curCloser.close();
                service.shutdownNow();
                try {
                    service.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage());
                }
                buffer.clear();
            }
        }
    }
    private void internalInit() {
        synchronized(lock) {
            try {
                movieOrder.reset();
            } catch (IOException e) {
                LOG.error(e.getMessage());
                throw new RuntimeException(e);
            }
            service = Executors.newCachedThreadPool();
            curCloser = new AtomicCloser();
            service.execute(new MovieReader(curCloser, movieOrder, readerProcessor));
            service.execute(new PanelThread(curCloser, panel, panelProcessor));
            service.execute(new SpeakerThread(curCloser, speaker));
        }
    }
    @Override
    protected void init() {
        internalInit();
    }

    @Override
    protected void clear() {
        stop();
    }
    private static class Checker extends BackedRunner {
        private final MovieBuffer buffer = BufferFactory.getMovieBuffer();
        public Checker(Closer closer) {
            super(closer, new BaseTimer(TimeUnit.SECONDS, 5));
        }
        @Override
        protected void work() {
            if (LOG.isDebugEnabled()) {
                LOG.debug(buffer.getFrameSize() + ", " + buffer.getSampleSize());
            }
        }

        @Override
        protected void init() {
        }
        @Override
        protected void clear() {
        }
    }
    private static class MovieReader implements Runnable {
        private final MovieOrder movieOrder;
        private final MovieBuffer buffer = BufferFactory.getMovieBuffer();
        private final FrameProcessor processor;
        private final Closer closer;
        private MovieStream stream;
        public MovieReader(Closer closer, MovieOrder movieOrder, FrameProcessor processor) {
            this.closer = closer;
            this.movieOrder = movieOrder;
            this.processor = processor;
        }
        @Override
        public void run() {
            try {
                stream = movieOrder.getNextMovieStream();
                while (stream != null && !closer.isClosed() && !Thread.interrupted()) {
                    MovieStream.Type type = stream.readNextType();
                    switch(type) {
                        case VIDEO: {
                            Frame frame = stream.getFrame();
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
                            Sample sample = stream.getSample();
                            if (sample == null) {
                                break;
                            }
                            buffer.writeSample(sample);
                            break;
                        }
                        case EOF:
                            stream = movieOrder.getNextMovieStream();
                            break;
                        default:
                            break;
                    }
                }
                buffer.writeFrame(Frame.newNullFrame());
                buffer.writeSample(Sample.newNullSample());
            } catch(IOException e) {
                LOG.error(e.getMessage());
            } catch(InterruptedException e) {
                LOG.error("MovieReader is interrupted");
            } finally {

            }
        }
    }
    private static class PanelThread implements Runnable {
        private final Sleeper sleeper = new Sleeper(0);
        private final MovieBuffer buffer = BufferFactory.getMovieBuffer();
        private final AtomicReference<Frame> frameRef = BufferFactory.getFrameRef();
        private final BasePanel panel;
        private final Closer closer;
        private final FrameProcessor processor;
        public PanelThread(Closer closer, BasePanel panel, FrameProcessor processor) {
            this.closer = closer;
            this.processor = processor;
            this.panel = panel;
        }
        @Override
        public void run() {
            try {
                MovieAttribute attribute = null;
                while (!closer.isClosed() && !Thread.interrupted()) {
                    Frame frame = buffer.readFrame();
                    if (frame.getImage() == null) {
                        break;
                    }
                    if (attribute == null || frame.getMovieAttribute().getIndex() != attribute.getIndex()) {
                        attribute = frame.getMovieAttribute();
                        sleeper.reset();
                    }
                    if (buffer.hadPause()) {
                        sleeper.reset();
                    }
                    sleeper.sleepByTimeStamp(frame.getTimestamp());
                    frameRef.set(frame);
                    if (processor != null && processor.needProcess(frame)) {
                        processor.process(frame);
                    }
                    panel.write(frame.getImage());
                }
            } catch(InterruptedException e) {
                LOG.debug(e.getMessage() + "Panel thread is interrupted");
            }
        }
    }
    private static class SpeakerThread implements Runnable {
        private final MovieBuffer buffer = BufferFactory.getMovieBuffer();
        private final Closer closer;
        private final Speaker speaker;
        public SpeakerThread(Closer closer, Speaker speaker) {
            this.closer = closer;
            this.speaker = speaker;
        }
        @Override
        public void run() {
            try {
                while (!Thread.interrupted() && !closer.isClosed()) {
                    Sample sampler = buffer.readSample();
                    if (sampler.getData() == null) {
                        break;
                    }
                    speaker.write(sampler.getData());
                }
            } catch(InterruptedException e) {
                LOG.debug(e.getMessage() + "Speak thread is interrupted");
            } finally {
                speaker.flush();
            }
        }
    }
    private static class Sleeper {
        private final long microTolerance;
        private long streamStartTime = 0;
        private long clockStartTime = 0;
        public Sleeper(long microTolerance) {
            this.microTolerance = microTolerance;
        }
        public long sleepByTimeStamp(long streamCurrentTime) throws InterruptedException {
            if (streamStartTime == 0) {
                clockStartTime = System.nanoTime();
                streamStartTime = streamCurrentTime;
                return 0;
            }
            final long clockTimeInterval = (System.nanoTime() - clockStartTime) / 1000;
            final long streamTimeInterval = (streamCurrentTime - streamStartTime);
            final long microsecondsToSleep = (streamTimeInterval - (clockTimeInterval + microTolerance));
            if (microsecondsToSleep > 0) {
                TimeUnit.MICROSECONDS.sleep(microsecondsToSleep);
            }
            return microsecondsToSleep;
        }
        public void reset() {
            clockStartTime = 0;
            streamStartTime = 0;
        }
    }
}
