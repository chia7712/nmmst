package tw.gov.nmmst.controller;

import java.util.Optional;
import net.java.games.input.Component;
import org.junit.Test;
import tw.gov.nmmst.media.BasePanel;
import tw.gov.nmmst.media.Frame;
import tw.gov.nmmst.media.MediaWorker;
import tw.gov.nmmst.media.MovieBuffer;
import tw.gov.nmmst.media.Sample;

public class TestWheelTrigger {
    @Test
    public void triggerOff() {
        WheelTrigger trigger = new WheelTrigger(new MediaWorker() {
            @Override
            public void setNextFlow(int movieIndex) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override
            public MovieBuffer getMovieBuffer() {
                return new MovieBufferTest(0, 0, 93 * 1000 * 1000, 100 * 1000 * 1000);
            }
            @Override
            public void setPause(boolean value) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override
            public void stopAsync() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override
            public BasePanel getPanel() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
        });
        trigger.triggerOff(new ComponentTest(0));
        trigger.triggerOff(new ComponentTest(0.91f));
        assert(trigger.getQueue().size() != 0);
        
        trigger.triggerOff(new ComponentTest(0));
        trigger.triggerOff(new ComponentTest(-0.91f));
        assert(trigger.getQueue().size() != 0);
    }
    private static class ComponentTest implements Component {
        private final float data;
        ComponentTest(float data) {
            this.data = data;
        }
        @Override
        public Component.Identifier getIdentifier() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public boolean isRelative() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public boolean isAnalog() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public float getDeadZone() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public float getPollData() {
            return data;
        }
        @Override
        public String getName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    /**
     * For test.
     */
    private static class MovieBufferTest implements MovieBuffer {
        private final int currentIndex;
        private final int lastIndex;
        private final long currentTimestamp;
        private final long currentDuration;
        public MovieBufferTest(int currentIndex, int lastIndex,
            long currentTimestamp, long currentDuration) {
            this.currentDuration = currentDuration;
            this.currentTimestamp = currentTimestamp;
            this.lastIndex = lastIndex;
            this.currentIndex = currentIndex;
        }
        @Override
        public Optional<Frame> readFrame() throws InterruptedException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public Optional<Sample> readSample() throws InterruptedException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public void writeFrame(final Frame frame) throws InterruptedException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public void writeSample(final Sample sample)
                throws InterruptedException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public void setPause(final boolean value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public boolean isPause() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public boolean hadPause() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public void clear() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public long getHeapSize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public int getFrameNumber() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public int getSampleNumber() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public int getFrameCapacity() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public int getSampleCapacity() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public int getCurrentMovieIndex() {
            return currentIndex;
        }
        @Override
        public long getCurrentTimestamp() {
            return currentTimestamp;
        }
        @Override
        public long getCurrentDuration() {
            return currentDuration;
        }
        @Override
        public int getLastMovieIndex() {
            return lastIndex;
        }
        @Override
        public long getLastTimestamp() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public long getLastDuration() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void writeEof() throws InterruptedException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
