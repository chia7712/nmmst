package net.nmmst.processor;

import java.util.LinkedList;
import java.util.List;
import net.nmmst.movie.Frame;
import net.nmmst.player.NodeInformation;
import net.nmmst.processor.LinearProcessor.Format;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class ProcessorFactory {
    private static final long firstMovieChangeTime = (2 * 60 + 30) * 1000 * 1000;
    private static final TimeLocation GRAY_TIME_LOCATION = new TimeLocation(0, 0, firstMovieChangeTime);
    private static final TimeLocation SCRIM_TIME_LOCATION = TimeLocation.reverse(GRAY_TIME_LOCATION);
    private static final LinearProcessor.Format GRAY_FORMTA = new LinearProcessor.Format(
        0.051,
        0.1253,
        0.6,
        0.9,
        0.6,
        0.9
    );
    private static final LinearProcessor.Format	SCRIM_FORMTA = new LinearProcessor.Format(
        0.0985,
        0.168,
        0.6,
        0.9,
        0.6,
        0.9
    );
    private ProcessorFactory(){}
    public static FrameProcessor getSequenceProcessor(NodeInformation.Location location) {
        SequenceProcessor processor = new SequenceProcessor();
        processor.add(new LinearProcessor(location, GRAY_FORMTA, GRAY_TIME_LOCATION));
        processor.add(new LinearProcessor(location, SCRIM_FORMTA, SCRIM_TIME_LOCATION));
        return processor;
    }
    public static FrameProcessor getSingleProcessor(NodeInformation.Location location, Format format) {
        return new LinearProcessor(location, format);
    }
    public static FrameProcessor getSingleProcessor(NodeInformation.Location location) {
        return new LinearProcessor(location, GRAY_FORMTA);
    }
    private static class SequenceProcessor implements FrameProcessor {
        private final List<FrameProcessor> processors = new LinkedList();
        public void add(FrameProcessor processor) {
            processors.add(processor);
        }
        @Override
        public boolean needProcess(Frame frame) {
            return true;
        }

        @Override
        public void process(Frame frame) {
            for (FrameProcessor processor : processors) {
                if (processor.needProcess(frame)) {
                    processor.process(frame);
                }
            }
        }
    }
}
