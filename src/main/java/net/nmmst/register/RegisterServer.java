package net.nmmst.register;

import java.io.IOException;
import java.net.ServerSocket;
import net.nmmst.movie.BufferFactory;
import net.nmmst.movie.MovieBuffer;
import net.nmmst.tools.BackedRunner;
import net.nmmst.tools.Closer;
import net.nmmst.tools.SerialStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class RegisterServer extends BackedRunner {
    private static final Logger LOG = LoggerFactory.getLogger(RegisterServer.class);
    private final MovieBuffer buffer = BufferFactory.getMovieBuffer();
    private final ServerSocket server;
    public RegisterServer(Closer closer, int bindPort) throws IOException {
        super(closer);
        server = new ServerSocket(bindPort);
    }
    @Override
    protected void work() {
        try (SerialStream client = new SerialStream(server.accept())) {
            client.write(new PlayerState(buffer.getFrameSize(), buffer.getSampleSize(), buffer.getFrameQueueSize(), buffer.getSampleQueueSize()));
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }
    @Override
    protected void clear() {
        try {
            server.close();
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }
    @Override
    protected void init() {
    }
}