package net.nmmst.register;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicBoolean;

import net.nmmst.movie.BufferFactory;
import net.nmmst.movie.MovieBuffer;
import net.nmmst.tools.Closure;
import net.nmmst.tools.SerialStream;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class RegisterServer implements Closure {
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        while(!close.get() && !Thread.interrupted()) {
            SerialStream client = null;
            try {
                client = new SerialStream(server.accept());
                client.write(new PlayerState(buffer.getFrameSize(), buffer.getSampleSize(), buffer.getMaxFrameSize(), buffer.getMaxSampleSize()));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if(client != null) {
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