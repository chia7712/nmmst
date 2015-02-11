package net.nmmst.controller;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] args = line.split(" ");
                if (args.length != 7) {
                    continue;
                }
                ovalInformations.add(new OvalInformation(
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
        for (int index = 0; index != 7; ++index) {
            final int selectNumber = (int)(Math.random() * 3) + 1;
            final int duration = 3 * 1000 * 1000;
            for (int selectIndex = 0; selectIndex != selectNumber; ++selectIndex) {
                final int ovalNumber = (int)(Math.random() * 3) + 1;
                for (int ovalIndex = 0; ovalIndex != ovalNumber; ++ovalIndex) {
                    ovalInformations.add(new OvalInformation(
                        index,
                        duration * (selectIndex + 1),
                        duration * (selectIndex + 2),
                        100,
                        (int)(Math.random() * 1000),
                        (int)(Math.random() * 1000),
                        Painter.getStringImage(index + " " + selectIndex + " " + ovalIndex, 500, 500, 100)));
                }

            }
        }
        return new ArrayList(ovalInformations);
    }
    private OvalInformation(int movieIndex, long minMicroTime, long maxMicroTime, int diameter, int x, int y, BufferedImage snapshotImage) {
        this.movieIndex = movieIndex;
        this.minMicroTime = Math.min(minMicroTime, maxMicroTime);
        this.maxMicroTime = Math.max(minMicroTime, maxMicroTime);
        this.diameter = diameter;
        this.x = x;
        this.y = y;
        this.snapshotImage = Painter.process(snapshotImage);
    }
    public int getIndex() {
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
    public OvalInformation copyOf() {
        return new OvalInformation(movieIndex, minMicroTime, maxMicroTime, diameter, x, y, snapshotImage);
    }
    @Override
    public int compareTo(OvalInformation arg0) {
        return x == arg0.x ? 0 : x > arg0.x ? 1 : -1;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof OvalInformation) {
            OvalInformation dst = (OvalInformation)obj;
            return (movieIndex == dst.getIndex())
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
