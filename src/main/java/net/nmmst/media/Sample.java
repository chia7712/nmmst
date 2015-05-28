package net.nmmst.media;

/**
 * Maintains the byte array of audio.
 */
public class Sample {
    /**
     * A byte array of audio.
     */
    private final byte[]  data;
    /**
     * The attribute of this audio.
     */
    private final MovieAttribute attribute;
    private final boolean end;
    public Sample() {
        data = null;
        attribute = null;
        end = true;
    }
    /**
     * Constructs a sample by specified attribute and audio data.
     * @param movieAttribute The movie attribute
     * @param audioData A byte array of audio
     */
    public Sample(MovieAttribute movieAttribute, byte[] audioData) {
        attribute = movieAttribute;
        data = audioData;
        end = false;
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
        return data == null ? 0 : data.length;
    }
    /**
     * Indicates whether end.
     * @return True if end of movie
     */
    public boolean isEnd() {
        return end;
    }
}
