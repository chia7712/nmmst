package tw.gov.nmmst;

import tw.gov.nmmst.app.FusionTuner;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tw.gov.nmmst.media.Frame;
import tw.gov.nmmst.media.MovieStream;
import tw.gov.nmmst.media.Sample;
/**
 * Captures the frame of movie.
 */
public class MovieSnapshoter extends JFrame implements WindowListener {
    /**
     * Log.
     */
    private static final Log LOG
            = LogFactory.getLog(MovieSnapshoter.class);
    /**
     * Total number of movies.
     */
    private static final int MOVIE_NUMBER = 4;
    /**
     * Frame width.
     */
    private static final int DEFAULT_WIDTH = 1024;
    /**
     * Frame height.
     */
    private static final int DEFAULT_HEIGHT = 768;
    /**
     * Time scale. For example, the value "1000 * 1000" represents
     * microsecond.
     */
    private static final int TIME_SCALE = 1000 * 1000;
    /**
     * The fields for filling in movie pathes.
     */
    private final List<JTextField> pahtFields = new LinkedList();
    /**
     * The field for filling in directory path.
     */
    private final JTextField saveField
            = FusionTuner.setFont(new JTextField("D:\\"));
    /**
     * The field for filling in the snapshot moment.
     */
    private final JTextField timeField
            = FusionTuner.setFont(new JTextField(5));
    /**
     * Queues the request for snapshoting frame from movies.
     */
    private final BlockingQueue<Boolean> queue = new LinkedBlockingQueue();
    /**
     * Thread pool.
     */
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    /**
     * Constructs a movie snapshotter.
     */
    public MovieSnapshoter() {
        final JButton btn = FusionTuner.setFont(new JButton("Start"));
        btn.addActionListener(event -> {
            queue.offer(true);
            btn.setEnabled(false);
        });
        setLayout(new GridLayout(0, 1));
        for (int index = 0; index != MOVIE_NUMBER; ++index) {
            JTextField text = FusionTuner.setFont(new JTextField());
            pahtFields.add(text);
            add(text);
        }
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 0));
        panel.add(btn);
        panel.add(saveField);
        panel.add(timeField);
        add(panel);
        service.execute(() -> {
            try {
                while (!Thread.interrupted()) {
                    queue.take();
                    List<File> files = getMoviePathes();
                    List<Runnable> threads = new LinkedList();
                    for (int index = 0; index != files.size(); ++index) {
                        try (MovieStream stream = new MovieStream(
                                files.get(index), index)) {
                            boolean end = false;
                            while (!end) {
                                switch (stream.readNextType()) {
                                    case VIDEO:
                                        Optional<Frame> frame
                                            = stream.getFrame();
                                        if (frame.isPresent()
                                            && frame.get().getTimestamp()
                                                >= getTime()) {
                                            save(frame.get().getImage(),
                                                    index);
                                            end = true;
                                        }
                                        break;
                                    case AUDIO:
                                        Optional<Sample> sample
                                            = stream.getSample();
                                    default:
                                        break;
                                }
                            }
                        }
                    }
                    btn.setEnabled(true);
                }
            } catch (InterruptedException | IOException e) {
                LOG.error(e);
            }
        });
    }
    /**
     * Gets the snapshot moment.
     * @return The snapshot moment
     */
    private int getTime() {
        return Integer.valueOf(timeField.getText()) * TIME_SCALE;
    }
    /**
     * Saves the frame.
     * @param image The image to save
     * @param index The movie index
     */
    private void save(final BufferedImage image, final int index) {
        File f = new File(saveField.getText(), String.valueOf(index));
        try {
            ImageIO.write(image, ".jpg", f);
        } catch (IOException e) {
            LOG.error(e);
        }
    }
    /**
     * Retrieves the movie pathes.
     * @return A list of movie path
     */
    private List<File> getMoviePathes() {
        return pahtFields.stream()
                         .map(text -> new File(text.getText()))
                         .collect(Collectors.toCollection(ArrayList::new));
    }
    /**
     * Invokes a JFrame which is used for snapshoting frame of movie.
     * @param args No use
     * @throws IOException If failed to open movie file
     */
    public static void main(final String[] args)
        throws IOException {
        JFrame frame = new MovieSnapshoter();
        SwingUtilities.invokeLater(() -> {
            frame.setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
    @Override
    public void windowOpened(final WindowEvent e) {
    }
    @Override
    public void windowClosing(final WindowEvent e) {
    }
    @Override
    public final void windowClosed(final WindowEvent e) {
        service.shutdownNow();
    }
    @Override
    public void windowIconified(final WindowEvent e) {
    }
    @Override
    public void windowDeiconified(final WindowEvent e) {
    }
    @Override
    public void windowActivated(final WindowEvent e) {
    }
    @Override
    public void windowDeactivated(final WindowEvent e) {
    }
}
