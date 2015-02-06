package net.nmmst.player;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
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
import net.nmmst.tools.Closure;
import net.nmmst.tools.Painter;
import net.nmmst.tools.Ports;
import net.nmmst.tools.WindowsFunctions;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class PlayerFrame extends JFrame {
    private static final long serialVersionUID = -3141878788425623471L;
    private final BufferedImage initImage = Painter.fillColor(1920, 1080, Color.BLACK);
    private final MovieOrder movieOrder = MovieOrder.getDefaultMovieOrder();
    private final BasicPanel panel = new BasicPanel(initImage);
    private final Speaker speaker = new Speaker(getAudioFormat(movieOrder.getMovieAttribute()));
    private final MovieBuffer buffer = BufferFactory.getMovieBuffer();
    private final BlockingQueue<Request> requestBuffer = BufferFactory.getRequestBuffer();
    private final RequestServer requestServer = new RequestServer(Ports.REQUEST.get());
    private final RegisterServer registerServer = new RegisterServer(Ports.REGISTER.get());
    private final Runnable[] longTermThreads = {
        requestServer, 
        new CheckThread(),
        new ExecuteRequest(),
        registerServer
    };
    private final ExecutorService longTermThreadsPool = Executors.newFixedThreadPool(longTermThreads.length);
    private final List<Closure> closures = new LinkedList();
    private final PlayerInformation playerInfomation;
    private ExecutorService shortTermThreadsPool;
    public PlayerFrame(PlayerInformation playerInfomation) throws IOException, LineUnavailableException {
        this.playerInfomation = playerInfomation;
        add(panel);
        init();
        for(Runnable runnable : longTermThreads) {
            longTermThreadsPool.execute(runnable);
        }
    }
    private void init() throws IOException {
        System.out.println("init");
        buffer.clear();
        panel.write(initImage);
        buffer.setPause(true);
        movieOrder.reset();
        shortTermThreadsPool = Executors.newFixedThreadPool(3);
        closures.clear();
        closures.add(new SpeakerThread(speaker));
        closures.add(new PanelThread(panel));
        closures.add(new MovieReader(movieOrder, ProcessorFactory.newTwoTierProcessor(playerInfomation.getLocation())));
        for(Closure closure : closures) {
            shortTermThreadsPool.execute(closure);
        }
        System.out.println("init over");
    }
    private static AudioFormat getAudioFormat(MovieAttribute[] attributes) {
        for(MovieAttribute attribute : attributes) {
            return attribute.getAutioFormat();
        }
        return null;
    }
    private class ExecuteRequest implements Runnable {
        @Override
        public void run() {
            while(true) {
                try {
                    Request request = requestBuffer.take();
                    System.out.println(request.getType());
                    switch(request.getType()) {
                        case START:
                            if(buffer.isPause()) {
                                buffer.setPause(false);
                            }
                            break;
                        case STOP:
                            for(Closure closure : closures) {
                                closure.close();
                            }
                            shortTermThreadsPool.shutdownNow();
                            break;
                        case PAUSE:
                            buffer.setPause(true);
                            break;
                        case SELECT:
                            if(request.getArgument() instanceof SelectRequest) {
                                SelectRequest selectRequest = (SelectRequest)request.getArgument();
                                int[] indexs = selectRequest.getIndexs();
                                boolean[] values = selectRequest.getValues();
                                if(indexs.length == values.length) {
                                    for(int index = 0; index != indexs.length; ++index) {
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
                        case TEST_1: {
                            BufferedImage image = getTestImage(true);
                            if(image == null) {
                                break;
                            }
                            Object obj = request.getArgument();
                            if(!(obj instanceof LinearProcessor.Format)) {
                                break;
                            }
                            LinearProcessor processor = new LinearProcessor(playerInfomation.getLocation(), (LinearProcessor.Format)obj);
                            processor.process(image);
                            panel.write(image);
                            break;
                        }    
                        case TEST_2: {
                            BufferedImage image = getTestImage(false);
                            if(image == null) {
                                break;
                            }
                            Object obj = request.getArgument();
                            if(!(obj instanceof LinearProcessor.Format)) {
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
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    private static BufferedImage getTestImage(boolean mode) {
        try {
            File file = new File(mode ? "D:\\海科影片\\test.jpg" : "D:\\海科影片\\test2.jpg");
            return ImageIO.read(file);
        } catch (IOException ex) {
            return null;
        }
    }
    private class CheckThread implements Runnable {
        @Override
        public void run() {
            while(true) {
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                int count = 0;
                for(Closure closure : closures) {
                    if(closure.isClosed()) {
                        ++count;
                    }
                }
                if(count == closures.size()) {
                    try {
                        init();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }

    }


    public static void main(String[] args) throws UnknownHostException, IOException, LineUnavailableException, InterruptedException  {
        final JFrame f = new PlayerFrame(getPlayerLocationa());
        f.setCursor(f.getToolkit().createCustomCursor(new ImageIcon("").getImage(),new Point(16, 16),""));
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //f.setSize(new Dimension(600, 600));
                f.setExtendedState(JFrame.MAXIMIZED_BOTH);
                f.requestFocusInWindow();
                f.setUndecorated(true);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
            }
        });

    }
    private static PlayerInformation getPlayerLocationa() throws UnknownHostException {
        String localIP = InetAddress.getLocalHost().getHostAddress();
        for(PlayerInformation playerInformation : PlayerInformation.get()) {
            if(playerInformation.getLocation() != PlayerInformation.Location.CENTER && playerInformation.getIP().compareTo(localIP) == 0) {
                return playerInformation;
            }
        }
        throw new IllegalArgumentException();
    }
}
