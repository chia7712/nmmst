package net.nmmst.test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import net.nmmst.movie.Frame;
import net.nmmst.movie.MovieStream;
import net.nmmst.movie.Sample;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class Benchmarker {
    private static org.slf4j.Logger LOG = LoggerFactory.getLogger(Benchmarker.class);
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IOException("<movie path> <pic path>");
        }
        MovieStream stream = new MovieStream(args[0], 0);
        Counter videoCount = new Counter("VIDEO");
        Counter audioCount = new Counter("AUDIO");
        Reporter reporter = new Reporter(Arrays.asList(videoCount, audioCount));
        Saver saver = new Saver(args[1]);
        Executors.newSingleThreadExecutor().execute(reporter);
        while (true) {
            switch(stream.readNextType()) {
                case VIDEO: {
                    final long startTime = System.nanoTime();
                    Frame frame = stream.getFrame();
                    final long endTime = System.nanoTime();
                    videoCount.add((endTime - startTime) / 1000000);
                    saver.save(frame);
                    break;
                }
                case AUDIO: {
                    final long startTime = System.nanoTime();
                    Sample sample = stream.getSample();
                    final long endTime = System.nanoTime();
                    audioCount.add((endTime - startTime) / 1000000);     
                }
                case EOF:
                    break;
                default:
                    break;
            }
        }               
    }
    private static class Saver {
        private final String rootDir;
        private int count;
        public Saver(String rootDir) throws IOException {
            File dir = new File(rootDir);
            if (!dir.exists()) {
                dir.mkdir();
                this.rootDir = rootDir;
                return;
            }
            throw new IOException(rootDir + " cann't be existed");
                
        }
        public void save(Frame frame) throws IOException {
            if (frame != null && frame.getImage() != null && random(frame)) {
                ImageIO.write(frame.getImage(), "jpg", new File(rootDir, frame.getTimestamp() + ".jpg"));
            }
        }
        private boolean random(Frame frame) {
            ++count;
            return count == 1000;
        }
    }
    private static class Reporter implements Runnable {
        private final int period = 1000;
        private final List<Counter> counters = new LinkedList();
        public Reporter(List<Counter> counters) {
            this.counters.addAll(counters);
        }
        @Override
        public void run() {
            while (true) {
                try {
                    TimeUnit.MILLISECONDS.sleep(period);
                    StringBuilder str = new StringBuilder();
                    for (Counter counter : counters) {
                        str.append(counter.toString()).append("\t");
                    }
                    System.out.print("\r" + str.toString());
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage());
                }
            }
        }
    }
    private static class Counter {
        private long count;
        private long spendTime;
        private final String name;
        public Counter(String name) {
            this.name = name;
        }
        public synchronized void add(long spendTime) {
            ++count;
            this.spendTime += spendTime;
        }
        public synchronized double average() {
            return (double)spendTime / (double)count;
        }
        @Override
        public String toString() {
            return name + "\t" + average() + "\tms";
        }
    }
}
