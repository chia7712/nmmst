package tw.gov.nmmst.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tw.gov.nmmst.NProperties;
import tw.gov.nmmst.NodeInformation;
import tw.gov.nmmst.utils.ProjectorUtil;
import tw.gov.nmmst.utils.RequestUtil;
import tw.gov.nmmst.utils.SerialStream;
import tw.gov.nmmst.utils.WolUtil;

public class FusionOp {

  /**
   * Log.
   */
  private static final Log LOG
          = LogFactory.getLog(FusionOp.class);

  public static void main(String[] args) throws SocketException, InterruptedException, IOException {
    if (args.length != 1) {
      LOG.info("Usage: <wakeupC/wakeupP/sleepP/play/stop>");
      System.exit(0);
    }
    NProperties properties = new NProperties();
    Collection<NodeInformation> videoNodes = NodeInformation.getFusionVideoNodes(properties);
    NodeInformation selfInformation = NodeInformation.getNodeInformationByAddress(properties);
    String cmd = args[0];
    if (cmd.equalsIgnoreCase("wakeupc")) {
      for (NodeInformation nodeInformation : videoNodes) {
        LOG.info(nodeInformation);
        WolUtil.wakeup(new InetSocketAddress(
                selfInformation.getIP(), 0),
                NodeInformation.getBroadCast(selfInformation),
                nodeInformation.getMac());
      }
    } else if (cmd.equalsIgnoreCase("play")) {
      SerialStream.sendAll(videoNodes,
              new RequestUtil.Request(RequestUtil.RequestType.START),
              true);
    } else if (cmd.equalsIgnoreCase("stop")) {
      SerialStream.sendAll(videoNodes,
              new RequestUtil.Request(RequestUtil.RequestType.STOP),
              false);
    } else if (cmd.equalsIgnoreCase("wakeupP")) {
      ProjectorUtil.enableAllMachine(properties, true);
    } else if (cmd.equalsIgnoreCase("sleepP")) {
      ProjectorUtil.enableAllMachine(properties, false);
    } else {
      throw new RuntimeException("Nonsupported cmd : " + cmd);
    }
  }
}
