/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.nmmst.tools;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import net.nmmst.player.NodeInformation;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class ProjectorUtil {
    private static final int TCP_PORT = 0xAAA0;
    private static final int UDP_PORT = 0xA001;
//    private static final String BROADCAST = "192.168.100.255";
    private static final byte[] PACKET_DATA = {0x3F};
//    private static final String[] MACHINE_ADDRESSES = {
//        "192.168.100.11"
//        , "192.168.100.12"
//        , "192.168.100.13"
//        , "192.168.100.14"
//    };
    public static boolean[] switchAllMachine(boolean enable) throws IOException {
        List<NodeInformation> nodeInformations = NodeInformation.getProjectors();
        boolean[] rval = new boolean[nodeInformations.size()];
        int index = 0;
        for (NodeInformation projector : nodeInformations) {
            rval[index++] = switchMachine(projector.getIP(), enable);
        }
        return rval;
    } 
    public static boolean switchMachine(String address, boolean enable) throws IOException {
        try (ProjectorConnection connection = new ProjectorConnection(address)) {
            byte[][] data = {
              getEnableLampCommand(enable),
              getEnableProjectorCommand(enable)
            };
            for (byte[] d : data) {
                connection.writeBytes(d, 0, d.length);
                if (!connection.readAcknowledge() || !connection.readAnswerResult()) {
                    return false;
                }
            }
            return true;
        }
    }
    public static String discoverDevices() throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName(NodeInformation.getBroadCast());
            socket.send(new DatagramPacket(PACKET_DATA, PACKET_DATA.length, address, UDP_PORT));
            DatagramPacket recvPacket = new DatagramPacket(new byte[1024], 1024, address, UDP_PORT);
            socket.receive(recvPacket);
            return new String(recvPacket.getData(), recvPacket.getOffset(), recvPacket.getLength());
        }
    }
     private static byte[] getEnableProjectorCommand(boolean enable) {
        byte[] buffer = new byte[8];
        buffer[1] = 0x00; // device address
        buffer[2] = 0x00; // answer prefix (1)
        buffer[3] = 0x03; // answer prefix (2)
        buffer[4] = 0x02; // answer prefix data (send result)
        buffer[5] = (byte) (enable ? 0x65 : 0x66); // data byte (0x66 switch projector off, 0x65 switch projector on)
        buffer[6] = generateChecksum(buffer);
        buffer[0] = (byte) 0xFE;
        buffer[7] = (byte) 0xFF;
        return buffer;
    }
    private static byte[] getEnableLampCommand(boolean enable) {
        byte[] buffer = new byte[10];
        buffer[1] = 0x00; // device address
        buffer[2] = 0x00; // answer prefix (1)
        buffer[3] = 0x03; // answer prefix (2)
        buffer[4] = 0x02; // answer prefix data (send result)
        buffer[5] = 0x76; // command byte (1)
        buffer[6] = 0x1A; // command byte (2)
        buffer[7] = (byte) (enable ? 0x01 : 0x00); // data byte (0x00 switch lamp off, 0x01 switch lamp on)
        buffer[8] = generateChecksum(buffer);
        buffer[0] = (byte) 0xFE;
        buffer[9] = (byte) 0xFF;
        return buffer;
    }
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
            System.out.println("connect..." + address + ":" + TCP_PORT);
            connection = new Socket(address, TCP_PORT);
            System.out.println("connect...over");
            try {
                out = connection.getOutputStream();
                in = connection.getInputStream();
            } catch(IOException e) {
                if (connection != null && connection.isConnected()) {
                    connection.close();
                }
                throw e;
            }
        }
        public void writeBytes(byte[] buffer, int offset, int length) throws IOException {
            out.write(buffer, offset, length);
        }
        /**
        * [0] Start byte (0xFE)
        * [1] Device address (0x00)
        * [2] Command (0x00)
        * [3] Acknowledge (0x06 ACK, 0x15 NACK)
        */
        public boolean readAcknowledge() throws IOException {
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
        * [0] Start byte (0xFE)
        * [1] Device address (0x00)
        * [2] Command (0x00)
        * [3] Command (0x03)
        * [4] Reply (0x00 no success, 0x01 success)
        */
        public boolean readAnswerResult() throws IOException {
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
}
