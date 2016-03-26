package tw.gov.nmmst.media;

import java.awt.image.BufferedImage;
import tw.gov.nmmst.processor.FrameProcessor;
import tw.gov.nmmst.threads.Closer;
/**
 * A media controller encapsulates the details of video output, audio output
 * and the media decoder. It may invokes many threads for decodeing
 * media, drawing image and writing audio data.
 * The straightforward methods is used for executing the
 * {@link net.nmmst.utils.RequestUtil.RequestType}.
 */
public interface MediaWorker {
    /**
     * @return A instnace of builder
     */
    static Builder newBuilder() {
        return new Builder();
    }
    /**
     * A util to build the media worker.
     */
    class Builder {
        /**
         * Movie info.
         */
        private MovieInfo movieInfo;
        /**
         * Movie buffer.
         */
        private MovieBuffer buffer;
        /**
         * Close the errand.
         */
        private Closer closer;
        /**
         * Process the frame.
         */
        private FrameProcessor processor;
        /**
         * Show the frame.
         */
        private BasePanel panel;
        /**
         * Initial image.
         */
        private BufferedImage initImage;
        /**
         * @param v The movie info to set
         * @return Current builder
         */
        public Builder setMovieInfo(final MovieInfo v) {
            if (isValid(v)) {
                movieInfo = v;
            }
            return this;
        }
        /**
         * @param v The movie buffer to set
         * @return Current builder
         */
        public Builder setMovieBuffer(final MovieBuffer v) {
            if (isValid(v)) {
                buffer = v;
            }
            return this;
        }
        /**
         * @param v The closer to set
         * @return Current builder
         */
        public Builder setCloser(final Closer v) {
            if (isValid(v)) {
                closer = v;
            }
            return this;
        }
        /**
         * @param v The processor to set
         * @return Current builder
         */
        public Builder setFrameProcessor(final FrameProcessor v) {
            if (isValid(v)) {
                processor = v;
            }
            return this;
        }
        /**
         * @param v The panel to set
         * @return Current builder
         */
        public Builder setBasePanel(final BasePanel v) {
            if (isValid(v)) {
                panel = v;
            }
            return this;
        }
        /**
         * @param v The initial image to set
         * @return Current builder
         */
        public Builder setBufferedImage(final BufferedImage v) {
            if (isValid(v)) {
                initImage = v;
            }
            return this;
        }
        /**
         * @return A instance of media worker
         */
        @SuppressWarnings(value = {"AvoidInlineConditionals"})
        public MediaWorker build() {
            checkNull(movieInfo, "movieInfo");
            checkNull(buffer, "buffer");
            checkNull(closer, "close");
            checkNull(panel, "panel");
            checkNull(initImage, "initImage");
            return new BaseMediaWorker(
                movieInfo,
                buffer,
                closer,
                processor == null ? FrameProcessor.empty() : processor,
                panel,
                initImage);
        }
        /**
         * Checks whether the object is null.
         * @param obj The object to check
         * @return True if the object is not null
         */
        private static boolean isValid(final Object obj) {
            return obj != null;
        }
        /**
         * Throws the runtime exception if the object is null.
         * @param obj The object to check
         * @param name The msg to throw
         */
        private static void checkNull(final Object obj, final String name) {
            if (!isValid(obj)) {
                throw new RuntimeException("The " + name + " is null");
            }
        }
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
