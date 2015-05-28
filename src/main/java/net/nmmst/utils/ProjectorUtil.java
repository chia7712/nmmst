package net.nmmst.utils;

import net.nmmst.NProperties;
import net.nmmst.NodeInformation;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Utility methods helps for waking specified project up.
 */
public final class ProjectorUtil {
    private static final Logger LOG
            = LoggerFactory.getLogger(ProjectorUtil.class);  
    private static final int TCP_PORT = 0xAAA0;
    private static final int UDP_PORT = 0xA001;
    private static final byte[] PACKET_DATA = {0x3F};
    public static boolean[] enableAllMachine(final NProperties properties,
            final boolean enable) throws IOException {
        Collection<NodeInformation> nodeInformations
                = NodeInformation.getProjectors(properties);
        boolean[] rval = new boolean[nodeInformations.size()];
        int index = 0;
        for (NodeInformation projector : nodeInformations) {
            rval[index++] = enableMachine(projector.getIP(), enable);
        }
        return rval;
    } 
    public static boolean enableMachine(final String address,
            final boolean enable) throws IOException {
        try (ProjectorConnection connection = new ProjectorConnection(address)) {
            byte[][] data = {
              getEnableLampCommand(enable),
              getEnableProjectorCommand(enable)
            };
            for (byte[] d : data) {
                connection.writeBytes(d, 0, d.length);
                if (!connection.readAcknowledge() || !connection.readReply()) {
                    return false;
                }
            }
            return true;
        }
    }
//    public static String discoverDevices() throws IOException {
//        try (DatagramSocket socket = new DatagramSocket()) {
//            InetAddress address = InetAddress.getByName(
//                    NodeInformation.getBroadCast());
//            socket.send(new DatagramPacket(
//                    PACKET_DATA,
//                    PACKET_DATA.length,
//                    address, UDP_PORT));
//            DatagramPacket recvPacket = new DatagramPacket(
//                    new byte[1024],
//                    1024,
//                    address,
//                    UDP_PORT);
//            socket.receive(recvPacket);
//            return new String(recvPacket.getData(),
//                              recvPacket.getOffset(),
//                              recvPacket.getLength());
//        }
//    }
     private static byte[] getEnableProjectorCommand(boolean enable) {
        byte[] buffer = new byte[8];
        // device address
        buffer[1] = 0x00;
        // answer prefix (1)
        buffer[2] = 0x00;
        // answer prefix (2)
        buffer[3] = 0x03;
        // answer prefix data (send result)
        buffer[4] = 0x02;
        // data byte (0x66 switch projector off, 0x65 switch projector on)
        buffer[5] = (byte) (enable ? 0x65 : 0x66);
        buffer[6] = generateChecksum(buffer);
        buffer[0] = (byte) 0xFE;
        buffer[7] = (byte) 0xFF;
        return buffer;
    }
    private static byte[] getEnableLampCommand(boolean enable) {
        byte[] buffer = new byte[10];
        // device address
        buffer[1] = 0x00;
        // answer prefix (1)
        buffer[2] = 0x00;
        // answer prefix (2)
        buffer[3] = 0x03;
        // answer prefix data (send result)
        buffer[4] = 0x02;
        // command byte (1)
        buffer[5] = 0x76;
        // command byte (2)
        buffer[6] = 0x1A;
        // data byte (0x00 switch lamp off, 0x01 switch lamp on)
        buffer[7] = (byte) (enable ? 0x01 : 0x00);
        buffer[8] = generateChecksum(buffer);
        buffer[0] = (byte) 0xFE;
        buffer[9] = (byte) 0xFF;
        return buffer;
    }
    /**
     * Generates checksum for specified data.
     * @param data The data need the checksum 
     * @return Checksum, a byte
     */
    private static byte generateChecksum(byte[] data) {
        int sum = 0;
        for (byte b : data) {
            sum += b;
        }
        return (byte) (sum % 256);
    }
    private static class ProjectorConnection implements Closeable {
        private final Socket connection;
        private final OutputStream out;
        private final InputStream in;
        public ProjectorConnection(String address) throws IOException {
            connection = new Socket(address, TCP_PORT);
            
            try {
                out = connection.getOutputStream();
                in = connection.getInputStream();
                LOG.info("connect success : " + address + ":" + TCP_PORT);
            } catch(IOException e) {
                if (connection != null && connection.isConnected()) {
                    connection.close();
                }
                throw e;
            }
        }
        public void writeBytes(final byte[] buffer,
                              final int offset,
                              final int length) throws IOException {
            out.write(buffer, offset, length);
        }
        /**
        * [0] Start byte (0xFE)
        * [1] Device address (0x00)
        * [2] Command (0x00)
        * [3] Acknowledge (0x06 ACK, 0x15 NACK)
        */
        private boolean readAcknowledge() throws IOException {
            int c = in.read();
            while (c != -1) {
                if (c == 0xFE) {
                    // start reading command
                    int deviceAddress = in.read();
                    int cmd1 = in.read(); // should be 0x00
                    int cmd2 = in.read(); // is either 0x06 or 0x15
                    switch(cmd2) {
                        case 0x06:// got ACK
                            return true;
                        case 0x15:// got NACK
                            return false;
                        default:
                            return false;
                    }
                } else {
                    // read next byte
                    c = in.read();
                }
            }
            return false;
        }
        /**
         * Reads the reply from specified device.
         * [0] Start byte (0xFE)
         * [1] Device address (0x00)
         * [2] Command (0x00)
         * [3] Command (0x03)
         * [4] Reply (0x00 no success, 0x01 success)
         * @return trur if the device has completed the command
         */
        private boolean readReply() throws IOException {
            int c = in.read();
            while (c != -1) {
                if (c == 0xFE) {
                    // start reading command
                    int deviceAddress = in.read();
                    int cmd1 = in.read(); // should be 0x00
                    int cmd2 = in.read(); // should be 0x03
                    int cmd3 = in.read(); // should be 0x03
                    switch(cmd3) {
                        case 0x00:
                            return false;
                        case 0x01:
                            return true;
                        default:
                            return false;
                    }
                } else {
                    // read next byte
                    c = in.read();
                }
            }
            return false;
        }
        @Override
        public void close() throws IOException {
            connection.close();
        }
    }
    /**
     * Can't be instantiated with this ctor.
     */
    private ProjectorUtil() {
    }
}
