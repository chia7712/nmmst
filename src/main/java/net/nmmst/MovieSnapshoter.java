package net.nmmst;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import net.nmmst.media.Frame;
import net.nmmst.media.MovieStream;
import net.nmmst.media.Sample;
/**
 * Captures the frame of movie.
 */
public class MovieSnapshoter {
    private static final File[] files = new File[] {
      new File("D:\\海科\\影像廳影片 - 20150417\\畫面分割輸出", "title01-cin01.mpg"),
      new File("D:\\海科\\影像廳影片 - 20150417\\畫面分割輸出", "title01-cin02.mpg"),
      new File("D:\\海科\\影像廳影片 - 20150417\\畫面分割輸出", "title01-cin03.mpg"),
      new File("D:\\海科\\影像廳影片 - 20150417\\畫面分割輸出", "title01-cin04.mpg")
    };
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 1) {
            throw new IOException("<save time>");
        }
        final long saveTime = Long.valueOf(args[0]);
        ExecutorService service = Executors.newFixedThreadPool(files.length);
        int index = 1;
        for (File f : files) {
            final int localIndex = index;
            ++index;
            service.execute(() -> {
                try {
                    Saver saver = new Saver("D:\\");
                    MovieStream stream = new MovieStream(f, 0);
                    boolean end = false;
                    while (!end) {
                        switch(stream.readNextType()) {
                            case VIDEO: {
                                Optional<Frame> frame = stream.getFrame();
                                if (frame.isPresent()
                                    && frame.get().getTimestamp() >= saveTime) {
                                    System.out.println("save : "
                                            + saver.save(frame.get(),
                                                    String.valueOf(localIndex)));
                                    end = true;
                                }
                                break;
                            }
                            case AUDIO: {
                                Optional<Sample> sample = stream.getSample();
                            }
                            default:
                                break;
                        }
                    }    
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }
        service.shutdown();
        service.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        System.exit(0);
    }
    private static class Saver {
        private final String rootDir;
        public Saver(String rootDir) throws IOException {
            File dir = new File(rootDir);
            if (!dir.exists()) {
                dir.mkdir();
            }
            this.rootDir = rootDir;
        }
        public File save(Frame frame, String filename) throws IOException {
            if (frame != null && frame.getImage() != null) {
                File saveFile = new File(rootDir, filename + ".jpg");
                if (ImageIO.write(frame.getImage(), "jpg", saveFile)) {
                    return saveFile;
                }
            }
            return null;
        }
    }
}
