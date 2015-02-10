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
import net.nmmst.tools.NMConstants;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class PlayerInformation {
    public enum Location{LU, RU, LD, RD, CENTER};
    private final Location location;
    private final String ip;
    private final String mac;
    public static List<PlayerInformation> get() {
        List<PlayerInformation> playerInformations = getFromFile();
            return playerInformations == null || playerInformations.isEmpty() ? 
                    getDefault() 
                    : playerInformations;
    }
    public static String getBroadCast() {
        return "192.168.100.255";
    }
    private static List<PlayerInformation> getDefault() {
        return Arrays.asList(
            new PlayerInformation(PlayerInformation.Location.LU, "192.168.100.1",  "00-0B-AB-6D-7D-25"),
            new PlayerInformation(PlayerInformation.Location.RU, "192.168.100.2",  "00-0B-AB-67-4E-83"),
            new PlayerInformation(PlayerInformation.Location.LD, "192.168.100.3",  "00-0B-AB-67-4E-70"),
            new PlayerInformation(PlayerInformation.Location.RD, "192.168.100.4",  "00-0B-AB-67-4E-75"),
            new PlayerInformation(PlayerInformation.Location.CENTER, "192.168.100.38", "00-0B-AB-67-4E-7F"));
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
            return new String();
        }
        return argsInt[0] + "." +
                argsInt[1] + "." + 
                argsInt[2] + "." + 
                argsInt[3] + ".";

    }
    //location ip mac
    private static List<PlayerInformation> getFromFile() {
        File configurationFile = new File(NMConstants.PLAYER_INFORMATION);
        if (!configurationFile.exists()) {
            return new ArrayList();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(configurationFile))){
            Set<PlayerInformation> playerInformations = new HashSet();
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] args = line.split(" ");
                if (args.length != 3) {
                    continue;
                }
                PlayerInformation.Location location = null;
                for (PlayerInformation.Location loc : PlayerInformation.Location.values()) {
                    if (loc.toString().compareTo(args[0]) == 0) {
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
                playerInformations.add(new PlayerInformation(location, ip, args[2]));
            }
            if (playerInformations.size() != 5) {
                return null;
            }
            return new ArrayList(playerInformations);
        } catch (IOException e) {
            return null;
        }
    }
    public PlayerInformation(Location location, String ip, String mac) {
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
        if (obj instanceof PlayerInformation) {
            PlayerInformation playerInformation = (PlayerInformation)obj;
            if (playerInformation.location == location || playerInformation.ip.compareTo(ip) == 0 || playerInformation.mac.compareTo(mac) == 0) {
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
}
