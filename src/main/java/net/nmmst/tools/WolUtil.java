package net.nmmst.tools;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class WolUtil {
    private static final Logger LOG = LoggerFactory.getLogger(WolUtil.class);
    private static final int PORT = 80;
    private static byte[] genMagicPacket(String macStr) {
        byte[] mac = getMacBytes(macStr);
        if (mac.length == 0) {
            return mac;
        }
        byte[] bytes = new byte[6 + 16 * mac.length];
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) 0xff;
        }
        for (int i = 6; i < bytes.length; i += mac.length) {
            System.arraycopy(mac, 0, bytes, i, mac.length);
        }
        return bytes;
    }
    public static boolean wakeup(String broadcase, String macStr) {
        byte[] bytes = genMagicPacket(macStr);
        if (bytes.length == 0) {
            return false;
        }
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName(broadcase);
            socket.send(new DatagramPacket(bytes, bytes.length, address, PORT));
            return true;
        } catch (IOException e) {
            LOG.error(e.getMessage());
            return false;
        }
        
    }
    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            return new byte[0];
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            LOG.error(e.getMessage());
            return new byte[0];
        }
        return bytes;
    }
    private WolUtil(){}
}
