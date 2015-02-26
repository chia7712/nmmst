package net.nmmst.controller;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.util.Pair;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.nmmst.movie.BufferFactory;
import net.nmmst.player.MediaWorker;
import net.nmmst.register.RegisterServer;
import net.nmmst.request.Request;
import net.nmmst.request.RequestServer;
import net.nmmst.request.SelectRequest;
import net.nmmst.tools.AtomicCloser;
import net.nmmst.tools.BasePanel;
import net.nmmst.tools.Closer;
import net.nmmst.tools.NMConstants;
import net.nmmst.tools.Painter;
import net.nmmst.tools.Ports;
import net.nmmst.tools.WindowsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class ControlFrame extends JFrame {
    private static final long serialVersionUID = -3141878788425623471L;
    private static final Logger LOG = LoggerFactory.getLogger(ControlFrame.class);  
    private final BufferedImage dashboardImage = Painter.loadOrStringImage(
            new File(NMConstants.CONTROLLER_DASHBOARD_JPG), 
            "Dashboard", 
            NMConstants.IMAGE_WIDTH, 
            NMConstants.IMAGE_HEIGHT, 
            NMConstants.FONT_SIZE);
    private final BufferedImage stopImage = Painter.loadOrStringImage(
            new File(NMConstants.CONTROLLER_STOP_JPG), 
            "Stop", 
            NMConstants.IMAGE_WIDTH, 
            NMConstants.IMAGE_HEIGHT,
            NMConstants.FONT_SIZE);
    private final BlockingQueue<Request> requestBuffer = BufferFactory.getRequestBuffer();
    private final Closer closer = new AtomicCloser();
    private final RequestServer requestServer = new RequestServer(closer, Ports.REQUEST_OTHERS.get());
    private final RegisterServer registerServer = new RegisterServer(closer, Ports.REGISTER.get());
    private final OvalTrigger ovalTrigger = new OvalTrigger();
    private final WheelTrigger wheelTrigger = new WheelTrigger();
    private final ControlEvent controlEvent = new ControlEvent(
            closer,
            Arrays.asList(ovalTrigger, wheelTrigger)
    );
    private final MediaWorker media = new MediaWorker(closer, null, ovalTrigger, null);
    private final CardLayout cardLayout = new CardLayout();
    private final BlockingQueue<KeyDescriptor> keyQueue = new LinkedBlockingQueue();
    private final JPanel mainPanel = new JPanel();
    private final BasePanel dashboardPanel = new BasePanel(dashboardImage, BasePanel.Mode.FILL);
    private final BasePanel stopPanel = new BasePanel(stopImage, BasePanel.Mode.FILL);
    private final SnapshotPanel snapshotPanel = new SnapshotPanel();
    private final List<BufferedImage> defaultImages = OvalInformation.getDefaultImage();
    private final List<Pair<Component, String>> compAndName = Arrays.asList(
        new Pair<Component, String>(dashboardPanel, KeyDescriptor.DASHBOARD.toString()),
        new Pair<Component, String>(media.getPanel(), KeyDescriptor.MOVIE.toString()),
        new Pair<Component, String>(stopPanel, KeyDescriptor.STOP.toString()),
        new Pair<Component, String>(snapshotPanel, KeyDescriptor.SNAPSHOTS.toString())
    );
    private final ExecutorService longTermThreadPool = Executors.newCachedThreadPool();
    public ControlFrame() throws IOException, LineUnavailableException {
        this.addKeyListener(new ButtonListener(keyQueue));
        this.add(mainPanel);
        mainPanel.setLayout(cardLayout);
        compAndName.stream().forEach((pair) -> {
            mainPanel.add(pair.getKey(), pair.getValue());
        });
        longTermThreadPool.execute(controlEvent);
        longTermThreadPool.execute(requestServer);
        longTermThreadPool.execute(registerServer);
        longTermThreadPool.execute(media);
        longTermThreadPool.execute(() -> {
            try {
                while (!closer.isClosed() && !Thread.interrupted()) {
                    KeyDescriptor key = keyQueue.take();
                    cardLayout.show(mainPanel, key.toString());
                }
            } catch(InterruptedException e){
                LOG.error(e.getMessage());
            }
        });
        longTermThreadPool.execute(() -> {
            while (!closer.isClosed() && !Thread.interrupted()) {
                try {
                    Request request = requestBuffer.take();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Request : " + request.getType().name());
                    }
                    switch (request.getType()) {
                        case ADD_SNAPSHOTS: {
                            Object obj = request.getArgument();
                            if (obj != null && obj instanceof Integer[]) {
                                Integer[] indexes = (Integer[])obj;
                                List<BufferedImage> images = new LinkedList();
                                for (Integer i : indexes) {
                                    if (i < defaultImages.size()) {
                                        images.add(defaultImages.get(i));
                                    }
                                }
                                snapshotPanel.addImages(images);
                            }
                            break;
                        }
                        case START:
                            media.setPause(false);
                            wheelTrigger.resetDecided();
                            snapshotPanel.cleanSnapshots();
                            break;
                        case STOP:
                            media.stop();
                            break;
                        case PAUSE:
                            media.setPause(true);
                            break;
                        case SELECT: {
                            Object obj = request.getArgument();
                            if (obj != null && obj instanceof SelectRequest) {
                                SelectRequest selectRequest = (SelectRequest)obj;
                                int[] indexs = selectRequest.getIndexs();
                                boolean[] values = selectRequest.getValues();
                                if (indexs.length == values.length) {
                                    for (int index = 0; index != indexs.length; ++index) {
                                        media.setMovieEnable(indexs[index], values[index]);
                                    }
                                }
                            }
                            break;
                        }
                        case REBOOT:
                            WindowsUtil.reboot();
                            break;
                        case SHUTDOWN:
                            WindowsUtil.shutdown();
                            break;
                        default:
                            break;
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Request : " + request.getType().name() + " is done");
                    }
                } catch (InterruptedException | IOException e) {
                    LOG.error(e.getMessage());
                }
            }
        });
    }
    public void setClosed(boolean value) {
        closer.close();
        longTermThreadPool.shutdownNow();
    }
    public static void main(String[] args) throws UnknownHostException, IOException, LineUnavailableException, InterruptedException {
        final ControlFrame f = new ControlFrame();
        f.setCursor(f.getToolkit().createCustomCursor(new ImageIcon("").getImage(),new Point(16, 16),""));
        f.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                f.setClosed(true);
            }
        });
        SwingUtilities.invokeLater(() -> {
            if (NMConstants.TESTS) {
                f.setSize(NMConstants.FRAME_Dimension);
            } else {
                f.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
            if (!NMConstants.TESTS) {
                f.setUndecorated(true);
            }
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setVisible(true);
        });
    }
}
