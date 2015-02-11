package net.nmmst.register;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.nmmst.player.NodeInformation;
import net.nmmst.tools.BackedRunner;
import net.nmmst.tools.SerialStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class RegisterClient implements BackedRunner {
    private static final Logger LOG = LoggerFactory.getLogger(RegisterClient.class);
    private final List<NodeInformation> nodeInformations = NodeInformation.getVideoNodes();
    private final Map<NodeInformation, PlayerState> playerStates = new HashMap();
    private final AtomicBoolean close = new AtomicBoolean(false);
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final int bindPort;
    public RegisterClient(int bindPort) throws IOException {
        this.bindPort = bindPort;
    }
    @Override
    public void close() {
        close.set(true);
    }
    public boolean isBuffered() {
        synchronized(playerStates) {
            try {
                if (playerStates.size() != nodeInformations.size()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("current player number = " + playerStates.size());
                    }
                    return false;
                }
                for (Map.Entry<NodeInformation, PlayerState> entry : playerStates.entrySet()) {
                    int bufferSize = entry.getValue().getFrameBufferSize();
                    int frameBuffered = entry.getValue().getFrameBuffered();
                    double ratio = (double)frameBuffered / (double) bufferSize;
                    if (ratio <= 0.9) {
                        return false;
                    }
                }
                return true;
            } finally {
                playerStates.clear();
            }
        }
    }
    private void checkPlayers() {
        synchronized(playerStates) {
            playerStates.clear();
        }
        for (NodeInformation nodeInformation : nodeInformations) {
            SerialStream client = null;
            try {
                client = new SerialStream(new Socket(nodeInformation.getIP(), bindPort));
                Object obj = client.read();
                if (obj instanceof PlayerState) {
                    PlayerState state = (PlayerState)obj;
                    synchronized(playerStates) {
                        playerStates.put(nodeInformation, state);
                    }
                }
            } catch(IOException | ClassNotFoundException e) {
                LOG.error(e.getMessage());
            } finally {
                if (client != null) {
                    client.close();
                }
            }
        }
    }
    @Override
    public void run() {
        try {
            while (!close.get() && !Thread.interrupted()) {
                    checkPlayers();
                    TimeUnit.SECONDS.sleep(3);
            }
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }
    }
    @Override
    public boolean isClosed() {
        return isClosed.get();
    }
	
}