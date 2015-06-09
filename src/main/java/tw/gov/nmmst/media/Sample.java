package tw.gov.nmmst.media;

/**
 * Maintains the byte array of audio.
 */
public final class Sample {
    /**
     * A byte array of audio.
     */
    private final byte[]  data;
    /**
     * The attribute of this audio.
     */
    private final MovieAttribute attribute;
    /**
     * Constructs a sample with end flag.
     */
    public Sample() {
        data = null;
        attribute = null;
    }
    /**
     * Constructs a sample by specified attribute and audio data.
     * @param movieAttribute The movie attribute
     * @param audioData A byte array of audio
     */
    public Sample(final MovieAttribute movieAttribute,
            final byte[] audioData) {
        attribute = movieAttribute;
        data = audioData;
    }
    /**
     * @see MovieStream
     * @return Movie Attribute
     */
    public MovieAttribute getMovieAttribute() {
        return attribute;
    }
    /**
     * @return Audio data
     */
    public byte[] getData() {
        return data;
    }
    /**
     * @return Size of audio data
     */
    public long getHeapSize() {
        return data.length;
    }
    /**
     * Indicates whether end.
     * @return {@code true} if end of movie
     */
    public boolean isEnd() {
        return data == null;
    }
}
