package net.nmmst.request;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import net.nmmst.movie.BufferFactory;
import net.nmmst.tools.Closure;
import net.nmmst.tools.SerialStream;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class RequestServer implements Closure {
    private final AtomicBoolean close = new AtomicBoolean(false);
    private final AtomicBoolean	 isClosed = new AtomicBoolean(false);
    private final BlockingQueue<Request> requestBuffer = BufferFactory.getRequestBuffer();
    private final ServerSocket server;
    public RequestServer(int bindPort)throws IOException {
        server = new ServerSocket(bindPort);
    }
    @Override
    public void run() {
        while (!close.get() && !Thread.interrupted()) {
            SerialStream stream = null;
            try {
                stream = new SerialStream(server.accept());
                Object obj = stream.read();
                if (obj instanceof Request) {
                    requestBuffer.put((Request)obj);
                }
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (stream != null)
                    stream.close();
            }
        }

    }
    @Override
    public void close() {
        close.set(true);
    }

    @Override
    public boolean isClosed() {
        return isClosed.get();
    }
}
