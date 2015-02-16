package net.nmmst.master;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
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
import java.util.concurrent.TimeUnit;
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
import net.nmmst.tools.AtomicCloser;
import net.nmmst.tools.BasePanel;
import net.nmmst.tools.BaseTimer;
import net.nmmst.tools.Closer;
import net.nmmst.tools.NMConstants;
import net.nmmst.tools.Painter;
import net.nmmst.tools.Ports;
import net.nmmst.tools.ProjectorUtil;
import net.nmmst.tools.SerialStream;
import net.nmmst.tools.WolUtil;
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
    private final BufferedImage testImage = Painter.loadOrStringImage(new File(rootPath + "//m_test.jpg"), "test", NMConstants.IMAGE_WIDTH, NMConstants.IMAGE_HEIGHT, NMConstants.FONT_SIZE);
//            ImageIO.read(new File(rootPath + "//m_test.jpg"));
    private final BufferedImage retestImage = Painter.getStringImage("還原測試", 400, 200, 100);
    private final BasePanel startPanel = new BasePanel(startImage, BasePanel.Mode.FILL);
    private final BasePanel stopPanel = new BasePanel(stopImage, BasePanel.Mode.FILL);
    private final BasePanel refreshPanel = new BasePanel(refreshImage, BasePanel.Mode.FILL);
    private final BasePanel backgroundPanel = new BasePanel(backgroundImage, BasePanel.Mode.FILL);
    private final BasePanel testPanel = new BasePanel(testImage, BasePanel.Mode.FILL);
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
    private final Closer closer = new AtomicCloser();
    private final BlockingQueue<Request>  requestBuffer = BufferFactory.getRequestBuffer();
    private final RequestServer requestServer = new RequestServer(closer, Ports.REQUEST_MASTER.get());
    private final RegisterClient registerClient = new RegisterClient(closer, new BaseTimer(TimeUnit.SECONDS, 1), Ports.REGISTER.get());
    private final DioInterface dio = DioFactory.getDefault();
    private final List<NodeInformation> videoNodes = NodeInformation.getVideoNodes();
    private final ExecutorService longTermThreadsPool = Executors.newCachedThreadPool();
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
            private boolean move = false;
            @Override
            public void mouseReleased(MouseEvent arg0)  {
                try {
                    if (move) {
                        dio.initializeSubmarineAndGray();
                        move = false;
                        testPanel.write(testImage);
                    } else {
                        dio.submarineGotoEnd();
                        move = true;
                        testPanel.write(retestImage);
                    }
                } catch(InterruptedException e) {
                    LOG.error(e.getMessage());
                }
            }
        });
        backgroundPanel.add(startPanel);
        backgroundPanel.add(stopPanel);
        backgroundPanel.add(refreshPanel);
        backgroundPanel.add(testPanel);
        for (Pair<JButton, Request> pair : btnAndReq) {
            final JButton btn = pair.getKey();
            final Request req = pair.getValue();
            btn.addActionListener((ActionEvent arg0) -> {
                requestBuffer.offer(req);
            });
            backgroundPanel.add(btn);
        }
        longTermThreadsPool.execute(requestServer);
        longTermThreadsPool.execute(registerClient);
        longTermThreadsPool.execute(() -> {
            AtomicBoolean start = new AtomicBoolean(false);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            while (!closer.isClosed()) {
                try  {
                    Request request = requestBuffer.take();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Request : " + request.getType().name());
                    }
                    switch(request.getType()) {
                        case ADD_SNAPSHOTS: {
                            Object obj = request.getArgument();
                            if (obj != null && obj instanceof Integer[]) {
                                SerialStream.sendAll(videoNodes, new Request(Request.Type.ADD_SNAPSHOTS, (Integer[])obj), Ports.REQUEST_OTHERS.get());
                            }
                            break;
                        }
                        case START:
                            if (start.compareAndSet(false, true)) {
                                executor = Executors.newSingleThreadExecutor();
                                executor.execute(() -> {
                                    try {
                                        Happen.start(videoNodes, registerClient, dio);
                                    } catch (IOException | InterruptedException e) {
                                        LOG.error(e.getMessage());
                                    } finally {
                                        start.set(false);
                                    }
                                });
                            } else {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("already starting");
                                }
                            }
                            break;
                        case STOP: {
                            if (start.get()) {
                                executor.shutdownNow();
                                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
                            } else {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("already stoping");
                                }
                            }
                            dio.lightOff();
                            SerialStream.sendAll(videoNodes, new Request(Request.Type.STOP), Ports.REQUEST_OTHERS.get());
                            break;
                        }
                        case SELECT: {
                            Object obj = request.getArgument();
                            if (obj != null && obj instanceof SelectRequest) {
                                SelectRequest selectRequest = (SelectRequest)obj;
                                int[] indexs = selectRequest.getIndexs();
                                boolean[] values = selectRequest.getValues();
                                if (indexs.length == values.length) {
                                    for (int index = 0; index != indexs.length; ++index) {
                                        Happen.setValues(indexs[index], values[index]);
                                    }
                                    SerialStream.sendAll(videoNodes, new Request(Request.Type.SELECT, selectRequest), Ports.REQUEST_OTHERS.get());
                                }
                            }
                            break;
                        }
                        case REBOOT:
                            SerialStream.asynSendAll(videoNodes, new Request(Request.Type.REBOOT), Ports.REQUEST_OTHERS.get());
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
                            SerialStream.asynSendAll(videoNodes, new Request(Request.Type.SHUTDOWN), Ports.REQUEST_OTHERS.get());
                            ProjectorUtil.switchAllMachine(false);
                            break;
                        case WOL:
                            for (NodeInformation nodeInformation : videoNodes) {
                                WolUtil.wakeup(NodeInformation.getBroadCast(), nodeInformation.getMac());
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug(NodeInformation.getBroadCast() + " " + nodeInformation.getMac());
                                }
                            }
                            ProjectorUtil.switchAllMachine(true);
                            break;
                        default:
                            break;
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Request : " + request.getType().name() + " is done");
                    }
                } catch (InterruptedException | IOException e)  {
                    LOG.error(e.getMessage());
                } finally {
                    requestBuffer.clear();
                }
            }
        });
    }
    @Override
    public void close() throws IOException {
        closer.close();
        dio.close();
        longTermThreadsPool.shutdownNow();
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
