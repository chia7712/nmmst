package net.nmmst.master;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.util.Pair;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.nmmst.movie.BufferFactory;
import net.nmmst.player.NodeInformation;
import net.nmmst.register.RegisterClient;
import net.nmmst.request.Request;
import net.nmmst.request.RequestServer;
import net.nmmst.request.SelectRequest;
import net.nmmst.tools.BasicPanel;
import net.nmmst.tools.NMConstants;
import net.nmmst.tools.Painter;
import net.nmmst.tools.Ports;
import net.nmmst.tools.SerialStream;
import net.nmmst.tools.WOL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class MasterFrame extends JFrame implements Closeable {
    private static final long serialVersionUID = -4475933827111956737L;
    private static final Logger LOG = LoggerFactory.getLogger(MasterFrame.class);  
    private static final String rootPath = NMConstants.MASTER_ROOT_PATH;
    private final BufferedImage startImage = Painter.loadOrStringImage(new File(rootPath + "//m_start.jpg"), "start", NMConstants.IMAGE_WIDTH, NMConstants.IMAGE_HEIGHT, NMConstants.FONT_SIZE);
//            ImageIO.read(new File(rootPath + "//m_start.jpg"));
    private final BufferedImage stopImage = Painter.loadOrStringImage(new File(rootPath + "//m_stop.jpg"), "stop", NMConstants.IMAGE_WIDTH, NMConstants.IMAGE_HEIGHT, NMConstants.FONT_SIZE);
//            ImageIO.read(new File(rootPath + "//m_stop.jpg"));
    private final BufferedImage refreshImage = Painter.loadOrStringImage(new File(rootPath + "//m_refresh.jpg"), "refresh", NMConstants.IMAGE_WIDTH, NMConstants.IMAGE_HEIGHT, NMConstants.FONT_SIZE);
//            ImageIO.read(new File(rootPath + "//m_refresh.jpg"));
    private final BufferedImage backgroundImage = Painter.loadOrStringImage(new File(rootPath + "//m_background_all.jpg"), "background", NMConstants.IMAGE_WIDTH, NMConstants.IMAGE_HEIGHT, NMConstants.FONT_SIZE);
//            ImageIO.read(new File(rootPath + "//m_background_all.jpg"));
    private final BufferedImage testImage = Painter.loadOrStringImage(new File(rootPath + "//test.jpg"), "refresh", NMConstants.IMAGE_WIDTH, NMConstants.IMAGE_HEIGHT, NMConstants.FONT_SIZE);
//            ImageIO.read(new File(rootPath + "//m_test.jpg"));
    private final BasicPanel startPanel = new BasicPanel(startImage, BasicPanel.Mode.FILL);
    private final BasicPanel stopPanel = new BasicPanel(stopImage, BasicPanel.Mode.FILL);
    private final BasicPanel refreshPanel = new BasicPanel(refreshImage, BasicPanel.Mode.FILL);
    private final BasicPanel backgroundPanel = new BasicPanel(backgroundImage, BasicPanel.Mode.FILL);
    private final BasicPanel testPanel = new BasicPanel(testImage, BasicPanel.Mode.FILL);
    private final JButton deviceInitBtn = new JButton("展演準備");
    private final JButton shurdownBtn = new JButton("睡覺");
    private final JButton wakeupBtn = new JButton("起床");
    private final JButton party1Btn = new JButton("宴會一");
    private final JButton party2Btn = new JButton("宴會二");
    private final JButton lightOffBtn = new JButton("關閉燈光");
    private final List<Pair<JButton, Request>> btnAndReq = Arrays.asList(
            new Pair<>(deviceInitBtn, new Request(Request.Type.INIT)),
            new Pair<>(shurdownBtn, new Request(Request.Type.SHUTDOWN)),
            new Pair<>(wakeupBtn, new Request(Request.Type.WOL)),
            new Pair<>(party1Btn, new Request(Request.Type.PARTY1)),
            new Pair<>(party2Btn, new Request(Request.Type.PARTY2)),
            new Pair<>(lightOffBtn, new Request(Request.Type.LIGHT_OFF))
    );
//    private final JButton[] pairBtns = {
//        deviceInitBtn,
//        shurdownBtn,
//        wakeupBtn,
//        party1Btn,
//        party2Btn,
//        lightOffBtn
//    };
//    private final Request[] pairRequests = {
//        new Request(Request.Type.INIT),
//        new Request(Request.Type.SHUTDOWN),
//        new Request(Request.Type.WOL),
//        new Request(Request.Type.PARTY1),
//        new Request(Request.Type.PARTY2),
//        new Request(Request.Type.LIGHT_OFF)
//    };
    private final BlockingQueue<Request>  requestBuffer = BufferFactory.getRequestBuffer();
    private final RequestServer requestServer = new RequestServer(Ports.REQUEST.get());
    private final RegisterClient registerClient = new RegisterClient(Ports.REGISTER.get());
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final DioInterface dio = DioFactory.getTest();
    private final List<NodeInformation> nodeInformations = NodeInformation.getVideoNodes();
    private final Runnable[] longTermThreads = {
        requestServer,
        new ExecutorRequest(),
        registerClient
    };
    private final Closeable[] closeables = {
        dio,
        registerClient,
        requestServer
    };
    private final ExecutorService longTermThreadsPool = Executors.newFixedThreadPool(longTermThreads.length);
    public MasterFrame() throws Exception {
        add(backgroundPanel);
        backgroundPanel.setLayout(new FlowLayout());
        startPanel.setPreferredSize(new Dimension(400, 400));
        startPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent arg0) {
                requestBuffer.offer(new Request(Request.Type.START));
            }
        });
        stopPanel.setPreferredSize(new Dimension(100, 100));
        stopPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent arg0) {
                requestBuffer.offer(new Request(Request.Type.STOP));
            }
        });
        refreshPanel.setPreferredSize(new Dimension(100, 100));
        refreshPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent arg0) {
                requestBuffer.offer(new Request(Request.Type.REBOOT));
            }
        });
        testPanel.setPreferredSize(new Dimension(100, 100));
        testPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent arg0)  {
                //requestBuffer.offer(new Request(Request.Type.TEST_1));
            }
        });
        backgroundPanel.add(startPanel);
        backgroundPanel.add(stopPanel);
        backgroundPanel.add(refreshPanel);
        backgroundPanel.add(testPanel);
        for (Pair<JButton, Request> pair : btnAndReq) {
            final JButton btn = pair.getKey();
            final Request req = pair.getValue();
            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    requestBuffer.offer(req);
                }
            });
            backgroundPanel.add(btn);
        }
//        for (int index = 0; index != pairBtns.length; ++index) {
//            final int indexTmp = index;
//            pairBtns[index].addActionListener(new ActionListener() {
//                @Override
//                public void actionPerformed(ActionEvent arg0) {
//                    requestBuffer.offer(pairRequests[indexTmp]);
//                }
//            });
//            backgroundPanel.add(pairBtns[index]);
//        }
        for (Runnable runnable : longTermThreads) {
            longTermThreadsPool.execute(runnable);
        }
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
    private class ExecutorRequest implements Runnable {
        @Override
        public void run() {
            final AtomicBoolean start = new AtomicBoolean(false);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            while (!closed.get()) {
                try  {
                    Request request = requestBuffer.take();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Request : " + request.getType().name());
                    }
                    
                    switch(request.getType()) {
                        case START:
                            if (!start.get()) {
                                start.set(true);
                                executor = Executors.newSingleThreadExecutor();
                                executor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Happen.start(nodeInformations, registerClient, dio);
                                        } catch (IOException | InterruptedException e) {
                                            LOG.error(e.getMessage());
                                        } finally {
                                            start.set(false);
                                        }
                                    }
                                });
                            }
                            break;
                        case STOP:
                            if (start.get()) {
                                executor.shutdownNow();
                                dio.lightOff();
                                SerialStream.sendAll(nodeInformations, new Request(Request.Type.STOP), Ports.REQUEST.get());
                            }
                            break;
                        case SELECT:
                            if (request.getArgument() instanceof SelectRequest) {
                                SelectRequest selectRequest = (SelectRequest)request.getArgument();
                                int[] indexs = selectRequest.getIndexs();
                                boolean[] values = selectRequest.getValues();
                                if (indexs.length == values.length) {
                                    for (int index = 0; index != indexs.length; ++index) {
                                        Happen.setValues(indexs[index], values[index]);
                                    }
                                    SerialStream.sendAll(nodeInformations, new Request(Request.Type.SELECT, selectRequest), Ports.REQUEST.get());
                                }
                            }
                            break;
                        case REBOOT:
                            SerialStream.sendAll(nodeInformations, new Request(Request.Type.REBOOT), Ports.REQUEST.get());
                            break;
                        case INIT:
                            dio.stoneGotoLeft();
                            dio.lightWork();
                            dio.initializeSubmarineAndGray();
                            break;
                        case LIGHT_OFF:
                            dio.lightOff();
                            break;
                        case PARTY1:
                            //don't touch gray??
                            dio.grayUptoEnd();
                            dio.stoneGotoRight();
                            dio.lightParty(1);
                            break;
                        case PARTY2:
                            //don't touch gray??
                            dio.grayUptoEnd();
                            dio.stoneGotoRight();
                            dio.lightParty(2);
                            break;
                        case SHUTDOWN:
                            SerialStream.sendAll(nodeInformations, new Request(Request.Type.SHUTDOWN), Ports.REQUEST.get());
                            break;
                        case WOL:
                            for (NodeInformation nodeInformation : nodeInformations) {
                                WOL.wakeup(NodeInformation.getBroadCast(), nodeInformation.getMac());
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug(NodeInformation.getBroadCast() + " " + nodeInformation.getMac());
                                }
                            }
                            break;
                        default:
                            break;
                    }
                } catch (InterruptedException | IOException e)  {
                    LOG.error(e.getMessage());
                } finally {
                    requestBuffer.clear();
                }
            }
        }
    }
    public static void main(String[] args) throws Exception {
        final MasterFrame f = new MasterFrame();
        f.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent ex) {
                try {
                    f.close();
                } catch (IOException e) {
                    LOG.error(e.getMessage());
                }
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
