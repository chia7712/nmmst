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
//    public static FrameProcessor newSingleProcessor(PlayerInformation.Location location, Format format)
//    {
//        return new SingleProcessor(location, format);
//    }
//    public static FrameProcessor newSingleProcessor(PlayerInformation.Location location)
//    {
//        return newSingleProcessor(location, grayDefaultFormat);    
//    }
    public static FrameProcessor newTwoTierProcessor(PlayerInformation.Location location, Format firstFormat, Format secondFormat) {
        return new TwoTierProcessor(location, firstFormat, secondFormat);
    }
    public static FrameProcessor newTwoTierProcessor(PlayerInformation.Location location) {
        return newTwoTierProcessor(location, grayDefaultFormat, scrimDefaultFormat); 
    }
//    public static FrameProcessor newBasedProcessor(PlayerInformation.Location location)
//    {
//        return new BasedProcessor(location, grayDefaultFormat);
//    }
//    public static FrameProcessor newBasedProcessor(PlayerInformation.Location location, Format format)
//    {
//        return new BasedProcessor(location, format);
//    }
//    public static FrameProcessor newGrayProcessor(PlayerInformation.Location location, Format format)
//    {
//        return new GrayProcessor(location, format);
//    }
//    public static FrameProcessor newGrayProcessor(PlayerInformation.Location location)
//    {
//        return new GrayProcessor(location, grayDefaultFormat);
//    }
//    public static FrameProcessor newScrimProcessor(PlayerInformation.Location location)
//    {
//        return new ScrimProcessor(location, scrimDefaultFormat);
//    }
//    public static FrameProcessor newScrimProcessor(PlayerInformation.Location location, Format format)
//    {
//        return new ScrimProcessor(location, format);
//    }
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
            if(frame.getMovieAttribute().getIndex() == 0 && frame.getTimestamp() <= firstMovieChangeTime) {
                firstProcessor.process(frame);
            } else {
                secondProcessor.process(frame);
            }
        }
        
    }
//    private static class GrayProcessor extends LinearProcessor
//    {
//
//        public GrayProcessor(PlayerInformation.Location location, Format format) 
//        {
//            super(location, format);
//        }
//        @Override
//        public boolean needProcess(Frame frame)
//        {
//            if(frame.getMovieAttribute().getIndex() == 0 && frame.getTimestamp() <= firstMovieChangeTime)
//                return true;
//            return false;
//        }
//
//    }
//    private static class ScrimProcessor extends LinearProcessor 
//    {
//
//        public ScrimProcessor(PlayerInformation.Location location, Format format) 
//        {
//            super(location, format);
//        }
//        @Override
//        public boolean needProcess(Frame frame)
//        {
//            MovieAttribute attribute = frame.getMovieAttribute();
//            if(attribute.getIndex() > 0)
//                    return true;
//            if(attribute.getIndex() == 0 && frame.getTimestamp() > firstMovieChangeTime)
//                    return true;
//            return false;
//        }
//    }
}
