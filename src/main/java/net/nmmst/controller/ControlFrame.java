package net.nmmst.controller;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.util.Pair;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.nmmst.movie.BufferFactory;
import net.nmmst.movie.MovieAttribute;
import net.nmmst.movie.MovieBuffer;
import net.nmmst.movie.MovieOrder;
import net.nmmst.movie.MovieReader;
import net.nmmst.player.PanelThread;
import net.nmmst.player.Speaker;
import net.nmmst.player.SpeakerThread;
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
    private final BufferedImage	initImage = Painter.getFillColor(
            NMConstants.IMAGE_WIDTH, 
            NMConstants.IMAGE_HEIGHT, 
            Color.BLACK);
    private final MovieOrder movieOrder = MovieOrder.get();
    private final BasicPanel moviePanel = new BasicPanel(initImage);
    private final Speaker speaker = new Speaker(getAudioFormat(movieOrder.getMovieAttribute()));
    private final MovieBuffer buffer = BufferFactory.getMovieBuffer();
    private final BlockingQueue<Request> requestBuffer = BufferFactory.getRequestBuffer();
    private final RequestServer requestServer = new RequestServer(Ports.REQUEST.get());
    private final OvalTrigger ovalTrigger = new OvalTrigger();
    private final WheelTrigger wheelTrigger = new WheelTrigger();
    private final RegisterServer registerServer = new RegisterServer(Ports.REGISTER.get());
    private final CardLayout cardLayout = new CardLayout();
    private final List<BackedRunner> backedRunners = new LinkedList();
    private final BlockingQueue<KeyDescriptor> keyQueue = new LinkedBlockingQueue();
    private final JPanel mainPanel = new JPanel();
    private final BasicPanel dashboardPanel = new BasicPanel(dashboardImage, BasicPanel.Mode.FILL);
    private final BasicPanel stopPanel = new BasicPanel(stopImage, BasicPanel.Mode.FILL);
    private final SnapshotPanel snapshotPanel = new SnapshotPanel();
    private final List<Pair<Component, String>> compAndName = Arrays.asList(
        new Pair<Component, String>(dashboardPanel, KeyDescriptor.DASHBOARD.toString()),
        new Pair<Component, String>(moviePanel, KeyDescriptor.MOVIE.toString()),
        new Pair<Component, String>(stopPanel, KeyDescriptor.STOP.toString()),
        new Pair<Component, String>(snapshotPanel, KeyDescriptor.SNAPSHOTS.toString())
    );
    private ExecutorService worker;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final Runnable[] longTermThreads = {
        requestServer, 
        new Runnable() {
            @Override
            public void run() {
                try {
                    while (!closed.get()) {
                        TimeUnit.SECONDS.sleep(NMConstants.CHECK_PERIOD);
                        int count = 0;
                        for (BackedRunner backedRunner : backedRunners) {
                            if (backedRunner.isClosed()) {
                                ++count;
                            }
                        }
                        if (count == backedRunners.size()) {
                            try {
                                init();
                            } catch (IOException e) {
                                LOG.error(e.getMessage());
                            }
                        }
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("frame size = " + buffer.getFrameSize() + ", sample size = " + buffer.getSampleSize());
                        }
                    }
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage());
                }
            }
        },
        new ExecuteThread(),
        registerServer,
        new Runnable() {
            @Override
            public void run() {
                try {
                    while (!closed.get()) {
                        KeyDescriptor key = keyQueue.take();
                        switch(key) {
                            case SNAPSHOTS:
                                snapshotPanel.setOvalInformations(ovalTrigger.getSnapshots());
                                break;
                            default:
                                break;
                        }
                        cardLayout.show(mainPanel, key.toString());
                    }
                } catch(InterruptedException e){
                    LOG.error(e.getMessage());
                }
            }
        },
        new ControlEvent(Arrays.asList(ovalTrigger, wheelTrigger))
    };
    private final ExecutorService longTermPool = Executors.newFixedThreadPool(longTermThreads.length);
    public ControlFrame() throws IOException, LineUnavailableException {
        this.addKeyListener(new ButtonListener(keyQueue));
        this.add(mainPanel);
        mainPanel.setLayout(cardLayout);
        for (Pair<Component, String> pair : compAndName) {
            mainPanel.add(pair.getKey(), pair.getValue());
        }
        init();
        for (Runnable runnable : longTermThreads) {
            longTermPool.execute(runnable);
        }
    }
    private void init() throws IOException {
        buffer.clear();
        moviePanel.write(initImage);
        buffer.setPause(true);
        movieOrder.reset();
        backedRunners.clear();
        backedRunners.add(new SpeakerThread(speaker));
        backedRunners.add(new PanelThread(moviePanel, ovalTrigger));
        backedRunners.add(new MovieReader(movieOrder));
        worker = Executors.newFixedThreadPool(backedRunners.size());
        for (BackedRunner backedRunner : backedRunners) {
            worker.execute(backedRunner);
        }
    }
    private static AudioFormat getAudioFormat(MovieAttribute[] attributes) {
        for (MovieAttribute attribute : attributes) {
            return attribute.getAutioFormat();
        }
        return null;
    }
    private class ExecuteThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Request request = requestBuffer.take();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Request : " + request.getType().name());
                    }
                    switch (request.getType()) {
                        case START:
                            if (buffer.isPause()) {
                                buffer.setPause(false);
                            }
                            break;
                        case STOP:
                            for (BackedRunner backedRunner : backedRunners) {
                                backedRunner.close();
                            }
                            worker.shutdownNow();
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
                        default:
                            break;
                    }
                } catch (InterruptedException | IOException e) {
                    LOG.error(e.getMessage());
                }
            }
        }
    }
    public void setClosed(boolean value) {
        closed.set(value);
    }
    public static void main(String[] args) throws UnknownHostException, IOException, LineUnavailableException, InterruptedException {
        final ControlFrame f = new ControlFrame();
//        f.setCursor(f.getToolkit().createCustomCursor(new ImageIcon("").getImage(),new Point(16, 16),""));
        f.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                f.setClosed(true);
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (NMConstants.TESTS) {
                    f.setSize(NMConstants.FRAME_Dimension);
                } else {
                    f.setExtendedState(JFrame.MAXIMIZED_BOTH);
                }
                f.setUndecorated(true);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
            }
        });
    }
}
