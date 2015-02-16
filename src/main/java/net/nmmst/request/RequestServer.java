package net.nmmst.request;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import net.nmmst.movie.BufferFactory;
import net.nmmst.tools.BackedRunner;
import net.nmmst.tools.Closer;
import net.nmmst.tools.SerialStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class RequestServer extends BackedRunner {
    private static final Logger LOG = LoggerFactory.getLogger(RequestServer.class);
    private final BlockingQueue<Request> requestBuffer = BufferFactory.getRequestBuffer();
    private final ServerSocket server;
    public RequestServer(Closer closer, int bindPort)throws IOException {
        super(closer);
        server = new ServerSocket(bindPort);
    }
    @Override
    protected void work() {
        try (SerialStream stream = new SerialStream(server.accept())) {
            Object obj = stream.read();
            if (obj instanceof Request) {
                requestBuffer.put((Request)obj);
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    protected void clear() {
        try {
            server.close();
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
    }
    @Override
    protected void init() {
    }
}
