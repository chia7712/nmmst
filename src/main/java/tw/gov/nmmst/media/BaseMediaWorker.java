package tw.gov.nmmst.media;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sound.sampled.LineUnavailableException;
import tw.gov.nmmst.processor.FrameProcessor;
import tw.gov.nmmst.threads.AtomicCloser;
import tw.gov.nmmst.threads.Closer;
import tw.gov.nmmst.threads.Taskable;
import tw.gov.nmmst.NConstants;
import tw.gov.nmmst.NProperties;
import tw.gov.nmmst.utils.Painter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Controls the audio and video output.
 * It uses the {@link net.nmmst.utils.BasePanel} as the video output
 * and {@link net.nmmst.player.Speaker} as the audio output.
 */
class BaseMediaWorker implements MediaWorker {
    /**
     * Log.
     */
    private static final Logger LOG
            = LoggerFactory.getLogger(BaseMediaWorker.class);
    /**
     * Includes audio output, video output and decoder.
     */
    private static final int THREAD_NUMBER = 3;
    /**
     * Initial image is drawn in the beginning of this media.
     */
    private final BufferedImage initImage;
    /**
     * Frame processor.
     */
    private final Optional<FrameProcessor> processor;
    /**
     * Image output.
     */
    private final BasePanel panel;
    /**
     * Movie info provides the order and information.
     */
    private final MovieInfo movieInfo;
    /**
     * Movie buffer.
     */
    private final MovieBuffer buffer;
    /**
     * Working flag.
     */
    private final AtomicBoolean working = new AtomicBoolean(false);
    /**
     * Thread pool.
     */
    private ExecutorService service;
    /**
     * A reference to reader is used for modifying the play order.
     * @see #setNextFlow(int)
     */
    private MovieReader reader;
    /**
     * Closer.
     */
    private AtomicCloser curCloser;
    /**
     * Constructs a media worker for specified properties,
     * closer and frame processor.
     * @param properties NProperties
     * @param closer Closer
     * @param frameProcessor FrameProcessor
     * @throws IOException If failed to create movie info
     */
    public BaseMediaWorker(final NProperties properties, final Closer closer,
            final FrameProcessor frameProcessor
            ) throws IOException {
        movieInfo = new MovieInfo(properties);
        buffer = BufferFactory.createMovieBuffer(properties);
        processor = Optional.ofNullable(frameProcessor);
        buffer.setPause(true);
        initImage = Painter.getFillColor(
            properties.getInteger(NConstants.GENERATED_IMAGE_WIDTH),
            properties.getInteger(NConstants.GENERATED_IMAGE_HEIGHT),
            Color.BLACK);
        panel = new BasePanel(initImage, BasePanel.Mode.FILL);
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
    public void setNextFlow(final int movieIndex) {
        if (reader != null) {
            reader.setNextFlow(movieIndex);
        }
    }
    @Override
    public MovieBuffer getMovieBuffer() {
        return buffer;
    }
    @Override
    public void setPause(final boolean enable) {
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
    /**
     * Initializes all threads only if this media isn't working.
     */
    private void initializeMediaThreads() {
        if (working.compareAndSet(false, true)) {
            service = Executors.newFixedThreadPool(THREAD_NUMBER);
            curCloser = new AtomicCloser();
            reader = new MovieReader(
                    curCloser, buffer, movieInfo, processor);
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
    /**
     * A thread for decoding the media.
     */
    private static class MovieReader implements Runnable {
        /**
         * A movie buffer to write.
         */
        private final MovieBuffer buffer;
        /**
         * This processor modify the frame after decode the frame.
         */
        private final Optional<FrameProcessor> processor;
        /**
         * A closer is used to close this thread.
         */
        private final AtomicCloser closer;
        /**
         * A play flow.
         */
        private final MovieInfo.PlayFlow playFlow;
        /**
         * Constructs a reader for decoding a list of media.
         * @param atomicCloser Close
         * @param movieBuffer Movie buffer
         * @param movieInfo Movie info provides the play order
         * @param frameProcessor Frame processor
         */
        MovieReader(final AtomicCloser atomicCloser,
                final MovieBuffer movieBuffer,
                final MovieInfo movieInfo,
                final Optional<FrameProcessor> frameProcessor) {
            closer = atomicCloser;
            buffer = movieBuffer;
            processor = frameProcessor;
            playFlow = movieInfo.createPlayFlow();
        }
        /**
         * Sets the next movie index.
         * @param movieIndex Movie index
         */
        void setNextFlow(final int movieIndex) {
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
                            switch (type) {
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
            } catch (InterruptedException | IOException e) {
                LOG.error(e.getMessage() + "MovieReader is interrupted");
            }
        }
    }
    /**
     * A thread for writing frame data.
     */
    private static class PanelThread implements Runnable {
        /**
         * Controls the sleep period for play video regularly.
         */
        private final Sleeper sleeper = new Sleeper(0);
        /**
         * Movie buffer.
         */
        private final MovieBuffer buffer;
        /**
         * Image output.
         */
        private final BasePanel panel;
        /**
         * A closer is used for closing this thread.
         */
        private final AtomicCloser closer;
        /**
         * A frame/image processor.
         */
        private final Optional<FrameProcessor> processor;
        /**
         * Constructs a thread for drawing the frame.
         * @param atomicCloser Closer
         * @param movieBuffer Movie buffer
         * @param basePanel Image output
         * @param frameProcessor Frame/Image processor
         */
        PanelThread(final AtomicCloser atomicCloser,
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
            } catch (InterruptedException e) {
                LOG.debug(e.getMessage() + "Panel thread is interrupted");
            }
        }
    }
    /**
     * A thread for writing audio data.
     */
    private static class SpeakerThread implements Runnable {
        /**
         * Movie buffer.
         */
        private final MovieBuffer buffer;
        /**
         * Closer.
         */
        private final AtomicCloser closer;
        /**
         * Constructs a thread for writing the audio data.
         * @param atomicCloser Closer
         * @param movieBuffer Movie buffer
         */
        SpeakerThread(final AtomicCloser atomicCloser,
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
            } catch (InterruptedException e) {
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
}
