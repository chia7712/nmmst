package net.nmmst.movie;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javafx.util.Pair;
import net.nmmst.request.SelectRequest;
import net.nmmst.tools.NMConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class MovieOrder {
    private static final Logger LOG = LoggerFactory.getLogger(MovieOrder.class);
    private final List<Pair<Boolean, String>> moviePaths = new LinkedList();
    private final List<Pair<Boolean, MovieStream>> movieStreams = new LinkedList();
    private int nextIndex = 0;
    private int preIndex = -1;
    public static MovieOrder get() throws IOException {
        return new MovieOrder(getDefault());
    }
    private static List<Pair<Boolean, String>> getDefault() throws IOException {
        List<Pair<Boolean, String>> moviePaths	= new LinkedList();
        moviePaths.add(new Pair(true,  NMConstants.MOVIE_ROOT_PATH + "//1.mpg"));
        moviePaths.add(new Pair(true,  NMConstants.MOVIE_ROOT_PATH + "//2A.mpg"));
        moviePaths.add(new Pair(false, NMConstants.MOVIE_ROOT_PATH + "//3B.mpg"));
        moviePaths.add(new Pair(true,  NMConstants.MOVIE_ROOT_PATH + "//4.mpg"));
        moviePaths.add(new Pair(true,  NMConstants.MOVIE_ROOT_PATH + "//5A.mpg"));
        moviePaths.add(new Pair(false, NMConstants.MOVIE_ROOT_PATH + "//6B.mpg"));
        moviePaths.add(new Pair(true,  NMConstants.MOVIE_ROOT_PATH + "//7.mpg"));
        return moviePaths;
    }
    private MovieOrder(List<Pair<Boolean, String>> moviePaths) throws IOException {
        int index = 0;
        for (Pair<Boolean, String> path : moviePaths) {
            this.moviePaths.add(new Pair(path.getKey(), path.getValue()));
            movieStreams.add(new Pair(path.getKey(), new MovieStream(path.getValue(), index)));
            ++index;
        }
    }
    public synchronized void setEnable(int nextIndex, boolean value) {
        if (nextIndex >= moviePaths.size()) {
            return;
        }
        Pair<Boolean, String> path = moviePaths.get(nextIndex);
        moviePaths.set(nextIndex, new Pair(value, path.getValue()));
        Pair<Boolean, MovieStream> stream = movieStreams.get(nextIndex);
        movieStreams.set(nextIndex, new Pair(value, stream.getValue()));
        if (LOG.isDebugEnabled()) {
            LOG.debug(nextIndex + ", " + value);
        }
    }
    public synchronized MovieAttribute[] getMovieAttribute() {
        List<MovieAttribute> attributes = new LinkedList();
        movieStreams.stream().forEach((stream) -> {
            attributes.add(stream.getValue());
        });
        return attributes.toArray(new MovieAttribute[attributes.size()]);
    }
    public synchronized void setnextIndex(int nextIndex) {
        this.nextIndex = nextIndex;
    }
    public synchronized MovieStream getNextMovieStream() throws IOException {
        closeCurrentStream();
        while (nextIndex >= 0 && nextIndex < moviePaths.size()) {
            Pair<Boolean, MovieStream> stream = movieStreams.get(nextIndex);
            if (stream.getKey()) {
                preIndex = nextIndex;
                ++nextIndex;
                return stream.getValue();
            }
            ++nextIndex;
        }
        return null;
    }
    private void closeCurrentStream() throws IOException {
        if (preIndex >= moviePaths.size() || preIndex < 0) {
            return;
        }
        Pair<Boolean, String> path = moviePaths.get(preIndex);
        Pair<Boolean, MovieStream> stream = movieStreams.get(preIndex);
        stream.getValue().close();
        movieStreams.set(preIndex, new Pair(stream.getKey(), new MovieStream(path.getValue(), preIndex)));
    }
    public synchronized void reset() throws IOException {
        closeCurrentStream();
//        List<Pair<Boolean, String>> initPaths = getDefault();
//        for (int index = 0; index != initPaths.size(); ++index) {
//            setEnable(index, initPaths.get(index).getKey());
//        }
        nextIndex = 0;
        preIndex = -1;
    }
//    public synchronized void reset(SelectRequest request) throws IOException {
//        if (request == null) {
//            reset();
//            return;
//        }
//        int[] indexes = request.getIndexs();
//        boolean[] values = request.getValues();
//        if (indexes == null || indexes.length != 7 || values == null || values.length != 7) {
//            reset();
//            return;
//        }
//        closeCurrentStream();
//        for (int i = 0; i != 7; ++i) {
//            setEnable(indexes[i], values[i]);
//        }
//        nextIndex = 0;
//        preIndex = -1;
//    }
}
