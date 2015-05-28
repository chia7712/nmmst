package net.nmmst.media;

import java.io.IOException;
import net.nmmst.processor.FrameProcessor;
import net.nmmst.threads.Closer;
import net.nmmst.NProperties;

public interface MediaWorker {
    /**
     * Instantiates the media work.
     * @param properties NProperties
     * @param closer Closer to set for closing the media worker
     * @return MediaWorker
     * @throws IOException If failed to open media file
     */
    public static MediaWorker createMediaWorker(
            final NProperties properties, final Closer closer)
            throws IOException {
        return new BaseMediaWorker(properties, closer, null);
    }
    /**
     * Instantiates the media work.
     * @param properties NProperties
     * @param closer Closer to set for closing the media worker
     * @param frameProcessor FrameProcessor
     * @return MediaWorker
     * @throws IOException If failed to open media file
     */
    public static MediaWorker createMediaWorker(
            final NProperties properties, final Closer closer,
            final FrameProcessor frameProcessor)throws IOException {
        return new BaseMediaWorker(properties, closer, frameProcessor);
    }
    /**
     * Sets the movie index fo specified order.
     * The movie index will replace the old index if the order has been set.
     * @param movieIndex The next index
     */
    public void setNextFlow(int movieIndex);
    /**
     * Retrieves the movie buffer.
     * @return Movie buffer
     */
    public MovieBuffer getMovieBuffer();
    /**
     * Pauses the {@link #readFrame()} and {@link  #readSample()}.
     * @param value Pause if value is true; Otherwise, no pause
     */
    public void setPause(boolean value);
    /**
     * Stops this media work.
     */
    public void stopAsync();
    /**
     * @return The panel to draw in the media worker
     */
    public BasePanel getPanel();
}
