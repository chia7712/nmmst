package tw.gov.nmmst.media;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import tw.gov.nmmst.NConstants;
import tw.gov.nmmst.NProperties;
/**
 * Factory to create the sample and frame buffer.
 */
public final class BufferFactory {
    /**
     * Instaniates the movie buffer.
     * The max size is getted from {@link NProperties} by
     * {@link NConstants#FRAME_QUEUE_SIZE}
     * @param properties NProperties provides the size of queue
     * @return Movie buffer
     */
    public static MovieBuffer createMovieBuffer(final NProperties properties) {
        return new BaseBuffer(properties);
    }
    /**
     * The base buffer saves the frames and samples in the
     * {@link ArrayBlockingQueue} and {@link LinkedBlockingQueue}
     * respectively.
     */
    private static class BaseBuffer implements MovieBuffer {
        /**
         * Buffers the samples.
         */
        private final BlockingQueue<Sample> samples
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
        private final BlockingQueue<Frame> frameQueue;
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
         * @param properties NProperties provides the limit of
         * buffer.
         */
        public BaseBuffer(final NProperties properties) {
            frameBufferLimit = properties.getInteger(
                NConstants.FRAME_QUEUE_SIZE);
            frameQueue = new ArrayBlockingQueue(frameBufferLimit);
        }
        /**
         * Waits until no pause.
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
        public Frame readFrame() throws InterruptedException {
            if (waitForPause()) {
                hadPause.set(true);
            }
            Frame frame = frameQueue.take();
            heapSize.addAndGet(-frame.getHeapSize());
            if (!frame.isEnd()) {
                currentFrame.set(frame);
            }
            return frame;
        }
        @Override
        public Sample readSample() throws InterruptedException {
            waitForPause();
            Sample sample = samples.take();
            heapSize.addAndGet(-sample.getHeapSize());
            return sample;
        }
        @Override
        public void writeFrame(final Frame frame) throws InterruptedException {
            heapSize.addAndGet(frame.getHeapSize());
            frameQueue.put(frame);
            if (!frame.isEnd()) {
                lastFrame.set(frame);
            }
        }
        @Override
        public void writeSample(final Sample sample)
                throws InterruptedException {
            heapSize.addAndGet(sample.getHeapSize());
            samples.put(sample);
        }
        @Override
        public void setPause(final boolean value) {
            pause.set(value);
            if (!pause.get()) {
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
    }
    /**
     * Can't be instantiated with this ctor.
     */
    private BufferFactory() {
    }
}
