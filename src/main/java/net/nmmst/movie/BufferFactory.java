package net.nmmst.movie;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.nmmst.request.Request;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class BufferFactory {
    private final static SingleBuffer buffer = new SingleBuffer();
    private final static BlockingQueue<Request> requestBuffer = new LinkedBlockingQueue();
    private final static AtomicReference<Frame> frameRef	= new AtomicReference();
    private BufferFactory(){}
    public static MovieBuffer getMovieBuffer() {
        return buffer;
    }
    public static BlockingQueue<Request> getRequestBuffer() {
        return requestBuffer;
    }
    public static AtomicReference<Frame> getFrameRef() {
        return frameRef; 
    }
    private static class SingleBuffer implements MovieBuffer {
        private	static final int framesSize = 100;
        private final BlockingQueue<Frame> frames = new ArrayBlockingQueue(framesSize);
        private final BlockingQueue<Sample> samples = new LinkedBlockingQueue();
        private final AtomicBoolean pause = new AtomicBoolean(false);
        private final AtomicBoolean hadPause = new AtomicBoolean(false);
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
            return frames.take(); 	
        }
        @Override
        public Sample readSample() throws InterruptedException {
            waitForPause();
            return samples.take();
        }
        @Override
        public void writeFrame(Frame frame) throws InterruptedException {
            frames.put(frame);
        }
        @Override
        public void writeSample(Sample sample) throws InterruptedException {
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
            frames.clear();
            samples.clear();
        }
        @Override
        public int getFrameSize() {
            return frames.size();
        }
        @Override
        public int getSampleSize() {
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
        public int getMaxFrameSize() {
            return framesSize;
        }
        @Override
        public int getMaxSampleSize() {
            return Integer.MAX_VALUE;
        }
    }
}
