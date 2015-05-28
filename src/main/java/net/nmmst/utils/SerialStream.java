package net.nmmst.utils;

import net.nmmst.NProperties;
import net.nmmst.NConstants;
import net.nmmst.NodeInformation;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Transfer the serial object to nodes.
 * @see net.nmmst.request.Request
 * @see net.nmmst.register.SerializedBufferMetrics
 * 
 */
public class SerialStream implements Closeable {
    /**
     * Log.
     */
    private static final Logger LOG
            = LoggerFactory.getLogger(SerialStream.class);
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
     * @return True if no error happen, false otherwise
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
     * @return True if no error happen, false otherwise
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
     * @return True if no error happen, false otherwise
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
                handlers.add(new SerialStream(new Socket(
                        playerInfo.getIP(), playerInfo.getRequestPort())));
            } catch(IOException e) {
                LOG.error(e.getMessage());
                handlers.stream()
                        .filter((handler) -> (handler != null))
                        .forEach((handler) -> {
                    handler.close();
                });
                throw e;
            }
        }
        return innerSend(request, handlers);
    }
    /**
     * Sends message to all nodes. This method will be blocked until
     * accomplish all transfer process. 
     * @param nodeInformations The nodes to send to
     * @param serial The object to transfer
     * @param port The destination port
     * @return True if no error happen, false otherwise
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
                handlers.add(new SerialStream(new Socket(
                        playerInfo.getIP(), port)));
            } catch(IOException e) {
                LOG.error(e.getMessage());
                handlers.stream()
                        .filter((handler) -> (handler != null))
                        .forEach((handler) -> {
                    handler.close();
                });
                throw e;
            }
        }
        return innerSend(serial, handlers);
    }
    private static boolean innerSend(final Serializable serial,
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
                    LOG.error(e.getMessage());
                } finally {
                    handler.close();
                }
            });
        });
        service.shutdown();
        return service.awaitTermination(5, TimeUnit.SECONDS);
    }
    public SerialStream(final Socket socket) throws IOException {
        client = socket;
    }
    public SerialStream(final String ip, final int port)
            throws UnknownHostException, IOException {
        this(new Socket(ip, port));
    }
    /**
     * Returns the local IP address string in textual presentation.
     * @return The local raw IP address in a string format
     */
    public String getLocalAddress() {
        return client.getLocalAddress().getHostAddress();
    }
    /**
     * Returns the remote IP address string in textual presentation.
     * @return The remote raw IP address in a string format
     */
    public String getHostAddress() {
        return client.getInetAddress().getHostAddress();
    }
    public Object read() throws ClassNotFoundException, IOException {
        if (input == null) {
            input = new ObjectInputStream(client.getInputStream());
        }
        return input.readObject();
    }
    public void write(final Serializable serial) throws IOException {
        if (output == null) {
            output = new ObjectOutputStream(client.getOutputStream());
        }
        output.writeObject(serial);
    }
    @Override
    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }
}