package tw.gov.nmmst.utils;

import tw.gov.nmmst.NProperties;
import tw.gov.nmmst.NConstants;
import tw.gov.nmmst.NodeInformation;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import tw.gov.nmmst.media.BufferMetrics;
import tw.gov.nmmst.threads.Closer;
import tw.gov.nmmst.threads.Taskable;
import tw.gov.nmmst.threads.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * This utility supports watcher and reporter for user and video node
 * respectively.
 */
public final class RegisterUtil {
    /**
     * Log.
     */
    private static final Logger LOG
            = LoggerFactory.getLogger(RegisterUtil.class);
    /**
     * This interface is used for monitoring the buffer status
     * of all video nodes.
     */
    public interface Watcher {
        /**
         * Checks the buffer status for specified node.
         * @param node NodeInformation
         * @return {@code true} if the specified node has enough buffer,
         * {@code false} otherwise
         */
        boolean isBufferInsufficient(NodeInformation node);
        /**
         * Checks the buffer status for all nodes.
         * @return {@code true} if all node have enough buffer,
         * {@code false} otherwise
         */
        boolean isBufferInsufficient();
        /**
         * Checks whether the modification of play flow is conflict with
         * currently decoded movie. We disallow changing the skip flag for being
         * decoded movie.
         * @see net.nmmst.movie.MovieOrder.MovieInfo
         * @param index The next movie index
         * @return {@code true} if no conflict, {@code false} otherwist
         */
        boolean isConflictWithBuffer(int index);
        /**
         * Checks all video nodes right now.
         */
        void checkNow();
    }
    /**
     * Instantiates a Watcher for monitoring buffer status for all video nodes.
     * @param closer This closer is used for stoping the watcher
     * @param timer Control the check period
     * @param properties NProperties
     * @return A watcher implementation
     */
    public static Watcher createWatcher(final Closer closer, final Timer timer,
                final NProperties properties) {
        return closer.invokeNewThread(new WatcherImpl(properties), timer);
    }
    /**
     * Invokes the reporter for transfering the buffer metrics.
     * @param closer This closer to add closeable
     * @param nodeInformation Node information
     * @param bufferMetrics BufferMetrics
     * @throws IOException If failed to establish a server socket
     */
    public static void invokeReporter(
            final Closer closer,
            final NodeInformation nodeInformation,
            final BufferMetrics bufferMetrics) throws IOException {
        closer.invokeNewThread(new Reporter(nodeInformation,
                bufferMetrics), null);
    }
    /**
     * A {@link Watcher} implementation.
     */
    private static class WatcherImpl implements Taskable, Watcher {
        /**
         * The node inforamtion to monitor.
         */
        private final Collection<NodeInformation> nodeInformations;
        /**
         * The current status of video nodes.
         */
        private final Map<NodeInformation, SerializedBufferMetrics> playerStates
                = new HashMap();
        /**
         * The lower limit for frame buffer.
         */
        private final double lowerLimit;
        /**
         * Instantiates a {@link Watcher}.
         * @param properties NProperties
         */
        public WatcherImpl(final NProperties properties) {
            nodeInformations = NodeInformation.getVideoNodes(properties);
            lowerLimit = properties.getDouble(
                    NConstants.FRAME_BUFFER_LOWERLIMIT);
        }
        @Override
        public void work() {
            checkNow();
        }
        @Override
        public void clear() {
            nodeInformations.clear();
            playerStates.clear();
        }
        /**
         * Checks the buffer status for specified node.
         * @param node The node to check
         * @param metrics The buffer metric of specified node
         * @param lowerLimit The buffer size limit
         * @return {@code true} if the node has enough buffer,
         * {@code false} otherwise
         */
        private static boolean hasLowerBuffer(final NodeInformation node,
                final SerializedBufferMetrics metrics,
                final double lowerLimit) {
            final double ratio = (double) metrics.getFrameNumber()
                    / (double) metrics.getFrameCapacity();
            if (ratio <= lowerLimit) {
                StringBuilder builder = new StringBuilder(
                        node.toString());
                builder.append(" buffer frame : ")
                       .append(metrics.getFrameNumber())
                       .append(", capacity : ")
                       .append(metrics.getFrameCapacity());
                if (LOG.isDebugEnabled()) {
                    LOG.debug(builder.toString());
                }
                return true;
            }
            return false;
        }
        @Override
        public boolean isBufferInsufficient(final NodeInformation node) {
            synchronized (playerStates) {
                SerializedBufferMetrics metrics = playerStates.get(node);
                if (metrics == null) {
                    return true;
                }
                return hasLowerBuffer(node, metrics, lowerLimit);
            }
        }

        @Override
        public boolean isBufferInsufficient() {
            synchronized (playerStates) {
                if (playerStates.size() != nodeInformations.size()) {
                    if (LOG.isDebugEnabled()) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("current player number : ")
                           .append(playerStates.size())
                           .append(", it should be : ")
                           .append(nodeInformations.size())
                           .append("\n");
                        nodeInformations.stream().forEach((nodeInfo) -> {
                            builder.append(nodeInfo.toString()).append("\n");
                        });
                        LOG.debug(builder.toString());
                    }
                    return true;
                }
                return playerStates.entrySet().stream().anyMatch((entry)
                    -> (hasLowerBuffer(entry.getKey(),
                                      entry.getValue(),
                                      lowerLimit)));
            }
        }

        @Override
        public void checkNow() {
            synchronized (playerStates) {
                playerStates.clear();
            }
            nodeInformations.stream().forEach((nodeInformation) -> {
                try (SerialStream client = new SerialStream(new Socket(
                        nodeInformation.getIP(),
                        nodeInformation.getRegisterPort()))) {
                    Object obj = client.read();
                    if (obj != null
                        && obj.getClass() == SerializedBufferMetrics.class) {
                        SerializedBufferMetrics state
                            = (SerializedBufferMetrics) obj;
                        synchronized (playerStates) {
                            playerStates.put(nodeInformation, state);
                        }
                        LOG.info(nodeInformation + "\n" + state);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    LOG.error(e.getMessage() + ":" + nodeInformation);
                }
            });
        }

        @Override
        public boolean isConflictWithBuffer(final int index) {
            synchronized (playerStates) {
                return playerStates.size() != nodeInformations.size();
            }
        }
    }

    /**
     * Reports the buffer metrics.
     */
    private static class Reporter implements Taskable {
        /**
         * The server to accept connection from the master node.
         */
        private final ServerSocket server;
        /**
         * This video node's buffer info.
         */
        private final BufferMetrics metrics;
        /**
         * Constructs a server to provide the buffer info.
         * @param info Node information
         * @param bufferMetrics BufferMetrics
         * @throws IOException If failed to establish a server socket
         */
        public Reporter(final NodeInformation info,
                final BufferMetrics bufferMetrics)throws IOException {
            server = new ServerSocket(info.getRegisterPort());
            metrics = bufferMetrics;
            LOG.info(server.getLocalSocketAddress()
            + ":" + server.getLocalPort());
        }
        @Override
        public void close() {
            try {
                server.close();
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            }
        }

        @Override
        public void work() {
            try (SerialStream client = new SerialStream(server.accept())) {
                client.write(new SerializedBufferMetrics(metrics));
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }
    }
    /**
     * This is a {@link BufferMetrics} wrapper which is able to
     * be serialized.
     */
    private static class SerializedBufferMetrics
        implements Serializable, BufferMetrics {
        /**
         * Frame number.
         */
        private final int frameNumber;
        /**
         * Sample number.
         */
        private final int sampleNumber;
        /**
         * Frame capacity.
         */
        private final int frameCapacity;
        /**
         * Sample capacity.
         */
        private final int sampleCapacity;
        /**
         * The heap size to include the frames and samples.
         */
        private final long heapSize;
        /**
         * The last index from the buffer of video node.
         */
        private final int lastIndex;
        /**
         * The last micro timestamp from the buffer of video node.
         */
        private final long lastTimestamp;
        /**
         * The duration from movie of last frame.
         */
        private final long lastDuration;
        /**
         * The current index from the buffer of video node.
         */
        private final int currentIndex;
        /**
         * The current micro timestamp from the buffer of video node.
         */
        private final long currentTimestamp;
        /**
         * The duration from movie of current frame.
         */
        private final long currentDuration;
        /**
         * Constructs a serialized {@link BufferMetrics}.
         * @param metrics BufferMetrics
         */
        public SerializedBufferMetrics(final BufferMetrics metrics) {
            frameNumber = metrics.getFrameNumber();
            sampleNumber = metrics.getSampleNumber();
            frameCapacity = metrics.getFrameCapacity();
            sampleCapacity = metrics.getSampleCapacity();
            heapSize = metrics.getHeapSize();
            lastIndex = metrics.getLastMovieIndex();
            lastTimestamp = metrics.getLastTimestamp();
            lastDuration = metrics.getLastDuration();
            currentIndex = metrics.getCurrentMovieIndex();
            currentTimestamp = metrics.getCurrentTimestamp();
            currentDuration = metrics.getCurrentDuration();
        }
        @Override
        public long getHeapSize() {
            return heapSize;
        }
        @Override
        public int getFrameNumber() {
            return frameNumber;
        }
        @Override
        public int getSampleNumber() {
            return sampleNumber;
        }
        @Override
        public int getFrameCapacity() {
            return frameCapacity;
        }
        @Override
        public int getSampleCapacity() {
            return sampleCapacity;
        }
        @Override
        public int getLastMovieIndex() {
            return lastIndex;
        }
        @Override
        public long getLastTimestamp() {
            return lastTimestamp;
        }
        @Override
        public long getLastDuration() {
            return lastDuration;
        }

        @Override
        public int getCurrentMovieIndex() {
            return currentIndex;
        }

        @Override
        public long getCurrentTimestamp() {
            return currentTimestamp;
        }

        @Override
        public long getCurrentDuration() {
            return currentDuration;
        }
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            return builder.append("frame/capacity: ")
                          .append(frameNumber)
                          .append("/")
                          .append(frameCapacity)
                          .append(", sample/capacity : ")
                          .append(sampleNumber)
                          .append("/")
                          .append(sampleCapacity)
                          .append(", current index/timestamp/duration : ")
                          .append(currentIndex)
                          .append("/")
                          .append(currentTimestamp)
                          .append("/")
                          .append(currentDuration)
                          .append(", last index/timestamp/duration : ")
                          .append(lastIndex)
                          .append("/")
                          .append(lastTimestamp)
                          .append("/")
                          .append(lastDuration)
                          .toString();
        }
    }
    /**
     * Can't be instantiated with this ctor.
     */
    private RegisterUtil() {
    }
}
