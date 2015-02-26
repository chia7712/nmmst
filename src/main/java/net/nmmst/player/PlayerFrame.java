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
import javax.sound.sampled.LineUnavailableException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.nmmst.controller.OvalInformation;
import net.nmmst.movie.BufferFactory;
import net.nmmst.processor.LinearProcessor;
import net.nmmst.processor.ProcessorFactory;
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
public class PlayerFrame extends JFrame implements Closeable {
    private static final long serialVersionUID = -3141878788425623471L;
    private static final Logger LOG = LoggerFactory.getLogger(PlayerFrame.class);  
    private final Closer closer = new AtomicCloser();
    private final BlockingQueue<Request> requestBuffer = BufferFactory.getRequestBuffer();
    private final NodeInformation playerInfomation = NodeInformation.getByAddress();
    private final SnapshotHandler snapshotHandler = new SnapshotHandler(playerInfomation.getLocation().ordinal());
    private final RequestServer requestServer = new RequestServer(closer, Ports.REQUEST_OTHERS.get());
    private final RegisterServer registerServer = new RegisterServer(closer, Ports.REGISTER.get());
    private final MediaWorker media = new MediaWorker(closer, ProcessorFactory.getSingleProcessor(playerInfomation.getLocation()), null, snapshotHandler);
    private final ExecutorService longTermThreadPool = Executors.newCachedThreadPool();
    public PlayerFrame() throws IOException, LineUnavailableException {
        add(media.getPanel());
        longTermThreadPool.execute(requestServer);
        longTermThreadPool.execute(registerServer);
        longTermThreadPool.execute(media);
        longTermThreadPool.execute(() -> {
            try {
                while (!closer.isClosed() && !Thread.interrupted()) {
                    Request request = requestBuffer.take();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Request : " + request.getType().name());
                    }
                    switch(request.getType()) {
                        case ADD_SNAPSHOTS: {
                            Object obj = request.getArgument();
                            if (obj != null && obj instanceof Integer[]) {
                                Integer[] indexes = (Integer[])obj;
                                for (Integer index : indexes) {
                                    snapshotHandler.add(index);
                                }
                            }
                            break;
                        }
                        case START:
                           media.setPause(false);
                           break;
                        case STOP: {
                            media.stop();
                            break;
                        }
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
                        case TEST: {
                            BufferedImage image = getTestImage();
                            Object obj = request.getArgument();
                            if (!(obj instanceof LinearProcessor.Format)) {
                                break;
                            }
                            LinearProcessor processor = new LinearProcessor(playerInfomation.getLocation(), (LinearProcessor.Format)obj);
                            processor.process(image);
                            media.getPanel().write(image);
                            break;
                        }
                        default:
                            break;
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Request : " + request.getType().name() + " is done");
                    }
                }
            } catch (InterruptedException | IOException e) {
                    LOG.error(e.getMessage());
            }
        });
    }
    private static BufferedImage getTestImage() {
        return Painter.getFillColor(
            NMConstants.IMAGE_WIDTH, 
            NMConstants.IMAGE_HEIGHT, 
            Color.WHITE);
    }
    @Override
    public void close() throws IOException {
        closer.close();
        longTermThreadPool.shutdownNow();
    }
    public static void main(String[] args) throws UnknownHostException, IOException, LineUnavailableException, InterruptedException  {
        final JFrame f = new PlayerFrame();
        f.setCursor(f.getToolkit().createCustomCursor(new ImageIcon("").getImage(),new Point(16, 16),""));
        SwingUtilities.invokeLater(() -> {
            if (NMConstants.TESTS) {
                f.setSize(NMConstants.FRAME_Dimension);
            } else {
                f.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
            f.requestFocusInWindow();
            if (!NMConstants.TESTS) {
                f.setUndecorated(true);
            }
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setVisible(true);
        });
    }
    private static class SnapshotHandler implements MediaWorker.EofTrigger {
        private final BufferedImage initImage = Painter.getFillColor(
                NMConstants.IMAGE_WIDTH, 
                NMConstants.IMAGE_HEIGHT, 
                Color.BLACK);
        private final List<BufferedImage> defaultImages = OvalInformation.getDefaultImage();
        private final List<Integer> indexes = new LinkedList();
        private final int indexOne;
        public SnapshotHandler(int indexOne) {
            this.indexOne = indexOne;
        }
        public void add(int index) {
            indexes.add(index);
        }
        @Override
        public void process(BasePanel panel) {
            panel.write(choseOne());
        }
        private BufferedImage choseOne() {
            if (indexOne < indexes.size()) {
                int imageIndex = indexes.get(indexOne);
                if (imageIndex < defaultImages.size()) {
                    return defaultImages.get(imageIndex);
                }
            }
            return initImage;
        }
    }
}
