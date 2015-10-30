package tw.gov.nmmst.media;

/**
 * Maintains the byte array of audio.
 */
public final class Sample {
    /**
     * Empty sample.
     */
    public static final Sample EMPTY_SAMPLE
        = null;
    /**
     * A byte array of audio.
     */
    private final byte[]  data;
    /**
     * The attribute of this audio.
     */
    private final MovieAttribute attribute;
    /**
     * Constructs a sample by specified attribute and audio data.
     * @param movieAttribute The movie attribute
     * @param audioData A byte array of audio
     */
    public Sample(final MovieAttribute movieAttribute,
            final byte[] audioData) {
        attribute = movieAttribute;
        data = audioData;
        if (data == null || data.length == 0) {
            throw new RuntimeException("No found of sample data");
        }
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
}
