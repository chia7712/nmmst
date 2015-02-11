package net.nmmst.player;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.nmmst.movie.BufferFactory;
import net.nmmst.movie.MovieAttribute;
import net.nmmst.movie.MovieBuffer;
import net.nmmst.movie.MovieOrder;
import net.nmmst.movie.MovieReader;
import net.nmmst.processor.LinearProcessor;
import net.nmmst.processor.ProcessorFactory;
import net.nmmst.register.RegisterServer;
import net.nmmst.request.Request;
import net.nmmst.request.RequestServer;
import net.nmmst.request.SelectRequest;
import net.nmmst.tools.BasicPanel;
import net.nmmst.tools.BackedRunner;
import net.nmmst.tools.NMConstants;
import net.nmmst.tools.Painter;
import net.nmmst.tools.Ports;
import net.nmmst.tools.WindowsFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class PlayerFrame extends JFrame implements Closeable {
    private static final long serialVersionUID = -3141878788425623471L;
    private static final Logger LOG = LoggerFactory.getLogger(PlayerFrame.class);  
    private final BufferedImage initImage = Painter.getFillColor(NMConstants.IMAGE_WIDTH, NMConstants.IMAGE_HEIGHT, Color.BLACK);
    private final MovieOrder movieOrder = MovieOrder.get();
    private final BasicPanel panel = new BasicPanel(initImage);
    private final Speaker speaker = new Speaker(getAudioFormat(movieOrder.getMovieAttribute()));
    private final MovieBuffer buffer = BufferFactory.getMovieBuffer();
    private final BlockingQueue<Request> requestBuffer = BufferFactory.getRequestBuffer();
    private final RequestServer requestServer = new RequestServer(Ports.REQUEST.get());
    private final RegisterServer registerServer = new RegisterServer(Ports.REGISTER.get());
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final Runnable[] longTermThreads = {
        requestServer, 
        new Runnable() {
            @Override
            public void run() {
                try {
                    while (!closed.get() && !Thread.interrupted()) {
                        TimeUnit.SECONDS.sleep(NMConstants.CHECK_PERIOD);
                        int count = 0;
                        for (BackedRunner backedRunner : backRunners) {
                            if (backedRunner.isClosed()) {
                                ++count;
                            }
                        }
                        if (count == backRunners.size()) {
                            init();
                        }
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("frame size = " + buffer.getFrameSize() + ", sample size = " + buffer.getSampleSize());
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    LOG.error(e.getMessage());
                    throw new RuntimeException(e);
                } finally {
                    for (BackedRunner backedRunner : backRunners) {
                        backedRunner.close();
                    }
                }
            }
        },
        new ExecuteRequest(),
        registerServer
    };
    private final Closeable[] closeables = {
        requestServer,
        registerServer,
        speaker
    };
    private final ExecutorService longTermThreadsPool = Executors.newFixedThreadPool(longTermThreads.length);
    private final List<BackedRunner> backRunners = new LinkedList();
    private final NodeInformation playerInfomation;
    private ExecutorService shortTermThreadsPool;
    public PlayerFrame(NodeInformation playerInfomation) throws IOException, LineUnavailableException {
        this.playerInfomation = playerInfomation;
        add(panel);
        init();
        for (Runnable runnable : longTermThreads) {
            longTermThreadsPool.execute(runnable);
        }
    }
    private void init() throws IOException {
        buffer.clear();
        panel.write(initImage);
        buffer.setPause(true);
        movieOrder.reset();
        shortTermThreadsPool = Executors.newFixedThreadPool(3);
        backRunners.clear();
        backRunners.add(new SpeakerThread(speaker));
        backRunners.add(new PanelThread(panel));
        backRunners.add(new MovieReader(movieOrder, ProcessorFactory.getSingleProcessor(playerInfomation.getLocation())));
        for (BackedRunner backRunner : backRunners) {
            shortTermThreadsPool.execute(backRunner);
        }
    }
    private static AudioFormat getAudioFormat(MovieAttribute[] attributes) {
        for (MovieAttribute attribute : attributes) {
            return attribute.getAutioFormat();
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        closed.set(true);
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch(IOException e) {
                LOG.error(e.getMessage());
            }
        }
    }
    private class ExecuteRequest implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Request request = requestBuffer.take();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Request : " + request.getType().name());
                    }
                    switch(request.getType()) {
                        case START:
                            if (buffer.isPause()) {
                                buffer.setPause(false);
                            }
                            break;
                        case STOP:
                            for (BackedRunner backRunner : backRunners) {
                                backRunner.close();
                            }
                            shortTermThreadsPool.shutdownNow();
                            break;
                        case PAUSE:
                            buffer.setPause(true);
                            break;
                        case SELECT:
                            if (request.getArgument() instanceof SelectRequest) {
                                SelectRequest selectRequest = (SelectRequest)request.getArgument();
                                int[] indexs = selectRequest.getIndexs();
                                boolean[] values = selectRequest.getValues();
                                if (indexs.length == values.length) {
                                    for (int index = 0; index != indexs.length; ++index) {
                                        movieOrder.setEnable(indexs[index], values[index]);
                                    }
                                }
                            }
                            break;
                        case REBOOT:
                            WindowsFunctions.reboot();
                            break;
                        case SHUTDOWN:
                            WindowsFunctions.shutdown();
                            break;
                        case TEST: {
                            BufferedImage image = getTestImage();
                            Object obj = request.getArgument();
                            if (!(obj instanceof LinearProcessor.Format)) {
                                break;
                            }
                            LinearProcessor processor = new LinearProcessor(playerInfomation.getLocation(), (LinearProcessor.Format)obj);
                            processor.process(image);
                            panel.write(image);
                            break;
                        }
                        default:
                            break;
                    }
                } catch (InterruptedException | IOException e) {
                    LOG.error(e.getMessage());
                }
            }
        }
    }
    private static BufferedImage getTestImage() {
        return Painter.getFillColor(NMConstants.IMAGE_WIDTH, NMConstants.IMAGE_HEIGHT, Color.WHITE);
    }
    public static void main(String[] args) throws UnknownHostException, IOException, LineUnavailableException, InterruptedException  {
        final JFrame f = new PlayerFrame(getNodeLocationa());
        f.setCursor(f.getToolkit().createCustomCursor(new ImageIcon("").getImage(),new Point(16, 16),""));
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (NMConstants.TESTS) {
                    f.setSize(NMConstants.FRAME_Dimension);
                } else {
                    f.setExtendedState(JFrame.MAXIMIZED_BOTH);
                }
                f.requestFocusInWindow();
                f.setUndecorated(true);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
            }
        });
    }
    private static NodeInformation getNodeLocationa() throws UnknownHostException {
        String localIP = getLocalAddress();
        for (NodeInformation nodeInformation : NodeInformation.getPrimaryVideoNodes()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("nodeInformation : " + nodeInformation.toString());
            }
            if (nodeInformation.getIP().compareToIgnoreCase(localIP) == 0) {
                return nodeInformation;
            }
        }
        throw new IllegalArgumentException();
    }
    private static String getLocalAddress() throws UnknownHostException {
//        return InetAddress.getLocalHost().getHostAddress();
        return "192.168.11.15";
    }
}
