package tw.gov.nmmst.media;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sound.sampled.LineUnavailableException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tw.gov.nmmst.processor.FrameProcessor;
import tw.gov.nmmst.threads.AtomicCloser;
import tw.gov.nmmst.threads.Closer;
import tw.gov.nmmst.threads.Taskable;

/**
 * Controls the audio and video output. It uses the
 * {@link net.nmmst.utils.BasePanel} as the video output and
 * {@link net.nmmst.player.Speaker} as the audio output. It used a block queue
 * to save the decored frame and audio. It accept a trigger to invoke some
 * action on the different phase.
 */
class BaseMediaWorker implements MediaWorker {

  /**
   * Log.
   */
  private static final Log LOG
          = LogFactory.getLog(BaseMediaWorker.class);
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
  private final FrameProcessor processor;
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
   *
   * @see #setNextFlow(int)
   */
  private MovieReader reader;
  /**
   * Closer.
   */
  private AtomicCloser curCloser;

  /**
   * Constructs a base media worker.
   *
   * @param movieInfo The movie to play
   * @param buffer Saves the frame and audio
   * @param closer Control the errand
   * @param processor Process the decoded frame
   * @param panel The panel to draw the frame
   * @param initImage The initial image drawed on the panel
   */
  BaseMediaWorker(final MovieInfo movieInfo, final MovieBuffer buffer,
          final Closer closer, final FrameProcessor processor,
          final BasePanel panel, final BufferedImage initImage) {
    this.movieInfo = movieInfo;
    this.buffer = buffer;
    this.processor = processor;
    this.buffer.setPause(true);
    this.initImage = initImage;
    this.panel = panel;
    this.panel.write(initImage);
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
          processor.playOver(initImage)
                  .ifPresent(image -> panel.writeAndLock(image));
          buffer.setPause(true);
          buffer.clear();
          working.set(false);
          initializeMediaThreads();
        } catch (InterruptedException e) {
          LOG.error(e);
        }
      }

      @Override
      public void clear() {
        stopAsync();
      }
    });
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
      service.execute(new PanelThread(curCloser, buffer, panel,
              processor));
      service.execute(new SpeakerThread(curCloser, buffer));
      processor.init();
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
    private final FrameProcessor processor;
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
     *
     * @param closer Close
     * @param buffer Movie buffer
     * @param movieInfo Movie info provides the play order
     * @param processor Frame processor
     */
    MovieReader(final AtomicCloser closer,
            final MovieBuffer buffer,
            final MovieInfo movieInfo,
            final FrameProcessor processor) {
      this.closer = closer;
      this.buffer = buffer;
      this.processor = processor;
      this.playFlow = movieInfo.createPlayFlow();
    }

    /**
     * Sets the next movie index.
     *
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
                  Optional<Frame> frame = stream.getFrame()
                          .flatMap(f -> processor.postDecodeFrame(f));
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
        buffer.writeEof();
      } catch (InterruptedException | IOException e) {
        LOG.error("MovieReader is interrupted", e);
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
    private final FrameProcessor processor;

    /**
     * Constructs a thread for drawing the frame.
     *
     * @param closer Closer
     * @param buffer Movie buffer
     * @param panel Image output
     * @param processor Frame/Image processor
     */
    PanelThread(final AtomicCloser closer,
            final MovieBuffer buffer,
            final BasePanel panel,
            final FrameProcessor processor) {
      this.closer = closer;
      this.processor = processor;
      this.buffer = buffer;
      this.panel = panel;
    }

    @Override
    public void run() {
      try {
        MovieAttribute attribute = null;
        int previousIndex = -1;
        while (!closer.isClosed() && !Thread.interrupted()) {
          Optional<Frame> frameOpt = buffer.readFrame();
          if (!frameOpt.isPresent()) {
            break;
          }
          Frame frame = frameOpt.get();
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
          processor.prePrintPanel(frame.getImage())
                  .ifPresent(image -> panel.write(image));
          int currentIndex = frame.getMovieAttribute().getIndex();
          if (currentIndex != previousIndex) {
            LOG.info("play index : " + currentIndex);
            previousIndex = currentIndex;
          }
        }
      } catch (InterruptedException e) {
        LOG.debug("Panel thread is interrupted", e);
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
     *
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
          Optional<Sample> sampleOpt = buffer.readSample();
          if (!sampleOpt.isPresent()) {
            break;
          }
          Sample sample = sampleOpt.get();
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
        LOG.debug("Speak thread is interrupted", e);
      } catch (LineUnavailableException e) {
        LOG.debug(e);
      } finally {
        if (spk != null) {
          spk.close();
        }
      }
    }
  }
}
