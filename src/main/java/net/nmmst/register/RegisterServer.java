package net.nmmst.register;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicBoolean;

import net.nmmst.movie.BufferFactory;
import net.nmmst.movie.MovieBuffer;
import net.nmmst.tools.BackedRunner;
import net.nmmst.tools.SerialStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class RegisterServer implements BackedRunner {
    private static final Logger LOG = LoggerFactory.getLogger(RegisterServer.class);
    private final AtomicBoolean close = new AtomicBoolean(false);
    private final AtomicBoolean isClosed = new AtomicBoolean(false);	
    private final MovieBuffer buffer = BufferFactory.getMovieBuffer();
    private final ServerSocket server;
    public RegisterServer(int bindPort) throws IOException {
        server = new ServerSocket(bindPort);
    }
    @Override
    public void close() {
        close.set(true);
        try {
            server.close();
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }
    @Override
    public void run() {
        while (!close.get() && !Thread.interrupted()) {
            SerialStream client = null;
            try {
                client = new SerialStream(server.accept());
                client.write(new PlayerState(buffer.getFrameSize(), buffer.getSampleSize(), buffer.getMaxFrameSize(), buffer.getMaxSampleSize()));
            } catch (IOException e) {
                LOG.error(e.getMessage());
            } finally {
                if (client != null) {
                    client.close();
                }
            }
        }
    }
    @Override
    public boolean isClosed() {
        return isClosed.get();
    }
}