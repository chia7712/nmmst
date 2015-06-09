package tw.gov.nmmst.processor;

import tw.gov.nmmst.NodeInformation;
import tw.gov.nmmst.processor.LinearProcessor.Factor;
/**
 * instantiates the {@link FrameProcessor} which only overrides
 * the {@link FrameProcessor#postDecodeFrame(net.nmmst.movie.Frame)}.
 */
public final class ProcessorFactory {
    /**
     * Default factor for gray screen.
     */
    private static final LinearProcessor.Factor GRAY_FORMTA
            = new LinearProcessor.Factor(
                0.053,
                0.118,
                0.6,
                0.9,
                0.6,
                0.9
    );
    /**
     * Instantiates the FrameProcessor with specified node location and
     * factor.
     * @param location The node locaction
     * @param factor The fusion factor
     * @return A frame processor
     */
    public static FrameProcessor createFrameProcessor(
            final NodeInformation.Location location,
            final Factor factor) {
        return new LinearProcessor(location, factor);
    }
    /**
     * Instantiates the FrameProcessor with specified node location
     * and default fusion factor.
     * @param location The node locaction
     * @return A frame processor
     */
    public static FrameProcessor createFrameProcessor(
            final NodeInformation.Location location) {
        switch (location) {
            case LU:
                return new LinearProcessor(location,
                        new LinearProcessor.Factor(
                            0.052,
                            0.118,
                            0.6,
                            0.9,
                            0.6,
                            0.9
                ));
            case RU:
                return new LinearProcessor(location,
                        new LinearProcessor.Factor(
                            0.052,
                            0.1159,
                            0.6,
                            0.9,
                            0.6,
                            0.9
                ));
            case LD:
                return new LinearProcessor(location,
                        new LinearProcessor.Factor(
                            0.054,
                            0.118,
                            0.6,
                            0.9,
                            0.6,
                            0.9
                ));
            case RD:
                return new LinearProcessor(location,
                        new LinearProcessor.Factor(
                            0.054,
                            0.118,
                            0.6,
                            0.9,
                            0.6,
                            0.9
                ));
            default:
               return new LinearProcessor(location, GRAY_FORMTA);
        }
    }
    /**
     * Can't be instantiated with this ctor.
     */
    private ProcessorFactory() {
    }
}
