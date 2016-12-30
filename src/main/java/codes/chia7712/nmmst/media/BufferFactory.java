package codes.chia7712.nmmst.media;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import codes.chia7712.nmmst.NConstants;
import codes.chia7712.nmmst.NProperties;

/**
 * Factory to create the sample and frame buffer.
 */
public final class BufferFactory {

  /**
   * Instaniates the movie buffer. The max size is getted from
   * {@link NProperties} by {@link NConstants#FRAME_QUEUE_SIZE}
   *
   * @param properties NProperties provides the size of queue
   * @return Movie buffer
   */
  public static MovieBuffer createMovieBuffer(final NProperties properties) {
    return new BaseBuffer(properties);
  }

  /**
   * The base buffer saves the frames and samples in the
   * {@link ArrayBlockingQueue} and {@link LinkedBlockingQueue} respectively.
   */
  private static class BaseBuffer implements MovieBuffer {

    /**
     * Buffers the samples.
     */
    private final BlockingQueue<Optional<Sample>> samples
            = new LinkedBlockingQueue();
    /**
     * Indicates the pause status.
     */
    private final AtomicBoolean pause = new AtomicBoolean(false);
    /**
     * Indicates the pause status whether happened.
     */
    private final AtomicBoolean hadPause = new AtomicBoolean(false);
    /**
     * Buffers the frames.
     */
    private final BlockingQueue<Optional<Frame>> frameQueue;
    /**
     * The max limit of frame buffer.
     */
    private final int frameBufferLimit;
    /**
     * The total size of used heap.
     */
    private final AtomicLong heapSize = new AtomicLong();
    /**
     * A frame to read recently.
     */
    private final AtomicReference<Frame> currentFrame
            = new AtomicReference();
    /**
     * A frame to write recently.
     */
    private final AtomicReference<Frame> lastFrame
            = new AtomicReference();

    /**
     * Instantiates a buffer for specified properties.
     *
     * @param properties NProperties provides the limit of buffer.
     */
    BaseBuffer(final NProperties properties) {
      frameBufferLimit = properties.getInteger(
              NConstants.FRAME_QUEUE_SIZE);
      frameQueue = new ArrayBlockingQueue(frameBufferLimit);
    }

    /**
     * Waits until no pause.
     *
     * @return {@code true} if a pause event has happened
     * @throws InterruptedException If any breaks
     */
    private boolean waitForPause() throws InterruptedException {
      boolean bePaused = false;
      synchronized (pause) {
        while (pause.get()) {
          pause.wait();
          bePaused = true;
        }
      }
      return bePaused;
    }

    @Override
    public Optional<Frame> readFrame() throws InterruptedException {
      if (waitForPause()) {
        hadPause.set(true);
      }
      Optional<Frame> frame = frameQueue.take();
      frame.ifPresent(f -> {
        heapSize.addAndGet(-f.getHeapSize());
        currentFrame.set(f);
      });
      return frame;
    }

    @Override
    public Optional<Sample> readSample() throws InterruptedException {
      waitForPause();
      Optional<Sample> sample = samples.take();
      sample.ifPresent(s -> heapSize.addAndGet(-s.getHeapSize()));
      return sample;
    }

    @Override
    public void writeFrame(final Frame frame) throws InterruptedException {
      Optional<Frame> frameOpt = Optional.ofNullable(frame);
      frameOpt.ifPresent(f -> {
        heapSize.addAndGet(f.getHeapSize());
        lastFrame.set(f);
      });
      frameQueue.put(frameOpt);
    }

    @Override
    public void writeSample(final Sample sample)
            throws InterruptedException {
      Optional<Sample> sampleOpt = Optional.ofNullable(sample);
      sampleOpt.ifPresent(s -> heapSize.addAndGet(s.getHeapSize()));
      samples.put(sampleOpt);
    }

    @Override
    public void setPause(final boolean value) {
      pause.set(value);
      if (!value) {
        synchronized (pause) {
          pause.notifyAll();
        }
      }
    }

    @Override
    public void clear() {
      frameQueue.clear();
      samples.clear();
      heapSize.set(0);
    }

    @Override
    public int getFrameNumber() {
      return frameQueue.size();
    }

    @Override
    public int getSampleNumber() {
      return samples.size();
    }

    @Override
    public boolean isPause() {
      return pause.get();
    }

    @Override
    public boolean hadPause() {
      return hadPause.getAndSet(false);
    }

    @Override
    public int getFrameCapacity() {
      return frameBufferLimit;
    }

    @Override
    public int getSampleCapacity() {
      return Integer.MAX_VALUE;
    }

    @Override
    public long getHeapSize() {
      return heapSize.get();
    }

    @Override
    public int getLastMovieIndex() {
      Frame frame = lastFrame.get();
      if (frame != null) {
        return frame.getMovieAttribute().getIndex();
      }
      return 0;
    }

    @Override
    public long getLastTimestamp() {
      Frame frame = lastFrame.get();
      if (frame != null) {
        return frame.getTimestamp();
      }
      return 0;
    }

    @Override
    public long getLastDuration() {
      Frame frame = lastFrame.get();
      if (frame != null) {
        return frame.getMovieAttribute().getDuration();
      }
      return 0;
    }

    @Override
    public int getCurrentMovieIndex() {
      Frame frame = currentFrame.get();
      if (frame != null) {
        return frame.getMovieAttribute().getIndex();
      }
      return 0;
    }

    @Override
    public long getCurrentTimestamp() {
      Frame frame = currentFrame.get();
      if (frame != null) {
        return frame.getTimestamp();
      }
      return 0;
    }

    @Override
    public long getCurrentDuration() {
      Frame frame = currentFrame.get();
      if (frame != null) {
        return frame.getMovieAttribute().getDuration();
      }
      return 0;
    }

    @Override
    public void writeEof() throws InterruptedException {
      writeFrame(null);
      writeSample(null);
    }
  }

  /**
   * Can't be instantiated with this ctor.
   */
  private BufferFactory() {
  }
}
