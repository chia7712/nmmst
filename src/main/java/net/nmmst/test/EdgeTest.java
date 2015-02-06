package net.nmmst.test;

import java.io.IOException;
import net.nmmst.player.PlayerInformation;
import net.nmmst.processor.LinearProcessor;
import net.nmmst.request.Request;
import net.nmmst.tools.Ports;
import net.nmmst.tools.SerialStream;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class EdgeTest {
	private	static final LinearProcessor.Format defaultFormat = new LinearProcessor.Format(
            0.05,
            0.1235,
            0.6,
            0.9,
            0.6,
            0.9
	);
	private	static final PlayerInformation[] playerInformations = new PlayerInformation[]{
            new PlayerInformation(PlayerInformation.Location.LU, "192.168.100.1",  "00-0B-AB-6D-7D-25"),
            new PlayerInformation(PlayerInformation.Location.RU, "192.168.100.2",  "00-0B-AB-67-4E-83"),
            new PlayerInformation(PlayerInformation.Location.LD, "192.168.100.3",  "00-0B-AB-67-4E-70"),
            new PlayerInformation(PlayerInformation.Location.RD, "192.168.100.4",  "00-0B-AB-67-4E-75")
	};
	public static void main(String[] args) throws InterruptedException, IOException {
            SerialStream.sendAll(playerInformations, new Request(Request.Type.REBOOT), Ports.REQUEST.get());
//		SerialStream.sendAll(playerInformations, new Boolean(true), Ports.TEST.get());
		///SerialStream.sendAll(playerInformations, new Request(Request.Type.TEST_2, getFormat(args)), Ports.REQUEST.get());
		//SerialStream.sendAll(playerInformations, defaultFormat, Ports.TEST.get());
//		BufferedImage testImage 	= Painter.fillColor(1920, 1080, Color.WHITE);
//		PlayerInformation player = new PlayerInformation(PlayerInformation.Location.LU, 	 "192.168.100.1",  "00-0B-AB-6D-7D-25");
//		BourkeProcessor processor = new BourkeProcessor(player.getLocation(), defaultFormat);
//		processor.process(testImage);
		System.out.println("ok");
	}
	private static LinearProcessor.Format getFormat(String[] args) {
            if(args.length != 6) {
                return null;
            }
            return new LinearProcessor.Format(
                Double.valueOf(args[0]),
                Double.valueOf(args[1]),
                Double.valueOf(args[2]),
                Double.valueOf(args[3]),
                Double.valueOf(args[4]),
                Double.valueOf(args[5]));
        }
	private static double normalized(double value, double max, double min) {
		//return value;
            return (value - min) / (max - min);
	}
	private static double rightEquation(double value, double curvature, double gamma) {
            if(value <= 0.5 || value >= 1) {
                  throw new IllegalArgumentException();
            }
            return 1 - (1 - gamma) * Math.pow((2 * (1 - value)), curvature);
	}
	private static double leftEquation(double value, double curvature, double gamma) {
            if(value < 0 || value > 0.5) {
                throw new IllegalArgumentException();
            } 
            return gamma * Math.pow(2 * value, curvature);
	}

}
