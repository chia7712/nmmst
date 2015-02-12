package net.nmmst.player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.nmmst.tools.NMConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class NodeInformation implements Comparable<NodeInformation> {
    private static final Logger LOG = LoggerFactory.getLogger(NodeInformation.class);
    public enum Location{LU, RU, LD, RD, CENTER, MASTER};
    private final Location location;
    private final String ip;
    private final String mac;
    private static List<NodeInformation> get() {
        List<NodeInformation> nodeInformations = getFromFile();
            return nodeInformations == null || nodeInformations.isEmpty() ? 
                    getDefault() 
                    : nodeInformations;
    }
    public static List<NodeInformation> getPrimaryVideoNodes() {
        Set<Location> locations = new TreeSet();
        locations.add(Location.LU);
        locations.add(Location.RU);
        locations.add(Location.LD);
        locations.add(Location.RD);
        return get(locations);
    }
    public static NodeInformation getMasterNode() {
        return get(Location.MASTER);
    }
    public static List<NodeInformation> getVideoNodes() {
        Set<Location> locations = new TreeSet();
        locations.add(Location.LU);
        locations.add(Location.RU);
        locations.add(Location.LD);
        locations.add(Location.RD);
        locations.add(Location.CENTER);
        return get(locations);
    }
    private static List<NodeInformation> get(Set<Location> locations) {
        List<NodeInformation> nodeInformations = new ArrayList(locations.size());
        for (NodeInformation nodeInformation : get()) {
            if (locations.contains(nodeInformation.getLocation())) {
                nodeInformations.add(nodeInformation);
            }
        }
        return nodeInformations;
    }
    private static NodeInformation get(Location location) {
        for (NodeInformation nodeInformation : get()) {
            if (nodeInformation.getLocation() == location) {
                return nodeInformation;
            }
        }
        throw new RuntimeException("Cannot find location : " + location);
    }
    public static String getBroadCast() {
        return "192.168.100.255";
    }
    private static List<NodeInformation> getDefault() {
        return Arrays.asList(
            new NodeInformation(NodeInformation.Location.LU, "192.168.100.1",  "00-0B-AB-6D-7D-25"),
            new NodeInformation(NodeInformation.Location.RU, "192.168.100.2",  "00-0B-AB-67-4E-83"),
            new NodeInformation(NodeInformation.Location.LD, "192.168.100.3",  "00-0B-AB-67-4E-70"),
            new NodeInformation(NodeInformation.Location.RD, "192.168.100.4",  "00-0B-AB-67-4E-75"),
            new NodeInformation(NodeInformation.Location.CENTER, "192.168.100.38", "00-0B-AB-67-4E-7F"),
            new NodeInformation(NodeInformation.Location.MASTER, "192.168.100.31", "00-0B-AB-67-4E-7F"));
    }
    private static String getModifiedIP(String ip) {
        String[] args = ip.split("\\.");
        if (args.length != 4) {
            return new String();
        }
        int[] argsInt = new int[4];
        try {
            argsInt[0] = Integer.parseInt(args[0]);
            argsInt[1] = Integer.parseInt(args[1]);
            argsInt[2] = Integer.parseInt(args[2]);
            argsInt[3] = Integer.parseInt(args[3]);
        } catch(NumberFormatException e) {
            LOG.error(e.getMessage());
            return new String();
        }
        return argsInt[0] + "." +
                argsInt[1] + "." + 
                argsInt[2] + "." + 
                argsInt[3];

    }
    //location ip mac
    private static List<NodeInformation> getFromFile() {
        File configurationFile = new File(NMConstants.PLAYER_INFORMATION);
        if (!configurationFile.exists()) {
            return new ArrayList();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(configurationFile))){
            Set<NodeInformation> nodeInformations = new TreeSet();
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] args = line.split(" ");
                if (args.length != 3) {
                    continue;
                }
                NodeInformation.Location location = null;
                for (NodeInformation.Location loc : NodeInformation.Location.values()) {
                    if (loc.toString().compareToIgnoreCase(args[0]) == 0) {
                        location = loc;
                        break;
                    }
                }
                if (location == null) {
                    continue;
                }
                String ip = getModifiedIP(args[1]);
                if (ip.length() == 0) {
                    continue;
                }
                nodeInformations.add(new NodeInformation(location, ip, args[2]));
            }
            if (nodeInformations.size() != Location.values().length) {
                return null;
            }
            return new ArrayList(nodeInformations);
        } catch (IOException e) {
            return null;
        }
    }
    public NodeInformation(Location location, String ip, String mac) {
        this.location = location;
        this.ip = ip;
        this.mac = mac;
    }
    public Location getLocation() {
        return location;
    }
    public String getIP() {
        return ip;
    }
    public String getMac() {
        return mac;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof NodeInformation) {
            NodeInformation nodeInformation = (NodeInformation)obj;
            if (nodeInformation.location == location 
                    && nodeInformation.ip.compareToIgnoreCase(ip) == 0 
                    && nodeInformation.mac.compareToIgnoreCase(mac) == 0) {
                return true;
            }
        }
        return false;
    }
    @Override
    public String toString() {
        return location + " " + ip + " " + mac;
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    @Override
    public int compareTo(NodeInformation other) {
        int rval = location.compareTo(other.getLocation());
        if (rval != 0) {
            return rval;
        }
        rval = ip.compareTo(other.getIP());
        if (rval != 0) {
            return rval;
        }
        return mac.compareTo(other.getMac());
    }
}
