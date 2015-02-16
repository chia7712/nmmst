package net.nmmst.tools;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.nmmst.player.NodeInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class SerialStream implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(SerialStream.class);
    private final Socket client;
    private ObjectInputStream input = null;
    private ObjectOutputStream output = null;
    public static boolean send(NodeInformation nodeInformation, Serializable serial, int port) throws InterruptedException, IOException {
        return sendAll(Arrays.asList(nodeInformation), serial, port);
    }
    public static boolean asynSendAll(List<NodeInformation> nodeInformations, final Serializable serial, int port) throws IOException {
        boolean rval = true;
        for (NodeInformation playerInfo : nodeInformations) {
            try(SerialStream stream = new SerialStream(new Socket(playerInfo.getIP(), port))) {
                stream.write(serial);
            } catch(IOException e) {
                LOG.debug(e.getMessage() + ":" + playerInfo.getIP());
                rval = false;
            }
        }
        return rval;
    }
    public static boolean sendAll(List<NodeInformation> nodeInformations, final Serializable serial, int port) throws InterruptedException, IOException {
        List<SerialStream> handlers = new ArrayList(nodeInformations.size());
        for (NodeInformation playerInfo : nodeInformations) {
            try {
                handlers.add(new SerialStream(new Socket(playerInfo.getIP(), port)));
            } catch(IOException e) {
                LOG.error(e.getMessage());
                for (SerialStream handler : handlers) {
                    if (handler != null) {
                        handler.close();
                    }
                }
                throw e;
            }
        }
        ExecutorService service = Executors.newFixedThreadPool(nodeInformations.size());
        final CountDownLatch latch = new CountDownLatch(nodeInformations.size());
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
    public SerialStream(Socket client) throws IOException {
        this.client = client;
    }

    public SerialStream(String ip, int port) throws UnknownHostException, IOException {
        this(new Socket(ip, port));
    }

    public String getLocalAddress() {
        return client.getLocalAddress().getHostAddress();
    }
    public String getHostAddress() {
        return client.getInetAddress().getHostAddress();
    }
    public Object read() throws ClassNotFoundException, IOException {
        if (input == null) {
            input = new ObjectInputStream(client.getInputStream());
        }
        return input.readObject();
    }
    public void write(Serializable serial) throws IOException {
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
