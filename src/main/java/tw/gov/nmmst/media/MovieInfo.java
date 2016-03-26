package tw.gov.nmmst.media;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import javax.sound.sampled.AudioFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tw.gov.nmmst.NConstants;
import tw.gov.nmmst.NProperties;

/**
 * Control the order of movies. Each movie has a skip tag to indicate whether
 * this movie should be skiped. So we can control the play flow by setting
 * the skip flag for each movie.
 * @see MediaWorker
 */
public final class MovieInfo {
    /**
     * Log.
     */
    private static final Log LOG
            = LogFactory.getLog(MovieInfo.class);
    /**
     * Maintains the movie file. Key is the movie index.
     */
    private final TreeMap<Integer, MovieAttributeClone> movieMap
            = new TreeMap();
    /**
     * Maintains the movie order.
     * Key is the play sequenece which is incremental.
     */
    private final Map<Integer, MovieAttributeClone> playOrder
            = new TreeMap();
    /**
     * Constructs the movie order for specifed file list getted from properties.
     * The specified key is {@link NConstants#MOVIE_PATH}
     * @param properties NProperties
     * @throws java.io.IOException If failed to open movie file
     */
    public MovieInfo(final NProperties properties) throws IOException {
        this(properties.getStrings(NConstants.MOVIE_PATH),
             properties.getIntegers(NConstants.MOVIE_ORDER));
    }
    /**
     * Constructs the movie order by cloning the file list.
     * The initial order is equal with order in list.
     * @param movieFiles The source file
     * @param defaultPlayOrder The play order
     * @throws java.io.IOException If failed to open movie file
     * @throws RuntimeException If the input is empty
     */
    public MovieInfo(final List<String> movieFiles,
            final List<Integer> defaultPlayOrder) throws IOException {
        for (int index = 0; index != movieFiles.size(); ++index) {
            try (MovieStream stream = new MovieStream(
                    movieFiles.get(index), index)) {
                movieMap.put(index, new MovieAttributeClone(stream));
            }
        }
        for (Integer index : defaultPlayOrder) {
            if (!movieMap.containsKey(index)) {
                throw new RuntimeException(
                    "The movie doesn't exist for index " + index);
            }
        }
        int order = 0;
        for (Integer index : defaultPlayOrder) {
            LOG.info("order:index," + order + ":" + index);
            playOrder.put(order, movieMap.get(index));
            ++order;
        }
        if (movieMap.isEmpty() || playOrder.isEmpty()) {
            throw new RuntimeException("No readable movies or order");
        }
    }
    /**
     * Returns the number of movies.
     * @return The number of movies
     */
    public int size() {
        return movieMap.size();
    }
    /**
     * Returns {@code true} if this movie info contains a mapping
     * for the specified key.
     * @param index The index whose presence in this map is to be tested
     * @return {@code true} if this map contains a mapping for the
     *         specified key
     */
    public boolean containsIndex(final int index) {
        return movieMap.containsKey(index);
    }
    /**
     * Retrieves the movie attruibute for specified index.
     * @param index The movie index
     * @return Movie attuibute
     */
    public Optional<MovieAttribute> getMovieAttribute(final int index) {
        return Optional.ofNullable(movieMap.get(index));
    }
    /**
     * Clones the movie order.
     * @return Movie order
     */
    private Map<Integer, MovieAttribute> cloneOrder() {
        return new TreeMap(playOrder);
    }
    /**
     * Creates a play flow with a order.
     * The {@link PlayFlow}'s order is the same as this movie's.
     * @return A play flow
     */
    public PlayFlow createPlayFlow() {
        return new PlayFlow(this);
    }
    /**
     * The flow for plaing all movie.
     */
    public static final class PlayFlow implements Iterator<MovieAttribute> {
        /**
         * The movies to play.
         */
        private final MovieInfo mInfo;
        /**
         * Key is the movie index, and value is the movie attribute.
         */
        private final Map<Integer, MovieAttribute> playOrder;
        /**
         * Synchronize the modification for playOrder.
         */
        private final Object lock = new Object();
        /**
         * Next play ordr.
         */
        private int nextOrder = 0;
        /**
         * Current movie attribute to play.
         */
        private MovieAttribute attribute = null;
        /**
         * Constructs a play flow with default order.
         * @param movieInfo The movie info
         */
        public PlayFlow(final MovieInfo movieInfo) {
            mInfo = movieInfo;
            playOrder = mInfo.cloneOrder();
        }
        /**
         * Sets the movie index for next order.
         * @param index The movie index
         */
        public void setNextFlow(final int index) {
            LOG.info("nextOrder:index," + nextOrder + ":" + index);
            synchronized (lock) {
                mInfo.getMovieAttribute(index).ifPresent(m -> {
                    playOrder.put(nextOrder, m);
                });
                playOrder.forEach((k, v) -> {
                    LOG.info("order:index," + k + ":" + v.getIndex()
                    + " " + v.getFile().getName());
                });
            }
        }
        @Override
        public boolean hasNext() {
            synchronized (lock) {
                try {
                    if (attribute != null) {
                        attribute = null;
                    }
                    attribute = playOrder.get(nextOrder);
                    if (attribute != null) {
                        LOG.info("currentOrder:index," + nextOrder + ":"
                        + attribute.getIndex());
                    }
                    return attribute != null;
                } finally {
                    ++nextOrder;
                }
            }
        }
        @Override
        public MovieAttribute next() {
            return attribute;
        }
    }
    /**
     * A clone for specified movie attribute.
     */
    private static class MovieAttributeClone implements MovieAttribute {
        /**
         * Movid order.
         */
        private final int index;
        /**
         * Movie file.
         */
        private final File file;
        /**
         * Movie duration.
         */
        private final long duration;
        /**
         * Movie audio format.
         */
        private final AudioFormat audioFormat;
        /**
         * Constructs a clone for a movie atrribute.
         * @param attribute The movie attribute to clone
         */
        MovieAttributeClone(final MovieAttribute attribute) {
            file = attribute.getFile().getAbsoluteFile();
            index = attribute.getIndex();
            duration = attribute.getDuration();
            audioFormat = new AudioFormat(
                attribute.getAudioFormat().getEncoding(),
                attribute.getAudioFormat().getSampleRate(),
                attribute.getAudioFormat().getSampleSizeInBits(),
                attribute.getAudioFormat().getChannels(),
                attribute.getAudioFormat().getFrameSize(),
                attribute.getAudioFormat().getFrameRate(),
                attribute.getAudioFormat().isBigEndian()
            );
        }
        @Override
        public File getFile() {
            return file.getAbsoluteFile();
        }
        @Override
        public int getIndex() {
            return index;
        }
        @Override
        public long getDuration() {
            return duration;
        }
        @Override
        public AudioFormat getAudioFormat() {
            return audioFormat;
        }
    }
}
