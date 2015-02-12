package net.nmmst.test;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.nmmst.player.NodeInformation;
import net.nmmst.processor.BourkeProcessor;
import net.nmmst.tools.BasicPanel;
import net.nmmst.tools.Painter;
import net.nmmst.tools.Ports;
import net.nmmst.tools.SerialStream;
import net.nmmst.tools.WindowsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class PlayerTest extends JFrame {
    private static final long serialVersionUID = -2016746022673317548L;
    private static final Logger LOG = LoggerFactory.getLogger(PlayerTest.class);
    private final BasicPanel panel = new BasicPanel(Painter.getFillColor(1920, 1080, Color.WHITE));
    private final ServerSocket server = new ServerSocket(Ports.TEST.get());
    public PlayerTest(final NodeInformation nodeInformation) throws IOException {
        this.add(panel);
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    SerialStream stream = null;
                    try {
                        stream = new SerialStream(server.accept());
                        Object obj = stream.read();
                        if (obj instanceof BourkeProcessor.Format) {
                            BourkeProcessor processor = new BourkeProcessor(nodeInformation.getLocation(), (BourkeProcessor.Format)obj);
                            BufferedImage testImage = Painter.getFillColor(1920, 1080, Color.WHITE);
                            processor.process(testImage);
                            panel.write(testImage);
                        }
                        if (obj instanceof Boolean) {
                            Boolean b = (Boolean)obj;
                            if (b) {
                                WindowsUtil.reboot();
                            } else {
                                BufferedImage testImage = Painter.getFillColor(1920, 1080, Color.WHITE);
                                panel.write(testImage);
                            }
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        LOG.error(e.getMessage());
                    } finally {
                        if (stream != null) {
                            stream.close();
                        }
                    }
                }

            }

        });
    }
    public static void main(String[] args) throws IOException {
        final JFrame f = new PlayerTest(getPlayerLocationa());
        f.setCursor(f.getToolkit().createCustomCursor(new ImageIcon("").getImage(),new Point(16, 16),""));
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                    f.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    f.requestFocusInWindow();
                    f.setUndecorated(true);
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f.setVisible(true);
            }
        });
    }
    private static NodeInformation getPlayerLocationa() throws UnknownHostException {
        String localIP = InetAddress.getLocalHost().getHostAddress();
        for (NodeInformation nodeInformation : NodeInformation.getPrimaryVideoNodes()) {
            if (nodeInformation.getLocation() != NodeInformation.Location.CENTER && nodeInformation.getIP().compareToIgnoreCase(localIP) == 0) {
                return nodeInformation;
            }
        }
        throw new IllegalArgumentException();
    }
}
