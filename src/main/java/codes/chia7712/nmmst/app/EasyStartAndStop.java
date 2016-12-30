package codes.chia7712.nmmst.app;

import java.io.File;
import java.io.IOException;
import codes.chia7712.nmmst.NProperties;
import codes.chia7712.nmmst.NodeInformation;
import codes.chia7712.nmmst.utils.RequestUtil;
import codes.chia7712.nmmst.utils.SerialStream;
import codes.chia7712.nmmst.views.FusionFrame;

public class EasyStartAndStop {

  public static void main(String[] args) throws InterruptedException, IOException {
    NProperties properties = new NProperties(new File(FusionFrame.class.getName()));
    NodeInformation node = NodeInformation.get(properties, NodeInformation.Location.LU).get();
    SerialStream.send(node, new RequestUtil.Request(RequestUtil.RequestType.START));
  }
}
