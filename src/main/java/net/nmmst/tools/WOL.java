package net.nmmst.tools;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class WOL {
    private static final int PORT = 80;
    private static byte[] genMagicPacket(String mac_str) {
        byte[] mac = getMacBytes(mac_str);
        if(mac.length == 0) {
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
    public static boolean wakeup(String broadcase, String mac_str) {
        byte[] bytes = genMagicPacket(mac_str);
        if(bytes.length == 0) {
            return false;
        }
        DatagramSocket socket = null;
        try {
            InetAddress address = InetAddress.getByName(broadcase);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
            socket = new DatagramSocket();
            socket.send(packet);
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if(socket != null) {
                socket.close();
            }
        }
        return false;
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
            return new byte[0];
        }
        return bytes;
    }
    private WOL(){}
}
