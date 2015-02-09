package net.nmmst.master;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.nmmst.movie.BufferFactory;
import net.nmmst.player.PlayerInformation;
import net.nmmst.register.RegisterClient;
import net.nmmst.request.Request;
import net.nmmst.request.RequestServer;
import net.nmmst.request.SelectRequest;
import net.nmmst.tools.BasicPanel;
import net.nmmst.tools.Ports;
import net.nmmst.tools.SerialStream;
import net.nmmst.tools.WOL;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class MasterFrame extends JFrame {
    private static final long serialVersionUID = -4475933827111956737L;
    private static final String rootPath = "D:\\海科圖片\\";
    private final BufferedImage startImage = ImageIO.read(new File(rootPath + "m_start.jpg"));
    private final BufferedImage stopImage = ImageIO.read(new File(rootPath + "m_stop.jpg"));
    private final BufferedImage refreshImage = ImageIO.read(new File(rootPath + "m_refresh.jpg"));
    private final BufferedImage backgroundImage = ImageIO.read(new File(rootPath + "m_background_all.jpg"));
    private final BufferedImage testImage = ImageIO.read(new File(rootPath + "m_test.jpg"));
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
    private final JButton[] pairBtns = {
        deviceInitBtn,
        shurdownBtn,
        wakeupBtn,
        party1Btn,
        party2Btn,
        lightOffBtn
    };
    private final Request[] pairRequests = {
        new Request(Request.Type.INIT),
        new Request(Request.Type.SHUTDOWN),
        new Request(Request.Type.WOL),
        new Request(Request.Type.PARTY1),
        new Request(Request.Type.PARTY2),
        new Request(Request.Type.LIGHT_OFF)
    };
    private final BlockingQueue<Request>  requestBuffer = BufferFactory.getRequestBuffer();
    private final RequestServer requestServer = new RequestServer(Ports.REQUEST.get());
    private final RegisterClient registerClient = new RegisterClient(Ports.REGISTER.get());
    private final AtomicBoolean close = new AtomicBoolean(false);
    private final DioAction dioAction = new DioAction();
    private final PlayerInformation[] playerInformations = PlayerInformation.get();
    private final Runnable[] longTermThreads = {
        requestServer,
        new ExecutorRequest(),
        registerClient
    };
    private final ExecutorService longTermThreadsPool = Executors.newFixedThreadPool(longTermThreads.length);
    public MasterFrame() throws Exception {
        for (Runnable runnable : longTermThreads) {
            longTermThreadsPool.execute(runnable);
        }
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
        for (int index = 0; index != pairBtns.length; ++index) {
            final int index_ = index;
            pairBtns[index].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    requestBuffer.offer(pairRequests[index_]);
                }
            });
            backgroundPanel.add(pairBtns[index]);
        }
    }
    private class ExecutorRequest implements Runnable {
        @Override
        public void run() {
            final AtomicBoolean start = new AtomicBoolean(false);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            while(!close.get()) {
                try  {
                    Request request = requestBuffer.take();
                    System.out.println(request.getType());
                    switch(request.getType()) {
                        case START:
                            if (!start.get()) {
                                start.set(true);
                                executor = Executors.newSingleThreadExecutor();
                                executor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Happen.start(playerInformations, registerClient, dioAction);
                                        } catch (IOException | InterruptedException e) {
                                            e.printStackTrace();
                                        } finally {
                                            start.set(false);
                                        }
                                    }
                                });
                            }
                            break;
                        case STOP:
                            if(start.get()) {
                                executor.shutdownNow();
                                dioAction.light_off();
                                SerialStream.sendAll(playerInformations, new Request(Request.Type.STOP), Ports.REQUEST.get());
                            }
                            break;
                        case SELECT:
                            if(request.getArgument() instanceof SelectRequest) {
                                SelectRequest selectRequest = (SelectRequest)request.getArgument();
                                int[] indexs = selectRequest.getIndexs();
                                boolean[] values = selectRequest.getValues();
                                if(indexs.length == values.length) {
                                    for (int index = 0; index != indexs.length; ++index) {
                                        Happen.setValues(indexs[index], values[index]);
                                    }
                                    SerialStream.sendAll(playerInformations, new Request(Request.Type.SELECT, selectRequest), Ports.REQUEST.get());
                                }
                            }
                            break;
                        case REBOOT:
                            SerialStream.sendAll(playerInformations, new Request(Request.Type.REBOOT), Ports.REQUEST.get());
                            break;
                        case INIT:
                            dioAction.stoneToLeft();
                            dioAction.light_work();
                            dioAction.initializeSubmarineAndGray();
                            break;
                        case LIGHT_OFF:
                            dioAction.light_off();
                            break;
                        case PARTY1:
                            //don't touch gray??
                            dioAction.grayUpToEnd();
                            dioAction.stoneToRight();
                            dioAction.light_party1();
                            break;
                        case PARTY2:
                            //don't touch gray??
                            dioAction.grayUpToEnd();
                            dioAction.stoneToRight();
                            dioAction.light_party2();
                            break;
                        case SHUTDOWN:
                            SerialStream.sendAll(playerInformations, new Request(Request.Type.SHUTDOWN), Ports.REQUEST.get());
                            break;
                        case WOL:
                            for (PlayerInformation playerInformation : playerInformations) {
//                                WOL.wakeup("192.168.100.255", playerInformation.getMac());
//                                System.out.println("192.168.100.255 " + playerInformation.getMac());
                                WOL.wakeup(PlayerInformation.getBroadCast(), playerInformation.getMac());
                                System.out.println(PlayerInformation.getBroadCast() + " " + playerInformation.getMac());
                            }
                            break;
                        default:
                            break;
                    }
                    System.out.println(request.getType() + " over");
                } catch (InterruptedException | IOException e)  {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    requestBuffer.clear();
                }
            }
        }
    }
    public static void main(String[] args) throws Exception {
        final JFrame f = new MasterFrame();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //f.setSize(new Dimension(600, 600));
                f.setExtendedState(JFrame.MAXIMIZED_BOTH);
                f.setUndecorated(true);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
            }
        });
    }
}
