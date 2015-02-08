package net.nmmst.movie;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import net.nmmst.tools.NMConstants;
import net.nmmst.tools.Pair;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class MovieOrder {
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
        for(Pair<Boolean, String> path : moviePaths) {
            this.moviePaths.add(path.copyOf());
            movieStreams.add(new Pair(path.getFirst(), new MovieStream(path.getSecond(), index)));
            ++index;
        }
    }
    public synchronized void setEnable(int nextIndex, boolean value) {
        if(nextIndex >= moviePaths.size()) {
            return;
        }
        Pair<Boolean, String> path = moviePaths.get(nextIndex);
        moviePaths.set(nextIndex, new Pair(value, path.getSecond()));
        Pair<Boolean, MovieStream> stream = movieStreams.get(nextIndex);
        movieStreams.set(nextIndex, new Pair(value, stream.getSecond()));
    }
    public synchronized MovieAttribute[] getMovieAttribute() {
        List<MovieAttribute> attributes = new LinkedList();
        for (Pair<Boolean, MovieStream> stream : movieStreams) {
            attributes.add(stream.getSecond());	
        }
        return attributes.toArray(new MovieAttribute[attributes.size()]);
    }
    public synchronized void setnextIndex(int nextIndex) {
        this.nextIndex = nextIndex;
    }
    public synchronized MovieStream getNextMovieStream() throws IOException {
        closeCurrentStream();
        while (nextIndex >= 0 && nextIndex < moviePaths.size()) {
            Pair<Boolean, MovieStream> stream = movieStreams.get(nextIndex);
            if(stream.getFirst()) {
                preIndex = nextIndex;
                ++nextIndex;
                return stream.getSecond();
            }
            ++nextIndex;
        }
        return null;
    }
    private void closeCurrentStream() throws IOException {
        if(preIndex >= moviePaths.size() || preIndex < 0) {
            return;
        }
        Pair<Boolean, String> path = moviePaths.get(preIndex);
        Pair<Boolean, MovieStream> stream = movieStreams.get(preIndex);
        stream.getSecond().close();
        movieStreams.set(preIndex, new Pair(stream.getFirst(), new MovieStream(path.getSecond(), preIndex)));
    }
    public synchronized void reset() throws IOException {
        closeCurrentStream();
        List<Pair<Boolean, String>> initPaths = getDefault();
        for(int index = 0; index != initPaths.size(); ++index) {
            setEnable(index, initPaths.get(index).getFirst());
        }
        nextIndex = 0;
        preIndex = -1;
    }
}
