package tw.gov.nmmst.utils;

import tw.gov.nmmst.NProperties;
import tw.gov.nmmst.NodeInformation;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * Transfer the serial object to nodes.
 * @see net.nmmst.request.Request
 * @see net.nmmst.register.SerializedBufferMetrics
 */
public class SerialStream implements Closeable {
    /**
     * Log.
     */
    private static final Log LOG
            = LogFactory.getLog(SerialStream.class);
    /**
     * Waitting time for sending request.
     */
    private static final int WAITTING_TIME = 5;
    /**
     * The socket to send message.
     */
    private final Socket client;
    /**
     * The input stream.
     */
    private ObjectInputStream input = null;
    /**
     * The output stream.
     */
    private ObjectOutputStream output = null;
    /**
     * Sends message to all nodes. This method will be blocked until
     * accomplish all transfer process.
     * @param nodeInformation The node to send to
     * @param request The object to transfer
     * @param properties NProperties
     * @return {@code true} if no error happen, {@code false} otherwise
     * @throws InterruptedException If this method is broke
     * @throws IOException If failed to transfer object
     */
    public static boolean send(final NodeInformation nodeInformation,
            final RequestUtil.Request request, final NProperties properties)
            throws InterruptedException, IOException {
        return sendAll(Arrays.asList(nodeInformation),
                request,
                nodeInformation.getRequestPort());
    }
    /**
     * Sends message to the node. This method will be blocked until
     * accomplish transfer process.
     * @param nodeInformation The node to send to
     * @param serial The object to transfer
     * @param port The destination port
     * @return {@code true} if no error happen, {@code false} otherwise
     * @throws InterruptedException If this method is broke
     * @throws IOException If failed to transfer object
     */
    public static boolean send(final NodeInformation nodeInformation,
            final Serializable serial, final int port)
            throws InterruptedException, IOException {
        return sendAll(Arrays.asList(nodeInformation), serial, port);
    }
    /**
     * Sends message to all nodes. This method will be blocked until
     * accomplish all transfer process.
     * @param nodeInformations The nodes to send to
     * @param request The object to transfer
     * @return {@code true} if no error happen, {@code false} otherwise
     * @throws InterruptedException If this method is broke
     * @throws IOException If failed to transfer object
     */
    public static boolean sendAll(
            final Collection<NodeInformation> nodeInformations,
            final RequestUtil.Request request)
            throws InterruptedException, IOException {
        List<SerialStream> handlers = new ArrayList(nodeInformations.size());
        for (NodeInformation playerInfo : nodeInformations) {
            try {
                SerialStream stream = new SerialStream(new Socket(
                        playerInfo.getIP(), playerInfo.getRequestPort()));
                stream.openOutputStream();
                handlers.add(stream);
            } catch (IOException e) {
                LOG.error(e);
                handlers.stream()
                        .filter((handler) -> (handler != null))
                        .forEach((handler) -> {
                    handler.close();
                });
                throw e;
            }
        }
        return internalSend(request, handlers);
    }
    /**
     * Sends message to all nodes. This method will be blocked until
     * accomplish all transfer process.
     * @param nodeInformations The nodes to send to
     * @param serial The object to transfer
     * @param port The destination port
     * @return {@code true} if no error happen, {@code false} otherwise
     * @throws InterruptedException If this method is broke
     * @throws IOException If failed to transfer object
     */
    public static boolean sendAll(
            final Collection<NodeInformation> nodeInformations,
            final Serializable serial, final int port)
            throws InterruptedException, IOException {
        List<SerialStream> handlers = new ArrayList(nodeInformations.size());
        for (NodeInformation playerInfo : nodeInformations) {
            try {
                SerialStream stream = new SerialStream(new Socket(
                        playerInfo.getIP(), playerInfo.getRequestPort()));
                stream.openOutputStream();
                handlers.add(stream);
            } catch (IOException e) {
                LOG.error(e);
                handlers.stream()
                        .filter((handler) -> (handler != null))
                        .forEach((handler) -> {
                    handler.close();
                });
                throw e;
            }
        }
        return internalSend(serial, handlers);
    }
    /**
     * Sends the serial object to all nodes.
     * This method will be blocked until all nodes receive
     * the object or timeout.
     * @param serial The object to send
     * @param handlers The connected remote nodes
     * @return {@code true} if succeed
     * @throws InterruptedException Anyone brokes this medhod
     */
    private static boolean internalSend(final Serializable serial,
            final List<SerialStream> handlers) throws InterruptedException {
        ExecutorService service
                = Executors.newFixedThreadPool(handlers.size());
        final CountDownLatch latch
                = new CountDownLatch(handlers.size());
        handlers.forEach(handler -> {
            service.execute(() -> {
                latch.countDown();
                try {
                    latch.await();
                    handler.write(serial);
                } catch (IOException | InterruptedException e) {
                    LOG.error(e);
                } finally {
                    handler.close();
                }
            });
        });
        service.shutdown();
        return service.awaitTermination(WAITTING_TIME, TimeUnit.SECONDS);
    }
    /**
     * Constructs a serial stream for existed socket.
     * @param socket socket
     * @exception SocketException if there is an error
     * in the underlying protocol, such as a TCP error.
     */
    public SerialStream(final Socket socket) throws SocketException {
        client = socket;
        client.setTcpNoDelay(true);
    }
    /**
     * Constructs a serial stream for specified address and port.
     * @param address Remote address
     * @param port Remote port
     * @throws UnknownHostException If the IP address of
     * the host could not be determined
     * @throws IOException If an I/O error occurs when creating the socket
     */
    public SerialStream(final String address, final int port)
            throws UnknownHostException, IOException {
        this(new Socket(address, port));
    }
    /**
     * Creates an ObjectInputStream that reads from the specified InputStream.
     * @throws IOException Any of the usual Input/Output
     * related exceptions
     */
    public final void openInputStream() throws IOException {
        if (input == null) {
            input = new ObjectInputStream(client.getInputStream());
        }
    }
    /**
     * Creates an ObjectOutputStream that writes to the specified OutputStream.
     * @throws IOException Any of the usual Input/Output
     * related exceptions
     */
    public final void openOutputStream() throws IOException {
        if (output == null) {
            output = new ObjectOutputStream(client.getOutputStream());
        }
    }
    /**
     * Returns the local IP address string in textual presentation.
     * @return The local raw IP address in a string format
     */
    public final String getLocalAddress() {
        return client.getLocalAddress().getHostAddress();
    }
    /**
     * Returns the remote IP address string in textual presentation.
     * @return The remote raw IP address in a string format
     */
    public final String getHostAddress() {
        return client.getInetAddress().getHostAddress();
    }
    /**
     * Reads an object from this stream.
     * @return The Object read from the stream
     * @throws ClassNotFoundException Class of a serialized object
     * cannot be found
     * @throws IOException Any of the usual Input/Output
     * related exceptions
     */
    public final Object read() throws ClassNotFoundException, IOException {
        openInputStream();
        return input.readObject();
    }
    /**
     * Writes the specified object to this stream.
     * @param serial The serial object to write
     * @throws IOException Any of the usual Input/Output
     * related exceptions
     */
    public final void write(final Serializable serial) throws IOException {
        openOutputStream();
        output.writeObject(serial);
    }
    @Override
    public final void close() {
        try {
            client.close();
        } catch (IOException e) {
            LOG.error(e);
        }
    }
}
