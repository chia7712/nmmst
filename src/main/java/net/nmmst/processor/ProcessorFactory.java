package net.nmmst.processor;

import net.nmmst.movie.Frame;
import net.nmmst.player.PlayerInformation;
import net.nmmst.processor.LinearProcessor.Format;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class ProcessorFactory {
    private static final long firstMovieChangeTime = (2 * 60 + 30) * 1000 * 1000;
    private static final LinearProcessor.Format grayDefaultFormat = new LinearProcessor.Format(
        0.051,
        0.1253,
        0.6,
        0.9,
        0.6,
        0.9
    );
    private static final LinearProcessor.Format	scrimDefaultFormat = new LinearProcessor.Format(
        0.0985,
        0.168,
        0.6,
        0.9,
        0.6,
        0.9
    );
    private ProcessorFactory(){}
    public static FrameProcessor newTwoTierProcessor(PlayerInformation.Location location, Format firstFormat, Format secondFormat) {
        return new TwoTierProcessor(location, firstFormat, secondFormat);
    }
    public static FrameProcessor newTwoTierProcessor(PlayerInformation.Location location) {
        return new TwoTierProcessor(location, grayDefaultFormat, scrimDefaultFormat); 
    }
    public static FrameProcessor newSingleProcessor(PlayerInformation.Location location, Format format) {
        return new SingleProcessor(location, format);
    }
    public static FrameProcessor newSingleProcessor(PlayerInformation.Location location) {
        return new SingleProcessor(location, grayDefaultFormat);
    }
    private static class SingleProcessor extends LinearProcessor {
        public SingleProcessor(PlayerInformation.Location location, Format format) {
            super(location, format);
        }
        @Override
        public boolean needProcess(Frame frame) {
            return true;
        }           
    }
    private static class TwoTierProcessor implements FrameProcessor {
        private final FrameProcessor firstProcessor;
        private final FrameProcessor secondProcessor;
        public TwoTierProcessor(PlayerInformation.Location location, Format firstFormat, Format secondFormat) {
            firstProcessor = new LinearProcessor(location, firstFormat);
            secondProcessor = new LinearProcessor(location, secondFormat);
        }
        @Override
        public boolean needProcess(Frame frame) {
            return true;
        }

        @Override
        public void process(Frame frame) {
            if (frame.getMovieAttribute().getIndex() == 0 && frame.getTimestamp() <= firstMovieChangeTime) {
                firstProcessor.process(frame);
            } else {
                secondProcessor.process(frame);
            }
        }
    }
}
