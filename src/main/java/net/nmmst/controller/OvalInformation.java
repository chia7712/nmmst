package net.nmmst.controller;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.nmmst.tools.NMConstants;
import net.nmmst.tools.Painter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class OvalInformation implements Comparable<OvalInformation> {
    private static final Logger LOG = LoggerFactory.getLogger(OvalInformation.class);
    private static final List<String> DEFAULT_IMAGE_PATH = new ArrayList(
            Arrays.asList(
                    "D://海科圖片//1.jpg",
                    "D://海科圖片//2.jpg",
                    "D://海科圖片//3.jpg",
                    "D://海科圖片//4.jpg",
                    "D://海科圖片//5.jpg",
                    "D://海科圖片//6.jpg",
                    "D://海科圖片//7.jpg",
                    "D://海科圖片//8.jpg",
                    "D://海科圖片//9.jpg",
                    "D://海科圖片//10.jpg",
                    "D://海科圖片//11.jpg",
                    "D://海科圖片//12.jpg",
                    "D://海科圖片//13.jpg",
                    "D://海科圖片//14.jpg"
                    )
    );
    private static final List<BufferedImage> DEFAULT_IMAGE = loadDefaultImage();
    private static List<BufferedImage> loadDefaultImage() {
        List<BufferedImage> images = new LinkedList();
        DEFAULT_IMAGE_PATH.stream().forEach((imagePath) -> {
            images.add(Painter.loadOrStringImage(new File(imagePath), 640, 480, NMConstants.FONT_SIZE));
        });
        return images;
    }
    public static final List<BufferedImage> getDefaultImage() {
        return DEFAULT_IMAGE;
    }
    private final int number;
    private final int movieIndex;
    private final long minMicroTime;
    private final long maxMicroTime;
    private final int diameter;
    private final int x;
    private final int y;
    private final BufferedImage	snapshotImage;
    public static List<OvalInformation> get() {
        List<OvalInformation> ovalInformations = getFromFile();
        return ovalInformations == null || ovalInformations.isEmpty() ? 
                getDefault() 
                : ovalInformations;
    }
    //index mintime maxtime diameter x y snapshotPath
    private static List<OvalInformation> getFromFile() {
        File configurationFile = new File(NMConstants.CONTROLLER_OVAL_INFORMATION);
        if (!configurationFile.exists()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(configurationFile))) {
            Set<OvalInformation> ovalInformations = new HashSet();
            int number = 0;
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                String[] args = line.split(" ");
                if (args.length != 7) {
                    continue;
                }
                ovalInformations.add(new OvalInformation(
                    number++,
                    Integer.valueOf(args[0]),
                    Long.valueOf(args[1]),
                    Long.valueOf(args[2]),
                    Integer.valueOf(args[3]),
                    Integer.valueOf(args[4]),
                    Integer.valueOf(args[5]),
                    Painter.loadOrStringImage(new File(args[6]), NMConstants.IMAGE_WIDTH, NMConstants.IMAGE_HEIGHT, NMConstants.FONT_SIZE)));
            }
            return new ArrayList(ovalInformations);
        } catch (IOException | NumberFormatException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }
    private static List<OvalInformation> getDefault() {
        List<OvalInformation> ovalInformations = new LinkedList();
        final int[] movieIndexes = new int[]{
          0,
          1,
          2,
          3,
          4,
          5,
          6
        };
        final int selectNumber = 3;
        final int duration = 3 * 1000 * 1000;
        final int ovalNumber = 3;
        for (int movieIndex : movieIndexes) {
            for (int selectIndex = 0; selectIndex != selectNumber; ++selectIndex) {
                for (int ovalIndex = 0; ovalIndex != ovalNumber; ++ovalIndex) {
                    int number = (int) (Math.random() * DEFAULT_IMAGE.size());
                    ovalInformations.add(new OvalInformation(
                        number,
                        movieIndex,
                        duration * (selectIndex + 1),
                        duration * (selectIndex + 2),
                        100,
                        (int)(Math.random() * 1000),
                        (int)(Math.random() * 1000),
                        DEFAULT_IMAGE.get(number)));
                }
            }
        }
        return new ArrayList(ovalInformations);
    }
    private OvalInformation(int number, int movieIndex, long minMicroTime, long maxMicroTime, int diameter, int x, int y, BufferedImage snapshotImage) {
        this.number = number;
        this.movieIndex = movieIndex;
        this.minMicroTime = Math.min(minMicroTime, maxMicroTime);
        this.maxMicroTime = Math.max(minMicroTime, maxMicroTime);
        this.diameter = diameter;
        this.x = x;
        this.y = y;
        this.snapshotImage = snapshotImage;
    }
    public int getNumber() {
        return number;
    }
    public int getMovieIndex() {
        return movieIndex;
    }
    public long getMinMicroTime() {
        return minMicroTime;
    }
    public long getMaxMicroTime() {
        return maxMicroTime;
    }
    public int getDiameter() {
        return diameter;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public BufferedImage getImage() {
        return snapshotImage;
    }
    @Override
    public int compareTo(OvalInformation other) {
        int rval = Integer.compare(movieIndex, other.getMovieIndex());
        if (rval != 0) {
            return rval;
        }
        rval = Long.compare(minMicroTime, other.getMinMicroTime());
        if (rval != 0) {
            return rval;
        }
        rval = Long.compare(maxMicroTime, other.getMaxMicroTime());
        if (rval != 0) {
            return rval;
        }
        rval = Integer.compare(x, other.getX());
        if (rval != 0) {
            return rval;
        }
        rval = Integer.compare(y, other.getY());
        if (rval != 0) {
            return rval;
        }
        return Integer.compare(diameter, other.getDiameter());
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof OvalInformation) {
            OvalInformation dst = (OvalInformation)obj;
            return (movieIndex == dst.getMovieIndex())
                    && (minMicroTime == dst.getMinMicroTime())
                    && (maxMicroTime == dst.getMaxMicroTime())
                    && (diameter == dst.getDiameter())
                    && (x == dst.getX())
                    && (y == dst.getY());
        }
        return false;
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        return str.append(movieIndex)
           .append(" ")
           .append(minMicroTime)
           .append(" ")
           .append(maxMicroTime)
           .append(" ")
           .append(diameter)
           .append(" ")
           .append(x)
           .append(" ")
           .append(y).toString();
    }
}
