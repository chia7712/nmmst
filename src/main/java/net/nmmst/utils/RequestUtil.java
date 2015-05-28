package net.nmmst.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.imageio.ImageIO;
import net.nmmst.NodeInformation;
import net.nmmst.processor.LinearProcessor;
import net.nmmst.threads.Closer;
import net.nmmst.threads.Taskable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class RequestUtil {
    /**
     * Log.
     */
    private static final Logger LOG
            = LoggerFactory.getLogger(RequestUtil.class);
    /**
     * Creates the reporter for transfering the buffer metrics.
     * @param closer This closer to add closeable
     * @param nodeInformation Node information
     * @return The BlockingQueue reveives the {@link Request} from remote nodes
     * @throws IOException If failed to establish a server socket
     */
    public static BlockingQueue<Request> createRemoteQueue(
            final NodeInformation nodeInformation,
            final Closer closer) throws IOException {
        return closer.invokeNewThread(new RemoteRequestQueue(nodeInformation),
                null).get();
    }
    /**
     * Receives the request from master node.
     */
    private static class RemoteRequestQueue implements Taskable {
        /**
         * Buffers the request.
         */
        private final BlockingQueue<Request> requestQueue
                = new LinkedBlockingQueue();
        /**
         * The server to receive the request.
         */
        private final ServerSocket server;
        /**
         * Constructs a request server for receiving the request from master node.
         * @param closer Closer
         * @param nodeInformation Node information
         * @throws IOException 
         */
        private RemoteRequestQueue(final NodeInformation nodeInformation)
                throws IOException {
            server = new ServerSocket(nodeInformation.getRequestPort());
            LOG.info(server.getLocalSocketAddress()
            + ":" + server.getLocalPort());
        }
        private BlockingQueue<Request> get() {
            return requestQueue;
        }
        @Override
        public void work() {
            try (SerialStream stream = new SerialStream(server.accept())) {
                Object obj = stream.read();
                if (obj instanceof Request) {
                    requestQueue.put((Request)obj);
                }
            } catch (IOException | ClassNotFoundException
                    | InterruptedException e) {
                LOG.error(e.getMessage());
            }
        }
        @Override
        public void clear() {
            try {
                server.close();
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            }
        }
    }
    public enum RequestType {
        START,
        STOP,
        PAUSE,
        INIT,
        SELECT,
        FUSION_TEST,
        PARTY1,
        PARTY2,
        LIGHT_OFF,
        REBOOT,
        SHUTDOWN,
        WOL
    }
    /**
     * Base request for encapsulating the {@link RequestType}.
     */
    public static class Request implements Serializable {
        private final RequestType type;
        public Request(final RequestType requestType) {
            type = requestType;
        }
        public RequestType getType() {
            return type;
        }
    }
    /**
     * Encapsulates the {@link net.nmmst.processor.LinearProcessor.Factor}
     * and {@link java.awt.image.BufferedImage}.
     */
    public static class FusionTestRequest extends Request {
        /**
         * The bytes array of image data.
         */
        private final byte[] imageData;
        /**
         * Overlay for x axis.
         */
        private final double overlayX;
        /**
         * Overlay for y axis.
         */
        private final double overlayY;
        /**
         * The scale of first x axis.
         */
        private final double scaleMinX;
        /**
         * The scale of end x axis.
         */
        private final double scaleMaxX;
        /**
         * The scale of first y axis.
         */
        private final double scaleMinY;
        /**
         * The scale of end y axis.
         */
        private final double scaleMaxY;
        public FusionTestRequest(final BufferedImage image,
                final LinearProcessor.Factor factor) throws IOException {
            super(RequestType.FUSION_TEST);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);    
            imageData = baos.toByteArray();
            overlayX = factor.getOverlayX();
            overlayY = factor.getOverlayY();
            scaleMinX = factor.getScaleMinX();
            scaleMaxX = factor.getScaleMaxX();
            scaleMinY = factor.getScaleMinY();
            scaleMaxY = factor.getScaleMaxY();
        }
        public LinearProcessor.Factor getFactor() {
            return new LinearProcessor.Factor(
                overlayX, overlayY,
                scaleMinX, scaleMaxX,
                scaleMinY,  scaleMaxY
            );
        }
        public BufferedImage getImage() throws IOException {
            return ImageIO.read(new ByteArrayInputStream(imageData));
        }
    }
    /**
     * This request is used for setting the video flow.
     */
    public static class SelectRequest extends Request {
        /**
         * The next movie index.
         */
        private final int index;
        /**
         * Constructs a SelectRequest for specified indexes and values.
         * @param movieIndex The movie index
         */
        public SelectRequest(int movieIndex) {
            super(RequestType.SELECT);
            index = movieIndex;
        }
        /**
         * @return The movie index
         */
        public int getIndex() {
            return index;
        }
    }
    /**
     * Can't be instantiated with this ctor.
     */
    private RequestUtil() {
    }
}
