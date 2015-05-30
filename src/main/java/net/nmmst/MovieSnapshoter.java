package net.nmmst;

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
import net.nmmst.media.Frame;
import net.nmmst.media.MovieStream;
import net.nmmst.media.Sample;
/**
 * Captures the frame of movie.
 */
public class MovieSnapshoter extends JFrame implements WindowListener {
    private static final int MOVIE_NUMBER = 4; 
    private static final int DEFAULT_WIDTH = 1024;
    private static final int DEFAULT_HEIGHT = 768;
    private static final int TIME_SCALE = 1000 * 1000;
    private final List<JTextField> pahtFields = new LinkedList();
    private final JTextField saveField
            = FusionTuner.setFont(new JTextField("D:\\"));
    private final JTextField timeField
            = FusionTuner.setFont(new JTextField(5)); 
    private final BlockingQueue<Boolean> queue = new LinkedBlockingQueue();
    private final ExecutorService service = Executors.newSingleThreadExecutor();
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
                                switch(stream.readNextType()) {
                                    case VIDEO: {
                                        Optional<Frame> frame = stream.getFrame();
                                        if (frame.isPresent()
                                            && frame.get().getTimestamp()
                                                >= getTime()) {
                                            save(frame.get().getImage(), index);
                                            end = true;
                                        }
                                        break;
                                    }
                                    case AUDIO: {
                                        Optional<Sample> sample
                                            = stream.getSample();
                                    }
                                    default:
                                        break;
                                }
                            } 
                        }
                    }
                    btn.setEnabled(true);
                }
            } catch (InterruptedException | IOException e) {
            }
        });
    }
    private int getTime() {
        return Integer.valueOf(timeField.getText()) * TIME_SCALE;
    }
    private void save(BufferedImage image, int index) {
        File f = new File(saveField.getText(), String.valueOf(index));
        try {
            ImageIO.write(image, ".jpg", f);
        } catch (IOException ex) {
        }
    }
    private List<File> getMoviePathes() {
        return pahtFields.stream()
                         .map(text -> new File(text.getText()))
                         .collect(Collectors.toCollection(ArrayList::new));
    }
    public static void main(String[] args)
        throws IOException, InterruptedException {
        JFrame frame = new MovieSnapshoter();
        SwingUtilities.invokeLater(() -> {
            frame.setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
    @Override
    public void windowOpened(WindowEvent e) {
    }
    @Override
    public void windowClosing(WindowEvent e) {
    }
    @Override
    public void windowClosed(WindowEvent e) {
        service.shutdownNow();
    }
    @Override
    public void windowIconified(WindowEvent e) {
    }
    @Override
    public void windowDeiconified(WindowEvent e) {
    }
    @Override
    public void windowActivated(WindowEvent e) {
    }
    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}
