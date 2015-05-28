package net.nmmst.media;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import javax.sound.sampled.AudioFormat;
import net.nmmst.NConstants;
import net.nmmst.NProperties;

/**
 * Control the order of movies. Each movie has a skip tag to indicate whether
 * this movie should be skiped. So we can control the play flow by setting
 * the skip flag for each movie.
 */
public class MovieInfo {
    /**
     * Maintains the movie file. Key is the movie index.
     */
    private final TreeMap<Integer, MovieAttributeWrapper> movieMap = new TreeMap();
    /**
     * Maintains the movie order. Key is the play sequenece which is incremental.
     */
    private final Map<Integer, MovieAttributeWrapper> playOrder = new TreeMap();
    /**
     * Constructs the movie order for specifed file list getted from properties.
     * The specified key is {@link NConstants#MOVIE_PATH}
     * @param properties NProperties
     * @throws java.io.IOException If failed to open movie file
     */
    public MovieInfo(NProperties properties) throws IOException {
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
                movieMap.put(index, new MovieAttributeWrapper(stream));
            }
        }
        for (Integer index : defaultPlayOrder) {
            if (!movieMap.containsKey(index)) {
                throw new RuntimeException(
                    "The movie doesn't exist for index " + index );
            }
        }
        int order = 0;
        for (Integer index : defaultPlayOrder) {
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
    public boolean isIndexExist(int index) {
        return movieMap.containsKey(index);
    }
    /**
     * Retrieves the movie attruibute for specified index.
     * @param index The movie index
     * @return Movie attuibute
     */
    public Optional<MovieAttribute> getMovieAttribute(int index) {
        return Optional.ofNullable(movieMap.get(index));
    }
    private Map<Integer, MovieAttribute> cloneOrder() {
        return new TreeMap(playOrder);
    }
    public PlayFlow createPlayFlow() {
        return new PlayFlow(this);
    }
    public static class PlayFlow implements Iterator<MovieAttribute> {
        private final MovieInfo mOrder;
        private final Map<Integer, MovieAttribute> playOrder;
        private final Object lock = new Object();
        private int currentOrder = 0;
        private MovieAttribute attribute = null;
        public PlayFlow(final MovieInfo movieOrder) {
            mOrder = movieOrder;
            playOrder = mOrder.cloneOrder();
        }
        public void setNextFlow(int index) {
            setFlow(currentOrder + 1, index);
        }
        private void setFlow(int order, int index) {
            synchronized(lock) {
                if (order != currentOrder) {
                    mOrder.getMovieAttribute(index).ifPresent(m -> {
                        playOrder.put(order, m);
                    });
                }
            }
        }
        @Override
        public boolean hasNext() {
            synchronized(lock) {
                try {
                    if (attribute != null) {
                        attribute = null;
                    }
                    attribute = playOrder.get(currentOrder);
                    return attribute != null;
                } finally {
                    ++currentOrder;
                }
            }
        }
        @Override
        public MovieAttribute next() {
            return attribute;
        }
    }

     
    /**
     * Maintains the ref count, file, skip flag.
     */
    private static class MovieAttributeWrapper implements MovieAttribute {
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
        public MovieAttributeWrapper(final MovieAttribute attribute) {
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
