package net.nmmst.register;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.nmmst.player.PlayerInformation;
import net.nmmst.tools.Closure;
import net.nmmst.tools.SerialStream;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class RegisterClient implements Closure {
    private final List<PlayerInformation> playerInformations = PlayerInformation.get();
    private final Map<PlayerInformation, PlayerState> playerStates = new HashMap();
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
                if (playerStates.size() != playerInformations.size()) {
                    System.out.println("current player number = " + playerStates.size());
                    return false;
                }
                for (Map.Entry<PlayerInformation, PlayerState> entry : playerStates.entrySet()) {
                    int bufferSize = entry.getValue().getFrameBufferSize();
                    int frameBuffered = entry.getValue().getFrameBuffered();
                    double ratio = (double)frameBuffered / (double) bufferSize;
                    System.out.println(entry.getKey() + " " + entry.getValue());
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
        for (PlayerInformation playerInformation : playerInformations) {
            SerialStream client = null;
            try {
                client = new SerialStream(new Socket(playerInformation.getIP(), bindPort));
                Object obj = client.read();
                if (obj instanceof PlayerState) {
                    PlayerState state = (PlayerState)obj;
                    synchronized(playerStates) {
                        System.out.println(playerInformation + " " + state);
                        playerStates.put(playerInformation, state);
                    }
                }
            } catch(IOException | ClassNotFoundException e) {
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Override
    public boolean isClosed() {
        return isClosed.get();
    }
	
}