
package tw.gov.nmmst.app;

import java.io.File;
import java.io.IOException;
import tw.gov.nmmst.NProperties;
import tw.gov.nmmst.NodeInformation;
import tw.gov.nmmst.utils.RequestUtil;
import tw.gov.nmmst.utils.SerialStream;
import tw.gov.nmmst.views.FusionFrame;

public class EasyStartAndStop {
    public static void main(String[] args) throws InterruptedException, IOException {
        NProperties properties = new NProperties(new File(FusionFrame.class.getName()));
        NodeInformation node = NodeInformation.get(properties, NodeInformation.Location.LU).get();
        SerialStream.send(node, new RequestUtil.Request(RequestUtil.RequestType.START));
    }
}
