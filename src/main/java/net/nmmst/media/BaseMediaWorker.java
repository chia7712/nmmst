package net.nmmst.media;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sound.sampled.LineUnavailableException;
import net.nmmst.processor.FrameProcessor;
import net.nmmst.threads.AtomicCloser;
import net.nmmst.threads.BaseTimer;
import net.nmmst.threads.Closer;
import net.nmmst.threads.Taskable;
import net.nmmst.NConstants;
import net.nmmst.NProperties;
import net.nmmst.utils.Painter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Controls the audio and video output.
 * It uses the {@link net.nmmst.utils.BasePanel} as the video output
 * and {@link net.nmmst.player.Speaker} as the audio output.
 */
class BaseMediaWorker implements MediaWorker {
    private static final Logger LOG
            = LoggerFactory.getLogger(BaseMediaWorker.class);
    private static final int THREAD_NUMBER = 3;
    private final BufferedImage initImage;
    private final Optional<FrameProcessor> processor;
    private final BasePanel panel;
    private final MovieInfo movieOrder;
    private final MovieBuffer buffer;
    private final AtomicBoolean working = new AtomicBoolean(false);
    private ExecutorService service;
    private MovieReader reader;
    private AtomicCloser curCloser;
    public BaseMediaWorker(final NProperties properties, final Closer closer,
            final FrameProcessor frameProcessor
            ) throws IOException {
        movieOrder = new MovieInfo(properties);
        buffer = BufferFactory.createMovieBuffer(properties);
        processor = Optional.ofNullable(frameProcessor);
        buffer.setPause(true);
        initImage = Painter.getFillColor(
            properties.getInteger(NConstants.GENERATED_IMAGE_WIDTH),
            properties.getInteger(NConstants.GENERATED_IMAGE_HEIGHT),   
            Color.BLACK);
        panel = new BasePanel(initImage, BasePanel.Mode.FILL);
        closer.invokeNewThread(new Checker(buffer),
                new BaseTimer(TimeUnit.SECONDS, 5));
        closer.invokeNewThread(new Taskable() {
            @Override
            public void init() {
                initializeMediaThreads();
            }
            @Override
            public void work() {
                try {
                    service.awaitTermination(Long.MAX_VALUE,
                            TimeUnit.DAYS);
                    processor.flatMap(p -> p.playOver(initImage))
                            .ifPresent(image -> panel.write(image));
                    buffer.setPause(true);
                    working.set(false);
                    initializeMediaThreads();
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage());
                }
            }
            @Override
            public void clear() {
                stopAsync();
            }
        }, null);
    }
    @Override
    public void setNextFlow(int movieIndex) {
        if (reader != null) {
            reader.setNextFlow(movieIndex);
        }
    }
    @Override
    public MovieBuffer getMovieBuffer() {
        return buffer;
    }
    @Override
    public void setPause(boolean enable) {
        buffer.setPause(enable);
    }
    @Override
    public final void stopAsync() {
        if (working.compareAndSet(true, false)) {
            curCloser.close();
            service.shutdownNow();
            buffer.clear();
        }
    }
    private void initializeMediaThreads() {
        if (working.compareAndSet(false, true)) {
            service = Executors.newFixedThreadPool(THREAD_NUMBER);
            curCloser = new AtomicCloser();
            reader = new MovieReader(
                    curCloser, buffer, movieOrder, processor);
            service.execute(reader);
            service.execute(new PanelThread(
                    curCloser, buffer, panel, processor));
            service.execute(new SpeakerThread(curCloser, buffer));
            processor.ifPresent(p -> p.init());
            service.shutdown();
        }
    }

    @Override
    public BasePanel getPanel() {
        return panel;
    }
    private static class Checker implements Taskable {
        private final BufferMetrics metrics;
        public Checker(final BufferMetrics bufferMetrics) {
            metrics = bufferMetrics;
        }
        @Override
        public void work() {
            if (LOG.isDebugEnabled()) {
                LOG.debug(metrics.getFrameNumber()
                        + " frames, " + metrics.getSampleNumber()
                        + " samples, " 
                        + (metrics.getHeapSize() / (1024 * 1024)) + " MB");
            }
        }
    }
    private static class MovieReader implements Runnable {
        private final MovieBuffer buffer;
        private final Optional<FrameProcessor> processor;
        private final AtomicCloser closer;
        private final MovieInfo.PlayFlow playFlow;
        public MovieReader(final AtomicCloser atomicCloser,
                final MovieBuffer movieBuffer,
                final MovieInfo movieOrder,
                final Optional<FrameProcessor> frameProcessor) {
            closer = atomicCloser;
            buffer = movieBuffer;
            processor = frameProcessor;
            playFlow = movieOrder.createPlayFlow();
        }
        
        public void setNextFlow(int movieIndex) {
            playFlow.setNextFlow(movieIndex);
        }
        @Override
        public void run() {
            try {
                while (playFlow.hasNext()) {
                    MovieAttribute attribute = playFlow.next();
                    try (MovieStream stream = new MovieStream(
                            attribute.getFile(), attribute.getIndex())) {
                        boolean eof = false;
                        while (!eof) {
                            if (closer.isClosed() || Thread.interrupted()) {
                                return;
                            }
                            MovieStream.Type type = stream.readNextType();
                            switch(type) {
                                case VIDEO: 
                                    Optional<Frame> frame
                                        = stream.getFrame()
                                            .flatMap(f -> processor.flatMap(
                                              p -> p.postDecodeFrame(f)));
                                    if (frame.isPresent()) {
                                        buffer.writeFrame(frame.get());
                                    }
                                    break;
                                case AUDIO: 
                                    Optional<Sample> sample
                                        = stream.getSample();
                                    if (sample.isPresent()) {
                                        buffer.writeSample(sample.get());
                                    }
                                    break;
                                default:
                                    eof = true;
                                    break;
                            }
                        }
                    }
                }
                buffer.writeFrame(new Frame());
                buffer.writeSample(new Sample());
            } catch(InterruptedException | IOException e) {
                LOG.error(e.getMessage() + "MovieReader is interrupted");
            }
        }
    }
    private static class PanelThread implements Runnable {
        private final Sleeper sleeper = new Sleeper(0);
        private final MovieBuffer buffer;
        private final BasePanel panel;
        private final AtomicCloser closer;
        private final Optional<FrameProcessor> processor;
        public PanelThread(final AtomicCloser atomicCloser,
                final MovieBuffer movieBuffer,
                final BasePanel basePanel,
                final Optional<FrameProcessor> frameProcessor) {
            closer = atomicCloser;
            processor = frameProcessor;
            buffer = movieBuffer;
            panel = basePanel;
        }
        @Override
        public void run() {
            try {
                MovieAttribute attribute = null;
                while (!closer.isClosed() && !Thread.interrupted()) {
                    Frame frame = buffer.readFrame();
                    if (frame.isEnd()) {
                        break;
                    }
                    if (attribute == null
                            || frame.getMovieAttribute().getIndex()
                               != attribute.getIndex()) {
                        attribute = frame.getMovieAttribute();
                        sleeper.reset();
                    }
                    if (buffer.hadPause()) {
                        sleeper.reset();
                    }
                    sleeper.sleepByTimeStamp(frame.getTimestamp());
                    processor.flatMap(p -> p.prePrintPanel(frame.getImage()))
                             .ifPresent(image -> panel.write(image));
                }
            } catch(InterruptedException e) {
                LOG.debug(e.getMessage() + "Panel thread is interrupted");
            }
        }
    }
    private static class SpeakerThread implements Runnable {
        private final MovieBuffer buffer;
        private final AtomicCloser closer;
        public SpeakerThread(final AtomicCloser atomicCloser,
                final MovieBuffer movieBuffer) {
            closer = atomicCloser;
            buffer = movieBuffer;
        }
        @Override
        public void run() {
            Speaker spk = null;
            try {
                while (!Thread.interrupted() && !closer.isClosed()) {
                    Sample sample = buffer.readSample();
                    if (sample.isEnd()) {
                        break;
                    }
                    if (spk == null) {
                        spk = new Speaker(sample.getMovieAttribute()
                                .getAudioFormat());
                    }

                    if (!spk.getAudioFormat().matches(
                            sample.getMovieAttribute().getAudioFormat())) {
                        spk.close();
                        spk = new Speaker(sample.getMovieAttribute()
                                .getAudioFormat());
                    }
                    spk.write(sample.getData());
                }
            } catch(InterruptedException e) {
                LOG.debug(e.getMessage() + "Speak thread is interrupted");
            } catch (LineUnavailableException e) {
                LOG.debug(e.getMessage());
            } finally {
                if (spk != null) {
                    spk.close();
                }
            }
        }
    }
    /**
     * Controls the execution period.
     * @see BackedRunner
     */
    private static class Sleeper {
        private final long tolerance;
        private long streamStartTime = 0;
        private long clockStartTime = 0;
        public Sleeper(final long microTolerance) {
            tolerance = microTolerance;
        }
        public long sleepByTimeStamp(final long streamCurrentTime)
                throws InterruptedException {
            if (streamStartTime == 0) {
                clockStartTime = System.nanoTime();
                streamStartTime = streamCurrentTime;
                return 0;
            }
            final long clockTimeInterval
                    = (System.nanoTime() - clockStartTime) / 1000;
            final long streamTimeInterval
                    = (streamCurrentTime - streamStartTime);
            final long microsecondsToSleep
                    = (streamTimeInterval - (clockTimeInterval + tolerance));
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
