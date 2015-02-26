package net.nmmst.movie;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import net.nmmst.controller.OvalInformation;
import net.nmmst.request.Request;
import net.nmmst.tools.NMConstants;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class BufferFactory {
    private final static SingleBuffer buffer = new SingleBuffer(NMConstants.FRAME_QUEUE_LIMIT);
    private final static List<OvalInformation> snapshots = Collections.synchronizedList(new LinkedList());
    private final static BlockingQueue<Request> requestBuffer = new LinkedBlockingQueue();
    private final static AtomicReference<Frame> frameRef	= new AtomicReference();
    private BufferFactory(){}
    public static MovieBuffer getMovieBuffer() {
        return buffer;
    }
    public static List<OvalInformation> getSnapshots() {
        return snapshots;
    }
    public static BlockingQueue<Request> getRequestBuffer() {
        return requestBuffer;
    }
    public static AtomicReference<Frame> getFrameRef() {
        return frameRef; 
    }
    private static class SingleBuffer implements MovieBuffer {
        
        private final BlockingQueue<Sample> samples = new LinkedBlockingQueue();
        private final AtomicBoolean pause = new AtomicBoolean(false);
        private final AtomicBoolean hadPause = new AtomicBoolean(false);
        private final BlockingQueue<Frame> frameQueue;
        private final int queueMaxLength;
        private final AtomicLong heapSize = new AtomicLong();
        public SingleBuffer(int queueMaxLength) {
            this.queueMaxLength = queueMaxLength;
            frameQueue = new ArrayBlockingQueue(queueMaxLength);
        }
        private boolean waitForPause() throws InterruptedException {
            boolean bePaused = false;
            synchronized(pause) {
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
        public void writeFrame(Frame frame) throws InterruptedException {
            heapSize.addAndGet(frame.getHeapSize());
            frameQueue.put(frame);
        }
        @Override
        public void writeSample(Sample sample) throws InterruptedException {
            heapSize.addAndGet(sample.getHeapSize());
            samples.put(sample);	
        }
        @Override
        public void setPause(boolean value) {
            pause.set(value);
            if (!pause.get()) {
                synchronized(pause) {
                    pause.notifyAll();
                }
            }
        }
        @Override
        public void clear() {
            frameQueue.clear();
            samples.clear();
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
        public int getFrameQueueMaxLength() {
            return queueMaxLength;
        }
        @Override
        public int getSampleQueueMaxLength() {
            return Integer.MAX_VALUE;
        }
        @Override
        public long getHeapSize() {
            return heapSize.get();
        }
    }
}
