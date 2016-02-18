package tw.gov.nmmst.media;

import java.io.IOException;
import tw.gov.nmmst.processor.FrameProcessor;
import tw.gov.nmmst.threads.Closer;
import tw.gov.nmmst.NProperties;
/**
 * A media controller encapsulates the details of video output, audio output
 * and the media decoder. It may invokes many threads for decodeing
 * media, drawing image and writing audio data.
 * The straightforward methods is used for executing the
 * {@link net.nmmst.utils.RequestUtil.RequestType}.
 */
public interface MediaWorker {
    /**
     * Trigger the action in the flow end.
     */
    @FunctionalInterface
    public interface Trigger {
        /**
         * Trigger something.
         */
        void endFlow();
    }
    /**
     * Instantiates the media work.
     * @param properties NProperties
     * @param closer Closer to set for closing the media worker
     * @param frameProcessor FrameProcessor
     * @param trigger Trigger
     * @return MediaWorker
     * @throws IOException If failed to open media file
     */
    static MediaWorker createMediaWorker(
            final NProperties properties, final Closer closer,
            final FrameProcessor frameProcessor,
            final Trigger trigger)throws IOException {
        return new BaseMediaWorker(properties, closer, frameProcessor, trigger);
    }
    /**
     * Sets the movie index fo specified order.
     * The movie index will replace the old index if the order has been set.
     * @param movieIndex The next index
     */
    void setNextFlow(int movieIndex);
    /**
     * Retrieves the movie buffer.
     * @return Movie buffer
     */
    MovieBuffer getMovieBuffer();
    /**
     * Pauses the {@link #readFrame()} and {@link  #readSample()}.
     * @param value Pause if value is true; Otherwise, no pause
     */
    void setPause(boolean value);
    /**
     * Stops this media work.
     */
    void stopAsync();
    /**
     * @return The panel to draw in the media worker
     */
    BasePanel getPanel();
}
