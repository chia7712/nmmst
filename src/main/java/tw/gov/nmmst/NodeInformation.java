package tw.gov.nmmst;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Contains the location, address and mac number for specified node which
 * includes the master, controler, fusion node and all projects.
 */
public class NodeInformation implements Comparable<NodeInformation> {

  /**
   * Builds the node information with some optional arguments.
   */
  public static final class Builder {

    /**
     * Node address.
     */
    private String ip;
    /**
     * Node mac number.
     */
    private String mac;
    /**
     * Node location.
     */
    private Location loc;
    /**
     * The port to bind for register.
     */
    private int registerPort;
    /**
     * The port to bind for request.
     */
    private int requestPort;

    /**
     * Builds a node information.
     *
     * @return Node information
     */
    public NodeInformation build() {
      return new NodeInformation(loc, ip, mac,
              requestPort, registerPort);
    }

    /**
     * @param address The node address to set
     * @return The builder itself
     */
    public Builder setAddress(final String address) {
      ip = address;
      return this;
    }

    /**
     * @param location The node location to set
     * @return The builder itself
     */
    public Builder setLocation(final Location location) {
      loc = location;
      return this;
    }

    /**
     * @param macNumber The node mac number to set
     * @return The builder itself
     */
    public Builder setMac(final String macNumber) {
      mac = macNumber;
      return this;
    }

    /**
     * @param port The node port to set for register
     * @return The builder itself
     */
    public Builder setRegisterPort(final int port) {
      registerPort = port;
      return this;
    }

    /**
     * @param port The node port to set for request
     * @return The builder itself
     */
    public Builder setRequestPort(final int port) {
      requestPort = port;
      return this;
    }
  }

  /**
   * Enumerates all valid node locations. If we face the screen, the fusion node
   * and project locations are as follows:
   * <p>
   * |--------|--------| | | | | LU | RU | | | | |--------|--------| | | | | LD
   * | RD | | | | |--------|--------|
   * <p>
   */
  public enum Location {
    /**
     * The left-up pc.
     */
    LU,
    /**
     * The right-up pc.
     */
    RU,
    /**
     * The left-down pc.
     */
    LD,
    /**
     * The right-down pc.
     */
    RD,
    /**
     * The left-up projector.
     */
    LU_P,
    /**
     * The right-up projector.
     */
    RU_P,
    /**
     * The left-down projector.
     */
    LD_P,
    /**
     * The right-down projector.
     */
    RD_P,
    /**
     * The controller pc.
     */
    CONTROLLER,
    /**
     * The master pc.
     */
    MASTER;

    /**
     * Picks up a location for specified str by comparing the location name with
     * argument.
     *
     * @param name The name to compare
     * @return The location is equal with str. Oterwise, a empty optaional class
     * returns.
     */
    public static Optional<Location> match(final String name) {
      for (Location location : Location.values()) {
        if (location.name().compareToIgnoreCase(name) == 0) {
          return Optional.of(location);
        }
      }
      return Optional.empty();
    }
  };
  /**
   * The node location.
   */
  private final Location location;
  /**
   * The node address.
   */
  private final String ip;
  /**
   * The node mac.
   */
  private final String mac;
  /**
   * Used for request server.
   */
  private final int reqPort;
  /**
   * Used for register server.
   */
  private final int regPort;

  /**
   * Lists all IPV4 addresses.
   *
   * @return All IPV4 addresses
   * @throws SocketException If failed to list all network interfaces
   */
  public static List<String> listIpV4() throws SocketException {
    List<String> addresses = new LinkedList();
    Enumeration<NetworkInterface> nets
            = NetworkInterface.getNetworkInterfaces();
    Collections
            .list(nets)
            .stream()
            .map((netint) -> netint.getInetAddresses())
            .forEach((inetAddresses) -> {
              Collections
                      .list(inetAddresses)
                      .stream()
                      .filter((inetAddress)
                              -> (inetAddress instanceof Inet4Address))
                      .forEach((inetAddress)
                              -> addresses.add(((Inet4Address) inetAddress)
                              .getHostAddress())
                      );
            });
    return addresses;
  }

  /**
   * Retrieves the node information by comparing the node address with all
   * default node inforamtions.
   *
   * @param properties NProperties
   * @return The node information
   * @throws java.net.SocketException If failed to find the IPv4 for local
   * machine
   */
  public static NodeInformation getNodeInformationByAddress(
          final NProperties properties) throws SocketException {
    List<String> allAddress = listIpV4();
    for (String address : allAddress) {
      for (NodeInformation nodeInformation
              : NProperties.stringToNodes(properties.getString(
                      NConstants.NODE_INFORMATION))) {
        if (nodeInformation.getIP().compareToIgnoreCase(address)
                == 0) {
          return nodeInformation;
        }
      }
    }
    StringBuilder builder = new StringBuilder();
    allAddress.forEach(ip -> builder.append(ip).append(" "));
    throw new RuntimeException("No suitable address : "
            + builder.toString());
  }

  /**
   * Retrieves all selectable node information.
   *
   * @param properties NProperties
   * @return All selectable node information
   */
  public static Collection<NodeInformation> getSelectableNodes(
          final NProperties properties) {
    Set<Location> locations = new TreeSet();
    locations.add(Location.LU);
    locations.add(Location.RU);
    locations.add(Location.LD);
    locations.add(Location.RD);
    locations.add(Location.MASTER);
    locations.add(Location.CONTROLLER);
    return get(properties, locations);
  }

  /**
   * Retrieves all fusion node information.
   *
   * @param properties NProperties
   * @return All fustion node information
   */
  public static Collection<NodeInformation> getFusionVideoNodes(
          final NProperties properties) {
    Set<Location> locations = new TreeSet<>();
    locations.add(Location.LU);
    locations.add(Location.RU);
    locations.add(Location.LD);
    locations.add(Location.RD);
    return get(properties, locations);
  }

  /**
   * Retrieves the controller information.
   *
   * @param properties NProperties
   * @return The controller information
   */
  public static Optional<NodeInformation> getContorllerNode(
          final NProperties properties) {
    return get(properties, Location.CONTROLLER);
  }

  /**
   * Retrieves the master information.
   *
   * @param properties NProperties
   * @return The master information
   */
  public static Optional<NodeInformation> getMasterNode(
          final NProperties properties) {
    return get(properties, Location.MASTER);
  }

  /**
   * Retrieves all fusion node inforamtions. It contains fusion node and control
   * node.
   *
   * @param properties NProperties
   * @return All fusion node inforamtions
   */
  public static Collection<NodeInformation> getVideoNodes(
          final NProperties properties) {
    Set<NodeInformation> locations = new TreeSet<>();
    getFusionVideoNodes(properties).stream().forEach((node) -> {
      locations.add(node);
    });
    get(properties, Location.CONTROLLER).ifPresent(locations::add);
    return locations;
  }

  /**
   * Retrieves all project informations.
   *
   * @param properties NProperties
   * @return All proejct informations
   */
  public static Collection<NodeInformation> getProjectors(
          final NProperties properties) {
    Set<Location> locations = new TreeSet<>();
    locations.add(Location.LU_P);
    locations.add(Location.RU_P);
    locations.add(Location.LD_P);
    locations.add(Location.RD_P);
    return get(properties, locations);
  }

  /**
   * Retrieves the node inforamtions by corresponding locations.
   *
   * @param properties NProperties
   * @param locations The locations to select the node information
   * @return A collection of node inforamtion
   */
  private static Collection<NodeInformation> get(final NProperties properties,
          final Set<Location> locations) {
    List<NodeInformation> nodeInformations
            = NProperties.stringToNodes(properties.getString(
                    NConstants.NODE_INFORMATION));
    Set<NodeInformation> rval = new TreeSet<>();
    for (NodeInformation nodeInformation : nodeInformations) {
      for (Location location : locations) {
        if (location == nodeInformation.getLocation()) {
          rval.add(nodeInformation);
          break;
        }
      }
    }
    return rval;
  }

  /**
   * Retrieves the node inforamtions by corresponding locations.
   *
   * @param properties NProperties
   * @param location The locations to select the node information
   * @return The node information
   */
  public static Optional<NodeInformation> get(final NProperties properties,
          final Location location) {
    List<NodeInformation> nodeInformations
            = NProperties.stringToNodes(properties.getString(
                    NConstants.NODE_INFORMATION));
    for (NodeInformation nodeInformation : nodeInformations) {
      if (nodeInformation.getLocation() == location) {
        return Optional.of(nodeInformation);
      }
    }
    return Optional.empty();
  }

  /**
   * Retrieves the broadcast address with non-loop network. The runtime
   * excpetion will be thrown if there are more than one network interface in
   * this machine.
   *
   * @param nodeInforation NodeInformation
   * @return The broadcast address
   * @throws SocketException If failed to get the network interface
   */
  public static String getBroadCast(final NodeInformation nodeInforation)
          throws SocketException {
    Enumeration<NetworkInterface> interfaces
            = NetworkInterface.getNetworkInterfaces();
    List<String> address = new LinkedList();
    while (interfaces.hasMoreElements()) {
      NetworkInterface networkInterface = interfaces.nextElement();
      if (networkInterface.isLoopback()) {
        continue;
      }
      networkInterface.getInterfaceAddresses()
              .stream()
              .filter(interfaceAddress -> {
                return interfaceAddress.getAddress()
                        .getHostAddress().equals(nodeInforation.getIP());
              })
              .map((interfaceAddress) -> interfaceAddress.getBroadcast())
              .filter((broadcast) -> broadcast != null)
              .forEach((broadcast) -> {
                address.add(broadcast.getHostAddress());
              });
    }
    if (address.size() == 1) {
      return address.get(0);
    } else if (address.isEmpty()) {
      throw new RuntimeException("No valid broadcast address");
    } else {
      throw new RuntimeException(
              "The expected number of network interface is 1"
              + ", but there are " + address.size());
    }
  }

  /**
   * Constructs a node information with specified location, address and mac
   * number.
   *
   * @param nodeLocation The node location
   * @param nodeAddress The node address
   * @param nodeMacNo The node mac number
   * @param requestPort The port to bind in request server
   * @param registerPort The port to bind in register server
   */
  public NodeInformation(final Location nodeLocation,
          final String nodeAddress,
          final String nodeMacNo,
          final int requestPort,
          final int registerPort) {
    location = nodeLocation;
    ip = nodeAddress;
    mac = nodeMacNo;
    reqPort = requestPort;
    regPort = registerPort;
  }

  /**
   * Retrieves the node location.
   *
   * @return The node location
   */
  public final Location getLocation() {
    return location;
  }

  /**
   * Retrieves the node address.
   *
   * @return The node address
   */
  public final String getIP() {
    return ip;
  }

  /**
   * Retrieves the node mac number.
   *
   * @return The node mac number
   */
  public final String getMac() {
    return mac;
  }

  /**
   * @return Request port
   */
  public final int getRequestPort() {
    return reqPort;
  }

  /**
   * @return Register port
   */
  public final int getRegisterPort() {
    return regPort;
  }

  @Override
  public final boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    NodeInformation nodeInformation = (NodeInformation) obj;
    return nodeInformation.location == location
            && nodeInformation.ip.compareToIgnoreCase(ip) == 0
            && nodeInformation.mac.compareToIgnoreCase(mac) == 0;
  }

  @Override
  public final String toString() {
    return NProperties.nodeToString(this);
  }

  @Override
  public final int hashCode() {
    return toString().hashCode();
  }

  @Override
  public final int compareTo(final NodeInformation other) {
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
