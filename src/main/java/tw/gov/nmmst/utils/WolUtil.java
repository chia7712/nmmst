package tw.gov.nmmst.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Utility methods is able to wake all computers and projectors up.
 */
public final class WolUtil {
    /**
     * Log.
     */
    private static final Logger LOG = LoggerFactory.getLogger(WolUtil.class);
    /**
     * WOL port.
     */
    private static final int PORT = 80;
    /**
     * Generates the magic packet.
     * @param macNumber The mac number of node waked
     * @return A bytes array
     */
    private static byte[] generateMagicPacket(final String macNumber) {
        byte[] mac = convertToBytes(macNumber);
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
    /**
     * Wakes the computer up by mac number.
     * @param broadcast The broadcast address
     * @param macNumber Mac number
     * @return {@code true} if succeed
     */
    public static boolean wakeup(final String broadcast,
            final String macNumber) {
        LOG.info("broadcase : " + broadcast + ", mac : " + macNumber);
        byte[] bytes = generateMagicPacket(macNumber);
        if (bytes.length == 0) {
            return false;
        }
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName(broadcast);
            socket.send(new DatagramPacket(bytes, bytes.length, address, PORT));
            return true;
        } catch (IOException e) {
            LOG.error(e.getMessage());
            return false;
        }
    }
    /**
     * Converts mac string to bytes.
     * @param macNumber Mac number
     * @return A bytes array of mac number
     */
    private static byte[] convertToBytes(final String macNumber) {
        byte[] bytes = new byte[6];
        String[] hex = macNumber.split("(\\:|\\-)");
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
    /**
     * Can't be instantiated with this ctor.
     */
    private WolUtil() {
    }
}