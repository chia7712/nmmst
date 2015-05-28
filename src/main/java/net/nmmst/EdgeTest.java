package net.nmmst;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.nmmst.processor.LinearProcessor;
import net.nmmst.utils.RequestUtil;
import net.nmmst.utils.SerialStream;

public class EdgeTest {
	private	static final LinearProcessor.Factor TENTATIVE_FACTOR
            = new LinearProcessor.Factor(
            0.0335,
            0.118,
            0.6,
            0.9,
            0.6,
            0.9
	);
	private	static final List<NodeInformation> TENTATIVE_INFORMATIONS
            = Arrays.asList(
            new NodeInformation(
                NodeInformation.Location.LU,
                "192.168.100.1", 
                "00-0B-AB-6D-7D-25",
                9999, 10000),
            new NodeInformation(
                NodeInformation.Location.RU,
                "192.168.100.2", 
                "00-0B-AB-67-4E-83",
                9999, 10000),
            new NodeInformation(
                NodeInformation.Location.LD,
                "192.168.100.3", 
                "00-0B-AB-67-4E-70",
                9999, 10000),
            new NodeInformation(
                NodeInformation.Location.RD,
                "192.168.100.4", 
                "00-0B-AB-67-4E-75",
                9999, 10000)
        );
	public static void main(String[] args)
                throws InterruptedException, IOException {
            NProperties properties = new NProperties();
            if (args.length == 6) {
                SerialStream.sendAll(
                    TENTATIVE_INFORMATIONS,
                    new RequestUtil.FusionTestRequest(null, getFormat(args)));
            } else if (args.length == 7) {
                switch (args[0].toLowerCase()) {
                    case "lu":
                        SerialStream.send(
                            TENTATIVE_INFORMATIONS.get(0),
                            new RequestUtil.FusionTestRequest(null, getFormat(args)),
                            properties);
                        break;
                    case "ru":
                        SerialStream.send(
                            TENTATIVE_INFORMATIONS.get(1),
                            new RequestUtil.FusionTestRequest(null, getFormat(args)),
                            properties);
                        break;
                    case "ld":
                        SerialStream.send(
                            TENTATIVE_INFORMATIONS.get(2),
                            new RequestUtil.FusionTestRequest(null, getFormat(args)),
                            properties);
                        break;
                    case "rd":
                        SerialStream.send(
                            TENTATIVE_INFORMATIONS.get(3),
                            new RequestUtil.FusionTestRequest(null, getFormat(args)),
                            properties);
                        break;
                    default:
                        System.out.println("Only for lu, ru, ld and rd");
                        break;
                }
            }
	}
	private static LinearProcessor.Factor getFormat(String[] args) {
            if (args.length == 6) {
                return new LinearProcessor.Factor(
                    Double.valueOf(args[0]),
                    Double.valueOf(args[1]),
                    Double.valueOf(args[2]),
                    Double.valueOf(args[3]),
                    Double.valueOf(args[4]),
                    Double.valueOf(args[5]));
            } else if (args.length == 7) {
                return new LinearProcessor.Factor(
                    Double.valueOf(args[1]),
                    Double.valueOf(args[2]),
                    Double.valueOf(args[3]),
                    Double.valueOf(args[4]),
                    Double.valueOf(args[5]),
                    Double.valueOf(args[6]));
            } else {
                return null;
            }
        }
}
