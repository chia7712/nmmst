package net.nmmst.register;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.nmmst.player.NodeInformation;
import net.nmmst.tools.BackedRunner;
import net.nmmst.tools.Closer;
import net.nmmst.tools.NMConstants;
import net.nmmst.tools.SerialStream;
import net.nmmst.tools.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class RegisterClient extends BackedRunner {
    private static final Logger LOG = LoggerFactory.getLogger(RegisterClient.class);
    private final List<NodeInformation> nodeInformations = NodeInformation.getVideoNodes();
    private final Map<NodeInformation, PlayerState> playerStates = new HashMap();
    private final int bindPort;
    public RegisterClient(Closer closer, Timer timer, int bindPort) throws IOException {
        super(closer, timer);
        this.bindPort = bindPort;
    }
    public boolean isBuffered() {
        synchronized(playerStates) {
            if (playerStates.size() != nodeInformations.size()) {
                if (LOG.isDebugEnabled()) {
                    StringBuilder str = new StringBuilder();
                    for (NodeInformation nodeInfo : nodeInformations) {
                        str.append(nodeInfo.getIP())
                           .append(" ");
                    }
                    LOG.debug("current player number : " + playerStates.size() + " " + str.toString());
                }
                return false;
            }
            for (Map.Entry<NodeInformation, PlayerState> entry : playerStates.entrySet()) {
                int bufferSize = entry.getValue().getFrameBufferSize();
                int frameBuffered = entry.getValue().getFrameBuffered();
                double ratio = (double)frameBuffered / (double) bufferSize;
                if (ratio <= NMConstants.PLAYER_BUFFER_LOWBOUND_LIMIT) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(entry.getKey().getIP() + " : frames = " + frameBuffered + ", max = "  + bufferSize);
                    }
                    return false;
                }
            }
            return true;
        }
    }
    @Override
    protected void work() {
        synchronized(playerStates) {
            playerStates.clear();
        }
        nodeInformations.stream().forEach((nodeInformation) -> {
            try (SerialStream client = new SerialStream(new Socket(nodeInformation.getIP(), bindPort))) {
                Object obj = client.read();
                if (obj != null && obj instanceof PlayerState) {
                    PlayerState state = (PlayerState)obj;
                    synchronized(playerStates) {
                        playerStates.put(nodeInformation, state);
                    }
                }
            } catch(IOException | ClassNotFoundException e) {
                LOG.error(e.getMessage() + ":" + nodeInformation.getIP());
            }
        });
    }
    @Override
    protected void clear() {
        nodeInformations.clear();
        nodeInformations.clear();
    }

    @Override
    protected void init() {
    }
}