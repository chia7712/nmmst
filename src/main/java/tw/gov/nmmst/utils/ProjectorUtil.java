package tw.gov.nmmst.utils;

import tw.gov.nmmst.NProperties;
import tw.gov.nmmst.NodeInformation;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility methods helps for waking specified project up.
 */
public final class ProjectorUtil {

  /**
   * Log.
   */
  private static final Log LOG
          = LogFactory.getLog(ProjectorUtil.class);
  /**
   * TCP port.
   */
  private static final int TCP_PORT = 0xAAA0;
  /**
   * UDP port.
   */
  private static final int UDP_PORT = 0xA001;
  /**
   * Packet data.
   */
  private static final byte[] PACKET_DATA = {0x3F};

  /**
   * Enables/Disables all machines.
   *
   * @param properties NProperties provides the projector inforamtion
   * @param enable Indicates the enable/disable flag
   * @return {@code true} if sending succeedfually
   * @throws IOException If failed to send packet
   */
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

  /**
   * Enables/Disables a machine.
   *
   * @param address Remote node's address
   * @param enable Indicates the enable/disable flag
   * @return {@code true} if sending succeedfually
   * @throws IOException If failed to send packet
   */
  public static boolean enableMachine(final String address,
          final boolean enable) throws IOException {
    try (ProjectorConnection connection
            = new ProjectorConnection(address)) {
      byte[][] data = {
        getEnableLampCommand(enable),
        generateProjectorCommand(enable)
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

  /**
   * Discovers the device inforamtion.
   *
   * @param nodeInforamtion NodeInformation provides the broadcast
   * @return Projector information
   * @throws IOException If failed to manipulate socket
   */
  public static String discoverDevices(final NodeInformation nodeInforamtion)
          throws IOException {
    try (DatagramSocket socket = new DatagramSocket()) {
      InetAddress address = InetAddress.getByName(
              NodeInformation.getBroadCast(nodeInforamtion));
      socket.send(new DatagramPacket(
              PACKET_DATA,
              PACKET_DATA.length,
              address, UDP_PORT));
      DatagramPacket recvPacket = new DatagramPacket(
              new byte[1024],
              1024,
              address,
              UDP_PORT);
      socket.receive(recvPacket);
      return new String(recvPacket.getData(),
              recvPacket.getOffset(),
              recvPacket.getLength());
    }
  }

  /**
   * Generates the projector command.
   *
   * @param enable Enable/disable flag
   * @return A bytes array of command
   */
  private static byte[] generateProjectorCommand(final boolean enable) {
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
    if (enable) {
      buffer[5] = (byte) 0x65;
    } else {
      buffer[5] = (byte) 0x66;
    }
    buffer[6] = generateChecksum(buffer);
    buffer[0] = (byte) 0xFE;
    buffer[7] = (byte) 0xFF;
    return buffer;
  }

  /**
   * Generates the lamp command.
   *
   * @param enable Enable/disable flag
   * @return A bytes array of command
   */
  private static byte[] getEnableLampCommand(final boolean enable) {
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
    if (enable) {
      buffer[7] = (byte) 0x01;
    } else {
      buffer[7] = (byte) 0x00;
    }
    buffer[8] = generateChecksum(buffer);
    buffer[0] = (byte) 0xFE;
    buffer[9] = (byte) 0xFF;
    return buffer;
  }

  /**
   * Generates checksum for specified data.
   *
   * @param data The data need the checksum
   * @return Checksum, a byte
   */
  private static byte generateChecksum(final byte[] data) {
    int sum = 0;
    for (byte b : data) {
      sum += b;
    }
    return (byte) (sum % 256);
  }

  /**
   * Encapsulates the details of connection with projects.
   */
  private static class ProjectorConnection implements Closeable {

    /**
     * A socket connection.
     */
    private final Socket connection;
    /**
     * Output stream.
     */
    private final OutputStream out;
    /**
     * Input stream.
     */
    private final InputStream in;

    /**
     * Constructs a projector connection.
     *
     * @param address Projector address
     * @throws IOException If failed to connection projector
     */
    ProjectorConnection(final String address) throws IOException {
      connection = new Socket(address, TCP_PORT);
      try {
        out = connection.getOutputStream();
        in = connection.getInputStream();
        LOG.info("connect success : " + address + ":" + TCP_PORT);
      } catch (IOException e) {
        if (connection != null && connection.isConnected()) {
          connection.close();
        }
        throw e;
      }
    }

    /**
     * Writrs bytes array to remote projector.
     *
     * @param buffer The data
     * @param offset The start offset in the data
     * @param length The number of bytes to write
     * @throws IOException If an I/O error occurs. In particular, an
     * <code>IOException</code> is thrown if the output stream is closed
     */
    void writeBytes(final byte[] buffer, final int offset,
            final int length) throws IOException {
      out.write(buffer, offset, length);
    }

    /**
     * Reads the acknowledge from projector. [0] Start byte (0xFE) [1] Device
     * address (0x00) [2] Command (0x00) [3] Acknowledge (0x06 ACK, 0x15 NACK)
     *
     * @return <code>true</code> if previouse command is success. Otherwise,
     * <code>false</code> returns
     * @throws IOException If an I/O error occurs. In particular, an
     * <code>IOException</code> is thrown if the input stream is closed
     */
    boolean readAcknowledge() throws IOException {
      int c = in.read();
      while (c != -1) {
        if (c == 0xFE) {
          // start reading command
          int deviceAddress = in.read();
          int cmd1 = in.read(); // should be 0x00
          int cmd2 = in.read(); // is either 0x06 or 0x15
          switch (cmd2) {
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
     * Reads the reply from specified device. [0] Start byte (0xFE) [1] Device
     * address (0x00) [2] Command (0x00) [3] Command (0x03) [4] Reply (0x00 no
     * success, 0x01 success)
     *
     * @return trur if the device has completed the command
     * @throws IOException If an I/O error occurs. In particular, an
     * <code>IOException</code> is thrown if the input stream is closed
     */
    boolean readReply() throws IOException {
      int c = in.read();
      while (c != -1) {
        if (c == 0xFE) {
          // start reading command
          int deviceAddress = in.read();
          int cmd1 = in.read(); // should be 0x00
          int cmd2 = in.read(); // should be 0x03
          int cmd3 = in.read(); // should be 0x03
          switch (cmd3) {
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
